package com.github.auties00.cobalt.calls2.dsp;

/**
 * Splits a full-band PCM block into frequency sub-bands and recombines them, the shared quadrature-mirror
 * filter bank the audio processing path uses to run band-limited algorithms.
 *
 * <p>The filter is configured for two bands and converts between one full-band block and two half-rate
 * sub-band blocks. {@link #analysis(float[], float[], float[])} splits a full-band block into a low band
 * and a high band, each at half the input length and half the sample rate;
 * {@link #synthesis(float[], float[], float[])} recombines a low band and a high band back into a full-band
 * block. The split deinterleaves the full-band block into its even and odd polyphase components, runs each
 * through a three-section all-pass branch, and forms the low band as their half-sum and the high band as
 * their half-difference, the standard power-complementary quadrature-mirror design, so the analysis followed
 * by the synthesis reconstructs the input up to the filters' group delay with no spectral hole at the band
 * edge. The filter state carries across blocks so the split is continuous.
 *
 * <p>The filter is single-writer: the audio path drives one instance from its thread. It is pure-Java
 * convolution with no native state.
 *
 * @implNote This implementation ports the two-band path of {@code webrtc::SplittingFilter::Analysis} (fn10084)
 * and {@code Synthesis} (fn10085) of the wa-voip WASM module {@code ff-tScznZ8P} ({@code rev-rtc-dsp},
 * source {@code xplat/rtc/webrtc/latest/modules/audio_processing/splitting_filter.cc}; the native asserts
 * {@code num_bands_ == 2 || num_bands_ == 3}, string at mem {@code 0x14444} / fn10095). The per-branch
 * three-section all-pass {@link #allPassQmf(float[], int, float[], float[], float[])} ports fn10094, the
 * WebRtcSpl QMF all-pass: it cascades three first-order sections, ping-ponging between the input and output
 * buffers and carrying a six-element state ({@code state[0..1]} section one, {@code state[2..3]} section two,
 * {@code state[4..5]} section three). The branch coefficients {@link #ALLPASS_COEFFICIENTS_1} and
 * {@link #ALLPASS_COEFFICIENTS_2} are read verbatim from the WASM data segment {@code data-segment-0088.bin}
 * at mem {@code 0x1237c8} (branch one) and {@code 0x1237d4} (branch two), the float32 values fn10084/fn10085
 * load via {@code f32.load}; they are the canonical WebRtcSpl {@code kAllPassCoefsBranch1}/
 * {@code kAllPassCoefsBranch2} QMF coefficients. The analysis routes the odd polyphase component through
 * branch one and the even through branch two, then forms {@code low = (odd_f + even_f) * 0.5} and
 * {@code high = (odd_f - even_f) * 0.5} (fn10084:69-71); the synthesis inverts this and saturates each output
 * sample to the int16 range {@code [-32768, 32767]} (fn10085:68-83). The three-band path (which uses the
 * separate {@code ThreeBandFilterBank} {@code kFilterCoeffs}/{@code kDctModulation} tables, also present in
 * this binary at mem {@code 0x1237e0}/{@code 0x123880} and exercised by the {@code num_bands_ == 3} branch of
 * fn10084/fn10085) is not implemented and is rejected at construction: the captured WhatsApp call audio
 * format is 16 kHz mono, for which the two-band split is the one the processing path needs. See the
 * {@code TODO} in {@link #SplittingFilter(int)}.
 */
public final class SplittingFilter {
    /**
     * The all-pass coefficients for the first polyphase branch of the two-band split.
     *
     * <p>The three first-order all-pass section coefficients read from the WASM data segment at mem
     * {@code 0x1237c8} (the WebRtcSpl {@code kAllPassCoefsBranch1} QMF set); the odd polyphase component is
     * filtered through this branch.
     */
    private static final float[] ALLPASS_COEFFICIENTS_1 = {0.0979309082f, 0.5643005371f, 0.8737335205f};

    /**
     * The all-pass coefficients for the second polyphase branch of the two-band split.
     *
     * <p>The three first-order all-pass section coefficients read from the WASM data segment at mem
     * {@code 0x1237d4} (the WebRtcSpl {@code kAllPassCoefsBranch2} QMF set); the even polyphase component is
     * filtered through this branch.
     */
    private static final float[] ALLPASS_COEFFICIENTS_2 = {0.3255157471f, 0.7486267090f, 0.9614562988f};

    /**
     * The number of all-pass state memories each branch carries, two per section across three sections.
     */
    private static final int STATE_LENGTH = 6;

    /**
     * The lower saturation bound applied to each synthesized full-band sample, the int16 floor.
     */
    private static final float SATURATION_MIN = -32768.0f;

    /**
     * The upper saturation bound applied to each synthesized full-band sample, the int16 ceiling.
     */
    private static final float SATURATION_MAX = 32767.0f;

    /**
     * The message used when the unsupported three-band path is requested.
     */
    private static final String THREE_BAND_UNSUPPORTED =
            "Three-band splitting filter is not implemented; the 16 kHz mono call path uses two bands";

    /**
     * The number of bands this filter splits into, fixed at two.
     */
    private final int bands;

    /**
     * The all-pass state for the odd-component branch on analysis, six section memories.
     */
    private final float[] analysisState1;

    /**
     * The all-pass state for the even-component branch on analysis, six section memories.
     */
    private final float[] analysisState2;

    /**
     * The all-pass state for the first branch on synthesis, six section memories.
     */
    private final float[] synthesisState1;

    /**
     * The all-pass state for the second branch on synthesis, six section memories.
     */
    private final float[] synthesisState2;

    /**
     * Constructs a splitting filter for the given number of bands.
     *
     * @param bands the number of bands to split into; must be {@code 2}
     * @throws IllegalArgumentException if {@code bands} is not {@code 2}
     */
    public SplittingFilter(int bands) {
        if (bands != 2) {
            throw new IllegalArgumentException(THREE_BAND_UNSUPPORTED + ", got " + bands);
            // TODO: implement the three-band split. The WebRTC three-band filter bank
            //  (modules/audio_processing/three_band_filter_bank.cc ThreeBandFilterBank: kSparsity=2,
            //  kStrideLog2=1, kNumZeroFilters=2, kFilterSize=4, the 10x4 kFilterCoeffs and 10x3
            //  kDctModulation) is the distinct coefficient set the three-band path needs. Both tables ARE
            //  present in this binary, recovered verbatim from data-segment-0088.bin at mem 0x1237e0 (40
            //  float32 = kFilterCoeffs, the symmetric 10x4 FIR) and 0x123880 (30 float32 = kDctModulation,
            //  rows {2,2,2}/{sqrt3,0,-sqrt3}/{1,-2,1}/...), and fn10084/fn10085 run the num_bands_==3 branch
            //  over them (FIR convolution fn10108/fn10107 plus the DCT-modulation accumulate). It is left
            //  unimplemented only because no path in Cobalt constructs SplittingFilter with three bands yet:
            //  the call audio is 16 kHz mono (two-band), and no 48 kHz / full-band APM stage is wired. Port
            //  the three-band branch from fn10084 (analysis) and fn10085 (synthesis) when a 48 kHz
            //  super-wideband audio path lands. SplittingFilterTest pins rejection of three bands.
        }
        this.bands = bands;
        this.analysisState1 = new float[STATE_LENGTH];
        this.analysisState2 = new float[STATE_LENGTH];
        this.synthesisState1 = new float[STATE_LENGTH];
        this.synthesisState2 = new float[STATE_LENGTH];
    }

    /**
     * Returns the number of bands this filter splits into.
     *
     * @return the band count, {@code 2}
     */
    public int bands() {
        return bands;
    }

    /**
     * Splits a full-band block into a low band and a high band at half the rate.
     *
     * <p>Deinterleaves the full-band block into its even and odd polyphase components, runs the odd component
     * through the first branch and the even through the second, and forms the low band as their half-sum and
     * the high band as their half-difference, the power-complementary quadrature-mirror split. The output
     * bands are each half the full-band length.
     *
     * @param fullBand the full-band input block, even length; never {@code null}
     * @param lowBand  the low-band output, at least half the full-band length; never {@code null}
     * @param highBand the high-band output, at least half the full-band length; never {@code null}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if {@code fullBand} has odd length or an output band is too short
     */
    public void analysis(float[] fullBand, float[] lowBand, float[] highBand) {
        java.util.Objects.requireNonNull(fullBand, "fullBand cannot be null");
        java.util.Objects.requireNonNull(lowBand, "lowBand cannot be null");
        java.util.Objects.requireNonNull(highBand, "highBand cannot be null");
        if ((fullBand.length & 1) != 0) {
            throw new IllegalArgumentException("fullBand length must be even, got " + fullBand.length);
        }
        var half = fullBand.length / 2;
        requireCapacity(lowBand, half, "lowBand");
        requireCapacity(highBand, half, "highBand");

        var even = new float[half];
        var odd = new float[half];
        for (var i = 0; i < half; i++) {
            even[i] = fullBand[2 * i];
            odd[i] = fullBand[2 * i + 1];
        }
        var oddFiltered = new float[half];
        var evenFiltered = new float[half];
        allPassQmf(odd, half, oddFiltered, ALLPASS_COEFFICIENTS_1, analysisState1);
        allPassQmf(even, half, evenFiltered, ALLPASS_COEFFICIENTS_2, analysisState2);
        for (var i = 0; i < half; i++) {
            lowBand[i] = (oddFiltered[i] + evenFiltered[i]) * 0.5f;
            highBand[i] = (oddFiltered[i] - evenFiltered[i]) * 0.5f;
        }
    }

    /**
     * Recombines a low band and a high band into a full-band block at twice the rate.
     *
     * <p>Inverts the analysis: forms the two polyphase branches from the band difference and sum, runs each
     * through its all-pass branch, interleaves the second branch into the even positions and the first into
     * the odd positions, and saturates each output sample to the int16 range. The output is twice the band
     * length.
     *
     * @param lowBand  the low-band input; never {@code null}
     * @param highBand the high-band input, the same length as {@code lowBand}; never {@code null}
     * @param fullBand the full-band output, at least twice the band length; never {@code null}
     * @throws NullPointerException     if any argument is {@code null}
     * @throws IllegalArgumentException if the bands differ in length or {@code fullBand} is too short
     */
    public void synthesis(float[] lowBand, float[] highBand, float[] fullBand) {
        java.util.Objects.requireNonNull(lowBand, "lowBand cannot be null");
        java.util.Objects.requireNonNull(highBand, "highBand cannot be null");
        java.util.Objects.requireNonNull(fullBand, "fullBand cannot be null");
        if (lowBand.length != highBand.length) {
            throw new IllegalArgumentException("lowBand and highBand must be the same length");
        }
        var half = lowBand.length;
        requireCapacity(fullBand, half * 2, "fullBand");

        var difference = new float[half];
        var sum = new float[half];
        for (var i = 0; i < half; i++) {
            difference[i] = lowBand[i] - highBand[i];
            sum[i] = lowBand[i] + highBand[i];
        }
        var branch1 = new float[half];
        var branch2 = new float[half];
        allPassQmf(sum, half, branch2, ALLPASS_COEFFICIENTS_2, synthesisState2);
        allPassQmf(difference, half, branch1, ALLPASS_COEFFICIENTS_1, synthesisState1);
        for (var i = 0; i < half; i++) {
            fullBand[2 * i] = saturate(branch1[i]);
            fullBand[2 * i + 1] = saturate(branch2[i]);
        }
    }

    /**
     * Clears the analysis and synthesis filter state so the next block does not carry prior samples.
     *
     * <p>Used when the stream is reconfigured.
     */
    public void reset() {
        java.util.Arrays.fill(analysisState1, 0.0f);
        java.util.Arrays.fill(analysisState2, 0.0f);
        java.util.Arrays.fill(synthesisState1, 0.0f);
        java.util.Arrays.fill(synthesisState2, 0.0f);
    }

    /**
     * Runs one half-rate block through the three-section all-pass branch of the quadrature-mirror filter,
     * updating the branch state.
     *
     * <p>Cascades three first-order all-pass sections, ping-ponging between the input buffer and the output
     * buffer: section one writes the output buffer from the input, section two overwrites the input buffer
     * from the output, and section three writes the output buffer from the input, so the filtered block ends
     * in the output buffer. Each section is the standard first-order all-pass recursion
     * {@code y[n] = c * (x[n] - y[n-1]) + x[n-1]} seeded from the carried per-section state, and after each
     * section the section's two state memories are refreshed with the section's last input and output sample
     * so the next block continues the recursion. The input buffer is overwritten in place by section two.
     *
     * @param input        the half-rate input block; overwritten in place by the second section
     * @param length       the number of samples to filter
     * @param output       the half-rate filtered output block, the same length as {@code input}
     * @param coefficients the three all-pass section coefficients
     * @param state        the six section memories, two per section, updated in place
     */
    private static void allPassQmf(float[] input, int length, float[] output, float[] coefficients, float[] state) {
        if (length == 0) {
            return;
        }
        var coefficient0 = coefficients[0];
        var value = coefficient0 * (input[0] - state[1]) + state[0];
        output[0] = value;
        for (var i = 1; i < length; i++) {
            value = coefficient0 * (input[i] - value) + input[i - 1];
            output[i] = value;
        }
        state[0] = input[length - 1];
        state[1] = output[length - 1];

        var coefficient1 = coefficients[1];
        value = coefficient1 * (output[0] - state[3]) + state[2];
        input[0] = value;
        for (var i = 1; i < length; i++) {
            value = coefficient1 * (output[i] - value) + output[i - 1];
            input[i] = value;
        }
        state[2] = output[length - 1];
        state[3] = input[length - 1];

        var coefficient2 = coefficients[2];
        value = coefficient2 * (input[0] - state[5]) + state[4];
        output[0] = value;
        for (var i = 1; i < length; i++) {
            value = coefficient2 * (input[i] - value) + input[i - 1];
            output[i] = value;
        }
        state[4] = input[length - 1];
        state[5] = output[length - 1];
    }

    /**
     * Clamps a synthesized sample to the int16 range.
     *
     * @param sample the sample to clamp
     * @return the sample bounded into {@code [-32768, 32767]}
     */
    private static float saturate(float sample) {
        if (sample < SATURATION_MIN) {
            return SATURATION_MIN;
        }
        if (sample >= SATURATION_MAX) {
            return SATURATION_MAX;
        }
        return sample;
    }

    /**
     * Verifies an output buffer holds at least the required number of samples.
     *
     * @param buffer   the buffer to check
     * @param required the minimum length
     * @param name     the buffer name for the error message
     * @throws IllegalArgumentException if the buffer is shorter than {@code required}
     */
    private static void requireCapacity(float[] buffer, int required, String name) {
        if (buffer.length < required) {
            throw new IllegalArgumentException(name + " length " + buffer.length + " is below required " + required);
        }
    }
}
