package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.newsletter.NewsletterMessageInfo;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

/**
 * Builds common SMAX stanza child nodes used across newsletter
 * message types.
 *
 * @apiNote WASmaxOutMessagePublishPayloadMixin: wraps the serialised
 * protobuf in a {@code <plaintext>} node.
 * WASmaxOutMessagePublishNewsletterMediaPublishMixin: includes
 * {@code <media_id>handle</media_id>} when a media handle is available.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMessagePublishPayloadMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutMessagePublishNewsletterMediaPublishMixin")
public final class NewsletterStanza {
    /**
     * Prevents instantiation of this utility class.
     */
    private NewsletterStanza() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Builds the {@code <plaintext>} node wrapping a serialised payload.
     *
     * @param payload the serialised protobuf bytes
     * @return the plaintext node
     *
     * @apiNote WASmaxOutMessagePublishPayloadMixin
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutMessagePublishPayloadMixin", exports = "applyMixin",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Node buildPlaintext(byte[] payload) {
        return new NodeBuilder()
                .description("plaintext")
                .content(payload)
                .build();
    }

    /**
     * Builds the {@code <plaintext>} node with a mediatype attribute.
     *
     * @param payload   the serialised protobuf bytes
     * @param mediaType the SMAX media type, or {@code null} for text
     * @return the plaintext node
     *
     * @apiNote WASmaxOutMessagePublishNewsletterMediaPublishMixin:
     * includes {@code mediatype} attribute on the plaintext node.
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutMessagePublishNewsletterMediaPublishMixin", exports = "applyMixin",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Node buildPlaintext(byte[] payload, String mediaType) {
        return new NodeBuilder()
                .description("plaintext")
                .attribute("mediatype", mediaType)
                .content(payload)
                .build();
    }

    /**
     * Builds the {@code <media_id>} node from the newsletter message
     * info's media handle, or returns {@code null} if absent.
     *
     * @param info the newsletter message info
     * @return the media_id node, or {@code null}
     *
     * @apiNote WASmaxOutMessagePublishNewsletterMediaPublishMixin
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutMessagePublishNewsletterMediaPublishMixin", exports = "applyMixin",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Node buildMediaId(NewsletterMessageInfo info) {
        return info.mediaHandle()
                .map(NewsletterStanza::buildMediaId)
                .orElse(null);
    }

    /**
     * Builds a {@code <media_id>} node from a literal handle string.
     *
     * @param handle the media handle
     * @return the media_id node
     *
     * @implNote WASmaxOutMessagePublishNewsletterMediaPublishMixin:
     * emits {@code <media_id>handle</media_id>} as a child of the
     * {@code <message>} stanza.
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutMessagePublishNewsletterMediaPublishMixin", exports = "applyMixin",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static Node buildMediaId(String handle) {
        return new NodeBuilder()
                .description("media_id")
                .content(handle)
                .build();
    }
}
