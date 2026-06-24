package com.github.auties00.cobalt.calls2.net.bwe;

/**
 * Names the two phases of the remote-picture-in-picture second-stage combine the {@link BitrateCombiner}
 * runs when {@link CombineMode#REMOTE_PIP} is active.
 *
 * <p>A new combine begins in {@link #ENTER}, where the combined estimate ramps toward the remote
 * receiver estimate and the packet-pair link capacity. It converts to {@link #THROTTLE} once enough
 * time has passed or the remote estimate has grown large enough, after which the second stage applies
 * the steadier throttled fusion. The transition is one-way for the life of the combine.
 *
 * @implNote This implementation models the {@code kRemotePipEnter} / {@code kRemotePipThrottle} phase
 * field used by {@code tfrc_combine_bitrate_estimates} (fn4364) in the wa-voip engine
 * ({@code bwe/tfrc_sender_bwe_bitrate_update.cc}); the recovered log reads "After 7.5s or received
 * 200kbps REMB, convert phase from kRemotePipEnter to kRemotePipThrottle"
 * (re/calls2-spec/SPEC.md sec 15.3).
 */
public enum RemotePipPhase {
    /**
     * The entering phase: the combined estimate ramps toward the remote estimate and link capacity.
     */
    ENTER,

    /**
     * The throttled phase: the second stage applies the steadier fusion after the entering phase ends.
     */
    THROTTLE
}
