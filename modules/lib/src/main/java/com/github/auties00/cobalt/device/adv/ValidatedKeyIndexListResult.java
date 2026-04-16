package com.github.auties00.cobalt.device.adv;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.identity.ADVEncryptionType;

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.SequencedSet;

/**
 * Holds the decoded payload of a signed key index list after cryptographic validation.
 *
 * <p>A signed key index list accompanies every device list update and establishes which
 * companion-device key indexes the account considers legitimate. When Cobalt receives
 * a device sync response or a device add/remove notification, the raw protobuf is
 * verified against the primary account's signature key and the resulting payload is
 * exposed through this container so downstream code can reason about companion device
 * validity, account type (E2EE vs HOSTED), and identity rotation state without
 * re-decoding the protobuf.
 *
 * <p>Consumed by {@link DeviceADVValidator} and
 * {@link com.github.auties00.cobalt.device.stanza.DeviceUSyncResponseParser} when
 * validating USync responses and by
 * {@link com.github.auties00.cobalt.device.DeviceService} when handling device add
 * notifications.
 *
 * @implNote WAWebHandleAdvDeviceNotificationUtils.verifySKeyIndexWithAccSigKey: decodes
 * and validates the signed key index list, extracting these fields after signature
 * verification. The WA Web return value also includes an {@code identityUpdatePromise}
 * for saving the identity key, which in Cobalt is handled by the caller
 * ({@code DeviceService}).
 */
@WhatsAppWebModule(moduleName = "WAWebHandleAdvDeviceNotificationUtils")
public final class ValidatedKeyIndexListResult {
    private final long rawId;
    private final Instant timestamp;
    private final SequencedSet<Integer> validIndexes;
    private final int currentIndex;
    private final ADVEncryptionType accountType;
    private final byte[] accountSignatureKey;

    /**
     * Creates a new validated key index list result.
     *
     * @implNote WAWebHandleAdvDeviceNotificationUtils.verifySKeyIndexWithAccSigKey: constructs
     * the return object from decoded {@code ADVKeyIndexListSpec} fields plus the
     * {@code accountSignatureKey} from the outer {@code ADVSignedKeyIndexListSpec}.
     * @param rawId               the raw identity ID from the key index list
     * @param timestamp           the timestamp from the key index list
     * @param validIndexes        the list of valid key indexes for device validation
     * @param currentIndex        the current key index counter
     * @param accountType         the account type (E2EE or HOSTED)
     * @param accountSignatureKey the account signature key (32 bytes)
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleAdvDeviceNotificationUtils",
            exports = "verifySKeyIndexWithAccSigKey",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public ValidatedKeyIndexListResult(
            long rawId,
            Instant timestamp,
            SequencedSet<Integer> validIndexes,
            int currentIndex,
            ADVEncryptionType accountType,
            byte[] accountSignatureKey
    ) {
        this.rawId = rawId;
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp cannot be null");
        this.validIndexes = Objects.requireNonNull(validIndexes, "validIndexes cannot be null");
        this.currentIndex = currentIndex;
        this.accountType = Objects.requireNonNull(accountType, "accountType cannot be null");
        this.accountSignatureKey = Objects.requireNonNull(accountSignatureKey, "accountSignatureKey cannot be null");
    }

    /**
     * Returns the raw identity ID from the key index list.
     *
     * @implNote WAWebHandleAdvDeviceNotificationUtils.verifySKeyIndexWithAccSigKey:
     * {@code rawId} field from decoded {@code ADVKeyIndexListSpec}.
     * @return the raw identity ID
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleAdvDeviceNotificationUtils",
            exports = "verifySKeyIndexWithAccSigKey",
            adaptation = WhatsAppAdaptation.DIRECT)
    public long rawId() {
        return rawId;
    }

    /**
     * Returns the timestamp from the key index list.
     *
     * @implNote WAWebHandleAdvDeviceNotificationUtils.verifySKeyIndexWithAccSigKey:
     * {@code timestamp} field from decoded {@code ADVKeyIndexListSpec}.
     * @return the timestamp
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleAdvDeviceNotificationUtils",
            exports = "verifySKeyIndexWithAccSigKey",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Returns the list of valid key indexes for device validation.
     *
     * @implNote WAWebHandleAdvDeviceNotificationUtils.verifySKeyIndexWithAccSigKey:
     * {@code validIndexes} field from decoded {@code ADVKeyIndexListSpec}.
     * @return an unmodifiable view of the valid indexes
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleAdvDeviceNotificationUtils",
            exports = "verifySKeyIndexWithAccSigKey",
            adaptation = WhatsAppAdaptation.DIRECT)
    public SequencedSet<Integer> validIndexes() {
        return Collections.unmodifiableSequencedSet(validIndexes);
    }

    /**
     * Returns the current key index counter.
     *
     * @implNote WAWebHandleAdvDeviceNotificationUtils.verifySKeyIndexWithAccSigKey:
     * {@code currentIndex} field from decoded {@code ADVKeyIndexListSpec}.
     * @return the current index
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleAdvDeviceNotificationUtils",
            exports = "verifySKeyIndexWithAccSigKey",
            adaptation = WhatsAppAdaptation.DIRECT)
    public int currentIndex() {
        return currentIndex;
    }

    /**
     * Returns the account type (E2EE or HOSTED).
     *
     * @implNote WAWebHandleAdvDeviceNotificationUtils.verifySKeyIndexWithAccSigKey:
     * {@code accountType} field from decoded {@code ADVKeyIndexListSpec}.
     * @return the account type
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleAdvDeviceNotificationUtils",
            exports = "verifySKeyIndexWithAccSigKey",
            adaptation = WhatsAppAdaptation.DIRECT)
    public ADVEncryptionType accountType() {
        return accountType;
    }

    /**
     * Returns the account signature key (32 bytes).
     *
     * @implNote WAWebHandleAdvDeviceNotificationUtils.verifySKeyIndexWithAccSigKey:
     * {@code accountSignatureKey} field from outer {@code ADVSignedKeyIndexListSpec}.
     * @return the account signature key
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleAdvDeviceNotificationUtils",
            exports = "verifySKeyIndexWithAccSigKey",
            adaptation = WhatsAppAdaptation.DIRECT)
    public byte[] accountSignatureKey() {
        return accountSignatureKey;
    }
}
