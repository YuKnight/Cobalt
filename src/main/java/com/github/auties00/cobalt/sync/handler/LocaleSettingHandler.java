package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.LocaleSetting;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles locale setting changes.
 */
public final class LocaleSettingHandler implements WebAppStateActionHandler {
    public static final LocaleSettingHandler INSTANCE = new LocaleSettingHandler();

    private LocaleSettingHandler() {

    }

    @Override
    public String actionName() {
        return LocaleSetting.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return LocaleSetting.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return LocaleSetting.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (client.store().device() != null && client.store().device().platform() == ClientPlatformType.WINDOWS) {
            return MutationApplicationResult.skipped();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof LocaleSetting setting)) {
            return MutationApplicationResult.malformed();
        }

        var newLocale = setting.locale().orElse(null);
        if (newLocale == null) {
            return MutationApplicationResult.skipped();
        }

        var oldLocale = client.store().locale().orElse(null);
        client.store().setLocale(newLocale);
        for (var listener : client.store().listeners()) {
            Thread.startVirtualThread(() -> listener.onLocaleChanged(client, oldLocale, newLocale));
        }
        return MutationApplicationResult.success();
    }
}
