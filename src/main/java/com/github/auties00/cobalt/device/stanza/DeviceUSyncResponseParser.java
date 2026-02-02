package com.github.auties00.cobalt.device.stanza;

import com.github.auties00.cobalt.device.info.DeviceConstants;
import com.github.auties00.cobalt.device.info.DeviceInfo;
import com.github.auties00.cobalt.device.info.DeviceList;
import com.github.auties00.cobalt.model.auth.KeyIndexList;
import com.github.auties00.cobalt.model.auth.KeyIndexListSpec;
import com.github.auties00.cobalt.model.auth.SignedKeyIndexListSpec;
import com.github.auties00.cobalt.node.Node;
import it.auties.protobuf.exception.ProtobufDeserializationException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parses USync IQ responses into DeviceList objects.
 * Validates key index lists against the ADV protobuf validIndexes.
 */
public final class DeviceUSyncResponseParser {
    private static final System.Logger LOGGER = System.getLogger("DeviceUSyncResponseParser");

    private DeviceUSyncResponseParser() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Parses a USync response into a list of device list results.
     * Results can be either full device lists or omitted results (delta confirmations).
     *
     * @param responseNode the IQ response node
     * @return list of device list results
     */
    public static List<DeviceListResult> parse(Node responseNode) {
        return responseNode.streamChild("usync")
                .flatMap(usync -> usync.streamChild("result"))
                .flatMap(result -> result.streamChild("devices"))
                .flatMap(devices -> devices.streamChildren("user"))
                .flatMap(DeviceUSyncResponseParser::parseDevices)
                .toList();
    }

    private static Stream<DeviceListResult> parseDevices(Node userNode) {
        var jid = userNode.getAttributeAsJid("jid");
        if(jid.isEmpty()) {
            return Stream.empty();
        }

        var userJid = jid.get().toUserJid();
        var deviceListNode = userNode.getChild("device-list");
        var keyIndexListNode = userNode.getChild("key-index-list", null);

        // Check for omitted result: no key-index-list OR key-index-list has no content (signedKeyIndexBytes)
        var hasSignedKeyIndexBytes = keyIndexListNode != null
                && keyIndexListNode.toContentBytes().isPresent();

        if (keyIndexListNode == null || !hasSignedKeyIndexBytes) {
            // This is an omitted result - server confirmed dhash matches
            // Check if device-list has companion devices (which would be invalid for omitted)
            if (deviceListNode.isPresent()) {
                var hasCompanionDevices = deviceListNode.get()
                        .streamChildren("device")
                        .anyMatch(device -> {
                            var id = device.getAttributeAsInt("id");
                            return id.isPresent() && id.getAsInt() != DeviceConstants.PRIMARY_DEVICE_ID;
                        });

                if (hasCompanionDevices) {
                    // Drop: response with companion devices but no key index bytes
                    return Stream.empty();
                }
            }

            // Return omitted result with timestamp and expectedTs if available
            var timestamp = keyIndexListNode != null
                    ? keyIndexListNode.getAttributeAsLong("ts", null)
                    : null;
            var expectedTs = keyIndexListNode != null
                    ? keyIndexListNode.getAttributeAsLong("expected_ts", null)
                    : null;

            return Stream.of(new DeviceListResult.Omitted(userJid, timestamp, expectedTs));
        }

        // Full device list response
        if (deviceListNode.isEmpty()) {
            return Stream.empty();
        }

        var keyIndexMap = buildKeyIndexMap(userNode);

        // Feature 8: Validate key index list from protobuf
        var validatedKeyIndexInfo = validateKeyIndexList(keyIndexListNode);

        var devices = deviceListNode.get()
                .streamChildren("device")
                .flatMap(deviceNode -> parseDeviceEntry(deviceNode, keyIndexMap, validatedKeyIndexInfo))
                .toList();

        // Parse additional metadata from key-index-list
        var expectedTs = keyIndexListNode.getAttributeAsLong("expected_ts", 0);
        var rawId = validatedKeyIndexInfo != null
                ? String.valueOf(validatedKeyIndexInfo.rawId())
                : keyIndexListNode.getAttributeAsString("ts", null);
        var advAccountType = parseAdvAccountType(validatedKeyIndexInfo)
                .orElse(null);

        var currentIndex = validatedKeyIndexInfo != null ? validatedKeyIndexInfo.currentIndex() : 0;
        var validIndexes = validatedKeyIndexInfo != null ? validatedKeyIndexInfo.validIndexes() : List.<Integer>of();

        // Use extended factory method with key index validation data
        var deviceList = DeviceList.of(userJid, devices, java.time.Duration.ofDays(1), rawId, advAccountType, expectedTs, currentIndex, validIndexes);

        return Stream.of(new DeviceListResult.Full(deviceList));
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

            // Validate required fields
            if (keyIndexList.rawId() == 0 && keyIndexList.timestamp() == 0) {
                LOGGER.log(System.Logger.Level.WARNING, "Key index list missing rawId and timestamp");
                return null;
            }

            return keyIndexList;
        } catch (ProtobufDeserializationException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to decode key index list protobuf", e);
            return null;
        }
    }

    private static Map<Integer, Integer> buildKeyIndexMap(Node userNode) {
        return userNode.streamChild("key-index-list")
                .flatMap(keyIndexList -> keyIndexList.streamChildren("device"))
                .flatMap(DeviceUSyncResponseParser::parseKeyIndexEntry)
                .collect(Collectors.toUnmodifiableMap(KeyIndexEntry::deviceId, KeyIndexEntry::keyIndex));
    }

    private static Stream<KeyIndexEntry> parseKeyIndexEntry(Node deviceNode) {
        var jid = deviceNode.getAttributeAsJid("jid");
        if(jid.isEmpty()) {
            return Stream.empty();
        }

        var keyIndex = deviceNode.getAttributeAsInt("key-index", -1);
        if(keyIndex == -1) {
            return Stream.empty();
        }

        var result = new KeyIndexEntry(jid.get().device(), keyIndex);
        return Stream.of(result);
    }

    private static Stream<DeviceInfo> parseDeviceEntry(Node deviceNode, Map<Integer, Integer> keyIndexMap, KeyIndexList validatedKeyIndexInfo) {
        var id = deviceNode.getAttributeAsInt("id");
        if(id.isEmpty()) {
            return Stream.empty();
        }

        var deviceId = id.getAsInt();
        var keyIndex = keyIndexMap.getOrDefault(deviceId, 0);

        // Feature 8: Validate keyIndex against validIndexes from protobuf
        if (validatedKeyIndexInfo != null && validatedKeyIndexInfo.validIndexes() != null && !validatedKeyIndexInfo.validIndexes().isEmpty()) {
            if (keyIndex != 0 && !validatedKeyIndexInfo.validIndexes().contains(keyIndex)) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Device {0} has keyIndex {1} not in validIndexes {2}, excluding from result",
                        deviceId, keyIndex, validatedKeyIndexInfo.validIndexes());
                return Stream.empty();
            }
        }

        if (deviceId == DeviceConstants.HOSTED_DEVICE_ID) {
            return Stream.of(DeviceInfo.hosted(keyIndex));
        } else {
            return Stream.of(DeviceInfo.e2ee(deviceId, keyIndex));
        }
    }

    private record KeyIndexEntry(int deviceId, int keyIndex) {

    }

    private static Optional<DeviceInfo.Type> parseAdvAccountType(KeyIndexList keyIndexList) {
        if (keyIndexList == null || keyIndexList.accountType() == null) {
            return Optional.empty();
        }

        var result = switch (keyIndexList.accountType()) {
            case E2EE -> DeviceInfo.Type.E2EE;
            case HOSTED -> DeviceInfo.Type.HOSTED;
        };
        return Optional.of(result);
    }
}
