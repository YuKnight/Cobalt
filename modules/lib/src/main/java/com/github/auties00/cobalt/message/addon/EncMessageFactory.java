package com.github.auties00.cobalt.message.addon;

import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainerSpec;
import com.github.auties00.cobalt.model.message.security.EncCommentMessageBuilder;
import com.github.auties00.cobalt.model.message.security.EncCommentMessage;
import com.github.auties00.cobalt.model.message.security.EncReactionMessageBuilder;
import com.github.auties00.cobalt.model.message.security.EncReactionMessage;
import com.github.auties00.cobalt.model.message.text.CommentMessage;
import com.github.auties00.cobalt.model.message.text.ReactionMessage;
import com.github.auties00.cobalt.model.message.text.ReactionMessageSpec;

import java.util.Objects;

/**
 * Factory for creating encrypted addon messages (comments and reactions)
 * from their plaintext counterparts.
 *
 * <p>The encryption logic is separated from the model classes so that
 * the model module remains free of service-layer dependencies.
 *
 * @implNote WAWebAddonEncryption.encryptAddOn
 */
public final class EncMessageFactory {
    private EncMessageFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Encrypts a plaintext {@code CommentMessage} into an
     * {@code EncCommentMessage} using the parent message's secret.
     *
     * @param comment       the plaintext comment message
     * @param parentMessage the parent message being commented on
     * @param selfJid       the sender's user JID
     * @return the encrypted comment message
     * @implNote WAWebAddonEncryption.encryptAddOn with ENC_COMMENT use case
     */
    public static EncCommentMessage encryptComment(CommentMessage comment, ChatMessageInfo parentMessage, Jid selfJid) {
        Objects.requireNonNull(comment, "comment cannot be null");
        Objects.requireNonNull(parentMessage, "parentMessage cannot be null");
        Objects.requireNonNull(selfJid, "selfJid cannot be null");

        var parentSecret = parentMessage.messageSecret()
                .orElseThrow(() -> new IllegalArgumentException("Parent message has no messageSecret"));
        var parentKey = parentMessage.key();
        var parentKeyId = parentKey.id()
                .orElseThrow(() -> new IllegalArgumentException("Parent key has no keyId"));
        var parentKeyJid = parentKey.parentJid()
                .orElseThrow(() -> new IllegalArgumentException("Parent key has no parentJid"));
        var originalSender = parentKey.senderJid()
                .orElse(parentKey.fromMe() ? selfJid : parentKeyJid)
                .toUserJid();

        var commentContent = comment.message()
                .orElseThrow(() -> new IllegalArgumentException("Comment has no message content"));
        var plaintext = MessageContainerSpec.encode(commentContent);

        var encrypted = MessageAddonEncryption.encrypt(
                plaintext, parentSecret, parentKeyId,
                originalSender, selfJid.toUserJid(),
                MessageAddonType.ENC_COMMENT);

        return new EncCommentMessageBuilder()
                .targetMessageKey(comment.targetMessageKey().orElse(null))
                .encPayload(encrypted.ciphertext())
                .encIv(encrypted.iv())
                .build();
    }

    /**
     * Encrypts a plaintext {@code ReactionMessage} into an
     * {@code EncReactionMessage} using the parent message's secret.
     *
     * @param reaction      the plaintext reaction message
     * @param parentMessage the parent message being reacted to
     * @param selfJid       the sender's user JID
     * @return the encrypted reaction message
     * @implNote WAWebReactionEncryptMsgData, WAWebAddonEncryption.encryptAddOn with ENC_REACTION use case
     */
    public static EncReactionMessage encryptReaction(ReactionMessage reaction, ChatMessageInfo parentMessage, Jid selfJid) {
        Objects.requireNonNull(reaction, "reaction cannot be null");
        Objects.requireNonNull(parentMessage, "parentMessage cannot be null");
        Objects.requireNonNull(selfJid, "selfJid cannot be null");

        var parentSecret = parentMessage.messageSecret()
                .orElseThrow(() -> new IllegalArgumentException("Parent message has no messageSecret"));
        var parentKey = parentMessage.key();
        var parentKeyId = parentKey.id()
                .orElseThrow(() -> new IllegalArgumentException("Parent key has no keyId"));
        var parentKeyJid = parentKey.parentJid()
                .orElseThrow(() -> new IllegalArgumentException("Parent key has no parentJid"));
        var originalSender = parentKey.senderJid()
                .orElse(parentKey.fromMe() ? selfJid : parentKeyJid)
                .toUserJid();

        var plaintext = ReactionMessageSpec.encode(reaction);

        var encrypted = MessageAddonEncryption.encrypt(
                plaintext, parentSecret, parentKeyId,
                originalSender, selfJid.toUserJid(),
                MessageAddonType.ENC_REACTION);

        return new EncReactionMessageBuilder()
                .targetMessageKey(reaction.key().orElse(null))
                .encPayload(encrypted.ciphertext())
                .encIv(encrypted.iv())
                .build();
    }
}
