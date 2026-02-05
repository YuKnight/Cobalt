package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.message.encryption.MessageEncryption;
import com.github.auties00.cobalt.message.encryption.MessageSignalEncryptionType;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;

import java.util.Map;
import java.util.Objects;

/**
 * Context records for message stanza creation.
 */
public final class StanzaContext {
    private StanzaContext() {
        throw new UnsupportedOperationException("Container class");
    }

    /**
     * Context for user message stanza creation.
     * <p>
     * Contains all the additional attributes and child nodes needed for a complete
     * message stanza per WhatsApp Web implementation.
     *
     * @param addressingMode        the addressing mode (pn or lid), or null
     * @param editAttribute         the edit attribute (for revokes/edits), or null
     * @param isResend              whether this is a resend (sets device_fanout="false")
     * @param isBotFeedback         whether this is a bot feedback message
     * @param peerRecipientLid      peer recipient LID for LID chats, or null
     * @param peerRecipientPn       peer recipient phone number, or null
     * @param peerRecipientUsername peer recipient username, or null
     * @param recipientPn           recipient phone number (for LID->PN mapping), or null
     * @param bizNode               the biz node for privacy mode, or null
     * @param metaNode              the meta node with message metadata, or null
     * @param botNode               the bot node for bot messages, or null
     * @param tcToken               the trust contact token bytes, or null
     * @param reportingTokenNode    the reporting token node, or null
     * @param senderContentBinding  the sender's content binding bytes, or null
     * @param contentBindings       map of user JID to content binding bytes
     * @param ctwaAttributionNode   the CTWA attribution node, or null
     *
     * @apiNote WAWebSendMsgCreateFanoutStanza.createFanoutMsgStanza
     */
    public record UserStanzaContext(
            String addressingMode,
            String editAttribute,
            boolean isResend,
            boolean isBotFeedback,
            Jid peerRecipientLid,
            Jid peerRecipientPn,
            String peerRecipientUsername,
            Jid recipientPn,
            Node bizNode,
            Node metaNode,
            Node botNode,
            byte[] tcToken,
            Node reportingTokenNode,
            byte[] senderContentBinding,
            Map<Jid, byte[]> contentBindings,
            Node ctwaAttributionNode
    ) {
        public UserStanzaContext {
            // Allow all nulls for optional fields
        }

        /**
         * Creates a minimal context with just the basic required fields.
         */
        public static UserStanzaContext basic(
                String addressingMode,
                String editAttribute,
                boolean isResend
        ) {
            return new UserStanzaContext(
                    addressingMode, editAttribute, isResend, false,
                    null, null, null, null,
                    null, null, null, null, null, null, null, null
            );
        }
    }

    /**
     * Context for group message stanza creation.
     *
     * @param addressingMode       the addressing mode (pn or lid)
     * @param editAttribute        the edit attribute, or null
     * @param isBotFeedback        whether this is a bot feedback message
     * @param botNeedsIdentity     whether the bot encryption requires identity node
     * @param bizNode              the biz node, or null
     * @param metaNode             the meta node, or null
     * @param botNode              the bot node, or null
     * @param senderContentBinding the sender's content binding bytes, or null
     * @param contentBindings      map of user JID to content binding bytes
     * @param reportingTokenNode   the reporting token node, or null
     *
     * @apiNote WAWebSendGroupSkmsgJob.encryptAndSendSenderKeyMsg
     */
    public record GroupStanzaContext(
            String addressingMode,
            String editAttribute,
            boolean isBotFeedback,
            boolean botNeedsIdentity,
            Node bizNode,
            Node metaNode,
            Node botNode,
            byte[] senderContentBinding,
            Map<Jid, byte[]> contentBindings,
            Node reportingTokenNode
    ) {
        public GroupStanzaContext {
            // Allow all nulls for optional fields
        }

        /**
         * Creates a minimal context with just the basic required fields.
         */
        public static GroupStanzaContext basic(String addressingMode, String editAttribute) {
            return new GroupStanzaContext(
                    addressingMode, editAttribute, false, false,
                    null, null, null, null, null, null
            );
        }
    }

    /**
     * Represents an encrypted message payload for a specific device.
     *
     * @param deviceJid      the target device JID
     * @param encryptionType the encryption type (pkmsg, msg, or skmsg)
     * @param ciphertext     the encrypted message bytes
     * @param mediaType      the media type attribute, or null
     * @param decryptFail    the decrypt-fail attribute ("hide" or null)
     * @param nativeFlowName the native flow name for interactive messages, or null
     */
    public record EncryptedDeviceNode(
            Jid deviceJid,
            MessageSignalEncryptionType encryptionType,
            byte[] ciphertext,
            String mediaType,
            String decryptFail,
            String nativeFlowName
    ) {
        public EncryptedDeviceNode {
            Objects.requireNonNull(deviceJid, "deviceJid cannot be null");
            Objects.requireNonNull(encryptionType, "encryptionType cannot be null");
            Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        }

        /**
         * Creates an encrypted device node from an encryption payload.
         */
        public static EncryptedDeviceNode from(
                MessageEncryption.MessageEncryptedPayload payload,
                String mediaType,
                String decryptFail,
                String nativeFlowName
        ) {
            return new EncryptedDeviceNode(
                    payload.recipientJid(),
                    payload.type(),
                    payload.ciphertext(),
                    mediaType,
                    decryptFail,
                    nativeFlowName
            );
        }
    }

    /**
     * Represents an encrypted group message using sender key.
     *
     * @param ciphertext     the encrypted message bytes
     * @param mediaType      the media type attribute, or null
     * @param decryptFail    the decrypt-fail attribute ("hide" or null)
     * @param nativeFlowName the native flow name for interactive messages, or null
     */
    public record EncryptedGroupMessage(
            byte[] ciphertext,
            String mediaType,
            String decryptFail,
            String nativeFlowName
    ) {
        public EncryptedGroupMessage {
            Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        }

        /**
         * Creates from an encryption payload.
         */
        public static EncryptedGroupMessage from(
                MessageEncryption.MessageEncryptedPayload payload,
                String mediaType,
                String decryptFail,
                String nativeFlowName
        ) {
            return new EncryptedGroupMessage(payload.ciphertext(), mediaType, decryptFail, nativeFlowName);
        }
    }
}
