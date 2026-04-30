package com.github.auties00.cobalt.message.send.crypto;

import com.github.auties00.cobalt.message.MessageEncryptionType;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;

/**
 * Carries the result of encrypting a single outbound message.
 *
 * @param type         the Signal encryption type (pkmsg, msg, or skmsg)
 * @param ciphertext   the encrypted message bytes
 * @param recipientJid the recipient device JID, or {@code null} for sender-key
 *                     group encryption
 */
@WhatsAppWebModule(moduleName = "WAWebEncryptMsgProtobuf")
public record MessageEncryptedPayload(
        MessageEncryptionType type,
        byte[] ciphertext,
        Jid recipientJid
) {
    /**
     * Returns whether this payload establishes a new Signal session.
     *
     * @return {@code true} when the payload is a {@code PreKeySignalMessage}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean isPreKeyMessage() {
        return type.isPreKeyMessage();
    }

    /**
     * Returns whether this payload is a sender-key group message.
     *
     * @return {@code true} when the payload is a {@code SenderKeyMessage}
     */
    public boolean isSenderKeyMessage() {
        return type.isSenderKeyMessage();
    }
}
