package com.github.auties00.cobalt.calls2.stream;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Transmits continuous silence as the local audio of a call.
 *
 * <p>This is the device-backed {@link AudioOutput} returned by {@link AudioOutput#silence()}. Every
 * {@link #take()} yields a fresh {@link AudioFrame} of all-zero signed 16-bit samples with a
 * monotonically non-decreasing presentation timestamp. The stream never ends on its own; it keeps
 * producing silence until the call engine shuts it down. The default profile emits 160-sample, 10 ms
 * frames matching the call media format.
 *
 * <p>The emitted PCM is true digital silence, not comfort noise; a producer that needs audible comfort
 * noise should layer a noise generator on top. Because {@link #take()} never returns {@code null}, the
 * call treats this as an endless stream, so the engine swaps it out rather than relying on it to signal
 * end-of-stream.
 *
 * @implNote This implementation is the calls2 port of the legacy silence capture stream; the per-frame
 * timestamp it advances is denominated in the {@link AudioFrame#ptsMicros()} microsecond clock rather than
 * the legacy millisecond clock.
 */
public final class SilenceAudioOutput extends BufferedAudioOutput {
    /**
     * Holds the number of samples in each emitted frame.
     */
    private final int frameSize;

    /**
     * Holds the duration represented by one frame, in microseconds, by which each frame's presentation
     * timestamp advances.
     */
    private final long frameDurationMicros;

    /**
     * Holds the presentation timestamp, in microseconds, of the next frame, advanced atomically so the
     * stream may be drained from any thread.
     */
    private final AtomicLong ptsMicros = new AtomicLong();

    /**
     * Constructs a silence stream for the default call profile.
     *
     * <p>Equivalent to {@link #SilenceAudioOutput(int, long)} with 160 samples and a 10000 microsecond
     * (10 ms) frame duration, matching the call media cadence.
     */
    public SilenceAudioOutput() {
        this(160, 10_000);
    }

    /**
     * Constructs a silence stream with an explicit frame geometry.
     *
     * @param frameSize           the number of samples per frame
     * @param frameDurationMicros the duration of each frame in microseconds
     * @throws IllegalArgumentException if {@code frameSize} or {@code frameDurationMicros} is less than
     *                                  {@code 1}
     */
    public SilenceAudioOutput(int frameSize, long frameDurationMicros) {
        if (frameSize < 1) {
            throw new IllegalArgumentException("frameSize must be >= 1");
        }
        if (frameDurationMicros < 1) {
            throw new IllegalArgumentException("frameDurationMicros must be >= 1");
        }
        this.frameSize = frameSize;
        this.frameDurationMicros = frameDurationMicros;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns a fresh all-zero frame and advances the presentation timestamp by the configured frame
     * duration. Never returns {@code null}, so the stream ends only when {@link #shutdown()} runs.
     *
     * @return a new silent frame; never {@code null}
     * @implNote This implementation returns one frame per call with no sleep: the call engine's capture
     * loop paces outbound audio to wall-clock using each frame's running presentation timestamp, so the
     * silence is transmitted at its natural rate without this stream having to sleep.
     */
    @Override
    public AudioFrame take() {
        if (closed.get()) {
            return null;
        }
        var pts = ptsMicros.getAndAdd(frameDurationMicros);
        return new AudioFrame(new short[frameSize], pts);
    }
}
