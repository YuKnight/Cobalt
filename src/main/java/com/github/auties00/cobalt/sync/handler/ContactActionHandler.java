package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.contact.ContactAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles contact actions.
 *
 * <p>This handler processes mutations that update contact information
 * (names, profile pictures, etc.).
 *
 * <p>Index format: ["contact", "contactJid"]
 */
public final class ContactActionHandler implements WebAppStateActionHandler {
    public static final ContactActionHandler INSTANCE = new ContactActionHandler();

    private ContactActionHandler() {

    }

    @Override
    public String actionName() {
        return ContactAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return ContactAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return ContactAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!(mutation.value().action().orElse(null) instanceof ContactAction action)) {
            return false;
        }

        var indexArray = JSON.parseArray(mutation.index());
        var contactJidString = indexArray.getString(1);
        if (contactJidString == null || contactJidString.isEmpty()) {
            return false;
        }

        var contactJid = Jid.of(contactJidString);

        // Web skips LID contacts in the regular contact sync handler
        if (contactJid.hasLidServer()) {
            return true;
        }

        switch (mutation.operation()) {
            case SET -> {
                var contact = client.store()
                        .findContactByJid(contactJid)
                        .orElseGet(() -> client.store().addNewContact(contactJid));
                action.fullName().ifPresent(contact::setFullName);
                action.firstName().ifPresent(contact::setShortName);
                action.username().ifPresent(contact::setUsername);
                action.lidJid().ifPresent(lid -> {
                    contact.setLid(lid);
                    client.store().registerLidMapping(contactJid, lid);
                });
            }
            case REMOVE -> {
                var contact = client.store()
                        .findContactByJid(contactJid);
                if (contact.isPresent()) {
                    contact.get().setFullName(null);
                    contact.get().setShortName(null);
                }
            }
        }

        return true;
    }
}
