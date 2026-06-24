package com.github.auties00.cobalt.calls2.media.audio.mlow.encode;

/**
 * Per-frame low-band quantized parameter set of the MLow speech encoder, the encode-relevant subset of the
 * native {@code LbQuantParams} struct ({@code smpl_typedef.h}) that {@code smpl_encode_lb_params}
 * ({@code smpl_param_coding.c}) serializes.
 *
 * <p>The MLow core encoder fills one of these per internal 20 ms frame from the analysis-by-synthesis search
 * stages: the voicing decision, the two-stage line-spectral-frequency indices and the LSF interpolation
 * index, the fixed-codebook excitation pulses, and then either the voiced gain-and-lag set (per-subframe
 * adaptive-codebook and fixed-codebook gain indices plus the pitch-lag block segmentation and lag indices) or
 * the unvoiced residual-energy set (frame energy index, shape index, per-subframe fixed-codebook gain offset
 * indices, and the reconstructed per-subframe Q14 decibel energies the gain-offset window keys off). The
 * serializer reads exactly these fields, in the native order, and emits the range-coded bitstream that the
 * decode-side {@link com.github.auties00.cobalt.calls2.media.audio.mlow.param.ParamDecoder} reconstructs them
 * from.
 *
 * <p>This is the carrier between the encode front end (the LPC/LSF, pitch, voicing, CELP, and residual-energy
 * search blocks) and {@link ParamEncoder}. It is a plain immutable value: a frame's parameters are gathered
 * by the front end, packed into one instance, and handed to the serializer. The {@code pulses} array is the
 * stacked signed pulse layout the native {@code LbQuantParams.pulses} holds (indexed by absolute sample
 * position within the frame, each entry the signed pulse magnitude at that position, with coincident pulses
 * summed); {@link PulseEncoder} re-derives the per-subframe pulse counts and positions from it exactly as the
 * native {@code smpl_encode_pulses} does, so {@code sfPulses} here is an analysis convenience the serializer
 * recomputes rather than trusts.
 *
 * <p>Scope is the SMPL 16 kHz, 60 ms, mono low-band path with prediction order {@value #LPC_ORDER}. The
 * high-band parameters ({@code HbQuantParams}) are out of scope at 16 kHz and are not carried here. This type
 * is internal to the MLow encode implementation and is intentionally not exported from the module.
 *
 * @param voiced         {@code true} when the frame is coded voiced, the native {@code voiced}
 * @param lsfIdx         the {@value #LPC_ORDER}{@code  + 1} LSF indices: the stage-1 centroid at index
 *                       {@code 0} then the {@value #LPC_ORDER} stage-2 level indices, the native
 *                       {@code lsf_idx}
 * @param lsfInterpolIdx the LSF interpolation index for a multi-subframe coded-as-active-voice frame, the
 *                       native {@code lsf_interpol_idx}; {@code 0} when not coded
 * @param pulses         the stacked signed fixed-codebook pulses indexed by absolute sample position,
 *                       {@code framelen} entries, the native {@code LbQuantParams.pulses}
 * @param sfPulses       the per-subframe pulse count, one entry per subframe, the native {@code sf_pulses};
 *                       an analysis convenience the serializer recomputes from {@code pulses}
 * @param acbgIdx        the per-subframe adaptive-codebook gain index, valid only for a voiced frame, the
 *                       native {@code acbg_idx}
 * @param fcbgIdx        the per-subframe fixed-codebook gain index (voiced) or gain offset index (unvoiced),
 *                       the native {@code fcbg_idx}; entries for subframes without pulses are unused
 * @param laginds        the per-pitch-subframe integer pitch lag indices, valid only for a voiced frame, the
 *                       native {@code laginds}, {@value #PITCH_NUM_SUBFRAMES} entries
 * @param blocksegsIx    the pitch-lag block-segmentation index, valid only for a voiced frame, the native
 *                       {@code blocksegs_ix}
 * @param nrgresFrameQi  the frame-level residual-energy index, valid only for an unvoiced frame, the native
 *                       {@code nrgres_frame_qi}
 * @param nrgresShapeQi  the residual-energy shape index, valid only for an unvoiced multi-subframe frame, the
 *                       native {@code nrgres_shape_qi}
 * @param nrgresDbqQ14   the per-subframe reconstructed residual energy in Q14 decibels, valid only for an
 *                       unvoiced frame, the native {@code nrgres_dbq_Q14}; the fixed-codebook gain offset
 *                       window start is derived from it
 */
public record LbQuantParams(
        boolean voiced,
        int[] lsfIdx,
        int lsfInterpolIdx,
        short[] pulses,
        int[] sfPulses,
        int[] acbgIdx,
        int[] fcbgIdx,
        int[] laginds,
        int blocksegsIx,
        int nrgresFrameQi,
        int nrgresShapeQi,
        int[] nrgresDbqQ14) {
    /**
     * Linear-prediction order of the MLow short-term filter, the native {@code SMPL_LPC_ORDER}; the LSF
     * index array has one more entry than this (the stage-1 centroid plus the per-coefficient stage-2
     * indices).
     */
    public static final int LPC_ORDER = 16;

    /**
     * Number of pitch subframes a 20 ms frame's lags span, the native {@code SMPL_PITCH_NUM_SUBFRAMES}; the
     * length of {@link #laginds()}.
     */
    public static final int PITCH_NUM_SUBFRAMES = 8;
}
