package com.github.auties00.cobalt.calls2.dsp;

import com.github.auties00.cobalt.calls2.media.video.EncodedVideoFrame;

/**
 * Buffers received video frames and releases each at its scheduled render time so the renderer sees a
 * smooth, jitter-absorbed stream.
 *
 * <p>The jitter buffer is the video counterpart of the audio {@code NetEq}: received frames arrive out
 * of order and with variable delay, and the buffer holds them until the {@link VideoTimingController}
 * says each is due. {@link #insert(EncodedVideoFrame, long, long, int)} adds a received frame with its
 * arrival time, capture timestamp, and sequence number, feeding the jitter estimate. {@link #poll(long)}
 * is called once per render tick and returns the frame due now, or {@code null} when the next frame is
 * not yet due, mirroring the WebRTC frame-buffer pull. The buffer reconciles the released frame's render
 * time with audio through the {@link AvSyncFeedbackSink} supplied at construction so lip sync holds.
 *
 * <p>Insertion runs on the transport receive thread and polling on the render thread; the
 * implementation owns the concurrency between them. The type is sealed so the engine can match
 * exhaustively over the single production implementation.
 *
 * @implSpec An implementation must release frames in capture-timestamp order, never release a frame
 * before its render time computed by the timing controller, drop a frame whose render time has already
 * passed by more than the maximum video delay rather than stalling the stream, and reset the jitter
 * estimator and timing controller when it detects render timing that cannot be satisfied. {@link #poll(long)}
 * must be non-blocking and return {@code null} when no frame is due.
 * @implNote The production implementation {@link LiveVideoJitterBuffer} ports
 * {@code WaWebrtcVideoJitterBufferController} ({@code video/video_stream_buffer_controller.cc},
 * {@code OnFrameToDecode} fn10480 of the wa-voip WASM module {@code ff-tScznZ8P}, {@code rev-rtc-dsp}).
 */
public sealed interface VideoJitterBuffer permits LiveVideoJitterBuffer {
    /**
     * Inserts a received frame, recording its timing and feeding the jitter estimate.
     *
     * <p>The frame is ordered into the buffer by its capture timestamp; a duplicate of an
     * already-buffered or already-released frame is discarded. The arrival time and capture timestamp
     * update the jitter estimator, and the sequence number tracks gaps for the retransmission gate.
     *
     * @param frame              the received compressed frame; never {@code null}
     * @param arrivalMs          the local arrival time of the frame in milliseconds, from a monotonic
     *                           source
     * @param captureRtpTimestamp the frame's 90 kHz capture RTP timestamp
     * @param sequenceNumber     the frame's RTP sequence number, used to detect gaps
     * @return {@code true} if the frame was buffered, {@code false} if it was discarded as a duplicate
     *         or as already released
     */
    boolean insert(EncodedVideoFrame frame, long arrivalMs, long captureRtpTimestamp, int sequenceNumber);

    /**
     * Returns the frame due for rendering at the given time, or {@code null} when none is due.
     *
     * <p>Releases the earliest buffered frame whose render time computed by the timing controller has
     * arrived; returns {@code null} when the buffer is empty or the earliest frame is not yet due. A
     * frame whose render time has passed by more than the maximum video delay is dropped and the next is
     * considered.
     *
     * @param nowMs the current render-thread time in milliseconds, from a monotonic source
     * @return the released frame and its render time, or {@code null} when no frame is due
     */
    ReleasedFrame poll(long nowMs);

    /**
     * Reports the number of frames currently buffered awaiting their render time.
     *
     * @return the count of buffered frames
     */
    int bufferedFrameCount();

    /**
     * Reports the receiver-side packet-loss-ratio estimate, in the range {@code [0, 1]}.
     *
     * <p>Derived from the sequence-number gaps the buffer observes; the transport feeds it back to the
     * sender's rate control.
     *
     * @return the observed loss ratio over the recent scan window
     */
    double packetLossRatio();

    /**
     * Drops all buffered frames and resets the jitter estimate and timing controller.
     *
     * <p>Called on a stream reset, a resolution change that invalidates the estimate, or render timing
     * that cannot be satisfied.
     */
    void reset();

    /**
     * One frame released from the buffer together with the render instant it was scheduled for.
     *
     * <p>The {@link #renderTimeMs()} is the local-clock instant the renderer should display the frame;
     * a release at or after that instant is on time, and the renderer may present immediately.
     *
     * @param frame        the released compressed frame; never {@code null}
     * @param renderTimeMs the local-clock render instant the frame was scheduled for, in milliseconds
     */
    record ReleasedFrame(EncodedVideoFrame frame, long renderTimeMs) {
    }
}
