package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles device capabilities actions.
 *
 * <p>Index format: ["device_capabilities", "deviceJid"]
 */
public final class DeviceCapabilitiesHandler implements WebAppStateActionHandler {
    public static final DeviceCapabilitiesHandler INSTANCE = new DeviceCapabilitiesHandler();

    private DeviceCapabilitiesHandler() {

    }

    @Override
    public String actionName() {
        return "device_capabilities";
    }

    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR_LOW;
    }

    @Override
    public int version() {
        return 7;
    }

    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web source (WAWebDeviceCapabilitiesSync): on SET, reads value.deviceCapabilities.
        // Extracts the device ID from indexParts[1] (JID suffix before '@').
        // If the device ID is "0" (primary device), maps the protobuf capabilities
        // and merges them into localStorage via mergeDeviceCapabilitiesToStorage.
        // Also monitors LID migration timestamps and schedules logout if needed.
        // No equivalent device capabilities storage exists in the Java data model.
        if (mutation.operation() != SyncdOperation.SET) {
            return true;
        }

        if (mutation.value().deviceCapabilities().isEmpty()) {
            return true;
        }

        return true;
    }
}
