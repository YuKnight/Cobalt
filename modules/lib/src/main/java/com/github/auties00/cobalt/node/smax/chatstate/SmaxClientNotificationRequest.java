package com.github.auties00.cobalt.node.smax.chatstate;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the {@code to} (chat JID)
 * attribute and one of the two state-type children
 * ({@link SmaxClientNotificationComposing} or {@link SmaxClientNotificationPaused}) into a
 * {@code <chatstate/>} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutChatstateClientNotificationRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutChatstateStateTypes")
public final class SmaxClientNotificationRequest implements SmaxOperation.Request {
    /**
     * The chat JID receiving the indicator.
     */
    private final Jid chatstateTo;

    /**
     * The state-type — either a {@link SmaxClientNotificationComposing} or a
     * {@link SmaxClientNotificationPaused}; never {@code null}.
     */
    private final SmaxClientNotificationStateType stateType;

    /**
     * Constructs a new client-notification request.
     *
     * @param chatstateTo the chat JID; never {@code null}
     * @param stateType   the state-type payload; never {@code null}
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    public SmaxClientNotificationRequest(Jid chatstateTo, SmaxClientNotificationStateType stateType) {
        this.chatstateTo = Objects.requireNonNull(chatstateTo, "chatstateTo cannot be null");
        this.stateType = Objects.requireNonNull(stateType, "stateType cannot be null");
    }

    /**
     * Returns the chat JID being notified.
     *
     * @return the chat JID; never {@code null}
     */
    public Jid chatstateTo() {
        return chatstateTo;
    }

    /**
     * Returns the state-type payload.
     *
     * @return the state-type; never {@code null}
     */
    public SmaxClientNotificationStateType stateType() {
        return stateType;
    }

    /**
     * Builds the outbound chatstate stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the
     *         {@code <chatstate to=…><composing|paused/></chatstate>}
     *         envelope
     *
     * @implNote {@code WASmaxOutChatstateClientNotificationRequest.makeClientNotificationRequest}
     *           merges the state-type mixin
     *           ({@code WASmaxOutChatstateComposingMixin} /
     *           {@code WASmaxOutChatstatePausedMixin}) over the
     *           {@code <chatstate to=JID>} envelope.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutChatstateClientNotificationRequest",
            exports = "makeClientNotificationRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var stateChild = stateType.toNode().build();
        return new NodeBuilder()
                .description("chatstate")
                .attribute("to", chatstateTo)
                .content(stateChild);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxClientNotificationRequest) obj;
        return Objects.equals(this.chatstateTo, that.chatstateTo)
                && Objects.equals(this.stateType, that.stateType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatstateTo, stateType);
    }

    @Override
    public String toString() {
        return "SmaxClientNotificationRequest[chatstateTo=" + chatstateTo
                + ", stateType=" + stateType + ']';
    }
}
