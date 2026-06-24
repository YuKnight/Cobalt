package com.github.auties00.cobalt.calls2.core.control;

import com.github.auties00.cobalt.calls2.signaling.LinkQueryStanza;

import java.util.Optional;

/**
 * Enumerates the action verb a call-link query carries to select what the relay resolves.
 *
 * <p>A call-link query asks the relay to expand a link token, and the {@code action} verb tells the relay
 * which view of the link to return: a passive {@link #PREVIEW} for a user who is about to join, or a
 * {@link #LINK_EDIT} lookup for the link owner who is about to change the link's configuration. Each
 * constant binds the {@link #wireValue() wire literal} the query stamps into its {@code action}
 * attribute.
 *
 * @implNote This implementation ports the two {@code action} literals the wa-voip WASM module
 * {@code ff-tScznZ8P} writes into the {@code <link_query>} {@code action} attribute (shared data offset
 * {@code 0x56cc3}) in {@code serialize_link_query} ({@code call_link.cc}): {@code preview} and
 * {@code link_edit}. The verb is carried by {@link LinkQueryStanza#action()} as a raw string; this enum is
 * the typed control-layer view a controller selects with, mapped to and from that string by
 * {@link #wireValue()} and {@link #ofWire(String)}.
 * @see LinkQueryStanza
 */
public enum CallLinkQueryAction {
    /**
     * Represents a passive preview lookup a user issues before joining a call link.
     */
    PREVIEW("preview"),

    /**
     * Represents an edit lookup the link owner issues before changing the link configuration.
     */
    LINK_EDIT("link_edit");

    /**
     * Holds the wire literal this action stamps into the {@code action} attribute.
     */
    private final String wireValue;

    /**
     * Constructs a query-action constant bound to its wire literal.
     *
     * @param wireValue the literal stamped into the {@code action} attribute
     */
    CallLinkQueryAction(String wireValue) {
        this.wireValue = wireValue;
    }

    /**
     * Returns the wire literal this action stamps into the {@code action} attribute.
     *
     * @return the {@code action} attribute literal; never {@code null}
     */
    public String wireValue() {
        return wireValue;
    }

    /**
     * Looks up the query action whose {@linkplain #wireValue() wire literal} equals the given value.
     *
     * @param wireValue the {@code action} attribute literal to resolve, or {@code null}
     * @return the matching query action, or an empty result when the literal names no action
     */
    public static Optional<CallLinkQueryAction> ofWire(String wireValue) {
        for (var action : values()) {
            if (action.wireValue.equals(wireValue)) {
                return Optional.of(action);
            }
        }
        return Optional.empty();
    }
}
