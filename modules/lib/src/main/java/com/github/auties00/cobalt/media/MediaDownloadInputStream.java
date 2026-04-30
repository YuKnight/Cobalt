package com.github.auties00.cobalt.media;

import com.github.auties00.cobalt.exception.WhatsAppMediaException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
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
 * Streams, decrypts, and verifies a media payload downloaded from
 * WhatsApp's CDN.
 *
 * <p>The wire format delivered by the CDN has the shape
 * {@code ciphertext || HMAC[0:10]}: the ciphertext is AES-CBC encrypted
 * and the trailing 10 bytes are the truncated HMAC-SHA256 over
 * {@code IV || ciphertext}. The IV itself is not present on the wire;
 * it is reproduced from the media key on the recipient side via HKDF.
 *
 * <p>Reads drive an internal state machine through four stages:
 * <ol>
 *   <li>{@link State#READ_DATA}: read ciphertext, update the HMAC and
 *       digests, decrypt with AES-CBC.</li>
 *   <li>{@link State#READ_MAC}: pull the trailing 10-byte HMAC from
 *       the wire.</li>
 *   <li>{@link State#VALIDATE_ALL}: verify the encrypted-payload hash,
 *       the HMAC, and the plaintext hash.</li>
 *   <li>{@link State#DONE}: all data consumed and validated.</li>
 * </ol>
 *
 * <p>For inflatable media types (app-state blobs and history sync
 * payloads) the decrypted bytes are additionally decompressed with zlib
 * before they reach the caller.
 */
@WhatsAppWebModule(moduleName = "WAMediaCrypto")
@WhatsAppWebModule(moduleName = "WAWebCryptoDecryptMedia")
final class MediaDownloadInputStream extends MediaInputStream {
    /**
     * The HTTP client backing the download connection. Owned by this
     * stream and closed when the caller closes the stream so that the
     * underlying socket is released.
     */
    private final HttpClient client;

    /**
     * The zlib inflater for inflatable media types (app-state blobs and
     * history sync payloads), or {@code null} when no decompression is
     * needed.
     */
    @WhatsAppWebExport(moduleName = "WAWebMmsMediaTypes", exports = "MEDIA_TYPES",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private final Inflater inflater;

    /**
     * The primary working buffer used for reading ciphertext from the
     * raw stream and producing the in-place decrypted plaintext.
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "hmacAndDecrypt",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private final byte[] buffer;

    /**
     * The current read cursor within {@link #buffer}.
     */
    private int bufferOffset;

    /**
     * The number of valid decrypted bytes currently held by
     * {@link #buffer}.
     */
    private int bufferLimit;

    /**
     * The buffer holding decompressed output for inflatable media
     * types, or {@code null} when no decompression is needed.
     */
    private final byte[] inflatedBuffer;

    /**
     * The current read cursor within {@link #inflatedBuffer}.
     */
    private int inflatedOffset;

    /**
     * The number of valid decompressed bytes currently held by
     * {@link #inflatedBuffer}.
     */
    private int inflatedLimit;

    /**
     * The buffer accumulating the trailing HMAC bytes pulled from the
     * raw stream, or {@code null} for unencrypted media.
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "hmacAndDecrypt",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final byte[] macBuffer;

    /**
     * The number of HMAC bytes accumulated into {@link #macBuffer} so
     * far. Reaches {@link #MAC_LENGTH} once the trailer has been fully
     * read.
     */
    private int macBufferOffset;

    /**
     * The SHA-256 digest accumulating the decrypted plaintext, or
     * {@code null} when no expected plaintext hash is provided.
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "hmacAndDecrypt",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final MessageDigest plaintextDigest;

    /**
     * The expected SHA-256 of the plaintext used for integrity
     * verification, or {@code null} when no expected value is provided.
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "hmacAndDecrypt",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final byte[] expectedPlaintextSha256;

    /**
     * The SHA-256 digest accumulating the encrypted payload
     * ({@code ciphertext || HMAC[0:10]}), or {@code null} when no
     * expected ciphertext hash is provided.
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoDecryptMedia", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final MessageDigest ciphertextDigest;

    /**
     * The expected SHA-256 of the encrypted payload used for integrity
     * verification, or {@code null} when no expected value is provided.
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoDecryptMedia", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final byte[] expectedCiphertextSha256;

    /**
     * The AES-CBC cipher in decrypt mode, or {@code null} when the
     * media type does not use end-to-end encryption.
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "hmacAndDecrypt",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final Cipher cipher;

    /**
     * The HMAC-SHA256 instance verifying the authenticity of the
     * downloaded payload, or {@code null} for unencrypted media.
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "hmacAndDecrypt",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final Mac mac;

    /**
     * The number of ciphertext bytes remaining before the HMAC trailer
     * begins. Decremented as the raw stream is consumed.
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoDecryptMedia", exports = "default",
            adaptation = WhatsAppAdaptation.DIRECT)
    private long remainingText;

    /**
     * The current state of the decryption and verification state
     * machine.
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "hmacAndDecrypt",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private State state;

    /**
     * Constructs a new download stream that transparently decrypts and
     * verifies the payload from the given raw stream.
     *
     * <p>When the provider carries a media key and key name, HKDF
     * derives the IV, cipher key, and HMAC key and primes the HMAC
     * with the IV bytes so that the tag covers
     * {@code IV || ciphertext}. The payload is expected to carry
     * {@code payloadLength - MAC_LENGTH} ciphertext bytes followed by
     * the 10-byte HMAC trailer.
     *
     * <p>When the provider has no media key the stream passes through
     * the raw bytes without decryption, still optionally verifying the
     * plaintext SHA-256 hash when one is supplied.
     *
     * @param client         the HTTP client managing the download
     * @param rawInputStream the raw input stream from the CDN
     * @param payloadLength  the total payload length in bytes
     *                       (ciphertext plus HMAC trailer)
     * @param provider       the media provider with decryption metadata
     * @throws WhatsAppMediaException if key derivation or cipher
     *         initialisation fails
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "hmacAndDecrypt",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebCryptoDecryptMedia", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    MediaDownloadInputStream(HttpClient client, InputStream rawInputStream, long payloadLength, MediaProvider provider) throws WhatsAppMediaException {
        super(rawInputStream);
        Objects.requireNonNull(client, "client cannot be null");
        Objects.requireNonNull(rawInputStream, "rawInputStream must not be null");
        Objects.requireNonNull(provider, "provider must not be null");

        this.client = client;

        this.inflater = provider.mediaPath().inflatable() ? new Inflater() : null;

        this.buffer = new byte[BUFFER_LENGTH];
        this.inflatedBuffer = isInflatable() ? new byte[BUFFER_LENGTH] : null;

        this.expectedPlaintextSha256 = provider.mediaSha256().orElse(null);
        this.plaintextDigest = expectedPlaintextSha256 != null ? newHash() : null;

        var hasKeyName = provider.mediaPath().keyName().isPresent();
        var hasMediaKey = provider.mediaKey().isPresent();

        if (hasKeyName != hasMediaKey) {
            throw new WhatsAppMediaException.Download("Media key and key name must both be present or both be absent");
        } else if (hasKeyName) {
            this.expectedCiphertextSha256 = provider.mediaEncryptedSha256().orElse(null);
            this.ciphertextDigest = expectedCiphertextSha256 != null ? newHash() : null;

            var mediaKey = provider.mediaKey()
                    .orElseThrow(() -> new WhatsAppMediaException.Download("Media key must be present"));
            var keyName = provider.mediaPath().keyName()
                    .orElseThrow(() -> new WhatsAppMediaException.Download("Key name must be present"));

            var expanded = deriveMediaKeyData(mediaKey, keyName);
            var iv = new IvParameterSpec(expanded, 0, IV_LENGTH);
            var cipherKey = new SecretKeySpec(expanded, IV_LENGTH, KEY_LENGTH, "AES");
            var macKey = new SecretKeySpec(expanded, IV_LENGTH + KEY_LENGTH, KEY_LENGTH, "HmacSHA256");

            this.cipher = newCipher(Cipher.DECRYPT_MODE, cipherKey, iv);
            this.mac = newMac(macKey);

            // Prime the HMAC with the IV so the tag covers iv || ciphertext.
            this.mac.update(expanded, 0, IV_LENGTH);

            this.remainingText = payloadLength - MAC_LENGTH;
            this.macBuffer = new byte[MAC_LENGTH];
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
     * @return the next byte of decrypted data, or {@code -1} if the
     *         stream is exhausted and validated
     * @throws WhatsAppMediaException.Download if decryption,
     *         decompression, or validation fails
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "hmacAndDecrypt",
            adaptation = WhatsAppAdaptation.ADAPTED)
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
     * Reads up to {@code len} decrypted (and optionally decompressed)
     * bytes into the supplied array.
     *
     * @param b   the destination buffer
     * @param off the start offset in the destination buffer
     * @param len the maximum number of bytes to read
     * @return the number of bytes read, or {@code -1} if the stream is
     *         exhausted and validated
     * @throws WhatsAppMediaException.Download if decryption,
     *         decompression, or validation fails
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "hmacAndDecrypt",
            adaptation = WhatsAppAdaptation.ADAPTED)
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
     * Drives the decryption and verification state machine forward
     * until output data becomes available or the stream is fully
     * consumed and validated.
     *
     * @return {@code true} if the stream is fully consumed and
     *         validated, {@code false} if output data is available for
     *         reading
     * @throws WhatsAppMediaException.Download if any decryption or
     *         validation step fails
     */
    @WhatsAppWebExport(moduleName = "WAMediaCrypto", exports = "hmacAndDecrypt",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebCryptoDecryptMedia", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
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
                                        ciphertextDigest.update(buffer, 0, read);
                                    }

                                    mac.update(buffer, 0, read);

                                    bufferOffset = 0;
                                    bufferLimit = cipher.update(buffer, 0, read, buffer, 0);
                                } else {
                                    bufferOffset = 0;
                                    bufferLimit = read;
                                }

                                if (plaintextDigest != null) {
                                    plaintextDigest.update(buffer, 0, bufferLimit);
                                }

                                if (inflatable) {
                                    inflater.setInput(buffer, 0, bufferLimit);

                                    inflatedOffset = 0;
                                    inflatedLimit = inflater.inflate(inflatedBuffer);
                                }
                            } else {
                                if (isEncrypted()) {
                                    // Drain the cipher's final padded block.
                                    bufferOffset = 0;
                                    bufferLimit = cipher.doFinal(buffer, 0);

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

                        case READ_MAC -> {
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
                                    // encFilehash covers ciphertext and the 10-byte HMAC trailer.
                                    ciphertextDigest.update(macBuffer);
                                }

                                if (!inflatable || inflater.finished()) {
                                    state = State.VALIDATE_ALL;
                                }
                            }
                        }

                        case VALIDATE_ALL -> {
                            if (isEncrypted()) {
                                if (ciphertextDigest != null) {
                                    var actualCiphertextSha256 = ciphertextDigest.digest();
                                    if (!MessageDigest.isEqual(expectedCiphertextSha256, actualCiphertextSha256)) {
                                        throw new WhatsAppMediaException.Download("Ciphertext SHA256 hash doesn't match the expected value");
                                    }
                                }

                                // Constant-time comparison of the truncated HMAC trailer.
                                var actualCiphertextMac = mac.doFinal();
                                if (!MessageDigest.isEqual(
                                        Arrays.copyOf(macBuffer, MAC_LENGTH),
                                        Arrays.copyOf(actualCiphertextMac, MAC_LENGTH))) {
                                    throw new WhatsAppMediaException.Download("Mac doesn't match the expected value");
                                }
                            }

                            if (plaintextDigest != null) {
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
     * @return {@code true} if the cipher is initialised, {@code false}
     *         for unencrypted media
     */
    private boolean isEncrypted() {
        return cipher != null;
    }

    /**
     * Returns whether this stream applies zlib decompression after
     * decryption.
     *
     * @return {@code true} if the inflater is initialised,
     *         {@code false} otherwise
     */
    private boolean isInflatable() {
        return inflater != null;
    }

    /**
     * Closes the underlying raw stream, the owned HTTP client, and the
     * zlib inflater if one was allocated.
     *
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
     * The explicit states of the decryption and verification state
     * machine driven by {@link #isDone()}.
     */
    private enum State {
        /**
         * Reads ciphertext from the raw stream, decrypts it, and
         * optionally inflates the result for the caller.
         */
        READ_DATA,

        /**
         * Pulls the trailing 10-byte HMAC from the raw stream.
         */
        READ_MAC,

        /**
         * Verifies the encrypted-payload hash, the HMAC, and the
         * plaintext hash.
         */
        VALIDATE_ALL,

        /**
         * Marks the stream as fully consumed and validated.
         */
        DONE
    }
}
