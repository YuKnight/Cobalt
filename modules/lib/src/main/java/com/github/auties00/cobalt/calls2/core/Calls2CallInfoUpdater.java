package com.github.auties00.cobalt.calls2.core;

/**
 * Folds a lifecycle event into a call's periodically-snapshotted info, mapping it to the result and
 * log-state fields.
 *
 * <p>The engine keeps a periodically-refreshed immutable snapshot of each call's info (its durations, its
 * result, its link info) for the host and for field stats, and updates that snapshot's result and
 * log-state fields from the call events as they occur. Cobalt exposes that update through this seam: the
 * {@link Calls2LifecycleController} reports each lifecycle {@link CallEventType} it raises, and the
 * implementer (the info-manager unit) maps the event to the call's {@link Calls2CallResult} and call-log
 * state and refreshes the snapshot. The mapping is the engine's, not the controller's; the controller
 * only reports the event and lets the info manager record its effect on the result.
 *
 * @apiNote This is an internal engine collaborator, not a public surface; embedders never call it. The
 * snapshot the implementer maintains is read by listeners and by the end-of-call telemetry, not through
 * this seam.
 * @implNote This implementation seam corresponds to {@code call_info_manager_update} (fn11076,
 * {@code call_info_manager.cc}) in the wa-voip WASM module {@code ff-tScznZ8P}: the event-to-result mapper
 * that writes the call's log-state at {@code call_context+0x47c} and result at {@code call_context+0x480}
 * (for example {@code 0x2f}/terminate maps to result {@code 6}, {@code 0x42} to {@code 6}, {@code 0x43}
 * to {@code 7}), then resets and copies the {@code call_info} snapshot (fn10678). The numeric result
 * codes are {@link Calls2CallResult}.
 */
@FunctionalInterface
public interface Calls2CallInfoUpdater {
    /**
     * Records the effect of one lifecycle event on the identified call's result and refreshes its info
     * snapshot.
     *
     * <p>The implementer maps the event to the call's result and log-state the way the engine's
     * info-manager update does and re-snapshots the call info. An event that does not affect the result
     * leaves it unchanged but may still refresh the snapshot's durations; an unknown call identifier is a
     * no-op.
     *
     * @param callId    the identifier of the call the event belongs to
     * @param eventType the lifecycle event to fold into the call's result
     * @throws NullPointerException if {@code callId} or {@code eventType} is {@code null}
     */
    void updateForEvent(String callId, CallEventType eventType);
}
