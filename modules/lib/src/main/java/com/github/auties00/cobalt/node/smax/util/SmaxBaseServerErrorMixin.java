package com.github.auties00.cobalt.node.smax.util;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;

import java.util.Objects;
import java.util.Optional;

/**
 * Shared parser for the catch-all {@code ServerError} variant produced by
 * every SMAX RPC.
 *
 * <p>Every domain in WA Web ships a {@code WASmaxIn*BaseServerErrorMixin}
 * module. These are all near-identical: assert the {@code <iq>} tag,
 * delegate envelope validation to {@link SmaxIqErrorResponseMixin#validate},
 * then route the {@code <error/>} child through {@code WASmaxIn*ServerErrors}
 * to extract a transient-internal-failure projection. Cobalt deduplicates
 * the family into the single helper here. Per-domain enums layer the
 * {@code 5xx} → semantic-name mapping where callers need it, but the bulk
 * of {@code ServerError} variants only need the {@code (code, text)} pair.
 *
 * @implNote {@code WASmaxInGroupsBaseServerErrorMixin.parseBaseServerErrorMixin}
 *           and the equivalents from other domains all share the same
 *           wire-shape. Cobalt's
 *           {@link #parseServerError(Node, Node)} produces the same
 *           projection (envelope-validated {@code (code, text)} pair) without
 *           the {@code WAResultOrError} indirection.
 */
@WhatsAppWebModule(moduleName = "WASmaxInGroupsBaseServerErrorMixin")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsServerErrors")
public final class SmaxBaseServerErrorMixin {

    /**
     * Private constructor. The class is a static-only utility.
     */
    private SmaxBaseServerErrorMixin() {
        throw new AssertionError("SmaxBaseServerErrorMixin cannot be instantiated");
    }

    /**
     * Tries to parse a server-error envelope.
     *
     * <p>Returns {@link Optional#empty()} when the envelope check fails
     * ({@code description != "iq"} or {@code type != "error"} or echoed
     * identifiers don't match), the {@code <error/>} child is missing or
     * malformed, or the parsed code is below the {@code 500} threshold
     * that distinguishes server-side from client-side errors.
     *
     * @param reply   the inbound error stanza. Never {@code null}
     * @param request the outbound request. Used to validate echoed
     *                identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed envelope, or empty
     *         when the stanza does not match the server-error schema
     * @throws NullPointerException if either argument is {@code null}
     *
     * @implNote {@code WASmaxInGroupsBaseServerErrorMixin.parseBaseServerErrorMixin}
     *           composes {@code parseIQErrorResponseMixin} with
     *           {@code parseServerErrors}. Cobalt fuses the steps into the
     *           single check here and returns {@code Optional.empty} on
     *           any failure rather than building a structured error chain.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsBaseServerErrorMixin",
            exports = "parseBaseServerErrorMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxIqErrorResponseMixin.Envelope> parseServerError(Node reply, Node request) {
        Objects.requireNonNull(reply, "reply cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        if (!SmaxIqErrorResponseMixin.validate(reply, request)) {
            return Optional.empty();
        }
        var envelope = SmaxIqErrorResponseMixin.parseError(reply).orElse(null);
        if (envelope == null) {
            return Optional.empty();
        }
        if (envelope.code() < 500) {
            return Optional.empty();
        }
        return Optional.of(envelope);
    }

    /**
     * Tries to parse a client-error envelope (codes in {@code [400, 500)}).
     *
     * <p>Used by the {@code ClientError} variants of every SMAX RPC. The
     * complementary range to {@link #parseServerError(Node, Node)}.
     *
     * @param reply   the inbound error stanza. Never {@code null}
     * @param request the outbound request. Used to validate echoed
     *                identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed envelope, or empty
     *         when the stanza does not match the client-error schema
     * @throws NullPointerException if either argument is {@code null}
     */
    public static Optional<SmaxIqErrorResponseMixin.Envelope> parseClientError(Node reply, Node request) {
        Objects.requireNonNull(reply, "reply cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        if (!SmaxIqErrorResponseMixin.validate(reply, request)) {
            return Optional.empty();
        }
        var envelope = SmaxIqErrorResponseMixin.parseError(reply).orElse(null);
        if (envelope == null) {
            return Optional.empty();
        }
        if (envelope.code() < 400 || envelope.code() >= 500) {
            return Optional.empty();
        }
        return Optional.of(envelope);
    }
}
