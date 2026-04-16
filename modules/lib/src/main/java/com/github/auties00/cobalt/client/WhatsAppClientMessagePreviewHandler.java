package com.github.auties00.cobalt.client;

import com.github.auties00.cobalt.model.message.Message;

/**
 * A hook that decorates outgoing messages with link previews before they
 * are handed off to the send pipeline.
 *
 * <p>When a user sends a message that contains a URL, WhatsApp normally
 * attaches a preview card (title, description, thumbnail) derived from the
 * target page's metadata. This interface lets the integrator control that
 * behaviour: implementations may fetch previews synchronously, attach
 * cached previews, infer a preview from the message content, or skip the
 * preview step entirely.
 *
 * <p>Use {@link #enabled(boolean)} to opt into preview generation (with an
 * optional inference flag) or {@link #disabled()} to suppress it.
 *
 * @see WhatsAppClientBuilder.Options#messagePreviewHandler(WhatsAppClientMessagePreviewHandler)
 */
// TODO: wire up the preview pipeline; the default implementations below are stubs.
@FunctionalInterface
public interface WhatsAppClientMessagePreviewHandler {
    /**
     * Returns a handler that enables preview generation with the given
     * inference policy.
     *
     * <p>When {@code allowInference} is {@code true}, the handler is allowed
     * to synthesise a preview from the message text even if the URL itself
     * cannot be fetched; when {@code false}, only previews derived from a
     * live fetch of the target page are attached.
     *
     * @param allowInference whether inference from message content is allowed
     * @return a no-op handler placeholder that will be replaced by the real
     *         preview pipeline
     */
    static WhatsAppClientMessagePreviewHandler enabled(boolean allowInference) {
        return message -> {};
    }

    /**
     * Returns a handler that suppresses all preview generation.
     *
     * <p>Outgoing messages that contain URLs are sent verbatim with no
     * preview card attached.
     *
     * @return a no-op handler that leaves every message untouched
     */
    static WhatsAppClientMessagePreviewHandler disabled() {
        return _ -> {};
    }

    /**
     * Decorates the given outgoing message with a link preview if
     * appropriate.
     *
     * <p>Implementations are expected to mutate the supplied {@link Message}
     * in place to attach preview metadata; returning is not required.
     * Implementations may block on network I/O because Cobalt calls this
     * method on a virtual thread.
     *
     * @param message the outgoing message to inspect and optionally enrich
     */
    void attribute(Message message);
}
