package com.github.auties00.cobalt.node.smax.usernotice;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the
 * {@code <get_user_disclosures t="…"/>} payload in the canonical
 * {@code <iq xmlns="tos" type="get" to="s.whatsapp.net">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutUserNoticeGetDisclosuresRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutUserNoticeBaseIQGetRequestMixin")
public final class SmaxUserNoticeGetDisclosuresRequest implements SmaxOperation.Request {
    /**
     * The client-side fetch timestamp (seconds since epoch) carried
     * by the {@code t} attribute of the
     * {@code <get_user_disclosures/>} child.
     */
    private final long getUserDisclosuresT;

    /**
     * Constructs a request.
     *
     * @param getUserDisclosuresT the client-side fetch timestamp in
     *                            seconds
     */
    public SmaxUserNoticeGetDisclosuresRequest(long getUserDisclosuresT) {
        this.getUserDisclosuresT = getUserDisclosuresT;
    }

    /**
     * Returns the client-side fetch timestamp.
     *
     * @return the timestamp in seconds
     */
    public long getUserDisclosuresT() {
        return getUserDisclosuresT;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <get_user_disclosures/>} payload
     *
     * @implNote {@code WASmaxOutUserNoticeGetDisclosuresRequest.makeGetDisclosuresRequest}
     *           composes {@code WASmaxOutUserNoticeBaseIQGetRequestMixin}
     *           ({@code id=generateId()}, {@code type="get"}) over
     *           {@code <iq xmlns="tos" to="s.whatsapp.net">} with a
     *           single {@code <get_user_disclosures t="…"/>} child.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutUserNoticeGetDisclosuresRequest",
            exports = "makeGetDisclosuresRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutUserNoticeGetDisclosuresRequest: smax("get_user_disclosures", {t: INT(t)})
        var getUserDisclosuresNode = new NodeBuilder()
                .description("get_user_disclosures")
                .attribute("t", getUserDisclosuresT)
                .build();
        // smax("iq", {to: S_WHATSAPP_NET, xmlns: "tos", id: generateId(), type: "get"})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "tos")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(getUserDisclosuresNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxUserNoticeGetDisclosuresRequest) obj;
        return this.getUserDisclosuresT == that.getUserDisclosuresT;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserDisclosuresT);
    }

    @Override
    public String toString() {
        return "SmaxUserNoticeGetDisclosuresRequest[getUserDisclosuresT=" + getUserDisclosuresT + ']';
    }
}
