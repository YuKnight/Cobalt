package com.github.auties00.cobalt.node.iq.tos;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound {@code <iq xmlns="tos" type="set">} stanza variant.
 * Wraps a single {@code <delete id="…"/>} child.
 */
@WhatsAppWebModule(moduleName = "WAWebTosJob")
public final class IqDeleteTosRequest implements IqOperation.Request {
    /**
     * The notice id to delete. Routed verbatim into the
     * {@code <delete>} child's {@code id} attribute.
     */
    private final String noticeId;

    /**
     * Constructs a new delete-tos request.
     *
     * @param noticeId the notice id to delete. Never {@code null}
     * @throws NullPointerException if {@code noticeId} is {@code null}
     */
    public IqDeleteTosRequest(String noticeId) {
        this.noticeId = Objects.requireNonNull(noticeId, "noticeId cannot be null");
    }

    /**
     * Returns the notice id being deleted.
     *
     * @return the notice id. Never {@code null}
     */
    public String noticeId() {
        return noticeId;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <delete>} payload
     *
     * @implNote {@code WAWebTosJob.deleteTosState} composes the IQ via
     *           {@code wap("iq", {xmlns:"tos", id, type:"set",
     *           to:S_WHATSAPP_NET}, wap("delete", {id:e}))}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebTosJob",
            exports = "deleteTosState", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebTosJob: wap("delete", {id})
        var deleteNode = new NodeBuilder()
                .description("delete")
                .attribute("id", noticeId)
                .build();
        // WAWebTosJob: wap("iq", {xmlns:"tos", id, type:"set", to:S_WHATSAPP_NET}, delete)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "tos")
                .attribute("to", Jid.userServer())
                .attribute("type", "set")
                .content(deleteNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqDeleteTosRequest) obj;
        return Objects.equals(this.noticeId, that.noticeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noticeId);
    }

    @Override
    public String toString() {
        return "IqDeleteTosRequest[noticeId=" + noticeId + ']';
    }
}
