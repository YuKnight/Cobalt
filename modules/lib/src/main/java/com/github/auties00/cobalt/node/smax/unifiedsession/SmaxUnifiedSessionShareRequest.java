package com.github.auties00.cobalt.node.smax.unifiedsession;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;

/**
 * The outbound stanza variant. Wraps the {@code <unified_session
 * id/>} payload in the bare {@code <ib>} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutUnifiedSessionShareRequest")
public final class SmaxUnifiedSessionShareRequest implements SmaxOperation.Request {
    /**
     * The unified-session id token to share with the relay.
     */
    private final String unifiedSessionId;

    /**
     * Constructs a request with the given unified-session id.
     *
     * @param unifiedSessionId the unified-session id token. Never
     *                         {@code null}
     * @throws NullPointerException if {@code unifiedSessionId} is
     *                              {@code null}
     */
    public SmaxUnifiedSessionShareRequest(String unifiedSessionId) {
        this.unifiedSessionId = Objects.requireNonNull(unifiedSessionId, "unifiedSessionId cannot be null");
    }

    /**
     * Returns the unified-session id token.
     *
     * @return the id. Never {@code null}
     */
    public String unifiedSessionId() {
        return unifiedSessionId;
    }

    /**
     * Builds the outbound stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the {@code <ib>} envelope
     *         and the {@code <unified_session/>} payload
     *
     * @implNote {@code WASmaxOutUnifiedSessionShareRequest.makeShareRequest}
     *           emits {@code <ib><unified_session
     *           id=CUSTOM_STRING(t)/></ib>}. No id/xmlns/to attributes
     *           are populated on the outer envelope.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutUnifiedSessionShareRequest",
            exports = "makeShareRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var unifiedSessionNode = new NodeBuilder()
                .description("unified_session")
                .attribute("id", unifiedSessionId)
                .build();
        return new NodeBuilder()
                .description("ib")
                .content(unifiedSessionNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxUnifiedSessionShareRequest) obj;
        return Objects.equals(this.unifiedSessionId, that.unifiedSessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unifiedSessionId);
    }

    @Override
    public String toString() {
        return "SmaxUnifiedSessionShareRequest[unifiedSessionId=" + unifiedSessionId + ']';
    }
}
