package com.github.auties00.cobalt.media;

import com.github.auties00.cobalt.exception.WhatsAppMediaException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.media.MediaProvider;
import com.github.auties00.cobalt.util.DataUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Optional;

/**
 * Streams a piece of media to be uploaded to WhatsApp's CDN, encrypting
 * it on the fly and capturing the integrity hashes that accompany the
 * outgoing message.
 *
 * <p>WhatsApp transfers two kinds of media: end-to-end encrypted
 * attachments such as images, videos, audio, documents, and stickers;
 * and unencrypted assets such as newsletter media, profile pictures, and
 * business cover photos. {@link Ciphertext} produces the encrypted form
 * for the first group and {@link Plaintext} passes the second group
 * through while still computing the SHA-256 hash that the server expects
 * on the upload metadata.
 *
 * <p>The encrypted form has the shape {@code ciphertext || HMAC[0:10]}:
 * the plaintext is AES-CBC encrypted with PKCS5 padding and the trailing
 * 10 bytes are the truncated HMAC-SHA256 over {@code IV || ciphertext}.
 * The IV itself is not part of the wire format because it is derived
 * from the random media key via HKDF and the recipient reproduces the
 * same derivation when decrypting.
 *
 * <p>Once the stream has been fully read, callers should query
 * {@link #fileSha256()}, {@link #fileEncSha256()}, {@link #fileKey()},
 * and {@link #fileLength()} to populate the outgoing message protobuf.
 */
@WhatsAppWebModule(moduleName = "WAMediaCrypto")
@WhatsAppWebModule(moduleName = "WAWebCryptoEncryptMedia")
public abstract sealed class MediaUploadInputStream extends MediaInputStream {
    /**
     * Constructs a new upload stream wrapping the given raw plaintext
     * stream.
     *
     * @param rawInputStream the underlying plaintext input stream
     */
    MediaUploadInputStream(InputStream rawInputStream) {
        super(rawInputStream);
    }

    /**
     * Returns the total number of plaintext bytes processed from the
     * underlying stream.
     *
     * <p>Recorded into the outgoing message protobuf's {@code fileLength}
     * field so that recipients can pre-allocate buffers and report
     * accurate progress.
     *
     * @return the plaintext file length in bytes
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    public abstract long fileLength();

    /**
     * Returns the SHA-256 digest of the plaintext content.
     *
     * <p>Available only after the stream has been fully consumed
     * (a {@code read} returned {@code -1}). The value populates the
     * outgoing message protobuf's {@code fileSha256} field so that
     * recipients can verify the decrypted bytes after download.
     *
     * @return the plaintext SHA-256 hash
     * @throws IllegalStateException if the stream has not been fully consumed
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    public abstract byte[] fileSha256();

    /**
     * Returns the SHA-256 digest of the encrypted payload
     * ({@code ciphertext || HMAC[0:10]}).
     *
     * <p>Available only after the stream has been fully consumed.
     * Populates the outgoing message protobuf's {@code fileEncSha256}
     * field. Empty for plaintext uploads that skip encryption.
     *
     * @return an {@link Optional} holding the encrypted SHA-256 hash, or
     *         empty for unencrypted media
     * @throws IllegalStateException if the stream has not been fully consumed
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    public abstract Optional<byte[]> fileEncSha256();

    /**
     * Returns the 32-byte random media key used as the HKDF seed.
     *
     * <p>The media key is stored in the outgoing message protobuf so
     * that recipients can reproduce the same IV, cipher key, and HMAC
     * key when decrypting. Empty for plaintext uploads.
     *
     * @return an {@link Optional} holding the media key, or empty for
     *         unencrypted media
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    public abstract Optional<byte[]> fileKey();

    /**
     * Creates the appropriate upload stream variant for the supplied
     * provider.
     *
     * <p>Returns a {@link Ciphertext} stream when the provider's media
     * path advertises a key name (end-to-end encrypted media) or a
     * {@link Plaintext} stream otherwise (newsletter media, profile
     * pictures, business cover photos).
     *
     * @param provider    the media provider describing the media type
     * @param inputStream the raw plaintext input stream
     * @return the appropriate upload stream
     * @throws WhatsAppMediaException if cipher initialisation fails
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    static MediaUploadInputStream of(MediaProvider provider, InputStream inputStream) throws WhatsAppMediaException {
        var keyName = provider.mediaPath()
                .keyName();
        if (keyName.isPresent()) {
            return new Ciphertext(inputStream, keyName.get());
        } else {
            return new Plaintext(inputStream);
        }
    }

    /**
     * An encrypted upload stream that performs AES-CBC encryption with
     * trailing truncated HMAC-SHA256 authentication.
     *
     * <p>The pipeline generates a random 32-byte media key, derives the
     * IV, cipher key, and HMAC key via HKDF-SHA256, encrypts the
     * plaintext with AES-CBC, computes HMAC-SHA256 over
     * {@code IV || ciphertext} truncated to 10 bytes, and emits
     * {@code ciphertext || HMAC[0:10]} on the wire while accumulating
     * SHA-256 over both the plaintext and the encrypted output.
     */
    @WhatsAppWebModule(moduleName = "WAMediaCrypto")
    @WhatsAppWebModule(moduleName = "WAWebCryptoEncryptMedia")
    private static final class Ciphertext extends MediaUploadInputStream {
        /**
         * Accumulates SHA-256 over the plaintext as bytes are read from
         * the underlying stream. The final value is exposed by
         * {@link #fileSha256()}.
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final MessageDigest plaintextDigest;

        /**
         * Accumulates SHA-256 over the encrypted output
         * ({@code ciphertext || HMAC[0:10]}). The final value is exposed
         * by {@link #fileEncSha256()}.
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final MessageDigest ciphertextDigest;

        /**
         * The HMAC-SHA256 instance computing the authentication tag over
         * {@code IV || ciphertext}. Only the first {@link #MAC_LENGTH}
         * bytes of the final tag are written to the output stream.
         */
        @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "encryptAndHmac",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final Mac ciphertextMac;

        /**
         * The AES-CBC cipher in encrypt mode.
         */
        @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "encryptAndHmac",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final Cipher cipher;

        /**
         * Scratch buffer for staging plaintext chunks before passing
         * them to the cipher.
         */
        @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "encryptAndHmac",
                adaptation = WhatsAppAdaptation.ADAPTED)
        private final byte[] plaintextBuffer;

        /**
         * Scratch buffer for the cipher output. Sized to accommodate
         * one extra AES block so that {@code doFinal} can emit its
         * trailing PKCS5 padding block without reallocation.
         */
        @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "encryptAndHmac",
                adaptation = WhatsAppAdaptation.ADAPTED)
        private final byte[] ciphertextBuffer;

        /**
         * The output buffer exposed to callers via {@link #read()} and
         * {@link #read(byte[], int, int)}.
         */
        @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "encryptAndHmac",
                adaptation = WhatsAppAdaptation.ADAPTED)
        private final byte[] outputBuffer;

        /**
         * The randomly generated 32-byte media key seeding the HKDF
         * derivation.
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final byte[] mediaKey;

        /**
         * The finalised SHA-256 digest of the plaintext, populated when
         * the underlying stream is exhausted.
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        private byte[] plaintextHash;

        /**
         * The finalised SHA-256 digest of
         * {@code ciphertext || HMAC[0:10]}, populated when the
         * underlying stream is exhausted.
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        private byte[] ciphertextHash;

        /**
         * The running count of plaintext bytes read from the underlying
         * stream.
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        private long plaintextLength;

        /**
         * Whether the stream has been fully processed and the trailing
         * HMAC plus hashes have been emitted.
         */
        private boolean finalized;

        /**
         * The current read cursor within {@link #outputBuffer}.
         */
        private int outputPosition;

        /**
         * The number of valid bytes currently held by
         * {@link #outputBuffer}.
         */
        private int outputLimit;

        /**
         * Constructs an encrypted upload stream for the given media type.
         *
         * <p>Generates a random 32-byte media key, HKDF-derives the IV,
         * cipher key, and HMAC key, initialises the AES-CBC cipher in
         * encrypt mode, and primes the HMAC with the IV bytes so that
         * the authentication tag covers {@code IV || ciphertext}.
         *
         * @param rawInputStream the plaintext input stream to encrypt
         * @param keyName        the HKDF info string for the media type
         * @throws WhatsAppMediaException if key derivation or cipher
         *         initialisation fails
         */
        @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "encryptAndHmac",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public Ciphertext(InputStream rawInputStream, String keyName) throws WhatsAppMediaException {
            super(rawInputStream);

            this.plaintextDigest = newHash();
            this.ciphertextDigest = newHash();

            this.mediaKey = DataUtils.randomByteArray(32);

            var expanded = deriveMediaKeyData(mediaKey, keyName);
            var iv = new IvParameterSpec(expanded, 0, IV_LENGTH);
            var cipherKey = new SecretKeySpec(expanded, IV_LENGTH, KEY_LENGTH, "AES");
            var macKey = new SecretKeySpec(expanded, IV_LENGTH + KEY_LENGTH, KEY_LENGTH, "HmacSHA256");

            this.cipher = newCipher(Cipher.ENCRYPT_MODE, cipherKey, iv);
            this.ciphertextMac = newMac(macKey);

            // Feed the IV into the HMAC so the tag covers iv || ciphertext.
            ciphertextMac.update(expanded, 0, IV_LENGTH);

            this.plaintextBuffer = new byte[BUFFER_LENGTH];

            // BUFFER_LENGTH is block-aligned (8192 = 512 * 16) so streaming
            // cipher.update never carries a partial block. The extra block
            // here is reserved for doFinal's PKCS5 padding output.
            this.ciphertextBuffer = new byte[BUFFER_LENGTH + CBC_BLOCK_SIZE];
            this.outputBuffer = new byte[BUFFER_LENGTH];
            this.plaintextLength = 0;
        }

        /**
         * Reads a single byte of the encrypted payload.
         *
         * @return the next byte of encrypted data, or {@code -1} if the
         *         stream is exhausted
         * @throws IOException if an I/O or encryption error occurs
         */
        @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "encryptAndHmac",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @Override
        public int read() throws IOException {
            ensureDataAvailable();
            if (outputPosition >= outputLimit) {
                return -1;
            }

            return outputBuffer[outputPosition++] & 0xFF;
        }

        /**
         * Reads up to {@code len} bytes of the encrypted payload into
         * the supplied array.
         *
         * @param b   the destination buffer
         * @param off the start offset in the destination buffer
         * @param len the maximum number of bytes to read
         * @return the number of bytes actually read, or {@code -1} if
         *         the stream is exhausted
         * @throws IOException if an I/O or encryption error occurs
         */
        @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "encryptAndHmac",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            ensureDataAvailable();
            if (outputPosition >= outputLimit) {
                return -1;
            }

            var available = outputLimit - outputPosition;
            var toRead = Math.min(len, available);
            System.arraycopy(outputBuffer, outputPosition, b, off, toRead);
            outputPosition += toRead;
            return toRead;
        }

        /**
         * Refills {@link #outputBuffer} with encrypted data from the
         * underlying stream.
         *
         * <p>Each pass reads a plaintext chunk, encrypts it with
         * AES-CBC, extends the HMAC and digest accumulators, and copies
         * the ciphertext into the output buffer. On end-of-stream the
         * cipher is finalised, the truncated HMAC is appended, and the
         * SHA-256 hashes are captured for the accessor methods.
         *
         * @throws IOException if an I/O or encryption error occurs
         */
        @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "encryptAndHmac",
                adaptation = WhatsAppAdaptation.ADAPTED)
        private void ensureDataAvailable() throws IOException {
            try {
                while (outputPosition >= outputLimit && !finalized) {
                    this.outputPosition = 0;
                    this.outputLimit = 0;

                    var plaintextRead = rawInputStream.read(plaintextBuffer, 0, plaintextBuffer.length);
                    if (plaintextRead == -1) {
                        rawInputStream.close();

                        // Drain the cipher's final padded block.
                        var finalCiphertextLen = cipher.doFinal(ciphertextBuffer, 0);
                        processChunk(finalCiphertextLen);

                        // Compute the full HMAC tag. The encFilehash digest is
                        // extended with the 10 bytes that go on the wire.
                        var mac = ciphertextMac.doFinal();
                        ciphertextDigest.update(mac, 0, MAC_LENGTH);

                        // Append the truncated HMAC to the output buffer.
                        var macSpace = outputBuffer.length - outputLimit;
                        var macToCopy = Math.min(MAC_LENGTH, macSpace);
                        System.arraycopy(mac, 0, outputBuffer, outputLimit, macToCopy);
                        outputLimit += macToCopy;

                        plaintextHash = plaintextDigest.digest();
                        ciphertextHash = ciphertextDigest.digest();

                        finalized = true;
                        break;
                    }

                    plaintextDigest.update(plaintextBuffer, 0, plaintextRead);
                    plaintextLength += plaintextRead;

                    var ciphertextLen = cipher.update(plaintextBuffer, 0, plaintextRead, ciphertextBuffer, 0);
                    processChunk(ciphertextLen);
                }
            } catch (GeneralSecurityException exception) {
                throw new IOException("Cannot encrypt data", exception);
            }
        }

        /**
         * Routes a freshly produced ciphertext chunk through the
         * encFilehash digest, the HMAC accumulator, and the
         * caller-facing output buffer.
         *
         * @param length the number of valid ciphertext bytes in the
         *               cipher output buffer
         */
        @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "encryptAndHmac",
                adaptation = WhatsAppAdaptation.ADAPTED)
        private void processChunk(int length) {
            if (length <= 0) {
                return;
            }

            ciphertextDigest.update(ciphertextBuffer, 0, length);
            ciphertextMac.update(ciphertextBuffer, 0, length);

            var toCopy = Math.min(length, outputBuffer.length);
            System.arraycopy(ciphertextBuffer, 0, outputBuffer, 0, toCopy);
            outputLimit = toCopy;
        }

        /**
         * Returns the total number of plaintext bytes read.
         *
         * @return the plaintext file length in bytes
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public long fileLength() {
            return plaintextLength;
        }

        /**
         * Returns the finalised SHA-256 digest of the plaintext.
         *
         * @return the plaintext SHA-256 hash
         * @throws IllegalStateException if the stream has not been fully consumed
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public byte[] fileSha256() {
            if (plaintextHash == null) {
                throw new IllegalStateException("Cannot get file SHA-256 hash before the file has been fully read");
            }

            return plaintextHash;
        }

        /**
         * Returns the finalised SHA-256 digest of
         * {@code ciphertext || HMAC[0:10]}.
         *
         * @return an {@link Optional} holding the encrypted SHA-256 hash
         * @throws IllegalStateException if the stream has not been fully consumed
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public Optional<byte[]> fileEncSha256() {
            if (ciphertextHash == null) {
                throw new IllegalStateException("Cannot get file encrypted SHA-256 hash before the file has been fully read");
            }

            return Optional.of(ciphertextHash);
        }

        /**
         * Returns the random 32-byte media key generated for this
         * upload.
         *
         * @return an {@link Optional} holding the media key
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public Optional<byte[]> fileKey() {
            return Optional.of(mediaKey);
        }
    }

    /**
     * A plaintext upload stream that passes the underlying content
     * through unchanged while computing the SHA-256 digest needed for
     * the outgoing message protobuf.
     *
     * <p>Used for media types that do not participate in end-to-end
     * encryption, such as newsletter media, profile pictures, and
     * business cover photos.
     */
    @WhatsAppWebModule(moduleName = "WAWebCryptoEncryptMedia")
    private static final class Plaintext extends MediaUploadInputStream {
        /**
         * Accumulates SHA-256 over the plaintext content. Consumed by
         * {@link #fileSha256()} once the underlying stream is exhausted.
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final MessageDigest plaintextDigest;

        /**
         * The running count of plaintext bytes read from the underlying
         * stream.
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        private long plaintextLength;

        /**
         * The finalised SHA-256 digest of the plaintext, populated when
         * the underlying stream is exhausted.
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        private byte[] plaintextHash;

        /**
         * Whether the underlying stream has been fully consumed and the
         * SHA-256 digest has been finalised.
         */
        private boolean finalized;

        /**
         * Constructs a plaintext upload stream.
         *
         * @param rawInputStream the underlying input stream to pass through
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Plaintext(InputStream rawInputStream) {
            super(rawInputStream);
            try {
                this.plaintextDigest = MessageDigest.getInstance("SHA-256");
                this.plaintextLength = 0;
            } catch (GeneralSecurityException exception) {
                throw new InternalError("Cannot initialize stream", exception);
            }
        }

        /**
         * Reads a single byte from the underlying stream while extending
         * the plaintext digest.
         *
         * @return the next byte of data, or {@code -1} if end of stream
         * @throws IOException if an I/O error occurs
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @Override
        public int read() throws IOException {
            var ch = rawInputStream.read();
            if (ch != -1) {
                plaintextDigest.update((byte) ch);
                plaintextLength++;
            } else if (!finalized) {
                finalized = true;
                plaintextHash = plaintextDigest.digest();
            }
            return ch;
        }

        /**
         * Reads up to {@code len} bytes from the underlying stream
         * while extending the plaintext digest.
         *
         * @param b   the destination buffer
         * @param off the start offset in the destination buffer
         * @param len the maximum number of bytes to read
         * @return the number of bytes read, or {@code -1} if end of stream
         * @throws IOException if an I/O error occurs
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            var result = rawInputStream.read(b, off, len);
            if (result != -1) {
                plaintextDigest.update(b, off, result);
                plaintextLength += result;
            } else if (!finalized) {
                finalized = true;
                plaintextHash = plaintextDigest.digest();
            }
            return result;
        }

        /**
         * Returns the total number of plaintext bytes read.
         *
         * @return the plaintext file length in bytes
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public long fileLength() {
            return plaintextLength;
        }

        /**
         * Returns the finalised SHA-256 digest of the plaintext.
         *
         * @return the plaintext SHA-256 hash
         * @throws IllegalStateException if the stream has not been fully consumed
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public byte[] fileSha256() {
            if (plaintextHash == null) {
                throw new IllegalStateException("Cannot get file SHA-256 hash before the file has been fully read");
            }

            return plaintextHash;
        }

        /**
         * Returns an empty optional because plaintext uploads never
         * produce an encrypted hash.
         *
         * @return an empty {@link Optional}
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public Optional<byte[]> fileEncSha256() {
            return Optional.empty();
        }

        /**
         * Returns an empty optional because plaintext uploads never
         * generate a media key.
         *
         * @return an empty {@link Optional}
         */
        @WhatsAppWebExport(moduleName = "WAWebCryptoEncryptMedia", exports = "default",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public Optional<byte[]> fileKey() {
            return Optional.empty();
        }
    }
}
