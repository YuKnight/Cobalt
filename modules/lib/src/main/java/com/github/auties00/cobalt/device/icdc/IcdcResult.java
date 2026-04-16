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
 * Carries the Identity Change Detection Consistency (ICDC) data derived from a
 * single user's device list.
 *
 * <p>Every outgoing message that supports multi-device encryption attaches an
 * {@code icdcMeta} payload so that the recipient can tell, without an explicit
 * device list exchange, whether the sender's view of the recipient's companion
 * devices has drifted from the server's view. This record is the Cobalt-side
 * representation of that payload for one participant (sender or recipient).
 *
 * <p>Produced by {@link IcdcComputer#compute} and {@link IcdcComputer#computeFromDeviceList},
 * and consumed by the outbound message encoder to populate the
 * {@code deviceListMetadata} field in messageContextInfo.
 *
 * @implNote WAWebIdentityIcdcApi.getICDCMetaFromDeviceRecord: returns
 * {@code {keyHash, timestamp, keyIndexes, senderAccountType, receiverAccountType}}.
 * Cobalt uses a single {@code accountType} field because the caller
 * ({@code IcdcEnricher}) maps sender/receiver based on which JID was used
 * to compute the ICDC metadata.
 */
@WhatsAppWebModule(moduleName = "WAWebIdentityIcdcApi")
public final class IcdcResult {

    /**
     * The truncated SHA-256 hash of sorted, concatenated identity keys,
     * or {@code null} if no companion devices were found.
     *
     * @implNote WAWebIdentityIcdcApi.getICDCMetaFromDeviceRecord: {@code n.keyHash}.
     */
    private final byte[] keyHash;

    /**
     * The device list timestamp, or {@code null} if the user has no
     * companion devices and the timestamp is not recent.
     *
     * @implNote WAWebIdentityIcdcApi.getICDCMetaFromDeviceRecord: {@code n.timestamp}.
     */
    private final Instant timestamp;

    /**
     * The key indexes of devices whose identity keys were successfully
     * retrieved, or {@code null} if all devices were included.
     *
     * @implNote WAWebIdentityIcdcApi.getICDCMetaFromDeviceRecord: {@code n.keyIndexes},
     * only set when {@code y.length !== i.length}.
     */
    private final List<Integer> keyIndexes;

    /**
     * The hosted account encryption type, or {@code null} if the account
     * is not a hosted business account.
     *
     * @implNote WAWebIdentityIcdcApi.getICDCMetaFromDeviceRecord:
     * {@code n.senderAccountType} (for self) or {@code n.receiverAccountType}
     * (for non-self). Cobalt collapses both into a single field since the
     * caller determines which role (sender/receiver) this result represents.
     */
    private final ADVEncryptionType accountType;

    /**
     * Constructs an ICDC result with the given components.
     *
     * @param keyHash    the truncated identity key hash, or {@code null}
     * @param timestamp  the device list timestamp, or {@code null}
     * @param keyIndexes the key indexes of included devices, or {@code null}
     * @param accountType the hosted account encryption type, or {@code null}
     * @implNote WAWebIdentityIcdcApi.getICDCMetaFromDeviceRecord: constructs
     * the result object {@code n = {keyHash, timestamp, keyIndexes, senderAccountType, receiverAccountType}}.
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
     * Returns the SHA-256 hash of sorted, concatenated identity keys,
     * truncated to the configured hash length (minimum 8 bytes).
     *
     * @return the key hash, or empty if no companion devices were found
     * @implNote WAWebIdentityIcdcApi.getICDCMetaFromDeviceRecord: {@code n.keyHash},
     * computed via {@code computeIdentityHash(identityKeysToBinary(curveKeys), hashLength)}.
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
     * <p>Present when the user has companion devices, or when the
     * timestamp is recent (within 720 hours per WA Web).
     *
     * @return the timestamp, or empty if stale and no companion devices
     * @implNote WAWebIdentityIcdcApi.getICDCMetaFromDeviceRecord: {@code n.timestamp},
     * included when {@code hasMultipleDevices || isRecent(timestamp)}.
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Optional<Instant> timestamp() {
        return Optional.ofNullable(timestamp);
    }

    /**
     * Returns the key indexes of devices whose identity keys were
     * successfully retrieved.
     *
     * <p>When all devices in the list had their identity keys available,
     * this returns an empty list to avoid sending redundant data.
     *
     * @return an unmodifiable list of key indexes, or an empty list if
     *         all devices were included
     * @implNote WAWebIdentityIcdcApi.getICDCMetaFromDeviceRecord: {@code n.keyIndexes},
     * only set when {@code y.length !== i.length}.
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
     * Returns the account encryption type for hosted device detection.
     *
     * <p>For the sender, this is {@link ADVEncryptionType#HOSTED} when
     * the sender's own account is hosted.  For the recipient, this is
     * {@link ADVEncryptionType#HOSTED} when the recipient's account type
     * is hosted.
     *
     * @return the account type, or empty if not a hosted account
     * @implNote WAWebIdentityIcdcApi.getICDCMetaFromDeviceRecord:
     * {@code n.senderAccountType} or {@code n.receiverAccountType},
     * gated by {@code WAWebBizCoexGatingUtils.bizHostedDevicesEnabled()}.
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Optional<ADVEncryptionType> accountType() {
        return Optional.ofNullable(accountType);
    }

    /**
     * Returns a human-readable representation of this ICDC result for diagnostic logging.
     *
     * <p>The hash is rendered as a byte-length summary rather than the raw bytes to
     * keep logs concise; the other fields are shown verbatim.
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
