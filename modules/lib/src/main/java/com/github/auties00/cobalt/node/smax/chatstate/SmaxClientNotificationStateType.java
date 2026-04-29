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
 * Sealed disjunction over the two state-type children supported by
 * the WA Web {@code WASmaxOutChatstateStateTypes.mergeStateTypes}
 * dispatcher.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutChatstateStateTypes")
public sealed interface SmaxClientNotificationStateType permits SmaxClientNotificationComposing, SmaxClientNotificationPaused {
    /**
     * Builds the state-type child node.
     *
     * @return a {@link NodeBuilder} carrying the state-type child
     */
    NodeBuilder toNode();
}
