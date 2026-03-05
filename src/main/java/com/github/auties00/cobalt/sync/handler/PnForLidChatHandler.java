package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.chat.PnForLidChatAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles phone number for LID chat actions.
 *
 * <p>This handler processes mutations that associate a phone number with a
 * LID-based chat by registering a LID-to-PN mapping in the store. It mirrors
 * the logic in the WhatsApp Web {@code WAWebPnForLidChatSync} module.
 *
 * <p>Index format: ["pnForLidChat", "lidJid"]
 */
public final class PnForLidChatHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code PnForLidChatHandler}.
     */
    public static final PnForLidChatHandler INSTANCE = new PnForLidChatHandler();

    private PnForLidChatHandler() {

    }

    @Override
    public String actionName() {
        return PnForLidChatAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return PnForLidChatAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return PnForLidChatAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web only supports SET for this action
        if (mutation.operation() != SyncdOperation.SET) {
            return true;
        }

        if (!(mutation.value().action().orElse(null) instanceof PnForLidChatAction action)) {
            return false;
        }

        var indexArray = JSON.parseArray(mutation.index());
        var lidJidString = indexArray.getString(1);
        if (lidJidString == null || lidJidString.isEmpty()) {
            return false;
        }

        var pnJid = action.pnJid().orElse(null);
        if (pnJid == null) {
            return false;
        }

        var lidJid = Jid.of(lidJidString);
        client.store().registerLidMapping(pnJid, lidJid);

        return true;
    }
}
