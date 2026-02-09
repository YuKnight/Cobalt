package com.github.auties00.cobalt.message.addon;

/**
 * Use-case types for add-on messages.
 *
 * <p>These are used in the HKDF info parameter to derive different
 * keys for different types of add-ons.  The {@link #value()} string
 * is concatenated into the HKDF info alongside the stanza ID,
 * original sender, and addon sender.
 *
 * @apiNote WAUseCaseSecret.UseCaseSecretModificationType: the exact
 * string values used for key derivation.
 */
public enum MessageAddonType {
    /**
     * Poll vote add-on.
     * Uses AAD: {@code stanzaId + "\0" + voterJid}
     *
     * @apiNote WAUseCaseSecret: POLL_VOTE = "Poll Vote"
     */
    POLL_VOTE("Poll Vote", true),

    /**
     * Encrypted reaction add-on (for CAG groups).
     *
     * @apiNote WAUseCaseSecret: ENC_REACTION = "Enc Reaction"
     */
    ENC_REACTION("Enc Reaction", false),

    /**
     * Encrypted comment add-on.
     *
     * @apiNote WAUseCaseSecret: ENC_COMMENT = "Enc Comment"
     */
    ENC_COMMENT("Enc Comment", false),

    /**
     * Encrypted event response add-on (RSVP).
     * Uses AAD: {@code stanzaId + "\0" + responderJid}
     *
     * @apiNote WAUseCaseSecret: EVENT_RESPONSE = "Event Response"
     */
    EVENT_RESPONSE("Event Response", true),

    /**
     * Encrypted event edit add-on.
     *
     * @apiNote WAUseCaseSecret: EVENT_EDIT_ENCRYPTED = "Event Edit"
     */
    EVENT_EDIT("Event Edit", false),

    /**
     * Encrypted message edit add-on.
     *
     * @apiNote WAUseCaseSecret: MESSAGE_EDIT = "Message Edit"
     */
    MESSAGE_EDIT("Message Edit", false);

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
     *
     * <p>Poll votes and event responses use AAD to cryptographically bind
     * the ciphertext to a specific stanza and sender, preventing
     * substitution attacks.
     *
     * @return true if AAD should be used
     *
     * @apiNote WAWebAddonEncryption function d(): returns AAD string
     * only for PollVote and EventResponse types.
     */
    public boolean usesAad() {
        return usesAad;
    }
}
