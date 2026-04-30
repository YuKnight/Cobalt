package com.github.auties00.cobalt.socket.layer;

import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * One layer in the socket client stack, providing bidirectional byte
 * I/O and a handful of transport-control hooks.
 *
 * <p>Layers are composed bottom-to-top: a transport layer at the head,
 * then a tunnel layer for proxy support, then optional security layers
 * for TLS, and finally an application layer (WebSocket and the
 * WhatsApp datagram framer). Each layer delegates raw byte movement to
 * the inner layer it wraps and adds its own framing, encryption or
 * tunnelling on top.
 *
 * <p>The transport-control hooks ({@link #finishConnect()},
 * {@link #startHandshake(SocketClientLayerContext, long)},
 * {@link #registerLayerContext(SocketClientLayerContext)}) propagate
 * down the chain to the bottommost transport layer so higher-level
 * code can drive connection lifecycle without holding a reference to
 * the NIO channel or selector.
 *
 * @param <C> the type of layer context this layer publishes; the
 *            selector uses it to type its lookups
 */
public interface SocketClientLayer<C extends SocketClientLayerContext> {
    /**
     * Connects this layer (and every layer below it) to {@code address}.
     *
     * @param address  the remote endpoint
     * @param listener the callback that receives inbound events
     * @throws IOException if any layer fails to connect
     */
    void connect(InetSocketAddress address, SocketClientLayerListener listener) throws IOException;

    /**
     * Disconnects this layer and releases any resources it owns.
     */
    void disconnect();

    /**
     * Returns whether this layer (transitively) is currently connected.
     *
     * @return {@code true} if connected
     */
    boolean isConnected();

    /**
     * Sends one logical binary payload represented by the supplied
     * buffers.
     *
     * <p>Implementations may enqueue the buffers for asynchronous
     * write and may transform their content in place (framing,
     * masking, encryption). Callers must therefore treat each buffer
     * as transferred and avoid mutating it after this call.
     *
     * @param buffers payload buffers in send order
     * @throws IOException if the payload cannot be enqueued
     */
    void sendBinary(ByteBuffer... buffers) throws IOException;

    /**
     * Reads bytes into {@code buffer}.
     *
     * <p>The destination buffer stays in write mode after the call;
     * callers must invoke {@link ByteBuffer#flip()} themselves before
     * reading from it.
     *
     * @param buffer the destination buffer, in write mode
     * @param fully  {@code true} to keep reading until the buffer is
     *               full, {@code false} to return after the first
     *               successful read
     * @return the number of bytes read, or {@code -1} on
     *         end-of-stream
     * @throws IOException if reading fails
     */
    int readBinary(ByteBuffer buffer, boolean fully) throws IOException;

    /**
     * Finishes the connection setup and transitions to the
     * post-handshake asynchronous data flow.
     *
     * <p>Wrapper layers delegate to their inner layer; only the
     * transport implements the actual selector transition.
     *
     * @throws IOException if the transition fails
     */
    void finishConnect() throws IOException;

    /**
     * Variant of {@link #finishConnect()} that feeds leftover bytes
     * (typically read past a synchronous protocol upgrade boundary,
     * such as the WebSocket HTTP upgrade) into the pipeline before
     * asynchronous processing begins.
     *
     * @param leftover the leftover bytes in read mode, or {@code null}
     * @throws IOException if the transition fails
     */
    void finishConnect(ByteBuffer leftover) throws IOException;

    /**
     * Initiates a TLS handshake on the inner transport and blocks the
     * calling virtual thread until it completes.
     *
     * @param tlsContext the TLS layer context that drives the handshake
     * @param timeout    the handshake timeout in milliseconds
     * @throws IOException if the handshake fails or times out
     */
    void startHandshake(SocketClientLayerContext tlsContext, long timeout) throws IOException;

    /**
     * Registers a layer context with the selector pipeline so the
     * inbound read path can reach it.
     *
     * <p>Contexts are registered during stack construction in the same
     * order their layers are composed; the concrete context type
     * determines its position in the chain.
     *
     * @param context the layer context to register
     * @throws IOException if registration fails
     */
    void registerLayerContext(SocketClientLayerContext context) throws IOException;

}
