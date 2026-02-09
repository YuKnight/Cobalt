package com.github.auties00.cobalt.message.receive.crypto;

/**
 * Outcome of the E2E decryption pipeline for an incoming message.
 *
 * <p>After iterating over all encrypted payloads, the decryption handler
 * produces one of these results.  The result determines what receipt is
 * sent back to the sender and whether the message is stored locally.
 *
 * @apiNote WAWebHandleMsgTypes.flow.E2EProcessResult: the exact set of
 * result values used in WA Web.
 */
public enum MessageDecryptionResult {
    /**
     * At least one encrypted payload was successfully decrypted and
     * the protobuf content was processed.
     */
    SUCCESS,

    /**
     * Decryption failed with a retryable Signal error — a retry receipt
     * should be sent so the sender re-encrypts and resends.
     */
    RETRY,

    /**
     * HSM template mismatch — the stanza indicated HSM but the protobuf
     * did not contain a highly structured message, or vice versa.
     * No receipt is sent.
     */
    HSM_MISMATCH,

    /**
     * The message payload was not available (fanout placeholder from a
     * companion device).  An ack is sent but no content is stored.
     */
    BACKFILL,

    /**
     * The decrypted protobuf could not be parsed or contained
     * unrecognised content.  A NACK with ParsingError is sent.
     */
    PARSE_ERROR,

    /**
     * The decrypted protobuf failed structural validation (e.g.
     * stanza type vs protobuf content mismatch, invalid DSM).
     * A NACK with InvalidProtobuf is sent, optionally with an
     * e2eFailureReason.
     */
    PARSE_VALIDATION_ERROR,

    /**
     * The Signal protocol reported a duplicate message counter (old
     * counter error).  The message was already decrypted previously.
     * Handled specially: if dedup-eligible, cached for receipt; otherwise
     * treated like a normal delivery.
     */
    SIGNAL_OLD_COUNTER_ERROR
}
