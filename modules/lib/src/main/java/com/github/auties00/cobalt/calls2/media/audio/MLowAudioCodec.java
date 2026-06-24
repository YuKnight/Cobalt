package com.github.auties00.cobalt.calls2.media.audio;

import com.github.auties00.cobalt.calls2.media.audio.mlow.MlowDecoder;
import com.github.auties00.cobalt.calls2.media.audio.mlow.MlowEncoder;

import java.util.Arrays;
import java.util.Objects;

/**
 * The MLow low-bitrate speech codec, a permitted {@link AudioCodec} backed by the pure-Java
 * {@link MlowEncoder} and {@link MlowDecoder} kernels.
 *
 * <p>MLow is WhatsApp's in-house low-bitrate speech codec (the {@code smpl} codec in the wa-voip sources),
 * a deterministic float CELP vocoder, not a neural network. A frame is built from a classical CELP chain:
 * linear-predictive (LPC) short-term spectral modelling whose coefficients are transmitted as line
 * spectral frequencies under a two-stage LSF vector quantization; a pitch/long-term-prediction adaptive
 * codebook for the periodic excitation; an algebraic fixed codebook of signed unit pulses for the
 * residual; and the short-term synthesis filter that produces the speech. The quantized parameters are
 * entropy-coded with the range coder behind a leading TOC byte naming the frame configuration, so both
 * directions are deterministic arithmetic coding with no learned weights in the path. The only
 * machine-learning component is an optional, default-disabled bandwidth-extension (BWE) postfilter that
 * sits after the codec core and is not part of coding a frame.
 *
 * <p>{@link #encode(short[], int)} delegates to {@link MlowEncoder#encode(short[])}, which runs voice
 * activity detection, the per-subframe analysis-by-synthesis search, and the range-coded serialization,
 * emitting the packet TOC byte followed by the payload. {@link #decode(byte[], int, boolean)} delegates to
 * {@link MlowDecoder#decode(byte[])}, which parses the TOC byte, decodes every contained internal frame in
 * continuous decoder-state order, and scales the synthesized signal to signed 16-bit PCM. Both kernels
 * thread cross-frame and cross-packet state, so the codec is single-writer per the {@link AudioCodec}
 * contract and must be fed every frame of a stream in order. Scope is the SMPL 16 kHz, 60 ms, mono,
 * low-band path with the postfilter disabled: a 60 ms MLow packet carries 960 samples regardless of the
 * {@code frameSize} requested, because the sample count is fixed by the packet's TOC.
 *
 * <p>The codec runs at the locked 9600 bps profile, so {@link #modify(OpusCodecParams)} is a no-op: MLow
 * carries its own internal rate control rather than the Opus bitrate and complexity knobs, and this scope
 * pins the rate. Packet-loss recovery ({@link #recover(byte[], int)}, the MLow PLC and RED-based
 * reconstruction) and in-band forward-error-correction decode are out of scope for this milestone and
 * throw. The 1:1 and group media paths select MLow over {@link OpusAudioCodec} on the per-call
 * {@code p->use_mlow_codec} voip parameter.
 *
 * @implNote This implementation routes the encode path through the pure-Java {@code AudioEncoderMLowImpl}
 * port {@link MlowEncoder} and the decode path through the {@code AudioDecoderMLowImpl} port
 * {@link MlowDecoder} of the wa-voip WASM module {@code ff-tScznZ8P}; the codec is selected per call by the
 * engine field {@code p->use_mlow_codec} ({@code media.encoder.use_mlow_codec}). The per-frame
 * classification flags are read back from the emitted TOC byte ({@code bit 6} is the VAD flag, {@code bit 1}
 * the in-band FEC flag), matching {@code mlow_packet_has_vad_flag} and {@code mlow_packet_has_fec_content};
 * the level reproduces the RMS {@code -dBov} the native encode stamps, identical to
 * {@link OpusAudioCodec}'s computation. The loss-recovery path and the in-band FEC decode are deferred to
 * the concealment sub-milestone, so they stay throwing rather than fabricating behaviour. Both kernels
 * produce the whole packet's samples and ignore the requested per-frame sample count, so this codec does
 * not enforce the {@code frameSize * channels} length the native fixed-rate codecs assume beyond requiring
 * a whole packet of input.
 */
public final class MLowAudioCodec implements AudioCodec {
    /**
     * The full-scale signed-16-bit magnitude the audio level is measured against, matching
     * {@link OpusAudioCodec}.
     */
    private static final double AUDIO_LEVEL_FULL_SCALE = 32767.0;

    /**
     * The decibel scale factor mapping the captured root-mean-square ratio to a {@code -dBov} magnitude,
     * matching {@link OpusAudioCodec}.
     */
    private static final double AUDIO_LEVEL_DB_FACTOR = -20.0;

    /**
     * The payload-byte threshold below which a frame is treated as discontinuous-transmission or comfort
     * noise rather than speech, matching {@link OpusAudioCodec}.
     */
    private static final int SPEECH_THRESHOLD_BYTES = 3;

    /**
     * The TOC-byte mask selecting the voice-activity flag (bit 6).
     */
    private static final int TOC_VAD_MASK = 0x40;

    /**
     * The TOC-byte mask selecting the in-band forward-error-correction flag (bit 1).
     */
    private static final int TOC_FEC_MASK = 0x02;

    /**
     * The pure-Java MLow CELP analysis-by-synthesis kernel the encode path delegates to.
     *
     * <p>Threads cross-frame and cross-packet state internally, so it is constructed once per stream and
     * driven only from the single writer thread.
     */
    private final MlowEncoder encoder;

    /**
     * The pure-Java MLow CELP synthesis kernel the decode path delegates to.
     *
     * <p>Threads cross-frame and cross-packet state internally, so it is constructed once per stream and
     * driven only from the single writer thread.
     */
    private final MlowDecoder kernel;

    /**
     * The cumulative count of frames passed to {@link #encode(short[], int)}, for the stats snapshot.
     */
    private long totalEncodedFrames;

    /**
     * The cumulative count of packets passed to {@link #decode(byte[], int, boolean)}, for the stats
     * snapshot.
     */
    private long totalDecodedFrames;

    /**
     * Whether the most recently encoded frame was classified as discontinuous (DTX or comfort noise).
     */
    private boolean lastWasDiscontinuous;

    /**
     * Whether this codec has been closed; once closed every operation throws.
     */
    private boolean closed;

    /**
     * Constructs the MLow codec backed by fresh {@link MlowEncoder} and {@link MlowDecoder} kernels.
     *
     * <p>Both kernels start in their reset state, ready to code the first frame of a stream.
     */
    public MLowAudioCodec() {
        this.encoder = new MlowEncoder();
        this.kernel = new MlowDecoder();
        this.closed = false;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation delegates to {@link MlowEncoder#encode(short[])} over the first
     * {@code frameSize} samples, which must be a whole number of 20 ms (320-sample) frames at 16 kHz mono
     * (a 60 ms packet is 960 samples). The voice-activity and in-band FEC flags are read back from the
     * emitted TOC byte; a frame whose payload is below {@link #SPEECH_THRESHOLD_BYTES} bytes is classified
     * discontinuous; the level is the RMS {@code -dBov} of the captured PCM.
     */
    @Override
    public EncodedAudioFrame encode(short[] pcm, int frameSize) {
        Objects.requireNonNull(pcm, "pcm cannot be null");
        requireOpen();
        if (pcm.length < frameSize) {
            throw new IllegalArgumentException("pcm length " + pcm.length + " is below frameSize " + frameSize);
        }
        var frame = pcm.length == frameSize ? pcm : Arrays.copyOf(pcm, frameSize);
        var payload = encoder.encode(frame);
        totalEncodedFrames++;
        var voiceActive = payload.length > 0 && (payload[0] & TOC_VAD_MASK) != 0;
        var hasFec = voiceActive && (payload[0] & TOC_FEC_MASK) != 0;
        var discontinuous = payload.length < SPEECH_THRESHOLD_BYTES;
        lastWasDiscontinuous = discontinuous;
        var levelDbov = audioLevelDbov(pcm, frameSize);
        return new EncodedAudioFrame(payload, voiceActive, discontinuous, hasFec, levelDbov);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation delegates to {@link MlowDecoder#decode(byte[])}, which decodes the
     * whole packet from its TOC byte in continuous decoder-state order. The {@code frameSize} request is
     * not enforced: an MLow packet's sample count is fixed by its TOC (a 60 ms packet yields 960 samples),
     * so the returned array holds the packet's own sample count. A {@code decodeFec} request is rejected
     * because MLow in-band forward-error-correction is out of scope for this milestone.
     */
    @Override
    public short[] decode(byte[] payload, int frameSize, boolean decodeFec) {
        Objects.requireNonNull(payload, "payload cannot be null");
        requireOpen();
        if (decodeFec) {
            throw new UnsupportedOperationException(
                    "MLow in-band forward-error-correction decode is not implemented");
        }
        var pcm = kernel.decode(payload);
        totalDecodedFrames++;
        return pcm;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation throws {@link UnsupportedOperationException}: MLow packet-loss
     * recovery (PLC and RED-based reconstruction) lands in the concealment sub-milestone.
     */
    @Override
    public short[] recover(byte[] nextPayload, int frameSize) {
        requireOpen();
        throw new UnsupportedOperationException("MLow loss recovery is not implemented");
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation is a no-op: MLow runs the locked 9600 bps profile and carries its own
     * internal rate control rather than the Opus bitrate and complexity knobs {@code params} expresses, so
     * there is nothing to reconfigure on this scope.
     */
    @Override
    public void modify(OpusCodecParams params) {
        requireOpen();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation reports the encode-side and decode-side frame counters; the remaining
     * fields are zero because MLow does not surface the per-bitstream byte and timing statistics the native
     * Opus codec exposes.
     */
    @Override
    public AudioCodecStats stats() {
        return new AudioCodecStats(totalEncodedFrames, totalDecodedFrames, 0L, 0L, 0L, 0L, 0, 0);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation returns the classification of the most recently encoded frame, read
     * from its TOC byte and payload length.
     */
    @Override
    public boolean lastFrameWasDiscontinuous() {
        return lastWasDiscontinuous;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation marks the codec closed and releases its references to the pure-Java
     * kernels; the kernels hold no native resource, so closing only flips the closed flag. A second call
     * has no effect.
     */
    @Override
    public void close() {
        closed = true;
    }

    /**
     * Returns the captured frame's loudness as a positive {@code -dBov} magnitude in {@code [0, 127]}.
     *
     * <p>Computes the root-mean-square energy of the first {@code sampleCount} samples and maps it to a
     * decibel magnitude relative to full scale, identical to {@link OpusAudioCodec}'s level so the MLow and
     * Opus encode paths stamp the same audio-level on equivalent input. An empty or silent block reports
     * {@link EncodedAudioFrame#SILENCE_LEVEL}.
     *
     * @param pcm         the captured samples; never {@code null}
     * @param sampleCount the number of valid samples at the start of {@code pcm}
     * @return the {@code -dBov} level magnitude in {@code [0, 127]}
     */
    private static int audioLevelDbov(short[] pcm, int sampleCount) {
        if (sampleCount < 1) {
            return EncodedAudioFrame.SILENCE_LEVEL;
        }
        var sumSquares = 0.0;
        for (var index = 0; index < sampleCount; index++) {
            double sample = pcm[index];
            sumSquares += sample * sample;
        }
        var rms = (int) Math.sqrt(sumSquares / sampleCount);
        var ratio = rms / AUDIO_LEVEL_FULL_SCALE;
        if (ratio <= 0.0) {
            return EncodedAudioFrame.SILENCE_LEVEL;
        }
        var level = AUDIO_LEVEL_DB_FACTOR * Math.log10(ratio);
        return (int) Math.clamp(level, 0.0, EncodedAudioFrame.SILENCE_LEVEL) & 0x7F;
    }

    /**
     * Verifies that the codec is still open.
     *
     * @throws IllegalStateException if the codec has been closed
     */
    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("MLowAudioCodec is closed");
        }
    }
}
