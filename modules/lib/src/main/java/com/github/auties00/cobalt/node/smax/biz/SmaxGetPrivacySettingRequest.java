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
 * The outbound stanza variant. Wraps the empty
 * {@code <privacy/>} payload in the canonical
 * {@code <iq xmlns="w:biz" type="get" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizSettingsGetPrivacySettingRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizSettingsBaseIQGetRequestMixin")
public final class SmaxGetPrivacySettingRequest implements SmaxOperation.Request {
    /**
     * Constructs a new request. No parameters.
     */
    public SmaxGetPrivacySettingRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         bare {@code <privacy/>} child
     *
     * @implNote {@code WASmaxOutBizSettingsGetPrivacySettingRequest.makeGetPrivacySettingRequest}
     *           composes
     *           {@code WASmaxOutBizSettingsBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over a
     *           bare {@code <iq xmlns="w:biz" to="s.whatsapp.net">}
     *           with a single empty {@code <privacy/>} child.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizSettingsGetPrivacySettingRequest",
            exports = "makeGetPrivacySettingRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var privacyNode = new NodeBuilder()
                .description("privacy")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz")
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
        return SmaxGetPrivacySettingRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "SmaxGetPrivacySettingRequest[]";
    }
}
