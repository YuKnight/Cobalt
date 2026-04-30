package com.github.auties00.cobalt.socket.threading;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Result returned by {@link SocketClientLayerContext#processInbound(int)}
 * to instruct the selector how to proceed after a read.
 *
 * <p>The selector picks the next action from the result: deliver
 * complete datagrams, flush handshake bytes, suspend the key while
 * delegated tasks run on a virtual thread, or close the connection.
 */
public sealed interface SocketClientInboundResult {
    /**
     * Indicates that processing finished normally and the selector
     * should continue its event loop.
     */
    record Continue() implements SocketClientInboundResult {

    }

    /**
     * Indicates that the layer needs to write data back to the channel.
     *
     * <p>Returned during TLS handshakes when the engine reports
     * {@code NEED_WRAP} and when WebSocket has to echo a control frame
     * (PONG response to a PING, CLOSE echo).
     *
     * @param data the buffers to write to the channel
     */
    record NeedsWrite(ByteBuffer... data) implements SocketClientInboundResult {
        /**
         * Validates the {@code data} component.
         *
         * @throws NullPointerException if {@code data} is {@code null}
         */
        public NeedsWrite {
            Objects.requireNonNull(data, "data cannot be null");
        }
    }

    /**
     * Indicates that the layer does not yet have enough bytes to
     * produce output and is waiting for more inbound data.
     */
    record Buffering() implements SocketClientInboundResult {

    }

    /**
     * Indicates that the layer has delegated CPU-heavy tasks (typically
     * {@code SSLEngine}'s {@code NEED_TASK}) to a virtual thread.
     *
     * <p>The selector clears the key's interest ops until the tasks
     * complete and {@link SocketClientLayerContext#runDelegatedTasks(Runnable)}
     * fires the callback that re-arms them.
     */
    record Suspended() implements SocketClientInboundResult {

    }

    /**
     * Indicates that the connection should be closed, either because
     * the channel reached end-of-stream, the TLS engine signalled
     * {@code CLOSED}, or an unrecoverable protocol error was detected.
     */
    record Close() implements SocketClientInboundResult {

    }
}
