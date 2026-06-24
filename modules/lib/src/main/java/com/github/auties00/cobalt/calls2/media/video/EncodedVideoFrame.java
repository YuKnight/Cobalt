package com.github.auties00.cobalt.calls2.media.video;

import com.github.auties00.cobalt.calls2.common.VideoDecoderCapability;

import java.util.Objects;

/**
 * Holds one compressed picture produced by a {@link VideoCodec}, tagged with the codec that produced
 * it, whether it is independently decodable, and the picture dimensions in force when it was encoded.
 *
 * <p>The {@link #payload()} is the codec's compressed access unit: a length-prefix-free concatenation
 * of the picture's NAL units for {@link VideoDecoderCapability#H264 H264}, or the single compressed
 * frame for {@link VideoDecoderCapability#VP8 VP8} and {@link VideoDecoderCapability#VP9 VP9}. The
 * {@link #keyFrame()} flag reports whether the access unit is an intra picture that can be decoded
 * without any earlier frame: a true value marks an instantaneous decoder refresh that resets the
 * decoder's reference state, while a false value marks an inter picture that depends on previously
 * decoded references. The packetizer needs this flag to mark RTP keyframe boundaries, and the
 * bandwidth estimator needs it because a keyframe is a deliberate, large cost spike rather than a
 * congestion signal.
 *
 * <p>The {@link #width()} and {@link #height()} record the encoded picture size, which a mid-call
 * resolution change carries forward picture by picture; for an inter frame they equal the dimensions
 * of the keyframe that opened the current resolution. An empty access unit is represented by a
 * zero-length array, not {@code null}, and arises when the encoder drops a frame under the rate
 * controller's frame-skip control.
 *
 * @param payload   the compressed access-unit bytes; never {@code null}, possibly empty when the
 *                  encoder dropped the frame
 * @param codec     the codec that produced this access unit
 * @param keyFrame  whether the access unit is an independently decodable intra picture
 * @param width     the encoded picture width in pixels
 * @param height    the encoded picture height in pixels
 * @param ptsMicros the presentation timestamp in microseconds carried through from the source frame
 * @implNote This implementation carries the classification the encode ports of the wa-voip WASM module
 * {@code ff-tScznZ8P} attach to each emitted picture: for H.264 the {@code eFrameType} of the
 * {@code SFrameBSInfo} (OpenH264 {@code videoFrameTypeIDR}) maps to {@link #keyFrame()}, and for VP8 the
 * {@code VPX_FRAME_IS_KEY} flag bit of the {@code vpx_codec_cx_pkt} frame package maps to it. The
 * dimensions are taken from the source {@code VideoFrame} the encoder consumed rather than re-parsed
 * from the bitstream, since the calls2 encode path always drives the encoder at a known configured size.
 */
public record EncodedVideoFrame(
        byte[] payload,
        VideoDecoderCapability codec,
        boolean keyFrame,
        int width,
        int height,
        long ptsMicros
) {
    /**
     * Validates that the payload and codec are present.
     *
     * @throws NullPointerException if {@code payload} or {@code codec} is {@code null}
     */
    public EncodedVideoFrame {
        Objects.requireNonNull(payload, "payload cannot be null");
        Objects.requireNonNull(codec, "codec cannot be null");
    }

    /**
     * Returns whether this access unit carries no compressed bytes.
     *
     * <p>An empty access unit is produced when the rate controller's frame-skip logic drops a frame;
     * the sender transmits nothing for it.
     *
     * @return {@code true} if the {@linkplain #payload() payload} is zero-length
     */
    public boolean isEmpty() {
        return payload.length == 0;
    }
}
