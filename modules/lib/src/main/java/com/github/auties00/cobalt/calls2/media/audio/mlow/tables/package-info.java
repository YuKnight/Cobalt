/**
 * Static entropy-coding and codebook tables for the MLow speech codec, plus the loaders that expand
 * them into the runtime arrays the decode pipeline reads.
 *
 * <p>MLow is WhatsApp's deterministic float CELP low-bitrate speech vocoder. Its native sources bake
 * the codec's probability models and quantization codebooks in as packed static tables, then expand
 * them once at codec init into decoder-ready form. The leaf expansion logic lives in
 * {@code smpl_helpers.c} and is ported here as {@link com.github.auties00.cobalt.calls2.media.audio.mlow.tables.CmfBuilder}:
 * the bit-exact-critical delta-cumulative-mass-function to cumulative-mass-function transform that
 * produces the {@code int[]} CMF arrays
 * {@link com.github.auties00.cobalt.calls2.media.audio.mlow.entropy.MlowEntropyWrapper#decodeUpdate}
 * scans, plus the companion bit-cost and unpack helpers the table loaders call.
 *
 * <p>This package targets only the tables the 16 kHz, 60 ms, mono SMPL-mode decode path needs. The
 * high-band (above 16 kHz) tables and any 32 kHz, 48 kHz, stereo, or CELT-only tables are out of scope
 * for the shipped configuration and are not ported.
 *
 * <p>This package is internal to the MLow decode implementation and is intentionally not exported from
 * the module.
 */
package com.github.auties00.cobalt.calls2.media.audio.mlow.tables;
