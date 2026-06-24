package com.github.auties00.cobalt.calls2.core.control;

import java.util.Arrays;
import java.util.Objects;

/**
 * One inertial-measurement-unit sample uploaded over a call's application-data stream.
 *
 * <p>An IMU sample carries a device's motion and orientation reading at a point in time so the remote side
 * can drive motion-aware effects (for example stabilizing or reorienting a rendered video). The reading
 * itself is an opaque binary {@link #payload() payload}; this record does not ascribe a field layout to it,
 * because the byte layout of the IMU frame is owned by the native mobile sensor producer and the shared
 * call core only transports the frame verbatim. The {@link #timestampMicros() timestamp} pins the sample on
 * the sender's monotonic clock so the receiver can order samples.
 *
 * <p>The engine uploads IMU readings on the self participant's application-data stream over the SCTP data
 * channel, the same transport that carries reactions and transcripts, packing each reading into a fixed
 * {@linkplain #FRAME_SIZE thirty-six-byte} frame and batching several frames per packet at the configured
 * IMU clock rate.
 *
 * @implNote This implementation models one frame produced by {@code send_imu_data_on_stream} (fn11527,
 * {@code misc/imu_data_controller.cc}) in the wa-voip WASM module {@code ff-tScznZ8P}, which drains the
 * mutex-guarded ring of {@code 0x24}-byte frames and writes them on the self participant's
 * {@code app_data_stream} (fn6246) over the SCTP data channel rather than on a separate media stream. An
 * {@code imu_data} SSRC media-type code ({@code 10}) is allocated per device in the SSRC table, but the
 * controller's frames travel on the application-data stream, not that SSRC set. The recovered frame size is
 * the {@code 0x24}-byte send-buffer entry; its byte layout is set by the native mobile sensor producer
 * outside this module, which only copies the frame verbatim, so Cobalt faithfully carries it as an opaque
 * blob alongside a send timestamp the receiver orders by.
 * @param timestampMicros the sender's monotonic timestamp for this sample, in microseconds
 * @param payload         the opaque IMU reading bytes; never {@code null}
 */
public record ImuSample(long timestampMicros, byte[] payload) {
    /**
     * The fixed size, in bytes, of one IMU frame on the application-data stream.
     *
     * <p>Recovered as the {@code 0x24}-byte send-buffer entry the native controller drains and writes;
     * the layout of the bytes within the frame is set by the native mobile sensor producer outside the
     * shared call core, which transports the frame verbatim.
     */
    public static final int FRAME_SIZE = 0x24;

    /**
     * Validates and defensively copies the payload bytes.
     *
     * @throws NullPointerException if {@code payload} is {@code null}
     */
    public ImuSample {
        Objects.requireNonNull(payload, "payload cannot be null");
        payload = payload.clone();
    }

    /**
     * Returns a copy of the opaque IMU reading bytes.
     *
     * <p>The returned array is a fresh copy, so mutating it does not affect this sample.
     *
     * @return a copy of the payload bytes; never {@code null}
     */
    @Override
    public byte[] payload() {
        return payload.clone();
    }

    /**
     * Compares this sample to another for value equality over its timestamp and bytes.
     *
     * @param obj the object to compare against
     * @return {@code true} when {@code obj} is an {@link ImuSample} with the same timestamp and bytes
     */
    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof ImuSample other
                && timestampMicros == other.timestampMicros
                && Arrays.equals(payload, other.payload));
    }

    /**
     * Returns a content-based hash over the timestamp and payload bytes.
     *
     * @return the hash code of the sample
     */
    @Override
    public int hashCode() {
        return 31 * Long.hashCode(timestampMicros) + Arrays.hashCode(payload);
    }

    /**
     * Returns a diagnostic string recording the timestamp and payload length.
     *
     * @return a diagnostic representation of the sample
     */
    @Override
    public String toString() {
        return "ImuSample[timestampMicros=" + timestampMicros + ", payloadLength=" + payload.length + ']';
    }
}
