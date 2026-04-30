package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

/**
 * Builds common SMAX stanza child nodes used across newsletter message types.
 *
 * <p>The {@code <plaintext>} payload wrapper is currently the only shared element.
 * Media handles are encoded as the {@code media_id} attribute on the outer
 * {@code <message>} node (per {@code mergeNewsletterMediaPublishMixin}), not as a child.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutMessagePublishPayloadMixin")
public final class NewsletterStanza {
    /**
     * Prevents instantiation of this utility class.
     */
    private NewsletterStanza() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Builds a {@code <plaintext>} node wrapping a serialised payload.
     *
     * @param payload the serialised protobuf bytes
     * @return the plaintext node
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
     * Builds a {@code <plaintext>} node with a {@code mediatype} attribute carrying the
     * SMAX media subtype string (e.g. {@code "image"}, {@code "video"}, {@code "url"}).
     *
     * @param payload   the serialised protobuf bytes
     * @param mediaType the SMAX media subtype, must not be {@code null}
     * @return the plaintext node
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutMessagePublishNewsletterMediaMixin", exports = "applyMixin",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Node buildPlaintext(byte[] payload, String mediaType) {
        return new NodeBuilder()
                .description("plaintext")
                .attribute("mediatype", mediaType)
                .content(payload)
                .build();
    }
}
