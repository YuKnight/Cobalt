package com.github.auties00.cobalt.calls2.core;

/**
 * The single egress through which the wa-voip call engine publishes in-call events to the host.
 *
 * <p>Every call-engine unit (the lifecycle controller, the state machine, the in-call controllers)
 * constructs a typed {@link CallEvent} and hands it here rather than touching the listener registry
 * directly. The bus owns two responsibilities the native engine couples into one dispatcher: it applies
 * the should-emit gate that suppresses the engine's internal-only and diagnostic event ids, and it fans
 * the surviving host-facing events out to the registered listeners. Concentrating both behind this seam
 * keeps the producers decoupled from the listener bus and gives the engine exactly one place where an
 * event becomes observable.
 *
 * <p>This interface is the producer-facing contract; {@link LiveCallEventBus} is the production
 * implementation that gates against the recovered host-facing event set and dispatches each listener on
 * its own virtual thread.
 *
 * @see CallEvent
 * @see CallEventType
 * @see LiveCallEventBus
 */
public sealed interface CallEventBus permits LiveCallEventBus {
    /**
     * Publishes a call event, gating it and fanning the survivors out to the registered listeners.
     *
     * @implSpec An implementation first consults {@link #shouldEmit(CallEventType)} for the event's
     * {@link CallEvent#type()} and returns without side effect when the gate rejects it. For a gated-in
     * event the implementation maps the typed {@link CallEvent} onto the matching host callbacks and
     * invokes each interested listener; a listener invocation must not run on the caller's thread, so
     * that a slow or throwing listener can neither stall the call engine nor abort the fan-out to the
     * other listeners. A {@code null} event is rejected.
     * @param event the event to publish
     * @throws NullPointerException if {@code event} is {@code null}
     */
    void emit(CallEvent event);

    /**
     * Returns whether an event of the given type is fanned out to listeners rather than suppressed.
     *
     * @implSpec An implementation returns {@code true} only for the host-facing event ids and
     * {@code false} for the engine's internal lifecycle and diagnostic ids. The decision depends solely
     * on the {@link CallEventType}, so it is stable for a given type and may be consulted by a producer
     * to skip building a payload it knows will be dropped.
     * @param type the event type to test
     * @return {@code true} if an event of this type is emitted to listeners, {@code false} if suppressed
     * @throws NullPointerException if {@code type} is {@code null}
     */
    boolean shouldEmit(CallEventType type);
}
