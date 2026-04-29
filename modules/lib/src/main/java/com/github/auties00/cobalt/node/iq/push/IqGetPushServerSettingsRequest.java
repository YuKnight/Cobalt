package com.github.auties00.cobalt.node.iq.push;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.util.RandomIdUtils;

/**
 * The outbound {@code <iq xmlns="urn:xmpp:whatsapp:push" type="get">}
 * stanza variant — wraps a single bare {@code <settings/>} payload.
 */
@WhatsAppWebModule(moduleName = "WAWebGetPushServerSettingsJob")
public final class IqGetPushServerSettingsRequest implements IqOperation.Request {
    /**
     * Constructs a new query-push-server-settings request.
     */
    public IqGetPushServerSettingsRequest() {
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <settings/>} payload
     *
     * @implNote {@code WAWebGetPushServerSettingsJob} composes the IQ
     *           via {@code WAWap.wap("iq", {to:S_WHATSAPP_NET,
     *           type:"get", xmlns:"urn:xmpp:whatsapp:push", id},
     *           wap("settings", null))}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebGetPushServerSettingsJob",
            exports = "getPushServerSettings", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var settingsNode = new NodeBuilder()
                .description("settings")
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("id", RandomIdUtils.newId())
                .attribute("xmlns", "urn:xmpp:whatsapp:push")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(settingsNode);
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
        return IqGetPushServerSettingsRequest.class.hashCode();
    }

    @Override
    public String toString() {
        return "IqGetPushServerSettingsRequest[]";
    }
}
