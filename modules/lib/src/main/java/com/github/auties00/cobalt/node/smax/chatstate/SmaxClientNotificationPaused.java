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
 * The {@code SmaxClientNotificationPaused} state-type — the user has stopped typing.
 *
 * @implNote {@code WASmaxOutChatstatePausedMixin.mergePausedMixin}
 *           emits {@code smax("paused", null)} — a bare,
 *           attribute-less child.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutChatstatePausedMixin")
public final class SmaxClientNotificationPaused implements SmaxClientNotificationStateType {
    /**
     * Constructs a new {@code SmaxClientNotificationPaused} state-type.
     */
    public SmaxClientNotificationPaused() {
    }

    /**
     * Builds the {@code <paused/>} child node.
     *
     * @return a {@link NodeBuilder} carrying the child
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutChatstatePausedMixin",
            exports = "mergePausedMixin", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        return new NodeBuilder()
                .description("paused");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return SmaxClientNotificationPaused.class.hashCode();
    }

    @Override
    public String toString() {
        return "SmaxClientNotificationPaused[]";
    }
}
