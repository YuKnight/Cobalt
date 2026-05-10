package com.github.auties00.cobalt.node.iq;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.NodeBuilder;

/**
 * Root sealed type for every typed legacy {@code <iq>} operation modelled
 * in Cobalt.
 *
 * <p>"Legacy IQ" denotes the older WhatsApp Web pattern that builds an
 * {@code <iq>} stanza directly via {@code WAComms._sendIq} (driven by the
 * {@code WADeprecatedSendIq} dispatcher) without going through the SMAX
 * typed-stanza-builder framework. The pattern predates the SMAX rewrite
 * and remains in use across roughly forty modules in the WA Web bundle
 * that have not yet migrated to typed SMAX.
 *
 * <p>Cobalt models every legacy-IQ operation as a pair of independent
 * top-level types — an {@code Iq<Op>Request} class implementing
 * {@link Request} and an {@code Iq<Op>Response} sealed interface
 * implementing {@link Response} whose permits enumerate the documented
 * reply variants. The shape is identical to the {@code SmaxOperation}
 * hierarchy but the source provenance is a {@code WAWeb*Job} /
 * {@code WAWeb*Action} module rather than a {@code WASmax*RPC} dispatcher.
 *
 * <p>The sealed hierarchy permits exactly two participants — {@link Request}
 * and {@link Response} — so every legacy-IQ operation handle is statically
 * classified as one or the other. {@link Request} declares the
 * {@code toNode()} contract used by
 * {@code WhatsAppClient.sendNode(IqOperation.Request)} to dispatch the
 * operation; {@link Response} carries no methods today and exists purely
 * as the closed counterpart of {@code Request}.
 */
@WhatsAppWebModule(moduleName = "WADeprecatedSendIq")
public sealed interface IqOperation permits IqOperation.Request, IqOperation.Response {
    /**
     * The outbound side of a legacy-IQ operation — every concrete
     * operation's {@code Iq<Op>Request} class implements this interface.
     *
     * <p>Carries the {@code toNode()} factory required by
     * {@code WhatsAppClient.sendNode(IqOperation.Request)} so that call
     * sites can dispatch a request by passing the typed value directly.
     */
    non-sealed interface Request extends IqOperation {
        /**
         * Builds the outbound IQ stanza for this request.
         *
         * <p>Each concrete {@code Iq<Op>Request} implementation
         * serialises its typed fields into the canonical {@code <iq>}
         * envelope expected by the relay.
         *
         * @return the outbound stanza builder; never {@code null}
         */
        NodeBuilder toNode();
    }

    /**
     * The inbound side of a legacy-IQ operation — every concrete
     * operation's {@code Iq<Op>Response} sealed interface (or its
     * individual variant classes) implements this interface.
     *
     * <p>Carries no abstract methods today; the type exists purely as
     * the closed counterpart of {@link Request} so the entire legacy-IQ
     * surface can be reasoned about as a single sealed hierarchy.
     */
    non-sealed interface Response extends IqOperation {
    }
}
