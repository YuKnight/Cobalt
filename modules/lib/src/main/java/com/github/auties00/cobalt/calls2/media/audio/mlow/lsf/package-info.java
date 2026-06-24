/**
 * Two-stage line-spectral-frequency (LSF) inverse vector quantizer for the MLow speech codec.
 *
 * <p>MLow is WhatsApp's deterministic float CELP low-bitrate speech vocoder. Each frame's short-term
 * linear-prediction spectrum is coded as a stage-1 codebook centroid plus a per-coefficient stage-2 scalar
 * refinement, both range-coded into the bitstream. This package is the decode-side inverse of that
 * quantizer: the port of the decode subset of {@code smpl_lsf_quant.c} together with the LSF index-decode
 * steps of {@code smpl_decode_lb_params}.
 *
 * <p>{@link com.github.auties00.cobalt.calls2.media.audio.mlow.lsf.LsfDequantizer} reads the stage-1 and
 * stage-2 indices from
 * {@link com.github.auties00.cobalt.calls2.media.audio.mlow.entropy.MlowRangeDecoder} using the LSF
 * cumulative mass functions, then reconstructs the per-frame LSF vector from the decode-ready codebook
 * built by {@link com.github.auties00.cobalt.calls2.media.audio.mlow.tables.LsfCodebooks}. The decoded
 * integer indices are bit-exact against the native decoder; the reconstructed {@code float} LSF vector is
 * near-exact to within IEEE-754 single-precision rounding. The encoder-only quantization and survivor
 * search are out of scope.
 *
 * <p>Scope is the 16 kHz / 60 ms / mono SMPL low-band decode path. This package is internal to the MLow
 * decode implementation and is intentionally not exported from the module.
 */
package com.github.auties00.cobalt.calls2.media.audio.mlow.lsf;
