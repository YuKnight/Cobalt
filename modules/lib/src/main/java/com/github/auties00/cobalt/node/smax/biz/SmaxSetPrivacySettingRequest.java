package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the
 * {@code <privacy><smb_data_sharing_with_meta_consent value="..."/></privacy>}
 * payload in the canonical
 * {@code <iq xmlns="w:biz" type="set" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizSettingsSetPrivacySettingRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizSettingsSmbDataSharingSettingMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBizSettingsSmbDataSharingSettingValueMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBizSettingsBaseIQSetRequestMixin")
public final class SmaxSetPrivacySettingRequest implements SmaxOperation.Request {
    /**
     * The optional consent value. One of {@code "true"} /
     * {@code "false"} / {@code "notSet"}; {@code null} omits the
     * inner {@code <smb_data_sharing_with_meta_consent>} child via
     * the {@code optionalMerge} of the JS mixin pair.
     */
    private final String dataSharingConsent;

    /**
     * Constructs a new request optionally carrying a consent value.
     *
     * @param dataSharingConsent the optional consent value; may be
     *                           {@code null} to clear the stored
     *                           choice
     */
    public SmaxSetPrivacySettingRequest(String dataSharingConsent) {
        this.dataSharingConsent = dataSharingConsent;
    }

    /**
     * Returns the optional consent value.
     *
     * @return an {@link Optional} carrying the consent value, or
     *         empty when no consent was supplied
     */
    public Optional<String> dataSharingConsent() {
        return Optional.ofNullable(dataSharingConsent);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <privacy/>} payload
     *
     * @implNote {@code WASmaxOutBizSettingsSetPrivacySettingRequest.makeSetPrivacySettingRequest}
     *           composes
     *           {@code WASmaxOutBizSettingsBaseIQSetRequestMixin}
     *           ({@code id=generateId()}, {@code type="set"}) over a
     *           {@code <iq xmlns="w:biz" to="s.whatsapp.net">}
     *           carrying a {@code <privacy>} child whose
     *           {@code <smb_data_sharing_with_meta_consent>} child
     *           is wrapped in
     *           {@code WASmaxOutBizSettingsSmbDataSharingSettingValueMixin}'s
     *           {@code smax$any value="..."} attribute.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizSettingsSetPrivacySettingRequest",
            exports = "makeSetPrivacySettingRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var privacyBuilder = new NodeBuilder()
                .description("privacy");
        if (dataSharingConsent != null) {
            var consentNode = new NodeBuilder()
                    .description("smb_data_sharing_with_meta_consent")
                    .attribute("value", dataSharingConsent)
                    .build();
            privacyBuilder.content(consentNode);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(privacyBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxSetPrivacySettingRequest) obj;
        return Objects.equals(this.dataSharingConsent, that.dataSharingConsent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSharingConsent);
    }

    @Override
    public String toString() {
        return "SmaxSetPrivacySettingRequest[dataSharingConsent=" + dataSharingConsent + ']';
    }
}
