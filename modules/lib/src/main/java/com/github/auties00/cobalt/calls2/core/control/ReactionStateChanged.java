package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.model.call.datachannel.ReactionInfo;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@link ControlCallEvent} reporting that a participant's reaction overlay should appear or clear.
 *
 * <p>Reactions travel over the call's application-data side-channel rather than over signaling. The engine
 * emits this when a participant's reaction arrives, carrying the originating {@link #participant()
 * participant} device JID and the {@linkplain #reaction() reaction}, and again when that reaction expires,
 * carrying an empty reaction so the host takes the overlay down. The {@link #self() self} flag marks the
 * local user's own reaction echoed back for display.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x91}
 * ({@link CallEventType#REACTION_STATE_CHANGED}) of module {@code ff-tScznZ8P}, emitted on a decoded
 * inbound {@code wa.voip.reactionInfo} and on the reaction-clear timer. Cobalt sources the reaction from
 * the application-data controller's reaction observer, which delivers a present reaction on arrival and an
 * empty one on expiry; the full native payload byte layout for the event is not recovered.
 * @param participant the device JID whose reaction changed; never {@code null}
 * @param reaction    the arrived reaction, or empty when the reaction expired; never {@code null}
 * @param self        {@code true} when the reaction is the local user's own
 */
public record ReactionStateChanged(Jid participant, Optional<ReactionInfo> reaction, boolean self)
        implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code participant} or {@code reaction} is {@code null}
     */
    public ReactionStateChanged {
        Objects.requireNonNull(participant, "participant cannot be null");
        Objects.requireNonNull(reaction, "reaction cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#REACTION_STATE_CHANGED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.REACTION_STATE_CHANGED;
    }
}
