package com.github.auties00.cobalt.calls2.net.bwe;

/**
 * Classifies the delay-based congestion state of a link as one of the three states the GoogCC
 * over-use detector emits.
 *
 * <p>The {@link TrendlineEstimator} produces one of these values per packet group by comparing the
 * smoothed inter-arrival delay trend against an adaptive threshold: a sustained positive trend means
 * the bottleneck queue is filling ({@link #OVERUSING}), a sustained negative trend means it is
 * draining ({@link #UNDERUSING}), and anything in between means the link is in equilibrium
 * ({@link #NORMAL}). The {@link AimdRateControl} consumes the classification to drive its
 * multiplicative-decrease, additive-increase, and hold transitions.
 *
 * @implNote This implementation mirrors WebRTC's {@code BandwidthUsage} enum
 * ({@code kBwNormal}/{@code kBwUnderusing}/{@code kBwOverusing}) used by
 * {@code modules/congestion_controller/goog_cc/trendline_estimator.cc}, reached in the wa-voip engine
 * through {@code bwe/bwe_webrtc_delay_based.cc} (fn6193). The ordinal order matches WebRTC so the
 * decision-table branches port directly.
 */
public enum BandwidthUsage {
    /**
     * The link is in equilibrium: the smoothed delay trend stays within the adaptive threshold band.
     *
     * <p>The rate controller is free to probe upward (additive or multiplicative increase) while in
     * this state.
     */
    NORMAL,

    /**
     * The bottleneck queue is draining: the smoothed delay trend is below the negated adaptive
     * threshold.
     *
     * <p>The rate controller holds the current estimate rather than increasing, because a falling
     * queue often follows a recent decrease and an immediate ramp would re-congest the link.
     */
    UNDERUSING,

    /**
     * The bottleneck queue is filling: the smoothed delay trend stays above the adaptive threshold for
     * at least the over-using time threshold.
     *
     * <p>The rate controller applies a multiplicative decrease in response to this state.
     */
    OVERUSING
}
