package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Collection;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onContactBlacklist onContactBlacklist} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface ContactBlacklistListener extends WhatsAppListener {
    /**
     * Notifies the listener that the per-axis privacy contact
     * blacklist for one category was refreshed against the server.
     *
     * @apiNote
     * Fires each time
     * {@link LinkedWhatsAppClient#refreshContactBlacklist(String, com.github.auties00.cobalt.model.setting.privacy.ContactBlacklistAddressingMode)}
     * commits a fresh server-authoritative view of that category. Use
     * to redraw the privacy settings surface against the new
     * authoritative set.
     *
     * @param whatsapp the client emitting the event
     * @param category the privacy axis category that was refreshed
     * @param blockedContacts the new authoritative entries for that
     *                        category; may be empty
     */
    void onContactBlacklist(LinkedWhatsAppClient whatsapp, String category, Collection<Jid> blockedContacts);
}
