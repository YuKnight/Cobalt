package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.core.CallEventType;

import java.util.Objects;

/**
 * A {@link ControlCallEvent} instructing the host which call tone to play, if any.
 *
 * <p>The engine plays at most one call tone at a time, the highest-priority tone among those currently
 * active. When that selection changes, the engine emits this carrying the {@link #tone() tone} the host
 * should now play; {@link ToneType#NONE} instructs the host to stop playing any tone. The host owns the
 * actual audio playback, so this event is the engine's single point of control over it.
 *
 * @implNote This implementation models the payload of the wa-voip event {@code 0x5f}
 * ({@link CallEventType#PLAY_CALL_TONE}) of module {@code ff-tScznZ8P}, emitted by the tone-priority
 * selector when the highest set bit of the tone bitmask changes. Cobalt carries the resolved
 * {@link ToneType} rather than the raw bitmask, since the engine has already reduced the mask to the one
 * tone to play; the full native payload byte layout is not recovered.
 * @param tone the tone the host should play, or {@link ToneType#NONE} to play none; never {@code null}
 */
public record PlayCallTone(ToneType tone) implements ControlCallEvent {
    /**
     * Validates the record components.
     *
     * @throws NullPointerException if {@code tone} is {@code null}
     */
    public PlayCallTone {
        Objects.requireNonNull(tone, "tone cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link CallEventType#PLAY_CALL_TONE}
     */
    @Override
    public CallEventType type() {
        return CallEventType.PLAY_CALL_TONE;
    }
}
