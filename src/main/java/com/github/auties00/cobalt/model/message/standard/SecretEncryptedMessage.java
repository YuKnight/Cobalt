package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.EncryptedMessage;
import com.github.auties00.cobalt.model.message.common.Message;
import com.github.auties00.cobalt.model.message.common.ServerMessage;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Arrays;
import java.util.Objects;

/**
 * A secret encrypted message used for event edits and message edits.
 */
@ProtobufMessage(name = "Message.SecretEncryptedMessage")
public final class SecretEncryptedMessage implements ServerMessage, EncryptedMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey targetMessageKey;

    @ProtobufProperty(index = 2, type = ProtobufType.BYTES)
    final byte[] encPayload;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    final byte[] encIv;

    @ProtobufProperty(index = 4, type = ProtobufType.ENUM)
    final SecretEncType secretEncType;

    SecretEncryptedMessage(ChatMessageKey targetMessageKey, byte[] encPayload, byte[] encIv, SecretEncType secretEncType) {
        this.targetMessageKey = Objects.requireNonNull(targetMessageKey, "targetMessageKey cannot be null");
        this.encPayload = Objects.requireNonNull(encPayload, "encPayload cannot be null");
        this.encIv = Objects.requireNonNull(encIv, "encIv cannot be null");
        this.secretEncType = secretEncType;
    }

    public ChatMessageKey targetMessageKey() {
        return targetMessageKey;
    }

    public byte[] encPayload() {
        return encPayload;
    }

    public byte[] encIv() {
        return encIv;
    }

    public SecretEncType secretEncType() {
        return secretEncType;
    }

    @Override
    public String secretName() {
        return secretEncType != null ? secretEncType.name() : "Secret Encrypted";
    }

    @Override
    public Message.Type type() {
        return Message.Type.SECRET_ENCRYPTED;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SecretEncryptedMessage that
                && Objects.equals(targetMessageKey, that.targetMessageKey)
                && Arrays.equals(encPayload, that.encPayload)
                && Arrays.equals(encIv, that.encIv)
                && secretEncType == that.secretEncType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetMessageKey, Arrays.hashCode(encPayload), Arrays.hashCode(encIv), secretEncType);
    }

    @Override
    public String toString() {
        return "SecretEncryptedMessage[" +
                "targetMessageKey=" + targetMessageKey +
                ", encPayload=" + Arrays.toString(encPayload) +
                ", encIv=" + Arrays.toString(encIv) +
                ", secretEncType=" + secretEncType +
                ']';
    }

    @ProtobufEnum(name = "Message.SecretEncryptedMessage.SecretEncType")
    public enum SecretEncType {
        UNKNOWN(0),
        EVENT_EDIT(1),
        MESSAGE_EDIT(2);

        final int index;

        SecretEncType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
