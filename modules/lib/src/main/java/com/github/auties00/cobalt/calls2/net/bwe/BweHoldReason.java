package com.github.auties00.cobalt.calls2.net.bwe;

/**
 * Enumerates the reasons the {@link BweHoldController} freezes the bandwidth-estimate target, each
 * carrying the bitmask bit the recovered hold machine uses.
 *
 * <p>The hold machine shares one hold slot across all reasons: entering any reason freezes the target
 * and stamps a per-reason start timer, so the controller can report how long each reason has held and
 * end them independently. The reasons are congestion (a clamp-down detected congestion), init (the
 * conservative startup hold), mid-call probing, additive probing, receive-drop (the sender estimate
 * fell to the receiver estimate), and reinforcement-learning congestion.
 *
 * @implNote This implementation enumerates the reason bitmask passed to {@code tfrc_start_bwe_hold}
 * (fn4428) and stored at offset {@code +0xc140} in the wa-voip engine
 * ({@code bwe/tfrc_sender_bwe_bitrate_update.cc}); the bit values are the recovered literals
 * (re/calls2-spec/SPEC.md sec 15.4).
 */
public enum BweHoldReason {
    /**
     * A clamp-down detected congestion; the target is frozen to avoid oscillation after the drop.
     */
    CONGESTION(0x01),

    /**
     * The conservative startup hold while the initial estimate settles.
     */
    INIT(0x02),

    /**
     * Mid-call probing is in progress; the target is frozen until the probe resolves.
     */
    MCP(0x04),

    /**
     * Additive probing is in progress; the target is frozen until the probe resolves.
     */
    ADDITIVE(0x08),

    /**
     * The sender estimate fell to or below the receiver estimate; the target is frozen pending
     * recovery.
     */
    RECV_DROP(0x10),

    /**
     * A reinforcement-learning model reported congestion; the target is frozen while the model holds.
     */
    RL(0x20);

    /**
     * The bitmask bit the recovered hold machine assigns to this reason.
     */
    private final int mask;

    /**
     * Constructs a hold reason bound to its bitmask bit.
     *
     * @param mask the bitmask bit
     */
    BweHoldReason(int mask) {
        this.mask = mask;
    }

    /**
     * Returns the bitmask bit the recovered hold machine assigns to this reason.
     *
     * @return the bitmask bit
     */
    public int mask() {
        return mask;
    }
}
