package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

/**
 * Per-user action enum.
 */
@WhatsAppWebModule(moduleName = "WAWebSetPrivacyJob")
public enum IqSetPrivacyUserAction {
    /**
     * Adds the user to the per-category list.
     */
    ADD("add"),

    /**
     * Removes the user from the per-category list.
     */
    REMOVE("remove");

    /**
     * The wire string emitted in the {@code action} attribute.
     */
    private final String wire;

    /**
     * Constructs a user-action constant.
     *
     * @param wire the wire string. Never {@code null}
     */
    IqSetPrivacyUserAction(String wire) {
        this.wire = wire;
    }

    /**
     * Returns the wire string.
     *
     * @return the wire string. Never {@code null}
     */
    public String wire() {
        return wire;
    }
}
