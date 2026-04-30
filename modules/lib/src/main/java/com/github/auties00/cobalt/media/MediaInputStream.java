package com.github.auties00.cobalt.media;

import com.github.auties00.cobalt.exception.WhatsAppMediaException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

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
 * Common base for the streaming media encryption and decryption pipelines
 * used when transferring attachments to and from WhatsApp.
 *
 * <p>Holds the cryptographic primitives shared by
 * {@link MediaUploadInputStream} (encrypt-then-HMAC) and
 * {@link MediaDownloadInputStream} (verify-then-decrypt): HKDF-SHA256 key
 * derivation, AES-CBC cipher creation with PKCS5 padding, HMAC-SHA256
 * initialisation, and SHA-256 hashing.
 *
 * <p>WhatsApp media keys follow a fixed derivation: a 32-byte random
 * media key is HKDF-expanded against a media-type specific info string
 * into {@value #EXPANDED_SIZE} bytes that are sliced into a 16-byte IV,
 * a 32-byte AES key, a 32-byte HMAC key, and a 32-byte reference key
 * reserved for media previews.
 */
@WhatsAppWebModule(moduleName = "WAMediaCrypto")
abstract class MediaInputStream extends InputStream {
    /**
     * The default buffer size in bytes used for streaming reads and
     * cipher operations.
     *
     * <p>Sized as a multiple of the AES block size so that streaming
     * {@code cipher.update} calls produce output that fits in the
     * staging buffer without partial-block carry.
     */
    static final int BUFFER_LENGTH = 8192;

    /**
     * The number of trailing HMAC bytes appended to the ciphertext.
     *
     * <p>WhatsApp computes a full 32-byte HMAC-SHA256 over
     * {@code IV || ciphertext} but publishes only the first 10 bytes,
     * which is enough for integrity while keeping the wire overhead
     * minimal for media that is often transferred over constrained
     * links.
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "HMAC_LENGTH",
            adaptation = WhatsAppAdaptation.DIRECT)
    static final int MAC_LENGTH = 10;

    /**
     * The total size in bytes of the HKDF-expanded media key material.
     *
     * <p>The expanded buffer is partitioned as:
     * <ul>
     *   <li>bytes {@code 0..15}: initialisation vector</li>
     *   <li>bytes {@code 16..47}: AES-CBC cipher key</li>
     *   <li>bytes {@code 48..79}: HMAC-SHA256 key</li>
     *   <li>bytes {@code 80..111}: reference key for media previews</li>
     * </ul>
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "computeMediaKeys",
            adaptation = WhatsAppAdaptation.DIRECT)
    static final int EXPANDED_SIZE = 112;

    /**
     * The symmetric key length in bytes used for both the AES cipher key
     * and the HMAC key.
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "computeMediaKeys",
            adaptation = WhatsAppAdaptation.DIRECT)
    static final int KEY_LENGTH = 32;

    /**
     * The initialisation vector length in bytes for AES-CBC.
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "IV_LENGTH",
            adaptation = WhatsAppAdaptation.DIRECT)
    static final int IV_LENGTH = 16;

    /**
     * The AES-CBC block size in bytes.
     *
     * <p>Used to size the ciphertext staging buffer so that
     * {@code Cipher.doFinal} can emit the trailing PKCS5 padding block
     * without reallocation.
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "CBC_BLOCK_SIZE",
            adaptation = WhatsAppAdaptation.DIRECT)
    static final int CBC_BLOCK_SIZE = 16;

    /**
     * The underlying raw input stream providing the source bytes.
     *
     * <p>Carries plaintext to be encrypted in the upload pipeline and
     * the ciphertext-plus-HMAC payload delivered by the CDN in the
     * download pipeline.
     */
    final InputStream rawInputStream;

    /**
     * Constructs a new media input stream wrapping the given raw stream.
     *
     * @param rawInputStream the underlying input stream
     * @throws NullPointerException if {@code rawInputStream} is {@code null}
     */
    MediaInputStream(InputStream rawInputStream) {
        this.rawInputStream = Objects.requireNonNull(rawInputStream, "rawInputStream must not be null");
    }

    /**
     * Derives the expanded key material for a media payload from the raw
     * 32-byte media key and the media-type specific info string.
     *
     * <p>Performs HKDF-SHA256 extract-then-expand with no explicit salt
     * (defaulting to 32 zero bytes per RFC 5869) and the UTF-8 encoding
     * of the key name as the info parameter. Callers slice the returned
     * {@value #EXPANDED_SIZE}-byte array into the IV, cipher key, HMAC
     * key, and reference key.
     *
     * @param mediaKey     the 32-byte raw media key
     * @param mediaKeyName the HKDF info string identifying the media type,
     *                     for example {@code "WhatsApp Image Keys"}
     * @return the expanded key material
     * @throws WhatsAppMediaException if the HKDF derivation fails
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "computeMediaKeys",
            adaptation = WhatsAppAdaptation.DIRECT)
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
     * Creates a fresh SHA-256 {@link MessageDigest} for plaintext or
     * ciphertext hashing.
     *
     * <p>SHA-256 is used to compute the {@code fileSha256} and
     * {@code fileEncSha256} fields recorded on the outgoing media
     * message protobuf.
     *
     * @return a fresh SHA-256 digest
     * @throws WhatsAppMediaException if SHA-256 is unavailable
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto",
            exports = {"encryptAndHmac", "hmacAndDecrypt"},
            adaptation = WhatsAppAdaptation.DIRECT)
    MessageDigest newHash() throws WhatsAppMediaException {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (GeneralSecurityException exception) {
            throw new WhatsAppMediaException("Cannot create new hash", exception);
        }
    }

    /**
     * Creates and initialises a new AES-CBC cipher with PKCS5 padding.
     *
     * @param mode the cipher mode, either {@link Cipher#ENCRYPT_MODE} or
     *             {@link Cipher#DECRYPT_MODE}
     * @param key  the AES secret key
     * @param iv   the initialisation vector
     * @return the initialised cipher
     * @throws WhatsAppMediaException if cipher creation or initialisation fails
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto",
            exports = {"encryptAndHmac", "hmacAndDecrypt"},
            adaptation = WhatsAppAdaptation.DIRECT)
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
     * Creates and initialises a new HMAC-SHA256 instance bound to the
     * supplied HMAC key.
     *
     * @param key the HMAC-SHA256 secret key
     * @return the initialised MAC
     * @throws WhatsAppMediaException if MAC creation or initialisation fails
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto",
            exports = {"encryptAndHmac", "hmacAndDecrypt"},
            adaptation = WhatsAppAdaptation.DIRECT)
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
     * @throws IOException if an I/O error occurs while closing
     */
    @Override
    public void close() throws IOException {
        rawInputStream.close();
    }
}
