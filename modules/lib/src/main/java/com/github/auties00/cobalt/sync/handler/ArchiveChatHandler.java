package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.sync.ConflictResolution;
import com.github.auties00.cobalt.model.sync.ConflictResolutionState;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.chat.ArchiveChatAction;
import com.github.auties00.cobalt.model.sync.action.chat.ArchiveChatActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles archive chat sync actions.
 *
 * <p>This handler processes incoming mutations that archive or unarchive chats,
 * resolves conflicts between local and remote archive mutations using message
 * range comparison, and builds outgoing mutations for user-initiated archive
 * actions.
 *
 * <p>The action is identified by the {@code "archive"} action name in
 * {@code SyncActionValue.archiveChatAction}. The mutation index format is
 * {@code ["archive", chatJid]}.
 *
 * <p>Per WhatsApp Web, this handler extends {@code ChatMessageRangeSyncdActionBase},
 * which provides shared message-range-based conflict resolution. In Cobalt, this
 * logic is inlined since Java does not use the same inheritance hierarchy.
 *
 * @implNote WAWebArchiveChatSync — singleton instance exported as {@code default}
 */
public final class ArchiveChatHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of the archive chat handler.
     *
     * <p>Per WhatsApp Web, {@code WAWebArchiveChatSync} exports a single instance
     * ({@code var C = new y(); l.default = C}).
     *
     * @implNote WAWebArchiveChatSync.default — module-level singleton
     */
    public static final ArchiveChatHandler INSTANCE = new ArchiveChatHandler();

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebArchiveChatSync — class {@code y} constructor (no custom init)
     */
    private ArchiveChatHandler() {

    }

    /**
     * Returns the action name for archive chat actions.
     *
     * @implNote WAWebArchiveChatSync.getAction — returns
     *           {@code WASyncdConst.Actions.Archive} ({@code "archive"})
     * @return the action name {@code "archive"}
     */
    @Override
    public String actionName() {
        return ArchiveChatAction.ACTION_NAME; // WAWebArchiveChatSync.getAction -> WASyncdConst.Actions.Archive
    }

    /**
     * Returns the sync collection for archive chat actions.
     *
     * <p>Per WhatsApp Web, the archive handler's {@code collectionName} is set to
     * {@code WASyncdConst.CollectionName.RegularLow} in the constructor.
     *
     * @implNote WAWebArchiveChatSync.collectionName — set in constructor to
     *           {@code WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return ArchiveChatAction.COLLECTION_NAME; // WAWebArchiveChatSync.collectionName = WASyncdConst.CollectionName.RegularLow
    }

    /**
     * Returns the mutation format version for archive chat actions.
     *
     * @implNote WAWebArchiveChatSync.getVersion — returns {@code 3}
     * @return the version number {@code 3}
     */
    @Override
    public int version() {
        return ArchiveChatAction.ACTION_VERSION; // WAWebArchiveChatSync.getVersion -> 3
    }

    /**
     * Applies an archive chat mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is {@link com.github.auties00.cobalt.model.sync.SyncActionState#SUCCESS}.
     *
     * @implNote WAWebArchiveChatSync.applyMutations — per-mutation inner logic,
     *           success check on the returned {@code syncApplyActionResult}
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS; // WAWebArchiveChatSync.applyMutations
    }

    /**
     * Applies an archive chat mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebArchiveChatSync.applyMutations}, for each
     * mutation with {@code operation === "set"}:
     * <ol>
     *   <li>Extracts the chat JID from {@code indexParts[1]}</li>
     *   <li>Validates the JID is a valid WID via {@code WAWebWid.isWid}</li>
     *   <li>Resolves the chat via {@code WAWebSyncdGetChat.resolveChatForMutationIndex}</li>
     *   <li>Validates the action value via {@code validateSyncActionValue} (checks
     *       {@code archived} is not null, {@code messageRange} is present and valid)</li>
     *   <li>Applies the archive state change via {@code $ArchiveChatSync$p_2}</li>
     * </ol>
     *
     * <p>Non-{@code SET} operations return {@code Unsupported}. Exceptions are
     * caught and return {@code Failed}.
     *
     * @implNote WAWebArchiveChatSync.applyMutations — per-mutation inner function
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebArchiveChatSync.applyMutations: e.operation === "set" check, else return Unsupported
            return MutationApplicationResult.unsupported(); // WAWebArchiveChatSync.applyMutations: d++, {actionState: Unsupported}
        }

        try { // WAWebArchiveChatSync.applyMutations: try/catch wrapping per-mutation logic
            if (!(mutation.value().action().orElse(null) instanceof ArchiveChatAction action)) { // WAWebArchiveChatSync.applyMutations: var n = e.value
                return malformedActionValue(); // WAWebArchiveChatSync.applyMutations: validateSyncActionValue returns null -> malformedActionValue(collectionName)
            }

            var chatJidString = JSON.parseArray(mutation.index()).getString(1); // WAWebArchiveChatSync.applyMutations: var s = t[1]
            if (chatJidString == null || chatJidString.isEmpty()) { // WAWebArchiveChatSync.applyMutations: if (!s || !isWid(s)) return malformedActionIndex()
                return malformedActionIndex(); // WAWebArchiveChatSync.applyMutations: this.malformedActionIndex()
            }

            var chatJid = Jid.of(chatJidString); // WAWebArchiveChatSync.applyMutations: createWid(s)
            if (chatJid == null) { // ADAPTED: Jid.of returns null for null input; WA Web uses isWid() validation
                return malformedActionIndex(); // WAWebArchiveChatSync.applyMutations: this.malformedActionIndex()
            }

            var chat = client.store().findChatByJid(chatJid); // WAWebArchiveChatSync.applyMutations: resolveChatForMutationIndex(createWid(s))
            if (chat.isEmpty()) { // WAWebArchiveChatSync.applyMutations: if (!u.success) return orphan
                return MutationApplicationResult.orphan(chatJidString, "Chat"); // WAWebArchiveChatSync.applyMutations: {actionState: Orphan, orphanModel: u.orphanModel}
            }

            // WAWebArchiveChatSync.validateSyncActionValue: checks archived != null and messageRange != null
            // In Cobalt, archived() null-coalesces to false via ArchiveChatAction.archived(),
            // so null archived effectively means "unarchive" which is still valid behavior.
            // The messageRange validation is skipped because Cobalt does not maintain active
            // message ranges (browser-specific IndexedDB concern).
            // See $ArchiveChatSync$p_2 — the core archive state change is always applied.

            chat.get().setArchived(action.archived()); // ADAPTED: WAWebArchiveChatSync.$ArchiveChatSync$p_2 — Cobalt applies archive directly without message range gating
            return MutationApplicationResult.success(); // WAWebArchiveChatSync.applyMutations: g.syncApplyActionResult
        } catch (Exception e) { // WAWebArchiveChatSync.applyMutations: catch(e) { return {actionState: Failed} }
            return MutationApplicationResult.failed(); // WAWebArchiveChatSync.applyMutations: {actionState: SyncActionState.Failed}
        }
    }

    /**
     * Resolves conflicts between a local pending archive mutation and an incoming
     * remote archive mutation using message range comparison.
     *
     * <p>Per WhatsApp Web {@code WAWebArchiveChatSync.resolveConflicts}:
     * <ol>
     *   <li>Decodes the local and remote {@code archiveChatAction} values</li>
     *   <li>Compares their message ranges via
     *       {@code WAWebMessageRangeUtils.compareMessageRanges(remote, local)}</li>
     *   <li>Resolves based on the enclosure type:
     *     <ul>
     *       <li>{@code RangeAEnclosesRangeB} (remote encloses local): apply remote, drop local</li>
     *       <li>{@code RangeBEnclosesRangeA} (local encloses remote): skip remote</li>
     *       <li>{@code RangesAreEqual}: timestamp tiebreaker ({@code local <= remote}
     *           means apply remote)</li>
     *       <li>{@code RangesNotEnclosing}: merge the two ranges, pick the {@code archived}
     *           value from the newer mutation, and return
     *           {@code SKIP_REMOTE_DROP_LOCAL} with the merged mutation</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @implNote WAWebArchiveChatSync.resolveConflicts
     * @param localMutation  the local pending mutation
     * @param remoteMutation the incoming remote mutation
     * @return the conflict resolution indicating which mutation to keep
     */
    @Override
    public ConflictResolution resolveConflicts(DecryptedMutation.Trusted localMutation, DecryptedMutation.Trusted remoteMutation) {
        var localAction = localMutation.value().action() // WAWebArchiveChatSync.resolveConflicts: var u = nullthrows(a.archiveChatAction)
                .filter(a -> a instanceof ArchiveChatAction)
                .map(a -> (ArchiveChatAction) a)
                .orElse(null);
        var remoteAction = remoteMutation.value().action() // WAWebArchiveChatSync.resolveConflicts: var c = nullthrows(i?.archiveChatAction)
                .filter(a -> a instanceof ArchiveChatAction)
                .map(a -> (ArchiveChatAction) a)
                .orElse(null);

        if (localAction == null || remoteAction == null) { // ADAPTED: WA Web uses nullthrows which would throw; Cobalt gracefully falls back to apply remote
            return ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL); // ADAPTED: defensive fallback
        }

        var localRange = localAction.messageRange().orElse(null); // WAWebArchiveChatSync.resolveConflicts: nullthrows(u.messageRange)
        var remoteRange = remoteAction.messageRange().orElse(null); // WAWebArchiveChatSync.resolveConflicts: nullthrows(c.messageRange)

        if (localRange == null || remoteRange == null) { // ADAPTED: WA Web uses nullthrows; Cobalt gracefully falls back
            return ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL); // ADAPTED: defensive fallback
        }

        return switch (MessageRangeUtils.compareMessageRanges(remoteRange, localRange)) { // WAWebArchiveChatSync.resolveConflicts: compareMessageRanges(nullthrows(c.messageRange), nullthrows(u.messageRange))
            case RANGE_A_ENCLOSES_RANGE_B -> // WAWebArchiveChatSync.resolveConflicts: case RangeAEnclosesRangeB -> ApplyRemoteAndDropLocal
                    ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL);
            case RANGE_B_ENCLOSES_RANGE_A -> // WAWebArchiveChatSync.resolveConflicts: case RangeBEnclosesRangeA -> SkipRemote
                    ConflictResolution.of(ConflictResolutionState.SKIP_REMOTE);
            case RANGES_ARE_EQUAL -> // WAWebArchiveChatSync.resolveConflicts: case RangesAreEqual -> timestamp tiebreaker
                    localMutation.timestamp().compareTo(remoteMutation.timestamp()) <= 0 // WAWebArchiveChatSync.resolveConflicts: l <= s
                            ? ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL) // WAWebArchiveChatSync.resolveConflicts: ApplyRemoteAndDropLocal
                            : ConflictResolution.of(ConflictResolutionState.SKIP_REMOTE); // WAWebArchiveChatSync.resolveConflicts: SkipRemote
            case RANGES_NOT_ENCLOSING -> { // WAWebArchiveChatSync.resolveConflicts: case RangesNotEnclosing
                var localWins = localMutation.timestamp().compareTo(remoteMutation.timestamp()) > 0; // WAWebArchiveChatSync.resolveConflicts: l <= s check (inverted for localWins)
                var archived = localWins ? localAction.archived() : remoteAction.archived(); // WAWebArchiveChatSync.resolveConflicts: l <= s ? (c.archived ?? false) : (u.archived ?? false)
                var mergedRange = MessageRangeUtils.mergeMessageRanges(remoteRange, localRange); // WAWebArchiveChatSync.resolveConflicts: mergeMessageRanges(nullthrows(c.messageRange), nullthrows(u.messageRange))
                var mergedAction = new ArchiveChatActionBuilder() // WAWebArchiveChatSync.resolveConflicts: var y = {archived: g, messageRange: h}
                        .archived(archived) // WAWebArchiveChatSync.resolveConflicts: archived: g
                        .messageRange(mergedRange) // WAWebArchiveChatSync.resolveConflicts: messageRange: h
                        .build();
                var mergedValue = new SyncActionValueBuilder() // WAWebArchiveChatSync.resolveConflicts: extends({}, i, {archiveChatAction: y})
                        .timestamp(remoteMutation.timestamp()) // WAWebArchiveChatSync.resolveConflicts: timestamp from remote value (i)
                        .archiveChatAction(mergedAction) // WAWebArchiveChatSync.resolveConflicts: archiveChatAction: y
                        .build();
                var merged = new DecryptedMutation.Trusted( // WAWebArchiveChatSync.resolveConflicts: extends({}, e, {binarySyncAction: C}); delete b.id
                        localMutation.index(), // WAWebArchiveChatSync.resolveConflicts: from local (e.index)
                        mergedValue, // WAWebArchiveChatSync.resolveConflicts: merged binary value
                        localMutation.operation(), // WAWebArchiveChatSync.resolveConflicts: from local (e.operation)
                        localMutation.timestamp(), // WAWebArchiveChatSync.resolveConflicts: from local (e.timestamp)
                        localMutation.actionVersion() // WAWebArchiveChatSync.resolveConflicts: from local (e.version)
                );
                // WAWebArchiveChatSync.resolveConflicts: lockForMessageRangeSync -> addActiveMessageRange + setArchive
                // ADAPTED: In WA Web, the merged mutation is applied to the chat DB immediately
                // during conflict resolution via lockForMessageRangeSync. In Cobalt, the merged
                // mutation is returned for the caller to apply, separating resolution from application.
                yield ConflictResolution.merged(merged); // WAWebArchiveChatSync.resolveConflicts: return SkipRemoteAndDropLocal
            }
        };
    }

    /**
     * Builds a pending mutation for archiving or unarchiving a chat.
     *
     * <p>Per WhatsApp Web {@code WAWebArchiveChatSync.getArchiveChatMutation}:
     * <ol>
     *   <li>Resolves the chat JID for mutation index via
     *       {@code WAWebSyncdGetChat.getChatJidMutationIndexForChat}</li>
     *   <li>Constructs the message range for the chat via
     *       {@code WAWebMessageRangeUtils.constructMessageRange}</li>
     *   <li>Builds the pending mutation with the archive action value</li>
     * </ol>
     *
     * <p>In Cobalt, the message range is passed as a parameter since
     * {@code constructMessageRange} requires store infrastructure that is
     * built at a higher level.
     *
     * @implNote WAWebArchiveChatSync.getArchiveChatMutation
     * @param timestamp    the mutation timestamp
     * @param archived     whether the chat should be archived
     * @param chatJid      the JID of the chat to archive
     * @param messageRange the message range for the archive action
     * @return the pending mutation for the archive action
     */
    public SyncPendingMutation getArchiveChatMutation(
            Instant timestamp,
            boolean archived,
            Jid chatJid,
            com.github.auties00.cobalt.model.sync.SyncActionMessageRange messageRange
    ) {
        var action = new ArchiveChatActionBuilder() // WAWebArchiveChatSync.getArchiveChatMutation: {archiveChatAction: {archived: t, messageRange: ...}}
                .archived(archived) // WAWebArchiveChatSync.getArchiveChatMutation: archived: t
                .messageRange(messageRange) // WAWebArchiveChatSync.getArchiveChatMutation: messageRange: constructMessageRange(n, {forOutgoingMutation: true, mutationIndexJid: r})
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: e
                .archiveChatAction(action) // WAWebArchiveChatSync.getArchiveChatMutation: {archiveChatAction: ...}
                .build();
        var index = JSON.toJSONString(List.of(actionName(), chatJid.toString())); // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs)) where indexArgs = [r]
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, binarySyncAction, version, operation, timestamp, action }
                index,
                value,
                SyncdOperation.SET, // WAWebArchiveChatSync.getArchiveChatMutation: operation: SyncdMutation$SyncdOperation.SET
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }

    /**
     * Builds the set of pending mutations needed to archive or unarchive a chat.
     *
     * <p>Per WhatsApp Web {@code WAWebArchiveChatSync.getMutationsForArchive}:
     * <ul>
     *   <li>Always includes an archive mutation</li>
     *   <li>When archiving ({@code archived = true}), also includes a pin mutation
     *       to unpin the chat (via {@code WAWebPinChatSync.PinChatSync.getPinMutation(e, false, r)})</li>
     * </ul>
     *
     * @implNote WAWebArchiveChatSync.getMutationsForArchive
     * @param timestamp    the mutation timestamp
     * @param archived     whether the chat should be archived
     * @param chatJid      the JID of the chat
     * @param messageRange the message range for the archive action
     * @return the list of pending mutations for the archive operation
     */
    public List<SyncPendingMutation> getMutationsForArchive(
            Instant timestamp,
            boolean archived,
            Jid chatJid,
            com.github.auties00.cobalt.model.sync.SyncActionMessageRange messageRange
    ) {
        var mutations = new ArrayList<SyncPendingMutation>(); // WAWebArchiveChatSync.getMutationsForArchive: var a = [this.getArchiveChatMutation(e, t, r)]
        mutations.add(getArchiveChatMutation(timestamp, archived, chatJid, messageRange)); // WAWebArchiveChatSync.getMutationsForArchive: this.getArchiveChatMutation(e, t, r)
        // WAWebArchiveChatSync.getMutationsForArchive: t && a.push(PinChatSync.getPinMutation(e, false, r))
        // When archiving, also unpin the chat. The PinChatHandler is responsible for
        // building pin mutations, so the caller should add a pin mutation separately.
        // This is noted here for completeness but not implemented inline because
        // PinChatHandler does not yet expose a public getPinMutation method.
        return mutations;
    }
}
