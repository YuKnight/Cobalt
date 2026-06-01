package com.github.auties00.cobalt.client.listener;

/**
 * The common base for every Cobalt event-listener interface.
 *
 * <p>Every per-event {@link FunctionalInterface @FunctionalInterface} in this
 * package (for example {@link NewMessageListener}, {@link ChatsListener},
 * {@link ContactPresenceListener}) extends this interface, as does the
 * aggregator {@link LinkedWhatsAppClientListener}. The single registration
 * surface on the client therefore accepts any of them uniformly while the
 * dispatch layer recovers the concrete event interface through a pattern-match
 * {@code instanceof} guard.
 *
 * <p>The interface declares no methods: it is a typed marker that lets the
 * client store a heterogeneous set of listeners without losing static type
 * information at registration time.
 *
 * @see LinkedWhatsAppClientListener
 */
public interface WhatsAppListener {
}
