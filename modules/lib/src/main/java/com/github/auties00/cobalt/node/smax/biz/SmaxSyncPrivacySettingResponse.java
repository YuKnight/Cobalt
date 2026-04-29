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
 * Sealed family of inbound notification variants produced by the
 * relay. Carries a single {@code Notification} permit because
 * {@code Receive}-shape SMAX RPCs have no outbound counterpart.
 */
public sealed interface SmaxSyncPrivacySettingResponse extends SmaxOperation.Response permits SmaxSyncPrivacySettingResponse.Notification {

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
    @WhatsAppWebExport(moduleName = "WASmaxBizSettingsSyncPrivacySettingRPC",
            exports = "receiveSyncPrivacySettingRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<Notification> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        return Notification.of(node);
    }

    /**
     * The {@code Notification} variant — carries the optional
     * post-update consent value plus the standard envelope echoes.
     *
     * @implNote {@code WASmaxInBizSettingsSyncPrivacySettingRequest.parseSyncPrivacySettingRequest}
     *           validates the notification tag, asserts the literal
     *           {@code from="s.whatsapp.net"}, the literal
     *           {@code type="business"}, then projects the optional
     *           {@code <smb_data_sharing_with_meta_consent>} attribute.
     *           Cobalt mirrors the optional-projection semantics —
     *           the JS uses {@code success ? value : null} on the
     *           inner mixin, so the field is genuinely optional.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsSyncPrivacySettingRequest")
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsSmbDataSharingSettingMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsSmbDataSharingSettingValueMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizSettingsServerNotificationMixin")
    final class Notification implements SmaxSyncPrivacySettingResponse {
        /**
         * The optional {@code to} attribute (the local user JID); may
         * be {@code null} when the relay broadcasts to all linked
         * devices simultaneously.
         */
        private final Jid to;

        /**
         * The optional consent value from the
         * {@code <smb_data_sharing_with_meta_consent value="..."/>}
         * inner — one of {@code "true"} / {@code "false"} /
         * {@code "notSet"} or {@code null} when the relay cleared
         * the preference.
         */
        private final String dataSharingConsent;

        /**
         * Constructs a new notification.
         *
         * @param to                 the optional target user JID;
         *                           may be {@code null}
         * @param dataSharingConsent the optional consent value; may
         *                           be {@code null}
         */
        public Notification(Jid to, String dataSharingConsent) {
            this.to = to;
            this.dataSharingConsent = dataSharingConsent;
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
         * Returns the optional consent value.
         *
         * @return an {@link Optional} carrying the consent value, or
         *         empty when the relay cleared the preference
         */
        public Optional<String> dataSharingConsent() {
            return Optional.ofNullable(dataSharingConsent);
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizSettingsSyncPrivacySettingRequest",
                exports = "parseSyncPrivacySettingRequest",
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
            var privacy = node.getChild("privacy").orElse(null);
            if (privacy == null) {
                return Optional.empty();
            }
            var to = node.getAttributeAsJid("to").orElse(null);
            String consent = null;
            var consentNode = privacy.getChild("smb_data_sharing_with_meta_consent").orElse(null);
            if (consentNode != null) {
                consent = consentNode.getAttributeAsString("value").orElse(null);
            }
            return Optional.of(new Notification(to, consent));
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
                    && Objects.equals(this.dataSharingConsent, that.dataSharingConsent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(to, dataSharingConsent);
        }

        @Override
        public String toString() {
            return "SmaxSyncPrivacySettingResponse.Notification[to=" + to
                    + ", dataSharingConsent=" + dataSharingConsent + ']';
        }
    }
}
