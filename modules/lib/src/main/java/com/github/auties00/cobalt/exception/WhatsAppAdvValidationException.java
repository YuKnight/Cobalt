package com.github.auties00.cobalt.exception;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;

/**
 * Thrown when the cryptographic identity of a companion device cannot be
 * verified through Advanced Device Verification (ADV).
 *
 * <p>Every device linked to a WhatsApp account carries a triple of
 * signatures the primary device produces when the companion is paired.
 * Before Cobalt accepts a prekey bundle or message from a remote device,
 * it re-checks those signatures and an HMAC over the serialized device
 * identity. Any failure in that check raises one of the nested
 * subtypes, each describing a specific step of the verification that
 * went wrong, and carries the JID of the device that failed.
 *
 * <p>All ADV validation failures are fatal because accepting an
 * unverified device would allow a third party to inject themselves into
 * the end-to-end-encrypted exchange.
 *
 * @see MissingDeviceIdentity
 * @see EmptyDeviceIdentity
 * @see AccountSignatureFailed
 * @see DeviceSignatureFailed
 * @see HmacValidationFailed
 * @see CryptoError
 */
public sealed abstract class WhatsAppAdvValidationException extends WhatsAppException
        permits WhatsAppAdvValidationException.MissingDeviceIdentity,
                WhatsAppAdvValidationException.EmptyDeviceIdentity,
                WhatsAppAdvValidationException.AccountSignatureFailed,
                WhatsAppAdvValidationException.DeviceSignatureFailed,
                WhatsAppAdvValidationException.HmacValidationFailed,
                WhatsAppAdvValidationException.CryptoError {

    /**
     * The JID of the device whose identity could not be verified.
     */
    private final Jid jid;

    /**
     * Constructs a new ADV validation exception with the specified message and device JID.
     *
     * @param message the detail message describing the validation failure
     * @param jid     the JID of the device that failed validation
     * @throws NullPointerException if {@code jid} is {@code null}
     */
    protected WhatsAppAdvValidationException(String message, Jid jid) {
        super(message);
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
    }

    /**
     * Constructs a new ADV validation exception with a message, device JID, and cause.
     *
     * @param message the detail message describing the validation failure
     * @param jid     the JID of the device that failed validation
     * @param cause   the underlying cause of the validation failure
     * @throws NullPointerException if {@code jid} is {@code null}
     */
    protected WhatsAppAdvValidationException(String message, Jid jid, Throwable cause) {
        super(message, cause);
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
    }

    /**
     * Returns the JID of the device that failed ADV validation.
     *
     * @return the device JID, never {@code null}
     */
    public Jid jid() {
        return jid;
    }

    /**
     * Returns whether the failure invalidates the current session.
     *
     * <p>All ADV validation failures are fatal because the device on the
     * other end cannot be trusted as a peer in the end-to-end-encrypted
     * exchange.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFatal() {
        return true;
    }

    /**
     * Thrown when a prekey response is missing the {@code device-identity}
     * node Cobalt needs in order to start ADV validation.
     *
     * <p>WhatsApp guarantees this node accompanies every prekey bundle
     * served by an ADV-capable device, so its absence indicates either a
     * malformed response from the server or a peer that does not speak
     * ADV.
     */
    public static final class MissingDeviceIdentity extends WhatsAppAdvValidationException {
        /**
         * Constructs a new missing device identity exception.
         *
         * @param jid the JID of the device whose identity is missing
         */
        public MissingDeviceIdentity(Jid jid) {
            super("Missing device-identity in prekey response for " + jid, jid);
        }
    }

    /**
     * Thrown when the {@code device-identity} node is present in a prekey
     * response but its payload is empty or syntactically broken.
     */
    public static final class EmptyDeviceIdentity extends WhatsAppAdvValidationException {
        /**
         * Constructs a new empty device identity exception.
         *
         * @param jid the JID of the device with empty identity
         */
        public EmptyDeviceIdentity(Jid jid) {
            super("Empty device-identity node for " + jid, jid);
        }
    }

    /**
     * Thrown when the account signature in the device identity does not
     * verify against the account's identity key.
     *
     * <p>The account signature is produced by the primary device when a
     * companion is linked. A mismatch means the companion was not
     * authorized by the account owner and must not be trusted.
     */
    public static final class AccountSignatureFailed extends WhatsAppAdvValidationException {
        /**
         * Constructs a new account signature failed exception.
         *
         * @param jid the JID of the device whose account signature failed
         */
        public AccountSignatureFailed(Jid jid) {
            super("ADV account signature verification failed for " + jid, jid);
        }
    }

    /**
     * Thrown when the device signature in the device identity does not
     * verify against the device's own public key.
     *
     * <p>The device signature is produced by the companion device itself
     * to prove it possesses the private key matching the public key it
     * announces. A mismatch means the device cannot prove that ownership.
     */
    public static final class DeviceSignatureFailed extends WhatsAppAdvValidationException {
        /**
         * Constructs a new device signature failed exception.
         *
         * @param jid the JID of the device whose device signature failed
         */
        public DeviceSignatureFailed(Jid jid) {
            super("ADV device signature verification failed for " + jid, jid);
        }
    }

    /**
     * Thrown when the HMAC stamped over the serialized device identity
     * does not match the value Cobalt computes from the shared secret.
     *
     * <p>The HMAC protects the identity payload from in-flight tampering;
     * a mismatch means the bytes were modified or the verification key is
     * out of sync.
     */
    public static final class HmacValidationFailed extends WhatsAppAdvValidationException {
        /**
         * Constructs a new HMAC validation failed exception.
         *
         * @param jid the JID of the device whose HMAC validation failed
         */
        public HmacValidationFailed(Jid jid) {
            super("ADV HMAC validation failed for " + jid, jid);
        }
    }

    /**
     * Thrown when a low-level cryptographic operation fails while
     * validating ADV signatures.
     *
     * <p>This wraps unexpected JCE failures such as malformed key
     * encodings, missing algorithms, or provider misconfiguration so the
     * caller can distinguish a genuine signature mismatch from an
     * environment problem.
     */
    public static final class CryptoError extends WhatsAppAdvValidationException {
        /**
         * Constructs a new crypto error exception.
         *
         * @param jid   the JID of the device being validated when the error occurred
         * @param cause the underlying cryptographic exception
         */
        public CryptoError(Jid jid, Throwable cause) {
            super("ADV cryptographic operation failed for " + jid, jid, cause);
        }
    }
}
