package com.github.auties00.cobalt.socket;

import com.github.auties00.cobalt.exception.WhatsAppException;
import com.github.auties00.cobalt.node.Node;

/**
 * Receives the events that a {@link WhatsAppSocketClient} surfaces once
 * the Noise handshake has completed.
 *
 * <p>Implementations are notified when an inbound datagram has been
 * decrypted and parsed into a {@link Node}, when a recoverable or fatal
 * error is detected on the connection, and when the channel is closed.
 */
public interface WhatsAppSocketListener {
    /**
     * Receives a decrypted and decoded WhatsApp node.
     *
     * @param node the deserialized node
     */
    void onNode(Node node);

    /**
     * Receives an exception observed while processing inbound traffic
     * or while interacting with the underlying transport.
     *
     * @param exception the error
     */
    void onError(WhatsAppException exception);

    /**
     * Receives notification that the connection has been closed; no
     * further events will be delivered after this call.
     */
    void onClose();
}
