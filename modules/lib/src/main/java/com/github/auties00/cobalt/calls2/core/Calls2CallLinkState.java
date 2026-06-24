package com.github.auties00.cobalt.calls2.core;

import java.util.Optional;

/**
 * Enumerates the five sub-states of the group-call link-join handshake.
 *
 * <p>While a call is in {@link Calls2CallState#LINK} the engine tracks a separate link sub-state that
 * advances through the query and join legs of joining a call via a call link. Each constant binds the
 * {@link #wireOrdinal()} the engine stores alongside the call state. This sub-state is only meaningful
 * while the call state is {@link Calls2CallState#LINK}; outside that phase it remains {@link #NONE}.
 *
 * @implNote This implementation ports the call-link-state table at WASM data offset {@code 0x1284fc} in
 * module {@code ff-tScznZ8P} (five entries, stored at {@code call_context[0x28530]} and valid only while
 * {@code call_context[0]} equals the {@code Link} state). A sub-state change drives the engine's
 * link-state event.
 */
public enum Calls2CallLinkState {
    /**
     * Represents no link-join activity: the initial and inactive sub-state.
     */
    NONE(0),

    /**
     * Represents a link query having been sent and its acknowledgement awaited.
     */
    LINK_QUERY_SENT(1),

    /**
     * Represents a link query having been acknowledged.
     */
    LINK_QUERY_ACKED(2),

    /**
     * Represents a link join having been sent and its acknowledgement awaited.
     */
    LINK_JOIN_SENT(3),

    /**
     * Represents a link join having been acknowledged, completing the handshake.
     */
    LINK_JOIN_ACKED(4);

    /**
     * Holds the numeric sub-state value the engine stores in the call context.
     */
    private final int wireOrdinal;

    /**
     * Constructs a constant bound to its native sub-state value.
     *
     * @param wireOrdinal the numeric sub-state value stored in the call context
     */
    Calls2CallLinkState(int wireOrdinal) {
        this.wireOrdinal = wireOrdinal;
    }

    /**
     * Returns the numeric sub-state value the engine stores in the call context.
     *
     * @return the native sub-state value, in the range {@code 0} to {@code 4}
     */
    public int wireOrdinal() {
        return wireOrdinal;
    }

    /**
     * Looks up the link sub-state for a native sub-state value.
     *
     * <p>The lookup is keyed on the protocol value. A value outside the range {@code 0} to {@code 4}
     * yields an empty result.
     *
     * @param wireOrdinal the native sub-state value
     * @return the matching sub-state, or an empty result when the value is out of range
     */
    public static Optional<Calls2CallLinkState> ofWireOrdinal(int wireOrdinal) {
        if (wireOrdinal < 0 || wireOrdinal >= values().length) {
            return Optional.empty();
        }
        return Optional.of(values()[wireOrdinal]);
    }
}
