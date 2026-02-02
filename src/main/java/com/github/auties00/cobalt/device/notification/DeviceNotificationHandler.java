package com.github.auties00.cobalt.device.notification;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.device.info.DeviceConstants;
import com.github.auties00.cobalt.device.info.DeviceInfo;
import com.github.auties00.cobalt.device.info.DeviceList;
import com.github.auties00.cobalt.model.auth.KeyIndexList;
import com.github.auties00.cobalt.model.auth.KeyIndexListSpec;
import com.github.auties00.cobalt.model.auth.SignedKeyIndexListSpec;
import com.github.auties00.cobalt.model.info.ChatMessageInfoBuilder;
import com.github.auties00.cobalt.model.info.MessageInfoStubType;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.model.ChatMessageKey;
import com.github.auties00.cobalt.model.message.model.ChatMessageKeyBuilder;
import com.github.auties00.cobalt.model.message.model.MessageStatus;
import com.github.auties00.cobalt.node.Node;
import it.auties.protobuf.exception.ProtobufDeserializationException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles real-time device add/remove notifications.
 * <p>
 * Device notifications are sent when:
 * - A new companion device is added to an account
 * - A companion device is removed from an account
 * <p>
 * This allows clients to update their cached device lists without polling.
 */
public final class DeviceNotificationHandler {
    private static final System.Logger LOGGER = System.getLogger("DeviceNotificationHandler");

    private DeviceNotificationHandler() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Handles a device notification (add or remove).
     *
     * @param client the WhatsApp client
     * @param node   the notification node
     * @param action the action type ("add" or "remove")
     * @param userJid the user JID
     */
    public static void handleDeviceNotification(
            WhatsAppClient client,
            Node node,
            String action,
            Jid userJid
    ) {
        Objects.requireNonNull(client, "client cannot be null");
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(action, "action cannot be null");
        Objects.requireNonNull(userJid, "userJid cannot be null");

        // Parse device list and key index list from notification
        var deviceListNode = node.getChild("device-list").orElse(null);
        var keyIndexListNode = node.getChild("key-index-list").orElse(null);

        if (keyIndexListNode == null) {
            LOGGER.log(System.Logger.Level.WARNING, "Device notification missing key-index-list for {0}", userJid);
            return;
        }

        var timestamp = keyIndexListNode.getAttributeAsLong("ts", null);
        if (timestamp == null) {
            LOGGER.log(System.Logger.Level.WARNING, "Device notification missing timestamp for {0}", userJid);
            return;
        }

        switch (action) {
            case "add" -> handleDeviceAdd(client, userJid, deviceListNode, keyIndexListNode, timestamp);
            case "remove" -> handleDeviceRemove(client, userJid, keyIndexListNode, timestamp);
            default -> LOGGER.log(System.Logger.Level.WARNING, "Unknown device action: {0}", action);
        }
    }

    /**
     * Handles a device add notification.
     * Validates the signed key index bytes and updates the device list.
     */
    private static void handleDeviceAdd(
            WhatsAppClient client,
            Jid userJid,
            Node deviceListNode,
            Node keyIndexListNode,
            long timestamp
    ) {
        // Validate signed key index bytes
        var signedKeyIndexBytes = keyIndexListNode.toContentBytes();
        if (signedKeyIndexBytes.isEmpty()) {
            LOGGER.log(System.Logger.Level.WARNING, "Device add notification missing signedKeyIndexBytes for {0}", userJid);
            // Can't validate without signature - clear cache to force full refresh
            client.store().removeDeviceList(userJid);
            return;
        }

        // Parse device list
        if (deviceListNode == null) {
            LOGGER.log(System.Logger.Level.WARNING, "Device add notification missing device-list for {0}", userJid);
            return;
        }

        // Build key index map
        var keyIndexMap = buildKeyIndexMap(keyIndexListNode);

        // Feature 8: Validate key index list from protobuf
        var validatedKeyIndexInfo = validateKeyIndexList(keyIndexListNode);

        // Parse devices from device-list node with key index validation
        var devices = deviceListNode.streamChildren("device")
                .flatMap(deviceNode -> {
                    var id = deviceNode.getAttributeAsInt("id");
                    if (id.isEmpty()) {
                        return java.util.stream.Stream.empty();
                    }

                    var deviceId = id.getAsInt();
                    var keyIndex = keyIndexMap.getOrDefault(deviceId, 0);

                    // Feature 8: Validate keyIndex against validIndexes
                    if (validatedKeyIndexInfo != null && validatedKeyIndexInfo.validIndexes() != null && !validatedKeyIndexInfo.validIndexes().isEmpty()) {
                        if (keyIndex != 0 && !validatedKeyIndexInfo.validIndexes().contains(keyIndex)) {
                            LOGGER.log(System.Logger.Level.WARNING,
                                    "Device {0} has keyIndex {1} not in validIndexes {2}, excluding from notification",
                                    deviceId, keyIndex, validatedKeyIndexInfo.validIndexes());
                            return java.util.stream.Stream.<DeviceInfo>empty();
                        }
                    }

                    if (deviceId == DeviceConstants.HOSTED_DEVICE_ID) {
                        return java.util.stream.Stream.of(DeviceInfo.hosted(keyIndex));
                    } else {
                        return java.util.stream.Stream.of(DeviceInfo.e2ee(deviceId, keyIndex));
                    }
                })
                .toList();

        // Parse additional metadata
        var expectedTs = keyIndexListNode.getAttributeAsLong("expected_ts", null);
        var rawId = validatedKeyIndexInfo != null
                ? String.valueOf(validatedKeyIndexInfo.rawId())
                : String.valueOf(timestamp);
        var advAccountType = parseAdvAccountType(keyIndexListNode).orElse(null);
        var currentIndex = validatedKeyIndexInfo != null ? validatedKeyIndexInfo.currentIndex() : 0;
        var validIndexes = validatedKeyIndexInfo != null && validatedKeyIndexInfo.validIndexes() != null
                ? validatedKeyIndexInfo.validIndexes()
                : List.<Integer>of();

        // Create new device list
        var newDeviceList = DeviceList.of(
                userJid,
                devices,
                java.time.Duration.ofDays(1),
                rawId,
                advAccountType,
                expectedTs,
                currentIndex,
                validIndexes
        );

        // Check for identity changes and account type transitions
        var cachedList = client.store().findDeviceList(userJid);
        if (cachedList.isPresent()) {
            // Feature 9: Detect account type transitions
            if (newDeviceList.hasAccountTypeChanged(cachedList.get())) {
                var oldType = cachedList.get().advAccountType();
                var newType = newDeviceList.advAccountType();
                handleAccountTypeTransition(client, userJid, oldType, newType, cachedList.get());
            }

            var changes = newDeviceList.mismatch(cachedList.get());
            if (!changes.identityChangedDevices().isEmpty()) {
                // Feature 7: Mark devices and cleanup stale Signal sessions
                for (var changedDevice : changes.identityChangedDevices()) {
                    client.store().markIdentityChange(changedDevice);
                    client.store().cleanupSignalSessionsForDevice(changedDevice);
                }

                // Notify listeners about identity changes
                for (var listener : client.store().listeners()) {
                    Thread.startVirtualThread(() ->
                            listener.onDeviceIdentityChanged(client, userJid, changes.identityChangedDevices())
                    );
                }
            }

            // Feature 7: Cleanup sessions for removed devices
            if (!changes.removedDevices().isEmpty()) {
                for (var removedDevice : changes.removedDevices()) {
                    client.store().cleanupSignalSessionsForDevice(removedDevice);
                }
            }
        }

        // Store updated device list
        client.store().addDeviceList(newDeviceList);

        // Notify listeners about device list change
        for (var listener : client.store().listeners()) {
            Thread.startVirtualThread(() ->
                    listener.onDeviceListChanged(client, userJid, "add")
            );
        }

        LOGGER.log(System.Logger.Level.DEBUG, "Device added for {0}: {1} devices", userJid, devices.size());
    }

    /**
     * Handles a device remove notification.
     * Updates the device list by removing devices based on the key index list.
     */
    private static void handleDeviceRemove(
            WhatsAppClient client,
            Jid userJid,
            Node keyIndexListNode,
            long timestamp
    ) {
        // For remove notifications, we need to get the current device list
        var cachedList = client.store().findDeviceList(userJid);
        if (cachedList.isEmpty()) {
            // No cached list - nothing to remove from
            LOGGER.log(System.Logger.Level.DEBUG, "No cached device list for {0}, ignoring remove", userJid);
            return;
        }

        var oldList = cachedList.get();

        // Build key index map from notification
        var validKeyIndexes = buildKeyIndexMap(keyIndexListNode);

        // Filter devices to keep only those with valid key indexes
        var remainingDevices = oldList.devices().stream()
                .filter(device -> validKeyIndexes.containsKey(device.id()))
                .toList();

        // Feature 7: Cleanup Signal sessions for removed devices
        var removedDevices = oldList.devices().stream()
                .filter(device -> !validKeyIndexes.containsKey(device.id()))
                .toList();
        for (var removedDevice : removedDevices) {
            var deviceJid = removedDevice.toDeviceJid(userJid.user(), userJid.server());
            client.store().cleanupSignalSessionsForDevice(deviceJid);
        }

        // If all devices were removed, just clear the cache
        if (remainingDevices.isEmpty()) {
            client.store().removeDeviceList(userJid);

            // Notify listeners
            for (var listener : client.store().listeners()) {
                Thread.startVirtualThread(() ->
                        listener.onDeviceListChanged(client, userJid, "remove")
                );
            }

            LOGGER.log(System.Logger.Level.DEBUG, "All devices removed for {0}", userJid);
            return;
        }

        // Create updated device list with remaining devices
        var newTimestamp = Instant.ofEpochMilli(timestamp);
        var expectedTs = keyIndexListNode.getAttributeAsLong("expected_ts", null);

        var updatedList = new DeviceList(
                userJid,
                remainingDevices,
                newTimestamp,
                newTimestamp.plus(java.time.Duration.ofDays(1)),
                String.valueOf(timestamp),
                oldList.deleted(),
                oldList.deletedChangedToHost(),
                oldList.advAccountType(),
                expectedTs,
                oldList.expectedTsLastDeviceJobTs(),
                oldList.expectedTsUpdateTs(),
                oldList.currentIndex(),
                oldList.validIndexes()
        );

        // Store updated device list
        client.store().addDeviceList(updatedList);

        // Notify listeners
        for (var listener : client.store().listeners()) {
            Thread.startVirtualThread(() ->
                    listener.onDeviceListChanged(client, userJid, "remove")
            );
        }

        LOGGER.log(System.Logger.Level.DEBUG, "Devices removed for {0}: {1} remaining", userJid, remainingDevices.size());
    }

    /**
     * Builds a map of device ID to key index from the key-index-list node.
     */
    private static Map<Integer, Integer> buildKeyIndexMap(Node keyIndexListNode) {
        return keyIndexListNode.streamChildren("device")
                .flatMap(deviceNode -> {
                    var jid = deviceNode.getAttributeAsJid("jid");
                    if (jid.isEmpty()) {
                        return java.util.stream.Stream.empty();
                    }

                    var keyIndex = deviceNode.getAttributeAsInt("key-index", -1);
                    if (keyIndex == -1) {
                        return java.util.stream.Stream.empty();
                    }

                    return java.util.stream.Stream.of(Map.entry(jid.get().device(), keyIndex));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Validates and extracts key index list data from the signed protobuf.
     * Returns null if validation fails.
     */
    private static KeyIndexList validateKeyIndexList(Node keyIndexListNode) {
        try {
            var signedKeyIndexBytes = keyIndexListNode.toContentBytes();
            if (signedKeyIndexBytes.isEmpty()) {
                return null;
            }

            var signedKeyIndexList = SignedKeyIndexListSpec.decode(signedKeyIndexBytes.get());
            if (signedKeyIndexList.details() == null) {
                return null;
            }

            var keyIndexList = KeyIndexListSpec.decode(signedKeyIndexList.details());
            if (keyIndexList.rawId() == 0 && keyIndexList.timestamp() == 0) {
                LOGGER.log(System.Logger.Level.WARNING, "Key index list missing rawId and timestamp in notification");
                return null;
            }

            return keyIndexList;
        } catch (ProtobufDeserializationException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to decode key index list protobuf in notification", e);
            return null;
        }
    }

    /**
     * Handles an account type transition detected via device notification.
     * Cleans up Signal sessions and generates system messages.
     */
    private static void handleAccountTypeTransition(
            WhatsAppClient client,
            Jid userJid,
            DeviceInfo.Type oldType,
            DeviceInfo.Type newType,
            DeviceList oldList
    ) {
        LOGGER.log(System.Logger.Level.INFO, "Account type changed via notification for {0}: {1} -> {2}", userJid, oldType, newType);

        // Cleanup all Signal sessions for old devices
        for (var device : oldList.devices()) {
            var deviceJid = device.toDeviceJid(userJid.user(), userJid.server());
            client.store().cleanupSignalSessionsForDevice(deviceJid);
        }

        // If transitioning to HOSTED, mark device list as deleted-changed-to-host
        if (newType == DeviceInfo.Type.HOSTED) {
            client.store().addDeviceList(DeviceList.deleted(userJid, true));
        }

        // Notify listeners about account type change
        for (var listener : client.store().listeners()) {
            Thread.startVirtualThread(() ->
                    listener.onAccountTypeChanged(client, userJid, oldType, newType)
            );
        }

        // Create system message in chat
        var chat = client.store().findChatByJid(userJid).orElse(null);
        if (chat != null) {
            var stubType = (newType == DeviceInfo.Type.E2EE)
                    ? MessageInfoStubType.E2E_ENCRYPTED_NOW
                    : MessageInfoStubType.CIPHERTEXT;

            var key = new ChatMessageKeyBuilder()
                    .id(ChatMessageKey.randomId(client.store().clientType()))
                    .chatJid(chat.jid())
                    .senderJid(userJid)
                    .build();
            var message = new ChatMessageInfoBuilder()
                    .status(MessageStatus.DELIVERED)
                    .timestampSeconds(System.currentTimeMillis() / 1000)
                    .key(key)
                    .ignore(true)
                    .stubType(stubType)
                    .senderJid(userJid)
                    .build();
            chat.addMessage(message);

            for (var listener : client.store().listeners()) {
                Thread.startVirtualThread(() -> listener.onNewMessage(client, message));
            }
        }
    }

    /**
     * Parses the ADV account type from the key-index-list protobuf content.
     */
    private static Optional<DeviceInfo.Type> parseAdvAccountType(Node keyIndexList) {
        try {
            var signedKeyIndexBytes = keyIndexList.toContentBytes();
            if (signedKeyIndexBytes.isEmpty()) {
                return Optional.empty();
            }

            var signedKeyIndexList = SignedKeyIndexListSpec.decode(signedKeyIndexBytes.get());
            if (signedKeyIndexList.details() == null) {
                return Optional.empty();
            }

            var keyIndexListProto = KeyIndexListSpec.decode(signedKeyIndexList.details());
            if (keyIndexListProto.accountType() == null) {
                return Optional.empty();
            }

            var result = switch (keyIndexListProto.accountType()) {
                case E2EE -> DeviceInfo.Type.E2EE;
                case HOSTED -> DeviceInfo.Type.HOSTED;
            };
            return Optional.of(result);
        } catch (ProtobufDeserializationException e) {
            return Optional.empty();
        }
    }
}
