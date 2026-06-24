package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting that a participant raised or lowered a hand.
 *
 * <p>In a group call a participant can raise a hand to ask to speak. The engine emits this when a
 * participant's hand state changes, carrying the {@link #participant() participant} device JID and the new
 * {@link #raised() raised} flag, and a {@link #self() self} flag distinguishing the local user's own
 * gesture. The raised state also feeds the grid-ranking comparator so a raised hand sorts first.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x94}
 * ({@link CallEventType#RAISE_HAND_STATE_CHANGED}) of module {@code ff-tScznZ8P}, emitted on a local
 * raise-hand action and on an inbound {@code <raise_hand>} report. The full native payload byte layout for
 * this event is not recovered; Cobalt carries the participant and the raised flag the listener surface
 * needs.
 * @param participant the device JID whose hand state changed; never {@code null}
 * @param raised      {@code true} when the hand is now raised, {@code false} when lowered
 * @param self        {@code true} when the participant is the local user
 */
public record RaiseHandStateChanged(Jid participant, boolean raised, boolean self) implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code participant} is {@code null}
     */
    public RaiseHandStateChanged {
        Objects.requireNonNull(participant, "participant cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#RAISE_HAND_STATE_CHANGED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.RAISE_HAND_STATE_CHANGED;
    }
}
