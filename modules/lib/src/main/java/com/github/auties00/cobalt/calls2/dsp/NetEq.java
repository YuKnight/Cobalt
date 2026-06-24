package com.github.auties00.cobalt.calls2.dsp;

import com.github.auties00.cobalt.calls2.media.audio.AudioDecoderReceiver;
import com.github.auties00.cobalt.calls2.stream.AudioFrame;

import java.util.List;

/**
 * The adaptive audio jitter buffer, the receiver-side resilience stage that turns a jittery, lossy stream
 * of received audio packets into a smooth playout at a fixed render cadence.
 *
 * <p>A jitter buffer absorbs the variation in packet arrival timing and conceals lost packets so the
 * playback path can pull one audio frame every get period regardless of network conditions.
 * {@link #insertPacket(RtpAudioPacket)} buffers a received packet and updates the delay and NACK
 * estimators; {@link #getAudio()} is the per-period pull that asks the decision logic for an operation,
 * decodes or conceals one frame, and returns it; {@link #pendingNackList(long)} reports the sequence
 * numbers to request a retransmission for; {@link #setMinimumDelayMillis(int)} and
 * {@link #setMaximumDelayMillis(int)} bound the target playout delay; {@link #ingestAvSyncFeedbackMillis(int)}
 * accepts a lip-sync correction from the video timing path; {@link #flush()} drains the buffer on a stall;
 * and {@link #statistics()} snapshots the lifetime counters for telemetry. The {@link AudioDecoderReceiver}
 * the playback pump pulls from is a thin adapter over {@link #getAudio()}, copying each served frame into the
 * pump's scratch array.
 *
 * <p>The hierarchy is sealed for exhaustive matching: {@link LiveNetEq} is the sole production
 * implementation. An implementation owns the concurrency between {@link #insertPacket(RtpAudioPacket)} on
 * the transport receive thread and {@link #getAudio()} on the playback pull thread, mirroring the WebRTC
 * NetEq design where insert and get-audio are separately locked.
 *
 * @implNote This interface is the facade over the {@code concerto::NetEqImpl} of the wa-voip WASM module
 * {@code ff-tScznZ8P} ({@code rev-rtc-dsp}): {@code InsertPacketInternal} (fn7503) is
 * {@link #insertPacket(RtpAudioPacket)} and {@code GetAudioInternal} (fn7521) is {@link #getAudio()}. The
 * avsync feedback ingestion ({@code mvp->enable_avsync_feedback_ingestion_neteq}) is
 * {@link #ingestAvSyncFeedbackMillis(int)}, and the min/max delay setters mirror
 * {@code pjmedia_stream_set_neteq_min_delay}. The native NetEq is WhatsApp's fork of WebRTC NetEq and has
 * no C ABI, so the decision/state/framing glue is ported in Java while the per-sample decode reaches the
 * codec through the {@link AudioDecoderReceiver.FrameDecoder} seam.
 */
public sealed interface NetEq permits LiveNetEq {
    /**
     * Inserts one received audio packet into the buffer and updates the delay and NACK estimators.
     *
     * @implSpec Implementations must store the packet for playout, discarding it if it duplicates a
     * buffered packet or precedes the playout cursor, and must update the inter-arrival histogram and the
     * NACK tracker from its sequence number and arrival time. This method may be called from the transport
     * receive thread concurrently with {@link #getAudio()}.
     *
     * @param packet the received packet to buffer; never {@code null}
     * @throws NullPointerException if {@code packet} is {@code null}
     */
    void insertPacket(RtpAudioPacket packet);

    /**
     * Pulls one rendered audio frame, the per-get-period decode-or-conceal cycle.
     *
     * @implSpec Implementations must ask the decision logic for an operation, render exactly one frame by
     * decoding the scheduled packet, concealing a gap, or generating comfort noise, and return the frame
     * with a presentation timestamp. This method is called from the playback pull thread once per get
     * period.
     *
     * @return the rendered audio frame; never {@code null}
     */
    AudioFrame getAudio();

    /**
     * Returns the sequence numbers to request a retransmission for at the given time.
     *
     * @implSpec Implementations must return the sequence numbers the NACK tracker judges still missing and
     * old enough to retransmit, or an empty list when none are due or the link is too slow for
     * retransmission to help.
     *
     * @param nowMillis the current local monotonic time in milliseconds
     * @return the sequence numbers to NACK, ascending; never {@code null}, possibly empty
     */
    List<Integer> pendingNackList(long nowMillis);

    /**
     * Sets the lower bound on the target playout delay in milliseconds.
     *
     * @implSpec Implementations must clamp the estimated target level no lower than this bound from the next
     * estimate onward.
     *
     * @param minimumDelayMillis the minimum target playout delay; clamped non-negative
     */
    void setMinimumDelayMillis(int minimumDelayMillis);

    /**
     * Sets the upper bound on the target playout delay in milliseconds.
     *
     * @implSpec Implementations must clamp the estimated target level no higher than this bound from the
     * next estimate onward.
     *
     * @param maximumDelayMillis the maximum target playout delay; clamped no lower than the minimum
     */
    void setMaximumDelayMillis(int maximumDelayMillis);

    /**
     * Ingests a lip-sync delay correction from the video timing path.
     *
     * @implSpec Implementations must bias the target playout delay by the supplied correction so audio and
     * video stay synchronized; a positive value lengthens the audio delay to wait for slower video.
     *
     * @param correctionMillis the signed delay correction in milliseconds
     */
    void ingestAvSyncFeedbackMillis(int correctionMillis);

    /**
     * Flushes the buffer, draining queued packets on a stall.
     *
     * @implSpec Implementations must drop queued packets so playout can resume near the target level, and
     * must reset the estimators so jitter from before the stall does not bias the post-flush target.
     */
    void flush();

    /**
     * Returns a snapshot of the buffer's lifetime counters.
     *
     * @return the current statistics snapshot; never {@code null}
     */
    NetEqStatistics statistics();
}
