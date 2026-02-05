package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.EncryptedMessage;
import com.github.auties00.cobalt.model.message.common.Message;
import com.github.auties00.cobalt.model.message.common.ServerMessage;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Arrays;
import java.util.Objects;

/**
 * An encrypted event response message (e.g., RSVP to an event).
 */
@ProtobufMessage(name = "Message.EncEventResponseMessage")
public final class EncEventResponseMessage implements ServerMessage, EncryptedMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey eventCreationMessageKey;

    @ProtobufProperty(index = 2, type = ProtobufType.BYTES)
    final byte[] encPayload;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    final byte[] encIv;

    EncEventResponseMessage(ChatMessageKey eventCreationMessageKey, byte[] encPayload, byte[] encIv) {
        this.eventCreationMessageKey = Objects.requireNonNull(eventCreationMessageKey, "eventCreationMessageKey cannot be null");
        this.encPayload = Objects.requireNonNull(encPayload, "encPayload cannot be null");
        this.encIv = Objects.requireNonNull(encIv, "encIv cannot be null");
    }

    public ChatMessageKey eventCreationMessageKey() {
        return eventCreationMessageKey;
    }

    public ChatMessageKey targetMessageKey() {
        return eventCreationMessageKey;
    }

    public byte[] encPayload() {
        return encPayload;
    }

    public byte[] encIv() {
        return encIv;
    }

    @Override
    public String secretName() {
        return "Enc Event Response";
    }

    @Override
    public Message.Type type() {
        return Message.Type.ENCRYPTED_EVENT_RESPONSE;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EncEventResponseMessage that
                && Objects.equals(eventCreationMessageKey, that.eventCreationMessageKey)
                && Arrays.equals(encPayload, that.encPayload)
                && Arrays.equals(encIv, that.encIv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventCreationMessageKey, Arrays.hashCode(encPayload), Arrays.hashCode(encIv));
    }

    @Override
    public String toString() {
        return "EncEventResponseMessage[" +
                "eventCreationMessageKey=" + eventCreationMessageKey +
                ", encPayload=" + Arrays.toString(encPayload) +
                ", encIv=" + Arrays.toString(encIv) +
                ']';
    }
}
