package com.github.auties00.cobalt.message.addon;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Objects;

/**
 * Immutable pair of the AES-GCM ciphertext and IV produced by the inner
 * addon encryption layer.
 *
 * <p>Returned by {@link MessageAddonEncryption#encrypt} and consumed by
 * addon protobuf builders that split the payload into an
 * {@code encPayload}/{@code encIv} pair. The ciphertext already includes the
 * trailing 16-byte GCM authentication tag, so callers do not need to
 * manipulate it separately.
 *
 * @param ciphertext the AES-GCM ciphertext including the 16-byte auth tag
 * @param iv         the 12-byte initialization vector used for this
 *                   ciphertext
 * @implNote WAWebAddonEncryption.encryptAddOn: returns
 * {@code {encPayload: L}} where {@code L} is the {@code WACryptoAesGcm.gcmEncrypt}
 * output. The IV is not part of the return value on the JS side because it is
 * always supplied by the caller; Cobalt keeps them together in the record so
 * the helper can also generate the IV internally.
 */
@WhatsAppWebModule(moduleName = "WAWebAddonEncryption")
public record MessageEncryptedAddon(byte[] ciphertext, byte[] iv) {
    /**
     * Expected size of the AES-GCM initialization vector in bytes.
     *
     * @implNote WACryptoAesGcm: the standard 12-byte IV for AES-GCM.
     */
    private static final int AES_GCM_IV_SIZE = 12;

    /**
     * Compact constructor that validates the IV length and rejects
     * {@code null} components.
     *
     * @throws NullPointerException     if {@code ciphertext} or {@code iv} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code iv} is not exactly 12 bytes
     * @implNote WAWebAddonEncryption.encryptAddOn: produces the ciphertext
     * via {@code WACryptoAesGcm.gcmEncrypt} and expects a caller-supplied IV.
     * Cobalt validates the IV length eagerly so malformed addons surface at
     * construction time rather than inside the cipher.
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
