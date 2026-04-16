package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.message.send.crypto.MessageEncryption;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryptedPayload;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds {@code <participants>} stanza nodes that wrap per-device
 * encrypted payloads and optional content-binding tags.
 *
 * @apiNote WAWebSendMsgCreateFanoutStanza: wraps per-device payloads
 * in {@code <to jid=...><enc ...>ciphertext</enc></to>} under
 * {@code <participants>}.
 * WAWebSendGroupSkmsgJob: same structure with {@code decrypt-fail="hide"}
 * for SK distribution, plus optional {@code <content_binding>} children.
 */
@WhatsAppWebModule(moduleName = "WAWebSendMsgCreateFanoutStanza")
@WhatsAppWebModule(moduleName = "WAWebSendGroupSkmsgJob")
public final class ParticipantsStanza {
    /**
     * Prevents instantiation of this utility class.
     */
    private ParticipantsStanza() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Builds a {@code <participants>} node for sender-key distribution,
     * optionally including per-device content-binding (RCAT) tags.
     *
     * <p>Each {@code <enc>} child has {@code decrypt-fail="hide"} because
     * distribution messages should never produce a visible placeholder.
     * When a content-binding tag exists for a device's user JID, a
     * {@code <content_binding>} child is appended to the {@code <to>} node.
     *
     * @param payloads        the per-device encrypted SK distribution payloads
     * @param contentBindings per-recipient RCAT tags keyed by user JID,
     *                        or {@code null} if RCAT is not applicable
     * @param decryptFail     the decrypt-fail attribute for the protobuf
     *                        enc nodes (from the original message), or
     *                        {@code null} to use {@code "hide"}
     * @return the {@code <participants>} node, or {@code null} if
     *         {@code payloads} is empty
     *
     * @apiNote WAWebSendGroupSkmsgJob: wraps SK distribution payloads in
     * {@code <to jid=...><enc v="2" type="..." decrypt-fail="...">},
     * with optional {@code <content_binding>} per device from the RCAT map.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendGroupSkmsgJob", exports = "encryptAndSendSenderKeyMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Node buildSenderKeyDistribution(
            List<MessageEncryptedPayload> payloads,
            Map<Jid, byte[]> contentBindings,
            String decryptFail
    ) {
        if (payloads == null || payloads.isEmpty()) {
            return null;
        }

        var children = new ArrayList<Node>(payloads.size());
        for (var payload : payloads) {
            if (payload.recipientJid() == null) {
                continue;
            }

            var encNode = new NodeBuilder()
                    .description("enc")
                    .attribute("v", String.valueOf(MessageEncryption.CIPHERTEXT_VERSION))
                    .attribute("type", payload.type().protocolValue())
                    .attribute("decrypt-fail", decryptFail != null ? decryptFail : "hide")
                    .content(payload.ciphertext())
                    .build();

            // WAWebSendGroupSkmsgJob: optional <content_binding> per device
            var contentBindingNode = resolveContentBinding(
                    payload.recipientJid(), contentBindings);

            var toNode = new NodeBuilder()
                    .description("to")
                    .attribute("jid", payload.recipientJid())
                    .content(encNode, contentBindingNode)
                    .build();
            children.add(toNode);
        }

        return new NodeBuilder()
                .description("participants")
                .content(children)
                .build();
    }

    /**
     * Builds a {@code <participants>} node containing only
     * content-binding tags for devices that already have sender keys.
     *
     * <p>This is used when no sender-key distribution is needed but
     * RCAT content bindings still need to be delivered to existing
     * SK recipients.
     *
     * @param devices         the existing SK device JIDs
     * @param contentBindings per-recipient RCAT tags keyed by user JID
     * @return the {@code <participants>} node, or {@code null} if no
     *         content bindings match the device list
     *
     * @apiNote WAWebSendGroupSkmsgJob: when no SK distribution is needed
     * but content bindings exist ({@code s != null}), builds participants
     * with just {@code <to jid=...><content_binding>tag</content_binding></to>}
     * for each existing SK device.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendGroupSkmsgJob", exports = "encryptAndSendSenderKeyMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Node buildContentBindingOnly(
            List<Jid> devices,
            Map<Jid, byte[]> contentBindings
    ) {
        if (contentBindings == null || contentBindings.isEmpty() || devices == null) {
            return null;
        }

        var children = new ArrayList<Node>();
        for (var device : devices) {
            var bindingNode = resolveContentBinding(device, contentBindings);
            if (bindingNode == null) {
                continue;
            }
            var toNode = new NodeBuilder()
                    .description("to")
                    .attribute("jid", device)
                    .content(bindingNode)
                    .build();
            children.add(toNode);
        }

        return children.isEmpty() ? null : new NodeBuilder()
                .description("participants")
                .content(children)
                .build();
    }

    /**
     * Returns whether any payload is a pre-key message, requiring a
     * {@code <device-identity>} node in the stanza.
     *
     * @param payloads the encrypted payloads
     * @return {@code true} if at least one is a pre-key message
     *
     * @apiNote WAWebSendMsgCreateFanoutStanza: sets
     * {@code shouldHaveIdentity = true} when any result has type Pkmsg.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static boolean requiresIdentityNode(List<MessageEncryptedPayload> payloads) {
        return payloads != null
                && payloads.stream()
                        .anyMatch(MessageEncryptedPayload::isPreKeyMessage);
    }

    /**
     * Resolves the {@code <content_binding>} node for a device from
     * the RCAT map, matching by user JID.
     *
     * @apiNote WAWebSendGroupSkmsgJob: looks up the content binding
     * by converting the device JID to a user JID via
     * {@code widToUserJid(asUserWidOrThrow(device))}.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendGroupSkmsgJob", exports = "encryptAndSendSenderKeyMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static Node resolveContentBinding(Jid deviceJid, Map<Jid, byte[]> contentBindings) {
        if (contentBindings == null) {
            return null;
        }

        var userJid = deviceJid.toUserJid();
        var binding = contentBindings.get(userJid);
        if (binding == null) {
            return null;
        }

        return new NodeBuilder()
                .description("content_binding")
                .content(binding)
                .build();
    }
}
