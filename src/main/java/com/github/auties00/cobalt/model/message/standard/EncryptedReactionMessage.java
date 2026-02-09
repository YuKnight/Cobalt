package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.message.addon.MessageAddonEncryption;
import com.github.auties00.cobalt.message.addon.MessageAddonType;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.EncryptedMessage;
import it.auties.protobuf.annotation.ProtobufBuilder;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Arrays;
import java.util.Objects;

/**
 * An encrypted reaction message used in CAG (Community Announcement Group)
 * contexts where reactions must be dual-encrypted.
 * <p>
 * Use {@link EncryptedReactionMessageSimpleBuilder} to create outgoing encrypted reactions.
 * It encrypts the reactions automatically.
 *
 * @apiNote WAWebReactionEncryptMsgData.encryptReactionMsgData: encrypts
 * reaction data as ReactionMessage protobuf, then wraps as
 * EncReactionMessage with encPayload and encIv.
 */
@ProtobufMessage(name = "Message.EncReactionMessage")
public final class EncryptedReactionMessage implements EncryptedMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey targetMessageKey;

    @ProtobufProperty(index = 2, type = ProtobufType.BYTES)
    final byte[] encPayload;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    final byte[] encIv;

    EncryptedReactionMessage(ChatMessageKey targetMessageKey, byte[] encPayload, byte[] encIv) {
        this.targetMessageKey = Objects.requireNonNull(targetMessageKey, "targetMessageKey cannot be null");
        this.encPayload = Objects.requireNonNull(encPayload, "encPayload cannot be null");
        this.encIv = Objects.requireNonNull(encIv, "encIv cannot be null");
    }

    /**
     * Constructs an encrypted reaction message from a plaintext
     * {@link ReactionMessage}, encrypting it with the parent message's
     * messageSecret.
     *
     * @param reaction      the plaintext reaction message
     * @param parentMessage the parent message being reacted to (must contain messageSecret)
     * @param selfJid       the sender's user JID
     * @return the encrypted reaction message
     * @throws IllegalArgumentException if the parent message has no messageSecret
     *
     * @apiNote WAWebReactionEncryptMsgData: encodes ReactionMessage protobuf,
     * encrypts via WAWebAddonEncryption.encryptAddOn with ENC_REACTION use case.
     */
    @ProtobufBuilder(className = "EncryptedReactionMessageSimpleBuilder")
    static EncryptedReactionMessage simpleBuilder(ReactionMessage reaction, ChatMessageInfo parentMessage, Jid selfJid) {
        Objects.requireNonNull(reaction, "reaction cannot be null");
        Objects.requireNonNull(parentMessage, "parentMessage cannot be null");
        Objects.requireNonNull(selfJid, "selfJid cannot be null");

        var parentSecret = parentMessage.messageSecret()
                .orElseThrow(() -> new IllegalArgumentException("Parent message has no messageSecret"));
        var parentKey = parentMessage.key();
        var originalSender = parentKey.senderJid()
                .orElse(parentKey.fromMe() ? selfJid : parentKey.chatJid())
                .toUserJid();

        // WAWebReactionEncryptMsgData: encode the plaintext reaction as protobuf
        var plaintext = ReactionMessageSpec.encode(reaction);

        // WAWebAddonEncryption.encryptAddOn: encrypt with ENC_REACTION use case
        var encrypted = MessageAddonEncryption.encrypt(
                plaintext, parentSecret, parentKey.id(),
                originalSender, selfJid.toUserJid(),
                MessageAddonType.ENC_REACTION);

        return new EncryptedReactionMessageBuilder()
                .targetMessageKey(reaction.key())
                .encPayload(encrypted.ciphertext())
                .encIv(encrypted.iv())
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

    @Override
    public Type type() {
        return Type.ENCRYPTED_REACTION;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EncryptedReactionMessage that
                && Objects.equals(targetMessageKey, that.targetMessageKey)
                && Arrays.equals(encPayload, that.encPayload)
                && Arrays.equals(encIv, that.encIv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetMessageKey, Arrays.hashCode(encPayload), Arrays.hashCode(encIv));
    }

    @Override
    public String toString() {
        return "EncryptedReactionMessage[" +
                "targetMessageKey=" + targetMessageKey +
                ", encPayload=" + Arrays.toString(encPayload) +
                ", encIv=" + Arrays.toString(encIv) +
                ']';
    }
}
