package com.github.auties00.cobalt.stream.receipt;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.message.MessageService;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.stream.SocketStream;
import com.github.auties00.cobalt.stream.call.CallReceiptStreamHandler;

/**
 * Dispatches incoming {@code <receipt>} stanzas to the appropriate specialised
 * handler based on the child tag they carry.
 *
 * <p>WhatsApp multiplexes two distinct receipt flows onto the same stanza tag:
 * <ul>
 *   <li>Call receipts: carry an {@code <offer>}, {@code <accept>}, or
 *       {@code <reject>} child and acknowledge the state of a VoIP call
 *       signalling message</li>
 *   <li>Message receipts: acknowledge delivery, read, and play states for
 *       individual or group messages</li>
 * </ul>
 *
 * <p>This handler inspects the first child of the incoming stanza, routes
 * call receipts to the {@link CallReceiptStreamHandler}, and forwards
 * everything else to the {@link MessageReceiptStreamHandler}.
 *
 * @implNote WA Web dispatches the same two paths inline inside its
 * {@code WAWebHandleReceipt} module using {@code hasChild} checks on
 * {@code offer}/{@code accept}/{@code reject}. Cobalt splits the two paths
 * into dedicated handler classes for testability.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleReceipt")
public final class ReceiptStreamHandler implements SocketStream.Handler {
    /**
     * Handler for VoIP call receipts carrying {@code <offer>},
     * {@code <accept>}, or {@code <reject>} children.
     */
    private final CallReceiptStreamHandler callReceiptHandler;

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
     */
    public ReceiptStreamHandler(WhatsAppClient whatsapp, MessageService messageService) {
        this.callReceiptHandler = new CallReceiptStreamHandler(whatsapp);
        this.messageReceiptHandler = new MessageReceiptStreamHandler(whatsapp, messageService);
    }

    /**
     * Routes an incoming {@code <receipt>} stanza to the appropriate
     * specialised handler based on its first child.
     *
     * @param node the incoming {@code <receipt>} stanza
     */
    @Override
    public void handle(Node node) {
        if (isCallReceipt(node)) {
            callReceiptHandler.handle(node);
            return;
        }

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
     * @implNote WA Web performs the same classification inline using
     * {@code hasChild} on the three call signalling tags.
     */
    private boolean isCallReceipt(Node node) {
        var child = node.getChild().orElse(null);
        return child != null && switch (child.description()) {
            case "offer", "accept", "reject" -> true;
            default -> false;
        };
    }
}
