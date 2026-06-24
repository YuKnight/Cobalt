package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;

/**
 * A {@link ControlCallEvent} reporting that a participant's screen-share stream changed.
 *
 * <p>The engine emits this when a participant starts, stops, or fails to start screen sharing. It carries
 * the {@link #sharer() sharer} device JID, the new {@link #state() screen-share state}, and the negotiated
 * {@link #version() screen-share protocol version} distinguishing the single-stream V2 path from the
 * dual-stream V3 path.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x74}
 * ({@link CallEventType#SCREEN_SHARE}) of module {@code ff-tScznZ8P}, emitted by
 * {@code send_screen_share_event} (state, sharer_jid, version, reason) in {@code media/screen_share.cc}.
 * The full native payload byte layout for this event is not recovered beyond that call site; Cobalt
 * carries the sharer, state, and version the listener surface needs.
 * @param sharer  the device JID of the participant whose screen-share state changed; never {@code null}
 * @param state   the new screen-share state; never {@code null}
 * @param version the negotiated screen-share protocol version
 */
public record ScreenShareEvent(Jid sharer, ScreenShareState state, int version) implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code sharer} or {@code state} is {@code null}
     */
    public ScreenShareEvent {
        Objects.requireNonNull(sharer, "sharer cannot be null");
        Objects.requireNonNull(state, "state cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#SCREEN_SHARE}
     */
    @Override
    public CallEventType type() {
        return CallEventType.SCREEN_SHARE;
    }
}
