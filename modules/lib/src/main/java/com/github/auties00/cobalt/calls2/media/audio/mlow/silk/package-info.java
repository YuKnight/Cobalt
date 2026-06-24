/**
 * Integer fixed-point SILK signal-processing primitives the MLow speech codec embeds verbatim.
 *
 * <p>MLow is WhatsApp's deterministic float CELP low-bitrate speech vocoder, but a few of its
 * spectral-conversion steps must be bit-exact across platforms and so run in SILK's integer fixed-point
 * arithmetic rather than the fast-math float used everywhere else in the codec. This package is the port of
 * those integer routines, kept separate from the float decode packages so the Q-format conventions and the
 * SILK {@code SigProc_FIX.h} macro arithmetic stay isolated.
 *
 * <p>{@link com.github.auties00.cobalt.calls2.media.audio.mlow.silk.SilkNlsf2a} converts a normalized line
 * spectral frequency (NLSF) vector to linear-prediction filter coefficients, the port of
 * {@code silk/NLSF2A.c} and its supporting {@code silk/LPC_fit.c}, {@code silk/bwexpander_32.c}, and
 * {@code silk/LPC_inv_pred_gain.c}. The integer intermediates are bit-exact against the native decoder. The
 * float adapter that the decode path actually drives lives in
 * {@link com.github.auties00.cobalt.calls2.media.audio.mlow.lsf.NlsfBridge}.
 *
 * <p>Scope is the SMPL 16 kHz / 60 ms / mono path with {@code SMPL_LPC_ORDER == 16}. This package is internal
 * to the MLow decode implementation and is intentionally not exported from the module.
 */
package com.github.auties00.cobalt.calls2.media.audio.mlow.silk;
