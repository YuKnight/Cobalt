package com.github.auties00.cobalt.exception;

import com.github.auties00.cobalt.model.jid.Jid;

/**
 * Exception thrown when a JID (Jabber Identifier) cannot be parsed or is malformed.
 * <p>
 * JIDs are the unique identifiers used in WhatsApp to identify users, groups, broadcasts,
 * and other entities. This exception is thrown when a string cannot be parsed into a valid
 * JID structure.
 *
 * <h2>JID Format</h2>
 * A valid WhatsApp JID typically follows the format:
 * <pre>user@server</pre>
 * Where:
 * <ul>
 *   <li><b>user:</b> The local part identifying the entity (phone number, group ID, etc.)</li>
 *   <li><b>server:</b> The server suffix indicating the entity type</li>
 * </ul>
 *
 * <h2>Server Types</h2>
 * Common server suffixes include:
 * <ul>
 *   <li>{@code s.whatsapp.net} - User accounts (phone number based)</li>
 *   <li>{@code g.us} - Group chats</li>
 *   <li>{@code broadcast} - Broadcast lists</li>
 *   <li>{@code status@broadcast} - Status updates</li>
 *   <li>{@code lid} - LID-based identifiers (privacy-enhanced IDs)</li>
 *   <li>{@code c.us} - Business catalogs</li>
 *   <li>{@code newsletter} - Newsletter/channel identifiers</li>
 * </ul>
 *
 * <h2>Possible Causes</h2>
 * <ul>
 *   <li><b>Invalid format:</b> Missing '@' separator or incorrect structure</li>
 *   <li><b>Empty components:</b> Empty user or server parts</li>
 *   <li><b>Invalid characters:</b> Characters not allowed in JID format</li>
 *   <li><b>Unknown server:</b> Unrecognized server suffix</li>
 *   <li><b>Numeric overflow:</b> Phone numbers that exceed numeric range</li>
 * </ul>
 *
 * <h2>Fatality</h2>
 * Malformed JID exceptions are non-fatal. The error affects only the specific JID
 * being parsed and does not impact the client session.
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
     * Returns whether this exception represents a fatal error.
     * <p>
     * Malformed JID exceptions are non-fatal as they only affect the specific
     * JID being parsed and don't impact the overall client session.
     *
     * @return {@code false}
     */
    @Override
    public boolean isFatal() {
        return false;
    }
}
