package com.github.auties00.cobalt.exception;

import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;

/**
 * Exception thrown when Account Device Verification (ADV) validation fails.
 * <p>
 * ADV is WhatsApp's cryptographic mechanism for validating companion device identities
 * in the multi-device architecture. When a new device connects or sends a message,
 * WhatsApp uses ADV to verify that the device is legitimately associated with the claimed
 * account.
 *
 * <h2>ADV Architecture</h2>
 * ADV validation uses a chain of cryptographic signatures:
 * <ol>
 *   <li><b>Account Signature:</b> Created by the primary device using the account's identity key,
 *       proving the device was authorized by the account owner</li>
 *   <li><b>Device Signature:</b> Created by the companion device itself, proving it possesses
 *       the corresponding private key</li>
 *   <li><b>HMAC Validation:</b> Ensures the device identity data hasn't been tampered with</li>
 * </ol>
 *
 * <h2>Signed Data Structure</h2>
 * The ADV signature covers:
 * <ul>
 *   <li>Device identity details (device type, platform info)</li>
 *   <li>Device's public key</li>
 *   <li>Account signature (for device signature only)</li>
 * </ul>
 *
 * <h2>Exception Hierarchy</h2>
 * <ul>
 *   <li>{@link MissingDeviceIdentity} - Device identity node not present in response</li>
 *   <li>{@link EmptyDeviceIdentity} - Device identity node present but empty</li>
 *   <li>{@link AccountSignatureFailed} - Account signature verification failed</li>
 *   <li>{@link DeviceSignatureFailed} - Device signature verification failed</li>
 *   <li>{@link HmacValidationFailed} - HMAC integrity check failed</li>
 *   <li>{@link CryptoError} - Low-level cryptographic operation failed</li>
 * </ul>
 *
 * <h2>Security Implications</h2>
 * ADV validation failures may indicate:
 * <ul>
 *   <li>Man-in-the-middle attack attempting to inject a rogue device</li>
 *   <li>Unauthorized device registration attempt</li>
 *   <li>Data corruption during transmission</li>
 *   <li>Protocol implementation bugs</li>
 * </ul>
 *
 * <h2>Fatality</h2>
 * All ADV validation failures are fatal. The session should not continue with an
 * unverified device as this could compromise end-to-end encryption security.
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
     * The JID of the device that failed ADV validation.
     */
    private final Jid jid;

    /**
     * Constructs a new ADV validation exception with the specified message and device JID.
     *
     * @param message the detail message describing the validation failure
     * @param jid     the JID of the device that failed validation; must not be null
     * @throws NullPointerException if jid is null
     */
    protected WhatsAppAdvValidationException(String message, Jid jid) {
        super(message);
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
    }

    /**
     * Constructs a new ADV validation exception with a message, device JID, and cause.
     *
     * @param message the detail message describing the validation failure
     * @param jid     the JID of the device that failed validation; must not be null
     * @param cause   the underlying cause of the validation failure
     * @throws NullPointerException if jid is null
     */
    protected WhatsAppAdvValidationException(String message, Jid jid, Throwable cause) {
        super(message, cause);
        this.jid = Objects.requireNonNull(jid, "jid cannot be null");
    }

    /**
     * Returns the JID of the device that failed ADV validation.
     * <p>
     * This JID identifies the specific device whose identity could not be verified.
     * It includes both the user identifier and device identifier components.
     *
     * @return the device JID; never null
     */
    public Jid jid() {
        return jid;
    }

    /**
     * Returns whether this exception represents a fatal error.
     * <p>
     * All ADV validation failures are fatal because proceeding with an unverified
     * device would compromise the security of the end-to-end encryption.
     *
     * @return {@code true}
     */
    @Override
    public boolean isFatal() {
        return true;
    }

    /**
     * Exception thrown when the prekey response does not contain the required device-identity node.
     * <p>
     * When requesting prekeys from another device, the response must include device identity
     * information for ADV validation. This exception indicates the response was malformed
     * or the sender doesn't support ADV.
     *
     * <h2>Expected Response Structure</h2>
     * <pre>
     * &lt;iq type="result"&gt;
     *   &lt;keys&gt;
     *     &lt;device-identity&gt;...ADV data...&lt;/device-identity&gt;
     *     ...prekey data...
     *   &lt;/keys&gt;
     * &lt;/iq&gt;
     * </pre>
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
     * Exception thrown when the device-identity node is present but contains no data.
     * <p>
     * The device identity structure exists but is empty or invalid, indicating a
     * protocol error or corrupted response from the sender.
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
     * Exception thrown when the account signature verification fails.
     * <p>
     * The account signature proves the device was authorized by the account owner.
     * It is created by signing the device identity details concatenated with
     * the device's public key using the account's identity key.
     *
     * <h2>Verification Process</h2>
     * <ol>
     *   <li>Extract the account signature from the device identity</li>
     *   <li>Reconstruct the signed data (identity details + device public key)</li>
     *   <li>Verify the signature using the account's identity public key</li>
     * </ol>
     *
     * <h2>Failure Implications</h2>
     * <ul>
     *   <li>The device was not legitimately registered by the account owner</li>
     *   <li>The identity data was tampered with after signing</li>
     *   <li>A potential man-in-the-middle attack is in progress</li>
     * </ul>
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
     * Exception thrown when the device signature verification fails.
     * <p>
     * The device signature proves the companion device possesses the private key
     * corresponding to its claimed public key. It is created by signing the
     * device identity details concatenated with the device's public key and
     * the account signature.
     *
     * <h2>Verification Process</h2>
     * <ol>
     *   <li>Extract the device signature from the device identity</li>
     *   <li>Reconstruct the signed data (identity details + device key + account signature)</li>
     *   <li>Verify the signature using the device's public key</li>
     * </ol>
     *
     * <h2>Failure Implications</h2>
     * <ul>
     *   <li>The device doesn't possess the claimed private key</li>
     *   <li>The signature was forged or corrupted</li>
     *   <li>A potential impersonation attack is in progress</li>
     * </ul>
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
     * Exception thrown when the HMAC validation of the device identity fails.
     * <p>
     * HMAC (Hash-based Message Authentication Code) is used to verify the
     * integrity of the device identity data. The HMAC is computed over the
     * serialized device identity using a derived key.
     *
     * <h2>HMAC Computation</h2>
     * <ol>
     *   <li>Derive the HMAC key from shared secrets</li>
     *   <li>Compute HMAC-SHA256 over the serialized device identity</li>
     *   <li>Compare with the HMAC included in the data</li>
     * </ol>
     *
     * <h2>Failure Implications</h2>
     * The device identity data was corrupted or tampered with during transmission.
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
     * Exception thrown when a cryptographic operation fails during ADV validation.
     * <p>
     * This is a catch-all for low-level cryptographic errors such as invalid key
     * formats, unsupported algorithms, or other security provider failures that
     * prevented validation from completing.
     *
     * <h2>Possible Causes</h2>
     * <ul>
     *   <li>Invalid key format or encoding</li>
     *   <li>Unsupported cryptographic algorithm</li>
     *   <li>JCE provider not available or misconfigured</li>
     *   <li>Key size restrictions (e.g., export restrictions)</li>
     * </ul>
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
