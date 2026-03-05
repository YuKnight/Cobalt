package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.bot.AiThreadRenameAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles AI thread rename actions.
 *
 * <p>Per WhatsApp Web {@code WAWebAiThreadRenameSync}, this action only supports
 * SET operations. The web client validates that {@code aiThreadRenameAction.newTitle}
 * is non-null and non-whitespace, resolves a bot JID from index[1] and thread ID
 * from index[2], then calls {@code bulkCreateOrUpdateThreadsMetadata} to update
 * the thread title in IndexedDB and fires a frontend event. Since these are
 * UI/IndexedDB-specific operations with no equivalent in this client's data model,
 * the mutation is acknowledged but not applied locally.
 *
 * <p>Index format: ["ai_thread_rename", chatJid, threadId]
 */
public final class AiThreadRenameHandler implements WebAppStateActionHandler {
    public static final AiThreadRenameHandler INSTANCE = new AiThreadRenameHandler();

    private AiThreadRenameHandler() {

    }

    @Override
    public String actionName() {
        return AiThreadRenameAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return AiThreadRenameAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return AiThreadRenameAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return true;
    }
}
