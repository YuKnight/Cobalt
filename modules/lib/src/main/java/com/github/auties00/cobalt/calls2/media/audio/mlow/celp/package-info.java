/**
 * CELP excitation-parameter decoders for the MLow speech codec: the decode-side ports of the pulse,
 * gain, residual-energy, and pitch-lag parameter coding in {@code smpl_param_coding.c},
 * {@code smpl_pulse_coding.c}, {@code smpl_quant_nrg_res.c}, and the decode half of
 * {@code smpl_pitch_util.c}.
 *
 * <p>An MLow low-band frame's excitation is described by a small set of quantized integer parameters that
 * the codec range-codes into the bitstream: the fixed-codebook pulse train, the adaptive- and
 * fixed-codebook gains, the per-subframe residual energy (for unvoiced frames), and the per-subframe
 * pitch lags (for voiced frames). The decoders in this package turn the range-coded bitstream back into
 * those integer parameters and their dequantized float values, reading the cumulative mass functions and
 * codebooks expanded by the {@code tables} package through the entropy primitives of the {@code entropy}
 * package.
 *
 * <p>The decoded integer parameters are reproduced bit-for-bit against the native decoder; the float
 * dequantized values (gains, energies) are near-exact, reproducing the native single-precision
 * arithmetic including its fast power approximation where the shipped build uses one.
 *
 * <p>This package targets only the 16 kHz, 60 ms, mono SMPL-mode low-band decode path. The high-band
 * (above 16 kHz) parameter decoders are out of scope for the shipped configuration and are not ported.
 *
 * <p>This package is internal to the MLow decode implementation and is intentionally not exported from
 * the module.
 */
package com.github.auties00.cobalt.calls2.media.audio.mlow.celp;
