package com.github.auties00.cobalt.device.icdc;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.identity.ADVEncryptionType;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Identity Change Detection Consistency (ICDC) data derived from a single user's
 * device list.
 *
 * <p>Every outgoing message that supports multi-device encryption attaches an
 * {@code icdcMeta} payload so that the recipient can tell, without an explicit
 * device list exchange, whether the sender's view of the recipient's companion
 * devices has drifted from the server's view. This record represents that payload
 * for one participant.
 *
 * <p>Produced by {@link IcdcComputer} and consumed by the outbound message encoder
 * to populate the {@code deviceListMetadata} field in {@code messageContextInfo}.
 */
@WhatsAppWebModule(moduleName = "WAWebIdentityIcdcApi")
public final class IcdcResult {

    /**
     * The truncated SHA-256 hash of the sorted, concatenated identity keys, or
     * {@code null} if no companion devices were found.
     */
    private final byte[] keyHash;

    /**
     * The device list timestamp, or {@code null} when the user has no companion
     * devices and the timestamp is not recent.
     */
    private final Instant timestamp;

    /**
     * The indexes of devices whose identity keys were successfully retrieved, or
     * {@code null} when every device was included.
     */
    private final List<Integer> keyIndexes;

    /**
     * The hosted account encryption type, or {@code null} for non-hosted accounts.
     */
    private final ADVEncryptionType accountType;

    /**
     * Constructs a new ICDC result.
     *
     * @param keyHash     the truncated identity key hash, or {@code null}
     * @param timestamp   the device list timestamp, or {@code null}
     * @param keyIndexes  the indexes of included devices, or {@code null}
     * @param accountType the hosted account encryption type, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.ADAPTED)
    IcdcResult(
            byte[] keyHash,
            Instant timestamp,
            List<Integer> keyIndexes,
            ADVEncryptionType accountType
    ) {
        this.keyHash = keyHash;
        this.timestamp = timestamp;
        this.keyIndexes = keyIndexes;
        this.accountType = accountType;
    }

    /**
     * Returns the truncated SHA-256 hash of the participant's identity keys.
     *
     * @return the key hash, or empty if no companion devices were found
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Optional<byte[]> keyHash() {
        return Optional.ofNullable(keyHash);
    }

    /**
     * Returns the device list timestamp.
     *
     * <p>Present when the user has companion devices, or when the timestamp is
     * within the recent threshold (720 hours).
     *
     * @return the timestamp, or empty when stale and no companion devices
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Optional<Instant> timestamp() {
        return Optional.ofNullable(timestamp);
    }

    /**
     * Returns the indexes of devices whose identity keys were successfully
     * retrieved.
     *
     * <p>An empty list signals that every device in the list was included, so the
     * indexes do not need to be transmitted.
     *
     * @return an unmodifiable list of key indexes, or empty when every device was
     *         included
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public List<Integer> keyIndexes() {
        return keyIndexes != null
                ? Collections.unmodifiableList(keyIndexes)
                : List.of();
    }

    /**
     * Returns the hosted account encryption type for this participant.
     *
     * <p>For the sender, populated when the sender's own account is hosted. For the
     * recipient, populated when the recipient's account is hosted.
     *
     * @return the account type, or empty when the account is not hosted
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Optional<ADVEncryptionType> accountType() {
        return Optional.ofNullable(accountType);
    }

    /**
     * Returns a diagnostic representation suitable for logs.
     *
     * <p>The hash is summarised by length to keep log lines short.
     *
     * @return the diagnostic string
     */
    @Override
    public String toString() {
        return "IcdcResult[" +
                "keyHash=" + (keyHash != null ? keyHash.length + " bytes" : "null") +
                ", timestamp=" + timestamp +
                ", keyIndexes=" + keyIndexes +
                ", accountType=" + accountType +
                ']';
    }
}
