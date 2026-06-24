package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.VideoStreamState;
import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting that a participant's video stream state changed.
 *
 * <p>The engine emits this whenever a participant's {@link VideoStreamState} changes: the simple camera
 * on/off lifecycle and every step of the video-upgrade negotiation (request, accept, reject, cancel, and
 * their timeout variants). It carries the affected {@link #participant() participant} device JID, the new
 * {@link #state() video state}, and a {@link #self() self} flag distinguishing the local user's own change
 * from a peer's. This is the real video-state mechanism that supersedes the deprecated video-upgrade
 * string facade.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x92}
 * ({@link CallEventType#VIDEO_STATE_CHANGED}) of module {@code ff-tScznZ8P}, emitted when the
 * {@code kVideoState*} machine transitions for any participant, fed both by a local camera or upgrade
 * action and by an inbound {@code video_state} report. The full native payload byte layout for this event
 * is not recovered; Cobalt carries the participant and the {@link VideoStreamState} the listener surface
 * needs.
 * @param participant the device JID whose video state changed; never {@code null}
 * @param state       the new video stream state; never {@code null}
 * @param self        {@code true} when the participant is the local user
 */
public record VideoStateChanged(Jid participant, VideoStreamState state, boolean self) implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code participant} or {@code state} is {@code null}
     */
    public VideoStateChanged {
        Objects.requireNonNull(participant, "participant cannot be null");
        Objects.requireNonNull(state, "state cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#VIDEO_STATE_CHANGED}
     */
    @Override
    public CallEventType type() {
        return CallEventType.VIDEO_STATE_CHANGED;
    }
}
