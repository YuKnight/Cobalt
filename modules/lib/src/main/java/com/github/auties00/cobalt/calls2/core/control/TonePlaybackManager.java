package com.github.auties00.cobalt.calls2.core.control;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Tracks which call tones are active and tells the host which single tone to play.
 *
 * <p>Several call conditions can each want a tone at once: a call connecting, a peer alerting, a busy
 * signal. This manager keeps the set of active tones as a bitmask and plays only the highest-priority one,
 * matching the engine, so a busy signal overrides a ringback and a ringback overrides a connecting tone.
 * {@link #activate(ToneType)} sets a tone's bit and {@link #deactivate(ToneType)} clears it; whenever the
 * highest-priority active tone changes, the manager emits a {@link PlayCallTone} carrying the tone the host
 * should now play, or {@link ToneType#NONE} when the last tone clears.
 *
 * <p>The bitmask and the last-played tone are guarded by a lock so a concurrent activate and deactivate
 * never emit a tone out of order. The manager is bound to its event sink at construction; it owns no
 * timers. The host performs the actual audio playback; this manager is the engine's single point of
 * control over which tone is audible.
 *
 * @implNote This implementation reproduces the tone-priority selector of the wa-voip WASM module
 * {@code ff-tScznZ8P}: tones are ORed into a bitmask by their {@link ToneType#bit() priority bit} and the
 * selector plays the highest set bit, emitting event {@code 0x5f} ({@code PlayCallTone}) when the
 * selection changes. Cobalt keeps the bitmask, resolves the active tone through
 * {@link ToneType#highestPriority(int)}, and emits the resolved {@link ToneType} rather than the raw mask.
 * The info-mutex is replaced by a {@link ReentrantLock} per the threading design.
 */
public final class TonePlaybackManager {
    /**
     * The event sink the tone-to-play event is emitted into.
     */
    private final CallEventSink events;

    /**
     * Guards the active-tone bitmask and the last-played tone.
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * The bitmask of currently active tones, an OR of their {@linkplain ToneType#bit() priority bits}.
     */
    private int activeMask;

    /**
     * The tone last reported to the host through a {@link PlayCallTone} event.
     */
    private ToneType playing = ToneType.NONE;

    /**
     * Constructs a tone-playback manager bound to its event sink.
     *
     * @param events the event sink to emit the tone-to-play event into; never {@code null}
     * @throws NullPointerException if {@code events} is {@code null}
     */
    public TonePlaybackManager(CallEventSink events) {
        this.events = Objects.requireNonNull(events, "events cannot be null");
    }

    /**
     * Activates a tone, emitting a new tone-to-play event if it becomes the highest-priority tone.
     *
     * <p>Sets the tone's {@linkplain ToneType#bit() priority bit} in the active mask and re-evaluates the
     * highest-priority active tone; a change emits a {@link PlayCallTone}. Activating {@link ToneType#NONE}
     * is a no-op, since it carries no bit.
     *
     * @param tone the tone to activate; never {@code null}
     * @throws NullPointerException if {@code tone} is {@code null}
     */
    public void activate(ToneType tone) {
        Objects.requireNonNull(tone, "tone cannot be null");
        lock.lock();
        try {
            activeMask |= tone.bit();
            reevaluate();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Deactivates a tone, emitting a new tone-to-play event if the highest-priority tone changes.
     *
     * <p>Clears the tone's {@linkplain ToneType#bit() priority bit} from the active mask and re-evaluates
     * the highest-priority active tone; a change emits a {@link PlayCallTone}, which is
     * {@link ToneType#NONE} when the last tone clears. Deactivating {@link ToneType#NONE} is a no-op.
     *
     * @param tone the tone to deactivate; never {@code null}
     * @throws NullPointerException if {@code tone} is {@code null}
     */
    public void deactivate(ToneType tone) {
        Objects.requireNonNull(tone, "tone cannot be null");
        lock.lock();
        try {
            activeMask &= ~tone.bit();
            reevaluate();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Clears every active tone, emitting a stop ({@link ToneType#NONE}) if a tone was playing.
     *
     * <p>Used at teardown so no tone is left audible; emits a {@link PlayCallTone} carrying
     * {@link ToneType#NONE} only when a tone was playing.
     */
    public void clear() {
        lock.lock();
        try {
            activeMask = 0;
            reevaluate();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the tone currently selected for playback.
     *
     * @return the tone the host should be playing, or {@link ToneType#NONE} when none; never {@code null}
     */
    public ToneType playing() {
        lock.lock();
        try {
            return playing;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Re-evaluates the highest-priority active tone and emits a change.
     *
     * <p>Resolves the highest set bit of the active mask through {@link ToneType#highestPriority(int)};
     * when it differs from the last-played tone, records it and emits a {@link PlayCallTone}. Called under
     * the lock.
     */
    private void reevaluate() {
        var next = ToneType.highestPriority(activeMask);
        if (next != playing) {
            playing = next;
            events.emit(new PlayCallTone(next));
        }
    }
}
