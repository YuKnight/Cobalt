package com.github.auties00.cobalt.node.smax.clientexpiration;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound projections. The receive-only RPC has a
 * single {@link Inbound} permit.
 */
public sealed interface SmaxClientExpirationResponse extends SmaxOperation.Response
        permits SmaxClientExpirationResponse.Inbound {

    /**
     * Tries to parse the inbound stanza.
     *
     * @param node the inbound stanza. Never {@code null}
     * @return an {@link Optional} carrying the parsed projection,
     *         or empty when the stanza shape does not match the
     *         documented schema
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxClientExpirationClientExpirationRPC",
            exports = "receiveClientExpirationRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxClientExpirationResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        return Inbound.of(node);
    }

    /**
     * The inbound projection of the
     * {@code <ib from="s.whatsapp.net"><client_expiration t?/></ib>}
     * stanza.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInClientExpirationClientExpirationRequest")
    final class Inbound implements SmaxClientExpirationResponse {
        /**
         * The {@code from} JID echoed on the {@code <ib>} envelope.
         * Always the literal {@code s.whatsapp.net} server JID.
         */
        private final Jid from;

        /**
         * The optional non-negative Unix-epoch expiration cutoff
         * carried on the {@code <client_expiration/>} child's
         * {@code t} attribute.
         */
        private final Long clientExpirationT;

        /**
         * Constructs a new inbound projection.
         *
         * @param from               the {@code from} JID. Never
         *                           {@code null}
         * @param clientExpirationT  the optional cutoff timestamp;
         *                           may be {@code null}
         * @throws NullPointerException if {@code from} is
         *                              {@code null}
         */
        public Inbound(Jid from, Long clientExpirationT) {
            this.from = Objects.requireNonNull(from, "from cannot be null");
            this.clientExpirationT = clientExpirationT;
        }

        /**
         * Returns the {@code from} JID.
         *
         * @return the JID. Never {@code null}
         */
        public Jid from() {
            return from;
        }

        /**
         * Returns the optional cutoff timestamp.
         *
         * @return an {@link Optional} carrying the timestamp
         */
        public Optional<Long> clientExpirationT() {
            return Optional.ofNullable(clientExpirationT);
        }

        /**
         * Tries to parse an {@link Inbound} projection from the
         * given stanza.
         *
         * @param node the inbound stanza. Never {@code null}
         * @return an {@link Optional} carrying the parsed
         *         projection
         * @throws NullPointerException if {@code node} is
         *                              {@code null}
         */
        @WhatsAppWebExport(moduleName = "WASmaxInClientExpirationClientExpirationRequest",
                exports = "parseClientExpirationRequest",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Inbound> of(Node node) {
            Objects.requireNonNull(node, "node cannot be null");
            // WASmaxParseUtils.assertTag(e, "ib"): rejects any envelope whose tag is not "ib".
            if (!node.hasDescription("ib")) {
                return Optional.empty();
            }
            // WASmaxParseJid.literalJid(attrDomainJid, e, "from", "s.whatsapp.net"): the "from" attribute must be the literal s.whatsapp.net server JID.
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null || !Jid.userServer().equals(from)) {
                return Optional.empty();
            }
            // WASmaxParseUtils.flattenedChildWithTag(e, "client_expiration"): exactly one <client_expiration/> child must be present.
            var expirationNode = node.getChild("client_expiration").orElse(null);
            if (expirationNode == null) {
                return Optional.empty();
            }
            // WASmaxParseUtils.optional(attrIntRange, n, "t", 0, undefined): "t" is optional, but when present must be a non-negative integer.
            var clientExpirationT = expirationNode.getAttributeAsLong("t");
            if (clientExpirationT.isPresent() && clientExpirationT.getAsLong() < 0) {
                return Optional.empty();
            }
            var clientExpirationValue = clientExpirationT.isPresent()
                    ? Long.valueOf(clientExpirationT.getAsLong())
                    : null;
            return Optional.of(new Inbound(from, clientExpirationValue));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Inbound) obj;
            return Objects.equals(this.from, that.from)
                    && Objects.equals(this.clientExpirationT, that.clientExpirationT);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, clientExpirationT);
        }

        @Override
        public String toString() {
            return "SmaxClientExpirationResponse.Inbound[from=" + from
                    + ", clientExpirationT=" + clientExpirationT + ']';
        }
    }
}
