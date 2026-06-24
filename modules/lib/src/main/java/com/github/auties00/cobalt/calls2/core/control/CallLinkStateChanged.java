package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.Calls2CallLinkState;
import com.github.auties00.cobalt.calls2.core.CallEventType;

import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting that the call-link join sub-state advanced.
 *
 * <p>While joining a call through a call link, the engine advances a link sub-state through the query and
 * join legs. It emits this on each change, carrying the new {@link #state() link sub-state}, so the host
 * can reflect the join progress.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x6a}
 * ({@link CallEventType#CALL_LINK_STATE_CHANGED}) of module {@code ff-tScznZ8P}, emitted when the
 * link-state field at {@code call_context[0x28530]} changes (the table at data offset {@code 0x1284fc}).
 * Cobalt carries the typed {@link Calls2CallLinkState}; the full native payload byte layout is not
 * recovered.
 * @param state the new call-link join sub-state; never {@code null}
 */
public record CallLinkStateChanged(Calls2CallLinkState state) implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code state} is {@code null}
     */
    public CallLinkStateChanged {
        Objects.requireNonNull(state, "state cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#CALL_LINK_STATE_CHANGED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.CALL_LINK_STATE_CHANGED;
    }
}
