package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;

/**
 * A {@link ControlCallEvent} reporting that a waiting-room toggle request was acknowledged.
 *
 * <p>When the relay acknowledges a host's request to enable or disable the call's waiting room, the engine
 * emits this carrying the {@link #enabled() applied setting}, so the host can confirm the waiting room is
 * now on or off.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0xa1}
 * ({@link CallEventType#WAITING_ROOM_TOGGLE_ACKED}) of module {@code ff-tScznZ8P}, fed by the parsed
 * waiting-room toggle ack. Cobalt carries the applied enabled flag; the full native payload byte layout is
 * not recovered.
 * @param enabled {@code true} when the waiting room is now enabled, {@code false} when disabled
 */
public record WaitingRoomToggleAcked(boolean enabled) implements ControlCallEvent {
    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#WAITING_ROOM_TOGGLE_ACKED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.WAITING_ROOM_TOGGLE_ACKED;
    }
}
