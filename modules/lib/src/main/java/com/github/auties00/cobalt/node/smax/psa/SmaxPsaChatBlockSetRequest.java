package com.github.auties00.cobalt.node.smax.psa;

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
 * The outbound stanza variant — wraps a {@code <blocking action="…"/>}
 * payload in the canonical {@code <iq xmlns="w:comms:chat" type="set"
 * to="s.whatsapp.net">} envelope.
 *
 * <p>The {@code blockingAction} field is a free-form opaque string at
 * the WA Web wire layer ({@code CUSTOM_STRING}) — typed callers
 * generally pass one of {@link SmaxPsaChatBlockGet.BlockingStatus}'s
 * literals to flip the flag, but the relay accepts any non-empty
 * string and rejects unknown literals server-side rather than at the
 * stanza-builder level.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPsaChatBlockSetRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutPsaBaseIQSetRequestMixin")
public final class SmaxPsaChatBlockSetRequest implements SmaxOperation.Request {
    /**
     * The free-form action string carried by the {@code action}
     * attribute of the {@code <blocking/>} child.
     */
    private final String blockingAction;

    /**
     * Constructs a request.
     *
     * @param blockingAction the action string; never {@code null}
     * @throws NullPointerException if {@code blockingAction} is
     *                              {@code null}
     */
    public SmaxPsaChatBlockSetRequest(String blockingAction) {
        this.blockingAction = Objects.requireNonNull(blockingAction, "blockingAction cannot be null");
    }

    /**
     * Returns the action string.
     *
     * @return the action string; never {@code null}
     */
    public String blockingAction() {
        return blockingAction;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <blocking action="…"/>} child
     *
     * @implNote {@code WASmaxOutPsaChatBlockSetRequest.makeChatBlockSetRequest}
     *           composes {@code WASmaxOutPsaBaseIQSetRequestMixin}
     *           ({@code id=generateId()}, {@code type="set"}) over
     *           {@code <iq xmlns="w:comms:chat" to="s.whatsapp.net">}
     *           with a single {@code <blocking action="…"/>} child.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPsaChatBlockSetRequest",
            exports = "makeChatBlockSetRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutPsaChatBlockSetRequest: smax("blocking", {action: CUSTOM_STRING(t)})
        var blockingNode = new NodeBuilder()
                .description("blocking")
                .attribute("action", blockingAction)
                .build();
        // smax("iq", {to: S_WHATSAPP_NET, xmlns: "w:comms:chat", id: generateId(), type: "set"})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:comms:chat")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(blockingNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxPsaChatBlockSetRequest) obj;
        return Objects.equals(this.blockingAction, that.blockingAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockingAction);
    }

    @Override
    public String toString() {
        return "SmaxPsaChatBlockSetRequest[blockingAction=" + blockingAction + ']';
    }
}
