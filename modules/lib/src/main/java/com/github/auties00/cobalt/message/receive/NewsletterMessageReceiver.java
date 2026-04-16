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
 * Processes incoming plaintext newsletter messages into
 * {@link NewsletterMessageInfo} records.
 *
 * <p>Unlike regular chat messages, newsletter messages are not
 * end-to-end encrypted: the content is carried as raw protobuf bytes
 * inside a {@code <plaintext>} child of the message node. The stanza
 * also carries a server-assigned {@code server_id}, a timestamp, and
 * an {@code is_sender} attribute indicating whether the current user
 * authored the post. Cobalt decodes the protobuf, constructs the
 * message key, and builds a {@link NewsletterMessageInfo} with a
 * {@code DELIVERED} status.
 *
 * @implNote WAWebHandleNewsletterMsg.default: the main entry point
 * for incoming newsletter message handling. Cobalt adapts the
 * pipeline by folding WAWebNewsletterMsgParser,
 * WAWebNewsletterMsgProcessor.preprocessNewsletterMsg, and
 * WAWebNewsletterMsgUtils.mapMsgStanzaToMsgData into this single
 * method.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleNewsletterMsg")
final class NewsletterMessageReceiver extends MessageReceiver<NewsletterMessageInfo> {
    /**
     * Logger for diagnostic messages during newsletter message
     * processing.
     *
     * @implNote WAWebHandleNewsletterMsg uses WALogger with tagged
     * template literals; Cobalt uses {@code System.Logger} instead.
     */
    private static final System.Logger LOGGER = System.getLogger(NewsletterMessageReceiver.class.getName());

    /**
     * Constructs a new newsletter message receiver with the required
     * store dependency.
     *
     * @param store the central session data store
     *
     * @implNote WAWebHandleNewsletterMsg uses module-level imports
     * for store access; Cobalt uses constructor-based DI.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleNewsletterMsg", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    NewsletterMessageReceiver(WhatsAppStore store) {
        super(store);
    }

    /**
     * Processes an incoming plaintext newsletter message node.
     *
     * <p>Extracts the {@code id}, {@code t} (timestamp),
     * {@code server_id}, and {@code is_sender} attributes from the
     * stanza, reads the raw protobuf bytes from the
     * {@code <plaintext>} child, decodes them into a
     * {@code MessageContainer}, and assembles a
     * {@link NewsletterMessageInfo} with a {@code DELIVERED} status.
     *
     * @param node    the raw {@code <message>} node
     * @param fromJid the newsletter JID from the {@code from} attribute
     * @return the processed newsletter message info, or {@code null}
     *         if the plaintext content is missing or cannot be decoded
     *
     * @implNote WAWebHandleNewsletterMsg.default: calls
     * WAWebNewsletterMsgParser.default which delegates to the SMAX
     * parser (WASmaxMessageDeliverNewsletterRPC) to extract
     * {@code id}, {@code server_id}, {@code t}, {@code is_sender},
     * and the plaintext payload, then processes the parsed message
     * through WAWebNewsletterMsgProcessor.preprocessNewsletterMsg
     * and WAWebNewsletterMsgUtils.mapMsgStanzaToMsgData. Cobalt folds
     * these steps into one method since neither preprocessing nor
     * SMAX envelope handling has an independent Java counterpart.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleNewsletterMsg", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    NewsletterMessageInfo receive(Node node, Jid fromJid) {
        // WAWebNewsletterMsgParser.default
        // Extracts the stanza identifier attribute

        var id = node.getRequiredAttributeAsString("id");

        // WAWebNewsletterMsgParser.default
        // Extracts and converts the timestamp from the t attribute

        var timestampSeconds = node.getRequiredAttributeAsLong("t");
        var timestamp = Instant.ofEpochSecond(timestampSeconds);

        // WAWebNewsletterMsgParser.default
        // Extracts the server-assigned newsletter message id

        var serverId = node.getRequiredAttributeAsInt("server_id");

        // WAWebNewsletterMsgParser.default
        // Detects whether the current user authored this post via the is_sender attribute

        var isSender = "true".equals(node.getAttributeAsString("is_sender", null));

        // WAWebNewsletterMsgUtils.mapMsgStanzaToMsgData
        // Retrieves the plaintext protobuf payload from the plaintext child

        var plaintext = node.getChild("plaintext")
                .flatMap(Node::toContentBytes)
                .orElse(null);
        if (plaintext == null || plaintext.length == 0) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Newsletter message {0} has no plaintext content", id);
            return null;
        }

        // WAWebNewsletterMsgUtils.mapMsgStanzaToMsgData
        // Decodes the plaintext as a MessageContainer protobuf

        var container = decodeProtobuf(id, plaintext);
        if (container == null) {
            return null;
        }

        // WAWebNewsletterMsgUtils.mapMsgStanzaToMsgData
        // Constructs the message key with the newsletter JID as parent

        var key = new MessageKeyBuilder()
                .id(id)
                .parentJid(fromJid)
                .fromMe(isSender)
                .build();

        // WAWebNewsletterMsgUtils.mapMsgStanzaToMsgData
        // Assembles the newsletter message info with DELIVERED status

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
