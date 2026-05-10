package com.github.auties00.cobalt.message.preview;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.message.text.ExtendedTextMessage;
import com.github.auties00.cobalt.model.message.text.ExtendedTextMessageBuilder;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Per-session cache of resolved link previews keyed by URL.
 *
 * <p>WhatsApp Web keeps two parallel caches: one for regular chats and
 * one for newsletter chats. The two are separated so a preview rendered
 * inside a newsletter (which goes through a server-mediated
 * is-domain-previewable check) is not surfaced into a regular 1:1 chat
 * that would normally render the preview directly. Cobalt mirrors the
 * split because the JS callers consult the right cache before doing
 * any further work.
 *
 * <p>The caches are unbounded {@link ConcurrentMap}s, mirroring the JS
 * {@code Map} usage. Real-world session lifetimes keep memory pressure
 * low; if it ever becomes a concern the implementation can swap in a
 * size-bounded eviction policy without touching the call sites.
 */
@WhatsAppWebModule(moduleName = "WAWebLinkPreviewCache")
final class LinkPreviewCache {
    /**
     * Marker stored when a URL has been resolved but produced no
     * preview, so subsequent lookups short-circuit without re-fetching.
     */
    private static final ExtendedTextMessage NEGATIVE = new ExtendedTextMessageBuilder().build();

    /**
     * Cache for non-newsletter chats.
     */
    private final ConcurrentMap<String, ExtendedTextMessage> regular;

    /**
     * Cache for newsletter chats.
     */
    private final ConcurrentMap<String, ExtendedTextMessage> newsletter;

    /**
     * Creates an empty cache pair.
     */
    LinkPreviewCache() {
        this.regular = new ConcurrentHashMap<>();
        this.newsletter = new ConcurrentHashMap<>();
    }

    /**
     * Returns the cached preview for {@code url} when one is available.
     *
     * @param url            the URL whose preview is requested
     * @param newsletterChat whether the URL was looked up in a
     *                       newsletter chat
     * @return the cached preview, or {@link Optional#empty()} when the
     *         URL has not been resolved yet
     */
    @WhatsAppWebExport(moduleName = "WAWebLinkPreviewCache", exports = "getPreviewCache",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebLinkPreviewCache", exports = "getNewsletterPreviewCache",
            adaptation = WhatsAppAdaptation.ADAPTED)
    Optional<ExtendedTextMessage> get(String url, boolean newsletterChat) {
        var pick = newsletterChat ? newsletter : regular;
        var cached = pick.get(url);
        if (cached == null) {
            return Optional.empty();
        }
        if (cached == NEGATIVE) {
            return Optional.of(NEGATIVE);
        }
        return Optional.of(cached);
    }

    /**
     * Returns whether the negative sentinel was returned by
     * {@link #get(String, boolean)}.
     *
     * @param preview the preview returned from the cache
     * @return {@code true} when {@code preview} is the negative
     *         sentinel
     */
    static boolean isNegative(ExtendedTextMessage preview) {
        return preview == NEGATIVE;
    }

    /**
     * Stores {@code preview} as the resolved value for {@code url}.
     *
     * @param url            the URL being cached
     * @param newsletterChat whether the URL was resolved in a newsletter
     *                       chat
     * @param preview        the preview to cache; {@code null} stores
     *                       the negative sentinel so future lookups
     *                       short-circuit
     */
    void put(String url, boolean newsletterChat, ExtendedTextMessage preview) {
        var pick = newsletterChat ? newsletter : regular;
        pick.put(url, preview != null ? preview : NEGATIVE);
    }

    /**
     * Clears every cached entry, invoked when the user opts out of
     * link previews or the session is recycled.
     */
    @WhatsAppWebExport(moduleName = "WAWebLinkPreviewCache", exports = "clearPreviewCache",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebLinkPreviewCache", exports = "clearNewsletterPreviewCache",
            adaptation = WhatsAppAdaptation.DIRECT)
    void clear() {
        regular.clear();
        newsletter.clear();
    }
}
