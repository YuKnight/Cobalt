package com.github.auties00.cobalt.calls2.net.transport;

/**
 * Enumerates the four kinds an inbound transport datagram is classified into by its leading byte before
 * it is routed to the matching subsystem.
 *
 * <p>A single UDP flow toward a WhatsApp relay or a Web-P2P peer multiplexes STUN connectivity checks,
 * DTLS handshake and application-data records, and SRTP/SRTCP media on one port. The receiver tells them
 * apart by the first byte of each datagram per the RFC 7983 demultiplexing ranges, which the native
 * inbound demux (fn4976) applies before dispatching: STUN to the binding parser, DTLS or SCTP-over-DTLS
 * to the data-channel controller, and media to the SRTP unprotect path. This enum names those buckets;
 * {@link InboundPacketDemux#classify(byte[])} computes the mapping and a value of {@link #UNKNOWN} marks a
 * datagram that falls in none of the ranges and is dropped.
 *
 * @implNote This implementation mirrors the classification in {@code on_rx_data_internal} (fn4976) of
 * {@code transport/wa_transport.cc} in the wa-voip WASM module {@code ff-tScznZ8P}, which inspects the
 * leading byte to separate STUN, DTLS/SCTP, and RTP/RTCP traffic; the byte ranges are the RFC 7983
 * boundaries WebRTC stacks share, also reproduced in Cobalt's legacy {@code DtlsSrtpDriver} demux.
 */
public enum PacketClass {
    /**
     * Marks a STUN message (RFC 5389 binding request or response, including the WhatsApp-proprietary
     * relay attributes), recognised by a leading byte in {@code 0..3}, or a TURN ChannelData message in
     * {@code 64..79}.
     */
    STUN,

    /**
     * Marks a DTLS record (handshake or application data), recognised by a leading byte in {@code 20..63};
     * on the Web-P2P path the application-data records carry the SCTP DataChannel traffic.
     */
    DTLS,

    /**
     * Marks an SRTP or SRTCP media packet, recognised by a leading byte in {@code 128..191} (a valid RTP
     * version-2 header), which carries audio or video and any piggybacked WARP control bytes.
     */
    RTP,

    /**
     * Marks a datagram whose leading byte falls in none of the recognised ranges; such datagrams are
     * dropped rather than routed.
     */
    UNKNOWN
}
