package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.PushNameSetting;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles push name setting changes.
 *
 * <p>This handler processes mutations that update the user's display name (push name).
 */
public final class PushNameSettingHandler implements WebAppStateActionHandler {
    public static final PushNameSettingHandler INSTANCE = new PushNameSettingHandler();

    private PushNameSettingHandler() {

    }

    @Override
    public String actionName() {
        return PushNameSetting.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return PushNameSetting.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return PushNameSetting.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return true;
        }

        if (!(mutation.value().action().orElse(null) instanceof PushNameSetting setting)) {
            return false;
        }

        var name = setting.name().orElse("");

        client.store()
                .setName(name);

        client.store()
                .jid()
                .flatMap(entry -> client.store().findContactByJid(entry.withoutData()))
                .ifPresent(contact -> contact.setChosenName(name));

        return true;
    }
}
