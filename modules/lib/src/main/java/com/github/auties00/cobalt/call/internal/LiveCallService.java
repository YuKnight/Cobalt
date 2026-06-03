package com.github.auties00.cobalt.call.internal;

import com.github.auties00.cobalt.ack.CallRelay;
import com.github.auties00.cobalt.call.*;
import com.github.auties00.cobalt.call.internal.audio.AudioPipelineOptions;
import com.github.auties00.cobalt.call.internal.interaction.CallInteractionEncoder;
import com.github.auties00.cobalt.call.internal.rtp.srtp.SrtpRole;
import com.github.auties00.cobalt.call.internal.session.GroupCallSession;
import com.github.auties00.cobalt.call.internal.session.VoiceCallSession;
import com.github.auties00.cobalt.call.internal.signaling.CallIdGenerator;
import com.github.auties00.cobalt.call.internal.signaling.CallReceiver;
import com.github.auties00.cobalt.call.internal.signaling.CallStanza;
import com.github.auties00.cobalt.call.internal.transport.ActiveCallTransport;
import com.github.auties00.cobalt.call.internal.transport.dtls.DtlsCertificate;
import com.github.auties00.cobalt.call.internal.transport.dtls.DtlsSrtpDriver;
import com.github.auties00.cobalt.call.internal.transport.ice.IceCredentials;
import com.github.auties00.cobalt.call.internal.transport.relay.WaRelayConnector;
import com.github.auties00.cobalt.call.session.VoiceCallOptions;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.listener.linked.LinkedCallEndedListener;
import com.github.auties00.cobalt.message.MessageEncryptionType;
import com.github.auties00.cobalt.message.MessageService;
import com.github.auties00.cobalt.model.call.datachannel.E2eRekeyPayload;
import com.github.auties00.cobalt.model.call.datachannel.E2eRekeyPayloadSpec;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.wam.WamService;
import com.github.auties00.cobalt.wam.event.CallEventBuilder;
import com.github.auties00.cobalt.wam.type.CallResultType;
import com.github.auties00.cobalt.wam.type.CallSide;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Live implementation of {@link CallService} that coordinates one client's call activity.
 *
 * <p>This service owns the registry of in-flight {@link ActiveCall} sessions
 * and exposes the call-control entry points used by {@link LinkedWhatsAppClient}:
 * {@link #placeCall(Jid, CallOptions)} for outbound calls, and
 * {@link #accept(IncomingCall, CallOptions)} and
 * {@link #reject(IncomingCall, CallEndReason)} for inbound offers. It sits
 * between the public client API and listener surface above and the signaling
 * classes ({@link CallReceiver}, {@link CallStanza}) and transport and media
 * layers below. There is one instance per {@link LinkedWhatsAppClient}.
 *
 * <p>The service implements the signaling and state-machine portion of a
 * call: it produces {@link ActiveCall} instances that own their lifecycle and
 * react to peer-side state changes routed back through
 * {@link #onPeerAccept(String)}, {@link #onPeerReject(String, String)}, and
 * {@link #onPeerTerminate(String, String)}. A placed or accepted call sits in
 * {@link CallState#CONNECTING} until the media plane wires into its
 * {@link ActiveCall} and is terminated by either a local
 * {@link ActiveCall#hangup()} or a peer termination.
 */
public class LiveCallService implements CallService {
    /**
     * Holds the owning client.
     *
     * <p>The client is used to send signaling stanzas, resolve the local self
     * {@link Jid}, and surface end-of-call notifications to listeners.
     */
    private final LinkedWhatsAppClient whatsapp;

    /**
     * Tracks live calls keyed by their unique call identifier.
     *
     * <p>An entry is added on {@link #placeCall(Jid, CallOptions)} or
     * {@link #accept(IncomingCall, CallOptions)} and removed by
     * {@link #unregister(String)} when the call reaches
     * {@link CallState#ENDED}.
     */
    private final ConcurrentHashMap<String, ActiveCall> activeCalls = new ConcurrentHashMap<>();

    /**
     * Tracks per-call telemetry accumulators keyed by call identifier.
     *
     * <p>An accumulator is created alongside its {@link ActiveCall} and is
     * drained into a WAM Call event when the call ends.
     */
    private final ConcurrentHashMap<String, CallStatsAccumulator> stats = new ConcurrentHashMap<>();

    /**
     * Holds the deferred caller-side media bring-up continuations, keyed by call identifier.
     *
     * <p>For an outbound call the relay is allocated eagerly when the offer ACK arrives, but the
     * peer-DTLS handshake and media session are deferred until the peer answers (the remote side
     * only opens its relay DataChannel after {@code <accept>}). The continuation that runs those
     * deferred steps is parked here and executed exactly once by whichever of relay-allocation or
     * {@link #onPeerAccept(String)} observes both conditions met. Removal from this map is the
     * run-once gate.
     */
    private final ConcurrentHashMap<String, Runnable> pendingMediaPlane = new ConcurrentHashMap<>();

    /**
     * Call ids whose media plane bring-up has already been initiated, so a relay that arrives both
     * inline in the offer and again in a {@code <group_update>} push does not start two transports.
     */
    private final java.util.Set<String> mediaPlaneStarted = ConcurrentHashMap.newKeySet();

    /**
     * Holds the peer's most recent ICE candidate set per call, parsed from the {@code <accept>} and
     * {@code <transport>} stanzas by {@link com.github.auties00.cobalt.call.internal.signaling.CallReceiver}.
     *
     * <p>WhatsApp's primary media path is peer-to-peer: each side advertises its transport addresses and
     * runs STUN connectivity checks against the other's, carrying SRTP over the nominated pair. These
     * are the addresses the call's ICE agent forms pairs against.
     */
    private final ConcurrentHashMap<String, java.util.List<java.net.InetSocketAddress>> peerCandidates =
            new ConcurrentHashMap<>();

    /**
     * Holds the per-call media key generated for an outbound call, retained so the peer-to-peer ICE
     * connectivity check can key its STUN MESSAGE-INTEGRITY with it.
     */
    private final ConcurrentHashMap<String, byte[]> callKeys = new ConcurrentHashMap<>();

    /**
     * Holds the WAM service used to commit the per-call telemetry event.
     */
    private final WamService wamService;

    /**
     * Holds the {@link MessageService} that owns the per-device fanout encryption + addressing-mode
     * resolution for outbound call offers and the Signal decryption for inbound
     * {@code <enc_rekey>} envelopes.
     */
    private final MessageService messageService;

    /**
     * Length, in bytes, of the per-call shared key that authenticates the relay-encrypted
     * media stream. The wire format wraps the key in a
     * {@code MessageContainer{ call: Call{ callKey } }} protobuf and Signal-encrypts it per
     * peer device.
     */
    private static final int CALL_KEY_LENGTH = 32;

    /**
     * Logger for transport bring-up diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(LiveCallService.class.getName());

    /**
     * Default local audio SSRC seeded into the outgoing-audio pipeline. SSRCs flow with
     * the relay's call session id rather than being negotiated peer-to-peer, so a fixed
     * value works for the MVP 1:1 audio path.
     */
    private static final int DEFAULT_LOCAL_AUDIO_SSRC = 0xCB01;

    /**
     * Default remote audio SSRC accepted by the inbound-audio pipeline.
     */
    private static final int DEFAULT_REMOTE_AUDIO_SSRC = 0xCB02;

    /**
     * Default local video SSRC for the camera or screen-share track.
     */
    private static final int DEFAULT_LOCAL_VIDEO_SSRC = 0xCB03;

    /**
     * Default remote video SSRC accepted by the inbound-video pipeline.
     */
    private static final int DEFAULT_REMOTE_VIDEO_SSRC = 0xCB04;

    /**
     * Default local SSRC for the screen-share track, distinct from the camera SSRC so a camera and a
     * screen share can run at once.
     */
    private static final int DEFAULT_LOCAL_SCREEN_SSRC = 0xCB05;

    /**
     * Default remote SSRC accepted for the peer's screen-share track.
     */
    private static final int DEFAULT_REMOTE_SCREEN_SSRC = 0xCB06;

    /**
     * Default Opus RTP payload type.
     *
     * @implNote This implementation uses {@code 120}, the dynamic payload type WhatsApp's voip engine
     * stamps on Opus media RTP. A live 1:1 call to a WhatsApp Android client shows its outbound audio
     * arriving with payload type {@code 120}; sending any other type leaves the peer unable to map the
     * stream to a codec, so it never reaches the connected media state. The payload type is not carried
     * in the {@code <audio>} call-signaling element, so it is a fixed profile constant rather than a
     * negotiated value.
     */
    private static final int DEFAULT_OPUS_PAYLOAD_TYPE = 120;

    /**
     * DTLS-handshake timeout used by the media-session bring-up before the call is marked
     * connected.
     */
    private static final long DTLS_HANDSHAKE_TIMEOUT_SECONDS = 15;

    /**
     * Time budget for the ICE connectivity check to bind the relay path before media is pumped.
     *
     * @implNote This implementation uses 8 seconds: the relay binding is a single roundtrip retried
     * every 50 ms, so 8 seconds comfortably covers a slow relay without stalling the call setup past
     * the user-visible ring window.
     */
    private static final long ICE_CONNECTIVITY_TIMEOUT_SECONDS = 8;

    /**
     * Default SCTP port used by both sides for the in-call data channel, matching the WebRTC
     * convention of {@code 5000} on both ends.
     */
    private static final int DEFAULT_SCTP_PORT = 5000;

    /**
     * Source of randomness for {@code callKey} bytes.
     */
    private final SecureRandom random = new SecureRandom();

    /**
     * Constructs a service bound to the given client and {@link MessageService}.
     *
     * @param whatsapp       the owning client
     * @param wamService     the WAM telemetry service used for end-of-call field-stats events
     * @param messageService the {@link MessageService} used to build, encrypt, and ship the
     *                       outbound {@code <call><offer>} stanza per peer and to decrypt the
     *                       inbound {@code <enc_rekey>} envelope
     */
    public LiveCallService(LinkedWhatsAppClient whatsapp, WamService wamService,
                           MessageService messageService) {
        this.whatsapp = Objects.requireNonNull(whatsapp, "whatsapp cannot be null");
        this.wamService = wamService;
        this.messageService = Objects.requireNonNull(messageService, "messageService cannot be null");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActiveCall placeCall(Jid peer, CallOptions options) {
        Objects.requireNonNull(peer, "peer cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        var selfJid = whatsapp.store().accountStore().jid()
                .orElseThrow(() -> new IllegalStateException("Not logged in"));
        var callId = CallIdGenerator.generate();
        var session = new ActiveCall(this, callId, peer, peer, selfJid, true, options);
        activeCalls.put(callId, session);
        var accumulator = new CallStatsAccumulator(callId, CallSide.CALLER, options.videoEnabled(), Instant.now());
        accumulator.startTicker(session);
        stats.put(callId, accumulator);

        var callKey = new byte[CALL_KEY_LENGTH];
        random.nextBytes(callKey);
        callKeys.put(callId, callKey.clone());
        LOGGER.log(System.Logger.Level.INFO,
                "Generated call key for " + callId + " hex=" + java.util.HexFormat.of().formatHex(callKey));

        var ack = messageService.sendCall(peer, callId, callKey, options.videoEnabled());
        ack.relay().ifPresent(relay -> bringUpMediaPlane(session, relay, true));

        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActiveCall placeGroupCall(java.util.Set<Jid> peers, Jid groupJid, CallOptions options) {
        Objects.requireNonNull(peers, "peers cannot be null");
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        if (peers.isEmpty()) {
            throw new IllegalArgumentException("peers cannot be empty");
        }
        var selfJid = whatsapp.store().accountStore().jid()
                .orElseThrow(() -> new IllegalStateException("Not logged in"));
        var callId = CallIdGenerator.generate();
        var peerList = List.copyOf(peers);
        var primaryPeer = peerList.get(0);
        var session = new ActiveCall(this, callId, primaryPeer, groupJid, selfJid, true, true, options);
        activeCalls.put(callId, session);
        var accumulator = new CallStatsAccumulator(callId, CallSide.CALLER, options.videoEnabled(), Instant.now());
        accumulator.startTicker(session);
        stats.put(callId, accumulator);

        // A group call sends ONE offer to the group-call JID (<group>@call) carrying the group-jid and
        // a <group_info> of participants, with the shared call key encrypted to every participant
        // device; the server fans it out to the group's members.
        var callKey = new byte[CALL_KEY_LENGTH];
        random.nextBytes(callKey);
        callKeys.put(callId, callKey.clone());

        var ack = messageService.sendGroupCall(groupJid, peerList, callId, callKey, options.videoEnabled());
        ack.relay().ifPresent(relay -> bringUpMediaPlane(session, relay, true));
        return session;
    }

    /**
     * Drives the transport bring-up chain and instantiates the media session for one
     * {@link ActiveCall} after the relay-tokens block has been parsed.
     *
     * <p>Runs the entire connect chain on a virtual thread so the calling stanza handler
     * returns immediately:
     * <ol>
     *   <li>Build {@link ActiveCallTransport.StartParameters} from the relay-derived ICE
     *       credentials, a fresh self-signed {@link DtlsCertificate}, and
     *       {@link ActiveCallTransport#WA_PEER_DTLS_FINGERPRINT_SHA256}.</li>
     *   <li>Drive {@code transport.start}, then {@code connectRelay}, then
     *       {@code connectDtls} so the single shared
     *       {@link com.github.auties00.cobalt.call.internal.transport.dtls.DtlsSrtpDriver
     *       DtlsSrtpDriver} owned by the transport completes its handshake.</li>
     *   <li>Open the default SCTP data channel via
     *       {@code connectDataChannel(DEFAULT_SCTP_PORT, DEFAULT_SCTP_PORT)} so the same DTLS
     *       layer carries in-call signaling.</li>
     *   <li>Instantiate {@link VoiceCallSession} bound to the already-handshaked driver and
     *       start it; its completer wires the RTP and SRTP plumbing without re-running DTLS.</li>
     *   <li>Block until the session reports {@link VoiceCallSession#connected()}, attach the
     *       session to the call for lifecycle ownership, and flip the call to
     *       {@link CallState#ACTIVE} via {@link ActiveCall#notifyActive()}.</li>
     * </ol>
     *
     * @implNote This implementation consolidates the DTLS handshake on a single
     * {@link com.github.auties00.cobalt.call.internal.transport.dtls.DtlsSrtpDriver
     * DtlsSrtpDriver} per call: the relay allocation produces one UDP socket and the byte-0 demux in
     * {@code DtlsSrtpDriver} routes STUN, DTLS (application data for SCTP), and SRTP/SRTCP back to
     * the right consumer. This matches WA's wire architecture, which issues a single
     * {@code auth_token} + {@code relay_key} per call rather than one per logical stream.
     *
     * @param call          the call whose transport is being brought up
     * @param transportSpec the parsed relay-tokens block (from the offer ACK on the caller
     *                      side, or from the inbound offer on the callee side)
     * @param isCaller      {@code true} for the call placer (DTLS client), {@code false}
     *                      for the callee (DTLS server)
     */
    private void bringUpMediaPlane(ActiveCall call, CallRelay transportSpec,
                                   boolean isCaller) {
        if (!mediaPlaneStarted.add(call.callId())) {
            return;
        }
        Thread.ofVirtual()
                .name("call-bringup-" + call.callId())
                .start(() -> bringUpMediaPlane0(call, transportSpec, isCaller));
    }

    /**
     * Runs the transport + media-session bring-up steps documented on
     * {@link #bringUpMediaPlane(ActiveCall, CallRelay, boolean)} on the calling
     * thread.
     *
     * @param call          the call whose transport is being brought up
     * @param transportSpec the parsed relay-tokens block
     * @param isCaller      whether the local side placed the call
     */
    private void bringUpMediaPlane0(ActiveCall call, CallRelay transportSpec,
                                    boolean isCaller) {
        try {
            // The async bring-up vthread can lose a race with an inbound reject/timeout/terminate:
            // the receiver closes the call before this thread reaches connectRelay. Re-check the
            // registry and skip silently when the call is no longer active, instead of throwing.
            if (!activeCalls.containsKey(call.callId())) {
                return;
            }
            if (transportSpec.authTokens().isEmpty() || transportSpec.callKey().isEmpty()) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Cannot bring up call " + call.callId() + ": relay-tokens block is missing auth_token or key");
                return;
            }
            var authToken = transportSpec.authTokens().getFirst().bytes();
            var relayKey = transportSpec.callKey().orElseThrow();
            var iceCreds = IceCredentials.fromRelay(authToken, relayKey);
            var localCert = DtlsCertificate.generate();
            var params = new ActiveCallTransport.StartParameters(
                    iceCreds, localCert,
                    ActiveCallTransport.WA_PEER_DTLS_FINGERPRINT_SHA256,
                    isCaller, transportSpec);

            var transport = call.transport();
            var hbhKey = transportSpec.hbhKey().orElse(null);

            // The caller defers the ENTIRE relay bring-up (Allocate + DTLS + media) until the callee
            // answers. The relay only honours an Allocate once the call is accepted and the callee has
            // engaged its own side; an Allocate fired eagerly at offer-ACK (before <accept>) is
            // silently dropped by the relay, which is why media never came up. The callee already
            // holds the peer's accept (it just sent its own), so it runs inline. The 30-byte
            // hop-by-hop key from the <hbh_key> relay-block element keys the media SRTP and is known
            // now, independent of the relay handshake.
            Runnable bringUp = () -> {
                WaRelayConnector.Allocation allocation;
                try {
                    transport.start(params);
                    allocation = transport.connectRelay();
                } catch (IllegalStateException _) {
                    // Transport closed by an inbound reject/terminate while starting or allocating.
                    return;
                } catch (RuntimeException e) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "Relay Allocate failed for call " + call.callId() + ": " + e.getMessage());
                    return;
                }
                completeMediaPlane0(call, localCert, allocation, isCaller, hbhKey);
            };
            if (isCaller) {
                pendingMediaPlane.put(call.callId(), bringUp);
                if (call.peerAccepted()) {
                    runPendingMediaPlane(call.callId());
                }
                return;
            }
            bringUp.run();
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Transport bring-up failed for call " + call.callId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Runs the deferred half of the media bring-up: the peer-DTLS handshake, the SCTP data channel,
     * and the media session, after the relay has been allocated and (for the caller) the peer has
     * accepted.
     *
     * @param call       the call whose media plane is being completed
     * @param localCert  the self-signed DTLS certificate generated for this call
     * @param allocation the relay allocation produced by {@code connectRelay}
     * @param isCaller   whether the local side placed the call
     */
    private void completeMediaPlane0(ActiveCall call, DtlsCertificate localCert,
                                     WaRelayConnector.Allocation allocation, boolean isCaller,
                                     byte[] hbhKey) {
        try {
            if (!activeCalls.containsKey(call.callId())) {
                return;
            }
            var transport = call.transport();
            var role = isCaller ? SrtpRole.CLIENT : SrtpRole.SERVER;
            var voiceOpts = new VoiceCallOptions(
                    DEFAULT_LOCAL_AUDIO_SSRC,
                    DEFAULT_REMOTE_AUDIO_SSRC,
                    DEFAULT_OPUS_PAYLOAD_TYPE,
                    AudioPipelineOptions.defaults());

            // Hop-by-hop 1:1 media: the relay issued the SRTP keys in <hbh_key>, so the media plane
            // does not wait on a peer DTLS-SRTP handshake (the relay forwards SRTP and the peer P2P
            // path is disabled). Build the driver purely as the raw-SRTP byte transport over the relay
            // DataChannel and wire media immediately; skip the SCTP control channel, which the relayed
            // media path does not use.
            if (hbhKey != null) {
                DtlsSrtpDriver hbhDriver;
                try {
                    hbhDriver = transport.buildDtlsDriver();
                } catch (IllegalStateException _) {
                    return;
                }
                // Validate the relay path with an ICE connectivity check before pumping media: the
                // native Desktop client exchanges STUN binding checks (MI keyed by the relay <key>, no
                // USERNAME) with the relay over this same UDP socket, and the relay only forwards media
                // once the path is bound. The driver's RFC 7983 demux multiplexes STUN, DTLS, and SRTP.
                boolean iceBound = transport.connectIce(hbhDriver, ICE_CONNECTIVITY_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (!iceBound) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "ICE connectivity check did not succeed for call " + call.callId()
                                    + "; pumping media anyway");
                }
                if (call.isGroup()) {
                    // Group media routes through the SFU over the same hop-by-hop SRTP relay leg as a
                    // 1:1 call; per-participant end-to-end confidentiality (SFrame) layers on top.
                    var groupSession = new GroupCallSession(call, hbhDriver, voiceOpts);
                    groupSession.useHopByHopKey(hbhKey);
                    call.attachGroupSession(groupSession);
                    groupSession.start();
                    groupSession.awaitConnected(DTLS_HANDSHAKE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    if (groupSession.connected()) {
                        call.notifyActive();
                    }
                    return;
                }
                var voiceSession = new VoiceCallSession(call, hbhDriver, role, localCert,
                        ActiveCallTransport.WA_PEER_DTLS_FINGERPRINT_SHA256, voiceOpts);
                voiceSession.useHopByHopKey(hbhKey);
                call.attachVoiceSession(voiceSession);
                voiceSession.start();
                voiceSession.awaitConnected(DTLS_HANDSHAKE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (voiceSession.connected()) {
                    call.notifyActive();
                    maybeStartVideoTrack(call, voiceSession);
                }
                return;
            }

            try {
                transport.connectDtls(DTLS_HANDSHAKE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (IllegalStateException _) {
                // Transport closed by an inbound reject/terminate during the handshake.
                return;
            }
            try {
                transport.connectDataChannel(DEFAULT_SCTP_PORT, DEFAULT_SCTP_PORT);
            } catch (RuntimeException e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Data channel bring-up failed for call " + call.callId() + ": " + e.getMessage());
            }

            var driver = transport.dtlsDriver().orElseThrow(() ->
                    new IllegalStateException("connectDtls completed but driver is not exposed"));
            if (call.isGroup()) {
                // GroupCallSession owns its own DtlsSrtpDriver today; we hand it the relay's
                // datagram path and let it run a second handshake on top of the same UDP socket.
                // The consolidation (one driver per call, shared with the SCTP DataChannel) tracked
                // in Phase 4 covers VoiceCallSession only; the GroupCallSession variant lands next.
                var groupSession = new GroupCallSession(call, allocation.transport(),
                        role, localCert,
                        ActiveCallTransport.WA_PEER_DTLS_FINGERPRINT_SHA256,
                        voiceOpts);
                call.attachGroupSession(groupSession);
                groupSession.start();
                groupSession.awaitConnected(DTLS_HANDSHAKE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (groupSession.connected()) {
                    call.notifyActive();
                }
            } else {
                var voiceSession = new VoiceCallSession(call, driver, role, localCert,
                        ActiveCallTransport.WA_PEER_DTLS_FINGERPRINT_SHA256,
                        voiceOpts);
                voiceSession.useHopByHopKey(hbhKey);
                call.attachVoiceSession(voiceSession);
                voiceSession.start();
                voiceSession.awaitConnected(DTLS_HANDSHAKE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                if (voiceSession.connected()) {
                    call.notifyActive();
                }
            }
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Transport bring-up failed for call " + call.callId() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Starts a camera video track on the session when the call was set up with video enabled.
     *
     * <p>For an audio-and-video call the local camera track must start as soon as the media plane is up
     * so the peer receives the local video; a mid-call audio-to-video upgrade routes through
     * {@link #startLocalVideo(String)} instead. A failure to start the track is logged and swallowed so
     * a missing or busy camera does not tear down an otherwise-healthy audio call.
     *
     * @param call    the call
     * @param session the connected voice-call session
     */
    private void maybeStartVideoTrack(ActiveCall call, VoiceCallSession session) {
        if (!call.options().videoEnabled()) {
            return;
        }
        try {
            session.startVideoTrack(com.github.auties00.cobalt.call.session.VideoTrackOptions.defaults(
                    DEFAULT_LOCAL_VIDEO_SSRC, DEFAULT_REMOTE_VIDEO_SSRC));
        } catch (RuntimeException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Could not start video track for call " + call.callId() + ": " + e.getMessage());
        }
    }

    /**
     * Starts a camera video track mid-call, used by an accepted audio-to-video upgrade.
     *
     * <p>Looks up the call's attached voice session and starts a {@link com.github.auties00.cobalt.call.session.VideoTrackOptions.Kind#CAMERA}
     * track keyed on the session's already-negotiated SRTP endpoint. A no-op when the call is not
     * tracked or has no voice session yet.
     *
     * @param callId the call identifier
     */
    @Override
    public void startLocalVideo(String callId) {
        var call = activeCalls.get(callId);
        if (call == null) {
            return;
        }
        call.voiceSession().ifPresent(session -> {
            try {
                session.startVideoTrack(com.github.auties00.cobalt.call.session.VideoTrackOptions.defaults(
                        DEFAULT_LOCAL_VIDEO_SSRC, DEFAULT_REMOTE_VIDEO_SSRC));
            } catch (RuntimeException e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Could not start camera track for call " + callId + ": " + e.getMessage());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startScreenShare(String callId) {
        var call = activeCalls.get(callId);
        if (call == null) {
            return;
        }
        call.voiceSession().ifPresent(session -> {
            try {
                session.startScreenShare(com.github.auties00.cobalt.call.session.VideoTrackOptions.screenShareDefaults(
                        DEFAULT_LOCAL_SCREEN_SSRC, DEFAULT_REMOTE_SCREEN_SSRC));
            } catch (RuntimeException e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Could not start screen-share track for call " + callId + ": " + e.getMessage());
            }
        });
    }

    /**
     * Runs the parked caller-side media bring-up continuation for the given call, if one is
     * registered and not yet run.
     *
     * <p>The {@link ConcurrentHashMap#remove(Object) remove} is the run-once gate: only the caller
     * that pops a non-{@code null} continuation executes it, so a relay-allocation/accept race runs
     * the deferred steps exactly once. The continuation runs on its own virtual thread so neither
     * the stanza-handler thread nor the relay-allocation thread blocks on the DTLS handshake.
     *
     * @param callId the call identifier
     */
    private void runPendingMediaPlane(String callId) {
        var continuation = pendingMediaPlane.remove(callId);
        if (continuation != null) {
            Thread.ofVirtual().name("call-bringup-complete-" + callId).start(continuation);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActiveCall accept(IncomingCall offer, CallOptions options) {
        Objects.requireNonNull(offer, "offer cannot be null");
        Objects.requireNonNull(options, "options cannot be null");
        var session = new ActiveCall(
                this,
                offer.callId(),
                offer.peer(),
                offer.chatJid(),
                offer.peer(),
                false,
                offer.group(),
                options);
        activeCalls.put(offer.callId(), session);
        var accumulator = new CallStatsAccumulator(offer.callId(), CallSide.CALLEE, options.videoEnabled(), Instant.now());
        accumulator.startTicker(session);
        stats.put(offer.callId(), accumulator);
        if (offer.group()) {
            // A group callee joins by addressing <preaccept> then <accept> to the call MUC address
            // (<callId>@call), not to the creator device. When the offer carried no inline <relay>
            // (the native desktop caller omits it), the server responds to this join with a
            // <group_update> that carries the <relay> block, which the receiver feeds back here to
            // bring up the media plane.
            var callTarget = Jid.of(offer.callId() + "@call");
            // The captured real callee sends <accept> only after the server acks the <preaccept>;
            // firing both back-to-back makes the server see the accept before it registers the
            // preaccept and drop the join. Send the preaccept with sendNode so this blocks on the
            // matching <ack> (correlated by stanza id) before the accept goes out. Runs on a virtual
            // thread so the call-listener dispatch is not blocked; the bring-up that consumes the
            // relay arrives later in a <group_update>.
            Thread.ofVirtual().name("call-join-" + offer.callId()).start(() -> {
                try {
                    whatsapp.sendNode(CallStanza.preaccept(offer.peer(), offer.callId(), callTarget));
                } catch (RuntimeException e) {
                    LOGGER.log(System.Logger.Level.DEBUG,
                            "Group preaccept ack wait failed for " + offer.callId() + ": " + e.getMessage());
                }
                if (activeCalls.containsKey(offer.callId())) {
                    // Send the accept with sendNode as well: this injects the mandatory stanza id on
                    // the <call> envelope (sendNodeWithNoResponse leaves it absent, and the server
                    // silently drops an id-less call action so the join never completes) and blocks on
                    // the matching <ack>. The <group_update> carrying the relay follows the ack.
                    try {
                        whatsapp.sendNode(CallStanza.accept(offer.peer(), offer.callId(), callTarget));
                    } catch (RuntimeException e) {
                        LOGGER.log(System.Logger.Level.DEBUG,
                                "Group accept ack wait failed for " + offer.callId() + ": " + e.getMessage());
                    }
                }
            });
        } else {
            whatsapp.sendNodeWithNoResponse(CallStanza.accept(offer.peer(), offer.callId()).build());
        }
        if (offer.transportSpec().isPresent()) {
            bringUpMediaPlane(session, offer.transportSpec().get(), false);
        }
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reject(IncomingCall offer, CallEndReason reason) {
        Objects.requireNonNull(offer, "offer cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");
        whatsapp.sendNodeWithNoResponse(CallStanza.reject(offer.peer(), offer.callId()).build());
        whatsapp.store().chatStore().removeCall(offer.callId());
        for (var listener : whatsapp.store().listeners()) {
            if (listener instanceof LinkedCallEndedListener typed) {
                Thread.startVirtualThread(() ->
                        typed.onCallEnded(whatsapp, offer.callId(), offer.peer(), reason));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActiveCall find(String callId) {
        return callId == null ? null : activeCalls.get(callId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerAccept(String callId) {
        var session = find(callId);
        if (session != null) {
            session.onPeerAccept();
            // Set the accept flag (above) before draining the map: this orders against the
            // relay-allocation thread's put + peerAccepted() check so the deferred bring-up runs
            // exactly once regardless of which side wins the race.
            runPendingMediaPlane(callId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGroupRelay(String callId, CallRelay relay) {
        if (callId == null || relay == null) {
            return;
        }
        var session = activeCalls.get(callId);
        if (session == null || !session.isGroup()) {
            return;
        }
        bringUpMediaPlane(session, relay, false);
    }

    /**
     * {@inheritDoc}
     */
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerCandidates(String callId, java.util.List<java.net.InetSocketAddress> candidates) {
        if (callId == null || candidates == null || candidates.isEmpty()) {
            return;
        }
        var call = activeCalls.get(callId);
        if (call == null) {
            return;
        }
        peerCandidates.put(callId, java.util.List.copyOf(candidates));
        LOGGER.log(System.Logger.Level.INFO,
                "Peer ICE candidates for call " + callId + ": " + candidates);
        startP2pProbe(call);
    }

    /**
     * Advertises the local host candidate to the peer and listens for inbound peer-to-peer traffic.
     *
     * <p>WhatsApp's primary media path is direct P2P: the peer sends its candidates in the
     * {@code <accept>}/{@code <transport>} stanzas and runs STUN connectivity checks against the local
     * candidates it learns. This emits a symmetric {@code <transport>} stanza carrying the local LAN
     * host candidate and opens the matching UDP socket, logging every inbound datagram so the peer's
     * connectivity checks (and the MESSAGE-INTEGRITY keying they use) can be confirmed on the wire
     * before the full ICE agent and SRTP media plane are driven over the nominated pair.
     *
     * @param call the call to probe
     */
    private void startP2pProbe(ActiveCall call) {
        Thread.ofVirtual().name("p2p-probe-" + call.callId()).start(() -> {
            try (var channel = java.nio.channels.DatagramChannel.open()) {
                channel.bind(new java.net.InetSocketAddress(0));
                channel.configureBlocking(false);
                var port = ((java.net.InetSocketAddress) channel.getLocalAddress()).getPort();
                var peerAddrs = peerCandidates.getOrDefault(call.callId(), java.util.List.of());
                var lanIps = gatherLanIpv4(peerAddrs);
                if (lanIps.isEmpty()) {
                    LOGGER.log(System.Logger.Level.WARNING, "P2P probe: no LAN IPv4 to advertise");
                    return;
                }
                var localCandidates = lanIps.stream()
                        .map(ip -> new java.net.InetSocketAddress(ip, port))
                        .toList();
                whatsapp.sendNodeWithNoResponse(CallStanza.transport(
                        call.peer(), call.creator(), call.callId(),
                        localCandidates).build());
                LOGGER.log(System.Logger.Level.INFO,
                        "P2P probe: advertised local candidates " + localCandidates + " for call " + call.callId());

                // Drive the controlling-side ICE connectivity check: send STUN binding requests to the
                // peer's candidates, keyed (no USERNAME) with the call key, and route inbound STUN back
                // to the agent. The agent nominates the first pair whose check succeeds.
                var callKey = callKeys.get(call.callId());
                if (callKey == null) {
                    LOGGER.log(System.Logger.Level.WARNING, "P2P probe: no call key for " + call.callId());
                    return;
                }
                var credentials = new com.github.auties00.cobalt.call.internal.transport.ice.IceCredentials(
                        "cobalt", callKey, "peer", callKey);
                var lastSource = new java.util.concurrent.atomic.AtomicReference<java.net.SocketAddress>();
                var agent = new com.github.auties00.cobalt.call.internal.transport.ice.IceAgent(
                        true, credentials, (packet, destination) -> {
                    try {
                        var dst = destination != null ? destination : lastSource.get();
                        if (dst != null) {
                            channel.send(java.nio.ByteBuffer.wrap(packet), dst);
                        }
                    } catch (java.io.IOException _) {
                    }
                });
                agent.addLocalCandidate(com.github.auties00.cobalt.call.internal.transport.ice.IceCandidate.host(
                        com.github.auties00.cobalt.call.internal.transport.ice.IceComponent.RTP,
                        localCandidates.getFirst(), "host"));
                var i = 0;
                for (var peerAddr : peerAddrs) {
                    agent.addRemoteCandidate(com.github.auties00.cobalt.call.internal.transport.ice.IceCandidate.host(
                            com.github.auties00.cobalt.call.internal.transport.ice.IceComponent.RTP,
                            peerAddr, "peer" + (i++)));
                }
                agent.setListener(new com.github.auties00.cobalt.call.internal.transport.ice.IceAgent.Listener() {
                    @Override
                    public void onCheckSucceeded(com.github.auties00.cobalt.call.internal.transport.ice.IceCandidatePair pair) {
                        LOGGER.log(System.Logger.Level.INFO, "P2P ICE CHECK SUCCEEDED: " + pair.remote().transportAddress());
                    }

                    @Override
                    public void onNominated(com.github.auties00.cobalt.call.internal.transport.ice.IceCandidatePair pair) {
                        LOGGER.log(System.Logger.Level.INFO, "P2P ICE NOMINATED: " + pair.remote().transportAddress());
                    }
                });
                agent.start();
                var buffer = java.nio.ByteBuffer.allocate(2048);
                var deadlineNanos = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(20);
                var lastTick = 0L;
                while (System.nanoTime() < deadlineNanos && activeCalls.containsKey(call.callId())) {
                    buffer.clear();
                    var source = channel.receive(buffer);
                    if (source == null) {
                        if (System.nanoTime() - lastTick > 50_000_000L) {
                            agent.tick();
                            lastTick = System.nanoTime();
                        }
                        Thread.sleep(15);
                        continue;
                    }
                    lastSource.set(source);
                    buffer.flip();
                    var len = buffer.remaining();
                    var bytes = new byte[len];
                    buffer.get(bytes);
                    var b0 = bytes[0] & 0xFF;
                    LOGGER.log(System.Logger.Level.INFO,
                            "P2P inbound from " + source + " len=" + len + " b0=0x" + Integer.toHexString(b0)
                                    + " hex=" + java.util.HexFormat.of().formatHex(
                                            java.util.Arrays.copyOf(bytes, Math.min(len, 48))));
                    if (b0 <= 3) {
                        agent.handleInboundStun(bytes);
                    }
                }
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING, "P2P probe failed for call " + call.callId() + ": " + e);
            }
        });
    }

    /**
     * Gathers the site-local IPv4 addresses of up, non-loopback interfaces to advertise as local host
     * candidates, ordered so addresses sharing a {@code /24} prefix with a peer candidate come first.
     *
     * <p>A host commonly has several site-local IPv4 addresses (the real LAN NIC plus virtual adapters
     * such as Hyper-V or WSL); only the one on the peer's subnet is reachable, so all are advertised and
     * the peer-subnet match is ordered first.
     *
     * @param peerAddrs the peer's transport addresses, used to prefer the matching subnet
     * @return the local site-local IPv4 addresses, peer-subnet matches first
     */
    private static java.util.List<java.net.InetAddress> gatherLanIpv4(
            java.util.List<java.net.InetSocketAddress> peerAddrs) {
        var out = new java.util.ArrayList<java.net.InetAddress>();
        try {
            var nics = java.net.NetworkInterface.getNetworkInterfaces();
            while (nics != null && nics.hasMoreElements()) {
                var nic = nics.nextElement();
                if (!nic.isUp() || nic.isLoopback()) {
                    continue;
                }
                var addrs = nic.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    var addr = addrs.nextElement();
                    if (addr instanceof java.net.Inet4Address && addr.isSiteLocalAddress()) {
                        out.add(addr);
                    }
                }
            }
        } catch (java.net.SocketException _) {
        }
        out.sort(java.util.Comparator.comparingInt(addr -> sharesPeerSubnet(addr, peerAddrs) ? 0 : 1));
        return out;
    }

    /**
     * Returns whether the given local address shares a {@code /24} prefix with any peer candidate.
     *
     * @param local     the local address
     * @param peerAddrs the peer's transport addresses
     * @return {@code true} when a peer candidate is on the same {@code /24}
     */
    private static boolean sharesPeerSubnet(java.net.InetAddress local,
                                            java.util.List<java.net.InetSocketAddress> peerAddrs) {
        var localBytes = local.getAddress();
        for (var peer : peerAddrs) {
            var peerBytes = peer.getAddress().getAddress();
            if (peerBytes.length == 4 && localBytes.length == 4
                    && localBytes[0] == peerBytes[0] && localBytes[1] == peerBytes[1]
                    && localBytes[2] == peerBytes[2]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the peer's most recently reported ICE candidates for a call, or an empty list when none
     * have been received.
     *
     * @param callId the call identifier
     * @return the peer's transport addresses, highest priority first
     */
    java.util.List<java.net.InetSocketAddress> peerCandidates(String callId) {
        return peerCandidates.getOrDefault(callId, java.util.List.of());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerReject(String callId, String reason) {
        var session = find(callId);
        if (session != null) {
            session.onPeerEnded(reason);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerTerminate(String callId, String reason) {
        var session = find(callId);
        if (session != null) {
            session.onPeerEnded(reason);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregister(String callId) {
        var session = activeCalls.remove(callId);
        var accumulator = stats.remove(callId);
        pendingMediaPlane.remove(callId);
        peerCandidates.remove(callId);
        callKeys.remove(callId);
        if (whatsapp != null) {
            whatsapp.store().chatStore().removeCall(callId);
        }
        if (wamService == null || session == null || accumulator == null) {
            return;
        }
        emitFieldstatsEvent(session, accumulator);
    }

    /**
     * Builds and commits the WAM Call event for one ended call.
     *
     * <p>The event combines the accumulator's start-time dimensions with the
     * session's terminal {@link CallEndReason}, mapped to a
     * {@link CallResultType}, and is committed through {@link WamService}. Any
     * {@link RuntimeException} raised while building or committing is swallowed
     * so that telemetry never propagates a failure into the call path.
     *
     * @param session     the call that just ended
     * @param accumulator the per-call telemetry
     */
    private void emitFieldstatsEvent(ActiveCall session, CallStatsAccumulator accumulator) {
        try {
            accumulator.stopTicker();
            var endReason = session.endReason().orElse(CallEndReason.UNKNOWN);
            var connectedDuration = accumulator.connectedDurationSeconds();
            var builder = new CallEventBuilder()
                    .callRandomId(accumulator.callId())
                    .callSide(accumulator.side())
                    .callResult(mapToResultType(endReason))
                    .videoEnabled(accumulator.videoEnabled())
                    .videoEnabledAtCallStart(accumulator.videoEnabled())
                    .callOfferElapsedT(accumulator.startedAt());
            if (connectedDuration > 0) {
                builder.durationTSs(Instant.ofEpochSecond(connectedDuration));
            }
            wamService.commit(builder.build());
        } catch (RuntimeException _) {
        }
    }

    /**
     * Maps a {@link CallEndReason} to the {@link CallResultType} reported in
     * the WAM Call event.
     *
     * @implNote This implementation reports
     * {@link CallEndReason#ACCEPTED_ELSEWHERE} as
     * {@link CallResultType#CONNECTED}: it arrives as a peer-side terminate,
     * but from the local user's perspective the call did connect, just not on
     * this device.
     *
     * @param reason the canonical end reason
     * @return the matching WAM result type
     */
    private static CallResultType mapToResultType(CallEndReason reason) {
        return switch (reason) {
            case HANGUP -> CallResultType.CONNECTED;
            case TIMEOUT -> CallResultType.MISSED;
            case REJECT_DO_NOT_DISTURB, REJECT_BLOCKED -> CallResultType.REJECTED_BY_USER;
            case MIC_PERMISSION_DENIED, CAMERA_PERMISSION_DENIED -> CallResultType.SETUP_ERROR;
            case ACCEPTED_ELSEWHERE -> CallResultType.CONNECTED;
            case UNKNOWN -> CallResultType.INVALID;
        };
    }

    /**
     * Holds the per-call telemetry dimensions, mutated by a per-call ticker bound to the
     * {@link ActiveCall}'s lifecycle.
     *
     * <p>The accumulator captures the deterministic fields available when a call begins
     * ({@link #callId}, {@link #side}, {@link #videoEnabled}, {@link #startedAt}) and is mutated
     * thereafter by {@link #startTicker(ActiveCall)}, which spawns a virtual thread that polls the
     * call's state at {@link #TICK_INTERVAL_MS} cadence. The ticker stamps {@link #connectedAt} the
     * first time it observes {@link CallState#ACTIVE} and {@link #endedAt} when it observes
     * {@link CallState#ENDED}, then exits. {@link #stopTicker()} interrupts the thread on hangup.
     */
    static final class CallStatsAccumulator {
        /** Period of the per-call ticker, milliseconds. */
        private static final long TICK_INTERVAL_MS = 250;

        private final String callId;
        private final CallSide side;
        private final boolean videoEnabled;
        private final Instant startedAt;
        private volatile Instant connectedAt;
        private volatile Instant endedAt;
        private volatile Thread ticker;

        /**
         * Constructs a new accumulator.
         *
         * @param callId       the call identifier
         * @param side         which side initiated the call
         * @param videoEnabled whether video was enabled at call setup
         * @param startedAt    when the call was placed or accepted
         */
        CallStatsAccumulator(String callId, CallSide side, boolean videoEnabled, Instant startedAt) {
            this.callId = callId;
            this.side = side;
            this.videoEnabled = videoEnabled;
            this.startedAt = startedAt;
        }

        /** @return the call identifier */
        String callId() { return callId; }
        /** @return which side initiated the call */
        CallSide side() { return side; }
        /** @return whether video was enabled at call setup */
        boolean videoEnabled() { return videoEnabled; }
        /** @return when the call was placed or accepted */
        Instant startedAt() { return startedAt; }

        /**
         * Returns the connected duration in seconds, or zero when the call never reached
         * {@link CallState#ACTIVE} or {@link #endedAt} was not stamped before unregister.
         *
         * @return the connected duration in seconds
         */
        long connectedDurationSeconds() {
            var c = connectedAt;
            var e = endedAt;
            if (c == null) {
                return 0;
            }
            var end = e != null ? e : Instant.now();
            var dur = end.getEpochSecond() - c.getEpochSecond();
            return Math.max(0, dur);
        }

        /**
         * Starts the per-call ticker, which stamps {@link #connectedAt} when the call hits
         * {@link CallState#ACTIVE} and {@link #endedAt} when it hits {@link CallState#ENDED}.
         *
         * @param call the call to observe
         */
        void startTicker(ActiveCall call) {
            if (ticker != null) {
                return;
            }
            this.ticker = Thread.ofVirtual()
                    .name("call-stats-ticker-" + callId)
                    .start(() -> runTicker(call));
        }

        /**
         * Stops the ticker. Idempotent.
         */
        void stopTicker() {
            var t = ticker;
            if (t != null) {
                t.interrupt();
                ticker = null;
            }
            if (endedAt == null) {
                endedAt = Instant.now();
            }
        }

        /**
         * Polls the call's state until ENDED, stamping {@link #connectedAt} on the first ACTIVE
         * observation and {@link #endedAt} on ENDED.
         *
         * @param call the call to observe
         */
        private void runTicker(ActiveCall call) {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    var state = call.state();
                    if (connectedAt == null && state == CallState.ACTIVE) {
                        connectedAt = Instant.now();
                    }
                    if (state == CallState.ENDED) {
                        if (endedAt == null) {
                            endedAt = Instant.now();
                        }
                        return;
                    }
                    Thread.sleep(TICK_INTERVAL_MS);
                }
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendTerminate(Jid peer, Jid creator, String callId, CallEndReason reason) {
        whatsapp.sendNodeWithNoResponse(CallStanza.terminate(peer, creator, callId, reason).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMute(Jid peer, Jid creator, String callId, boolean muted) {
        whatsapp.sendNodeWithNoResponse(CallStanza.mute(peer, creator, callId, muted).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendVideoState(Jid peer, Jid creator, String callId, boolean enabled) {
        whatsapp.sendNodeWithNoResponse(CallStanza.videoState(peer, creator, callId, enabled).build());
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation expresses the request as an enabling video-state stanza and
     * delegates to {@link #sendVideoState(Jid, Jid, String, boolean)} so a subclass overriding
     * that one method intercepts the whole video-state family.
     */
    @Override
    public void sendVideoUpgradeRequest(Jid peer, Jid creator, String callId) {
        sendVideoState(peer, creator, callId, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendVideoUpgradeReject(Jid peer, Jid creator, String callId) {
        sendVideoState(peer, creator, callId, false);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation routes each interaction to the transport the live WhatsApp voip
     * engine uses for it. The raise-hand, lower-hand, and peer-mute interactions are server-relayed
     * {@code <call>} signaling stanzas (a {@code <user_action action="raise_hand">} wrapper and a
     * {@code <mute_v2 request-state>} payload respectively), captured live and sent via
     * {@link com.github.auties00.cobalt.client.LinkedWhatsAppClient#sendNode(NodeBuilder) sendNode} so
     * the server assigns an {@code id} and acknowledges them, mirroring the accept path. The reaction,
     * key-frame, and video-upgrade interactions ride the call's pre-negotiated DataChannel as
     * SRTP-protected packets, which only transmit once the media plane is up.
     */
    @Override
    public void sendInteraction(Jid peer, Jid creator, String callId, CallInteraction interaction) {
        Objects.requireNonNull(callId, "callId cannot be null");
        Objects.requireNonNull(interaction, "interaction cannot be null");
        var call = activeCalls.get(callId);
        if (call == null) {
            return;
        }
        var relayTarget = call.isGroup() ? Jid.of(callId + "@call") : peer;
        switch (interaction) {
            case CallInteraction.RaiseHand _ ->
                    sendCallActionStanza(CallStanza.raiseHand(relayTarget, creator, callId, true), callId);
            case CallInteraction.LowerHand _ ->
                    sendCallActionStanza(CallStanza.raiseHand(relayTarget, creator, callId, false), callId);
            case CallInteraction.PeerMuteRequest request -> {
                var muteTarget = Jid.of(request.target());
                sendCallActionStanza(CallStanza.peerMute(muteTarget, creator, callId), callId);
            }
            case CallInteraction.Reaction _, CallInteraction.KeyFrameRequest _,
                 CallInteraction.VideoUpgradeRequest _ -> sendDataPlaneInteraction(call, interaction);
        }
    }

    /**
     * Sends one server-relayed call-action signaling stanza on a virtual thread.
     *
     * <p>The stanza is dispatched through {@link com.github.auties00.cobalt.client.LinkedWhatsAppClient#sendNode(NodeBuilder)}
     * so the server assigns its {@code id} and the call returns the acknowledgement; the send runs on
     * a virtual thread so a caller invoking {@link ActiveCall#raiseHand()} or
     * {@link ActiveCall#requestPeerMute(String)} never blocks on the round trip.
     *
     * @param stanza the {@code <call>} stanza builder
     * @param callId the call identifier, used only to name the worker thread
     */
    private void sendCallActionStanza(NodeBuilder stanza, String callId) {
        Thread.ofVirtual().name("call-action-" + callId).start(() -> {
            try {
                whatsapp.sendNode(stanza);
            } catch (RuntimeException ignored) {
                // best-effort: a dropped action ack does not invalidate the call
            }
        });
    }

    /**
     * Sends a data-plane interaction over the call's pre-negotiated DataChannel.
     *
     * <p>The interaction is encoded by {@link CallInteractionEncoder} and SRTP-protected before being
     * written to the channel. It is a no-op when the media plane has not been brought up (no
     * DataChannel or no SRTP endpoint yet), because these interactions only have meaning once media is
     * flowing.
     *
     * @param call        the active call
     * @param interaction the interaction to encode and send
     */
    private void sendDataPlaneInteraction(ActiveCall call, CallInteraction interaction) {
        var transport = call.transport();
        var channelOpt = transport.dataChannel();
        var srtpOpt = transport.srtp();
        if (channelOpt.isEmpty() || srtpOpt.isEmpty()) {
            return;
        }
        var packet = CallInteractionEncoder.encode(interaction, transport.interactionStreamState());
        var ciphertext = srtpOpt.get().protectRtp(packet);
        channelOpt.get().send(ciphertext);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation decrypts the Signal envelope through {@link MessageDecryption}
     * and parses the plaintext as {@link E2eRekeyPayload}. The parsed bundle is then handed to
     * {@link ActiveCall#applyRekey(E2eRekeyPayload)}, which both caches it for inspection and
     * delegates to the attached media-plane session so the new per-domain master keys land on the
     * shared {@link com.github.auties00.cobalt.call.internal.rtp.srtp.SrtpEndpoint SrtpEndpoint} via
     * {@link com.github.auties00.cobalt.call.internal.rtp.srtp.SrtpEndpoint#rotateMasterKey(byte[]) rotateMasterKey}.
     */
    @Override
    public void onEncRekey(String callId, Jid senderJid, MessageEncryptionType encType, byte[] ciphertext) {
        var call = activeCalls.get(callId);
        if (call == null) {
            return;
        }
        try {
            var plaintext = messageService.processCall(senderJid, encType, ciphertext);
            var rekey = E2eRekeyPayloadSpec.decode(plaintext);
            call.applyRekey(rekey);
        } catch (RuntimeException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "enc_rekey decode failed for call " + callId + ": " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyEnded(String callId, Jid fromJid, String wireReason) {
        var parsed = CallEndReason.fromWireValue(wireReason);
        for (var listener : whatsapp.store().listeners()) {
            if (listener instanceof LinkedCallEndedListener typed) {
                Thread.startVirtualThread(() -> typed.onCallEnded(whatsapp, callId, fromJid, parsed));
            }
        }
    }
}
