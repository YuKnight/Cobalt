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
 * Orchestrates the processing of every incoming {@code <message>}
 * stanza by routing it to the correct receiver.
 *
 * <p>This service is the single entry point for inbound message
 * handling and mirrors the role of the outbound sending service. It
 * inspects the stanza's {@code from} JID and dispatches:
 * <ul>
 *   <li>Newsletter JIDs (for example {@code "xxx@newsletter"}) go to
 *       {@link NewsletterMessageReceiver}, which handles the plaintext
 *       newsletter pipeline.</li>
 *   <li>All other JIDs go to {@link ChatMessageReceiver}, which
 *       handles the full Signal protocol decryption pipeline for 1:1,
 *       group, broadcast, status, and peer messages.</li>
 * </ul>
 * A pending-message dedup cache prevents duplicate processing when
 * the same message is delivered more than once during offline/online
 * transitions.
 *
 * @implNote WAWebCommsHandleMessagingStanza.handleMessagingStanza
 * handles the {@code "message"} tag for non-newsletter E2E messages
 * (routing to WAWebHandleMsg) and the {@code "receipt"} tag for
 * non-call, non-retry receipts (routing to WAWebHandleMsgReceipt).
 * Newsletter messages fall through to
 * WAWebCommsHandleWorkerCompatibleStanza.handleWorkerCompatibleStanza
 * which routes them to WAWebHandleNewsletterMsg. Cobalt unifies
 * both newsletter and E2E message routing here, while receipt
 * handling is split out to a separate {@code ReceiptStreamHandler}
 * at the socket stream level.
 */
@WhatsAppWebModule(moduleName = "WAWebCommsHandleMessagingStanza")
@WhatsAppWebModule(moduleName = "WAWebHandleMsg")
public final class MessageReceivingService {
    /**
     * Logger for diagnostic messages during message routing and dedup
     * handling.
     *
     * @implNote WAWebCommsHandleMessagingStanza uses WALogger;
     * Cobalt uses {@code System.Logger} instead.
     */
    private static final System.Logger LOGGER = System.getLogger(MessageReceivingService.class.getName());

    /**
     * The receiver handling E2E-encrypted chat messages.
     *
     * @implNote WAWebCommsHandleMessagingStanza.handleMessagingStanza
     * delegates to WAWebHandleMsg for non-newsletter messages; Cobalt
     * uses constructor-based DI via this field.
     */
    private final ChatMessageReceiver chatReceiver;

    /**
     * The receiver handling plaintext newsletter messages.
     *
     * @implNote WAWebCommsHandleWorkerCompatibleStanza.handleWorkerCompatibleStanza
     * delegates newsletter messages to WAWebHandleNewsletterMsg;
     * Cobalt uses constructor-based DI via this field.
     */
    private final NewsletterMessageReceiver newsletterReceiver;

    /**
     * Dedup cache preventing duplicate processing of the same E2E
     * message during offline/online transitions.
     *
     * @implNote WAWebMessageDedupUtils maintains a module-level
     * pending message cache keyed by
     * WAWebPendingMessageKey.createPendingMessageKey(key, ts, encs)
     * that is cleared when the offline delivery count reaches zero.
     * In WA Web the dedup is invoked from WAWebHandleMsg rather than
     * from handleMessagingStanza; Cobalt wraps the dedup check around
     * the receiver call in {@link #process(Node)}.
     */
    private final MessageDedup dedup;

    /**
     * Creates a new message receiving service and assembles the
     * internal receiver graph from the provided dependencies.
     *
     * @param store      the central session data repository
     * @param decryption the decryption service for Signal protocol
     *                   (PKMSG/MSG/SKMSG) and bot messages (MSMSG)
     *
     * @implNote WAWebCommsHandleMessagingStanza uses module-level
     * imports for WAWebHandleMsg and WAWebHandleMsgReceipt; Cobalt
     * assembles the receiver graph via constructor-based DI.
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
     * Processes an incoming {@code <message>} node, producing the
     * appropriate {@link MessageInfo} subtype.
     *
     * <p>Newsletter messages (identified by the newsletter server on
     * the {@code from} JID) produce {@link NewsletterMessageInfo};
     * all other messages go through E2E decryption and produce
     * {@link ChatMessageInfo}. Returns {@code null} for unavailable
     * fanout placeholders that should be silently acknowledged.
     *
     * @param node the raw incoming {@code <message>} node
     * @return the processed message info, or {@code null} for
     *         unavailable messages
     *
     * @throws NullPointerException             if {@code node} is {@code null}
     * @throws WhatsAppMessageException.Receive if decryption or
     *         validation fails for E2E messages
     *
     * @implNote WAWebCommsHandleMessagingStanza.handleMessagingStanza
     * checks {@code WAWebWid.isNewsletter(from)} and returns
     * {@code undefined} for newsletter messages so they fall through
     * to WAWebCommsHandleWorkerCompatibleStanza which calls
     * WAWebHandleNewsletterMsg. For non-newsletter messages it calls
     * {@code WAWebHandleMsg(e)}. Cobalt unifies both paths here, with
     * the newsletter check ({@code fromJid.hasNewsletterServer()})
     * routing to {@link NewsletterMessageReceiver} and all other
     * messages going to {@link ChatMessageReceiver}.
     */
    @WhatsAppWebExport(moduleName = "WAWebCommsHandleMessagingStanza", exports = "handleMessagingStanza",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebHandleMsg", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MessageInfo process(Node node) {
        Objects.requireNonNull(node, "node");

        // WAWebCommsHandleMessagingStanza.handleMessagingStanza
        // Routes newsletter messages (WAWebWid.isNewsletter) to the newsletter receiver
        var fromJid = node.getRequiredAttributeAsJid("from");
        if (fromJid.hasNewsletterServer()) {
            return newsletterReceiver.receive(node, fromJid);
        }

        // WAWebHandleMsg and WAWebMessageDedupUtils.addPendingMessage
        // Builds a simplified dedup key and skips already-pending messages to avoid double processing
        var id = node.getRequiredAttributeAsString("id");
        var dedupKey = fromJid + ":" + id;
        if (dedup.isPending(dedupKey)) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Duplicate message {0}, skipping", id);
            return null;
        }
        dedup.add(dedupKey);

        // WAWebHandleMsg
        // Delegates to the chat receiver and guarantees dedup cleanup in a finally block
        try {
            return chatReceiver.receive(node, fromJid);
        } finally {
            dedup.remove(dedupKey);
        }
    }

    /**
     * Clears the pending-message dedup cache.
     *
     * <p>Should be invoked when the offline delivery phase ends so
     * that messages re-received in a new session are not mistakenly
     * considered duplicates.
     *
     * @implNote WAWebMessageDedupUtils.maybeClearPendingMessages:
     * clears the cache when the offline count reaches zero.
     */
    @WhatsAppWebExport(moduleName = "WAWebMessageDedupUtils", exports = "maybeClearPendingMessages",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public void clearPendingMessages() {
        // WAWebMessageDedupUtils.maybeClearPendingMessages
        // Clears the pending-message cache to let newly arriving messages be processed fresh
        dedup.clear();
    }
}
