package com.github.auties00.cobalt.socket.layer.security.impl;

import com.github.auties00.cobalt.socket.layer.security.SocketClientSecurityLayerContext;
import com.github.auties00.cobalt.socket.threading.SocketClientInboundResult;
import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;
import com.github.auties00.cobalt.util.DataUtils;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * Layer context that provides TLS encryption and decryption.
 *
 * <p>Owns an {@link SSLEngine} and its associated net and app
 * buffers. The same implementation serves both tunnel-level TLS
 * (client-to-proxy) and transport-level TLS (client-to-target); the
 * composition of the stack picks the role.
 *
 * @implNote The inbound read path takes a zero-copy fast path:
 *     encrypted bytes are read into {@link #netInBuffer} and then
 *     unwrapped directly into the next layer's
 *     {@link SocketClientLayerContext#inboundTarget()}. A slow path
 *     through {@link #appInBuffer} handles the rare
 *     {@code BUFFER_OVERFLOW} case after the buffer sizes have been
 *     fixed by {@link #resizeSslBuffers()} at end of handshake.
 * @implNote The outbound path coalesces multiple application buffers
 *     into a single TLS record via
 *     {@link SSLEngine#wrap(ByteBuffer[], int, int, ByteBuffer)} so
 *     batched writes do not produce one record per buffer.
 */
final class TlsLayerContext implements SocketClientSecurityLayerContext {
    /**
     * Next layer in the inbound chain; receives the decrypted bytes
     * that this layer's unwrap produces.
     */
    private volatile SocketClientLayerContext nextLayer;

    /**
     * Previous layer in the outbound chain; receives the encrypted
     * bytes that this layer's wrap produces, or {@code null} when
     * this is the bottommost crypto layer and writes go straight to
     * the channel.
     */
    private volatile SocketClientLayerContext prevLayer;

    /**
     * Underlying SSL engine, configured by
     * {@link #initSsl(SSLEngine)}.
     */
    private SSLEngine sslEngine;

    /**
     * Direct buffer holding encrypted bytes coming from the channel
     * before they are unwrapped.
     */
    private ByteBuffer netInBuffer;

    /**
     * Direct buffer holding encrypted bytes produced by wrap and
     * waiting to be written to the channel.
     */
    private ByteBuffer netOutBuffer;

    /**
     * Heap buffer used as the slow-path destination when the next
     * layer's inbound target cannot accept more bytes.
     */
    private ByteBuffer appInBuffer;

    /**
     * Whether the TLS handshake is still running.
     */
    private volatile boolean sslHandshaking;

    /**
     * Whether the SSL engine has delegated tasks waiting on a virtual
     * thread.
     */
    private volatile boolean sslTasksPending;

    /**
     * Whether the TLS handshake has completed successfully.
     */
    private volatile boolean sslHandshakeComplete;

    /**
     * Monitor used to park the calling thread until
     * {@link #sslHandshakeComplete} becomes {@code true}.
     */
    private final Object sslHandshakeLock;

    /**
     * Channel reference captured at handshake completion; used by
     * {@link #sendCloseNotify()} to write the TLS close-notify alert
     * during teardown.
     */
    private volatile SocketChannel channel;

    /**
     * Creates a TLS layer context.
     */
    public TlsLayerContext() {
        this.sslHandshakeLock = new Object();
    }

    @Override
    public void setNextLayer(SocketClientLayerContext next) {
        this.nextLayer = next;
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
     * Initializes the TLS engine and allocates the initial net and
     * app buffers based on the engine's session sizes.
     *
     * @param engine the configured SSL engine
     */
    public void initSsl(SSLEngine engine) {
        this.sslEngine = engine;
        var session = engine.getSession();
        this.netInBuffer = ByteBuffer.allocateDirect(session.getPacketBufferSize());
        this.netOutBuffer = ByteBuffer.allocateDirect(session.getPacketBufferSize());
        this.appInBuffer = ByteBuffer.allocate(session.getApplicationBufferSize());
        this.sslHandshaking = true;
        this.sslHandshakeComplete = false;
    }

    /**
     * Grows {@link #netInBuffer}, {@link #netOutBuffer} and
     * {@link #appInBuffer} to match the session sizes finalised at
     * end of handshake.
     */
    private void resizeSslBuffers() {
        var session = sslEngine.getSession();
        var packetSize = session.getPacketBufferSize();
        var appSize = session.getApplicationBufferSize();

        if (netInBuffer.capacity() < packetSize) {
            var old = netInBuffer;
            netInBuffer = ByteBuffer.allocateDirect(packetSize);
            old.flip();
            netInBuffer.put(old);
        }
        if (netOutBuffer.capacity() < packetSize) {
            netOutBuffer = ByteBuffer.allocateDirect(packetSize);
        }
        if (appInBuffer.capacity() < appSize) {
            appInBuffer = ByteBuffer.allocate(appSize);
        }
    }

    @Override
    public ByteBuffer inboundTarget() {
        return netInBuffer;
    }

    @Override
    public SocketClientInboundResult processInbound(int bytesRead) throws IOException {
        if (bytesRead == -1) {
            return new SocketClientInboundResult.Close();
        }
        if (sslHandshaking) {
            // Bytes are left accumulated in netInBuffer for
            // driveHandshake() to unwrap. processDataSsl is unsafe
            // here: the engine refuses to unwrap application data
            // mid-handshake and nextLayer may not even be registered
            // yet.
            return new SocketClientInboundResult.Buffering();
        }
        return processDataSsl();
    }

    @Override
    public SocketClientInboundResult driveHandshake(SocketChannel channel) throws IOException {
        // Flush leftover bytes from a previous handshake iteration.
        if (netOutBuffer.position() > 0) {
            netOutBuffer.flip();
            if (!writeHandshakeBytes(channel, netOutBuffer)) {
                netOutBuffer.compact();
                return new SocketClientInboundResult.Buffering();
            }
            netOutBuffer.compact();
        }

        while (true) {
            switch (sslEngine.getHandshakeStatus()) {
                case NEED_WRAP -> {
                    netOutBuffer.clear();
                    SSLEngineResult result;
                    try {
                        result = sslEngine.wrap(DataUtils.EMPTY_BYTE_BUFFER, netOutBuffer);
                    } catch (SSLException e) {
                        throw new IOException("TLS handshake wrap failed", e);
                    }
                    netOutBuffer.flip();

                    if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
                        return new SocketClientInboundResult.Close();
                    }

                    if (!writeHandshakeBytes(channel, netOutBuffer)) {
                        netOutBuffer.compact();
                        return new SocketClientInboundResult.Buffering();
                    }
                    netOutBuffer.compact();
                }

                case NEED_UNWRAP -> {
                    // Bytes flow into netInBuffer through the normal
                    // inbound path (selector read, then any outer
                    // layer's unwrap). If nothing has arrived yet, wait.
                    netInBuffer.flip();
                    if (!netInBuffer.hasRemaining()) {
                        netInBuffer.compact();
                        return new SocketClientInboundResult.Buffering();
                    }
                    SSLEngineResult result;
                    try {
                        result = sslEngine.unwrap(netInBuffer, appInBuffer);
                    } catch (SSLException e) {
                        netInBuffer.compact();
                        throw new IOException("TLS handshake unwrap failed", e);
                    }
                    netInBuffer.compact();

                    switch (result.getStatus()) {
                        case BUFFER_UNDERFLOW -> {
                            return new SocketClientInboundResult.Buffering();
                        }
                        case CLOSED -> {
                            return new SocketClientInboundResult.Close();
                        }
                        default -> {
                        }
                    }
                }

                case NEED_TASK -> {
                    sslTasksPending = true;
                    return new SocketClientInboundResult.Suspended();
                }

                case FINISHED, NOT_HANDSHAKING -> {
                    sslHandshaking = false;
                    sslHandshakeComplete = true;
                    this.channel = channel;
                    appInBuffer.clear();
                    resizeSslBuffers();
                    synchronized (sslHandshakeLock) {
                        sslHandshakeLock.notifyAll();
                    }
                    return new SocketClientInboundResult.Continue();
                }
            }
        }
    }

    /**
     * Drains the post-handshake data path: unwraps every byte of
     * {@link #netInBuffer} into the next layer's inbound target,
     * falling back to {@link #appInBuffer} when the target cannot
     * accept more data.
     *
     * @return the processing result for the selector
     * @throws IOException if {@link SSLEngine#unwrap} fails
     */
    private SocketClientInboundResult processDataSsl() throws IOException {
        if (nextLayer == null) {
            return new SocketClientInboundResult.Buffering();
        }

        appInBuffer.flip();
        if (appInBuffer.hasRemaining()) {
            var result = feedNextLayer(appInBuffer);
            appInBuffer.compact();
            if (!(result instanceof SocketClientInboundResult.Continue)) {
                return result;
            }
        } else {
            appInBuffer.compact();
        }

        netInBuffer.flip();
        while (netInBuffer.hasRemaining()) {
            var target = nextLayer.inboundTarget();
            SSLEngineResult result;
            try {
                result = sslEngine.unwrap(netInBuffer, target);
            } catch (SSLException e) {
                netInBuffer.compact();
                throw new IOException("SSL unwrap failed", e);
            }

            switch (result.getStatus()) {
                case OK -> {
                    if (result.bytesProduced() > 0) {
                        var layerResult = nextLayer.processInbound(result.bytesProduced());
                        if (!(layerResult instanceof SocketClientInboundResult.Continue)) {
                            netInBuffer.compact();
                            return layerResult;
                        }
                    }
                }
                case BUFFER_UNDERFLOW -> {
                    netInBuffer.compact();
                    return new SocketClientInboundResult.Buffering();
                }
                case BUFFER_OVERFLOW -> {
                    try {
                        result = sslEngine.unwrap(netInBuffer, appInBuffer);
                    } catch (SSLException e) {
                        netInBuffer.compact();
                        throw new IOException("SSL unwrap failed", e);
                    }

                    if (result.getStatus() == SSLEngineResult.Status.OK && result.bytesProduced() > 0) {
                        appInBuffer.flip();
                        var layerResult = feedNextLayer(appInBuffer);
                        appInBuffer.compact();
                        if (!(layerResult instanceof SocketClientInboundResult.Continue)) {
                            netInBuffer.compact();
                            return layerResult;
                        }
                    } else if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                        netInBuffer.compact();
                        throw new IOException("SSL appInBuffer overflow: buffer sized incorrectly after handshake");
                    }
                }
                case CLOSED -> {
                    netInBuffer.compact();
                    return new SocketClientInboundResult.Close();
                }
            }
        }
        netInBuffer.compact();
        return new SocketClientInboundResult.Continue();
    }

    /**
     * Feeds {@code source} into the next inbound layer, returning its
     * result.
     *
     * @param source the buffer containing decrypted bytes
     * @return the next layer's processing result
     * @throws IOException if the next layer fails to process the bytes
     */
    private SocketClientInboundResult feedNextLayer(ByteBuffer source) throws IOException {
        return nextLayer.feedFromSource(source);
    }

    /**
     * Writes the contents of {@code bytes} to the channel, routing
     * through {@link #prevLayer} so any outer TLS layer below this
     * one wraps the bytes again.
     *
     * @param channel the channel to write to
     * @param bytes   a read-mode view of {@link #netOutBuffer}
     * @return {@code true} if every byte was written, {@code false}
     *         if the write was partial and the caller should buffer
     *         and retry
     * @throws IOException if writing fails
     */
    private boolean writeHandshakeBytes(SocketChannel channel, ByteBuffer bytes) throws IOException {
        var prev = prevLayer;
        if (prev != null) {
            return prev.processOutbound(channel, new ByteBuffer[]{bytes}, 0, 1);
        }
        while (bytes.hasRemaining()) {
            if (channel.write(bytes) == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void runDelegatedTasks(Runnable onComplete) {
        Thread.startVirtualThread(() -> {
            Runnable task;
            while ((task = sslEngine.getDelegatedTask()) != null) {
                task.run();
            }
            sslTasksPending = false;
            onComplete.run();
        });
    }

    /**
     * Wraps outbound application data into TLS records and writes them
     * to the channel.
     *
     * @param channel the socket channel to write to
     * @param buffers the application data buffers
     * @param offset  the offset into the buffers array
     * @param count   the number of buffers to wrap
     * @return {@code true} if all data was written
     * @throws IOException if a TLS or I/O error occurs
     */
    public boolean wrapAndWrite(SocketChannel channel, ByteBuffer[] buffers, int offset, int count) throws IOException {
        if (netOutBuffer.position() > 0) {
            netOutBuffer.flip();
            channel.write(netOutBuffer);
            if (netOutBuffer.hasRemaining()) {
                netOutBuffer.compact();
                return false;
            }
            netOutBuffer.compact();
        }

        netOutBuffer.clear();
        SSLEngineResult result;
        try {
            result = sslEngine.wrap(buffers, offset, count, netOutBuffer);
        } catch (SSLException e) {
            throw new IOException("SSL wrap failed", e);
        }

        if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
            throw new IOException("SSL netOutBuffer overflow: buffer sized incorrectly after handshake");
        }

        netOutBuffer.flip();
        while (netOutBuffer.hasRemaining()) {
            if (channel.write(netOutBuffer) == 0) {
                netOutBuffer.compact();
                return false;
            }
        }
        netOutBuffer.compact();
        return true;
    }

    /**
     * Wraps outbound application data into a TLS record without writing
     * to a channel.
     *
     * @param buffers the application data buffers
     * @param offset  the offset into the buffers array
     * @param count   the number of buffers to wrap
     * @return the encrypted buffer in read mode, or {@code null} if
     *         nothing was produced
     * @throws IOException if a TLS error occurs
     */
    public ByteBuffer wrapToBuffer(ByteBuffer[] buffers, int offset, int count) throws IOException {
        netOutBuffer.clear();
        SSLEngineResult result;
        try {
            result = sslEngine.wrap(buffers, offset, count, netOutBuffer);
        } catch (SSLException e) {
            throw new IOException("SSL wrap failed", e);
        }

        if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
            throw new IOException("SSL netOutBuffer overflow: buffer sized incorrectly after handshake");
        }

        netOutBuffer.flip();
        return netOutBuffer.hasRemaining() ? netOutBuffer.asReadOnlyBuffer() : null;
    }

    @Override
    public boolean hasPendingOutput() {
        return netOutBuffer != null && netOutBuffer.position() > 0;
    }

    @Override
    public boolean isHandshaking() {
        return sslHandshaking;
    }

    @Override
    public boolean isTasksPending() {
        return sslTasksPending;
    }

    @Override
    public Object handshakeLock() {
        return sslHandshakeLock;
    }

    @Override
    public boolean isHandshakeComplete() {
        return sslHandshakeComplete;
    }

    @Override
    public void beginHandshake() throws IOException {
        sslEngine.beginHandshake();
    }

    @Override
    public boolean drainToNextLayer() throws IOException {
        if (sslEngine == null || appInBuffer == null) {
            return true;
        }

        appInBuffer.flip();
        if (!appInBuffer.hasRemaining()) {
            appInBuffer.compact();
            return true;
        }

        var result = feedNextLayer(appInBuffer);
        appInBuffer.compact();
        return !(result instanceof SocketClientInboundResult.Close);
    }

    @Override
    public boolean processOutbound(SocketChannel channel, ByteBuffer[] buffers, int offset, int count) throws IOException {
        var prev = prevLayer;
        if (prev != null) {
            var wrapped = wrapToBuffer(buffers, offset, count);
            if (wrapped == null) {
                return false;
            }
            return prev.processOutbound(channel, new ByteBuffer[]{wrapped}, 0, 1);
        }
        return wrapAndWrite(channel, buffers, offset, count);
    }

    @Override
    public void onDisconnect() {
        if (sslEngine != null) {
            sslEngine.closeOutbound();
            sendCloseNotify();
        }
        synchronized (sslHandshakeLock) {
            sslHandshakeLock.notifyAll();
        }
        if (nextLayer != null) {
            nextLayer.onDisconnect();
        }
    }

    /**
     * Best-effort send of the TLS close-notify alert during teardown,
     * silently ignoring channel-close races.
     */
    private void sendCloseNotify() {
        var ch = this.channel;
        if (ch == null || !ch.isOpen()) {
            return;
        }
        try {
            netOutBuffer.clear();
            sslEngine.wrap(DataUtils.EMPTY_BYTE_BUFFER, netOutBuffer);
            netOutBuffer.flip();
            while (netOutBuffer.hasRemaining()) {
                if (ch.write(netOutBuffer) == 0) {
                    break;
                }
            }
        } catch (ClosedChannelException _) {
        } catch (IOException _) {
        }
    }
}
