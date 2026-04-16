package com.github.auties00.cobalt.stream.control;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.stream.SocketStream;

/**
 * Handles the top-level {@code <error>} stanza emitted by the WhatsApp
 * server for protocol-level problems that are not scoped to any specific
 * request or session event.
 *
 * <p>The most common payload here is {@code code=479}
 * ({@code smax-invalid}), which the server sends when the client's last
 * outbound stanza failed the server-side schema-driven parser. For any
 * other code value the handler logs the event at {@code ERROR} so that the
 * unknown condition is surfaced in the application logs without tearing
 * down the session.
 *
 * <p>This handler never dispatches a failure to the client's error handler:
 * generic {@code <error>} stanzas are informational and do not require the
 * session to be reset.
 *
 * @implNote WAWebHandleError: mirrors the top-level error stanza parser
 * that WA Web installs via {@code WADeprecatedWapParser("error", ...)}.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleError")
public final class ErrorStreamHandler implements SocketStream.Handler {
    /**
     * Logger for diagnostic messages about received error stanzas.
     */
    private static final System.Logger LOGGER = System.getLogger(ErrorStreamHandler.class.getName());

    /**
     * Reason code emitted by the server when the client sent a stanza that
     * failed validation against the server-side schema.
     *
     * @implNote WAWebSmaxInvalidError: the server reports schema validation
     * failures via the {@code smax-invalid} tag with code {@code 479}.
     */
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
     * received code.
     *
     * <p>Behaviour by {@code code} attribute:
     * <ul>
     *   <li>missing code: logged at {@code WARNING}</li>
     *   <li>{@code 479} (smax-invalid): logged at {@code WARNING} to flag
     *       that the previous outbound stanza failed server-side schema
     *       validation</li>
     *   <li>any other code: logged at {@code ERROR}</li>
     * </ul>
     *
     * @param node the {@code <error>} stanza received from the server
     * @implNote WAWebHandleError: Cobalt omits any recovery logic because
     * WA Web treats these stanzas as informational too.
     */
    @Override
    public void handle(Node node) {
        var code = node.getAttributeAsInt("code", (Integer) null);
        if (code == null) {
            LOGGER.log(System.Logger.Level.WARNING, "Received error stanza without code: {0}", node);
            return;
        }

        if (code == SMAX_INVALID_CODE) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Received error stanza for invalid stanza sent (smax-invalid, code={0})",
                    code);
            return;
        }

        LOGGER.log(System.Logger.Level.ERROR, "Received error stanza with unknown code={0}", code);
    }
}
