package com.github.auties00.cobalt.node.smax.privacy;

/**
 * The outbound action to perform on the target JID.
 */
public enum SmaxUpdateBlockListAction {
    /**
     * Add the target JID to the blocklist.
     */
    BLOCK("block"),

    /**
     * Remove the target JID from the blocklist.
     */
    UNBLOCK("unblock");

    /**
     * The wire string for this action.
     */
    private final String wire;

    /**
     * Constructs an action enum constant.
     *
     * @param wire the wire string
     */
    SmaxUpdateBlockListAction(String wire) {
        this.wire = wire;
    }

    /**
     * Returns the wire string.
     *
     * @return the wire string; never {@code null}
     */
    public String wire() {
        return wire;
    }
}
