package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.preference.LabelBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.LabelEditAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles label edit actions.
 *
 * <p>This handler processes mutations that create, update, or delete chat/message labels.
 *
 * <p>Index format: ["label_edit", "labelId"]
 */
public final class LabelEditHandler implements WebAppStateActionHandler {
    public static final LabelEditHandler INSTANCE = new LabelEditHandler();

    private LabelEditHandler() {

    }

    @Override
    public String actionName() {
        return LabelEditAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return LabelEditAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return LabelEditAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!(mutation.value().action().orElse(null) instanceof LabelEditAction action)) {
            return false;
        }

        // Web only supports SET; REMOVE is unsupported
        if (mutation.operation() != SyncdOperation.SET) {
            return true;
        }

        var indexArray = JSON.parseArray(mutation.index());
        var labelId = indexArray.getInteger(1);
        if (labelId == null) {
            return false;
        }

        if (action.deleted()) {
            client.store()
                    .removeLabel(labelId);
        } else {
            var existing = client.store()
                    .findLabel(labelId);
            if (existing.isPresent()) {
                var label = existing.get();
                action.name().ifPresent(label::setName);
                action.color().ifPresent(label::setColor);
            } else {
                var name = action.name().orElse("");
                var color = action.color().orElse(0);
                var label = new LabelBuilder()
                        .id(labelId)
                        .name(name)
                        .color(color)
                        .build();
                client.store()
                        .addLabel(label);
            }
        }

        return true;
    }
}
