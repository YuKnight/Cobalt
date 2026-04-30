package com.github.auties00.cobalt.stream.receipt;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.message.MessageService;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.stream.SocketStream;
import com.github.auties00.cobalt.stream.call.CallReceiptStreamHandler;
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
 * call receipts to the {@link CallReceiptStreamHandler}, and forwards
 * everything else (both retry and regular message receipts) to the
 * {@link MessageReceiptStreamHandler}, which performs the secondary
 * {@code retry}/{@code enc_rekey_retry} split internally.
 *
 * @implNote WA Web splits this dispatch across three sibling modules:
 * <ul>
 *   <li>{@code WAWebCommsHandleMessagingStanza.handleMessagingStanza}
 *       handles the non-call, non-retry receipt branch by calling
 *       {@code WAWebHandleMsgReceipt(e)} when
 *       {@code !isCallReceipt(e) && attrs.type !== "retry"}</li>
 *   <li>{@code WAWebCommsHandleWorkerCompatibleStanza.handleWorkerCompatibleStanza}
 *       handles call receipts by calling
 *       {@code WAWebHandleVoipCallReceipt.handleCallReceipt(t)} when
 *       {@code WAWebCommsHandleStanzaUtils.isCallReceipt(t)} returns
 *       {@code true}. Its {@code isCallReceipt} implementation checks
 *       {@code content[0].tag} against the literals
 *       {@code "offer"}, {@code "accept"}, and {@code "reject"}</li>
 *   <li>{@code WAWebCommsHandleLoggedInStanza.handleLoggedInStanza}
 *       handles retry receipts by calling
 *       {@code WAWebHandleMessageRetryRequest.handleMessageRetryRequest(e)}
 *       when {@code attrs.type === "retry" || attrs.type === "enc_rekey_retry"},
 *       otherwise it emits a {@code WALogger.WARN} "Unhandled receipt stanza"
 *       log line</li>
 * </ul>
 * Cobalt collapses these three entry points into a single dispatch class
 * that performs the call-receipt branch inline and delegates the remaining
 * branch (retry vs. regular) to {@link MessageReceiptStreamHandler}. The
 * WA Web parse-failure recovery (nack with
 * {@code NackReason.ParsingError}/{@code UnhandledError}) is replaced by
 * Cobalt's configurable {@code WhatsAppClientErrorHandler} pipeline and
 * lives outside this class.
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
     * @param wamService     the WAM telemetry service forwarded to the message-receipt handler
     */
    public ReceiptStreamHandler(WhatsAppClient whatsapp, MessageService messageService, WamService wamService) {
        this.callReceiptHandler = new CallReceiptStreamHandler(whatsapp);
        this.messageReceiptHandler = new MessageReceiptStreamHandler(whatsapp, messageService, wamService);
    }

    /**
     * Routes an incoming {@code <receipt>} stanza to the appropriate
     * specialised handler.
     *
     * <p>Call receipts (first child is {@code <offer>}, {@code <accept>}, or
     * {@code <reject>}) are forwarded to the {@link CallReceiptStreamHandler}.
     * All other receipts — both {@code retry}/{@code enc_rekey_retry} and
     * regular delivery/read/played — are forwarded to the
     * {@link MessageReceiptStreamHandler}, which performs the retry vs.
     * message-receipt split internally.
     *
     * @param node the incoming {@code <receipt>} stanza
     * @implNote Combines the three WA Web dispatch paths:
     *           {@code WAWebCommsHandleWorkerCompatibleStanza.handleWorkerCompatibleStanza}
     *           (call receipts),
     *           {@code WAWebCommsHandleMessagingStanza.handleMessagingStanza}
     *           (non-call, non-retry receipts), and
     *           {@code WAWebCommsHandleLoggedInStanza.handleLoggedInStanza}
     *           (retry receipts).
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
     * @implNote Mirrors
     * {@code WAWebCommsHandleStanzaUtils.isCallReceipt}, which checks
     * {@code Array.isArray(e.content) && e.content.length > 0} and then
     * matches {@code e.content[0].tag} against the literals
     * {@code "offer"}, {@code "accept"}, and {@code "reject"}.
     */
    private boolean isCallReceipt(Node node) {
        var child = node.getChild().orElse(null);
        return child != null && switch (child.description()) {
            case "offer", "accept", "reject" -> true;
            default -> false;
        };
    }
}
