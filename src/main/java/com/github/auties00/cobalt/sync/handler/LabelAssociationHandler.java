package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.preference.LabelBuilder;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.LabelAssociationAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles label association actions.
 *
 * <p>Index format: ["label_jid", "labelId", "chatJid"]
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
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        var indexArray = JSON.parseArray(mutation.index());
        var labelId = indexArray.getString(1);
        var targetJidString = indexArray.getString(2);
        if (labelId == null || labelId.isEmpty() || targetJidString == null || targetJidString.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        if (!(mutation.value().action().orElse(null) instanceof LabelAssociationAction action)) {
            return MutationApplicationResult.malformed();
        }

        var label = client.store()
                .findLabel(labelId)
                .orElseGet(() -> {
                    var newLabel = new LabelBuilder()
                            .id(labelId)
                            .name("")
                            .color(0)
                            .build();
                    client.store().addLabel(newLabel);
                    return newLabel;
                });

        var targetJid = Jid.of(targetJidString);
        if (action.labeled()) {
            label.addAssignment(targetJid);
        } else {
            label.removeAssignment(targetJid);
        }

        return MutationApplicationResult.success();
    }
}
