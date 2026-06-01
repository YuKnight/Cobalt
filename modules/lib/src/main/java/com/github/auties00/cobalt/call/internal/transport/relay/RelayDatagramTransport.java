package com.github.auties00.cobalt.call.internal.transport.relay;

import com.github.auties00.cobalt.call.internal.transport.ice.DatagramTransport;
import com.github.auties00.cobalt.call.internal.transport.sctp.datachannel.DataChannel;
import com.github.auties00.cobalt.exception.WhatsAppCallException;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bridges the call layer's {@link DatagramTransport} contract onto a live
 * {@link RelayChannelDriver}'s pre-negotiated DataChannel.
 *
 * <p>WA's edgeray demultiplexes packets flowing on the DataChannel by inspecting the first byte
 * (RFC 7983 ranges) just like a WebRTC peer would: STUN bytes are processed by the relay itself
 * (Allocate, Binding, etc.); non-STUN bytes (DTLS / SCTP / SRTP / SRTCP) are forwarded to the
 * peer endpoint the relay has bound for this allocation. The JS side of WA Web treats this as
 * symmetric: any bytes the WASM stack writes via {@code channel.send(buffer)} just get pushed to
 * the relay, and any bytes the relay forwards back are handed straight to the WASM as
 * application data.
 *
 * <p>This adapter takes the same view. Outbound: {@link #send(byte[])} forwards the bytes verbatim
 * to {@link RelayChannelDriver#sendBinary(byte[])}. Inbound: the adapter wires itself as the
 * channel's binary message listener and pushes each inbound binary payload to the registered
 * {@link InboundListener}. The peer DTLS layer ({@link
 * com.github.auties00.cobalt.call.internal.transport.dtls.DtlsSrtpDriver}) layered on top sees a
 * straight peer-to-peer datagram pipe and does not have to know it is being tunneled through the
 * relay.
 */
public final class RelayDatagramTransport implements DatagramTransport {
    /**
     * Holds the underlying relay channel driver. Lifetime is owned by the call layer; the adapter
     * does NOT close the driver in {@link #close()}.
     */
    private final RelayChannelDriver driver;

    /**
     * Holds the relay's remote address, surfaced via {@link #remoteAddress()} for diagnostics.
     */
    private final InetSocketAddress remote;

    /**
     * Holds the currently registered inbound listener.
     */
    private final AtomicReference<InboundListener> listener = new AtomicReference<>();

    /**
     * Tracks whether {@link #close()} has been invoked.
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Constructs a relay-tunneled datagram transport on top of the given channel driver and binds
     * the channel's binary message listener.
     *
     * @param driver  the live relay channel driver, which must already have completed
     *                {@link RelayChannelDriver#connect()}
     * @param channel the pre-negotiated DataChannel the driver opened
     * @throws NullPointerException if either argument is {@code null}
     */
    public RelayDatagramTransport(RelayChannelDriver driver, DataChannel channel) {
        this.driver = Objects.requireNonNull(driver, "driver cannot be null");
        Objects.requireNonNull(channel, "channel cannot be null");
        this.remote = driver.remote();
        channel.setMessageListener(message -> {
            if (closed.get()) {
                return;
            }
            if (message instanceof DataChannel.Message.Binary binary) {
                var l = listener.get();
                if (l != null) {
                    try {
                        l.onDatagram(binary.data());
                    } catch (RuntimeException _) {
                    }
                }
            }
        });
    }

    @Override
    public InetSocketAddress localAddress() {
        // The relay-tunneled path has no meaningful local 5-tuple at this layer.
        return new InetSocketAddress(0);
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return remote;
    }

    @Override
    public void send(byte[] packet) {
        Objects.requireNonNull(packet, "packet cannot be null");
        if (closed.get()) {
            throw new WhatsAppCallException.Ice("RelayDatagramTransport already closed");
        }
        driver.sendBinary(packet);
    }

    @Override
    public void setInboundListener(InboundListener listener) {
        this.listener.set(listener);
    }

    @Override
    public void close() {
        closed.set(true);
    }
}
