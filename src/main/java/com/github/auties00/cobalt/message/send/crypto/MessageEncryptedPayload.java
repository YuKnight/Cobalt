package com.github.auties00.cobalt.message.send.crypto;

import com.github.auties00.cobalt.model.jid.Jid;

/**
 * The result of encrypting a message.
 *
 * @param type         the Signal encryption type (pkmsg, msg, or skmsg)
 * @param ciphertext   the encrypted message bytes
 * @param recipientJid the recipient device JID, or {@code null} for group messages
 * @apiNote WAWebBackendJobs.flow.CiphertextType
 */
public record MessageEncryptedPayload(
        MessageSignalEncryptionType type,
        byte[] ciphertext,
        Jid recipientJid
) {
    /**
     * Returns whether this message establishes a new session.
     *
     * @return {@code true} if this is a PreKeySignalMessage
     * @apiNote WAWebSendMsgCreateFanoutStanza: sets shouldHaveIdentity when any
     * encryption result has type Pkmsg
     */
    public boolean isPreKeyMessage() {
        return type.isPreKeyMessage();
    }

    /**
     * Returns whether this is a group sender key message.
     *
     * @return {@code true} if this is a SenderKeyMessage
     */
    public boolean isSenderKeyMessage() {
        return type.isSenderKeyMessage();
    }
}
