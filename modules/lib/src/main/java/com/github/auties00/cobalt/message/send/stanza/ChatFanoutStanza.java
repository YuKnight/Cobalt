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
import java.util.Objects;

/**
 * Builds the outgoing {@code <message>} stanza for 1:1 chat and group-direct fanout.
 *
 * <p>In this mode the protobuf is encrypted individually for each device. When there is
 * exactly one payload, it targets a primary device (device ID 0), and the message is not
 * bot related, the {@code <enc>} node is placed directly under {@code <message>};
 * otherwise all {@code <enc>} nodes are wrapped in a {@code <participants>} container.
 *
 * @see GroupSkmsgFanoutStanza
 * @see ParticipantsStanza
 * @see TcTokenStanza
 * @see CsTokenStanza
 */
@WhatsAppWebModule(moduleName = "WAWebSendMsgCreateFanoutStanza")
@WhatsAppWebModule(moduleName = "WAWebSendDirectMsgToDeviceList")
public final class ChatFanoutStanza {
    /**
     * Prevents instantiation of this utility class.
     */
    private ChatFanoutStanza() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Builds the {@code <message>} stanza for chat or group-direct fanout.
     *
     * @param messageId             the message stanza ID
     * @param chatJid               the chat JID
     * @param type                  the stanza type attribute ({@code "text"},
     *                              {@code "media"}, {@code "reaction"}, {@code "poll"},
     *                              {@code "event"})
     * @param payloads              the per-device encrypted payloads
     * @param editAttribute         the edit attribute, or {@code null}
     * @param addressingMode        {@code "pn"} or {@code "lid"}, or {@code null}
     * @param deviceFanout          {@code "false"} for resends or bot feedback, or
     *                              {@code null}
     * @param mediaType             the enc mediatype attribute, or {@code null}
     * @param decryptFail           the enc decrypt-fail attribute, or {@code null}
     * @param nativeFlowName        the enc native_flow_name attribute, or {@code null}
     * @param contentBindings       per-recipient RCAT tags keyed by user JID, or
     *                              {@code null}
     * @param isBotRelated          whether this is a bot feedback or bot-targeted message
     * @param peerRecipientLid      the peer_recipient_lid, or {@code null}
     * @param peerRecipientPn       the peer_recipient_pn, or {@code null}
     * @param recipientPn           the recipient_pn, or {@code null}
     * @param peerRecipientUsername the peer_recipient_username, or {@code null}
     * @param identityNode          optional {@code <device-identity>}
     * @param metaNode              optional {@code <meta>}
     * @param bizNode               optional {@code <biz>}
     * @param botNode               optional {@code <bot>}
     * @param reportingNode         optional {@code <reporting>}
     * @param senderContentBinding  optional {@code <sender_content_binding>}
     * @param botMetadataNode       optional metadata-only {@code <bot>}
     * @param tctokenNode           optional {@code <tctoken>}
     * @param cstokenNode           optional {@code <cstoken>}, used as fallback when
     *                              {@code tctokenNode} is {@code null}
     * @param ctwaNode              optional {@code <ctwa_attribution>}
     * @param groupDirectSkmsgNode  optional empty {@code <enc type="skmsg">} for
     *                              group-direct fanout
     * @return a {@link NodeBuilder} for the {@code <message>} stanza
     * @throws NullPointerException if {@code messageId}, {@code chatJid}, {@code type},
     *                              or {@code payloads} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSendDirectMsgToDeviceList", exports = "sendDirectMsgToDeviceList",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static NodeBuilder build(
            String messageId,
            Jid chatJid,
            String type,
            List<MessageEncryptedPayload> payloads,
            String editAttribute,
            String addressingMode,
            String deviceFanout,
            String mediaType,
            String decryptFail,
            String nativeFlowName,
            Map<Jid, byte[]> contentBindings,
            boolean isBotRelated,
            Jid peerRecipientLid,
            Jid peerRecipientPn,
            Jid recipientPn,
            String peerRecipientUsername,
            Node identityNode,
            Node metaNode,
            Node bizNode,
            Node botNode,
            Node reportingNode,
            Node senderContentBinding,
            Node botMetadataNode,
            Node tctokenNode,
            Node cstokenNode,
            Node ctwaNode,
            Node groupDirectSkmsgNode
    ) {
        Objects.requireNonNull(messageId, "messageId");
        Objects.requireNonNull(chatJid, "chatJid");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(payloads, "payloads");

        var singlePrimary = payloads.size() == 1
                && payloads.getFirst().recipientJid() != null
                && payloads.getFirst().recipientJid().device() == 0
                && !isBotRelated;

        var needsIdentity = ParticipantsStanza.requiresIdentityNode(payloads);

        var builder = new NodeBuilder()
                .description("message")
                .attribute("id", messageId)
                .attribute("to", chatJid)
                .attribute("type", type)
                .attribute("edit", editAttribute)
                .attribute("device_fanout", deviceFanout)
                .attribute("addressing_mode", addressingMode)
                .attribute("peer_recipient_lid", peerRecipientLid)
                .attribute("peer_recipient_pn", peerRecipientPn)
                .attribute("recipient_pn", recipientPn)
                .attribute("peer_recipient_username", peerRecipientUsername);

        if (singlePrimary) {
            builder.content(buildEncNode(payloads.getFirst(), mediaType, decryptFail, nativeFlowName));
        } else {
            builder.content(buildParticipantsNode(payloads, mediaType, decryptFail, nativeFlowName, contentBindings));
        }

        // tctoken takes precedence; cstoken is the fallback when tctoken is absent
        var tokenNode = tctokenNode != null ? tctokenNode : cstokenNode;
        builder.content(
                botNode,
                groupDirectSkmsgNode,
                needsIdentity ? identityNode : null,
                bizNode,
                metaNode,
                senderContentBinding,
                botMetadataNode,
                reportingNode,
                tokenNode,
                ctwaNode
        );

        return builder;
    }

    /**
     * Builds the {@code <message>} stanza for chat or group-direct fanout without a
     * {@code <cstoken>} fallback.
     *
     * <p>Convenience overload that delegates to the full
     * {@link #build(String, Jid, String, List, String, String, String, String, String,
     * String, Map, boolean, Jid, Jid, Jid, String, Node, Node, Node, Node, Node, Node,
     * Node, Node, Node, Node, Node)} with {@code null} for {@code cstokenNode}.
     *
     * @param messageId             the message stanza ID
     * @param chatJid               the chat JID
     * @param type                  the stanza type attribute
     * @param payloads              the per-device encrypted payloads
     * @param editAttribute         the edit attribute, or {@code null}
     * @param addressingMode        the addressing mode, or {@code null}
     * @param deviceFanout          the device fanout flag, or {@code null}
     * @param mediaType             the enc mediatype attribute, or {@code null}
     * @param decryptFail           the enc decrypt-fail attribute, or {@code null}
     * @param nativeFlowName        the enc native_flow_name attribute, or {@code null}
     * @param contentBindings       per-recipient RCAT tags, or {@code null}
     * @param isBotRelated          whether this is a bot related message
     * @param peerRecipientLid      the peer_recipient_lid, or {@code null}
     * @param peerRecipientPn       the peer_recipient_pn, or {@code null}
     * @param recipientPn           the recipient_pn, or {@code null}
     * @param peerRecipientUsername the peer_recipient_username, or {@code null}
     * @param identityNode          optional {@code <device-identity>}
     * @param metaNode              optional {@code <meta>}
     * @param bizNode               optional {@code <biz>}
     * @param botNode               optional {@code <bot>}
     * @param reportingNode         optional {@code <reporting>}
     * @param senderContentBinding  optional {@code <sender_content_binding>}
     * @param botMetadataNode       optional metadata-only {@code <bot>}
     * @param tctokenNode           optional {@code <tctoken>}
     * @param ctwaNode              optional {@code <ctwa_attribution>}
     * @param groupDirectSkmsgNode  optional group-direct skmsg node
     * @return a {@link NodeBuilder} for the {@code <message>} stanza
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static NodeBuilder build(
            String messageId,
            Jid chatJid,
            String type,
            List<MessageEncryptedPayload> payloads,
            String editAttribute,
            String addressingMode,
            String deviceFanout,
            String mediaType,
            String decryptFail,
            String nativeFlowName,
            Map<Jid, byte[]> contentBindings,
            boolean isBotRelated,
            Jid peerRecipientLid,
            Jid peerRecipientPn,
            Jid recipientPn,
            String peerRecipientUsername,
            Node identityNode,
            Node metaNode,
            Node bizNode,
            Node botNode,
            Node reportingNode,
            Node senderContentBinding,
            Node botMetadataNode,
            Node tctokenNode,
            Node ctwaNode,
            Node groupDirectSkmsgNode
    ) {
        return build(
                messageId, chatJid, type, payloads,
                editAttribute, addressingMode, deviceFanout,
                mediaType, decryptFail, nativeFlowName,
                contentBindings, isBotRelated,
                peerRecipientLid, peerRecipientPn, recipientPn,
                peerRecipientUsername,
                identityNode, metaNode, bizNode, botNode,
                reportingNode, senderContentBinding, botMetadataNode,
                tctokenNode, null, ctwaNode, groupDirectSkmsgNode
        );
    }

    /**
     * Builds an {@code <enc>} node for a single device payload with all protocol
     * attributes.
     *
     * @param payload        the encrypted payload
     * @param mediaType      the mediatype attribute, or {@code null}
     * @param decryptFail    the decrypt-fail attribute, or {@code null}
     * @param nativeFlowName the native_flow_name attribute, or {@code null}
     * @return the enc node
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static Node buildEncNode(
            MessageEncryptedPayload payload,
            String mediaType,
            String decryptFail,
            String nativeFlowName
    ) {
        return new NodeBuilder()
                .description("enc")
                .attribute("v", String.valueOf(MessageEncryption.CIPHERTEXT_VERSION))
                .attribute("type", payload.type().protocolValue())
                .attribute("mediatype", mediaType)
                .attribute("decrypt-fail", decryptFail)
                .attribute("native_flow_name", nativeFlowName)
                .content(payload.ciphertext())
                .build();
    }

    /**
     * Builds the {@code <participants>} node for multi-device chat fanout, including
     * per-device content bindings when present.
     *
     * @param payloads        the per-device encrypted payloads
     * @param mediaType       the mediatype attribute, or {@code null}
     * @param decryptFail     the decrypt-fail attribute, or {@code null}
     * @param nativeFlowName  the native_flow_name attribute, or {@code null}
     * @param contentBindings per-recipient RCAT tags, or {@code null}
     * @return the participants node
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static Node buildParticipantsNode(
            List<MessageEncryptedPayload> payloads,
            String mediaType,
            String decryptFail,
            String nativeFlowName,
            Map<Jid, byte[]> contentBindings
    ) {
        var children = new ArrayList<Node>(payloads.size());
        for (var payload : payloads) {
            if (payload.recipientJid() == null) {
                continue;
            }

            var encNode = buildEncNode(payload, mediaType, decryptFail, nativeFlowName);

            Node contentBindingNode = null;
            if (contentBindings != null) {
                var binding = contentBindings.get(payload.recipientJid().toUserJid());
                if (binding != null) {
                    contentBindingNode = new NodeBuilder()
                            .description("content_binding")
                            .content(binding)
                            .build();
                }
            }

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
}
