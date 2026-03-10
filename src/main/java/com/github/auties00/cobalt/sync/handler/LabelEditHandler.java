package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.preference.LabelBuilder;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.LabelEditAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles label edit actions.
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
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof LabelEditAction action)) {
            return MutationApplicationResult.malformed();
        }

        var labelId = JSON.parseArray(mutation.index()).getString(1);
        if (labelId == null || labelId.isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        if (action.deleted()) {
            client.store().removeLabel(labelId);
            return MutationApplicationResult.success();
        }

        var label = new LabelBuilder()
                .id(labelId)
                .name(action.name().orElse(""))
                .color(action.color().orElse(0))
                .predefinedId(action.predefinedId().isPresent() ? action.predefinedId().getAsInt() : null)
                .orderIndex(action.orderIndex().isPresent() ? action.orderIndex().getAsInt() : null)
                .isActive(action.isActive() ? true : null)
                .type(action.type().orElse(null))
                .isImmutable(action.isImmutable() ? true : null)
                .build();
        client.store().removeLabel(labelId);
        client.store().addLabel(label);
        return MutationApplicationResult.success();
    }
}
