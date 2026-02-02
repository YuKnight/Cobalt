package com.github.auties00.cobalt.exception;

/**
 * Exception thrown when message decryption fails.
 * Contains the reason for the failure to include in retry receipts.
 */
public final class MessageDecryptionException extends MessageException {
    private final Reason reason;

    public MessageDecryptionException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public MessageDecryptionException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    /**
     * Returns the retry reason to send in the retry receipt.
     *
     * @return the retry reason
     */
    public Reason reason() {
        return reason;
    }

    /**
     * Reasons for sending a retry receipt when message decryption fails.
     * These are sent back to the sender to request message re-transmission.
     */
    public enum Reason {
        /**
         * No Signal session exists with the sender device.
         * Sender should re-send as PreKeySignalMessage (pkmsg).
         */
        SESSION_NOT_FOUND("session_not_found"),

        /**
         * The Signal message was malformed or corrupt.
         * Sender should re-send with a valid message.
         */
        INVALID_MESSAGE("invalid_message"),

        /**
         * Generic decryption failure.
         * Could be caused by out-of-sync counter or other issues.
         */
        DECRYPT_FAILED("decrypt_fail"),

        /**
         * No sender key exists for group message decryption.
         * Sender should re-distribute their sender key.
         */
        NO_SENDER_KEY("no_sender_key"),

        /**
         * The sender key was invalid or expired.
         * Sender should create and distribute a new sender key.
         */
        INVALID_SENDER_KEY("invalid_sender_key"),

        /**
         * ADV (Account Device Verification) signature validation failed.
         * Sender should re-send with valid device identity signature.
         */
        ADV_FAILURE("adv_failure"),

        /**
         * Identity key changed for the sender.
         * Session needs to be re-established.
         */
        UNTRUSTED_IDENTITY("untrusted_identity"),

        /**
         * Duplicate message counter detected.
         * This is usually a replay attack or sync issue.
         */
        DUPLICATE_MESSAGE("duplicate_message"),

        /**
         * Device sent message validation failed.
         * BCL hash or destination was invalid.
         */
        INVALID_DSM("invalid_dsm");

        private final String protocolValue;

        Reason(String protocolValue) {
            this.protocolValue = protocolValue;
        }

        /**
         * Returns the protocol value used in retry receipt stanzas.
         *
         * @return the retry reason string value
         */
        public String protocolValue() {
            return protocolValue;
        }

        /**
         * Parses a retry reason from its protocol value.
         *
         * @param value the protocol value string
         * @return the corresponding retry reason
         * @throws IllegalArgumentException if the value is unknown
         */
        public static Reason fromProtocolValue(String value) {
            for (var reason : values()) {
                if (reason.protocolValue.equals(value)) {
                    return reason;
                }
            }
            throw new IllegalArgumentException("Unknown retry reason: " + value);
        }

        /**
         * Returns whether this reason should trigger a retry receipt.
         * Some reasons (like DUPLICATE_MESSAGE) should not send retry receipts.
         *
         * @return true if a retry receipt should be sent
         */
        public boolean shouldSendRetryReceipt() {
            return switch (this) {
                case DUPLICATE_MESSAGE -> false; // Per design doc edge case 59: process normally
                case SESSION_NOT_FOUND, INVALID_MESSAGE, DECRYPT_FAILED,
                     NO_SENDER_KEY, INVALID_SENDER_KEY, ADV_FAILURE,
                     UNTRUSTED_IDENTITY, INVALID_DSM -> true;
            };
        }
    }
}
