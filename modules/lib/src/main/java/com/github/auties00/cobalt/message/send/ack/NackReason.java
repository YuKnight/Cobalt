package com.github.auties00.cobalt.message.send.ack;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Holds the well-known server nack (negative-acknowledgement) error codes.
 *
 * <p>The same codes appear on both sides of the protocol: the server writes them
 * into the {@code error} attribute of the {@code <ack>} node when rejecting a
 * client stanza ({@link AckResult#error()}), and the client echoes them back when
 * constructing its own nack stanza for a stanza it could not process.
 *
 * @see AckResult#error()
 */
@WhatsAppWebModule(moduleName = "WAWebCreateNackFromStanza")
public final class NackReason {
    /**
     * Indicates the group's addressing mode has changed since the client's last sync.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int STALE_GROUP_ADDRESSING_MODE = 421;

    /**
     * Indicates the chat has reached its new-message cap.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int NEW_CHAT_MESSAGES_CAPPED = 475;

    /**
     * Indicates the server could not parse the stanza.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int PARSING_ERROR = 487;

    /**
     * Indicates the stanza tag was not recognised.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int UNRECOGNIZED_STANZA = 488;

    /**
     * Indicates the stanza class was not recognised.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int UNRECOGNIZED_STANZA_CLASS = 489;

    /**
     * Indicates the stanza type attribute was not recognised.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int UNRECOGNIZED_STANZA_TYPE = 490;

    /**
     * Indicates the protobuf payload was invalid.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int INVALID_PROTOBUF = 491;

    /**
     * Indicates the hosted-companion stanza was invalid.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int INVALID_HOSTED_COMPANION_STANZA = 493;

    /**
     * Indicates the message secret was missing.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int MISSING_MESSAGE_SECRET = 495;

    /**
     * Indicates the Signal counter on the inbound stanza was older than expected.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int SIGNAL_ERROR_OLD_COUNTER = 496;

    /**
     * Indicates the message had been deleted on the peer's device.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int MESSAGE_DELETED_ON_PEER = 499;

    /**
     * Indicates an otherwise unhandled server-side error.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int UNHANDLED_ERROR = 500;

    /**
     * Indicates that admin revoke is not supported for this message.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int UNSUPPORTED_ADMIN_REVOKE = 550;

    /**
     * Indicates LID groups are not supported by this client.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int UNSUPPORTED_LID_GROUP = 551;

    /**
     * Indicates a database operation failed on the server.
     */
    @WhatsAppWebExport(moduleName = "WAWebCreateNackFromStanza", exports = "NackReason",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int DB_OPERATION_FAILED = 552;

    /**
     * Prevents instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private NackReason() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
