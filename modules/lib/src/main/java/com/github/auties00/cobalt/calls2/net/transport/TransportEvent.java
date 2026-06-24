package com.github.auties00.cobalt.calls2.net.transport;

/**
 * Enumerates the transport-layer events that drive the call transport state machine.
 *
 * <p>The media transport reports its progress to the {@link CallTransportController} as a sequence of
 * these events: the relay being allocated, downlink and uplink media traffic starting and stopping,
 * the relay-bind phase failing when no relay answers, and inbound application data arriving. The
 * controller advances its bring-up sequence and emits the matching engine call events in response.
 *
 * @implNote This implementation enumerates the {@code k*} transport notifications of
 *           {@code call_transport.cc} ({@code kRelayCreateSuccess}, {@code kRelayBindsFailed},
 *           {@code kRxTrafficStarted}, {@code kRxTrafficStopped}, {@code kTxTrafficStart},
 *           {@code kTxTrafficStopped}, {@code kRxAppData}) from the wa-voip WASM module
 *           {@code ff-tScznZ8P}, recovered in the transport-signaling reverse.
 */
public enum TransportEvent {
    /**
     * The relay was allocated and its remote fingerprint set ({@code kRelayCreateSuccess}).
     *
     * <p>This is the success outcome of relay election; the controller proceeds to set up the data
     * channel.
     */
    RELAY_CREATE_SUCCESS,

    /**
     * No relay answered the bind requests ({@code kRelayBindsFailed}).
     *
     * <p>The controller surfaces this as a transport failure to the application; the call cannot reach
     * a relay.
     */
    RELAY_BINDS_FAILED,

    /**
     * Downlink media and stream started for the winning candidate pair or relay index
     * ({@code kRxTrafficStarted}).
     *
     * <p>The controller begins the receive media pipeline and the application-data side channel.
     */
    RX_TRAFFIC_STARTED,

    /**
     * Downlink media stopped ({@code kRxTrafficStopped}).
     */
    RX_TRAFFIC_STOPPED,

    /**
     * Uplink media to the chosen pair or relay started ({@code kTxTrafficStart}).
     *
     * <p>The controller begins the send media pipeline; the transport is now bidirectional.
     */
    TX_TRAFFIC_START,

    /**
     * Uplink media stopped ({@code kTxTrafficStopped}).
     */
    TX_TRAFFIC_STOPPED,

    /**
     * Inbound application data arrived from the peer or SFU ({@code kRxAppData}).
     *
     * <p>The controller hands the bytes to the application-data controller, which demultiplexes them
     * into reaction, rekey, subscription, and feedback handlers.
     */
    RX_APP_DATA
}
