package com.github.auties00.cobalt.call.internal;

import com.github.auties00.cobalt.call.ActiveCall;
import com.github.auties00.cobalt.call.CallEndReason;
import com.github.auties00.cobalt.call.CallInteraction;
import com.github.auties00.cobalt.call.CallOptions;
import com.github.auties00.cobalt.call.CallState;
import com.github.auties00.cobalt.call.IncomingCall;
import com.github.auties00.cobalt.call.internal.signaling.CallReceiver;
import com.github.auties00.cobalt.call.internal.signaling.CallStanza;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;

/**
 * Coordinates one client's call activity.
 *
 * <p>This service owns the registry of in-flight {@link ActiveCall} sessions and exposes the
 * call-control entry points used by {@link LinkedWhatsAppClient}: {@link #placeCall(Jid, CallOptions)}
 * for outbound calls, and {@link #accept(IncomingCall, CallOptions)} and
 * {@link #reject(IncomingCall, CallEndReason)} for inbound offers. It sits between the public
 * client API and listener surface above and the signaling classes ({@link CallReceiver},
 * {@link CallStanza}) and transport and media layers below. There is one instance per
 * {@link LinkedWhatsAppClient}.
 *
 * <p>The service implements the signaling and state-machine portion of a call: it produces
 * {@link ActiveCall} instances that own their lifecycle and react to peer-side state changes routed
 * back through {@link #onPeerAccept(String)}, {@link #onPeerReject(String, String)}, and
 * {@link #onPeerTerminate(String, String)}.
 *
 * @implSpec
 * Implementations must track each placed or accepted call in a registry keyed by call id and must
 * route peer-side state changes to the matching {@link ActiveCall}. The video-state family
 * ({@link #sendVideoUpgradeRequest(Jid, Jid, String)} and
 * {@link #sendVideoUpgradeReject(Jid, Jid, String)}) should funnel through
 * {@link #sendVideoState(Jid, Jid, String, boolean)} so one override intercepts all of them.
 */
public interface CallService {
    /**
     * Places an outbound call to {@code peer} with the given options and returns its live session.
     *
     * <p>A fresh call identifier is generated, an {@link ActiveCall} is registered in the in-flight
     * registry along with a caller-side telemetry accumulator, and the offer stanza is sent. The
     * session is parked in {@link CallState#CONNECTING} until the peer's acceptance arrives through
     * {@link #onPeerAccept(String)}.
     *
     * @implSpec
     * Implementations must register the session before sending the offer.
     *
     * @param peer    the {@link Jid} of the callee
     * @param options the local side's preferred settings
     * @return the live session
     * @throws NullPointerException  if {@code peer} or {@code options} is {@code null}
     * @throws IllegalStateException if the client is not logged in
     */
    ActiveCall placeCall(Jid peer, CallOptions options);

    /**
     * Places an outbound group call into {@code groupJid}, fanning the offer out to every
     * {@code peer} in {@code peers}, and returns its live session.
     *
     * <p>A fresh call identifier is generated and one {@link ActiveCall} is registered for the whole
     * call. The same per-call shared key is encrypted per-device of every peer and shipped as one
     * {@code <call to="peer">} stanza per peer; every stanza shares the call-id and call-creator so
     * the relay binds them to one SFU allocation.
     *
     * @implSpec
     * Implementations must register one session keyed by the generated call-id and must send one
     * offer stanza per peer before returning. The session's {@link ActiveCall#chatJid()} is the
     * group; mid-call control plane goes through the call's DataChannel rather than per-peer XMPP.
     *
     * @param peers    the user JIDs of every other group participant (the local user is excluded)
     * @param groupJid the group {@link Jid}
     * @param options  the local side's preferred settings
     * @return the live session
     * @throws NullPointerException     if {@code peers}, {@code groupJid}, or {@code options} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code peers} is empty
     * @throws IllegalStateException    if the client is not logged in
     */
    ActiveCall placeGroupCall(java.util.Set<Jid> peers, Jid groupJid, CallOptions options);

    /**
     * Accepts an inbound call offer and returns its live session.
     *
     * <p>An {@link ActiveCall} is registered along with a callee-side telemetry accumulator, the
     * accept stanza is sent, and the session is parked in {@link CallState#CONNECTING}. This method
     * is invoked after the one-shot guard on the offer has been claimed.
     *
     * @implSpec
     * Implementations must register the session before sending the accept stanza.
     *
     * @param offer   the offer being accepted
     * @param options the local side's preferred settings
     * @return the live session
     * @throws NullPointerException if {@code offer} or {@code options} is {@code null}
     */
    ActiveCall accept(IncomingCall offer, CallOptions options);

    /**
     * Rejects an inbound call offer with the given reason.
     *
     * <p>The reject stanza is sent to the peer, the offer is removed from the store, and
     * {@code onCallEnded} is fired on every listener. This method is invoked after the one-shot
     * guard on the offer has been claimed.
     *
     * @implSpec
     * Implementations must notify listeners of the end after sending the reject stanza.
     *
     * @param offer  the offer being rejected
     * @param reason the {@link CallEndReason} to communicate to the peer
     * @throws NullPointerException if {@code offer} or {@code reason} is {@code null}
     */
    void reject(IncomingCall offer, CallEndReason reason);

    /**
     * Returns the {@link ActiveCall} tracked under the given identifier, or {@code null} if none is
     * tracked.
     *
     * <p>A {@code null} argument yields {@code null}. This lookup lets {@link CallReceiver} route
     * peer-side state transitions to the matching session.
     *
     * @implSpec
     * Implementations must return {@code null} for a {@code null} argument and for an unknown id.
     *
     * @param callId the call's unique identifier
     * @return the matching session, or {@code null}
     */
    ActiveCall find(String callId);

    /**
     * Reports that the peer accepted a previously-placed outbound call.
     *
     * <p>The acceptance is forwarded to the matching session, if one is still tracked; otherwise
     * the report is dropped.
     *
     * @implSpec
     * Implementations must drop the report when no session is tracked for {@code callId}.
     *
     * @param callId the call identifier
     */
    void onPeerAccept(String callId);

    /**
     * Reports the peer's ICE candidates parsed from a {@code <accept>} or {@code <transport>} stanza.
     *
     * <p>WhatsApp's primary media path is peer-to-peer: the peer advertises its transport addresses as
     * {@code <te priority="N">IPv4PORT</te>} elements, and each endpoint runs STUN connectivity checks
     * against the other's candidates over a shared UDP socket, carrying SRTP over the nominated pair.
     * This forwards the peer's candidates to the matching session so its ICE agent can run those
     * checks; the report is dropped when no session is tracked for {@code callId}.
     *
     * @implSpec Implementations must drop the report when no session is tracked for {@code callId}.
     * @param callId     the call identifier
     * @param candidates the peer's transport addresses, highest priority first
     */
    void onPeerCandidates(String callId, java.util.List<java.net.InetSocketAddress> candidates);

    /**
     * Starts the local camera video track on a connected call, used by an audio-to-video upgrade.
     *
     * <p>Adds a {@link com.github.auties00.cobalt.call.session.VideoTrackOptions.Kind#CAMERA} track to
     * the call's media session so local video frames written to the call's video sink are encoded and
     * sent to the peer over the already-negotiated media transport. A no-op when the call is not
     * tracked or has no media session yet.
     *
     * @param callId the call identifier
     */
    void startLocalVideo(String callId);

    /**
     * Starts a screen-share video track on a connected call.
     *
     * <p>Adds a {@link com.github.auties00.cobalt.call.session.VideoTrackOptions.Kind#SCREEN_SHARE}
     * track to the call's media session so screen frames written to the call's video sink are encoded
     * and sent to the peer, signaled as a screen capture so the peer renders it distinctly from a
     * camera feed. A no-op when the call is not tracked or has no media session yet.
     *
     * @param callId the call identifier
     */
    void startScreenShare(String callId);

    /**
     * Reports that the peer rejected a previously-placed outbound call.
     *
     * <p>The matching session, if still tracked, is ended with the given wire reason; otherwise the
     * report is dropped.
     *
     * @implSpec
     * Implementations must drop the report when no session is tracked for {@code callId}.
     *
     * @param callId the call identifier
     * @param reason the wire-level rejection reason, or {@code null}
     */
    void onPeerReject(String callId, String reason);

    /**
     * Reports that the peer terminated an in-flight call.
     *
     * <p>The matching session, if still tracked, is ended with the given wire reason; otherwise the
     * report is dropped.
     *
     * @implSpec
     * Implementations must drop the report when no session is tracked for {@code callId}.
     *
     * @param callId the call identifier
     * @param reason the wire-level reason, or {@code null}
     */
    void onPeerTerminate(String callId, String reason);

    /**
     * Removes a session from the registry and emits its end-of-call telemetry.
     *
     * <p>This method is invoked by {@link ActiveCall} when it transitions to
     * {@link CallState#ENDED}. The session and its telemetry accumulator are removed, the call is
     * removed from the store, and the accumulator is drained into a WAM Call event.
     *
     * @implSpec
     * Implementations must remove the session from the registry and must skip telemetry when no
     * accumulator is available.
     *
     * @param callId the call identifier
     */
    void unregister(String callId);

    /**
     * Sends a call-termination stanza to the peer.
     *
     * <p>This method is invoked by {@link ActiveCall#hangup()}.
     *
     * @param peer    the peer {@link Jid}
     * @param creator the call-creator {@link Jid}: the local user for outbound calls, the peer for
     *                inbound calls
     * @param callId  the call identifier
     * @param reason  the {@link CallEndReason} to communicate
     */
    void sendTerminate(Jid peer, Jid creator, String callId, CallEndReason reason);

    /**
     * Sends a mute-state stanza notifying the peer that the local microphone was muted or unmuted.
     *
     * @param peer    the peer {@link Jid}
     * @param creator the call-creator {@link Jid}
     * @param callId  the call identifier
     * @param muted   {@code true} if the microphone is now muted
     */
    void sendMute(Jid peer, Jid creator, String callId, boolean muted);

    /**
     * Sends a video-state stanza notifying the peer that local video was enabled or disabled.
     *
     * @implSpec
     * Implementations should route {@link #sendVideoUpgradeRequest(Jid, Jid, String)} and
     * {@link #sendVideoUpgradeReject(Jid, Jid, String)} through this method.
     *
     * @param peer    the peer {@link Jid}
     * @param creator the call-creator {@link Jid}
     * @param callId  the call identifier
     * @param enabled {@code true} if local video is now on
     */
    void sendVideoState(Jid peer, Jid creator, String callId, boolean enabled);

    /**
     * Sends the request leg of a mid-call video upgrade.
     *
     * <p>The request is expressed as an enabling video-state stanza.
     *
     * @param peer    the peer {@link Jid}
     * @param creator the call-creator {@link Jid}
     * @param callId  the call identifier
     */
    void sendVideoUpgradeRequest(Jid peer, Jid creator, String callId);

    /**
     * Sends the peer-side rejection of a mid-call video upgrade.
     *
     * <p>The rejection is expressed as a disabling video-state stanza and keeps the caller
     * audio-only.
     *
     * @param peer    the peer {@link Jid}
     * @param creator the call-creator {@link Jid}
     * @param callId  the call identifier
     */
    void sendVideoUpgradeReject(Jid peer, Jid creator, String callId);

    /**
     * Sends an in-call interaction over the call's pre-negotiated DataChannel.
     *
     * <p>The interaction is encoded into an RTP-shaped packet, encrypted with the call's negotiated
     * SRTP keys, and handed to the call's default DataChannel. The call is a no-op when no call is
     * registered for {@code callId}, when the DataChannel is not open, or when the SRTP endpoint is
     * not yet available. The {@code peer} and {@code creator} parameters are unused on this path.
     *
     * @implSpec
     * Implementations must be a no-op when the call, its DataChannel, or its SRTP endpoint is
     * unavailable.
     *
     * @param peer        the peer {@link Jid} (unused on this path)
     * @param creator     the call-creator {@link Jid} (unused on this path)
     * @param callId      the call identifier
     * @param interaction the interaction payload
     * @throws NullPointerException if {@code callId} or {@code interaction} is {@code null}
     */
    void sendInteraction(Jid peer, Jid creator, String callId, CallInteraction interaction);

    /**
     * Reports that the peer published a new end-to-end keying material bundle for an in-flight call.
     *
     * <p>Group calls rotate their per-domain SRTP master keys (audio, video, app-data) on participant
     * join and leave. Each rotation is published as one Signal-encrypted {@code <enc_rekey>} stanza
     * whose plaintext is a {@link com.github.auties00.cobalt.model.call.datachannel.E2eRekeyPayload}.
     *
     * @implSpec
     * Implementations must drop the report when no session is tracked for {@code callId}. The
     * implementation owns the Signal-decryption step and the parse of
     * {@link com.github.auties00.cobalt.model.call.datachannel.E2eRekeyPayload}.
     *
     * @param callId    the call identifier
     * @param senderJid the device {@link Jid} that authored the rekey envelope
     * @param encType   the wire-level Signal envelope variant ({@code msg} or {@code pkmsg})
     * @param ciphertext the Signal-encrypted bytes carried inside the {@code <enc>} child
     */
    void onEncRekey(String callId, Jid senderJid, com.github.auties00.cobalt.message.MessageEncryptionType encType, byte[] ciphertext);

    /**
     * Brings up the callee media plane from a relay block delivered out-of-band in a
     * {@code <group_update>} push rather than inline in the original offer.
     *
     * <p>The native desktop caller omits the {@code <relay>} from the group offer; the server instead
     * sends it inside the {@code <group_update>} that confirms the join (after the callee's
     * {@code <preaccept>} and {@code <accept>}). This routes that late-arriving relay to the matching
     * session so it allocates the relay and starts hop-by-hop SRTP, exactly as the inline-relay path
     * does. The report is dropped when no session is tracked for {@code callId}, when the session is
     * not a group call, or when its media plane was already brought up (for example from an inline
     * relay).
     *
     * @implSpec
     * Implementations must bring the media plane up at most once per call and must drop the report
     * for an unknown or non-group {@code callId}.
     *
     * @param callId the call identifier
     * @param relay  the relay block parsed from the {@code <group_update>}
     */
    void onGroupRelay(String callId, com.github.auties00.cobalt.ack.CallRelay relay);

    /**
     * Notifies all registered listeners that a call ended.
     *
     * <p>The wire reason is parsed into a typed {@link CallEndReason} via
     * {@link CallEndReason#fromWireValue(String)}, so an unrecognized or absent literal surfaces as
     * {@link CallEndReason#UNKNOWN}.
     *
     * @implSpec
     * Implementations must surface an unrecognized or {@code null} wire reason as
     * {@link CallEndReason#UNKNOWN}.
     *
     * @param callId     the call identifier
     * @param fromJid    the {@link Jid} of the party that ended the call
     * @param wireReason the wire-level reason literal, or {@code null}
     */
    void notifyEnded(String callId, Jid fromJid, String wireReason);
}
