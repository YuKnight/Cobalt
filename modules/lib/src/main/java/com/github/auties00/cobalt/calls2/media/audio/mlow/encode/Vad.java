package com.github.auties00.cobalt.calls2.media.audio.mlow.encode;

/**
 * Encoder voice-activity detector of the MLow speech codec, the port of {@code smpl_vad.c}
 * ({@code smpl_VAD_Init}, {@code smpl_VAD_GetSA_Q8_c}, {@code smpl_VAD_GetNoiseLevels} and the analysis
 * filter bank) together with the {@code update_vad_dtx_status} packet decision in the same file.
 *
 * <p>The detector consumes the high-passed 16 kHz speech of one packet and produces the per-packet voice
 * activity flag that the encoder writes into the packet table-of-contents byte (bit 6, the {@code VAD} bit of
 * {@link com.github.auties00.cobalt.calls2.media.audio.mlow.MlowTocByte}), the {@code coded_as_active_voice}
 * flag that selects the active-voice coding tables throughout the low-band encoder, and the per-frame
 * activity classification ({@link FrameActivity}) that gates the bit-rate controller and the discontinuous
 * transmission silence-insertion-descriptor (SID) decision. It is a fixed-point detector lifted verbatim from
 * the SILK VAD: every band energy, noise-level estimate, signal-to-noise ratio and sigmoid is computed in
 * integer arithmetic exactly as the native code, so the speech-activity value and the resulting decision are
 * bit-identical to the reference encoder.
 *
 * <p>Each call to {@link #processPacket(short[], int, int, int, boolean)} runs three steps per 20 ms frame and
 * one decision step per packet:
 * <ul>
 *   <li><b>Sub-band energies.</b> A cascade of three first-order all-pass analysis filter banks
 *       ({@link #analysisFilterBank}) splits the frame into the four SILK bands (0 to 1 kHz, 1 to 2 kHz, 2 to
 *       4 kHz, 4 to 8 kHz), the lowest band is high-passed at {@code -3 dB / 66 Hz} ({@link #highPassFilter}),
 *       and the energy of each band is accumulated per internal sub-frame with the last sub-frame carried
 *       across calls.</li>
 *   <li><b>Noise-level estimation.</b> {@link #updateNoiseLevels} smooths an inverse-energy estimate of the
 *       per-band noise floor, faster during the first 20 seconds (the {@code counter} ramp) and slower once
 *       converged, biased towards a pink-noise spectral shape.</li>
 *   <li><b>Speech-activity probability.</b> The per-band signal-plus-noise to noise ratio is converted to the
 *       log domain, root-mean-squared into a single decibel signal-to-noise ratio, and mapped through a
 *       sigmoid into the {@code speech_activity_Q8} value; a frequency-tilt measure is produced as a
 *       by-product.</li>
 *   <li><b>Packet decision.</b> Each frame is classified {@link FrameActivity#ACTIVE},
 *       {@link FrameActivity#HANGOVER} or {@link FrameActivity#INACTIVE} by thresholding the activity value
 *       against {@value #SPEECH_ACTIVITY_DTX_THRES_Q8} (Q8) and applying the hangover countdown; a packet with
 *       any active frame sets {@link VadDecision#vad()}, a packet with any non-inactive frame sets
 *       {@link VadDecision#codedAsActiveVoice()}, and an entirely inactive packet with discontinuous
 *       transmission enabled produces a SID frame on the {@link VadDecision#sendSidFrame()} schedule.</li>
 * </ul>
 *
 * <p>The external {@code activity} argument is the Opus-level voice-activity decision the analysis stage feeds
 * in: {@link #ACTIVITY_NO_DECISION} when no Opus decision is available (the SILK threshold alone decides),
 * {@link #ACTIVITY_NONE} when Opus marks the frame inactive (which clamps the SILK activity below the
 * threshold), and {@link #ACTIVITY_PRESENT} when Opus marks the frame active. On the
 * {@link #ACTIVITY_NO_DECISION} path the detector reduces to the pure SILK threshold.
 *
 * <p>This detector is stateful across the frames of a stream: it carries the four-band noise floor, the
 * analysis filter-bank states, the high-pass state, the per-band last-sub-frame energy, the noise-estimation
 * frame counter, and the hangover and SID counters of the discontinuous-transmission state machine.
 * {@link #reset()} restores the exact post-construction state, including the noise-floor seed and the 60 ms
 * hangover and 400 ms SID interval the encoder initializes. Construct one detector per logical stream, feed it
 * every packet in order, and reset it between independent streams. This type is not thread-safe.
 *
 * <p>Scope is the shipped SMPL 16 kHz / mono configuration; the validated path is the 60 ms packet of three
 * 20 ms frames. The three VAD tuning knobs ({@code vad_noise_lvl_update_speed}, {@code vad_non_binariness},
 * {@code vad_highpass_sharpness}) default to zero in the shipped encoder, which collapses their scaling factors
 * to unity and reduces every coefficient to the stock SILK VAD constant; this port fixes them at zero and is
 * documented at each use site.
 *
 * @implNote This implementation reproduces the native integer arithmetic operation for operation, including the
 * saturating adds, the rounding right shifts, the piecewise-parabolic {@code silk_lin2log}, the look-up-table
 * {@code silk_sigm_Q15} and the {@code silk_SQRT_APPROX} square root, all inlined here as private helpers so the
 * detector has no dependency outside the codec package. The hot energy loop accumulates with the native
 * left-to-right integer running sum (no float reduction is involved, so the {@code -Ofast} lane-grouping lesson
 * of the float stages does not apply). The resampling branch of {@code update_vad_dtx_status} is omitted because
 * the 16 kHz path never resamples ({@code fs == fs_vad}); a caller at a higher API rate must resample to 16 kHz
 * before invoking this detector, exactly as the native encoder does upstream of {@code smpl_VAD_GetSA_Q8_c}.
 */
public final class Vad {
    /**
     * Number of analysis bands of the SILK voice-activity detector, the native {@code SMPL_VAD_N_BANDS}.
     */
    private static final int N_BANDS = 4;

    /**
     * Base-two logarithm of the number of internal sub-frames per band, the native
     * {@code VAD_INTERNAL_SUBFRAMES_LOG2}.
     */
    private static final int INTERNAL_SUBFRAMES_LOG2 = 2;

    /**
     * Number of internal sub-frames per band, the native {@code VAD_INTERNAL_SUBFRAMES}.
     */
    private static final int INTERNAL_SUBFRAMES = 1 << INTERNAL_SUBFRAMES_LOG2;

    /**
     * Noise-level smoothing coefficient in Q16, the native {@code VAD_NOISE_LEVEL_SMOOTH_COEF_Q16} (must be
     * less than 4096).
     */
    private static final int NOISE_LEVEL_SMOOTH_COEF_Q16 = 1024;

    /**
     * Pink-noise bias seed of the per-band noise-level estimate, the native {@code VAD_NOISE_LEVELS_BIAS}.
     */
    private static final int NOISE_LEVELS_BIAS = 50;

    /**
     * Sigmoid input offset of the speech-probability mapping in Q5, the native
     * {@code VAD_NEGATIVE_OFFSET_Q5}; the sigmoid is zero at this negative offset.
     */
    private static final int NEGATIVE_OFFSET_Q5 = 128;

    /**
     * Signal-to-noise to sigmoid-input scaling factor in Q16, the native {@code VAD_SNR_FACTOR_Q16}.
     */
    private static final int SNR_FACTOR_Q16 = 45000;

    /**
     * Per-band tilt weighting factors, the native {@code tiltWeights}.
     */
    private static final int[] TILT_WEIGHTS = {30000, 6000, -12000, -12000};

    /**
     * First all-pass coefficient of the two-band filter bank, the native {@code A_fb1_20} ({@code 3894 << 1}).
     */
    private static final int A_FB1_20 = 3894 << 1;

    /**
     * Second all-pass coefficient of the two-band filter bank, the native {@code A_fb1_21}
     * ({@code (opus_int16)(18107 << 1)}, which wraps to {@code -29322}).
     */
    private static final int A_FB1_21 = -29322;

    /**
     * Maximum value of an unsigned eight-bit quantity, the native {@code silk_uint8_MAX}; the clamp ceiling of
     * the Q8 speech-activity value.
     */
    private static final int UINT8_MAX = 0xFF;

    /**
     * Maximum value of a signed sixteen-bit quantity, the native {@code silk_int16_MAX}.
     */
    private static final int INT16_MAX = 0x7FFF;

    /**
     * Minimum value of a signed sixteen-bit quantity, the native {@code silk_int16_MIN}.
     */
    private static final int INT16_MIN = -0x8000;

    /**
     * Maximum value of a signed thirty-two-bit quantity, the native {@code silk_int32_MAX}.
     */
    private static final int INT32_MAX = 0x7FFFFFFF;

    /**
     * Speech-activity discontinuous-transmission threshold in Q8, the native
     * {@code SILK_FIX_CONST(SMPL_SPEECH_ACTIVITY_DTX_THRES, 8)} for the float constant {@code 0.05}.
     *
     * <p>{@code round(0.05 * 256) == 13}; the activity-to-class decision and the Opus-inactive clamp both use
     * this Q8 threshold.
     */
    private static final int SPEECH_ACTIVITY_DTX_THRES_Q8 = (int) (0.05 * (1 << 8) + 0.5);

    /**
     * Default discontinuous-transmission hangover length in milliseconds, the native
     * {@code SMPL_DEFAULT_HANGOVER_MS}.
     */
    private static final int DEFAULT_HANGOVER_MS = 60;

    /**
     * Default silence-insertion-descriptor interval in milliseconds, the native
     * {@code SMPL_DEFAULT_SID_INTERVAL_MS}.
     */
    private static final int DEFAULT_SID_INTERVAL_MS = 400;

    /**
     * Opus-level voice-activity sentinel meaning no Opus decision is available, the native
     * {@code SMPL_OPUS_VAD_NO_DECISION}; on this path the SILK threshold alone classifies each frame.
     */
    public static final int ACTIVITY_NO_DECISION = -1;

    /**
     * Opus-level voice-activity value meaning the frame is inactive, the native
     * {@code SMPL_OPUS_VAD_NO_ACTIVITY}; this clamps the SILK activity below the discontinuous-transmission
     * threshold.
     */
    public static final int ACTIVITY_NONE = 0;

    /**
     * Opus-level voice-activity value meaning the frame is active, the native {@code SMPL_OPUS_VAD_ACTIVITY}.
     */
    public static final int ACTIVITY_PRESENT = 1;

    /**
     * Per-frame voice-activity classification, the native {@code SmplVadTypes} enumeration.
     *
     * <p>The ordinal of each constant equals the native integer value ({@code INACTIVE == 0},
     * {@code HANGOVER == 1}, {@code ACTIVE == 2}); the packet decision tests the classification by identity.
     */
    public enum FrameActivity {
        /**
         * The frame has no detected voice activity and is not within the hangover period, the native
         * {@code INACTIVE}.
         */
        INACTIVE,
        /**
         * The frame has no detected voice activity but falls within the hangover period after active speech,
         * the native {@code HANGOVER}; it is still coded as if it may carry voiced energy.
         */
        HANGOVER,
        /**
         * The frame has detected voice activity, the native {@code ACTIVE}.
         */
        ACTIVE
    }

    /**
     * Per-packet voice-activity decision returned by {@link #processPacket(short[], int, int, int, boolean)},
     * the instrumented form of the native {@code smpl_vad_status} plus the relevant
     * {@code smpl_dtx_status} outputs.
     *
     * @param vad                the packet voice-activity flag, {@code true} when any frame is
     *                           {@link FrameActivity#ACTIVE} (the table-of-contents {@code VAD} bit)
     * @param codedAsActiveVoice {@code true} when any frame is not {@link FrameActivity#INACTIVE}, so the
     *                           packet is coded with the active-voice tables
     * @param frameActivities    the per-frame classification, one entry per frame of the packet
     * @param speechActivityQ8   the per-frame speech-activity value in Q8 after the Opus-inactive clamp, one
     *                           entry per frame
     * @param inputTiltQ15       the per-frame frequency-tilt measure in Q15, one entry per frame
     * @param sidFrame           {@code true} when the packet is a silence-insertion-descriptor frame
     *                           (discontinuous transmission enabled and the packet is not coded as active
     *                           voice)
     * @param sendSidFrame       {@code true} when this SID frame is actually emitted under the SID interval
     *                           schedule; meaningful only when {@link #sidFrame()} is {@code true}
     */
    public record VadDecision(
            boolean vad,
            boolean codedAsActiveVoice,
            FrameActivity[] frameActivities,
            int[] speechActivityQ8,
            int[] inputTiltQ15,
            boolean sidFrame,
            boolean sendSidFrame) {
    }

    /**
     * Per-band pink-noise bias seeds, the native {@code NoiseLevelBias}; constant after construction.
     */
    private final int[] noiseLevelBias;

    /**
     * Per-band noise-energy floor estimate, the native {@code NL}.
     */
    private final int[] noiseLevel;

    /**
     * Per-band inverse noise-energy floor estimate, the native {@code inv_NL}.
     */
    private final int[] inverseNoiseLevel;

    /**
     * Analysis filter-bank state of the 0 to 8 kHz split, the native {@code AnaState}.
     */
    private final int[] anaState;

    /**
     * Analysis filter-bank state of the 0 to 4 kHz split, the native {@code AnaState1}.
     */
    private final int[] anaState1;

    /**
     * Analysis filter-bank state of the 0 to 2 kHz split, the native {@code AnaState2}.
     */
    private final int[] anaState2;

    /**
     * Per-band carried last-sub-frame energy, the native {@code XnrgSubfr}.
     */
    private final int[] subFrameEnergy;

    /**
     * Single-element high-pass filter state of the lowest band, the native {@code HPstate}.
     */
    private final int[] highPassState;

    /**
     * Noise-estimation frame counter used during the initial faster-smoothing phase, the native
     * {@code counter}.
     */
    private int counter;

    /**
     * Remaining discontinuous-transmission hangover in milliseconds, the native
     * {@code dtx_state.remaining_dtx_hangover}.
     */
    private int remainingHangoverMs;

    /**
     * Configured discontinuous-transmission hangover length in milliseconds, the native
     * {@code dtx_state.hangover_ms}.
     */
    private final int hangoverMs;

    /**
     * Configured silence-insertion-descriptor interval in milliseconds, the native
     * {@code dtx_state.sid_interval_ms}.
     */
    private final int sidIntervalMs;

    /**
     * Remaining milliseconds until the next silence-insertion-descriptor frame is emitted, the native
     * {@code dtx_state.dtx_remaining_ms}.
     */
    private int dtxRemainingMs;

    /**
     * Latched emit flag of the current silence-insertion-descriptor frame, the native
     * {@code dtx_state.send_sid_frame}.
     *
     * <p>This is a persistent field of the native discontinuous-transmission state, written only on the SID
     * branch of {@code update_vad_dtx_status} and left unchanged when the packet is coded as active voice. It
     * is meaningful only when the current packet is a SID frame; on a non-SID packet it retains its previous
     * value, which the encoder never reads.
     */
    private boolean sendSidFrame;

    /**
     * Creates a voice-activity detector in the encoder's post-initialization state.
     *
     * <p>The noise floor is seeded with the pink-noise spectral shape of {@code smpl_VAD_Init}, the noise
     * counter is set to its initial value, and the hangover and silence-insertion-descriptor counters are set
     * to the encoder defaults of 60 ms and 400 ms.
     */
    public Vad() {
        this.noiseLevelBias = new int[N_BANDS];
        this.noiseLevel = new int[N_BANDS];
        this.inverseNoiseLevel = new int[N_BANDS];
        this.anaState = new int[2];
        this.anaState1 = new int[2];
        this.anaState2 = new int[2];
        this.subFrameEnergy = new int[N_BANDS];
        this.highPassState = new int[1];
        this.hangoverMs = DEFAULT_HANGOVER_MS;
        this.sidIntervalMs = DEFAULT_SID_INTERVAL_MS;
        reset();
    }

    /**
     * Restores the detector to its exact post-construction state.
     *
     * <p>Clears every filter-bank and high-pass state, re-seeds the per-band noise floor and its inverse from
     * the pink-noise bias, sets the noise counter to its initial value, and resets the hangover counter to the
     * configured hangover length and the silence-insertion-descriptor counter to zero, mirroring
     * {@code smpl_VAD_Init} followed by the encoder's discontinuous-transmission state initialization.
     */
    public void reset() {
        for (int b = 0; b < N_BANDS; b++) {
            noiseLevelBias[b] = Math.max(NOISE_LEVELS_BIAS / (b + 1), 1);
        }
        for (int b = 0; b < N_BANDS; b++) {
            noiseLevel[b] = 100 * noiseLevelBias[b];
            inverseNoiseLevel[b] = INT32_MAX / noiseLevel[b];
        }
        counter = 15;
        anaState[0] = anaState[1] = 0;
        anaState1[0] = anaState1[1] = 0;
        anaState2[0] = anaState2[1] = 0;
        for (int b = 0; b < N_BANDS; b++) {
            subFrameEnergy[b] = 0;
        }
        highPassState[0] = 0;
        remainingHangoverMs = hangoverMs;
        dtxRemainingMs = 0;
        sendSidFrame = false;
    }

    /**
     * Processes one packet of 16 kHz mono speech and returns its voice-activity decision.
     *
     * <p>The samples are laid out as {@code framesPerPacket} consecutive frames of {@code frameLen} samples.
     * Each frame is run through {@link #speechActivity(short[], int, int)} to obtain its Q8 activity value,
     * then classified and accumulated into the packet decision exactly as {@code update_vad_dtx_status}; the
     * detector state advances across the call.
     *
     * @param pcm             the packet samples, length at least {@code framesPerPacket * frameLen}
     * @param frameLen        the number of samples per frame (320 for a 20 ms frame at 16 kHz)
     * @param framesPerPacket the number of frames in the packet (3 for a 60 ms packet)
     * @param activity        the Opus-level voice-activity decision, one of {@link #ACTIVITY_NO_DECISION},
     *                        {@link #ACTIVITY_NONE} or {@link #ACTIVITY_PRESENT}
     * @param useDtx          {@code true} when discontinuous transmission is enabled, which permits
     *                        silence-insertion-descriptor frames on entirely inactive packets
     * @return the per-packet voice-activity decision
     */
    public VadDecision processPacket(short[] pcm, int frameLen, int framesPerPacket, int activity, boolean useDtx) {
        int fs = 16000;
        int packetMs = (frameLen * framesPerPacket * 1000) / fs;
        boolean vad = false;
        boolean codedAsActiveVoice = false;
        var types = new FrameActivity[framesPerPacket];
        var saQ8 = new int[framesPerPacket];
        var tiltQ15 = new int[framesPerPacket];
        var results = new float[framesPerPacket];

        for (int i = 0; i < framesPerPacket; i++) {
            int speechActivityQ8 = speechActivity(pcm, i * frameLen, frameLen);
            tiltQ15[i] = lastInputTiltQ15;
            results[i] = speechActivityQ8 / 256.0f;
            if (activity == ACTIVITY_NO_DECISION) {
                types[i] = results[i] > 0.05f ? FrameActivity.ACTIVE : FrameActivity.INACTIVE;
            } else {
                if (activity == ACTIVITY_NONE && speechActivityQ8 >= SPEECH_ACTIVITY_DTX_THRES_Q8) {
                    speechActivityQ8 = SPEECH_ACTIVITY_DTX_THRES_Q8 - 1;
                    results[i] = speechActivityQ8 / 256.0f;
                }
                types[i] = results[i] > 0.05f ? FrameActivity.ACTIVE : FrameActivity.INACTIVE;
            }
            saQ8[i] = speechActivityQ8;
        }

        for (int i = 0; i < framesPerPacket; i++) {
            if (types[i] == FrameActivity.ACTIVE) {
                remainingHangoverMs = hangoverMs;
            } else {
                if (remainingHangoverMs > 0) {
                    types[i] = FrameActivity.HANGOVER;
                    remainingHangoverMs -= packetMs / framesPerPacket;
                }
            }
            if (types[i] == FrameActivity.ACTIVE) {
                vad = true;
            }
            if (types[i] != FrameActivity.INACTIVE) {
                codedAsActiveVoice = true;
            }
        }

        boolean sidFrame = useDtx && !codedAsActiveVoice;
        if (sidFrame) {
            sendSidFrame = true;
            if (sidIntervalMs > packetMs) {
                if (dtxRemainingMs >= packetMs) {
                    sendSidFrame = false;
                    dtxRemainingMs -= packetMs;
                } else {
                    sendSidFrame = true;
                    dtxRemainingMs = sidIntervalMs;
                }
            }
        } else {
            dtxRemainingMs = 0;
        }

        return new VadDecision(vad, codedAsActiveVoice, types, saQ8, tiltQ15, sidFrame, sendSidFrame);
    }

    /**
     * Frequency-tilt measure of the most recent {@link #speechActivity(short[], int, int)} call in Q15, the
     * native {@code psEncC->input_tilt_Q15}.
     */
    private int lastInputTiltQ15;

    /**
     * Computes the speech-activity level of one frame in Q8, the port of {@code smpl_VAD_GetSA_Q8_c}.
     *
     * <p>Filters and decimates the frame into the four SILK bands, high-passes the lowest band, accumulates
     * the per-band sub-frame energies (carrying the last sub-frame across calls), updates the noise-floor
     * estimate, forms the per-band log signal-to-noise ratios, and maps the root-mean-square decibel ratio
     * through the speech-probability sigmoid. The frequency-tilt by-product is stored in
     * {@link #lastInputTiltQ15} for the caller to read.
     *
     * @param pcm      the frame samples
     * @param offset   the index of the first frame sample in {@code pcm}
     * @param frameLen the number of frame samples; must be a multiple of eight and at most 512
     * @return the speech-activity level in Q8, clamped to {@code [0, 255]}
     */
    private int speechActivity(short[] pcm, int offset, int frameLen) {
        int decimatedFrameLen1 = frameLen >> 1;
        int decimatedFrameLen2 = frameLen >> 2;
        int decimatedFrameLen = frameLen >> 3;

        int[] xOffset = new int[N_BANDS];
        xOffset[0] = 0;
        xOffset[1] = decimatedFrameLen + decimatedFrameLen2;
        xOffset[2] = xOffset[1] + decimatedFrameLen;
        xOffset[3] = xOffset[2] + decimatedFrameLen2;
        short[] x = new short[xOffset[3] + decimatedFrameLen1];

        // 0-8 kHz to 0-4 kHz and 4-8 kHz, reading directly from the caller's PCM frame.
        analysisFilterBankIn(pcm, offset, anaState, x, 0, x, xOffset[3], frameLen);
        // 0-4 kHz to 0-2 kHz and 2-4 kHz.
        analysisFilterBank(x, 0, anaState1, x, 0, x, xOffset[2], decimatedFrameLen1);
        // 0-2 kHz to 0-1 kHz and 1-2 kHz.
        analysisFilterBank(x, 0, anaState2, x, 0, x, xOffset[1], decimatedFrameLen2);

        // HP filter on lowest band, -3 dB @ 66 Hz. highpass_sharpness is fixed at 0, so the scaling is unity.
        int aNegQ16 = 53084;
        aNegQ16 = (aNegQ16 * 100) / 100;
        int bQ16 = (65536 + aNegQ16) / 2;
        highPassFilter(x, bQ16, aNegQ16, highPassState, x, decimatedFrameLen);

        int[] xnrg = new int[N_BANDS];
        int sumSquared;
        for (int b = 0; b < N_BANDS; b++) {
            decimatedFrameLen = frameLen >> Math.min(N_BANDS - b, N_BANDS - 1);
            int decSubFrameLen = decimatedFrameLen >> INTERNAL_SUBFRAMES_LOG2;
            int decSubFrameOffset = 0;
            xnrg[b] = subFrameEnergy[b];
            sumSquared = 0;
            for (int s = 0; s < INTERNAL_SUBFRAMES; s++) {
                sumSquared = 0;
                for (int i = 0; i < decSubFrameLen; i++) {
                    int xTmp = x[xOffset[b] + i + decSubFrameOffset] >> 3;
                    sumSquared = sumSquared + xTmp * xTmp;
                }
                if (s < INTERNAL_SUBFRAMES - 1) {
                    xnrg[b] = addPosSat32(xnrg[b], sumSquared);
                } else {
                    xnrg[b] = addPosSat32(xnrg[b], sumSquared >> 1);
                }
                decSubFrameOffset += decSubFrameLen;
            }
            subFrameEnergy[b] = sumSquared;
        }

        updateNoiseLevels(xnrg);

        int sumSq = 0;
        int inputTilt = 0;
        for (int b = 0; b < N_BANDS; b++) {
            int speechNrg = xnrg[b] - noiseLevel[b];
            if (speechNrg > 0) {
                int nrgToNoiseRatioQ8;
                if ((xnrg[b] & 0xFF800000) == 0) {
                    nrgToNoiseRatioQ8 = (xnrg[b] << 8) / (noiseLevel[b] + 1);
                } else {
                    nrgToNoiseRatioQ8 = xnrg[b] / ((noiseLevel[b] >> 8) + 1);
                }
                int snrQ7 = lin2log(nrgToNoiseRatioQ8) - 8 * 128;
                sumSq = sumSq + snrQ7 * snrQ7;
                if (speechNrg < (1 << 20)) {
                    snrQ7 = smulwb(sqrtApprox(speechNrg) << 6, snrQ7);
                }
                inputTilt = smlawb(inputTilt, TILT_WEIGHTS[b], snrQ7);
            }
        }

        sumSq = sumSq / N_BANDS;
        int snrDbQ7 = (short) (3 * sqrtApprox(sumSq));

        // vad_non_binariness is fixed at 0, so the SNR scaling factor is the stock SILK constant.
        int vadSnrFactorQ16 = (SNR_FACTOR_Q16 * 150) / 150;
        int saQ15 = sigmQ15(smulwb(vadSnrFactorQ16, snrDbQ7) - NEGATIVE_OFFSET_Q5);

        lastInputTiltQ15 = (sigmQ15(inputTilt) - 16384) << 1;

        return Math.min(saQ15 >> 7, UINT8_MAX);
    }

    /**
     * Updates the per-band noise-floor estimate from the current sub-band energies, the port of
     * {@code smpl_VAD_GetNoiseLevels}.
     *
     * <p>During the first 20 seconds (the {@code counter} ramp) a minimum smoothing coefficient enforces
     * faster adaptation. Per band the inverse energy is smoothed towards the inverse noise floor with a
     * coefficient that is small when the band energy is high (slow update on speech) and large when the band
     * energy is below the floor (fast update on noise), then re-inverted into the stored noise floor and
     * limited to guarantee seven bits of head room.
     *
     * @param px the per-band sub-band energies for this frame
     */
    private void updateNoiseLevels(int[] px) {
        int minCoef;
        if (counter < 1000) {
            minCoef = INT16_MAX / ((counter >> 4) + 1);
            counter++;
        } else {
            minCoef = 0;
        }

        for (int k = 0; k < N_BANDS; k++) {
            int nl = noiseLevel[k];
            int nrg = addPosSat32(px[k], noiseLevelBias[k]);
            int invNrg = INT32_MAX / nrg;

            int coef;
            if (nrg > (nl << 3)) {
                coef = NOISE_LEVEL_SMOOTH_COEF_Q16 >> 3;
            } else if (nrg < nl) {
                coef = NOISE_LEVEL_SMOOTH_COEF_Q16;
            } else {
                coef = smulwb(smulww(invNrg, nl), NOISE_LEVEL_SMOOTH_COEF_Q16 << 1);
            }
            // noise_lvl_update_speed is fixed at 0, so the coefficient scaling is unity.
            coef = (coef * 100) / 100;
            coef = Math.max(coef, minCoef);

            inverseNoiseLevel[k] = smlawb(inverseNoiseLevel[k], invNrg - inverseNoiseLevel[k], coef);
            nl = INT32_MAX / inverseNoiseLevel[k];
            nl = Math.min(nl, 0x00FFFFFF);
            noiseLevel[k] = nl;
        }
    }

    /**
     * Splits a 16 kHz {@code short} PCM frame into a decimated low band and high band using first-order
     * all-pass filters, the port of {@code smpl_ana_filt_bank_1} reading from the caller's PCM array.
     *
     * <p>This is the entry-stage filter bank whose input is the raw frame; the internal stages read from the
     * scratch array through {@link #analysisFilterBank}. Internal variables and state are Q10.
     *
     * @param in       the input PCM array
     * @param inOffset the index of the first input sample
     * @param state    the two-element filter-bank state, updated in place
     * @param outL     the low-band output array
     * @param outLOff  the index of the first low-band output sample
     * @param outH     the high-band output array
     * @param outHOff  the index of the first high-band output sample
     * @param n        the number of input samples
     */
    private static void analysisFilterBankIn(short[] in, int inOffset, int[] state,
                                             short[] outL, int outLOff, short[] outH, int outHOff, int n) {
        int n2 = n >> 1;
        for (int k = 0; k < n2; k++) {
            int in32 = ((int) in[inOffset + 2 * k]) << 10;
            int y = in32 - state[0];
            int xx = smlawb(y, y, A_FB1_21);
            int out1 = state[0] + xx;
            state[0] = in32 + xx;

            in32 = ((int) in[inOffset + 2 * k + 1]) << 10;
            y = in32 - state[1];
            xx = smulwb(y, A_FB1_20);
            int out2 = state[1] + xx;
            state[1] = in32 + xx;

            outL[outLOff + k] = (short) sat16(rshiftRound(out2 + out1, 11));
            outH[outHOff + k] = (short) sat16(rshiftRound(out2 - out1, 11));
        }
    }

    /**
     * Splits a decimated {@code short} band into a lower and upper sub-band using first-order all-pass
     * filters, the port of {@code smpl_ana_filt_bank_1} reading from and writing to the scratch array.
     *
     * <p>The low-band output may alias the input region in place, exactly as the native cascade overwrites the
     * front of the scratch buffer; the high band is written disjointly. Internal variables and state are Q10.
     *
     * @param in       the input array
     * @param inOffset the index of the first input sample
     * @param state    the two-element filter-bank state, updated in place
     * @param outL     the low-band output array
     * @param outLOff  the index of the first low-band output sample
     * @param outH     the high-band output array
     * @param outHOff  the index of the first high-band output sample
     * @param n        the number of input samples
     */
    private static void analysisFilterBank(short[] in, int inOffset, int[] state,
                                           short[] outL, int outLOff, short[] outH, int outHOff, int n) {
        int n2 = n >> 1;
        for (int k = 0; k < n2; k++) {
            int in32 = ((int) in[inOffset + 2 * k]) << 10;
            int y = in32 - state[0];
            int xx = smlawb(y, y, A_FB1_21);
            int out1 = state[0] + xx;
            state[0] = in32 + xx;

            in32 = ((int) in[inOffset + 2 * k + 1]) << 10;
            y = in32 - state[1];
            xx = smulwb(y, A_FB1_20);
            int out2 = state[1] + xx;
            state[1] = in32 + xx;

            outL[outLOff + k] = (short) sat16(rshiftRound(out2 + out1, 11));
            outH[outHOff + k] = (short) sat16(rshiftRound(out2 - out1, 11));
        }
    }

    /**
     * Applies the first-order auto-regressive moving-average high-pass filter with a zero at DC to the lowest
     * band, the port of {@code smpl_filt_hp_FIX}.
     *
     * <p>Implemented as the native direct-form-II transposed structure with a single-element state; the input
     * and output arrays may be the same.
     *
     * @param in    the input array, indexed from zero
     * @param bQ16  the gain coefficient in Q16
     * @param aNegQ16 the negated auto-regressive coefficient in Q16
     * @param state the single-element filter state, updated in place
     * @param out   the output array, indexed from zero
     * @param len   the signal length (must be even)
     */
    private static void highPassFilter(short[] in, int bQ16, int aNegQ16, int[] state, short[] out, int len) {
        for (int k = 0; k < len; k++) {
            int inVal = smulwb(bQ16, in[k]);
            short outVal = (short) sat16(state[0] - inVal);
            state[0] = smlawb(inVal, aNegQ16, outVal);
            out[k] = outVal;
        }
    }

    /**
     * Multiplies a 32-bit value by the low 16 bits of another and keeps the high 32 bits of the 48-bit
     * product, the native {@code silk_SMULWB} ({@code (a * (int16) b) >> 16}).
     *
     * @param a the 32-bit multiplicand
     * @param b the multiplier whose low 16 bits are used as a signed 16-bit value
     * @return the high 32 bits of {@code a * (short) b}
     */
    private static int smulwb(int a, int b) {
        return (int) (((long) a * (short) b) >> 16);
    }

    /**
     * Multiplies and accumulates the high part of a Q16 product, the native {@code silk_SMLAWB}
     * ({@code a + ((b * (int16) c) >> 16)}).
     *
     * @param a the accumulator
     * @param b the 32-bit multiplicand
     * @param c the multiplier whose low 16 bits are used as a signed 16-bit value
     * @return {@code a + smulwb(b, c)}
     */
    private static int smlawb(int a, int b, int c) {
        return a + (int) (((long) b * (short) c) >> 16);
    }

    /**
     * Multiplies the high parts of two Q16 values, the native {@code silk_SMULWW}
     * ({@code ((int64) a * b) >> 16}).
     *
     * @param a the first 32-bit operand
     * @param b the second 32-bit operand
     * @return the high 32 bits of the 64-bit product
     */
    private static int smulww(int a, int b) {
        return (int) (((long) a * b) >> 16);
    }

    /**
     * Saturates a 32-bit value into the signed 16-bit range, the native {@code silk_SAT16}.
     *
     * @param a the value to saturate
     * @return {@code a} clamped to {@code [-32768, 32767]}
     */
    private static int sat16(int a) {
        return a > INT16_MAX ? INT16_MAX : (a < INT16_MIN ? INT16_MIN : a);
    }

    /**
     * Rounds and right-shifts a 32-bit value, the native {@code silk_RSHIFT_ROUND}.
     *
     * @param a     the value to shift
     * @param shift the non-negative shift amount
     * @return {@code a} arithmetically right-shifted by {@code shift} with round-half-up
     */
    private static int rshiftRound(int a, int shift) {
        return shift == 1 ? (a >> 1) + (a & 1) : ((a >> (shift - 1)) + 1) >> 1;
    }

    /**
     * Adds two 32-bit values and saturates a positive overflow to the signed 32-bit maximum, the native
     * {@code silk_ADD_POS_SAT32}.
     *
     * @param a the first addend
     * @param b the second addend
     * @return {@code a + b}, or {@link #INT32_MAX} when the sum sets the sign bit
     */
    private static int addPosSat32(int a, int b) {
        int sum = a + b;
        return (sum & 0x80000000) != 0 ? INT32_MAX : sum;
    }

    /**
     * Approximates {@code 128 * log2()} of a positive linear value, the native {@code silk_lin2log}.
     *
     * <p>Uses the piecewise-parabolic SILK approximation over the leading-zero count and the seven
     * fractional bits after the leading one.
     *
     * @param inLin the input in linear scale
     * @return the approximate base-two logarithm scaled by 128
     */
    private static int lin2log(int inLin) {
        int lz = inLin != 0 ? Integer.numberOfLeadingZeros(inLin) : 32;
        int fracQ7 = ror32(inLin, 24 - lz) & 0x7F;
        return (smlawb(fracQ7, fracQ7 * (128 - fracQ7), 179)) + ((31 - lz) << 7);
    }

    /**
     * Approximates the square root of a non-negative value, the native {@code silk_SQRT_APPROX}.
     *
     * <p>Scales a seed by the leading-zero parity and refines it with the fractional part of the input.
     *
     * @param x the input value
     * @return the approximate square root, or zero for non-positive input
     */
    private static int sqrtApprox(int x) {
        if (x <= 0) {
            return 0;
        }
        int lz = Integer.numberOfLeadingZeros(x);
        int fracQ7 = ror32(x, 24 - lz) & 0x7F;
        int y;
        if ((lz & 1) != 0) {
            y = 32768;
        } else {
            y = 46214;
        }
        y >>= (lz >> 1);
        y = smlawb(y, y, 213 * fracQ7);
        return y;
    }

    /**
     * Maps an input in Q5 through the SILK approximate sigmoid into Q15, the native {@code silk_sigm_Q15}.
     *
     * <p>Linearly interpolates a six-entry look-up table on the magnitude of the input, clipping beyond the
     * table range.
     *
     * @param inQ5 the input in Q5
     * @return the sigmoid value in Q15, in {@code [0, 32767]}
     */
    private static int sigmQ15(int inQ5) {
        int[] slopeQ10 = {237, 153, 73, 30, 12, 7};
        int[] posQ15 = {16384, 23955, 28861, 31213, 32178, 32548};
        int[] negQ15 = {16384, 8812, 3906, 1554, 589, 219};
        if (inQ5 < 0) {
            inQ5 = -inQ5;
            if (inQ5 >= 6 * 32) {
                return 0;
            }
            int ind = inQ5 >> 5;
            return negQ15[ind] - slopeQ10[ind] * (inQ5 & 0x1F);
        } else {
            if (inQ5 >= 6 * 32) {
                return 32767;
            }
            int ind = inQ5 >> 5;
            return posQ15[ind] + slopeQ10[ind] * (inQ5 & 0x1F);
        }
    }

    /**
     * Rotates a 32-bit value right by the given amount, the native {@code silk_ROR32}; a negative amount
     * rotates left.
     *
     * @param a32 the value to rotate
     * @param rot the rotation amount; positive rotates right, negative rotates left
     * @return the rotated value
     */
    private static int ror32(int a32, int rot) {
        if (rot == 0) {
            return a32;
        } else if (rot < 0) {
            int m = -rot;
            return (a32 << m) | (a32 >>> (32 - m));
        } else {
            return (a32 << (32 - rot)) | (a32 >>> rot);
        }
    }
}
