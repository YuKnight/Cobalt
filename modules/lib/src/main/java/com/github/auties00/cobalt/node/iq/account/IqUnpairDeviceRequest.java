package com.github.auties00.cobalt.node.iq.account;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.Objects;

/**
 * The outbound {@code <iq xmlns="md" type="set">} stanza variant — wraps
 * the {@code <remove-companion-device jid reason/>} payload.
 */
@WhatsAppWebModule(moduleName = "WAWebUnpairDeviceJob")
public final class IqUnpairDeviceRequest implements IqOperation.Request {
    /**
     * The companion-device JID being unpaired. Routed verbatim into the
     * {@code <remove-companion-device>} child's {@code jid} attribute.
     */
    private final Jid deviceJid;

    /**
     * The free-form caller-supplied unpair reason. Routed verbatim into
     * the {@code <remove-companion-device>} child's {@code reason}
     * attribute and surfaced server-side for telemetry.
     */
    private final String reason;

    /**
     * Constructs a new unpair-device request.
     *
     * @param deviceJid the companion-device JID to unpair; never
     *                  {@code null}
     * @param reason    the caller-supplied free-form reason string;
     *                  never {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public IqUnpairDeviceRequest(Jid deviceJid, String reason) {
        this.deviceJid = Objects.requireNonNull(deviceJid, "deviceJid cannot be null");
        this.reason = Objects.requireNonNull(reason, "reason cannot be null");
    }

    /**
     * Returns the companion-device JID being unpaired.
     *
     * @return the device JID; never {@code null}
     */
    public Jid deviceJid() {
        return deviceJid;
    }

    /**
     * Returns the free-form unpair reason.
     *
     * @return the reason string; never {@code null}
     */
    public String reason() {
        return reason;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <remove-companion-device>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUnpairDeviceJob",
            exports = "unpairDevice", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebUnpairDeviceJob: wap("remove-companion-device", {jid, reason})
        var removePayload = new NodeBuilder()
                .description("remove-companion-device")
                .attribute("jid", deviceJid)
                .attribute("reason", reason)
                .build();
        // WAWebUnpairDeviceJob: wap("iq", {to:S_WHATSAPP_NET, type:"set", id, xmlns:"md"}, ...)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "md")
                .attribute("to", Jid.userServer())
                .attribute("type", "set")
                .content(removePayload);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqUnpairDeviceRequest) obj;
        return Objects.equals(this.deviceJid, that.deviceJid)
                && Objects.equals(this.reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceJid, reason);
    }

    @Override
    public String toString() {
        return "IqUnpairDeviceRequest[deviceJid=" + deviceJid
                + ", reason=" + reason + ']';
    }
}
