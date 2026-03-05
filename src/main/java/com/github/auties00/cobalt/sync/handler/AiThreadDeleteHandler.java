package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles AI thread delete actions.
 *
 * <p>Per WhatsApp Web {@code WAWebAiThreadDeleteSync}, this action only supports
 * SET operations. The web client resolves a bot JID from index[1] and a thread ID
 * from index[2], then calls {@code bulkDeleteThreads} to remove the thread from
 * IndexedDB and fires a frontend event. Since these are UI/IndexedDB-specific
 * operations with no equivalent in this client's data model, the mutation is
 * acknowledged but not applied locally.
 *
 * <p>Index format: ["ai_thread_delete", chatJid, threadId]
 */
public final class AiThreadDeleteHandler implements WebAppStateActionHandler {
    /**
     * Canonical WhatsApp Web action name for this action type.
     */
    public static final String ACTION_NAME = "ai_thread_delete";

    /**
     * Canonical WhatsApp Web action version for this action type.
     */
    public static final int ACTION_VERSION = 7;

    /**
     * Canonical WhatsApp Web collection name for this action type.
     */
    public static final SyncPatchType COLLECTION_NAME = SyncPatchType.REGULAR_HIGH;

    public static final AiThreadDeleteHandler INSTANCE = new AiThreadDeleteHandler();

    private AiThreadDeleteHandler() {

    }

    @Override
    public String actionName() {
        return ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return COLLECTION_NAME;
    }

    @Override
    public int version() {
        return ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return true;
    }
}
