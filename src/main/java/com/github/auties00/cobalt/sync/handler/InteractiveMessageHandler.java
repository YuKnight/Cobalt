package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.InteractiveMessageAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles interactive message actions.
 *
 * <p>The WhatsApp Web implementation ({@code WAWebInteractiveMessageSync}) only performs
 * frontend-specific operations (disabling CTA buttons in the UI) with no data model
 * mutations, so this handler is intentionally a no-op.
 *
 * <p>Index format: ["interactive_message_action", "chatJid", "messageId", "fromMe", "participant", "subId"]
 */
public final class InteractiveMessageHandler implements WebAppStateActionHandler {
    public static final InteractiveMessageHandler INSTANCE = new InteractiveMessageHandler();

    private InteractiveMessageHandler() {

    }

    @Override
    public String actionName() {
        return InteractiveMessageAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return InteractiveMessageAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return InteractiveMessageAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return true;
    }
}
