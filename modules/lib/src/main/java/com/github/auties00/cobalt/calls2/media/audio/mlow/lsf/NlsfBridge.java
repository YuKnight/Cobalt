package com.github.auties00.cobalt.calls2.media.audio.mlow.lsf;

import com.github.auties00.cobalt.calls2.media.audio.mlow.silk.SilkNlsf2a;

/**
 * Float adapter from MLow's normalized line spectral frequencies (NLSFs) to float linear-prediction filter
 * coefficients, the port of {@code smpl_NLSF2A} in {@code smpl/smpl_lsf_wrapper.c}.
 *
 * <p>The MLow decoder carries its line spectral frequencies as {@code float} radians in {@code (0, SMPL_PI)},
 * but the conversion to linear-prediction coefficients runs in SILK's integer fixed-point core (the rest of
 * MLow is built with fast-math float arithmetic that would perturb the bit-exact integer pipeline). This
 * class is the bridge: it quantizes the float NLSFs to Q15 {@code int16}, runs the integer core
 * {@link SilkNlsf2a#nlsf2a32(int[], short[], int)}, and converts the resulting {@code QA+1} (Q17) integer
 * coefficients back to {@code float}.
 *
 * <p>The float result {@code a} is the monic prediction filter with {@code a[0] == 1} followed by the
 * {@code d} negated, scaled integer coefficients: {@code a[i + 1] = -a32_QA1[i] / 2^17}. This is exactly the
 * form {@code smpl_NLSF2A} produces, before the separate float bandwidth-expansion stabilization that the
 * MLow decoder applies downstream ({@code smpl_lpc_stabilize}), which is not part of this bridge.
 *
 * <p>The integer intermediates are bit-exact against the native decoder. The float coefficients match the
 * native single-precision result to within IEEE-754 rounding of the one division per coefficient; the
 * scaling constant {@code 1 / 2^17} is an exact power of two, so the only rounding is the integer-to-float
 * widening and the multiply, which both round identically to the C cast and multiply.
 *
 * <p>Scope is the SMPL 16 kHz / 60 ms / mono low-band path with order {@value #LPC_ORDER}. The native
 * wrapper accepts any order up to {@code SMPL_LPC_ORDER} and short-circuits a larger one; this bridge
 * exposes the order-16 conversion the low-band decode drives, plus a general-order entry point for the
 * order-4 high-band case kept for symmetry. This type is stateless and thread-safe.
 *
 * @implNote This implementation ports {@code smpl_NLSF2A} verbatim. The float-to-{@code int16} quantization
 * reproduces the {@code SCALE1} macro's operation order: {@code SCALE1} is {@code 32768.0f/SMPL_PI}, so the
 * native {@code roundf(nlsf[i] * SCALE1)} expands to {@code roundf(nlsf[i] * 32768.0f / SMPL_PI)} and evaluates
 * as {@code (nlsf[i] * 32768.0f) / SMPL_PI}, a {@code float} multiply then a {@code float} divide. This method
 * therefore divides by {@link #SMPL_PI} after multiplying by {@link #SCALE1_NUM} rather than multiplying by a
 * precomputed reciprocal, because the two orders disagree at the rare exact half-way boundary (multiply-then-
 * divide rounds up, the reciprocal rounds down), and that single {@code int16} flip propagates through the
 * {@code silk_NLSF2A_32} polynomial. {@link Math#round(float)} rounds half up, which matches {@code roundf}
 * round-half-away-from-zero for the non-negative in-band values; the pi constant is the {@code float}-rounded
 * {@link #SMPL_PI} the reference uses, so the boundary lands identically. The back-conversion multiplies by
 * {@link #SCALE2} which equals {@code 1 / (1 << (QA + 1))} with {@code QA == 16}, the {@code SCALE2_ALT} of the
 * wrapper.
 */
public final class NlsfBridge {
    /**
     * Linear-prediction order of the MLow short-term filter, {@code SMPL_LPC_ORDER}; the NLSF vector length.
     */
    private static final int LPC_ORDER = 16;

    /**
     * Upper band edge for the NLSF range, the {@code float}-rounded {@code SMPL_PI} constant.
     *
     * <p>This is the exact single-precision value the reference uses to scale radians to the Q15 cosine-table
     * domain; the constant is load-bearing because it sets the {@code int16} quantization boundary.
     */
    private static final float SMPL_PI = 3.1415926535897f;

    /**
     * Numerator of the radians-to-Q15 scale, the {@code 32768.0f} of the wrapper's {@code SCALE1} macro.
     *
     * <p>The native {@code SCALE1} is the object-like macro {@code 32768.0f/SMPL_PI}, so {@code nlsf[i] * SCALE1}
     * expands to {@code nlsf[i] * 32768.0f / SMPL_PI} and is evaluated left to right as
     * {@code (nlsf[i] * 32768.0f) / SMPL_PI}: a {@code float} multiply followed by a {@code float} divide, not a
     * multiply by a precomputed reciprocal. The two orders round differently at the {@code int16} boundary (the
     * exact half-way case rounds up under multiply-then-divide and down under the reciprocal), which flips an
     * {@code int16} root and then the whole {@code silk_NLSF2A_32} polynomial; this numerator is kept separate
     * from {@link #SMPL_PI} so {@link #nlsf2a(float[], int)} reproduces the macro's operation order exactly.
     */
    private static final float SCALE1_NUM = 32768.0f;

    /**
     * Scale mapping a {@code QA+1} (Q17) integer coefficient back to {@code float}, the wrapper's
     * {@code SCALE2_ALT} of {@code 1 / (1 << (QA + 1))} with {@code QA == 16}.
     *
     * <p>This is an exact power of two ({@code 1 / 131072}), so the back-conversion introduces only the
     * single rounding of the integer-to-float multiply.
     */
    private static final float SCALE2 = 1.0f / (1 << (16 + 1));

    /**
     * Prevents instantiation of this static utility holder.
     *
     * <p>The conversion is a pure function with no state.
     */
    private NlsfBridge() {
    }

    /**
     * Converts an order-{@value #LPC_ORDER} float NLSF vector to float linear-prediction coefficients, the
     * order-16 specialization of {@code smpl_NLSF2A}.
     *
     * <p>Equivalent to {@link #nlsf2a(float[], int)} with {@code d} fixed at {@value #LPC_ORDER}; provided as
     * the common case the low-band decode path uses.
     *
     * @param nlsf the normalized line spectral frequencies in radians, {@value #LPC_ORDER} entries in
     *             {@code (0, SMPL_PI)}
     * @return a freshly allocated monic filter of {@value #LPC_ORDER}{@code  + 1} entries, {@code a[0] == 1}
     */
    public static float[] nlsf2a(float[] nlsf) {
        return nlsf2a(nlsf, LPC_ORDER);
    }

    /**
     * Converts a float NLSF vector to float linear-prediction coefficients, the port of {@code smpl_NLSF2A}.
     *
     * <p>Quantizes each float NLSF to a Q15 {@code int16} by {@code round(nlsf[i] * 32768.0f / SMPL_PI)} (the
     * multiply-then-divide order of the native {@code SCALE1} macro), runs the integer core
     * {@link SilkNlsf2a#nlsf2a32(int[], short[], int)} to obtain the {@code QA+1} coefficients, and writes the
     * monic float filter: {@code a[0] = 1} and {@code a[i + 1] = -a32_QA1[i] * SCALE2}. The native wrapper
     * returns without writing for an order above {@code SMPL_LPC_ORDER}; this method mirrors that by returning a
     * filter of all zeros (its {@code a[0]} left at zero) for such an order.
     *
     * @param nlsf the normalized line spectral frequencies in radians, at least {@code d} entries in
     *             {@code (0, SMPL_PI)}
     * @param d    the filter order; must be {@code 4}, {@code 10}, or {@code 16} for a converted result, and
     *             at most {@value #LPC_ORDER}
     * @return a freshly allocated monic filter of {@code d + 1} entries with {@code a[0] == 1}, or all zeros
     *         when {@code d} exceeds {@value #LPC_ORDER}
     */
    public static float[] nlsf2a(float[] nlsf, int d) {
        float[] a = new float[d + 1];
        if (d > LPC_ORDER) {
            return a;
        }
        short[] nlsf16 = new short[d];
        for (int i = 0; i < d; i++) {
            nlsf16[i] = (short) Math.round(nlsf[i] * SCALE1_NUM / SMPL_PI);
        }
        int[] a32QA1 = new int[LPC_ORDER + 1];
        SilkNlsf2a.nlsf2a32(a32QA1, nlsf16, d);

        a[0] = 1.0f;
        for (int i = 0; i < d; i++) {
            a[i + 1] = -a32QA1[i] * SCALE2;
        }
        return a;
    }
}
