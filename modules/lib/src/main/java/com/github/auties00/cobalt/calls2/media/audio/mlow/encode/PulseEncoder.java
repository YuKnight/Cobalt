package com.github.auties00.cobalt.calls2.media.audio.mlow.encode;

import com.github.auties00.cobalt.calls2.media.audio.mlow.entropy.MlowEntropyWrapper;
import com.github.auties00.cobalt.calls2.media.audio.mlow.entropy.MlowRangeEncoder;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.PulseTables;

/**
 * Algebraic fixed-codebook (FCB) excitation pulse serializer for the MLow speech codec, the port of
 * {@code smpl_encode_pulses} in {@code smpl_pulse_coding.c} and the encode side of
 * {@code smpl_decode_pulse_pos_signs} in {@code smpl_pulse_coding_util.c}, the exact inverse of
 * {@link com.github.auties00.cobalt.calls2.media.audio.mlow.celp.PulseDecoder}.
 *
 * <p>MLow codes each frame's excitation as a sparse set of unit pulses on an integer sample grid. From the
 * stacked signed pulse layout the core encoder produced (one signed magnitude per absolute sample position),
 * this serializer emits, in the fixed order the decoder reads:
 * <ul>
 * <li>the total pulse count for the frame, against the closed-form cumulative model {@code smpl_num_pulses_cmf}
 * in the regular (non-low-rate) mode or against a static low-rate CMF selected by voicing class;</li>
 * <li>the split of that total across subframes: a two-level split for a four-subframe frame, a single split
 * for a two-subframe frame, and the whole count for a one-subframe frame, driven by the binomial split CMF
 * family;</li>
 * <li>each pulse's position within its subframe, coded as run lengths against the run-length CMF whose shape
 * depends on the remaining samples and pulses, with coincident pulses stacking into a single position whose
 * run length is zero;</li>
 * <li>each pulse's sign, packed into uniform symbols of up to {@link #MAX_SIGNS_PER_SYMBOL} bits.</li>
 * </ul>
 *
 * <p>The serializer is stateless: every call operates purely on the supplied range encoder, the shared
 * {@link PulseTables.Tables}, the per-frame mode flags, and the input pulse layout, and writes the
 * per-subframe pulse counts it re-derived back into the caller-supplied {@code sfPulses} array, exactly as
 * the native {@code smpl_encode_pulses} fills its {@code sf_pulses} out-parameter.
 *
 * <p>Scope is the 16 kHz, 60 ms, mono SMPL-mode low-band path. The serialization is nevertheless written
 * generically over frame length, subframe count, and mode flags, exactly as the native function is.
 *
 * @implNote This implementation mirrors {@code smpl_encode_pulses} statement for statement. It re-derives the
 * pulse positions and the per-subframe pulse counts from the stacked {@code pulses} array (the native loop
 * that scans {@code pulses[sf * subfrlen + i]} for nonzero entries) rather than trusting any precomputed
 * counts, so the position list is in the same order the decoder reconstructs. The closed-form pulse-count CMF
 * {@code smpl_num_pulses_cmf} and the {@code smpl_pdf_to_CMF}-derived split and run-length windows are shared
 * with {@link PulseTables} and {@link com.github.auties00.cobalt.calls2.media.audio.mlow.celp.PulseDecoder};
 * each {@code ec_encode} span is taken with the same {@code -cmf[base]} relative offset the native code uses,
 * so the emitted bits round-trip the decoder bit-for-bit. The sign packing reproduces the native
 * most-significant-first bit order: the first decoded sign occupies the high bit of the uniform symbol.
 */
public final class PulseEncoder {
    /**
     * Maximum number of pulse signs packed into a single uniform range-coder symbol, the native
     * {@code MAX_SIGNS_PER_SYMBOL}.
     *
     * <p>Signs are emitted as uniform symbols over {@code 1 << signsInSymbol} values; each full symbol carries
     * this many one-bit signs, with a final short symbol holding any remainder.
     */
    static final int MAX_SIGNS_PER_SYMBOL = 15;

    /**
     * Reference frame length, in samples, that the per-mode maximum pulse count is scaled against, the native
     * literal {@code 320} ({@code SMPL_FRAME_LEN}).
     */
    private static final int MAX_PULSES_REF_FRAMELEN = 320;

    /**
     * Pulse-count bin step for the run-length CMF index, the native {@code SMPL_MAX_PULSES_PER_SF}; the
     * subframe split degenerate ranges are bounded by twice this.
     */
    private static final int MAX_PULSES_PER_SF = 40;

    /**
     * Per-mode maximum pulse count, the native {@code smpl_max_pulses_per_frame[2][3]} indexed
     * {@code [lowRate][codedAsActiveVoice + voiced]}.
     *
     * <p>The second index is the sum of the coded-as-active-voice flag and the voiced flag, selecting the
     * background-noise, unvoiced, or voiced column. The value scales by {@code framelen / 320} to yield the
     * frame's pulse-count ceiling.
     */
    private static final int[][] MAX_PULSES_PER_FRAME = {
            {80, 160, 160},
            {16, 32, 32}
    };

    /**
     * Prevents instantiation of this stateless serializer utility.
     */
    private PulseEncoder() {
        throw new AssertionError("no instances");
    }

    /**
     * Serializes one frame's excitation pulse layout to the range-coder stream, {@code smpl_encode_pulses}.
     *
     * <p>Re-derives the pulse positions and per-subframe pulse counts from the stacked {@code pulses} array,
     * encodes the frame pulse count, splits it across subframes, then encodes each pulse's position and sign,
     * advancing {@code encoder} past every emitted symbol. The per-frame mode flags select the pulse-count
     * model, the maximum pulse count, and the split structure exactly as the native function does. The
     * re-derived per-subframe counts are written into {@code sfPulses}.
     *
     * @param encoder            the range encoder to write to and advance
     * @param tables             the prebuilt pulse-coding CMF families
     * @param pulses             the stacked signed pulse layout indexed by absolute sample position,
     *                           {@code framelen} entries
     * @param framelen           the frame length in samples
     * @param nSubfr             the number of subframes in the frame; 1, 2, or 4
     * @param lowRate            {@code true} for the low-rate pulse-count model, {@code false} for the
     *                           closed-form model
     * @param voiced             {@code true} if the frame is coded voiced
     * @param codedAsActiveVoice {@code true} if the frame is coded as active voice
     * @param sfPulses           the per-subframe pulse-count output, written in place, at least
     *                           {@code nSubfr} entries
     */
    public static void encode(
            MlowRangeEncoder encoder,
            PulseTables.Tables tables,
            short[] pulses,
            int framelen,
            int nSubfr,
            boolean lowRate,
            boolean voiced,
            boolean codedAsActiveVoice,
            int[] sfPulses) {
        int subfrlen = framelen / nSubfr;
        int voicedFlag = voiced ? 1 : 0;
        int codedFlag = codedAsActiveVoice ? 1 : 0;

        short[] positions = new short[framelen];
        int nPositions = 0;
        for (int sf = 0; sf < nSubfr; sf++) {
            sfPulses[sf] = 0;
            int base = sf * subfrlen;
            for (int i = 0; i < subfrlen; i++) {
                if (pulses[base + i] != 0) {
                    sfPulses[sf] += Math.abs(pulses[base + i]);
                    positions[nPositions++] = (short) (base + i);
                }
            }
        }
        int nPulses = 0;
        for (int i = 0; i < nSubfr; i++) {
            nPulses += sfPulses[i];
        }

        int maxPulses = MAX_PULSES_PER_FRAME[lowRate ? 1 : 0][codedFlag + voicedFlag] * framelen / MAX_PULSES_REF_FRAMELEN;
        int maxSubfrPulses = maxPulses / nSubfr;
        if (lowRate) {
            MlowEntropyWrapper.encodeUpdate(encoder, tables.nPulseCmfs()[codedFlag + voicedFlag], nPulses);
        } else {
            long fl = numPulsesCmf(maxPulses + 1, nPulses);
            long fh = numPulsesCmf(maxPulses + 1, nPulses + 1);
            long ft = numPulsesCmf(maxPulses + 1, maxPulses + 1);
            encoder.encode(fl, fh, ft);
        }

        if (nPulses == 0) {
            return;
        }

        if (nSubfr == 4) {
            int nPulsesFirsthalf = sfPulses[0] + sfPulses[1];
            int minSplit = Math.max(nPulses - MAX_PULSES_PER_SF * 2, 0);
            int minSplit2 = Math.max(nPulses - maxSubfrPulses * 2, 0);
            int maxSplit2 = nPulses - minSplit;
            if (maxSplit2 > minSplit2) {
                int cmfIx = nPulsesFirsthalf - minSplit;
                int[] cmf = tables.splitCmfs()[nPulses - 1];
                long sub = cmf[minSplit2 - minSplit] & 0xFFFFFFFFL;
                encoder.encode((cmf[cmfIx] & 0xFFFFFFFFL) - sub,
                        (cmf[cmfIx + 1] & 0xFFFFFFFFL) - sub,
                        (cmf[maxSplit2 - minSplit + 1] & 0xFFFFFFFFL) - sub);
            }
            if (nPulsesFirsthalf > 0) {
                encodeSplit2Subfrs(encoder, tables, nPulsesFirsthalf, sfPulses[0], maxSubfrPulses);
            }
            if (nPulsesFirsthalf < nPulses) {
                encodeSplit2Subfrs(encoder, tables, nPulses - nPulsesFirsthalf, sfPulses[2], maxSubfrPulses);
            }
        } else if (nSubfr == 2) {
            encodeSplit2Subfrs(encoder, tables, nPulses, sfPulses[0], maxSubfrPulses);
        } else {
            sfPulses[0] = nPulses;
        }

        encodePositions(encoder, tables, pulses, positions, subfrlen, nSubfr, sfPulses);
        encodeSigns(encoder, pulses, positions, nPositions);
    }

    /**
     * Encodes the split of a pulse count between two subframes, {@code encode_split_2_subfrs}.
     *
     * <p>When the split range is degenerate (a single feasible value) no symbol is emitted, matching the
     * native early return. Otherwise the first subframe's count is encoded against the split CMF window
     * {@code [minSplit, maxSplit]} of {@code splitCmfs[nPulses - 1]}, with every span taken relative to
     * {@code cmf[minSplit]}.
     *
     * @param encoder          the range encoder to write to and advance
     * @param tables           the prebuilt pulse-coding CMF families
     * @param nPulses          the count to split between the two subframes
     * @param nPulsesFirsthalf the first subframe's count to encode
     * @param maxSubfrPulses   the per-subframe pulse-count ceiling
     */
    private static void encodeSplit2Subfrs(
            MlowRangeEncoder encoder,
            PulseTables.Tables tables,
            int nPulses,
            int nPulsesFirsthalf,
            int maxSubfrPulses) {
        int minSplit = Math.max(nPulses - maxSubfrPulses, 0);
        int maxSplit = nPulses - minSplit;
        if (maxSplit == minSplit) {
            return;
        }
        int[] cmf = tables.splitCmfs()[nPulses - 1];
        long sub = cmf[minSplit] & 0xFFFFFFFFL;
        encoder.encode((cmf[nPulsesFirsthalf] & 0xFFFFFFFFL) - sub,
                (cmf[nPulsesFirsthalf + 1] & 0xFFFFFFFFL) - sub,
                (cmf[maxSplit + 1] & 0xFFFFFFFFL) - sub);
    }

    /**
     * Encodes every pulse's position as a run length, the position loop of {@code smpl_encode_pulses}.
     *
     * <p>Walks the subframes in order; within a subframe each pulse's gap to the previous pulse (or to the
     * subframe start for the first) is encoded against the run-length CMF selected by the remaining sample and
     * pulse counts. A stacked position (a sample with magnitude greater than one) emits one run-length symbol
     * per stacked unit, with a zero gap for each unit after the first, exactly as the native inner
     * {@code k < n_stacked} loop does.
     *
     * @param encoder   the range encoder to write to and advance
     * @param tables    the prebuilt pulse-coding CMF families
     * @param pulses    the stacked signed pulse layout
     * @param positions the absolute distinct pulse positions in decode order
     * @param subfrlen  the subframe length in samples
     * @param nSubfr    the number of subframes
     * @param sfPulses  the per-subframe pulse counts
     */
    private static void encodePositions(
            MlowRangeEncoder encoder,
            PulseTables.Tables tables,
            short[] pulses,
            short[] positions,
            int subfrlen,
            int nSubfr,
            int[] sfPulses) {
        int runLengthStep = tables.runLengthStep();
        int posIx = 0;
        for (int i = 0; i < nSubfr; i++) {
            int pulsesLeft = sfPulses[i];
            int nSamplesLeft = subfrlen;
            int posPrev = 1;
            while (pulsesLeft > 0) {
                int posFrame = positions[posIx++];
                int nStacked = Math.abs(pulses[posFrame]);
                int pos = posFrame - subfrlen * i + 1;
                for (int k = 0; k < nStacked; k++) {
                    int cmfInd = (nSamplesLeft + runLengthStep - 1) / runLengthStep - 1;
                    int[] cmfp = tables.runLenCmfs()[cmfInd][pulsesLeft - 1];
                    int maxSamples = tables.runLenMaxSamples(cmfInd);
                    int ixStart = maxSamples - nSamplesLeft;
                    int ix = ixStart + (pos - posPrev);
                    if (ixStart > 0) {
                        long sub = cmfp[ixStart] & 0xFFFFFFFFL;
                        encoder.encode((cmfp[ix] & 0xFFFFFFFFL) - sub,
                                (cmfp[ix + 1] & 0xFFFFFFFFL) - sub,
                                (cmfp[maxSamples] & 0xFFFFFFFFL) - sub);
                    } else {
                        encoder.encode(cmfp[ix] & 0xFFFFFFFFL, cmfp[ix + 1] & 0xFFFFFFFFL,
                                cmfp[maxSamples] & 0xFFFFFFFFL);
                    }
                    nSamplesLeft = subfrlen - pos + 1;
                    posPrev = pos;
                    pulsesLeft -= 1;
                }
            }
        }
    }

    /**
     * Encodes every pulse's sign as packed uniform symbols, the sign loop of {@code smpl_encode_pulses}.
     *
     * <p>Each sign bit is one for a positive pulse and zero for a negative pulse; bits are accumulated
     * most-significant-first into a symbol of up to {@link #MAX_SIGNS_PER_SYMBOL} bits and emitted as a
     * uniform symbol over {@code 1 << signsInSymbol} values, with a final short symbol for any remainder.
     *
     * @param encoder    the range encoder to write to and advance
     * @param pulses     the stacked signed pulse layout
     * @param positions  the absolute distinct pulse positions in decode order
     * @param nPositions the number of distinct pulse positions
     */
    private static void encodeSigns(MlowRangeEncoder encoder, short[] pulses, short[] positions, int nPositions) {
        int sym = 0;
        int signsInSym = 0;
        for (int i = 0; i < nPositions; i++) {
            int sgn = pulses[positions[i]] > 0 ? 1 : 0;
            sym = (sym << 1) + sgn;
            signsInSym++;
            if (signsInSym == MAX_SIGNS_PER_SYMBOL) {
                MlowEntropyWrapper.encodeUniform(encoder, 1 << MAX_SIGNS_PER_SYMBOL, sym);
                sym = 0;
                signsInSym = 0;
            }
        }
        if (signsInSym > 0) {
            MlowEntropyWrapper.encodeUniform(encoder, 1 << signsInSym, sym);
        }
    }

    /**
     * Evaluates the closed-form pulse-count cumulative model, {@code smpl_num_pulses_cmf}.
     *
     * <p>Returns {@code 0} for {@code nPulses == 0}; otherwise
     * {@code maxPulses * (nPulses + 1) - (((nPulses - 2) * (nPulses - 3)) >> 1)}, the cumulative frequency up
     * to and including {@code nPulses} on the non-low-rate pulse-count scale. The native call passes
     * {@code max_pulses + 1} as the first argument.
     *
     * @param maxPulses the first model argument, the native {@code max_pulses + 1}
     * @param nPulses   the pulse count to evaluate the cumulative frequency at
     * @return the cumulative frequency as an unsigned value
     */
    private static long numPulsesCmf(int maxPulses, int nPulses) {
        if (nPulses == 0) {
            return 0;
        }
        return ((long) maxPulses * (nPulses + 1) - (((nPulses - 2) * (nPulses - 3)) >> 1)) & 0xFFFFFFFFL;
    }
}
