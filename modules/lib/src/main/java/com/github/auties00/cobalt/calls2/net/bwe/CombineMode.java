package com.github.auties00.cobalt.calls2.net.bwe;

/**
 * Selects how the {@link BitrateCombiner} fuses the sender-side estimate with the remote receiver
 * estimate into one combined target.
 *
 * <p>The mode is a configuration value carried in the voip parameters and read per call. The first
 * three modes are the plain WebRTC-style fusions ({@link #MIN}, {@link #MAX}, {@link #AVG}); the
 * remaining five are WhatsApp extensions that latch on inflection points, gate on the remote estimate
 * crossing a threshold, blend toward the sender estimate with an exponential moving average, or route
 * into the early-congestion detector. Each constant carries the integer the voip parameter uses so the
 * decode is a direct ordinal lookup.
 *
 * @implNote This implementation enumerates the {@code combine_mode} values read at voip-params offset
 * {@code +0xac} by {@code tfrc_combine_bitrate_estimates} (fn4364) in the wa-voip engine
 * ({@code bwe/tfrc_sender_bwe_bitrate_update.cc}); modes 3 through 7 are WhatsApp-specific extensions
 * of WebRTC's min-combine (re/calls2-spec/SPEC.md sec 15.3). The captured {@code voip_settings} blob
 * (re/calls2-spec/captures) sets {@code sbwe_combine_policy=3}, selecting {@link #MIN_FLOOR}.
 */
public enum CombineMode {
    /**
     * Takes the minimum of the sender and remote estimates.
     */
    MIN(0),

    /**
     * Takes the maximum of the sender and remote estimates.
     */
    MAX(1),

    /**
     * Takes the arithmetic mean of the sender and remote estimates.
     */
    AVG(2),

    /**
     * Takes the minimum with an inflection-point floor, the value the captured configuration selects.
     *
     * <p>Uses the sender / remote inflection latch so that once the sender estimate has crossed the
     * remote estimate the combined value does not drop below the latched floor.
     */
    MIN_FLOOR(3),

    /**
     * Routes the combine into the remote-picture-in-picture second stage gated by
     * {@link RemotePipPhase}.
     */
    REMOTE_PIP(4),

    /**
     * Averages only when the remote estimate is at or above the remote-pip enter threshold and no
     * inflection has latched, otherwise holds.
     */
    AVG_GATED(5),

    /**
     * Blends the combined value toward the sender estimate with an exponential moving average once the
     * inflection latch is set.
     */
    EMA_BLEND(6),

    /**
     * Routes the combine into the early-congestion-detect path that trusts the receiver estimate.
     */
    EARLY_CONGESTION(7);

    /**
     * The integer the voip parameter uses to select this mode.
     */
    private final int value;

    /**
     * Constructs a combine mode bound to its voip-parameter integer.
     *
     * @param value the selector integer
     */
    CombineMode(int value) {
        this.value = value;
    }

    /**
     * Returns the integer the voip parameter uses to select this mode.
     *
     * @return the selector integer
     */
    public int value() {
        return value;
    }

    /**
     * Returns the combine mode for a voip-parameter selector integer, falling back to {@link #MIN_FLOOR}
     * for an unrecognized value.
     *
     * <p>{@link #MIN_FLOOR} is the fallback because it is the value the captured live configuration
     * selects, so an unknown selector degrades to the observed default rather than to a more or less
     * aggressive fusion.
     *
     * @param value the selector integer read from the voip parameters
     * @return the matching mode, or {@link #MIN_FLOOR} when none matches
     */
    public static CombineMode ofValue(int value) {
        for (var mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        return MIN_FLOOR;
    }
}
