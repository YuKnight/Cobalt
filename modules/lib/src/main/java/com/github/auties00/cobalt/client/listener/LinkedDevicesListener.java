package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Collection;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onLinkedDevices onLinkedDevices} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface LinkedDevicesListener extends WhatsAppListener {
    /**
     * Notifies the listener that the list of devices linked to this
     * account was refreshed against the server.
     *
     * @apiNote
     * Fires each time {@link LinkedWhatsAppClient#refreshLinkedDevices()}
     * commits a fresh server-authoritative copy of the paired-device
     * list. Use to redraw the Linked Devices settings surface.
     *
     * @param whatsapp       the client emitting the event
     * @param linkedDevices  the new authoritative set of device JIDs;
     *                       may be empty
     */
    void onLinkedDevices(LinkedWhatsAppClient whatsapp, Collection<Jid> linkedDevices);
}
