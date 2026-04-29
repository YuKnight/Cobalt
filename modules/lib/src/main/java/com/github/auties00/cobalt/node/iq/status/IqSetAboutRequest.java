package com.github.auties00.cobalt.node.iq.status;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound stanza variant — wraps the new about-text payload
 * in the canonical {@code <iq xmlns="status" type="set"><status>...
 * </status></iq>} envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebSetAboutJob")
public final class IqSetAboutRequest implements IqOperation.Request {
    /**
     * The new about-text value (UTF-8). Empty string clears the
     * about field.
     */
    private final String about;

    /**
     * Constructs a new request.
     *
     * @param about the new about text; never {@code null}
     * @throws NullPointerException if {@code about} is {@code null}
     */
    public IqSetAboutRequest(String about) {
        this.about = Objects.requireNonNull(about, "about cannot be null");
    }

    /**
     * Returns the new about text.
     *
     * @return the about-text string; never {@code null}
     */
    public String about() {
        return about;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <status>} payload
     *
     * @implNote {@code WAWebSetAboutJob.setAbout}:
     *           {@code wap("iq",{to:S_WHATSAPP_NET,type:"set",
     *           xmlns:"status",id}, wap("status", null, content))}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSetAboutJob",
            exports = "setAbout", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebSetAboutJob: wap("status", null, content)
        var statusNode = new NodeBuilder()
                .description("status")
                .content(about)
                .build();
        // WAWebSetAboutJob: wap("iq",{to:S_WHATSAPP_NET,type:"set",xmlns:"status",id}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "status")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(statusNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSetAboutRequest) obj;
        return Objects.equals(this.about, that.about);
    }

    @Override
    public int hashCode() {
        return Objects.hash(about);
    }

    @Override
    public String toString() {
        return "IqSetAboutRequest[about=" + about + ']';
    }
}
