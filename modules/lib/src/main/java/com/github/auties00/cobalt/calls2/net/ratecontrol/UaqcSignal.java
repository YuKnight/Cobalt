package com.github.auties00.cobalt.calls2.net.ratecontrol;

/**
 * The three-level congestion verdict each unified-audio-quality-control input signal yields.
 *
 * <p>Each of the packet-loss-ratio, round-trip-time, and receiver-estimated-maximum-bitrate signals is
 * evaluated with hysteresis against a lower and an upper threshold and reduced to one of these levels:
 * a value at or below the lower threshold is {@link #NON_CONGESTED}, a value at or above the upper
 * threshold (or whose trend exceeds the up-slope) is {@link #CONGESTED}, and anything in between is
 * {@link #HOLD}. The {@link UnifiedAudioQualityControl} aggregates the three levels into a quality-state
 * move: any {@link #NON_CONGESTED} signal moves the state toward a better one, otherwise any
 * {@link #CONGESTED} signal moves it toward a worse one, and all {@link #HOLD} keeps the state.
 *
 * @implNote This implementation reproduces the {@code 0/1/2} return of {@code uaqc_eval_plr_signal}
 * (fn3854), {@code uaqc_eval_rtt_signal} (fn3856), and {@code uaqc_eval_remb_signal} (fn3857) in the
 * wa-voip WASM module {@code ff-tScznZ8P} ({@code rate_control/uaqc/uaqc_signals.cc},
 * {@code rev-net-bwe}). The ordinal order matches the native integers so the aggregate comparison ports
 * directly.
 */
public enum UaqcSignal {
    /**
     * The signal is below the lower threshold: the link shows no congestion on this input.
     *
     * <p>A single signal at this level lets the quality state move toward a better state.
     */
    NON_CONGESTED,

    /**
     * The signal sits between the lower and upper thresholds: inconclusive.
     *
     * <p>A signal at this level neither improves nor worsens the quality state on its own.
     */
    HOLD,

    /**
     * The signal is at or above the upper threshold, or its trend exceeds the up-slope: congested.
     *
     * <p>When no signal is {@link #NON_CONGESTED}, a signal at this level moves the quality state toward
     * a worse state.
     */
    CONGESTED
}
