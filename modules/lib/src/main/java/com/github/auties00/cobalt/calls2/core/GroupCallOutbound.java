package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.calls2.core.participant.CallMembership;
import com.github.auties00.cobalt.calls2.core.participant.CallParticipantUserNode;
import com.github.auties00.cobalt.calls2.platform.VoipHostApi;
import com.github.auties00.cobalt.calls2.signaling.Calls2CallStanza;
import com.github.auties00.cobalt.calls2.signaling.GroupInfoStanza;
import com.github.auties00.cobalt.calls2.signaling.GroupUpdateStanza;
import com.github.auties00.cobalt.calls2.signaling.TerminateStanza;
import com.github.auties00.cobalt.model.call.CallEndReason;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.List;
import java.util.Objects;

/**
 * Owns the outbound mid-call membership traffic of a single group call: the per-peer offer-send
 * timestamps, the unanswered-offer sweep, the per-peer terminate fanout, and the {@code <group_update>}
 * add/remove build.
 *
 * <p>This is the engine's outbound-group-call unit, one instance per live group call, held by the
 * {@link Calls2LifecycleController} for the call's lifetime and dropped on teardown. Where the controller
 * owns the offer, accept, and rekey legs, this unit owns the mid-call roster traffic the controller does
 * not: it marks each peer device the moment an offer or rekey is fanned to it (recording the offer-send
 * timestamp on that device's {@link CallMembership.CallMembershipSlot membership slot}), clears the mark
 * when the peer connects, sweeps the roster on every watchdog tick and terminates any peer whose offer has
 * gone unanswered past the {@link Calls2CallTimers#UNANSWERED_GROUP_OFFER_TIMEOUT} cutoff, and builds the
 * {@code <group_update>} the host sends to add or remove participants, reconciling the call's membership as
 * it does.
 *
 * <p>The per-peer offer-send timestamp lives on the membership slot rather than on the participant
 * identity, so it survives a {@link CallMembership#reconcile(GroupInfoStanza) reconcile} that replaces a
 * present member's identity in place; this unit is the sole writer of that timestamp. The sweep reads each
 * slot's timestamp, and a peer whose elapsed time exceeds the cutoff is terminated through a per-peer
 * {@link TerminateStanza} (one terminate per device) shipped fire-and-forget on the host, after which the
 * peer's timestamp is cleared so it is terminated once rather than on every subsequent tick.
 *
 * <p>This unit is reached from two threads: the controller's call threads, which hold the call's
 * orchestration lock when they drive {@link #sendGroupParticipants(Jid, Jid, List, boolean)},
 * {@link #markOfferSent(Jid)}, {@link #fanOfferSent(List)}, and {@link #clearConnectedOffers()}, and the
 * watchdog driver thread, which drives {@link #sweepUnansweredOffers()} holding no call lock. The shared
 * state is the membership slots, whose offer-send timestamps are {@code volatile}, and the membership
 * manager's own internal lock, so the sweep reads a consistent slot snapshot without taking the call lock.
 *
 * <p>This is an internal engine collaborator, not a public surface; the {@link Calls2LifecycleController} is
 * its only caller, constructing one unit per group call and dropping it on teardown.
 *
 * @implNote This implementation reproduces the outbound-group-call portion of {@code call_lifecycle.cc} in
 * the wa-voip WASM module {@code ff-tScznZ8P}: the per-participant unanswered-offer sweep of
 * {@code periodic_call_timer_callback} (fn10932, {@code call_lifecycle.cc:8227}, the
 * {@code (char)participant[4] == 0 && *offer_send_t != 0 && 44999 < elapsed} guard that batches the matching
 * participant ids and terminates them through {@code invoke_iiii(&DAT_ram_00001ced, ...)}), the
 * {@code <group_update>} membership-update build (message type 17, {@link GroupUpdateStanza}) the host
 * dispatches to add or remove participants, and the per-peer terminate fanout
 * ({@code make_and_send_terminate_with_reason_str}, fn11456) one terminate per device. The native sweep
 * gates on the participant's connected sub-state ({@code (participant[2] & ~1) == 2}); the load-bearing gate
 * is the per-participant offer-send timestamp ({@code *offer_send_t != 0}), which is set only while an offer
 * is outstanding and cleared on connect, so this port gates the sweep on the slot's offer-send timestamp.
 */
final class GroupCallOutbound {
    /**
     * Logs the membership-update fanout, the unanswered-offer sweep, and the per-peer terminations.
     */
    private static final System.Logger LOGGER = System.getLogger(GroupCallOutbound.class.getName());

    /**
     * The roster {@code state} attribute literal a connected group participant carries.
     *
     * <p>A participant whose roster {@code state} is this literal has connected, so this unit clears its
     * outstanding-offer marker; the value is the {@code "connected"} entry of the engine's
     * seven-entry server-user-state table.
     */
    private static final String CONNECTED_STATE_LITERAL = "connected";

    /**
     * The identifier of the group call this unit serves.
     */
    private final String callId;

    /**
     * The local device JID, stamped as the call creator on a membership update and a per-peer terminate,
     * and excluded from the roster so the local participant is never terminated or sent an update about
     * itself.
     */
    private final Jid self;

    /**
     * The membership manager whose slots carry the per-peer offer-send timestamps this unit sets, clears,
     * and sweeps, and which it reconciles on an add or remove.
     */
    private final CallMembership membership;

    /**
     * The host API the per-peer terminate fanout and the {@code <group_update>} membership update ship on,
     * fire-and-forget.
     */
    private final VoipHostApi host;

    /**
     * The unanswered-offer cutoff, in milliseconds: a peer whose offer has been outstanding at least this
     * long is terminated.
     *
     * <p>This is {@link Calls2CallTimers#UNANSWERED_GROUP_OFFER_TIMEOUT} in milliseconds. The sweep
     * terminates a peer once its elapsed time reaches this cutoff ({@code elapsed >= 45000}), the
     * integer-millisecond equivalent of the engine's {@code 44999 < elapsed} test.
     */
    private final long unansweredOfferCutoffMillis;

    /**
     * Constructs an outbound-group-call unit for one group call.
     *
     * @param callId     the identifier of the group call
     * @param self       the local device JID, stamped as the call creator and excluded from the roster
     * @param membership the call's membership manager, whose slots carry the offer-send timestamps
     * @param host       the host API the terminate fanout and group update ship on
     * @throws NullPointerException if {@code callId}, {@code self}, {@code membership}, or {@code host} is
     *                              {@code null}
     */
    GroupCallOutbound(String callId, Jid self, CallMembership membership, VoipHostApi host) {
        this.callId = Objects.requireNonNull(callId, "callId cannot be null");
        this.self = Objects.requireNonNull(self, "self cannot be null");
        this.membership = Objects.requireNonNull(membership, "membership cannot be null");
        this.host = Objects.requireNonNull(host, "host cannot be null");
        this.unansweredOfferCutoffMillis = Calls2CallTimers.UNANSWERED_GROUP_OFFER_TIMEOUT.toMillis();
    }

    /**
     * Builds and ships a {@code <group_update>} adding or removing the given participants on this group
     * call, reconciling the call's membership against the change.
     *
     * <p>The update carries the call header and a {@code <group_info>} roster of the affected participants
     * with the add-or-remove intent, wrapped in a {@code <call>} envelope addressed to {@code target} and
     * shipped fire-and-forget on the host. The call's {@link CallMembership} is reconciled against the
     * change: an add reconciles the affected participants into the roster (allocating a slot per new
     * member), and a remove drops each affected member's slot. On an add the newly rostered members start
     * with no outstanding offer; the per-participant key fanout and its offer-send marking are driven by the
     * controller's rekey path, not here.
     *
     * @implNote This implementation builds the {@code <group_update>} of message type 17
     * ({@link GroupUpdateStanza}) carrying a {@code <group_info>} roster ({@link GroupInfoStanza}) of the
     * affected participants and reconciles the membership the same way the inbound
     * {@code post_process_group_info} (fn10987) path does, so an add and a remove keep the call's roster in
     * step with the update. The native engine carries the add-or-remove intent in the roster framing rather
     * than as a distinct attribute; this build rosters the affected participants and reconciles a remove by
     * dropping their slots directly, because a {@code <group_update>} roster is a membership snapshot the
     * receiver diffs rather than an explicit add/remove verb.
     *
     * @param target       the group call target JID the update is addressed to
     * @param creator      the call-creator JID stamped on the update
     * @param participants the participant user JIDs to add or remove; must be non-empty
     * @param added        {@code true} to add the participants, {@code false} to remove them
     * @throws NullPointerException     if {@code target}, {@code creator}, or {@code participants} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code participants} is empty
     */
    void sendGroupParticipants(Jid target, Jid creator, List<Jid> participants, boolean added) {
        Objects.requireNonNull(target, "target cannot be null");
        Objects.requireNonNull(creator, "creator cannot be null");
        Objects.requireNonNull(participants, "participants cannot be null");
        if (participants.isEmpty()) {
            throw new IllegalArgumentException("participants cannot be empty");
        }
        var entries = participants.stream()
                .map(participant -> CallParticipantUserNode.ofUser(participant).toNode())
                .toList();
        var roster = GroupInfoStanza.ofUsers(null, -1, entries);
        var update = new GroupUpdateStanza(callId, creator, null, false, false, roster, List.of());
        host.sendSignaling(Calls2CallStanza.toCall(update, target, callId));
        reconcileLocalMembership(participants, added);
    }

    /**
     * Marks every device of every participant in the given roster as having an offer outstanding, recording
     * the current time as their offer-send timestamp.
     *
     * <p>Called by the controller as it fans the offer or rekey out to the connected roster, so the
     * unanswered-offer sweep can later terminate a peer that never connects. A participant the roster lists
     * with no membership slot (a participant not yet reconciled) is skipped, since the timestamp lives on
     * the slot; the offer is re-marked on the next fanout once the slot exists.
     *
     * @param recipients the participant device JIDs the offer or rekey was fanned to
     * @throws NullPointerException if {@code recipients} is {@code null}
     */
    void fanOfferSent(List<Jid> recipients) {
        Objects.requireNonNull(recipients, "recipients cannot be null");
        for (var device : recipients) {
            markOfferSent(device);
        }
    }

    /**
     * Marks the participant owning the given device JID as having an offer outstanding, recording the
     * current time as its offer-send timestamp.
     *
     * <p>Resolves the device's owning membership slot and stamps it with the current wall-clock time, unless
     * the participant is already reported connected, in which case the fanout is a key rotation rather than
     * an unanswered offer and the slot is left unmarked. A device JID that resolves to no slot is ignored,
     * so a stray or stale device leaves the roster unchanged.
     *
     * @param deviceJid the participant device JID an offer or rekey was fanned to
     * @throws NullPointerException if {@code deviceJid} is {@code null}
     */
    void markOfferSent(Jid deviceJid) {
        Objects.requireNonNull(deviceJid, "deviceJid cannot be null");
        membership.findByDeviceJid(deviceJid).ifPresent(slot -> {
            if (!isConnected(slot.identity())) {
                slot.offerSendMillis(System.currentTimeMillis());
            }
        });
    }

    /**
     * Clears the outstanding-offer marker of every participant the roster now reports connected.
     *
     * <p>Called by the controller after a membership reconcile so a peer that connected stops being a
     * sweep candidate: a member whose roster {@code state} is {@code "connected"} has its offer-send
     * timestamp cleared. A member that is not yet connected keeps its outstanding-offer marker, so a peer
     * that never connects is still terminated by the sweep once the cutoff elapses.
     */
    void clearConnectedOffers() {
        for (var slot : membership.slots()) {
            if (isConnected(slot.identity())) {
                slot.offerSendMillis(CallMembership.CallMembershipSlot.NO_OUTSTANDING_OFFER);
            }
        }
    }

    /**
     * Sweeps the roster for peers whose offer has gone unanswered past the cutoff and terminates each.
     *
     * <p>Walks every membership slot and, for a slot carrying an offer-send timestamp whose elapsed time
     * exceeds the {@link Calls2CallTimers#UNANSWERED_GROUP_OFFER_TIMEOUT} cutoff, fans a per-peer
     * {@link TerminateStanza} (one terminate per device) to that peer with {@link CallEndReason#TIMEOUT} and
     * clears the peer's timestamp so it is terminated once rather than on every subsequent tick. A slot with
     * no outstanding offer ({@link CallMembership.CallMembershipSlot#NO_OUTSTANDING_OFFER}) is skipped. This
     * runs on the watchdog driver thread holding no call lock; the membership manager's own lock guards the
     * slot snapshot, and each slot's timestamp is {@code volatile}.
     *
     * @implNote This implementation reproduces the {@code periodic_call_timer_callback} (fn10932)
     * unanswered-offer sweep at {@code call_lifecycle.cc:8227}: a participant whose offer-send timestamp is
     * non-zero and whose elapsed time satisfies the native {@code 44999 < elapsed} guard is collected and
     * terminated, and its timestamp zeroed ({@code *offer_send_t = 0}). With the cutoff held as a
     * forty-five-second {@link Calls2CallTimers#UNANSWERED_GROUP_OFFER_TIMEOUT}, the equivalent
     * integer-millisecond test is {@code elapsed >= 45000}. The native batch terminate
     * ({@code invoke_iiii(&DAT_ram_00001ced, ctx, participant_ids, count)}) groups the matching participant
     * ids into a single call; this port fans one {@link TerminateStanza} per peer device, the per-device
     * terminate shape of {@code make_and_send_terminate_with_reason_str} (fn11456), because the host
     * signaling seam ships one stanza per recipient.
     */
    void sweepUnansweredOffers() {
        var now = System.currentTimeMillis();
        for (var slot : membership.slots()) {
            var sentAt = slot.offerSendMillis();
            if (sentAt == CallMembership.CallMembershipSlot.NO_OUTSTANDING_OFFER) {
                continue;
            }
            // The native guard is `44999 < elapsed`; with the cutoff held as 45000 ms, the equivalent
            // integer-millisecond test is `elapsed >= 45000`, so a peer is terminated the moment its
            // elapsed time reaches the cutoff rather than strictly exceeds it.
            if (now - sentAt < unansweredOfferCutoffMillis) {
                continue;
            }
            terminatePeer(slot.identity());
            slot.offerSendMillis(CallMembership.CallMembershipSlot.NO_OUTSTANDING_OFFER);
        }
    }

    /**
     * Terminates one peer by fanning a per-device {@link TerminateStanza} to each of its devices.
     *
     * <p>Each device of the peer receives its own {@code <call to="<deviceJid>"><terminate>} carrying
     * {@link CallEndReason#TIMEOUT} and naming the device in its {@code <destination>} fanout, shipped
     * fire-and-forget on the host. A peer the roster lists with no device contributes no terminate.
     *
     * @param identity the unanswered peer's roster identity
     */
    private void terminatePeer(CallParticipantUserNode identity) {
        for (var device : identity.devices()) {
            var deviceJid = device.jid();
            var terminate = TerminateStanza.of(callId, self, CallEndReason.TIMEOUT, List.of(deviceJid));
            host.sendSignaling(Calls2CallStanza.toCall(terminate, deviceJid, callId));
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Terminated unanswered group-call peer {0} on call {1}", deviceJid, callId);
        }
    }

    /**
     * Reconciles the call's membership against an add or remove of the affected participants.
     *
     * <p>An add allocates a slot for each affected participant that has none yet (a fresh
     * {@code <user>}-form identity); a remove drops each affected participant's slot, clearing its
     * outstanding-offer marker with it. A participant already present on an add is left unchanged, and a
     * participant absent on a remove is a no-op.
     *
     * @param participants the affected participant user JIDs
     * @param added        {@code true} to add the participants, {@code false} to remove them
     */
    private void reconcileLocalMembership(List<Jid> participants, boolean added) {
        for (var participant : participants) {
            if (added) {
                membership.allocate(CallParticipantUserNode.ofUser(participant));
            } else {
                membership.remove(participant);
            }
        }
    }

    /**
     * Returns whether a roster identity reports the participant connected.
     *
     * @param identity the participant identity
     * @return {@code true} when the identity's roster {@code state} is the {@code "connected"} literal
     */
    private static boolean isConnected(CallParticipantUserNode identity) {
        return identity.state().map(CONNECTED_STATE_LITERAL::equals).orElse(false);
    }
}
