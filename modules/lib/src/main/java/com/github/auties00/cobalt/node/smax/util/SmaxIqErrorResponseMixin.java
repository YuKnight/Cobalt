package com.github.auties00.cobalt.node.smax.util;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;

import java.util.Objects;
import java.util.Optional;

/**
 * Shared envelope-validation helper for the standard
 * {@code <iq from id type="error">} reply produced by every domain's SMAX
 * {@code ClientError}/{@code ServerError}-shape response variant.
 *
 * <p>The class mirrors {@link SmaxIqResultResponseMixin} but for the error
 * envelope: it validates the {@code iq} tag, the echoed {@code id}, the
 * echoed {@code from}/{@code to}, and asserts {@code type="error"}; it also
 * exposes a companion {@link #parseError(Node)} that extracts the
 * {@code <error code="…" text="…"/>} child carried by every error
 * envelope.
 *
 * @implNote {@code WASmaxInGroupsIQErrorResponseMixin.parseIQErrorResponseMixin}
 *           is the per-RPC analogue; Cobalt collapses every copy into the
 *           single helper here. The {@code parseError} extraction is
 *           lifted out separately because {@code ClientError} variants
 *           combine the envelope check with a per-RPC client-error-code
 *           enum lookup, while {@code ServerError} variants only need the
 *           generic {@code <error/>} child.
 */
@WhatsAppWebModule(moduleName = "WASmaxInGroupsIQErrorResponseMixin")
public final class SmaxIqErrorResponseMixin {

    /**
     * Private constructor — the class is a static-only utility.
     */
    private SmaxIqErrorResponseMixin() {
        throw new AssertionError("SmaxIqErrorResponseMixin cannot be instantiated");
    }

    /**
     * Validates that the supplied reply is a well-formed
     * {@code <iq type="error">} echoing the request's {@code id} and
     * {@code to} attributes.
     *
     * @param reply   the inbound stanza received from the relay; never
     *                {@code null}
     * @param request the outbound stanza emitted by the caller — used to
     *                cross-check the echoed {@code id} and {@code from}
     *                attributes; never {@code null}
     * @return {@code true} when {@code reply} is an error envelope echoing
     *         {@code request}'s identifiers; {@code false} otherwise
     * @throws NullPointerException if either argument is {@code null}
     *
     * @implNote {@code WASmaxInGroupsIQErrorResponseMixin.parseIQErrorResponseMixin}
     *           runs the same checks; Cobalt collapses the structured
     *           projection to a boolean.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsIQErrorResponseMixin",
            exports = "parseIQErrorResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public static boolean validate(Node reply, Node request) {
        Objects.requireNonNull(reply, "reply cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        if (!reply.hasDescription("iq")) {
            return false;
        }
        if (!reply.hasAttribute("type", "error")) {
            return false;
        }
        var requestId = request.getAttributeAsString("id").orElse(null);
        if (requestId == null) {
            return false;
        }
        if (!reply.hasAttribute("id", requestId)) {
            return false;
        }
        var requestTo = request.getAttributeAsString("to").orElse(null);
        if (requestTo != null && !reply.hasAttribute("from", requestTo)) {
            return false;
        }
        return true;
    }

    /**
     * Extracts the {@code <error code="…" text="…"/>} child carried by an
     * {@code <iq type="error">} envelope.
     *
     * <p>Returns {@link Optional#empty()} when the reply has no
     * {@code <error/>} child or when the child is malformed (missing
     * {@code code} attribute).
     *
     * @param reply the inbound error stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed envelope, or empty
     *         when the {@code <error/>} child is missing
     * @throws NullPointerException if {@code reply} is {@code null}
     */
    public static Optional<Envelope> parseError(Node reply) {
        Objects.requireNonNull(reply, "reply cannot be null");
        var error = reply.getChild("error").orElse(null);
        if (error == null) {
            return Optional.empty();
        }
        var code = error.getAttributeAsInt("code").orElse(-1);
        if (code < 0) {
            return Optional.empty();
        }
        var text = error.getAttributeAsString("text").orElse(null);
        return Optional.of(new Envelope(code, text));
    }

    /**
     * Container for the {@code code}/{@code text} pair carried by every
     * {@code <error/>} child of an error envelope.
     *
     * @param code the numeric error code; always non-negative
     * @param text the optional human-readable error text; may be
     *             {@code null}
     */
    public record Envelope(int code, String text) {
        /**
         * Returns the {@code text} attribute as an {@link Optional} for
         * idiomatic null-safe access.
         *
         * @return an {@link Optional} carrying the human-readable text, or
         *         empty when the relay omitted it
         */
        public Optional<String> textAsOptional() {
            return Optional.ofNullable(text);
        }
    }
}
