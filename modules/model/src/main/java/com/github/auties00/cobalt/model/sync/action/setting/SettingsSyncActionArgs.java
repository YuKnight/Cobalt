package com.github.auties00.cobalt.model.sync.action.setting;


import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link SettingsSyncAction}.
 *
 * <p>The sync index produced is
 * {@code ["settings_sync", platform, settingKey, scope]}.
 *
 * <p>The platform identifies the client type (e.g. Web, Hybrid/Windows),
 * the setting key identifies which setting is being synced, and the scope
 * provides additional context (e.g. {@code "app"}).
 *
 * @param platform   the numeric platform identifier string (e.g. WEB, HYBRID from {@code SettingPlatform} enum)
 * @param settingKey the numeric setting key string (from {@code SettingKey} enum)
 * @param scope      the scope or context for the setting (e.g. {@code "app"})
 */
public record SettingsSyncActionArgs(String platform, String settingKey, String scope) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a three-element array encoding the platform, setting key, and scope
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{platform, settingKey, scope};
    }
}
