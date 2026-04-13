package com.github.auties00.cobalt.socket;

import com.github.auties00.cobalt.client.WhatsAppClientProxy;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.exception.WhatsAppStreamException;
import com.github.auties00.cobalt.model.device.*;
import com.github.auties00.cobalt.model.device.pairing.*;
import com.github.auties00.cobalt.model.device.pairing.ClientPayload.DevicePairingRegistrationData;
import com.github.auties00.cobalt.model.device.pairing.ClientPayload.UserAgent;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.binary.NodeDecoder;
import com.github.auties00.cobalt.node.binary.NodeEncoder;
import com.github.auties00.cobalt.node.binary.NodeTokens;
import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.layer.application.websocket.WebSocketClient;
import com.github.auties00.cobalt.socket.layer.security.SocketClientTransportSecurityLayer;
import com.github.auties00.cobalt.socket.layer.security.SocketClientTunnelSecurityLayer;
import com.github.auties00.cobalt.socket.layer.transport.SocketClientTransportLayer;
import com.github.auties00.cobalt.socket.layer.transport.SocketClientTransportLayerContext;
import com.github.auties00.cobalt.socket.layer.tunnel.SocketClientTunnelLayer;
import com.github.auties00.cobalt.socket.layer.tunnel.TunnelLayerContext;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.cobalt.util.FastRandomUtils;
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
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.Objects;

/**
 * A sealed WhatsApp socket client hierarchy that provides Noise XX encryption
 * and int24-framed datagram transport over either a WebSocket connection
 * ({@link Web}) or a raw TCP connection ({@link Mobile}).
 *
 * <p>For WEB clients, the data flow is:
 * <pre>
 * Node -> serialize -> Noise encrypt + int24 prefix -> WebSocket binary frame -> TLS -> TCP
 * </pre>
 *
 * <p>For mobile clients, the data flow is:
 * <pre>
 * Node -> serialize -> Noise encrypt + int24 prefix -> TCP
 * </pre>
 *
 * <p>The Noise XX handshake derives separate read and write AES-GCM keys.
 * Outbound messages are encrypted at {@code sendBinary()} call time.
 * Inbound decryption is performed through a listener wrapper that
 * intercepts assembled datagrams from the {@link WhatsAppLayerContext}.
 */
public sealed abstract class WhatsAppSocketClient {
    private static final byte[] WHATSAPP_VERSION_HEADER = "WA".getBytes(StandardCharsets.UTF_8);
    private static final byte[] WEB_VERSION = new byte[]{6, NodeTokens.DICTIONARY_VERSION};
    private static final byte[] WEB_PROLOGUE = FastRandomUtils.concatByteArrays(WHATSAPP_VERSION_HEADER, WEB_VERSION);
    private static final byte[] MOBILE_VERSION = new byte[]{5, NodeTokens.DICTIONARY_VERSION};
    private static final byte[] MOBILE_PROLOGUE = FastRandomUtils.concatByteArrays(WHATSAPP_VERSION_HEADER, MOBILE_VERSION);
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int INT24_BYTE_SIZE = 3;
    private static final int GCM_TAG_BYTE_SIZE = 16;
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    private static final int MAX_MESSAGE_LENGTH = 0xFFFFFF;
    private static final int MAX_HANDSHAKE_MESSAGE_LENGTH = 0xFFFF;

    /**
     * Creates a new WhatsApp socket client for the given store.
     *
     * <p>For WEB clients, builds a WebSocket client over TCP + TLS.
     * For mobile clients, builds a raw TCP transport.
     *
     * @param store the WhatsApp store
     * @return a new WhatsApp socket client
     */
    public static WhatsAppSocketClient newCipheredSocketClient(WhatsAppStore store) {
        Objects.requireNonNull(store, "store cannot be null");

        var transport = createTransport(store);
        var tunnel = createTunnel(transport, store);

        if (store.device().platform() == ClientPlatformType.WEB) {
            return new Web(store, new WebSocketClient(tunnel));
        } else {
            return new Mobile(store, tunnel);
        }
    }

    /**
     * Creates the base transport layer for the given store.
     *
     * <p>WEB clients wrap the TCP transport in TLS.
     * Mobile clients use raw TCP.
     *
     * @param store the WhatsApp store
     * @return the transport layer
     */
    private static SocketClientLayer createTransport(WhatsAppStore store) {
        var transport = SocketClientTransportLayer.newTcpTransport();
        if (store.device().platform() == ClientPlatformType.WEB) {
            return SocketClientTransportSecurityLayer.newTlsTransport(transport, WhatsAppSslEngineFactory.newWebSSLEngineFactory());
        } else {
            return transport;
        }
    }

    /**
     * Creates the tunnel layer for the given store, wrapping the transport
     * in a proxy tunnel if configured.
     *
     * @param transport the base transport layer
     * @param store     the WhatsApp store
     * @return the tunnel layer, or the transport itself if no proxy
     */
    private static SocketClientLayer createTunnel(SocketClientLayer transport, WhatsAppStore store) {
        return switch (store.proxy().orElse(null)) {
            case WhatsAppClientProxy.Http http -> {
                var httpTunnel = SocketClientTunnelLayer.newHttpTunnel(http, transport);
                yield switch (http) {
                    case WhatsAppClientProxy.Http.Plain _ -> httpTunnel;
                    case WhatsAppClientProxy.Http.Secure _ -> SocketClientTunnelSecurityLayer.newTlsTunnel(httpTunnel, WhatsAppSslEngineFactory.newWebSSLEngineFactory());
                };
            }
            case WhatsAppClientProxy.Socks socks -> SocketClientTunnelLayer.newSocksTunnel(socks, transport);
            case null -> transport;
        };
    }

    /**
     * The WhatsApp store for this connection.
     */
    final WhatsAppStore store;

    /**
     * The original listener to receive deserialized nodes and close events.
     */
    private WhatsAppSocketListener listener;

    /**
     * The AES-GCM cipher used for encrypting outbound messages.
     */
    private volatile Cipher writeCipher;

    /**
     * The AES write key derived from the Noise handshake.
     */
    private volatile SecretKeySpec writeKey;

    /**
     * The write nonce counter, incremented for each outbound message.
     */
    private long writeCounter;

    /**
     * The AES-GCM cipher used for decrypting inbound messages.
     */
    private volatile Cipher readCipher;

    /**
     * The AES read key derived from the Noise handshake.
     */
    private volatile SecretKeySpec readKey;

    /**
     * The read nonce counter, incremented for each inbound datagram.
     */
    private long readCounter;

    /**
     * Reusable length prefix buffer for outbound messages.
     *
     * <p>Safe to reuse because {@link #sendBinary(ByteBuffer...)} is
     * {@code synchronized}.
     */
    private final ByteBuffer reusableLengthPrefix = ByteBuffer.allocate(INT24_BYTE_SIZE);

    /**
     * Reusable buffer for the GCM authentication tag produced by
     * {@code doFinal()}.  Lazily sized after the first cipher init.
     *
     * <p>Safe to reuse because {@link #sendBinary(ByteBuffer...)} is
     * {@code synchronized}.
     */
    private ByteBuffer reusableFinalChunk;

    /**
     * Constructs a new WhatsApp socket client with the given store.
     *
     * @param store the WhatsApp store
     */
    private WhatsAppSocketClient(WhatsAppStore store) {
        this.store = store;
    }

    /**
     * Connects and performs the Noise XX handshake.
     *
     * <p>Delegates the transport-specific connection and handshake
     * sequencing to the subtype via {@link #connectImpl}.
     *
     * @param listener the callback for deserialized nodes and close events
     * @throws IOException if the connection or handshake fails
     */
    public final void connect(WhatsAppSocketListener listener) throws IOException {
        Objects.requireNonNull(listener, "listener cannot be null");
        this.listener = listener;

        var decryptingListener = new DecryptingListener();
        var appContext = new WhatsAppLayerContext(decryptingListener);
        connectImpl(appContext, decryptingListener);
    }

    /**
     * Performs the transport-specific connection and Noise handshake sequencing.
     *
     * @param appContext          the application layer context for datagram reassembly
     * @param decryptingListener  the decrypting listener wrapper
     * @throws IOException if the connection or handshake fails
     */
    abstract void connectImpl(WhatsAppLayerContext appContext, DecryptingListener decryptingListener) throws IOException;

    /**
     * Disconnects and destroys cipher keys.
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

        disconnectTransport();
    }

    /**
     * Disconnects the underlying transport layer.
     */
    abstract void disconnectTransport();

    /**
     * Returns whether the connection is active.
     *
     * @return {@code true} if connected
     */
    public abstract boolean isConnected();

    /**
     * Encrypts the given plaintext buffers with AES-GCM and sends the
     * result, prefixed with a 3-byte int24 ciphertext length header.
     *
     * <p>Before the handshake completes (no write key), buffers are
     * passed through without encryption.
     *
     * <p>This method is synchronized to ensure the write counter stays
     * consistent across concurrent callers.
     *
     * @param buffers the plaintext buffers to encrypt and send
     * @throws IOException if the write fails
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
            var finalProduced = writeCipher.doFinal(EMPTY_BUFFER, reusableFinalChunk);
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
     * Encrypts a single plaintext buffer segment in place.
     *
     * @param source the source buffer (must be writable)
     * @return a view of the encrypted segment
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
     * Sends raw buffers through the underlying transport (WebSocket or TCP).
     *
     * @param buffers the buffers to send
     * @throws IOException if the write fails
     */
    abstract void sendRaw(ByteBuffer... buffers) throws IOException;

    /**
     * Reads raw bytes from the underlying transport.
     *
     * @param buffer the destination buffer
     * @param fully  {@code true} to fill the buffer completely
     * @return bytes read, or {@code -1} on end-of-stream
     * @throws IOException if reading fails
     */
    abstract int readRaw(ByteBuffer buffer, boolean fully) throws IOException;

    /**
     * Performs the Noise XX handshake.
     *
     * <p>If {@code appContext} is non-null (WEB path), handshake reads
     * go through the layer context's blocking read mechanism.  Otherwise
     * (mobile path), reads go through the raw transport.
     *
     * @param appContext the WhatsApp layer context for blocking reads,
     *                   or {@code null} for raw transport reads
     * @throws IOException if the handshake fails
     */
    void performNoiseHandshake(WhatsAppLayerContext appContext) throws IOException {
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
            sendHandshakeMessage(prologue, requestBytes);

            // Read server hello
            var serverHelloPayload = readHandshakeMessage(appContext);

            // Process server hello
            var serverHandshake = HandshakeMessageSpec.decode(ProtobufInputStream.fromBuffer(serverHelloPayload));
            var serverHello = serverHandshake.serverHello()
                    .orElseThrow(() -> new IOException("Missing server hello"));

            handshake.updateHash(ephemeralKeyPair.publicKey().toEncodedPoint());

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
            handshake.cipher(payload, false);

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
        } catch (IOException e) {
            throw e;
        } catch (Throwable e) {
            throw new IOException("Noise handshake failure", e);
        }
    }

    /**
     * Sends a handshake message with an optional prologue prefix and a
     * 3-byte int24 length header.
     *
     * @param prologue     the prologue to prepend, or {@code null} for none
     * @param messageBytes the serialized handshake message
     * @throws IOException if the write fails
     */
    private void sendHandshakeMessage(byte[] prologue, byte[] messageBytes) throws IOException {
        var lengthPrefix = ByteBuffer.allocate(INT24_BYTE_SIZE);
        var len = messageBytes.length;
        lengthPrefix.put((byte) ((len >> 16) & 0xFF));
        lengthPrefix.put((byte) ((len >> 8) & 0xFF));
        lengthPrefix.put((byte) (len & 0xFF));
        lengthPrefix.flip();
        if (prologue != null) {
            sendRaw(ByteBuffer.wrap(prologue), lengthPrefix, ByteBuffer.wrap(messageBytes));
        } else {
            sendRaw(lengthPrefix, ByteBuffer.wrap(messageBytes));
        }
    }

    /**
     * Reads a complete handshake message.
     *
     * <p>If {@code appContext} is non-null, reads go through the layer
     * context's blocking read mechanism.  Otherwise reads go through
     * the raw transport.
     *
     * @param appContext the WhatsApp layer context for blocking reads,
     *                   or {@code null} for raw transport reads
     * @return the message payload as a {@link ByteBuffer} in read mode
     * @throws IOException if the read fails or yields an invalid length
     */
    private ByteBuffer readHandshakeMessage(WhatsAppLayerContext appContext) throws IOException {
        var lengthBuf = ByteBuffer.allocate(INT24_BYTE_SIZE);
        var bytesRead = readHandshakeRaw(lengthBuf, true, appContext);
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
        bytesRead = readHandshakeRaw(payloadBuf, true, appContext);
        if (bytesRead < length) {
            throw new IOException("Failed to read handshake message payload");
        }

        payloadBuf.flip();
        return payloadBuf;
    }

    /**
     * Reads raw bytes during the handshake phase.
     *
     * <p>If {@code appContext} is non-null (WEB path), posts a pending
     * read to the layer context and blocks until the selector fulfills
     * it through the layer context chain.  Otherwise (mobile path),
     * reads directly from the raw transport.
     *
     * @param buffer     the destination buffer
     * @param fully      {@code true} to fill the buffer completely
     * @param appContext the layer context for blocking reads, or
     *                   {@code null} for raw transport reads
     * @return bytes read, or {@code -1} on end-of-stream
     * @throws IOException if reading fails
     */
    private int readHandshakeRaw(ByteBuffer buffer, boolean fully, WhatsAppLayerContext appContext) throws IOException {
        if (appContext != null) {
            var read = new SocketClientTransportLayerContext.PendingRead(buffer, fully);
            if (!appContext.setPendingRead(read)) {
                throw new IOException("Failed to post handshake read: another read is pending");
            }
            synchronized (read.lock) {
                while (read.length == -1 || (fully && read.length >= 0 && read.buffer.hasRemaining())) {
                    try {
                        read.lock.wait(30_000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Handshake read interrupted", e);
                    }
                }
            }
            return read.length;
        }
        return readRaw(buffer, fully);
    }

    /**
     * Returns the handshake prologue bytes for the current client type.
     *
     * @return the prologue bytes
     */
    abstract byte[] getHandshakePrologue();

    /**
     * Builds the serialized client payload for the Noise handshake.
     *
     * @return the serialized client payload
     */
    abstract byte[] getHandshakePayload();

    /**
     * Serializes a {@link Node} and sends it through the encrypted channel.
     *
     * @param node the node to send
     * @throws IOException if serialization or sending fails
     */
    public final void sendNode(Node node) throws IOException {
        var encoded = new byte[NodeEncoder.sizeOf(node)];
        var length = NodeEncoder.encode(node, encoded, 0, encoded.length);
        sendBinary(ByteBuffer.wrap(encoded, 0, length));
    }

    /**
     * A listener wrapper that decrypts each inbound datagram, deserializes
     * it into a {@link Node}, and forwards it to the application listener.
     */
    class DecryptingListener implements SocketClientLayerListener {
        /**
         * Decrypts and deserializes an inbound datagram, forwarding
         * each decoded {@link Node} to the application listener.
         *
         * @param datagram the raw inbound datagram
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
            } catch (Exception e) {
                listener.onError(new WhatsAppStreamException.MalformedNode("Failed to process inbound datagram", e));
            }
        }

        /**
         * Decrypts a datagram using the read cipher.
         *
         * @param datagram the encrypted datagram
         * @return the decrypted plaintext, or {@code null} if decryption fails
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
                    disconnect();
                    return null;
                }

                if (datagram.isReadOnly()) {
                    var output = ByteBuffer.allocate(datagram.remaining() - GCM_TAG_BYTE_SIZE);
                    readCipher.doFinal(datagram.duplicate(), output);
                    output.flip();
                    return output;
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
                disconnect();
                return null;
            }
        }

        /**
         * Forwards the close event to the application listener.
         */
        @Override
        public void onClose() {
            listener.onClose();
        }
    }

    /**
     * WebSocket-based WhatsApp socket client for WEB platform connections.
     *
     * <p>Connects via WebSocket over TLS, performs the Noise XX handshake
     * through the layer context's blocking read mechanism, then transitions
     * to asynchronous mode.
     */
    static final class Web extends WhatsAppSocketClient {
        /**
         * The WebSocket endpoint for WEB connections.
         */
        private static final URI WEB_SOCKET_ENDPOINT = URI.create("wss://web.whatsapp.com/ws/chat");

        /**
         * The WebSocket client for this connection.
         */
        private final WebSocketClient webSocketClient;

        /**
         * Constructs a new WEB socket client.
         *
         * @param store           the WhatsApp store
         * @param webSocketClient the WebSocket client
         */
        Web(WhatsAppStore store, WebSocketClient webSocketClient) {
            super(store);
            this.webSocketClient = webSocketClient;
        }

        /**
         * Connects via WebSocket, performs the Noise handshake through
         * the layer context, marks the handshake complete, and starts
         * the async listener executor.
         *
         * @param appContext          the application layer context
         * @param decryptingListener  the decrypting listener wrapper
         * @throws IOException if the connection or handshake fails
         */
        @Override
        void connectImpl(WhatsAppLayerContext appContext, DecryptingListener decryptingListener) throws IOException {
            webSocketClient.connect(WEB_SOCKET_ENDPOINT, appContext, decryptingListener);
            performNoiseHandshake(appContext);
            appContext.markHandshakeComplete();
            appContext.startListenerExecutor();
        }

        /**
         * Disconnects the WebSocket client.
         */
        @Override
        void disconnectTransport() {
            webSocketClient.disconnect();
        }

        /**
         * Returns whether the WebSocket connection is active.
         *
         * @return {@code true} if connected
         */
        @Override
        public boolean isConnected() {
            return webSocketClient.isConnected();
        }

        /**
         * Sends raw buffers through the WebSocket client.
         *
         * @param buffers the buffers to send
         * @throws IOException if the write fails
         */
        @Override
        void sendRaw(ByteBuffer... buffers) throws IOException {
            webSocketClient.sendBinary(buffers);
        }

        /**
         * Reads raw bytes from the WebSocket client.
         *
         * @param buffer the destination buffer
         * @param fully  {@code true} to fill the buffer completely
         * @return bytes read, or {@code -1} on end-of-stream
         * @throws IOException if reading fails
         */
        @Override
        int readRaw(ByteBuffer buffer, boolean fully) throws IOException {
            return webSocketClient.readBinary(buffer, fully);
        }

        /**
         * Returns the WEB handshake prologue.
         *
         * @return the WEB prologue bytes
         */
        @Override
        byte[] getHandshakePrologue() {
            return WEB_PROLOGUE;
        }

        /**
         * Builds the WEB client handshake payload.
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
         * Constructs the user agent for the WEB handshake payload.
         *
         * @return the user agent
         */
        private UserAgent getUserAgent() {
            return new ClientPayloadUserAgentBuilder()
                    .platform(store.device().platform())
                    .appVersion(store.clientVersion())
                    .mcc("000")
                    .mnc("000")
                    .releaseChannel(store.releaseChannel())
                    .localeLanguageIso6391("en")
                    .localeCountryIso31661Alpha2("US")
                    .deviceType(ClientPayload.ClientType.PHONE)
                    .deviceModelType(store.device().modelId())
                    .build();
        }

        /**
         * Constructs the WEB client payload.
         *
         * <p>If a JID is present, builds a reconnection payload.
         * Otherwise, builds a new pairing payload with registration data.
         *
         * @param agent the user agent
         * @return the client payload
         */
        private ClientPayload getClientPayload(UserAgent agent) {
            var jid = store.jid();
            if (jid.isPresent()) {
                return new ClientPayloadBuilder()
                        .connectType(ClientPayload.ConnectType.WIFI_UNKNOWN)
                        .connectReason(ClientPayload.ConnectReason.USER_ACTIVATED)
                        .userAgent(agent)
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
                        .devicePairingData(createRegisterData())
                        .passive(false)
                        .pull(false)
                        .build();
            }
        }

        /**
         * Creates the device pairing registration data for new WEB sessions,
         * including companion device properties.
         *
         * @return the registration data
         */
        private DevicePairingRegistrationData createRegisterData() {
            return new ClientPayloadDevicePairingRegistrationDataBuilder()
                    .buildHash(store.clientVersion().toHash())
                    .eRegid(FastRandomUtils.intToBytes(store.registrationId(), 4))
                    .eKeytype(FastRandomUtils.intToBytes(SignalIdentityPublicKey.type(), 1))
                    .eIdent(store.identityKeyPair().publicKey().toEncodedPoint())
                    .eSkeyId(FastRandomUtils.intToBytes(store.signedKeyPair().id(), 3))
                    .eSkeyVal(store.signedKeyPair().publicKey().toEncodedPoint())
                    .eSkeySig(store.signedKeyPair().signature())
                    .deviceProps(createCompanionProps())
                    .build();
        }

        /**
         * Creates and encodes the companion device properties for WEB clients.
         *
         * @return the encoded companion device properties
         */
        private byte[] createCompanionProps() {
            var historyLength = store.webHistoryPolicy()
                    .orElse(WhatsAppWebClientHistory.standard(true));
            var config = new DevicePropsHistorySyncConfigBuilder()
                    .inlineInitialPayloadInE2EeMsg(true)
                    .supportBotUserAgentChatHistory(true)
                    .supportCallLogHistory(true)
                    .storageQuotaMb(historyLength.size())
                    .fullSyncSizeMbLimit(historyLength.size())
                    .build();
            var platformType = switch (store.device().platform()) {
                case IOS, IOS_BUSINESS -> DevicePlatformType.IOS_PHONE;
                case ANDROID, ANDROID_BUSINESS -> DevicePlatformType.ANDROID_PHONE;
                case WINDOWS -> DevicePlatformType.UWP;
                case MACOS -> DevicePlatformType.IOS_CATALYST;
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
     * Raw TCP-based WhatsApp socket client for mobile platform connections.
     *
     * <p>Connects via direct TCP, performs the Noise XX handshake through
     * raw transport reads, then transitions to asynchronous mode.
     */
    static final class Mobile extends WhatsAppSocketClient {
        /**
         * The TCP endpoint for mobile connections.
         */
        private static final InetSocketAddress SOCKET_ENDPOINT = new InetSocketAddress("g.whatsapp.net", 443);

        /**
         * The raw transport layer for this connection.
         */
        private final SocketClientLayer mobileLayer;

        /**
         * Constructs a new mobile socket client.
         *
         * @param store       the WhatsApp store
         * @param mobileLayer the raw transport layer
         */
        Mobile(WhatsAppStore store, SocketClientLayer mobileLayer) {
            super(store);
            this.mobileLayer = mobileLayer;
        }

        /**
         * Connects via direct TCP, registers the tunnel layer context,
         * performs the Noise handshake through raw transport reads,
         * starts the async listener executor, and finishes the connection.
         *
         * @param appContext          the application layer context
         * @param decryptingListener  the decrypting listener wrapper
         * @throws IOException if the connection or handshake fails
         */
        @Override
        void connectImpl(WhatsAppLayerContext appContext, DecryptingListener decryptingListener) throws IOException {
            mobileLayer.connect(SOCKET_ENDPOINT, decryptingListener);
            var gatingContext = new TunnelLayerContext(appContext, false);
            mobileLayer.registerLayerContext(SocketClientTunnelLayer.class, gatingContext);
            performNoiseHandshake(null);
            appContext.startListenerExecutor();
            mobileLayer.finishConnect();
        }

        /**
         * Disconnects the raw transport layer.
         */
        @Override
        void disconnectTransport() {
            mobileLayer.disconnect();
        }

        /**
         * Returns whether the raw transport connection is active.
         *
         * @return {@code true} if connected
         */
        @Override
        public boolean isConnected() {
            return mobileLayer.isConnected();
        }

        /**
         * Sends raw buffers through the raw transport layer.
         *
         * @param buffers the buffers to send
         * @throws IOException if the write fails
         */
        @Override
        void sendRaw(ByteBuffer... buffers) throws IOException {
            mobileLayer.sendBinary(buffers);
        }

        /**
         * Reads raw bytes from the raw transport layer.
         *
         * @param buffer the destination buffer
         * @param fully  {@code true} to fill the buffer completely
         * @return bytes read, or {@code -1} on end-of-stream
         * @throws IOException if reading fails
         */
        @Override
        int readRaw(ByteBuffer buffer, boolean fully) throws IOException {
            return mobileLayer.readBinary(buffer, fully);
        }

        /**
         * Returns the mobile handshake prologue.
         *
         * @return the mobile prologue bytes
         */
        @Override
        byte[] getHandshakePrologue() {
            return MOBILE_PROLOGUE;
        }

        /**
         * Builds the mobile client handshake payload.
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
         * Constructs the user agent for the mobile handshake payload,
         * including device-specific fields.
         *
         * @return the user agent
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
         * Constructs the mobile client payload.
         *
         * @param agent the user agent
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
