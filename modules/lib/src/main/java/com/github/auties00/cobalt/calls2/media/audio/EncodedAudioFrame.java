package com.github.auties00.cobalt.calls2.media.audio;

import java.util.Objects;

/**
 * Holds one encoded audio frame produced by an {@link AudioCodec}, with the classification flags and
 * the measured loudness the encode path derives from the captured block.
 *
 * <p>The {@link #payload()} is the codec packet bytes; {@link #voiceActive()} reports whether the
 * encoder judged the frame to carry speech (from the TOC/VAD inspection), {@link #discontinuous()}
 * reports whether the frame is a discontinuous-transmission or comfort-noise frame (a payload shorter
 * than the speech threshold), {@link #hasForwardErrorCorrection()} reports whether the packet
 * embeds an in-band FEC (LBRR) copy of the previous frame, and {@link #levelDbov()} carries the
 * frame's loudness as a positive {@code -dBov} magnitude in {@code [0, 127]} ({@code 0} loudest,
 * {@code 127} silence) measured from the captured PCM before encoding. A frame may be both empty
 * (zero-length payload, when DTX produced nothing) and discontinuous; an empty payload is represented
 * by a zero-length array, not {@code null}.
 *
 * @param payload                   the encoded codec packet bytes; never {@code null}, possibly empty
 *                                  when DTX suppressed output
 * @param voiceActive               whether the encoder classified the frame as voice-active
 * @param discontinuous             whether the frame is a DTX or comfort-noise frame
 * @param hasForwardErrorCorrection whether the packet embeds an in-band FEC (LBRR) copy of the
 *                                  previous frame
 * @param levelDbov                 the frame loudness as a positive {@code -dBov} magnitude in
 *                                  {@code [0, 127]}, where {@code 0} is the loudest possible signal
 *                                  and {@code 127} is silence
 * @implNote This implementation carries the per-frame classification {@code opus_codec_encode}
 * (fn6266) of the wa-voip WASM module {@code ff-tScznZ8P} computes after the native encode: the
 * speech-vs-DTX split at the 3-byte threshold ({@code < 3} bytes is DTX/CN), the VAD flag from
 * {@code wa_opus_check_vad_flags_wrapper} (fn6250), and the in-band FEC flag from
 * {@code wa_opus_packet_has_fec_wrapper} (fn6249). The {@link #levelDbov()} reproduces the per-frame
 * audio-level the native encode stamps onto the frame struct (field {@code +0x4a}) when the
 * {@code calculate_audio_level} codec flag is set: the root-mean-square energy of the captured PCM
 * mapped to {@code -dBov}, computed by {@code fn4303} in
 * {@code media/src/audio/wa_audio_level_rtp_ext_utils.cc} and fed into the audio-level RTP-extension
 * history. The downstream sender uses {@link #voiceActive()} for mixing, {@link #discontinuous()} for
 * zero-rate transmit bookkeeping, and {@link #levelDbov()} for the per-packet audio-level extension
 * (the loudest frame, the minimum {@code -dBov}, governs the group).
 */
public record EncodedAudioFrame(
        byte[] payload,
        boolean voiceActive,
        boolean discontinuous,
        boolean hasForwardErrorCorrection,
        int levelDbov
) {
    /**
     * The {@code -dBov} magnitude representing silence or a below-floor signal.
     *
     * <p>Equal to {@code 127}, the quietest representable level; a frame with no measurable energy
     * carries this value, and it is also the default a non-PCM-derived frame reports.
     */
    public static final int SILENCE_LEVEL = 127;

    /**
     * Validates that the payload is present and clamps the level into the representable range.
     *
     * <p>A negative level is raised to {@code 0} (loudest) and a level above {@link #SILENCE_LEVEL} is
     * lowered to it, so {@link #levelDbov()} always lies in {@code [0, 127]}.
     *
     * @throws NullPointerException if {@code payload} is {@code null}
     */
    public EncodedAudioFrame {
        Objects.requireNonNull(payload, "payload cannot be null");
        if (levelDbov < 0) {
            levelDbov = 0;
        } else if (levelDbov > SILENCE_LEVEL) {
            levelDbov = SILENCE_LEVEL;
        }
    }

    /**
     * Returns whether this frame carries no encoded bytes.
     *
     * <p>An empty frame is produced when discontinuous transmission suppresses output entirely for a
     * silence frame; the sender skips transmitting it.
     *
     * @return {@code true} if the {@linkplain #payload() payload} is zero-length
     */
    public boolean isEmpty() {
        return payload.length == 0;
    }
}
