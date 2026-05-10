package com.github.auties00.cobalt.node.smax.chatstate;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.NodeBuilder;

/**
 * Sealed disjunction over the two state-type children supported by
 * the WA Web {@code WASmaxOutChatstateStateTypes.mergeStateTypes}
 * dispatcher.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutChatstateStateTypes")
public sealed interface SmaxClientNotificationStateType permits SmaxClientNotificationComposing, SmaxClientNotificationPaused {
    /**
     * Builds the state-type child node. Dynamic dispatch over the
     * sealed permits realises the WA Web
     * {@code WASmaxOutChatstateStateTypes.mergeStateTypes} switch.
     *
     * @return a {@link NodeBuilder} carrying the state-type child
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutChatstateStateTypes",
            exports = "mergeStateTypes", adaptation = WhatsAppAdaptation.ADAPTED)
    NodeBuilder toNode();
}
