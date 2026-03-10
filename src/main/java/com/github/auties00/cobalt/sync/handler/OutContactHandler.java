package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.OutContactAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

public final class OutContactHandler implements WebAppStateActionHandler {
    public static final OutContactHandler INSTANCE = new OutContactHandler();

    private OutContactHandler() {

    }

    @Override
    public String actionName() {
        return OutContactAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return OutContactAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return OutContactAction.ACTION_VERSION;
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
        if (indexArray.size() < 2) {
            return MutationApplicationResult.malformed();
        }

        var userJidString = indexArray.getString(1);
        if (userJidString == null || userJidString.isBlank()) {
            return MutationApplicationResult.malformed();
        }

        if (!(mutation.value().action().orElse(null) instanceof OutContactAction action)) {
            return MutationApplicationResult.malformed();
        }

        var contact = client.store().findContactByJid(Jid.of(userJidString))
                .orElseGet(() -> client.store().addNewContact(Jid.of(userJidString)));
        action.fullName().ifPresent(value -> {
            contact.setFullName(value);
            if (contact.chosenName().isEmpty()) {
                contact.setChosenName(value);
            }
        });
        action.firstName().ifPresent(value -> {
            contact.setShortName(value);
            if (contact.chosenName().isEmpty()) {
                contact.setChosenName(value);
            }
        });
        return MutationApplicationResult.success();
    }
}
