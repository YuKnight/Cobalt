package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.signaling.ScreenShareStanza;

import java.util.Optional;

/**
 * Enumerates the screen-share transition a participant reports.
 *
 * <p>A screen-share event reports a transition in a participant's screen-sharing stream: it begins, it
 * stops, or it fails to start. Each constant binds the {@link #code() numeric state code} carried by the
 * {@code screenshare_state} attribute of the {@code <screen_share>} action and by the screen-share event.
 *
 * @implNote This implementation ports the screen-share state codes the wa-voip WASM module
 * {@code ff-tScznZ8P} passes to {@code send_screen_share_event} ({@code media/screen_share.cc}):
 * {@code 1} started, {@code 2} stopped, {@code 3} failed, the same codes
 * {@link ScreenShareStanza#STATE_STARTED}, {@link ScreenShareStanza#STATE_STOPPED}, and
 * {@link ScreenShareStanza#STATE_FAILED} carry on the wire. The single-stream V2 versus dual-stream V3
 * distinction is a separate {@code version} value, not a state, and is tracked by the screen-share
 * controller rather than by this enum.
 * @see ScreenShareStanza
 */
public enum ScreenShareState {
    /**
     * Represents a screen-share stream that has started.
     */
    STARTED(ScreenShareStanza.STATE_STARTED),

    /**
     * Represents a screen-share stream that has stopped.
     */
    STOPPED(ScreenShareStanza.STATE_STOPPED),

    /**
     * Represents a screen-share stream that failed to start.
     */
    FAILED(ScreenShareStanza.STATE_FAILED);

    /**
     * Holds the numeric state code the wire and the event carry for this transition.
     */
    private final int code;

    /**
     * Constructs a screen-share state constant bound to its numeric code.
     *
     * @param code the numeric state code
     */
    ScreenShareState(int code) {
        this.code = code;
    }

    /**
     * Returns the numeric state code the wire and the event carry for this transition.
     *
     * @return the numeric state code ({@code 1} started, {@code 2} stopped, {@code 3} failed)
     */
    public int code() {
        return code;
    }

    /**
     * Looks up the screen-share state whose {@linkplain #code() code} equals the given value.
     *
     * @param code the numeric state code to resolve
     * @return the matching state, or an empty result when no state carries the code
     */
    public static Optional<ScreenShareState> ofCode(int code) {
        for (var state : values()) {
            if (state.code == code) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
