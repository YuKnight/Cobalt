/**
 * Encode-side line-spectral-frequency (LSF) vector quantizer for the MLow speech codec.
 *
 * <p>MLow is WhatsApp's deterministic float CELP low-bitrate speech vocoder. Each frame's short-term
 * linear-prediction spectrum is coded as a stage-1 codebook centroid plus a per-coefficient stage-2 scalar
 * refinement. This package is the encode-side analysis-by-synthesis quantizer that chooses those indices: the
 * port of the encode subset of {@code smpl_lsf_quant.c} ({@code smpl_lsf_quant}, {@code smpl_lsf_quant_cond},
 * {@code smpl_lsf_quant_core}, and {@code VQ_temp}), the exact inverse of the decode-side
 * {@link com.github.auties00.cobalt.calls2.media.audio.mlow.lsf.LsfDequantizer}.
 *
 * <p>{@link com.github.auties00.cobalt.calls2.media.audio.mlow.encode.LsfQuantizer} converts the float
 * prediction filter to line spectral frequencies with
 * {@link com.github.auties00.cobalt.calls2.media.audio.mlow.lsf.A2nlsfBridge}, runs a two-stage weighted
 * vector-quantization survivor search over the encoder codebook built by
 * {@link com.github.auties00.cobalt.calls2.media.audio.mlow.tables.EncoderTables} and
 * {@link com.github.auties00.cobalt.calls2.media.audio.mlow.tables.LsfCodebooks}, and returns the stage-1,
 * conditional, and stage-2 indices the range encoder writes, alongside the quantized LSF vector the
 * analysis-by-synthesis loop consumes. The selected integer indices are bit-exact against the native encoder,
 * so they reconstruct to exactly the LSF vector {@code LsfDequantizer} produces from the same indices.
 *
 * <p>Scope is the 16 kHz / 60 ms / mono SMPL low-band encode path. This package is internal to the MLow
 * encode implementation and is intentionally not exported from the module.
 */
package com.github.auties00.cobalt.calls2.media.audio.mlow.encode;
