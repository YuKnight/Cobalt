package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.node.Node;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Parser for message send acknowledgment responses.
 * <p>
 * Parses the ack stanza returned by the server after sending a message
 * to extract sync information and error codes.
 *
 * @apiNote WAWebSendMsgCommonApi.sendMsgAckSyncParser: parses ack stanzas
 * to extract t, sync, phash, refreshLid, addressingMode, count, and error attributes.
 */
public final class AckParser {
    private AckParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Parses an ack node into a structured result.
     * <p>
     * Ack structure:
     * <pre>{@code
     * <ack id="{msgId}" t="{timestamp}" class="message" from="{chatJid}"
     *      sync="{sync}" phash="{phash}" addressing_mode="{mode}"
     *      count="{count}" error="{error}" refresh_lid="{true|false}"/>
     * }</pre>
     *
     * @param ackNode the ack node to parse
     * @return the parsed ack result, or empty if parsing fails
     *
     * @apiNote WAWebSendMsgCommonApi.sendMsgAckSyncParser.parse
     */
    public static Optional<MessageAck> parse(Node ackNode) {
        Objects.requireNonNull(ackNode, "ackNode cannot be null");

        // WAWebSendMsgCommonApi.sendMsgAckSyncParser: e.assertTag("ack")
        if (!ackNode.description().equals("ack")) {
            return Optional.empty();
        }

        // WAWebSendMsgCommonApi.sendMsgAckSyncParser: extract attributes
        var timestamp = ackNode.getAttributeAsLong("t", 0);

        // WAWebSendMsgCommonApi.sendMsgAckSyncParser: maybeAttrString("sync")
        var sync = ackNode.getAttributeAsString("sync", null);

        // WAWebSendMsgCommonApi.sendMsgAckSyncParser: maybeAttrString("phash")
        // If present, indicates a phash mismatch requiring resend
        var phash = ackNode.getAttributeAsString("phash", null);

        // WAWebSendMsgCommonApi.sendMsgAckSyncParser: hasAttr("refresh_lid") ? attrString("refresh_lid") === "true" : false
        var refreshLid = ackNode.getAttributeAsBool("refresh_lid", false);

        // WAWebSendMsgCommonApi.sendMsgAckSyncParser: maybeAttrString("addressing_mode")
        var addressingMode = ackNode.getAttributeAsString("addressing_mode")
                .orElse(null);

        // WAWebSendMsgCommonApi.sendMsgAckSyncParser: maybeAttrInt("count")
        var count = ackNode.getAttributeAsInt("count");

        // WAWebSendMsgCommonApi.sendMsgAckSyncParser: maybeAttrInt("error")
        var error = ackNode.getAttributeAsInt("error");

        return Optional.of(new MessageAck(
                timestamp,
                sync,
                phash,
                refreshLid,
                addressingMode,
                count,
                error
        ));
    }

    /**
     * Parsed message send acknowledgment.
     *
     * @param timestamp      the server timestamp
     * @param sync           the sync attribute, or null
     * @param phash          the participant hash from server if mismatched, or null if matched
     * @param refreshLid     whether to refresh LID mapping
     * @param addressingMode the addressing mode from server (pn or lid)
     * @param count          the message count, if present
     * @param error          the error code, if present
     *
     * @apiNote WAWebSendMsgCommonApi.sendMsgAckSyncParser: defines the ack structure
     */
    public record MessageAck(
            long timestamp,
            String sync,
            String phash,
            boolean refreshLid,
            String addressingMode,
            OptionalInt count,
            OptionalInt error
    ) {
        /**
         * Returns whether the ack indicates a phash mismatch requiring resend.
         * <p>
         * When the server returns a phash, it means the sender's participant list
         * doesn't match the server's, and the message should be resent after
         * syncing the device list.
         *
         * @return {@code true} if phash is present (mismatch detected)
         *
         * @apiNote WAWebSendGroupSkmsgJob: if phash != localPhash, calls resendPersistedGroupMsgWrapper
         */
        public boolean hasPhashMismatch() {
            return phash != null && !phash.isEmpty();
        }

        /**
         * Returns whether the ack contains an error code.
         *
         * @return {@code true} if an error code is present
         */
        public boolean hasError() {
            return error.isPresent();
        }

        /**
         * Returns whether this is a stale group addressing mode error.
         * <p>
         * Error code 421 indicates the group's addressing mode has changed
         * and the sender needs to query the group metadata again.
         *
         * @return {@code true} if error is 421 (StaleGroupAddressingMode)
         *
         * @apiNote WAWebCreateNackFromStanza.NackReason.StaleGroupAddressingMode = 421
         */
        public boolean isStaleGroupAddressingModeError() {
            return error.isPresent() && error.getAsInt() == 421;
        }

        /**
         * Returns whether this ack indicates success.
         *
         * @return {@code true} if no error is present
         */
        public boolean isSuccess() {
            return !hasError();
        }
    }
}
