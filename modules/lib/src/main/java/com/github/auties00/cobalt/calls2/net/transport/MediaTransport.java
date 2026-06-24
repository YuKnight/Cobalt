package com.github.auties00.cobalt.calls2.net.transport;

import java.net.SocketAddress;
import java.util.function.Consumer;

/**
 * The single media transport seam for a WhatsApp Web call: an {@code RTCPeerConnection}-equivalent that
 * brings the call up over ICE, DTLS, and one SCTP data channel, then multiplexes every packet over that
 * channel.
 *
 * <p>The live capture (see {@code re/calls2-spec/captures/webrtc-datachannel-transport-2026-06-21.md})
 * proves both one-to-one and group/SFU calls use exactly this transport: a peer connection whose SDP has
 * a single {@code m=application UDP/DTLS/SCTP webrtc-datachannel} line and no media tracks, carrying one
 * SCTP data channel labelled {@code sctp-prewarm}. All of audio RTP (hop-by-hop SRTP protected, keyed by
 * the relay {@code <hbh_key>}), RTCP, application STUN keepalives, WARP control, and the subscription ride
 * that one data channel as SCTP DATA. There is no raw UDP media socket and no DTLS-SRTP media track: the
 * media stays hop-by-hop SRTP and is carried as SCTP DATA, not protected by exported DTLS keying material.
 *
 * <p>This sealed interface is the common surface the call session drives: it sends outbound media, RTCP,
 * and standalone WARP control as SCTP DATA, accepts inbound datagrams from the socket reader for the
 * ICE/DTLS/SCTP demultiplex, and reports its progress as {@link TransportEvent}s. An implementation owns
 * the connection's lifecycle from {@link #start()} to {@link #close()} and is driven by the single call
 * transport thread; it is not required to be thread-safe beyond accepting inbound datagrams from the
 * socket reader, which it must hand to the same processing path safely.
 *
 * @implSpec An implementation MUST run ICE connectivity checks and the DTLS handshake to bring up the SCTP
 * data channel, MUST classify each inbound data-channel message by its leading byte and route it (media,
 * RTCP, STUN, WARP) to its handler, and MUST emit the matching {@link TransportEvent} as its connection
 * state advances. {@link #sendMedia(byte[], int)} and {@link #sendRtcp(byte[], int)} MUST hop-by-hop
 * SRTP-protect the bytes and write them as SCTP DATA on the data channel.
 * @implNote This interface unifies the data-channel transport of {@code wa_transport.cc} (the demux and
 *           send orchestrator), {@code wa_transport_p2p.cc} (ICE/DTLS bring-up), and
 *           {@code wa_hbh_srtp_relay.cc}/{@code wa_transport_warp.cc} (hop-by-hop SRTP and WARP) from the
 *           wa-voip WASM module {@code ff-tScznZ8P}. The live capture
 *           (re/calls2-spec/captures/webrtc-datachannel-transport-2026-06-21.md) confirms a single
 *           {@code RTCPeerConnection}/SCTP-data-channel transport carries the whole media and control plane.
 */
public sealed interface MediaTransport extends AutoCloseable permits LiveRelayTransport {
    /**
     * Starts the transport, beginning its connection bring-up.
     *
     * <p>This begins ICE connectivity checks and the DTLS handshake; once the DTLS-wrapped SCTP
     * association opens its data channel the transport reports {@link TransportEvent#RELAY_CREATE_SUCCESS}.
     * The method returns once bring-up has been initiated; the connection becoming usable is reported
     * asynchronously through the {@link TransportEvent} listener.
     *
     * @throws com.github.auties00.cobalt.exception.WhatsAppCallException if bring-up cannot be initiated
     */
    void start();

    /**
     * Sends one outbound media (RTP) packet over the transport as SCTP DATA.
     *
     * <p>The packet occupies the first {@code length} bytes of {@code packet}; the transport hop-by-hop
     * SRTP-protects it in place (keyed by the relay {@code <hbh_key>}) and writes the protected bytes as
     * one SCTP DATA message on the data channel. The buffer must have trailing room for the SRTP
     * authentication tag.
     *
     * @param packet the buffer holding the cleartext RTP packet, with trailing room for the tag
     * @param length the length, in bytes, of the cleartext RTP packet
     * @return the number of cleartext bytes sent, or a non-positive value on failure
     * @throws NullPointerException     if {@code packet} is {@code null}
     * @throws IllegalArgumentException if {@code length} is negative or exceeds {@code packet.length}
     * @throws IllegalStateException    if the transport is not started or has been closed
     */
    int sendMedia(byte[] packet, int length);

    /**
     * Sends one outbound RTCP packet over the transport as SCTP DATA.
     *
     * <p>The transport hop-by-hop SRTCP-protects the bytes in place and writes them as one SCTP DATA
     * message on the data channel.
     *
     * @param packet the buffer holding the cleartext RTCP packet, with trailing room for the trailer
     * @param length the length, in bytes, of the cleartext RTCP packet
     * @return the number of cleartext bytes sent, or a non-positive value on failure
     * @throws NullPointerException     if {@code packet} is {@code null}
     * @throws IllegalArgumentException if {@code length} is negative or exceeds {@code packet.length}
     * @throws IllegalStateException    if the transport is not started or has been closed
     */
    int sendRtcp(byte[] packet, int length);

    /**
     * Sends one standalone WARP control message over the transport as SCTP DATA.
     *
     * <p>A standalone WARP message (such as a BWE configuration) is written as its own SCTP DATA message
     * on the data channel, with the hop-by-hop WARP message-integrity tag appended when the relay
     * negotiated one.
     *
     * @param message the standalone WARP message to send
     * @return the number of bytes sent, or a non-positive value on failure
     * @throws NullPointerException  if {@code message} is {@code null}
     * @throws IllegalStateException if the transport is not started or has been closed
     */
    int sendStandaloneWarp(WarpMessage.Standalone message);

    /**
     * Accepts one inbound datagram from the socket reader for the ICE/DTLS/SCTP demultiplex.
     *
     * <p>The datagram is classified by its leading byte: STUN connectivity traffic goes to the ICE agent
     * (which may answer a binding request, hence the source address is required), and a DTLS record is
     * decrypted into an SCTP packet, fed to the SCTP stack, and surfaced on the data channel where each
     * SCTP DATA message is leading-byte demultiplexed again into media, RTCP, STUN, and WARP.
     *
     * @param datagram the inbound datagram bytes
     * @param source   the transport address the datagram arrived from, used to answer ICE binding requests
     * @throws NullPointerException if {@code datagram} or {@code source} is {@code null}
     */
    void onInboundDatagram(byte[] datagram, SocketAddress source);

    /**
     * Registers the listener that receives this transport's {@link TransportEvent}s.
     *
     * <p>The transport reports the data channel opening, traffic start and stop, bring-up failure, and
     * inbound application data through the listener so the {@link CallTransportController} can advance its
     * bring-up sequence. A second registration replaces the first.
     *
     * @param listener the event listener; never {@code null}
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    void onTransportEvent(Consumer<TransportEvent> listener);

    /**
     * Registers the listener that receives the {@link RtcpFeedback} parsed from each inbound RTCP packet.
     *
     * <p>When a listener is registered the transport unprotects each inbound RTCP message, parses it with
     * {@link RtcpFeedbackParser}, and delivers the fused {@link RtcpFeedback} to the listener; before any
     * registration inbound RTCP is dropped. This is the seam the sender-side bandwidth estimator and
     * rate-control loop attach to so each feedback packet drives one rate-control tick. A second
     * registration replaces the first; passing {@code null} clears the listener.
     *
     * @apiNote This is an optional, additive registration: a transport delivers feedback only after a
     *          listener is set, and a caller that does not run rate control simply never registers one.
     *          Registering a listener does not change how media is demultiplexed or sent.
     * @implSpec The default implementation ignores the listener; a transport overrides this to unprotect
     *           and parse inbound RTCP.
     * @param listener the feedback listener, or {@code null} to clear it
     */
    default void onInboundRtcp(Consumer<RtcpFeedback> listener) {
        // A transport without an RTCP feedback path ignores the registration by default; the live
        // transport overrides this to unprotect and parse inbound RTCP.
    }

    /**
     * Releases the transport's resources and stops its connection.
     *
     * <p>This closes the hop-by-hop SRTP context, the DTLS transport, the SCTP association, the data
     * channel, and any timers the transport holds. After this call the send methods throw
     * {@link IllegalStateException}. Closing an already-closed transport has no effect.
     */
    @Override
    void close();
}
