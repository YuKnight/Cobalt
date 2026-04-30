package com.github.auties00.cobalt.exception;

import com.github.auties00.cobalt.model.jid.Jid;

/**
 * Thrown when a string cannot be parsed as a WhatsApp JID.
 *
 * <p>JIDs are the addresses WhatsApp uses to identify users, groups,
 * status broadcasts, newsletters, and similar entities. They follow a
 * {@code user@server} shape where the server suffix selects the entity
 * kind (for example {@code s.whatsapp.net} for a contact,
 * {@code g.us} for a group, {@code newsletter} for a newsletter). When
 * the input is missing the separator, has an empty component, contains
 * forbidden characters, or names an unknown server suffix, this
 * exception is raised.
 *
 * <p>Parsing failures are non-fatal: only the offending value is
 * rejected and the rest of the session is unaffected.
 *
 * @see Jid
 */
public final class WhatsAppMalformedJidException extends WhatsAppException {

    /**
     * Constructs a new malformed JID exception with the specified detail message.
     *
     * @param message the detail message explaining why the JID is malformed
     */
    public WhatsAppMalformedJidException(String message) {
        super(message);
    }

    /**
     * Returns whether the failure invalidates the current session.
     *
     * <p>A JID that fails to parse only invalidates the specific
     * operation that produced it, not the active session.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFatal() {
        return false;
    }
}
