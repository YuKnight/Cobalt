package com.github.auties00.cobalt.calls.stream.audio;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import com.github.auties00.cobalt.calls.stream.AudioFrame;
import com.github.auties00.cobalt.calls.stream.AudioInput;

/**
 * Discards the remote audio of a call.
 *
 * <p>This is the {@link AudioInput} returned by {@link AudioInput#discard()}: every
 * {@link #offer(AudioFrame)} is dropped and nothing is buffered, so it is the sink to install when a call
 * needs no inbound audio. It is not read from; {@link #read()} blocks until the call ends and then returns
 * {@code null}, so a consumer that drains it in a loop terminates cleanly when the call ends.
 *
 * <p>This implementation class is not instantiated directly by application code; it is obtained through the
 * {@link AudioInput#discard()} factory on the public {@link AudioInput} interface.
 */
public final class DiscardAudioInput implements AudioInput {
    /**
     * Latch counted down by {@link #shutdown()} to release a pending {@link #read()}.
     *
     * <p>The latch starts at one and stays raised for the life of the call. A thread parked in
     * {@link #read()} waits on it, and {@link #shutdown()} counts it down exactly when the call ends so
     * that read observes the end of stream and returns {@code null}.
     */
    private final CountDownLatch done = new CountDownLatch(1);

    /**
     * {@inheritDoc}
     *
     * <p>Drops the frame.
     *
     * @param frame {@inheritDoc}
     * @throws NullPointerException if {@code frame} is {@code null}
     */
    @Override
    public void offer(AudioFrame frame) {
        Objects.requireNonNull(frame, "frame cannot be null");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Blocks until {@link #shutdown()} and then returns {@code null}, since nothing is ever buffered.
     *
     * @return {@code null} once the call has ended
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    @Override
    public AudioFrame read() throws InterruptedException {
        done.await();
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wakes a pending {@link #read()}. Idempotent.
     */
    @Override
    public void shutdown() {
        done.countDown();
    }
}
