package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting that another participant requested the local user to mute.
 *
 * <p>In a group call an admin can ask a participant to mute itself; the engine emits this when such a
 * request arrives addressed to the local user, carrying the {@link #requester() requesting participant}
 * device JID. The host typically surfaces a prompt or mutes automatically; acting on the request mutes
 * the local microphone and emits a {@link MuteStateChanged}. This is distinct from the local user muting
 * itself: it is the inbound peer-mute request, not the resulting state change.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x66}
 * ({@link CallEventType#MUTE_BY_ANOTHER_PARTICIPANT}) of module {@code ff-tScznZ8P}, emitted when an
 * inbound {@code mute_v2} carrying {@code request-state} names the local user. The full native payload
 * byte layout for this event is not recovered; Cobalt carries the requesting participant the listener
 * surface needs.
 * @param requester the device JID of the participant that requested the local user to mute; never
 *                  {@code null}
 */
public record MuteByAnotherParticipant(Jid requester) implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code requester} is {@code null}
     */
    public MuteByAnotherParticipant {
        Objects.requireNonNull(requester, "requester cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#MUTE_BY_ANOTHER_PARTICIPANT}
     */
    @Override
    public CallEventType type() {
        return CallEventType.MUTE_BY_ANOTHER_PARTICIPANT;
    }
}
