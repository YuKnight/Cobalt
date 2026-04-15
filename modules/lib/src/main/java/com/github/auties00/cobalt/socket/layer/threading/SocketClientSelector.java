package com.github.auties00.cobalt.socket.layer.threading;

import com.github.auties00.cobalt.socket.layer.transport.SocketClientTransportLayerContext;
import com.github.auties00.cobalt.socket.layer.tunnel.SocketClientTunnelLayerContext;

import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A singleton NIO event loop that multiplexes all socket connections on
 * a single virtual thread.
 *
 * <p>This selector is a thin orchestrator that reads bytes from the
 * channel into the bottommost layer context's
 * {@link SocketClientLayerContext#inboundTarget() inboundTarget()}, calls
 * {@link SocketClientLayerContext#processInbound(int) processInbound()},
 * and handles the resulting {@link SocketClientInboundResult}.  All protocol-specific
 * logic (TLS, WebSocket, datagram framing, proxy handshake) lives in
 * the layer contexts, not here.
 *
 * <p>The outbound write path drains the transport context's
 * {@link SocketClientTransportLayerContext.PendingWrites} queue.  If a
 * TLS layer context is present, buffers are wrapped before writing;
 * otherwise they are written directly via
 * {@link java.nio.channels.GatheringByteChannel}.
 */
public final class SocketClientSelector implements Runnable {
    /**
     * The singleton instance.
     */
    public static final SocketClientSelector INSTANCE;

    static {
        try {
            INSTANCE = new SocketClientSelector();
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static final long SAFETY_TIMEOUT_MS = 5000;

    private final System.Logger logger;
    private final Selector selector;
    private final AtomicBoolean wakeupPending;
    private volatile Thread selectorThread;

    private SocketClientSelector() throws IOException {
        this.logger = System.getLogger(SocketClientSelector.class.getName());
        this.selector = Selector.open();
        this.wakeupPending = new AtomicBoolean();
    }

    private void wakeup() {
        if (wakeupPending.compareAndSet(false, true)) {
            selector.wakeup();
        }
    }

    /**
     * Registers a channel with this selector.
     *
     * <p>Starts the selector virtual thread if it is not already running.
     *
     * @param channel          the non-blocking socket channel
     * @param transportContext the transport-level state for this connection
     * @throws IOException if registration fails
     */
    public synchronized void register(SocketChannel channel, SocketClientTransportLayerContext transportContext) throws IOException {
        selector.wakeup();
        channel.register(selector, SelectionKey.OP_CONNECT, SocketClientContext.newConnectionContext(transportContext));
        if (selectorThread == null || !selectorThread.isAlive()) {
            selectorThread = Thread.startVirtualThread(this);
        }
    }

    /**
     * Registers a layer context for the given channel.
     *
     * @param channel      the channel
     * @param layerContext the context to register
     * @return {@code true} if registered, {@code false} if the channel
     *         is not registered with this selector
     */
    public boolean registerLayerContext(SocketChannel channel, SocketClientLayerContext layerContext) {
        var selKey = channel.keyFor(selector);
        if (selKey == null) {
            return false;
        }
        ((SocketClientContext) selKey.attachment()).addLayerContext(layerContext);
        return true;
    }

    /**
     * Unregisters a channel, cancels its key, closes the channel, and
     * notifies all layer contexts.
     *
     * @param channel the channel to unregister
     */
    public void unregister(SocketChannel channel) {
        var key = channel.keyFor(selector);
        if (key == null) {
            return;
        }

        key.cancel();

        var ctx = (SocketClientContext) key.attachment();
        var transportCtx = ctx.transportContext();

        // Notify connection lock
        synchronized (transportCtx.connectionLock) {
            transportCtx.connectionLock.notifyAll();
        }

        // Use connected CAS to guard single notification
        if (!transportCtx.compareAndSetConnected(true, false)) {
            try {
                channel.close();
            } catch (IOException _) {
            }
            wakeup();
            return;
        }

        // Notify all layer contexts before closing the channel so that
        // the TLS layer can send its close_notify alert (RFC 8446 §6.1)
        var bottom = ctx.bottomProcessingContext();
        if (bottom != null) {
            bottom.onDisconnect();
        }

        try {
            channel.close();
        } catch (IOException _) {
        }

        wakeup();
    }

    /**
     * Returns whether the given channel is connected.
     *
     * @param channel the channel to check
     * @return {@code true} if connected
     */
    public boolean isConnected(SocketChannel channel) {
        if (channel == null) {
            return false;
        }

        var key = channel.keyFor(selector);
        if (key == null) {
            return false;
        }

        return ((SocketClientContext) key.attachment()).transportContext().isConnected();
    }

    /**
     * Posts a blocking read request for the pre-tunnel phase.
     *
     * @param channel the channel
     * @param read    the pending read request
     * @return {@code true} if the read was posted
     */
    public boolean addRead(SocketChannel channel, SocketClientTransportLayerContext.PendingRead read) {
        var key = channel.keyFor(selector);
        if (key == null || !key.isValid()) {
            return false;
        }

        var ctx = (SocketClientContext) key.attachment();
        var tunnelCtx = ctx.tunnelContext();
        if (tunnelCtx.isEmpty()) {
            return false;
        }

        if (!tunnelCtx.get().setPendingRead(read)) {
            return false;
        }

        try {
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        } catch (CancelledKeyException _) {
            return false;
        }
        wakeup();
        return true;
    }

    /**
     * Enqueues outbound buffers for writing.
     *
     * @param channel the channel
     * @param buffers the buffers to write
     * @return {@code true} if the write was enqueued
     */
    public boolean addWrite(SocketChannel channel, ByteBuffer... buffers) {
        var key = channel.keyFor(selector);
        if (key == null || !key.isValid()) {
            return false;
        }

        if (buffers == null || buffers.length == 0) {
            return true;
        }

        var ctx = (SocketClientContext) key.attachment();
        var pendingWrites = ctx.transportContext().pendingWrites;
        var hasWrites = false;
        for (var buffer : buffers) {
            if (buffer != null && buffer.hasRemaining()) {
                if (!pendingWrites.offer(buffer)) {
                    return false;
                }
                hasWrites = true;
            }
        }
        if (!hasWrites) {
            return true;
        }

        try {
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        } catch (CancelledKeyException _) {
            return false;
        }
        wakeup();
        return true;
    }

    /**
     * Marks the connection as ready and transitions it to asynchronous
     * data flow.
     *
     * <p>Marks the tunnel context as tunnelled and enables read interest.
     *
     * @param channel the channel
     * @return {@code true} if successfully marked
     */
    public boolean finishConnect(SocketChannel channel) {
        var key = channel.keyFor(selector);
        if (key == null || !key.isValid()) {
            return false;
        }

        var ctx = (SocketClientContext) key.attachment();

        // Mark tunnel as established
        ctx.tunnelContext().ifPresent(SocketClientTunnelLayerContext::markTunnelled);

        try {
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        } catch (CancelledKeyException _) {
            return false;
        }
        wakeup();
        return true;
    }

    /**
     * Marks the connection as ready, feeds leftover bytes into the
     * pipeline, and drains any buffered TLS application data.
     *
     * @param channel  the channel
     * @param leftover leftover bytes from a synchronous parser, or {@code null}
     * @return {@code true} if finalization succeeded
     */
    public boolean finishConnect(SocketChannel channel, ByteBuffer leftover) {
        if (!finishConnect(channel)) {
            return false;
        }
        if (leftover != null && leftover.hasRemaining() && !preSeedDatagram(channel, leftover)) {
            return false;
        }
        return drainAppBuffer(channel);
    }

    /**
     * Initiates a security handshake and blocks the calling virtual thread
     * until it completes.
     *
     * @param channel         the channel
     * @param securityContext  the security layer context to handshake
     * @param timeout         the handshake timeout in milliseconds
     * @throws IOException if the handshake fails or times out
     */
    public void startHandshake(SocketChannel channel, SocketClientLayerContext securityContext, long timeout) throws IOException {
        var key = channel.keyFor(selector);
        if (key == null || !key.isValid()) {
            throw new IOException("Channel not registered");
        }

        var ctx = (SocketClientContext) key.attachment();

        securityContext.beginHandshake();

        try {
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (CancelledKeyException e) {
            throw new IOException("Key cancelled during handshake init");
        }
        wakeup();

        var lock = securityContext.handshakeLock();
        if (lock == null) {
            throw new IOException("Security context does not support handshaking");
        }

        synchronized (lock) {
            var deadline = System.currentTimeMillis() + timeout;
            while (!securityContext.isHandshakeComplete() && ctx.transportContext().isConnected()) {
                var remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    throw new IOException("Handshake timed out");
                }
                try {
                    lock.wait(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Handshake interrupted", e);
                }
            }
        }

        if (!securityContext.isHandshakeComplete()) {
            throw new IOException("Handshake failed: connection lost");
        }
    }

    /**
     * Drains any leftover buffered data from all layer contexts into
     * the layer pipeline.
     *
     * @param channel the channel
     * @return {@code true} if draining succeeded or no layers needed
     *         draining, {@code false} if draining failed
     */
    public boolean drainAppBuffer(SocketChannel channel) {
        var key = channel.keyFor(selector);
        if (key == null) {
            return false;
        }

        var ctx = (SocketClientContext) key.attachment();
        try {
            return ctx.drainAllLayers();
        } catch (IOException _) {
            return false;
        }
    }

    /**
     * Feeds leftover bytes from a proxy handshake into the datagram
     * pipeline.
     *
     * @param channel  the channel
     * @param leftover the leftover bytes in read mode
     * @return {@code true} if processing succeeded
     */
    public boolean preSeedDatagram(SocketChannel channel, ByteBuffer leftover) {
        if (!leftover.hasRemaining()) {
            return true;
        }

        var key = channel.keyFor(selector);
        if (key == null) {
            return false;
        }

        var ctx = (SocketClientContext) key.attachment();

        // Leftover bytes from the HTTP upgrade parser are already
        // TLS-decrypted.  Feed them into the tunnel context (the first
        // non-TLS context in the chain) rather than the TLS bottom, so
        // they are not erroneously re-decrypted.
        var tunnelCtx = ctx.tunnelContext();
        SocketClientLayerContext target;
        if (tunnelCtx.isPresent()) {
            target = tunnelCtx.get();
        } else {
            target = ctx.bottomProcessingContext();
            if (target == null) {
                return false;
            }
        }

        try {
            return !(target.feedFromSource(leftover) instanceof SocketClientInboundResult.Close);
        } catch (IOException _) {
            return false;
        }
    }

    @Override
    public void run() {
        try {
            while (selector.isOpen()) {
                var readyChannels = selector.select(SAFETY_TIMEOUT_MS);
                wakeupPending.set(false);
                if (readyChannels > 0) {
                    var iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        var key = iterator.next();
                        iterator.remove();
                        handleKey(key);
                    }
                }
                if (selector.keys().isEmpty()) {
                    synchronized (this) {
                        if (selector.keys().isEmpty()) {
                            selectorThread = null;
                            break;
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            logger.log(Level.ERROR, throwable);

            for (var key : selector.keys()) {
                if (key.channel() instanceof SocketChannel socketChannel) {
                    unregister(socketChannel);
                }
            }

            synchronized (this) {
                selectorThread = null;
            }
        }
    }

    @SuppressWarnings("MagicConstant")
    private void handleKey(SelectionKey key) {
        var attachment = key.attachment();
        if (!(attachment instanceof SocketClientContext ctx)) {
            return;
        }

        var channel = (SocketChannel) key.channel();
        var transportCtx = ctx.transportContext();

        try {
            if (key.isConnectable()) {
                if (channel.finishConnect()) {
                    key.interestOps(SelectionKey.OP_READ);
                    synchronized (transportCtx.connectionLock) {
                        transportCtx.connectionLock.notifyAll();
                    }
                }
            }

            // Check for any layer currently handshaking
            var handshakingCtx = ctx.findHandshakingContext();
            if (handshakingCtx.isPresent()) {
                var hCtx = handshakingCtx.get();
                if (hCtx.isTasksPending()) {
                    return;
                }
                if (!processHandshake(channel, ctx, key, hCtx)) {
                    unregister(channel);
                }
                return;
            }

            if (key.isReadable()) {
                if (!processRead(channel, ctx)) {
                    unregister(channel);
                    return;
                }
            }
            if (key.isWritable()) {
                processWrite(channel, ctx);
                var hasPendingWrites = !transportCtx.pendingWrites.isEmpty() || ctx.hasPendingOutput();
                key.interestOps(updateWriteInterestOps(key.interestOps(), hasPendingWrites));
            }
        } catch (Exception _) {
            unregister(channel);
        }
    }

    private boolean processHandshake(SocketChannel channel, SocketClientContext ctx, SelectionKey key, SocketClientLayerContext layerCtx) throws IOException {
        var result = layerCtx.driveHandshake(channel);
        return handleInboundResult(ctx, key, result);
    }

    private boolean processRead(SocketChannel channel, SocketClientContext ctx) throws IOException {
        var bottom = ctx.bottomProcessingContext();
        if (bottom == null) {
            return false;
        }

        var target = bottom.inboundTarget();
        var bytesRead = channel.read(target);
        var result = bottom.processInbound(bytesRead);

        var key = channel.keyFor(selector);
        return key != null && handleInboundResult(ctx, key, result);
    }

    private boolean handleInboundResult(SocketClientContext ctx, SelectionKey key, SocketClientInboundResult result) {
        return switch (result) {
            case SocketClientInboundResult.Continue _, SocketClientInboundResult.Buffering _ -> true;
            case SocketClientInboundResult.Close _ -> false;
            case SocketClientInboundResult.NeedsWrite needsWrite -> {
                // Enqueue the write data and enable write interest
                var transportCtx = ctx.transportContext();
                for (var buf : needsWrite.data()) {
                    if (buf != null && buf.hasRemaining()) {
                        transportCtx.pendingWrites.offer(buf);
                    }
                }
                try {
                    key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                } catch (CancelledKeyException _) {
                    yield false;
                }
                wakeup();
                yield true;
            }
            case SocketClientInboundResult.Suspended _ -> {
                // Delegated tasks pending — disable interest, run tasks
                try {
                    key.interestOps(0);
                } catch (CancelledKeyException _) {
                    yield true;
                }
                ctx.findTasksPendingContext().ifPresent(layer -> layer.runDelegatedTasks(() -> {
                    try {
                        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    } catch (CancelledKeyException _) {
                        return;
                    }
                    wakeup();
                }));
                yield true;
            }
        };
    }

    private boolean processWrite(SocketChannel channel, SocketClientContext ctx) throws IOException {
        var transportCtx = ctx.transportContext();
        var bottom = ctx.bottomProcessingContext();
        while (transportCtx.isConnected()) {
            var claim = transportCtx.pendingWrites.claim();
            if (claim.isEmpty()) {
                return true;
            }

            boolean success;
            if (bottom != null) {
                success = bottom.processOutbound(channel, claim.array(), claim.offset(), claim.count());
            } else {
                channel.write(claim.array(), claim.offset(), claim.count());
                success = true;
            }

            var consumed = countConsumed(claim);
            transportCtx.pendingWrites.release(consumed);
            if (!success) {
                return false;
            }
        }
        return false;
    }

    private static int countConsumed(SocketClientTransportLayerContext.PendingWrites.Claim claim) {
        var consumed = 0;
        for (var i = claim.offset(); i < claim.offset() + claim.count(); i++) {
            if (claim.array()[i].hasRemaining()) {
                break;
            }
            consumed++;
        }
        return consumed;
    }

    static int updateWriteInterestOps(int currentOps, boolean hasPendingWrites) {
        return hasPendingWrites
                ? (currentOps | SelectionKey.OP_WRITE)
                : (currentOps & ~SelectionKey.OP_WRITE);
    }
}
