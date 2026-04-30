package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.message.send.crypto.MessageEncryption;
import com.github.auties00.cobalt.message.MessageEncryptionType;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;

import java.util.Objects;

/**
 * Builds the outgoing {@code <message>} stanza for group sender-key (SKMSG) fanout.
 *
 * <p>In this mode the SKMSG ciphertext is sent once in a single
 * {@code <enc type="skmsg">} node. Sender-key distribution messages for new members are
 * provided as a pre-built {@code <participants>} node alongside the SKMSG.
 *
 * @see ChatFanoutStanza
 * @see ParticipantsStanza
 */
@WhatsAppWebModule(moduleName = "WAWebSendGroupSkmsgJob")
public final class GroupSkmsgFanoutStanza {
    /**
     * Prevents instantiation of this utility class.
     */
    private GroupSkmsgFanoutStanza() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Builds the {@code <message>} stanza for group SKMSG fanout.
     *
     * @param messageId            the message stanza ID
     * @param groupJid             the group JID
     * @param type                 the stanza type attribute
     * @param phash                the participant hash (V2), or {@code null} for bot
     *                             feedback messages where phash is dropped
     * @param skmsgCiphertext      the SKMSG-encrypted ciphertext, or {@code null} for
     *                             bot feedback messages where the enc node is omitted
     *                             (delivery happens only via {@code <bot>})
     * @param mediaType            the enc mediatype, or {@code null}
     * @param decryptFail          the decrypt-fail attribute, or {@code null}
     * @param editAttribute        the edit attribute, or {@code null}
     * @param addressingMode       {@code "pn"} or {@code "lid"}
     * @param skDistributionNode   optional {@code <participants>} with SK distribution
     * @param identityNode         optional {@code <device-identity>}
     * @param metaNode             optional {@code <meta>}
     * @param bizNode              optional {@code <biz>}
     * @param botNode              optional {@code <bot>}
     * @param reportingNode        optional {@code <reporting>}
     * @param senderContentBinding optional {@code <sender_content_binding>}
     * @return a {@link NodeBuilder} for the {@code <message>} stanza
     * @throws NullPointerException if {@code messageId}, {@code groupJid}, {@code type},
     *                              or {@code addressingMode} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendGroupSkmsgJob", exports = "encryptAndSendSenderKeyMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static NodeBuilder build(
            String messageId,
            Jid groupJid,
            String type,
            String phash,
            byte[] skmsgCiphertext,
            String mediaType,
            String decryptFail,
            String editAttribute,
            String addressingMode,
            Node skDistributionNode,
            Node identityNode,
            Node metaNode,
            Node bizNode,
            Node botNode,
            Node reportingNode,
            Node senderContentBinding
    ) {
        Objects.requireNonNull(messageId, "messageId");
        Objects.requireNonNull(groupJid, "groupJid");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(addressingMode, "addressingMode");

        // Bot feedback messages omit the enc node entirely; delivery happens only via
        // the <bot> child of the parent stanza.
        var skmsgEncNode = skmsgCiphertext != null
                ? new NodeBuilder()
                        .description("enc")
                        .attribute("v", String.valueOf(MessageEncryption.CIPHERTEXT_VERSION))
                        .attribute("type", MessageEncryptionType.SKMSG.protocolValue())
                        .attribute("mediatype", mediaType)
                        .attribute("decrypt-fail", decryptFail)
                        .content(skmsgCiphertext)
                        .build()
                : null;

        return new NodeBuilder()
                .description("message")
                .attribute("id", messageId)
                .attribute("to", groupJid)
                .attribute("type", type)
                .attribute("phash", phash)
                .attribute("edit", editAttribute)
                .attribute("addressing_mode", addressingMode)
                .content(
                        skDistributionNode,
                        skmsgEncNode,
                        identityNode,
                        bizNode,
                        metaNode,
                        botNode,
                        senderContentBinding,
                        reportingNode
                )
                ;
    }
}
