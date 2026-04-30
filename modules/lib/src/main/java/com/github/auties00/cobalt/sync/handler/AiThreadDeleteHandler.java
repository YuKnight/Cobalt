package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.DeviceCapabilities;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles AI thread delete sync actions.
 *
 * <p>Per WhatsApp Web {@code WAWebAiThreadDeleteSync}, this handler extends
 * {@code ChatSyncdActionBase} and processes the {@code "ai_thread_delete"} action.
 * It only supports SET operations. The handler validates that {@code index[1]}
 * is a valid bot WID ({@code isWid} and {@code isBot} checks) and that
 * {@code index[2]} is a non-null, non-whitespace thread ID.
 *
 * <p>Index format: {@code ["ai_thread_delete", chatJid, threadId]}
 *
 * <p>The gating check in WA Web verifies {@code isBotEnabled() && isAiChatThreadsInfraEnabled()},
 * which are AB prop-based runtime checks. In Cobalt, these are adapted to a
 * {@code DeviceCapabilities.AiThread.SupportLevel} check since Cobalt does not
 * have an AB props subsystem.
 *
 * @implNote WAWebAiThreadDeleteSync.default singleton instance of the handler class
 *           that extends {@code WAWebSyncdAction.ChatSyncdActionBase}
 */
@WhatsAppWebModule(moduleName = "WAWebAiThreadDeleteSync")
public final class AiThreadDeleteHandler implements WebAppStateActionHandler {
    /**
     * Logger for this handler.
     *
     * @implNote ADAPTED: WAWebAiThreadDeleteSync uses WALogger; Cobalt uses java.util.logging
     */
    private static final Logger LOGGER = Logger.getLogger(AiThreadDeleteHandler.class.getName());

    /**
     * Canonical WhatsApp Web action name for the AI thread delete action type.
     *
     * @implNote WAWebAiThreadDeleteSync.getAction returns
     *           {@code WASyncdConst.Actions.AiThreadDelete} which is {@code "ai_thread_delete"}
     */
    @WhatsAppWebExport(moduleName = "WAWebAiThreadDeleteSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String ACTION_NAME = "ai_thread_delete";

    /**
     * Canonical WhatsApp Web mutation format version for this action type.
     *
     * @implNote WAWebAiThreadDeleteSync.getVersion returns {@code 7}
     */
    @WhatsAppWebExport(moduleName = "WAWebAiThreadDeleteSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public static final int ACTION_VERSION = 7;

    /**
     * Canonical WhatsApp Web collection name for this action type.
     *
     * @implNote WAWebAiThreadDeleteSync constructor sets {@code collectionName}
     *           to {@code WASyncdConst.CollectionName.RegularHigh}
     */
    @WhatsAppWebExport(moduleName = "WAWebAiThreadDeleteSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR_HIGH;

    /**
     * Singleton instance of this handler.
     *
     * @implNote WAWebAiThreadDeleteSync the module creates a single instance
     *           ({@code new u()}) and exports it as the default export
     */
    @WhatsAppWebExport(moduleName = "WAWebAiThreadDeleteSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final AiThreadDeleteHandler INSTANCE = new AiThreadDeleteHandler();

    /**
     * Constructs the singleton AI thread delete handler.
     *
     * @implNote WAWebAiThreadDeleteSync private constructor for singleton pattern
     */
    @WhatsAppWebExport(moduleName = "WAWebAiThreadDeleteSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private AiThreadDeleteHandler() {

    }

    /**
     * Returns the action type name this handler processes.
     *
     * @implNote WAWebAiThreadDeleteSync.getAction returns
     *           {@code WASyncdConst.Actions.AiThreadDelete} ({@code "ai_thread_delete"})
     * @return the action type name
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAiThreadDeleteSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return ACTION_NAME;
    }

    /**
     * Returns the sync collection this handler's action belongs to.
     *
     * @implNote WAWebAiThreadDeleteSync constructor sets {@code collectionName}
     *           to {@code WASyncdConst.CollectionName.RegularHigh}
     * @return the sync patch type / collection name
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAiThreadDeleteSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for this handler.
     *
     * @implNote WAWebAiThreadDeleteSync.getVersion returns {@code 7}
     * @return the handler's supported mutation version
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAiThreadDeleteSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return ACTION_VERSION;
    }

    /**
     * Applies a single AI thread delete mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is {@link SyncActionState#SUCCESS}.
     *
     * @implNote WAWebAiThreadDeleteSync.applyMutations per-mutation application logic
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAiThreadDeleteSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies an AI thread delete mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebAiThreadDeleteSync.applyMutations}, the per-mutation
     * logic performs the following steps wrapped in a try/catch:
     * <ol>
     *   <li>If operation is not SET, return {@code Unsupported}</li>
     *   <li>Extract {@code indexParts[1]} (chatJid) and {@code indexParts[2]} (threadId)</li>
     *   <li>Validate both are non-null, non-whitespace; chatJid must be a valid Wid</li>
     *   <li>Create Wid from chatJid and verify it {@code isBot()}</li>
     *   <li>Check bot gating: {@code isBotEnabled() && isAiChatThreadsInfraEnabled()}</li>
     *   <li>Create AI thread from mutation index and resolve thread from store</li>
     *   <li>If thread not found, return {@code Orphan} with orphan model</li>
     *   <li>If found, call bulk delete and fire frontend notification, return {@code Success}</li>
     * </ol>
     *
     * <p>Any exception thrown inside the block is caught and reported as
     * {@link SyncActionState#FAILED}, mirroring WA Web's {@code catch(e) { return {actionState: Failed} }}.
     *
     * @implNote WAWebAiThreadDeleteSync.applyMutations the per-mutation handler within
     *           the Promise.all mapping function
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebAiThreadDeleteSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        try {
            if (mutation.operation() != SyncdOperation.SET) {
                return MutationApplicationResult.unsupported();
            }

            var indexArray = JSON.parseArray(mutation.index());
            if (indexArray.size() < 3) {
                return malformedActionIndex();
            }

            var chatJidString = indexArray.getString(1);
            var threadId = indexArray.getString(2);
            if (chatJidString == null || chatJidString.isBlank()
                    || threadId == null || threadId.isBlank()) {
                return malformedActionIndex();
            }

            var chatJid = Jid.of(chatJidString);
            if (!chatJid.isBot()) {
                return malformedActionIndex();
            }

            // ADAPTED: WAWebAiThreadDeleteSync.applyMutations — WA Web checks isBotEnabled() && isAiChatThreadsInfraEnabled()
            // (AB prop-based gating). Cobalt maps this to DeviceCapabilities.AiThread.SupportLevel check
            // since Cobalt does not have an AB props subsystem.
            var aiThreadSupported = client.store().primaryDeviceCapabilities()
                    .flatMap(DeviceCapabilities::aiThread)
                    .flatMap(DeviceCapabilities.AiThread::supportLevel)
                    .filter(level -> level != DeviceCapabilities.AiThread.SupportLevel.NONE)
                    .isPresent();
            if (!aiThreadSupported) {
                return MutationApplicationResult.unsupported();
            }

            // ADAPTED: WAWebAiThreadDeleteSync.applyMutations — WA Web calls
            // createAiThreadFromMutationIndex(botWid, threadId) then resolveThreadForMutationIndex(thread).
            // Cobalt collapses WA Web's ThreadsMetadata IDB table into the aiThreadTitles map.
            var titles = new HashMap<>(client.store().aiThreadTitles());
            var key = chatJidString + "|" + threadId;
            if (!titles.containsKey(key)) {
                // WA Web uses SyncModelType.Thread for orphan model type.
                return MutationApplicationResult.orphan(key, "Thread");
            }

            // ADAPTED: WAWebAiThreadDeleteSync.$AiThreadDeleteSync$p_1 — WA Web calls
            // bulkDeleteThreads(botWid, [thread]) which deletes metadata and messages from IDB,
            // then fires frontendFireAndForget("deleteChatAiThreads", {chatId, threadIds, msgIds}).
            // Cobalt removes from the flat aiThreadTitles store map; the frontend notification
            // is intentionally omitted because Cobalt has no browser frontend bridge.
            titles.remove(key);
            client.store().setAiThreadTitles(titles);
            return MutationApplicationResult.success();
        } catch (Exception e) {
            LOGGER.warning("AI thread delete mutation failed: " + e.getMessage());
            return MutationApplicationResult.failed();
        }
    }

    /**
     * Builds a pending outgoing mutation that deletes an AI thread across
     * linked devices.
     *
     * <p>Per WhatsApp Web {@code WAWebAiThreadDeleteSync}: emits a SET
     * mutation at {@code ["ai_thread_delete", botJid, threadId]} in the
     * REGULAR_HIGH collection with {@code version = 7}. The action has no
     * dedicated sub-message payload the index alone identifies the thread.
     *
     * @implNote WAWebAiThreadDeleteSync outgoing SET mutation. The WA Web
     *           {@code ai_thread_delete} action carries no value payload, so
     *           the {@link SyncActionValueBuilder} is populated with only the
     *           timestamp
     * @param chatJid  the bot JID owning the thread
     * @param threadId the AI thread identifier
     * @return the pending mutation ready to be pushed via
     *         {@link com.github.auties00.cobalt.sync.WebAppStateService#pushPatches}
     */
    @WhatsAppWebExport(moduleName = "WAWebAiThreadDeleteSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getAiThreadDeleteMutation(Jid chatJid, String threadId) {
        var timestamp = Instant.now();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .build();
        var index = JSON.toJSONString(List.of(ACTION_NAME, chatJid.toString(), threadId)); // ["ai_thread_delete", chatJid, threadId]
        var mutation = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                ACTION_VERSION
        );
        return new SyncPendingMutation(mutation, 0);
    }
}
