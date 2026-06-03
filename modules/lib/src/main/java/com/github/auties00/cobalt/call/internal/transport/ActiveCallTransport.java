package com.github.auties00.cobalt.call.internal.transport;

import com.github.auties00.cobalt.ack.CallRelay;
import com.github.auties00.cobalt.call.ActiveCall;
import com.github.auties00.cobalt.call.internal.interaction.InteractionStreamState;
import com.github.auties00.cobalt.call.internal.rtp.srtp.SrtpEndpoint;
import com.github.auties00.cobalt.call.internal.rtp.srtp.SrtpRole;
import com.github.auties00.cobalt.call.internal.transport.dtls.DtlsCertificate;
import com.github.auties00.cobalt.call.internal.transport.dtls.DtlsSrtpDriver;
import com.github.auties00.cobalt.call.internal.transport.dtls.DtlsSrtpEndpoint;
import com.github.auties00.cobalt.call.internal.transport.ice.IceAgent;
import com.github.auties00.cobalt.call.internal.transport.ice.IceCandidate;
import com.github.auties00.cobalt.call.internal.transport.ice.IceCandidatePair;
import com.github.auties00.cobalt.call.internal.transport.ice.IceComponent;
import com.github.auties00.cobalt.call.internal.transport.ice.IceCredentials;
import com.github.auties00.cobalt.call.internal.transport.relay.WaRelayConnector;
import com.github.auties00.cobalt.call.internal.transport.sctp.SctpDtlsBridge;
import com.github.auties00.cobalt.call.internal.transport.sctp.datachannel.DataChannel;
import com.github.auties00.cobalt.call.internal.transport.sctp.datachannel.DataChannelTransport;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Owns the transport-layer state of one {@link ActiveCall}.
 *
 * <p>WhatsApp Web's VoIP transport stack is layered, from the bottom up: an ICE-selected network
 * path, a WA relay (TURN-like) tunnel that carries STUN, DTLS, and SCTP packets multiplexed on one
 * UDP socket, a DTLS-SRTP layer that derives keying material and protects media, and one or more
 * SCTP-borne {@link DataChannel}s that carry peer signaling (in-call interactions and application
 * data). This class holds one instance of each layer for a single call and sequences their
 * lifecycle.
 *
 * <p>{@link #start(StartParameters)} stores the negotiated parameters and instantiates the lower
 * layers; {@link #connectRelay()}, {@link #connectDtls(long, TimeUnit)}, and
 * {@link #connectDataChannel(int, int)} drive the relay, DTLS, and SCTP handshakes in turn; and
 * {@link #close()} tears every layer down in reverse construction order (data channel, then SCTP,
 * then DTLS, then relay, then ICE), releasing native handles. The handshake steps are exposed as
 * separate methods rather than folded into {@link #start(StartParameters)} so that a transport can
 * be constructed and exercised without firing real UDP input or output.
 */
public final class ActiveCallTransport implements AutoCloseable {
    /**
     * The hardcoded peer DTLS SHA-256 fingerprint used in every WA Web call.
     *
     * <p>WhatsApp's transport model does not perform mutual DTLS certificate verification: the
     * relay vouches for both endpoints via the ICE credentials (auth_token as ufrag, relay_key as
     * pwd, both from the relay-tokens block). The "remote answer SDP" the WA Web JS feeds into
     * {@code RTCPeerConnection.setRemoteDescription} is fabricated locally from the relay
     * credentials and this hardcoded fingerprint string; the relay-side wasm accepts whatever
     * cert the client presents and the client trusts whatever fingerprint the local code injects.
     *
     * <p>Source: {@code WAWebVoipRelayConnectionUtils.createAnswerSdp} in the WA Web bundle,
     * confirmed against every captured remote-answer SDP.
     *
     * <p>Callers pass this constant as {@link StartParameters#peerFingerprintSha256()}
     * regardless of which peer is being called.
     */
    public static final byte[] WA_PEER_DTLS_FINGERPRINT_SHA256 = {
            (byte) 0xF9, (byte) 0xCA, (byte) 0x0C, (byte) 0x98,
            (byte) 0xA3, (byte) 0xCC, (byte) 0x71, (byte) 0xD6,
            (byte) 0x42, (byte) 0xCE, (byte) 0x5A, (byte) 0xE2,
            (byte) 0x53, (byte) 0xD2, (byte) 0x15, (byte) 0x20,
            (byte) 0xD3, (byte) 0x1B, (byte) 0xBA, (byte) 0xD8,
            (byte) 0x57, (byte) 0xA4, (byte) 0xF0, (byte) 0xAF,
            (byte) 0xBE, (byte) 0x0B, (byte) 0xFB, (byte) 0xF3,
            (byte) 0x6B, (byte) 0x0C, (byte) 0xA0, (byte) 0x68
    };

    /**
     * Enumerates the strictly forward-progressing lifecycle of an {@link ActiveCallTransport}.
     *
     * <p>A transport advances {@link #IDLE} to {@link #STARTED} to {@link #OPEN} to {@link #CLOSED}
     * and never moves backwards; {@link #CLOSED} is terminal.
     */
    public enum State {
        /**
         * Indicates the transport has been constructed and is awaiting {@link #start(StartParameters)}.
         */
        IDLE,
        /**
         * Indicates {@link #start(StartParameters)} has been called.
         *
         * <p>The negotiated parameters are stored and the lower-layer objects (ICE agent and
         * SCTP data-channel transport) are instantiated, but no wire packets have been exchanged.
         */
        STARTED,
        /**
         * Indicates the default data channel has reached
         * {@link com.github.auties00.cobalt.call.internal.transport.sctp.datachannel.DataChannelState#OPEN}.
         */
        OPEN,
        /**
         * Indicates {@link #close()} has been invoked and every layer has been torn down.
         */
        CLOSED
    }

    /**
     * Carries the negotiated parameters fed into {@link #start(StartParameters)}, filled from the
     * offer and accept exchange.
     *
     * @param credentials           the ICE ufrag and password pair for both sides
     * @param localCert             the self-signed DTLS certificate of the local side
     * @param peerFingerprintSha256 the peer's certificate fingerprint advertised in the offer or accept
     * @param dtlsClient            {@code true} when the local side acts as DTLS client, that is, is the call placer
     * @param relay             the parsed {@link CallRelay} from the inbound offer, read by
     *                              {@link #connectRelay()} to drive the WA relay handshake; {@code null} on the
     *                              outgoing-call path until the outgoing offer builder fills its own spec
     */
    public record StartParameters(
            IceCredentials credentials,
            DtlsCertificate localCert,
            byte[] peerFingerprintSha256,
            boolean dtlsClient,
            CallRelay relay
    ) {
    }

    /**
     * Holds the current lifecycle state, updated only by {@link #start(StartParameters)} and
     * {@link #close()}.
     */
    private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);

    /**
     * Holds the negotiated parameters set by {@link #start(StartParameters)} and read by the
     * handshake methods; {@code null} while {@link State#IDLE}.
     */
    private volatile StartParameters startParameters;

    /**
     * Holds the ICE agent constructed in {@link #start(StartParameters)}; {@code null} while
     * {@link State#IDLE} and after {@link #close()}.
     */
    private volatile IceAgent ice;

    /**
     * Holds the virtual thread that keeps the edgeray STUN bind consent-fresh for the call's
     * lifetime; {@code null} on the raw-UDP path and before {@link #connectIce(DtlsSrtpDriver, long, TimeUnit)}.
     *
     * <p>An edgeray relay forwards a participant's media only while that participant keeps the relay
     * bind path consent-refreshed; unlike a plain TURN relay (which bridges by call-id with no
     * connectivity check), the edge drops the forwarding after its consent timeout. This thread runs
     * the {@link IceAgent} tick loop over the DataChannel STUN seam until {@link #close()} interrupts
     * it, so the relay keeps forwarding for the whole call rather than dropping it after a few seconds.
     */
    private volatile Thread iceConsentTicker;

    /**
     * Holds the DTLS-SRTP endpoint built after ICE nominates a pair; {@code null} until then.
     */
    private volatile DtlsSrtpEndpoint dtls;

    /**
     * Holds the SCTP and DCEP data-channel transport.
     *
     * <p>An initial transport is constructed in {@link #start(StartParameters)};
     * {@link #connectDataChannel(int, int)} replaces it with one whose outbound sink writes into the
     * negotiated DTLS application-data layer and then binds and connects the SCTP association.
     */
    private volatile DataChannelTransport dataChannelTransport;

    /**
     * Holds the default data channel, opened once the SCTP association carries data-channel traffic;
     * {@code null} until then.
     */
    private volatile DataChannel defaultChannel;

    /**
     * Holds the per-call RTP-stream state for outgoing in-call interactions.
     *
     * <p>This carries the SSRC, sequence, and timestamp counters for each logical interaction
     * stream. It is constructed once per call so that SSRCs stay stable across every interaction in
     * the call.
     */
    private final InteractionStreamState interactionStreamState =
            new InteractionStreamState();

    /**
     * Returns the per-call interaction-stream state.
     *
     * <p>The returned state is consumed by
     * {@link com.github.auties00.cobalt.call.internal.interaction.CallInteractionEncoder} to produce
     * the SSRC, sequence, and timestamp triple of the next outgoing interaction packet.
     *
     * @return the stream state
     */
    public InteractionStreamState interactionStreamState() {
        return interactionStreamState;
    }

    /**
     * Returns the current lifecycle state.
     *
     * @return the state
     */
    public State state() {
        return state.get();
    }

    /**
     * Returns the default {@link DataChannel} if it has reached
     * {@link com.github.auties00.cobalt.call.internal.transport.sctp.datachannel.DataChannelState#OPEN},
     * or {@link Optional#empty()} otherwise.
     *
     * @return the default channel, or empty
     */
    public Optional<DataChannel> dataChannel() {
        return Optional.ofNullable(defaultChannel);
    }

    /**
     * Returns the ICE agent if the transport has started, or {@link Optional#empty()} otherwise.
     *
     * @return the agent, or empty
     */
    public Optional<IceAgent> iceAgent() {
        return Optional.ofNullable(ice);
    }

    /**
     * Starts the transport with the negotiated parameters.
     *
     * <p>Constructs the ICE agent and the SCTP data-channel transport from the supplied parameters
     * and transitions from {@link State#IDLE} to {@link State#STARTED}. No wire packets are exchanged
     * here; the outbound STUN and SCTP sinks are wired to discard until the relay and DTLS layers are
     * connected. The DTLS-SRTP endpoint is deferred until ICE nominates a pair.
     *
     * @param parameters the negotiated parameters from the offer and accept exchange
     * @throws NullPointerException  if {@code parameters} is {@code null}
     * @throws IllegalStateException if the transport is not {@link State#IDLE}
     */
    public void start(StartParameters parameters) {
        Objects.requireNonNull(parameters, "parameters cannot be null");
        if (!state.compareAndSet(State.IDLE, State.STARTED)) {
            throw new IllegalStateException("ActiveCallTransport.start: state=" + state.get());
        }
        this.startParameters = parameters;
        IceAgent.OutboundSink stunSink = (packet, destination) -> {};
        this.ice = new IceAgent(parameters.dtlsClient(), parameters.credentials(), stunSink);
        Consumer<byte[]> sctpSink = packet -> {};
        this.dataChannelTransport = new DataChannelTransport(parameters.dtlsClient(), sctpSink);
    }

    /**
     * Returns the negotiated start parameters, present once {@link #start(StartParameters)} has been
     * called.
     *
     * @return the parameters, or empty
     */
    public Optional<StartParameters> startParameters() {
        return Optional.ofNullable(startParameters);
    }

    /**
     * Drives the WA relay allocate handshake against the first te2 endpoint in the offer spec.
     *
     * <p>Runs the {@link WaRelayConnector} allocate exchange against the endpoint at index {@code 0}
     * of the spec's te2 list and stores the resulting {@link WaRelayConnector.Allocation} on the
     * transport for the DTLS layer to consume. This is deliberately not invoked from
     * {@link #start(StartParameters)} so that a transport can be constructed without firing real UDP
     * input or output; production paths call it explicitly after {@link #start(StartParameters)}.
     *
     * @return the relay allocation result
     * @throws IllegalStateException if no {@link CallRelay} was supplied at start, or if the
     *                               transport is not in {@link State#STARTED}
     */
    public WaRelayConnector.Allocation connectRelay() {
        if (state.get() != State.STARTED) {
            throw new IllegalStateException("connectRelay requires state=STARTED, got " + state.get());
        }
        var params = this.startParameters;
        if (params == null || params.relay() == null) {
            throw new IllegalStateException("connectRelay requires StartParameters.relay to be non-null");
        }
        var connector = new WaRelayConnector();
        // Try every te2 endpoint and, failing those, the relaylatency-advertised endpoints: captured
        // traffic shows a successful client allocates to the <relaylatency><te> endpoints rather than
        // the offer-ACK <te2> endpoints (which time out).
        var allocation = connector.connectAny(params.relay(), java.util.List.copyOf(relayLatencyEndpoints));
        this.relayAllocation = allocation;
        return allocation;
    }

    /**
     * Holds the result of the last successful {@link #connectRelay()} call; {@code null} if relay
     * connection has not yet succeeded for this call.
     */
    private volatile WaRelayConnector.Allocation relayAllocation;

    /**
     * Returns the relay allocation if {@link #connectRelay()} has succeeded for this call.
     *
     * @return the allocation, or empty
     */
    public Optional<WaRelayConnector.Allocation> relayAllocation() {
        return Optional.ofNullable(relayAllocation);
    }

    /**
     * Holds the DTLS-SRTP driver that wraps the WhatsApp Web GraphQL transport with the TLS state machine and
     * demultiplexes STUN, DTLS, and SRTP byte ranges per RFC 7983; {@code null} until
     * {@link #connectDtls(long, TimeUnit)} runs.
     */
    private volatile DtlsSrtpDriver dtlsDriver;

    /**
     * Drives the DTLS handshake over the relay-allocated UDP transport and returns the negotiated
     * SRTP endpoint.
     *
     * <p>Constructs a {@link DtlsSrtpDriver} over the {@link WaRelayConnector.Allocation} transport
     * with the local DTLS role derived from {@link StartParameters#dtlsClient()}, starts it on a
     * dedicated virtual thread, waits up to the given timeout for the handshake to complete, and
     * stores the driver on the transport. The DTLS client role maps to {@link SrtpRole#CLIENT} and
     * the server role to {@link SrtpRole#SERVER}.
     *
     * @param timeout the handshake timeout
     * @param unit    the timeout unit
     * @return the negotiated SRTP endpoint
     * @throws IllegalStateException if {@link #connectRelay()} has not yet succeeded, or if start
     *                               parameters are missing
     * @throws IOException          if the DTLS handshake fails
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    public SrtpEndpoint connectDtls(
            long timeout, TimeUnit unit)
            throws IOException, InterruptedException {
        var allocation = this.relayAllocation;
        if (allocation == null) {
            throw new IllegalStateException("connectDtls requires connectRelay() to have succeeded first");
        }
        var params = this.startParameters;
        if (params == null) {
            throw new IllegalStateException("connectDtls requires StartParameters");
        }
        var role = params.dtlsClient()
                ? SrtpRole.CLIENT
                : SrtpRole.SERVER;
        // Prefer the relay-tunneled DatagramTransport when the relay-channel driver is alive: peer
        // DTLS bytes ride INSIDE the relay's DataChannel (WA's edgeray forwards non-STUN traffic
        // to the bound peer endpoint), so a raw-UDP socket would never reach the remote side.
        com.github.auties00.cobalt.call.internal.transport.ice.DatagramTransport peerTransport;
        if (allocation.driver() != null && allocation.driver().channel() != null) {
            peerTransport = new com.github.auties00.cobalt.call.internal.transport.relay.RelayDatagramTransport(
                    allocation.driver(), allocation.driver().channel());
        } else {
            peerTransport = allocation.transport();
        }
        var driver = new DtlsSrtpDriver(
                peerTransport, role,
                params.localCert(), params.peerFingerprintSha256());
        driver.start();
        var srtpEndpoint = driver.awaitHandshake(timeout, unit);
        this.dtlsDriver = driver;
        return srtpEndpoint;
    }

    /**
     * Drives the ICE connectivity check over the relay-allocated UDP socket and blocks until a
     * candidate pair succeeds, or the timeout elapses.
     *
     * <p>Constructs an {@link IceAgent} keyed on the relay credentials whose outbound STUN sink writes
     * through the supplied {@link DtlsSrtpDriver} (RFC 7983 byte-0 demux multiplexes STUN, DTLS, and
     * SRTP on the one relay socket), adds the local host candidate and the relay endpoint as the remote
     * candidate, points the driver's STUN handler back at {@link IceAgent#handleInboundStun(byte[])},
     * then starts the agent and drives {@link IceAgent#tick()} on a virtual thread until a check
     * succeeds. The binding requests carry no USERNAME and a MESSAGE-INTEGRITY keyed by the relay
     * {@code <key>}, matching the native Desktop client's wire form. This replaces the no-op agent
     * created by {@link #start(StartParameters)}.
     *
     * @param driver  the DTLS-SRTP driver wrapping the relay socket, whose STUN send/receive seam the
     *                agent drives
     * @param timeout the maximum time to wait for a successful connectivity check
     * @param unit    the unit of {@code timeout}
     * @return {@code true} if a connectivity check succeeded within the budget, {@code false} otherwise
     * @throws IllegalStateException if {@link #connectRelay()} or {@link #start(StartParameters)} has
     *                               not run
     */
    public boolean connectIce(DtlsSrtpDriver driver, long timeout, TimeUnit unit) {
        var allocation = this.relayAllocation;
        var params = this.startParameters;
        if (allocation == null || params == null) {
            throw new IllegalStateException("connectIce requires connectRelay() and start() to have run");
        }
        var udp = allocation.transport();
        if (udp == null) {
            // Edgeray DataChannel path: there is no raw UDP socket, but the relay still requires the
            // STUN bind path to be established AND kept consent-fresh before it forwards this
            // participant's media to the call. (A plain TURN relay bridges by call-id with no check;
            // an edgeray edge does not, and drops the forwarding after a consent timeout.) Run the
            // IceAgent over the supplied driver's DataChannel STUN seam, respond to the relay's inbound
            // binding requests, and keep ticking for the whole call so consent never lapses.
            var relayDriver = allocation.driver();
            if (relayDriver == null) {
                return false;
            }
            var edgeAgent = new IceAgent(params.dtlsClient(), params.credentials(),
                    (packet, destination) -> driver.sendStun(packet));
            this.ice = edgeAgent;
            edgeAgent.addLocalCandidate(IceCandidate.host(IceComponent.RTP, allocation.relayedAddress(), "host"));
            edgeAgent.addRemoteCandidate(IceCandidate.host(IceComponent.RTP, relayDriver.remote(), "relay"));
            var edgeBound = new java.util.concurrent.CountDownLatch(1);
            edgeAgent.setListener(new IceAgent.Listener() {
                @Override
                public void onCheckSucceeded(IceCandidatePair pair) {
                    edgeBound.countDown();
                }

                @Override
                public void onNominated(IceCandidatePair pair) {
                    edgeBound.countDown();
                }
            });
            driver.setStunHandler(edgeAgent::handleInboundStun);
            edgeAgent.start();
            var consent = Thread.ofVirtual().name("ice-consent-edgeray").start(() -> {
                try {
                    while (state.get() != State.CLOSED && !Thread.currentThread().isInterrupted()) {
                        edgeAgent.tick();
                        Thread.sleep(50);
                    }
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                }
            });
            this.iceConsentTicker = consent;
            try {
                // Report bound once the first check succeeds, but leave the consent ticker running for
                // the call's lifetime so the relay keeps forwarding.
                return edgeBound.await(timeout, unit);
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        var agent = new IceAgent(params.dtlsClient(), params.credentials(),
                (packet, destination) -> driver.sendStun(packet));
        this.ice = agent;
        agent.addLocalCandidate(IceCandidate.host(IceComponent.RTP, udp.localAddress(), "host"));
        agent.addRemoteCandidate(IceCandidate.host(IceComponent.RTP, udp.remoteAddress(), "relay"));
        var succeeded = new java.util.concurrent.CountDownLatch(1);
        agent.setListener(new IceAgent.Listener() {
            @Override
            public void onCheckSucceeded(IceCandidatePair pair) {
                succeeded.countDown();
            }

            @Override
            public void onNominated(IceCandidatePair pair) {
                succeeded.countDown();
            }
        });
        driver.setStunHandler(agent::handleInboundStun);
        agent.start();
        var ticker = Thread.ofVirtual().name("ice-tick-" + udp.localAddress().getPort()).start(() -> {
            try {
                while (state.get() != State.CLOSED && !Thread.currentThread().isInterrupted()) {
                    agent.tick();
                    Thread.sleep(50);
                }
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        });
        this.iceConsentTicker = ticker;
        try {
            return succeeded.await(timeout, unit);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Builds the DTLS-SRTP driver over the relay-tunneled transport and stores it WITHOUT starting it
     * or awaiting a handshake, for the hop-by-hop media path where the SRTP keys come from the relay
     * {@code <hbh_key>} rather than a peer DTLS export.
     *
     * <p>The relay forwards SRTP and the peer never completes a peer DTLS handshake (the WhatsApp P2P
     * path is disabled), so awaiting one would always time out. The driver is still used purely as the
     * byte transport: its {@code sendSrtp} writes raw SRTP to the relay DataChannel and its inbound
     * demux forwards raw SRTP to the handler, both independent of DTLS state. The caller starts the
     * driver via {@code VoiceCallSession.start()}, which kicks the (ignored) handshake and installs the
     * inbound demux.
     *
     * @return the built driver
     * @throws IllegalStateException if {@link #connectRelay()} has not yet succeeded, or if start
     *                               parameters are missing
     */
    public DtlsSrtpDriver buildDtlsDriver() {
        var allocation = this.relayAllocation;
        if (allocation == null) {
            throw new IllegalStateException("buildDtlsDriver requires connectRelay() to have succeeded first");
        }
        var params = this.startParameters;
        if (params == null) {
            throw new IllegalStateException("buildDtlsDriver requires StartParameters");
        }
        var role = params.dtlsClient() ? SrtpRole.CLIENT : SrtpRole.SERVER;
        com.github.auties00.cobalt.call.internal.transport.ice.DatagramTransport peerTransport;
        if (allocation.driver() != null && allocation.driver().channel() != null) {
            peerTransport = new com.github.auties00.cobalt.call.internal.transport.relay.RelayDatagramTransport(
                    allocation.driver(), allocation.driver().channel());
        } else {
            peerTransport = allocation.transport();
        }
        var driver = new DtlsSrtpDriver(
                peerTransport, role, params.localCert(), params.peerFingerprintSha256());
        this.dtlsDriver = driver;
        return driver;
    }

    /**
     * Returns the DTLS driver if {@link #connectDtls(long, TimeUnit)} has succeeded.
     *
     * @return the driver, or empty
     */
    public Optional<DtlsSrtpDriver> dtlsDriver() {
        return Optional.ofNullable(dtlsDriver);
    }

    /**
     * Returns the negotiated SRTP endpoint if {@link #connectDtls(long, TimeUnit)} has succeeded.
     *
     * @return the SRTP endpoint, or empty
     */
    public Optional<SrtpEndpoint> srtp() {
        return Optional.ofNullable(dtlsDriver).map(d -> d.srtpEndpoint());
    }

    /**
     * Holds the SCTP-over-DTLS inbound bridge produced by {@link #connectDataChannel(int, int)}.
     */
    private volatile SctpDtlsBridge sctpDtlsBridge;

    /**
     * Holds the most recent observed RTT sample per WhatsApp Web GraphQL endpoint, populated from inbound
     * {@code <relaylatency><te latency=... relay_name=...>} stanzas via
     * {@link #recordRelayLatency(String, long)}. The map keeps the latest sample per
     * {@code relay_name} so a future relay re-election can pick the lowest-RTT candidate.
     */
    private final java.util.concurrent.ConcurrentHashMap<String, Long> relayLatencies =
            new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Records one round-trip-time sample for a named relay.
     *
     * <p>Called by the call receiver when a {@code <relaylatency><te ... />} stanza arrives.
     * Replaces any previous sample for the relay, since callers track the freshest value rather
     * than a moving average for the simple relay-election heuristic.
     *
     * @param relayName the WhatsApp Web GraphQL endpoint identifier ({@code relay_name} attribute), or
     *                  {@code null} to skip the recording
     * @param latency   the round-trip-time sample (units mirror the wire value)
     */
    public void recordRelayLatency(String relayName, long latency) {
        if (relayName == null) {
            return;
        }
        relayLatencies.put(relayName, latency);
    }

    /**
     * Holds the relay transport addresses advertised in {@code <relaylatency><te>} stanzas.
     *
     * <p>These are the relay endpoints the server tells the client to measure and allocate against;
     * the captured corpus shows a successful client allocates to these rather than to the offer-ACK
     * {@code <te2>} endpoints. Recorded by {@link #recordRelayEndpoint(java.net.InetSocketAddress)} and
     * consumed by {@link #connectRelay()} as additional Allocate targets.
     */
    private final java.util.List<java.net.InetSocketAddress> relayLatencyEndpoints =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * Records one relay transport address advertised in a {@code <relaylatency><te>} stanza.
     *
     * @param endpoint the relay transport address, or {@code null} to skip
     */
    public void recordRelayEndpoint(java.net.InetSocketAddress endpoint) {
        if (endpoint != null && !relayLatencyEndpoints.contains(endpoint)) {
            relayLatencyEndpoints.add(endpoint);
        }
    }

    /**
     * Returns the relay endpoints advertised in {@code <relaylatency><te>} stanzas, in arrival order.
     *
     * @return the relaylatency-advertised relay endpoints
     */
    public java.util.List<java.net.InetSocketAddress> relayLatencyEndpoints() {
        return java.util.List.copyOf(relayLatencyEndpoints);
    }

    /**
     * Returns the latest RTT sample for a relay, if any has been observed.
     *
     * @param relayName the WhatsApp Web GraphQL endpoint identifier
     * @return the latest sample, or empty when none has been observed
     */
    public Optional<Long> relayLatency(String relayName) {
        if (relayName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(relayLatencies.get(relayName));
    }

    /**
     * Returns an immutable snapshot of all observed relay-latency samples, keyed by relay name.
     *
     * @return the snapshot
     */
    public java.util.Map<String, Long> relayLatencies() {
        return java.util.Map.copyOf(relayLatencies);
    }

    /**
     * Opens the SCTP association and prepares the data-channel transport.
     *
     * <p>Builds a {@link DataChannelTransport} whose outbound sink writes each SCTP packet as one
     * encrypted DTLS application-data record, bridges the inbound side so decrypted DTLS records feed
     * back into the transport via a {@link SctpDtlsBridge}, then binds the local port and connects to
     * the remote port. Requires {@link #connectDtls(long, TimeUnit)} to have succeeded so the SCTP
     * packets ride the negotiated DTLS application-data layer. WebRTC's conventional SCTP ports are
     * {@code 5000} on both sides.
     *
     * @param localPort  the local SCTP port
     * @param remotePort the peer's SCTP port
     * @return the data-channel transport, ready to open channels via
     *         {@link DataChannelTransport#open(String, com.github.auties00.cobalt.call.internal.transport.sctp.datachannel.DataChannelOptions)}
     * @throws IllegalStateException if {@link #connectDtls(long, TimeUnit)} has not yet succeeded, or
     *                               if the driver's DTLS transport handshake state has been lost
     */
    public DataChannelTransport connectDataChannel(int localPort, int remotePort) {
        var driver = this.dtlsDriver;
        if (driver == null) {
            throw new IllegalStateException(
                    "connectDataChannel requires connectDtls() to have succeeded first");
        }
        var bcDtls = driver.dtlsTransport();
        if (bcDtls == null) {
            throw new IllegalStateException(
                    "connectDataChannel: driver.dtlsTransport() is null - handshake state lost");
        }
        var params = this.startParameters;
        var transport = new DataChannelTransport(params.dtlsClient(), packet -> {
            try { bcDtls.send(packet, 0, packet.length); }
            catch (IOException _) {}
        });
        this.sctpDtlsBridge = new SctpDtlsBridge(
                bcDtls, transport);
        transport.bind(localPort);
        transport.connect(remotePort);
        this.dataChannelTransport = transport;
        return transport;
    }

    /**
     * Returns the SCTP-over-DTLS bridge if {@link #connectDataChannel(int, int)} has succeeded.
     *
     * @return the bridge, or empty
     */
    public Optional<SctpDtlsBridge> sctpDtlsBridge() {
        return Optional.ofNullable(sctpDtlsBridge);
    }

    /**
     * Tears every layer down in reverse construction order.
     *
     * <p>Closes the default channel, the SCTP data-channel transport, the SCTP-over-DTLS bridge, the
     * DTLS driver, and the relay allocation transport in turn, swallowing any runtime exception from
     * each so a failure in one layer does not block teardown of the rest, and clears the ICE agent
     * and start parameters. Transitions to {@link State#CLOSED}. This method is idempotent: invoking
     * it on an already-closed or never-started transport is a no-op.
     */
    @Override
    public void close() {
        if (state.getAndSet(State.CLOSED) == State.CLOSED) {
            return;
        }
        var channel = this.defaultChannel;
        if (channel != null) {
            try { channel.close(); } catch (RuntimeException _) {}
            this.defaultChannel = null;
        }
        var transport = this.dataChannelTransport;
        if (transport != null) {
            try { transport.close(); } catch (RuntimeException _) {}
            this.dataChannelTransport = null;
        }
        var bridge = this.sctpDtlsBridge;
        if (bridge != null) {
            try { bridge.close(); } catch (RuntimeException _) {}
            this.sctpDtlsBridge = null;
        }
        var driver = this.dtlsDriver;
        if (driver != null) {
            try { driver.close(); } catch (RuntimeException _) {}
            this.dtlsDriver = null;
        }
        var allocation = this.relayAllocation;
        if (allocation != null) {
            try { allocation.transport().close(); } catch (RuntimeException _) {}
            this.relayAllocation = null;
        }
        var consent = this.iceConsentTicker;
        if (consent != null) {
            consent.interrupt();
            this.iceConsentTicker = null;
        }
        this.dtls = null;
        this.ice = null;
        this.startParameters = null;
    }
}
