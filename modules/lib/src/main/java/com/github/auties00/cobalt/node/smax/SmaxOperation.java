package com.github.auties00.cobalt.node.smax;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.NodeBuilder;

/**
 * Root sealed type for every typed SMAX operation modelled in Cobalt.
 *
 * <p>SMAX is WhatsApp's typed-stanza-builder framework — a layer above raw
 * {@code <iq>} / {@code <presence>} / {@code <message>} stanzas that
 * replaces ad-hoc node construction with a declarative request/response
 * schema. Each SMAX RPC in WA Web ships as three modules:
 * <ul>
 *   <li>{@code WASmaxOut...Request} — the outbound stanza shape
 *   <li>{@code WASmaxIn...Response*} — one or more inbound reply variants
 *       (typically {@code Success}, {@code ClientError}, {@code ServerError},
 *       plus per-RPC alternates such as {@code SuccessWithMatch},
 *       {@code MigratedSuccess}, etc.)
 *   <li>{@code WASmax...RPC} — the {@code sendXxxRPC} / {@code castXxxRPC}
 *       / {@code receiveXxxRPC} export that wires the request and response
 *       parsers together
 * </ul>
 *
 * <p>Cobalt models every SMAX RPC as a pair of independent top-level types
 * — a {@code Smax<Op>Request} class implementing {@link Request} and a
 * {@code Smax<Op>Response} sealed interface implementing {@link Response}
 * whose permits enumerate the documented reply variants. {@code Cast}-shape
 * RPCs (one-way outbound) only ship a {@code Request}; {@code Receive}-shape
 * RPCs (server-pushed) only ship a {@code Response} with a
 * {@code Response.of(Node)} static factory.
 *
 * <p>The sealed hierarchy permits exactly two participants — {@link Request}
 * and {@link Response} — so every SMAX operation handle is statically
 * classified as one or the other. {@link Request} declares the
 * {@code toNode()} contract used by
 * {@code WhatsAppClient.sendNode(SmaxOperation.Request)} to dispatch the
 * operation; {@link Response} carries no methods today and exists purely
 * as the closed counterpart of {@code Request}.
 *
 * @implNote {@code WAComms.sendSmaxStanza} / {@code WAComms.castSmaxStanza}:
 *           the transport entry points used by every WA Web SMAX dispatcher
 *           accept an already-constructed SMAX stanza and route it through
 *           the same socket pipeline that legacy {@code <iq>} stanzas use.
 *           The sealed hierarchy on the Cobalt side mirrors the static
 *           classification WA Web carries through the {@code WASmaxOut...}
 *           / {@code WASmaxIn...} module-name convention; the marker
 *           imposes no runtime contract beyond the abstract {@code toNode()}
 *           method declared on {@link Request}.
 */
@WhatsAppWebModule(moduleName = "WAComms")
public sealed interface SmaxOperation permits SmaxOperation.Request, SmaxOperation.Response {
    /**
     * The outbound side of a SMAX operation — every concrete operation's
     * {@code Smax<Op>Request} class implements this interface.
     *
     * <p>Carries the {@code toNode()} factory required by
     * {@code WhatsAppClient.sendNode(SmaxOperation.Request)} so that call
     * sites can dispatch a request by passing the typed value directly.
     */
    non-sealed interface Request extends SmaxOperation {
        /**
         * Builds the outbound SMAX stanza for this request.
         *
         * <p>Each concrete {@code Smax<Op>Request} implementation
         * serialises its typed fields into the canonical {@code <iq>} /
         * {@code <presence>} / {@code <message>} envelope expected by
         * the relay.
         *
         * @return the outbound stanza builder; never {@code null}
         */
        NodeBuilder toNode();
    }

    /**
     * The inbound side of a SMAX operation — every concrete operation's
     * {@code Smax<Op>Response} sealed interface (or its individual
     * variant classes) implements this interface.
     *
     * <p>Carries no abstract methods today; the type exists purely as
     * the closed counterpart of {@link Request} so the entire SMAX
     * surface can be reasoned about as a single sealed hierarchy.
     */
    non-sealed interface Response extends SmaxOperation {
    }
}
