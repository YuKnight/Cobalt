package com.github.auties00.cobalt.socket.implementation.threading;

import com.github.auties00.cobalt.socket.implementation.SocketListener;
import com.github.auties00.cobalt.socket.implementation.context.SocketContext;
import com.github.auties00.cobalt.socket.implementation.context.SocketPendingRead;
import com.github.auties00.cobalt.socket.implementation.context.SocketPendingWrites;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A singleton NIO selector that multiplexes all socket connections onto a
 * single daemon platform thread.
 *
 * <p> Channels are registered via {@link #register(SocketChannel, int, SocketContext)}
 * and deregistered via {@link #unregister(SocketChannel)}.  The selector
 * thread is started lazily on the first registration and exits automatically
 * when no channels remain.  A subsequent registration restarts it.
 *
 * <p> The selector processes three kinds of I/O events:
 * <ol>
 *   <li><em>Connect</em> -- completes a non-blocking
 *       {@link SocketChannel#connect connect} and notifies the waiting
 *       thread via {@link SocketContext#connectionLock}.</li>
 *   <li><em>Read</em> -- in the pre-tunnel phase, fills a
 *       {@link SocketPendingRead} and notifies the caller; in the
 *       post-tunnel phase, reassembles length-prefixed WhatsApp datagrams
 *       and delivers them to
 *       {@link SocketListener#onDatagram
 *       SocketListener.onDatagram} on a virtual thread.</li>
 *   <li><em>Write</em> -- drains the connection's
 *       {@link SocketContext#pendingWrites} queue via a zero-copy
 *       gathering write (or TLS-wrapped coalesced write for SSL
 *       connections).</li>
 * </ol>
 *
 * <p> Connections may optionally use TLS via {@code SSLEngine}.  When TLS
 * is active, all channel reads and writes pass through
 * {@code SSLEngine.unwrap()} and {@code SSLEngine.wrap()} respectively.
 * The TLS handshake is driven entirely by the selector thread.
 *
 * @see SocketContext
 * @since 1.0
 */
public final class CentralSelector implements Runnable {
    public static final CentralSelector INSTANCE;

    static {
        try {
            INSTANCE = new CentralSelector();
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static final int MAX_MESSAGE_LENGTH = 1048576;
    private static final long SAFETY_TIMEOUT_MS = 5000;
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);

    private final System.Logger logger;
    private final Selector selector;
    private final AtomicBoolean wakeupPending;
    private volatile Thread selectorThread;

    private CentralSelector() throws IOException {
        this.logger = System.getLogger(CentralSelector.class.getName());
        this.selector = Selector.open();
        this.wakeupPending = new AtomicBoolean();
    }

    private void wakeup() {
        if (wakeupPending.compareAndSet(false, true)) {
            selector.wakeup();
        }
    }

    @SuppressWarnings("MagicConstant")
    public synchronized void register(SocketChannel channel, int ops, SocketContext context) throws IOException {
        selector.wakeup();
        channel.register(selector, ops, context);
        if (selectorThread == null || !selectorThread.isAlive()) {
            selectorThread = Thread.startVirtualThread(this);
        }
    }

    public void unregister(SocketChannel channel) {
        var key = channel.keyFor(selector);
        if (key == null) {
            return;
        }

        // Always cancel key and close channel to prevent event loop leaks
        key.cancel();
        try {
            channel.close();
        } catch (IOException _) {

        }

        var ctx = (SocketContext) key.attachment();

        // Always notify waiting threads so they don't block until timeout
        var pendingRead = ctx.pendingBinaryRead;
        if (pendingRead != null) {
            pendingRead.length = -1;
            ctx.pendingBinaryRead = null;
            synchronized (pendingRead.lock) {
                pendingRead.lock.notifyAll();
            }
        }

        synchronized (ctx.connectionLock) {
            ctx.connectionLock.notifyAll();
        }

        synchronized (ctx.sslHandshakeLock) {
            ctx.sslHandshakeLock.notifyAll();
        }

        // Use connected CAS to guard listener notification and executor shutdown
        if (!ctx.connected.compareAndSet(true, false)) {
            wakeup();
            return;
        }

        if (ctx.sslEngine != null) {
            ctx.sslEngine.closeOutbound();
        }

        ctx.stopListenerExecutor();

        try {
            ctx.listener.onClose();
        } catch (Throwable error) {
            logger.log(Level.ERROR, error);
        }

        wakeup();
    }

    public boolean isConnected(SocketChannel channel) {
        if (channel == null) {
            return false;
        }

        var key = channel.keyFor(selector);
        if (key == null) {
            return false;
        }

        return ((SocketContext) key.attachment()).connected.get();
    }

    public boolean preSeedDatagram(SocketChannel channel, ByteBuffer leftover) {
        if (!leftover.hasRemaining()) {
            return true;
        }

        var key = channel.keyFor(selector);
        if (key == null) {
            return false;
        }

        var ctx = (SocketContext) key.attachment();
        return feedDatagram(ctx, leftover);
    }

    public boolean drainSslAppBuffer(SocketChannel channel) {
        var key = channel.keyFor(selector);
        if (key == null) {
            return false;
        }

        var ctx = (SocketContext) key.attachment();
        if (ctx.sslEngine == null || ctx.appInBuffer == null) {
            return true;
        }

        ctx.appInBuffer.flip();
        if (!ctx.appInBuffer.hasRemaining()) {
            ctx.appInBuffer.compact();
            return true;
        }

        var result = feedDatagram(ctx, ctx.appInBuffer);
        ctx.appInBuffer.compact();
        return result;
    }

    private boolean feedDatagram(SocketContext ctx, ByteBuffer source) {
        while (source.hasRemaining()) {
            var noDatagram = ctx.datagramBuffer == null;
            var target = noDatagram ? ctx.datagramLengthBuffer : ctx.datagramBuffer;
            var count = Math.min(source.remaining(), target.remaining());
            var savedLimit = source.limit();
            source.limit(source.position() + count);
            target.put(source);
            source.limit(savedLimit);
            if (!target.hasRemaining() && !advanceDatagram(ctx, noDatagram)) {
                return false;
            }
        }
        return true;
    }

    public boolean addRead(SocketChannel channel, SocketPendingRead read) {
        var key = channel.keyFor(selector);
        if (key == null || !key.isValid()) {
            return false;
        }
        var ctx = (SocketContext) key.attachment();
        if (ctx.pendingBinaryRead != null) {
            return false;
        }
        ctx.pendingBinaryRead = read;
        try {
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        } catch (CancelledKeyException _) {
            ctx.pendingBinaryRead = null;
            return false;
        }
        wakeup();
        return true;
    }

    public boolean markReady(SocketChannel channel) {
        var key = channel.keyFor(selector);
        if (key == null || !key.isValid()) {
            return false;
        }
        var ctx = (SocketContext) key.attachment();
        ctx.startListenerExecutor();
        ctx.tunnelled = true;
        try {
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        } catch (CancelledKeyException _) {
            return false;
        }
        wakeup();
        return true;
    }

    public boolean addWrite(SocketChannel channel, ByteBuffer buffer) {
        var key = channel.keyFor(selector);
        if (key == null || !key.isValid()) {
            return false;
        }
        var ctx = (SocketContext) key.attachment();
        ctx.pendingWrites.offer(buffer);
        try {
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        } catch (CancelledKeyException _) {
            return false;
        }
        wakeup();
        return true;
    }

    public void startTlsHandshake(SocketChannel channel, long timeout) throws IOException {
        var key = channel.keyFor(selector);
        if (key == null || !key.isValid()) {
            throw new IOException("Channel not registered");
        }

        var ctx = (SocketContext) key.attachment();
        if (ctx.sslEngine == null) {
            throw new IOException("SSL not initialized on context");
        }

        ctx.sslEngine.beginHandshake();

        try {
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        } catch (CancelledKeyException e) {
            throw new IOException("Key cancelled during TLS init");
        }
        wakeup();

        synchronized (ctx.sslHandshakeLock) {
            var deadline = System.currentTimeMillis() + timeout;
            while (!ctx.sslHandshakeComplete && ctx.connected.get()) {
                var remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    throw new IOException("TLS handshake timed out");
                }
                try {
                    ctx.sslHandshakeLock.wait(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("TLS handshake interrupted", e);
                }
            }
        }

        if (!ctx.sslHandshakeComplete) {
            throw new IOException("TLS handshake failed: connection lost");
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

    private void handleKey(SelectionKey key) {
        var attachment = key.attachment();
        if (!(attachment instanceof SocketContext ctx)) {
            return;
        }

        var channel = (SocketChannel) key.channel();
        try {
            if (key.isConnectable()) {
                if (channel.finishConnect()) {
                    key.interestOps(SelectionKey.OP_READ);
                    synchronized (ctx.connectionLock) {
                        ctx.connectionLock.notifyAll();
                    }
                }
            }

            if (ctx.sslEngine != null && ctx.sslHandshaking) {
                if (ctx.sslTasksPending) {
                    return;
                }
                if (!driveHandshake(channel, ctx, key)) {
                    unregister(channel);
                }
                return;
            }

            if (key.isReadable()) {
                if (!processRead(channel, ctx, key)) {
                    unregister(channel);
                    return;
                }
            }
            if (key.isWritable()) {
                if (processWrite(channel, ctx)) {
                    if (!ctx.pendingWrites.isEmpty()) {
                        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                    } else {
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                    }
                }
            }
        } catch (Exception _) {
            unregister(channel);
        }
    }

    private boolean driveHandshake(SocketChannel channel, SocketContext ctx, SelectionKey key) throws IOException {
        var engine = ctx.sslEngine;

        // Flush residual encrypted data from a previous partial write
        if (ctx.netOutBuffer.position() > 0) {
            ctx.netOutBuffer.flip();
            while (ctx.netOutBuffer.hasRemaining()) {
                if (channel.write(ctx.netOutBuffer) == 0) {
                    ctx.netOutBuffer.compact();
                    key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                    return true;
                }
            }
            ctx.netOutBuffer.compact();
        }

        while (ctx.connected.get()) {
            switch (engine.getHandshakeStatus()) {
                case NEED_WRAP -> {
                    ctx.netOutBuffer.clear();
                    SSLEngineResult result;
                    try {
                        result = engine.wrap(EMPTY_BUFFER, ctx.netOutBuffer);
                    } catch (SSLException e) {
                        throw new IOException("TLS handshake wrap failed", e);
                    }
                    ctx.netOutBuffer.flip();

                    if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
                        return false;
                    }

                    while (ctx.netOutBuffer.hasRemaining()) {
                        if (channel.write(ctx.netOutBuffer) == 0) {
                            ctx.netOutBuffer.compact();
                            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                            return true;
                        }
                    }
                    ctx.netOutBuffer.compact();
                }

                case NEED_UNWRAP -> {
                    if (channel.read(ctx.netInBuffer) == -1) {
                        return false;
                    }

                    ctx.netInBuffer.flip();
                    SSLEngineResult result;
                    try {
                        result = engine.unwrap(ctx.netInBuffer, ctx.appInBuffer);
                    } catch (SSLException e) {
                        ctx.netInBuffer.compact();
                        throw new IOException("TLS handshake unwrap failed", e);
                    }
                    ctx.netInBuffer.compact();

                    switch (result.getStatus()) {
                        case BUFFER_UNDERFLOW -> {
                            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                            return true;
                        }
                        case CLOSED -> {
                            return false;
                        }
                        default -> {
                            // BUFFER_OVERFLOW is unreachable during the handshake:
                            // OpenJDK's SSLEngineImpl.readRecord() skips the overflow
                            // check when !conContext.isNegotiated, and handshake records
                            // produce 0 application bytes.  By the time isNegotiated
                            // becomes true, getHandshakeStatus() returns FINISHED and
                            // driveHandshake() exits before any data-path unwrap.
                            // This holds across TLS 1.0-1.3 (including 0.5-RTT).
                        }
                    }
                }

                case NEED_TASK -> {
                    ctx.sslTasksPending = true;
                    key.interestOps(0);
                    Thread.startVirtualThread(() -> {
                        Runnable task;
                        while ((task = engine.getDelegatedTask()) != null) {
                            task.run();
                        }
                        ctx.sslTasksPending = false;
                        try {
                            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        } catch (CancelledKeyException _) {
                            return;
                        }
                        wakeup();
                    });
                    return true;
                }

                case FINISHED, NOT_HANDSHAKING -> {
                    ctx.sslHandshaking = false;
                    ctx.sslHandshakeComplete = true;
                    ctx.appInBuffer.clear();
                    resizeSslBuffers(ctx);
                    synchronized (ctx.sslHandshakeLock) {
                        ctx.sslHandshakeLock.notifyAll();
                    }
                    return true;
                }
            }
        }

        throw new IOException("Unexpected end of stream");
    }

    private static void resizeSslBuffers(SocketContext ctx) {
        var session = ctx.sslEngine.getSession();
        var packetSize = session.getPacketBufferSize();
        var appSize = session.getApplicationBufferSize();
        if (ctx.netInBuffer.capacity() < packetSize) {
            ctx.netInBuffer = ByteBuffer.allocateDirect(packetSize);
        }
        if (ctx.netOutBuffer.capacity() < packetSize) {
            ctx.netOutBuffer = ByteBuffer.allocateDirect(packetSize);
        }
        if (ctx.appInBuffer.capacity() < appSize) {
            ctx.appInBuffer = ByteBuffer.allocate(appSize);
        }
    }

    private boolean processRead(SocketChannel channel, SocketContext ctx, SelectionKey key) throws IOException {
        if (!ctx.tunnelled) {
            return processPreTunnelRead(channel, ctx, key);
        } else if (ctx.sslEngine != null) {
            return processDatagramSsl(channel, ctx);
        } else {
            return processDatagramDirect(channel, ctx);
        }
    }

    private boolean processPreTunnelRead(SocketChannel channel, SocketContext ctx, SelectionKey key) throws IOException {
        var pendingRead = ctx.pendingBinaryRead;
        if (pendingRead == null) {
            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
            return true;
        }

        var bytesRead = sslRead(channel, ctx, pendingRead.buffer);
        if (bytesRead == -1) {
            pendingRead.length = -1;
            synchronized (pendingRead.lock) {
                pendingRead.lock.notifyAll();
            }
            return false;
        }

        if (bytesRead == 0) {
            return true;
        }

        if (pendingRead.length == -1) {
            pendingRead.length = 0;
        }
        pendingRead.length += bytesRead;
        if (!pendingRead.fullRead || !pendingRead.buffer.hasRemaining()) {
            if (pendingRead.fullRead) {
                pendingRead.buffer.flip();
            }
            ctx.pendingBinaryRead = null;
            synchronized (pendingRead.lock) {
                pendingRead.lock.notifyAll();
            }
        }

        return true;
    }

    private int sslRead(SocketChannel channel, SocketContext ctx, ByteBuffer target) throws IOException {
        if (ctx.sslEngine == null) {
            return channel.read(target);
        }

        // Slow path: drain leftover decrypted data from previous unwrap
        ctx.appInBuffer.flip();
        if (ctx.appInBuffer.hasRemaining()) {
            var transferred = transferBytes(ctx.appInBuffer, target);
            ctx.appInBuffer.compact();
            if (transferred > 0) {
                return transferred;
            }
        } else {
            ctx.appInBuffer.compact();
        }

        // Read encrypted data from channel into direct buffer
        var bytesRead = channel.read(ctx.netInBuffer);
        if (bytesRead == -1) {
            return -1;
        }
        if (bytesRead == 0 && ctx.netInBuffer.position() == 0) {
            return 0;
        }

        // Fast path: unwrap directly into target (avoids appInBuffer copy)
        ctx.netInBuffer.flip();
        SSLEngineResult result;
        try {
            result = ctx.sslEngine.unwrap(ctx.netInBuffer, target);
        } catch (SSLException e) {
            ctx.netInBuffer.compact();
            throw new IOException("SSL unwrap failed", e);
        }

        switch (result.getStatus()) {
            case OK -> {
                ctx.netInBuffer.compact();
                return result.bytesProduced();
            }
            case BUFFER_UNDERFLOW -> {
                ctx.netInBuffer.compact();
                return 0;
            }
            case BUFFER_OVERFLOW -> {
                // Target too small for a full TLS record; unwrap into appInBuffer
                try {
                    result = ctx.sslEngine.unwrap(ctx.netInBuffer, ctx.appInBuffer);
                } catch (SSLException e) {
                    ctx.netInBuffer.compact();
                    throw new IOException("SSL unwrap failed", e);
                }
                ctx.netInBuffer.compact();

                if (result.getStatus() != SSLEngineResult.Status.OK) {
                    return 0;
                }

                ctx.appInBuffer.flip();
                var transferred = transferBytes(ctx.appInBuffer, target);
                ctx.appInBuffer.compact();
                return transferred;
            }
            case CLOSED -> {
                ctx.netInBuffer.compact();
                return -1;
            }
        }

        ctx.netInBuffer.compact();
        return 0;
    }

    private boolean processDatagramDirect(SocketChannel channel, SocketContext ctx) throws IOException {
        while (ctx.connected.get()) {
            var noDatagram = ctx.datagramBuffer == null;
            var target = noDatagram ? ctx.datagramLengthBuffer : ctx.datagramBuffer;
            var bytesRead = channel.read(target);
            if (bytesRead == -1) {
                return false;
            }
            if (target.hasRemaining()) {
                return true;
            }
            if (!advanceDatagram(ctx, noDatagram)) {
                return false;
            }
        }
        return false;
    }

    private boolean processDatagramSsl(SocketChannel channel, SocketContext ctx) throws IOException {
        while (ctx.connected.get()) {
            var bytesRead = channel.read(ctx.netInBuffer);
            if (bytesRead == -1) {
                return false;
            }

            // Unwrap all available TLS records
            ctx.netInBuffer.flip();
            while (ctx.netInBuffer.hasRemaining()) {
                SSLEngineResult result;
                try {
                    result = ctx.sslEngine.unwrap(ctx.netInBuffer, ctx.appInBuffer);
                } catch (SSLException e) {
                    ctx.netInBuffer.compact();
                    throw new IOException("SSL unwrap failed", e);
                }

                switch (result.getStatus()) {
                    case BUFFER_UNDERFLOW -> {} // incomplete TLS record
                    case CLOSED -> {
                        ctx.netInBuffer.compact();
                        return false;
                    }
                    case OK -> {
                        continue;
                    }
                    case BUFFER_OVERFLOW -> {
                        ctx.netInBuffer.compact();
                        throw new IOException("SSL appInBuffer overflow: buffer sized incorrectly after handshake");
                    }
                }
                break;
            }
            ctx.netInBuffer.compact();

            // Drain decrypted data into datagram framing
            ctx.appInBuffer.flip();
            while (ctx.appInBuffer.hasRemaining() && ctx.connected.get()) {
                var noDatagram = ctx.datagramBuffer == null;
                var target = noDatagram ? ctx.datagramLengthBuffer : ctx.datagramBuffer;
                var count = Math.min(ctx.appInBuffer.remaining(), target.remaining());
                var savedLimit = ctx.appInBuffer.limit();
                ctx.appInBuffer.limit(ctx.appInBuffer.position() + count);
                target.put(ctx.appInBuffer);
                ctx.appInBuffer.limit(savedLimit);

                if (!target.hasRemaining() && !advanceDatagram(ctx, noDatagram)) {
                    ctx.appInBuffer.compact();
                    return false;
                }
            }
            ctx.appInBuffer.compact();

            if (bytesRead == 0) {
                return true;
            }
        }
        return false;
    }

    private boolean advanceDatagram(SocketContext ctx, boolean noDatagram) {
        if (noDatagram) {
            ctx.datagramLengthBuffer.flip();
            var length = ((ctx.datagramLengthBuffer.get() & 0xFF) << 16)
                         | ((ctx.datagramLengthBuffer.get() & 0xFF) << 8)
                         | (ctx.datagramLengthBuffer.get() & 0xFF);
            if (length > MAX_MESSAGE_LENGTH) {
                return false;
            }
            ctx.datagramBuffer = ByteBuffer.allocate(length);
            return true;
        } else {
            ctx.datagramBuffer.flip();
            ctx.datagramLengthBuffer.clear();
            var buffer = ctx.datagramBuffer;
            ctx.datagramBuffer = null;
            // Ordering is important at this stage
            ctx.listenerVirtualExecutor.execute(() -> ctx.listener.onDatagram(buffer));
            return true;
        }
    }

    private boolean processWrite(SocketChannel channel, SocketContext ctx) throws IOException {
        if (ctx.sslEngine != null) {
            return processWriteSsl(channel, ctx);
        } else {
            return processWriteDirect(channel, ctx);
        }
    }

    private boolean processWriteDirect(SocketChannel channel, SocketContext ctx) throws IOException {
        while (ctx.connected.get()) {
            var claim = ctx.pendingWrites.claim();
            if (claim.isEmpty()) {
                return true;
            }

            channel.write(claim.array(), claim.offset(), claim.count());

            var consumed = countConsumed(claim);
            ctx.pendingWrites.release(consumed);

            if (consumed < claim.count()) {
                return false;
            }
        }
        return false;
    }

    private boolean processWriteSsl(SocketChannel channel, SocketContext ctx) throws IOException {
        // Flush residual encrypted data from a previous partial write
        if (ctx.netOutBuffer.position() > 0) {
            ctx.netOutBuffer.flip();
            channel.write(ctx.netOutBuffer);
            if (ctx.netOutBuffer.hasRemaining()) {
                ctx.netOutBuffer.compact();
                return false;
            }
            ctx.netOutBuffer.compact();
        }

        while (ctx.connected.get()) {
            var claim = ctx.pendingWrites.claim();
            if (claim.isEmpty()) {
                return true;
            }

            // Coalesced wrap: pack multiple app buffers into one TLS record
            ctx.netOutBuffer.clear();
            SSLEngineResult result;
            try {
                result = ctx.sslEngine.wrap(claim.array(), claim.offset(), claim.count(), ctx.netOutBuffer);
            } catch (SSLException e) {
                throw new IOException("SSL wrap failed", e);
            }

            if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                throw new IOException("SSL netOutBuffer overflow: buffer sized incorrectly after handshake");
            }

            ctx.netOutBuffer.flip();
            while (ctx.netOutBuffer.hasRemaining()) {
                if (channel.write(ctx.netOutBuffer) == 0) {
                    ctx.netOutBuffer.compact();
                    ctx.pendingWrites.release(countConsumed(claim));
                    return false;
                }
            }
            ctx.netOutBuffer.compact();

            var consumed = countConsumed(claim);
            ctx.pendingWrites.release(consumed);

            if (consumed < claim.count()) {
                continue;
            }
        }
        return false;
    }

    private static int countConsumed(SocketPendingWrites.Claim claim) {
        var consumed = 0;
        for (var i = claim.offset(); i < claim.offset() + claim.count(); i++) {
            if (claim.array()[i].hasRemaining()) {
                break;
            }
            consumed++;
        }
        return consumed;
    }

    private static int transferBytes(ByteBuffer src, ByteBuffer dst) {
        var count = Math.min(src.remaining(), dst.remaining());
        if (count <= 0) {
            return 0;
        }
        var savedLimit = src.limit();
        src.limit(src.position() + count);
        dst.put(src);
        src.limit(savedLimit);
        return count;
    }
}
