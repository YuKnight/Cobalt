package com.github.auties00.cobalt.calls2.media.audio.mlow.encode;

import com.github.auties00.cobalt.calls2.media.audio.mlow.encode.LsfQuantizer.QuantizedLsf;

/**
 * Short-term analysis front-end of the MLow speech encoder, chaining the linear-prediction analysis and the
 * line-spectral-frequency vector quantizer into one per-frame call, the analysis-and-quantization head of the
 * {@code smpl_base_encode} loop in {@code smpl_core_encoder.c}.
 *
 * <p>The native encoder turns each high-passed 16 kHz frame into a quantized short-term spectrum in two steps
 * that this class wires together:
 * <ul>
 * <li>{@code smpl_window} then {@code smpl_lpc} then {@code smpl_bwe_expand} build the bandwidth-expanded
 * prediction filter {@code A} from the frame's look-back buffer ({@link LpcAnalysis});</li>
 * <li>{@code smpl_lsf_quant} or {@code smpl_lsf_quant_cond} search the two-stage codebook for the stage-1 and
 * stage-2 indices that quantize the line spectral frequencies of {@code A} ({@link LsfQuantizer}).</li>
 * </ul>
 * The conditional branch is taken exactly when the native {@code cond_coding} flag is set, which holds when the
 * frame is not the first of its packet and its voicing equals the previous frame's voicing; on that branch the
 * quantizer conditions on the previous frame's quantized line spectral frequencies, which this front-end
 * threads on its {@link #previousLsf} field.
 *
 * <p>This class owns the cross-frame analysis state the two stages do not: the previous frame's quantized LSF
 * vector that the conditional path reads and that the native encoder keeps as {@code prev_lsf}. The caller
 * supplies the already windowed-and-high-passed look-back buffer per frame (the native {@code lpcbuf}); the
 * windowing geometry and the high-pass live upstream of the analysis and are not reproduced here.
 *
 * <p>The selected indices ({@link QuantizedLsf#indices()}) are the bit-exact gate against the native encoder.
 * Because the {@link LpcAnalysis} prediction filter is bit-for-bit identical to the native trace oracle and the
 * {@link LsfQuantizer} survivor search replays every {@code float} accumulation in native order, the stage-1,
 * conditional, and stage-2 indices match the native encoder with zero flips across tonal, synthetic-speech and
 * decoded-speech input on the high-rate path, and the quantized LSF vector round-trips through the decode-side
 * dequantizer to exactly the vector the decoder reconstructs.
 *
 * <p>Scope is the SMPL 16 kHz / 60 ms / mono low-band encode path with prediction order
 * {@value #LPC_ORDER}. One front-end instance carries the state of a single logical stream; construct one per
 * stream, feed it every frame in order, and call {@link #reset()} between independent streams. This type is
 * stateful and is not thread-safe.
 *
 * @implNote This implementation holds one {@link LpcAnalysis} and one {@link LsfQuantizer}, both immutable and
 * shareable, and the single mutable {@code prev_lsf} carry. The look-back buffer is passed straight to
 * {@link LpcAnalysis#window(float[], int, boolean, float[])} and then {@link LpcAnalysis#analyze(float[])},
 * which already applies the bandwidth expansion, so this front-end adds no arithmetic of its own beyond the
 * carry update. The previous-LSF carry is overwritten with each frame's quantized reconstruction
 * ({@link QuantizedLsf#lsf()}). On the high-rate path this equals the native {@code enc_state->prev_lsf}, which
 * the native encoder writes after the per-subframe LSF interpolation: the last subframe's interpolation
 * coefficient is {@code 1.0} for every high-rate configuration ({@code smpl_lsf_interpol_4} ends in
 * {@code 1.0} for both interpolation indices, and {@code smpl_lsf_interpol_2} ends in {@code 1.0} for
 * index {@code 0}), so the interpolated last-subframe vector the native code stores is exactly the quantized
 * vector this front-end stores. Validated against the trace oracle, the LSF indices are bit-exact across every
 * frame on the high-rate path (target bitrates above the low-rate threshold) including all conditionally coded
 * frames.
 */
public final class EncodeFrontEnd {
    /**
     * Linear-prediction order of the MLow short-term filter, {@code SMPL_LPC_ORDER}; the length of the
     * quantized LSF vector carried across frames.
     */
    private static final int LPC_ORDER = 16;

    /**
     * Length of the 20 ms-frame analysis look-back buffer, the native {@code lpcbuf_len} for a 60 ms packet.
     *
     * <p>The window stage reads exactly this many samples from the supplied look-back buffer.
     */
    private static final int LPC_BUF_LEN_20MS = 448;

    /**
     * The short-term linear-prediction analysis, shared and immutable.
     */
    private final LpcAnalysis analysis;

    /**
     * The two-stage line-spectral-frequency vector quantizer, shared and immutable.
     */
    private final LsfQuantizer quantizer;

    /**
     * The previous frame's quantized LSF vector, the native {@code enc_state->prev_lsf}.
     *
     * <p>Read only on the conditional coding path and overwritten with each frame's quantized reconstruction;
     * starts zeroed, which the first frame of a stream (always non-conditional) does not read.
     */
    private final float[] previousLsf;

    /**
     * The windowed look-back buffer scratch, the native {@code lpcbuf_windowed}, reused across frames.
     */
    private final float[] windowed;

    /**
     * The result of one frame's short-term analysis and quantization.
     *
     * <p>{@code analysis} is the full {@link LpcAnalysis} output (the raw and bandwidth-expanded filters, the
     * analysis line spectral frequencies, and the autocorrelation); {@code quantized} is the
     * {@link LsfQuantizer} output (the selected indices, the quantized LSF vector, and the rate-distortion
     * scalars). The indices are the bit-exact gate; the quantized LSF vector is the one threaded back as the
     * previous-frame carry.
     *
     * @param analysis  the short-term analysis result for the frame
     * @param quantized the LSF quantization result for the frame
     */
    public record FrameResult(LpcAnalysis.Result analysis, QuantizedLsf quantized) {
    }

    /**
     * Constructs a front-end with a fresh analysis stage, quantizer, and zeroed cross-frame carry.
     *
     * <p>The analysis allocates its own FFT setup and the quantizer loads the shared encoder tables; the
     * previous-LSF carry starts zeroed, ready for the first non-conditional frame of a stream.
     */
    public EncodeFrontEnd() {
        this.analysis = new LpcAnalysis();
        this.quantizer = new LsfQuantizer();
        this.previousLsf = new float[LPC_ORDER];
        this.windowed = new float[LPC_BUF_LEN_20MS];
    }

    /**
     * Returns this front-end to its freshly constructed state.
     *
     * <p>Zeroes the previous-LSF carry; the analysis and quantizer stages are stateless across frames, so no
     * other state is held. Call this between independent streams; do not call it between the frames of one
     * stream, which must thread the carry.
     */
    public void reset() {
        java.util.Arrays.fill(previousLsf, 0.0f);
    }

    /**
     * Analyzes and quantizes one frame's short-term spectrum, the analysis-and-quantization head of
     * {@code smpl_base_encode}.
     *
     * <p>Windows the look-back buffer, runs the linear-prediction analysis and bandwidth expansion, and
     * searches the two-stage codebook for the quantizer indices, taking the conditional path when
     * {@code condCoding} is set. The quantized LSF vector is stored as the previous-frame carry for the next
     * conditionally coded frame.
     *
     * @param lookback   the frame's look-back buffer, at least {@value #LPC_BUF_LEN_20MS} samples from
     *                   {@code offset}, the native {@code lpcbuf}
     * @param offset     the offset of the first look-back sample within {@code lookback}
     * @param longWindow {@code true} to use the long trailing analysis taper (more frames follow in the
     *                   packet), {@code false} for the short taper with trailing zeros, the native
     *                   {@code numframe < frames_per_packet - 1}
     * @param condCoding {@code true} to take the conditional quantization path, the native {@code cond_coding}
     * @param surv       the survivor count, the native {@code lsf_surv}
     * @param rdwAdj      the rate-distortion weighting adjustment, the native {@code RDw_adj}
     * @param voiced     {@code 0} for the unvoiced class, {@code 1} for the voiced class, the native
     *                   {@code voiced_buf} entry
     * @param lowRate    {@code 0} for high rate, {@code 1} for low rate, the native {@code lowRate}
     * @return the frame's analysis and quantization result
     */
    public FrameResult process(float[] lookback, int offset, boolean longWindow, boolean condCoding,
                               int surv, float rdwAdj, int voiced, int lowRate) {
        analysis.window(lookback, offset, longWindow, windowed);
        LpcAnalysis.Result lpc = analysis.analyze(windowed);

        QuantizedLsf quant;
        if (condCoding) {
            quant = quantizer.quantCond(surv, lpc.lpc(), previousLsf, rdwAdj, voiced, lowRate);
        } else {
            quant = quantizer.quant(surv, lpc.lpc(), rdwAdj, voiced, lowRate);
        }
        // The carry stored here equals the native enc_state->prev_lsf only on the high-rate path, where every
        // smpl_lsf_interpol_4 row (and smpl_lsf_interpol_2 row 0) ends in 1.0 so the interpolated last-subframe
        // LSF is exactly the quantized vector. On the low-rate alternative-index path smpl_lsf_interpol_2[1]
        // ends in 0.95, so the true carry is the blended last-subframe vector; that path threads its carry
        // through EncoderLsfInterp.commit (the committed candidate's prevLsf) rather than this field. The
        // high-rate quantizer front-end stores the quantized vector directly, which matches.
        System.arraycopy(quant.lsf(), 0, previousLsf, 0, LPC_ORDER);
        return new FrameResult(lpc, quant);
    }
}
