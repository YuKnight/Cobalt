package com.github.auties00.cobalt.client.listener;

import com.github.auties00.cobalt.call.CallEndReason;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;

/**
 * A functional interface for the {@link LinkedWhatsAppClientListener#onCallEnded onCallEnded} event.
 *
 * <p>{@link LinkedWhatsAppClientListener} extends this interface and supplies an empty
 * default implementation, so the event can also be observed in isolation as a
 * lambda.
 *
 * @see LinkedWhatsAppClientListener
 */
@FunctionalInterface
public interface CallEndedListener extends WhatsAppListener {
    /**
     * Notifies the listener that a call has terminated.
     *
     * <p>The {@code reason} carries the cause the peer supplied (for
     * example a timeout or a hangup); it is {@link CallEndReason#UNKNOWN}
     * when the peer did not supply one or the cause was unrecognised.
     *
     * @param whatsapp the client emitting the event
     * @param callId   the identifier of the call that ended
     * @param fromJid  the JID of the party that ended the call
     * @param reason   the parsed reason; {@link CallEndReason#UNKNOWN}
     *                 if the peer did not supply one or the wire literal
     *                 was unrecognised
     */
    void onCallEnded(LinkedWhatsAppClient whatsapp, String callId, Jid fromJid, CallEndReason reason);
}
