package com.github.auties00.cobalt.calls2.media.audio.mlow.silk;

/**
 * Integer fixed-point conversion of normalized line spectral frequencies (NLSFs) into linear-prediction
 * filter coefficients, the bit-exact port of {@code silk/NLSF2A.c} from the SILK signal-processing library
 * that the MLow speech codec embeds.
 *
 * <p>MLow reconstructs the short-term linear-prediction filter of every subframe by mapping a quantized
 * NLSF vector (the cosine-domain line spectral frequencies, in Q15) to monic whitening-filter coefficients.
 * The mapping is the classic split into an even and an odd symmetric polynomial whose roots are
 * {@code 2*cos(LSF)}: a piecewise-linear table approximates the cosine, two convolutions build the
 * polynomials from their roots, and the even and odd parts are combined into the filter taps. The whole
 * pipeline runs in fixed point so the result is bit-reproducible across platforms; this class reproduces
 * every shift, rounding, and overflow exactly.
 *
 * <p>The class exposes two entry points:
 * <ul>
 * <li>{@link #nlsf2a32(int[], short[], int)} ports {@code silk_NLSF2A_32}: it stops at the unscaled
 * {@code QA+1} (that is, Q17) integer coefficients, which is the form the MLow float bridge consumes. This
 * is the only entry point the MLow decode path drives.</li>
 * <li>{@link #nlsf2a(short[], short[], int)} ports the full {@code silk_NLSF2A}: it additionally fits
 * the coefficients into Q12 {@code int16} ({@link #lpcFit(short[], int[], int, int, int)}) and applies the
 * stability bandwidth-expansion loop ({@link #bwExpander32(int[], int, int)} guarded by
 * {@link #lpcInversePredGain(short[], int)}). MLow does not call this variant (it stabilizes in the float
 * domain instead), but it is ported for completeness and faithfulness to the SILK source.</li>
 * </ul>
 *
 * <p>Q-format conventions. The cosine root vector is built in {@code QA} (Q16). The polynomial coefficients
 * accumulate in {@code QA} as well. The combined filter coefficients land in {@code QA+1} (Q17); the float
 * bridge divides them by {@code 2^17}. The full {@link #nlsf2a(short[], short[], int)} path then
 * rounds {@code QA+1 -> Q12} for the {@code int16} output, and the stability inner product runs in Q30.
 *
 * <p>SILK macro fidelity. The fixed-point macros from {@code SigProc_FIX.h} are reproduced as private
 * helpers with the exact integer arithmetic of their C definitions: {@link #rshiftRound(int, int)} and
 * {@link #rshiftRound64(long, int)} round by the SILK rule {@code (((a >> (s-1)) + 1) >> 1)} (special-cased
 * at {@code s == 1}); {@link #smull(int, int)} widens to {@code long} before multiplying; {@link #smulww(int,
 * int)} returns {@code (a*b) >> 16} of a 64-bit product; {@link #smmul(int, int)} returns the high 32 bits of
 * the 64-bit product; and {@link #inverse32VarQ(int, int)} replays the Newton-style reciprocal of
 * {@code silk_INVERSE32_varQ}. All arithmetic is done in {@code int}/{@code long} two's-complement, which
 * matches C {@code opus_int32}/{@code opus_int64} including deliberate overflow in the residual steps.
 *
 * <p>Scope is the SMPL 16 kHz / 60 ms / mono path with {@code SMPL_LPC_ORDER == 16}; the order-10 and
 * order-4 root orderings of the SILK source are carried so the port stays a faithful copy, but MLow only
 * exercises order 16. This type is stateless and thread-safe.
 *
 * @implNote This implementation is a literal transcription of {@code silk/NLSF2A.c},
 * {@code silk/LPC_fit.c}, {@code silk/bwexpander_32.c}, and {@code silk/LPC_inv_pred_gain.c}. The cosine
 * table {@link #LSF_COS_TAB_Q12} is copied byte-for-byte from {@code silk/table_LSF_cos.c}. The reciprocal
 * helper {@link #inverse32VarQ(int, int)} keeps the C variable names and intermediate Q-domains so the
 * residual-refinement overflow (which the C code relies on) is reproduced exactly. {@link #clz32(int)} maps
 * {@code silk_CLZ32} to {@link Integer#numberOfLeadingZeros(int)}, which returns {@code 32} for a zero input
 * exactly as the SILK macro does.
 */
public final class SilkNlsf2a {
    /**
     * Q-domain of the cosine root vector and the convolution polynomials, the SILK {@code QA == 16}.
     *
     * <p>The line-spectral cosine values and the even/odd polynomials are carried in this Q-domain; the
     * combined filter coefficients are then one bit higher, in {@code QA + 1}.
     */
    private static final int QA = 16;

    /**
     * Size of the cosine approximation table, the SILK {@code LSF_COS_TAB_SZ_FIX}.
     *
     * <p>The table holds this many entries plus one guard entry so the linear interpolation can read both
     * {@code table[f_int]} and {@code table[f_int + 1]} for any {@code f_int} in {@code [0, 127]}.
     */
    private static final int LSF_COS_TAB_SZ = 128;

    /**
     * Maximum prediction filter order the SILK helpers size their scratch arrays for, the SILK
     * {@code SILK_MAX_ORDER_LPC}.
     *
     * <p>MLow uses order 16; this larger bound is kept so the ported helpers match the SILK buffer sizes.
     */
    private static final int MAX_ORDER_LPC = 24;

    /**
     * Maximum number of stability bandwidth-expansion iterations in the full
     * {@link #nlsf2a(short[], short[], int)} path, the SILK {@code MAX_LPC_STABILIZE_ITERATIONS}.
     */
    private static final int MAX_LPC_STABILIZE_ITERATIONS = 16;

    /**
     * Reciprocal of the maximum tolerated prediction power gain, the SILK {@code MAX_PREDICTION_POWER_GAIN}
     * of {@code 1e4f}.
     *
     * <p>The inverse-prediction-gain test rejects a filter whose inverse gain in Q30 falls below
     * {@code 2^30 / MAX_PREDICTION_POWER_GAIN}, which marks it as (too close to) unstable.
     */
    private static final float MAX_PREDICTION_POWER_GAIN = 1.0e4f;

    /**
     * Largest {@code opus_int16} value, the SILK {@code silk_int16_MAX}.
     */
    private static final int INT16_MAX = 0x7FFF;

    /**
     * Smallest {@code opus_int16} value, the SILK {@code silk_int16_MIN}.
     */
    private static final int INT16_MIN = -0x8000;

    /**
     * Root ordering for an order-16 filter, the SILK {@code ordering16}.
     *
     * <p>The cosine roots are scattered into the convolution buffer in this order rather than ascending,
     * which the SILK comment notes improves the numerical accuracy of the polynomial-from-roots expansion.
     */
    private static final int[] ORDERING16 = {0, 15, 8, 7, 4, 11, 12, 3, 2, 13, 10, 5, 6, 9, 14, 1};

    /**
     * Root ordering for an order-10 filter, the SILK {@code ordering10}.
     */
    private static final int[] ORDERING10 = {0, 9, 6, 3, 4, 5, 8, 1, 2, 7};

    /**
     * Root ordering for an order-4 filter, the SILK {@code ordering4}.
     */
    private static final int[] ORDERING4 = {0, 3, 2, 1};

    /**
     * Cosine approximation table in Q12, the SILK {@code silk_LSFCosTab_FIX_Q12}.
     *
     * <p>Entry {@code i} is {@code 2^12 * cos(pi * i / 128)} rounded to an even integer; the trailing guard
     * entry mirrors entry {@code 0} negated. The piecewise-linear interpolation reads two adjacent entries
     * and a fractional weight to approximate {@code 2*cos(LSF)} for each line spectral frequency.
     */
    private static final short[] LSF_COS_TAB_Q12 = {
            8192, 8190, 8182, 8170,
            8152, 8130, 8104, 8072,
            8034, 7994, 7946, 7896,
            7840, 7778, 7714, 7644,
            7568, 7490, 7406, 7318,
            7226, 7128, 7026, 6922,
            6812, 6698, 6580, 6458,
            6332, 6204, 6070, 5934,
            5792, 5648, 5502, 5352,
            5198, 5040, 4880, 4718,
            4552, 4382, 4212, 4038,
            3862, 3684, 3502, 3320,
            3136, 2948, 2760, 2570,
            2378, 2186, 1990, 1794,
            1598, 1400, 1202, 1002,
            802, 602, 402, 202,
            0, -202, -402, -602,
            -802, -1002, -1202, -1400,
            -1598, -1794, -1990, -2186,
            -2378, -2570, -2760, -2948,
            -3136, -3320, -3502, -3684,
            -3862, -4038, -4212, -4382,
            -4552, -4718, -4880, -5040,
            -5198, -5352, -5502, -5648,
            -5792, -5934, -6070, -6204,
            -6332, -6458, -6580, -6698,
            -6812, -6922, -7026, -7128,
            -7226, -7318, -7406, -7490,
            -7568, -7644, -7714, -7778,
            -7840, -7896, -7946, -7994,
            -8034, -8072, -8104, -8130,
            -8152, -8170, -8182, -8190,
            -8192
    };

    /**
     * Prevents instantiation of this static utility holder.
     *
     * <p>All conversion routines are static; the class carries no state.
     */
    private SilkNlsf2a() {
    }

    /**
     * Converts an NLSF vector to unscaled {@code QA+1} (Q17) monic whitening-filter coefficients, the port
     * of {@code silk_NLSF2A_32}.
     *
     * <p>Each normalized line spectral frequency in {@code nlsf} (Q15) is mapped to {@code 2*cos(LSF)} in
     * {@code QA} by reading the {@link #LSF_COS_TAB_Q12} cosine table at the integer part and linearly
     * interpolating with the fractional part; the roots are scattered into a working buffer in the
     * order-specific scatter order ({@link #ORDERING16}/{@link #ORDERING10}/{@link #ORDERING4}). The even and
     * odd polynomials are then built from their roots with {@link #findPoly(int[], int[], int, int)} and
     * combined into the filter coefficients, which are left in {@code QA+1}. The output is written in place;
     * no fitting, rounding to {@code int16}, or stability check is performed here.
     *
     * @param a32QA1 the output filter coefficients in {@code QA+1} (Q17), at least {@code d} entries written
     * @param nlsf   the normalized line spectral frequencies in Q15, {@code d} entries, each non-negative
     * @param d      the filter order; must be {@code 4}, {@code 10}, or {@code 16}
     */
    public static void nlsf2a32(int[] a32QA1, short[] nlsf, int d) {
        int[] ordering = d == 16 ? ORDERING16 : d == 10 ? ORDERING10 : ORDERING4;
        int[] cosLsfQA = new int[MAX_ORDER_LPC];
        for (int k = 0; k < d; k++) {
            int nlsfK = nlsf[k] & 0xFFFF;

            // f_int on a scale 0-127 (rounded down)
            int fInt = nlsfK >> (15 - 7);

            // f_frac, range 0..255
            int fFrac = nlsfK - (fInt << (15 - 7));

            int cosVal = LSF_COS_TAB_Q12[fInt];
            int delta = LSF_COS_TAB_Q12[fInt + 1] - cosVal;

            // Linear interpolation, result in QA
            cosLsfQA[ordering[k]] = rshiftRound((cosVal << 8) + delta * fFrac, 20 - QA);
        }

        int dd = d >> 1;

        int[] p = new int[MAX_ORDER_LPC / 2 + 1];
        int[] q = new int[MAX_ORDER_LPC / 2 + 1];
        findPoly(p, cosLsfQA, 0, dd);
        findPoly(q, cosLsfQA, 1, dd);

        for (int k = 0; k < dd; k++) {
            int pTmp = p[k + 1] + p[k];
            int qTmp = q[k + 1] - q[k];
            a32QA1[k] = -qTmp - pTmp;
            a32QA1[d - k - 1] = qTmp - pTmp;
        }
    }

    /**
     * Builds one symmetric polynomial from its interleaved cosine roots, the port of
     * {@code silk_NLSF2A_find_poly}.
     *
     * <p>The roots are read from {@code cLSF} starting at {@code offset} with stride two (the SILK source
     * passes {@code &cos_LSF_QA[0]} for the even polynomial and {@code &cos_LSF_QA[1]} for the odd one). The
     * polynomial is grown one root at a time by the standard recurrence; every multiply-accumulate uses the
     * 64-bit-widened SILK {@code silk_SMULL} followed by {@link #rshiftRound64(long, int)} so the rounding
     * matches the fixed-point source exactly. The result is in {@code QA} with {@code dd + 1} coefficients.
     *
     * @param out    the output polynomial in {@code QA}, {@code dd + 1} entries written
     * @param cLSF   the cosine root buffer in {@code QA}, read with stride two from {@code offset}
     * @param offset the index of the first root to read ({@code 0} for the even polynomial, {@code 1} for
     *               the odd one)
     * @param dd     the polynomial order, half the filter order
     */
    private static void findPoly(int[] out, int[] cLSF, int offset, int dd) {
        out[0] = 1 << QA;
        out[1] = -cLSF[offset];
        for (int k = 1; k < dd; k++) {
            int ftmp = cLSF[offset + 2 * k];
            out[k + 1] = (out[k - 1] << 1) - (int) rshiftRound64(smull(ftmp, out[k]), QA);
            for (int n = k; n > 1; n--) {
                out[n] += out[n - 2] - (int) rshiftRound64(smull(ftmp, out[n - 1]), QA);
            }
            out[1] -= ftmp;
        }
    }

    /**
     * Converts an NLSF vector to Q12 {@code int16} monic whitening-filter coefficients with the stability
     * loop, the port of the full {@code silk_NLSF2A}.
     *
     * <p>Runs {@link #nlsf2a32(int[], short[], int)} to obtain the unscaled {@code QA+1} coefficients, fits
     * them into Q12 {@code int16} with {@link #lpcFit(short[], int[], int, int, int)}, then repeatedly checks
     * stability with {@link #lpcInversePredGain(short[], int)}; while the filter is unstable (and fewer than
     * {@link #MAX_LPC_STABILIZE_ITERATIONS} iterations have run) it bandwidth-expands the unscaled
     * coefficients with {@link #bwExpander32(int[], int, int)}, re-rounds to Q12, and measures again. MLow
     * does not invoke this path; it is provided for a complete, faithful port of the SILK function.
     *
     * @param aQ12 the output filter coefficients in Q12, {@code d} entries written
     * @param nlsf the normalized line spectral frequencies in Q15, {@code d} entries
     * @param d    the filter order; must be {@code 4}, {@code 10}, or {@code 16}
     */
    public static void nlsf2a(short[] aQ12, short[] nlsf, int d) {
        int[] a32QA1 = new int[MAX_ORDER_LPC];
        nlsf2a32(a32QA1, nlsf, d);

        lpcFit(aQ12, a32QA1, 12, QA + 1, d);

        for (int i = 0; lpcInversePredGain(aQ12, d) == 0 && i < MAX_LPC_STABILIZE_ITERATIONS; i++) {
            bwExpander32(a32QA1, d, 65536 - (2 << i));
            for (int k = 0; k < d; k++) {
                aQ12[k] = (short) rshiftRound(a32QA1[k], QA + 1 - 12);
            }
        }
    }

    /**
     * Fits {@code int32} coefficients into {@code int16} without wrap-around, the port of
     * {@code silk_LPC_fit}.
     *
     * <p>The coefficients are scaled from Q{@code QIN} to Q{@code QOUT}. Up to ten times, the largest absolute
     * coefficient is found; if scaling it to {@code QOUT} would exceed {@code int16}, the chirp factor that
     * just brings it into range is computed and {@link #bwExpander32(int[], int, int)} is applied to the
     * input in place, then the loop repeats. On the tenth failed iteration the output is saturated to
     * {@code int16} and the input is written back as the saturated value re-shifted; otherwise the output is
     * the rounded down-shift. The input array is modified in place by the chirp passes.
     *
     * @param aQOut the output coefficients in Q{@code QOUT}, {@code d} entries written
     * @param aQIn  the input coefficients in Q{@code QIN}, modified in place by the chirp passes
     * @param qOut  the output Q-domain
     * @param qIn   the input Q-domain
     * @param d     the filter order
     */
    private static void lpcFit(short[] aQOut, int[] aQIn, int qOut, int qIn, int d) {
        int idx = 0;
        int i;
        for (i = 0; i < 10; i++) {
            int maxabs = 0;
            for (int k = 0; k < d; k++) {
                int absval = Math.abs(aQIn[k]);
                if (absval > maxabs) {
                    maxabs = absval;
                    idx = k;
                }
            }
            maxabs = rshiftRound(maxabs, qIn - qOut);

            if (maxabs > INT16_MAX) {
                maxabs = Math.min(maxabs, 163838);
                int chirpQ16 = fixConst(0.999, 16) - div32(((maxabs - INT16_MAX) << 14),
                        (maxabs * (idx + 1)) >> 2);
                bwExpander32(aQIn, d, chirpQ16);
            } else {
                break;
            }
        }

        if (i == 10) {
            for (int k = 0; k < d; k++) {
                aQOut[k] = (short) sat16(rshiftRound(aQIn[k], qIn - qOut));
                aQIn[k] = aQOut[k] << (qIn - qOut);
            }
        } else {
            for (int k = 0; k < d; k++) {
                aQOut[k] = (short) rshiftRound(aQIn[k], qIn - qOut);
            }
        }
    }

    /**
     * Bandwidth-expands a 32-bit AR filter in place, the port of {@code silk_bwexpander_32}.
     *
     * <p>Each coefficient is multiplied by a chirp factor in Q16 that geometrically decays from
     * {@code chirpQ16} toward zero, pulling the filter poles inward. The decay update uses the SILK
     * {@code silk_SMULWW} and {@link #rshiftRound(int, int)} so the integer trajectory matches the source.
     *
     * @param ar       the AR filter without the leading {@code 1}, modified in place, {@code d} entries
     * @param d        the filter length
     * @param chirpQ16 the initial chirp factor in Q16
     */
    private static void bwExpander32(int[] ar, int d, int chirpQ16) {
        int chirpMinusOneQ16 = chirpQ16 - 65536;
        for (int i = 0; i < d - 1; i++) {
            ar[i] = smulww(chirpQ16, ar[i]);
            chirpQ16 += rshiftRound(chirpQ16 * chirpMinusOneQ16, 16);
        }
        ar[d - 1] = smulww(chirpQ16, ar[d - 1]);
    }

    /**
     * Computes the inverse prediction gain of a Q12 filter and tests its stability, the port of
     * {@code silk_LPC_inverse_pred_gain_c} together with its {@code LPC_inverse_pred_gain_QA_c} helper.
     *
     * <p>The Q12 coefficients are lifted to Q{@code QA_GAIN} (Q24) and, if the DC response is unstable, the
     * filter is rejected immediately. Otherwise the reflection-coefficient recursion runs from the highest
     * order down: each step rejects the filter if a reflection coefficient leaves the unit circle or the
     * running inverse gain falls below the {@link #MAX_PREDICTION_POWER_GAIN} floor, and otherwise updates the
     * coefficients via the SILK {@code silk_INVERSE32_varQ} reciprocal. A non-zero return is the inverse gain
     * in Q30; zero means the filter is (too close to) unstable.
     *
     * @param aQ12  the prediction coefficients in Q12, {@code order} entries
     * @param order the prediction order
     * @return the inverse prediction gain in Q30, or {@code 0} if the filter is unstable
     */
    private static int lpcInversePredGain(short[] aQ12, int order) {
        final int qaGain = 24;
        int[] atmpQA = new int[MAX_ORDER_LPC];
        int dcResp = 0;
        for (int k = 0; k < order; k++) {
            dcResp += aQ12[k];
            atmpQA[k] = aQ12[k] << (qaGain - 12);
        }
        if (dcResp >= 4096) {
            return 0;
        }
        return inversePredGainQA(atmpQA, order, qaGain);
    }

    /**
     * Runs the reflection-coefficient stability recursion in Q{@code qaGain}, the port of
     * {@code LPC_inverse_pred_gain_QA_c}.
     *
     * <p>The recursion peels off one reflection coefficient per outer step. Each step checks the coefficient
     * against the SILK {@code A_LIMIT} (the Q{@code qaGain} value of {@code 0.99975}), forms the squared
     * reflection coefficient in Q30, multiplies it into the running inverse gain, rejects the filter when the
     * inverse gain drops below the power-gain floor, and otherwise rescales the remaining coefficients with
     * the {@link #inverse32VarQ(int, int)} reciprocal under saturating subtraction. Every overflow guard of
     * the source is reproduced.
     *
     * @param aQA    the prediction coefficients in Q{@code qaGain}, modified in place by the recursion
     * @param order  the prediction order
     * @param qaGain the Q-domain of {@code aQA}
     * @return the inverse prediction gain in Q30, or {@code 0} if the filter is unstable
     */
    private static int inversePredGainQA(int[] aQA, int order, int qaGain) {
        int aLimit = fixConst(0.99975, qaGain);
        int invGainQ30 = fixConst(1, 30);
        int k;
        for (k = order - 1; k > 0; k--) {
            if (aQA[k] > aLimit || aQA[k] < -aLimit) {
                return 0;
            }

            int rcQ31 = -(aQA[k] << (31 - qaGain));

            int rcMult1Q30 = fixConst(1, 30) - smmul(rcQ31, rcQ31);

            invGainQ30 = smmul(invGainQ30, rcMult1Q30) << 2;
            if (invGainQ30 < (int) (1.0f / MAX_PREDICTION_POWER_GAIN * (1L << 30) + 0.5)) {
                return 0;
            }

            int mult2Q = 32 - clz32(Math.abs(rcMult1Q30));
            int rcMult2 = inverse32VarQ(rcMult1Q30, mult2Q + 30);

            for (int n = 0; n < (k + 1) >> 1; n++) {
                int tmp1 = aQA[n];
                int tmp2 = aQA[k - n - 1];
                long tmp64 = rshiftRound64(smull(subSat32(tmp1, mul32FracQ(tmp2, rcQ31, 31)), rcMult2), mult2Q);
                if (tmp64 > Integer.MAX_VALUE || tmp64 < Integer.MIN_VALUE) {
                    return 0;
                }
                aQA[n] = (int) tmp64;
                tmp64 = rshiftRound64(smull(subSat32(tmp2, mul32FracQ(tmp1, rcQ31, 31)), rcMult2), mult2Q);
                if (tmp64 > Integer.MAX_VALUE || tmp64 < Integer.MIN_VALUE) {
                    return 0;
                }
                aQA[k - n - 1] = (int) tmp64;
            }
        }

        if (aQA[k] > aLimit || aQA[k] < -aLimit) {
            return 0;
        }

        int rcQ31 = -(aQA[0] << (31 - qaGain));
        int rcMult1Q30 = fixConst(1, 30) - smmul(rcQ31, rcQ31);

        invGainQ30 = smmul(invGainQ30, rcMult1Q30) << 2;
        if (invGainQ30 < (int) (1.0f / MAX_PREDICTION_POWER_GAIN * (1L << 30) + 0.5)) {
            return 0;
        }

        return invGainQ30;
    }

    /**
     * Computes {@code (a32 * b32) >> Q} rounded as a 32-bit result, the {@code MUL32_FRAC_Q} macro of
     * {@code LPC_inv_pred_gain.c}.
     *
     * <p>The product is formed in 64 bits and shifted by {@code q} with SILK rounding before being narrowed
     * to {@code int}.
     *
     * @param a32 the first factor
     * @param b32 the second factor
     * @param q   the right-shift amount
     * @return the rounded 32-bit fractional product
     */
    private static int mul32FracQ(int a32, int b32, int q) {
        return (int) rshiftRound64(smull(a32, b32), q);
    }

    /**
     * Returns {@code a >> shift} with SILK round-to-nearest on a 32-bit operand, the
     * {@code silk_RSHIFT_ROUND} macro.
     *
     * <p>For {@code shift == 1} this is {@code (a >> 1) + (a & 1)}; otherwise it is
     * {@code (((a >> (shift - 1)) + 1) >> 1)}. The arithmetic right shift matches C signed shifting.
     *
     * @param a     the value to shift
     * @param shift the right-shift amount; at least one
     * @return the rounded shifted value
     */
    private static int rshiftRound(int a, int shift) {
        return shift == 1 ? (a >> 1) + (a & 1) : ((a >> (shift - 1)) + 1) >> 1;
    }

    /**
     * Returns {@code a >> shift} with SILK round-to-nearest on a 64-bit operand, the
     * {@code silk_RSHIFT_ROUND64} macro.
     *
     * <p>For {@code shift == 1} this is {@code (a >> 1) + (a & 1)}; otherwise it is
     * {@code (((a >> (shift - 1)) + 1) >> 1)}, computed on {@code long}.
     *
     * @param a     the value to shift
     * @param shift the right-shift amount; at least one
     * @return the rounded shifted value
     */
    private static long rshiftRound64(long a, int shift) {
        return shift == 1 ? (a >> 1) + (a & 1) : ((a >> (shift - 1)) + 1) >> 1;
    }

    /**
     * Returns the 64-bit product of two 32-bit values, the SILK {@code silk_SMULL} macro.
     *
     * <p>Both factors are widened to {@code long} before multiplying so the product does not overflow.
     *
     * @param a32 the first factor
     * @param b32 the second factor
     * @return the 64-bit product
     */
    private static long smull(int a32, int b32) {
        return (long) a32 * b32;
    }

    /**
     * Returns {@code (a32 * b32) >> 16} of the 64-bit product, the SILK {@code silk_SMULWW} macro under the
     * {@code OPUS_FAST_INT64} definition.
     *
     * @param a32 the first factor
     * @param b32 the second factor
     * @return the 64-bit product right-shifted by 16, narrowed to {@code int}
     */
    private static int smulww(int a32, int b32) {
        return (int) (((long) a32 * b32) >> 16);
    }

    /**
     * Returns the high 32 bits of the 64-bit product, the SILK {@code silk_SMMUL} macro.
     *
     * <p>This is {@code (a32 * b32) >> 32} computed on the widened product, matching
     * {@code silk_RSHIFT64(silk_SMULL(a, b), 32)}.
     *
     * @param a32 the first factor
     * @param b32 the second factor
     * @return the upper 32 bits of the product
     */
    private static int smmul(int a32, int b32) {
        return (int) (((long) a32 * b32) >> 32);
    }

    /**
     * Subtracts with 32-bit saturation, the SILK {@code silk_SUB_SAT32} macro.
     *
     * <p>Computes {@code a - b}, clamping to {@link Integer#MAX_VALUE} or {@link Integer#MIN_VALUE} on signed
     * overflow exactly as the SILK bit-twiddling macro does.
     *
     * @param a the minuend
     * @param b the subtrahend
     * @return the saturated difference
     */
    private static int subSat32(int a, int b) {
        long r = (long) a - b;
        if (r > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (r < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) r;
    }

    /**
     * Saturates an {@code int} to the {@code int16} range, the SILK {@code silk_SAT16} macro.
     *
     * @param a the value to saturate
     * @return {@code a} clamped to {@code [INT16_MIN, INT16_MAX]}
     */
    private static int sat16(int a) {
        return a > INT16_MAX ? INT16_MAX : Math.max(a, INT16_MIN);
    }

    /**
     * Performs integer division narrowed to {@code int}, the SILK {@code silk_DIV32} macro.
     *
     * <p>Java integer division truncates toward zero, matching C {@code int32} division.
     *
     * @param a32 the dividend
     * @param b32 the divisor
     * @return the truncated quotient
     */
    private static int div32(int a32, int b32) {
        return a32 / b32;
    }

    /**
     * Computes a fixed-point constant, the SILK {@code SILK_FIX_CONST} macro.
     *
     * <p>Returns {@code (int)(c * (1 << q) + 0.5)}, the rounded Q{@code q} representation of {@code c}, with
     * the product formed against a 64-bit one to match the macro's {@code (opus_int64)1 << q}.
     *
     * @param c the real constant
     * @param q the target Q-domain
     * @return the rounded fixed-point value
     */
    private static int fixConst(double c, int q) {
        return (int) (c * (double) (1L << q) + 0.5);
    }

    /**
     * Counts the leading zero bits of a 32-bit value, the SILK {@code silk_CLZ32} macro.
     *
     * <p>{@link Integer#numberOfLeadingZeros(int)} returns {@code 32} for a zero input, matching the SILK
     * macro's zero handling.
     *
     * @param in32 the value to inspect
     * @return the number of leading zero bits, {@code 32} for zero
     */
    private static int clz32(int in32) {
        return Integer.numberOfLeadingZeros(in32);
    }

    /**
     * Inverts a 32-bit value into a Q{@code qres} reciprocal, the port of {@code silk_INVERSE32_varQ}.
     *
     * <p>Normalizes {@code b32}, takes a 14-bit-accurate reciprocal seed via integer division, and refines it
     * once with a residual that the SILK source deliberately lets overflow; the intermediate Q-domains and
     * variable names are kept so the integer trajectory matches the source exactly. The final shift converts
     * to the requested Q-domain with left-saturation, plain right shift, or a zero floor for an out-of-range
     * shift.
     *
     * @param b32  the denominator in Q0; must be non-zero
     * @param qres the Q-domain of the result; must be positive
     * @return a good approximation of {@code (1 << qres) / b32}
     */
    private static int inverse32VarQ(int b32, int qres) {
        int bHeadrm = clz32(Math.abs(b32)) - 1;
        int b32Nrm = b32 << bHeadrm;

        int b32Inv = (Integer.MAX_VALUE >> 2) / (b32Nrm >> 16);

        int result = b32Inv << 16;

        int errQ32 = (((1 << 29) - smulwb(b32Nrm, b32Inv)) << 3);

        result = smlaww(result, errQ32, b32Inv);

        int lshift = 61 - bHeadrm - qres;
        if (lshift <= 0) {
            return lshiftSat32(result, -lshift);
        }
        if (lshift < 32) {
            return result >> lshift;
        }
        return 0;
    }

    /**
     * Returns {@code (a32 * (int16)b32) >> 16}, the SILK {@code silk_SMULWB} macro under
     * {@code OPUS_FAST_INT64}.
     *
     * <p>The low 16 bits of {@code b32} are sign-extended to {@code int16} before the 64-bit-widened
     * multiply.
     *
     * @param a32 the 32-bit factor
     * @param b32 the factor whose low 16 bits are used as a signed {@code int16}
     * @return the product right-shifted by 16
     */
    private static int smulwb(int a32, int b32) {
        return (int) (((long) a32 * (short) b32) >> 16);
    }

    /**
     * Returns {@code a32 + ((b32 * c32) >> 16)} of the 64-bit product, the SILK {@code silk_SMLAWW} macro
     * under {@code OPUS_FAST_INT64}.
     *
     * @param a32 the accumulator
     * @param b32 the first factor
     * @param c32 the second factor
     * @return the accumulated value
     */
    private static int smlaww(int a32, int b32, int c32) {
        return a32 + (int) (((long) b32 * c32) >> 16);
    }

    /**
     * Left-shifts with the SILK {@code silk_LSHIFT_SAT32} clamping, used by {@link #inverse32VarQ(int, int)}.
     *
     * <p>The value is first limited to the range that can be shifted left by {@code shift} without signed
     * overflow, then shifted, matching {@code silk_LSHIFT32(silk_LIMIT(a, int32_min >> shift, int32_max >>
     * shift), shift)}.
     *
     * @param a     the value to shift
     * @param shift the left-shift amount; non-negative
     * @return the clamped left-shifted value
     */
    private static int lshiftSat32(int a, int shift) {
        int lo = Integer.MIN_VALUE >> shift;
        int hi = Integer.MAX_VALUE >> shift;
        int limited = a > hi ? hi : Math.max(a, lo);
        return limited << shift;
    }
}
