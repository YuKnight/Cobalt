package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting that a participant's mute state changed.
 *
 * <p>The engine emits this whenever a participant's audio mute state is observed to change: the local
 * user toggling their own microphone, or an inbound {@code mute_v2} self-state report from a peer. It
 * carries the affected {@link #participant() participant} device JID and the new {@link #muted() mute
 * state}, and the {@link #self() self} flag distinguishes the local user's own change from a peer's.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x49}
 * ({@link CallEventType#MUTE_STATE_CHANGED}) of module {@code ff-tScznZ8P}, emitted both when the local
 * mute toggles and when an inbound {@code mute_v2} self-state report updates a peer's mute flag. The full
 * native payload byte layout for this event is not recovered; Cobalt carries the participant and mute
 * flag the listener surface needs.
 * @param participant the device JID whose mute state changed; never {@code null}
 * @param muted       {@code true} when the participant is now muted, {@code false} when unmuted
 * @param self        {@code true} when the participant is the local user
 */
public record MuteStateChanged(Jid participant, boolean muted, boolean self) implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code participant} is {@code null}
     */
    public MuteStateChanged {
        Objects.requireNonNull(participant, "participant cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#MUTE_STATE_CHANGED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.MUTE_STATE_CHANGED;
    }
}
