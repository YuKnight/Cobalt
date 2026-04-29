package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound notification variants. Carries a single
 * {@code Notification} permit because {@code Receive}-shape SMAX
 * RPCs have no outbound counterpart.
 */
public sealed interface SmaxNonceNotificationResponse extends SmaxOperation.Response permits SmaxNonceNotificationResponse.Notification {

    /**
     * Tries to parse the inbound notification.
     *
     * @param node the inbound notification stanza received from the
     *             relay; never {@code null}
     * @return an {@link Optional} carrying the parsed notification,
     *         or empty when the stanza does not match the documented
     *         schema
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxBizCtwaAdAccountNonceNotificationRPC",
            exports = "receiveNonceNotificationRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<Notification> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        return Notification.of(node);
    }

    /**
     * The {@code Notification} variant — carries the freshly-pushed
     * nonce string plus the standard envelope echoes.
     *
     * @implNote {@code WASmaxInBizCtwaAdAccountNonceNotificationRequest.parseNonceNotificationRequest}
     *           validates the notification tag, asserts
     *           {@code from="s.whatsapp.net"} and
     *           {@code type="business"}, asserts the
     *           {@code <wa_ad_account_nonce>} child exists, and
     *           projects the child's text content as
     *           {@code waAdAccountNonceElementValue}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountNonceNotificationRequest")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountServerNotificationMixin")
    final class Notification implements SmaxNonceNotificationResponse {
        /**
         * The optional {@code to} attribute (the local user JID); may
         * be {@code null} when the relay broadcasts to all linked
         * devices simultaneously.
         */
        private final Jid to;

        /**
         * The freshly-pushed nonce — the text content of the
         * {@code <wa_ad_account_nonce>} child.
         */
        private final String nonce;

        /**
         * Constructs a new notification.
         *
         * @param to    the optional target user JID; may be
         *              {@code null}
         * @param nonce the nonce string; never {@code null}
         * @throws NullPointerException if {@code nonce} is
         *                              {@code null}
         */
        public Notification(Jid to, String nonce) {
            this.to = to;
            this.nonce = Objects.requireNonNull(nonce, "nonce cannot be null");
        }

        /**
         * Returns the optional target user JID.
         *
         * @return an {@link Optional} carrying the JID
         */
        public Optional<Jid> to() {
            return Optional.ofNullable(to);
        }

        /**
         * Returns the nonce string.
         *
         * @return the nonce; never {@code null}
         */
        public String nonce() {
            return nonce;
        }

        /**
         * Tries to parse a notification from the given inbound
         * stanza.
         *
         * @param node the inbound notification stanza
         * @return an {@link Optional} carrying the parsed
         *         notification, or empty when the stanza does not
         *         match the documented schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaAdAccountNonceNotificationRequest",
                exports = "parseNonceNotificationRequest",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Notification> of(Node node) {
            Objects.requireNonNull(node, "node cannot be null");
            if (!node.hasDescription("notification")) {
                return Optional.empty();
            }
            var fromValue = node.getAttributeAsString("from").orElse(null);
            if (!"s.whatsapp.net".equals(fromValue)) {
                return Optional.empty();
            }
            if (!node.hasAttribute("type", "business")) {
                return Optional.empty();
            }
            var nonceNode = node.getChild("wa_ad_account_nonce").orElse(null);
            if (nonceNode == null) {
                return Optional.empty();
            }
            var nonce = nonceNode.toContentString().orElse(null);
            if (nonce == null) {
                return Optional.empty();
            }
            var to = node.getAttributeAsJid("to").orElse(null);
            return Optional.of(new Notification(to, nonce));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Notification) obj;
            return Objects.equals(this.to, that.to)
                    && Objects.equals(this.nonce, that.nonce);
        }

        @Override
        public int hashCode() {
            return Objects.hash(to, nonce);
        }

        @Override
        public String toString() {
            return "SmaxNonceNotificationResponse.Notification[to=" + to
                    + ", nonce=" + nonce + ']';
        }
    }
}
