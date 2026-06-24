/**
 * Bit-exact range-coder entropy layer for the MLow speech codec bitstream.
 *
 * <p>MLow is WhatsApp's deterministic float CELP low-bitrate speech vocoder. Its compressed bitstream is
 * produced by the byte-wise range coder that Opus/CELT inherited from Martin (1979); the MLow native
 * sources carry {@code celt/entdec.c} and {@code celt/entcode.c} verbatim and drive them through the
 * {@code smpl_entropy_wrapper.c} shim. This package is the faithful, integer bit-exact Java port of that
 * decode-side entropy machinery and is the foundation the rest of the MLow decode pipeline reads from.
 *
 * <p>{@link com.github.auties00.cobalt.calls2.media.audio.mlow.entropy.MlowRangeDecoder} is the core
 * decoder: the {@code rng}/{@code val} register pair, the {@code 2^23} normalization threshold, the
 * front-and-back two-stream buffer, and the full primitive set (decode, update, icdf, bit-logp, uint,
 * raw bits, tell). {@link com.github.auties00.cobalt.calls2.media.audio.mlow.entropy.MlowRangeEncoder} is
 * its inverse: the same registers and threshold driven the other way, with the carry-propagation chain
 * ({@code rem}/{@code ext} buffering and the {@code 0xFF}-run ripple) and the stream-finalization flush that
 * the decoder has no counterpart for. An encoder and a decoder built from these two classes round-trip any
 * symbol stream byte for byte. {@link com.github.auties00.cobalt.calls2.media.audio.mlow.entropy.MlowEntropyWrapper}
 * adds the symbol-level helpers MLow's parameter codecs call (cumulative-table decode/encode and uniform
 * decode/encode).
 *
 * <p>This package is internal to the MLow implementation and is intentionally not exported from the
 * module.
 */
package com.github.auties00.cobalt.calls2.media.audio.mlow.entropy;
