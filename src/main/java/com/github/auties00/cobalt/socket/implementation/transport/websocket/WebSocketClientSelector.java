package com.github.auties00.cobalt.socket.implementation.transport.websocket;

import com.github.auties00.cobalt.socket.implementation.context.AbstractSocketClientContext;
import com.github.auties00.cobalt.socket.implementation.context.SocketPendingWrites;
import com.github.auties00.cobalt.socket.implementation.context.ssl.AbstractSSLSocketClientSelector;
import com.github.auties00.cobalt.socket.implementation.transport.websocket.frame.WebSocketDecodedFrame;
import com.github.auties00.cobalt.socket.implementation.transport.websocket.frame.WebSocketFrameConstants;
import com.github.auties00.cobalt.socket.implementation.transport.websocket.frame.WebSocketFrameEncoder;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public final class WebSocketClientSelector extends AbstractSSLSocketClientSelector {
    private static final int WEBSOCKET_READ_BUFFER_SIZE = 16384;

    public static final WebSocketClientSelector INSTANCE;

    static {
        try {
            INSTANCE = new WebSocketClientSelector();
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private WebSocketClientSelector() throws IOException {
        super();
    }

    @Override
    protected boolean processRead(SocketChannel channel, AbstractSocketClientContext ctx, SelectionKey key) throws IOException {
        var webSocketCtx = (WebSocketClientContext) ctx;
        return webSocketCtx.sslEngine != null
                ? processWebSocketSsl(channel, webSocketCtx, key)
                : processWebSocketDirect(channel, webSocketCtx, key);
    }

    private boolean processWebSocketDirect(SocketChannel channel, WebSocketClientContext ctx, SelectionKey key) throws IOException {
        if (ctx.netInBuffer == null) {
            ctx.netInBuffer = ByteBuffer.allocateDirect(WEBSOCKET_READ_BUFFER_SIZE);
        }

        while (ctx.connected.get()) {
            var bytesRead = channel.read(ctx.netInBuffer);
            if (bytesRead == -1) {
                return false;
            }
            if (bytesRead == 0 && ctx.netInBuffer.position() == 0) {
                return true;
            }

            ctx.netInBuffer.flip();
            var result = feedWebSocket(ctx, ctx.netInBuffer, key);
            ctx.netInBuffer.compact();
            if (!result) {
                return false;
            }

            if (bytesRead == 0) {
                return true;
            }
        }

        return false;
    }

    private boolean processWebSocketSsl(SocketChannel channel, WebSocketClientContext ctx, SelectionKey key) throws IOException {
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
                    case BUFFER_UNDERFLOW -> { } // incomplete TLS record
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

            // Drain decrypted data into websocket frame parser
            ctx.appInBuffer.flip();
            if (ctx.appInBuffer.hasRemaining() && !feedWebSocket(ctx, ctx.appInBuffer, key)) {
                ctx.appInBuffer.compact();
                return false;
            }
            ctx.appInBuffer.compact();

            if (bytesRead == 0) {
                return true;
            }
        }

        return false;
    }

    private boolean feedWebSocket(WebSocketClientContext ctx, ByteBuffer source, SelectionKey key) {
        while (source.hasRemaining()) {
            var decodedFrame = ctx.webSocketFrameDecoder.decode(source);
            switch (decodedFrame) {
                case WebSocketDecodedFrame.None _ -> {
                    return true;
                }
                case WebSocketDecodedFrame.Invalid _ -> {
                    return false;
                }
                case WebSocketDecodedFrame.Data data -> {
                    if (!feedDatagram(ctx, data.payload())) {
                        return false;
                    }
                }
                case WebSocketDecodedFrame.Control control -> {
                    if (!handleWebSocketControl(ctx, key, control.opcode(), control.payload(), control.length())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean feedDatagram(WebSocketClientContext ctx, ByteBuffer source) {
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

    private boolean handleWebSocketControl(AbstractSocketClientContext ctx, SelectionKey key, byte opcode, byte[] payload, int length) {
        return switch (opcode) {
            case WebSocketFrameConstants.OPCODE_PING -> key == null
                                                        || enqueueWebSocketControlFrame(ctx, key, WebSocketFrameConstants.OPCODE_PONG, payload, length);
            case WebSocketFrameConstants.OPCODE_CLOSE -> {
                if (key != null) {
                    enqueueWebSocketControlFrame(ctx, key, WebSocketFrameConstants.OPCODE_CLOSE, payload, length);
                }
                yield false;
            }
            case WebSocketFrameConstants.OPCODE_PONG -> true;
            default -> false;
        };
    }

    private boolean enqueueWebSocketControlFrame(AbstractSocketClientContext ctx, SelectionKey key, byte opcode, byte[] payload, int length) {
        if (!key.isValid()) {
            return false;
        }

        var frame = WebSocketFrameEncoder.encodeControlFrame(opcode, payload, length);
        if (frame.header().hasRemaining()) {
            ctx.pendingWrites.offer(frame.header());
        }
        if (frame.payload().hasRemaining()) {
            ctx.pendingWrites.offer(frame.payload());
        }

        try {
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
        } catch (CancelledKeyException _) {
            return false;
        }
        wakeup();
        return true;
    }

    @Override
    protected boolean processWrite(SocketChannel channel, AbstractSocketClientContext ctx) throws IOException {
        var webSocketCtx = (WebSocketClientContext) ctx;
        if (webSocketCtx.sslEngine != null) {
            return processWriteSsl(channel, webSocketCtx);
        } else {
            return processWriteDirect(channel, webSocketCtx);
        }
    }

    private boolean processWriteDirect(SocketChannel channel, WebSocketClientContext ctx) throws IOException {
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

    private boolean processWriteSsl(SocketChannel channel, WebSocketClientContext ctx) throws IOException {
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
}
