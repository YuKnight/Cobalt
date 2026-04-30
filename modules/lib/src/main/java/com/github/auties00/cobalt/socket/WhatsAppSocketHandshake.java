package com.github.auties00.cobalt.socket;

import com.github.auties00.cobalt.util.DataUtils;
import com.github.auties00.cobalt.util.GcmUtils;

import javax.crypto.*;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Cryptographic state machine for the WhatsApp Noise XX key exchange.
 *
 * <p>The handshake chains a SHA-256 hash, an HKDF salt, a symmetric
 * cipher key and a 64-bit nonce counter. Each {@link #cipher(byte[], boolean)}
 * call encrypts or decrypts a payload, mixes the resulting bytes into
 * the running hash and advances the nonce. {@link #mixIntoKey(byte[])}
 * folds new key material into the salt and cipher key whenever a Diffie
 * Hellman shared secret has been computed.
 *
 * <p>{@link #finish()} derives the final 64 bytes of key material (32
 * bytes write key followed by 32 bytes read key) once both sides have
 * exchanged their {@code ClientFinish}/{@code ServerHello} messages.
 *
 * <p>Instances are {@link AutoCloseable} and the WhatsApp socket client
 * always uses them inside a try-with-resources block so that the AES
 * key is destroyed promptly when the handshake completes or fails.
 *
 * @implNote Adapts WA Web's {@code WANoiseHandshake.NoiseHandshake} class.
 */
final class WhatsAppSocketHandshake implements AutoCloseable {
    /**
     * Empty IKM passed to HKDF when {@link #finish()} expands the final
     * read and write keys, mirroring WA Web's {@code new Uint8Array(0)}.
     */
    private static final byte[] FINISH_KEY = DataUtils.EMPTY_BYTE_ARRAY;

    /**
     * Noise protocol name padded to exactly 32 bytes so it can be used
     * verbatim as the initial hash, salt and cipher key without an
     * additional SHA-256 reduction.
     */
    private static final byte[] NOISE_PROTOCOL = "Noise_XX_25519_AESGCM_SHA256\0\0\0\0".getBytes(StandardCharsets.UTF_8);

    /**
     * HKDF-SHA256 instance reused for every salt rotation.
     */
    private final KDF kdf;

    /**
     * SHA-256 digest reused to chain {@code SHA-256(hash || data)} into
     * the running handshake hash.
     */
    private final MessageDigest hashDigest;

    /**
     * AES/GCM/NoPadding cipher reused to encrypt and decrypt every
     * handshake payload.
     */
    private final Cipher cipher;

    /**
     * Running handshake hash; starts at {@link #NOISE_PROTOCOL} and is
     * updated on every {@code authenticate} step.
     */
    private byte[] hash;

    /**
     * Current HKDF salt; rotated by {@link #mixIntoKey(byte[])} whenever
     * fresh shared key material is folded in.
     */
    private SecretKeySpec salt;

    /**
     * Current symmetric AES key used as AAD-bearing payload cipher.
     */
    private SecretKeySpec cryptoKey;

    /**
     * AES-GCM nonce counter; reset to zero whenever the cipher key
     * rotates.
     */
    private long counter;

    /**
     * Initializes the handshake state and mixes in the protocol prologue.
     *
     * <p>Merges WA Web's {@code constructor} and {@code start} steps. The
     * hash, salt and cipher key all begin as the 32-byte Noise protocol
     * name, then the prologue (the WhatsApp version header) is folded
     * into the running hash via {@link #updateHash(byte[])}.
     *
     * @param prologue the protocol prologue bytes that identify the
     *                 client variant (web or mobile)
     * @throws NoSuchAlgorithmException if HKDF-SHA256 or SHA-256 is
     *         unavailable on this JDK
     * @throws NoSuchPaddingException   if AES/GCM/NoPadding is
     *         unavailable on this JDK
     */
    WhatsAppSocketHandshake(byte[] prologue) throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.kdf = KDF.getInstance("HKDF-SHA256");
        this.hashDigest = MessageDigest.getInstance("SHA-256");
        this.cipher = Cipher.getInstance("AES/GCM/NoPadding");
        this.hash = NOISE_PROTOCOL;
        this.salt = new SecretKeySpec(NOISE_PROTOCOL, "AES");
        this.cryptoKey = new SecretKeySpec(NOISE_PROTOCOL, 0, 32, "AES");
        this.counter = 0;
        updateHash(prologue);
    }

    /**
     * Folds {@code data} into the running hash by computing
     * {@code SHA-256(hash || data)} and storing the digest as the new
     * hash.
     *
     * @param data the bytes to mix into the running hash
     */
    public void updateHash(byte[] data) {
        hashDigest.update(hash);
        hashDigest.update(data);
        this.hash = hashDigest.digest();
    }

    /**
     * Encrypts or decrypts a payload with the current AES key, using the
     * running hash as AES-GCM AAD and the monotonic nonce counter.
     *
     * <p>Encryption mixes the produced ciphertext into the running hash;
     * decryption mixes the input ciphertext.
     *
     * @implNote The 12-byte nonce is built as four zero bytes followed
     *     by the big-endian 64-bit counter; the counter is incremented
     *     on every call so consecutive payloads never reuse a nonce.
     * @param text    plaintext when {@code encrypt} is {@code true},
     *                ciphertext when {@code false}
     * @param encrypt {@code true} to encrypt, {@code false} to decrypt
     * @return the ciphertext or plaintext respectively
     * @throws IllegalBlockSizeException          if the input length is invalid
     * @throws BadPaddingException                if the GCM authentication tag fails
     * @throws InvalidAlgorithmParameterException if the nonce parameters are invalid
     * @throws InvalidKeyException                if the cipher key is invalid
     */
    byte[] cipher(byte[] text, boolean encrypt) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        cipher.init(
                encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,
                cryptoKey,
                GcmUtils.createNonce(counter++)
        );
        cipher.updateAAD(hash);
        var result = cipher.doFinal(text);
        updateHash(encrypt ? result : text);
        return result;
    }

    /**
     * Derives the final 64 bytes of key material from the current salt
     * via HKDF with an empty IKM.
     *
     * <p>The first 32 bytes are the write key and the remaining 32
     * bytes are the read key. WA Web wraps the same material in a
     * {@code NoiseSocket}; Cobalt returns the raw bytes so the caller
     * can install them in its {@link Cipher} pair directly.
     *
     * @return the concatenated write and read key material
     * @throws GeneralSecurityException if HKDF expansion fails
     */
    byte[] finish() throws GeneralSecurityException {
        var params = HKDFParameterSpec.ofExtract()
                .addSalt(salt)
                .addIKM(FINISH_KEY)
                .thenExpand(null, 64);
        return kdf.deriveData(params);
    }

    /**
     * Folds new key material into the handshake state.
     *
     * <p>HKDF-Extract-and-Expand under the current salt produces 64
     * bytes. The first 32 bytes become the new salt, the remaining 32
     * bytes become the new cipher key, and the nonce counter is reset
     * to zero so the rotated key starts at nonce 0.
     *
     * @param bytes the new key material, typically a Curve25519 shared
     *              secret produced during the handshake
     * @throws GeneralSecurityException if HKDF expansion fails
     */
    void mixIntoKey(byte[] bytes) throws GeneralSecurityException {
        var params = HKDFParameterSpec.ofExtract()
                .addSalt(salt)
                .addIKM(bytes)
                .thenExpand(null, 64);
        var expanded = kdf.deriveData(params);
        this.salt = new SecretKeySpec(expanded, 0, 32, "AES");
        this.cryptoKey = new SecretKeySpec(expanded, 32, 32, "AES");
        this.counter = 0;
    }

    /**
     * Releases the handshake's key material.
     *
     * <p>Drops the hash and salt references, attempts to destroy the
     * cipher key via {@link SecretKeySpec#destroy()} (silently ignoring
     * implementations that do not support destruction) and resets the
     * nonce counter. WA Web relies on the browser's garbage collector
     * for the same effect; Cobalt does it eagerly so secrets do not
     * linger in heap memory for longer than the handshake.
     */
    @Override
    public void close() {
        this.hash = null;
        this.salt = null;
        try {
            cryptoKey.destroy();
        } catch (DestroyFailedException _) {

        }
        this.cryptoKey = null;
        this.counter = 0;
    }
}
