package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.business.BusinessVerifiedName;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.interactive.InteractiveMessage;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.time.Instant;
import java.util.Objects;

/**
 * Builds the {@code <biz>} stanza child node that carries verified business account
 * privacy mode metadata and native flow attributes.
 *
 * <p>The privacy mode form of the node is included only when the recipient has a
 * verified business name with all three privacy mode fields populated
 * ({@code host_storage}, {@code actual_actors}, {@code privacy_mode_ts}). A native flow
 * form is also produced when the message protobuf carries a native flow name.
 *
 * @see ChatFanoutStanza
 */
@WhatsAppWebModule(moduleName = "WAWebSendMsgCreateFanoutStanza")
public final class BizStanza {
    /**
     * Store used to look up the recipient's verified business name.
     */
    private final WhatsAppStore store;

    /**
     * Creates a new biz stanza builder with the given store.
     *
     * @param store the WhatsApp store for contact lookups
     * @throws NullPointerException if {@code store} is {@code null}
     */
    public BizStanza(WhatsAppStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    /**
     * Builds the {@code <biz>} node for the given chat recipient with no native flow
     * context.
     *
     * <p>Returns {@code null} if the recipient has no verified business name with privacy
     * mode populated.
     *
     * @param chatJid the recipient chat JID
     * @return the biz node, or {@code null} if not applicable
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node build(Jid chatJid) {
        return build(chatJid, null, false);
    }

    /**
     * Builds the {@code <biz>} node for the given chat recipient, including the native
     * flow name attribute and an interactive child when applicable.
     *
     * <p>Resolution order: when the contact has a verified business name with privacy
     * mode, builds a {@code <biz>} node with the {@code host_storage},
     * {@code actual_actors}, {@code privacy_mode_ts} and {@code native_flow_name}
     * attributes. Otherwise, when {@code nativeFlowName} is non-null and
     * {@code isNativeFlowInteractive} is {@code true}, builds a {@code <biz>} node with
     * an {@code <interactive>} child containing a {@code <native_flow>} element.
     * Otherwise, when {@code nativeFlowName} is non-null, builds a simple {@code <biz>}
     * node with just the {@code native_flow_name} attribute. Otherwise returns
     * {@code null}.
     *
     * @param chatJid                 the recipient chat JID
     * @param nativeFlowName          the native flow name from the protobuf, or
     *                                {@code null}
     * @param isNativeFlowInteractive whether this is a native flow interactive message
     * @return the biz node, or {@code null} if not applicable
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node build(Jid chatJid, String nativeFlowName, boolean isNativeFlowInteractive) {
        var verifiedName = store.findVerifiedBusinessName(chatJid)
                .orElse(null);
        if (verifiedName != null && verifiedName.hasPrivacyMode()) {
            var hostStorage = verifiedName.hostStorage()
                    .map(BusinessVerifiedName.HostStorageType::index)
                    .orElse(null);
            var actualActors = verifiedName.actualActors()
                    .map(BusinessVerifiedName.ActualActorsType::index)
                    .orElse(null);
            var privacyModeTs = verifiedName.privacyModeTimestamp()
                    .map(Instant::getEpochSecond)
                    .orElse(null);

            return new NodeBuilder()
                    .description("biz")
                    .attribute("host_storage", hostStorage)
                    .attribute("actual_actors", actualActors)
                    .attribute("privacy_mode_ts", privacyModeTs)
                    .attribute("native_flow_name", nativeFlowName)
                    .build();
        }

        if (nativeFlowName != null && isNativeFlowInteractive) {
            var nativeFlowNode = new NodeBuilder()
                    .description("native_flow")
                    .attribute("name", nativeFlowName)
                    .build();
            var interactiveNode = new NodeBuilder()
                    .description("interactive")
                    .attribute("v", "1")
                    .attribute("type", "native_flow")
                    .content(nativeFlowNode)
                    .build();
            return new NodeBuilder()
                    .description("biz")
                    .content(interactiveNode)
                    .build();
        }

        if (nativeFlowName != null) {
            return new NodeBuilder()
                    .description("biz")
                    .attribute("native_flow_name", nativeFlowName)
                    .build();
        }

        return null;
    }

    /**
     * Builds the {@code <biz>} node for group messages that carry a payment-info native
     * flow interactive message.
     *
     * <p>Returns {@code null} for any other message type.
     *
     * @param container the message container
     * @return the biz node, or {@code null}
     * @see GroupSkmsgFanoutStanza
     */
    @WhatsAppWebExport(moduleName = "WAWebSendGroupSkmsgJob", exports = "encryptAndSendSenderKeyMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node buildGroup(MessageContainer container) {
        if (!(container.content() instanceof InteractiveMessage im)) {
            return null;
        }

        var imc = im.content();
        if (imc.isEmpty() || !(imc.get() instanceof InteractiveMessage.NativeFlowMessage nativeFlow)) {
            return null;
        }

        var buttons = nativeFlow.buttons();
        if (buttons.isEmpty()) {
            return null;
        }

        var nativeFlowName = buttons.getFirst().name();
        if (nativeFlowName.isEmpty() || !"payment_info".equals(nativeFlowName.get())) {
            return null;
        }

        var nativeFlowNode = new NodeBuilder()
                .description("native_flow")
                .attribute("name", nativeFlowName.get())
                .build();
        var interactiveNode = new NodeBuilder()
                .description("interactive")
                .attribute("v", "1")
                .attribute("type", "native_flow")
                .content(nativeFlowNode)
                .build();
        return new NodeBuilder()
                .description("biz")
                .content(interactiveNode)
                .build();
    }
}
