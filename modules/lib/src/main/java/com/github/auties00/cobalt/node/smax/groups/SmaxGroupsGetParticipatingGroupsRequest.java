package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetParticipatingGroupsRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseGetServerMixin")
public final class SmaxGroupsGetParticipatingGroupsRequest implements SmaxOperation.Request {
    /**
     * When {@code true}, the relay includes a per-group participant
     * list inside each {@code <group>} child.
     */
    private final boolean includeParticipants;

    /**
     * When {@code true}, the relay includes the per-group description
     * inside each {@code <group>} child.
     */
    private final boolean includeDescription;

    /**
     * Constructs a request.
     *
     * @param includeParticipants {@code true} to include participants
     *                            in the projection
     * @param includeDescription  {@code true} to include the
     *                            description in the projection
     */
    public SmaxGroupsGetParticipatingGroupsRequest(boolean includeParticipants, boolean includeDescription) {
        this.includeParticipants = includeParticipants;
        this.includeDescription = includeDescription;
    }

    /**
     * Returns whether participants are included.
     *
     * @return the flag
     */
    public boolean includeParticipants() {
        return includeParticipants;
    }

    /**
     * Returns whether description is included.
     *
     * @return the flag
     */
    public boolean includeDescription() {
        return includeDescription;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         {@code <participating/>} payload
     *
     * @implNote {@code WASmaxOutGroupsGetParticipatingGroupsRequest.makeGetParticipatingGroupsRequest}
     *           composes
     *           {@code <participating>HAS_OPTIONAL_CHILD(participants)
     *           HAS_OPTIONAL_CHILD(description)</participating>}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsGetParticipatingGroupsRequest",
            exports = "makeGetParticipatingGroupsRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var participatingBuilder = new NodeBuilder().description("participating");
        if (includeParticipants) {
            participatingBuilder.content(new NodeBuilder().description("participants").build());
        }
        if (includeDescription) {
            participatingBuilder.content(new NodeBuilder().description("description").build());
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", JidServer.groupOrCommunity())
                .attribute("type", "get")
                .content(participatingBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsGetParticipatingGroupsRequest) obj;
        return this.includeParticipants == that.includeParticipants
                && this.includeDescription == that.includeDescription;
    }

    @Override
    public int hashCode() {
        return Objects.hash(includeParticipants, includeDescription);
    }

    @Override
    public String toString() {
        return "SmaxGroupsGetParticipatingGroupsRequest[includeParticipants=" + includeParticipants
                + ", includeDescription=" + includeDescription + ']';
    }
}
