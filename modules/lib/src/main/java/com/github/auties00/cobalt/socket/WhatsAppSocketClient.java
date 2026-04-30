package com.github.auties00.cobalt.socket;

import com.github.auties00.cobalt.proxy.WhatsAppProxy;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.exception.WhatsAppSessionException;
import com.github.auties00.cobalt.exception.WhatsAppStreamException;
import com.github.auties00.cobalt.model.device.DevicePlatformType;
import com.github.auties00.cobalt.model.device.DevicePropsBuilder;
import com.github.auties00.cobalt.model.device.DevicePropsHistorySyncConfigBuilder;
import com.github.auties00.cobalt.model.device.DevicePropsSpec;
import com.github.auties00.cobalt.model.device.pairing.*;
import com.github.auties00.cobalt.model.device.pairing.ClientPayload.DevicePairingRegistrationData;
import com.github.auties00.cobalt.model.device.pairing.ClientPayload.UserAgent;
import com.github.auties00.cobalt.model.signal.CertChainSpec;
import com.github.auties00.cobalt.model.signal.NoiseCertificateCertChainDetailsSpec;
import com.github.auties00.cobalt.model.signal.NoiseCertificateDetailsSpec;
import com.github.auties00.cobalt.model.signal.NoiseCertificateSpec;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.binary.NodeDecoder;
import com.github.auties00.cobalt.node.binary.NodeEncoder;
import com.github.auties00.cobalt.node.binary.NodeTokens;
import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.layer.application.websocket.WebSocketClientLayer;
import com.github.auties00.cobalt.socket.layer.application.whatsapp.WhatsAppSocketClientLayer;
import com.github.auties00.cobalt.socket.layer.security.SocketClientSecurityLayer;
import com.github.auties00.cobalt.socket.layer.transport.SocketClientTransportLayer;
import com.github.auties00.cobalt.socket.layer.tunnel.SocketClientTunnelLayer;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.cobalt.util.DataUtils;
import com.github.auties00.cobalt.util.GcmUtils;
import com.github.auties00.curve25519.Curve25519;
import com.github.auties00.libsignal.key.SignalIdentityKeyPair;
import com.github.auties00.libsignal.key.SignalIdentityPublicKey;
import it.auties.protobuf.stream.ProtobufInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Top-level WhatsApp socket client that performs the Noise XX handshake
 * and exchanges int24-prefixed encrypted datagrams with the WhatsApp
 * server.
 *
 * <p>The class is sealed into {@link Browser}, {@link Desktop} and
 * {@link Mobile} subtypes that pick the right transport stack and the
 * right handshake payload for each client form factor. Browser and
 * Windows hybrid companions tunnel everything through a WebSocket; the
 * macOS desktop and the native mobile clients run Noise directly over
 * TCP.
 *
 * <p>For Web clients the data flow is:
 * <pre>
 * Node -&gt; serialize -&gt; Noise encrypt + int24 prefix -&gt; WebSocket binary frame -&gt; TLS -&gt; TCP
 * </pre>
 *
 * <p>For Mobile clients the data flow is:
 * <pre>
 * Node -&gt; serialize -&gt; Noise encrypt + int24 prefix -&gt; TCP
 * </pre>
 */
public sealed abstract class WhatsAppSocketClient {
    /**
     * 32-byte Ed25519 public key of the WhatsApp Noise root CA.
     *
     * <p>Acts as the trust anchor for the two-level certificate chain
     * (root then intermediate then leaf) that the server presents
     * during the Noise handshake. The intermediate must have
     * {@code issuerSerial == 0} and be signed by this key; the leaf
     * must be signed by the intermediate and its embedded key must
     * match the server static key carried in the Noise hello.
     */
    private static final byte[] NOISE_ROOT_CA_PUBLIC_KEY = HexFormat.of().parseHex(
            "142375574d0a587166aae71ebe516437c4a28b73e3695c6ce1f7f9545da8ee6b"
    );

    /**
     * Two-byte WhatsApp protocol identifier ({@code "WA"}) prepended to
     * every handshake prologue.
     */
    private static final byte[] WHATSAPP_VERSION_HEADER = "WA".getBytes(StandardCharsets.UTF_8);

    /**
     * AES/GCM transformation used for both the handshake and the post
     * handshake datagram cipher.
     */
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    /**
     * Number of bytes in the int24 length prefix that frames every
     * datagram on the wire.
     */
    private static final int INT24_BYTE_SIZE = 3;

    /**
     * Size of the AES-GCM authentication tag appended to every
     * encrypted datagram.
     */
    private static final int GCM_TAG_BYTE_SIZE = 16;

    /**
     * Maximum length encodable in an unsigned int24 prefix.
     */
    private static final int MAX_MESSAGE_LENGTH = 0xFFFFFF;

    /**
     * Defensive upper bound on a single Noise handshake message.
     */
    private static final int MAX_HANDSHAKE_MESSAGE_LENGTH = 0xFFFF;

    /**
     * Builds a WhatsApp socket client tailored to the platform recorded
     * in {@code store}.
     *
     * <p>Web and Windows companions get a WebSocket-over-TLS stack; the
     * macOS desktop and the native mobile clients get raw TCP. The TLS
     * engine is the Chrome-style factory by default, which matches the
     * JA3 fingerprint WhatsApp expects.
     *
     * @param store the WhatsApp store carrying the platform, identity
     *              keys and proxy configuration
     * @return a socket client ready to {@link #connect(WhatsAppSocketListener)}
     */
    public static WhatsAppSocketClient newCipheredSocketClient(WhatsAppStore store) {
        return newCipheredSocketClient(store, WhatsAppSslEngineFactory.chrome());
    }

    /**
     * Variant of {@link #newCipheredSocketClient(WhatsAppStore)} that
     * accepts a custom {@link WhatsAppSslEngineFactory}.
     *
     * @param store            the WhatsApp store
     * @param sslEngineFactory factory used for both the proxy hop and
     *                         the end-to-end hop when TLS is required
     * @return a socket client wired with the supplied SSL engine factory
     */
    static WhatsAppSocketClient newCipheredSocketClient(WhatsAppStore store, WhatsAppSslEngineFactory sslEngineFactory) {
        Objects.requireNonNull(store, "store cannot be null");
        Objects.requireNonNull(sslEngineFactory, "sslEngineFactory cannot be null");

        var platform = store.device().platform();
        var transport = createTransport();
        var tunnelSecurity = createTunnelSecurity(transport, store, sslEngineFactory);
        var tunnel = createTunnel(tunnelSecurity, store);
        var transportSecurity = createTransportSecurity(tunnel, store, sslEngineFactory);

        return switch (platform) {
            case WEB, WINDOWS -> {
                var userAgent = store.device().toUserAgent(store.clientVersion());
                var webSocket = new WebSocketClientLayer(transportSecurity, "/ws/chat", userAgent);
                var whatsAppLayer = new WhatsAppSocketClientLayer(webSocket);
                yield new Browser(store, whatsAppLayer);
            }
            case MACOS -> {
                var whatsAppLayer = new WhatsAppSocketClientLayer(transportSecurity);
                yield new Desktop(store, whatsAppLayer);
            }
            default -> {
                var whatsAppLayer = new WhatsAppSocketClientLayer(transportSecurity);
                yield new Mobile(store, whatsAppLayer);
            }
        };
    }

    /**
     * Creates the bottom of the stack, a non-blocking TCP transport
     * registered with the central selector.
     *
     * @return the transport layer
     */
    private static SocketClientLayer<?> createTransport() {
        return SocketClientTransportLayer.newTcpTransport();
    }

    /**
     * Creates the security layer that protects the proxy hop, returning
     * a TLS layer for HTTPS proxies and a plain passthrough otherwise.
     *
     * @param transport     the transport layer below
     * @param store         the WhatsApp store, consulted for the proxy
     * @param engineFactory the factory used when TLS is required
     * @return the proxy-side security layer
     */
    private static SocketClientLayer<?> createTunnelSecurity(SocketClientLayer<?> transport, WhatsAppStore store, WhatsAppSslEngineFactory engineFactory) {
        return switch (store.proxy().orElse(null)) {
            case WhatsAppProxy.Http.Secure _ -> SocketClientSecurityLayer.newTls(transport, engineFactory);
            case null, default -> SocketClientSecurityLayer.newPlain(transport);
        };
    }

    /**
     * Creates the tunnel layer matching the configured proxy, falling
     * back to a direct (no-op) tunnel when none is configured.
     *
     * @param tunnelSecurity the security layer below
     * @param store          the WhatsApp store, consulted for the proxy
     * @return the tunnel layer
     */
    private static SocketClientLayer<?> createTunnel(SocketClientLayer<?> tunnelSecurity, WhatsAppStore store) {
        return switch (store.proxy().orElse(null)) {
            case WhatsAppProxy.Http http -> SocketClientTunnelLayer.newHttpTunnel(http, tunnelSecurity);
            case WhatsAppProxy.Socks socks -> SocketClientTunnelLayer.newSocksTunnel(socks, tunnelSecurity);
            case null -> SocketClientTunnelLayer.newDirectTunnel(tunnelSecurity);
        };
    }

    /**
     * Creates the security layer that protects the end-to-end hop.
     *
     * <p>Web and Windows companions wrap the tunnel in TLS because the
     * WebSocket upgrade runs over HTTPS; macOS and mobile clients use a
     * plain passthrough because the application layer adds its own
     * Noise XX encryption.
     *
     * @param tunnel        the tunnel layer below
     * @param store         the WhatsApp store, consulted for the platform
     * @param engineFactory the factory used when TLS is required
     * @return the end-to-end security layer
     */
    private static SocketClientLayer<?> createTransportSecurity(SocketClientLayer<?> tunnel, WhatsAppStore store, WhatsAppSslEngineFactory engineFactory) {
        var platform = store.device().platform();
        if (platform == ClientPlatformType.WEB || platform == ClientPlatformType.WINDOWS) {
            return SocketClientSecurityLayer.newTls(tunnel, engineFactory);
        }
        return SocketClientSecurityLayer.newPlain(tunnel);
    }

    /**
     * The WhatsApp store, owning the identity keys and platform metadata
     * required to assemble the handshake payload.
     */
    final WhatsAppStore store;

    /**
     * Topmost application layer; owns the per-connection layer context
     * and exposes the plumbing the Noise driver uses to read and write
     * during the handshake.
     */
    final WhatsAppSocketClientLayer whatsAppLayer;

    /**
     * Listener that receives deserialized nodes, errors and the close
     * event, set on every {@link #connect(WhatsAppSocketListener)}.
     */
    private WhatsAppSocketListener listener;

    /**
     * AES-GCM cipher used to encrypt outbound datagrams.
     */
    private volatile Cipher writeCipher;

    /**
     * AES write key derived from the Noise handshake.
     */
    private volatile SecretKeySpec writeKey;

    /**
     * Monotonic nonce counter for outbound datagrams; incremented under
     * the {@link #sendBinary(ByteBuffer...)} monitor on every send.
     */
    private long writeCounter;

    /**
     * AES-GCM cipher used to decrypt inbound datagrams.
     */
    private volatile Cipher readCipher;

    /**
     * AES read key derived from the Noise handshake.
     */
    private volatile SecretKeySpec readKey;

    /**
     * Monotonic nonce counter for inbound datagrams; incremented by the
     * decrypting listener as each datagram is consumed.
     */
    private long readCounter;

    /**
     * Reusable 3-byte int24 length prefix written before every outbound
     * datagram. Safe to share because {@link #sendBinary(ByteBuffer...)}
     * is {@code synchronized}.
     */
    private final ByteBuffer reusableLengthPrefix = ByteBuffer.allocate(INT24_BYTE_SIZE);

    /**
     * Reusable buffer for the GCM authentication tag produced by
     * {@link Cipher#doFinal(ByteBuffer, ByteBuffer)}, lazily sized once
     * the cipher is initialized. Safe to share for the same reason as
     * {@link #reusableLengthPrefix}.
     */
    private ByteBuffer reusableFinalChunk;

    /**
     * Wall-clock duration of the transport-open phase, captured between
     * the entry of {@link #connect(WhatsAppSocketListener)} and the
     * point at which the transport reports as open (the WebSocket
     * upgrade completion for browsers, the pre-tunnel TCP connect for
     * macOS and mobile).
     *
     * <p>Mirrors WA Web's {@code socket_open} QPL span and the
     * {@code WebcSocketConnectWamEvent.webcSocketConnectDuration} field.
     */
    private volatile Duration socketConnectDuration;

    /**
     * Wall-clock duration of the Noise XX handshake, captured from the
     * first byte of {@code ClientHello} until the read and write keys
     * have been derived.
     *
     * <p>Mirrors WA Web's {@code auth_handshake} QPL span and the
     * {@code WebcSocketConnectWamEvent.webcAuthHandshakeDuration} field.
     */
    private volatile Duration authHandshakeDuration;

    /**
     * Common constructor invoked by every subtype.
     *
     * @param store         the WhatsApp store
     * @param whatsAppLayer the topmost layer, already wrapping the full
     *                      transport, security and tunnel stack
     */
    private WhatsAppSocketClient(WhatsAppStore store, WhatsAppSocketClientLayer whatsAppLayer) {
        this.store = store;
        this.whatsAppLayer = whatsAppLayer;
    }

    /**
     * Opens the underlying connection and performs the Noise XX
     * handshake, dispatching decoded nodes to {@code listener}.
     *
     * <p>The subtype controls when the transport finishes connecting
     * relative to the handshake via {@link #connectImpl(Instant)}, since
     * Browser, Desktop and Mobile each need a different ordering.
     *
     * @param listener the callback that receives decoded nodes, errors
     *                 and the close event
     * @throws IOException if the connection or handshake fails
     */
    public final void connect(WhatsAppSocketListener listener) throws IOException {
        Objects.requireNonNull(listener, "listener cannot be null");
        this.listener = listener;

        var decryptingListener = new DecryptingListener();
        // Open the socket_open QPL span.
        var socketOpenStart = Instant.now();
        whatsAppLayer.connect(getEndpoint(), decryptingListener);
        connectImpl(socketOpenStart);
    }

    /**
     * Records that the transport-open phase has finished and stamps
     * {@link #socketConnectDuration} with the elapsed time.
     *
     * <p>Subtypes call this from {@link #connectImpl(Instant)} at the
     * point that corresponds to the WA Web {@code socket_open} QPL span
     * close.
     *
     * @param socketOpenStart the timestamp captured at the entry of
     *                        {@link #connect(WhatsAppSocketListener)}
     */
    final void markSocketOpenDone(Instant socketOpenStart) {
        this.socketConnectDuration = Duration.between(socketOpenStart, Instant.now());
    }

    /**
     * Returns the measured transport-open duration for the current
     * session.
     *
     * @return the duration, or {@code null} if the transport has not
     *         finished opening yet
     */
    public final Duration socketConnectDuration() {
        return socketConnectDuration;
    }

    /**
     * Returns the measured Noise handshake duration for the current
     * session.
     *
     * @return the duration, or {@code null} if the handshake has not
     *         finished yet
     */
    public final Duration authHandshakeDuration() {
        return authHandshakeDuration;
    }

    /**
     * Returns the remote endpoint that this client connects to.
     *
     * @return the remote endpoint, either the WebSocket endpoint for
     *         browsers or the TCP endpoint for desktop and mobile
     */
    abstract InetSocketAddress getEndpoint();

    /**
     * Sequences the Noise handshake against the transport.
     *
     * <p>Browser companions finish the connection (transitioning the
     * chain to async mode) before running the handshake because the
     * WebSocket upgrade has already happened. Desktop and Mobile run
     * the handshake first because their transport is still in
     * pre-tunnel mode and the handshake reads use the blocking-read
     * path.
     *
     * <p>Implementations must call {@link #markSocketOpenDone(Instant)}
     * before {@link #performNoiseHandshake()} so that
     * {@link #socketConnectDuration()} reports the correct value.
     *
     * @param socketOpenStart the timestamp captured at the entry of
     *                        {@link #connect(WhatsAppSocketListener)}
     * @throws IOException if the handshake fails
     */
    abstract void connectImpl(Instant socketOpenStart) throws IOException;

    /**
     * Disconnects from the server, destroys the AES read and write keys
     * and resets the nonce counters.
     *
     * <p>WA Web relies on the browser's garbage collector to dispose of
     * the keys; Cobalt destroys them eagerly via
     * {@link SecretKeySpec#destroy()} so the secrets do not linger in
     * heap memory beyond the lifetime of the connection.
     */
    public final void disconnect() {
        if (readKey != null) {
            try {
                readKey.destroy();
            } catch (DestroyFailedException _) {
            }
            readKey = null;
        }
        readCounter = 0;

        if (writeKey != null) {
            try {
                writeKey.destroy();
            } catch (DestroyFailedException _) {
            }
            writeKey = null;
        }
        writeCounter = 0;

        whatsAppLayer.disconnect();
    }

    /**
     * Returns whether the underlying connection is currently open.
     *
     * @return {@code true} if connected, {@code false} otherwise
     */
    public final boolean isConnected() {
        return whatsAppLayer.isConnected();
    }

    /**
     * Encrypts the given plaintext buffers with AES-GCM and writes them
     * to the server, prefixed with the int24 ciphertext length header.
     *
     * <p>Before the handshake completes (the write key is still
     * {@code null}), the buffers are forwarded as is without
     * encryption. The method is {@code synchronized} so the write
     * counter and the reusable header buffer can be used by concurrent
     * senders without a race.
     *
     * <p>WA Web splits the same operation across {@code NoiseSocket.sendFrame}
     * (encryption) and {@code FrameSocket.sendFrame} (int24 prefix);
     * Cobalt collapses both into a single method.
     *
     * @param buffers plaintext buffers in send order
     * @throws IOException if the payload exceeds int24, the cipher
     *         fails, or the underlying write fails
     */
    public final synchronized void sendBinary(ByteBuffer... buffers) throws IOException {
        if (writeKey == null) {
            sendRaw(buffers);
            return;
        }

        var plaintextLength = 0;
        var payloadCount = 0;
        for (var buf : buffers) {
            if (buf != null && buf.hasRemaining()) {
                try {
                    plaintextLength = Math.addExact(plaintextLength, buf.remaining());
                } catch (ArithmeticException e) {
                    throw new IOException("Cannot encrypt plaintext: payload length overflow", e);
                }
                payloadCount++;
            }
        }
        if (payloadCount == 0) {
            return;
        }

        try {
            writeCipher.init(
                    Cipher.ENCRYPT_MODE,
                    writeKey,
                    GcmUtils.createNonce(writeCounter++)
            );
        } catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new IOException("Cannot initialize write cipher", e);
        }

        try {
            var ciphertextLength = writeCipher.getOutputSize(plaintextLength);
            if (ciphertextLength > MAX_MESSAGE_LENGTH) {
                throw new IOException("Cannot encrypt plaintext: ciphertext length exceeds int24");
            }

            var output = new ByteBuffer[payloadCount + 2];
            reusableLengthPrefix.clear();
            reusableLengthPrefix.put((byte) ((ciphertextLength >> 16) & 0xFF));
            reusableLengthPrefix.put((byte) ((ciphertextLength >> 8) & 0xFF));
            reusableLengthPrefix.put((byte) (ciphertextLength & 0xFF));
            reusableLengthPrefix.flip();
            output[0] = reusableLengthPrefix;
            var outputIndex = 1;
            var producedCipherBytes = 0;

            for (var buffer : buffers) {
                if (buffer == null || !buffer.hasRemaining()) {
                    continue;
                }

                var encryptedSegment = encryptSegmentInPlace(buffer);
                producedCipherBytes += encryptedSegment.remaining();
                output[outputIndex++] = encryptedSegment;
            }

            var finalSize = writeCipher.getOutputSize(0);
            if (reusableFinalChunk == null || reusableFinalChunk.capacity() < finalSize) {
                reusableFinalChunk = ByteBuffer.allocate(finalSize);
            }
            reusableFinalChunk.clear();
            var finalProduced = writeCipher.doFinal(DataUtils.EMPTY_BYTE_BUFFER, reusableFinalChunk);
            reusableFinalChunk.flip();
            output[outputIndex] = reusableFinalChunk;

            if (producedCipherBytes + finalProduced != ciphertextLength) {
                throw new IOException(
                        "Cannot encrypt plaintext: produced ciphertext length mismatch"
                );
            }

            sendRaw(output);
        } catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
            throw new IOException("Cannot encrypt plaintext", e);
        }
    }

    /**
     * Encrypts a single plaintext segment in place inside {@code source}.
     *
     * <p>WA Web encrypts the entire payload at once through WebCrypto's
     * {@code subtle.encrypt}; Cobalt encrypts each segment individually
     * so each buffer can be reused as both input and output without an
     * intermediate allocation.
     *
     * @param source the source buffer; must be writable
     * @return a read-mode view over the encrypted bytes
     * @throws ShortBufferException if the output buffer is too small
     * @throws IOException          if the source buffer is read-only
     */
    private ByteBuffer encryptSegmentInPlace(ByteBuffer source) throws ShortBufferException, IOException {
        if (source.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }

        var start = source.position();
        var inputView = source.duplicate();
        var outputView = source.duplicate();
        outputView.position(start);
        outputView.limit(source.limit());

        var produced = writeCipher.update(inputView, outputView);
        var encrypted = source.duplicate();
        encrypted.position(start);
        encrypted.limit(start + produced);
        source.position(source.limit());
        return encrypted;
    }

    /**
     * Forwards raw buffers down the layer stack without encryption,
     * used during the handshake phase before the cipher keys exist.
     *
     * @param buffers the buffers to send
     * @throws IOException if the write fails
     */
    final void sendRaw(ByteBuffer... buffers) throws IOException {
        whatsAppLayer.sendBinary(buffers);
    }

    /**
     * Runs the full Noise XX handshake against the connected server.
     *
     * <p>Sends the {@code ClientHello}, processes the server's
     * {@code ServerHello}, verifies the certificate chain via
     * {@link #verifyCertificateChain(byte[], byte[])}, sends the
     * {@code ClientFinish} and finally splits the derived 64-byte key
     * material into the read and write AES keys.
     *
     * <p>Handshake reads use {@link WhatsAppSocketClientLayer#readBinary}
     * which routes through the selector's chain-tail walk; any outer
     * TLS layer below has already decrypted the bytes by the time they
     * reach the WhatsApp layer context.
     *
     * @throws IOException if the handshake fails or any of its
     *         underlying I/O operations does
     */
    final void performNoiseHandshake() throws IOException {
        // Open the auth_handshake QPL span; closed at the end of this method.
        var handshakeStart = Instant.now();
        var ephemeralKeyPair = SignalIdentityKeyPair.random();
        var prologue = getHandshakePrologue();

        try (var handshake = new WhatsAppSocketHandshake(prologue)) {
            // Send client hello
            var clientHello = new HandshakeMessageClientHelloBuilder()
                    .ephemeral(ephemeralKeyPair.publicKey().toEncodedPoint())
                    .build();
            var handshakeMessage = new HandshakeMessageBuilder()
                    .clientHello(clientHello)
                    .build();
            var requestBytes = HandshakeMessageSpec.encode(handshakeMessage);
            handshake.updateHash(ephemeralKeyPair.publicKey().toEncodedPoint());
            sendHandshakeMessage(ByteBuffer.wrap(prologue), requestBytes);

            // Read server hello
            var serverHelloPayload = readHandshakeMessage();

            // Process server hello
            var serverHandshake = HandshakeMessageSpec.decode(ProtobufInputStream.fromBuffer(serverHelloPayload));
            var serverHello = serverHandshake.serverHello()
                    .orElseThrow(() -> new IOException("Missing server hello"));

            var ephemeral = serverHello.ephemeral()
                    .orElseThrow(() -> new IOException("Missing server ephemeral key"));
            handshake.updateHash(ephemeral);

            var sharedEphemeral = Curve25519.sharedKey(
                    ephemeralKeyPair.privateKey().toEncodedPoint(), ephemeral
            );
            handshake.mixIntoKey(sharedEphemeral);

            var staticText = serverHello._static()
                    .orElseThrow(() -> new IOException("Missing server static key"));
            var decodedStaticText = handshake.cipher(staticText, false);

            var sharedStatic = Curve25519.sharedKey(
                    ephemeralKeyPair.privateKey().toEncodedPoint(), decodedStaticText
            );
            handshake.mixIntoKey(sharedStatic);

            var payload = serverHello.payload()
                    .orElseThrow(() -> new IOException("Missing server payload"));
            var decryptedCertificate = handshake.cipher(payload, false);

            // Verify certificate chain
            verifyCertificateChain(decryptedCertificate, decodedStaticText);

            // Send client finish
            var noiseKeyPair = store.noiseKeyPair();
            var encodedKey = handshake.cipher(noiseKeyPair.publicKey().toEncodedPoint(), true);
            var sharedPrivate = Curve25519.sharedKey(
                    noiseKeyPair.privateKey().toEncodedPoint(), ephemeral
            );
            handshake.mixIntoKey(sharedPrivate);

            var encodedPayload = handshake.cipher(getHandshakePayload(), true);
            var clientFinish = new HandshakeMessageClientFinishBuilder()
                    ._static(encodedKey)
                    .payload(encodedPayload)
                    .build();
            var clientHandshake = new HandshakeMessageBuilder()
                    .clientFinish(clientFinish)
                    .build();
            var finishBytes = HandshakeMessageSpec.encode(clientHandshake);
            sendHandshakeMessage(null, finishBytes);

            // Derive read/write keys
            var keys = handshake.finish();

            this.writeCipher = Cipher.getInstance(ALGORITHM);
            this.writeCounter = 0;
            this.writeKey = new SecretKeySpec(keys, 0, 32, "AES");

            this.readCipher = Cipher.getInstance(ALGORITHM);
            this.readCounter = 0;
            this.readKey = new SecretKeySpec(keys, 32, 32, "AES");

            // Close the auth_handshake QPL span now that both keys are derived.
            this.authHandshakeDuration = Duration.between(handshakeStart, Instant.now());
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Noise handshake failure", e);
        }
    }

    /**
     * Writes a single handshake message preceded by an int24 length
     * prefix and an optional prologue.
     *
     * @param prologue     the prologue to prepend, or {@code null} to
     *                     omit it (every message after the first one)
     * @param messageBytes the serialized handshake message
     * @throws IOException if the write fails
     */
    private void sendHandshakeMessage(ByteBuffer prologue, byte[] messageBytes) throws IOException {
        var lengthPrefix = ByteBuffer.allocate(INT24_BYTE_SIZE);
        var len = messageBytes.length;
        lengthPrefix.put((byte) ((len >> 16) & 0xFF));
        lengthPrefix.put((byte) ((len >> 8) & 0xFF));
        lengthPrefix.put((byte) (len & 0xFF));
        lengthPrefix.flip();
        if (prologue != null) {
            sendRaw(prologue, lengthPrefix, ByteBuffer.wrap(messageBytes));
        } else {
            sendRaw(lengthPrefix, ByteBuffer.wrap(messageBytes));
        }
    }

    /**
     * Reads a single handshake message off the wire.
     *
     * <p>Reads the int24 length prefix first, validates it, then reads
     * exactly that many payload bytes through the selector's pending
     * read mechanism.
     *
     * @return the payload buffer in read mode
     * @throws IOException if the read fails or the declared length is
     *         invalid
     */
    private ByteBuffer readHandshakeMessage() throws IOException {
        var lengthBuf = ByteBuffer.allocate(INT24_BYTE_SIZE);
        var bytesRead = whatsAppLayer.readBinary(lengthBuf, true);
        if (bytesRead < INT24_BYTE_SIZE) {
            throw new IOException("Failed to read handshake message length");
        }

        lengthBuf.flip();
        var length = ((lengthBuf.get() & 0xFF) << 16)
                | ((lengthBuf.get() & 0xFF) << 8)
                | (lengthBuf.get() & 0xFF);
        if (length <= 0) {
            throw new IOException("Invalid handshake message length: " + length);
        }
        if (length > MAX_HANDSHAKE_MESSAGE_LENGTH) {
            throw new IOException("Handshake message too large: " + length + " bytes (max " + MAX_HANDSHAKE_MESSAGE_LENGTH + ")");
        }

        var payloadBuf = ByteBuffer.allocate(length);
        bytesRead = whatsAppLayer.readBinary(payloadBuf, true);
        if (bytesRead < length) {
            throw new IOException("Failed to read handshake message payload");
        }

        payloadBuf.flip();
        return payloadBuf;
    }

    /**
     * Verifies the server certificate carried in the Noise handshake.
     *
     * <p>Web and Desktop companions receive a two-level chain
     * (intermediate plus leaf) and call into the chain verifier; Mobile
     * receives a flat {@code NoiseCertificate}. Each subtype overrides
     * this method with the matching verification.
     *
     * @param decryptedCertificate the decrypted certificate payload
     * @param serverStaticKey      the 32-byte server static public key
     *                             extracted from the Noise hello
     * @throws IOException if the certificate is malformed or invalid
     */
    abstract void verifyCertificateChain(byte[] decryptedCertificate, byte[] serverStaticKey) throws IOException;

    /**
     * Validates that a Curve25519 signature is exactly 64 bytes.
     *
     * @param signature the raw signature bytes
     * @return {@code signature}, returned for fluent chaining
     * @throws IOException if the signature is not exactly 64 bytes long
     */
    private static byte[] ensureSignatureSize(byte[] signature) throws IOException {
        if (signature.length != 64) {
            throw new IOException("Certificate signature has invalid length: " + signature.length + ", expected 64");
        }
        return signature;
    }

    /**
     * Returns the handshake prologue for the current client type.
     *
     * <p>Each subtype returns a defensive copy because the bytes are
     * mixed into the running hash and must not be mutated by the
     * caller.
     *
     * @return the prologue bytes
     */
    abstract byte[] getHandshakePrologue();

    /**
     * Builds the serialized client payload sent inside the
     * {@code ClientFinish} message.
     *
     * @return the serialized client payload
     */
    abstract byte[] getHandshakePayload();

    /**
     * Serializes a {@link Node} and sends it as one encrypted datagram.
     *
     * @param node the node to send
     * @throws IOException if serialization or the underlying write fails
     */
    public final void sendNode(Node node) throws IOException {
        var encoded = new byte[NodeEncoder.sizeOf(node)];
        var length = NodeEncoder.encode(node, encoded, 0, encoded.length);
        sendBinary(ByteBuffer.wrap(encoded, 0, length));
    }

    /**
     * Listener that decrypts each inbound datagram, parses it into one
     * or more {@link Node} instances and forwards them to the
     * application-supplied {@link WhatsAppSocketListener}.
     *
     * <p>WA Web wires the equivalent pipeline as the {@code NoiseSocket}
     * {@code onFrame} callback hooked into the underlying frame socket;
     * Cobalt installs the same logic as a listener on the Whatsapp
     * application layer context.
     */
    class DecryptingListener implements SocketClientLayerListener {
        /**
         * Decrypts a single inbound datagram and dispatches every
         * decoded node to the application listener.
         *
         * <p>A bad MAC tears down the connection through
         * {@link WhatsAppSessionException.BadMac}; any other failure
         * surfaces as
         * {@link com.github.auties00.cobalt.exception.WhatsAppStreamException.MalformedNode}.
         *
         * @param datagram the raw inbound datagram, in read mode
         */
        @Override
        public void onDatagram(ByteBuffer datagram) {
            try {
                var plaintext = decrypt(datagram);
                if (plaintext == null) {
                    return;
                }

                try(var decoder = NodeDecoder.of(plaintext)) {
                    while (decoder.hasData()) {
                        var node = decoder.decode();
                        listener.onNode(node);
                    }
                }
            } catch (WhatsAppSessionException.BadMac e) {
                listener.onError(e);
                disconnect();
            } catch (Exception e) {
                listener.onError(new WhatsAppStreamException.MalformedNode("Failed to process inbound datagram", e));
            }
        }

        /**
         * Decrypts a datagram with AES-GCM under the read key.
         *
         * <p>Before the handshake completes (the read key is still
         * {@code null}), the datagram is returned unchanged. After the
         * handshake the nonce is derived from the monotonic read
         * counter so consecutive datagrams cannot reuse a nonce.
         *
         * @param datagram the encrypted datagram, in read mode
         * @return a read-mode view over the decrypted plaintext, or the
         *         original buffer if the read key is not yet set
         * @throws WhatsAppSessionException.BadMac if the GCM tag does
         *         not authenticate or the datagram is too short to
         *         contain a tag
         */
        private ByteBuffer decrypt(ByteBuffer datagram) {
            if (readKey == null) {
                return datagram;
            }

            try {
                readCipher.init(
                        Cipher.DECRYPT_MODE,
                        readKey,
                        GcmUtils.createNonce(readCounter++)
                );
                if (datagram.remaining() <= GCM_TAG_BYTE_SIZE) {
                    throw new WhatsAppSessionException.BadMac("Datagram too short for GCM tag");
                }

                if (datagram.isReadOnly()) {
                    throw new ReadOnlyBufferException();
                }

                var start = datagram.position();
                var inputView = datagram.duplicate();
                var outputView = datagram.duplicate();
                outputView.position(start);
                outputView.limit(datagram.limit() - GCM_TAG_BYTE_SIZE);
                var produced = readCipher.doFinal(inputView, outputView);

                var plaintext = datagram.duplicate();
                plaintext.position(start);
                plaintext.limit(start + produced);
                return plaintext;
            } catch (GeneralSecurityException e) {
                throw new WhatsAppSessionException.BadMac("AES-GCM decryption failed", e);
            }
        }

        /**
         * Forwards the close event to the application listener.
         *
         * <p>The listener executor is drained by the WhatsApp layer
         * context's {@code onDisconnect} before this method runs, so by
         * the time the application sees the close event no further
         * datagrams are in flight.
         */
        @Override
        public void onClose() {
            listener.onClose();
        }
    }

    /**
     * Shared base for every companion-device socket client.
     *
     * <p>{@link Browser} (WebSocket over TLS) and {@link Desktop} (raw
     * TCP for the macOS app) share the {@code WEB_PROLOGUE}, the
     * two-level certificate chain verification against the WhatsApp
     * root CA, and the handshake payload shape (user agent plus
     * {@code webInfo} block plus either login credentials or
     * registration data with companion device props).
     *
     * <p>Subtypes diverge on the endpoint, the transport stack, the
     * exact sequencing of {@code connectImpl} relative to
     * {@code finishConnect} and the {@link #getWebSubPlatform()}
     * advertised inside the {@code webInfo} block.
     */
    static abstract sealed class Web extends WhatsAppSocketClient
            permits Browser, Desktop {
        /**
         * Two-byte web version footer appended to {@link #WHATSAPP_VERSION_HEADER}
         * to form the handshake prologue.
         */
        private static final byte[] WEB_VERSION = new byte[]{6, NodeTokens.DICTIONARY_VERSION};

        /**
         * Handshake prologue shared by every companion client.
         */
        private static final byte[] WEB_PROLOGUE = DataUtils.concatByteArrays(WHATSAPP_VERSION_HEADER, WEB_VERSION);

        /**
         * Common constructor invoked by the companion subtypes.
         *
         * @param store         the WhatsApp store
         * @param whatsAppLayer the topmost layer
         */
        Web(WhatsAppStore store, WhatsAppSocketClientLayer whatsAppLayer) {
            super(store, whatsAppLayer);
        }

        /**
         * Returns the {@code ClientPlatformType} that this client
         * advertises in the user agent.
         *
         * @return the platform value advertised at handshake time
         */
        abstract ClientPlatformType getPlatform();

        /**
         * Returns the {@code WebSubPlatform} that this client advertises
         * inside the {@code webInfo} block.
         *
         * <p>Browsers report {@code WEB_BROWSER}; native desktop apps
         * report one of {@code DARWIN}, {@code WIN_HYBRID}, etc.
         *
         * @return the sub-platform value advertised at handshake time
         */
        abstract ClientPayload.WebInfo.WebSubPlatform getWebSubPlatform();

        @Override
        final byte[] getHandshakePrologue() {
            return WEB_PROLOGUE.clone();
        }

        /**
         * Verifies a two-level companion certificate chain (intermediate
         * plus leaf) against the embedded root CA.
         *
         * @param decryptedCertificate the decrypted certificate payload
         * @param serverStaticKey      the 32-byte server static key
         * @throws IOException if the chain is malformed or any signature
         *         fails to verify
         */
        @Override
        final void verifyCertificateChain(byte[] decryptedCertificate, byte[] serverStaticKey) throws IOException {
            var certChain = CertChainSpec.decode(decryptedCertificate);
            var intermediate = certChain.intermediate()
                    .orElseThrow(() -> new IOException("Certificate chain missing intermediate certificate"));
            var leaf = certChain.leaf()
                    .orElseThrow(() -> new IOException("Certificate chain missing leaf certificate"));

            var intermediateDetails = intermediate.details()
                    .orElseThrow(() -> new IOException("Intermediate certificate missing details"));
            var intermediateSignature = intermediate.signature()
                    .orElseThrow(() -> new IOException("Intermediate certificate missing signature"));
            var parsedIntermediate = NoiseCertificateCertChainDetailsSpec.decode(intermediateDetails);

            var intermediateIssuerSerial = parsedIntermediate.issuerSerial()
                    .orElseThrow(() -> new IOException("Intermediate certificate missing issuerSerial"));
            if (intermediateIssuerSerial != 0) {
                throw new IOException("Intermediate certificate was not issued by root CA, issuerSerial: " + intermediateIssuerSerial);
            }

            if (!Curve25519.verifySignature(NOISE_ROOT_CA_PUBLIC_KEY, intermediateDetails, ensureSignatureSize(intermediateSignature))) {
                throw new IOException("Intermediate certificate has invalid signature");
            }

            var leafDetails = leaf.details()
                    .orElseThrow(() -> new IOException("Leaf certificate missing details"));
            var leafSignature = leaf.signature()
                    .orElseThrow(() -> new IOException("Leaf certificate missing signature"));
            var parsedLeaf = NoiseCertificateCertChainDetailsSpec.decode(leafDetails);

            var leafIssuerSerial = parsedLeaf.issuerSerial()
                    .orElseThrow(() -> new IOException("Leaf certificate missing issuerSerial"));
            var intermediateSerial = parsedIntermediate.serial()
                    .orElseThrow(() -> new IOException("Intermediate certificate missing serial"));
            if (leafIssuerSerial != intermediateSerial) {
                throw new IOException("Leaf certificate was not issued by intermediate");
            }

            var intermediateKey = parsedIntermediate.key()
                    .orElseThrow(() -> new IOException("Intermediate certificate missing key"));
            if (!Curve25519.verifySignature(intermediateKey, leafDetails, ensureSignatureSize(leafSignature))) {
                throw new IOException("Leaf certificate has invalid signature");
            }

            var leafKey = parsedLeaf.key()
                    .orElseThrow(() -> new IOException("Leaf certificate missing key"));
            if (!Arrays.equals(leafKey, serverStaticKey)) {
                throw new IOException("Leaf certificate key does not match handshake server static key");
            }
        }

        /**
         * Builds the serialized companion-device handshake payload by
         * choosing between login and registration based on whether the
         * store already carries a JID.
         *
         * @return the serialized client payload
         */
        @Override
        final byte[] getHandshakePayload() {
            var agent = getUserAgent();
            var payload = getClientPayload(agent);
            return ClientPayloadSpec.encode(payload);
        }

        /**
         * Builds the user agent advertised inside the handshake payload.
         *
         * @return the user agent value
         */
        private UserAgent getUserAgent() {
            var appVersion = store.clientVersion();
            var mcc = "000";
            var mnc = "000";
            // On the Windows hybrid shell the six-digit windowsBuild
            // URL parameter is copied into appVersion.quaternary and,
            // when six characters long, its halves overwrite mcc and
            // mnc. The quaternary is already set on the cached
            // ClientAppVersion by WhatsAppWindowsClientInfo, so this
            // block only mirrors the mcc/mnc override.
            if (store.device().platform() == ClientPlatformType.WINDOWS
                    && appVersion.quaternary().isPresent()) {
                var buildStr = Integer.toString(appVersion.quaternary().getAsInt());
                if (buildStr.length() == 6) {
                    mcc = buildStr.substring(0, 3);
                    mnc = buildStr.substring(3, 6);
                }
            }
            return new ClientPayloadUserAgentBuilder()
                    .platform(getPlatform())
                    .appVersion(appVersion)
                    .mcc(mcc)
                    .mnc(mnc)
                    .releaseChannel(store.releaseChannel())
                    .localeLanguageIso6391("en")
                    .localeCountryIso31661Alpha2("US")
                    .deviceType(ClientPayload.ClientType.PHONE)
                    .deviceModelType(store.device().modelId())
                    .build();
        }

        /**
         * Builds the companion-device client payload.
         *
         * <p>Returns a reconnection payload when the store already
         * carries a JID, otherwise a fresh pairing payload that
         * includes registration data. Either way the {@code webInfo}
         * sub-platform is the value returned by
         * {@link #getWebSubPlatform()}.
         *
         * @param agent the user agent value
         * @return the client payload
         */
        private ClientPayload getClientPayload(UserAgent agent) {
            var webInfo = new ClientPayloadWebInfoBuilder()
                    .webSubPlatform(getWebSubPlatform())
                    .build();
            var jid = store.jid();
            if (jid.isPresent()) {
                return new ClientPayloadBuilder()
                        .connectType(ClientPayload.ConnectType.WIFI_UNKNOWN)
                        .connectReason(ClientPayload.ConnectReason.USER_ACTIVATED)
                        .userAgent(agent)
                        .webInfo(webInfo)
                        .username(Long.parseLong(jid.get().user()))
                        .passive(true)
                        .pull(true)
                        .device(jid.get().device())
                        .build();
            } else {
                return new ClientPayloadBuilder()
                        .connectType(ClientPayload.ConnectType.WIFI_UNKNOWN)
                        .connectReason(ClientPayload.ConnectReason.USER_ACTIVATED)
                        .userAgent(agent)
                        .webInfo(webInfo)
                        .devicePairingData(createRegisterData())
                        .passive(false)
                        .pull(false)
                        .build();
            }
        }

        /**
         * Creates the device-pairing registration data for a new
         * companion-device session, including the encoded device
         * properties.
         *
         * @return the registration data
         */
        private DevicePairingRegistrationData createRegisterData() {
            return new ClientPayloadDevicePairingRegistrationDataBuilder()
                    .buildHash(store.clientVersion().toHash())
                    .eRegid(DataUtils.intToBytes(store.registrationId(), 4))
                    .eKeytype(DataUtils.intToBytes(SignalIdentityPublicKey.type(), 1))
                    .eIdent(store.identityKeyPair().publicKey().toEncodedPoint())
                    .eSkeyId(DataUtils.intToBytes(store.signedKeyPair().id(), 3))
                    .eSkeyVal(store.signedKeyPair().publicKey().toEncodedPoint())
                    .eSkeySig(store.signedKeyPair().signature())
                    .deviceProps(createCompanionProps())
                    .build();
        }

        /**
         * Creates and encodes the companion device properties.
         *
         * <p>Carries the history sync configuration flags that tell the
         * server which categories of history data to deliver to this
         * companion. The {@code platformType} is derived from the
         * store's device platform.
         *
         * @return the encoded companion device properties
         */
        private byte[] createCompanionProps() {
            var historyLength = store.webHistoryPolicy()
                    .orElse(WhatsAppWebClientHistory.standard(true));
            var config = new DevicePropsHistorySyncConfigBuilder()
                    .inlineInitialPayloadInE2EeMsg(true)
                    .supportBotUserAgentChatHistory(true)
                    .supportCagReactionsAndPolls(true)
                    .supportRecentSyncChunkMessageCountTuning(true)
                    .supportHostedGroupMsg(true)
                    .supportBizHostedMsg(true)
                    .supportFbidBotChatHistory(true)
                    .supportMessageAssociation(true)
                    .supportCallLogHistory(store.device().platform() == ClientPlatformType.WINDOWS)
                    .supportGroupHistory(true)
                    .storageQuotaMb(historyLength.size())
                    .fullSyncSizeMbLimit(historyLength.size())
                    .build();
            var platformType = switch (store.device().platform()) {
                case IOS, IOS_BUSINESS -> DevicePlatformType.IOS_PHONE;
                case ANDROID, ANDROID_BUSINESS -> DevicePlatformType.ANDROID_PHONE;
                case WINDOWS -> DevicePlatformType.UWP;
                case MACOS -> DevicePlatformType.IOS_CATALYST;
                case WEB -> DevicePlatformType.CHROME;
                default -> throw new IllegalStateException("Unexpected value: " + store.device().platform());
            };
            var props = new DevicePropsBuilder()
                    .os(store.name())
                    .platformType(platformType)
                    .requireFullSync(historyLength.isExtended())
                    .historySyncConfig(config)
                    .version(store.clientVersion())
                    .build();
            return DevicePropsSpec.encode(props);
        }
    }

    /**
     * Companion client for browser tabs and the Windows hybrid desktop
     * shell.
     *
     * <p>Connects via WebSocket over TLS to {@code web.whatsapp.com:443},
     * performs the HTTP upgrade inside
     * {@link WhatsAppSocketClientLayer#connect} and then runs the Noise
     * handshake through the async chain.
     *
     * <p>Browser and Windows hybrid share this class because the hybrid
     * shell ships the same JavaScript bundle and reuses the same
     * endpoint ({@code wss://web.whatsapp.com/ws/chat}); only the
     * handshake payload changes, which is expressed through
     * {@link #getWebSubPlatform()}.
     */
    static final class Browser extends Web {
        /**
         * Shared WebSocket endpoint for browser and Windows hybrid
         * companions.
         */
        private static final InetSocketAddress WEB_SOCKET_ENDPOINT = new InetSocketAddress("web.whatsapp.com", 443);

        /**
         * Constructs a browser companion client.
         *
         * @param store         the WhatsApp store
         * @param whatsAppLayer the topmost layer
         */
        Browser(WhatsAppStore store, WhatsAppSocketClientLayer whatsAppLayer) {
            super(store, whatsAppLayer);
        }

        @Override
        InetSocketAddress getEndpoint() {
            return WEB_SOCKET_ENDPOINT;
        }

        /**
         * Finishes the WebSocket-side connection first (so the chain
         * transitions to async mode), then runs the Noise handshake
         * through the async pipeline and finally tells the WhatsApp
         * context that it can start dispatching real datagrams.
         */
        @Override
        void connectImpl(Instant socketOpenStart) throws IOException {
            whatsAppLayer.finishConnect();
            markSocketOpenDone(socketOpenStart);
            performNoiseHandshake();
            whatsAppLayer.markHandshakeComplete();
            whatsAppLayer.startListenerExecutor();
        }

        @Override
        ClientPlatformType getPlatform() {
            // The Windows hybrid shell keeps UserAgent.platform as WEB
            // and only distinguishes itself from a browser through the
            // WebInfo.webSubPlatform field.
            return ClientPlatformType.WEB;
        }

        @Override
        ClientPayload.WebInfo.WebSubPlatform getWebSubPlatform() {
            return switch (store.device().platform()) {
                case WINDOWS -> ClientPayload.WebInfo.WebSubPlatform.WIN_HYBRID;
                case WEB -> ClientPayload.WebInfo.WebSubPlatform.WEB_BROWSER;
                default -> throw new IllegalStateException(
                        "Browser client does not support platform: " + store.device().platform());
            };
        }
    }

    /**
     * Companion client for the native macOS desktop application.
     *
     * <p>Connects via raw TCP like {@link Mobile} but authenticates as a
     * companion device using the web payload structure ({@code webInfo}
     * block, device-pairing registration data, two-level certificate
     * chain).
     *
     * <p>Unlike {@link Browser} there is no WebSocket upgrade, so Noise
     * runs directly over the transport and {@code finishConnect} is
     * deferred until after the handshake (the same ordering as
     * {@link Mobile}).
     *
     * <p>Only macOS uses this client. The Windows desktop build is a
     * hybrid web/native shell that reuses the web WebSocket endpoint
     * and is therefore served by {@link Browser}.
     */
    static final class Desktop extends Web {
        /**
         * TCP endpoint for desktop companions; shared with mobile.
         */
        private static final InetSocketAddress DESKTOP_ENDPOINT = new InetSocketAddress("g.whatsapp.net", 443);

        /**
         * Constructs a desktop companion client.
         *
         * @param store         the WhatsApp store
         * @param whatsAppLayer the topmost layer
         */
        Desktop(WhatsAppStore store, WhatsAppSocketClientLayer whatsAppLayer) {
            super(store, whatsAppLayer);
        }

        @Override
        InetSocketAddress getEndpoint() {
            return DESKTOP_ENDPOINT;
        }

        @Override
        void connectImpl(Instant socketOpenStart) throws IOException {
            // No WebSocket upgrade, so the transport is already open
            // when we get here. Close the socket_open span immediately.
            markSocketOpenDone(socketOpenStart);
            performNoiseHandshake();
            whatsAppLayer.markHandshakeComplete();
            whatsAppLayer.startListenerExecutor();
            whatsAppLayer.finishConnect();
        }

        @Override
        ClientPlatformType getPlatform() {
            return ClientPlatformType.MACOS;
        }

        @Override
        ClientPayload.WebInfo.WebSubPlatform getWebSubPlatform() {
            return ClientPayload.WebInfo.WebSubPlatform.DARWIN;
        }
    }

    /**
     * Native mobile (iOS or Android) socket client.
     *
     * <p>Connects via direct TCP, runs the Noise XX handshake through
     * the still-pre-tunnel transport so reads can use the blocking
     * path, and transitions to asynchronous mode only after the
     * handshake succeeds.
     */
    static final class Mobile extends WhatsAppSocketClient {
        /**
         * Two-byte mobile version footer appended to
         * {@link #WHATSAPP_VERSION_HEADER} to form the handshake
         * prologue.
         */
        private static final byte[] MOBILE_VERSION = new byte[]{5, NodeTokens.DICTIONARY_VERSION};

        /**
         * Handshake prologue advertised by mobile clients.
         */
        private static final byte[] MOBILE_PROLOGUE = DataUtils.concatByteArrays(WHATSAPP_VERSION_HEADER, MOBILE_VERSION);

        /**
         * TCP endpoint for mobile connections.
         */
        private static final InetSocketAddress SOCKET_ENDPOINT = new InetSocketAddress("g.whatsapp.net", 443);

        /**
         * Constructs a mobile socket client.
         *
         * @param store         the WhatsApp store
         * @param whatsAppLayer the topmost layer wrapping the raw TCP
         *                      transport
         */
        Mobile(WhatsAppStore store, WhatsAppSocketClientLayer whatsAppLayer) {
            super(store, whatsAppLayer);
        }

        @Override
        InetSocketAddress getEndpoint() {
            return SOCKET_ENDPOINT;
        }

        /**
         * Runs the Noise handshake over the still-pre-tunnel transport
         * and then transitions the chain to async mode by calling
         * {@link WhatsAppSocketClientLayer#finishConnect()} after the
         * keys have been derived.
         */
        @Override
        void connectImpl(Instant socketOpenStart) throws IOException {
            // No WebSocket upgrade, so the transport is already open
            // when we get here. Close the socket_open span immediately.
            markSocketOpenDone(socketOpenStart);
            performNoiseHandshake();
            whatsAppLayer.markHandshakeComplete();
            whatsAppLayer.startListenerExecutor();
            whatsAppLayer.finishConnect();
        }

        /**
         * Verifies the flat {@code NoiseCertificate} that the mobile
         * server returns instead of a two-level chain.
         *
         * <p>The certificate is verified against the root CA public key
         * and its embedded key must match the server static key from
         * the handshake.
         *
         * @param decryptedCertificate the decrypted certificate payload
         * @param serverStaticKey      the 32-byte server static key
         * @throws IOException if any check fails
         */
        @Override
        void verifyCertificateChain(byte[] decryptedCertificate, byte[] serverStaticKey) throws IOException {
            var cert = NoiseCertificateSpec.decode(decryptedCertificate);
            var details = cert.details()
                    .orElseThrow(() -> new IOException("NoiseCertificate missing details"));
            var signature = cert.signature()
                    .orElseThrow(() -> new IOException("NoiseCertificate missing signature"));

            if (!Curve25519.verifySignature(NOISE_ROOT_CA_PUBLIC_KEY, details, ensureSignatureSize(signature))) {
                throw new IOException("NoiseCertificate has invalid signature");
            }

            var parsedDetails = NoiseCertificateDetailsSpec.decode(details);
            var key = parsedDetails.key()
                    .orElseThrow(() -> new IOException("NoiseCertificate missing key"));
            if (!Arrays.equals(key, serverStaticKey)) {
                throw new IOException("NoiseCertificate key does not match handshake server static key");
            }
        }

        /**
         * Returns a defensive copy of the mobile handshake prologue.
         *
         * @return the prologue bytes
         */
        @Override
        byte[] getHandshakePrologue() {
            return MOBILE_PROLOGUE.clone();
        }

        /**
         * Builds the serialized mobile handshake payload.
         *
         * @return the serialized client payload
         */
        @Override
        byte[] getHandshakePayload() {
            var agent = getUserAgent();
            var payload = getClientPayload(agent);
            return ClientPayloadSpec.encode(payload);
        }

        /**
         * Builds the mobile-specific user agent, including the device
         * manufacturer, model, OS version and FDID.
         *
         * @return the user agent value
         */
        private UserAgent getUserAgent() {
            return new ClientPayloadUserAgentBuilder()
                    .platform(store.device().platform())
                    .appVersion(store.clientVersion())
                    .mcc("000")
                    .mnc("000")
                    .osVersion(store.device().osDeviceAppVersion().toString())
                    .manufacturer(store.device().manufacturer())
                    .device(store.device().model().replaceAll("_", " "))
                    .osBuildNumber(store.device().osBuildNumber())
                    .phoneId(store.fdid().toString().toUpperCase())
                    .releaseChannel(store.releaseChannel())
                    .localeLanguageIso6391("en")
                    .localeCountryIso31661Alpha2("US")
                    .deviceType(ClientPayload.ClientType.PHONE)
                    .deviceModelType(store.device().modelId())
                    .build();
        }

        /**
         * Builds the mobile client payload.
         *
         * @param agent the user agent value
         * @return the client payload
         */
        private ClientPayload getClientPayload(UserAgent agent) {
            var phoneNumber = store
                    .phoneNumber()
                    .orElseThrow(() -> new InternalError("Phone number was not set"));
            return new ClientPayloadBuilder()
                    .username(phoneNumber)
                    .passive(false)
                    .pushName(store.registered() ? store.name() : null)
                    .userAgent(agent)
                    .shortConnect(true)
                    .connectType(ClientPayload.ConnectType.WIFI_UNKNOWN)
                    .connectReason(ClientPayload.ConnectReason.USER_ACTIVATED)
                    .connectAttemptCount(0)
                    .device(0)
                    .oc(false)
                    .build();
        }
    }
}
