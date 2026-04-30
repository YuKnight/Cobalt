package com.github.auties00.cobalt.message.send;

import com.github.auties00.cobalt.message.addon.EncMessageFactory;
import com.github.auties00.cobalt.message.send.id.MessageIdGenerator;
import com.github.auties00.cobalt.message.send.id.MessageIdVersion;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.ChatMessageContextInfoBuilder;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.chat.ChatMessageInfoBuilder;
import com.github.auties00.cobalt.model.chat.group.GroupMetadata;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.model.message.*;
import com.github.auties00.cobalt.model.message.event.EncEventResponseMessage;
import com.github.auties00.cobalt.model.message.poll.PollUpdateMessage;
import com.github.auties00.cobalt.model.message.security.*;
import com.github.auties00.cobalt.model.message.text.CommentMessage;
import com.github.auties00.cobalt.model.message.text.ReactionMessage;
import com.github.auties00.cobalt.model.newsletter.NewsletterMessageInfo;
import com.github.auties00.cobalt.model.newsletter.NewsletterMessageInfoBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.cobalt.util.DataUtils;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Builds a fully populated {@link ChatMessageInfo} or
 * {@link NewsletterMessageInfo} from a raw {@link MessageContainer} so the
 * downstream send pipeline can dispatch it.
 *
 * <p>The pipeline generates a unique message id and a 32-byte
 * {@code messageSecret}, validates addon encryption state, auto-converts
 * {@link ReactionMessage} to {@link EncReactionMessage} for CAG groups,
 * stamps the secret onto the {@code messageContextInfo}, and wraps the
 * result in the appropriate {@link MessageInfo}. Device list metadata
 * (ICDC) is populated later by the per-device encryption stage, and random
 * padding is applied at the Signal binary level by {@code MessageEncryption}.
 */
@WhatsAppWebModule(moduleName = "WAWebOutgoingMessage")
@WhatsAppWebModule(moduleName = "WAWebE2EProtoGenerator")
@WhatsAppWebModule(moduleName = "WAWebAddonEncryptAddonMsgData")
final class MessagePreparer {
    /**
     * Holds the logger used for preparation diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(MessagePreparer.class.getName());

    /**
     * Holds the size, in bytes, of the per-message secret generated for every
     * outbound chat message.
     */
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryptionError", exports = "getValidatedMessageSecret",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final int MESSAGE_SECRET_SIZE = 32;

    /**
     * Holds the store consulted for self-JID resolution, newsletter lookups,
     * and group-metadata queries.
     */
    private final WhatsAppStore store;

    /**
     * Constructs a preparer bound to the given store.
     *
     * @param store the store providing JID and metadata lookups
     */
    @WhatsAppWebExport(moduleName = "WAWebE2EProtoGenerator", exports = "getProtobufMessage",
            adaptation = WhatsAppAdaptation.ADAPTED)
    MessagePreparer(WhatsAppStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    /**
     * Prepares the given container for sending to a chat by producing a fully
     * populated {@link ChatMessageInfo}. The {@code messageSecret} is set on
     * both the container's {@code messageContextInfo} and the resulting info
     * object; ICDC metadata is populated later in the pipeline.
     *
     * @param chatJid   the recipient chat JID
     * @param container the raw message container
     * @return the prepared message info, ready for the send pipeline
     * @throws NullPointerException  if any argument is {@code null}
     * @throws IllegalStateException if the client is not logged in
     */
    @WhatsAppWebExport(moduleName = "WAWebOutgoingMessage", exports = "createOutgoingMessageProtobuf",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebOutgoingMessage", exports = "createOutgoingMsgModelProtobuf",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebE2EProtoGenerator", exports = "getProtobufMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    ChatMessageInfo prepareChat(Jid chatJid, MessageContainer container) {
        Objects.requireNonNull(chatJid, "chatJid");
        Objects.requireNonNull(container, "container");

        var localJid = store.jid()
                .orElseThrow(() -> new IllegalStateException("Not logged in"));
        var messageId = MessageIdGenerator.generate(MessageIdVersion.V2, localJid);
        var timestamp = Instant.now();

        var messageSecret = DataUtils.randomByteArray(MESSAGE_SECRET_SIZE);

        var preparedContainer = prepareAddonContent(container, chatJid, localJid);

        var deviceInfo = new ChatMessageContextInfoBuilder()
                .messageSecret(messageSecret)
                .build();
        preparedContainer = preparedContainer.withMessageContextInfo(deviceInfo);

        var key = new MessageKeyBuilder()
                .id(messageId)
                .parentJid(chatJid)
                .fromMe(true)
                .senderJid(localJid)
                .build();

        return new ChatMessageInfoBuilder()
                .status(MessageStatus.PENDING)
                .senderJid(localJid)
                .key(key)
                .message(preparedContainer)
                .timestamp(timestamp)
                .broadcast(chatJid.hasServer(JidServer.broadcast()))
                .messageSecret(messageSecret)
                .build();
    }

    /**
     * Prepares the given container for sending to a newsletter by producing a
     * fully populated {@link NewsletterMessageInfo}. Newsletters do not use
     * E2E encryption so neither a {@code messageSecret} nor an ICDC stage is
     * involved.
     *
     * @param newsletterJid the newsletter JID
     * @param container     the raw message container
     * @return the prepared message info, ready for the send pipeline
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalStateException    if the client is not logged in
     * @throws IllegalArgumentException if the user has not joined the newsletter
     */
    @WhatsAppWebExport(moduleName = "WAWebNewsletterSendMessageQueryJob", exports = "querySendNewsletterMessage",
            adaptation = WhatsAppAdaptation.ADAPTED)
    NewsletterMessageInfo prepareNewsletter(Jid newsletterJid, MessageContainer container) {
        Objects.requireNonNull(newsletterJid, "newsletterJid");
        Objects.requireNonNull(container, "container");

        var localJid = store.jid()
                .orElseThrow(() -> new IllegalStateException("Not logged in"));
        var newsletter = store.findNewsletterByJid(newsletterJid)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot send to a newsletter that you didn't join: " + newsletterJid));
        var oldServerId = newsletter.newestMessage()
                .map(NewsletterMessageInfo::serverId)
                .orElse(0);
        var key = new MessageKeyBuilder()
                .id(MessageIdGenerator.generate(MessageIdVersion.V2, localJid))
                .parentJid(newsletterJid)
                .fromMe(true)
                .build();
        return new NewsletterMessageInfoBuilder()
                .key(key)
                .serverId(oldServerId + 1)
                .timestamp(Instant.now())
                .message(container)
                .status(MessageStatus.PENDING)
                .build();
    }

    /**
     * Validates the addon-encryption state of the container and auto-converts
     * a {@link ReactionMessage} or {@link CommentMessage} into its encrypted
     * counterpart when the target chat is a CAG group.
     *
     * @param container the original message container
     * @param chatJid   the target chat JID
     * @param selfJid   the sender's own JID
     * @return the container, possibly with addon content converted in place
     * @throws IllegalArgumentException if a poll vote is missing its encrypted
     *                                  metadata or an encrypted addon is missing
     *                                  its payload or IV
     */
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryptAddonMsgData", exports = "encryptAddOn",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryptAddonMsgData", exports = "createDualEncryptionHelper",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private MessageContainer prepareAddonContent(
            MessageContainer container,
            Jid chatJid,
            Jid selfJid
    ) {

        return switch (container.content()) {
            case PollUpdateMessage poll -> {
                if (poll.metadata().isEmpty()) {
                    throw new IllegalArgumentException(
                            "PollUpdateMessage must have encrypted metadata: "
                            + "use PollUpdateMessageSimpleBuilder to create poll votes");
                }
                yield container;
            }

            case ReactionMessage reaction when requiresEncryptedReaction(chatJid) -> {
                var parentMessage = resolveParentMessage(chatJid, reaction.key().orElse(null));
                if (parentMessage.isEmpty()) {
                    throw new IllegalArgumentException("Cannot encrypt reaction: parent message not found");
                }
                var encrypted = EncMessageFactory.encryptReaction(reaction, parentMessage.get(), selfJid);
                yield MessageContainer.of(encrypted);
            }

            case EncReactionMessage enc -> {
                Objects.requireNonNull(enc.encPayload(),
                        "EncryptedReactionMessage must have encPayload populated");
                Objects.requireNonNull(enc.encIv(),
                        "EncryptedReactionMessage must have encIv populated");
                yield container;
            }

            case EncEventResponseMessage enc -> {
                Objects.requireNonNull(enc.encPayload(),
                        "EncryptedEventResponseMessage must have encPayload populated");
                Objects.requireNonNull(enc.encIv(),
                        "EncryptedEventResponseMessage must have encIv populated");
                yield container;
            }

            case SecretEncMessage enc -> {
                Objects.requireNonNull(enc.encPayload(),
                        "SecretEncryptedMessage must have encPayload populated");
                Objects.requireNonNull(enc.encIv(),
                        "SecretEncryptedMessage must have encIv populated");
                yield container;
            }

            case CommentMessage comment when requiresEncryptedReaction(chatJid) -> {
                var parentMessage = resolveParentMessage(chatJid, comment.targetMessageKey().orElse(null));
                if (parentMessage.isEmpty()) {
                    throw new IllegalArgumentException("Cannot encrypt comment: parent message not found");
                }
                var encrypted = EncMessageFactory.encryptComment(comment, parentMessage.get(), selfJid);
                yield MessageContainer.of(encrypted);
            }

            case EncCommentMessage enc -> {
                Objects.requireNonNull(enc.encPayload(),
                        "EncryptedCommentMessage must have encPayload populated");
                Objects.requireNonNull(enc.encIv(),
                        "EncryptedCommentMessage must have encIv populated");
                yield container;
            }

            default -> container;
        };
    }

    /**
     * Returns whether a reaction or comment to this chat must be sent as the
     * encrypted addon variant. Encrypted addons are required for CAG default
     * subgroups using LID addressing.
     *
     * @param chatJid the target chat JID
     * @return {@code true} when the chat requires encrypted addons
     */
    @WhatsAppWebExport(moduleName = "WAWebSendGroupMsgJob", exports = "isCagAddon",
            adaptation = WhatsAppAdaptation.DIRECT)
    private boolean requiresEncryptedReaction(Jid chatJid) {
        if (!chatJid.hasGroupOrCommunityServer()) {
            return false;
        }

        var metadata = store.findChatMetadata(chatJid).orElse(null);
        return metadata instanceof GroupMetadata group
                && group.isDefaultSubgroup();
    }

    /**
     * Resolves the parent chat message referenced by the given key.
     *
     * @param parentJid the chat JID containing the parent message
     * @param key       the message key referencing the parent, or {@code null}
     * @return the resolved parent message, or empty when not found
     */
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryptAddonMsgData", exports = "encryptAddOn",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private Optional<ChatMessageInfo> resolveParentMessage(Jid parentJid, MessageKey key) {
        return key == null ? Optional.empty() : key.id()
                .flatMap(id -> store.findMessageById(parentJid, id))
                .filter(entry -> entry instanceof ChatMessageInfo)
                .map(entry -> (ChatMessageInfo) entry);
    }
}
