package com.github.auties00.cobalt.calls.engine;

import com.github.auties00.cobalt.calls.config.CallsFeatureGate;
import com.github.auties00.cobalt.calls.engine.control.LiveCallLinkIqSender;
import com.github.auties00.cobalt.calls.crypto.LiveCallKeyExchange;
import com.github.auties00.cobalt.calls.crypto.CallKeyExchange;
import com.github.auties00.cobalt.calls.platform.audio.LiveAudioCaptureDriver;
import com.github.auties00.cobalt.calls.platform.audio.LiveAudioPlaybackDriver;
import com.github.auties00.cobalt.calls.platform.LiveVoipHostApi;
import com.github.auties00.cobalt.calls.platform.VoipDriverManager;
import com.github.auties00.cobalt.calls.platform.VoipHostApi;
import com.github.auties00.cobalt.calls.stream.VideoOutput;
import com.github.auties00.cobalt.client.linked.LinkedWhatsAppClient;
import com.github.auties00.cobalt.device.DeviceService;
import com.github.auties00.cobalt.message.MessageService;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryption;
import com.github.auties00.cobalt.model.call.Call;
import com.github.auties00.cobalt.model.call.CallEndReason;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.stanza.StanzaBuilder;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.store.linked.LinkedWhatsAppAccountStore;
import com.github.auties00.cobalt.store.linked.LinkedWhatsAppStore;

import java.nio.channels.DatagramChannel;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import com.github.auties00.cobalt.calls.engine.info.CallInfoManager;
import com.github.auties00.cobalt.calls.engine.info.CallInfoUpdater;
import com.github.auties00.cobalt.calls.engine.info.LiveCallInfoUpdater;
import com.github.auties00.cobalt.calls.engine.event.CallLifecycleEventSink;
import com.github.auties00.cobalt.calls.engine.event.LiveCallLifecycleEventSink;
import com.github.auties00.cobalt.calls.engine.state.CallLifecycleState;
import com.github.auties00.cobalt.calls.engine.timer.CallTimerKind;
import com.github.auties00.cobalt.calls.engine.timer.CallTimerScheduler;
import com.github.auties00.cobalt.calls.engine.timer.CallTimers;
import com.github.auties00.cobalt.calls.engine.timer.LiveCallTimerScheduler;
import com.github.auties00.cobalt.calls.engine.event.LiveCallEventBus;
import com.github.auties00.cobalt.calls.engine.state.CallStateMachine;
import com.github.auties00.cobalt.calls.engine.mediaplane.LiveMediaSession;
import com.github.auties00.cobalt.calls.engine.mediaplane.LiveMediaDatagramSink;
import com.github.auties00.cobalt.calls.engine.event.CallEvent;
import com.github.auties00.cobalt.calls.engine.mediaplane.MediaPlane;
import com.github.auties00.cobalt.calls.engine.context.CallContext;
import com.github.auties00.cobalt.calls.engine.context.CallContextRegistry;
import com.github.auties00.cobalt.calls.engine.context.CallManager;
import com.github.auties00.cobalt.calls.engine.context.LiveCallContextRegistry;

/**
 * Assembles a live {@link LifecycleController} from a client's call engine units.
 *
 * <p>The lifecycle controller depends only on its nine collaborating seams; this assembler is the one
 * place that binds each seam to the concrete unit that backs it for a live client, so the call service can
 * hold a fully wired engine without knowing how a transition, a timer, or an event reaches its unit. It
 * composes the units a live client already owns: the offer ack send rides the client's request correlated
 * {@link LinkedWhatsAppClient#sendNode(StanzaBuilder) sendNode}, the call key crypto is the
 * {@link LiveCallKeyExchange} facade over the reused Signal pipeline, signaling egress and randomness come
 * from {@link LiveVoipHostApi}, the at most two call contexts live in a single {@link CallManager}, the
 * transition guard is the {@link CallStateMachine} over that manager, the per call timers run on
 * {@link CallTimers} virtual thread drivers, and the call info snapshots refresh through a single
 * {@link CallInfoManager}.
 *
 * <p>The engine is fully live across every seam. The signaling, ringing, state, timer, and listener path
 * sequences a call end to end (an inbound offer rings and reaches the host, an accept, reject, preaccept,
 * and terminate ship on the socket, the state machine drives the call, and a fired watchdog or lonely
 * timeout tears the call down with the correct {@link CallEndReason}), and the {@link MediaPlane} is
 * the real {@link LiveMediaSession.LiveMediaPlane}: once a call is answered it brings up the relay
 * transport from the relay block and the call key, runs the Opus encode and decode pipeline over the call's
 * application capture and playback streams (a microphone bound or speaker bound stream carries its platform
 * device behind the same interface, and a call that supplied no stream falls back to opening a platform
 * device), and ships and receives media as hop by hop SRTP carried as SCTP DATA over the call's one
 * DTLS wrapped SCTP data channel; the call's host {@link DatagramChannel} carries only the ICE connectivity
 * checks and DTLS records of that channel's bring up, not raw media. The media plane reports its first
 * traffic back to the controller through a connection sink bound after the controller is built, so the call
 * advances to {@link CallLifecycleState#CALL_ACTIVE} when media flows. A media plane bring up failure surfaces
 * as a non fatal call exception the controller isolates to that one call.
 *
 * @apiNote This is an internal engine assembler, not a public surface; the call service is its only caller.
 * @implNote This implementation binds the engine's host boundary: the {@link CallContextRegistry}
 * adopts a {@link CallContext} built from the controller's {@link Call} (with the controller's call
 * id, so the {@link CallStateMachine} resolves the same context by id) into the manager, choosing the
 * primary slot first and the secondary slot when a primary call is already live; the
 * {@link CallTimerScheduler} owns one {@link CallTimers} driver per call id, mapping each
 * {@link CallTimerKind} to the matching {@link CallTimers.Timer}; the {@link CallInfoUpdater}
 * refreshes the single {@link CallInfoManager} from the resolved context's accumulated durations; and
 * the {@link CallLifecycleEventSink} logs each gated lifecycle id. The typed host facing listener events the
 * application observes ({@code onCall}, {@code onCallEnded}, and the in call control events) are fanned out
 * by the call service and the in call control units through {@link LiveCallEventBus}, not by this opaque
 * sink, because the per event payload byte layout that would let this sink reconstruct the typed
 * {@link CallEvent} is not yet recovered.
 */
public final class EngineAssembler {
    /**
     * Hidden constructor; this assembler exposes only its static factory.
     */
    private EngineAssembler() {
        throw new AssertionError("No instances");
    }

    /**
     * Builds the engine's single {@link VoipDriverManager} over fresh platform capture and playback drivers
     * and the default camera and screen share source factories.
     *
     * <p>The manager owns the two audio capture drivers (microphone and system audio loopback), the audio
     * playback driver, and the camera and screen share video source factories for the lifetime of the
     * engine; each brought up media session routes its capture and playback bring up through this one
     * manager rather than opening a device directly. The audio drivers back onto {@code javax.sound}
     * lines and the video factories open the platform default camera or screen as a {@link VideoOutput},
     * so a host without the corresponding device fails at capture start time, which the media session
     * isolates, rather than at engine assembly. The returned manager is not yet
     * {@linkplain VoipDriverManager#initialize() initialized}; the caller initializes it once.
     *
     * @return a fresh, uninitialized driver manager owning the engine's capture and playback drivers
     */
    private static VoipDriverManager newVoipDriverManager() {
        return new VoipDriverManager(
                new LiveAudioCaptureDriver(),
                new LiveAudioCaptureDriver(),
                new LiveAudioPlaybackDriver(),
                deviceId -> VideoOutput.fromCamera(),
                surfaceId -> VideoOutput.fromScreen());
    }

    /**
     * Assembles a live {@link LifecycleController} for a client over its call engine units.
     *
     * <p>Builds the call key crypto facade over the supplied Signal pipeline, a {@link LiveVoipHostApi}
     * whose signaling rides the client and whose {@code call_sendto} host datagram seam is a real
     * {@link DatagramChannel}, the
     * engine's single {@link VoipDriverManager} (initialized once here, owning the audio capture and
     * playback drivers and the camera and screen share source factories every brought up media session
     * routes its capture and playback through), the real {@link LiveMediaSession.LiveMediaPlane} over that
     * host and driver manager, a {@link CallManager} with its {@link CallStateMachine} and
     * {@link CallInfoManager}, and the per call timer scheduler, then threads them into the controller
     * as its nine seams, binds the media plane's connection sink to the controller's
     * {@link LifecycleController#onMediaConnected(String) media-connected} entry point, binds a
     * {@link CallsFeatureGate} over the client's AB props service onto the controller so the start,
     * group start, and screen share entry points are gated on the server calling feature flags, and binds a
     * store backed own device predicate onto the controller so the companion device terminate guard can tell
     * a terminate authored by another device of the local account from one authored by the remote peer. The
     * same gate supplies the media plane's group call initial BWE seed decision
     * ({@link CallsFeatureGate#isInitBweForGroupCallEnabled()}), read per bring up.
     *
     * @param whatsapp          the owning client, used for signaling egress and the offer ack round trip
     * @param messageEncryption the encryption service the call key crypto wraps the key with
     * @param messageService    the message service the call key crypto decrypts inbound key envelopes with
     * @param deviceService     the device service the call key crypto ensures sessions through
     * @param store             the store supplying the local ADV signed device identity
     * @param abPropsService    the AB props service the {@link CallsFeatureGate} reads its calling feature
     *                          flags from, bound onto the controller so the start, group start, and
     *                          screen share entry points are gated
     * @param eventBus          the shared host event bus the in call control units publish their host facing
     *                          events onto, bound onto the controller so an answerable call gets its control
     *                          units
     * @return a fully wired lifecycle controller
     * @throws NullPointerException if any argument is {@code null}
     */
    public static LifecycleController assemble(LinkedWhatsAppClient whatsapp,
                                                     MessageEncryption messageEncryption,
                                                     MessageService messageService,
                                                     DeviceService deviceService,
                                                     LinkedWhatsAppStore store,
                                                     ABPropsService abPropsService,
                                                     LiveCallEventBus eventBus) {
        Objects.requireNonNull(whatsapp, "whatsapp cannot be null");
        Objects.requireNonNull(messageEncryption, "messageEncryption cannot be null");
        Objects.requireNonNull(messageService, "messageService cannot be null");
        Objects.requireNonNull(deviceService, "deviceService cannot be null");
        Objects.requireNonNull(store, "store cannot be null");
        Objects.requireNonNull(abPropsService, "abPropsService cannot be null");
        Objects.requireNonNull(eventBus, "eventBus cannot be null");

        var secureRandom = new SecureRandom();
        CallKeyExchange crypto =
                new LiveCallKeyExchange(messageEncryption, messageService, deviceService, store, secureRandom);
        var manager = new CallManager();
        var stateMachine = new CallStateMachine(manager);
        var infoManager = new CallInfoManager();
        var events = new LiveCallLifecycleEventSink();
        VoipHostApi host = new LiveVoipHostApi(whatsapp, new LiveMediaDatagramSink(), frame -> {
        }, (eventType, payload) -> {
        });

        var selfJid = new AtomicReference<>(ownLidDeviceJid(store).orElse(null));
        var connectionSink = new AtomicReference<Consumer<String>>();
        var voipDriverManager = newVoipDriverManager();
        voipDriverManager.initialize();
        // Built once and shared: this gate backs the controller's start, group start, and screen share
        // gating and supplies the group call initial BWE seed decision the media plane threads into each
        // group call's rate control loop, evaluated per call bring up so the AB props cache is warm when a
        // call starts rather than read on a cold cache here at assembly.
        var featureGate = new CallsFeatureGate(abPropsService);
        var mediaPlane = new LiveMediaSession.LiveMediaPlane(host, selfJid, connectionSink, voipDriverManager,
                featureGate::isInitBweForGroupCallEnabled);

        var timers = new LiveCallTimerScheduler(manager, events, featureGate);
        var controller = new LifecycleController(
                new LiveOfferAckSender(whatsapp),
                crypto,
                host,
                new LiveCallContextRegistry(manager, timers),
                stateMachine,
                timers,
                new LiveCallInfoUpdater(manager, infoManager),
                events,
                mediaPlane);
        timers.bindController(controller);
        timers.bindGroupOutboundResolver(controller::groupOutbound);
        connectionSink.set(controller::onMediaConnected);
        controller.bindEventBus(eventBus, () -> store.accountStore().jid().orElse(null));
        controller.bindFeatureGate(featureGate);
        controller.bindOwnDeviceResolver(deviceJid -> isOwnDevice(store, deviceJid));
        controller.bindCallLinkIqSender(new LiveCallLinkIqSender(whatsapp));
        return controller;
    }

    /**
     * Reports whether a device JID is one of the local account's own devices for the companion device
     * terminate guard.
     *
     * <p>A device JID belongs to the local account when it resolves to the same account as the account's
     * own phone number JID or LID, or when it appears in the account's linked device list; the
     * account equality test normalizes the device and agent suffixes away so a device JID
     * ({@code user:device@server}) matches the bare account JID. The store is read live on each call so a
     * self JID, LID, or linked device set that became known after assembly is picked up without rebinding.
     *
     * @implNote This implementation treats a terminate authored by another device of the local account as a
     * companion terminate. The own account match runs {@link Jid#isSameAccount(Jid)} against both the phone
     * number JID and the LID, because a call's signaling sender may use either addressing mode, and falls
     * back to the linked device list as the explicit enumeration of the account's companions when the self
     * identity is not yet populated.
     *
     * @param store     the store whose account store supplies the local identity and device list
     * @param deviceJid the device JID to test, never {@code null}
     * @return {@code true} when {@code deviceJid} is one of the local account's own devices
     */
    private static boolean isOwnDevice(LinkedWhatsAppStore store, Jid deviceJid) {
        var accountStore = store.accountStore();
        if (accountStore.jid().map(deviceJid::isSameAccount).orElse(false)
                || accountStore.lid().map(deviceJid::isSameAccount).orElse(false)) {
            return true;
        }
        for (var linkedDevice : accountStore.linkedDevices()) {
            if (deviceJid.isSameAccount(linkedDevice)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves the local account's own LID device JID, the {@code <lid>:<device>@lid} form the call media
     * plane keys its SSRCs and SFrame keys on.
     *
     * <p>The media plane derives the deterministic per device media SSRCs and the per participant SFrame base
     * key from the self device JID, keyed on the participant's {@code <lid>:<device>@lid} device JID, the form
     * {@link com.github.auties00.cobalt.calls.engine.participant.CallSecureSsrcGenerator} stamps. This
     * composes that JID from the account LID (the user part and the {@code @lid} domain) and the device
     * number carried on the account phone number JID, so a peer that pre registers a receive context for the
     * self device recognises the stamped SSRCs. When the session is not yet LID paired (no account LID) this
     * falls back to the account phone number JID so a degenerate pre LID session still seeds a JID; the media
     * plane's own random layout fallback covers a fully unseeded holder.
     *
     * @implNote This implementation keys the media plane on the LID device JID rather than the account phone
     * number JID: the SSRC and SFrame key derivations both run on the device's {@code <lid>:<device>@lid}
     * JID, so the self device JID must be the LID device form for the stamped SSRCs and the SFrame base key
     * to match. The device number is read from the account JID because the account LID is stored in its bare
     * user form; combining the LID user with that device number yields the device JID. The result seeds the
     * media plane's self JID holder, which the plane reads at each call bring up rather than capturing here.
     *
     * @param store the store whose account store supplies the LID and the device number
     * @return the own LID device JID, or the account phone number JID when no LID is set, or empty when the
     *         account is not yet paired
     */
    private static Optional<Jid> ownLidDeviceJid(LinkedWhatsAppStore store) {
        var accountStore = store.accountStore();
        var lid = accountStore.lid().orElse(null);
        if (lid == null) {
            return accountStore.jid();
        }
        var device = accountStore.jid().map(Jid::device).orElse(0);
        return Optional.of(lid.withServer(JidServer.lid()).withDevice(device));
    }
}
