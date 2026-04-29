package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsReportMessagesRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
public final class SmaxGroupsReportMessagesRequest implements SmaxOperation.Request {
    /**
     * The group JID hosting the reported message.
     */
    private final Jid groupJid;

    /**
     * The stanza-id of the reported message.
     */
    private final String reportMessageId;

    /**
     * Constructs a request.
     *
     * @param groupJid        the group JID; never {@code null}
     * @param reportMessageId the reported message id; never
     *                        {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public SmaxGroupsReportMessagesRequest(Jid groupJid, String reportMessageId) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        this.reportMessageId = Objects.requireNonNull(reportMessageId, "reportMessageId cannot be null");
    }

    /**
     * Returns the group JID.
     *
     * @return the group JID
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns the reported message id.
     *
     * @return the message id
     */
    public String reportMessageId() {
        return reportMessageId;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         report payload
     *
     * @implNote {@code WASmaxOutGroupsReportMessagesRequest.makeReportMessagesRequest}
     *           composes
     *           {@code <reports><report message_id=STANZA_ID(t)/></reports>}
     *           wrapped in
     *           {@code WASmaxOutGroupsBaseSetGroupMixin}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsReportMessagesRequest",
            exports = "makeReportMessagesRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // smax("report", {message_id: STANZA_ID(t)})
        var reportNode = new NodeBuilder()
                .description("report")
                .attribute("message_id", reportMessageId)
                .build();
        // smax("reports", null, report)
        var reportsNode = new NodeBuilder()
                .description("reports")
                .content(reportNode)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(reportsNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsReportMessagesRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.reportMessageId, that.reportMessageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, reportMessageId);
    }

    @Override
    public String toString() {
        return "SmaxGroupsReportMessagesRequest[groupJid=" + groupJid
                + ", reportMessageId=" + reportMessageId + ']';
    }
}
