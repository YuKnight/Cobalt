
package com.github.auties00.cobalt.client;

import com.github.auties00.libsignal.key.SignalIdentityKeyPair;
import com.github.auties00.libsignal.key.SignalIdentityPrivateKey;
import com.github.auties00.libsignal.key.SignalIdentityPublicKey;

import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * A portable representation of a WhatsApp mobile account's long-lived
 * credentials, encoded as six comma-separated fields.
 *
 * <p>The six-parts format is a de-facto interchange format used by the
 * WhatsApp reverse-engineering community to transport the minimal set of
 * credentials needed to resume a mobile session: the phone number, the
 * Noise protocol key pair (used during handshake with the WhatsApp server)
 * and the Signal identity key pair (used for end-to-end encryption),
 * together with an identity identifier. Cobalt can load a store from these
 * keys via {@link WhatsAppClientBuilder.Client#loadConnection(WhatsAppClientSixPartsKeys)}
 * and emit them back via {@link #toString()} for export.
 *
 * <p>The serialized form is:
 * <ol>
 *     <li>Phone number (with optional {@code +} prefix)</li>
 *     <li>Noise public key (Base64 encoded)</li>
 *     <li>Noise private key (Base64 encoded)</li>
 *     <li>Signal identity public key (Base64 encoded)</li>
 *     <li>Signal identity private key (Base64 encoded)</li>
 *     <li>Identity ID (Base64 encoded)</li>
 * </ol>
 *
 * <p>Parsing is tolerant of surrounding whitespace and embedded newlines so
 * that multi-line pasted input is handled correctly.
 *
 * @see WhatsAppClientBuilder
 * @see SignalIdentityKeyPair
 */
public final class WhatsAppClientSixPartsKeys {
    /**
     * The account's phone number in E.164 form (without the leading
     * {@code +}), stored as an unsigned long.
     */
    private final long phoneNumber;
    /**
     * The Noise protocol key pair used during the WhatsApp server
     * handshake to establish the encrypted transport.
     */
    private final SignalIdentityKeyPair noiseKeyPair;
    /**
     * The Signal identity key pair used for end-to-end encrypted
     * messaging with contacts.
     */
    private final SignalIdentityKeyPair identityKeyPair;
    /**
     * The opaque identity identifier issued by WhatsApp during account
     * registration.
     */
    private final byte[] identityId;

    /**
     * Constructs a new credentials record from its four logical components.
     *
     * @param phoneNumber     the account phone number, stored as an
     *                        unsigned long
     * @param noiseKeyPair    the Noise protocol key pair
     * @param identityKeyPair the Signal identity key pair
     * @param identityId      the opaque identity identifier
     */
    public WhatsAppClientSixPartsKeys(long phoneNumber, SignalIdentityKeyPair noiseKeyPair, SignalIdentityKeyPair identityKeyPair, byte[] identityId) {
        this.phoneNumber = phoneNumber;
        this.noiseKeyPair = noiseKeyPair;
        this.identityKeyPair = identityKeyPair;
        this.identityId = identityId;
    }

    /**
     * Parses a six-parts credentials string into a
     * {@code WhatsAppClientSixPartsKeys}.
     *
     * <p>The input is expected to contain exactly six comma-separated
     * fields (phone number, Noise public, Noise private, identity public,
     * identity private, identity ID). Surrounding whitespace and any
     * embedded newlines are stripped before parsing so multi-line pasted
     * input is handled correctly.
     *
     * @param sixParts the comma-separated credentials string
     * @return the parsed credentials record
     * @throws NullPointerException     if {@code sixParts} is {@code null}
     * @throws IllegalArgumentException if the input does not decode to
     *                                  exactly six parts or the phone
     *                                  number is malformed
     */
    public static WhatsAppClientSixPartsKeys of(String sixParts) {
        Objects.requireNonNull(sixParts, "Invalid six parts");
        var parts = sixParts.trim()
                .replaceAll("\n", "")
                .split(",", 6);
        if (parts.length != 6) {
            throw new IllegalArgumentException("Malformed six parts: " + sixParts);
        }
        var phoneNumber = parsePhoneNumber(parts);
        var noisePublicKey = SignalIdentityPublicKey.ofDirect(Base64.getDecoder().decode(parts[1]));
        var noisePrivateKey = SignalIdentityPrivateKey.ofDirect(Base64.getDecoder().decode(parts[2]));
        var identityPublicKey = SignalIdentityPublicKey.ofDirect(Base64.getDecoder().decode(parts[3]));
        var identityPrivateKey = SignalIdentityPrivateKey.ofDirect(Base64.getDecoder().decode(parts[4]));
        var identityId = Base64.getDecoder().decode(parts[5]);
        var noiseKeyPair = new SignalIdentityKeyPair(noisePublicKey, noisePrivateKey);
        var identityKeyPair = new SignalIdentityKeyPair(identityPublicKey, identityPrivateKey);
        return new WhatsAppClientSixPartsKeys(phoneNumber, noiseKeyPair, identityKeyPair, identityId);
    }

    /**
     * Parses the phone number from the first element of the split six-parts
     * array, honouring an optional {@code +} country-code prefix.
     *
     * @param parts the already-split six-parts array
     * @return the phone number as an unsigned long
     * @throws IllegalArgumentException if the phone number field is empty
     *                                  or does not parse as an unsigned
     *                                  decimal integer
     */
    private static long parsePhoneNumber(String[] parts) {
        var rawPhoneNumber = parts[0];
        if(rawPhoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Invalid phone number: " + rawPhoneNumber);
        }
        try {
            return Long.parseUnsignedLong(rawPhoneNumber, rawPhoneNumber.charAt(0) == '+' ? 1 : 0, rawPhoneNumber.length(), 10);
        }catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid phone number: " + rawPhoneNumber);
        }
    }

    /**
     * Serialises this credentials record to its canonical six-parts
     * string form.
     *
     * <p>The returned string is round-trippable through {@link #of(String)}
     * and uses the standard Base64 alphabet (no padding is stripped) for
     * every cryptographic field.
     *
     * @return the six-parts string representation
     */
    @Override
    public String toString() {
        return String.valueOf(phoneNumber) +
                ',' +
                Base64.getEncoder().encodeToString(noiseKeyPair.publicKey().toEncodedPoint()) +
                ',' +
                Base64.getEncoder().encodeToString(noiseKeyPair.privateKey().toEncodedPoint()) +
                ',' +
                Base64.getEncoder().encodeToString(identityKeyPair.publicKey().toEncodedPoint()) +
                ',' +
                Base64.getEncoder().encodeToString(identityKeyPair.privateKey().toEncodedPoint()) +
                ',' +
                Base64.getEncoder().encodeToString(identityId);
    }

    /**
     * Returns the phone number associated with these credentials.
     *
     * @return the phone number as an unsigned {@code long}
     */
    public long phoneNumber() {
        return phoneNumber;
    }

    /**
     * Returns the Noise protocol key pair used to establish the encrypted
     * transport with the WhatsApp servers.
     *
     * @return the Noise handshake key pair
     */
    public SignalIdentityKeyPair noiseKeyPair() {
        return noiseKeyPair;
    }

    /**
     * Returns the Signal identity key pair used for end-to-end encrypted
     * messaging.
     *
     * <p>This is the long-term key material that identifies the account
     * across Signal sessions and feeds into the Double Ratchet algorithm
     * used by WhatsApp.
     *
     * @return the Signal identity key pair
     */
    public SignalIdentityKeyPair identityKeyPair() {
        return identityKeyPair;
    }

    /**
     * Returns the opaque identity identifier bound to this account.
     *
     * <p>The returned array is the internal backing storage; callers must
     * not mutate it.
     *
     * @return the identity ID
     */
    public byte[] identityId() {
        return identityId;
    }

    /**
     * Compares this credentials record to another object for structural
     * equality.
     *
     * <p>Two records are equal when their phone number, Noise key pair,
     * Signal identity key pair, and identity ID all match.
     *
     * @param o the object to compare with
     * @return {@code true} if the records are equal
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof WhatsAppClientSixPartsKeys that
                && Objects.equals(phoneNumber, that.phoneNumber) &&
                Objects.equals(noiseKeyPair, that.noiseKeyPair) &&
                Objects.equals(identityKeyPair, that.identityKeyPair) &&
                Objects.deepEquals(identityId, that.identityId);
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, noiseKeyPair, identityKeyPair, Arrays.hashCode(identityId));
    }
}