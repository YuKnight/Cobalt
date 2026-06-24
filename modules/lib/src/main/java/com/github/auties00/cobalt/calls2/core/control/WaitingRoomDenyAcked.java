package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.calls2.signaling.WaitingRoomUser;

import java.util.List;
import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting that a waiting-room deny request was acknowledged.
 *
 * <p>When the relay acknowledges a host's request to deny waiting-room participants, the engine emits this
 * carrying the {@link #denied() denied participants} the ack echoed back, so the host can confirm which
 * queued participants were turned away.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0xa6}
 * ({@link CallEventType#WAITING_ROOM_DENY_ACKED}) of module {@code ff-tScznZ8P}, fed by the parsed
 * waiting-room deny ack. Cobalt carries the echoed {@link WaitingRoomUser} entries; the full native
 * payload byte layout is not recovered.
 * @param denied the denied participants the ack echoed; never {@code null}, defensively copied and
 *               unmodifiable
 */
public record WaitingRoomDenyAcked(List<WaitingRoomUser> denied) implements ControlCallEvent {
    /**
     * Validates the record components and defensively copies the denied list.
     *
     * @throws NullPointerException if {@code denied} is {@code null} or contains a {@code null} element
     */
    public WaitingRoomDenyAcked {
        Objects.requireNonNull(denied, "denied cannot be null");
        denied = List.copyOf(denied);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#WAITING_ROOM_DENY_ACKED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.WAITING_ROOM_DENY_ACKED;
    }
}
