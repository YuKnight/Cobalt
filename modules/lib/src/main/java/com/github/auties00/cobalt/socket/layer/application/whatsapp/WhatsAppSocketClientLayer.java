package com.github.auties00.cobalt.socket.layer.application.whatsapp;

import com.github.auties00.cobalt.socket.layer.SocketClientLayer;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.layer.application.SocketClientApplicationLayer;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientPendingRead;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Topmost layer of the socket stack, responsible for reassembling
 * int24-framed datagrams and surfacing the handshake-plumbing API
 * that the Noise driver in {@code WhatsAppSocketClient} drives.
 *
 * <p>The matching {@link WhatsAppSocketClientLayerContext} is created
 * lazily during {@link #connect(InetSocketAddress, SocketClientLayerListener)}
 * and registered with the listener passed in by the caller, typically
 * the decrypting listener that wraps the application's
 * {@link com.github.auties00.cobalt.socket.WhatsAppSocketListener}.
 */
public final class WhatsAppSocketClientLayer implements SocketClientApplicationLayer<WhatsAppSocketClientLayerContext> {
    /**
     * The inner layer providing raw byte I/O; either a WebSocket
     * client (Web companions) or the security stack over the tunnel
     * (Desktop and Mobile).
     */
    private final SocketClientLayer<?> innerLayer;

    /**
     * Layer context, created during {@link #connect(InetSocketAddress, SocketClientLayerListener)}
     * and read by the Noise driver for handshake plumbing.
     */
    private WhatsAppSocketClientLayerContext layerContext;

    /**
     * Creates a WhatsApp application layer over {@code innerLayer}.
     *
     * @param innerLayer the inner layer (WebSocket for Web, security
     *                   and transport for Desktop and Mobile)
     */
    public WhatsAppSocketClientLayer(SocketClientLayer<?> innerLayer) {
        this.innerLayer = Objects.requireNonNull(innerLayer, "innerLayer cannot be null");
    }

    /**
     * Connects the inner layer and registers a fresh layer context.
     *
     * <p>Deliberately does not call {@link #finishConnect()}; the
     * Noise driver sequences that step explicitly because Browser,
     * Desktop and Mobile need different orderings.
     *
     * @param address  the remote endpoint
     * @param listener the listener that receives decoded datagrams
     *                 and the close event
     * @throws IOException if the underlying connection fails
     */
    @Override
    public void connect(InetSocketAddress address, SocketClientLayerListener listener) throws IOException {
        innerLayer.connect(address, listener);
        this.layerContext = WhatsAppSocketClientLayerContext.newAppContext(listener);
        innerLayer.registerLayerContext(layerContext);
    }

    /**
     * Disconnects the inner layer.
     */
    @Override
    public void disconnect() {
        innerLayer.disconnect();
    }

    /**
     * Returns whether the inner layer is connected.
     *
     * @return {@code true} if the inner layer reports as connected
     */
    @Override
    public boolean isConnected() {
        return innerLayer.isConnected();
    }

    /**
     * Forwards {@code buffers} to the inner layer for asynchronous
     * sending.
     *
     * @param buffers payload buffers in send order
     * @throws IOException if the inner layer cannot accept the write
     */
    @Override
    public void sendBinary(ByteBuffer... buffers) throws IOException {
        innerLayer.sendBinary(buffers);
    }

    /**
     * Performs a blocking read against the inner layer.
     *
     * @param buffer the destination buffer, in write mode
     * @param fully  {@code true} to fill the buffer, {@code false} to
     *               return after the first successful read
     * @return the number of bytes read, or {@code -1} on
     *         end-of-stream
     * @throws IOException if reading fails
     */
    @Override
    public int readBinary(ByteBuffer buffer, boolean fully) throws IOException {
        return innerLayer.readBinary(buffer, fully);
    }

    /**
     * Transitions the inner layer from synchronous handshake mode to
     * asynchronous data flow.
     *
     * @throws IOException if the transition fails
     */
    @Override
    public void finishConnect() throws IOException {
        innerLayer.finishConnect();
    }

    /**
     * Variant of {@link #finishConnect()} that also feeds leftover
     * bytes from a synchronous protocol upgrade into the pipeline.
     *
     * @param leftover the leftover bytes in read mode, or {@code null}
     * @throws IOException if the transition fails
     */
    @Override
    public void finishConnect(ByteBuffer leftover) throws IOException {
        innerLayer.finishConnect(leftover);
    }

    /**
     * Initiates a TLS handshake on the inner transport.
     *
     * @param tlsContext the TLS layer context that drives the handshake
     * @param timeout    the handshake timeout in milliseconds
     * @throws IOException if the handshake fails or times out
     */
    @Override
    public void startHandshake(SocketClientLayerContext tlsContext, long timeout) throws IOException {
        innerLayer.startHandshake(tlsContext, timeout);
    }

    /**
     * Registers an additional layer context with the inner layer's
     * selector pipeline.
     *
     * @param context the context to register
     * @throws IOException if registration fails
     */
    @Override
    public void registerLayerContext(SocketClientLayerContext context) throws IOException {
        innerLayer.registerLayerContext(context);
    }

    /**
     * Posts a blocking read request on the layer context, used by the
     * Noise driver to pull handshake bytes out of the async chain.
     *
     * @param read the pending read request
     * @return {@code true} if the request was accepted
     * @throws IllegalStateException if called before
     *         {@link #connect(InetSocketAddress, SocketClientLayerListener)}
     */
    public boolean setPendingRead(SocketClientPendingRead read) {
        requireContext();
        return layerContext.setPendingRead(read);
    }

    /**
     * Transitions the layer context out of handshake mode.
     *
     * <p>After this call inbound bytes are reassembled as int24-framed
     * datagrams and dispatched to the listener instead of filling
     * blocking reads.
     *
     * @throws IllegalStateException if called before
     *         {@link #connect(InetSocketAddress, SocketClientLayerListener)}
     */
    public void markHandshakeComplete() {
        requireContext();
        layerContext.markHandshakeComplete();
    }

    /**
     * Starts the single-threaded virtual executor that serializes
     * listener callbacks.
     *
     * @throws IllegalStateException if called before
     *         {@link #connect(InetSocketAddress, SocketClientLayerListener)}
     */
    public void startListenerExecutor() {
        requireContext();
        layerContext.startListenerExecutor();
    }

    /**
     * Asserts that {@link #connect(InetSocketAddress, SocketClientLayerListener)}
     * has been called and the layer context exists.
     *
     * @throws IllegalStateException if {@link #layerContext} is
     *         {@code null}
     */
    private void requireContext() {
        if (layerContext == null) {
            throw new IllegalStateException("WhatsAppSocketClientLayer.connect must be called first");
        }
    }
}
