package com.github.auties00.cobalt.calls2.core;

import com.github.auties00.cobalt.model.call.Call;

/**
 * Allocates and frees the engine call context for a call on behalf of the lifecycle controller.
 *
 * <p>The engine keeps at most a primary and one optional secondary (dual) call context in its singleton
 * call manager, each with its own info-mutex; the context is allocated when a call starts, is answered,
 * or arrives as an offer, and is freed when the call ends. Cobalt exposes that allocation through this
 * seam: the {@link Calls2LifecycleController} asks the manager unit to allocate a context for a new call
 * and to free it on teardown, keyed by call identifier. The controller keeps its own light orchestration
 * handle for cross-unit wiring; the heavy context, its eleven timer entries, its durations, and its
 * per-context lock live behind this seam in the manager and state units, which the state transition and
 * timer seams then drive by the same identifier.
 *
 * <p>The dual-call ceiling is the manager's invariant: an allocation that would exceed a primary and one
 * secondary call is refused. The controller applies its own admission check before allocating, so this
 * seam's refusal is the manager's authoritative backstop rather than the primary gate.
 *
 * @apiNote This is an internal engine collaborator, not a public surface; embedders never call it.
 * @implNote This implementation seam corresponds to the call manager of {@code call_manager.cc}
 * (fn10728-fn10734) in the wa-voip WASM module {@code ff-tScznZ8P}: {@code call_manager_create} allocates
 * the singleton, {@code init_local_state} (fn10707) lays out a context, {@code call_manager_start_dual_call_from_context}
 * (fn10731) allocates the secondary context, and {@code call_manager_end_call} (fn10733) frees a context
 * (the secondary first, then the primary, when both end). The per-context {@code call-info-mutex}
 * ({@code call_manager+0x10}/{@code +0x14}) is replaced by a {@code ReentrantLock} per the Cobalt
 * threading model.
 */
public interface Calls2CallContextRegistry {
    /**
     * Allocates an engine call context for a new call and returns it.
     *
     * <p>The implementer lays out the context from the call's identity (its identifier, peer, creator,
     * direction, topology, and video flag, all carried by the {@link Call}) and its initial internal
     * state, registering it under the call's identifier so the state-transition and timer seams can drive
     * it, and returns the allocated context so the caller can hand it to its end-of-call collaborators (the
     * outbound call-log sink reads the finished context at teardown). Allocating a context for an identifier
     * that already has one refreshes that context rather than duplicating it. An allocation that would
     * exceed the dual-call ceiling is refused with an {@link IllegalStateException}.
     *
     * @param call         the public view of the call whose context is being allocated
     * @param initialState the call's initial internal state
     * @return the allocated engine call context, or {@code null} when the implementer builds none (a test
     *         registry with no manager)
     * @throws NullPointerException  if {@code call} or {@code initialState} is {@code null}
     * @throws IllegalStateException if allocating would exceed the dual-call ceiling
     */
    Calls2CallContext allocate(Call call, Calls2CallState initialState);

    /**
     * Frees the engine call context for a call.
     *
     * <p>Called as a call tears down so the context, its timers, and its manager slot are released.
     * Freeing a context for an identifier that has none is a no-op.
     *
     * @param callId the identifier of the call whose context is being freed
     * @throws NullPointerException if {@code callId} is {@code null}
     */
    void release(String callId);
}
