package com.github.auties00.cobalt.calls.engine.timer;

import com.github.auties00.cobalt.calls.engine.LifecycleController;

/**
 * Arms and cancels a call's timers on behalf of the lifecycle controller.
 *
 * <p>Each call owns a fixed set of timers, one per {@link CallTimerKind}, that fire after
 * engine defined intervals and drive lifetime decisions (the lonely caller timeout, the connection
 * watchdog, and the rest). This seam lets the {@link LifecycleController} arm and cancel those timers
 * by kind without depending on how they are scheduled or on the bodies of the callbacks they run. The
 * controller arms the timers it drives directly as a call advances (the lonely caller timer and the
 * watchdog when an outbound call starts) and cancels every timer when the call tears down; each timer's
 * callback action belongs to the scheduler, which the controller never invokes directly.
 *
 * @apiNote This is an internal engine collaborator, not a public surface; embedders never call it.
 */
public interface CallTimerScheduler {
    /**
     * Arms the given timer for the identified call.
     *
     * <p>The timer fires after the interval defined for its {@link CallTimerKind} and runs its callback,
     * which the scheduler owns. Arming a timer that is already armed for the call re arms it at a fresh
     * deadline rather than stacking a second entry.
     *
     * @param callId the identifier of the call whose timer is being armed
     * @param kind   the timer to arm
     * @throws NullPointerException if {@code callId} or {@code kind} is {@code null}
     */
    void arm(String callId, CallTimerKind kind);

    /**
     * Cancels the given timer for the identified call.
     *
     * <p>Cancelling stops the timer's pending callback from firing without affecting the call's other
     * timers. Cancelling a timer that is not armed is a no op.
     *
     * @param callId the identifier of the call whose timer is being cancelled
     * @param kind   the timer to cancel
     * @throws NullPointerException if {@code callId} or {@code kind} is {@code null}
     */
    void cancel(String callId, CallTimerKind kind);

    /**
     * Cancels every timer armed for the identified call.
     *
     * <p>Invoked as a call tears down so that no timer outlives the call. A call with no armed timers is a
     * no op.
     *
     * @param callId the identifier of the call whose timers are being cancelled
     * @throws NullPointerException if {@code callId} is {@code null}
     */
    void cancelAll(String callId);
}
