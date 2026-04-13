package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

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
 * @implNote WAWebAiThreadDeleteSync.default — singleton instance of the handler class
 *           that extends {@code WAWebSyncdAction.ChatSyncdActionBase}
 */
public final class AiThreadDeleteHandler implements WebAppStateActionHandler {
    /**
     * Canonical WhatsApp Web action name for the AI thread delete action type.
     *
     * @implNote WAWebAiThreadDeleteSync.getAction — returns
     *           {@code WASyncdConst.Actions.AiThreadDelete} which is {@code "ai_thread_delete"}
     */
    public static final String ACTION_NAME = "ai_thread_delete";

    /**
     * Canonical WhatsApp Web mutation format version for this action type.
     *
     * @implNote WAWebAiThreadDeleteSync.getVersion — returns {@code 7}
     */
    public static final int ACTION_VERSION = 7;

    /**
     * Canonical WhatsApp Web collection name for this action type.
     *
     * @implNote WAWebAiThreadDeleteSync constructor — sets {@code collectionName}
     *           to {@code WASyncdConst.CollectionName.RegularHigh}
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR_HIGH;

    /**
     * Singleton instance of this handler.
     *
     * @implNote WAWebAiThreadDeleteSync — the module creates a single instance
     *           ({@code new u()}) and exports it as the default export
     */
    public static final AiThreadDeleteHandler INSTANCE = new AiThreadDeleteHandler();

    /**
     * Constructs the singleton AI thread delete handler.
     *
     * @implNote WAWebAiThreadDeleteSync — private constructor for singleton pattern
     */
    private AiThreadDeleteHandler() {

    }

    /**
     * Returns the action type name this handler processes.
     *
     * @implNote WAWebAiThreadDeleteSync.getAction — returns
     *           {@code WASyncdConst.Actions.AiThreadDelete} ({@code "ai_thread_delete"})
     * @return the action type name
     */
    @Override
    public String actionName() {
        return ACTION_NAME;
    }

    /**
     * Returns the sync collection this handler's action belongs to.
     *
     * @implNote WAWebAiThreadDeleteSync constructor — sets {@code collectionName}
     *           to {@code WASyncdConst.CollectionName.RegularHigh}
     * @return the sync patch type / collection name
     */
    @Override
    public SyncPatchType collectionName() {
        return COLLECTION_NAME;
    }

    /**
     * Returns the mutation format version for this handler.
     *
     * @implNote WAWebAiThreadDeleteSync.getVersion — returns {@code 7}
     * @return the handler's supported mutation version
     */
    @Override
    public int version() {
        return ACTION_VERSION;
    }

    /**
     * Applies a single AI thread delete mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is {@link SyncActionState#SUCCESS}.
     *
     * @implNote WAWebAiThreadDeleteSync.applyMutations — per-mutation application logic
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS;
    }

    /**
     * Applies an AI thread delete mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebAiThreadDeleteSync.applyMutations}, the per-mutation
     * logic performs the following steps:
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
     * @implNote WAWebAiThreadDeleteSync.applyMutations — the per-mutation handler within
     *           the Promise.all mapping function
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // WAWebAiThreadDeleteSync.applyMutations: if (e.operation !== "set") return Unsupported
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        // WAWebAiThreadDeleteSync.applyMutations: extract indexParts[1] (chatJid) and indexParts[2] (threadId)
        var indexArray = JSON.parseArray(mutation.index());
        if (indexArray.size() < 3) {
            return malformedActionIndex(); // WAWebAiThreadDeleteSync.applyMutations -> this.malformedActionIndex()
        }

        var chatJidString = indexArray.getString(1);
        var threadId = indexArray.getString(2);
        // WAWebAiThreadDeleteSync.applyMutations: !n || !l || !isWid(n) || !isStringNotNullAndNotWhitespaceOnly(l)
        if (chatJidString == null || chatJidString.isBlank()
                || threadId == null || threadId.isBlank()) {
            return malformedActionIndex(); // WAWebAiThreadDeleteSync.applyMutations -> this.malformedActionIndex()
        }

        var chatJid = Jid.of(chatJidString);
        // WAWebAiThreadDeleteSync.applyMutations: if (!s.isBot()) return this.malformedActionIndex()
        if (!chatJid.hasBotServer()) {
            return malformedActionIndex(); // WAWebAiThreadDeleteSync.applyMutations -> this.malformedActionIndex()
        }

        // ADAPTED: WAWebAiThreadDeleteSync.applyMutations — WA Web checks isBotEnabled() && isAiChatThreadsInfraEnabled()
        // (AB prop-based gating). Cobalt maps this to DeviceCapabilities.AiThread.SupportLevel check
        // since Cobalt does not have an AB props subsystem.
        var aiThreadSupported = client.store().primaryDeviceCapabilities()
                .flatMap(com.github.auties00.cobalt.model.device.DeviceCapabilities::aiThread)
                .flatMap(com.github.auties00.cobalt.model.device.DeviceCapabilities.AiThread::supportLevel)
                .filter(level -> level != com.github.auties00.cobalt.model.device.DeviceCapabilities.AiThread.SupportLevel.NONE)
                .isPresent();
        if (!aiThreadSupported) {
            return MutationApplicationResult.unsupported(); // WAWebAiThreadDeleteSync.applyMutations: Unsupported if gating fails
        }

        // ADAPTED: WAWebAiThreadDeleteSync.applyMutations — WA Web calls
        // createAiThreadFromMutationIndex(botWid, threadId) then resolveThreadForMutationIndex(thread).
        // Cobalt collapses WA Web's ThreadsMetadata IDB table into the aiThreadTitles map.
        var titles = new java.util.HashMap<>(client.store().aiThreadTitles());
        var key = chatJidString + "|" + threadId;
        if (!titles.containsKey(key)) {
            // WAWebAiThreadDeleteSync.applyMutations: return {actionState: Orphan, orphanModel: d.orphanModel}
            return MutationApplicationResult.orphan(key, "Thread");
        }

        // ADAPTED: WAWebAiThreadDeleteSync.$AiThreadDeleteSync$p_1 — WA Web calls
        // bulkDeleteThreads(botWid, [thread]) which deletes metadata and messages from IDB,
        // then fires frontendFireAndForget("deleteChatAiThreads", ...).
        // Cobalt removes from the flat aiThreadTitles store map.
        titles.remove(key);
        client.store().setAiThreadTitles(titles);
        return MutationApplicationResult.success(); // WAWebAiThreadDeleteSync.$AiThreadDeleteSync$p_1: Success
    }
}
