package com.github.auties00.cobalt.stream.control;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.exception.WhatsAppSessionException;
import com.github.auties00.cobalt.exception.WhatsAppStreamException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.stream.SocketStream;

/**
 * Handles {@code stream:error} stanzas received from the WhatsApp server.
 * <p>
 * Stream errors indicate critical protocol-level issues that require immediate action.
 * The handler classifies the error into one of several types and dispatches the
 * appropriate exception to the client's error handler:
 * <ul>
 *   <li><b>conflict (replaced):</b> another session took over this connection</li>
 *   <li><b>conflict (device_removed or unknown):</b> device was removed or invalidated</li>
 *   <li><b>code 515:</b> server requests reconnection and re-login</li>
 *   <li><b>code 516:</b> server forces logout</li>
 *   <li><b>other 5xx codes:</b> server stream error, socket closed without retry</li>
 *   <li><b>non-5xx codes:</b> unrecognized error code, socket closed</li>
 *   <li><b>ack:</b> acknowledgement-related stream error, socket closed</li>
 *   <li><b>xml-not-well-formed:</b> server reports malformed XML, socket closed</li>
 *   <li><b>other:</b> unrecognized stream error, socket closed</li>
 * </ul>
 */
@WhatsAppWebModule(moduleName = "WAWebHandleStreamError")
public final class StreamErrorStreamHandler implements SocketStream.Handler {

    /**
     * Logger for diagnostic output related to stream error handling.
     */
    private static final System.Logger LOGGER = System.getLogger(StreamErrorStreamHandler.class.getName());

    /**
     * Stream error code indicating that the server requests the client to reconnect and re-login.
     */
    private static final int STREAM_ERROR_RESTART_LOGIN = 515;

    /**
     * Stream error code indicating that the server forces logout of the companion device.
     */
    private static final int STREAM_ERROR_LOGOUT = 516;

    /**
     * The WhatsApp client used to dispatch failure exceptions.
     */
    private final WhatsAppClient whatsapp;

    /**
     * Constructs a new stream error handler with the specified client.
     *
     * @param whatsapp the WhatsApp client to dispatch errors to; must not be {@code null}
     */
    public StreamErrorStreamHandler(WhatsAppClient whatsapp) {
        this.whatsapp = whatsapp;
    }

    /**
     * Handles a {@code stream:error} stanza by classifying the error type and dispatching
     * the appropriate exception to the client's error handler.
     * <p>
     * The classification follows the same priority as WA Web's parser:
     * <ol>
     *   <li>Check for {@code conflict} child node</li>
     *   <li>Check for {@code code} attribute</li>
     *   <li>Check for {@code ack} child node</li>
     *   <li>Check for {@code xml-not-well-formed} child node</li>
     *   <li>Fall through as unrecognized error</li>
     * </ol>
     * All paths that do not produce an early-return exception (conflict, code 515, code 516)
     * result in a socket-close exception being dispatched.
     *
     * @param node the {@code stream:error} node received from the server
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebHandleStreamError", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public void handle(Node node) {
        var conflict = node.getChild("conflict").orElse(null);
        if (conflict != null) {
            handleConflict(conflict.getAttributeAsString("type", null));
            return;
        }

        var code = node.getAttributeAsInt("code", (Integer) null);
        if (code != null) {
            handleCode(code);
            return;
        }

        var ack = node.getChild("ack").orElse(null);
        if (ack != null) {
            // Cobalt logs the ack id for diagnostics before closing the socket; WA Web simply closes the socket.
            LOGGER.log(System.Logger.Level.WARNING,
                    "Received stream:error ack for id={0}",
                    ack.getAttributeAsString("id", null));
            whatsapp.handleFailure(new WhatsAppSessionException.Closed("Stream error: ack"));
            return;
        }

        if (node.hasChild("xml-not-well-formed")) {
            // MalformedNode lets the configurable error handler distinguish a server-reported bad-XML close from a generic session close.
            whatsapp.handleFailure(new WhatsAppStreamException.MalformedNode("Server reported xml-not-well-formed"));
            return;
        }

        LOGGER.log(System.Logger.Level.WARNING, "Received unrecognized stream:error stanza: {0}", node);
        whatsapp.handleFailure(new WhatsAppSessionException.Closed("Stream error: unrecognized"));
    }

    /**
     * Handles a conflict-type stream error by dispatching the appropriate session exception.
     * <p>
     * A conflict type of {@code "replaced"} means another session has taken over this connection,
     * which maps to a {@link WhatsAppSessionException.Conflict}. All other conflict types
     * (including {@code "device_removed"} and unknown values) are treated as device removal,
     * which maps to a {@link WhatsAppSessionException.LoggedOut}.
     *
     * @param type the conflict type attribute value, or {@code null} if absent
     */
    private void handleConflict(String type) {
        if ("replaced".equals(type)) {
            whatsapp.handleFailure(new WhatsAppSessionException.Conflict("Stream replaced by another active session"));
            return;
        }

        whatsapp.handleFailure(new WhatsAppSessionException.LoggedOut("Server removed or invalidated this device"));
    }

    /**
     * Handles a code-type stream error by dispatching the appropriate session exception.
     * <p>
     * Server error codes in the 500-599 range have specific handling:
     * <ul>
     *   <li>{@code 515}: server requests reconnection and re-login</li>
     *   <li>{@code 516}: server forces companion logout</li>
     *   <li>Other 5xx: server stream error, socket closed without retry</li>
     * </ul>
     * Codes outside the 500-599 range close the socket.
     *
     * @param code the error code from the {@code stream:error} stanza
     */
    private void handleCode(int code) {
        LOGGER.log(System.Logger.Level.WARNING, "Received stream:error code={0}", code);
        if (code >= 500 && code < 600) {
            if (code == STREAM_ERROR_RESTART_LOGIN) {
                whatsapp.handleFailure(new WhatsAppSessionException.Reconnect("Server requested reconnect"));
                return;
            }

            if (code == STREAM_ERROR_LOGOUT) {
                whatsapp.handleFailure(new WhatsAppSessionException.LoggedOut("Server requested logout"));
                return;
            }

            whatsapp.handleFailure(new WhatsAppSessionException.Closed("Server stream error " + code));
            return;
        }

        whatsapp.handleFailure(new WhatsAppSessionException.Closed("Server stream error code " + code));
    }
}
