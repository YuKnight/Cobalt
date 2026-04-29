package com.github.auties00.cobalt.node.iq.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;

/**
 * The outbound stanza variant — wraps a bare {@code <privacy/>} child in
 * the canonical {@code <iq xmlns="privacy" type="get">} envelope.
 *
 * <p>Legacy-IQ RPC: fetches the user's full set of privacy settings
 * (last-seen, online, profile, about, read-receipts, group-add,
 * call-add, messages, defense-mode) by sending an
 * {@code <iq xmlns="privacy" type="get"><privacy/></iq>} stanza.
 *
 * @implNote {@code WAWebQueryPrivacySettingsJob.getPrivacy} optionally routes
 *           through MEX when the {@code mex_get_privacy_settings_mode} A/B
 *           flag is enabled; Cobalt models only the legacy XML path here.
 *           The MEX path is a separate module
 *           ({@code WAWebMexGetPrivacySetting}).
 */
@WhatsAppWebModule(moduleName = "WAWebQueryPrivacySettingsJob")
public final class IqQueryPrivacySettingsRequest implements IqOperation.Request {
    /**
     * Constructs a new request.
     */
    public IqQueryPrivacySettingsRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     *
     * @implNote {@code WAWebQueryPrivacySettingsJob.getPrivacy} composes
     *           {@code wap("iq",{xmlns:"privacy", to:S_WHATSAPP_NET,
     *           type:"get", id:generateId()}, wap("privacy",null))}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebQueryPrivacySettingsJob",
            exports = "getPrivacy", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var privacyNode = new NodeBuilder()
                .description("privacy")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "privacy")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(privacyNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return IqQueryPrivacySettingsRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "IqQueryPrivacySettingsRequest[]";
    }
}
