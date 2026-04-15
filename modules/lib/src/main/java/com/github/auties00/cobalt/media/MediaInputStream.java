package com.github.auties00.cobalt.media;

import com.github.auties00.cobalt.exception.WhatsAppMediaException;

import javax.crypto.Cipher;
import javax.crypto.KDF;
import javax.crypto.Mac;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Objects;

/**
 * Base input stream for WhatsApp media encryption and decryption operations.
 *
 * <p>Provides shared cryptographic primitives used by both the upload
 * ({@link MediaUploadInputStream}) and download ({@link MediaDownloadInputStream})
 * media processing streams, including HKDF key derivation, AES-CBC cipher
 * creation, HMAC-SHA256 computation, and SHA-256 hashing.
 *
 * <p>The key derivation follows WhatsApp's media crypto protocol: a 32-byte
 * media key is expanded via HKDF-SHA256 (with no salt) into 112 bytes, then
 * sliced into four subkeys: a 16-byte IV, a 32-byte AES cipher key, a 32-byte
 * HMAC key, and a 32-byte reference key.
 *
 * @implNote WAMediaCrypto
 */
abstract class MediaInputStream extends InputStream {
    /**
     * The default buffer size in bytes used for streaming read operations.
     *
     * @implNote WAMediaCrypto (internal buffer sizing)
     */
    static final int BUFFER_LENGTH = 8192;

    /**
     * The truncated HMAC length in bytes appended to encrypted media.
     *
     * <p>WhatsApp computes a full 32-byte HMAC-SHA256 over the IV concatenated
     * with the ciphertext, but only the first 10 bytes are appended to the
     * encrypted payload and transmitted.
     *
     * @implNote WAMediaCrypto.HMAC_LENGTH (v = 10)
     */
    static final int MAC_LENGTH = 10;

    /**
     * The total expanded key material size in bytes produced by HKDF.
     *
     * <p>The 112 bytes are partitioned as:
     * <ul>
     *   <li>bytes 0-15: initialization vector (IV)</li>
     *   <li>bytes 16-47: AES-CBC cipher key</li>
     *   <li>bytes 48-79: HMAC-SHA256 key</li>
     *   <li>bytes 80-111: reference key (used for media previews)</li>
     * </ul>
     *
     * @implNote WAMediaCrypto.computeMediaKeys (p function, expand to 112 bytes)
     */
    static final int EXPANDED_SIZE = 112;

    /**
     * The symmetric key length in bytes for both the AES cipher key and the
     * HMAC key, each occupying 32 bytes within the expanded key material.
     *
     * @implNote WAMediaCrypto.computeMediaKeys (cipherKey: 32 bytes, hmacKey: 32 bytes)
     */
    static final int KEY_LENGTH = 32;

    /**
     * The initialization vector length in bytes for AES-CBC encryption.
     *
     * @implNote WAMediaCrypto.IV_LENGTH (b = 16)
     */
    static final int IV_LENGTH = 16;

    /**
     * The underlying raw input stream providing the source data.
     *
     * @implNote WAMediaCrypto (plaintext source for encryption, ciphertext source for decryption)
     */
    final InputStream rawInputStream;

    /**
     * Constructs a new media input stream wrapping the specified raw input stream.
     *
     * @implNote WAMediaCrypto
     * @param rawInputStream the underlying input stream, must not be {@code null}
     * @throws NullPointerException if {@code rawInputStream} is {@code null}
     */
    MediaInputStream(InputStream rawInputStream) {
        this.rawInputStream = Objects.requireNonNull(rawInputStream, "rawInputStream must not be null");
    }

    /**
     * Derives the media key material from a raw media key and a key name
     * using HKDF-SHA256.
     *
     * <p>Performs HKDF extract-then-expand with no salt (defaults to 32 zero
     * bytes per RFC 5869) and the UTF-8 encoding of the key name as the info
     * parameter. The output is {@value #EXPANDED_SIZE} bytes that must be
     * sliced into the IV, cipher key, HMAC key, and reference key.
     *
     * @implNote WAMediaCrypto.computeMediaKeys calls WACryptoHkdf.extractAndExpand(mediaKey, info, 112)
     * @param mediaKey     the 32-byte raw media key
     * @param mediaKeyName the HKDF info string identifying the media type,
     *                     for example {@code "WhatsApp Image Keys"}
     * @return the {@value #EXPANDED_SIZE}-byte expanded key material
     * @throws WhatsAppMediaException if the HKDF derivation fails
     */
    byte[] deriveMediaKeyData(byte[] mediaKey, String mediaKeyName) throws WhatsAppMediaException {
        try {
            var hkdf = KDF.getInstance("HKDF-SHA256");
            var params = HKDFParameterSpec.ofExtract()
                    .addIKM(mediaKey)
                    .thenExpand(mediaKeyName.getBytes(StandardCharsets.UTF_8), EXPANDED_SIZE);
            return hkdf.deriveData(params);
        } catch (GeneralSecurityException e) {
            throw new WhatsAppMediaException("Cannot derive media key data", e);
        }
    }

    /**
     * Creates a new SHA-256 message digest instance.
     *
     * @implNote WAMediaCrypto (SHA-256 used for fileSha256 and fileEncSha256)
     * @return a fresh {@link MessageDigest} for SHA-256
     * @throws WhatsAppMediaException if the SHA-256 algorithm is not available
     */
    MessageDigest newHash() throws WhatsAppMediaException {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (GeneralSecurityException exception) {
            throw new WhatsAppMediaException("Cannot create new hash", exception);
        }
    }

    /**
     * Creates and initializes a new AES-CBC cipher with PKCS5 padding.
     *
     * @implNote WAMediaCrypto.encryptAndHmac / WAMediaCrypto.hmacAndDecrypt
     *           (WACryptoAesCbc.AesCbcStream for encrypt, WACryptoAesCbc.aesCbcDecrypt for decrypt)
     * @param mode the cipher mode, either {@link Cipher#ENCRYPT_MODE} or
     *             {@link Cipher#DECRYPT_MODE}
     * @param key  the AES secret key specification
     * @param iv   the initialization vector
     * @return the initialized {@link Cipher}
     * @throws WhatsAppMediaException if cipher creation or initialization fails
     */
    Cipher newCipher(int mode, SecretKeySpec key, IvParameterSpec iv) throws WhatsAppMediaException {
        try {
            var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(mode, key, iv);
            return cipher;
        } catch (GeneralSecurityException exception) {
            throw new WhatsAppMediaException("Cannot create new cipher", exception);
        }
    }

    /**
     * Creates and initializes a new HMAC-SHA256 instance with the given key.
     *
     * @implNote WAMediaCrypto.encryptAndHmac / WAMediaCrypto.hmacAndDecrypt
     *           (WACryptoHmac.encodeKeySha256 + WACryptoHmac.sign / WACryptoHmac.hmacSha256)
     * @param key the HMAC-SHA256 secret key specification
     * @return the initialized {@link Mac}
     * @throws WhatsAppMediaException if MAC creation or initialization fails
     */
    Mac newMac(SecretKeySpec key) throws WhatsAppMediaException {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            return mac;
        } catch (GeneralSecurityException exception) {
            throw new WhatsAppMediaException("Cannot create new mac", exception);
        }
    }

    /**
     * Closes the underlying raw input stream.
     *
     * @implNote WAMediaCrypto
     * @throws IOException if an I/O error occurs while closing
     */
    @Override
    public void close() throws IOException {
        rawInputStream.close();
    }
}
