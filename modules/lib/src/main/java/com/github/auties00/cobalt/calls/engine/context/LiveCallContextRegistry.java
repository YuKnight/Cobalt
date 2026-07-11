package com.github.auties00.cobalt.calls.engine.context;

import com.github.auties00.cobalt.calls.engine.state.CallLifecycleState;
import com.github.auties00.cobalt.calls.engine.state.CallStateMachine;
import com.github.auties00.cobalt.calls.engine.timer.CallTimerKind;
import com.github.auties00.cobalt.calls.engine.timer.LiveCallTimerScheduler;
import com.github.auties00.cobalt.model.call.Call;

import java.util.Objects;

/**
 * Allocates and frees engine call contexts in a {@link CallManager} on behalf of the controller.
 *
 * <p>A context is built from the controller's {@link Call} so it carries the same call id the controller
 * tracks, which lets the {@link CallStateMachine} resolve the very context this registry adopted when the
 * controller later requests a transition by id. The primary slot is filled first; a second concurrent call
 * fills the secondary (dual) slot. As it allocates a context this registry also wires the context's
 * {@linkplain CallContext#onScheduleConnectedLonelyTimer(java.util.function.Consumer) connected lonely timer}
 * seams onto the {@link LiveCallTimerScheduler}, because the engine arms the connected lonely timer from the
 * state transition guard rather than from the controller, so the seam must be in place before the context's
 * first transition.
 *
 * @param manager the call manager that holds the at most two call contexts
 * @param timers  the per call timer scheduler the connected lonely seams are wired onto
 */
public record LiveCallContextRegistry(CallManager manager, LiveCallTimerScheduler timers)
        implements CallContextRegistry {
    /**
     * Canonicalizes the registry over its manager and timer scheduler.
     *
     * @throws NullPointerException if {@code manager} or {@code timers} is {@code null}
     */
    public LiveCallContextRegistry {
        Objects.requireNonNull(manager, "manager cannot be null");
        Objects.requireNonNull(timers, "timers cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation builds a {@link CallContext} pinned to {@code call}'s call id, wires its
     * connected lonely timer scheduling and cancelling seams onto the {@link LiveCallTimerScheduler}, then
     * adopts it into the {@link CallManager}'s primary slot, or the secondary slot when a primary call is
     * already live, so the dual call ceiling is the manager's authoritative backstop, and returns the adopted
     * context so the controller can hand it to the outbound call log sink at teardown. The seams are wired
     * before the context is adopted because the state guard fires the schedule seam the moment the call enters
     * {@link CallLifecycleState#CONNECTED_LONELY}, which can be the context's very first transition.
     */
    @Override
    public CallContext allocate(Call call, CallLifecycleState initialState) {
        Objects.requireNonNull(call, "call cannot be null");
        Objects.requireNonNull(initialState, "initialState cannot be null");
        var direction = call.isOutgoing()
                ? CallContext.CallDirection.OUTGOING
                : CallContext.CallDirection.INCOMING;
        var role = manager.hasPrimary()
                ? CallContext.CallRole.SECONDARY
                : CallContext.CallRole.PRIMARY;
        var context = new CallContext(call.callId(), role, direction, call.peer(), call.creator(),
                call.creator(), call.chatJid(), call.isGroup(), call.isVideo());
        context.lock().lock();
        try {
            context.state(initialState);
        } finally {
            context.lock().unlock();
        }
        context.onScheduleConnectedLonelyTimer(timers::scheduleConnectedLonely);
        context.onCancelConnectedLonelyTimer(() -> timers.cancel(call.callId(), CallTimerKind.CONNECTED_LONELY));
        if (role == CallContext.CallRole.SECONDARY) {
            manager.startDualCall(context);
        } else {
            manager.startCall(context);
        }
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release(String callId) {
        Objects.requireNonNull(callId, "callId cannot be null");
        manager.endCall(callId);
    }
}
