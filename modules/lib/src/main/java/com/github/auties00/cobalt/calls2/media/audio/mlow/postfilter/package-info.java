/**
 * The deterministic MLow decode postfilter chain: the harmonic, LPC, and pitch-adaptive high-pass postfilters
 * the shipping SMPL decoder runs over the synthesized low-band before the {@code int16} conversion, the
 * decode-side port of the postfilter-on branch of {@code smpl_core_decode} ({@code smpl_core_decoder.c}) and
 * the postfilter bodies in {@code smpl_postfilter.c}, {@code smpl_postfilter_util.c},
 * {@code smpl_harm_postfilter.c}, and {@code smpl_harm_postfilter_util.c}.
 *
 * <p>The MLow byte-exact CELP kernel (the {@code mlow} package's {@code MlowDecoder}) reproduces the native
 * feature-dump decode path ({@code SMPL_DUMP_FEATURES}), which compiles every postfilter out and substitutes
 * a fixed second-order high-pass. The shipping decoder the live WhatsApp client runs takes the postfilter-on
 * branch, which raises the output level and shapes the harmonics in a signal-dependent way a flat output gain
 * only crudely approximates. This package supplies that chain as a stage on top of the postfilter-off kernel:
 * {@code MlowDecodePostfilter} consumes the kernel's pre-postfilter synthesis plus the per-frame decode
 * parameters and returns the postfiltered packet, threading the three postfilters' state across the frames of
 * a packet and across packets of a stream.
 *
 * <p>The three postfilters are an optional LPC postfilter (a pole-zero short-term shaper with a nyquist-tilt
 * gain, the low-rate tilt postfilter's mutually exclusive alternative), a pitch-adaptive high-pass postfilter
 * (a low-frequency de-emphasis whose resonance tracks the pitch), and a harmonic postfilter (a pitch comb run
 * once per packet that reinforces the periodicity through a pitch-tracking low-pass). By default the LPC
 * postfilter is disabled (the native {@code LPC_postfilter_mode} default disables it for the 16 kHz band, and
 * the live client gates it behind the {@code mlow_enable_lpc_postfilter} runtime parameter), so the low-rate
 * tilt is the active short-term shaper and the chain runs the high-pass then the harmonic postfilter; the LPC
 * path is ported and runtime-selectable for completeness.
 *
 * <p>Scope is the SMPL 16 kHz, mono, low-band path with {@code SMPL_LPC_ORDER == 16}. The high-band
 * (32/48 kHz) parametric extension, the neural ML bandwidth-extension enhancement (a default-disabled,
 * external component whose inference and weights are not present in the SMPL decoder source), the
 * comfort-noise injection, and the packet-loss concealment are out of scope.
 *
 * <p>This package is internal to the MLow decode implementation and is intentionally not exported from the
 * module.
 */
package com.github.auties00.cobalt.calls2.media.audio.mlow.postfilter;
