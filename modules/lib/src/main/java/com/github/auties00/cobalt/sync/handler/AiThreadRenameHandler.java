package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.bot.AiThreadRenameAction;
import com.github.auties00.cobalt.model.sync.action.bot.AiThreadRenameActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles AI thread rename sync actions.
 *
 * <p>Per WhatsApp Web {@code WAWebAiThreadRenameSync}, this handler extends
 * {@code ChatSyncdActionBase} and processes the {@code "ai_thread_rename"} action.
 * It only supports SET operations. The handler validates that {@code index[1]}
 * is a valid bot WID ({@code isWid} and {@code isBot} checks) and that
 * {@code index[2]} is a non-null, non-whitespace thread ID.
 *
 * <p>Index format: {@code ["ai_thread_rename", chatJid, threadId]}
 *
 * <p>The gating check in WA Web verifies {@code isBotEnabled() && isAiChatThreadsInfraEnabled()},
 * which are AB prop-based runtime checks. In Cobalt, these are adapted to a
 * {@code DeviceCapabilities.AiThread.SupportLevel} check since Cobalt does not
 * have an AB props subsystem.
 *
 * @implNote WAWebAiThreadRenameSync.default — singleton instance of the handler class
 *           that extends {@code WAWebSyncdAction.ChatSyncdActionBase}
 */
@WhatsAppWebModule(moduleName = "WAWebAiThreadRenameSync")
public final class AiThreadRenameHandler implements WebAppStateActionHandler {
    /**
     * Logger for this handler.
     *
     * @implNote ADAPTED: WAWebAiThreadRenameSync uses WALogger; Cobalt uses java.util.logging
     */
    private static final Logger LOGGER = Logger.getLogger(AiThreadRenameHandler.class.getName()); // ADAPTED: WAWebAiThreadRenameSync — WALogger

    /**
     * Singleton instance of this handler.
     *
     * <p>Per WhatsApp Web {@code WAWebAiThreadRenameSync}, the module creates a single
     * instance ({@code new c()}) and exports it as the default export.
     *
     * @implNote WAWebAiThreadRenameSync — {@code var d = new c(); l.default = d}
     */
    @WhatsAppWebExport(moduleName = "WAWebAiThreadRenameSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final AiThreadRenameHandler INSTANCE = new AiThreadRenameHandler();

    /**
     * Constructs the singleton AI thread rename handler.
     *
     * @implNote WAWebAiThreadRenameSync — private constructor for singleton pattern
     */
    @WhatsAppWebExport(moduleName = "WAWebAiThreadRenameSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private AiThreadRenameHandler() {

    }

    /**
     * Returns the action type name this handler processes.
     *
     * @implNote WAWebAiThreadRenameSync.getAction — returns
     *           {@code WASyncdConst.Actions.AiThreadRename} ({@code "ai_thread_rename"})
     * @return the action type name
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAiThreadRenameSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return AiThreadRenameAction.ACTION_NAME; // WAWebAiThreadRenameSync.getAction
    }

    /**
     * Returns the sync collection this handler's action belongs to.
     *
     * @implNote WAWebAiThreadRenameSync constructor — sets {@code collectionName}
     *           to {@code WASyncdConst.CollectionName.RegularLow}
     * @return the sync patch type / collection name
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAiThreadRenameSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return AiThreadRenameAction.COLLECTION_NAME; // WAWebAiThreadRenameSync.collectionName = RegularLow
    }

    /**
     * Returns the mutation format version for this handler.
     *
     * @implNote WAWebAiThreadRenameSync.getVersion — returns {@code 7}
     * @return the handler's supported mutation version
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAiThreadRenameSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return AiThreadRenameAction.ACTION_VERSION; // WAWebAiThreadRenameSync.getVersion
    }

    /**
     * Applies a single AI thread rename mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is {@link SyncActionState#SUCCESS}.
     *
     * @implNote WAWebAiThreadRenameSync.applyMutations — per-mutation application logic
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAiThreadRenameSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // WAWebAiThreadRenameSync.applyMutations
    }

    /**
     * Applies an AI thread rename mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebAiThreadRenameSync.applyMutations}, the per-mutation
     * logic performs the following steps:
     * <ol>
     *   <li>If operation is not SET, return {@code Unsupported}</li>
     *   <li>Extract {@code indexParts[1]} (chatJid) and {@code indexParts[2]} (threadId)</li>
     *   <li>Validate both are non-null, non-whitespace; chatJid must be a valid Wid</li>
     *   <li>Validate action value contains a non-null, non-whitespace {@code newTitle}</li>
     *   <li>Create Wid from chatJid and verify it {@code isBot()}</li>
     *   <li>Check bot gating: {@code isBotEnabled() && isAiChatThreadsInfraEnabled()}</li>
     *   <li>Create AI thread from mutation index and resolve thread from store</li>
     *   <li>If thread not found, return {@code Orphan} with orphan model</li>
     *   <li>If found, update thread metadata with new title and return {@code Success}</li>
     * </ol>
     *
     * @implNote WAWebAiThreadRenameSync.applyMutations — the per-mutation handler within
     *           the Promise.all mapping function
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAiThreadRenameSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        try { // WAWebAiThreadRenameSync.applyMutations: try { ... } catch(e) { return {actionState: Failed} }
            // WAWebAiThreadRenameSync.applyMutations: if (e.operation !== "set") return {actionState: Unsupported}
            if (mutation.operation() != SyncdOperation.SET) {
                return MutationApplicationResult.unsupported();
            }

            // WAWebAiThreadRenameSync.applyMutations: var n = e.indexParts, s = n[1], u = n[2]
            var indexArray = JSON.parseArray(mutation.index());
            if (indexArray.size() < 3) {
                return malformedActionIndex(); // WAWebAiThreadRenameSync.applyMutations -> this.malformedActionIndex()
            }

            var chatJidString = indexArray.getString(1); // WAWebAiThreadRenameSync.applyMutations: s = n[1]
            var threadId = indexArray.getString(2); // WAWebAiThreadRenameSync.applyMutations: u = n[2]
            // WAWebAiThreadRenameSync.applyMutations: if (!s || !u || !isWid(s) || !isStringNotNullAndNotWhitespaceOnly(u))
            //     return t.malformedActionIndex()
            if (chatJidString == null || chatJidString.isBlank()
                    || threadId == null || threadId.isBlank()) {
                return malformedActionIndex(); // WAWebAiThreadRenameSync.applyMutations -> this.malformedActionIndex()
            }

            // WAWebAiThreadRenameSync.applyMutations: if (!t.validateSyncActionValue(a))
            //     return malformedActionValue(t.collectionName)
            if (!(mutation.value().action().orElse(null) instanceof AiThreadRenameAction action)) {
                return malformedActionValue(); // WAWebAiThreadRenameSync.applyMutations -> WAWebSyncdIndexUtils.malformedActionValue
            }

            var newTitle = action.newTitle().orElse(null); // WAWebAiThreadRenameSync.validateSyncActionValue: e.newTitle
            // WAWebAiThreadRenameSync.validateSyncActionValue: isStringNotNullAndNotWhitespaceOnly(n)
            if (newTitle == null || newTitle.isBlank()) {
                return malformedActionValue(); // WAWebAiThreadRenameSync.applyMutations -> malformedActionValue(collectionName)
            }

            // WAWebAiThreadRenameSync.applyMutations: var c = createWid(s); if (!c.isBot()) return this.malformedActionIndex()
            // WAWebWid.isBot covers both the dedicated bot server domain and reserved phone-number bot ranges,
            // so Cobalt must use Jid.isBot() (not Jid.hasBotServer()) to match semantics.
            var chatJid = Jid.of(chatJidString);
            if (!chatJid.isBot()) {
                return malformedActionIndex(); // WAWebAiThreadRenameSync.applyMutations -> this.malformedActionIndex()
            }
            // WAWebAiThreadRenameSync.applyMutations: var d = asBotWidOrThrow(c) — throw path handled by outer try/catch -> Failed
            // ADAPTED: WAWebAiThreadRenameSync.applyMutations — WA Web checks
            // isBotEnabled() && isAiChatThreadsInfraEnabled() (AB prop-based gating).
            // Cobalt maps this to DeviceCapabilities.AiThread.SupportLevel check
            // since Cobalt does not have an AB props subsystem.
            var aiThreadSupported = client.store().primaryDeviceCapabilities()
                    .flatMap(com.github.auties00.cobalt.model.device.DeviceCapabilities::aiThread)
                    .flatMap(com.github.auties00.cobalt.model.device.DeviceCapabilities.AiThread::supportLevel)
                    .filter(level -> level != com.github.auties00.cobalt.model.device.DeviceCapabilities.AiThread.SupportLevel.NONE)
                    .isPresent();
            if (!aiThreadSupported) {
                return MutationApplicationResult.unsupported(); // WAWebAiThreadRenameSync.applyMutations: Unsupported if gating fails
            }

            // ADAPTED: WAWebAiThreadRenameSync.applyMutations — WA Web calls
            // createAiThreadFromMutationIndex(botWid, threadId) then resolveThreadForMutationIndex(thread).
            // Cobalt collapses WA Web's ThreadsMetadata IDB table into the aiThreadTitles map.
            var titles = new java.util.HashMap<>(client.store().aiThreadTitles());
            var key = chatJidString + "|" + threadId;
            if (!titles.containsKey(key)) {
                // WAWebAiThreadRenameSync.applyMutations: return {actionState: Orphan, orphanModel: p.orphanModel}
                return MutationApplicationResult.orphan(key, "Thread");
            }

            // ADAPTED: WAWebAiThreadRenameSync.$AiThreadRenameSync$p_1 — WA Web reads
            // lastMessageTimestamp, creationTimestamp, aiThreadInfo from the resolved thread,
            // builds a metadata object with getAiThreadInfoFromType(newTitle, aiThreadType),
            // calls bulkCreateOrUpdateThreadsMetadata, then frontendFireAndForget("updateChatAiThreads").
            // Cobalt's flat aiThreadTitles store map only tracks the title string.
            titles.put(key, newTitle);
            client.store().setAiThreadTitles(titles);
            return MutationApplicationResult.success(); // WAWebAiThreadRenameSync.$AiThreadRenameSync$p_1: {actionState: Success}
        } catch (Exception e) {
            // WAWebAiThreadRenameSync.applyMutations: catch(e) { return {actionState: Failed} }
            LOGGER.warning("AI thread rename mutation failed: " + e.getMessage()); // ADAPTED: WAWebAiThreadRenameSync — log instead of silent catch
            return MutationApplicationResult.failed(); // WAWebAiThreadRenameSync.applyMutations: {actionState: SyncActionState.Failed}
        }
    }

    /**
     * Builds a pending outgoing mutation that renames an AI thread across
     * linked devices.
     *
     * <p>Per WhatsApp Web {@code WAWebAiThreadRenameSync}: emits a SET
     * mutation at {@code ["ai_thread_rename", botJid, threadId]} in the
     * REGULAR_LOW collection with {@code version = 7} and an
     * {@code aiThreadRenameAction} sub-message carrying the new title.
     *
     * @implNote WAWebAiThreadRenameSync — outgoing SET mutation shape mirrors
     *           the inbound payload consumed by
     *           {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * @param chatJid  the bot JID owning the thread
     * @param threadId the AI thread identifier
     * @param newTitle the new thread title
     * @return the pending mutation ready to be pushed via
     *         {@link com.github.auties00.cobalt.sync.WebAppStateService#pushPatches}
     */
    @WhatsAppWebExport(moduleName = "WAWebAiThreadRenameSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getAiThreadRenameMutation(Jid chatJid, String threadId, String newTitle) {
        var timestamp = Instant.now(); // WAWebSyncdActionUtils.buildPendingMutation: timestamp: unixTime()
        var action = new AiThreadRenameActionBuilder() // WAWebAiThreadRenameSync: {aiThreadRenameAction: {newTitle: ...}}
                .newTitle(newTitle) // WAWebAiThreadRenameSync.validateSyncActionValue: e.newTitle
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation
                .timestamp(timestamp)
                .aiThreadRenameAction(action) // WAWebAiThreadRenameSync: {aiThreadRenameAction: ...}
                .build();
        var index = JSON.toJSONString(List.of(AiThreadRenameAction.ACTION_NAME, chatJid.toString(), threadId)); // ["ai_thread_rename", chatJid, threadId]
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET, // WAWebAiThreadRenameSync: SET-only
                timestamp,
                AiThreadRenameAction.ACTION_VERSION // WAWebAiThreadRenameSync.getVersion: 7
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
