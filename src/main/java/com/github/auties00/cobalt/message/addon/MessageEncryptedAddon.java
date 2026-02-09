package com.github.auties00.cobalt.message.addon;

import java.util.Objects;

/**
 * Represents an encrypted add-on message.
 *
 * @param ciphertext the AES-GCM encrypted content (includes 16-byte auth tag)
 * @param iv         the 12-byte initialization vector
 */
public record MessageEncryptedAddon(byte[] ciphertext, byte[] iv) {
    private static final int AES_GCM_IV_SIZE = 12;

    public MessageEncryptedAddon {
        Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        Objects.requireNonNull(iv, "iv cannot be null");
        if (iv.length != AES_GCM_IV_SIZE) {
            throw new IllegalArgumentException("IV must be " + AES_GCM_IV_SIZE + " bytes");
        }
    }
}
