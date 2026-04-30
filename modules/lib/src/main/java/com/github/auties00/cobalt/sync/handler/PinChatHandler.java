package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.contact.PinAction;
import com.github.auties00.cobalt.model.sync.action.contact.PinActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.event.MdSyncdDogfoodingFeatureUsageEventBuilder;
import com.github.auties00.cobalt.wam.type.MdFeatureCode;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles pin chat sync actions.
 *
 * <p>This handler processes incoming mutations that pin or unpin chats and
 * newsletters. The action is identified by the {@code "pin_v1"} action name in
 * {@code SyncActionValue.pinAction}. The mutation index format is
 * {@code ["pin_v1", chatJid]}.
 *
 * <p>Per WhatsApp Web, this handler enforces a maximum number of pinned
 * conversations: {@code MAX_PINNED_CHATS = 3} for chats and
 * {@code MAX_PINNED_NEWSLETTERS = 2} for newsletters. When a pin action would
 * exceed the limit, the oldest pinned conversation is unpinned (if its timestamp
 * is older than the incoming pin), or the incoming pin itself is rejected (if
 * the oldest pin is newer). In either case, a pending unpin mutation is queued
 * to propagate the rejection to other devices.
 *
 * <p>Per WhatsApp Web, this handler extends {@code ChatSyncdActionBase} (the
 * common base class for chat-jid-indexed sync actions) and sets
 * {@code chatJidIndex = 1} and {@code collectionName = RegularLow}.
 *
 * @implNote WAWebPinChatSync — singleton instance exported as {@code PinChatSync}
 */
public final class PinChatHandler implements WebAppStateActionHandler {
    /**
     * Maximum number of pinned chats allowed.
     *
     * <p>Per WhatsApp Web {@code WAWebPinChatLimits.MAX_PINNED_CHATS = 3}. The
     * WA Web {@code WAWebChatPinBridge.getPinLimit} also consults a premium
     * benefit gating function ({@code cr:12224.getPinnedChatsBenefitLimit})
     * which is not implemented in Cobalt, so the constant value is used directly.
     *
     * @implNote WAWebPinChatLimits.MAX_PINNED_CHATS
     */
    private static final int MAX_PINNED_CHATS = 3;

    /**
     * Maximum number of pinned newsletters allowed.
     *
     * <p>Per WhatsApp Web {@code WAWebPinChatLimits.MAX_PINNED_NEWSLETTERS = 2}.
     *
     * @implNote WAWebPinChatLimits.MAX_PINNED_NEWSLETTERS
     */
    private static final int MAX_PINNED_NEWSLETTERS = 2;

    /**
     * Singleton instance of the pin chat handler.
     *
     * <p>Per WhatsApp Web, {@code WAWebPinChatSync} exports a single instance
     * ({@code var f = new _(); l.PinChatSync = f}).
     *
     * @implNote WAWebPinChatSync.PinChatSync — module-level singleton
     */
    public static final PinChatHandler INSTANCE = new PinChatHandler();

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebPinChatSync — class {@code _} constructor (initializes
     *           {@code chatJidIndex = 1} and {@code collectionName = RegularLow})
     */
    private PinChatHandler() {

    }

    /**
     * Returns the action name for pin chat actions.
     *
     * @implNote WAWebPinChatSync.getAction — returns
     *           {@code WASyncdConst.Actions.Pin} ({@code "pin_v1"})
     * @return the action name {@code "pin_v1"}
     */
    @Override
    public String actionName() {
        return PinAction.ACTION_NAME;
    }

    /**
     * Returns the sync collection for pin chat actions.
     *
     * <p>Per WhatsApp Web, the pin chat handler's {@code collectionName} is set to
     * {@code WASyncdConst.CollectionName.RegularLow} in the constructor.
     *
     * @implNote WAWebPinChatSync.collectionName — set in constructor to
     *           {@code WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return PinAction.COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for pin chat actions.
     *
     * @implNote WAWebPinChatSync.getVersion — returns {@code 5}
     * @return the version number {@code 5}
     */
    @Override
    public int version() {
        return PinAction.ACTION_VERSION;
    }

    /**
     * Applies a pin chat mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is {@link SyncActionState#SUCCESS}.
     *
     * @implNote WAWebPinChatSync.applyMutations — per-mutation inner logic, success
     *           check on the returned {@code syncApplyActionResult}
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies a pin chat mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebPinChatSync.applyMutation}, for each mutation:
     * <ol>
     *   <li>If {@code operation === "remove"} returns {@code Unsupported}</li>
     *   <li>Extracts {@code chatJid} from {@code indexParts[1]}; if missing returns
     *       {@code malformedActionIndex}</li>
     *   <li>Validates {@code chatJid} is a valid WID; if not returns
     *       {@code malformedActionIndex}</li>
     *   <li>Validates {@code pinAction} is present; if not returns
     *       {@code malformedActionValue}</li>
     *   <li>Validates {@code pinned} field is set; if not returns
     *       {@code malformedActionValue}</li>
     *   <li>Resolves the chat via {@code WAWebSyncdGetChat.resolveChatForMutationIndex};
     *       if not found returns {@code Orphan}</li>
     *   <li>If {@code !pinned}, applies the unpin update directly and returns
     *       {@code Success}</li>
     *   <li>Otherwise loads local pins ({@code getLocalChatPins} or
     *       {@code getLocalNewsletterPins} based on {@code y.isNewsletter()}):
     *     <ul>
     *       <li>If the chat is already in the pin list, applies the update with
     *           the new timestamp and returns {@code Success}</li>
     *       <li>If the pin list size is below the limit
     *           ({@code WAWebChatPinBridge.getPinLimit}), applies the update and
     *           returns {@code Success}</li>
     *       <li>Otherwise finds the oldest pin {@code b}: if {@code b.timestamp < i}
     *           kicks out the oldest (applies unpin for {@code b}, pin for {@code y},
     *           and queues a pending unpin for {@code b}); otherwise rejects the
     *           incoming pin (queues a pending unpin for {@code y}). Returns
     *           {@code Success} in both branches.</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p>Exceptions are caught and return {@code Failed}.
     *
     * @implNote WAWebPinChatSync.applyMutation — per-mutation inner function
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        try {
            var indexArray = JSON.parseArray(mutation.index());
            var chatJidString = indexArray.size() > 1 ? indexArray.getString(1) : null;
            if (chatJidString == null || chatJidString.isEmpty()) {
                return malformedActionIndex();
            }

            Jid chatJid;
            try {
                chatJid = Jid.of(chatJidString);
            } catch (Exception e) { // ADAPTED: Jid.of throws WhatsAppMalformedJidException; WA Web isWid() returns false
                return malformedActionIndex();
            }
            if (chatJid == null) {
                return malformedActionIndex();
            }

            if (!(mutation.value().action().orElse(null) instanceof PinAction action)) {
                return malformedActionValue();
            }

            // ADAPTED: Cobalt's PinAction.pinned() boolean accessor coalesces null to false,
            // following the project-wide nullable boolean accessor pattern. A null pinned
            // field is therefore treated as "unpin" rather than "malformed".
            var currentTimestamp = mutation.timestamp();
            var isNewsletter = chatJid.hasNewsletterServer();

            // ADAPTED: WA Web's WAWebSyncdGetChat.resolveChatForMutationIndex queries a
            // unified chat table that contains both chats and newsletters. Cobalt stores
            // them separately, so the dispatch is split based on the JID's server type
            // and routes to the dedicated newsletter pin path when the JID is a newsletter.
            if (isNewsletter) {
                return applyNewsletterPinMutation(client, wamService, chatJid, action, currentTimestamp); // ADAPTED: Cobalt routes newsletter pins through a separate code path
            }

            var chat = client.store().findChatByJid(chatJid);
            if (chat.isEmpty()) {
                return MutationApplicationResult.orphan(chatJidString, "Chat");
            }

            if (!action.pinned()) {
                chat.get().setPinnedTimestamp(null);
                return MutationApplicationResult.success();
            }

            // Cobalt loads the current pinned chats directly from the in-memory store.
            // The "already pinned" early-out is checked first to mirror WA Web's
            // C.some(e => e.chatId.toString() === y.toString()) check.
            var allPinnedChats = client.store().chats().stream()
                    .filter(c -> c.pinnedTimestamp().isPresent())
                    .toList();

            var alreadyPinned = allPinnedChats.stream()
                    .anyMatch(c -> c.jid().equals(chatJid));
            if (alreadyPinned) {
                chat.get().setPinnedTimestamp(currentTimestamp);
                chat.get().setArchived(false);
                return MutationApplicationResult.success();
            }

            if (allPinnedChats.size() < MAX_PINNED_CHATS) {
                chat.get().setPinnedTimestamp(currentTimestamp);
                chat.get().setArchived(false);
                return MutationApplicationResult.success();
            }

            wamService.commit(new MdSyncdDogfoodingFeatureUsageEventBuilder()
                    .mdSyncdDogfoodingFeature(MdFeatureCode.UNPIN_4TH_CHAT_MUTATION)
                    .build());

            // Find the oldest pinned chat (lowest timestamp).
            var oldestPinned = allPinnedChats.stream()
                    .min(Comparator.comparing(c -> c.pinnedTimestamp().orElse(Instant.EPOCH)))
                    .orElseThrow(); // WA Web reduce on a non-empty array always succeeds; size >= MAX_PINNED_CHATS guarantees at least one entry
            var oldestTimestamp = oldestPinned.pinnedTimestamp().orElse(Instant.EPOCH);

            // If the oldest pin is older than the incoming pin, kick the oldest out
            // (S = oldest); otherwise the incoming pin is rejected (S = current).
            if (oldestTimestamp.isBefore(currentTimestamp)) {
                oldestPinned.setPinnedTimestamp(null);
                queueUnpinMutation(client, oldestPinned.jid(), currentTimestamp);
                chat.get().setPinnedTimestamp(currentTimestamp);
                chat.get().setArchived(false);
                return MutationApplicationResult.success();
            }

            // No local update is applied (v stays empty), only a pending unpin is queued.
            queueUnpinMutation(client, chatJid, currentTimestamp);
            return MutationApplicationResult.success();
        } catch (Exception e) {
            return MutationApplicationResult.failed();
        }
    }

    /**
     * Applies a pin chat mutation targeting a newsletter.
     *
     * <p>Per WhatsApp Web, newsletter pins are stored in the same unified chat
     * table as regular chat pins, with the {@code pin} field on the chat row
     * and the {@code y.isNewsletter()} branch in {@code applyMutation} routing
     * the limit check through {@code getLocalNewsletterPins} and
     * {@code MAX_PINNED_NEWSLETTERS}.
     *
     * <p>In Cobalt, newsletters are stored separately from chats and the
     * {@link com.github.auties00.cobalt.model.newsletter.Newsletter} class does
     * not carry a {@code pinnedTimestamp} field. The pinned newsletter state is
     * therefore tracked via a dedicated {@code newsletterPinStates} map on the
     * store, keyed by the newsletter JID string and valued by the pin
     * {@link Instant}. The semantic ordering — already-pinned passthrough,
     * limit check, oldest-vs-incoming kick-out — mirrors the WA Web logic
     * exactly.
     *
     * @implNote ADAPTED: WAWebPinChatSync.applyMutation y.isNewsletter() branch —
     *           Cobalt routes newsletter pins through a dedicated map because the
     *           {@code Newsletter} model has no pinnedTimestamp field
     * @param client          the WhatsApp client instance
     * @param wamService      the WAM telemetry service used for committing the dogfooding event
     * @param newsletterJid   the newsletter JID
     * @param action          the pin action (carries pinned flag)
     * @param currentTimestamp the mutation timestamp
     * @return the detailed application result
     */
    private MutationApplicationResult applyNewsletterPinMutation(
            WhatsAppClient client,
            WamService wamService,
            Jid newsletterJid,
            PinAction action,
            Instant currentTimestamp
    ) {
        var newsletter = client.store().findNewsletterByJid(newsletterJid);
        if (newsletter.isEmpty()) {
            return MutationApplicationResult.orphan(newsletterJid.toString(), "Newsletter");
        }

        var states = new HashMap<>(client.store().newsletterPinStates()); // ADAPTED: snapshot the current map for read-modify-write
        var key = newsletterJid.toString();

        if (!action.pinned()) {
            states.remove(key);
            client.store().setNewsletterPinStates(states); // ADAPTED: persist the updated map back to the store
            return MutationApplicationResult.success();
        }

        if (states.containsKey(key)) {
            states.put(key, currentTimestamp);
            client.store().setNewsletterPinStates(states); // ADAPTED: persist the updated map back to the store
            return MutationApplicationResult.success();
        }

        if (states.size() < MAX_PINNED_NEWSLETTERS) {
            states.put(key, currentTimestamp);
            client.store().setNewsletterPinStates(states); // ADAPTED: persist the updated map back to the store
            return MutationApplicationResult.success();
        }

        wamService.commit(new MdSyncdDogfoodingFeatureUsageEventBuilder()
                .mdSyncdDogfoodingFeature(MdFeatureCode.UNPIN_4TH_CHAT_MUTATION)
                .build());

        var oldest = states.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .orElseThrow(); // size >= MAX_PINNED_NEWSLETTERS guarantees at least one entry
        var oldestTimestamp = oldest.getValue();

        if (oldestTimestamp.isBefore(currentTimestamp)) {
            states.remove(oldest.getKey());
            states.put(key, currentTimestamp);
            client.store().setNewsletterPinStates(states); // ADAPTED: persist the updated map back to the store
            queueUnpinMutation(client, Jid.of(oldest.getKey()), currentTimestamp);
            return MutationApplicationResult.success();
        }

        queueUnpinMutation(client, newsletterJid, currentTimestamp);
        return MutationApplicationResult.success();
    }

    /**
     * Queues a pending unpin mutation to propagate to other devices.
     *
     * <p>Per WhatsApp Web {@code WAWebPinChatSync.createPendingUnpin}: builds a
     * pin mutation with {@code pinned = false} via {@code getPinMutation(t, false, e)}
     * and appends it to the pending mutations table via
     * {@code WAWebSyncdDb.appendPendingMutationsRows}.
     *
     * @implNote WAWebPinChatSync.createPendingUnpin
     * @param client    the WhatsApp client instance
     * @param chatJid   the chat or newsletter JID to unpin
     * @param timestamp the mutation timestamp
     */
    private void queueUnpinMutation(WhatsAppClient client, Jid chatJid, Instant timestamp) {
        var pending = getPinMutation(timestamp, false, chatJid);
        client.store().addPendingMutations(collectionName(), List.of(pending));
    }

    /**
     * Builds a pending mutation that pins or unpins a chat or newsletter.
     *
     * <p>Per WhatsApp Web {@code WAWebPinChatSync.getPinMutation}: constructs a
     * {@code pinAction} value, computes the chat-jid mutation index via
     * {@code WAWebSyncdGetChat.getChatJidMutationIndexForChat}, and delegates to
     * {@code WAWebSyncdActionUtils.buildPendingMutation} with the
     * {@code RegularLow} collection, the action version, the {@code SET}
     * operation, the timestamp, and the {@code Pin} action.
     *
     * <p>In Cobalt, the chat-jid mutation index is computed as
     * {@code chatJid.toString()} directly because Cobalt does not maintain the
     * LID-migration accountLid lookup that {@code getChatJidMutationIndexForChat}
     * performs in WA Web.
     *
     * @implNote WAWebPinChatSync.getPinMutation
     * @param timestamp the mutation timestamp
     * @param pinned    whether the chat should be pinned ({@code true}) or unpinned ({@code false})
     * @param chatJid   the JID of the chat or newsletter
     * @return the pending mutation for the pin action
     */
    public SyncPendingMutation getPinMutation(Instant timestamp, boolean pinned, Jid chatJid) {
        var pinAction = new PinActionBuilder()
                .pinned(pinned)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .pinAction(pinAction)
                .build();
        // ADAPTED: Cobalt does not implement the LID 1:1 migration accountLid path,
        // so the chat-jid mutation index resolves to the JID's canonical string form.
        var index = JSON.toJSONString(List.of(actionName(), chatJid.toString()));
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0);
    }

    /**
     * Builds the set of pending mutations needed to pin or unpin a chat.
     *
     * <p>Per WhatsApp Web {@code WAWebPinChatSync.getMutationsForPin}: when the
     * chat is being pinned ({@code t === true}), the WAM dogfooding feature
     * usage event {@code PIN_MUTATION} is committed (gated on {@code gkx 26258}),
     * the pin mutation is built, and an additional unarchive mutation is
     * appended via {@code WAWebArchiveChatSync.getArchiveChatMutation(e, false, n)}.
     * When unpinning, only the pin mutation is returned.
     *
     * <p>In Cobalt, WAM telemetry is intentionally omitted. The unarchive
     * mutation is NOT appended here because building an archive mutation
     * requires a message range that the caller must construct via the
     * higher-level archive sync infrastructure. Callers that need the combined
     * pin+unarchive sequence should explicitly invoke
     * {@code ArchiveChatHandler.getArchiveChatMutation} after this method.
     *
     * @implNote WAWebPinChatSync.getMutationsForPin — WAM commit and the
     *           cross-handler archive append are documented as caller-side
     *           responsibilities in Cobalt
     * @param timestamp the mutation timestamp
     * @param pinned    whether the chat should be pinned
     * @param chatJid   the JID of the chat or newsletter
     * @return the list of pending mutations for the pin operation
     */
    public List<SyncPendingMutation> getMutationsForPin(Instant timestamp, boolean pinned, Jid chatJid) {
        // The PIN_MUTATION WAM emission is performed at the caller (WhatsAppClient.pinChat) since
        // this method has no WamService handle (handler is a singleton with no injected client).
        var mutations = new ArrayList<SyncPendingMutation>();
        mutations.add(getPinMutation(timestamp, pinned, chatJid));
        // ADAPTED: When pinning, WA Web also queues an unarchive mutation. Cobalt's
        // archive mutation requires a message range that callers must construct
        // separately, so the unarchive append is delegated to the caller.
        return mutations;
    }
}
