package com.github.auties00.cobalt.calls2.platform;

import com.github.auties00.cobalt.exception.WhatsAppCallException;

import javax.crypto.KDF;
import javax.crypto.Mac;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * Supplies the HMAC-SHA256, HKDF-SHA256, and cryptographically-strong random-byte primitives the call
 * engine installs as native callbacks, reimplemented entirely on top of the Java Cryptography
 * Architecture except for entropy.
 *
 * <p>In the WhatsApp Web build the voip engine is an Emscripten WebAssembly module with BoringSSL
 * statically linked inside it; the engine registers three crypto callbacks (an HMAC-SHA256, an
 * HKDF-SHA256 extract-and-expand, and a standalone HKDF wrapper) that are computed by that in-module
 * BoringSSL, and it reaches out to the host for one thing only: random bytes. Cobalt runs no
 * WebAssembly, so the boundary inverts: the random-byte primitive that was the single host import is
 * served from {@link SecureRandom}, and the HMAC and HKDF primitives that were native in BoringSSL are
 * served from the platform JCA providers ({@code Mac("HmacSHA256")} and the JDK
 * {@code KDF("HKDF-SHA256")}). The byte-for-byte results are identical: HMAC-SHA256 and HKDF-SHA256 are
 * standardised in RFC 2104 and RFC 5869, and BoringSSL implements exactly those standards.
 *
 * <p>This class is the host-side reimplementation of the engine's crypto callbacks for the case where
 * the consumer of those primitives is itself reimplemented in Java (the SFrame key provider and the
 * data-channel certificate-fingerprint binding both consume HKDF/HMAC over the end-to-end call key). A
 * consumer that stays native and demands the engine's exact callback function pointers does not route
 * through this class; it is bound directly to its native library. Every method is stateless and
 * thread-safe: each call constructs its own {@link Mac} or {@link KDF} instance, and the shared
 * {@link SecureRandom} is safe for concurrent use.
 *
 * @implNote This implementation reimplements {@code native_hmac_sha256_cb} (fn1243),
 * {@code native_hkdf_sha256_cb} (fn1244), and {@code native_hkdf_extract_and_expand} (fn1245) from
 * {@code platforms/wasm/crypto/VoipCryptoNative.cpp} of the wa-voip engine (WASM module
 * {@code ff-tScznZ8P}). Those three callbacks delegate to the statically-linked BoringSSL HMAC (fn1257)
 * and HKDF (fn1256) over {@code EVP_sha256}; the in-module HMAC pads the key with the standard ipad
 * {@code 0x36} and opad {@code 0x5c} (fn1258) and HKDF runs RFC 5869 extract-then-expand. Here every one
 * of those primitives is the corresponding JCA algorithm, NOT a native binding: HMAC is
 * {@code Mac("HmacSHA256")}, the HKDF steps are the JDK 25 {@code KDF("HKDF-SHA256")} extract and expand
 * operations, and the digest output length is the SHA-256 size. The single primitive that remains a host
 * responsibility in the native engine, entropy ({@code get_random_bytes_js} / {@code getentropy}, the
 * generator of the 32-byte raw end-to-end call key in fn10890), is {@link SecureRandom} here. The
 * recovered guards are reproduced: {@link #hmacSha256(byte[], byte[])} asserts a {@value #SHA256_LENGTH}
 * -byte output to match fn1243's {@code 0x20} length check, and the HKDF expand rejects an output longer
 * than {@value #MAX_HKDF_BLOCKS} hash blocks to match fn1256's {@code N < 0x100} bound.
 */
public final class VoipCryptoNative {
    /**
     * Holds the output length, in bytes, of HMAC-SHA256 and of one HKDF-SHA256 hash block.
     *
     * <p>This is the SHA-256 digest size. The native HMAC callback asserts its output buffer is exactly
     * this length ({@code 0x20} in fn1243), and HKDF uses it as the hash length when computing the number
     * of expansion blocks.
     */
    public static final int SHA256_LENGTH = 32;

    /**
     * Holds the maximum number of hash blocks an HKDF-SHA256 expansion may produce.
     *
     * <p>RFC 5869 caps the expansion at {@code 255} blocks because the block counter is a single octet;
     * the native HKDF core (fn1256) enforces the equivalent {@code N < 0x100} guard. With a
     * {@value #SHA256_LENGTH}-byte hash this bounds any single derivation to {@code 255 * 32 = 8160}
     * bytes of output keying material.
     */
    public static final int MAX_HKDF_BLOCKS = 255;

    /**
     * Holds the JCA algorithm name of the HMAC primitive.
     */
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Holds the JCA algorithm name of the HKDF primitive.
     */
    private static final String HKDF_ALGORITHM = "HKDF-SHA256";

    /**
     * Holds the key algorithm name used to wrap an extracted pseudorandom key for the expand-only step.
     *
     * <p>The JDK {@code KDF("HKDF-SHA256")} expand operation takes the pseudorandom key as a
     * {@link javax.crypto.SecretKey}; the algorithm string on the wrapping {@link SecretKeySpec} is
     * opaque to HKDF and is set to {@code "HKDF"} to mirror {@link #hkdfExpand(byte[], byte[], int)} in
     * the existing SFrame transform.
     */
    private static final String HKDF_KEY_ALGORITHM = "HKDF";

    /**
     * Holds the shared cryptographically-strong random source backing {@link #randomBytes(int)} and
     * {@link #randomBytes(byte[])}.
     *
     * <p>{@link SecureRandom} is thread-safe, so a single seeded instance serves every caller; this is
     * the host replacement for the engine's only crypto host import.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Prevents instantiation of this stateless primitive holder.
     */
    private VoipCryptoNative() {
        throw new AssertionError("VoipCryptoNative is not instantiable");
    }

    /**
     * Computes the HMAC-SHA256 of a message under a key.
     *
     * <p>The result is always {@value #SHA256_LENGTH} bytes, the SHA-256 output size; the native
     * callback this replaces asserts that length on its output buffer. The key may be any length: HMAC
     * itself hashes a key longer than the SHA-256 block size and zero-pads a shorter one, so no length
     * constraint is imposed here.
     *
     * @param key  the secret key, of any length
     * @param data the message to authenticate
     * @return the {@value #SHA256_LENGTH}-byte HMAC-SHA256 tag
     * @throws NullPointerException       if {@code key} or {@code data} is {@code null}
     * @throws WhatsAppCallException.Srtp if the platform cannot compute HMAC-SHA256
     */
    public static byte[] hmacSha256(byte[] key, byte[] data) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(data, "data cannot be null");
        try {
            var mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            var tag = mac.doFinal(data);
            if (tag.length != SHA256_LENGTH) {
                throw new WhatsAppCallException.Srtp(
                        "HMAC-SHA256 produced " + tag.length + " bytes, expected " + SHA256_LENGTH);
            }
            return tag;
        } catch (GeneralSecurityException e) {
            throw new WhatsAppCallException.Srtp("Cannot compute HMAC-SHA256", e);
        }
    }

    /**
     * Performs a full HKDF-SHA256 extract-then-expand and returns the output keying material.
     *
     * <p>This is RFC 5869 HKDF: a pseudorandom key is extracted from the input keying material and salt,
     * then expanded with the info string into {@code length} bytes of output. A {@code null} salt is
     * treated as a zero-length salt, which RFC 5869 defines as a string of {@value #SHA256_LENGTH} zero
     * bytes; the platform provider applies that rule.
     *
     * @param ikm    the input keying material
     * @param salt   the optional salt, or {@code null} for the RFC 5869 default salt
     * @param info   the context and application-specific info
     * @param length the number of output bytes, in {@code [0, MAX_HKDF_BLOCKS * SHA256_LENGTH]}
     * @return the {@code length}-byte output keying material
     * @throws NullPointerException       if {@code ikm} or {@code info} is {@code null}
     * @throws IllegalArgumentException   if {@code length} is negative or exceeds
     *                                    {@value #MAX_HKDF_BLOCKS} hash blocks
     * @throws WhatsAppCallException.Srtp if the platform cannot compute HKDF-SHA256
     */
    public static byte[] hkdfSha256(byte[] ikm, byte[] salt, byte[] info, int length) {
        Objects.requireNonNull(ikm, "ikm cannot be null");
        Objects.requireNonNull(info, "info cannot be null");
        requireValidOutputLength(length);
        try {
            var kdf = KDF.getInstance(HKDF_ALGORITHM);
            var extract = HKDFParameterSpec.ofExtract().addIKM(ikm);
            if (salt != null) {
                extract.addSalt(salt);
            }
            return kdf.deriveData(extract.thenExpand(info, length));
        } catch (GeneralSecurityException e) {
            throw new WhatsAppCallException.Srtp("Cannot compute HKDF-SHA256", e);
        }
    }

    /**
     * Performs only the HKDF-SHA256 extract step and returns the pseudorandom key.
     *
     * <p>The extract step is {@code PRK = HMAC-SHA256(salt, ikm)}, always {@value #SHA256_LENGTH} bytes.
     * A {@code null} salt is the RFC 5869 default salt of {@value #SHA256_LENGTH} zero bytes. Pair this
     * with {@link #hkdfExpand(byte[], byte[], int)} when a single pseudorandom key feeds several
     * independent expansions.
     *
     * @param ikm  the input keying material
     * @param salt the optional salt, or {@code null} for the RFC 5869 default salt
     * @return the {@value #SHA256_LENGTH}-byte pseudorandom key
     * @throws NullPointerException       if {@code ikm} is {@code null}
     * @throws WhatsAppCallException.Srtp if the platform cannot compute the HKDF extract step
     */
    public static byte[] hkdfExtract(byte[] ikm, byte[] salt) {
        Objects.requireNonNull(ikm, "ikm cannot be null");
        try {
            var kdf = KDF.getInstance(HKDF_ALGORITHM);
            var extract = HKDFParameterSpec.ofExtract().addIKM(ikm);
            if (salt != null) {
                extract.addSalt(salt);
            }
            return kdf.deriveData(extract.extractOnly());
        } catch (GeneralSecurityException e) {
            throw new WhatsAppCallException.Srtp("Cannot compute HKDF-SHA256 extract", e);
        }
    }

    /**
     * Performs only the HKDF-SHA256 expand step and returns the output keying material.
     *
     * <p>The expand step turns a pseudorandom key (such as one from {@link #hkdfExtract(byte[], byte[])})
     * and an info string into {@code length} bytes of output through the RFC 5869 block ladder
     * {@code T(i) = HMAC(prk, T(i-1) || info || i)}.
     *
     * @param prk    the {@value #SHA256_LENGTH}-byte pseudorandom key from the extract step
     * @param info   the context and application-specific info
     * @param length the number of output bytes, in {@code [0, MAX_HKDF_BLOCKS * SHA256_LENGTH]}
     * @return the {@code length}-byte output keying material
     * @throws NullPointerException       if {@code prk} or {@code info} is {@code null}
     * @throws IllegalArgumentException   if {@code length} is negative or exceeds
     *                                    {@value #MAX_HKDF_BLOCKS} hash blocks
     * @throws WhatsAppCallException.Srtp if the platform cannot compute the HKDF expand step
     */
    public static byte[] hkdfExpand(byte[] prk, byte[] info, int length) {
        Objects.requireNonNull(prk, "prk cannot be null");
        Objects.requireNonNull(info, "info cannot be null");
        requireValidOutputLength(length);
        try {
            var kdf = KDF.getInstance(HKDF_ALGORITHM);
            var params = HKDFParameterSpec.expandOnly(new SecretKeySpec(prk, HKDF_KEY_ALGORITHM), info, length);
            return kdf.deriveData(params);
        } catch (GeneralSecurityException e) {
            throw new WhatsAppCallException.Srtp("Cannot compute HKDF-SHA256 expand", e);
        }
    }

    /**
     * Returns a freshly generated array of cryptographically-strong random bytes.
     *
     * <p>This is the host replacement for the engine's only crypto host import, the random-byte source
     * that mints the 32-byte raw end-to-end call key. The most common length is {@value #SHA256_LENGTH}
     * for that key.
     *
     * @param length the number of random bytes to generate
     * @return a new array of {@code length} cryptographically-strong random bytes
     * @throws IllegalArgumentException if {@code length} is negative
     */
    public static byte[] randomBytes(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length cannot be negative, got " + length);
        }
        var out = new byte[length];
        SECURE_RANDOM.nextBytes(out);
        return out;
    }

    /**
     * Fills a caller-supplied buffer with cryptographically-strong random bytes.
     *
     * <p>This is the array-filling form of {@link #randomBytes(int)}, mirroring the native callback
     * signature that writes entropy into an engine-owned output buffer rather than allocating one.
     *
     * @param out the buffer to fill in place
     * @throws NullPointerException if {@code out} is {@code null}
     */
    public static void randomBytes(byte[] out) {
        Objects.requireNonNull(out, "out cannot be null");
        SECURE_RANDOM.nextBytes(out);
    }

    /**
     * Validates that an HKDF output length is non-negative and within the RFC 5869 block bound.
     *
     * @param length the requested output length in bytes
     * @throws IllegalArgumentException if {@code length} is negative or would require more than
     *                                  {@value #MAX_HKDF_BLOCKS} hash blocks
     */
    private static void requireValidOutputLength(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("HKDF output length cannot be negative, got " + length);
        }
        var maxLength = MAX_HKDF_BLOCKS * SHA256_LENGTH;
        if (length > maxLength) {
            throw new IllegalArgumentException(
                    "HKDF output length " + length + " exceeds the maximum of " + maxLength + " bytes");
        }
    }
}
