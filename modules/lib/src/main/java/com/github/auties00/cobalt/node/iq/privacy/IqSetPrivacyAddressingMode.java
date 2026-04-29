package com.github.auties00.cobalt.node.iq.privacy;

/**
 * Wire-level addressing mode for the user-list payload.
 */
public enum IqSetPrivacyAddressingMode {
    /**
     * The legacy PN-addressed variant — emits a bare {@code <privacy/>}
     * envelope and {@code <user jid=PN_JID/>} children.
     */
    PN,

    /**
     * The migrated LID-addressed variant — emits
     * {@code <privacy addressing_mode="lid">} and {@code <user jid=LID_JID/>}
     * children with optional {@code username} or {@code pn_jid}
     * discriminators.
     */
    LID
}
