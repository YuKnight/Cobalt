package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound notification variants — carries a single
 * {@link Notification} permit because {@code Receive}-shape SMAX RPCs
 * have no outbound counterpart.
 */
public sealed interface SmaxBannerSuggestionResponse extends SmaxOperation.Response
        permits SmaxBannerSuggestionResponse.Notification {

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
    @WhatsAppWebExport(moduleName = "WASmaxBizCtwaActionBannerSuggestionRPC",
            exports = "receiveBannerSuggestionRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<Notification> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        return Notification.of(node);
    }

    /**
     * The {@code Notification} variant — carries the banner
     * suggestion plus envelope echoes.
     *
     * @implNote {@code WASmaxInBizCtwaActionBannerSuggestionRequest.parseBannerSuggestionRequest}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionBannerSuggestionRequest")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionServerNotificationMixin")
    final class Notification implements SmaxBannerSuggestionResponse {
        /**
         * The {@code from} attribute — always the literal
         * {@code s.whatsapp.net} server JID.
         */
        private final Jid from;

        /**
         * The optional {@code to} user JID; may be {@code null} when
         * the relay broadcasts to all linked devices.
         */
        private final Jid to;

        /**
         * The {@code type} attribute — always the literal
         * {@code "business"}.
         */
        private final String type;

        /**
         * The {@code target_entity_id} attribute on the
         * {@code <ctwa_suggestion/>} child — identifies the source
         * CTWA entity (ad, account, message thread).
         */
        private final String targetEntityId;

        /**
         * The optional {@code <banner/>} grandchild projection.
         */
        private final SmaxBannerSuggestionBanner banner;

        /**
         * Constructs a new notification.
         *
         * @param from           the server JID; never {@code null}
         * @param to             the optional target user JID; may
         *                       be {@code null}
         * @param type           the notification type; never
         *                       {@code null}
         * @param targetEntityId the CTWA target entity ID; never
         *                       {@code null}
         * @param banner         the optional banner projection; may
         *                       be {@code null}
         * @throws NullPointerException if {@code from},
         *                              {@code type} or
         *                              {@code targetEntityId} is
         *                              {@code null}
         */
        public Notification(Jid from, Jid to, String type, String targetEntityId, SmaxBannerSuggestionBanner banner) {
            this.from = Objects.requireNonNull(from, "from cannot be null");
            this.to = to;
            this.type = Objects.requireNonNull(type, "type cannot be null");
            this.targetEntityId = Objects.requireNonNull(targetEntityId, "targetEntityId cannot be null");
            this.banner = banner;
        }

        /**
         * Returns the server JID that emitted the notification.
         *
         * @return the JID; never {@code null}
         */
        public Jid from() {
            return from;
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
         * Returns the notification type.
         *
         * @return the type; never {@code null}
         */
        public String type() {
            return type;
        }

        /**
         * Returns the CTWA target entity ID.
         *
         * @return the id; never {@code null}
         */
        public String targetEntityId() {
            return targetEntityId;
        }

        /**
         * Returns the optional banner projection.
         *
         * @return an {@link Optional} carrying the banner, or empty
         *         when the relay omitted the {@code <banner/>}
         *         grandchild
         */
        public Optional<SmaxBannerSuggestionBanner> banner() {
            return Optional.ofNullable(banner);
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaActionBannerSuggestionRequest",
                exports = "parseBannerSuggestionRequest",
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
            var from = node.getAttributeAsJid("from").orElse(null);
            if (from == null) {
                return Optional.empty();
            }
            var to = node.getAttributeAsJid("to").orElse(null);
            var type = node.getAttributeAsString("type").orElse(null);
            if (!"business".equals(type)) {
                return Optional.empty();
            }
            var ctwaSuggestion = node.getChild("ctwa_suggestion").orElse(null);
            if (ctwaSuggestion == null) {
                return Optional.empty();
            }
            var targetEntityId = ctwaSuggestion.getAttributeAsString("target_entity_id").orElse(null);
            if (targetEntityId == null) {
                return Optional.empty();
            }
            SmaxBannerSuggestionBanner banner = null;
            var bannerNode = ctwaSuggestion.getChild("banner").orElse(null);
            if (bannerNode != null) {
                var parsed = SmaxBannerSuggestionBanner.of(bannerNode);
                if (parsed.isEmpty()) {
                    return Optional.empty();
                }
                banner = parsed.get();
            }
            return Optional.of(new Notification(from, to, type, targetEntityId, banner));
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
            return Objects.equals(this.from, that.from)
                    && Objects.equals(this.to, that.to)
                    && Objects.equals(this.type, that.type)
                    && Objects.equals(this.targetEntityId, that.targetEntityId)
                    && Objects.equals(this.banner, that.banner);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to, type, targetEntityId, banner);
        }

        @Override
        public String toString() {
            return "SmaxBannerSuggestionResponse.Notification[from=" + from
                    + ", to=" + to
                    + ", type=" + type
                    + ", targetEntityId=" + targetEntityId
                    + ", banner=" + banner + ']';
        }
    }
}
