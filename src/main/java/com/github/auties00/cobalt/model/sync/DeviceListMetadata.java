package com.github.auties00.cobalt.model.sync;

import com.github.auties00.cobalt.model.auth.ADVEncryptionType;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;

import java.util.List;

import static it.auties.protobuf.model.ProtobufType.*;

/**
 * Device list metadata for Identity Change Detection Consistency (ICDC).
 *
 * <p>Populated on every outgoing message to allow the recipient to verify
 * that the sender's and recipient's device lists have not changed since
 * the last key exchange.
 *
 * @apiNote WAWebProtobufsE2E.pb.DeviceListMetadata
 */
@ProtobufMessage(name = "DeviceListMetadata")
public record DeviceListMetadata(@ProtobufProperty(index = 1, type = BYTES) byte[] senderKeyHash,
                                 @ProtobufProperty(index = 2, type = UINT64) Long senderTimestamp,
                                 @ProtobufProperty(index = 3, type = UINT32, packed = true) List<Integer> senderKeyIndexes,
                                 @ProtobufProperty(index = 4, type = ENUM) ADVEncryptionType senderAccountType,
                                 @ProtobufProperty(index = 5, type = ENUM) ADVEncryptionType receiverAccountType,
                                 @ProtobufProperty(index = 8, type = BYTES) byte[] recipientKeyHash,
                                 @ProtobufProperty(index = 9, type = UINT64) Long recipientTimestamp,
                                 @ProtobufProperty(index = 10, type = UINT32, packed = true) List<Integer> recipientKeyIndexes) {
}
