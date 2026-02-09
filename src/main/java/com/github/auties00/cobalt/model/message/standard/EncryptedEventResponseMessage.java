package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.message.addon.MessageAddonEncryption;
import com.github.auties00.cobalt.message.addon.MessageAddonType;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.EncryptedMessage;
import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufBuilder;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Arrays;
import java.util.Objects;

/**
 * An encrypted event response message (e.g., RSVP to an event).
 * <p>
 * Use {@link EncryptedEventResponseMessageSimpleBuilder} to create outgoing encrypted event responses.
 * It encrypts the event responses automatically.
 *
 * @apiNote WAWebEventResponseAddonMessageProcessor: encrypts event
 * response data as EventResponseMessage protobuf, then wraps as
 * EncEventResponseMessage with encPayload and encIv.
 */
@ProtobufMessage(name = "Message.EncEventResponseMessage")
public final class EncryptedEventResponseMessage implements EncryptedMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey eventCreationMessageKey;

    @ProtobufProperty(index = 2, type = ProtobufType.BYTES)
    final byte[] encPayload;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    final byte[] encIv;

    EncryptedEventResponseMessage(ChatMessageKey eventCreationMessageKey, byte[] encPayload, byte[] encIv) {
        this.eventCreationMessageKey = Objects.requireNonNull(eventCreationMessageKey, "eventCreationMessageKey cannot be null");
        this.encPayload = Objects.requireNonNull(encPayload, "encPayload cannot be null");
        this.encIv = Objects.requireNonNull(encIv, "encIv cannot be null");
    }

    /**
     * Constructs an encrypted event response from a plaintext
     * {@link EventResponseMessage}, encrypting it with the parent
     * event's messageSecret.
     *
     * @param response     the plaintext event response
     * @param parentEvent  the parent event creation message (must contain messageSecret)
     * @param selfJid      the sender's user JID
     * @return the encrypted event response message
     * @throws IllegalArgumentException if the parent event has no messageSecret
     *
     * @apiNote WAWebAddonEncryption.encryptAddOn: encrypts with
     * ENC_EVENT_RESPONSE use case.
     */
    @ProtobufBuilder(className = "EncryptedEventResponseMessageSimpleBuilder")
    static EncryptedEventResponseMessage simpleBuilder(EventResponseMessage response, ChatMessageInfo parentEvent, Jid selfJid) {
        Objects.requireNonNull(response, "response cannot be null");
        Objects.requireNonNull(parentEvent, "parentEvent cannot be null");
        Objects.requireNonNull(selfJid, "selfJid cannot be null");

        var parentSecret = parentEvent.messageSecret()
                .orElseThrow(() -> new IllegalArgumentException("Parent event has no messageSecret"));
        var parentKey = parentEvent.key();
        var originalSender = parentKey.senderJid()
                .orElse(parentKey.fromMe() ? selfJid : parentKey.chatJid())
                .toUserJid();

        var plaintext = EventResponseMessageSpec.encode(response);

        var encrypted = MessageAddonEncryption.encrypt(
                plaintext, parentSecret, parentKey.id(),
                originalSender, selfJid.toUserJid(),
                MessageAddonType.EVENT_RESPONSE);

        return new EncryptedEventResponseMessageBuilder()
                .eventCreationMessageKey(parentKey)
                .encPayload(encrypted.ciphertext())
                .encIv(encrypted.iv())
                .build();
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
    public Message.Type type() {
        return Message.Type.ENCRYPTED_EVENT_RESPONSE;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EncryptedEventResponseMessage that
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
        return "EncryptedEventResponseMessage[" +
                "eventCreationMessageKey=" + eventCreationMessageKey +
                ", encPayload=" + Arrays.toString(encPayload) +
                ", encIv=" + Arrays.toString(encIv) +
                ']';
    }
}
