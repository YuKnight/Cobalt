package com.github.auties00.cobalt.message.send.encryption;

import com.github.auties00.cobalt.message.protocol.MessageSignalEncryptionType;
import com.github.auties00.cobalt.model.jid.Jid;

import java.util.Objects;

/**
 * Represents an encrypted payload for a specific device.
 * Each device in the fanout list receives its own encrypted copy
 * of the message, encrypted with that device's session.
 *
 * @param deviceJid the device JID this encryption is for
 * @param payload   the encrypted payload for this device
 */
public record MessageDeviceEncryption(Jid deviceJid, MessageEncryptedPayload payload) {
    public MessageDeviceEncryption {
        Objects.requireNonNull(deviceJid, "deviceJid cannot be null");
        Objects.requireNonNull(payload, "payload cannot be null");
    }

    /**
     * Returns the encryption type used for this device.
     * This is a convenience method that delegates to the payload.
     *
     * @return the encryption type
     */
    public MessageSignalEncryptionType encryptionType() {
        return payload.type();
    }

    /**
     * Returns whether this encryption requires a PreKeySignalMessage.
     * PreKey messages are used when establishing a new session with a device.
     *
     * @return true if this is a prekey message
     */
    public boolean isPreKeyMessage() {
        return payload.type().isPreKeyMessage();
    }

    /**
     * Returns whether this encryption uses a SenderKeyMessage.
     * Sender key messages are used for group encryption.
     *
     * @return true if this is a sender key message
     */
    public boolean isSenderKeyMessage() {
        return payload.type().isSenderKeyMessage();
    }
}
