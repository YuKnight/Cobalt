package com.github.auties00.cobalt.message.send.encryption;

import com.github.auties00.cobalt.message.protocol.MessageSignalEncryptionType;
import com.github.auties00.libsignal.protocol.SignalCiphertextMessage;

import java.util.Objects;

/**
 * Represents an encrypted message payload with its encryption type.
 * The encryption type determines how the ciphertext was encrypted
 * and how the server should route it.
 *
 * @param ciphertext the encrypted message bytes
 * @param type       the encryption type (PKMSG, MSG, or SKMSG)
 */
public record MessageEncryptedPayload(SignalCiphertextMessage ciphertext, MessageSignalEncryptionType type) {
    public MessageEncryptedPayload {
        Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
    }
}
