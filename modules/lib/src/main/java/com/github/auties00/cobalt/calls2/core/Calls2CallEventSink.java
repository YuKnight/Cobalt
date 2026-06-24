package com.github.auties00.cobalt.calls2.core;

/**
 * Receives the typed in-call events the lifecycle controller emits, after the should-emit gate.
 *
 * <p>The wa-voip engine funnels every state change that the host may care about through one generic
 * dispatcher, which selects the event by its {@link CallEventType} and, in a debug build, logs the
 * event's display name. Cobalt routes that single egress through this seam: the
 * {@link Calls2LifecycleController} reports each lifecycle event by its {@link CallEventType} and an
 * opaque serialized payload, and the implementer applies the engine's should-emit gate and fans the
 * surviving events out to the registered listeners, one fresh virtual thread per matching listener, so a
 * listener can block freely without stalling the socket reader.
 *
 * <p>This is the port the event-bus unit implements; the controller depends only on this contract, never
 * on the concrete fan-out implementation, so the two units compose without the controller knowing how an
 * event reaches a listener. The payload byte layout depends on the event type and is opaque to this seam;
 * the controller passes the bytes through verbatim and the implementer decodes only the events it
 * surfaces.
 *
 * @apiNote This is an internal engine collaborator, not a public surface; embedders never call it. The
 * application observes call events through the {@code WhatsAppListener} bus the implementer fans out to,
 * not through this seam.
 * @implNote This implementation seam corresponds to {@code wa_call_dispatch_event} (fn11072) in the
 * wa-voip WASM module {@code ff-tScznZ8P}: the single generic event dispatcher whose {@code should_emit}
 * gate suppresses internal-only ids before the host callback runs. The 172-event id space it dispatches
 * over is {@link CallEventType}; the per-event payload byte layouts are not yet recovered (SPEC section
 * 17.1), so the controller currently emits an empty payload for events whose layout is unresolved.
 */
@FunctionalInterface
public interface Calls2CallEventSink {
    /**
     * Emits one typed in-call event with its serialized payload.
     *
     * <p>The controller calls this for each lifecycle event it raises (a call-state transition, an offer
     * or accept or terminate leg, a fatal error, and so on). The implementer applies the should-emit gate
     * for the event's id and, when the event survives the gate, fans it out to the matching listeners; an
     * event the gate suppresses is dropped. The call is fire-and-forget from the controller's view: it
     * returns once the event has been handed to the bus, and any listener dispatch runs on the bus's own
     * threads rather than on the calling thread.
     *
     * @param eventType the kind of event being emitted; never {@code null}
     * @param payload   the serialized event payload whose layout is determined by {@code eventType};
     *                  never {@code null}, possibly empty when the event carries no payload or its layout
     *                  is not yet recovered
     */
    void emit(CallEventType eventType, byte[] payload);
}
