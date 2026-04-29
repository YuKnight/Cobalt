package com.github.auties00.cobalt.node.iq.push;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

/**
 * The Windows-store distribution ring controlling which {@code version}
 * attribute is sent on the wire.
 *
 * @implNote {@code WAWebSetWindowsPushConfig} local helper {@code m(e)}
 *           switches on the literal ring name — {@code "uwp_public"}
 *           drops the attribute (relay default), every other ring is
 *           routed verbatim. Cobalt models the four documented rings
 *           explicitly and exposes the wire-string mapping via
 *           {@link #wireValue()}.
 */
@WhatsAppWebModule(moduleName = "WAWebSetWindowsPushConfig")
public enum IqSetWindowsPushConfigRing {
    /**
     * The internal hybrid-dogfooding ring — wire value
     * {@code "uwp_hybrid_dogfooding"}.
     */
    HYBRID_DOGFOODING("uwp_hybrid_dogfooding"),
    /**
     * The pre-release alpha ring — wire value {@code "uwp_alpha"}.
     */
    ALPHA("uwp_alpha"),
    /**
     * The beta ring — wire value {@code "uwp_beta"}; this is also
     * the WA Web fallback when an unrecognised ring name is supplied.
     */
    BETA("uwp_beta"),
    /**
     * The public-release ring — the relay treats the missing
     * attribute as the public ring, so {@link #wireValue()} returns
     * {@code null} for this case.
     */
    PUBLIC(null);

    /**
     * The literal wire-side string emitted on the {@code version}
     * attribute, or {@code null} when the attribute should be
     * omitted entirely.
     */
    private final String wireValue;

    /**
     * Constructs a new ring entry.
     *
     * @param wireValue the wire-side string, or {@code null} for the
     *                  attribute-omitted case
     */
    IqSetWindowsPushConfigRing(String wireValue) {
        this.wireValue = wireValue;
    }

    /**
     * Returns the wire-side string, or {@code null} when the ring
     * is encoded as the missing-attribute default.
     *
     * @return the wire string, or {@code null}
     */
    public String wireValue() {
        return wireValue;
    }
}
