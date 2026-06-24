/**
 * Decode-path linear-filter primitives for the MLow speech codec: the synthesis-side ports of the FIR/IIR
 * filters in {@code smpl_filt.c}.
 *
 * <p>MLow speech synthesis is built from a small set of fixed-structure moving-average (MA, FIR) and
 * auto-regressive (AR, IIR) sections and their pole-zero (ARMA) and allpass cascades. The 16th-order AR
 * filter is the short-term LPC synthesis filter; the lower-order MA/AR/ARMA sections are the pulse-shaping,
 * tilt-postfilter, noise-shaping, and high-pass stages the decoder threads around it; the symmetric
 * fractional-delay FIR is the long-term-prediction interpolation kernel the adaptive-codebook basis uses.
 * The single {@code Filters} holder collects each primitive the synthesis path invokes, with the exact
 * single-precision operation order of the C scalar (non-NEON) implementation that the x86 reference build
 * compiles.
 *
 * <p>The two native state conventions are preserved: explicit caller-owned state vectors for the recursive
 * sections threaded across subframes and frames, and in-buffer history (samples kept in front of the input
 * or output window) for the fixed-order sections whose memory the native code addresses with negative-index
 * pointer arithmetic. The filters are stateless static methods over raw {@code float} arrays with explicit
 * offsets, so the contiguous history-plus-window layout is expressible without copies.
 *
 * <p>This package targets only the 16 kHz, 60 ms, mono SMPL-mode low-band decode path with
 * {@code SMPL_LPC_ORDER == 16} and the postfilter disabled. The high-band QMF allpass filterbank and the
 * 32-to-48 kHz upsampler (both only active above 16 kHz) are out of scope and are not ported.
 *
 * <p>This package is internal to the MLow decode implementation and is intentionally not exported from the
 * module.
 */
package com.github.auties00.cobalt.calls2.media.audio.mlow.filter;
