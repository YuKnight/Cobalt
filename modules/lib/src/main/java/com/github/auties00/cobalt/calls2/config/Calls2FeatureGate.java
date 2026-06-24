package com.github.auties00.cobalt.calls2.config;

import com.github.auties00.cobalt.model.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;

import java.util.Objects;

/**
 * Typed read facade over the server AB-props that gate wa-voip calling behaviour.
 *
 * <p>WhatsApp ships its calling capabilities behind server-pushed feature flags rather than
 * a static client build: each capability (one-to-one calling, group calling, call links,
 * screen share), each negotiated protocol version (admin control, LID addressing, audio
 * sharing, the rust-migration bitmap), and each engine implementation selector (the WASM
 * variant, the video and audio capture/render/playback impls) is keyed by a numeric AB-prop
 * the relay syncs over the binary-XMPP socket. The calls2 engine reads these flags to decide
 * whether to spin up at all, what to advertise in an {@code <offer>}, and which protocol
 * versions to negotiate.
 *
 * <p>This facade does not own any flag storage. It holds the same {@link ABPropsService}
 * instance the {@code LinkedWhatsAppClient} already owns and forwards each read to that
 * service's typed accessors keyed by the matching {@link ABProp} constant; the service caches
 * the synced values, applies the production-vs-beta default, and coerces the raw string. The
 * gate adds no {@link ABProp} constants and no persisted state: the constant catalogue already
 * enumerates every calling, group-call, call-link, screen-share, and engine-selector flag, and
 * {@code SyncStore} already persists the AB-props sync metadata. Boolean predicates return the
 * coerced flag; version and bitmap reads return the integer the relay synced (or the prop
 * default, ultimately {@code 0}); the WASM-variant read returns the raw string selector.
 *
 * <p>Reads block on the first AB-props sync by default, matching {@link ABPropsService}'s
 * {@code waitForSync = true} accessors, so a gate query made before the cache is warm waits up
 * to the service's sync timeout and then falls back to the prop default. The per-call
 * {@code <voip_settings>} parameter bundle is a separate engine-side channel (see
 * {@link VoipSettings}); it is never routed through this gate.
 *
 * @apiNote This is the single seam through which calls2 reads calling feature flags; engine
 * components take an injected {@code Calls2FeatureGate} rather than reaching for
 * {@link ABPropsService} directly, so the call-relevant flag subset is named once here instead
 * of scattering raw {@link ABProp} constants across the call path.
 * @implNote This implementation reads the host-side {@code WAWebABPropsConfigs} feature flags
 * that the wa-voip JS layer consults before offering or advertising a capability; the
 * {@code p->}/{@code vp->}/{@code mvp->} engine booleans the WASM toggles are deliberately not
 * read here because they are fields of the per-call {@code <voip_settings>} bundle, not server
 * AB-props (re/calls2-spec/SPEC.md sec 9.4; re/calls2-spec/parts/int-abprops.json).
 *
 * <p>Consumption map of the gate methods in the calls2 engine, recorded here so a reader can tell a
 * deliberately read-only selector from an unwired one:
 * <ul>
 *   <li><b>Consumed at a behaviour site:</b> {@link #isWebCallingEnabled()},
 *       {@link #isWebGroupCallingEnabled()}, {@link #isGroupCallsEnabled()},
 *       {@link #groupCallMaxParticipants()}, and {@link #isScreenShareEnabled()} gate the call-start,
 *       group-call-start, and screen-share entry points on {@code Calls2LifecycleController};
 *       {@link #screenShareMilestoneVersion()} selects the screen-share protocol version the in-call
 *       {@code ScreenShareController} advertises; {@link #callingLidVersion()},
 *       {@link #isPhoneNumberPrivacyEnabled()}, and {@link #isCallingUsernameEnabled()} select the
 *       {@code caller_pn} and {@code username} identity hints the {@code <offer>} carries; and
 *       {@link #isInitBweForGroupCallEnabled()} seeds the group-call initial bandwidth estimate in the
 *       media plane's rate-control loop; and {@link #heartbeatIntervalSeconds()} sets the cadence the
 *       engine assembler arms a placed group call's membership-heartbeat timer at.</li>
 *   <li><b>Owned by other engine pieces:</b> {@link #isCallLinkEnabled()} gates the call-link join path,
 *       and {@link #isIgnoreOneToOneTerminateInGroupCall()} and
 *       {@link #isIgnoreJoinableTerminateOnExpiredOffer()} gate the terminate guards; those flags are
 *       consumed where those features are built, not here.</li>
 *   <li><b>Intentionally read-only config with no Java analog:</b> {@link #callingRustMigrationBitmap()},
 *       {@link #callPerfOptimizationsBitmask()}, {@link #wasmVariant()}, {@link #videoCaptureImpl()},
 *       {@link #audioCaptureImpl()}, {@link #audioPlaybackImpl()}, and {@link #videoRendererImpl()}
 *       select native and WASM implementation variants the WASM engine downloads or links; Cobalt binds
 *       one Java implementation per role statically (the {@code VoipDriverManager} is built with concrete
 *       injected drivers, not an impl-variant menu) and its codecs and engine are bundled, so these
 *       choose nothing in the Java reimplementation. {@link #dynamicThreadPreallocateCount()} preallocates
 *       a native worker pool; Cobalt runs each media pump on an on-demand Project Loom virtual thread with
 *       no fixed pool to size. {@link #callAdminVersion()} negotiates a group-call admin-control protocol,
 *       {@link #callingAudioShareVersion()} an in-call audio-sharing protocol, and
 *       {@link #isCoexCallingEnabled()} the linked-while-primary coexistence mode; none of those protocols
 *       is built in the Java engine, so there is no version or capability field for them to feed.
 *       {@link #isVoipStackMessageOwnershipTransferEnabled()} chooses whether the host message plane hands
 *       an inbound call stanza to the voip stack rather than the message path; Cobalt's signaling receiver
 *       always routes call stanzas to the call engine, so there is no ownership hand-off to toggle. These
 *       are read so the typed surface stays complete and a future Java site can consult them; they are not
 *       dead code and must not be deleted.</li>
 *   <li><b>No Java consumption site yet (gap, not by design):</b>
 *       {@link #isCallResultFixFor404AcceptNackEnabled()} selects which call result a {@code 404} NACK on an
 *       outbound {@code accept} maps to. The accept ACK is now consumed
 *       ({@code Calls2LifecycleController.handleIncomingAck} and {@code LiveCalls2Service.handleInboundAck}),
 *       and {@code Calls2CallResult.fromAcceptAckError} maps {@code 404} to
 *       {@code Calls2CallResult.CALL_DOES_NOT_EXIST_FOR_REJOIN} (the fix-enabled result), but it does so
 *       unconditionally and never consults this flag, so with the flag disabled (its production default) the
 *       legacy result is not selected. The flag therefore still gates nothing; threading it into
 *       {@code fromAcceptAckError} so {@code 404} resolves to the legacy result when the flag is off remains
 *       a gap. Confirming the exact legacy (flag-disabled) result code needs the {@code handle_accept_ack}
 *       (fn11502) branch behind {@code enable_call_result_fix_for_404_accept_nack}, which is host/native-side
 *       and absent from the wa-voip WASM strings.</li>
 * </ul>
 */
public final class Calls2FeatureGate {
    /**
     * The AB-props service whose cached flags back every read.
     *
     * <p>This is the same {@link ABPropsService} instance the owning {@code LinkedWhatsAppClient}
     * uses for all other AB-prop reads; the gate never creates a second service or cache.
     */
    private final ABPropsService abPropsService;

    /**
     * Constructs a feature gate backed by the given AB-props service.
     *
     * @param abPropsService the AB-props service whose cached flags back every read
     * @throws NullPointerException if {@code abPropsService} is {@code null}
     */
    public Calls2FeatureGate(ABPropsService abPropsService) {
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService cannot be null");
    }

    /**
     * Returns whether one-to-one web calling is enabled for this account.
     *
     * <p>This is the master gate for the web/desktop one-to-one call surface: when it is
     * {@code false} the engine offers and advertises no one-to-one calling capability. Reads
     * {@link ABProp#ENABLE_WEB_CALLING}.
     *
     * @return {@code true} when one-to-one web calling is enabled
     */
    public boolean isWebCallingEnabled() {
        return abPropsService.getBool(ABProp.ENABLE_WEB_CALLING);
    }

    /**
     * Returns whether web group calling is enabled for this account.
     *
     * <p>This is the master gate for the web group-call surface, which also covers the
     * call-link join path because joining a call link enters a group call. When it is
     * {@code false} the engine neither starts nor joins group calls. Reads
     * {@link ABProp#ENABLE_WEB_GROUP_CALLING}.
     *
     * @return {@code true} when web group calling is enabled
     */
    public boolean isWebGroupCallingEnabled() {
        return abPropsService.getBool(ABProp.ENABLE_WEB_GROUP_CALLING);
    }

    /**
     * Returns whether group calls are available, the semantic gate the call path checks before
     * starting or joining a multi-party call.
     *
     * <p>Group calling requires both the one-to-one calling master gate and the group-calling
     * master gate, because the group call path builds on the same engine the one-to-one path
     * spins up; this predicate is the conjunction of {@link #isWebCallingEnabled()} and
     * {@link #isWebGroupCallingEnabled()}.
     *
     * @return {@code true} when group calls may be started or joined
     */
    public boolean isGroupCallsEnabled() {
        return isWebCallingEnabled() && isWebGroupCallingEnabled();
    }

    /**
     * Returns whether call links are available, the semantic gate the call path checks before
     * creating, querying, or joining a joinable call link.
     *
     * <p>A call link is a joinable entry point into a group call, so call-link support is gated
     * on group calling being available; this predicate is equivalent to
     * {@link #isGroupCallsEnabled()}. There is no separate host AB-prop that enables call links
     * independently of group calling.
     *
     * @return {@code true} when call links may be created or joined
     */
    public boolean isCallLinkEnabled() {
        return isGroupCallsEnabled();
    }

    /**
     * Returns whether in-call screen sharing is available.
     *
     * <p>Screen sharing is gated by a milestone version rather than a boolean: a positive
     * {@linkplain #screenShareMilestoneVersion() milestone version} means the capability is
     * enabled and screen sharing additionally requires the one-to-one calling master gate, since
     * the screen-share surface rides an active call. This predicate is {@code true} when
     * {@link #isWebCallingEnabled()} holds and the milestone version is greater than {@code 0}.
     *
     * @return {@code true} when in-call screen sharing may be started
     */
    public boolean isScreenShareEnabled() {
        return isWebCallingEnabled() && screenShareMilestoneVersion() > 0;
    }

    /**
     * Returns whether username-addressed calling is enabled.
     *
     * <p>When enabled the engine may place and accept calls addressed by username rather than by
     * phone number. Reads {@link ABProp#ENABLE_CALLING_USERNAME}.
     *
     * @return {@code true} when username-addressed calling is enabled
     */
    public boolean isCallingUsernameEnabled() {
        return abPropsService.getBool(ABProp.ENABLE_CALLING_USERNAME);
    }

    /**
     * Returns whether phone-number privacy is enabled for calling.
     *
     * <p>When enabled the engine withholds the caller's phone number where the calling-privacy
     * surface allows it. Reads {@link ABProp#ENABLE_CALLING_PHONE_NUMBER_PRIVACY}.
     *
     * @return {@code true} when calling phone-number privacy is enabled
     */
    public boolean isPhoneNumberPrivacyEnabled() {
        return abPropsService.getBool(ABProp.ENABLE_CALLING_PHONE_NUMBER_PRIVACY);
    }

    /**
     * Returns whether initial bandwidth estimation is enabled for group calls.
     *
     * <p>When enabled the engine seeds an initial BWE estimate when a group call starts rather
     * than ramping from cold. Reads {@link ABProp#ENABLE_INIT_BWE_FOR_GROUP_CALL}.
     *
     * @return {@code true} when initial group-call bandwidth estimation is enabled
     */
    public boolean isInitBweForGroupCallEnabled() {
        return abPropsService.getBool(ABProp.ENABLE_INIT_BWE_FOR_GROUP_CALL);
    }

    /**
     * Returns the group-call membership-heartbeat cadence, in seconds.
     *
     * <p>A placed group call arms its membership-heartbeat timer at this period; WhatsApp reads it from
     * this server setting at call bring-up rather than from a fixed build constant, which is why the
     * engine resolves it through this gate instead of a Java literal. Reads
     * {@link ABProp#HEARTBEAT_INTERVAL_S}, whose default is {@code 10}.
     *
     * @return the heartbeat interval, in seconds
     */
    public int heartbeatIntervalSeconds() {
        return abPropsService.getInt(ABProp.HEARTBEAT_INTERVAL_S);
    }

    /**
     * Returns whether inbound signaling messages are handed off into the voip stack.
     *
     * <p>When enabled the message plane transfers ownership of an inbound call signaling message
     * to the voip stack instead of processing it on the message path. Reads
     * {@link ABProp#VOIP_STACK_INCOMING_MESSAGE_OWNERSHIP_TRANSFER}.
     *
     * @return {@code true} when inbound signaling messages are handed off to the voip stack
     */
    public boolean isVoipStackMessageOwnershipTransferEnabled() {
        return abPropsService.getBool(ABProp.VOIP_STACK_INCOMING_MESSAGE_OWNERSHIP_TRANSFER);
    }

    /**
     * Returns whether a one-to-one {@code <terminate>} is ignored while a group call is active.
     *
     * <p>When enabled the engine drops a one-to-one terminate that arrives during a group call,
     * so a stale one-to-one leg does not tear down the group call. Reads
     * {@link ABProp#IGNORE_ONE_TO_ONE_TERMINATE_IN_GROUP_CALL}.
     *
     * @return {@code true} when a one-to-one terminate is ignored during a group call
     */
    public boolean isIgnoreOneToOneTerminateInGroupCall() {
        return abPropsService.getBool(ABProp.IGNORE_ONE_TO_ONE_TERMINATE_IN_GROUP_CALL);
    }

    /**
     * Returns whether a {@code <terminate>} is ignored for a joinable call link whose offer has
     * expired.
     *
     * <p>When enabled the engine drops a terminate for a joinable (call-link) offer that has
     * already expired, so a late terminate does not abort an otherwise valid join. Reads
     * {@link ABProp#IGNORE_JOINABLE_TERMINATE_ON_EXPIRED_OFFER}.
     *
     * @return {@code true} when a joinable terminate is ignored on an expired offer
     */
    public boolean isIgnoreJoinableTerminateOnExpiredOffer() {
        return abPropsService.getBool(ABProp.IGNORE_JOINABLE_TERMINATE_ON_EXPIRED_OFFER);
    }

    /**
     * Returns whether the call-result fix for a {@code 404} accept NACK is enabled.
     *
     * <p>When enabled the engine maps a {@code 404} NACK on an {@code accept} to the corrected
     * call result rather than the legacy one. Reads
     * {@link ABProp#ENABLE_CALL_RESULT_FIX_FOR_404_ACCEPT_NACK}.
     *
     * @return {@code true} when the {@code 404} accept-NACK call-result fix is enabled
     */
    public boolean isCallResultFixFor404AcceptNackEnabled() {
        return abPropsService.getBool(ABProp.ENABLE_CALL_RESULT_FIX_FOR_404_ACCEPT_NACK);
    }

    /**
     * Returns whether coexistence calling is enabled.
     *
     * <p>Coexistence calling lets a linked client place calls while a primary device is also
     * connected. This prop is off in production and on in the beta programme, so the resolved
     * value depends on whether the session is on WhatsApp Web Beta. Reads
     * {@link ABProp#COEX_CALLING_ENABLED}.
     *
     * @return {@code true} when coexistence calling is enabled
     */
    public boolean isCoexCallingEnabled() {
        return abPropsService.getBool(ABProp.COEX_CALLING_ENABLED);
    }

    /**
     * Returns the maximum number of participants allowed in a group call.
     *
     * <p>The engine caps group-call membership at this value. Reads
     * {@link ABProp#GROUP_CALL_MAX_PARTICIPANTS}, whose default is {@code 32}.
     *
     * @return the group-call participant ceiling
     */
    public int groupCallMaxParticipants() {
        return abPropsService.getInt(ABProp.GROUP_CALL_MAX_PARTICIPANTS);
    }

    /**
     * Returns the negotiated group-call admin-control protocol version.
     *
     * <p>The engine negotiates the admin-control protocol at this version; {@code 0} disables
     * admin control. Reads {@link ABProp#CALL_ADMIN_VERSION}.
     *
     * @return the admin-control protocol version
     */
    public int callAdminVersion() {
        return abPropsService.getInt(ABProp.CALL_ADMIN_VERSION);
    }

    /**
     * Returns the negotiated LID addressing version for calls.
     *
     * <p>The engine uses this version to decide whether and how to address call participants by
     * LID rather than phone number; {@code 0} disables LID addressing. Reads
     * {@link ABProp#CALLING_LID_VERSION}.
     *
     * @return the calling LID addressing version
     */
    public int callingLidVersion() {
        return abPropsService.getInt(ABProp.CALLING_LID_VERSION);
    }

    /**
     * Returns the negotiated in-call audio-sharing protocol version.
     *
     * <p>The engine negotiates in-call audio sharing at this version; {@code 0} disables audio
     * sharing. Reads {@link ABProp#CALLING_AUDIO_SHARE_VERSION}.
     *
     * @return the in-call audio-sharing version
     */
    public int callingAudioShareVersion() {
        return abPropsService.getInt(ABProp.CALLING_AUDIO_SHARE_VERSION);
    }

    /**
     * Returns the calling rust-migration bitmap.
     *
     * <p>Each bit selects whether a calling subsystem runs on the rust/native stack rather than
     * the legacy one; the engine masks this value per subsystem. Reads
     * {@link ABProp#CALLING_RUST_MIGRATION_BITMAP}.
     *
     * @return the rust-migration bitmap
     */
    public int callingRustMigrationBitmap() {
        return abPropsService.getInt(ABProp.CALLING_RUST_MIGRATION_BITMAP);
    }

    /**
     * Returns the web-calling performance-optimization bitmask.
     *
     * <p>Each bit toggles one calling performance optimization; the engine masks this value per
     * optimization. Reads {@link ABProp#WEB_CALLING_PERF_OPTIMIZATIONS_BITMASK}, whose default
     * is {@code 1}.
     *
     * @return the performance-optimization bitmask
     */
    public int callPerfOptimizationsBitmask() {
        return abPropsService.getInt(ABProp.WEB_CALLING_PERF_OPTIMIZATIONS_BITMASK);
    }

    /**
     * Returns the screen-share milestone version.
     *
     * <p>This is the host-side screen-share capability gate expressed as a milestone version: a
     * value greater than {@code 0} enables in-call screen sharing and selects its protocol
     * milestone, and {@link #isScreenShareEnabled()} folds this together with the calling master
     * gate. Reads {@link ABProp#CALLING_SCREEN_SHARE_MILESTONE_VERSION}, whose default is
     * {@code 2}.
     *
     * @return the screen-share milestone version
     */
    public int screenShareMilestoneVersion() {
        return abPropsService.getInt(ABProp.CALLING_SCREEN_SHARE_MILESTONE_VERSION);
    }

    /**
     * Returns the number of voip worker threads to preallocate.
     *
     * <p>The engine preallocates this many dynamic worker threads at startup rather than growing
     * the pool lazily; {@code 0} disables preallocation. Reads
     * {@link ABProp#WEB_VOIP_DYNAMIC_THREAD_PREALLOCATE_COUNT}.
     *
     * @return the worker-thread preallocation count
     */
    public int dynamicThreadPreallocateCount() {
        return abPropsService.getInt(ABProp.WEB_VOIP_DYNAMIC_THREAD_PREALLOCATE_COUNT);
    }

    /**
     * Returns the selector for the video renderer implementation.
     *
     * <p>The engine chooses among its video renderer implementations by this selector. Reads
     * {@link ABProp#WEB_VOIP_VIDEO_RENDERER}.
     *
     * @return the video renderer implementation selector
     */
    public int videoRendererImpl() {
        return abPropsService.getInt(ABProp.WEB_VOIP_VIDEO_RENDERER);
    }

    /**
     * Returns the selector for the video capture implementation.
     *
     * <p>The engine chooses among its video capture implementations by this selector. Reads
     * {@link ABProp#WEB_VOIP_VIDEO_CAPTURE_IMPL}.
     *
     * @return the video capture implementation selector
     */
    public int videoCaptureImpl() {
        return abPropsService.getInt(ABProp.WEB_VOIP_VIDEO_CAPTURE_IMPL);
    }

    /**
     * Returns the selector for the audio capture implementation.
     *
     * <p>The engine chooses among its audio capture implementations by this selector. Reads
     * {@link ABProp#WEB_VOIP_AUDIO_CAPTURE_IMPL}.
     *
     * @return the audio capture implementation selector
     */
    public int audioCaptureImpl() {
        return abPropsService.getInt(ABProp.WEB_VOIP_AUDIO_CAPTURE_IMPL);
    }

    /**
     * Returns the selector for the audio playback implementation.
     *
     * <p>The engine chooses among its audio playback implementations by this selector. Reads
     * {@link ABProp#WEB_VOIP_AUDIO_PLAYBACK_IMPL}.
     *
     * @return the audio playback implementation selector
     */
    public int audioPlaybackImpl() {
        return abPropsService.getInt(ABProp.WEB_VOIP_AUDIO_PLAYBACK_IMPL);
    }

    /**
     * Returns the voip WASM variant selector.
     *
     * <p>This selects which voip engine asset variant the host loads; the default is
     * {@code "prod-nonlab"}. Cobalt binds its codecs and engine statically, so this reduces to
     * an engine-configuration selector rather than a runtime asset download. Reads
     * {@link ABProp#WEB_VOIP_LOAD_WASM_VARIANT}.
     *
     * @return the voip WASM variant selector string
     */
    public String wasmVariant() {
        return abPropsService.getString(ABProp.WEB_VOIP_LOAD_WASM_VARIANT);
    }
}
