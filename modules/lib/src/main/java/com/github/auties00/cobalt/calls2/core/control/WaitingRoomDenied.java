package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;

/**
 * A {@link ControlCallEvent} reporting that the local user's waiting-room admission was denied.
 *
 * <p>When the local user is waiting in a call-link lobby and the host denies admission, the engine emits
 * this so the host can inform the user it will not be joining. It carries no further detail: the denial is
 * terminal for the current join attempt.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x9d}
 * ({@link CallEventType#WAITING_ROOM_DENIED}) of module {@code ff-tScznZ8P}, emitted when an inbound
 * waiting-room deny names the local user. The native event carries no recovered payload fields beyond the
 * denial itself, so this is a marker event.
 */
public record WaitingRoomDenied() implements ControlCallEvent {
    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#WAITING_ROOM_DENIED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.WAITING_ROOM_DENIED;
    }
}
