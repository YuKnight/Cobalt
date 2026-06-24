package com.github.auties00.cobalt.calls2.net.transport;

/**
 * Enumerates the bring-up states of the Web-P2P application-data channel.
 *
 * <p>On the WebRTC-interoperable Web-P2P path the application-data channel is brought up in stages: the
 * controller creates the data-channel controller, runs the DTLS handshake, opens the SCTP association
 * and its DCEP channel, and only then becomes ready to carry application data. This enum names those
 * stages so the {@link CallTransportController} can gate each step on the previous one and fall back to
 * the relay application-data stream if a send fails after the channel went ready.
 *
 * @implNote This implementation models the data-channel bring-up of {@code call_transport.cc} and
 *           {@code wa_data_channel_controller_wrapper.cc} from the wa-voip WASM module
 *           {@code ff-tScznZ8P}: the controller is created, the DTLS handshake is started
 *           ({@code start_dtls_handshake} sets the native state to {@code 2}), the SCTP association and
 *           DCEP open, the channel reaches {@code kDataChannelReady} ({@code DAT_a276c = 1}), and a send
 *           failure deactivates the Web-P2P path and falls back to the relay application-data stream. The
 *           proprietary relay path does not run this machine; it carries application data over the relay
 *           RTP stream directly.
 */
public enum DataChannelState {
    /**
     * No data channel exists yet; the data-channel controller has not been created.
     */
    UNINITIALIZED,

    /**
     * The data-channel controller is created and the DTLS handshake is in progress.
     *
     * <p>This corresponds to the native handshake-started state; the SCTP association cannot open until
     * the handshake completes.
     */
    DTLS_HANDSHAKING,

    /**
     * The DTLS handshake completed and the SCTP association with its DCEP channel is opening.
     */
    SCTP_OPENING,

    /**
     * The channel is open and ready to carry application data; buffered messages are flushed.
     */
    READY,

    /**
     * The Web-P2P channel was deactivated after a send failure; application data now rides the relay RTP
     * stream.
     */
    RELAY_FALLBACK,

    /**
     * The channel is closed and carries no further application data.
     */
    CLOSED
}
