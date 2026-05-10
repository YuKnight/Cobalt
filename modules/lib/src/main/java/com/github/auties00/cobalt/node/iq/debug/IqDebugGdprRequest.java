package com.github.auties00.cobalt.node.iq.debug;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound stanza variant — wraps a single
 * {@code <gdpr action="delete" [report_type=...]/>} child in the
 * canonical {@code <iq xmlns="urn:xmpp:whatsapp:account"
 * type="get"/>} envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebGdprHookUtils")
public final class IqDebugGdprRequest implements IqOperation.Request {
    /**
     * The report type to cancel.
     */
    private final IqDebugGdprReportType reportType;

    /**
     * Constructs a new request.
     *
     * @param reportType the report type; never {@code null}
     * @throws NullPointerException if {@code reportType} is
     *                              {@code null}
     */
    public IqDebugGdprRequest(IqDebugGdprReportType reportType) {
        this.reportType = Objects.requireNonNull(reportType, "reportType cannot be null");
    }

    /**
     * Returns the report type being cancelled.
     *
     * @return the report type; never {@code null}
     */
    public IqDebugGdprReportType reportType() {
        return reportType;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <gdpr>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebDebugGDPR",
            exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebGdprHookUtils",
            exports = "getGdprIq", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebGdprHookUtils.getGdprIq: wap("gdpr",{action:"delete", report_type? })
        var gdprBuilder = new NodeBuilder()
                .description("gdpr")
                .attribute("action", "delete");
        var wireType = reportType.wire().orElse(null);
        if (wireType != null) {
            gdprBuilder.attribute("report_type", wireType);
        }
        // WAWebGdprHookUtils.getGdprIq: wap("iq",{xmlns:"urn:xmpp:whatsapp:account",to:S_WHATSAPP_NET,type:"get",id}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "urn:xmpp:whatsapp:account")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(gdprBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqDebugGdprRequest) obj;
        return this.reportType == that.reportType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportType);
    }

    @Override
    public String toString() {
        return "IqDebugGdprRequest[reportType=" + reportType + ']';
    }
}
