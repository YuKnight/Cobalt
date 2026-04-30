package com.github.auties00.cobalt.node.smax.voip;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps a {@code <waiting_room_toggle
 * enabled link-token media/>} payload in the {@code <call to="call">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutVoipWaitingRoomToggleCallLinkRequest")
public final class SmaxWaitingRoomToggleCallLinkRequest implements SmaxOperation.Request {
    /**
     * The desired waiting-room state on the wire, {@code "0"} for
     * disabled, {@code "1"} for enabled. Modelled as a raw string
     * for forward-compat with relay-side enum extensions.
     */
    private final String waitingRoomToggleEnabled;

    /**
     * The call-link token whose waiting-room state should be toggled.
     */
    private final String waitingRoomToggleLinkToken;

    /**
     * The media type the call link is configured for; either
     * {@code "audio"} or {@code "video"} on the wire.
     */
    private final String waitingRoomToggleMedia;

    /**
     * Constructs a request.
     *
     * @param waitingRoomToggleEnabled   the desired enabled state on
     *                                   the wire; never {@code null}
     * @param waitingRoomToggleLinkToken the call-link token; never
     *                                   {@code null}
     * @param waitingRoomToggleMedia     the media type; never
     *                                   {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxWaitingRoomToggleCallLinkRequest(String waitingRoomToggleEnabled,
                   String waitingRoomToggleLinkToken,
                   String waitingRoomToggleMedia) {
        this.waitingRoomToggleEnabled = Objects.requireNonNull(waitingRoomToggleEnabled, "waitingRoomToggleEnabled cannot be null");
        this.waitingRoomToggleLinkToken = Objects.requireNonNull(waitingRoomToggleLinkToken, "waitingRoomToggleLinkToken cannot be null");
        this.waitingRoomToggleMedia = Objects.requireNonNull(waitingRoomToggleMedia, "waitingRoomToggleMedia cannot be null");
    }

    /**
     * Returns the desired enabled state on the wire.
     *
     * @return the enabled string; never {@code null}
     */
    public String waitingRoomToggleEnabled() {
        return waitingRoomToggleEnabled;
    }

    /**
     * Returns the call-link token.
     *
     * @return the token; never {@code null}
     */
    public String waitingRoomToggleLinkToken() {
        return waitingRoomToggleLinkToken;
    }

    /**
     * Returns the media type.
     *
     * @return the media type; never {@code null}
     */
    public String waitingRoomToggleMedia() {
        return waitingRoomToggleMedia;
    }

    /**
     * Builds the outbound stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the {@code <call>}
     *         envelope around a {@code <waiting_room_toggle/>}
     *         payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutVoipWaitingRoomToggleCallLinkRequest",
            exports = "makeWaitingRoomToggleCallLinkRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var toggleNode = new NodeBuilder()
                .description("waiting_room_toggle")
                .attribute("enabled", waitingRoomToggleEnabled)
                .attribute("link-token", waitingRoomToggleLinkToken)
                .attribute("media", waitingRoomToggleMedia)
                .build();
        return new NodeBuilder()
                .description("call")
                .attribute("to", JidServer.call())
                .content(toggleNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxWaitingRoomToggleCallLinkRequest) obj;
        return Objects.equals(this.waitingRoomToggleEnabled, that.waitingRoomToggleEnabled)
                && Objects.equals(this.waitingRoomToggleLinkToken, that.waitingRoomToggleLinkToken)
                && Objects.equals(this.waitingRoomToggleMedia, that.waitingRoomToggleMedia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(waitingRoomToggleEnabled, waitingRoomToggleLinkToken, waitingRoomToggleMedia);
    }

    @Override
    public String toString() {
        return "SmaxWaitingRoomToggleCallLinkRequest[waitingRoomToggleEnabled=" + waitingRoomToggleEnabled
                + ", waitingRoomToggleLinkToken=" + waitingRoomToggleLinkToken
                + ", waitingRoomToggleMedia=" + waitingRoomToggleMedia + ']';
    }
}
