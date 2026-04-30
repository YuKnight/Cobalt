package com.github.auties00.cobalt.message.send.ack;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;

import java.time.Instant;
import java.util.Objects;

/**
 * Parses the {@code <ack>} stanza returned by the server after each outgoing
 * message, extracting delivery metadata, the optional participant hash, and the
 * optional error code into an {@link AckResult}.
 *
 * @see AckResult
 * @see NackReason
 */
@WhatsAppWebModule(moduleName = "WAWebSendMsgCommonApi")
public final class AckParser {
    /**
     * Prevents instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private AckParser() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Parses the given server ack node into a structured result.
     *
     * @param ack the ack node returned by the server
     * @return the parsed result
     * @throws NullPointerException     if {@code ack} is {@code null}
     * @throws IllegalArgumentException if the node tag is not {@code "ack"}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static AckResult parse(Node ack) {
        Objects.requireNonNull(ack, "ack");
        if (!ack.hasDescription("ack")) {
            throw new IllegalArgumentException(
                    "Expected <ack> node, got <" + ack.description() + ">");
        }

        var timestampSeconds = ack.getAttributeAsLong("t", null);
        var timestamp = timestampSeconds != null
                ? Instant.ofEpochSecond(timestampSeconds)
                : null;
        var sync = ack.getAttributeAsString("sync", null);
        var phash = ack.getAttributeAsString("phash", null);
        var refreshLid = ack.getAttributeAsBool("refresh_lid", false);
        var addressingMode = ack.getAttributeAsString("addressing_mode", null);
        var count = ack.getAttributeAsInt("count", null);
        var error = ack.getAttributeAsInt("error", null);

        return new AckResult(
                timestamp,
                sync,
                phash,
                refreshLid,
                addressingMode,
                count,
                error
        );
    }
}
