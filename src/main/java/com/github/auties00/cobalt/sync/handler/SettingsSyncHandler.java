package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.SettingsSyncAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles settings sync actions.
 *
 * <p>Index format: ["settings_sync", ...]
 */
public final class SettingsSyncHandler implements WebAppStateActionHandler {
    public static final SettingsSyncHandler INSTANCE = new SettingsSyncHandler();

    private SettingsSyncHandler() {

    }

    @Override
    public String actionName() {
        return SettingsSyncAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return SettingsSyncAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return SettingsSyncAction.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web source (WAWebSettingsSync) on SET:
        // - Checks feature flags (settings_sync_enabled primary feature + AB prop)
        // - Deduplicates mutations by index, keeping the latest timestamp per index
        // - Validates index has 4 parts: [actionName, platform, settingKey, scope]
        // - Filters by platform (WEB or HYBRID on Windows)
        // - Maps settingKey to a field name via SETTING_KEY_TO_FIELD lookup
        // - Extracts the field value from settingsSyncAction
        // - Applies via applySettingUpdate which dispatches to the frontend bridge
        //   (applyAppSetting or applyPerChatSetting depending on scope)
        // All effects are web-frontend UI settings, so this is a no-op.
        return true;
    }
}
