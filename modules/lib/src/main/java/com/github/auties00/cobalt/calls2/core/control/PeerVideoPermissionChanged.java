package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting that a peer's permission to send video changed.
 *
 * <p>In a managed call a peer may be granted or denied permission to turn its camera on; the engine emits
 * this when that permission changes for a {@link #participant() participant}, carrying the new
 * {@link #allowed() allowed} flag. It is distinct from {@link VideoStateChanged}: this reports whether the
 * peer is permitted to send video, not whether it currently is.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x93}
 * ({@link CallEventType#PEER_VIDEO_PERMISSION_CHANGED}) of module {@code ff-tScznZ8P}. The full native
 * payload byte layout for this event is not recovered; Cobalt carries the participant and the permission
 * flag the listener surface needs.
 * @param participant the device JID whose video permission changed; never {@code null}
 * @param allowed     {@code true} when the participant is now permitted to send video
 */
public record PeerVideoPermissionChanged(Jid participant, boolean allowed) implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code participant} is {@code null}
     */
    public PeerVideoPermissionChanged {
        Objects.requireNonNull(participant, "participant cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#PEER_VIDEO_PERMISSION_CHANGED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.PEER_VIDEO_PERMISSION_CHANGED;
    }
}
