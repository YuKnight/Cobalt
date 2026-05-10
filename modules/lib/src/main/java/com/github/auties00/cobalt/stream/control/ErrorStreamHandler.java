package com.github.auties00.cobalt.stream.control;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.stream.SocketStream;

/**
 * Handles the top-level {@code <error>} stanza emitted by the WhatsApp
 * server for protocol-level problems that are not scoped to any specific
 * request or session event.
 *
 * <p>The most common payload here is {@code code=479}
 * ({@code smax-invalid}), which the server sends when the client's last
 * outbound stanza failed the server-side schema-driven parser. Any other
 * code value is logged at {@code ERROR} so that the unknown condition is
 * surfaced in the application logs without tearing down the session.
 *
 * <p>This handler never dispatches a failure to the client's error handler:
 * generic {@code <error>} stanzas are informational and do not require the
 * session to be reset. The handler is registered for the {@code "error"}
 * tag inside {@link SocketStream}, mirroring the
 * {@code WAWebCommsHandleLoggedInStanza} switch arm
 * {@code case "error": return WABackendHandleError.handleError(e)}.
 */
@WhatsAppWebModule(moduleName = "WABackendHandleError")
public final class ErrorStreamHandler implements SocketStream.Handler {
    /**
     * Logger for diagnostic messages about received error stanzas.
     */
    private static final System.Logger LOGGER = System.getLogger(ErrorStreamHandler.class.getName());

    /**
     * Reason code emitted by the server when the client sent a stanza that
     * failed validation against the server-side schema.
     */
    @WhatsAppWebExport(moduleName = "WABackendHandleError", exports = "SMAX_INVALID", adaptation = WhatsAppAdaptation.DIRECT)
    private static final int SMAX_INVALID_CODE = 479;

    /**
     * Constructs a new top-level {@code <error>} stanza handler.
     *
     * <p>No dependencies are required because the handler only logs
     * diagnostics; it does not dispatch exceptions or send stanzas.
     */
    public ErrorStreamHandler() {
    }

    /**
     * Handles an incoming top-level {@code <error>} stanza by logging the
     * received code at the appropriate severity.
     *
     * <p>Behaviour by {@code code} attribute:
     * <ul>
     *   <li>{@code 479} (smax-invalid): logged at {@code ERROR} with the
     *       message {@code "Invalid stanza sent (smax-invalid)"} to flag
     *       that the previous outbound stanza failed server-side schema
     *       validation</li>
     *   <li>any other code: logged at {@code ERROR} with the message
     *       {@code "Unknown error code: <code>"}</li>
     *   <li>missing or non-numeric code: logged at {@code WARNING} as a
     *       defensive fallback (WA Web would raise
     *       {@code XmppParsingFailure} inside the parser, but Cobalt's
     *       {@link SocketStream} dispatcher never auto-acks and has no
     *       parsing-failure recovery on this path)</li>
     * </ul>
     *
     * @param node the {@code <error>} stanza received from the server
     */
    @Override
    @WhatsAppWebExport(moduleName = "WABackendHandleError", exports = "handleError", adaptation = WhatsAppAdaptation.ADAPTED)
    public void handle(Node node) {
        var code = node.getAttributeAsInt("code", null);
        if (code == null) {
            // Cobalt logs and returns so that a malformed error stanza never propagates; WA Web's parser would throw XmppParsingFailure.
            LOGGER.log(System.Logger.Level.WARNING, "Received error stanza without code: {0}", node);
            return;
        }

        if (code == SMAX_INVALID_CODE) {
            LOGGER.log(System.Logger.Level.ERROR, "Invalid stanza sent (smax-invalid)");
            return;
        }

        LOGGER.log(System.Logger.Level.ERROR, "Unknown error code: {0}", code);
    }
}
