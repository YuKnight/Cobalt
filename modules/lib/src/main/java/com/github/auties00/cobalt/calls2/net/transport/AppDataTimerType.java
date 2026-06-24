package com.github.auties00.cobalt.calls2.net.transport;

/**
 * The three self-clearing timers an {@link AppDataController} arms to manage the in-call reaction
 * side-channel.
 *
 * <p>Each constant carries the native type tag the call object stamps into its timer slot so a fired
 * callback can identify which housekeeping action to run: clearing the outbound reaction buffer,
 * expiring aged inbound per-participant reactions, or retransmitting unacknowledged app-data. The tag
 * values are the wire-stable discriminators the engine uses, not arbitrary ordinals, so they are kept
 * explicit and looked up through {@link #ofTag(int)} when a timer fires.
 *
 * @implNote This implementation reproduces the three timer type tags wired by
 * {@code wa_app_data_controller_create} (fn11747) of the wa-voip WASM module {@code ff-tScznZ8P}
 * ({@code system/src/transport/app_data_controller.cc}), which registers timer slots at call offsets
 * {@code +0x248}, {@code +0x258}, and {@code +0x268} with type tags {@code 1}, {@code 2}, and
 * {@code 3} and the callback function pointers {@code DAT_1f02}/{@code DAT_1f03}/{@code DAT_1f04}
 * ({@code tx_reaction_clear} fn11749, {@code rx_reaction_clear} fn11750, {@code reaction_retransmission}).
 * The tag is read from the timer slot field at {@code slot+0x04} to dispatch the matching callback.
 */
public enum AppDataTimerType {
    /**
     * Clears the outbound reaction buffer so a recently sent reaction stops being retransmitted.
     *
     * <p>Fired once after a reaction is sent; the callback zeroes the controller's transmit reaction
     * state, matching the native {@code tx_reaction_clear} timer.
     */
    TX_REACTION_CLEAR(1),

    /**
     * Expires aged inbound reactions held per participant so a displayed reaction overlay is taken
     * down after its lifetime.
     *
     * <p>Re-arms itself on each fire to keep sweeping the participant reaction records, matching the
     * native {@code rx_reaction_clear} timer.
     */
    RX_REACTION_CLEAR(2),

    /**
     * Retransmits the most recent outbound app-data so a reaction survives a single lost packet on the
     * unreliable relay path.
     *
     * <p>Armed only when reaction retransmission is enabled, matching the native
     * {@code reaction_retransmission} timer.
     */
    REACTION_RETRANSMISSION(3);

    /**
     * Holds the native timer type tag this constant maps to.
     */
    private final int tag;

    /**
     * Constructs a timer type bound to its native type tag.
     *
     * @param tag the type tag stamped into the call object's timer slot for this timer
     */
    AppDataTimerType(int tag) {
        this.tag = tag;
    }

    /**
     * Returns the native timer type tag this constant maps to.
     *
     * <p>The tag is the value stored at {@code slot+0x04} in the native call object's timer slot and is
     * stable across versions, so it may be persisted or logged.
     *
     * @return the type tag, one of {@code 1}, {@code 2}, or {@code 3}
     */
    public int tag() {
        return tag;
    }

    /**
     * Resolves the timer type carrying the given native type tag.
     *
     * <p>Used when a fired timer reports only its slot tag and the controller must recover which
     * housekeeping action to run.
     *
     * @param tag the native type tag read from a timer slot
     * @return the matching timer type
     * @throws IllegalArgumentException if no timer type carries {@code tag}
     */
    public static AppDataTimerType ofTag(int tag) {
        for (var type : values()) {
            if (type.tag == tag) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown app-data timer tag: " + tag);
    }
}
