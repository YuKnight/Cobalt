package com.github.auties00.cobalt.message.addon;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Objects;

/**
 * Immutable pair of the AES-GCM ciphertext and IV produced by the inner addon
 * encryption layer.
 *
 * <p>Returned by {@link MessageAddonEncryption#encrypt} and consumed by addon
 * protobuf builders that split the payload into an
 * {@code encPayload}/{@code encIv} pair. The ciphertext already includes the
 * trailing 16-byte GCM authentication tag.
 *
 * @param ciphertext the AES-GCM ciphertext including the 16-byte auth tag
 * @param iv         the 12-byte initialization vector used for this ciphertext
 */
@WhatsAppWebModule(moduleName = "WAWebAddonEncryption")
public record MessageEncryptedAddon(byte[] ciphertext, byte[] iv) {
    /**
     * Expected size of the AES-GCM initialization vector in bytes.
     */
    private static final int AES_GCM_IV_SIZE = 12;

    /**
     * Compact constructor that validates the IV length and rejects
     * {@code null} components.
     *
     * @throws NullPointerException     if {@code ciphertext} or {@code iv} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code iv} is not exactly 12 bytes
     */
    @WhatsAppWebExport(moduleName = "WAWebAddonEncryption", exports = "encryptAddOn",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MessageEncryptedAddon {
        Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        Objects.requireNonNull(iv, "iv cannot be null");
        if (iv.length != AES_GCM_IV_SIZE) {
            throw new IllegalArgumentException("IV must be " + AES_GCM_IV_SIZE + " bytes");
        }
    }
}
