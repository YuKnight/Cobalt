package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.HashMap;

/**
 * Handles device capabilities actions.
 */
public final class DeviceCapabilitiesHandler implements WebAppStateActionHandler {
    public static final DeviceCapabilitiesHandler INSTANCE = new DeviceCapabilitiesHandler();

    private DeviceCapabilitiesHandler() {

    }

    @Override
    public String actionName() {
        return com.github.auties00.cobalt.model.device.DeviceCapabilities.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return com.github.auties00.cobalt.model.device.DeviceCapabilities.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return com.github.auties00.cobalt.model.device.DeviceCapabilities.ACTION_VERSION;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS;
    }

    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != com.github.auties00.cobalt.model.sync.data.SyncdOperation.SET) {
            return MutationApplicationResult.success();
        }

        var indexArray = JSON.parseArray(mutation.index());
        var deviceJidString = indexArray.size() > 1 ? indexArray.getString(1) : null;
        var capabilities = mutation.value().action().orElse(null) instanceof com.github.auties00.cobalt.model.device.DeviceCapabilities entry
                ? entry
                : null;
        if (capabilities == null || deviceJidString == null || deviceJidString.isBlank()) {
            return MutationApplicationResult.success();
        }

        var states = new HashMap<>(client.store().deviceCapabilitiesStates());
        var previous = states.put(deviceJidString, capabilities);
        client.store().setDeviceCapabilitiesStates(states);
        if (java.util.Objects.equals(previous, capabilities)) {
            return MutationApplicationResult.success();
        }

        var deviceJid = Jid.of(deviceJidString);
        if (deviceJid.device() == 0) {
            client.store().setPrimaryDeviceCapabilities(capabilities);
            capabilities.userHasAvatar().ifPresent(avatar -> client.store().setHasAvatar(avatar.userHasAvatar()));
            capabilities.lidMigration()
                    .flatMap(com.github.auties00.cobalt.model.device.DeviceCapabilities.LIDMigration::chatDbMigrationTimestamp)
                    .ifPresent(client.lidMigrationService()::observeChatDbMigrationTimestamp);
        }
        return MutationApplicationResult.success();
    }
}
