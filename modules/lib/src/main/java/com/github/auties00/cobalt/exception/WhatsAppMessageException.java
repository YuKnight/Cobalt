package com.github.auties00.cobalt.exception;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Base exception for message processing errors in the WhatsApp protocol.
 * <p>
 * This sealed class hierarchy represents all message-related failures that can occur during
 * message sending, receiving, and processing operations. Message exceptions are categorized
 * into decryption failures ({@link Receive}) which represent cryptographic and protocol-level
 * errors that prevent message content from being accessed.
 * <p>
 * Message exceptions are generally non-fatal errors, meaning the client connection should
 * remain active and continue processing other messages. The specific error information is
 * used to generate appropriate retry receipts or error acknowledgments back to the sender.
 *
 * <h2>Exception Hierarchy</h2>
 * <ul>
 *   <li>{@link Receive} - Abstract base for all decryption failures
 *     <ul>
 *       <li>{@link Receive.NoSession} - Signal session does not exist</li>
 *       <li>{@link Receive.InvalidKey} - Signal key validation failed</li>
 *       <li>{@link Receive.InvalidKeyId} - Signal key identifier is invalid</li>
 *       <li>{@link Receive.InvalidOneTimeKey} - One-time prekey validation failed</li>
 *       <li>{@link Receive.InvalidSignedPreKey} - Signed prekey validation failed</li>
 *       <li>{@link Receive.InvalidMessage} - Signal message structure is malformed</li>
 *       <li>{@link Receive.InvalidSignature} - Signal signature verification failed</li>
 *       <li>{@link Receive.DuplicateMessage} - Message counter already processed</li>
 *       <li>{@link Receive.FutureMessage} - Message counter too far ahead</li>
 *       <li>{@link Receive.BadMac} - Message authentication code failed</li>
 *       <li>{@link Receive.NoSenderKey} - Group sender key not available</li>
 *       <li>{@link Receive.InvalidSenderKey} - Group sender key validation failed</li>
 *       <li>{@link Receive.UnknownDevice} - Message from unrecognized companion device</li>
 *       <li>{@link Receive.InvalidDeviceSentMessage} - Device sent message validation failed</li>
 *       <li>{@link Receive.AdvFailure} - Account device verification failed</li>
 *       <li>{@link Receive.InvalidProtobuf} - Protobuf deserialization or validation failed</li>
 *       <li>{@link Receive.BroadcastEphemeralSettings} - Broadcast ephemeral settings error</li>
 *       <li>{@link Receive.HsmMismatch} - HSM template mismatch</li>
 *       <li>{@link Receive.Unknown} - Unclassified decryption failure</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @see Receive
 */
public sealed class WhatsAppMessageException extends WhatsAppException
        permits WhatsAppMessageException.Receive, WhatsAppMessageException.Send {

    /**
     * Constructs a new message exception with the specified detail message.
     *
     * @param message the detail message describing the error
     */
    public WhatsAppMessageException(String message) {
        super(message);
    }

    /**
     * Constructs a new message exception with the specified detail message and cause.
     *
     * @param message the detail message describing the error
     * @param cause   the underlying cause of this exception
     */
    public WhatsAppMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new message exception wrapping the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public WhatsAppMessageException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns whether this exception represents a fatal error.
     * <p>
     * Message exceptions are non-fatal by default, as individual message failures
     * should not terminate the entire client session.
     *
     * @return {@code false} for all message exceptions
     */
    @Override
    public boolean isFatal() {
        return false;
    }

    /**
     * Abstract base exception for all message decryption failures.
     * <p>
     * This sealed class hierarchy represents the complete taxonomy of errors that can occur
     * when attempting to decrypt an incoming WhatsApp message. Each subclass corresponds to
     * a specific failure mode in the Signal protocol, sender key distribution, device
     * verification, or message validation pipeline.
     *
     * <h2>Retry Receipt System</h2>
     * When decryption fails for retryable errors, the client sends a retry receipt to the
     * sender requesting message retransmission. The retry receipt includes:
     * <ul>
     *   <li><b>Retry reason code:</b> An integer indicating the failure type (see {@link RetryReason})</li>
     *   <li><b>Retry count:</b> Number of retry attempts made</li>
     *   <li><b>Registration ID:</b> Client's Signal registration identifier</li>
     *   <li><b>Key bundle:</b> Optional prekey bundle for session re-establishment</li>
     * </ul>
     *
     * <h2>Error Categories</h2>
     * Decryption errors fall into several categories:
     * <ul>
     *   <li><b>Session errors:</b> {@link NoSession} - No Signal session exists with the sender</li>
     *   <li><b>Key errors:</b> {@link InvalidKey}, {@link InvalidKeyId}, {@link InvalidOneTimeKey},
     *       {@link InvalidSignedPreKey} - Cryptographic key validation failures</li>
     *   <li><b>Message errors:</b> {@link InvalidMessage}, {@link InvalidSignature},
     *       {@link DuplicateMessage}, {@link FutureMessage} - Message structure or ordering issues</li>
     *   <li><b>MAC errors:</b> {@link BadMac} - Message integrity check failed</li>
     *   <li><b>Group errors:</b> {@link NoSenderKey}, {@link InvalidSenderKey} - Sender key distribution issues</li>
     *   <li><b>Device errors:</b> {@link UnknownDevice}, {@link InvalidDeviceSentMessage} - Multi-device validation failures</li>
     *   <li><b>ADV errors:</b> {@link AdvFailure} - Account device verification failed</li>
     *   <li><b>Validation errors:</b> {@link InvalidProtobuf} - Protobuf deserialization failures</li>
     *   <li><b>Protocol errors:</b> {@link BroadcastEphemeralSettings}, {@link HsmMismatch} - Protocol-specific failures</li>
     *   <li><b>Unknown:</b> {@link Unknown} - Unclassified failures</li>
     * </ul>
     *
     * @see RetryReason
     */
    public sealed static abstract class Receive extends WhatsAppMessageException
            permits Receive.NoSession, Receive.InvalidKey, Receive.InvalidKeyId,
                    Receive.InvalidOneTimeKey, Receive.InvalidSignedPreKey,
                    Receive.InvalidMessage, Receive.InvalidSignature,
                    Receive.DuplicateMessage, Receive.FutureMessage,
                    Receive.BadMac, Receive.NoSenderKey, Receive.InvalidSenderKey,
                    Receive.UnknownDevice, Receive.InvalidDeviceSentMessage,
                    Receive.AdvFailure, Receive.InvalidProtobuf,
                    Receive.BroadcastEphemeralSettings, Receive.HsmMismatch,
                    Receive.Unknown {

        /**
         * Constructs a new decryption exception with the specified detail message.
         *
         * @param message the detail message describing the decryption failure
         */
        protected Receive(String message) {
            super(message);
        }

        /**
         * Constructs a new decryption exception with the specified detail message and cause.
         *
         * @param message the detail message describing the decryption failure
         * @param cause   the underlying cause of this exception
         */
        protected Receive(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Returns the retry reason code for this decryption failure.
         * <p>
         * The retry reason is sent in retry receipt stanzas to inform the sender
         * what type of failure occurred. The sender uses this information to determine
         * how to retransmit the message (e.g., re-establish session, redistribute sender key).
         *
         * @return the retry reason for this decryption failure
         */
        public abstract RetryReason retryReason();

        /**
         * Returns whether a retry receipt should be sent for this failure.
         * <p>
         * Most decryption failures trigger retry receipts, but some (like duplicate messages)
         * are silently ignored since they don't represent actual failures.
         *
         * @return {@code true} if a retry receipt should be sent
         */
        public boolean shouldSendRetryReceipt() {
            return true;
        }

        /**
         * Returns the optional error code for NACK receipts.
         * <p>
         * Some decryption failures (particularly validation errors) include an error code
         * that should be sent in a NACK receipt instead of or in addition to a retry receipt.
         *
         * @return the error code for NACK receipts, or empty if not applicable
         */
        public Optional<String> errorCode() {
            return Optional.empty();
        }

        /**
         * Retry reason codes sent in retry receipt stanzas when message decryption fails.
         * <p>
         * These integer codes are defined by the WhatsApp protocol and indicate to the sender
         * what type of failure occurred. The sender uses this information to determine the
         * appropriate recovery action.
         *
         * <h2>Protocol Values</h2>
         * <table border="1">
         *   <caption>Retry Reason Protocol Values</caption>
         *   <tr><th>Code</th><th>Name</th><th>Recovery Action</th></tr>
         *   <tr><td>0</td><td>UNKNOWN_ERROR</td><td>Generic retry</td></tr>
         *   <tr><td>1</td><td>SIGNAL_ERROR_NO_SESSION</td><td>Send PreKeySignalMessage</td></tr>
         *   <tr><td>2</td><td>SIGNAL_ERROR_INVALID_KEY</td><td>Refresh key bundle</td></tr>
         *   <tr><td>3</td><td>SIGNAL_ERROR_INVALID_KEY_ID</td><td>Refresh key bundle</td></tr>
         *   <tr><td>4</td><td>SIGNAL_ERROR_INVALID_MESSAGE</td><td>Re-encrypt message</td></tr>
         *   <tr><td>5</td><td>SIGNAL_ERROR_INVALID_SIGNATURE</td><td>Verify identity</td></tr>
         *   <tr><td>6</td><td>SIGNAL_ERROR_FUTURE_MESSAGE</td><td>Synchronize counters</td></tr>
         *   <tr><td>7</td><td>SIGNAL_ERROR_BAD_MAC</td><td>Re-encrypt message</td></tr>
         *   <tr><td>8</td><td>SIGNAL_ERROR_INVALID_SESSION</td><td>Re-establish session</td></tr>
         *   <tr><td>9</td><td>SIGNAL_ERROR_INVALID_MSG_KEY</td><td>Re-encrypt message</td></tr>
         *   <tr><td>10</td><td>BAD_BROADCAST_EPH_SETTINGS</td><td>Resend broadcast settings</td></tr>
         *   <tr><td>11</td><td>UNKNOWN_COMPANION_NO_PREKEY</td><td>Register device</td></tr>
         *   <tr><td>12</td><td>ADV_FAILURE</td><td>Re-verify device</td></tr>
         *   <tr><td>13</td><td>STATUS_REVOKE_DELAY</td><td>Wait and retry</td></tr>
         * </table>
         */
        public enum RetryReason {
            /**
             * Unknown or unclassified error.
             * <p>
             * Used when the specific failure reason cannot be determined.
             * The sender should perform a generic retry attempt.
             */
            UNKNOWN_ERROR(0),

            /**
             * No Signal session exists with the sender device.
             * <p>
             * The recipient has no cryptographic session established with the sender.
             * This typically occurs when:
             * <ul>
             *   <li>First message from a new contact</li>
             *   <li>Session was deleted due to storage cleanup</li>
             *   <li>Sender device was recently registered</li>
             * </ul>
             * <p>
             * Recovery: Sender should transmit a PreKeySignalMessage to establish a new session.
             */
            SIGNAL_ERROR_NO_SESSION(1),

            /**
             * Invalid cryptographic key in the message.
             * <p>
             * The public key included in the message failed validation.
             * This can occur due to:
             * <ul>
             *   <li>Corrupted key data</li>
             *   <li>Invalid elliptic curve point</li>
             *   <li>Key format mismatch</li>
             * </ul>
             * <p>
             * Recovery: Sender should refresh their key bundle and retry.
             */
            SIGNAL_ERROR_INVALID_KEY(2),

            /**
             * Invalid key identifier in the message.
             * <p>
             * The key ID referenced does not match any known key.
             * This can occur when keys are rotated on one side but not synchronized.
             * <p>
             * Recovery: Sender should refresh their key bundle and retry.
             */
            SIGNAL_ERROR_INVALID_KEY_ID(3),

            /**
             * Invalid Signal message structure.
             * <p>
             * The message format does not conform to the Signal protocol specification.
             * This indicates corruption, truncation, or protocol version mismatch.
             * <p>
             * Recovery: Sender should re-encrypt and resend the message.
             */
            SIGNAL_ERROR_INVALID_MESSAGE(4),

            /**
             * Invalid cryptographic signature.
             * <p>
             * The signature on the message or key bundle failed verification.
             * This could indicate:
             * <ul>
             *   <li>Message tampering</li>
             *   <li>Identity key mismatch</li>
             *   <li>Corrupted signature data</li>
             * </ul>
             * <p>
             * Recovery: Sender should verify their identity key and retry.
             */
            SIGNAL_ERROR_INVALID_SIGNATURE(5),

            /**
             * Message counter is too far in the future.
             * <p>
             * The message's sequence number exceeds the acceptable lookahead window.
             * This can happen when:
             * <ul>
             *   <li>Messages were lost or reordered significantly</li>
             *   <li>Counter synchronization was lost</li>
             *   <li>Potential replay attack with manipulated counters</li>
             * </ul>
             * <p>
             * Recovery: Both parties should synchronize their message counters.
             */
            SIGNAL_ERROR_FUTURE_MESSAGE(6),

            /**
             * Message authentication code (MAC) verification failed.
             * <p>
             * The HMAC computed over the message did not match the expected value.
             * This is a strong indicator of:
             * <ul>
             *   <li>Message corruption during transmission</li>
             *   <li>Incorrect encryption key derivation</li>
             *   <li>Potential tampering or man-in-the-middle attack</li>
             * </ul>
             * <p>
             * Recovery: Sender should re-encrypt the message with fresh key material.
             */
            SIGNAL_ERROR_BAD_MAC(7),

            /**
             * Signal session state is invalid.
             * <p>
             * The session exists but is in an inconsistent or corrupted state
             * that prevents decryption.
             * <p>
             * Recovery: Both parties should re-establish the session from scratch.
             */
            SIGNAL_ERROR_INVALID_SESSION(8),

            /**
             * Invalid message key derivation.
             * <p>
             * The symmetric key derived for this specific message is invalid
             * or cannot be computed from the current session state.
             * <p>
             * Recovery: Sender should re-encrypt the message.
             */
            SIGNAL_ERROR_INVALID_MSG_KEY(9),

            /**
             * Invalid broadcast ephemeral settings.
             * <p>
             * The ephemeral messaging settings for a broadcast list could not
             * be decrypted or validated.
             * <p>
             * Recovery: Sender should resend the broadcast settings stanza.
             */
            BAD_BROADCAST_EPH_SETTINGS(10),

            /**
             * Unknown companion device without prekey.
             * <p>
             * A message was received from an unrecognized companion device
             * and no prekey bundle is available to establish a session.
             * <p>
             * Recovery: The unknown device should register its prekeys with the server.
             */
            UNKNOWN_COMPANION_NO_PREKEY(11),

            /**
             * Account Device Verification (ADV) failure.
             * <p>
             * The device's identity could not be verified using ADV signatures.
             * This indicates the device may not be legitimately associated with
             * the claimed account.
             * <p>
             * Recovery: Sender's device should re-verify its ADV chain.
             */
            ADV_FAILURE(12),

            /**
             * Status message revoke delay.
             * <p>
             * A status-related operation could not be processed due to
             * timing constraints on revocation.
             * <p>
             * Recovery: Wait for the delay period and retry.
             */
            STATUS_REVOKE_DELAY(13);

            /**
             * The protocol integer value sent in retry receipt stanzas.
             */
            private final int protocolValue;

            /**
             * Constructs a retry reason with the specified protocol value.
             *
             * @param protocolValue the integer value used in the protocol
             */
            RetryReason(int protocolValue) {
                this.protocolValue = protocolValue;
            }

            /**
             * Returns the protocol integer value for this retry reason.
             * <p>
             * This value is encoded in the {@code error} attribute of retry receipt stanzas.
             *
             * @return the protocol integer value
             */
            public int protocolValue() {
                return protocolValue;
            }

            /**
             * Returns whether a retry receipt should be sent for this reason.
             * <p>
             * Most retry reasons trigger retry receipts, but some (like duplicate messages
             * indicated by future message counter issues) may be silently ignored.
             *
             * @return {@code true} if a retry receipt should be sent
             */
            public boolean shouldSendRetryReceipt() {
                // All reasons should send retry receipts except for specific cases
                // that are handled by the Decrypt subclass overrides
                return true;
            }

            /**
             * Parses a retry reason from its protocol integer value.
             *
             * @param value the protocol integer value
             * @return the corresponding retry reason
             * @throws IllegalArgumentException if the value does not match any known reason
             */
            public static RetryReason fromProtocolValue(int value) {
                for (var reason : values()) {
                    if (reason.protocolValue == value) {
                        return reason;
                    }
                }
                throw new IllegalArgumentException("Unknown retry reason code: " + value);
            }
        }

        /**
         * Exception thrown when no Signal session exists with the sender device.
         * <p>
         * In the Signal protocol, a session must be established before encrypted messages
         * can be exchanged. This exception occurs when:
         * <ul>
         *   <li>Receiving a {@code msg} (Signal message) type when no session exists</li>
         *   <li>Receiving a {@code skmsg} (sender key message) when no sender key session exists</li>
         *   <li>The session was deleted from local storage</li>
         *   <li>The sender is a newly registered device</li>
         * </ul>
         * <p>
         * When this exception is thrown, a retry receipt is sent to the sender with reason
         * {@link RetryReason#SIGNAL_ERROR_NO_SESSION}, prompting them to resend the message
         * as a {@code pkmsg} (PreKeySignalMessage) which includes the key material needed
         * to establish a new session.
         *
         * <h2>Signal Error Messages</h2>
         * This exception maps to the following Signal library error strings:
         * <ul>
         *   <li>{@code errSignalNoSession} - No 1:1 session with sender</li>
         *   <li>{@code errLoadSenderKeySession} - No group sender key session</li>
         * </ul>
         */
        public static final class NoSession extends Receive {
            /**
             * Indicates whether this is a group (sender key) session error.
             */
            private final boolean isGroupSession;

            /**
             * Constructs a new no-session exception for a 1:1 session.
             */
            public NoSession() {
                super("No Signal session exists with sender device");
                this.isGroupSession = false;
            }

            /**
             * Constructs a new no-session exception with the specified message.
             *
             * @param message        the detail message
             * @param isGroupSession {@code true} if this is a group sender key session error
             */
            public NoSession(String message, boolean isGroupSession) {
                super(message);
                this.isGroupSession = isGroupSession;
            }

            /**
             * Constructs a new no-session exception with a cause.
             *
             * @param message        the detail message
             * @param isGroupSession {@code true} if this is a group sender key session error
             * @param cause          the underlying cause
             */
            public NoSession(String message, boolean isGroupSession, Throwable cause) {
                super(message, cause);
                this.isGroupSession = isGroupSession;
            }

            /**
             * Returns whether this is a group (sender key) session error.
             *
             * @return {@code true} if this is a group session error, {@code false} for 1:1 sessions
             */
            public boolean isGroupSession() {
                return isGroupSession;
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_NO_SESSION;
            }
        }

        /**
         * Exception thrown when a cryptographic key in the message is invalid.
         * <p>
         * This exception occurs when public key validation fails during message decryption.
         * The Signal protocol uses Curve25519 elliptic curve keys, and this error indicates
         * the key data does not represent a valid point on the curve.
         *
         * <h2>Possible Causes</h2>
         * <ul>
         *   <li>Key data was corrupted during transmission</li>
         *   <li>Key encoding/decoding error</li>
         *   <li>Intentionally malformed key (potential attack)</li>
         *   <li>Protocol version mismatch</li>
         * </ul>
         *
         * <h2>Signal Error Message</h2>
         * This exception maps to {@code errSignalInvalidKey}.
         */
        public static final class InvalidKey extends Receive {
            /**
             * Constructs a new invalid key exception.
             */
            public InvalidKey() {
                super("Invalid cryptographic key in Signal message");
            }

            /**
             * Constructs a new invalid key exception with the specified message.
             *
             * @param message the detail message
             */
            public InvalidKey(String message) {
                super(message);
            }

            /**
             * Constructs a new invalid key exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public InvalidKey(String message, Throwable cause) {
                super(message, cause);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_INVALID_KEY;
            }
        }

        /**
         * Exception thrown when a key identifier in the message does not match any known key.
         * <p>
         * Signal protocol messages reference keys by numeric identifiers. This exception occurs
         * when the referenced key ID is not found in the local key store, typically due to:
         * <ul>
         *   <li>Key rotation where old keys were deleted</li>
         *   <li>Key ID collision or wrap-around</li>
         *   <li>Corrupted key ID value</li>
         *   <li>Message referencing a key that was never stored</li>
         * </ul>
         *
         * <h2>Signal Error Message</h2>
         * This exception maps to {@code errSignalInvalidKeyId}.
         */
        public static final class InvalidKeyId extends Receive {
            /**
             * The invalid key identifier, if known.
             */
            private final Integer keyId;

            /**
             * Constructs a new invalid key ID exception.
             */
            public InvalidKeyId() {
                super("Invalid key identifier in Signal message");
                this.keyId = null;
            }

            /**
             * Constructs a new invalid key ID exception with the problematic key ID.
             *
             * @param keyId the invalid key identifier
             */
            public InvalidKeyId(int keyId) {
                super("Invalid key identifier in Signal message: " + keyId);
                this.keyId = keyId;
            }

            /**
             * Constructs a new invalid key ID exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public InvalidKeyId(String message, Throwable cause) {
                super(message, cause);
                this.keyId = null;
            }

            /**
             * Returns the invalid key identifier, if known.
             *
             * @return an {@link Optional} containing the key ID, or empty if not available
             */
            public Optional<Integer> keyId() {
                return Optional.ofNullable(keyId);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_INVALID_KEY_ID;
            }
        }

        /**
         * Exception thrown when a one-time prekey validation fails.
         * <p>
         * One-time prekeys are ephemeral Curve25519 keys used for initial session establishment.
         * Each prekey can only be used once to establish a session. This exception occurs when:
         * <ul>
         *   <li>The prekey was already consumed by another session</li>
         *   <li>The prekey ID does not exist in the local store</li>
         *   <li>The prekey data is corrupted or invalid</li>
         * </ul>
         *
         * <h2>Signal Error Message</h2>
         * This exception maps to {@code errSignalInvalidOneTimeKey}.
         */
        public static final class InvalidOneTimeKey extends Receive {
            /**
             * The invalid prekey identifier, if known.
             */
            private final Integer prekeyId;

            /**
             * Constructs a new invalid one-time key exception.
             */
            public InvalidOneTimeKey() {
                super("Invalid one-time prekey in Signal message");
                this.prekeyId = null;
            }

            /**
             * Constructs a new invalid one-time key exception with the problematic key ID.
             *
             * @param prekeyId the invalid prekey identifier
             */
            public InvalidOneTimeKey(int prekeyId) {
                super("Invalid one-time prekey in Signal message: " + prekeyId);
                this.prekeyId = prekeyId;
            }

            /**
             * Constructs a new invalid one-time key exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public InvalidOneTimeKey(String message, Throwable cause) {
                super(message, cause);
                this.prekeyId = null;
            }

            /**
             * Returns the invalid prekey identifier, if known.
             *
             * @return an {@link Optional} containing the prekey ID, or empty if not available
             */
            public Optional<Integer> prekeyId() {
                return Optional.ofNullable(prekeyId);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_INVALID_KEY;
            }
        }

        /**
         * Exception thrown when signed prekey validation fails.
         * <p>
         * Signed prekeys are medium-term keys signed by the identity key. They are used
         * in PreKeySignalMessage to establish sessions. This exception occurs when:
         * <ul>
         *   <li>The signed prekey signature is invalid</li>
         *   <li>The signed prekey has expired</li>
         *   <li>The signed prekey ID does not exist</li>
         *   <li>The key data is corrupted</li>
         * </ul>
         *
         * <h2>Signal Error Message</h2>
         * This exception maps to {@code errSignalInvalidSignedPreKey}.
         */
        public static final class InvalidSignedPreKey extends Receive {
            /**
             * The invalid signed prekey identifier, if known.
             */
            private final Integer signedPrekeyId;

            /**
             * Constructs a new invalid signed prekey exception.
             */
            public InvalidSignedPreKey() {
                super("Invalid signed prekey in Signal message");
                this.signedPrekeyId = null;
            }

            /**
             * Constructs a new invalid signed prekey exception with the problematic key ID.
             *
             * @param signedPrekeyId the invalid signed prekey identifier
             */
            public InvalidSignedPreKey(int signedPrekeyId) {
                super("Invalid signed prekey in Signal message: " + signedPrekeyId);
                this.signedPrekeyId = signedPrekeyId;
            }

            /**
             * Constructs a new invalid signed prekey exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public InvalidSignedPreKey(String message, Throwable cause) {
                super(message, cause);
                this.signedPrekeyId = null;
            }

            /**
             * Returns the invalid signed prekey identifier, if known.
             *
             * @return an {@link Optional} containing the signed prekey ID, or empty if not available
             */
            public Optional<Integer> signedPrekeyId() {
                return Optional.ofNullable(signedPrekeyId);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_INVALID_KEY;
            }
        }

        /**
         * Exception thrown when the Signal message structure is malformed.
         * <p>
         * Signal messages have a specific binary format that must be correctly parsed.
         * This exception occurs when:
         * <ul>
         *   <li>The message is truncated or incomplete</li>
         *   <li>The message version is unsupported</li>
         *   <li>Required fields are missing</li>
         *   <li>The ciphertext length is incorrect</li>
         *   <li>The message type indicator is invalid</li>
         * </ul>
         *
         * <h2>Signal Error Message</h2>
         * This exception maps to {@code errSignalInvalidMsg}.
         */
        public static final class InvalidMessage extends Receive {
            /**
             * Constructs a new invalid message exception.
             */
            public InvalidMessage() {
                super("Invalid Signal message structure");
            }

            /**
             * Constructs a new invalid message exception with the specified message.
             *
             * @param message the detail message
             */
            public InvalidMessage(String message) {
                super(message);
            }

            /**
             * Constructs a new invalid message exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public InvalidMessage(String message, Throwable cause) {
                super(message, cause);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_INVALID_MESSAGE;
            }
        }

        /**
         * Exception thrown when cryptographic signature verification fails.
         * <p>
         * Signal uses Ed25519 signatures to authenticate messages and key bundles.
         * This exception occurs when:
         * <ul>
         *   <li>The signature does not match the message content</li>
         *   <li>The signing key does not match the expected identity</li>
         *   <li>The signature data is corrupted or truncated</li>
         * </ul>
         * <p>
         * Signature failures are a security concern and may indicate tampering.
         *
         * <h2>Signal Error Message</h2>
         * This exception maps to {@code errSignalInvalidSignature}.
         */
        public static final class InvalidSignature extends Receive {
            /**
             * Constructs a new invalid signature exception.
             */
            public InvalidSignature() {
                super("Invalid cryptographic signature in Signal message");
            }

            /**
             * Constructs a new invalid signature exception with the specified message.
             *
             * @param message the detail message
             */
            public InvalidSignature(String message) {
                super(message);
            }

            /**
             * Constructs a new invalid signature exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public InvalidSignature(String message, Throwable cause) {
                super(message, cause);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_INVALID_SIGNATURE;
            }
        }

        /**
         * Exception thrown when a duplicate message is detected.
         * <p>
         * Signal tracks message counters to detect and prevent replay attacks.
         * This exception occurs when a message is received with a counter value
         * that has already been processed.
         * <p>
         * Duplicate messages are silently dropped and do NOT trigger retry receipts,
         * as they represent either:
         * <ul>
         *   <li>Network-level retransmission (normal operation)</li>
         *   <li>Sync messages from other devices</li>
         *   <li>Potential replay attacks</li>
         * </ul>
         *
         * <h2>Signal Error Message</h2>
         * This exception maps to {@code errDuplicateMsg}.
         */
        public static final class DuplicateMessage extends Receive {
            /**
             * The duplicate message counter value, if known.
             */
            private final Long counter;

            /**
             * Constructs a new duplicate message exception.
             */
            public DuplicateMessage() {
                super("Duplicate message counter detected");
                this.counter = null;
            }

            /**
             * Constructs a new duplicate message exception with the counter value.
             *
             * @param counter the duplicate message counter
             */
            public DuplicateMessage(long counter) {
                super("Duplicate message counter detected: " + counter);
                this.counter = counter;
            }

            /**
             * Constructs a new duplicate message exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public DuplicateMessage(String message, Throwable cause) {
                super(message, cause);
                this.counter = null;
            }

            /**
             * Returns the duplicate message counter value, if known.
             *
             * @return an {@link Optional} containing the counter, or empty if not available
             */
            public Optional<Long> counter() {
                return Optional.ofNullable(counter);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_FUTURE_MESSAGE;
            }

            /**
             * Duplicate messages should not trigger retry receipts.
             *
             * @return {@code false}
             */
            @Override
            public boolean shouldSendRetryReceipt() {
                return false;
            }
        }

        /**
         * Exception thrown when the message counter is too far in the future.
         * <p>
         * Signal maintains a lookahead window of acceptable future message counters.
         * This exception occurs when a message's counter exceeds this window, which
         * may indicate:
         * <ul>
         *   <li>Significant message loss or reordering</li>
         *   <li>Counter desynchronization after device restore</li>
         *   <li>Intentional counter manipulation (potential attack)</li>
         * </ul>
         *
         * <h2>Signal Error Messages</h2>
         * This exception maps to:
         * <ul>
         *   <li>{@code errSignalTooManyMessagesInFuture} - 1:1 messages</li>
         *   <li>{@code errSignalGrpTooManyMessagesInFuture} - Group messages</li>
         * </ul>
         */
        public static final class FutureMessage extends Receive {
            /**
             * The future message counter value, if known.
             */
            private final Long counter;

            /**
             * Whether this is a group message.
             */
            private final boolean isGroupMessage;

            /**
             * Constructs a new future message exception.
             */
            public FutureMessage() {
                super("Message counter too far in future");
                this.counter = null;
                this.isGroupMessage = false;
            }

            /**
             * Constructs a new future message exception with the counter value.
             *
             * @param counter        the future message counter
             * @param isGroupMessage {@code true} if this is a group message
             */
            public FutureMessage(long counter, boolean isGroupMessage) {
                super("Message counter too far in future: " + counter + (isGroupMessage ? " (group)" : ""));
                this.counter = counter;
                this.isGroupMessage = isGroupMessage;
            }

            /**
             * Constructs a new future message exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public FutureMessage(String message, Throwable cause) {
                super(message, cause);
                this.counter = null;
                this.isGroupMessage = false;
            }

            /**
             * Returns the future message counter value, if known.
             *
             * @return an {@link Optional} containing the counter, or empty if not available
             */
            public Optional<Long> counter() {
                return Optional.ofNullable(counter);
            }

            /**
             * Returns whether this is a group message error.
             *
             * @return {@code true} if this is a group message error
             */
            public boolean isGroupMessage() {
                return isGroupMessage;
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_FUTURE_MESSAGE;
            }
        }

        /**
         * Exception thrown when message authentication code (MAC) verification fails.
         * <p>
         * Each Signal message includes an HMAC computed over the ciphertext using a
         * derived authentication key. This exception occurs when the computed MAC
         * does not match the MAC included in the message.
         *
         * <h2>Possible Causes</h2>
         * <ul>
         *   <li>Message was corrupted during transmission</li>
         *   <li>Encryption keys are out of sync</li>
         *   <li>Message was tampered with (man-in-the-middle attack)</li>
         *   <li>Cipher key derivation failed</li>
         * </ul>
         *
         * <h2>MAC Error Types</h2>
         * Different MAC errors indicate different failure points:
         * <ul>
         *   <li>{@link MacErrorType#WITH_DECRYPTED_PLAINTEXT} - MAC failed but decryption succeeded (suspicious)</li>
         *   <li>{@link MacErrorType#INVALID_CIPHER_KEY} - Cipher key derivation failed</li>
         *   <li>{@link MacErrorType#INVALID_CIPHER_KEY_NEW_CHAIN} - Cipher key failed on new ratchet chain</li>
         * </ul>
         *
         * <h2>Signal Error Messages</h2>
         * This exception maps to:
         * <ul>
         *   <li>{@code errInvalidMacWithDecryptedPlaintext}</li>
         *   <li>{@code errInvalidMacInvalidCipherKey}</li>
         *   <li>{@code errInvalidMacInvalidCipherKeyNewChain}</li>
         * </ul>
         */
        public static final class BadMac extends Receive {
            /**
             * The specific type of MAC error.
             */
            private final MacErrorType errorType;

            /**
             * Constructs a new bad MAC exception.
             */
            public BadMac() {
                super("Message authentication code verification failed");
                this.errorType = MacErrorType.UNKNOWN;
            }

            /**
             * Constructs a new bad MAC exception with the specified error type.
             *
             * @param errorType the specific MAC error type
             */
            public BadMac(MacErrorType errorType) {
                super("Message authentication code verification failed: " + errorType);
                this.errorType = Objects.requireNonNull(errorType, "errorType cannot be null");
            }

            /**
             * Constructs a new bad MAC exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public BadMac(String message, Throwable cause) {
                super(message, cause);
                this.errorType = MacErrorType.UNKNOWN;
            }

            /**
             * Returns the specific type of MAC error.
             *
             * @return the MAC error type
             */
            public MacErrorType errorType() {
                return errorType;
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_BAD_MAC;
            }

            /**
             * Types of MAC verification failures.
             */
            public enum MacErrorType {
                /**
                 * Unknown MAC error type.
                 */
                UNKNOWN,

                /**
                 * MAC verification failed but decryption produced plaintext.
                 * <p>
                 * This is suspicious as it could indicate key compromise where an attacker
                 * has the encryption key but not the authentication key.
                 */
                WITH_DECRYPTED_PLAINTEXT,

                /**
                 * MAC verification failed due to invalid cipher key.
                 * <p>
                 * The derived cipher key could not be used to verify the MAC,
                 * indicating a key derivation or synchronization issue.
                 */
                INVALID_CIPHER_KEY,

                /**
                 * MAC verification failed due to invalid cipher key on a new ratchet chain.
                 * <p>
                 * The Double Ratchet created a new chain but the derived key is invalid.
                 */
                INVALID_CIPHER_KEY_NEW_CHAIN
            }
        }

        /**
         * Exception thrown when no sender key exists for group message decryption.
         * <p>
         * Group messages in WhatsApp use the Sender Keys protocol for efficient group
         * encryption. Each group member distributes a sender key that others use to
         * decrypt messages from that sender. This exception occurs when:
         * <ul>
         *   <li>Receiving a message from a new group member before their key distribution</li>
         *   <li>The sender key was deleted due to storage cleanup</li>
         *   <li>Joining a group and receiving old messages</li>
         * </ul>
         *
         * <h2>Signal Error Message</h2>
         * This exception maps to {@code errLoadSenderKeySession}.
         */
        public static final class NoSenderKey extends Receive {
            /**
             * Constructs a new no sender key exception.
             */
            public NoSenderKey() {
                super("No sender key exists for group message decryption");
            }

            /**
             * Constructs a new no sender key exception with the specified message.
             *
             * @param message the detail message
             */
            public NoSenderKey(String message) {
                super(message);
            }

            /**
             * Constructs a new no sender key exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public NoSenderKey(String message, Throwable cause) {
                super(message, cause);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_NO_SESSION;
            }
        }

        /**
         * Exception thrown when sender key validation fails.
         * <p>
         * This occurs when a sender key message cannot be decrypted because the
         * sender key state is corrupted or invalid.
         */
        public static final class InvalidSenderKey extends Receive {
            /**
             * Constructs a new invalid sender key exception.
             */
            public InvalidSenderKey() {
                super("Invalid sender key for group message decryption");
            }

            /**
             * Constructs a new invalid sender key exception with the specified message.
             *
             * @param message the detail message
             */
            public InvalidSenderKey(String message) {
                super(message);
            }

            /**
             * Constructs a new invalid sender key exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public InvalidSenderKey(String message, Throwable cause) {
                super(message, cause);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.SIGNAL_ERROR_NO_SESSION;
            }
        }

        /**
         * Exception thrown when a message is received from an unknown companion device.
         * <p>
         * In WhatsApp's multi-device architecture, messages can come from any of a user's
         * companion devices. This exception occurs when:
         * <ul>
         *   <li>A message arrives from a device not in the known device list</li>
         *   <li>The device has not yet distributed its prekeys</li>
         *   <li>The device was recently added and hasn't synchronized</li>
         * </ul>
         */
        public static final class UnknownDevice extends Receive {
            /**
             * Constructs a new unknown device exception.
             */
            public UnknownDevice() {
                super("Message received from unknown companion device");
            }

            /**
             * Constructs a new unknown device exception with the specified message.
             *
             * @param message the detail message
             */
            public UnknownDevice(String message) {
                super(message);
            }

            /**
             * Constructs a new unknown device exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public UnknownDevice(String message, Throwable cause) {
                super(message, cause);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.UNKNOWN_COMPANION_NO_PREKEY;
            }
        }

        /**
         * Exception thrown when device sent message (DSM) validation fails.
         * <p>
         * Device Sent Messages are a multi-device feature where messages sent from one device
         * are synchronized to other devices owned by the same user. DSM validation ensures:
         * <ul>
         *   <li>Messages that should be DSMs are properly formatted</li>
         *   <li>DSMs are only accepted from legitimate sender devices</li>
         *   <li>DSM structure matches protocol requirements</li>
         * </ul>
         *
         * <h2>DSM Error Types</h2>
         * <ul>
         *   <li>{@link DsmErrorType#INVALID_SENDER} - Message should not be a DSM but is marked as one</li>
         *   <li>{@link DsmErrorType#MISSING_DSM} - Message should be a DSM but isn't</li>
         *   <li>{@link DsmErrorType#INVALID_DSM} - DSM structure is malformed or invalid</li>
         * </ul>
         */
        public static final class InvalidDeviceSentMessage extends Receive {
            /**
             * The specific type of DSM error.
             */
            private final DsmErrorType errorType;

            /**
             * Constructs a new invalid DSM exception.
             */
            public InvalidDeviceSentMessage() {
                super("Device sent message validation failed");
                this.errorType = DsmErrorType.INVALID_DSM;
            }

            /**
             * Constructs a new invalid DSM exception with the specified error type.
             *
             * @param errorType the specific DSM error type
             */
            public InvalidDeviceSentMessage(DsmErrorType errorType) {
                super("Device sent message validation failed: " + errorType);
                this.errorType = Objects.requireNonNull(errorType, "errorType cannot be null");
            }

            /**
             * Constructs a new invalid DSM exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public InvalidDeviceSentMessage(String message, Throwable cause) {
                super(message, cause);
                this.errorType = DsmErrorType.INVALID_DSM;
            }

            /**
             * Returns the specific type of DSM error.
             *
             * @return the DSM error type
             */
            public DsmErrorType errorType() {
                return errorType;
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.UNKNOWN_ERROR;
            }

            @Override
            public Optional<String> errorCode() {
                return Optional.of("400");
            }

            /**
             * Types of device sent message validation failures.
             */
            public enum DsmErrorType {
                /**
                 * Message should not be a device sent message but is marked as one.
                 * <p>
                 * Protocol value: 1
                 */
                INVALID_SENDER(1),

                /**
                 * Message should be a device sent message but isn't.
                 * <p>
                 * Protocol value: 2
                 */
                MISSING_DSM(2),

                /**
                 * Device sent message structure is malformed or invalid.
                 * <p>
                 * Protocol value: 3
                 */
                INVALID_DSM(3);

                /**
                 * The protocol integer value.
                 */
                private final int protocolValue;

                /**
                 * Constructs a DSM error type with the specified protocol value.
                 *
                 * @param protocolValue the integer value used in the protocol
                 */
                DsmErrorType(int protocolValue) {
                    this.protocolValue = protocolValue;
                }

                /**
                 * Returns the protocol integer value.
                 *
                 * @return the protocol value
                 */
                public int protocolValue() {
                    return protocolValue;
                }

                /**
                 * Parses a DSM error type from its protocol value.
                 *
                 * @param value the protocol integer value
                 * @return the corresponding error type
                 * @throws IllegalArgumentException if the value is unknown
                 */
                public static DsmErrorType fromProtocolValue(int value) {
                    for (var type : values()) {
                        if (type.protocolValue == value) {
                            return type;
                        }
                    }
                    throw new IllegalArgumentException("Unknown DSM error code: " + value);
                }
            }
        }

        /**
         * Exception thrown when Account Device Verification (ADV) fails during message decryption.
         * <p>
         * ADV is WhatsApp's mechanism for verifying that a device is legitimately associated
         * with an account. When processing PreKeySignalMessages, the device identity included
         * in the message is validated using ADV signatures.
         * <p>
         * ADV failure during message decryption indicates the sender's device could not be
         * verified, which may indicate:
         * <ul>
         *   <li>The device is not properly registered</li>
         *   <li>ADV signatures are corrupted or forged</li>
         *   <li>Potential impersonation attack</li>
         * </ul>
         *
         * @see WhatsAppAdvValidationException for detailed ADV failure types
         */
        public static final class AdvFailure extends Receive {
            /**
             * Constructs a new ADV failure exception.
             */
            public AdvFailure() {
                super("Account device verification failed during message decryption");
            }

            /**
             * Constructs a new ADV failure exception with the specified message.
             *
             * @param message the detail message
             */
            public AdvFailure(String message) {
                super(message);
            }

            /**
             * Constructs a new ADV failure exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause (typically a WhatsAppAdvValidationException)
             */
            public AdvFailure(String message, Throwable cause) {
                super(message, cause);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.ADV_FAILURE;
            }
        }

        /**
         * Exception thrown when protobuf message deserialization or validation fails.
         * <p>
         * After Signal decryption succeeds, the plaintext is deserialized as a Protocol Buffer
         * message. This exception occurs when:
         * <ul>
         *   <li>The protobuf data is malformed or truncated</li>
         *   <li>Required protobuf fields are missing</li>
         *   <li>The message type does not match expectations</li>
         *   <li>Multiple message types are present when only one is expected</li>
         *   <li>Message validation rules are violated</li>
         * </ul>
         *
         * <h2>Validation Failures</h2>
         * Different validation failures have specific error reasons:
         * <ul>
         *   <li>{@link ProtobufErrorReason#INVALID_MESSAGE} - General protobuf parsing failure</li>
         *   <li>{@link ProtobufErrorReason#MESSAGE_TYPE_MISMATCH} - Message type doesn't match stanza type</li>
         *   <li>{@link ProtobufErrorReason#INVALID_NUMBER_OF_MESSAGE_TYPES} - Multiple message types present</li>
         * </ul>
         */
        public static final class InvalidProtobuf extends Receive {
            /**
             * The error code for NACK receipts.
             */
            private final String errorCode;

            /**
             * The specific error reason.
             */
            private final ProtobufErrorReason errorReason;

            /**
             * Constructs a new invalid protobuf exception.
             */
            public InvalidProtobuf() {
                super("Protobuf message validation failed");
                this.errorCode = "400";
                this.errorReason = ProtobufErrorReason.INVALID_MESSAGE;
            }

            /**
             * Constructs a new invalid protobuf exception with the specified message.
             *
             * @param message the detail message
             */
            public InvalidProtobuf(String message) {
                super(message);
                this.errorCode = "400";
                this.errorReason = ProtobufErrorReason.INVALID_MESSAGE;
            }

            /**
             * Constructs a new invalid protobuf exception with detailed information.
             *
             * @param errorCode   the error code for NACK receipts
             * @param message     the detail message
             * @param errorReason the specific error reason
             */
            public InvalidProtobuf(String errorCode, String message, ProtobufErrorReason errorReason) {
                super(message);
                this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
                this.errorReason = Objects.requireNonNull(errorReason, "errorReason cannot be null");
            }

            /**
             * Constructs a new invalid protobuf exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public InvalidProtobuf(String message, Throwable cause) {
                super(message, cause);
                this.errorCode = "400";
                this.errorReason = ProtobufErrorReason.INVALID_MESSAGE;
            }

            /**
             * Constructs a new invalid protobuf exception with full details and a cause.
             *
             * @param errorCode   the error code for NACK receipts
             * @param message     the detail message
             * @param errorReason the specific error reason
             * @param cause       the underlying cause
             */
            public InvalidProtobuf(String errorCode, String message, ProtobufErrorReason errorReason, Throwable cause) {
                super(message, cause);
                this.errorCode = Objects.requireNonNull(errorCode, "errorCode cannot be null");
                this.errorReason = Objects.requireNonNull(errorReason, "errorReason cannot be null");
            }

            /**
             * Returns the specific protobuf error reason.
             *
             * @return the error reason
             */
            public ProtobufErrorReason errorReason() {
                return errorReason;
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.UNKNOWN_ERROR;
            }

            @Override
            public Optional<String> errorCode() {
                return Optional.of(errorCode);
            }

            /**
             * Reasons for protobuf validation failures.
             */
            public enum ProtobufErrorReason {
                /**
                 * General protobuf parsing or validation failure.
                 */
                INVALID_MESSAGE,

                /**
                 * The message type in the protobuf does not match the expected stanza type.
                 * <p>
                 * For example, receiving a poll update message when a reaction was expected.
                 */
                MESSAGE_TYPE_MISMATCH,

                /**
                 * Multiple message types are present in a message that should contain only one.
                 */
                INVALID_NUMBER_OF_MESSAGE_TYPES
            }
        }

        /**
         * Exception thrown when broadcast ephemeral settings decryption fails.
         * <p>
         * Broadcast lists in WhatsApp can have ephemeral (disappearing) message settings.
         * These settings are encrypted and sent to recipients. This exception occurs when
         * the ephemeral settings cannot be decrypted or validated.
         */
        public static final class BroadcastEphemeralSettings extends Receive {
            /**
             * Constructs a new broadcast ephemeral settings exception.
             */
            public BroadcastEphemeralSettings() {
                super("Failed to decrypt broadcast ephemeral settings");
            }

            /**
             * Constructs a new broadcast ephemeral settings exception with the specified message.
             *
             * @param message the detail message
             */
            public BroadcastEphemeralSettings(String message) {
                super(message);
            }

            /**
             * Constructs a new broadcast ephemeral settings exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public BroadcastEphemeralSettings(String message, Throwable cause) {
                super(message, cause);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.BAD_BROADCAST_EPH_SETTINGS;
            }
        }

        /**
         * Exception thrown when an HSM (Hardware Security Module) template mismatch occurs.
         * <p>
         * HSM templates are used by business accounts for approved message templates.
         * This exception occurs when the decrypted message does not match the expected
         * HSM template, which can happen due to:
         * <ul>
         *   <li>Template version mismatch</li>
         *   <li>Template was modified or revoked</li>
         *   <li>Incorrect template parameters</li>
         * </ul>
         */
        public static final class HsmMismatch extends Receive {
            /**
             * Constructs a new HSM mismatch exception.
             */
            public HsmMismatch() {
                super("HSM template mismatch");
            }

            /**
             * Constructs a new HSM mismatch exception with the specified message.
             *
             * @param message the detail message
             */
            public HsmMismatch(String message) {
                super(message);
            }

            /**
             * Constructs a new HSM mismatch exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public HsmMismatch(String message, Throwable cause) {
                super(message, cause);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.UNKNOWN_ERROR;
            }

            @Override
            public Optional<String> errorCode() {
                return Optional.of("400");
            }
        }

        /**
         * Exception thrown for unclassified decryption failures.
         * <p>
         * This is a catch-all for decryption errors that don't fit into other categories.
         * When this exception is thrown, additional investigation may be needed to determine
         * the root cause.
         */
        public static final class Unknown extends Receive {
            /**
             * Constructs a new unknown decryption exception.
             */
            public Unknown() {
                super("Unknown message decryption failure");
            }

            /**
             * Constructs a new unknown decryption exception with the specified message.
             *
             * @param message the detail message
             */
            public Unknown(String message) {
                super(message);
            }

            /**
             * Constructs a new unknown decryption exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public Unknown(String message, Throwable cause) {
                super(message, cause);
            }

            @Override
            public RetryReason retryReason() {
                return RetryReason.UNKNOWN_ERROR;
            }
        }
    }

    /**
     * Abstract base exception for all message sending failures.
     * <p>
     * This sealed class hierarchy represents the complete taxonomy of errors that can occur
     * when attempting to send a WhatsApp message. Each subclass corresponds to a specific
     * failure mode in the encryption, network transmission, or server acknowledgment pipeline.
     *
     * <h2>Exception Categories</h2>
     * <ul>
     *   <li><b>Encryption errors:</b> {@link NoSession}, {@link InvalidKey}, {@link NoSenderKey},
     *       {@link SenderKeyExpired} - Cryptographic failures during message encryption</li>
     *   <li><b>Device errors:</b> {@link PhashMismatch}, {@link MissingPreKeys}, {@link IdentityChanged} - Multi-device synchronization failures</li>
     *   <li><b>Protocol errors:</b> {@link ServerNack}, {@link InvalidRecipient}, {@link PayloadTooLarge},
     *       {@link MessageExpired} - Server-side rejections</li>
     *   <li><b>Network errors:</b> {@link Timeout} - Transport failure</li>
     *   <li><b>Unknown:</b> {@link Unknown} - Unclassified failures</li>
     * </ul>
     *
     * <h2>Server NACK Error Codes</h2>
     * <table border="1">
     *   <caption>Server NACK Error Codes</caption>
     *   <tr><th>Code</th><th>Name</th><th>Description</th></tr>
     *   <tr><td>421</td><td>StaleGroupAddressingMode</td><td>Group addressing mode is stale</td></tr>
     *   <tr><td>475</td><td>NewChatMessagesCapped</td><td>Monthly message limit reached</td></tr>
     *   <tr><td>487</td><td>ParsingError</td><td>Server failed to parse message</td></tr>
     *   <tr><td>491</td><td>InvalidProtobuf</td><td>Invalid protobuf structure</td></tr>
     *   <tr><td>495</td><td>MissingMessageSecret</td><td>Message secret is required but missing</td></tr>
     *   <tr><td>496</td><td>SignalErrorOldCounter</td><td>Signal message counter is too old</td></tr>
     *   <tr><td>499</td><td>MessageDeletedOnPeer</td><td>Referenced message was deleted</td></tr>
     *   <tr><td>500</td><td>UnhandledError</td><td>Server encountered unhandled error</td></tr>
     *   <tr><td>550</td><td>UnsupportedAdminRevoke</td><td>Admin revoke not supported</td></tr>
     *   <tr><td>551</td><td>UnsupportedLIDGroup</td><td>LID groups not supported</td></tr>
     *   <tr><td>552</td><td>DBOperationFailed</td><td>Server database operation failed</td></tr>
     * </table>
     */
    public sealed static abstract class Send extends WhatsAppMessageException
            permits Send.NoSession, Send.InvalidKey, Send.NoSenderKey, Send.SenderKeyExpired,
                    Send.PhashMismatch, Send.MissingPreKeys, Send.IdentityChanged,
                    Send.ServerNack, Send.InvalidRecipient, Send.PayloadTooLarge, Send.MessageExpired,
                    Send.MessageCapped, Send.Timeout, Send.Unknown, Send.Duplicate, Send.Unauthorized {

        /**
         * Constructs a new send exception with the specified detail message.
         *
         * @param message the detail message describing the send failure
         */
        protected Send(String message) {
            super(message);
        }

        /**
         * Constructs a new send exception with the specified detail message and cause.
         *
         * @param message the detail message describing the send failure
         * @param cause   the underlying cause of this exception
         */
        protected Send(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Returns whether this send failure is retryable.
         * <p>
         * Most send failures can be retried after taking appropriate recovery actions
         * (e.g., refreshing device lists, fetching prekeys). Some failures like
         * {@link InvalidRecipient} or {@link MessageCapped} are not retryable.
         *
         * @return {@code true} if this failure can be retried
         */
        public abstract boolean isRetryable();

        /**
         * Exception thrown when no Signal session exists with the target device.
         * <p>
         * This occurs when attempting to encrypt a message for a device without
         * an established Signal session. Recovery requires fetching the device's
         * prekey bundle and establishing a new session.
         *
         * <h2>Recovery Action</h2>
         * <ol>
         *   <li>Fetch prekey bundle for the device</li>
         *   <li>Process the bundle to establish a Signal session</li>
         *   <li>Retry the message send</li>
         * </ol>
         */
        public static final class NoSession extends Send {
            /**
             * The device JID that has no session.
             */
            private final Jid deviceJid;

            /**
             * Constructs a new no-session exception.
             *
             * @param deviceJid the device JID missing a session
             */
            public NoSession(Jid deviceJid) {
                super("No Signal session exists with device: " + deviceJid);
                this.deviceJid = deviceJid;
            }

            /**
             * Constructs a new no-session exception with a cause.
             *
             * @param deviceJid the device JID missing a session
             * @param cause     the underlying cause
             */
            public NoSession(Jid deviceJid, Throwable cause) {
                super("No Signal session exists with device: " + deviceJid, cause);
                this.deviceJid = deviceJid;
            }

            /**
             * Returns the device JID that has no session.
             *
             * @return the device JID
             */
            public Jid deviceJid() {
                return deviceJid;
            }

            @Override
            public boolean isRetryable() {
                return true;
            }
        }

        /**
         * Exception thrown when encryption fails due to an invalid cryptographic key.
         * <p>
         * This can occur when:
         * <ul>
         *   <li>The session's ratchet key is corrupted</li>
         *   <li>Key derivation produces invalid material</li>
         *   <li>The recipient's public key is malformed</li>
         * </ul>
         */
        public static final class InvalidKey extends Send {
            /**
             * The device JID with the invalid key.
             */
            private final Jid deviceJid;

            /**
             * Constructs a new invalid key exception.
             *
             * @param deviceJid the device JID with invalid key
             */
            public InvalidKey(Jid deviceJid) {
                super("Invalid cryptographic key for device: " + deviceJid);
                this.deviceJid = deviceJid;
            }

            /**
             * Constructs a new invalid key exception with a cause.
             *
             * @param deviceJid the device JID with invalid key
             * @param cause     the underlying cause
             */
            public InvalidKey(Jid deviceJid, Throwable cause) {
                super("Invalid cryptographic key for device: " + deviceJid, cause);
                this.deviceJid = deviceJid;
            }

            /**
             * Returns the device JID with the invalid key.
             *
             * @return the device JID
             */
            public Jid deviceJid() {
                return deviceJid;
            }

            @Override
            public boolean isRetryable() {
                return true;
            }
        }

        /**
         * Exception thrown when no sender key exists for group message encryption.
         * <p>
         * Sender keys are used for efficient group encryption. This exception occurs
         * when attempting to encrypt a group message before the sender key has been
         * initialized or when the sender key record is missing.
         *
         * <h2>Recovery Action</h2>
         * <ol>
         *   <li>Generate a new sender key for the group</li>
         *   <li>Distribute the sender key to all group members</li>
         *   <li>Retry the message send</li>
         * </ol>
         */
        public static final class NoSenderKey extends Send {
            /**
             * The group JID missing the sender key.
             */
            private final Jid groupJid;

            /**
             * Constructs a new no-sender-key exception.
             *
             * @param groupJid the group JID missing sender key
             */
            public NoSenderKey(Jid groupJid) {
                super("No sender key exists for group: " + groupJid);
                this.groupJid = groupJid;
            }

            /**
             * Constructs a new no-sender-key exception with a cause.
             *
             * @param groupJid the group JID missing sender key
             * @param cause    the underlying cause
             */
            public NoSenderKey(Jid groupJid, Throwable cause) {
                super("No sender key exists for group: " + groupJid, cause);
                this.groupJid = groupJid;
            }

            /**
             * Returns the group JID missing the sender key.
             *
             * @return the group JID
             */
            public Jid groupJid() {
                return groupJid;
            }

            @Override
            public boolean isRetryable() {
                return true;
            }
        }

        /**
         * Exception thrown when the sender key has expired and needs rotation.
         * <p>
         * Sender keys are rotated periodically for forward secrecy. When a sender key
         * expires, a new key must be generated and distributed to all group members.
         */
        public static final class SenderKeyExpired extends Send {
            /**
             * The group JID with expired sender key.
             */
            private final Jid groupJid;

            /**
             * Constructs a new sender-key-expired exception.
             *
             * @param groupJid the group JID with expired key
             */
            public SenderKeyExpired(Jid groupJid) {
                super("Sender key expired for group: " + groupJid);
                this.groupJid = groupJid;
            }

            /**
             * Returns the group JID with expired sender key.
             *
             * @return the group JID
             */
            public Jid groupJid() {
                return groupJid;
            }

            @Override
            public boolean isRetryable() {
                return true;
            }
        }

        /**
         * Exception thrown when the participant hash (phash) doesn't match the server's expectation.
         * <p>
         * The phash is a hash of all device JIDs that should receive the message. A mismatch
         * indicates the client's device list is stale and needs to be refreshed.
         */
        public static final class PhashMismatch extends Send {
            /**
             * The phash the server expected.
             */
            private final String expectedHash;

            /**
             * The phash the client sent.
             */
            private final String actualHash;

            /**
             * Constructs a new phash mismatch exception.
             *
             * @param expectedHash the phash the client sent
             */
            public PhashMismatch(String expectedHash, String actualHash) {
                super("Phash mismatch: computed " + expectedHash);
                this.expectedHash = expectedHash;
                this.actualHash = actualHash;
            }

            /**
             * Returns the phash the server expected.
             *
             * @return the expected phash
             */
            public String expectedHash() {
                return expectedHash;
            }

            /**
             * Returns the phash the client sent.
             *
             * @return the actual phash
             */
            public String actualHash() {
                return actualHash;
            }

            @Override
            public boolean isRetryable() {
                return true;
            }
        }

        /**
         * Exception thrown when prekey bundles are missing for some recipient devices.
         * <p>
         * This occurs when the client needs to establish Signal sessions with devices
         * but doesn't have their prekey bundles cached.
         */
        public static final class MissingPreKeys extends Send {
            /**
             * The device JIDs missing prekeys.
             */
            private final List<Jid> devices;

            /**
             * Constructs a new missing prekeys exception.
             *
             * @param devices the device JIDs missing prekeys
             */
            public MissingPreKeys(List<Jid> devices) {
                super("Missing prekey bundles for " + devices.size() + " devices");
                this.devices = List.copyOf(devices);
            }

            /**
             * Returns the device JIDs missing prekeys.
             *
             * @return the device JIDs
             */
            public List<Jid> devices() {
                return devices;
            }

            @Override
            public boolean isRetryable() {
                return true;
            }
        }

        /**
         * Exception thrown when a recipient's identity key has changed.
         * <p>
         * This typically indicates the recipient has reinstalled the app or is using
         * a new device. The user should be notified of the safety number change.
         *
         * <h2>Security Consideration</h2>
         * Identity key changes may indicate a man-in-the-middle attack. Users should
         * verify the new safety number with the recipient through an out-of-band channel.
         */
        public static final class IdentityChanged extends Send {
            /**
             * The device JIDs with changed identity keys.
             */
            private final List<Jid> devices;

            /**
             * Constructs a new identity changed exception.
             *
             * @param devices the device JIDs with changed identities
             */
            public IdentityChanged(List<Jid> devices) {
                super("Identity key changed for " + devices.size() + " devices");
                this.devices = List.copyOf(devices);
            }

            /**
             * Returns the device JIDs with changed identity keys.
             *
             * @return the device JIDs
             */
            public List<Jid> devices() {
                return devices;
            }

            @Override
            public boolean isRetryable() {
                return true;
            }
        }

        /**
         * Exception thrown when the server sends a negative acknowledgment (NACK).
         * <p>
         * NACK responses include an error code indicating why the message was rejected.
         * Different error codes require different recovery strategies.
         */
        public static final class ServerNack extends Send {
            /**
             * Group addressing mode is stale.
             */
            public static final int STALE_GROUP_ADDRESSING_MODE = 421;

            /**
             * Monthly new chat message limit reached.
             */
            public static final int NEW_CHAT_MESSAGES_CAPPED = 475;

            /**
             * Server failed to parse the message.
             */
            public static final int PARSING_ERROR = 487;

            /**
             * Unrecognized stanza received.
             */
            public static final int UNRECOGNIZED_STANZA = 488;

            /**
             * Unrecognized stanza class.
             */
            public static final int UNRECOGNIZED_STANZA_CLASS = 489;

            /**
             * Unrecognized stanza type.
             */
            public static final int UNRECOGNIZED_STANZA_TYPE = 490;

            /**
             * Invalid protobuf structure.
             */
            public static final int INVALID_PROTOBUF = 491;

            /**
             * Invalid hosted companion stanza.
             */
            public static final int INVALID_HOSTED_COMPANION_STANZA = 493;

            /**
             * Message secret is required but missing.
             */
            public static final int MISSING_MESSAGE_SECRET = 495;

            /**
             * Signal message counter is too old.
             */
            public static final int SIGNAL_ERROR_OLD_COUNTER = 496;

            /**
             * Referenced message was deleted on peer.
             */
            public static final int MESSAGE_DELETED_ON_PEER = 499;

            /**
             * Server encountered an unhandled error.
             */
            public static final int UNHANDLED_ERROR = 500;

            /**
             * Admin revoke not supported.
             */
            public static final int UNSUPPORTED_ADMIN_REVOKE = 550;

            /**
             * LID groups not supported.
             */
            public static final int UNSUPPORTED_LID_GROUP = 551;

            /**
             * Server database operation failed.
             */
            public static final int DB_OPERATION_FAILED = 552;

            /**
             * The NACK error code from the server.
             */
            private final int errorCode;

            /**
             * The error description, if available.
             */
            private final String errorDescription;

            /**
             * Constructs a new server NACK exception.
             *
             * @param errorCode        the NACK error code
             * @param errorDescription the error description
             */
            public ServerNack(int errorCode, String errorDescription) {
                super("Server NACK: " + errorCode + " - " + errorDescription);
                this.errorCode = errorCode;
                this.errorDescription = errorDescription;
            }

            /**
             * Returns the NACK error code.
             *
             * @return the error code
             */
            public int errorCode() {
                return errorCode;
            }

            /**
             * Returns the error description.
             *
             * @return the error description, or null if not available
             */
            public String errorDescription() {
                return errorDescription;
            }

            @Override
            public boolean isRetryable() {
                return switch (errorCode) {
                    case STALE_GROUP_ADDRESSING_MODE, SIGNAL_ERROR_OLD_COUNTER, UNHANDLED_ERROR -> true;
                    default -> false;
                };
            }
        }

        /**
         * Exception thrown when the message recipient is invalid.
         * <p>
         * This can occur when:
         * <ul>
         *   <li>The recipient JID is malformed</li>
         *   <li>The recipient type is not supported (e.g., sending to a call JID)</li>
         *   <li>The recipient has blocked the sender</li>
         *   <li>The recipient account no longer exists</li>
         * </ul>
         */
        public static final class InvalidRecipient extends Send {
            /**
             * The invalid recipient JID.
             */
            private final Jid recipientJid;

            /**
             * The reason the recipient is invalid.
             */
            private final String reason;

            /**
             * Constructs a new invalid recipient exception.
             *
             * @param recipientJid the invalid recipient JID
             * @param reason       the reason the recipient is invalid
             */
            public InvalidRecipient(Jid recipientJid, String reason) {
                super("Invalid recipient " + recipientJid + ": " + reason);
                this.recipientJid = recipientJid;
                this.reason = reason;
            }

            /**
             * Returns the invalid recipient JID.
             *
             * @return the recipient JID
             */
            public Jid recipientJid() {
                return recipientJid;
            }

            /**
             * Returns the reason the recipient is invalid.
             *
             * @return the reason
             */
            public String reason() {
                return reason;
            }

            @Override
            public boolean isRetryable() {
                return false;
            }
        }

        /**
         * Exception thrown when the message payload exceeds the maximum allowed size.
         * <p>
         * WhatsApp imposes limits on message sizes to prevent abuse. Media messages
         * should be uploaded separately and referenced by URL.
         */
        public static final class PayloadTooLarge extends Send {
            /**
             * The actual payload size in bytes.
             */
            private final long actualSize;

            /**
             * The maximum allowed size in bytes.
             */
            private final long maxSize;

            /**
             * Constructs a new payload too large exception.
             *
             * @param actualSize the actual payload size
             * @param maxSize    the maximum allowed size
             */
            public PayloadTooLarge(long actualSize, long maxSize) {
                super("Message payload too large: " + actualSize + " bytes (max: " + maxSize + ")");
                this.actualSize = actualSize;
                this.maxSize = maxSize;
            }

            /**
             * Returns the actual payload size in bytes.
             *
             * @return the actual size
             */
            public long actualSize() {
                return actualSize;
            }

            /**
             * Returns the maximum allowed size in bytes.
             *
             * @return the maximum size
             */
            public long maxSize() {
                return maxSize;
            }

            @Override
            public boolean isRetryable() {
                return false;
            }
        }

        /**
         * Exception thrown when the message has expired before delivery.
         * <p>
         * Messages have a time-to-live after which they cannot be delivered.
         * This typically applies to ephemeral messages or messages queued
         * while offline for too long.
         */
        public static final class MessageExpired extends Send {
            /**
             * Constructs a new message expired exception.
             */
            public MessageExpired() {
                super("Message expired before delivery");
            }

            /**
             * Constructs a new message expired exception with a message ID.
             *
             * @param messageId the expired message ID
             */
            public MessageExpired(String messageId) {
                super("Message " + messageId + " expired before delivery");
            }

            @Override
            public boolean isRetryable() {
                return false;
            }
        }

        /**
         * Exception thrown when the user has reached their monthly message cap.
         * <p>
         * WhatsApp limits the number of new chat messages a user can send per month.
         * This limit applies to messages to contacts the user hasn't messaged before.
         */
        public static final class MessageCapped extends Send {
            /**
             * Constructs a new message capped exception.
             */
            public MessageCapped() {
                super("Monthly new chat message limit reached");
            }

            @Override
            public boolean isRetryable() {
                return false;
            }
        }

        /**
         * Exception thrown when the message send times out.
         * <p>
         * Timeouts can occur when:
         * <ul>
         *   <li>The server doesn't respond within the expected time</li>
         *   <li>Network congestion causes excessive delays</li>
         *   <li>The connection is unstable</li>
         * </ul>
         */
        public static final class Timeout extends Send {
            /**
             * Constructs a new timeout exception.
             *
             */
            public Timeout() {
                super("Message send timed out");
            }

            @Override
            public boolean isRetryable() {
                return true;
            }
        }

        /**
         * Exception thrown for unclassified send failures.
         * <p>
         * This is a catch-all for send errors that don't fit into other categories.
         * Additional investigation may be needed to determine the root cause.
         */
        public static final class Unknown extends Send {
            /**
             * Constructs a new unknown send exception.
             */
            public Unknown() {
                super("Unknown message send failure");
            }

            /**
             * Constructs a new unknown send exception with the specified message.
             *
             * @param message the detail message
             */
            public Unknown(String message) {
                super(message);
            }

            /**
             * Constructs a new unknown send exception with a cause.
             *
             * @param message the detail message
             * @param cause   the underlying cause
             */
            public Unknown(String message, Throwable cause) {
                super(message, cause);
            }

            @Override
            public boolean isRetryable() {
                return false;
            }
        }

        /**
         * Exception thrown when a duplicate message send is attempted.
         * <p>
         * This occurs when the same message ID is sent while a previous send
         * with that ID is still in progress. This prevents duplicate messages
         * from being sent due to race conditions or retries.
         *
         * @apiNote WAWebMessageDedupUtils: manages pending message cache to handle deduplication
         */
        public static final class Duplicate extends Send {
            /**
             * The message ID that was duplicated.
             */
            private final String messageId;

            /**
             * Constructs a new duplicate exception.
             *
             * @param messageId the duplicate message ID
             */
            public Duplicate(String messageId) {
                super("Duplicate message send attempted: " + messageId);
                this.messageId = messageId;
            }

            /**
             * Returns the message ID that was duplicated.
             *
             * @return the duplicate message ID
             */
            public String messageId() {
                return messageId;
            }

            @Override
            public boolean isRetryable() {
                return false;
            }
        }

        /**
         * Exception thrown when the sender lacks permission to send a message.
         * <p>
         * This occurs when attempting to send to a group where the sender does not
         * have the required permissions. For example, non-admins cannot send messages
         * to Community Announcement Groups (CAGs) with admin-only send permissions.
         *
         * <h2>Common Causes</h2>
         * <ul>
         *   <li>Non-admin sending to a CAG with admin-only send policy</li>
         *   <li>User removed from group but send still attempted</li>
         *   <li>Group settings changed after message was composed</li>
         * </ul>
         *
         * @apiNote WAWebGroupPermissionsApi.validateCagPermissions
         */
        public static final class Unauthorized extends Send {
            /**
             * Constructs a new unauthorized exception.
             *
             * @param message the detail message
             */
            public Unauthorized(String message) {
                super(message);
            }

            @Override
            public boolean isRetryable() {
                return false;
            }
        }
    }
}
