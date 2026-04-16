package com.github.auties00.cobalt.message.addon;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
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
 * Factory that converts plaintext comment and reaction messages into the
 * encrypted forms that WhatsApp expects on the wire for CAG (community /
 * announcement group) threads.
 *
 * <p>Both comment and reaction addons are delivered as an {@code <enc>} child
 * of the outer Signal envelope whose content is an AES-GCM ciphertext bound
 * to the parent message's secret. The factory builds that ciphertext via
 * {@link MessageAddonEncryption#encrypt} and packages the resulting bytes into
 * the matching Cobalt model type ({@link EncCommentMessage} or
 * {@link EncReactionMessage}) ready for the normal send pipeline.
 *
 * <p>The factory is stateless: instantiation is disabled and every entry
 * point is a static helper. Keeping the encryption step out of the model
 * classes lets the {@code model} module stay service-free while still giving
 * application code a single call to turn a plaintext addon into its
 * wire-ready counterpart.
 *
 * @implNote WAWebAddonEncryption: the JS module co-locates the encryption and
 * the protobuf spec selection in a single {@code encryptAddOn} call. Cobalt
 * splits those concerns: this factory picks the right protobuf spec and the
 * matching {@link MessageAddonType}, then hands the payload to the shared
 * encryption helper.
 */
@WhatsAppWebModule(moduleName = "WAWebAddonEncryption")
public final class EncMessageFactory {
    /**
     * Private constructor preventing instantiation.
     *
     * @throws UnsupportedOperationException always
     */
    private EncMessageFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Encrypts a plaintext {@link CommentMessage} into an
     * {@link EncCommentMessage} that can be attached to an outbound stanza
     * without exposing the comment body to the server.
     *
     * <p>The comment's inner {@link com.github.auties00.cobalt.model.message.MessageContainer}
     * is serialised via {@link MessageContainerSpec} and then encrypted with a
     * key derived from the parent message's {@code messageSecret}. The
     * original sender used in the key derivation is taken from the parent
     * key: if the parent was authored by the current user, the self JID is
     * used; otherwise the parent participant or chat JID is used.
     *
     * @param comment       the plaintext comment to encrypt
     * @param parentMessage the message the comment is attached to
     * @param selfJid       the JID of the current user (used when the parent
     *                      was authored by the current user)
     * @return the encrypted comment wrapper ready to be wrapped in a stanza
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if the parent message does not carry a
     *                                  {@code messageSecret}, the parent key
     *                                  has no id or parent JID, or the
     *                                  comment has no inner message
     * @implNote WAWebAddonEncryption.encryptAddOn with
     * {@code UseCaseSecretModificationType.ENC_COMMENT}: WA Web encodes the
     * wrapped {@code MessageSpec} payload via {@code encodeProtobuf}, calls
     * {@code WAUseCaseSecret.createUseCaseSecret} with the parent stanza id,
     * original sender, and addon sender, and then encrypts under AES-GCM. The
     * Cobalt factory follows the same sequence using
     * {@link MessageContainerSpec} for the protobuf step and
     * {@link MessageAddonEncryption#encrypt} for the crypto step.
     */
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryption", exports = "encryptAddOn",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static EncCommentMessage encryptComment(CommentMessage comment, ChatMessageInfo parentMessage, Jid selfJid) {
        Objects.requireNonNull(comment, "comment cannot be null");
        Objects.requireNonNull(parentMessage, "parentMessage cannot be null");
        Objects.requireNonNull(selfJid, "selfJid cannot be null");

        // WAWebAddonEncryption.encryptAddOn
        // Resolves the 32-byte messageSecret from the parent message, required for HKDF derivation

        var parentSecret = parentMessage.messageSecret()
                .orElseThrow(() -> new IllegalArgumentException("Parent message has no messageSecret"));

        // WAWebAddonEncryption.encryptAddOn
        // Reads the parent message key, which carries the stanza id and original sender info

        var parentKey = parentMessage.key();
        var parentKeyId = parentKey.id()
                .orElseThrow(() -> new IllegalArgumentException("Parent key has no keyId"));
        var parentKeyJid = parentKey.parentJid()
                .orElseThrow(() -> new IllegalArgumentException("Parent key has no parentJid"));

        // WAWebAddonEncryption.encryptAddOn
        // Picks the original sender JID used in key derivation: the sender wid if present,
        // otherwise the self JID when the parent was authored by the current user, else the parent JID

        var originalSender = parentKey.senderJid()
                .orElse(parentKey.fromMe() ? selfJid : parentKeyJid)
                .toUserJid();

        // WAWebAddonEncryption.encryptAddOn
        // Serialises the inner MessageContainer via encodeProtobuf(MessageSpec, comment.message)

        var commentContent = comment.message()
                .orElseThrow(() -> new IllegalArgumentException("Comment has no message content"));
        var plaintext = MessageContainerSpec.encode(commentContent);

        // WAWebAddonEncryption.encryptAddOn
        // Delegates the HKDF derivation and AES-GCM encryption to the shared addon helper

        var encrypted = MessageAddonEncryption.encrypt(
                plaintext, parentSecret, parentKeyId,
                originalSender, selfJid.toUserJid(),
                MessageAddonType.ENC_COMMENT);

        // WAWebAddonEncryption.encryptAddOn
        // Packs the ciphertext and IV into the EncCommentMessage protobuf wrapper

        return new EncCommentMessageBuilder()
                .targetMessageKey(comment.targetMessageKey().orElse(null))
                .encPayload(encrypted.ciphertext())
                .encIv(encrypted.iv())
                .build();
    }

    /**
     * Encrypts a plaintext {@link ReactionMessage} into an
     * {@link EncReactionMessage} so that the emoji and target key stay hidden
     * from the server while still being deliverable through the fanout
     * pipeline.
     *
     * <p>Follows the same HKDF-plus-AES-GCM scheme as
     * {@link #encryptComment(CommentMessage, ChatMessageInfo, Jid)} but uses
     * {@link MessageAddonType#ENC_REACTION} so that the derivation is bound to
     * a different context string.
     *
     * @param reaction      the plaintext reaction to encrypt
     * @param parentMessage the message the reaction is attached to
     * @param selfJid       the JID of the current user (used when the parent
     *                      was authored by the current user)
     * @return the encrypted reaction wrapper ready to be attached to a stanza
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if the parent message does not carry a
     *                                  {@code messageSecret}, or the parent
     *                                  key has no id or parent JID
     * @implNote WAWebAddonEncryption.encryptAddOn with
     * {@code UseCaseSecretModificationType.ENC_REACTION}: in the JS
     * implementation the dispatch from {@code MsgKind.ReactionEncrypted} to
     * the {@code ENC_REACTION} use-case and the {@code ReactionMessageSpec}
     * protobuf is done by {@code WAWebAddonEncryption.C}. Cobalt inlines the
     * dispatch here because the reaction type is already known at the call
     * site.
     */
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryption", exports = "encryptAddOn",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static EncReactionMessage encryptReaction(ReactionMessage reaction, ChatMessageInfo parentMessage, Jid selfJid) {
        Objects.requireNonNull(reaction, "reaction cannot be null");
        Objects.requireNonNull(parentMessage, "parentMessage cannot be null");
        Objects.requireNonNull(selfJid, "selfJid cannot be null");

        // WAWebAddonEncryption.encryptAddOn
        // Resolves the 32-byte messageSecret from the parent message, required for HKDF derivation

        var parentSecret = parentMessage.messageSecret()
                .orElseThrow(() -> new IllegalArgumentException("Parent message has no messageSecret"));

        // WAWebAddonEncryption.encryptAddOn
        // Reads the parent message key, which carries the stanza id and original sender info

        var parentKey = parentMessage.key();
        var parentKeyId = parentKey.id()
                .orElseThrow(() -> new IllegalArgumentException("Parent key has no keyId"));
        var parentKeyJid = parentKey.parentJid()
                .orElseThrow(() -> new IllegalArgumentException("Parent key has no parentJid"));

        // WAWebAddonEncryption.encryptAddOn
        // Picks the original sender JID used in key derivation: the sender wid if present,
        // otherwise the self JID when the parent was authored by the current user, else the parent JID

        var originalSender = parentKey.senderJid()
                .orElse(parentKey.fromMe() ? selfJid : parentKeyJid)
                .toUserJid();

        // WAWebAddonEncryption.encryptAddOn
        // Serialises the reaction protobuf via encodeProtobuf(ReactionMessageSpec, reaction)

        var plaintext = ReactionMessageSpec.encode(reaction);

        // WAWebAddonEncryption.encryptAddOn
        // Delegates the HKDF derivation and AES-GCM encryption to the shared addon helper

        var encrypted = MessageAddonEncryption.encrypt(
                plaintext, parentSecret, parentKeyId,
                originalSender, selfJid.toUserJid(),
                MessageAddonType.ENC_REACTION);

        // WAWebAddonEncryption.encryptAddOn
        // Packs the ciphertext and IV into the EncReactionMessage protobuf wrapper

        return new EncReactionMessageBuilder()
                .targetMessageKey(reaction.key().orElse(null))
                .encPayload(encrypted.ciphertext())
                .encIv(encrypted.iv())
                .build();
    }
}
