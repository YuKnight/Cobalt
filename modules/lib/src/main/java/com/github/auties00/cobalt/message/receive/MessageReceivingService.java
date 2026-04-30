package com.github.auties00.cobalt.message.receive;

import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.message.dedup.MessageDedup;
import com.github.auties00.cobalt.message.receive.crypto.MessageDecryption;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.message.MessageInfo;
import com.github.auties00.cobalt.model.newsletter.NewsletterMessageInfo;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.util.Objects;

/**
 * Routes every incoming {@code <message>} stanza to the correct receiver.
 *
 * <p>This service is the single entry point for inbound message handling. The
 * stanza's {@code from} JID determines the path:
 * <ul>
 *   <li>Newsletter JIDs (for example {@code "xxx@newsletter"}) go to
 *       {@link NewsletterMessageReceiver} for the plaintext newsletter pipeline.</li>
 *   <li>All other JIDs go to {@link ChatMessageReceiver} for the full Signal protocol
 *       decryption pipeline (1:1, group, broadcast, status, peer messages).</li>
 * </ul>
 *
 * <p>A pending-message dedup cache prevents duplicate processing when the same
 * message is delivered more than once during offline/online transitions.
 *
 * @implNote WA Web splits this routing across {@code WAWebCommsHandleMessagingStanza}
 * (for non-newsletter E2E messages) and {@code WAWebCommsHandleWorkerCompatibleStanza}
 * (newsletter fallback). Cobalt unifies both paths here, while receipt handling lives
 * in the separate {@code ReceiptStreamHandler} at the socket stream level.
 */
@WhatsAppWebModule(moduleName = "WAWebCommsHandleMessagingStanza")
@WhatsAppWebModule(moduleName = "WAWebHandleMsg")
public final class MessageReceivingService {
    /**
     * Logger for routing and dedup diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(MessageReceivingService.class.getName());

    /**
     * Receiver handling E2E-encrypted chat messages.
     */
    private final ChatMessageReceiver chatReceiver;

    /**
     * Receiver handling plaintext newsletter messages.
     */
    private final NewsletterMessageReceiver newsletterReceiver;

    /**
     * Dedup cache preventing duplicate processing of the same E2E message during
     * offline/online transitions.
     */
    private final MessageDedup dedup;

    /**
     * Creates a new message receiving service and assembles the internal receiver
     * graph from the provided dependencies.
     *
     * @param store      the central session data repository
     * @param decryption the decryption service for Signal protocol (PKMSG/MSG/SKMSG)
     *                   and bot messages (MSMSG)
     */
    @WhatsAppWebExport(moduleName = "WAWebCommsHandleMessagingStanza", exports = "handleMessagingStanza",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MessageReceivingService(
            WhatsAppStore store,
            MessageDecryption decryption
    ) {
        this.chatReceiver = new ChatMessageReceiver(store, decryption);
        this.newsletterReceiver = new NewsletterMessageReceiver(store);
        this.dedup = new MessageDedup();
    }

    /**
     * Processes an incoming {@code <message>} node, producing the appropriate
     * {@link MessageInfo} subtype.
     *
     * <p>Newsletter messages produce {@link NewsletterMessageInfo}; all other messages
     * go through E2E decryption and produce {@link ChatMessageInfo}. Returns
     * {@code null} for unavailable fanout placeholders that should be silently
     * acknowledged.
     *
     * @param node the raw incoming {@code <message>} node
     * @return the processed message info, or {@code null} for unavailable messages
     * @throws NullPointerException             if {@code node} is {@code null}
     * @throws WhatsAppMessageException.Receive if decryption or validation fails for
     *                                          E2E messages
     */
    @WhatsAppWebExport(moduleName = "WAWebCommsHandleMessagingStanza", exports = "handleMessagingStanza",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebHandleMsg", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MessageInfo process(Node node) {
        Objects.requireNonNull(node, "node");

        var fromJid = node.getRequiredAttributeAsJid("from");
        if (fromJid.hasNewsletterServer()) {
            return newsletterReceiver.receive(node, fromJid);
        }

        var id = node.getRequiredAttributeAsString("id");
        var dedupKey = fromJid + ":" + id;
        if (dedup.isPending(dedupKey)) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Duplicate message {0}, skipping", id);
            return null;
        }
        dedup.add(dedupKey);

        try {
            return chatReceiver.receive(node, fromJid);
        } finally {
            dedup.remove(dedupKey);
        }
    }

    /**
     * Clears the pending-message dedup cache.
     *
     * <p>Invoked when the offline delivery phase ends so messages re-received in a
     * new session are not mistakenly considered duplicates.
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "maybeClearPendingMessages",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public void clearPendingMessages() {
        dedup.clear();
    }
}
