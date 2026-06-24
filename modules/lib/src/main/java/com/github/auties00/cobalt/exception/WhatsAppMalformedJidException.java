package com.github.auties00.cobalt.exception;

import com.github.auties00.cobalt.client.linked.WhatsAppLinkedClientErrorResult;
import com.github.auties00.cobalt.model.jid.Jid;

/**
 * Thrown when a string cannot be parsed as a WhatsApp {@link Jid}.
 *
 * JIDs follow a {@code user@server} shape where the server suffix selects
 * the entity kind ({@code s.whatsapp.net} for a contact, {@code g.us} for
 * a group, {@code newsletter} for a newsletter, and so on). This exception
 * is raised when the input is missing the separator, has an empty
 * component, contains forbidden characters, or names an unknown server
 * suffix.
 *
 * @apiNote
 * Raised against caller-supplied JID strings; catch it locally to reject
 * bad input without affecting the rest of the operation, since only the
 * offending value is rejected. {@link #toErrorResult()} returns
 * {@link WhatsAppLinkedClientErrorResult#DISCARD} so the session keeps running.
 *
 * @implNote
 * This implementation has {@link #toErrorResult()} return
 * {@link WhatsAppLinkedClientErrorResult#DISCARD}: it invalidates only the
 * operation that produced the bad value.
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
     * {@inheritDoc}
     *
     * @implNote
     * This implementation always returns
     * {@link WhatsAppLinkedClientErrorResult#DISCARD}: a JID that fails to parse
     * only invalidates the operation that produced it.
     */
    @Override
    public WhatsAppLinkedClientErrorResult toErrorResult() {
        return WhatsAppLinkedClientErrorResult.DISCARD;
    }
}
