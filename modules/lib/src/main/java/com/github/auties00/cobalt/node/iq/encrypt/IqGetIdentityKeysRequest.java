package com.github.auties00.cobalt.node.iq.encrypt;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.util.RandomIdUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The outbound {@code <iq xmlns="encrypt" type="get">} stanza variant
 * — wraps the {@code <identity/>} payload with one {@code <user/>}
 * grandchild per requested device.
 */
@WhatsAppWebModule(moduleName = "WAWebGetIdentityKeysJob")
public final class IqGetIdentityKeysRequest implements IqOperation.Request {
    /**
     * The list of device JIDs whose identity keys are being
     * requested. Each entry is routed verbatim into the {@code jid}
     * attribute of one {@code <user/>} grandchild.
     */
    private final List<Jid> deviceJids;

    /**
     * Constructs a new get-identity-keys request.
     *
     * @param deviceJids the list of device JIDs to query; never
     *                   {@code null} and never empty
     * @throws NullPointerException     if {@code deviceJids} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code deviceJids} is empty
     */
    public IqGetIdentityKeysRequest(List<Jid> deviceJids) {
        Objects.requireNonNull(deviceJids, "deviceJids cannot be null");
        if (deviceJids.isEmpty()) {
            throw new IllegalArgumentException("deviceJids cannot be empty");
        }
        this.deviceJids = List.copyOf(deviceJids);
    }

    /**
     * Returns the unmodifiable list of device JIDs being queried.
     *
     * @return the device JIDs; never {@code null} or empty
     */
    public List<Jid> deviceJids() {
        return deviceJids;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <identity/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebGetIdentityKeysJob",
            exports = "getAndStoreIdentityKeys", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var userNodes = new ArrayList<Node>(deviceJids.size());
        for (var deviceJid : deviceJids) {
            var userNode = new NodeBuilder()
                    .description("user")
                    .attribute("jid", deviceJid)
                    .build();
            userNodes.add(userNode);
        }
        var identityNode = new NodeBuilder()
                .description("identity")
                .content(userNodes)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("id", RandomIdUtils.newId())
                .attribute("xmlns", "encrypt")
                .attribute("type", "get")
                .attribute("to", JidServer.user())
                .content(identityNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqGetIdentityKeysRequest) obj;
        return Objects.equals(this.deviceJids, that.deviceJids);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceJids);
    }

    @Override
    public String toString() {
        return "IqGetIdentityKeysRequest[deviceJids=" + deviceJids + ']';
    }
}
