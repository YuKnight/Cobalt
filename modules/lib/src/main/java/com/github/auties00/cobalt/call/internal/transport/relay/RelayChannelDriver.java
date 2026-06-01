package com.github.auties00.cobalt.call.internal.transport.relay;

import com.github.auties00.cobalt.call.internal.transport.dtls.DtlsCertificate;
import com.github.auties00.cobalt.call.internal.transport.dtls.DtlsSrtpDriver;
import com.github.auties00.cobalt.call.internal.transport.ice.UdpDatagramTransport;
import com.github.auties00.cobalt.call.internal.transport.sctp.SctpDtlsBridge;
import com.github.auties00.cobalt.call.internal.transport.sctp.datachannel.DataChannel;
import com.github.auties00.cobalt.call.internal.transport.sctp.datachannel.DataChannelOptions;
import com.github.auties00.cobalt.call.internal.transport.sctp.datachannel.DataChannelTransport;
import com.github.auties00.cobalt.call.internal.rtp.srtp.SrtpRole;
import com.github.auties00.cobalt.exception.WhatsAppCallException;
import org.bouncycastle.tls.DTLSTransport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HexFormat;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Brings up a WebRTC-style relay channel against an edgeray endpoint and exposes a binary
 * application byte stream over the pre-negotiated DataChannel (RFC 8832 {@code negotiated=true,
 * id=0, ordered=false, maxRetransmits=0}).
 *
 * <p>The relay does NOT speak raw STUN/TURN over UDP. Its UDP port expects a DTLS ClientHello and
 * silently drops anything else. WA Web models the connection as a full {@code RTCPeerConnection}
 * with a synthesised answer SDP pinning a hardcoded relay certificate, an out-of-band
 * pre-negotiated DataChannel (stream id 0), and lets the WASM voip stack write STUN Allocate /
 * Bind / NonSTUN payloads as application bytes through that DataChannel. This driver replicates
 * the same protocol end to end:
 *
 * <ol>
 *   <li>Open a UDP socket to the te2 endpoint via {@link UdpDatagramTransport}.</li>
 *   <li>Run the DTLS handshake as client through {@link DtlsSrtpDriver}, pinning the relay's
 *       hardcoded SHA-256 fingerprint ({@link #RELAY_FINGERPRINT_SHA256_HEX}) as the expected peer
 *       fingerprint.</li>
 *   <li>Stand up the SCTP association on top of the negotiated {@link DTLSTransport} via
 *       {@link DataChannelTransport} and {@link SctpDtlsBridge}; both peers use SCTP port
 *       {@value #SCTP_PORT}.</li>
 *   <li>Open the pre-negotiated DataChannel on stream id {@value #DATA_CHANNEL_STREAM_ID} with the
 *       relay's reliability profile (unordered, {@code maxRetransmits=0}).</li>
 * </ol>
 *
 * <p>After {@link #connect()} returns, callers send application bytes (STUN Allocate Request etc.)
 * via {@link #sendBinary(byte[])} and wait for inbound binary frames via
 * {@link #awaitBinary(long, TimeUnit)}.
 *
 * @implNote The {@link DtlsSrtpDriver} class is used as-is despite its SRTP-tinged name. The
 * {@code use_srtp} extension it advertises is ignored by the relay (which never sends SRTP and
 * does not require the extension to complete the handshake), and we discard the derived
 * {@code SrtpEndpoint}; we only need the negotiated application-data {@link DTLSTransport}. The
 * driver's RFC 7983 byte-0 demux is harmless here too: STUN-range packets in the {@code 0..3}
 * range would only arrive if the relay had elected to speak ICE on the same socket, which it
 * does not.
 */
public final class RelayChannelDriver implements AutoCloseable {
    /**
     * Hex SHA-256 fingerprint of the edgeray DTLS certificate, hardcoded by WA Web in
     * {@code WAWebVoipRelayConnectionUtils.createAnswerSdp}.
     */
    public static final String RELAY_FINGERPRINT_SHA256_HEX =
            "F9CA0C98A3CC71D642CE5AE253D21520D31BBAD857A4F0AFBE0BFBF36B0CA068";

    /**
     * WebRTC SCTP port; both peers use this value.
     */
    public static final int SCTP_PORT = 5000;

    /**
     * Pre-negotiated DataChannel stream id agreed with the relay out of band, mirroring WA Web's
     * {@code WAWebVoipRelayConnectionUtils.BASE_DATA_CHANNEL_OPTIONS} value of {@code id: 0}.
     */
    public static final int DATA_CHANNEL_STREAM_ID = 0;

    /**
     * Label assigned to the pre-negotiated DataChannel; matches WA Web's
     * {@code "pre-negotiated"} literal.
     */
    public static final String DATA_CHANNEL_LABEL = "pre-negotiated";

    /**
     * Default timeout for the DTLS handshake step, in seconds.
     */
    private static final int DTLS_TIMEOUT_SECONDS = 15;

    /**
     * Default timeout for the SCTP association handshake, in seconds.
     */
    private static final int SCTP_TIMEOUT_SECONDS = 15;

    /**
     * Holds the remote relay endpoint.
     */
    private final InetSocketAddress remote;

    /**
     * UDP transport to the relay.
     */
    private UdpDatagramTransport udp;

    /**
     * DTLS driver wrapping the UDP transport.
     */
    private DtlsSrtpDriver dtls;

    /**
     * SCTP association layered on top of DTLS application data.
     */
    private DataChannelTransport sctp;

    /**
     * Bridge that pumps DTLS app-data records into the SCTP feed.
     */
    private SctpDtlsBridge bridge;

    /**
     * Pre-negotiated binary DataChannel; populated once {@link #connect()} succeeds.
     */
    private DataChannel channel;

    /**
     * Queue of inbound binary application messages, drained by {@link #awaitBinary(long, TimeUnit)}.
     */
    private final LinkedBlockingQueue<byte[]> inbound = new LinkedBlockingQueue<>();

    /**
     * Closed-once latch.
     */
    private final AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Constructs a driver targeting the given relay endpoint.
     *
     * @param remote the relay's UDP address, derived from a {@code <te2>} content payload
     */
    public RelayChannelDriver(InetSocketAddress remote) {
        this.remote = Objects.requireNonNull(remote, "remote cannot be null");
    }

    /**
     * Returns the remote relay endpoint.
     *
     * @return the relay address
     */
    public InetSocketAddress remote() {
        return remote;
    }

    /**
     * Returns the pre-negotiated DataChannel opened against the relay, or {@code null} when
     * {@link #connect()} has not yet succeeded.
     *
     * @return the DataChannel, or {@code null}
     */
    public DataChannel channel() {
        return channel;
    }

    /**
     * Runs the full bring-up sequence (DTLS, SCTP, DataChannel) and returns when the channel is
     * usable.
     *
     * @throws WhatsAppCallException.Ice when any step of the handshake fails or times out
     */
    public void connect() {
        if (closed.get()) {
            throw new WhatsAppCallException.Ice("RelayChannelDriver already closed");
        }
        try {
            this.udp = new UdpDatagramTransport(remote);
        } catch (RuntimeException e) {
            throw new WhatsAppCallException.Ice(
                    "UDP open failed for relay " + remote, e);
        }

        var localCert = DtlsCertificate.generate();
        var expectedFingerprint = HexFormat.of().parseHex(RELAY_FINGERPRINT_SHA256_HEX);
        this.dtls = new DtlsSrtpDriver(udp, SrtpRole.CLIENT, localCert, expectedFingerprint);
        dtls.start();
        try {
            dtls.awaitHandshake(DTLS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (IOException e) {
            close();
            throw new WhatsAppCallException.Ice(
                    "DTLS handshake to relay " + remote + " failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            close();
            throw new WhatsAppCallException.Ice(
                    "Interrupted during DTLS handshake to relay " + remote, e);
        }
        var dtlsTransport = dtls.dtlsTransport();
        if (dtlsTransport == null) {
            close();
            throw new WhatsAppCallException.Ice(
                    "DTLS handshake reported success but transport is null");
        }

        this.sctp = new DataChannelTransport(true, packet -> {
            try {
                dtlsTransport.send(packet, 0, packet.length);
            } catch (IOException e) {
                throw new WhatsAppCallException.Ice("DTLS send failed", e);
            }
        });
        this.bridge = new SctpDtlsBridge(dtlsTransport, sctp);
        try {
            sctp.bind(SCTP_PORT);
            sctp.connect(SCTP_PORT, SCTP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (RuntimeException e) {
            close();
            throw new WhatsAppCallException.Ice(
                    "SCTP handshake to relay " + remote + " failed", e);
        }

        var channelOptions = new DataChannelOptions(
                false, OptionalInt.of(0), OptionalInt.empty(),
                "", true, OptionalInt.of(DATA_CHANNEL_STREAM_ID),
                DataChannelOptions.DEFAULT_PRIORITY);
        try {
            this.channel = sctp.open(DATA_CHANNEL_LABEL, channelOptions);
        } catch (RuntimeException e) {
            close();
            throw new WhatsAppCallException.Ice(
                    "Failed to open pre-negotiated DataChannel on stream "
                            + DATA_CHANNEL_STREAM_ID, e);
        }
        channel.setMessageListener(message -> {
            if (message instanceof DataChannel.Message.Binary binary) {
                inbound.add(binary.data());
            }
        });
    }

    /**
     * Sends one binary application payload through the DataChannel.
     *
     * @param payload the bytes to send (typically a STUN Allocate Request)
     * @throws WhatsAppCallException.Ice if the channel is not yet open
     */
    public void sendBinary(byte[] payload) {
        Objects.requireNonNull(payload, "payload cannot be null");
        if (channel == null) {
            throw new WhatsAppCallException.Ice("DataChannel not connected");
        }
        channel.send(payload);
    }

    /**
     * Blocks for up to the given duration waiting for the next inbound binary message.
     *
     * @param timeout the maximum time to wait
     * @param unit    the unit of {@code timeout}
     * @return the next binary payload, or {@code null} when the timeout elapses
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    public byte[] awaitBinary(long timeout, TimeUnit unit) throws InterruptedException {
        return inbound.poll(timeout, unit);
    }

    /**
     * Tears the entire stack down: closes the channel, the SCTP transport, the SCTP-DTLS bridge,
     * the DTLS driver, and the underlying UDP socket. Safe to call multiple times.
     */
    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        if (channel != null) {
            try { channel.close(); } catch (RuntimeException _) {}
        }
        if (sctp != null) {
            try { sctp.close(); } catch (RuntimeException _) {}
        }
        if (bridge != null) {
            try { bridge.close(); } catch (RuntimeException _) {}
        }
        if (dtls != null) {
            try { dtls.close(); } catch (RuntimeException _) {}
        }
        if (udp != null) {
            try { udp.close(); } catch (RuntimeException _) {}
        }
    }
}
