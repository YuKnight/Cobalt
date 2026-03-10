package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.SettingsSyncAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Handles settings sync actions.
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
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        if (!isSettingsSyncEnabled(client)) {
            return mutations.stream().map(_ -> MutationApplicationResult.unsupported()).toList();
        }

        var latestByIndex = new HashMap<String, DecryptedMutation.Trusted>();
        for (var mutation : mutations) {
            if (mutation.operation() != SyncdOperation.SET) {
                continue;
            }

            var key = mutation.index();
            var existing = latestByIndex.get(key);
            if (existing == null || mutation.timestamp().compareTo(existing.timestamp()) > 0) {
                latestByIndex.put(key, mutation);
            }
        }

        var results = new ArrayList<MutationApplicationResult>(mutations.size());
        for (var mutation : mutations) {
            if (mutation.operation() != SyncdOperation.SET) {
                results.add(MutationApplicationResult.unsupported());
                continue;
            }

            var latest = latestByIndex.get(mutation.index());
            if (latest == null) {
                results.add(MutationApplicationResult.malformed());
                continue;
            }

            if (latest != mutation) {
                results.add(MutationApplicationResult.skipped());
                continue;
            }

            results.add(applyMutationResult(client, mutation));
        }

        return results;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (!isSettingsSyncEnabled(client)) {
            return MutationApplicationResult.unsupported();
        }

        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        var indexArray = JSON.parseArray(mutation.index());
        if (indexArray.size() != 4) {
            return MutationApplicationResult.malformed();
        }

        if (!(mutation.value().action().orElse(null) instanceof SettingsSyncAction action)) {
            return MutationApplicationResult.malformed();
        }

        var platformValue = indexArray.getString(1);
        var settingKeyValue = indexArray.getString(2);
        var scope = indexArray.getString(3);
        if (platformValue == null || settingKeyValue == null || scope == null) {
            return MutationApplicationResult.malformed();
        }

        var platform = parsePlatform(platformValue);
        var settingKey = parseSettingKey(settingKeyValue);
        if (platform == null || settingKey == null) {
            return MutationApplicationResult.malformed();
        }

        if (!appliesToCurrentPlatform(client, platform)) {
            return MutationApplicationResult.skipped();
        }

        var states = new HashMap<>(client.store().settingsSyncStates());
        var aggregateKey = "%s|%s".formatted(platform.name(), scope);
        var aggregate = mergeSetting(states.get(aggregateKey), action, settingKey);
        states.put(aggregateKey, aggregate);
        client.store().setSettingsSyncStates(states);
        applySettingLocally(client, action, settingKey, scope);
        return MutationApplicationResult.success();
    }

    private boolean isSettingsSyncEnabled(WhatsAppClient client) {
        return client.store().primaryFeatures().contains("settings_sync_enabled")
                && client.abPropsService().getBool(ABProp.SETTINGS_SYNC_ENABLED);
    }

    private SettingsSyncAction.SettingPlatform parsePlatform(String value) {
        try {
            var index = Integer.parseInt(value);
            for (var platform : SettingsSyncAction.SettingPlatform.values()) {
                if (platform.index() == index) {
                    return platform;
                }
            }
            return null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private SettingsSyncAction.SettingKey parseSettingKey(String value) {
        try {
            var index = Integer.parseInt(value);
            for (var settingKey : SettingsSyncAction.SettingKey.values()) {
                if (settingKey.index() == index) {
                    return settingKey;
                }
            }
            return null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean appliesToCurrentPlatform(WhatsAppClient client, SettingsSyncAction.SettingPlatform platform) {
        return switch (platform) {
            case WEB -> true;
            case HYBRID -> client.store().device() != null && client.store().device().platform() == ClientPlatformType.WINDOWS;
            default -> false;
        };
    }

    private void applySettingLocally(
            WhatsAppClient client,
            SettingsSyncAction action,
            SettingsSyncAction.SettingKey settingKey,
            String scope
    ) {
        if (!"app".equals(scope)) {
            return;
        }

        switch (settingKey) {
            case LANGUAGE -> action.language().ifPresent(client.store()::setLocale);
            case DISABLE_LINK_PREVIEWS -> client.store().setDisableLinkPreviews(action.disableLinkPreviews());
            default -> {
            }
        }
    }

    private SettingsSyncAction mergeSetting(
            SettingsSyncAction current,
            SettingsSyncAction incoming,
            SettingsSyncAction.SettingKey settingKey
    ) {
        var target = current != null ? current : incoming;
        switch (settingKey) {
            case START_AT_LOGIN -> target.setStartAtLogin(incoming.startAtLogin());
            case MINIMIZE_TO_TRAY -> target.setMinimizeToTray(incoming.minimizeToTray());
            case LANGUAGE -> incoming.language().ifPresent(target::setLanguage);
            case REPLACE_TEXT_WITH_EMOJI -> target.setReplaceTextWithEmoji(incoming.replaceTextWithEmoji());
            case BANNER_NOTIFICATION_DISPLAY_MODE -> incoming.bannerNotificationDisplayMode()
                    .ifPresent(target::setBannerNotificationDisplayMode);
            case UNREAD_COUNTER_BADGE_DISPLAY_MODE -> incoming.unreadCounterBadgeDisplayMode()
                    .ifPresent(target::setUnreadCounterBadgeDisplayMode);
            case IS_MESSAGES_NOTIFICATION_ENABLED -> target.setMessagesNotificationEnabled(incoming.isMessagesNotificationEnabled());
            case IS_CALLS_NOTIFICATION_ENABLED -> target.setCallsNotificationEnabled(incoming.isCallsNotificationEnabled());
            case IS_REACTIONS_NOTIFICATION_ENABLED -> target.setReactionsNotificationEnabled(incoming.isReactionsNotificationEnabled());
            case IS_STATUS_REACTIONS_NOTIFICATION_ENABLED -> target.setStatusReactionsNotificationEnabled(incoming.isStatusReactionsNotificationEnabled());
            case IS_TEXT_PREVIEW_FOR_NOTIFICATION_ENABLED -> target.setTextPreviewForNotificationEnabled(incoming.isTextPreviewForNotificationEnabled());
            case DEFAULT_NOTIFICATION_TONE_ID -> incoming.defaultNotificationToneId()
                    .ifPresent(target::setDefaultNotificationToneId);
            case GROUP_DEFAULT_NOTIFICATION_TONE_ID -> incoming.groupDefaultNotificationToneId()
                    .ifPresent(target::setGroupDefaultNotificationToneId);
            case APP_THEME -> incoming.appTheme()
                    .ifPresent(target::setAppTheme);
            case WALLPAPER_ID -> incoming.wallpaperId()
                    .ifPresent(target::setWallpaperId);
            case IS_DOODLE_WALLPAPER_ENABLED -> target.setDoodleWallpaperEnabled(incoming.isDoodleWallpaperEnabled());
            case FONT_SIZE -> incoming.fontSize()
                    .ifPresent(target::setFontSize);
            case IS_PHOTOS_AUTODOWNLOAD_ENABLED -> target.setPhotosAutodownloadEnabled(incoming.isPhotosAutodownloadEnabled());
            case IS_AUDIOS_AUTODOWNLOAD_ENABLED -> target.setAudiosAutodownloadEnabled(incoming.isAudiosAutodownloadEnabled());
            case IS_VIDEOS_AUTODOWNLOAD_ENABLED -> target.setVideosAutodownloadEnabled(incoming.isVideosAutodownloadEnabled());
            case IS_DOCUMENTS_AUTODOWNLOAD_ENABLED -> target.setDocumentsAutodownloadEnabled(incoming.isDocumentsAutodownloadEnabled());
            case DISABLE_LINK_PREVIEWS -> target.setDisableLinkPreviews(incoming.disableLinkPreviews());
            case NOTIFICATION_TONE_ID -> incoming.notificationToneId()
                    .ifPresent(target::setNotificationToneId);
            case MEDIA_UPLOAD_QUALITY -> incoming.mediaUploadQuality()
                    .ifPresent(target::setMediaUploadQuality);
            case IS_SPELL_CHECK_ENABLED -> target.setSpellCheckEnabled(incoming.isSpellCheckEnabled());
            case IS_ENTER_TO_SEND_ENABLED -> target.setEnterToSendEnabled(incoming.isEnterToSendEnabled());
            case IS_GROUP_MESSAGE_NOTIFICATION_ENABLED -> target.setGroupMessageNotificationEnabled(incoming.isGroupMessageNotificationEnabled());
            case IS_GROUP_REACTIONS_NOTIFICATION_ENABLED -> target.setGroupReactionsNotificationEnabled(incoming.isGroupReactionsNotificationEnabled());
            case IS_STATUS_NOTIFICATION_ENABLED -> target.setStatusNotificationEnabled(incoming.isStatusNotificationEnabled());
            case STATUS_NOTIFICATION_TONE_ID -> incoming.statusNotificationToneId()
                    .ifPresent(target::setStatusNotificationToneId);
            case SHOULD_PLAY_SOUND_FOR_CALL_NOTIFICATION -> target.setShouldPlaySoundForCallNotification(incoming.shouldPlaySoundForCallNotification());
            case SETTING_KEY_UNKNOWN -> {
            }
        }
        return target;
    }
}
