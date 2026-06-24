package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.calls2.signaling.WaitingRoomUser;

import java.util.List;
import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting that a waiting-room admit request was acknowledged.
 *
 * <p>When the relay acknowledges a host's request to admit waiting-room participants, the engine emits
 * this carrying the {@link #admitted() admitted participants} the ack echoed back, so the host can confirm
 * which queued participants were released into the call. An empty list reflects the admit-all action.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0xa5}
 * ({@link CallEventType#WAITING_ROOM_ADMIT_ACKED}) of module {@code ff-tScznZ8P}, fed by the parsed
 * waiting-room admit ack. Cobalt carries the echoed {@link WaitingRoomUser} entries; the full native
 * payload byte layout is not recovered.
 * @param admitted the admitted participants the ack echoed; never {@code null}, defensively copied and
 *                 unmodifiable
 */
public record WaitingRoomAdmitAcked(List<WaitingRoomUser> admitted) implements ControlCallEvent {
    /**
     * Validates the record components and defensively copies the admitted list.
     *
     * @throws NullPointerException if {@code admitted} is {@code null} or contains a {@code null} element
     */
    public WaitingRoomAdmitAcked {
        Objects.requireNonNull(admitted, "admitted cannot be null");
        admitted = List.copyOf(admitted);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#WAITING_ROOM_ADMIT_ACKED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.WAITING_ROOM_ADMIT_ACKED;
    }
}
