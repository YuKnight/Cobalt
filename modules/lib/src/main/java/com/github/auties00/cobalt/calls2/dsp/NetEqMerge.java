package com.github.auties00.cobalt.calls2.dsp;

/**
 * The post-concealment cross-fade that joins a freshly decoded packet onto the tail of an expansion, a
 * faithful single-channel port of WhatsApp's WebRTC {@code Merge}.
 *
 * <p>When a packet finally arrives after one or more {@link NetEqOperation#EXPAND} concealment frames, the
 * synthesized concealment and the real decoded audio are at different phases and energies, so splicing them
 * directly would click. The merge operation lines the two signals up by their best correlation lag, scales
 * the expansion to the decoded energy, and cross-fades from the expansion into the decoded audio over a pitch
 * period so the transition is inaudible. The result is a single contiguous frame: the head is the
 * energy-matched, phase-aligned expansion tail, the body is the cross-fade, and the tail is the untouched
 * decoded audio.
 *
 * <p>This port is specialized to the single sixteen kilohertz mono channel the call audio format carries, so
 * the native {@code AudioMultiVector} per-channel loop collapses to one pass over flat {@code short[]} arrays
 * and the per-channel expand weight is one scalar. The expansion samples are the recent
 * {@link NetEqSyncBuffer} history the prior expansions wrote; the decoded samples are the codec output of the
 * arriving packet.
 *
 * @implNote This implementation ports {@code webrtc::Merge::Process} ({@code $f7470}) of the wa-voip WASM
 * module {@code ff-tScznZ8P}, reusing the bit-exact leaf kernels of {@link NetEqSignalProcessing}: the energy
 * dot products and the {@code SqrtFloor} energy ratio for the Q14 scale, the four kilohertz decimation and
 * the sixty-lag cross-correlation with parabolic peak refinement for the lag search, and the two Q-domain
 * cross-fade kernels {@code $f9924} (single-source weight ramp) and {@code $f9923} (two-source weighted
 * blend) for the splice. The narrowband int16 cross-fade variants {@code $f7327}/{@code $f7325} are not
 * ported because the call audio path is the Q-domain (wideband) path. The {@code 1260}-sample expanded cap,
 * the {@code min(fs_mult*64, input_length)} energy length, the {@code interpolation_length} clamp, and the
 * {@code increment = min(4194/fs_mult, (2^20 - weight*64)/old_length)} ramp slope are reproduced exactly from
 * the native body (see {@code .temp/wsola-work/merge_f7470.wat}).
 */
final class NetEqMerge {
    /**
     * The maximum number of expanded samples the merge consumes, the native expanded cap.
     *
     * <p>An expansion longer than this is trimmed before the merge so the splice region and output stay
     * bounded; the value is the native {@code kMaxExpandedLength}-style cap.
     */
    static final int MAX_EXPANDED_LENGTH = 1260;

    /**
     * Prevents instantiation of this stateless operation holder.
     */
    private NetEqMerge() {
    }

    /**
     * Applies the single-source weight ramp to one signal region, the first merge cross-fade segment.
     *
     * <p>Walks {@code length} samples writing {@code out[i] = (in[i] * weight + 8192) >> 14} where
     * {@code weight} ramps each sample by {@code increment} in the Q20 domain, clamped into {@code [0, 16384)}.
     * The weight is read from and written back to {@code weightState[0]} so a following segment continues the
     * ramp. This fades a single signal up or down in level without a second source.
     *
     * @implNote This implementation transcribes {@code $f9924}: the per-sample store uses the current Q14
     * weight {@code (in[i] * (weight & 0xFFFF) + 8192) >> 14} (the {@code +8192} encoded as {@code -(-8192)},
     * a logical {@code >> 14}); then the running Q20 weight is advanced
     * {@code q20 = max(increment + q20, 0)} and the next Q14 weight is {@code min(q20 >>> 6, 16384)} under
     * unsigned comparison, so the multiply uses the pre-advance weight on each sample. The running Q20 weight
     * is seeded {@code (weight << 6) | 32}, the {@code | 32} being the Q20 rounding bit.
     *
     * @param out         the output region
     * @param outPos      the offset into {@code out}
     * @param in          the single source region
     * @param inPos       the offset into {@code in}
     * @param length      the number of samples
     * @param increment   the per-sample Q14 weight increment
     * @param weightState a one-element array holding the Q14 weight, read and updated in place
     */
    static void rampSingle(short[] out, int outPos, short[] in, int inPos, int length, int increment,
                           int[] weightState) {
        int weight = weightState[0] & 0xFFFF;
        int q20 = (weight << 6) | 32;
        for (int i = 0; i < length; i++) {
            int blended = (in[inPos + i] * (weight & 0xFFFF) - (-8192)) >>> 14;
            out[outPos + i] = (short) blended;
            q20 = Math.max(increment + q20, 0);
            int next = q20 >>> 6;
            weight = Integer.compareUnsigned(next, 16384) >= 0 ? 16384 : next;
        }
        weightState[0] = weight;
    }

    /**
     * Cross-fades two signal regions with a descending weight, the second merge cross-fade segment.
     *
     * <p>Walks {@code length} samples writing
     * {@code out[i] = (expanded[i] * weight + decoded[i] * (16384 - weight) + 8192) >> 14}, where
     * {@code weight} starts at {@code weightState[0]} and decrements by {@code increment} each sample while the
     * complement weight ascends by the same, so the output fades from the expansion into the decoded audio.
     * The final weight is written back to {@code weightState[0]}.
     *
     * @implNote This implementation transcribes {@code $f9923}: the per-sample store is
     * {@code (expanded[i] * sext16(weight) + decoded[i] * sext16(16384 - weight) + 8192) >> 14} (logical
     * {@code >> 14}); the weight update is {@code complement += increment; weight -= increment} with no clamp,
     * matching the native unclamped two-source ramp.
     *
     * @param out         the output region
     * @param outPos      the offset into {@code out}
     * @param expanded    the fading-out expansion region
     * @param expandedPos the offset into {@code expanded}
     * @param decoded     the fading-in decoded region
     * @param decodedPos  the offset into {@code decoded}
     * @param length      the number of samples
     * @param increment   the per-sample Q14 weight decrement
     * @param weightState a one-element array holding the starting Q14 weight, updated in place
     */
    static void rampBlend(short[] out, int outPos, short[] expanded, int expandedPos, short[] decoded,
                          int decodedPos, int length, int increment, int[] weightState) {
        int weight = weightState[0] & 0xFFFF;
        int complement = 16384 - weight;
        for (int i = 0; i < length; i++) {
            int blended = (expanded[expandedPos + i] * (short) weight
                    + decoded[decodedPos + i] * (short) complement - (-8192)) >>> 14;
            out[outPos + i] = (short) blended;
            complement += increment;
            weight -= increment;
        }
        weightState[0] = weight;
    }

    /**
     * Computes the Q14 weight the merge scales the expansion by, the energy match.
     *
     * <p>Compares the energy of the expansion against the energy of the decoded audio over the leading
     * {@code min(fs_mult * 64, decodedLength)} samples; when the expansion is no louder than the decoded audio
     * the weight is unity ({@code 16384}), otherwise it is {@code SqrtFloor((decodedEnergy / expandedEnergy)
     * << 14)} clamped to unity and to the supplied per-channel expand weight floor.
     *
     * @implNote This implementation transcribes the {@code SignalScaling} block of {@code $f7470}
     * instruction-for-instruction. The energy length is {@code min(fs_mult << 6, decodedLength)}; the
     * overflow divisor is {@code INT32_MAX / energyLength}; each energy is a dot product scaled down by the
     * {@code WebRtcSpl_NormW32} of {@code (maxAbs^2) / overflowDivisor}. The two energies are aligned to a
     * common shift through unsigned-compared selects ({@code expandedEnergy >> sel} and
     * {@code decodedEnergy >> sel}); a decoded energy no greater than the expanded energy keeps the weight at
     * {@code 16384}. Otherwise {@code norm = NormW32(decodedAligned)}, the numerator is built from
     * {@code expandedAligned} ({@code norm > 2 ? expandedAligned >> (3 - norm) : expandedAligned << (norm - 3)}),
     * the denominator from {@code decodedAligned} ({@code norm > 16 ? decodedAligned >> (17 - norm) :
     * decodedAligned << (norm - 17)}), and the Q14 scale is {@code SqrtFloor((numerator / denominator) << 14)}.
     * The result is clamped to {@code 16384} then floored at {@code expandWeight}.
     *
     * @param expanded      the expansion samples
     * @param decoded       the decoded samples
     * @param decodedLength the number of decoded samples available
     * @param fsMult        the sample-rate multiplier
     * @param expandWeight  the per-channel expand weight floor, Q14
     * @return the Q14 scale weight, in {@code [expandWeight, 16384]}
     */
    static int signalScaling(short[] expanded, short[] decoded, int decodedLength, int fsMult,
                             int expandWeight) {
        int energyLength = Math.min(fsMult << 6, decodedLength);
        int expandedMaxAbs = NetEqSignalProcessing.maxAbs16(expanded, 0, energyLength);
        int decodedMaxAbs = NetEqSignalProcessing.maxAbs16(decoded, 0, energyLength);
        int overflowDivisor = Integer.MAX_VALUE / energyLength;

        int expandedShift = energyShift((expandedMaxAbs * expandedMaxAbs) / overflowDivisor);
        int expandedEnergy = dotProductWithScale(expanded, expanded, energyLength, expandedShift);
        int decodedShift = energyShift((decodedMaxAbs * decodedMaxAbs) / overflowDivisor);
        int decodedEnergy = dotProductWithScale(decoded, decoded, energyLength, decodedShift);

        int scale = 16384;
        int diffDec = expandedShift - decodedShift;
        int selDec = Integer.compareUnsigned(diffDec, expandedShift) <= 0 ? diffDec : 0;
        int decodedAligned = decodedEnergy >> selDec;
        int diffExp = decodedShift - expandedShift;
        int selExp = Integer.compareUnsigned(diffExp, decodedShift) <= 0 ? diffExp : 0;
        int expandedAligned = expandedEnergy >> selExp;

        if (decodedAligned > expandedAligned) {
            int norm = decodedAligned == 0
                    ? 0 : Integer.numberOfLeadingZeros(decodedAligned ^ (decodedAligned >> 31)) - 1;
            int numerator = norm > 2 ? expandedAligned >> (3 - norm) : expandedAligned << (norm - 3);
            int denominator = norm > 16 ? decodedAligned >> (17 - norm) : decodedAligned << (norm - 17);
            scale = NetEqSignalProcessing.sqrtFloor((numerator / denominator) << 14);
        }

        int clamped = Math.min(scale, 16384);
        return Math.max(clamped, expandWeight);
    }

    /**
     * Returns the per-energy right shift the merge dot product scales by, the energy-overflow guard.
     *
     * <p>Computes {@code value == 0 ? 0 : 32 - clz(value ^ (value >> 31))}, the shift that keeps the energy
     * accumulation inside thirty-two bits given the per-sample magnitude headroom.
     *
     * @implNote This implementation transcribes the energy-shift derivation inlined into the
     * {@code DotProductWithScale} calls of {@code $f7470}: the constant {@code 32} (also the dot product
     * scaling argument) is the minuend, so the shift is {@code 32 - clz(value ^ (value >> 31))}, selected to
     * {@code 0} when {@code value} is zero.
     *
     * @param value the squared-max-abs over the overflow divisor
     * @return the per-product right shift
     */
    private static int energyShift(int value) {
        if (value == 0) {
            return 0;
        }
        return 32 - Integer.numberOfLeadingZeros(value ^ (value >> 31));
    }

    /**
     * Computes a self-correlation energy with a per-element right shift, the merge energy dot product.
     *
     * <p>Accumulates {@code sum(in[i] * in[i] >> shift)} over {@code length} samples, the energy the
     * {@link #signalScaling(short[], short[], int, int, int)} ratio compares.
     *
     * @implNote This implementation reproduces the {@code DotProductWithScale} (native table {@code 5188})
     * call shape with the two vectors identical: the per-product arithmetic right shift matches
     * {@link NetEqSignalProcessing#crossCorrelation(int[], short[], short[], int, int, int, int)}.
     *
     * @param a      the first vector
     * @param b      the second vector
     * @param length the number of samples
     * @param shift  the per-product right shift
     * @return the scaled energy
     */
    private static int dotProductWithScale(short[] a, short[] b, int length, int shift) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (a[i] * b[i]) >> shift;
        }
        return sum;
    }

}
