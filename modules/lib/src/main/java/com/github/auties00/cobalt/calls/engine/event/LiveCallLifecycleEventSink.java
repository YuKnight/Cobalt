package com.github.auties00.cobalt.calls.engine.event;

import java.util.Objects;

/**
 * Records each lifecycle event the controller raises as a trace log line.
 *
 * <p>The typed events the application observes reach registered listeners through {@link LiveCallEventBus},
 * which the call service and the control units drive during a call. This sink is a separate diagnostic tap:
 * it receives the same {@link CallEventType} stream the {@code LifecycleController} emits and writes the
 * event id to the logger without decoding the opaque payload, so it never surfaces anything to a listener.
 */
public final class LiveCallLifecycleEventSink implements CallLifecycleEventSink {
    /**
     * Logger that receives one trace line per emitted lifecycle event, keyed by this class's name.
     */
    private static final System.Logger LOGGER = System.getLogger(LiveCallLifecycleEventSink.class.getName());

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation writes the {@link CallEventType} to {@link #LOGGER} at trace level and
     * discards the {@code payload}; it applies no emit gate and dispatches to no listener, since the typed
     * listener events fan out from the call service and the control units through {@link LiveCallEventBus}
     * rather than from this diagnostic sink.
     */
    @Override
    public void emit(CallEventType eventType, byte[] payload) {
        Objects.requireNonNull(eventType, "eventType cannot be null");
        LOGGER.log(System.Logger.Level.TRACE, "calls lifecycle event {0}", eventType);
    }
}
