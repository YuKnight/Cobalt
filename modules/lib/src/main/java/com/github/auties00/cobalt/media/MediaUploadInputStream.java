package com.github.auties00.cobalt.media;

import com.github.auties00.cobalt.exception.WhatsAppMediaException;
import com.github.auties00.cobalt.model.media.MediaProvider;
import com.github.auties00.cobalt.util.FastDataUtils;

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
 * An input stream that transparently encrypts media content during upload.
 *
 * <p>This sealed abstract class has two variants:
 * <ul>
 *   <li>{@link Ciphertext} -- for media types that require end-to-end encryption
 *       (those with a non-{@code null} key name in their {@link com.github.auties00.cobalt.model.media.MediaPath})</li>
 *   <li>{@link Plaintext} -- for unencrypted media types (newsletter media,
 *       profile pictures, etc.)</li>
 * </ul>
 *
 * <p>The encrypted stream format produced by {@link Ciphertext} is:
 * {@code ciphertext || HMAC[0:10]}, where the ciphertext is AES-CBC encrypted
 * plaintext (with PKCS5 padding), and the trailing 10 bytes are the truncated
 * HMAC-SHA256 computed over {@code IV || ciphertext}. The IV itself is NOT
 * included in the output stream; it is derived from the media key via HKDF and
 * transmitted separately in the message protobuf.
 *
 * @implNote WAMediaCrypto.encryptAndHmac, WAWebCryptoEncryptMedia
 */
public abstract sealed class MediaUploadInputStream extends MediaInputStream {
    /**
     * Constructs a new media upload input stream wrapping the given raw stream.
     *
     * @implNote WAMediaCrypto.encryptAndHmac
     * @param rawInputStream the underlying plaintext input stream
     */
    MediaUploadInputStream(InputStream rawInputStream) {
        super(rawInputStream);
    }

    /**
     * Returns the total number of plaintext bytes that have been read and
     * processed from the underlying stream.
     *
     * @implNote WAWebCryptoEncryptMedia (plaintext byte count for protobuf field)
     * @return the plaintext file length in bytes
     */
    public abstract long fileLength();

    /**
     * Returns the SHA-256 digest of the plaintext content.
     *
     * <p>This value is only available after the entire stream has been read
     * (i.e., after a read returns {@code -1}).
     *
     * @implNote WAMediaCrypto.hmacAndDecrypt returns plaintextHash via
     *           WAHashUtils.toPlaintextHash(SHA-256(plaintext))
     * @return the plaintext SHA-256 hash
     * @throws IllegalStateException if the stream has not been fully consumed
     */
    public abstract byte[] fileSha256();

    /**
     * Returns the SHA-256 digest of the encrypted payload
     * ({@code ciphertext || HMAC[0:10]}).
     *
     * <p>This value is only available after the entire stream has been read
     * and is empty for plaintext (unencrypted) uploads.
     *
     * @implNote WAWebCryptoEncryptMedia.encryptMedia returns hash (encFilehash) =
     *           SHA-256(ciphertext + hmac[0:10])
     * @return an {@link Optional} containing the encrypted SHA-256 hash, or
     *         empty for unencrypted media
     * @throws IllegalStateException if the stream has not been fully consumed
     */
    public abstract Optional<byte[]> fileEncSha256();

    /**
     * Returns the 32-byte random media key used for HKDF key derivation.
     *
     * <p>This key is stored in the message protobuf so recipients can derive
     * the same IV, cipher key, and HMAC key to decrypt the media.
     *
     * @implNote WAWebCryptoEncryptMedia.encryptMedia generates a random mediaKey
     *           and passes it to WAWebCryptoCreateMediaKeys
     * @return an {@link Optional} containing the media key, or empty for
     *         unencrypted media
     */
    public abstract Optional<byte[]> fileKey();

    /**
     * Creates the appropriate media upload stream for the given provider and
     * input stream.
     *
     * <p>If the provider's media path has a key name, a {@link Ciphertext}
     * stream is returned that encrypts the content. Otherwise, a
     * {@link Plaintext} stream is returned that passes content through
     * unmodified while computing the plaintext hash.
     *
     * @implNote WAWebCryptoEncryptMedia.encryptMedia dispatches to encryption
     *           when a media type with key derivation is provided
     * @param provider    the media provider describing the media type
     * @param inputStream the raw plaintext input stream
     * @return the appropriate upload stream
     * @throws WhatsAppMediaException if cipher initialization fails
     */
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
     * An encrypted media upload stream that performs AES-CBC encryption with
     * HMAC-SHA256 authentication.
     *
     * <p>The encryption process:
     * <ol>
     *   <li>Generates a random 32-byte media key</li>
     *   <li>Derives IV (16 bytes), cipher key (32 bytes), and HMAC key
     *       (32 bytes) via HKDF-SHA256</li>
     *   <li>Encrypts plaintext with AES-CBC using the derived IV and cipher
     *       key</li>
     *   <li>Computes HMAC-SHA256 over {@code IV || ciphertext} and truncates
     *       to 10 bytes</li>
     *   <li>Outputs: {@code ciphertext || HMAC[0:10]}</li>
     *   <li>Computes SHA-256 of both plaintext and the encrypted output</li>
     * </ol>
     *
     * @implNote WAMediaCrypto.encryptAndHmac, WAWebCryptoEncryptMedia.encryptMedia
     */
    private static final class Ciphertext extends MediaUploadInputStream {
        /**
         * The SHA-256 digest accumulator for the plaintext content.
         *
         * @implNote WAMediaCrypto.encryptAndHmac (plaintext is not hashed in the
         *           old API, but WAWebCryptoEncryptMedia computes fileSha256 separately)
         */
        private final MessageDigest plaintextDigest;

        /**
         * The SHA-256 digest accumulator for the encrypted output
         * ({@code ciphertext || HMAC[0:10]}).
         *
         * @implNote WAWebCryptoEncryptMedia returns hash = WAMediaCalculateFilehash.calculateFilehash(
         *           ciphertext + hmac[0:10]) which is SHA-256 base64 of that concatenation
         */
        private final MessageDigest ciphertextDigest;

        /**
         * The HMAC-SHA256 instance computing the authentication tag over
         * {@code IV || ciphertext}.
         *
         * @implNote WAMediaCrypto.encryptAndHmac: WACryptoHmac.sign(encodeKeySha256(hmacKey), ivCiphertext)
         */
        private final Mac ciphertextMac;

        /**
         * The AES-CBC cipher in encrypt mode.
         *
         * @implNote WAMediaCrypto.encryptAndHmac: WACryptoAesCbc.AesCbcStream(l, "encrypt", e, t)
         */
        private final Cipher cipher;

        /**
         * The buffer for reading plaintext chunks from the underlying stream.
         *
         * @implNote WAMediaCrypto.encryptAndHmac (64KB chunks in WA Web, 8KB here)
         */
        private final byte[] plaintextBuffer;

        /**
         * The buffer for holding the cipher output after encrypting a plaintext
         * chunk. Sized to accommodate one extra AES block for padding.
         *
         * @implNote WAMediaCrypto.encryptAndHmac (cipher output buffer)
         */
        private final byte[] ciphertextBuffer;

        /**
         * The output buffer exposed to callers via {@link #read()} and
         * {@link #read(byte[], int, int)}.
         *
         * @implNote WAMediaCrypto.encryptAndHmac (Binary buffer holding iv+ciphertext+hmac)
         */
        private final byte[] outputBuffer;

        /**
         * The randomly generated 32-byte media key used for HKDF key derivation.
         *
         * @implNote WAWebCryptoEncryptMedia.encryptMedia generates mediaKey randomly
         */
        private final byte[] mediaKey;

        /**
         * The computed SHA-256 hash of the plaintext, set after finalization.
         *
         * @implNote WAMediaCrypto.hmacAndDecrypt returns plaintextHash
         */
        private byte[] plaintextHash;

        /**
         * The computed SHA-256 hash of {@code ciphertext || HMAC[0:10]}, set
         * after finalization.
         *
         * @implNote WAWebCryptoEncryptMedia returns hash (encFilehash)
         */
        private byte[] ciphertextHash;

        /**
         * The running count of plaintext bytes read from the underlying stream.
         *
         * @implNote WAWebCryptoEncryptMedia (tracked for protobuf fileLength field)
         */
        private long plaintextLength;

        /**
         * Whether the stream has been fully processed and finalized.
         *
         * @implNote WAMediaCrypto.encryptAndHmac (stream finalization state)
         */
        private boolean finalized;

        /**
         * The current read position within the output buffer.
         *
         * @implNote NO_WA_BASIS (Java streaming adaptation)
         */
        private int outputPosition;

        /**
         * The number of valid bytes in the output buffer.
         *
         * @implNote NO_WA_BASIS (Java streaming adaptation)
         */
        private int outputLimit;

        /**
         * Constructs a new encrypted media upload stream.
         *
         * <p>Generates a random 32-byte media key, derives the IV, cipher key,
         * and HMAC key via HKDF, and initializes the AES-CBC cipher and
         * HMAC-SHA256 instances. The HMAC is primed with the IV bytes.
         *
         * @implNote WAMediaCrypto.encryptAndHmac: derives keys via computeMediaKeys,
         *           initializes AesCbcStream, writes IV to binary buffer, prepares HMAC
         * @param rawInputStream the plaintext input stream to encrypt
         * @param keyName        the HKDF info string for the media type
         * @throws WhatsAppMediaException if key derivation or cipher initialization fails
         */
        public Ciphertext(InputStream rawInputStream, String keyName) throws WhatsAppMediaException {
            super(rawInputStream);

            this.plaintextDigest = newHash();
            this.ciphertextDigest = newHash();

            this.mediaKey = FastDataUtils.randomByteArray(32);
            var expanded = deriveMediaKeyData(mediaKey, keyName); // WAMediaCrypto.computeMediaKeys
            var iv = new IvParameterSpec(expanded, 0, IV_LENGTH); // WAMediaCrypto.computeMediaKeys: iv = bytes[0:16]
            var cipherKey = new SecretKeySpec(expanded, IV_LENGTH, KEY_LENGTH, "AES"); // WAMediaCrypto.computeMediaKeys: cipherKey = bytes[16:48]
            var macKey = new SecretKeySpec(expanded, IV_LENGTH + KEY_LENGTH, KEY_LENGTH, "HmacSHA256"); // WAMediaCrypto.computeMediaKeys: hmacKey = bytes[48:80]

            this.cipher = newCipher(Cipher.ENCRYPT_MODE, cipherKey, iv); // WAMediaCrypto.encryptAndHmac: AesCbcStream(l, "encrypt", e, t)

            this.ciphertextMac = newMac(macKey); // WAMediaCrypto.encryptAndHmac: WACryptoHmac.encodeKeySha256(r)
            ciphertextMac.update(expanded, 0, IV_LENGTH); // WAMediaCrypto.encryptAndHmac: l.writeByteArray(t) feeds IV into HMAC input

            this.plaintextBuffer = new byte[BUFFER_LENGTH];
            this.ciphertextBuffer = new byte[BUFFER_LENGTH + cipher.getBlockSize()]; // ADAPTED: WAMediaCrypto.CBC_BLOCK_SIZE = 16
            this.outputBuffer = new byte[BUFFER_LENGTH];
            this.plaintextLength = 0;
        }

        /**
         * Reads a single byte from the encrypted output stream.
         *
         * @implNote ADAPTED: WAMediaCrypto.encryptAndHmac (streaming adaptation
         *           of the batch encryption that returns ivCiphertextHmac)
         * @return the next byte of encrypted data, or {@code -1} if the stream
         *         is exhausted
         * @throws IOException if an I/O or encryption error occurs
         */
        @Override
        public int read() throws IOException {
            ensureDataAvailable();
            if (outputPosition >= outputLimit) {
                return -1;
            }

            return outputBuffer[outputPosition++] & 0xFF;
        }

        /**
         * Reads up to {@code len} bytes from the encrypted output stream into
         * the specified array.
         *
         * @implNote ADAPTED: WAMediaCrypto.encryptAndHmac (streaming adaptation)
         * @param b   the destination buffer
         * @param off the start offset in the destination buffer
         * @param len the maximum number of bytes to read
         * @return the number of bytes actually read, or {@code -1} if the
         *         stream is exhausted
         * @throws IOException if an I/O or encryption error occurs
         */
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
         * Ensures that the output buffer contains data to return to the caller.
         *
         * <p>Reads plaintext from the underlying stream in chunks, encrypts each
         * chunk with AES-CBC, updates the HMAC and digest accumulators, and
         * fills the output buffer. On end-of-stream, finalizes the cipher,
         * appends the truncated 10-byte HMAC, and computes the final hashes.
         *
         * @implNote WAMediaCrypto.encryptAndHmac:
         *           for-loop encrypts in S=64KB chunks via AesCbcStream.append,
         *           then AesCbcStream.finalize for the last chunk,
         *           HMAC = sign(encodeKeySha256(hmacKey), ivCiphertext),
         *           writes hmac[0:10], computes SHA-256(ciphertext+hmac[0:10])
         * @throws IOException if an I/O or encryption error occurs
         */
        private void ensureDataAvailable() throws IOException {
            try {
                while (outputPosition >= outputLimit && !finalized) {
                    this.outputPosition = 0;
                    this.outputLimit = 0;

                    var plaintextRead = rawInputStream.read(plaintextBuffer, 0, plaintextBuffer.length);
                    if (plaintextRead == -1) {
                        rawInputStream.close();

                        var finalCiphertextLen = cipher.doFinal(ciphertextBuffer, 0); // WAMediaCrypto.encryptAndHmac: s.finalize(...)
                        processChunk(finalCiphertextLen);

                        var mac = ciphertextMac.doFinal(); // WAMediaCrypto.encryptAndHmac: WACryptoHmac.sign(f, _) → full 32-byte HMAC
                        ciphertextDigest.update(mac, 0, MAC_LENGTH); // WAMediaCrypto.encryptAndHmac: SHA-256 includes hmac[0:10]

                        var macSpace = outputBuffer.length - outputLimit;
                        var macToCopy = Math.min(MAC_LENGTH, macSpace);
                        System.arraycopy(mac, 0, outputBuffer, outputLimit, macToCopy); // WAMediaCrypto.encryptAndHmac: l.writeByteArray(new Uint8Array(g, 0, v))
                        outputLimit += macToCopy;

                        plaintextHash = plaintextDigest.digest();
                        ciphertextHash = ciphertextDigest.digest(); // WAMediaCrypto.encryptAndHmac: SHA-256(ciphertext + hmac[0:10])

                        finalized = true;
                        break;
                    }

                    plaintextDigest.update(plaintextBuffer, 0, plaintextRead);
                    plaintextLength += plaintextRead;

                    var ciphertextLen = cipher.update(plaintextBuffer, 0, plaintextRead, ciphertextBuffer, 0); // WAMediaCrypto.encryptAndHmac: s.append(chunk)
                    processChunk(ciphertextLen);
                }
            } catch (GeneralSecurityException exception) {
                throw new IOException("Cannot encrypt data", exception);
            }
        }

        /**
         * Processes a chunk of ciphertext output from the cipher, updating the
         * encrypted file hash digest and the HMAC accumulator, and copying the
         * data to the output buffer.
         *
         * @implNote WAMediaCrypto.encryptAndHmac:
         *           ciphertext flows into the Binary buffer which is then used
         *           for both SHA-256 and HMAC computation
         * @param length the number of valid ciphertext bytes in the buffer
         */
        private void processChunk(int length) {
            if (length <= 0) {
                return;
            }

            ciphertextDigest.update(ciphertextBuffer, 0, length); // WAMediaCrypto.encryptAndHmac: SHA-256(ciphertext)
            ciphertextMac.update(ciphertextBuffer, 0, length); // WAMediaCrypto.encryptAndHmac: HMAC(iv + ciphertext)
            var toCopy = Math.min(length, outputBuffer.length);
            System.arraycopy(ciphertextBuffer, 0, outputBuffer, 0, toCopy);
            outputLimit = toCopy;
        }

        /**
         * Returns the total number of plaintext bytes processed.
         *
         * @implNote WAWebCryptoEncryptMedia (plaintext.byteLength for the protobuf)
         * @return the plaintext file length in bytes
         */
        @Override
        public long fileLength() {
            return plaintextLength;
        }

        /**
         * Returns the SHA-256 digest of the plaintext content.
         *
         * @implNote WAMediaCrypto.hmacAndDecrypt returns plaintextHash =
         *           WAHashUtils.toPlaintextHash(SHA-256(plaintext))
         * @return the plaintext SHA-256 hash
         * @throws IllegalStateException if the stream has not been fully consumed
         */
        @Override
        public byte[] fileSha256() {
            if (plaintextHash == null) {
                throw new IllegalStateException("Cannot get file SHA-256 hash before the file has been fully read");
            }

            return plaintextHash;
        }

        /**
         * Returns the SHA-256 digest of the encrypted output
         * ({@code ciphertext || HMAC[0:10]}).
         *
         * @implNote WAWebCryptoEncryptMedia returns hash =
         *           WAMediaCalculateFilehash.calculateFilehash(ciphertext + hmac[0:10])
         *           which is SHA-256(ciphertext + hmac[0:10]) encoded as base64
         * @return an {@link Optional} containing the encrypted SHA-256 hash
         * @throws IllegalStateException if the stream has not been fully consumed
         */
        @Override
        public Optional<byte[]> fileEncSha256() {
            if (ciphertextHash == null) {
                throw new IllegalStateException("Cannot get file encrypted SHA-256 hash before the file has been fully read");
            }

            return Optional.of(ciphertextHash);
        }

        /**
         * Returns the randomly generated 32-byte media key.
         *
         * @implNote WAWebCryptoEncryptMedia.encryptMedia generates mediaKey
         *           and stores it in the message protobuf
         * @return an {@link Optional} containing the media key
         */
        @Override
        public Optional<byte[]> fileKey() {
            return Optional.of(mediaKey);
        }
    }

    /**
     * A plaintext (unencrypted) media upload stream that passes content
     * through unmodified while computing the SHA-256 digest.
     *
     * <p>Used for media types that do not require end-to-end encryption, such
     * as newsletter media, profile pictures, and business cover photos.
     *
     * @implNote WAWebCryptoEncryptMedia (no encryption path for types without key)
     */
    private static final class Plaintext extends MediaUploadInputStream {
        /**
         * The SHA-256 digest accumulator for the plaintext content.
         *
         * @implNote WAWebCryptoEncryptMedia (plaintext hash for integrity)
         */
        private final MessageDigest plaintextDigest;

        /**
         * The running count of plaintext bytes read.
         *
         * @implNote WAWebCryptoEncryptMedia (file size tracking)
         */
        private long plaintextLength;

        /**
         * The computed SHA-256 hash, set after the stream is exhausted.
         *
         * @implNote WAWebCryptoEncryptMedia (plaintext hash result)
         */
        private byte[] plaintextHash;

        /**
         * Whether the stream has been fully consumed and the hash computed.
         *
         * @implNote NO_WA_BASIS (Java streaming state tracking)
         */
        private boolean finalized;

        /**
         * Constructs a new plaintext media upload stream.
         *
         * @implNote WAWebCryptoEncryptMedia (no-encryption path)
         * @param rawInputStream the underlying input stream to pass through
         */
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
         * Reads a single byte, updating the plaintext digest.
         *
         * @implNote ADAPTED: WAWebCryptoEncryptMedia (streaming adaptation)
         * @return the next byte of data, or {@code -1} if end of stream
         * @throws IOException if an I/O error occurs
         */
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
         * Reads up to {@code len} bytes, updating the plaintext digest.
         *
         * @implNote ADAPTED: WAWebCryptoEncryptMedia (streaming adaptation)
         * @param b   the destination buffer
         * @param off the start offset in the destination buffer
         * @param len the maximum number of bytes to read
         * @return the number of bytes read, or {@code -1} if end of stream
         * @throws IOException if an I/O error occurs
         */
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
         * @implNote WAWebCryptoEncryptMedia (file size for protobuf)
         * @return the plaintext file length in bytes
         */
        @Override
        public long fileLength() {
            return plaintextLength;
        }

        /**
         * Returns the SHA-256 digest of the plaintext content.
         *
         * @implNote WAWebCryptoEncryptMedia (plaintext hash)
         * @return the plaintext SHA-256 hash
         * @throws IllegalStateException if the stream has not been fully consumed
         */
        @Override
        public byte[] fileSha256() {
            if (plaintextHash == null) {
                throw new IllegalStateException("Cannot get file SHA-256 hash before the file has been fully read");
            }

            return plaintextHash;
        }

        /**
         * Returns an empty optional since plaintext uploads have no encrypted
         * hash.
         *
         * @implNote WAWebCryptoEncryptMedia (no encryption, no encFilehash)
         * @return an empty {@link Optional}
         */
        @Override
        public Optional<byte[]> fileEncSha256() {
            return Optional.empty();
        }

        /**
         * Returns an empty optional since plaintext uploads have no media key.
         *
         * @implNote WAWebCryptoEncryptMedia (no encryption, no mediaKey)
         * @return an empty {@link Optional}
         */
        @Override
        public Optional<byte[]> fileKey() {
            return Optional.empty();
        }
    }
}
