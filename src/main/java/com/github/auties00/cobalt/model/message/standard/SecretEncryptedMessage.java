package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.message.addon.MessageAddonEncryption;
import com.github.auties00.cobalt.message.addon.MessageAddonType;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.EncryptedMessage;
import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.ProtobufType;

import java.util.Arrays;
import java.util.Objects;

/**
 * A secret encrypted message used for event edits and message edits.
 *
 * <p>Use {@link SecretEncryptedEventEditMessageBuilder} for editing events, and
 * {@link SecretEncryptedMessageEditMessageBuilder} for editing messages.  Both builders encrypt the inner content
 * internally using the parent message's messageSecret via
 * {@link MessageAddonEncryption}.
 *
 * @apiNote WAWebAddonEncryption: uses EVENT_EDIT or MESSAGE_EDIT
 * use-case types for HKDF key derivation.
 */
@ProtobufMessage(name = "Message.SecretEncryptedMessage")
public final class SecretEncryptedMessage implements EncryptedMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey targetMessageKey;

    @ProtobufProperty(index = 2, type = ProtobufType.BYTES)
    byte[] encPayload;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    byte[] encIv;

    @ProtobufProperty(index = 4, type = ProtobufType.ENUM)
    final SecretEncType secretEncType;

    SecretEncryptedMessage(ChatMessageKey targetMessageKey, byte[] encPayload, byte[] encIv, SecretEncType secretEncType) {
        this.targetMessageKey = Objects.requireNonNull(targetMessageKey, "targetMessageKey cannot be null");
        this.encPayload = encPayload;
        this.encIv = encIv;
        this.secretEncType = secretEncType;
    }

    /**
     * Creates an encrypted event edit message.
     *
     * @param editedEvent   the edited event content
     * @param parentMessage the original event message (must contain messageSecret)
     * @param selfJid       the sender's user JID
     * @return the encrypted secret message with type EVENT_EDIT
     *
     * @apiNote WAWebEventEditAddonMessageProcessor: encrypts
     * EventMessage protobuf with EVENT_EDIT use case.
     */
    @ProtobufBuilder(className = "SecretEncryptedEventEditMessageBuilder")
    static SecretEncryptedMessage eventEditBuilder(EventMessage editedEvent, ChatMessageInfo parentMessage, Jid selfJid) {
        Objects.requireNonNull(editedEvent, "editedEvent cannot be null");
        var plaintext = EventMessageSpec.encode(editedEvent);
        return encryptInternal(plaintext, parentMessage, selfJid, SecretEncType.EVENT_EDIT, MessageAddonType.EVENT_EDIT);
    }

    /**
     * Creates an encrypted message edit message.
     *
     * @param editedContent the edited message content (serialized protobuf)
     * @param parentMessage the original message (must contain messageSecret)
     * @param selfJid       the sender's user JID
     * @return the encrypted secret message with type MESSAGE_EDIT
     *
     * @apiNote WAWebMessageEditAddonMessageProcessor: encrypts
     * the edited message protobuf with MESSAGE_EDIT use case.
     */
    @ProtobufBuilder(className = "SecretEncryptedMessageEditMessageBuilder")
    static SecretEncryptedMessage messageEditBuilder(byte[] editedContent, ChatMessageInfo parentMessage, Jid selfJid) {
        Objects.requireNonNull(editedContent, "editedContent cannot be null");
        return encryptInternal(editedContent, parentMessage, selfJid, SecretEncType.MESSAGE_EDIT, MessageAddonType.MESSAGE_EDIT);
    }

    private static SecretEncryptedMessage encryptInternal(
            byte[] plaintext,
            ChatMessageInfo parentMessage,
            Jid selfJid,
            SecretEncType encType,
            MessageAddonType addonType
    ) {
        Objects.requireNonNull(parentMessage, "parentMessage cannot be null");
        Objects.requireNonNull(selfJid, "selfJid cannot be null");

        var parentSecret = parentMessage.messageSecret()
                .orElseThrow(() -> new IllegalArgumentException("Parent message has no messageSecret"));
        var parentKey = parentMessage.key();
        var originalSender = parentKey.senderJid()
                .orElse(parentKey.fromMe() ? selfJid : parentKey.chatJid())
                .toUserJid();

        var encrypted = MessageAddonEncryption.encrypt(
                plaintext, parentSecret, parentKey.id(),
                originalSender, selfJid.toUserJid(),
                addonType);

        return new SecretEncryptedMessageBuilder()
                .targetMessageKey(parentKey)
                .encPayload(encrypted.ciphertext())
                .encIv(encrypted.iv())
                .secretEncType(encType)
                .build();
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
