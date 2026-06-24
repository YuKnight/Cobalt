package com.github.auties00.cobalt.calls2.core;

/**
 * Arms and cancels a call's per-lifetime timers on behalf of the lifecycle controller.
 *
 * <p>The wa-voip engine schedules a fixed set of timers per call on its timer heap; Cobalt runs the same
 * timers on a virtual-thread scheduler with the engine's constants. This seam lets the
 * {@link Calls2LifecycleController} arm and cancel those timers by {@link Calls2CallTimerKind} without
 * depending on the timer unit's internal scheduling or on the per-timer callback bodies. The controller
 * arms the lifecycle-relevant timers as a call advances (the caller-lonely timer and the watchdog when an
 * outbound call starts) and cancels every timer when the call tears down; each timer's callback action is
 * owned by the timer unit, which the controller never invokes directly.
 *
 * @apiNote This is an internal engine collaborator, not a public surface; embedders never call it.
 * @implNote This implementation seam corresponds to the schedule and cancel helpers of
 * {@code call_timer.cc} (fn10947-fn10952) in the wa-voip WASM module {@code ff-tScznZ8P}, including the
 * teardown {@code stop_call_timer_worker_thread} (fn10952) that cancels every entry. The native timer
 * heap and worker thread are replaced by a virtual-thread scheduler per the Cobalt threading model; this
 * seam carries only the arm and cancel operations the controller drives.
 */
public interface Calls2CallTimerScheduler {
    /**
     * Arms the given timer for the identified call.
     *
     * <p>The timer fires after its engine-defined interval and runs its callback, which the timer unit
     * owns. Arming a timer that is already armed for the call re-arms it at a fresh deadline rather than
     * stacking a second entry, matching the engine's reschedule semantics. The connected-lonely timer is
     * armed by the state-transition guard as a call enters the connected-lonely state, so the controller
     * arms only the timers it drives directly.
     *
     * @param callId the identifier of the call whose timer is being armed
     * @param kind   the timer to arm
     * @throws NullPointerException if {@code callId} or {@code kind} is {@code null}
     */
    void arm(String callId, Calls2CallTimerKind kind);

    /**
     * Cancels the given timer for the identified call.
     *
     * <p>A timer that is not armed is a no-op to cancel. Cancelling a timer stops its pending callback
     * from firing without affecting the call's other timers.
     *
     * @param callId the identifier of the call whose timer is being cancelled
     * @param kind   the timer to cancel
     * @throws NullPointerException if {@code callId} or {@code kind} is {@code null}
     */
    void cancel(String callId, Calls2CallTimerKind kind);

    /**
     * Cancels every timer armed for the identified call.
     *
     * <p>Called as a call tears down so no timer outlives the call. A call with no armed timers is a
     * no-op.
     *
     * @param callId the identifier of the call whose timers are being cancelled
     * @throws NullPointerException if {@code callId} is {@code null}
     */
    void cancelAll(String callId);
}
