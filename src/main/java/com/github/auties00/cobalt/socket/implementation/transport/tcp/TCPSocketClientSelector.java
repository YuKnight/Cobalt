package com.github.auties00.cobalt.socket.implementation.transport.tcp;

import com.github.auties00.cobalt.socket.implementation.context.AbstractSocketClientContext;
import com.github.auties00.cobalt.socket.implementation.context.AbstractSocketClientSelector;
import com.github.auties00.cobalt.socket.implementation.context.SocketPendingWrites;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public final class TCPSocketClientSelector extends AbstractSocketClientSelector {
    public static final TCPSocketClientSelector INSTANCE;

    static {
        try {
            INSTANCE = new TCPSocketClientSelector();
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private TCPSocketClientSelector() throws IOException {
        super();
    }

    @Override
    protected boolean processRead(SocketChannel channel, AbstractSocketClientContext ctx, SelectionKey key) throws IOException {
        var tcpCtx = (TCPSocketClientContext) ctx;
        while (tcpCtx.connected.get()) {
            var noDatagram = tcpCtx.datagramBuffer == null;
            var target = noDatagram ? tcpCtx.datagramLengthBuffer : tcpCtx.datagramBuffer;
            var bytesRead = channel.read(target);
            if (bytesRead == -1) {
                return false;
            }
            if (target.hasRemaining()) {
                return true;
            }
            if (!advanceDatagram(tcpCtx, noDatagram)) {
                return false;
            }
        }
        return false;
    }

    @Override
    protected int executePreTunnelRead(SocketChannel channel, AbstractSocketClientContext ctx, ByteBuffer target) throws IOException {
        return channel.read(target);
    }

    @Override
    protected boolean processWrite(SocketChannel channel, AbstractSocketClientContext ctx) throws IOException {
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
