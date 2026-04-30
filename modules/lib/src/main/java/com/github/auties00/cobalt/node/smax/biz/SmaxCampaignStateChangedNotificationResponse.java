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
public sealed interface SmaxCampaignStateChangedNotificationResponse extends SmaxOperation.Response
        permits SmaxCampaignStateChangedNotificationResponse.Notification {

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
    @WhatsAppWebExport(moduleName = "WASmaxSmbMeteredMessagesCampaignCampaignStateChangedNotificationRPC",
            exports = "receiveCampaignStateChangedNotificationRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<Notification> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        return Notification.of(node);
    }

    /**
     * The {@code Notification} variant. Carries the campaign
     * identifiers, the new status, and the standard envelope echoes.
     *
     * @implNote {@code WASmaxInSmbMeteredMessagesCampaignCampaignStateChangedNotificationRequest.parseCampaignStateChangedNotificationRequest}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInSmbMeteredMessagesCampaignCampaignStateChangedNotificationRequest")
    @WhatsAppWebModule(moduleName = "WASmaxInSmbMeteredMessagesCampaignServerNotificationMixin")
    final class Notification implements SmaxCampaignStateChangedNotificationResponse {
        /**
         * The optional {@code to} attribute (the local user JID); may
         * be {@code null} when the relay broadcasts to all linked
         * devices simultaneously.
         */
        private final Jid to;

        /**
         * The optional {@code ad_id} attribute on the
         * {@code <mm_campaign>} child. The Facebook ads platform
         * advertisement identifier.
         */
        private final String adId;

        /**
         * The optional {@code ad_group_id} attribute. The parent
         * advertisement-group identifier.
         */
        private final String adGroupId;

        /**
         * The optional {@code ad_creative_id} attribute. The
         * advertisement-creative identifier.
         */
        private final String adCreativeId;

        /**
         * The {@code status} attribute on the {@code <mm_campaign>}
         * child. One of the documented enum literals
         * {@code "ok"} (cleared) or {@code "integrityNotCleared"}
         * (held back).
         */
        private final String status;

        /**
         * Constructs a new notification.
         *
         * @param to           the optional target user JID; may be
         *                     {@code null}
         * @param adId         the optional ad ID; may be {@code null}
         * @param adGroupId    the optional ad-group ID; may be
         *                     {@code null}
         * @param adCreativeId the optional ad-creative ID; may be
         *                     {@code null}
         * @param status       the campaign status; never
         *                     {@code null}
         * @throws NullPointerException if {@code status} is
         *                              {@code null}
         */
        public Notification(Jid to, String adId, String adGroupId,
                            String adCreativeId, String status) {
            this.to = to;
            this.adId = adId;
            this.adGroupId = adGroupId;
            this.adCreativeId = adCreativeId;
            this.status = Objects.requireNonNull(status, "status cannot be null");
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
         * Returns the optional advertisement ID.
         *
         * @return an {@link Optional} carrying the ID
         */
        public Optional<String> adId() {
            return Optional.ofNullable(adId);
        }

        /**
         * Returns the optional advertisement-group ID.
         *
         * @return an {@link Optional} carrying the ID
         */
        public Optional<String> adGroupId() {
            return Optional.ofNullable(adGroupId);
        }

        /**
         * Returns the optional advertisement-creative ID.
         *
         * @return an {@link Optional} carrying the ID
         */
        public Optional<String> adCreativeId() {
            return Optional.ofNullable(adCreativeId);
        }

        /**
         * Returns the campaign status enum value.
         *
         * @return the status; never {@code null}
         */
        public String status() {
            return status;
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
        @WhatsAppWebExport(moduleName = "WASmaxInSmbMeteredMessagesCampaignCampaignStateChangedNotificationRequest",
                exports = "parseCampaignStateChangedNotificationRequest",
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
            var campaign = node.getChild("mm_campaign").orElse(null);
            if (campaign == null) {
                return Optional.empty();
            }
            var status = campaign.getAttributeAsString("status").orElse(null);
            if (status == null) {
                return Optional.empty();
            }
            var to = node.getAttributeAsJid("to").orElse(null);
            var adId = campaign.getAttributeAsString("ad_id").orElse(null);
            var adGroupId = campaign.getAttributeAsString("ad_group_id").orElse(null);
            var adCreativeId = campaign.getAttributeAsString("ad_creative_id").orElse(null);
            return Optional.of(new Notification(to, adId, adGroupId, adCreativeId, status));
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
                    && Objects.equals(this.adId, that.adId)
                    && Objects.equals(this.adGroupId, that.adGroupId)
                    && Objects.equals(this.adCreativeId, that.adCreativeId)
                    && Objects.equals(this.status, that.status);
        }

        @Override
        public int hashCode() {
            return Objects.hash(to, adId, adGroupId, adCreativeId, status);
        }

        @Override
        public String toString() {
            return "SmaxCampaignStateChangedNotificationResponse.Notification[to=" + to
                    + ", adId=" + adId
                    + ", adGroupId=" + adGroupId
                    + ", adCreativeId=" + adCreativeId
                    + ", status=" + status + ']';
        }
    }
}
