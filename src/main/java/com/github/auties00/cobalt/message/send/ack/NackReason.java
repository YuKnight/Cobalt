package com.github.auties00.cobalt.message.send.ack;

/**
 * Well-known server nack (negative acknowledgement) error codes.
 *
 * <p>These codes may appear in the {@code error} attribute of the
 * {@code <ack>} node returned by the server after a message stanza
 * is sent.
 *
 * @apiNote WAWebCreateNackFromStanza.NackReason: defines the known
 * error codes the server may include in the ack's {@code error} attribute.
 * @see AckResult#error()
 */
public final class NackReason {
    /** The group's addressing mode has changed since the client's last sync. */
    public static final int STALE_GROUP_ADDRESSING_MODE = 421;

    /** The chat has reached its new-message cap. */
    public static final int NEW_CHAT_MESSAGES_CAPPED = 475;

    /** The stanza could not be parsed by the server. */
    public static final int PARSING_ERROR = 487;

    /** The stanza type is not recognised. */
    public static final int UNRECOGNIZED_STANZA = 488;

    /** The stanza class is not recognised. */
    public static final int UNRECOGNIZED_STANZA_CLASS = 489;

    /** The stanza type attribute is not recognised. */
    public static final int UNRECOGNIZED_STANZA_TYPE = 490;

    /** The protobuf payload is invalid. */
    public static final int INVALID_PROTOBUF = 491;

    /** The hosted companion stanza is invalid. */
    public static final int INVALID_HOSTED_COMPANION_STANZA = 493;

    /** The message secret is missing. */
    public static final int MISSING_MESSAGE_SECRET = 495;

    /** The Signal counter is older than expected. */
    public static final int SIGNAL_ERROR_OLD_COUNTER = 496;

    /** The message was deleted on the peer's device. */
    public static final int MESSAGE_DELETED_ON_PEER = 499;

    /** An unhandled server-side error. */
    public static final int UNHANDLED_ERROR = 500;

    /** Admin revoke is not supported for this message. */
    public static final int UNSUPPORTED_ADMIN_REVOKE = 550;

    /** LID groups are not supported by this client. */
    public static final int UNSUPPORTED_LID_GROUP = 551;

    /** A database operation failed on the server. */
    public static final int DB_OPERATION_FAILED = 552;

    private NackReason() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
