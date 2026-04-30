package com.github.auties00.cobalt.socket.threading;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Per-connection state and processing logic for a single layer in the
 * socket layer stack.
 *
 * <p>Each context owns its own buffers and protocol state (an
 * {@link javax.net.ssl.SSLEngine} for TLS, a frame decoder for
 * WebSocket, the int24 reassembly state machine for the WhatsApp
 * datagram layer). The {@link SocketClientSelector} drives the inbound
 * read path by calling {@link #processInbound(int)} on the head of the
 * chain; each layer then forwards its decoded bytes to the next layer
 * up via {@link #feedFromSource(ByteBuffer)}.
 *
 * <p>Outbound encoding (application framing, WebSocket framing, Noise
 * encryption) happens on the caller's virtual thread when
 * {@code sendBinary} is invoked; only TLS wrapping happens on the
 * selector thread during the write phase.
 */
public interface SocketClientLayerContext {
    /**
     * Returns the buffer into which inbound bytes should be placed.
     *
     * <p>For the head of the chain this is the buffer the selector
     * passes to {@code channel.read()}. For an intermediate TLS layer
     * it is the encrypted-data buffer; the layer unwraps directly into
     * the next layer's {@code inboundTarget()} so the decrypted bytes
     * land where they will be consumed without an intermediate copy.
     *
     * <p>The returned buffer is always in write mode, ready for
     * {@code put()} or {@code channel.read()}.
     *
     * @return the buffer to read or unwrap into
     */
    ByteBuffer inboundTarget();

    /**
     * Processes {@code bytesRead} bytes just placed into
     * {@link #inboundTarget()}.
     *
     * <p>The layer decodes the bytes according to its protocol and, as
     * soon as a complete unit is available, forwards it to the next
     * layer up via {@link #feedFromSource(ByteBuffer)}. The returned
     * {@link SocketClientInboundResult} tells the selector what to do
     * next: continue, wait for more data, flush handshake bytes,
     * suspend while delegated tasks run, or close the connection.
     *
     * @param bytesRead the number of bytes placed into the inbound
     *                  target, or {@code -1} if the channel reached
     *                  end-of-stream
     * @return the processing result
     * @throws IOException if an I/O error occurs during processing
     */
    SocketClientInboundResult processInbound(int bytesRead) throws IOException;

    /**
     * Feeds bytes from an upstream layer into this layer's processing
     * pipeline.
     *
     * <p>Used by intermediate layers (TLS in particular) that decode
     * bytes into their own buffers and then have to push them into the
     * next layer. The default implementation copies the source into
     * {@link #inboundTarget()} in chunks and calls
     * {@link #processInbound(int)} after each chunk; layers that can
     * skip the intermediate copy should override.
     *
     * @param source the buffer containing decoded bytes, in read mode
     * @return the processing result
     * @throws IOException if an I/O error occurs during processing
     */
    default SocketClientInboundResult feedFromSource(ByteBuffer source) throws IOException {
        while (source.hasRemaining()) {
            var target = inboundTarget();
            var count = Math.min(source.remaining(), target.remaining());
            var savedLimit = source.limit();
            source.limit(source.position() + count);
            target.put(source);
            source.limit(savedLimit);

            var result = processInbound(count);
            if (!(result instanceof SocketClientInboundResult.Continue)
                    && !(result instanceof SocketClientInboundResult.Buffering)) {
                return result;
            }
        }
        return new SocketClientInboundResult.Continue();
    }

    /**
     * Sets the next layer context in the inbound chain (the layer
     * immediately above this one), to which this layer's decoded
     * bytes flow.
     *
     * @param next the next layer context
     */
    default void setNextLayer(SocketClientLayerContext next) {
    }

    /**
     * Sets the previous layer context in the outbound chain (the layer
     * immediately below this one), to which this layer's encoded bytes
     * flow on the way to the channel.
     *
     * @param prev the previous layer context
     */
    default void setPrevLayer(SocketClientLayerContext prev) {
    }

    /**
     * Returns the previous layer in the outbound chain, or
     * {@code null} when this layer is the head (the transport).
     *
     * @return the previous layer context or {@code null}
     */
    default SocketClientLayerContext prevLayer() {
        return null;
    }

    /**
     * Releases resources and notifies waiters when the connection is
     * being torn down.
     *
     * <p>Invoked by the selector during unregistration, before the
     * channel is closed, so layers can perform graceful cleanup
     * (close-notify alerts, blocked-thread wakeups, executor shutdown).
     */
    void onDisconnect();

    /**
     * Offers a pending blocking-read request to this layer.
     *
     * <p>Used during synchronous handshake phases (proxy handshake,
     * WebSocket upgrade, Noise handshake) where the caller thread
     * blocks until the selector delivers bytes into a specific buffer.
     * The selector walks the chain tail-to-head and routes the read
     * to the first context that accepts it.
     *
     * @param read the pending read request
     * @return {@code true} if the request was accepted
     */
    default boolean setPendingRead(SocketClientPendingRead read) {
        return false;
    }

    /**
     * Returns whether this layer is currently performing a handshake.
     *
     * @return {@code true} if handshaking
     */
    default boolean isHandshaking() {
        return false;
    }

    /**
     * Returns whether this layer has delegated tasks waiting to run on
     * a virtual thread.
     *
     * @return {@code true} if tasks are pending
     */
    default boolean isTasksPending() {
        return false;
    }

    /**
     * Drives the handshake state machine with direct channel I/O.
     *
     * @param channel the socket channel for direct I/O
     * @return the result indicating what the selector should do next
     * @throws IOException if an I/O error occurs during handshake
     */
    default SocketClientInboundResult driveHandshake(SocketChannel channel) throws IOException {
        return new SocketClientInboundResult.Continue();
    }

    /**
     * Runs delegated tasks asynchronously and invokes the supplied
     * callback when they finish.
     *
     * @param onComplete callback to run once every delegated task has
     *                   completed
     */
    default void runDelegatedTasks(Runnable onComplete) {
        onComplete.run();
    }

    /**
     * Initiates this layer's handshake. The default is a no-op for
     * layers that do not handshake.
     *
     * @throws IOException if the handshake cannot be initiated
     */
    default void beginHandshake() throws IOException {
    }

    /**
     * Returns whether the handshake has completed successfully.
     *
     * @return {@code true} if the handshake has finished, {@code true}
     *         by default for layers that do not handshake
     */
    default boolean isHandshakeComplete() {
        return true;
    }

    /**
     * Returns the monitor used to synchronize handshake completion, or
     * {@code null} when this layer does not support blocking
     * handshakes.
     *
     * @return the handshake lock or {@code null}
     */
    default Object handshakeLock() {
        return null;
    }
    /**
     * Processes outbound buffers through this layer, walking down the
     * chain toward the transport.
     *
     * <p>The default implementation delegates straight to
     * {@link #prevLayer()}; layers that transform outbound bytes (TLS
     * wrap, framing, compression) override to apply their
     * transformation before delegating. The transport at the head of
     * the chain has no previous layer and writes directly to the
     * channel.
     *
     * @param channel the socket channel to write to
     * @param buffers the data buffers to write
     * @param offset  the offset into the buffers array
     * @param count   the number of buffers to process
     * @return {@code true} if every byte was written, {@code false} if
     *         the channel was not ready and the caller should retry
     * @throws IOException if an I/O error occurs during writing
     */
    default boolean processOutbound(SocketChannel channel, ByteBuffer[] buffers, int offset, int count) throws IOException {
        var prev = prevLayer();
        if (prev != null) {
            return prev.processOutbound(channel, buffers, offset, count);
        }
        channel.write(buffers, offset, count);
        return true;
    }

    /**
     * Returns whether this layer has buffered output waiting to be
     * flushed.
     *
     * @return {@code true} if pending output exists
     */
    default boolean hasPendingOutput() {
        return false;
    }
    /**
     * Drains any buffered decoded bytes into the next layer.
     *
     * @return {@code true} if draining succeeded or there was nothing
     *         to drain, {@code false} if the next layer signalled
     *         close
     * @throws IOException if layer processing fails
     */
    default boolean drainToNextLayer() throws IOException {
        return true;
    }

}
