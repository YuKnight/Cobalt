package com.github.auties00.cobalt.calls2.media.audio.mlow.encode;

import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.EncoderTables;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.MiscTables;

/**
 * Closed-loop adaptive-codebook (ACB) / long-term-prediction (LTP) gain search for one voiced
 * analysis-by-synthesis (AbS) subframe of the MLow speech codec, the port of {@code calc_acb_gain} and the
 * weighted adaptive-codebook auto-correlation setup of {@code smpl_celp_encoder} in {@code smpl_celp.c}.
 *
 * <p>For a voiced subframe the open-loop pitch estimator ({@link OpenLoopPitch}) has fixed the pitch lags, and
 * the AbS loop has synthesized the two-tap symmetric long-term-prediction basis from the adaptive-codebook
 * history (the native {@code smpl_syn_ltp_basis}, reused from
 * {@link com.github.auties00.cobalt.calls2.media.audio.mlow.celp.CelpSynthesizer}). This class projects that
 * basis through the perceptually-weighted impulse-response auto-correlation, walks the adaptive-codebook gain
 * codebook to pick the gain pair minimizing the weighted reconstruction error scaled by the entropy-coder bit
 * cost, and produces the residual fixed-codebook target the subsequent fixed-codebook search drives.
 *
 * <p>{@link #search} returns three things: the chosen adaptive-codebook gain index, the fixed-codebook target
 * {@code d_ltp} (the perceptually-weighted target with the dequantized adaptive-codebook contribution removed),
 * and an {@link AcbParams} holder carrying the weighted cross-correlation system ({@code Phi_acb},
 * {@code d_acb_lpc}, {@code acb_basis_phi}) and the residual weighted-error floor {@code werr_in}. The
 * fixed-codebook search ({@link FcbSearch}) consumes {@code d_ltp}; the gain quantizer
 * ({@link GainQuantizer#quantizeVoiced}) consumes the whole {@link AcbParams} to re-decide the
 * adaptive-codebook gain jointly with the fixed-codebook gain. The adaptive-codebook index this class returns
 * is provisional: it is used only to form {@code d_ltp}; the bitstream index is the one
 * {@link GainQuantizer#quantizeVoiced} commits.
 *
 * <p>The weighted error of a gain pair is {@code werr_in + g' Phi_acb g - 2 d_acb_lpc' g}; the rate-distortion
 * cost multiplies it by the conditional inverse-probability penalty of the index under the previous subframe's
 * committed adaptive-codebook index (the native {@code prev_acb_idx}, threaded in by the AbS loop, with
 * {@code -1} selecting the unconditional context). The cost tables come from {@link EncoderTables#gainCosts()}
 * and the gain codebooks from {@link MiscTables}.
 *
 * <p>Scope is the SMPL 16 kHz / 60 ms / mono low-band voiced path with {@code SMPL_ACBG_M == 2}. All
 * accumulations are single precision to mirror the native arithmetic. This type is stateless and thread-safe.
 *
 * @implNote This implementation ports {@code calc_acb_gain} verbatim: each basis vector is convolved with the
 * symmetric Toeplitz weighting through {@code smpl_mult_symtoepl2} into {@code acb_basis_phi}, the
 * {@code SMPL_ACBG_M}-square {@code Phi_acb} is the cross of basis against weighted basis, {@code d_acb_lpc} is
 * basis against the target, and the search minimizes {@code smpl_wnrg2} scaled by the conditional cost. The
 * fixed-codebook target is formed by {@code d_lpc} minus each weighted basis scaled by its negated chosen gain,
 * the native two {@code smpl_add_scale_vec} / {@code smpl_add_scale_vec_inplace} steps. The Toeplitz multiply
 * reads the flipped weighting column {@code PhiFlip} the AbS loop builds, matching the native
 * {@code pScratch->PhiFlip + SMPL_MAX_SF_LEN - L_resp + 1} base pointer.
 */
public final class AcbSearch {
    /**
     * Number of adaptive-codebook gain taps, the native {@code SMPL_ACBG_M}.
     */
    private static final int ACBG_M = 2;

    /**
     * Number of adaptive-codebook gain codebook entries, the native {@code SMPL_ACBG_N}.
     */
    private static final int ACBG_N = 16;

    /**
     * Maximum low-band subframe length in samples, the native {@code SMPL_MAX_SF_LEN}; the flipped weighting
     * column {@code PhiFlip} is centered at this index.
     */
    private static final int MAX_SF_LEN = 160;

    /**
     * The Q14 scale {@code 1 / 2^14} dequantizing the adaptive-codebook gain codebook entries.
     */
    private static final float Q14_SCALE = 1.0f / (1 << 14);

    /**
     * The gain inverse-probability cost tables, the native {@code CelpTables} gain-cost fields.
     */
    private final EncoderTables.GainCosts gainCosts;

    /**
     * The weighted adaptive-codebook cross-correlation system the gain search builds, the encode-time fields of
     * the native {@code ACBGparams} struct.
     *
     * <p>This holder threads the search state forward to {@link GainQuantizer#quantizeVoiced}, which augments
     * it with the fixed-codebook excitation to re-decide the gains jointly.
     *
     * @param werrIn      the residual weighted-error floor (the zero-input-response and target energy), the
     *                    native {@code werr_in}
     * @param phiAcb      the {@code SMPL_ACBG_M}-square weighted basis auto-correlation, row-major, the native
     *                    {@code Phi_acb}
     * @param dAcbLpc     the basis-against-target cross-correlation, {@code SMPL_ACBG_M} entries, the native
     *                    {@code d_acb_lpc}
     * @param acbBasisPhi the perceptually-weighted adaptive-codebook basis, {@code SMPL_ACBG_M * fcbSubfrlen}
     *                    entries, the native {@code acb_basis_phi}
     */
    public record AcbParams(float werrIn, float[] phiAcb, float[] dAcbLpc, float[] acbBasisPhi) {
    }

    /**
     * The closed-loop adaptive-codebook search result for one voiced subframe.
     *
     * @param acbIdx the provisional adaptive-codebook gain index used to form {@link #dLtp()}; the bitstream
     *               index is committed by {@link GainQuantizer#quantizeVoiced}
     * @param dLtp   the fixed-codebook target, the perceptually-weighted target with the dequantized
     *               adaptive-codebook contribution removed, {@code fcbSubfrlen} entries
     * @param params the weighted cross-correlation system the gain quantizer consumes
     */
    public record Result(int acbIdx, float[] dLtp, AcbParams params) {
    }

    /**
     * Constructs an adaptive-codebook gain search bound to the shared gain cost tables.
     */
    public AcbSearch() {
        this.gainCosts = EncoderTables.gainCosts();
    }

    /**
     * Runs the closed-loop adaptive-codebook gain search for one voiced subframe, {@code calc_acb_gain}.
     *
     * <p>Projects the long-term-prediction basis through the perceptually-weighted impulse-response
     * auto-correlation, picks the adaptive-codebook gain index minimizing the rate-distortion-scaled weighted
     * error, and forms the fixed-codebook target with the chosen contribution removed.
     *
     * @param phiFlip     the flipped perceptually-weighted impulse-response auto-correlation column the AbS loop
     *                    builds, the native {@code pScratch->PhiFlip}; centered at {@code SMPL_MAX_SF_LEN}, at
     *                    least {@code 2 * SMPL_MAX_SF_LEN} entries
     * @param lResp       the perceptual-response length, the native {@code L_resp} ({@code perc_resp_len})
     * @param acbBasis    the long-term-prediction basis from {@code smpl_syn_ltp_basis},
     *                    {@code SMPL_ACBG_M * fcbSubfrlen} entries
     * @param dLpc        the perceptually-weighted target cross-correlation, {@code fcbSubfrlen} entries
     * @param werrIn      the residual weighted-error floor, the native {@code werr_in}
     * @param fcbSubfrlen the subframe length in samples, the native {@code fcb_subfrlen}
     * @param lowRate     {@code true} selects the low-rate gain codebook and cost rows
     * @param prevAcbIdx  the previous subframe's committed adaptive-codebook index, or {@code -1} for the
     *                    unconditional context
     * @return the provisional adaptive-codebook index, fixed-codebook target, and weighted cross-correlation
     *         system
     */
    public Result search(float[] phiFlip, int lResp, float[] acbBasis, float[] dLpc, float werrIn,
                         int fcbSubfrlen, boolean lowRate, int prevAcbIdx) {
        float[] acbBasisPhi = new float[ACBG_M * fcbSubfrlen];
        float[] phiAcb = new float[ACBG_M * ACBG_M];
        float[] dAcbLpc = new float[ACBG_M];
        // Native base: pScratch->PhiFlip + SMPL_MAX_SF_LEN - L_resp + 1, length 2*L_resp-1 symmetric column.
        int symBase = MAX_SF_LEN - lResp + 1;
        for (int m = 0; m < ACBG_M; m++) {
            multSymToepl2(phiFlip, symBase, lResp, acbBasis, m * fcbSubfrlen, acbBasisPhi, m * fcbSubfrlen, fcbSubfrlen);
            for (int i = 0; i < ACBG_M; i++) {
                phiAcb[m * ACBG_M + i] = dotProd(acbBasis, i * fcbSubfrlen, acbBasisPhi, m * fcbSubfrlen, fcbSubfrlen);
            }
            dAcbLpc[m] = dotProd(acbBasis, m * fcbSubfrlen, dLpc, 0, fcbSubfrlen);
        }

        float bestRd = 1e30f;
        int bestAcbgIdx = 0;
        int transitionIdx = prevAcbIdx == -1 ? 0 : (prevAcbIdx + 1);
        float[] acbgInvProb = lowRate ? gainCosts.acbgInvProbLr()[transitionIdx] : gainCosts.acbgInvProbHr()[transitionIdx];
        short[] cbAcbgains = lowRate ? MiscTables.ACB_GAINS_LR_Q14 : MiscTables.ACB_GAINS_HR_Q14;
        float[] acbGains = new float[ACBG_M];
        for (int n = 0; n < ACBG_N; n++) {
            for (int m = 0; m < ACBG_M; m++) {
                acbGains[m] = cbAcbgains[n * ACBG_M + m] * Q14_SCALE;
            }
            float werrOut = werrIn + wnrg2(phiAcb, acbGains)
                    - 2.0f * (dAcbLpc[0] * acbGains[0] + dAcbLpc[1] * acbGains[1]);
            float rd = werrOut * acbgInvProb[n];
            if (rd < bestRd) {
                bestRd = rd;
                bestAcbgIdx = n;
            }
        }

        // Fixed-codebook target: d_ltp = d_lpc - g0 * acb_basis_phi[0] - g1 * acb_basis_phi[1].
        float[] dLtp = new float[fcbSubfrlen];
        float g = -cbAcbgains[bestAcbgIdx * ACBG_M] * Q14_SCALE;
        for (int i = 0; i < fcbSubfrlen; i++) {
            dLtp[i] = dLpc[i] + g * acbBasisPhi[i];
        }
        g = -cbAcbgains[bestAcbgIdx * ACBG_M + 1] * Q14_SCALE;
        for (int i = 0; i < fcbSubfrlen; i++) {
            dLtp[i] += g * acbBasisPhi[fcbSubfrlen + i];
        }

        return new Result(bestAcbgIdx, dLtp, new AcbParams(werrIn, phiAcb, dAcbLpc, acbBasisPhi));
    }

    /**
     * Multiplies a symmetric Toeplitz weighting against a vector, {@code smpl_mult_symtoepl2}.
     *
     * <p>The weighting is the symmetric column {@code c} centered at {@code cBase + lResp - 1}; output
     * {@code n} is the dot product of the column window against the matching window of {@code x}. The native
     * code splits the run into a ramp-up, a steady, and a ramp-down region so the column window slides exactly
     * as the symmetric Toeplitz band dictates.
     *
     * @param c     the symmetric weighting column array, the native {@code PhiFlip}
     * @param cBase the offset of the column's first valid sample, the native
     *              {@code SMPL_MAX_SF_LEN - L_resp + 1}
     * @param lResp the half-bandwidth, the native {@code L_resp}
     * @param x     the input vector array
     * @param xOff  the offset of the input vector
     * @param y     the output array
     * @param yOff  the offset of the output vector
     * @param n     the output length, the native {@code N} ({@code fcb_subfrlen})
     */
    private static void multSymToepl2(float[] c, int cBase, int lResp, float[] x, int xOff,
                                      float[] y, int yOff, int n) {
        int idx = 0;
        int len = lResp;
        for (; idx < lResp - 1; idx++) {
            y[yOff + idx] = dotProd(c, cBase + lResp - 1 - idx, x, xOff, len++);
        }
        len = 2 * lResp;
        for (; idx < n - lResp; idx++) {
            y[yOff + idx] = dotProd(c, cBase, x, xOff + idx - lResp + 1, len);
        }
        for (; idx < n; idx++) {
            y[yOff + idx] = dotProd(c, cBase, x, xOff + idx - lResp + 1, --len);
        }
    }

    /**
     * Evaluates the two-variable weighted-energy quadratic form, {@code smpl_wnrg2}.
     *
     * <p>Computes {@code x' C x} for the symmetric {@code 2x2} matrix {@code C} held row-major and the
     * two-vector {@code x}, in the native accumulation order.
     *
     * @param c the row-major {@code 2x2} matrix
     * @param x the two-vector
     * @return the quadratic form {@code x' C x}
     */
    private static float wnrg2(float[] c, float[] x) {
        return x[0] * (c[0] * x[0] + c[1] * x[1])
                + x[1] * (c[2] * x[0] + c[3] * x[1]);
    }

    /**
     * Computes a single-precision dot product over a window of two arrays, {@code smpl_dot_prod}.
     *
     * <p>The reference {@code smpl_dot_prod} in {@code smpl_codec_util.c} is compiled at {@code -Ofast} with
     * {@code -msse2}, which auto-vectorizes the accumulation into a four-lane packed-single reduction: four
     * partial sums each gather the products at indices congruent to their lane modulo four, are combined as
     * {@code (s0 + s2) + (s1 + s3)}, and the trailing {@code len mod 4} products are added sequentially. The
     * four-lane structure changes the rounding relative to a left-to-right sum, and the searched targets are
     * near-zero after adaptive-codebook cancellation, so the lane order is load-bearing and is reproduced here
     * exactly.
     *
     * @param a    the first array
     * @param aOff the offset into the first array
     * @param b    the second array
     * @param bOff the offset into the second array
     * @param len  the number of elements
     * @return the accumulated single-precision dot product
     */
    private static float dotProd(float[] a, int aOff, float[] b, int bOff, int len) {
        float s0 = 0.0f;
        float s1 = 0.0f;
        float s2 = 0.0f;
        float s3 = 0.0f;
        int m = len & ~3;
        int i = 0;
        for (; i < m; i += 4) {
            s0 += a[aOff + i] * b[bOff + i];
            s1 += a[aOff + i + 1] * b[bOff + i + 1];
            s2 += a[aOff + i + 2] * b[bOff + i + 2];
            s3 += a[aOff + i + 3] * b[bOff + i + 3];
        }
        float acc = (s0 + s2) + (s1 + s3);
        for (; i < len; i++) {
            acc += a[aOff + i] * b[bOff + i];
        }
        return acc;
    }
}
