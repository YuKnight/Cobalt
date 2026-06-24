package com.github.auties00.cobalt.calls2.core.control;

/**
 * The egress an in-call controller emits a {@link ControlCallEvent} into for fan-out to listeners.
 *
 * <p>When an in-call control operation changes call-visible state, the controller publishes a typed
 * {@link ControlCallEvent} describing the change. This seam hides the should-emit gate and the per-listener
 * virtual-thread fan-out the event bus performs: the bus implements this by passing the event through its
 * gate and, when the event survives, dispatching it to each matching listener on its own virtual thread.
 * A controller therefore depends only on this one-method sink, never on the bus implementation, and a
 * controller can be exercised in isolation by capturing the events it emits.
 *
 * @implSpec An implementation MUST accept any {@link ControlCallEvent} without throwing and MUST NOT block
 * the caller: the emit happens on the controller's own thread (a timer callback or an operation call), and
 * the fan-out to listeners is dispatched elsewhere so a slow listener never stalls the controller. An
 * implementation MAY suppress an event whose {@link ControlCallEvent#type()} the engine marks internal-only,
 * matching the native should-emit gate.
 * @implNote This implementation seam stands in for the native generic event dispatcher of module
 * {@code ff-tScznZ8P} ({@code wa_call_dispatch_event} fn11072) with its should-emit gate; Cobalt routes the
 * surviving events onto the {@code WhatsAppListener} bus with one fresh virtual thread per matching
 * listener rather than the native 127-slot pjlib ring, so this seam carries only the single emit downcall.
 * @see ControlCallEvent
 */
@FunctionalInterface
public interface CallEventSink {
    /**
     * Emits a control event for gating and fan-out to listeners.
     *
     * @implSpec An implementation MUST NOT block the caller and MUST NOT throw for a well-formed event.
     * @param event the event to emit; never {@code null}
     */
    void emit(ControlCallEvent event);
}
