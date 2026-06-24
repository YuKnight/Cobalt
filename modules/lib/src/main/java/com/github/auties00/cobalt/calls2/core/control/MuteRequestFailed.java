package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting that a peer-mute request the local user sent failed to be confirmed.
 *
 * <p>When the local user (an admin) asks a participant to mute, the engine retries the request and, if it
 * is never confirmed, emits this failure carrying the {@link #target() target participant} the request was
 * addressed to. The host can surface that the request did not take effect. This is the failure
 * counterpart of the outbound peer-mute request, distinct from the inbound
 * {@link MuteByAnotherParticipant} and from the resulting {@link MuteStateChanged}.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x6c}
 * ({@link CallEventType#MUTE_REQUEST_FAILED}) of module {@code ff-tScznZ8P}, emitted when the outbound
 * peer-mute request exhausts its retry budget without confirmation. The full native payload byte layout
 * for this event is not recovered; Cobalt carries the target participant the listener surface needs.
 * @param target the device JID of the participant the failed mute request was addressed to; never
 *               {@code null}
 */
public record MuteRequestFailed(Jid target) implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code target} is {@code null}
     */
    public MuteRequestFailed {
        Objects.requireNonNull(target, "target cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#MUTE_REQUEST_FAILED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.MUTE_REQUEST_FAILED;
    }
}
