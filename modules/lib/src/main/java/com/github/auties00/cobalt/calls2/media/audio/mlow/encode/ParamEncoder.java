package com.github.auties00.cobalt.calls2.media.audio.mlow.encode;

import com.github.auties00.cobalt.calls2.media.audio.mlow.entropy.MlowEntropyWrapper;
import com.github.auties00.cobalt.calls2.media.audio.mlow.entropy.MlowRangeEncoder;
import com.github.auties00.cobalt.calls2.media.audio.mlow.lsf.LsfDequantizer;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.LsfCodebooks;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.LsfCodebooks.Codebook;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.LsfCodebooks.Stage1;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.LsfCodebooks.Stage2;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.MiscTables;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.NrgResTables;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.PitchTables;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.PitchTables.Blockseg;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.PitchTables.PitchData;
import com.github.auties00.cobalt.calls2.media.audio.mlow.tables.PulseTables;

/**
 * Per-frame low-band parameter serializer for the MLow speech codec, the orchestrating port of
 * {@code smpl_encode_lb_params} (and its {@code encode_lb_voiced} / {@code encode_lb_unvoiced} /
 * {@code smpl_encode_lags} helpers) in {@code smpl_param_coding.c}, the exact inverse of
 * {@link com.github.auties00.cobalt.calls2.media.audio.mlow.param.ParamDecoder}.
 *
 * <p>This is the bit-exact checkpoint of the MLow encode back end. Per internal frame it range-encodes the
 * full low-band parameter set in exactly the order, and with exactly the conditional-coding state, that
 * {@code ParamDecoder} reads it back:
 * <ol>
 * <li>the voicing flag (only when the frame is coded as active voice), against the voicing CMF selected by the
 * frame number and the previous frame's voicing;</li>
 * <li>the stage-1 and per-coefficient stage-2 LSF indices, conditional on the previous frame when the
 * conditional-coding flag survives;</li>
 * <li>the LSF interpolation index, only for a multi-subframe frame coded as active voice;</li>
 * <li>the fixed-codebook excitation pulses ({@link PulseEncoder}), unless the frame is a SID frame in which
 * case the pulse counts are zero;</li>
 * <li>then either the voiced gain set (per-subframe adaptive-codebook gain index and, for subframes with
 * pulses, the fixed-codebook gain index) followed by the pitch lags, or the unvoiced residual-energy set
 * (frame energy index, shape index, per-subframe fixed-codebook gain offsets).</li>
 * </ol>
 *
 * <p>Write order is load-bearing: the range coder is a single serial stream, so emitting any symbol out of
 * order, or against the wrong CMF, desynchronizes every later symbol in the frame and every later frame in the
 * packet. The conditional-coding flag is threaded exactly as the native code threads it: it enters each frame
 * from the caller ({@code false} for the first frame of a packet, {@code true} for later frames whose voicing
 * matches the previous frame), and when it is {@code false} the per-frame predictors (previous
 * adaptive-codebook index, previous fixed-codebook index, previous residual-energy index, and the pitch-lag
 * carry) are reset.
 *
 * <p>This serializer is stateful across the frames of a packet and across packets of one continuous encode
 * session, mirroring the {@code ParamsEncoder} embedded in the native core encoder. Construct one per logical
 * stream and feed it every frame in order. {@link #reset()} returns it to the freshly constructed state.
 *
 * <p>Scope is the SMPL 16 kHz, 60 ms, mono low-band path. The high-band parameter encode
 * ({@code smpl_encode_hb_params}) is deliberately not invoked here; at 16 kHz the high band is absent. This
 * type is internal to the MLow encode implementation and is intentionally not exported from the module.
 *
 * @implNote This implementation merges {@code smpl_encode_lb_params}, its {@code encode_lb_voiced}
 * gain-and-lag tail, its {@code encode_lb_unvoiced} residual-energy tail, and {@code smpl_encode_lags} into one
 * serializer. The LSF, pulse, residual-energy, and pitch-lag CMFs are the same tables the decode leaf decoders
 * read; the stage-1 and stage-2 LSF CMFs are taken from {@link LsfCodebooks} (the decode-ready
 * {@link Stage1#cmf()} / {@link Stage1#cmfCond()} / {@link Stage2#cmf()}), which are exactly the C
 * {@code smpl_LSF_CMF_*} and {@code pLsfCb->st2[...].cmf[i]} the encoder uses. The voiced gain encode
 * reproduces the {@code (prev_acb_idx + 1)} conditional CMF row selection, the {@code mean += cb[acb*M] +
 * 2*cb[acb*M+1]} fixed-point accumulation, and the absolute-versus-delta fixed-codebook gain branch with the
 * native delta-window base {@code &fcbgains_v_delta_cmf[SMPL_FCBG_V_N - 1]} measured relative to
 * {@code pCMF[min_delta]}; the {@code mean_acbg_Q14 /= num_subfr} integer division selects the pitch-lag delta
 * mode exactly as the decoder recomputes it. The lag encode reproduces {@code smpl_encode_lags} including the
 * block-transition windowing for multi-frame packets and the uniform first-lag emission, with every CMF span
 * taken with the same relative offset the decoder resolves against. The conditional-coding reset and the
 * {@code prev_voiced} carry mirror the native {@code ParamsEncoder} state.
 */
public final class ParamEncoder {
    /**
     * Number of taps per adaptive-codebook gain codebook vector, the native {@code SMPL_ACBG_M}.
     */
    private static final int ACBG_M = MiscTables.ACBG_M;

    /**
     * Number of adaptive-codebook gain symbols, the native {@code SMPL_ACBG_N}; the conditional CMF row has
     * one more entry than this.
     */
    private static final int ACBG_N = MiscTables.ACBG_N;

    /**
     * Number of symbols in the voiced fixed-codebook-gain absolute CMF, the native {@code SMPL_FCBG_V_N}.
     */
    private static final int FCBG_V_N = 34;

    /**
     * Linear-prediction order of the MLow short-term filter, the native {@code SMPL_LPC_ORDER}.
     */
    private static final int LPC_ORDER = 16;

    /**
     * Number of coarse pitch-lag blocks, the native {@code PITCH_NUM_BLOCKS}; the total span of a
     * block-transition CMF row.
     */
    private static final int PITCH_NUM_BLOCKS = 9;

    /**
     * The within-block lag block size in lag indices, the native
     * {@code SMPL_PITCHBLOCK_MS * SMPL_PITCH_FS_KHZ * 2} = 64.
     */
    private static final int BLOCKSIZE = 64;

    /**
     * The shared prebuilt pulse-coding CMF families, the native {@code smpl_get_pulse_data()} product.
     */
    private final PulseTables.Tables pulseTables;

    /**
     * The shared decode-ready two-stage LSF codebook, supplying the stage-1 and stage-2 LSF CMFs.
     */
    private final Codebook lsfCodebook;

    /**
     * The shared 20 ms pitch encode data, the native {@code smpl_get_pitch_data(SMPL_PITCH_NUM_SUBFRAMES)}.
     */
    private final PitchData pitchData;

    /**
     * The previous adaptive-codebook gain index, the native {@code ParamsEncoder.prev_acb_idx}; {@code -1}
     * when conditional coding was reset.
     */
    private int prevAcbIdx;

    /**
     * The previous fixed-codebook gain index, the native {@code ParamsEncoder.prev_fcb_idx}; {@code -1} when
     * conditional coding was reset, which selects the absolute fixed-codebook gain encode.
     */
    private int prevFcbIdx;

    /**
     * The previous residual-energy index predictor, the native {@code ParamsEncoder.prev_nrgres_idx}; reset
     * but never read on the low-band path, matching the native code.
     */
    private int prevNrgresIdx;

    /**
     * The previous frame's last lag block, the native {@code ParamsEncoder.prev_lagblk}; {@code -1} when there
     * is no previous frame in this packet or conditional coding was reset.
     */
    private int prevLagblk;

    /**
     * The previous frame's last lag index, the native {@code ParamsEncoder.prev_lagidx}; {@code -1} when there
     * is no previous frame in this packet or conditional coding was reset.
     */
    private int prevLagidx;

    /**
     * Constructs a low-band parameter serializer over the shared MLow encode tables and clears its state.
     *
     * <p>The pulse, LSF, and pitch tables are the shared instances; the cross-frame predictors start in the
     * reset state, ready to encode the first frame of the first packet.
     */
    public ParamEncoder() {
        this.pulseTables = PulseTables.build();
        this.lsfCodebook = LsfCodebooks.load();
        this.pitchData = PitchTables.data20();
        reset();
    }

    /**
     * Returns this serializer to its freshly constructed state, the equivalent of re-running
     * {@code smpl_init_param_encoder} on the embedded {@code ParamsEncoder}.
     *
     * <p>Clears the conditional-coding predictors, the pitch-lag carry, and the previous-frame voicing flag.
     * Call this between independent encode sessions; do not call it between the packets of one continuous
     * stream, which must thread state.
     */
    public void reset() {
        this.prevAcbIdx = -1;
        this.prevFcbIdx = -1;
        this.prevNrgresIdx = -1;
        this.prevLagblk = -1;
        this.prevLagidx = -1;
    }

    /**
     * Serializes the full low-band parameter set of one internal frame, {@code smpl_encode_lb_params}.
     *
     * <p>Emits, in the native order, the voicing flag, the LSF indices, the LSF interpolation index, the
     * excitation pulses, and then the voiced or unvoiced gain set, advancing {@code encoder} past every
     * symbol. The {@code condCoding} argument is the conditional-coding flag the core encoder computed for
     * this frame ({@code false} for the first frame of a packet, {@code true} otherwise when the voicing
     * matches the previous frame); when it is {@code false} the cross-frame predictors are reset before the
     * LSF and gain encode read them.
     *
     * @param encoder    the range encoder positioned at the frame's first symbol
     * @param params     the frame's quantized low-band parameters
     * @param framelen   the frame length in samples, the native {@code framelen}
     * @param numSubfr   the number of subframes in the frame, the native {@code num_subfr}
     * @param codedAsActiveVoice {@code true} when the frame is coded as if it may contain voiced energy, the
     *                   native {@code coded_as_active_voice}
     * @param condCoding the conditional-coding flag on entry to this frame, the native {@code cond_coding}
     * @param lowRate    {@code true} for the low-rate mode, {@code false} for high rate
     * @param frameNum   the zero-based index of this frame within the packet, the native {@code frame_num}
     * @param prevVoiced the previous frame's voicing flag the core encoder threads in, the native
     *                   {@code prev_voiced}; ignored for the first frame of a packet
     * @param sid        {@code true} for a SID frame, the native {@code SID}
     */
    public void encodeFrame(MlowRangeEncoder encoder, LbQuantParams params, int framelen, int numSubfr,
                            boolean codedAsActiveVoice, boolean condCoding, boolean lowRate, int frameNum,
                            int prevVoiced, boolean sid) {
        int voiced = params.voiced() ? 1 : 0;
        int lowRateIx = lowRate ? 1 : 0;

        // Voicing
        if (codedAsActiveVoice) {
            int[] cmf = MiscTables.VUV_CMFS[frameNum == 0 ? 0 : prevVoiced == 0 ? 1 : 2];
            MlowEntropyWrapper.encodeUpdate(encoder, cmf, voiced);
        }

        if (!condCoding) {
            prevAcbIdx = -1;
            prevFcbIdx = -1;
            prevNrgresIdx = -1;
            prevLagblk = -1;
            prevLagidx = -1;
        }

        // LSF stage-1 and stage-2 indices
        int[] lsfIdx = params.lsfIdx();
        Stage1 st1 = lsfCodebook.stage1(voiced);
        int[] stage1Cmf = condCoding ? st1.cmfCond() : st1.cmf();
        MlowEntropyWrapper.encodeUpdate(encoder, stage1Cmf, lsfIdx[0]);
        Stage2 st2 = lsfCodebook.stage2(voiced, lowRateIx, lsfIdx[0]);
        for (int i = 0; i < LPC_ORDER; i++) {
            MlowEntropyWrapper.encodeUpdate(encoder, st2.cmf()[i], lsfIdx[i + 1]);
        }

        // LSF interpolation index
        if (codedAsActiveVoice && numSubfr > 1) {
            MlowEntropyWrapper.encodeUpdate(encoder, MiscTables.LSF_INTERP_CMF, params.lsfInterpolIdx());
        }

        // Excitation pulses
        int[] sfPulses = new int[numSubfr];
        if (!sid) {
            PulseEncoder.encode(encoder, pulseTables, params.pulses(), framelen, numSubfr, lowRate,
                    params.voiced(), codedAsActiveVoice, sfPulses);
        }

        if (codedAsActiveVoice && params.voiced()) {
            encodeVoiced(encoder, params, sfPulses, numSubfr, framelen / 40, lowRate);
        } else {
            encodeUnvoiced(encoder, params, sfPulses, numSubfr);
        }
    }

    /**
     * Encodes the voiced per-subframe gain set and the pitch lags, {@code encode_lb_voiced}.
     *
     * <p>For each subframe emits the adaptive-codebook gain index against the conditional CMF row selected by
     * {@code prevAcbIdx + 1}, accumulates the mean quantized gain from the rate-selected codebook, and, when
     * the subframe carries pulses, emits the fixed-codebook gain index either absolutely (the first one of a
     * conditional run) or as a signed delta from the previous index. The accumulated mean divided by the
     * subframe count selects the pitch-lag delta mode, then {@link #encodeLags} emits the block segmentation
     * and lag indices.
     *
     * @param encoder       the range encoder positioned at the first adaptive-codebook gain symbol
     * @param params        the frame's quantized parameters
     * @param sfPulses      the per-subframe pulse counts from the pulse encode, gating the fixed-codebook gain
     *                      emits
     * @param numSubfr      the number of subframes in the frame
     * @param pitchNumSubfr the number of pitch (lag) subframes, the native {@code framelen / SMPL_LAG_SUBFRLEN}
     * @param lowRate       {@code true} for the low-rate gain codebooks and CMFs
     */
    private void encodeVoiced(MlowRangeEncoder encoder, LbQuantParams params, int[] sfPulses, int numSubfr,
                              int pitchNumSubfr, boolean lowRate) {
        long meanAcbgQ14 = 0;
        short[] acbgCbk = lowRate ? MiscTables.ACB_GAINS_LR_Q14 : MiscTables.ACB_GAINS_HR_Q14;
        int[] acbgIdx = params.acbgIdx();
        int[] fcbgIdx = params.fcbgIdx();
        for (int sf = 0; sf < numSubfr; sf++) {
            int[] cmf = lowRate ? MiscTables.acbGainsCmfLr(prevAcbIdx + 1) : MiscTables.acbGainsCmfHr(prevAcbIdx + 1);
            MlowEntropyWrapper.encodeUpdate(encoder, cmf, acbgIdx[sf]);
            prevAcbIdx = acbgIdx[sf];
            meanAcbgQ14 += acbgCbk[prevAcbIdx * ACBG_M] + 2 * acbgCbk[prevAcbIdx * ACBG_M + 1];
            if (sfPulses[sf] > 0) {
                if (prevFcbIdx == -1) {
                    MlowEntropyWrapper.encodeUpdate(encoder, MiscTables.fcbgVCmf(), fcbgIdx[sf]);
                } else {
                    int delta = fcbgIdx[sf] - prevFcbIdx;
                    int minDelta = -prevFcbIdx;
                    int maxDelta = (FCBG_V_N - 1) - prevFcbIdx;
                    int[] dcmf = MiscTables.fcbgVDeltaCmf();
                    int base = FCBG_V_N - 1;
                    long sub = dcmf[base + minDelta] & 0xFFFFFFFFL;
                    encoder.encode((dcmf[base + delta] & 0xFFFFFFFFL) - sub,
                            (dcmf[base + delta + 1] & 0xFFFFFFFFL) - sub,
                            (dcmf[base + maxDelta + 1] & 0xFFFFFFFFL) - sub);
                }
                prevFcbIdx = fcbgIdx[sf];
            }
        }
        int meanInt = (int) (meanAcbgQ14 / numSubfr);

        int mode = 2;
        int[] thr = PitchTables.acbgainThr20Q14();
        if (meanInt < thr[0]) {
            mode = 0;
        } else if (meanInt < thr[1]) {
            mode = 1;
        }
        encodeLags(encoder, params.blocksegsIx(), params.laginds(), mode);
        Blockseg seg = pitchData.blocksegs()[params.blocksegsIx()];
        prevLagblk = seg.blocks()[seg.nblocks() - 1];
        prevLagidx = params.laginds()[pitchNumSubfr - 1];
    }

    /**
     * Encodes the pitch-lag block segmentation and per-segment lag indices, {@code smpl_encode_lags}.
     *
     * <p>Emits the block-segmentation index (directly for the first frame of a packet, or as a block
     * transition plus a windowed segmentation index for a multi-frame continuation), then the first segment's
     * lag (uniformly, when the segmentation does not chain onto the previous frame's block) and each remaining
     * segment's delta lag against the running previous lag. Every CMF span is taken with the same relative
     * offset the decoder resolves against.
     *
     * @param encoder     the range encoder positioned at the block-segmentation index symbol(s)
     * @param blocksegsIx the C block-segmentation index of this frame, the native {@code blocksegs_ix}
     * @param laginds     the per-pitch-subframe integer lag indices, the native {@code laginds}
     * @param mode        the within-block delta-lag CMF class selected by the mean quantized gain
     */
    private void encodeLags(MlowRangeEncoder encoder, int blocksegsIx, int[] laginds, int mode) {
        int ixJulia = pitchData.blocksegs2idx()[blocksegsIx] & 0xFF;
        Blockseg seg = pitchData.blocksegs()[blocksegsIx];
        int numBlocksegs = pitchData.numBlocksegs();

        if (prevLagblk < 0) {
            int[] cmf = pitchData.blocksegIdxCmf();
            encoder.encode(cmf[ixJulia - 1] & 0xFFFFFFFFL, cmf[ixJulia] & 0xFFFFFFFFL,
                    cmf[numBlocksegs] & 0xFFFFFFFFL);
        } else {
            int[] transCmf = pitchData.blockTransitionCmf()[prevLagblk];
            int block0 = seg.blocks()[0];
            encoder.encode(transCmf[block0] & 0xFFFFFFFFL, transCmf[block0 + 1] & 0xFFFFFFFFL,
                    transCmf[PITCH_NUM_BLOCKS] & 0xFFFFFFFFL);
            byte[] range = pitchData.firstBlockRange();
            int startIx = range[block0 * 2] & 0xFF;
            int rangeEnd = range[block0 * 2 + 1] & 0xFF;
            int cmfLen = rangeEnd - startIx + 1;
            int[] cmf = pitchData.blocksegIdxCmf();
            long sub = cmf[startIx] & 0xFFFFFFFFL;
            int rel = ixJulia - startIx - 1;
            encoder.encode((cmf[startIx + rel] & 0xFFFFFFFFL) - sub,
                    (cmf[startIx + rel + 1] & 0xFFFFFFFFL) - sub,
                    (cmf[startIx + cmfLen] & 0xFFFFFFFFL) - sub);
        }

        int blk = seg.blocks()[0];
        int deltaBlk = blk - prevLagblk;
        int startSeg = 0;
        int lagindsIx = 0;
        if (!(prevLagblk > -1 && deltaBlk >= -1 && deltaBlk <= 2)) {
            int idxMod = laginds[lagindsIx] - blk * BLOCKSIZE;
            encoder.encode(idxMod, idxMod + 1, BLOCKSIZE);
            prevLagblk = blk;
            prevLagidx = laginds[lagindsIx];
            lagindsIx += seg.seglens()[0];
            startSeg = 1;
        }

        int[] deltaLagCmf = pitchData.deltaLagCmfs()[mode];
        int[] blocks = seg.blocks();
        int[] seglens = seg.seglens();
        for (int k = startSeg; k < seg.nblocks(); k++) {
            blk = blocks[k];
            int idx = laginds[lagindsIx];
            lagindsIx += seglens[k];
            deltaBlk = blk - prevLagblk;
            int deltaIdx = idx - prevLagidx;
            int prevLagidxMod = prevLagidx - prevLagblk * BLOCKSIZE;
            int deltaRangeStart = -prevLagidxMod + deltaBlk * BLOCKSIZE;
            int windowStart = deltaRangeStart + 2 * BLOCKSIZE - 1;
            int ix = deltaIdx - deltaRangeStart;
            long sub = deltaLagCmf[windowStart] & 0xFFFFFFFFL;
            encoder.encode((deltaLagCmf[windowStart + ix] & 0xFFFFFFFFL) - sub,
                    (deltaLagCmf[windowStart + ix + 1] & 0xFFFFFFFFL) - sub,
                    (deltaLagCmf[windowStart + BLOCKSIZE] & 0xFFFFFFFFL) - sub);
            prevLagblk = blk;
            prevLagidx = idx;
        }
    }

    /**
     * Encodes the unvoiced residual-energy set, {@code encode_lb_unvoiced}.
     *
     * <p>Emits the frame-level residual-energy index (and, for multi-subframe frames, the shape index) against
     * the gain and shape CMFs for the subframe count, then for each subframe that carries pulses emits the
     * fixed-codebook gain offset index against a window of the offset CMF selected by subframe-count bin and
     * pulse-count bin, with the window start derived from the subframe's reconstructed Q14 decibel energy
     * exactly as the decoder derives it.
     *
     * @param encoder  the range encoder positioned at the frame-level residual-energy symbol
     * @param params   the frame's quantized parameters
     * @param sfPulses the per-subframe pulse counts, gating the fixed-codebook gain offset emits
     * @param numSubfr the number of subframes in the frame
     */
    private void encodeUnvoiced(MlowRangeEncoder encoder, LbQuantParams params, int[] sfPulses, int numSubfr) {
        int tableIx = numSubfrToIdx(numSubfr);
        if (numSubfr == 1) {
            MlowEntropyWrapper.encodeUpdate(encoder, NrgResTables.gain1Cmf(), params.nrgresFrameQi());
        } else if (numSubfr == 2) {
            MlowEntropyWrapper.encodeUpdate(encoder, NrgResTables.gain2Cmf(), params.nrgresFrameQi());
            MlowEntropyWrapper.encodeUpdate(encoder, NrgResTables.shapeCb2Cmf(), params.nrgresShapeQi());
        } else {
            MlowEntropyWrapper.encodeUpdate(encoder, NrgResTables.gain4Cmf(), params.nrgresFrameQi());
            MlowEntropyWrapper.encodeUpdate(encoder, NrgResTables.shapeCb4Cmf(), params.nrgresShapeQi());
        }

        int[] dbqQ14 = params.nrgresDbqQ14();
        int[] fcbgIdx = params.fcbgIdx();
        for (int i = 0; i < numSubfr; i++) {
            if (sfPulses[i] > 0) {
                int nrgresDbq = (dbqQ14[i] + (1 << 13)) >> 14;
                nrgresDbq = Math.min(Math.max(nrgresDbq, NrgResTables.RES_NRG_MIN_DB), NrgResTables.RES_NRG_MAX_DB);
                int minOffset = -nrgresDbq;
                int maxOffset = UV_GAIN_IDX_LEN - nrgresDbq;
                int cmfLen = maxOffset - minOffset + 2;
                int cmfIx = Math.min(sfPulses[i] / N_PULSES_STEP, FCB_G_OFFSET_CMFS - 1);
                int[] cmf = NrgResTables.fcbgOffsetCmf(tableIx, cmfIx);
                long sub = cmf[minOffset] & 0xFFFFFFFFL;
                int idx = fcbgIdx[i];
                encoder.encode((cmf[minOffset + idx] & 0xFFFFFFFFL) - sub,
                        (cmf[minOffset + idx + 1] & 0xFFFFFFFFL) - sub,
                        (cmf[minOffset + cmfLen - 1] & 0xFFFFFFFFL) - sub);
            }
        }
    }

    /**
     * Pulse-count bin step, the native {@code SMPL_N_PULSES_STEP}; the per-subframe pulse count is divided by
     * this to pick the fixed-codebook gain offset CMF column.
     */
    private static final int N_PULSES_STEP = 10;

    /**
     * Number of fixed-codebook gain offset CMF columns, the native {@code SMPL_FCB_G_OFFSET_CMFS}.
     */
    private static final int FCB_G_OFFSET_CMFS = 4;

    /**
     * Length in decibel steps of the unvoiced fixed-codebook gain index range, the native
     * {@code SMPL_UV_GAIN_IDX_LEN}.
     */
    private static final int UV_GAIN_IDX_LEN = 90;

    /**
     * Maps a subframe count to its table bin, {@code num_subfr_to_idx} in {@code smpl_param_coding.c}.
     *
     * @param numSubfr the subframe count; must be 1, 2, or 4
     * @return the table bin: 0 for one, 1 for two, 2 for four subframes
     * @throws IllegalArgumentException if {@code numSubfr} is not 1, 2, or 4
     */
    private static int numSubfrToIdx(int numSubfr) {
        return switch (numSubfr) {
            case 1 -> 0;
            case 2 -> 1;
            case 4 -> 2;
            default -> throw new IllegalArgumentException("numSubfr must be 1, 2, or 4: " + numSubfr);
        };
    }
}
