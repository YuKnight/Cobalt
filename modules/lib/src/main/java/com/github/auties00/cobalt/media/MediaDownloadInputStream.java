package com.github.auties00.cobalt.media;

import com.github.auties00.cobalt.exception.WhatsAppMediaException;
import com.github.auties00.cobalt.model.media.MediaProvider;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * An input stream that transparently decrypts and verifies downloaded media.
 *
 * <p>The downloaded payload from WhatsApp's CDN has the format
 * {@code ciphertext || HMAC[0:10]}, where the ciphertext is AES-CBC encrypted
 * content and the trailing 10 bytes are the truncated HMAC-SHA256 computed
 * over {@code IV || ciphertext}. The IV is not present in the payload; it is
 * derived from the media key via HKDF.
 *
 * <p>The decryption state machine progresses through these stages:
 * <ol>
 *   <li>{@link State#READ_DATA} -- reads and decrypts ciphertext bytes, updates
 *       HMAC and digest accumulators</li>
 *   <li>{@link State#READ_MAC} -- reads the trailing 10-byte HMAC from the
 *       stream</li>
 *   <li>{@link State#VALIDATE_ALL} -- verifies the HMAC, ciphertext hash, and
 *       plaintext hash</li>
 *   <li>{@link State#DONE} -- all data consumed and validated</li>
 * </ol>
 *
 * <p>For media types marked as inflatable (e.g., app state blobs, history sync),
 * the decrypted content is additionally decompressed via zlib inflate.
 *
 * @implNote WAMediaCrypto.hmacAndDecrypt, WAWebCryptoDecryptMedia
 */
final class MediaDownloadInputStream extends MediaInputStream {
    /**
     * The HTTP client used for the media download connection, closed when
     * this stream is closed.
     *
     * @implNote NO_WA_BASIS (Java resource management adaptation)
     */
    private final HttpClient client;

    /**
     * The zlib inflater for decompressing inflatable media types, or
     * {@code null} if the media type is not inflatable.
     *
     * @implNote ADAPTED: WAWebMmsMediaTypes (inflatable flag on media types
     *           like md-app-state and md-msg-hist)
     */
    private final Inflater inflater;

    /**
     * The primary I/O buffer used for reading raw bytes and holding decrypted
     * output.
     *
     * @implNote WAMediaCrypto.hmacAndDecrypt (ciphertext and plaintext buffers)
     */
    private final byte[] buffer;

    /**
     * The current read position within the buffer for output to callers.
     *
     * @implNote NO_WA_BASIS (Java streaming adaptation)
     */
    private int bufferOffset;

    /**
     * The number of valid decrypted bytes in the buffer.
     *
     * @implNote NO_WA_BASIS (Java streaming adaptation)
     */
    private int bufferLimit;

    /**
     * The secondary buffer for holding decompressed (inflated) output, or
     * {@code null} if the media type is not inflatable.
     *
     * @implNote ADAPTED: decompression for inflatable media types
     */
    private final byte[] inflatedBuffer;

    /**
     * The current read position within the inflated buffer.
     *
     * @implNote NO_WA_BASIS (Java streaming adaptation)
     */
    private int inflatedOffset;

    /**
     * The number of valid decompressed bytes in the inflated buffer.
     *
     * @implNote NO_WA_BASIS (Java streaming adaptation)
     */
    private int inflatedLimit;

    /**
     * The buffer for accumulating the trailing HMAC bytes read from the
     * stream, or {@code null} for unencrypted media.
     *
     * @implNote WAMediaCrypto.hmacAndDecrypt: mac parameter (a) is the
     *           extracted HMAC bytes from the downloaded payload
     */
    private final byte[] macBuffer;

    /**
     * The number of HMAC bytes read into the MAC buffer so far.
     *
     * @implNote NO_WA_BASIS (Java streaming adaptation for incremental MAC read)
     */
    private int macBufferOffset;

    /**
     * The SHA-256 digest accumulator for the decrypted plaintext, or
     * {@code null} if no expected plaintext hash was provided.
     *
     * @implNote WAMediaCrypto.hmacAndDecrypt: SHA-256(plaintext) is compared
     *           to the expected plaintextHash
     */
    private final MessageDigest plaintextDigest;

    /**
     * The expected SHA-256 hash of the decrypted plaintext for verification,
     * or {@code null} if no expected hash was provided.
     *
     * @implNote WAMediaCrypto.hmacAndDecrypt: toPlaintextHash comparison
     */
    private final byte[] expectedPlaintextSha256;

    /**
     * The SHA-256 digest accumulator for the encrypted payload
     * ({@code ciphertext || HMAC[0:10]}), or {@code null} if no expected
     * ciphertext hash was provided.
     *
     * @implNote WAWebCryptoDecryptMedia (optional encFilehash verification)
     */
    private final MessageDigest ciphertextDigest;

    /**
     * The expected SHA-256 hash of the encrypted payload for verification,
     * or {@code null} if no expected hash was provided.
     *
     * @implNote WAWebCryptoDecryptMedia (optional encFilehash verification)
     */
    private final byte[] expectedCiphertextSha256;

    /**
     * The AES-CBC cipher in decrypt mode, or {@code null} for unencrypted
     * media.
     *
     * @implNote WAMediaCrypto.hmacAndDecrypt: WACryptoAesCbc.aesCbcDecrypt(e, t, n)
     */
    private final Cipher cipher;

    /**
     * The HMAC-SHA256 instance for verifying the authentication tag, or
     * {@code null} for unencrypted media.
     *
     * @implNote WAMediaCrypto.hmacAndDecrypt: WACryptoHmac.hmacSha256(r, iv+ciphertext)
     */
    private final Mac mac;

    /**
     * The number of ciphertext bytes remaining to be read before the HMAC
     * trailer.
     *
     * @implNote WAWebCryptoDecryptMedia: ciphertext = ciphertextHmac.subarray(0, -10)
     */
    private long remainingText;

    /**
     * The current state of the decryption state machine.
     *
     * @implNote ADAPTED: WAMediaCrypto.hmacAndDecrypt (batch operation adapted
     *           to streaming state machine)
     */
    private State state;

    /**
     * Constructs a new media download input stream that decrypts and verifies
     * the content from the given raw input stream.
     *
     * <p>If the provider specifies a media key and key name, the stream
     * derives cryptographic keys via HKDF and initializes the AES-CBC cipher
     * and HMAC-SHA256 for decryption and verification. The HMAC is primed
     * with the IV bytes. The payload is expected to contain
     * {@code payloadLength - 10} bytes of ciphertext followed by 10 bytes
     * of truncated HMAC.
     *
     * <p>If no media key is present, the stream passes through the raw content
     * without decryption, optionally verifying the plaintext hash.
     *
     * @implNote WAMediaCrypto.hmacAndDecrypt: derives keys via computeMediaKeys,
     *           separates ciphertext from HMAC, verifies HMAC, decrypts, verifies plaintext hash.
     *           WAWebCryptoDecryptMedia: concat(iv, ciphertextHmac.subarray(0,-10)) for HMAC input,
     *           hmacSha256(macKey, data, 10) for truncated comparison
     * @param client         the HTTP client managing the download connection
     * @param rawInputStream the raw input stream from the CDN
     * @param payloadLength  the total payload length in bytes (ciphertext + HMAC)
     * @param provider       the media provider with decryption metadata
     * @throws WhatsAppMediaException if key derivation or cipher initialization fails
     */
    MediaDownloadInputStream(HttpClient client, InputStream rawInputStream, long payloadLength, MediaProvider provider) throws WhatsAppMediaException {
        super(rawInputStream);
        Objects.requireNonNull(client, "client cannot be null");
        Objects.requireNonNull(rawInputStream, "rawInputStream must not be null");
        Objects.requireNonNull(provider, "provider must not be null");

        this.client = client;
        this.inflater = provider.mediaPath().inflatable() ? new Inflater() : null; // ADAPTED: inflatable media types

        this.buffer = new byte[BUFFER_LENGTH];
        this.inflatedBuffer = isInflatable() ? new byte[BUFFER_LENGTH] : null;

        this.expectedPlaintextSha256 = provider.mediaSha256().orElse(null); // WAMediaCrypto.hmacAndDecrypt: expectedPlaintextHash
        this.plaintextDigest = expectedPlaintextSha256 != null ? newHash() : null;

        var hasKeyName = provider.mediaPath().keyName().isPresent();
        var hasMediaKey = provider.mediaKey().isPresent();

        if (hasKeyName != hasMediaKey) {
            throw new WhatsAppMediaException.Download("Media key and key name must both be present or both be absent");
        } else if (hasKeyName) {
            this.expectedCiphertextSha256 = provider.mediaEncryptedSha256().orElse(null); // WAWebCryptoDecryptMedia: optional encFilehash check
            this.ciphertextDigest = expectedCiphertextSha256 != null ? newHash() : null;

            var mediaKey = provider.mediaKey()
                    .orElseThrow(() -> new WhatsAppMediaException.Download("Media key must be present"));
            var keyName = provider.mediaPath().keyName()
                    .orElseThrow(() -> new WhatsAppMediaException.Download("Key name must be present"));

            var expanded = deriveMediaKeyData(mediaKey, keyName); // WAMediaCrypto.computeMediaKeys
            var iv = new IvParameterSpec(expanded, 0, IV_LENGTH); // WAMediaCrypto.computeMediaKeys: iv = bytes[0:16]
            var cipherKey = new SecretKeySpec(expanded, IV_LENGTH, KEY_LENGTH, "AES"); // WAMediaCrypto.computeMediaKeys: cipherKey = bytes[16:48]
            var macKey = new SecretKeySpec(expanded, IV_LENGTH + KEY_LENGTH, KEY_LENGTH, "HmacSHA256"); // WAMediaCrypto.computeMediaKeys: hmacKey = bytes[48:80]

            this.cipher = newCipher(Cipher.DECRYPT_MODE, cipherKey, iv); // WAMediaCrypto.hmacAndDecrypt: aesCbcDecrypt(e, t, n)
            this.mac = newMac(macKey); // WAMediaCrypto.hmacAndDecrypt: hmacSha256(r, iv+ciphertext)
            this.mac.update(expanded, 0, IV_LENGTH); // WAWebCryptoDecryptMedia: concat(iv, ciphertext) for HMAC input

            this.remainingText = payloadLength - MAC_LENGTH; // WAWebCryptoDecryptMedia: ciphertext = payload[0:-10]
            this.macBuffer = new byte[MAC_LENGTH]; // WAMediaCrypto.hmacAndDecrypt: mac bytes (a parameter)
        } else {
            this.expectedCiphertextSha256 = null;
            this.ciphertextDigest = null;
            this.cipher = null;
            this.mac = null;
            this.macBuffer = null;
            this.remainingText = payloadLength;
        }

        this.state = State.READ_DATA;
    }

    /**
     * Reads a single decrypted (and optionally decompressed) byte.
     *
     * @implNote ADAPTED: WAMediaCrypto.hmacAndDecrypt (streaming adaptation
     *           of batch decryption)
     * @return the next byte of decrypted data, or {@code -1} if the stream
     *         is exhausted and validated
     * @throws WhatsAppMediaException.Download if decryption, decompression, or
     *         validation fails
     */
    @Override
    public int read() throws WhatsAppMediaException.Download {
        if (isDone()) {
            return -1;
        } else if (isInflatable()) {
            return inflatedBuffer[inflatedOffset++] & 0xFF;
        } else {
            return buffer[bufferOffset++] & 0xFF;
        }
    }

    /**
     * Reads up to {@code len} decrypted (and optionally decompressed) bytes
     * into the specified array.
     *
     * @implNote ADAPTED: WAMediaCrypto.hmacAndDecrypt (streaming adaptation)
     * @param b   the destination buffer
     * @param off the start offset in the destination buffer
     * @param len the maximum number of bytes to read
     * @return the number of bytes read, or {@code -1} if the stream is
     *         exhausted and validated
     * @throws WhatsAppMediaException.Download if decryption, decompression, or
     *         validation fails
     */
    @Override
    public int read(byte[] b, int off, int len) throws WhatsAppMediaException.Download {
        if (isDone()) {
            return -1;
        } else if (isInflatable()) {
            var toRead = Math.min(len, inflatedLimit - inflatedOffset);
            System.arraycopy(inflatedBuffer, inflatedOffset, b, off, toRead);
            inflatedOffset += toRead;
            return toRead;
        } else {
            var toRead = Math.min(len, bufferLimit - bufferOffset);
            System.arraycopy(buffer, bufferOffset, b, off, toRead);
            bufferOffset += toRead;
            return toRead;
        }
    }

    /**
     * Drives the decryption state machine forward until output data is
     * available or the stream is exhausted.
     *
     * <p>The state machine transitions:
     * <ul>
     *   <li>{@link State#READ_DATA}: reads ciphertext, updates HMAC and
     *       digests, decrypts via AES-CBC, optionally inflates</li>
     *   <li>{@link State#READ_MAC}: reads the trailing 10-byte HMAC and
     *       updates the ciphertext digest</li>
     *   <li>{@link State#VALIDATE_ALL}: verifies the ciphertext hash, HMAC,
     *       and plaintext hash. Uses constant-time comparison for the HMAC
     *       to prevent timing attacks.</li>
     * </ul>
     *
     * @implNote WAMediaCrypto.hmacAndDecrypt:
     *           (1) validate HMAC size (10 <= a.length <= 32),
     *           (2) HMAC = hmacSha256(hmacKey, iv+ciphertext),
     *           (3) constant-time compare: N(hmac[0:10], mac),
     *           (4) decrypt: aesCbcDecrypt(cipherKey, iv, ciphertext),
     *           (5) plaintextHash = SHA-256(plaintext).
     *           WAWebCryptoDecryptMedia:
     *           (1) concat(iv, ciphertext), hmacSha256(macKey, data, 10),
     *           (2) arrayBuffersEqual comparison (via WACryptoPrimitives.verify),
     *           (3) aesCbcDecrypt, (4) optional plaintextHash check
     * @return {@code true} if the stream is fully consumed and validated,
     *         {@code false} if output data is available for reading
     * @throws WhatsAppMediaException.Download if any decryption or validation
     *         step fails
     */
    private boolean isDone() throws WhatsAppMediaException.Download {
        try {
            var inflatable = isInflatable();
            while ((inflatable ? inflatedOffset >= inflatedLimit : bufferOffset >= bufferLimit) && state != State.DONE) {
                if (inflatable && !inflater.needsInput() && !inflater.finished()) {
                    inflatedOffset = 0;
                    inflatedLimit = inflater.inflate(inflatedBuffer);
                } else {
                    switch (state) {
                        case READ_DATA -> {
                            if (remainingText > 0) {
                                var toRead = (int) Math.min(buffer.length, remainingText);
                                var read = rawInputStream.read(buffer, 0, toRead);
                                if (read == -1) {
                                    throw new WhatsAppMediaException.Download("Unexpected end of stream: expected " + remainingText + " more bytes");
                                }
                                remainingText -= read;

                                if (isEncrypted()) {
                                    if (ciphertextDigest != null) {
                                        ciphertextDigest.update(buffer, 0, read); // WAWebCryptoDecryptMedia: encFilehash covers ciphertext
                                    }

                                    mac.update(buffer, 0, read); // WAMediaCrypto.hmacAndDecrypt: HMAC(iv + ciphertext)

                                    bufferOffset = 0;
                                    bufferLimit = cipher.update(buffer, 0, read, buffer, 0); // WAMediaCrypto.hmacAndDecrypt: aesCbcDecrypt
                                } else {
                                    bufferOffset = 0;
                                    bufferLimit = read;
                                }

                                if (plaintextDigest != null) {
                                    plaintextDigest.update(buffer, 0, bufferLimit); // WAMediaCrypto.hmacAndDecrypt: SHA-256(plaintext)
                                }

                                if (inflatable) {
                                    inflater.setInput(buffer, 0, bufferLimit);

                                    inflatedOffset = 0;
                                    inflatedLimit = inflater.inflate(inflatedBuffer);
                                }
                            } else {
                                if (isEncrypted()) {
                                    bufferOffset = 0;
                                    bufferLimit = cipher.doFinal(buffer, 0); // WAMediaCrypto.hmacAndDecrypt: final decryption block

                                    if (plaintextDigest != null) {
                                        plaintextDigest.update(buffer, 0, bufferLimit);
                                    }

                                    if (inflatable) {
                                        inflater.setInput(buffer, 0, bufferLimit);

                                        inflatedOffset = 0;
                                        inflatedLimit = inflater.inflate(inflatedBuffer);
                                    }

                                    state = State.READ_MAC;
                                } else {
                                    if (!inflatable || inflater.finished()) {
                                        state = State.VALIDATE_ALL;
                                    }
                                }
                            }
                        }

                        case READ_MAC -> { // WAMediaCrypto.hmacAndDecrypt: extract mac bytes from payload
                            var toRead = MAC_LENGTH - macBufferOffset;
                            if (toRead > 0) {
                                var read = rawInputStream.read(macBuffer, macBufferOffset, toRead);
                                if (read == -1) {
                                    throw new WhatsAppMediaException.Download("Unexpected end of stream: expected " + toRead + " more bytes");
                                }
                                macBufferOffset += read;
                            }

                            if (macBufferOffset == MAC_LENGTH) {
                                if (ciphertextDigest != null) {
                                    ciphertextDigest.update(macBuffer); // WAWebCryptoEncryptMedia: SHA-256 covers ciphertext + hmac[0:10]
                                }

                                if (!inflatable || inflater.finished()) {
                                    state = State.VALIDATE_ALL;
                                }
                            }
                        }

                        case VALIDATE_ALL -> { // WAMediaCrypto.hmacAndDecrypt: verification phase
                            if (isEncrypted()) {
                                if (ciphertextDigest != null) { // WAWebCryptoDecryptMedia: optional encFilehash verification
                                    var actualCiphertextSha256 = ciphertextDigest.digest();
                                    if (!MessageDigest.isEqual(expectedCiphertextSha256, actualCiphertextSha256)) {
                                        throw new WhatsAppMediaException.Download("Ciphertext SHA256 hash doesn't match the expected value");
                                    }
                                }

                                // WAMediaCrypto.hmacAndDecrypt: constant-time HMAC comparison (N function)
                                // WAWebCryptoDecryptMedia: WACryptoUtils.arrayBuffersEqual -> WACryptoPrimitives.verify
                                var actualCiphertextMac = mac.doFinal();
                                if (!MessageDigest.isEqual(
                                        Arrays.copyOf(macBuffer, MAC_LENGTH),
                                        Arrays.copyOf(actualCiphertextMac, MAC_LENGTH))) {
                                    throw new WhatsAppMediaException.Download("Mac doesn't match the expected value");
                                }
                            }

                            if (plaintextDigest != null) { // WAMediaCrypto.hmacAndDecrypt: plaintextHash verification
                                var actualPlaintextSha256 = plaintextDigest.digest();
                                if (!MessageDigest.isEqual(expectedPlaintextSha256, actualPlaintextSha256)) {
                                    throw new WhatsAppMediaException.Download("Plaintext SHA256 hash doesn't match the expected value");
                                }
                            }

                            state = State.DONE;
                        }
                    }
                }
            }

            return state == State.DONE;
        } catch (IOException exception) {
            throw new WhatsAppMediaException.Download("Cannot read data", exception);
        } catch (GeneralSecurityException exception) {
            throw new WhatsAppMediaException.Download("Cannot decrypt data", exception);
        } catch (DataFormatException exception) {
            throw new WhatsAppMediaException.Download("Cannot inflate data", exception);
        }
    }

    /**
     * Returns whether this stream is processing encrypted media.
     *
     * @implNote WAMediaCrypto.hmacAndDecrypt (encrypted path requires cipher)
     * @return {@code true} if the cipher is initialized, {@code false} for
     *         unencrypted media
     */
    private boolean isEncrypted() {
        return cipher != null;
    }

    /**
     * Returns whether this stream applies zlib decompression after decryption.
     *
     * @implNote ADAPTED: WAWebMmsMediaTypes (inflatable flag on media types)
     * @return {@code true} if the inflater is initialized, {@code false}
     *         otherwise
     */
    private boolean isInflatable() {
        return inflater != null;
    }

    /**
     * Closes the underlying stream, the HTTP client, and the inflater (if
     * present).
     *
     * @implNote NO_WA_BASIS (Java resource management)
     * @throws IOException if an I/O error occurs while closing
     */
    @Override
    public void close() throws IOException {
        super.close();
        client.close();
        if (inflater != null) {
            inflater.end();
        }
    }

    /**
     * The states of the decryption and verification state machine.
     *
     * @implNote ADAPTED: WAMediaCrypto.hmacAndDecrypt (batch operation adapted
     *           to incremental streaming via state machine)
     */
    private enum State {
        /**
         * Reading and decrypting ciphertext bytes from the payload.
         *
         * @implNote WAMediaCrypto.hmacAndDecrypt: aesCbcDecrypt(cipherKey, iv, ciphertext)
         */
        READ_DATA,

        /**
         * Reading the trailing 10-byte HMAC from the payload.
         *
         * @implNote WAMediaCrypto.hmacAndDecrypt: mac extraction (a parameter)
         */
        READ_MAC,

        /**
         * Verifying the HMAC, ciphertext hash, and plaintext hash.
         *
         * @implNote WAMediaCrypto.hmacAndDecrypt: N(hmac[0:a.length], a) comparison,
         *           plaintextHash verification
         */
        VALIDATE_ALL,

        /**
         * All data consumed and verified; the stream is exhausted.
         *
         * @implNote WAMediaCrypto.hmacAndDecrypt: function return
         */
        DONE
    }
}
