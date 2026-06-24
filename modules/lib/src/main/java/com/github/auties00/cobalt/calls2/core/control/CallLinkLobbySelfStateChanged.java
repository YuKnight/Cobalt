package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;

import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting that the local user's own state in a call-link lobby changed.
 *
 * <p>When the local user joins a waiting-room-gated call link, it sits in the lobby until the host admits
 * it. The engine emits this as the local user's own lobby state advances, carrying the new
 * {@link #state() waiting-room state}, so the host can show whether the local user is still waiting, has
 * been admitted, or has been turned away.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x8b}
 * ({@link CallEventType#CALL_LINK_LOBBY_SELF_STATE_CHANGED}) of module {@code ff-tScznZ8P} (whose recovered
 * display name carries the engine typo "Self Sate Changed"). Cobalt carries the typed
 * {@link WaitingRoomUserState}; the full native payload byte layout is not recovered.
 * @param state the local user's new lobby state; never {@code null}
 */
public record CallLinkLobbySelfStateChanged(WaitingRoomUserState state) implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code state} is {@code null}
     */
    public CallLinkLobbySelfStateChanged {
        Objects.requireNonNull(state, "state cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#CALL_LINK_LOBBY_SELF_STATE_CHANGED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.CALL_LINK_LOBBY_SELF_STATE_CHANGED;
    }
}
