package com.github.auties00.cobalt.node.smax.passivemode;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants. Only {@code Success} is
 * documented by WA Web for this RPC.
 *
 * @implNote {@code WASmaxPassiveModePassiveIQRPC.sendPassiveIQRPC} tries
 *           {@code Success} and throws on no-match without a
 *           documented error variant. Cobalt mirrors the surface and
 *           returns {@link Optional#empty()} on no-match.
 */
public sealed interface SmaxPassiveModePassiveIQResponse extends SmaxOperation.Response
        permits SmaxPassiveModePassiveIQResponse.Success {

    /**
     * Tries the {@link Success} variant and returns it when it parses
     * cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxPassiveModePassiveIQRPC",
            exports = "sendPassiveIQRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxPassiveModePassiveIQResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        return Success.of(node, request);
    }

    /**
     * The {@code Success} reply variant. The relay accepted the
     * active→passive transition and echoed back the
     * {@code <iq type="result" from="s.whatsapp.net">} envelope.
     *
     * <p>Carries the {@code from} JID echoed by the relay (always
     * {@code s.whatsapp.net} per WA Web's
     * {@code literalJid(attrDomainJid, …, "s.whatsapp.net")}
     * assertion).
     *
     * @implNote {@code WASmaxInPassiveModePassiveIQResponseSuccess.parsePassiveIQResponseSuccess}
     *           validates the {@code <iq type="result"
     *           from="s.whatsapp.net">} envelope and the presence of the
     *           {@code <passive/>} child, returning
     *           {@code (type, from)}. Cobalt collapses to {@link #from()}
     *           since {@code type} is always the literal {@code "result"}
     *           when the parse succeeds.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPassiveModePassiveIQResponseSuccess")
    final class Success implements SmaxPassiveModePassiveIQResponse {
        /**
         * The {@code from} JID echoed by the relay.
         */
        private final Jid from;

        /**
         * Constructs a successful reply.
         *
         * @param from the echoed {@code from} JID. Never {@code null}
         * @throws NullPointerException if {@code from} is {@code null}
         */
        public Success(Jid from) {
            this.from = Objects.requireNonNull(from, "from cannot be null");
        }

        /**
         * Returns the {@code from} JID echoed by the relay.
         *
         * @return the JID. Never {@code null}
         */
        public Jid from() {
            return from;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPassiveModePassiveIQResponseSuccess",
                exports = "parsePassiveIQResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!node.hasDescription("iq")) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "result")) {
                return Optional.empty();
            }
            var requestId = request.getAttributeAsString("id").orElse(null);
            if (requestId == null || !node.hasAttribute("id", requestId)) {
                return Optional.empty();
            }
            if (node.getChild("passive").isEmpty()) {
                return Optional.empty();
            }
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null || !from.hasServer(JidServer.user())) {
                return Optional.empty();
            }
            return Optional.of(new Success(from));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Success) obj;
            return Objects.equals(this.from, that.from);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from);
        }

        @Override
        public String toString() {
            return "SmaxPassiveModePassiveIQResponse.Success[from=" + from + ']';
        }
    }
}
