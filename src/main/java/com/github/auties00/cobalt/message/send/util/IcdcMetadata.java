package com.github.auties00.cobalt.message.send.util;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.DeviceListMetadata;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Generates ICDC (Identity Context Device Context) metadata.
 * <p>
 * ICDC metadata is used for device identity verification. It contains hashes
 * of identity keys and device list timestamps for both sender and recipient.
 *
 * @apiNote WAWebICDCMetaApi.populateICDCMeta, WAWebIdentityIcdcApi.getICDCMeta
 */
public final class IcdcMetadata {
    private static final System.Logger LOGGER = System.getLogger("IcdcMetadata");
    private static final int DEFAULT_HASH_LENGTH = 8;
    private static final int MAX_TIMESTAMP_AGE_SECONDS = 720 * 60 * 60;

    private final WhatsAppStore store;

    public IcdcMetadata(WhatsAppStore store) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
    }

    /**
     * Gets ICDC metadata for a user.
     */
    public Meta get(Jid userJid) {
        Objects.requireNonNull(userJid, "userJid cannot be null");

        var deviceList = store.findDeviceList(userJid.toUserJid()).orElse(null);
        if (deviceList == null || deviceList.devices().isEmpty()) {
            return null;
        }

        var devices = deviceList.devices();
        var timestamp = deviceList.timestamp();

        var hasCompanionDevices = devices.stream().anyMatch(d -> d.id() != 0);

        if (!hasCompanionDevices && !isTimestampRecent(timestamp)) {
            return null;
        }

        var identityKeys = new ArrayList<byte[]>();
        var keyIndexes = new ArrayList<Integer>();

        for (var device : devices) {
            var deviceJid = userJid.toDeviceJid(device.id());
            var keyIndex = device.keyIndex();
            if (keyIndex == null) {
                keyIndex = 0;
            }

            if (isSelfUser(userJid)) {
                if (device.id() == store.deviceId().orElse(0)) {
                    var ownKeyPair = store.identityKeyPair();
                    if (ownKeyPair.isPresent()) {
                        identityKeys.add(ownKeyPair.get().publicKey());
                        keyIndexes.add(keyIndex);
                    }
                    continue;
                }
            }

            var signalAddress = deviceJid.toSignalAddress();
            var identity = store.findIdentityByAddress(signalAddress).orElse(null);
            if (identity != null && identity.publicKey() != null) {
                identityKeys.add(identity.publicKey());
                keyIndexes.add(keyIndex);
            }
        }

        if (identityKeys.isEmpty()) {
            return null;
        }

        byte[] keyHash;
        try {
            keyHash = computeIdentityHash(identityKeys, DEFAULT_HASH_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to compute identity hash: {0}", e.getMessage());
            return null;
        }

        Long includeTimestamp = (hasCompanionDevices || isTimestampRecent(timestamp)) ? timestamp : null;
        List<Integer> includeKeyIndexes = (keyIndexes.size() != devices.size()) ? keyIndexes : null;

        Integer accountType = null;
        if (store.isBizHostedDevicesEnabled()) {
            if (isSelfUser(userJid) && store.isHostedMeAccount()) {
                accountType = 1;
            } else if (!isSelfUser(userJid) && deviceList.isHosted()) {
                accountType = 1;
            }
        }

        return new Meta(keyHash, includeTimestamp, includeKeyIndexes, accountType);
    }

    /**
     * Creates device list metadata from ICDC metadata for sender and recipient.
     */
    public DeviceListMetadata createDeviceListMetadata(Meta senderMeta, Meta recipientMeta) {
        if (senderMeta == null && recipientMeta == null) {
            return null;
        }

        return new DeviceListMetadata(
                senderMeta != null ? senderMeta.keyHash() : null,
                senderMeta != null ? senderMeta.timestamp() : null,
                senderMeta != null ? senderMeta.keyIndexes() : null,
                recipientMeta != null ? recipientMeta.keyHash() : null,
                recipientMeta != null ? recipientMeta.timestamp() : null,
                recipientMeta != null ? recipientMeta.keyIndexes() : null
        );
    }

    /**
     * Creates device list metadata for a sender and recipient.
     */
    public DeviceListMetadata forMessage(Jid senderJid, Jid recipientJid) {
        var senderMeta = get(senderJid);
        var recipientMeta = isSelfUser(recipientJid) ? null : get(recipientJid);
        return createDeviceListMetadata(senderMeta, recipientMeta);
    }

    /**
     * Populates ICDC metadata for a specific device.
     */
    public DeviceListMetadata forDevice(Jid senderJid, Jid recipientJid) {
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        Objects.requireNonNull(recipientJid, "recipientJid cannot be null");

        Meta senderMeta;
        try {
            senderMeta = get(senderJid.toUserJid());
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to get sender ICDC meta: {0}", e.getMessage());
            throw new RuntimeException("Failed to create sender ICDC metadata", e);
        }

        Meta recipientMeta = null;
        if (!isSelfUser(recipientJid)) {
            try {
                recipientMeta = get(recipientJid.toUserJid());
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Failed to get recipient ICDC meta for {0}: {1}", recipientJid, e.getMessage());
                throw new RuntimeException("Failed to create recipient ICDC metadata", e);
            }
        }

        return createDeviceListMetadata(senderMeta, recipientMeta);
    }

    /**
     * Bulk pre-computes ICDC metadata for multiple recipients.
     */
    public Map<Jid, DeviceListMetadata> forRecipients(Jid senderJid, Collection<Jid> recipientJids) {
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        Objects.requireNonNull(recipientJids, "recipientJids cannot be null");

        var result = new HashMap<Jid, DeviceListMetadata>();

        Meta senderMeta;
        try {
            senderMeta = get(senderJid.toUserJid());
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING, "Failed to get sender ICDC meta for bulk: {0}", e.getMessage());
            throw new RuntimeException("Failed to create sender ICDC metadata", e);
        }

        var uniqueUserJids = recipientJids.stream()
                .map(Jid::toUserJid)
                .distinct()
                .toList();

        for (var recipientJid : uniqueUserJids) {
            Meta recipientMeta = null;
            if (!isSelfUser(recipientJid)) {
                try {
                    recipientMeta = get(recipientJid);
                } catch (Exception e) {
                    LOGGER.log(System.Logger.Level.WARNING,
                            "Failed to get recipient ICDC meta for {0}: {1}", recipientJid, e.getMessage());
                    throw new RuntimeException("Failed to create recipient ICDC metadata", e);
                }
            }

            var deviceListMetadata = createDeviceListMetadata(senderMeta, recipientMeta);
            result.put(recipientJid, deviceListMetadata);
        }

        return result;
    }

    private byte[] computeIdentityHash(List<byte[]> identityKeys, int hashLength) throws NoSuchAlgorithmException {
        int totalLength = identityKeys.stream().mapToInt(k -> k.length).sum();
        var concatenated = new byte[totalLength];
        int offset = 0;
        for (var key : identityKeys) {
            System.arraycopy(key, 0, concatenated, offset, key.length);
            offset += key.length;
        }

        var digest = MessageDigest.getInstance("SHA-256");
        var hash = digest.digest(concatenated);

        if (hash.length <= hashLength) {
            return hash;
        }
        return Arrays.copyOf(hash, hashLength);
    }

    private boolean isSelfUser(Jid jid) {
        var selfJid = store.jid().orElse(null);
        if (selfJid == null) {
            return false;
        }
        return Objects.equals(jid.toUserJid(), selfJid.toUserJid());
    }

    private boolean isTimestampRecent(Long timestamp) {
        if (timestamp == null) {
            return false;
        }
        var now = System.currentTimeMillis() / 1000;
        return (now - timestamp) < MAX_TIMESTAMP_AGE_SECONDS;
    }

    /**
     * ICDC metadata for a user.
     */
    public record Meta(byte[] keyHash, Long timestamp, List<Integer> keyIndexes, Integer accountType) {
        public Meta {
            Objects.requireNonNull(keyHash, "keyHash cannot be null");
        }
    }
}
