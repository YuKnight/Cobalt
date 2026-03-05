package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.LabelAssociationAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles label association actions.
 *
 * <p>This handler processes mutations that assign labels to chats or messages.
 *
 * <p>Index format: ["labelAssociationAction", "chatOrMessageJid", "labelId"]
 */
public final class LabelAssociationHandler implements WebAppStateActionHandler {
    public static final LabelAssociationHandler INSTANCE = new LabelAssociationHandler();

    private LabelAssociationHandler() {

    }

    @Override
    public String actionName() {
        return LabelAssociationAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return LabelAssociationAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return LabelAssociationAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!(mutation.value().action().orElse(null) instanceof LabelAssociationAction action)) {
            return false;
        }

        // Web only supports SET; REMOVE is unsupported
        if (mutation.operation() != SyncdOperation.SET) {
            return true;
        }

        var indexArray = JSON.parseArray(mutation.index());
        var targetJidString = indexArray.getString(1);
        var labelId = indexArray.getInteger(2);
        if (targetJidString == null || labelId == null) {
            return false;
        }

        var targetJid = Jid.of(targetJidString);

        var label = client.store()
                .findLabel(labelId);
        if (label.isEmpty()) {
            return false;
        }

        if (action.labeled()) {
            label.get().addAssignment(targetJid);
        } else {
            label.get().removeAssignment(targetJid);
        }

        return true;
    }
}
