package com.github.auties00.cobalt.model.auth;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

/**
 * Device identity information within the ADV (Account Device Verification) system.
 *
 * @param rawId       unique raw identifier for the device
 * @param timestamp   Unix timestamp of when this identity was created
 * @param keyIndex    index of the current key in the key index list
 * @param accountType encryption type for the account (E2EE or HOSTED)
 * @param deviceType  encryption type for the device (E2EE or HOSTED)
 */
@ProtobufMessage(name = "ADVDeviceIdentity")
public record DeviceIdentity(@ProtobufProperty(index = 1, type = ProtobufType.UINT32)
                             int rawId,
                             @ProtobufProperty(index = 2, type = ProtobufType.UINT64)
                             long timestamp,
                             @ProtobufProperty(index = 3, type = ProtobufType.UINT32)
                             int keyIndex,
                             @ProtobufProperty(index = 4, type = ProtobufType.ENUM)
                             ADVEncryptionType accountType,
                             @ProtobufProperty(index = 5, type = ProtobufType.ENUM)
                             ADVEncryptionType deviceType) {
}
