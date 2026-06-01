package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.call.CallLink;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onCallLinkDenied onCallLinkDenied} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface CallLinkDeniedListener extends WhatsAppListener {
    /**
     * Notifies the listener that the host of a call-link declined
     * the local user's join request; terminal for that link
     * attempt.
     *
     * @param whatsapp the client emitting the event
     * @param link     the link that was denied
     */
    void onCallLinkDenied(LinkedWhatsAppClient whatsapp, CallLink link);
}
