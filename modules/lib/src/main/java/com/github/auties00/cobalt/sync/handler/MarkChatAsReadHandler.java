package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.ConflictResolution;
import com.github.auties00.cobalt.model.sync.ConflictResolutionState;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionMessageRange;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.chat.MarkChatAsReadAction;
import com.github.auties00.cobalt.model.sync.action.chat.MarkChatAsReadActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.List;

/**
 * Handles mark chat as read sync actions.
 *
 * <p>This handler processes incoming mutations that mark a chat as read or
 * unread, resolves conflicts between local and remote mutations using message
 * range comparison, and builds outgoing mutations for user-initiated read-state
 * actions.
 *
 * <p>The action is identified by the {@code "markChatAsRead"} action name in
 * {@code SyncActionValue.markChatAsReadAction}. The mutation index format is
 * {@code ["markChatAsRead", chatJid]}.
 *
 * <p>Per WhatsApp Web, this handler extends {@code ChatMessageRangeSyncdActionBase},
 * which provides shared message-range-based conflict resolution. In Cobalt this
 * logic is inlined since Java does not use the same inheritance hierarchy.
 *
 * @implNote WAWebMarkChatAsReadSync — singleton instance exported as {@code default}
 */
public final class MarkChatAsReadHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of the mark chat as read handler.
     *
     * <p>Per WhatsApp Web, {@code WAWebMarkChatAsReadSync} exports a single instance
     * ({@code var g = new f(); l.default = g}).
     *
     * @implNote WAWebMarkChatAsReadSync.default — module-level singleton
     */
    public static final MarkChatAsReadHandler INSTANCE = new MarkChatAsReadHandler();

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebMarkChatAsReadSync — class {@code f} constructor (only initializes
     *           {@code chatJidIndex = 1} and {@code collectionName = RegularLow},
     *           both of which are compile-time constants in Cobalt)
     */
    private MarkChatAsReadHandler() {

    }

    /**
     * Returns the action name for mark chat as read actions.
     *
     * @implNote WAWebMarkChatAsReadSync.getAction — returns
     *           {@code WASyncdConst.Actions.MarkChatAsRead} ({@code "markChatAsRead"})
     * @return the action name {@code "markChatAsRead"}
     */
    @Override
    public String actionName() {
        return MarkChatAsReadAction.ACTION_NAME; // WAWebMarkChatAsReadSync.getAction -> WASyncdConst.Actions.MarkChatAsRead
    }

    /**
     * Returns the sync collection for mark chat as read actions.
     *
     * <p>Per WhatsApp Web, the handler's {@code collectionName} is set to
     * {@code WASyncdConst.CollectionName.RegularLow} in the constructor.
     *
     * @implNote WAWebMarkChatAsReadSync.collectionName — set in constructor to
     *           {@code WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return MarkChatAsReadAction.COLLECTION_NAME; // WAWebMarkChatAsReadSync.collectionName = WASyncdConst.CollectionName.RegularLow
    }

    /**
     * Returns the mutation format version for mark chat as read actions.
     *
     * @implNote WAWebMarkChatAsReadSync.getVersion — returns {@code 3}
     * @return the version number {@code 3}
     */
    @Override
    public int version() {
        return MarkChatAsReadAction.ACTION_VERSION; // WAWebMarkChatAsReadSync.getVersion -> 3
    }

    /**
     * Applies a mark chat as read mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is
     * {@link com.github.auties00.cobalt.model.sync.SyncActionState#SUCCESS}.
     *
     * @implNote WAWebMarkChatAsReadSync.applyMutations — per-mutation inner logic,
     *           success check on the returned {@code syncApplyActionResult}
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS; // WAWebMarkChatAsReadSync.applyMutations
    }

    /**
     * Applies a mark chat as read mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebMarkChatAsReadSync.applyMutations}, for each
     * mutation with {@code operation === "set"}:
     * <ol>
     *   <li>Extracts the chat JID from {@code indexParts[1]}; returns
     *       {@code malformedActionIndex} if empty</li>
     *   <li>Validates the sync action value via {@code validateSyncActionValue}
     *       (checks {@code read} is not {@code null} and {@code messageRange} is
     *       present and valid); returns {@code malformedActionValue} otherwise</li>
     *   <li>Validates the JID is a valid WID via {@code WAWebWid.isWid}</li>
     *   <li>Resolves the chat via {@code WAWebSyncdGetChat.resolveChatForMutationIndex}</li>
     *   <li>Delegates to {@code $MarkChatAsReadSync$p_3} which compares the local
     *       and remote message ranges and conditionally updates the chat's read
     *       state via {@code frontendSendAndReceive("updateChatReadStatus", ...)}</li>
     * </ol>
     *
     * <p>Non-{@code SET} operations return {@code Unsupported}. Exceptions are
     * caught and return {@code Failed}.
     *
     * <p>In Cobalt, the active-message-range gating from {@code $MarkChatAsReadSync$p_3}
     * is skipped because Cobalt does not maintain browser-side IndexedDB active
     * message ranges. The read-state change is applied directly: when
     * {@code read == true} the chat is marked as not unread with a zero unread count,
     * and when {@code read == false} the chat is marked as unread with an unread
     * count of {@code -1} (matching {@code WAWebConstantsDeprecated.MARKED_AS_UNREAD}).
     * The WA Web {@code _} helper's orphan branch for
     * {@code RangeBEnclosesRangeA}/{@code RangesNotEnclosing} is likewise skipped
     * because it is driven by the active-range comparison that Cobalt omits.
     *
     * @implNote WAWebMarkChatAsReadSync.applyMutations,
     *           WAWebMarkChatAsReadSync.$MarkChatAsReadSync$p_3,
     *           WAWebMarkChatAsReadSync.$MarkChatAsReadSync$p_1,
     *           WAWebMarkChatAsReadSync.validateSyncActionValue
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebMarkChatAsReadSync.applyMutations: e.operation === "set" check, else l++ and return Unsupported
            return MutationApplicationResult.unsupported(); // WAWebMarkChatAsReadSync.applyMutations: {actionState: SyncActionState.Unsupported}
        }

        try { // WAWebMarkChatAsReadSync.applyMutations: try/catch wrapping per-mutation logic
            if (!(mutation.value().action().orElse(null) instanceof MarkChatAsReadAction action)) { // WAWebMarkChatAsReadSync.validateSyncActionValue: var e = t.markChatAsReadAction
                return malformedActionValue(); // WAWebMarkChatAsReadSync.applyMutations: i++ and WAWebSyncdIndexUtils.malformedActionValue(a.collectionName)
            }

            var chatJidString = JSON.parseArray(mutation.index()).getString(1); // WAWebMarkChatAsReadSync.applyMutations: var s = t[1]
            if (chatJidString == null || chatJidString.isEmpty()) { // WAWebMarkChatAsReadSync.applyMutations: if (!s) return a.malformedActionIndex()
                return malformedActionIndex(); // WAWebMarkChatAsReadSync.applyMutations: this.malformedActionIndex()
            }

            var chatJid = Jid.of(chatJidString); // WAWebMarkChatAsReadSync.applyMutations: o("WAWebWidFactory").createWid(s)
            if (chatJid == null) { // ADAPTED: Jid.of returns null for null input; WA Web uses isWid() validation
                return malformedActionIndex(); // WAWebMarkChatAsReadSync.applyMutations: !isWid(s) -> malformedActionIndex()
            }

            var chat = client.store().findChatByJid(chatJid); // WAWebMarkChatAsReadSync.applyMutations: yield resolveChatForMutationIndex(createWid(s))
            if (chat.isEmpty()) { // WAWebMarkChatAsReadSync.applyMutations: if (!m.success) return {actionState: Orphan, orphanModel: m.orphanModel}
                return MutationApplicationResult.orphan(chatJidString, "Chat"); // WAWebMarkChatAsReadSync.applyMutations: {actionState: SyncActionState.Orphan, orphanModel: {modelType: Chat, modelId: s}}
            }

            // WAWebMarkChatAsReadSync.validateSyncActionValue: checks read != null and messageRange != null.
            // In Cobalt, MarkChatAsReadAction.read() null-coalesces to false, so a missing read
            // is treated as "mark as unread" which is still a meaningful state. The messageRange
            // validation is skipped because Cobalt does not maintain active message ranges
            // (browser-specific IndexedDB concern). See $MarkChatAsReadSync$p_3 — the core
            // read-state change is always applied.

            // WAWebMarkChatAsReadSync.$MarkChatAsReadSync$p_1: frontendSendAndReceive("updateChatReadStatus", {id: e, read: t})
            // ADAPTED: Cobalt applies the read-state change directly on the local chat, matching
            // the backend behavior that $p_1 would have triggered (unreadCount=0 / markedAsUnread=false
            // for read=true; unreadCount=-1 / markedAsUnread=true for read=false, per
            // WAWebConstantsDeprecated.MARKED_AS_UNREAD).
            if (action.read()) { // WAWebMarkChatAsReadSync.applyMutations: t === true branch
                chat.get().setMarkedAsUnread(false); // ADAPTED: $p_1 -> backend updateChatReadStatus clears markedAsUnread
                chat.get().setUnreadCount(0); // ADAPTED: $p_1 -> backend updateChatReadStatus zeroes unreadCount
            } else { // WAWebMarkChatAsReadSync.applyMutations: t === false branch
                chat.get().setMarkedAsUnread(true); // ADAPTED: $p_1 -> backend updateChatReadStatus sets markedAsUnread
                chat.get().setUnreadCount(-1); // ADAPTED: WAWebConstantsDeprecated.MARKED_AS_UNREAD = -1
            }

            return MutationApplicationResult.success(); // WAWebMarkChatAsReadSync.applyMutations: g.syncApplyActionResult / _(e, l) Success branch
        } catch (Exception e) { // WAWebMarkChatAsReadSync.applyMutations: catch(e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebMarkChatAsReadSync.applyMutations: {actionState: SyncActionState.Failed}
        }
    }

    /**
     * Resolves conflicts between a local pending mark-chat-as-read mutation and
     * an incoming remote mark-chat-as-read mutation using message range comparison.
     *
     * <p>Per WhatsApp Web {@code WAWebMarkChatAsReadSync.resolveConflicts}:
     * <ol>
     *   <li>Decodes the local and remote {@code markChatAsReadAction} values</li>
     *   <li>Compares their message ranges via
     *       {@code WAWebMessageRangeUtils.compareMessageRanges(remote, local)}</li>
     *   <li>Resolves based on the enclosure type:
     *     <ul>
     *       <li>{@code RangeAEnclosesRangeB} (remote encloses local): apply remote, drop local</li>
     *       <li>{@code RangeBEnclosesRangeA} (local encloses remote): skip remote</li>
     *       <li>{@code RangesAreEqual}: timestamp tiebreaker ({@code local <= remote}
     *           means apply remote)</li>
     *       <li>{@code RangesNotEnclosing}: merge the two ranges, pick the {@code read}
     *           value from the newer mutation, and return
     *           {@code SKIP_REMOTE_DROP_LOCAL} with the merged mutation</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p>In WA Web, the merged mutation is applied directly inside
     * {@code lockForMessageRangeSync} by calling {@code addActiveMessageRange} and
     * {@code $MarkChatAsReadSync$p_1}. In Cobalt, the merged mutation is returned
     * to the caller via {@link ConflictResolution#merged(DecryptedMutation.Trusted)}
     * so that application and resolution remain decoupled.
     *
     * @implNote WAWebMarkChatAsReadSync.resolveConflicts
     * @param localMutation  the local pending mutation
     * @param remoteMutation the incoming remote mutation
     * @return the conflict resolution indicating which mutation to keep and
     *         optionally a merged mutation
     */
    @Override
    public ConflictResolution resolveConflicts(DecryptedMutation.Trusted localMutation, DecryptedMutation.Trusted remoteMutation) {
        var localAction = localMutation.value().action() // WAWebMarkChatAsReadSync.resolveConflicts: var c = WANullthrows(i.markChatAsReadAction)
                .filter(a -> a instanceof MarkChatAsReadAction)
                .map(a -> (MarkChatAsReadAction) a)
                .orElse(null);
        var remoteAction = remoteMutation.value().action() // WAWebMarkChatAsReadSync.resolveConflicts: var p = WANullthrows(l?.markChatAsReadAction)
                .filter(a -> a instanceof MarkChatAsReadAction)
                .map(a -> (MarkChatAsReadAction) a)
                .orElse(null);

        if (localAction == null || remoteAction == null) { // ADAPTED: WA Web uses WANullthrows which would throw; Cobalt gracefully falls back to apply remote
            return ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL); // ADAPTED: defensive fallback
        }

        var localRange = localAction.messageRange().orElse(null); // WAWebMarkChatAsReadSync.resolveConflicts: WANullthrows(c.messageRange)
        var remoteRange = remoteAction.messageRange().orElse(null); // WAWebMarkChatAsReadSync.resolveConflicts: WANullthrows(p.messageRange)

        if (localRange == null || remoteRange == null) { // ADAPTED: WA Web uses WANullthrows; Cobalt gracefully falls back
            return ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL); // ADAPTED: defensive fallback
        }

        return switch (MessageRangeUtils.compareMessageRanges(remoteRange, localRange)) { // WAWebMarkChatAsReadSync.resolveConflicts: compareMessageRanges(WANullthrows(p.messageRange), WANullthrows(c.messageRange))
            case RANGE_A_ENCLOSES_RANGE_B -> // WAWebMarkChatAsReadSync.resolveConflicts: case RangeAEnclosesRangeB -> ApplyRemoteAndDropLocal
                    ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL);
            case RANGE_B_ENCLOSES_RANGE_A -> // WAWebMarkChatAsReadSync.resolveConflicts: case RangeBEnclosesRangeA -> SkipRemote
                    ConflictResolution.of(ConflictResolutionState.SKIP_REMOTE);
            case RANGES_ARE_EQUAL -> // WAWebMarkChatAsReadSync.resolveConflicts: case RangesAreEqual -> s <= u tiebreaker
                    localMutation.timestamp().compareTo(remoteMutation.timestamp()) <= 0 // WAWebMarkChatAsReadSync.resolveConflicts: s <= u
                            ? ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL) // WAWebMarkChatAsReadSync.resolveConflicts: ApplyRemoteAndDropLocal
                            : ConflictResolution.of(ConflictResolutionState.SKIP_REMOTE); // WAWebMarkChatAsReadSync.resolveConflicts: SkipRemote
            case RANGES_NOT_ENCLOSING -> { // WAWebMarkChatAsReadSync.resolveConflicts: case RangesNotEnclosing
                var localWins = localMutation.timestamp().compareTo(remoteMutation.timestamp()) > 0; // WAWebMarkChatAsReadSync.resolveConflicts: s <= u check (inverted for localWins)
                var read = localWins ? localAction.read() : remoteAction.read(); // WAWebMarkChatAsReadSync.resolveConflicts: s <= u ? (p.read ?? false) : (c.read ?? false)
                var mergedRange = MessageRangeUtils.mergeMessageRanges(remoteRange, localRange); // WAWebMarkChatAsReadSync.resolveConflicts: mergeMessageRanges(WANullthrows(p.messageRange), WANullthrows(c.messageRange))
                var mergedAction = new MarkChatAsReadActionBuilder() // WAWebMarkChatAsReadSync.resolveConflicts: var C = {read: h, messageRange: y}
                        .read(read) // WAWebMarkChatAsReadSync.resolveConflicts: read: h
                        .messageRange(mergedRange) // WAWebMarkChatAsReadSync.resolveConflicts: messageRange: y
                        .build();
                var mergedValue = new SyncActionValueBuilder() // WAWebMarkChatAsReadSync.resolveConflicts: extends({}, l, {markChatAsReadAction: C}) -- l is the remote SyncActionDataSpec value
                        .timestamp(remoteMutation.timestamp()) // ADAPTED: WA Web spreads all of l; in practice only timestamp and markChatAsReadAction are meaningful for this handler's collection
                        .markChatAsReadAction(mergedAction) // WAWebMarkChatAsReadSync.resolveConflicts: markChatAsReadAction: C
                        .build();
                var merged = new DecryptedMutation.Trusted( // WAWebMarkChatAsReadSync.resolveConflicts: extends({}, e, {binarySyncAction: b}); delete v.id
                        localMutation.index(), // WAWebMarkChatAsReadSync.resolveConflicts: from local (e.index)
                        mergedValue, // WAWebMarkChatAsReadSync.resolveConflicts: merged binary value
                        localMutation.operation(), // WAWebMarkChatAsReadSync.resolveConflicts: from local (e.operation)
                        localMutation.timestamp(), // WAWebMarkChatAsReadSync.resolveConflicts: from local (e.timestamp)
                        localMutation.actionVersion() // WAWebMarkChatAsReadSync.resolveConflicts: from local (e.version)
                );
                // WAWebMarkChatAsReadSync.resolveConflicts: lockForMessageRangeSync -> addActiveMessageRange + $MarkChatAsReadSync$p_1
                // ADAPTED: In WA Web, the merged mutation is applied to the chat DB immediately
                // during conflict resolution via lockForMessageRangeSync. In Cobalt, the merged
                // mutation is returned for the caller to apply, separating resolution from application.
                yield ConflictResolution.merged(merged); // WAWebMarkChatAsReadSync.resolveConflicts: return SkipRemoteAndDropLocal
            }
        };
    }

    /**
     * Builds a pending mutation for marking a chat as read or unread.
     *
     * <p>Per WhatsApp Web {@code WAWebMarkChatAsReadSync.getMarkChatAsReadMutation}:
     * <ol>
     *   <li>Resolves the chat JID for the mutation index via
     *       {@code WAWebSyncdGetChat.getChatJidMutationIndexForChat}</li>
     *   <li>Constructs the outgoing message range via
     *       {@code WAWebMessageRangeUtils.constructMessageRange}</li>
     *   <li>Builds the pending mutation with the mark-chat-as-read action value
     *       via {@code WAWebSyncdActionUtils.buildPendingMutation}</li>
     * </ol>
     *
     * <p>In Cobalt, the message range is passed as a parameter because
     * {@code constructMessageRange} requires store infrastructure that is built
     * at a higher level, mirroring the convention used by
     * {@link ArchiveChatHandler#getArchiveChatMutation}.
     *
     * @implNote WAWebMarkChatAsReadSync.getMarkChatAsReadMutation,
     *           WAWebSyncdActionUtils.buildPendingMutation
     * @param timestamp    the mutation timestamp
     * @param read         {@code true} to mark the chat as read, {@code false} to
     *                     mark it as unread
     * @param chatJid      the JID of the chat
     * @param messageRange the outgoing message range for this action
     * @return the pending mutation for the mark-chat-as-read operation
     */
    public SyncPendingMutation getMarkChatAsReadMutation(
            Instant timestamp,
            boolean read,
            Jid chatJid,
            SyncActionMessageRange messageRange
    ) {
        var action = new MarkChatAsReadActionBuilder() // WAWebMarkChatAsReadSync.getMarkChatAsReadMutation: {markChatAsReadAction: {read: t, messageRange: ...}}
                .read(read) // WAWebMarkChatAsReadSync.getMarkChatAsReadMutation: read: t
                .messageRange(messageRange) // WAWebMarkChatAsReadSync.getMarkChatAsReadMutation: messageRange: constructMessageRange(n, {forOutgoingMutation: true, mutationIndexJid: r})
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: e
                .markChatAsReadAction(action) // WAWebMarkChatAsReadSync.getMarkChatAsReadMutation: {markChatAsReadAction: ...}
                .build();
        var index = JSON.toJSONString(List.of(actionName(), chatJid.toString())); // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs)) where indexArgs = [r]
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, binarySyncAction, version, operation, timestamp, action }
                index,
                value,
                SyncdOperation.SET, // WAWebMarkChatAsReadSync.getMarkChatAsReadMutation: operation: SyncdMutation$SyncdOperation.SET
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
