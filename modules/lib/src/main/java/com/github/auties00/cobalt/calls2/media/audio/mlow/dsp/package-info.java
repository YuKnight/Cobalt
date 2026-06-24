/**
 * Single-precision digital-signal-processing primitives the MLow speech codec embeds for its spectral
 * analysis.
 *
 * <p>MLow is WhatsApp's deterministic float CELP low-bitrate speech vocoder. Its encoder estimates the
 * short-term linear-prediction spectrum of every frame with a fast Fourier transform; this package holds the
 * port of the FFT the native codec links for that step. The transform output feeds the line-spectral-frequency
 * quantizer, so it must be bit-reproducible against the native encoder down to the float least-significant
 * bit.
 *
 * <p>{@link com.github.auties00.cobalt.calls2.media.audio.mlow.dsp.Pffft} is the port of the SSE path of
 * {@code smpl/pffft/pffft.c} (Julien Pommier's PFFFT, an SSE adaptation of NETLIB FFTPACK v4). The native
 * build compiles the SSE variant on x86-64, whose butterfly evaluation order and intermediate single-precision
 * roundings differ from the scalar fallback; the SSE path is the one reproduced here so the result is
 * bit-faithful. The MLow encoder drives only the 512-point real forward transform, but the complex path and
 * the backward direction are ported as well and validated by the forward-then-backward identity.
 *
 * <p>Scope is the SMPL 16 kHz / 60 ms / mono path. This package is internal to the MLow implementation and is
 * intentionally not exported from the module.
 */
package com.github.auties00.cobalt.calls2.media.audio.mlow.dsp;
