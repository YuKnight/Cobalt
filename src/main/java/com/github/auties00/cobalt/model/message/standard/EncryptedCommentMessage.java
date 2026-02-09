package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.message.addon.MessageAddonEncryption;
import com.github.auties00.cobalt.message.addon.MessageAddonType;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.EncryptedMessage;
import com.github.auties00.cobalt.model.message.common.Message;
import com.github.auties00.cobalt.model.message.common.MessageContainerSpec;
import it.auties.protobuf.annotation.ProtobufBuilder;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Arrays;
import java.util.Objects;

/**
 * An encrypted comment message used in CAG (Community Announcement
 * Group) contexts where comments must be dual-encrypted.
 * <p>
 * Use {@link EncryptedCommentMessageSimpleBuilder} to create outgoing encrypted comments.
 * It encrypts the comments automatically.
 *
 * @apiNote WAWebAddonEncryption: encrypts comment data using
 * {@code MessageSpec} (full Message protobuf) with ENC_COMMENT use case.
 */
@ProtobufMessage(name = "Message.EncCommentMessage")
public final class EncryptedCommentMessage implements EncryptedMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey targetMessageKey;

    @ProtobufProperty(index = 2, type = ProtobufType.BYTES)
    final byte[] encPayload;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    final byte[] encIv;

    EncryptedCommentMessage(ChatMessageKey targetMessageKey, byte[] encPayload, byte[] encIv) {
        this.targetMessageKey = Objects.requireNonNull(targetMessageKey, "targetMessageKey cannot be null");
        this.encPayload = Objects.requireNonNull(encPayload, "encPayload cannot be null");
        this.encIv = Objects.requireNonNull(encIv, "encIv cannot be null");
    }

    /**
     * Constructs an encrypted comment from a plaintext
     * {@link CommentMessage}, encrypting the comment's inner
     * {@code Message} content with the parent message's messageSecret.
     *
     * @param comment       the plaintext comment message
     * @param parentMessage the parent message being commented on (must contain messageSecret)
     * @param selfJid       the sender's user JID
     * @return the encrypted comment message
     * @throws IllegalArgumentException if the parent message has no messageSecret
     *                                  or the comment has no message content
     *
     * @apiNote WAWebAddonEncryption.encryptAddOn: encrypts with
     * ENC_COMMENT use case, using full MessageSpec encoding.
     */
    @ProtobufBuilder(className = "EncryptedCommentMessageSimpleBuilder")
    static EncryptedCommentMessage simpleBuilder(CommentMessage comment, ChatMessageInfo parentMessage, Jid selfJid) {
        Objects.requireNonNull(comment, "comment cannot be null");
        Objects.requireNonNull(parentMessage, "parentMessage cannot be null");
        Objects.requireNonNull(selfJid, "selfJid cannot be null");

        var parentSecret = parentMessage.messageSecret()
                .orElseThrow(() -> new IllegalArgumentException("Parent message has no messageSecret"));
        var parentKey = parentMessage.key();
        var originalSender = parentKey.senderJid()
                .orElse(parentKey.fromMe() ? selfJid : parentKey.chatJid())
                .toUserJid();

        // WAWebAddonEncryption: CommentEncrypted uses MessageSpec (full Message protobuf)
        var commentContent = comment.message()
                .orElseThrow(() -> new IllegalArgumentException("Comment has no message content"));
        var plaintext = MessageContainerSpec.encode(commentContent);

        var encrypted = MessageAddonEncryption.encrypt(
                plaintext, parentSecret, parentKey.id(),
                originalSender, selfJid.toUserJid(),
                MessageAddonType.ENC_COMMENT);

        return new EncryptedCommentMessageBuilder()
                .targetMessageKey(comment.targetMessageKey())
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
    public Message.Type type() {
        return Message.Type.ENCRYPTED_COMMENT;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EncryptedCommentMessage that
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
        return "EncryptedCommentMessage[" +
                "targetMessageKey=" + targetMessageKey +
                ", encPayload=" + Arrays.toString(encPayload) +
                ", encIv=" + Arrays.toString(encIv) +
                ']';
    }
}
