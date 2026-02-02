package com.github.auties00.cobalt.message.addon;

/**
 * Use-case types for add-on messages.
 * These are used in the HKDF info parameter to derive different keys
 * for different types of add-ons.
 */
public enum MessageAddonType {
    /**
     * Poll vote add-on.
     * Uses AAD (Additional Authenticated Data) for binding: stanzaId + "\0" + voterJid
     */
    POLL_VOTE("Poll Vote", true),

    /**
     * Encrypted reaction add-on (for CAG).
     * Does not use AAD.
     */
    ENC_REACTION("Enc Reaction", false);

    private final String value;
    private final boolean usesAad;

    MessageAddonType(String value, boolean usesAad) {
        this.value = value;
        this.usesAad = usesAad;
    }

    public String value() {
        return value;
    }

    /**
     * Returns whether this use-case type requires AAD in AES-GCM encryption.
     * Poll votes use AAD to cryptographically bind the vote to a specific
     * stanza and voter, preventing vote substitution attacks.
     *
     * @return true if AAD should be used
     */
    public boolean usesAad() {
        return usesAad;
    }
}
