package com.github.auties00.cobalt.socket.layer.application.whatsapp;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.socket.layer.SocketClientLayerListener;
import com.github.auties00.cobalt.socket.layer.application.SocketClientApplicationLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientInboundResult;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientPendingRead;
import com.github.auties00.cobalt.util.DataUtils;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Topmost layer context in the read pipeline; reassembles
 * length-prefixed WhatsApp datagrams and delivers them to the
 * application listener.
 *
 * <p>Each message on the wire is prefixed with a 3-byte big-endian
 * integer that encodes the payload length. The context drives a
 * two-phase state machine (length prefix, then payload) and pushes
 * complete datagrams onto a single-threaded virtual executor so the
 * listener observes them in arrival order.
 *
 * <p>Buffer management is tuned for zero copy:
 * <ul>
 * <li>{@link #datagramLengthBuffer} is allocated once and reused for
 *     every datagram.</li>
 * <li>{@link #datagramBuffer} is allocated per message to the exact
 *     decoded length, handed to the listener and never reused.</li>
 * <li>Lower layers (and the selector) read or unwrap directly into
 *     whichever buffer {@link #inboundTarget()} returns.</li>
 * </ul>
 */
@WhatsAppWebModule(moduleName = "WAFrameSocket")
final class WhatsAppSocketClientLayerContext implements SocketClientApplicationLayerContext {
    /**
     * Number of bytes in the int24 length prefix.
     */
    private static final int INT24_BYTE_SIZE = 3;

    /**
     * Largest length encodable in an unsigned int24, used as a
     * defensive upper bound on inbound datagrams (WA Web does not
     * validate inbound length at all).
     */
    private static final int MAX_MESSAGE_LENGTH = 0xFFFFFF;

    /**
     * Listener that receives decoded datagrams and the close event.
     */
    private final SocketClientLayerListener listener;

    /**
     * 3-byte buffer used to read the length prefix of every inbound
     * datagram, allocated once and reused.
     */
    private final ByteBuffer datagramLengthBuffer;

    /**
     * Lock guarding the lifecycle of {@link #listenerExecutor}.
     */
    private final Object executorLock;

    /**
     * Whether this context is still in handshake mode.
     *
     * <p>In handshake mode inbound bytes are routed to the pending
     * blocking read instead of being reassembled as datagrams; this
     * lets the Noise handshake run as a synchronous flow while the
     * NIO chain is asynchronous. Transitions from {@code true} to
     * {@code false} exactly once via {@link #markHandshakeComplete()}.
     */
    private volatile boolean handshakeMode;

    /**
     * Pending blocking read request from the Noise handshake thread,
     * or {@code null} when no read is in flight.
     */
    private volatile SocketClientPendingRead pendingHandshakeRead;

    /**
     * Holds bytes that arrived during handshake mode before the
     * handshake thread had posted its pending read.
     *
     * <p>Written by the selector thread and drained by the handshake
     * virtual thread, both under {@link #handshakeLock}.
     */
    private ByteBuffer handshakeBuffer;

    /**
     * Monitor guarding {@link #handshakeBuffer} and
     * {@link #pendingHandshakeRead} against races between the selector
     * thread and the handshake virtual thread.
     */
    private final Object handshakeLock = new Object();

    /**
     * Per-message payload buffer, allocated once the length prefix is
     * known and discarded after the datagram is handed to the listener.
     */
    private ByteBuffer datagramBuffer;

    /**
     * Single-threaded virtual executor that serializes datagram
     * delivery off the selector thread.
     */
    private volatile ExecutorService listenerExecutor;

    /**
     * Previous layer in the chain (the layer below WhatsApp). The
     * WhatsApp context is the tail of the chain, so it has no next
     * layer.
     */
    private volatile SocketClientLayerContext prevLayer;

    /**
     * Initializes the layer context in handshake mode.
     *
     * @param listener the listener that will receive decoded datagrams
     */
    private WhatsAppSocketClientLayerContext(SocketClientLayerListener listener) {
        this.listener = Objects.requireNonNull(listener, "listener cannot be null");
        this.datagramLengthBuffer = ByteBuffer.allocate(INT24_BYTE_SIZE);
        this.executorLock = new Object();
        this.handshakeMode = true;
    }

    /**
     * Factory used by {@link WhatsAppSocketClientLayer} to construct a
     * fresh layer context bound to the given listener.
     *
     * @param listener the listener that will receive decoded datagrams
     *                 and the close event
     * @return a new {@code WhatsAppSocketClientLayerContext}
     */
    static WhatsAppSocketClientLayerContext newAppContext(SocketClientLayerListener listener) {
        return new WhatsAppSocketClientLayerContext(listener);
    }

    @Override
    public void setPrevLayer(SocketClientLayerContext prev) {
        this.prevLayer = prev;
    }

    @Override
    public SocketClientLayerContext prevLayer() {
        return prevLayer;
    }

    /**
     * Returns the buffer that should receive the next inbound bytes.
     *
     * <p>In handshake mode the pending read's destination is returned
     * (or an empty buffer if no read is posted yet); after the
     * handshake the buffer is either {@link #datagramLengthBuffer}
     * (when the length prefix is still being read) or
     * {@link #datagramBuffer} (when a payload is being accumulated).
     *
     * @return the buffer to read or unwrap into, in write mode
     */
    @Override
    public ByteBuffer inboundTarget() {
        if (handshakeMode) {
            var read = pendingHandshakeRead;
            return read != null ? read.buffer : DataUtils.EMPTY_BYTE_BUFFER;
        }
        return datagramBuffer != null ? datagramBuffer : datagramLengthBuffer;
    }

    /**
     * Drives the datagram reassembly state machine on the bytes just
     * placed into {@link #inboundTarget()}.
     *
     * <p>End-of-stream maps to {@link SocketClientInboundResult.Close};
     * an incomplete length prefix or payload yields
     * {@link SocketClientInboundResult.Buffering}; once a payload is
     * complete the datagram is dispatched to the listener and the
     * state resets for the next message.
     *
     * @param bytesRead the number of bytes placed into the target, or
     *                  {@code -1} for end-of-stream
     * @return the processing result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAFrameSocket", exports = "FrameSocket", adaptation = WhatsAppAdaptation.ADAPTED)
    public SocketClientInboundResult processInbound(int bytesRead) {
        if (handshakeMode) {
            return processHandshakeRead(bytesRead);
        }

        if (bytesRead == -1) {
            return new SocketClientInboundResult.Close();
        }

        if (bytesRead == 0) {
            return new SocketClientInboundResult.Buffering();
        }

        if (datagramBuffer == null) {
            // Still reading the 3-byte length prefix
            if (datagramLengthBuffer.hasRemaining()) {
                return new SocketClientInboundResult.Buffering();
            }

            // Length prefix complete — decode and allocate payload buffer
            datagramLengthBuffer.flip();
            var length = ((datagramLengthBuffer.get() & 0xFF) << 16)
                    | ((datagramLengthBuffer.get() & 0xFF) << 8)
                    | (datagramLengthBuffer.get() & 0xFF);
            datagramLengthBuffer.clear();

            if (length < 0 || length > MAX_MESSAGE_LENGTH) {
                return new SocketClientInboundResult.Close();
            }

            datagramBuffer = ByteBuffer.allocate(length);
            return new SocketClientInboundResult.Buffering();
        }

        // Still reading the payload
        if (datagramBuffer.hasRemaining()) {
            return new SocketClientInboundResult.Buffering();
        }

        // Payload complete — deliver
        datagramBuffer.flip();
        var completed = datagramBuffer;
        datagramBuffer = null;
        listenerExecutor.execute(() -> listener.onDatagram(completed));
        return new SocketClientInboundResult.Continue();
    }

    /**
     * Feeds bytes from an upstream layer (TLS or WebSocket) directly
     * into the datagram reassembly state machine, bypassing the
     * intermediate accumulation buffer that the default implementation
     * would otherwise use.
     *
     * @param source the buffer containing decoded bytes, in read mode
     * @return the processing result
     */
    @WhatsAppWebExport(moduleName = "WAFrameSocket", exports = "FrameSocket", adaptation = WhatsAppAdaptation.ADAPTED)
    public SocketClientInboundResult feedFromSource(ByteBuffer source) {
        if (handshakeMode) {
            return feedHandshakeRead(source);
        }

        while (source.hasRemaining()) {
            var noDatagram = datagramBuffer == null;
            var target = noDatagram ? datagramLengthBuffer : datagramBuffer;
            var count = Math.min(source.remaining(), target.remaining());
            var savedLimit = source.limit();
            source.limit(source.position() + count);
            target.put(source);
            source.limit(savedLimit);

            if (!target.hasRemaining()) {
                if (noDatagram) {
                    // Length prefix complete
                    datagramLengthBuffer.flip();
                    var length = ((datagramLengthBuffer.get() & 0xFF) << 16)
                            | ((datagramLengthBuffer.get() & 0xFF) << 8)
                            | (datagramLengthBuffer.get() & 0xFF);
                    datagramLengthBuffer.clear();

                    if (length < 0 || length > MAX_MESSAGE_LENGTH) {
                        return new SocketClientInboundResult.Close();
                    }

                    datagramBuffer = ByteBuffer.allocate(length);
                } else {
                    // Payload complete
                    datagramBuffer.flip();
                    var completed = datagramBuffer;
                    datagramBuffer = null;
                    listenerExecutor.execute(() -> listener.onDatagram(completed));
                }
            }
        }
        return new SocketClientInboundResult.Continue();
    }

    /**
     * Handles a completion notification while the context is still in
     * handshake mode, fulfilling or failing the pending blocking read.
     *
     * @param bytesRead the number of bytes read, or {@code -1} on
     *                  end-of-stream
     * @return the processing result
     */
    private SocketClientInboundResult processHandshakeRead(int bytesRead) {
        var read = pendingHandshakeRead;
        if (read == null) {
            return new SocketClientInboundResult.Buffering();
        }

        if (bytesRead == -1) {
            read.length = -1;
            synchronized (read.lock) {
                read.lock.notifyAll();
            }
            return new SocketClientInboundResult.Close();
        }

        if (bytesRead == 0) {
            return new SocketClientInboundResult.Buffering();
        }

        if (read.length == -1) {
            read.length = 0;
        }
        read.length += bytesRead;

        if (!read.fullRead || !read.buffer.hasRemaining()) {
            pendingHandshakeRead = null;
            synchronized (read.lock) {
                read.lock.notifyAll();
            }
        }

        return new SocketClientInboundResult.Continue();
    }

    /**
     * Feeds decoded bytes from an upstream layer into the pending
     * handshake read, buffering any leftover bytes for the next read.
     *
     * @param source the buffer containing decoded bytes, in read mode
     * @return the processing result
     */
    private SocketClientInboundResult feedHandshakeRead(ByteBuffer source) {
        synchronized (handshakeLock) {
            var read = pendingHandshakeRead;
            if (read != null) {
                var count = Math.min(source.remaining(), read.buffer.remaining());
                var savedLimit = source.limit();
                source.limit(source.position() + count);
                read.buffer.put(source);
                source.limit(savedLimit);

                if (read.length == -1) {
                    read.length = 0;
                }
                read.length += count;

                if (!read.fullRead || !read.buffer.hasRemaining()) {
                    pendingHandshakeRead = null;
                    synchronized (read.lock) {
                        read.lock.notifyAll();
                    }
                }
            }

            // Buffer any unconsumed bytes for the next read
            if (source.hasRemaining()) {
                if (handshakeBuffer == null) {
                    handshakeBuffer = ByteBuffer.allocate(source.remaining());
                } else if (handshakeBuffer.remaining() < source.remaining()) {
                    var newBuf = ByteBuffer.allocate(handshakeBuffer.position() + source.remaining());
                    handshakeBuffer.flip();
                    newBuf.put(handshakeBuffer);
                    handshakeBuffer = newBuf;
                }
                handshakeBuffer.put(source);
            }

            return new SocketClientInboundResult.Continue();
        }
    }

    /**
     * Posts a pending handshake read so the selector can fulfil it
     * with bytes pushed from upstream layers.
     *
     * <p>If bytes have already been buffered while no read was
     * pending, they are drained into the request before this method
     * returns.
     *
     * @param read the pending read request
     * @return {@code true} if the read was accepted, {@code false} if
     *         another read is already pending
     */
    public boolean setPendingRead(SocketClientPendingRead read) {
        synchronized (handshakeLock) {
            if (pendingHandshakeRead != null) {
                return false;
            }
            pendingHandshakeRead = read;

            // Drain any data that arrived before this read was posted
            if (handshakeBuffer != null && handshakeBuffer.position() > 0) {
                handshakeBuffer.flip();
                drainHandshakeBuffer();
                if (handshakeBuffer != null && !handshakeBuffer.hasRemaining()) {
                    handshakeBuffer = null;
                }
            }
            return true;
        }
    }

    /**
     * Drains {@link #handshakeBuffer} into the currently posted
     * handshake read.
     *
     * <p>Must be called under {@link #handshakeLock}.
     */
    private void drainHandshakeBuffer() {
        var read = pendingHandshakeRead;
        if (read == null || handshakeBuffer == null || !handshakeBuffer.hasRemaining()) {
            return;
        }

        var count = Math.min(handshakeBuffer.remaining(), read.buffer.remaining());
        var savedLimit = handshakeBuffer.limit();
        handshakeBuffer.limit(handshakeBuffer.position() + count);
        read.buffer.put(handshakeBuffer);
        handshakeBuffer.limit(savedLimit);

        if (read.length == -1) {
            read.length = 0;
        }
        read.length += count;

        if (!read.fullRead || !read.buffer.hasRemaining()) {
            pendingHandshakeRead = null;
            synchronized (read.lock) {
                read.lock.notifyAll();
            }
        }

        if (handshakeBuffer.hasRemaining()) {
            handshakeBuffer.compact();
        } else {
            handshakeBuffer = null;
        }
    }

    /**
     * Transitions this context from handshake mode to datagram
     * reassembly mode.
     *
     * <p>After this call inbound bytes are reassembled as int24-framed
     * datagrams and dispatched to the listener.
     */
    public void markHandshakeComplete() {
        this.handshakeMode = false;
    }

    /**
     * Lazily starts the single-threaded virtual executor that
     * serializes datagram delivery off the selector thread.
     */
    public void startListenerExecutor() {
        if (listenerExecutor == null || listenerExecutor.isShutdown()) {
            synchronized (executorLock) {
                if (listenerExecutor == null || listenerExecutor.isShutdown()) {
                    listenerExecutor = Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());
                }
            }
        }
    }

    /**
     * Tears down per-connection state on disconnect.
     *
     * <p>Completes any pending handshake read with end-of-stream so
     * the handshake virtual thread unblocks and observes the
     * disconnection, shuts down {@link #listenerExecutor} and finally
     * invokes the listener's {@link SocketClientLayerListener#onClose()}.
     */
    @Override
    public void onDisconnect() {
        var read = pendingHandshakeRead;
        if (read != null) {
            read.length = -1;
            pendingHandshakeRead = null;
            synchronized (read.lock) {
                read.lock.notifyAll();
            }
        }

        if (listenerExecutor != null && !listenerExecutor.isShutdown()) {
            synchronized (executorLock) {
                if (listenerExecutor != null && !listenerExecutor.isShutdown()) {
                    listenerExecutor.shutdownNow();
                    listenerExecutor = null;
                }
            }
        }

        try {
            listener.onClose();
        } catch (Throwable _) {
            // Listener exceptions during disconnect are swallowed so
            // the rest of the teardown completes.
        }
    }
}
