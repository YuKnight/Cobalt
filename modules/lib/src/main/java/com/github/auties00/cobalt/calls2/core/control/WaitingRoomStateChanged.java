package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.calls2.signaling.WaitingRoomUser;

import java.util.List;
import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting the current set of participants waiting in a call's lobby.
 *
 * <p>The call host learns who is waiting to be admitted from this event: the engine emits it with the
 * current {@link #waiting() waiting participants} whenever the lobby membership changes, so the host can
 * present an admit or deny choice. An empty list means no participant is currently waiting.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x9e}
 * ({@link CallEventType#WAITING_ROOM_STATE_CHANGED}) of module {@code ff-tScznZ8P}, fed by the inbound
 * {@code <waiting_room>} update the host receives. Cobalt carries the decoded {@link WaitingRoomUser}
 * entries; the full native payload byte layout is not recovered.
 * @param waiting the participants currently waiting in the lobby; never {@code null}, defensively copied
 *                and unmodifiable
 */
public record WaitingRoomStateChanged(List<WaitingRoomUser> waiting) implements ControlCallEvent {
    /**
     * Validates the record components and defensively copies the waiting list.
     *
     * @throws NullPointerException if {@code waiting} is {@code null} or contains a {@code null} element
     */
    public WaitingRoomStateChanged {
        Objects.requireNonNull(waiting, "waiting cannot be null");
        waiting = List.copyOf(waiting);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#WAITING_ROOM_STATE_CHANGED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.WAITING_ROOM_STATE_CHANGED;
    }
}
