package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

/**
 * Closed set of token-type discriminators recognised by the relay.
 */
@WhatsAppWebModule(moduleName = "WAWebSetPrivacyTokensJob")
public enum IqSetPrivacyTokensTokenType {
    /**
     * The {@code trusted_contact} token type. Grants the recipient
     * trusted-contact status for call/messages gating.
     */
    TRUSTED_CONTACT("trusted_contact");

    /**
     * The wire string emitted in the {@code type} attribute.
     */
    private final String wire;

    /**
     * Constructs a token-type constant.
     *
     * @param wire the wire string. Never {@code null}
     */
    IqSetPrivacyTokensTokenType(String wire) {
        this.wire = wire;
    }

    /**
     * Returns the wire string for this token type.
     *
     * @return the wire string. Never {@code null}
     */
    public String wire() {
        return wire;
    }
}
