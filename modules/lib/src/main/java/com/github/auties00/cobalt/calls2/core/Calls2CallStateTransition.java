package com.github.auties00.cobalt.calls2.core;

import java.util.Optional;

/**
 * Drives a single call's internal state through the wa-voip transition guard.
 *
 * <p>The engine routes every call-state change through one chokepoint that enforces which transitions
 * are legal, accounts for the time spent in the active and connected-lonely states, and schedules or
 * cancels the connected-lonely timer as the call enters or leaves those states. Cobalt exposes that
 * chokepoint through this seam: the {@link Calls2LifecycleController} asks for a transition to a new
 * {@link Calls2CallState}, and the implementer (the state-machine unit) applies the guard and reports the
 * prior state when the transition was accepted, or an empty result when the guard rejected it.
 *
 * <p>The guard's rules are the engine's, not the controller's: a transition to the same state is a
 * no-op, the two in-call states {@link Calls2CallState#CALL_ACTIVE} and
 * {@link Calls2CallState#CONNECTED_LONELY} may only move to each other or to {@link Calls2CallState#NONE},
 * and the {@link Calls2CallState#LINK} and {@link Calls2CallState#ENDING} transitions are silent. The
 * controller does not re-implement those rules; it only requests the target state and reacts to whether
 * the transition was accepted. The wrapper that fires the {@code call_state_event} after a successful
 * transition is the controller's responsibility through {@link Calls2CallEventSink}, mirroring the native
 * split between {@code change_call_state_no_event} and its event-firing wrapper {@code change_call_state}.
 *
 * @apiNote This is an internal engine collaborator, not a public surface; embedders never call it.
 * @implNote This implementation seam corresponds to {@code change_call_state_no_event} (fn10920,
 * {@code call_membership.cc}) in the wa-voip WASM module {@code ff-tScznZ8P}: the single transition guard
 * stored against {@code call_context[0]}, with its legal-successor checks, its active and lonely duration
 * accounting on leaving each in-call state, and its connected-lonely timer scheduling on entering
 * {@link Calls2CallState#CONNECTED_LONELY}. The event-firing wrapper {@code change_call_state} (fn10921),
 * which posts {@code call_state_event} (event id {@code 0x10}) after a successful transition, is layered
 * on top by the controller rather than by this guard.
 */
public interface Calls2CallStateTransition {
    /**
     * Transitions the identified call to a new internal state through the transition guard.
     *
     * <p>The implementer applies the engine's legal-successor rules for the call's current state. When
     * the transition is accepted it returns the state the call held before the change, so the controller
     * can tell whether the state actually moved (a transition to the current state returns that same
     * state and fires no event) and can fire the {@code call_state_event} for a real change. When the
     * guard rejects the transition (an illegal successor, or no call exists for the identifier) it
     * returns an empty result and the call's state is unchanged.
     *
     * @param callId   the identifier of the call to transition
     * @param newState the target internal state
     * @return an {@link Optional} holding the prior state when the transition was accepted, or empty when
     *         the guard rejected it or no call exists for the identifier
     * @throws NullPointerException if {@code callId} or {@code newState} is {@code null}
     */
    Optional<Calls2CallState> transition(String callId, Calls2CallState newState);
}
