package com.github.auties00.cobalt.stream.receipt;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.message.MessageService;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.stream.SocketStream;
import com.github.auties00.cobalt.call.signaling.CallReceiptReceiver;
import com.github.auties00.cobalt.wam.WamService;

/**
 * Dispatches incoming {@code <receipt>} stanzas to the appropriate specialised
 * handler based on the child tag they carry and the stanza {@code type}.
 *
 * <p>WhatsApp multiplexes three distinct receipt flows onto the same stanza tag:
 * <ul>
 *   <li>Call receipts: carry an {@code <offer>}, {@code <accept>}, or
 *       {@code <reject>} child and acknowledge the state of a VoIP call
 *       signalling message</li>
 *   <li>Retry receipts: have {@code type="retry"} or
 *       {@code type="enc_rekey_retry"} and request a re-send of a previously
 *       failed-to-decrypt message</li>
 *   <li>Message receipts: acknowledge delivery, read, and play states for
 *       individual or group messages (every other receipt)</li>
 * </ul>
 *
 * <p>This handler inspects the first child of the incoming stanza, routes
 * call receipts to the {@link CallReceiptReceiver}, and forwards
 * everything else (both retry and regular message receipts) to the
 * {@link MessageReceiptStreamHandler}, which performs the secondary
 * {@code retry}/{@code enc_rekey_retry} split internally.
 */
@WhatsAppWebModule(moduleName = "WAWebCommsHandleWorkerCompatibleStanza")
@WhatsAppWebModule(moduleName = "WAWebCommsHandleLoggedInStanza")
@WhatsAppWebModule(moduleName = "WAWebCommsHandleMessagingStanza")
@WhatsAppWebModule(moduleName = "WAWebCommsHandleStanzaUtils")
public final class ReceiptStreamHandler implements SocketStream.Handler {
    /**
     * Handler for VoIP call receipts carrying {@code <offer>},
     * {@code <accept>}, or {@code <reject>} children.
     */
    private final CallReceiptReceiver callReceiptHandler;

    /**
     * Handler for message delivery, read, and play receipts.
     */
    private final MessageReceiptStreamHandler messageReceiptHandler;

    /**
     * Constructs a new receipt dispatcher wired with the dependencies
     * required by both the call-receipt and the message-receipt handlers.
     *
     * @param whatsapp       the WhatsApp client used to send acknowledgements
     *                       and access the local store
     * @param messageService the message service used by the message-receipt
     *                       handler to propagate state changes
     * @param wamService     the WAM telemetry service forwarded to the message-receipt handler
     */
    public ReceiptStreamHandler(WhatsAppClient whatsapp, MessageService messageService, WamService wamService) {
        this.callReceiptHandler = new CallReceiptReceiver(whatsapp);
        this.messageReceiptHandler = new MessageReceiptStreamHandler(whatsapp, messageService, wamService);
    }

    /**
     * Routes an incoming {@code <receipt>} stanza to the appropriate
     * specialised handler.
     *
     * <p>Call receipts (first child is {@code <offer>}, {@code <accept>}, or
     * {@code <reject>}) are forwarded to the {@link CallReceiptReceiver}.
     * All other receipts — both {@code retry}/{@code enc_rekey_retry} and
     * regular delivery/read/played — are forwarded to the
     * {@link MessageReceiptStreamHandler}, which performs the retry vs.
     * message-receipt split internally.
     *
     * @param node the incoming {@code <receipt>} stanza
     */
    @Override
    public void handle(Node node) {
        if (isCallReceipt(node)) {
            callReceiptHandler.handle(node);
            return;
        }

        // Both retry and regular receipts land in MessageReceiptStreamHandler, which performs the retry vs regular split via isRetryReceipt().
        messageReceiptHandler.handle(node);
    }

    /**
     * Resets both underlying handlers so that any per-connection state
     * (for example, in-flight retry tracking) is discarded.
     */
    @Override
    public void reset() {
        callReceiptHandler.reset();
        messageReceiptHandler.reset();
    }

    /**
     * Checks whether the given receipt stanza targets a VoIP call by looking
     * at the first child's tag.
     *
     * @param node the {@code <receipt>} stanza to classify
     * @return {@code true} if the first child is {@code <offer>},
     *         {@code <accept>}, or {@code <reject>}; {@code false} otherwise
     */
    private boolean isCallReceipt(Node node) {
        var child = node.getChild().orElse(null);
        return child != null && switch (child.description()) {
            case "offer", "accept", "reject" -> true;
            default -> false;
        };
    }
}
