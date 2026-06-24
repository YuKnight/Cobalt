package com.github.auties00.cobalt.calls2.dsp;

/**
 * One audio/video synchronisation correction the {@link VideoTimingController} pushes toward the audio
 * jitter buffer to keep lip sync.
 *
 * <p>The video render path runs at a longer playout delay than audio because video jitter and decode
 * cost more time; left alone the two streams drift apart. The timing controller measures the relative
 * delay between the video frame it is about to render and the audio sample playing at the same wall
 * clock, and when that relative delay exceeds the configured tolerance it emits a correction asking the
 * audio buffer to add (positive) or remove (negative) playout delay so audio slides back into sync with
 * video. The {@link #relativeDelayMs()} is the raw measured offset; the {@link #correctionMs()} is the
 * bounded, weighted adjustment the audio buffer should actually apply this round, already clamped to the
 * per-update step and the maximum audio sync delay.
 *
 * <p>A correction of zero is a valid no-op the controller emits to signal that the streams are within
 * tolerance, so a consumer may treat any feedback as a heartbeat. The record is immutable and carries
 * no clock; the timing controller stamps the values from its own measurement.
 *
 * @param relativeDelayMs the measured video-minus-audio playout offset in milliseconds, positive when
 *                        video lags audio
 * @param correctionMs    the bounded delay adjustment the audio buffer should apply this round, in
 *                        milliseconds, positive to add audio delay and negative to remove it
 * @implNote This implementation models the payload of {@code pjmedia_stream_adjust_from_avsync_feedback}
 * fed by {@code WaWebrtcVideoJitterBufferController} in the wa-voip WASM module {@code ff-tScznZ8P}
 * ({@code rev-rtc-dsp} avsync host-API: {@code mvp->enable_avsync_feedback_to_audio},
 * {@code mvp->avsync_feedback_to_audio_{fraction,weight,min/max_threshold_ms,update_interval_ms}},
 * {@code mvp->enable_avsync_feedback_ingestion_neteq}). The native path computes a relative delay,
 * weights it, and clamps it to a maximum audio sync delay before the audio NetEq ingests it; this record
 * carries the already-bounded value so the NetEq side applies it directly. The weighting and clamp
 * constants live in {@link VideoTimingController}.
 */
public record AvSyncFeedback(double relativeDelayMs, double correctionMs) {
}
