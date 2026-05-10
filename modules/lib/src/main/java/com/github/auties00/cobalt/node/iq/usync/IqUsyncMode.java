package com.github.auties00.cobalt.node.iq.usync;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

/**
 * The {@code mode} attribute carried by the outbound
 * {@code <usync/>} envelope. Drives the relay's per-protocol
 * caching decisions: {@link #QUERY} fetches state, {@link #DELTA}
 * asks the relay to return only the entries that have changed
 * since the prior query, and {@link #FULL} forces a complete
 * refresh ignoring any caches.
 */
@WhatsAppWebModule(moduleName = "WAWebUsync")
public enum IqUsyncMode {
    /**
     * The default {@code "query"} mode. Fetches state for the
     * requested protocols.
     */
    QUERY("query"),
    /**
     * The {@code "delta"} mode. Fetches only the entries that
     * have changed since the prior query.
     */
    DELTA("delta"),
    /**
     * The {@code "full"} mode. Forces a complete refresh
     * ignoring any caches.
     */
    FULL("full");

    /**
     * The literal wire-side string emitted on the {@code mode}
     * attribute.
     */
    private final String wireValue;

    /**
     * Constructs a new mode entry.
     *
     * @param wireValue the wire-side string
     */
    IqUsyncMode(String wireValue) {
        this.wireValue = wireValue;
    }

    /**
     * Returns the wire-side string.
     *
     * @return the wire string. Never {@code null}
     */
    public String wireValue() {
        return wireValue;
    }
}
