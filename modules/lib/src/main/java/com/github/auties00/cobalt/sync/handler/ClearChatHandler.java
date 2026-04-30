package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.sync.ConflictResolution;
import com.github.auties00.cobalt.model.sync.ConflictResolutionState;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionMessageRange;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.chat.ClearChatAction;
import com.github.auties00.cobalt.model.sync.action.chat.ClearChatActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Instant;
import java.util.List;

/**
 * Handles clear chat sync actions.
 *
 * <p>This handler processes mutations that clear all messages from a chat
 * while keeping the chat itself. Per WhatsApp Web, the clear chat action
 * extends {@code ChatMessageRangeSyncdActionBase} and uses message-range-based
 * conflict resolution.
 *
 * <p>Index format: {@code ["clearChat", chatJid, deleteStarred, deleteMedia]}
 * where {@code deleteStarred} is {@code "1"} (delete starred) or {@code "0"}
 * (keep starred), and {@code deleteMedia} is {@code "1"} (keep media) or
 * {@code "0"} (delete media).
 *
 * @implNote WAWebClearChatSync — singleton instance exported as {@code default};
 *           extends {@code ChatMessageRangeSyncdActionBase} with
 *           {@code collectionName = RegularHigh}, {@code chatJidIndex = 1},
 *           {@code getVersion() = 6}, {@code getAction() = "clearChat"}
 */
@WhatsAppWebModule(moduleName = "WAWebClearChatSync")
public final class ClearChatHandler implements WebAppStateActionHandler {

    /**
     * Singleton instance of the clear chat handler.
     *
     * <p>Per WhatsApp Web, {@code WAWebClearChatSync} exports a single instance
     * ({@code var f = new _(); l.default = f}).
     *
     * @implNote WAWebClearChatSync.default — module-level singleton
     */
    @WhatsAppWebExport(moduleName = "WAWebClearChatSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final ClearChatHandler INSTANCE = new ClearChatHandler();

    /**
     * Index slot of the chat JID inside the mutation index parts array.
     *
     * <p>Per WhatsApp Web {@code WAWebClearChatSync}: the constructor sets
     * {@code chatJidIndex = 1}, so the chat JID is at {@code indexParts[1]}
     * (i.e. after the action name {@code "clearChat"} at slot {@code 0}).
     *
     * @implNote WAWebClearChatSync — class field {@code chatJidIndex = 1}
     */
    @WhatsAppWebExport(moduleName = "WAWebClearChatSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    private static final int CHAT_JID_INDEX = 1;

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @implNote WAWebClearChatSync — class constructor sets
     *           {@code collectionName = RegularHigh}, {@code chatJidIndex = 1}
     */
    @WhatsAppWebExport(moduleName = "WAWebClearChatSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private ClearChatHandler() {
    }

    /**
     * Returns the action name for clear chat actions.
     *
     * @implNote WAWebClearChatSync.getAction — returns
     *           {@code WASyncdConst.Actions.ClearChat} ({@code "clearChat"})
     * @return the action name {@code "clearChat"}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebClearChatSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return ClearChatAction.ACTION_NAME;
    }

    /**
     * Returns the sync collection for clear chat actions.
     *
     * <p>Per WhatsApp Web, the clear chat handler's {@code collectionName} is set to
     * {@code WASyncdConst.CollectionName.RegularHigh} in the constructor.
     *
     * @implNote WAWebClearChatSync.collectionName — set in constructor to
     *           {@code WASyncdConst.CollectionName.RegularHigh}
     * @return {@link SyncPatchType#REGULAR_HIGH}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebClearChatSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return ClearChatAction.COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for clear chat actions.
     *
     * @implNote WAWebClearChatSync.getVersion — returns {@code 6}
     * @return the version number {@code 6}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebClearChatSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return ClearChatAction.ACTION_VERSION;
    }

    /**
     * Applies a clear chat mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is {@link SyncActionState#SUCCESS}.
     *
     * @implNote WAWebClearChatSync.applyMutations — per-mutation inner logic,
     *           success check on the returned {@code SyncActionState}
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebClearChatSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies a clear chat mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebClearChatSync.applyMutations}, for each
     * mutation with {@code operation === "set"}:
     * <ol>
     *   <li>Extracts the index parts: {@code [action, chatJid, deleteStarred, deleteMedia]}</li>
     *   <li>Validates all index parts exist and the chat JID is valid via
     *       {@code WAWebWid.isWid}</li>
     *   <li>Validates the message range via
     *       {@code WAWebMessageRangeUtils.validateMessageRange}</li>
     *   <li>Resolves the chat via
     *       {@code WAWebSyncdGetChat.resolveChatForMutationIndex}</li>
     *   <li>Replaces remote JIDs in the message range via
     *       {@code WAWebMessageRangeUtils.replaceMessageRangeRemoteJid}</li>
     *   <li>Builds a set of starred message keys to skip via
     *       {@code $ClearChatSync$p_1}</li>
     *   <li>Applies the clear via {@code $ClearChatSync$p_2}, which calls
     *       {@code addActiveMessageRange} and {@code clearChat}</li>
     * </ol>
     *
     * <p>Non-{@code SET} operations return {@code Unsupported}. Exceptions are
     * caught and return {@code Failed}.
     *
     * @implNote WAWebClearChatSync.applyMutations — per-mutation inner function
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebClearChatSync", exports = {"applyMutations", "getMessageRange"}, adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        try {
            var indexParts = JSON.parseArray(mutation.index());
            var chatJidString = indexParts.getString(CHAT_JID_INDEX);
            var deleteStarredString = indexParts.getString(CHAT_JID_INDEX + 1);
            var deleteMediaString = indexParts.getString(CHAT_JID_INDEX + 2);

            if (chatJidString == null || chatJidString.isEmpty()
                    || deleteStarredString == null || deleteStarredString.isEmpty()
                    || deleteMediaString == null || deleteMediaString.isEmpty()) {
                return malformedActionIndex();
            }

            Jid chatJid;
            try {
                chatJid = Jid.of(chatJidString);
            } catch (Exception e) {
                return malformedActionIndex(); // ADAPTED: Jid.of throws for invalid JIDs; WA Web uses isWid() upfront
            }

            if (chatJid == null) { // ADAPTED: Jid.of returns null for null input
                return malformedActionIndex();
            }

            if (!(mutation.value().action().orElse(null) instanceof ClearChatAction clearChatAction)) {
                return malformedActionValue();
            }

            var messageRange = clearChatAction.messageRange().orElse(null);
            if (messageRange == null) {
                return malformedActionValue();
            }

            var chat = client.store().findChatByJid(chatJid);
            if (chat.isEmpty()) {
                return MutationApplicationResult.orphan(chatJidString, "Chat");
            }

            // $ClearChatSync$p_2: addActiveMessageRange, clearChat(chatId, messageRange, deleteStarred, starredKeys)
            // clearChat: queryAndRemoveMessagesInMessageRange(chatId, messageRange, {skipStarred: !deleteStarred, skipMessages: starredKeys, deleteAutomatedGreetingMessages: true})
            //            deleteAllThreadsForChat, deleteMessages, maybeClearGroupStatus
            // ADAPTED: WAWebClearChatSync.$ClearChatSync$p_2 performs granular message deletion
            // with starred message skipping, active message range tracking, thread deletion,
            // add-on cleanup, and group status clearing. Cobalt simplifies to Chat.removeMessages()
            // because:
            // 1. addActiveMessageRange is an IndexedDB-specific optimization (browser concern)
            // 2. skipStarred/skipMessages filtering requires per-message iteration against a range
            //    which is not yet supported by the Chat abstraction
            // 3. Thread deletion, add-on cleanup, and AI thread deletion are browser-specific UI concerns
            // 4. maybeClearGroupStatus is a StatusCollection concern not present in Cobalt's store
            chat.get().removeMessages();

            return MutationApplicationResult.success();
        } catch (Exception e) {
            return MutationApplicationResult.failed();
        }
    }

    /**
     * Resolves conflicts between a local pending clear chat mutation and an
     * incoming remote clear chat mutation using message range comparison.
     *
     * <p>Per WhatsApp Web {@code WAWebClearChatSync.resolveConflicts}:
     * <ol>
     *   <li>Decodes the local and remote {@code clearChatAction} values</li>
     *   <li>Compares their message ranges via
     *       {@code WAWebMessageRangeUtils.compareMessageRanges(remote, local)}</li>
     *   <li>Resolves based on the enclosure type:
     *     <ul>
     *       <li>{@code RangeAEnclosesRangeB} (remote encloses local): apply remote, drop local</li>
     *       <li>{@code RangeBEnclosesRangeA} (local encloses remote): skip remote</li>
     *       <li>{@code RangesAreEqual}: timestamp tiebreaker ({@code local <= remote}
     *           means apply remote)</li>
     *       <li>{@code RangesNotEnclosing}: merge the two ranges, apply the merged mutation
     *           to local state via {@code lockForMessageRangeSync}, and return
     *           {@code SKIP_REMOTE_DROP_LOCAL} with the merged mutation</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @implNote WAWebClearChatSync.resolveConflicts
     * @param localMutation  the local pending mutation
     * @param remoteMutation the incoming remote mutation
     * @return the conflict resolution indicating which mutation to keep
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebClearChatSync", exports = "resolveConflicts", adaptation = WhatsAppAdaptation.ADAPTED)
    public ConflictResolution resolveConflicts(DecryptedMutation.Trusted localMutation, DecryptedMutation.Trusted remoteMutation) {
        var localAction = localMutation.value().action()
                .filter(a -> a instanceof ClearChatAction)
                .map(a -> (ClearChatAction) a)
                .orElse(null);
        var remoteAction = remoteMutation.value().action()
                .filter(a -> a instanceof ClearChatAction)
                .map(a -> (ClearChatAction) a)
                .orElse(null);

        if (localAction == null || remoteAction == null) { // ADAPTED: WA Web uses nullthrows which would throw; Cobalt gracefully falls back to apply remote
            return ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL); // ADAPTED: defensive fallback
        }

        var localRange = localAction.messageRange().orElse(null);
        var remoteRange = remoteAction.messageRange().orElse(null);

        if (localRange == null || remoteRange == null) { // ADAPTED: WA Web uses nullthrows; Cobalt gracefully falls back
            return ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL); // ADAPTED: defensive fallback
        }

        return switch (MessageRangeUtils.compareMessageRanges(remoteRange, localRange)) {
            case RANGE_A_ENCLOSES_RANGE_B ->
                    ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL);
            case RANGE_B_ENCLOSES_RANGE_A ->
                    ConflictResolution.of(ConflictResolutionState.SKIP_REMOTE);
            case RANGES_ARE_EQUAL ->
                    localMutation.timestamp().compareTo(remoteMutation.timestamp()) <= 0
                            ? ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL)
                            : ConflictResolution.of(ConflictResolutionState.SKIP_REMOTE);
            case RANGES_NOT_ENCLOSING -> {
                var mergedRange = MessageRangeUtils.mergeMessageRanges(remoteRange, localRange);
                var mergedAction = new ClearChatActionBuilder()
                        .messageRange(mergedRange)
                        .build();
                var mergedValue = new SyncActionValueBuilder()
                        .timestamp(remoteMutation.timestamp())
                        .clearChatAction(mergedAction)
                        .build();
                var merged = new DecryptedMutation.Trusted(
                        localMutation.index(),
                        mergedValue,
                        localMutation.operation(),
                        localMutation.timestamp(),
                        localMutation.actionVersion()
                );
                // ADAPTED: In WA Web, the merged mutation is applied to the chat DB immediately
                // during conflict resolution via lockForMessageRangeSync. In Cobalt, the merged
                // mutation is returned for the caller to apply, separating resolution from application.
                yield ConflictResolution.merged(merged);
            }
        };
    }

    /**
     * Builds a pending mutation that clears a chat's messages.
     *
     * <p>Per WhatsApp Web {@code WAWebClearChatSync.getClearChatMutation}:
     * <pre>{@code
     * getClearChatMutation(timestamp, chatWid, deleteStarred, messageRange, skipLidLookup) {
     *   var indexJid = skipLidLookup ? chatWid.toString()
     *                                : yield getChatJidMutationIndexForChat(chatWid, Actions.ClearChat);
     *   var forwardRange = yield constructForwardMovingMessageRange(chatWid, indexJid);
     *   var indexArgs = [indexJid, deleteStarred ? "1" : "0", deleteMedia ? "1" : "0"];
     *   // merges with any existing pending ClearChat mutation for the same index
     *   return buildPendingMutation({
     *     collection: this.collectionName,
     *     indexArgs,
     *     value: {clearChatAction: {messageRange: forwardRange}},
     *     version: this.getVersion(),
     *     operation: SyncdMutation$SyncdOperation.SET,
     *     timestamp,
     *     action: this.getAction()
     *   });
     * }
     * }</pre>
     *
     * <p>The index format is {@code ["clearChat", chatJid, deleteStarred, deleteMedia]}.
     * Per {@code $ClearChatSync$p_3}, both boolean flags are encoded the same
     * way on the wire: {@code "1"} for {@code true}, {@code "0"} for {@code false}.
     * This matches how WA Web constructs outgoing mutations via $p_3; receivers
     * may apply their own inversion when interpreting the {@code deleteMedia}
     * index slot.
     *
     * <p>In Cobalt, the caller supplies the message range because Cobalt does
     * not maintain the active-message-range infrastructure (browser-specific
     * IndexedDB concern). A {@code null} range is permitted and will result in
     * a mutation without a range; the sync server tolerates this for chats
     * without any messages. The WAM telemetry commit
     * ({@code MdSyncdDogfoodingFeatureUsageWamEvent}) is performed at the caller
     * ({@code WhatsAppClient.clearChat}) since this method has no
     * {@link WamService} handle.
     *
     * @implNote WAWebClearChatSync.getClearChatMutation,
     *           WAWebSyncdActionUtils.buildPendingMutation
     * @param timestamp     the mutation timestamp
     * @param chatJid       the JID of the chat to clear
     * @param deleteStarred whether starred messages should also be deleted
     * @param deleteMedia   whether media files should be deleted (outgoing
     *                      flag written verbatim per {@code $ClearChatSync$p_3})
     * @param messageRange  the message range covering the messages to clear;
     *                      may be {@code null}
     * @return the pending mutation for the clear-chat action
     */
    @WhatsAppWebExport(moduleName = "WAWebClearChatSync", exports = {"getClearChatMutation", "$ClearChatSync$p_3"}, adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getClearChatMutation(
            Instant timestamp,
            Jid chatJid,
            boolean deleteStarred,
            boolean deleteMedia,
            SyncActionMessageRange messageRange
    ) {
        var actionBuilder = new ClearChatActionBuilder();
        if (messageRange != null) {
            actionBuilder.messageRange(messageRange);
        }
        var action = actionBuilder.build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .clearChatAction(action)
                .build();
        // deleteStarred: "1" = delete, "0" = keep; deleteMedia: "1" = keep, "0" = delete (per $p_2 -> s==="1", d==="0")
        var index = JSON.toJSONString(List.of(
                actionName(),
                chatJid.toString(),
                deleteStarred ? "1" : "0",
                deleteMedia ? "1" : "0"
        ));
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0); // ADAPTED: WA Web returns the raw mutation object; Cobalt wraps it in SyncPendingMutation for the outgoing queue
    }

    /**
     * Returns whether the mutation's chat JID is a LID-namespaced JID.
     *
     * <p>Per WhatsApp Web {@code ChatSyncdActionBase.isLidMutation}: reads the
     * mutation index slot at {@code chatJidIndex} (= {@code 1} for clear-chat),
     * constructs a Wid via {@code WAWebWidFactory.createWid}, and returns
     * {@code wid.isLid()}. {@code ChatMessageRangeSyncdActionBase} (the WA Web
     * superclass of {@code WAWebClearChatSync}) inherits this from
     * {@code ChatSyncdActionBase}.
     *
     * @implNote WAWebSyncdAction.ChatSyncdActionBase.isLidMutation — index slot
     *           lookup and {@code WAWebWidFactory.createWid(...).isLid()} check
     * @param mutation the mutation to inspect
     * @return {@code true} when the chat JID at index {@value #CHAT_JID_INDEX}
     *         is in the LID domain, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSyncdAction", exports = "ChatSyncdActionBase", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean isLidMutation(DecryptedMutation.Trusted mutation) {
        try {
            var indexParts = JSON.parseArray(mutation.index());
            var chatJidString = indexParts.getString(CHAT_JID_INDEX);
            if (chatJidString == null || chatJidString.isEmpty()) {
                return false;
            }
            var chatJid = Jid.of(chatJidString);
            return chatJid != null && chatJid.hasLidServer();
        } catch (Exception e) { // ADAPTED: malformed index returns false rather than propagating
            return false;
        }
    }
}
