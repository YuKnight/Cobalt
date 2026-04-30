package com.github.auties00.cobalt.message.receive;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.message.MessageKeyBuilder;
import com.github.auties00.cobalt.model.newsletter.NewsletterMessageInfo;
import com.github.auties00.cobalt.model.newsletter.NewsletterMessageInfoBuilder;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageStatus;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.time.Instant;

/**
 * Processes incoming plaintext newsletter messages into {@link NewsletterMessageInfo}
 * records.
 *
 * <p>Newsletter messages are not end-to-end encrypted: the content is carried as raw
 * protobuf bytes inside a {@code <plaintext>} child of the message node. The stanza
 * also carries a server-assigned {@code server_id}, a timestamp, and an
 * {@code is_sender} attribute indicating whether the current user authored the post.
 *
 * @implNote WA Web routes newsletters through three separate modules
 * ({@code WAWebNewsletterMsgParser}, {@code WAWebNewsletterMsgProcessor},
 * {@code WAWebNewsletterMsgUtils}). Cobalt folds them into one method since neither
 * preprocessing nor SMAX envelope handling has an independent Java counterpart.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleNewsletterMsg")
final class NewsletterMessageReceiver extends MessageReceiver<NewsletterMessageInfo> {
    /**
     * Logger for newsletter message processing diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(NewsletterMessageReceiver.class.getName());

    /**
     * Constructs a new newsletter message receiver.
     *
     * @param store the central session data store
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleNewsletterMsg", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    NewsletterMessageReceiver(WhatsAppStore store) {
        super(store);
    }

    /**
     * Processes an incoming plaintext newsletter message node.
     *
     * <p>Extracts {@code id}, {@code t} (timestamp), {@code server_id}, and
     * {@code is_sender} attributes; reads the raw protobuf bytes from the
     * {@code <plaintext>} child; decodes them into a {@code MessageContainer}; and
     * assembles a {@link NewsletterMessageInfo} with a {@code DELIVERED} status.
     *
     * @param node    the raw {@code <message>} node
     * @param fromJid the newsletter JID from the {@code from} attribute
     * @return the processed newsletter message info, or {@code null} if the plaintext
     *         content is missing or cannot be decoded
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleNewsletterMsg", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    NewsletterMessageInfo receive(Node node, Jid fromJid) {
        var id = node.getRequiredAttributeAsString("id");
        var timestampSeconds = node.getRequiredAttributeAsLong("t");
        var timestamp = Instant.ofEpochSecond(timestampSeconds);
        var serverId = node.getRequiredAttributeAsInt("server_id");
        var isSender = "true".equals(node.getAttributeAsString("is_sender", null));

        var plaintext = node.getChild("plaintext")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        if (plaintext == null || plaintext.length == 0) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Newsletter message {0} has no plaintext content", id);
            return null;
        }

        var container = decodeProtobuf(id, plaintext);
        if (container == null) {
            return null;
        }

        var key = new MessageKeyBuilder()
                .id(id)
                .parentJid(fromJid)
                .fromMe(isSender)
                .build();

        var info = new NewsletterMessageInfoBuilder()
                .key(key)
                .serverId(serverId)
                .timestamp(timestamp)
                .message(container)
                .status(MessageStatus.DELIVERED)
                .build();

        LOGGER.log(System.Logger.Level.DEBUG,
                "Processed newsletter message {0} from {1}", id, fromJid);
        return info;
    }
}
