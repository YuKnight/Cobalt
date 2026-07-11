package com.github.auties00.cobalt.calls.engine.timer;

import com.github.auties00.cobalt.calls.config.CallsFeatureGate;
import com.github.auties00.cobalt.calls.engine.context.CallContext;
import com.github.auties00.cobalt.calls.engine.context.CallManager;
import com.github.auties00.cobalt.calls.engine.GroupCallOutbound;
import com.github.auties00.cobalt.calls.engine.LifecycleController;
import com.github.auties00.cobalt.calls.engine.event.CallEventType;
import com.github.auties00.cobalt.calls.engine.event.CallLifecycleEventSink;
import com.github.auties00.cobalt.calls.engine.mediaplane.MediaStreams;
import com.github.auties00.cobalt.calls.engine.state.CallLifecycleState;
import com.github.auties00.cobalt.model.call.CallEndReason;
import com.github.auties00.cobalt.util.DataUtils;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Owns one {@link CallTimers} driver per call, arms or cancels its timers by kind, and runs each
 * timer's callback body when it fires.
 *
 * <p>Each call gets its own virtual thread timer driver, created lazily on the first arm and stopped
 * when the call's timers are cancelled wholesale on teardown. A {@link CallTimerKind} maps one to one
 * onto a {@link CallTimers.Timer}. The lifecycle relevant callbacks are bound here, where the call
 * manager, the event sink, and the lifecycle controller are all reachable: the
 * {@link CallTimerKind#PERIODIC} watchdog rearms itself every second and sweeps for the deadlines the
 * host path tracks, the {@link CallTimerKind#CALLER_LONELY} timeout ends an unanswered outbound call,
 * and the {@link CallTimerKind#CONNECTED_LONELY} timeout walks its interval sequence and ends a
 * connected call that never gained a peer. The remaining kinds are armed with their period but carry
 * no teardown body here, because their callbacks belong to the group call and in call control units
 * that arm them rather than to the lifecycle controller.
 *
 * <p>A fired lonely callback ends the call by calling back into the {@link LifecycleController},
 * which is {@linkplain #bindController(LifecycleController) bound} after the controller is built; the
 * callback runs on the timer driver thread holding no call lock, so the reentrant
 * {@link LifecycleController#endCall(String, CallEndReason)} teardown (which cancels this very
 * driver) neither deadlocks nor joins itself, because {@link CallTimers#stop()} skips joining the
 * driver thread from within its own callback.
 */
public final class LiveCallTimerScheduler implements CallTimerScheduler {
    /**
     * Logs a timeout that arrives before the controller is bound, which should never happen because a
     * timer is armed only from a controller method that runs after {@link #bindController}.
     */
    private static final System.Logger LOGGER = System.getLogger(LiveCallTimerScheduler.class.getName());

    /**
     * The empty payload emitted with a timer driven lifecycle event.
     */
    // TODO: populate the timer driven lifecycle event payload once its byte layout is known
    private static final byte[] EMPTY_PAYLOAD = DataUtils.EMPTY_BYTE_ARRAY;

    /**
     * Holds the live timer driver for each call id, created on first arm and removed on cancel all.
     */
    private final ConcurrentHashMap<String, CallTimers> timers = new ConcurrentHashMap<>();

    /**
     * The call manager used to resolve a firing timer's call context for the watchdog sweep.
     */
    private final CallManager manager;

    /**
     * The event sink a fired lonely or watchdog timeout emits its lifecycle event onto.
     */
    private final CallLifecycleEventSink events;

    /**
     * The calls feature gate, read for the AB prop configured group call heartbeat cadence when
     * arming the {@link CallTimers.Timer#HEARTBEAT} timer.
     */
    private final CallsFeatureGate featureGate;

    /**
     * The lifecycle controller a fired lonely timeout ends the call through, bound after construction
     * by {@link #bindController(LifecycleController)}; {@code null} only in the construction window
     * before any timer can be armed.
     */
    private volatile LifecycleController controller;

    /**
     * Resolves a call's outbound group call unit by call id for the watchdog's unanswered offer
     * sweep, bound after construction by {@link #bindGroupOutboundResolver(Function)}; {@code null}
     * only in the construction window before any watchdog can fire.
     *
     * <p>The watchdog calls this each tick rather than capturing the unit at arm time, so a one to one
     * call (no unit) yields an empty result and a group call torn down between ticks is no longer
     * swept.
     */
    private volatile Function<String, Optional<GroupCallOutbound>> groupOutboundResolver;

    /**
     * Constructs a timer scheduler over the call manager, event sink, and feature gate.
     *
     * @param manager     the call manager used to resolve a firing timer's call context
     * @param events      the event sink a fired timeout emits its lifecycle event onto
     * @param featureGate the calls feature gate read for the group call heartbeat cadence
     * @throws NullPointerException if {@code manager}, {@code events}, or {@code featureGate} is
     *                              {@code null}
     */
    public LiveCallTimerScheduler(CallManager manager, CallLifecycleEventSink events,
                                  CallsFeatureGate featureGate) {
        this.manager = Objects.requireNonNull(manager, "manager cannot be null");
        this.events = Objects.requireNonNull(events, "events cannot be null");
        this.featureGate = Objects.requireNonNull(featureGate, "featureGate cannot be null");
    }

    /**
     * Binds the lifecycle controller a fired lonely timeout ends the call through.
     *
     * <p>The controller takes this scheduler as a constructor argument, so it cannot be passed to the
     * scheduler's constructor; the assembler builds the scheduler, builds the controller, then binds
     * the controller here before returning, so the binding is in place before the controller's first
     * call method can arm any timer.
     *
     * @param controller the lifecycle controller
     * @throws NullPointerException if {@code controller} is {@code null}
     */
    public void bindController(LifecycleController controller) {
        this.controller = Objects.requireNonNull(controller, "controller cannot be null");
    }

    /**
     * Binds the resolver the watchdog reaches a call's outbound group call unit through for its
     * unanswered offer sweep.
     *
     * <p>The controller is built after this scheduler (it takes the scheduler as a constructor
     * argument), so this resolver cannot be passed to the constructor either; the assembler builds the
     * scheduler, builds the controller, then binds the controller's
     * {@link LifecycleController#groupOutbound(String) group outbound resolver} here before returning,
     * the same post construction wiring step {@link #bindController(LifecycleController)} uses, so the
     * binding is in place before any watchdog can fire.
     *
     * @param resolver the resolver from a call id to its outbound group call unit
     * @throws NullPointerException if {@code resolver} is {@code null}
     */
    public void bindGroupOutboundResolver(Function<String, Optional<GroupCallOutbound>> resolver) {
        this.groupOutboundResolver = Objects.requireNonNull(resolver, "resolver cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation arms the matching {@link CallTimers.Timer} with its callback body:
     * {@link CallTimerKind#PERIODIC} is armed at the one second watchdog cadence with the self
     * rescheduling {@link #periodicWatchdog(String, CallTimers)} sweep,
     * {@link CallTimerKind#CALLER_LONELY} at the short lonely state timeout with the
     * {@link #callerLonelyTimeout(String)} teardown, and {@link CallTimerKind#CONNECTED_LONELY} at its
     * direction picked first interval (see {@link #firstLonelyIntervalMillis(String)}) with the
     * interval walking {@link #connectedLonelyTimeout(String, CallTimers, int)} teardown (the
     * controller arms the connected lonely timer only as a direct request; the engine's usual path is
     * the state guard seam {@link #scheduleConnectedLonely(CallContext)}). The remaining kinds are
     * armed with their period and an inert body, because their callbacks are owned by the group call
     * and in call control units that arm them, not by the lifecycle controller.
     */
    @Override
    public void arm(String callId, CallTimerKind kind) {
        Objects.requireNonNull(callId, "callId cannot be null");
        Objects.requireNonNull(kind, "kind cannot be null");
        var driver = timers.computeIfAbsent(callId, CallTimers::new);
        switch (kind) {
            case PERIODIC ->
                    driver.arm(CallTimers.Timer.PERIODIC, CallTimers.Timer.PERIODIC.defaultPeriod(),
                            () -> periodicWatchdog(callId, driver));
            case CALLER_LONELY ->
                    driver.arm(CallTimers.Timer.CALLER_LONELY,
                            CallTimers.Timer.CALLER_LONELY.defaultPeriod(),
                            () -> callerLonelyTimeout(callId));
            case CONNECTED_LONELY -> armConnectedLonely(callId, driver, firstLonelyIntervalMillis(callId));
            case HEARTBEAT -> {
                var period = heartbeatPeriod();
                if (!period.isZero() && !period.isNegative()) {
                    driver.arm(CallTimers.Timer.HEARTBEAT, period, () -> heartbeatTick(callId, driver));
                }
            }
            default -> armUnownedTimer(driver, kind);
        }
    }

    /**
     * Sends the group call heartbeat and reschedules itself for the next cadence tick.
     *
     * <p>The lifecycle controller emits one heartbeat (itself doing nothing unless the call is an
     * active group call) and the timer rearms at the {@code heartbeat_interval_s} cadence as long as
     * the call's driver is still running, so the heartbeat stops once the call is torn down.
     *
     * @param callId the identifier of the call being heartbeated
     * @param driver the call's timer driver
     */
    private void heartbeatTick(String callId, CallTimers driver) {
        var current = controller;
        if (current != null) {
            current.sendHeartbeat(callId);
        }
        var period = heartbeatPeriod();
        if (!period.isZero() && !period.isNegative()) {
            driver.armIfRunning(CallTimers.Timer.HEARTBEAT, period,
                    () -> heartbeatTick(callId, driver));
        }
    }

    /**
     * Returns the group call heartbeat cadence read from the feature gate.
     *
     * <p>The {@code heartbeat_interval_s} server setting is read per call, so a configuration change
     * is picked up the next time a call arms its heartbeat.
     *
     * @return the heartbeat period
     */
    private Duration heartbeatPeriod() {
        return Duration.ofSeconds(featureGate.heartbeatIntervalSeconds());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancel(String callId, CallTimerKind kind) {
        Objects.requireNonNull(callId, "callId cannot be null");
        Objects.requireNonNull(kind, "kind cannot be null");
        var driver = timers.get(callId);
        if (driver != null) {
            driver.cancel(toTimer(kind));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelAll(String callId) {
        Objects.requireNonNull(callId, "callId cannot be null");
        var driver = timers.remove(callId);
        if (driver != null) {
            driver.stop();
        }
    }

    /**
     * Schedules the connected lonely timer for a call entering the connected lonely state.
     *
     * <p>This is the seam the state transition guard fires through
     * {@link CallContext#fireScheduleConnectedLonelyTimer()} the moment a call enters
     * {@link CallLifecycleState#CONNECTED_LONELY}; the engine arms the connected lonely timer from the
     * guard rather than from the controller, so this is the usual entry point for the timer. The first
     * interval is picked by direction from the context's
     * {@linkplain CallContext#connectedLonelyConfig() connected lonely configuration} (the short
     * thirty second interval for the caller, the long two hundred seventy second interval for the
     * callee), and the callback walks the rest of
     * {@link CallTimers#DEFAULT_CONNECTED_LONELY_INTERVALS_MS} before ending the call.
     *
     * @implNote This implementation picks the first interval by direction: the caller takes the short
     * interval and the callee the long interval, then the callback walks the interval array, stepping
     * up to the maximum interval before the lonely timeout teardown.
     *
     * @param context the call context entering the connected lonely state
     * @throws NullPointerException if {@code context} is {@code null}
     */
    public void scheduleConnectedLonely(CallContext context) {
        Objects.requireNonNull(context, "context cannot be null");
        var callId = context.callId();
        var first = context.connectedLonelyConfig().intervalForDirection(context.direction());
        var driver = timers.computeIfAbsent(callId, CallTimers::new);
        armConnectedLonely(callId, driver, first);
    }

    /**
     * Arms the connected lonely timer at its first interval with the interval walking teardown.
     *
     * <p>Shared by the state guard seam {@link #scheduleConnectedLonely(CallContext)} and the direct
     * {@link CallTimerKind#CONNECTED_LONELY} arm: the first interval fires
     * {@link #connectedLonelyTimeout(String, CallTimers, int)} with the next index, which walks the
     * remaining intervals of {@link CallTimers#DEFAULT_CONNECTED_LONELY_INTERVALS_MS} and ends the
     * call after the last.
     *
     * @param callId      the identifier of the call whose connected lonely timer is armed
     * @param driver      the call's timer driver
     * @param firstMillis the first interval, in milliseconds
     */
    private void armConnectedLonely(String callId, CallTimers driver, long firstMillis) {
        driver.arm(CallTimers.Timer.CONNECTED_LONELY, Duration.ofMillis(firstMillis),
                () -> connectedLonelyTimeout(callId, driver, 1));
    }

    /**
     * Returns the first connected lonely interval for a call, in milliseconds.
     *
     * <p>The first interval is picked by direction from the call context's connected lonely
     * configuration when the manager still holds the context (the short interval for the caller, the
     * long interval for the callee); when the context is not resolvable it falls back to the first
     * entry of {@link CallTimers#DEFAULT_CONNECTED_LONELY_INTERVALS_MS}.
     *
     * @param callId the identifier of the call whose first interval is resolved
     * @return the first connected lonely interval in milliseconds
     */
    private long firstLonelyIntervalMillis(String callId) {
        return manager.getByCallId(callId)
                .map(context -> context.connectedLonelyConfig().intervalForDirection(context.direction()))
                .orElse(CallTimers.DEFAULT_CONNECTED_LONELY_INTERVALS_MS[0]);
    }

    /**
     * Runs the periodic watchdog sweep and reschedules itself for the next second.
     *
     * <p>The watchdog resolves the firing call's context and, for a group call, terminates any peer
     * whose offer has gone unanswered past the {@link CallTimers#UNANSWERED_GROUP_OFFER_TIMEOUT}
     * cutoff; it then rearms itself for the next second, matching the engine's self rescheduling
     * watchdog. The two one to one call setup deadlines the engine watchdog also enforces are enforced
     * on the host path elsewhere: the offer ack deadline by the synchronous offer ack wait in
     * {@link LifecycleController#startCall(com.github.auties00.cobalt.model.jid.Jid,
     * com.github.auties00.cobalt.model.jid.Jid, java.util.List, boolean, MediaStreams)}, and the no
     * answer deadline by the {@link CallTimerKind#CALLER_LONELY} timer, so the watchdog adds no
     * further one to one teardown here.
     *
     * @implNote This implementation delegates the sweep to the call's {@link GroupCallOutbound} unit,
     * resolved through the controller's {@linkplain #bindGroupOutboundResolver(Function) bound
     * resolver}: the unit reads each peer's offer send timestamp off the call's
     * {@link com.github.auties00.cobalt.calls.engine.participant.CallMembership} and terminates a peer
     * whose offer has gone unanswered past {@link CallTimers#UNANSWERED_GROUP_OFFER_TIMEOUT}. A one to
     * one call resolves to no unit and is swept of nothing. The unanswered mute response and offer
     * peek sweeps belong to the in call control and offer peek units. A call the manager no longer
     * holds is not rescheduled, so the watchdog stops once the call is torn down.
     *
     * @param callId the identifier of the call whose watchdog fired
     * @param driver the call's timer driver the watchdog rearms itself on
     */
    private void periodicWatchdog(String callId, CallTimers driver) {
        var context = manager.getByCallId(callId).orElse(null);
        if (context == null) {
            return;
        }
        var resolver = groupOutboundResolver;
        if (resolver != null) {
            resolver.apply(callId).ifPresent(GroupCallOutbound::sweepUnansweredOffers);
        }
        driver.armIfRunning(CallTimers.Timer.PERIODIC, CallTimers.Timer.PERIODIC.defaultPeriod(),
                () -> periodicWatchdog(callId, driver));
    }

    /**
     * Ends an unanswered outbound one to one call when the caller lonely timeout fires.
     *
     * <p>The caller lonely timer is armed when an outbound call starts ringing; its expiry means the
     * callee never answered within the lonely state window, so the call ends with
     * {@link CallEndReason#TIMEOUT} and the {@link CallEventType#LONELY_STATE_TIMEOUT} event.
     *
     * @param callId the identifier of the call whose caller lonely timer fired
     */
    private void callerLonelyTimeout(String callId) {
        failCall(callId, CallEndReason.TIMEOUT, CallEventType.LONELY_STATE_TIMEOUT);
    }

    /**
     * Walks the connected lonely interval sequence, ending the call after the final interval.
     *
     * <p>The connected lonely timer walks {@link CallTimers#DEFAULT_CONNECTED_LONELY_INTERVALS_MS}: on
     * each interval before the last it rearms at the next interval so a connected but alone call
     * survives a transient peer absence, and on the final interval it ends the call with
     * {@link CallEndReason#TIMEOUT} and the {@link CallEventType#LONELY_STATE_TIMEOUT} event. The
     * {@code nextIndex} is the index of the interval to arm next, the host analogue of the engine's
     * current interval index.
     *
     * @param callId    the identifier of the call whose connected lonely timer fired
     * @param driver    the call's timer driver the timeout rearms itself on
     * @param nextIndex the index of the next interval to arm, or one past the last interval to end the
     *                  call
     */
    private void connectedLonelyTimeout(String callId, CallTimers driver, int nextIndex) {
        var intervals = CallTimers.DEFAULT_CONNECTED_LONELY_INTERVALS_MS;
        if (nextIndex >= intervals.length) {
            failCall(callId, CallEndReason.TIMEOUT, CallEventType.LONELY_STATE_TIMEOUT);
            return;
        }
        driver.armIfRunning(CallTimers.Timer.CONNECTED_LONELY, Duration.ofMillis(intervals[nextIndex]),
                () -> connectedLonelyTimeout(callId, driver, nextIndex + 1));
    }

    /**
     * Emits a timer driven lifecycle event and ends the call through the lifecycle controller.
     *
     * <p>Fires the cause event (the lonely timeout event) onto the shared event sink, then ends the
     * call through {@link LifecycleController#endCall(String, CallEndReason)}, which sends the
     * terminate, cancels every per call timer, closes the media plane, drives the state to
     * {@link CallLifecycleState#NONE}, and emits the ending events. The callback runs on the timer
     * driver thread holding no call lock, so the reentrant teardown (which cancels this very driver)
     * is safe.
     *
     * @param callId      the identifier of the call to end
     * @param reason      the end reason published on the call view and carried on the terminate
     * @param causeEvent  the lifecycle event for the cause of the teardown
     */
    private void failCall(String callId, CallEndReason reason, CallEventType causeEvent) {
        var current = controller;
        if (current == null) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Timer fired for call {0} before the lifecycle controller was bound", callId);
            return;
        }
        events.emit(causeEvent, EMPTY_PAYLOAD);
        current.endCall(callId, reason);
    }

    /**
     * Arms a timer whose callback body is owned by another engine unit at its period.
     *
     * <p>The heartbeat, lobby, ohai, key rotation, reaction clear, video upgrade, end to end restore,
     * and app data stream test timers are armed by the group call and in call control units that own
     * their callbacks; this scheduler only holds the driver and the lifecycle relevant bodies, so it
     * arms these with their period (or the watchdog cadence for a timer whose negotiated delay is not
     * threaded here yet) and an inert body the owning unit replaces when it arms the timer with its own
     * callback.
     *
     * @param driver the call's timer driver
     * @param kind   the timer kind whose callback is owned by another unit
     */
    private void armUnownedTimer(CallTimers driver, CallTimerKind kind) {
        // UPDATE_ENCRYPTION_KEY reaches this seam with an inert body: the membership driven rekey runs
        // immediately in the lifecycle controller over the reconciled group roster diff, so no per
        // call rekey timer is armed. The remaining kinds (LOBBY, OHAI, REACTION_CLEAR, VIDEO_UPGRADE,
        // E2EE_RESTORE, APP_DATA_STREAM_TEST) reach this seam only when a caller drives one through it.
        var timer = toTimer(kind);
        var period = timer.defaultPeriod().isZero()
                ? CallTimers.Timer.PERIODIC.defaultPeriod()
                : timer.defaultPeriod();
        driver.arm(timer, period, () -> {
        });
    }

    /**
     * Maps a controller timer kind onto its timer unit member.
     *
     * @param kind the controller facing timer kind
     * @return the matching {@link CallTimers.Timer}
     */
    private static CallTimers.Timer toTimer(CallTimerKind kind) {
        return switch (kind) {
            case PERIODIC -> CallTimers.Timer.PERIODIC;
            case CALLER_LONELY -> CallTimers.Timer.CALLER_LONELY;
            case OHAI -> CallTimers.Timer.OHAI;
            case HEARTBEAT -> CallTimers.Timer.HEARTBEAT;
            case LOBBY -> CallTimers.Timer.LOBBY;
            case CONNECTED_LONELY -> CallTimers.Timer.CONNECTED_LONELY;
            case UPDATE_ENCRYPTION_KEY -> CallTimers.Timer.UPDATE_ENCRYPTION_KEY;
            case REACTION_CLEAR -> CallTimers.Timer.REACTION_CLEAR;
            case VIDEO_UPGRADE -> CallTimers.Timer.VIDEO_UPGRADE;
            case E2EE_RESTORE -> CallTimers.Timer.E2EE_RESTORE;
            case APP_DATA_STREAM_TEST -> CallTimers.Timer.APP_DATA_STREAM_TEST;
        };
    }
}
