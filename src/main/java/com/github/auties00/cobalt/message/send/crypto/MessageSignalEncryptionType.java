package com.github.auties00.cobalt.message.send.crypto;

import com.github.auties00.libsignal.protocol.SignalCiphertextMessage;

/**
 * The encryption type used for message payloads.
 * This determines the Signal protocol message type:
 * <ul>
 *   <li>PKMSG - PreKeySignalMessage for establishing new sessions</li>
 *   <li>MSG - SignalMessage for messages within established sessions</li>
 *   <li>SKMSG - SenderKeyMessage for group messages</li>
 * </ul>
 * <p>
 * Both message sending and receiving need to understand these types.
 */
public enum MessageSignalEncryptionType {
    /**
     * PreKeySignalMessage - used for the first message to a device
     * when no session exists. Includes prekey data for session establishment.
     */
    PKMSG("pkmsg"),

    /**
     * SignalMessage - used for subsequent messages within an
     * established Signal session.
     */
    MSG("msg"),

    /**
     * SenderKeyMessage - used for group messages encrypted with
     * the sender's group key. All group members can decrypt with
     * the distributed sender key.
     */
    SKMSG("skmsg");

    private final String protocolValue;

    MessageSignalEncryptionType(String protocolValue) {
        this.protocolValue = protocolValue;
    }

    /**
     * Returns the protocol value used in stanza attributes.
     *
     * @return the encryption type string value (e.g., "pkmsg", "msg", "skmsg")
     */
    public String protocolValue() {
        return protocolValue;
    }

    /**
     * Determines the encryption type from a Signal ciphertext message.
     *
     * @param ciphertext the Signal ciphertext message
     * @return the corresponding encryption type
     * @throws IllegalArgumentException if the ciphertext type is unknown
     */
    public static MessageSignalEncryptionType fromSignalCiphertext(SignalCiphertextMessage ciphertext) {
        return switch (ciphertext.type()) {
            case SignalCiphertextMessage.PRE_KEY_TYPE -> PKMSG;
            case SignalCiphertextMessage.WHISPER_TYPE -> MSG;
            case SignalCiphertextMessage.SENDER_KEY_TYPE -> SKMSG;
            default -> throw new IllegalArgumentException(
                    "Unknown Signal ciphertext type: " + ciphertext.type()
            );
        };
    }

    /**
     * Parses an encryption type from its protocol value.
     *
     * @param value the protocol value (e.g., "pkmsg", "msg", "skmsg")
     * @return the corresponding encryption type
     * @throws IllegalArgumentException if the value is unknown
     */
    public static MessageSignalEncryptionType fromProtocolValue(String value) {
        return switch (value) {
            case "pkmsg" -> PKMSG;
            case "msg" -> MSG;
            case "skmsg" -> SKMSG;
            default -> throw new IllegalArgumentException("Unknown encryption type: " + value);
        };
    }

    /**
     * Returns whether this encryption type indicates a new session establishment.
     *
     * @return true if this is a PreKeySignalMessage
     */
    public boolean isPreKeyMessage() {
        return this == PKMSG;
    }

    /**
     * Returns whether this encryption type is for group messages.
     *
     * @return true if this is a SenderKeyMessage
     */
    public boolean isSenderKeyMessage() {
        return this == SKMSG;
    }
}
