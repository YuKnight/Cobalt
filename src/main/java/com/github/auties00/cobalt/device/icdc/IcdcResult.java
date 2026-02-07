package com.github.auties00.cobalt.device.icdc;

import com.github.auties00.cobalt.model.auth.ADVEncryptionType;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Result of computing ICDC (Identity Change Detection Consistency) metadata
 * for a single user (sender or recipient).
 *
 * <p>Contains the identity key hash, device timestamp, key indexes for
 * devices whose identity keys were found, and the optional hosted account type.
 *
 * @apiNote WAWebIdentityIcdcApi.getICDCMetaFromDeviceRecord: returns
 * {@code {keyHash, timestamp, keyIndexes, senderAccountType, receiverAccountType}}.
 */
public final class IcdcResult {
    private final byte[] keyHash;
    private final Instant timestamp;
    private final List<Integer> keyIndexes;
    private final ADVEncryptionType accountType;

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
     * Returns the SHA-256 hash of concatenated identity keys, truncated to
     * the configured hash length (default 8–10 bytes).
     *
     * @return the key hash, or empty if no companion devices were found
     *
     * @apiNote WAWebIdentityIcdcApi: computed via
     * {@code sha256(identityKeysToBinary(curveKeys)).slice(0, hashLength)}.
     */
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
     *
     * @apiNote WAWebIdentityIcdcApi: included when
     * {@code hasMultipleDevices || isRecent(timestamp)}.
     */
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
     *
     * @apiNote WAWebIdentityIcdcApi: only set when
     * {@code keyIndexes.length !== devices.length}.
     */
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
     *
     * @apiNote WAWebIdentityIcdcApi: gated by
     * {@code WAWebBizCoexGatingUtils.bizHostedDevicesEnabled()}.
     */
    public Optional<ADVEncryptionType> accountType() {
        return Optional.ofNullable(accountType);
    }

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
