package com.github.auties00.cobalt.device.icdc;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.DeviceConstants;
import com.github.auties00.cobalt.model.device.identity.ADVEncryptionType;
import com.github.auties00.cobalt.model.device.info.DeviceInfo;
import com.github.auties00.cobalt.model.device.info.DeviceList;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Computes Identity Change Detection Consistency (ICDC) metadata for a user's device
 * list.
 *
 * <p>Every outgoing WhatsApp message embeds a small chunk of ICDC metadata describing
 * the sender's (and, for 1:1 chats, the recipient's) known companion devices and the
 * identity keys used to encrypt to them. Recipients compare this metadata against
 * their own view and, when it disagrees, invalidate cached sessions and trigger a
 * device list refresh, defending against stale device lists that could lead to
 * undelivered or mis-encrypted messages.
 *
 * <p>The resulting {@link IcdcResult} carries a truncated SHA-256 hash of the
 * identity keys, the device list timestamp, the key indexes of devices whose
 * identity keys were available locally, and the hosted business account type when
 * applicable.
 *
 * <p>Identity-key serialisation and sorting follow
 * {@code WAWebIdentityApiUtils.identityKeysToBinary}.
 */
@WhatsAppWebModule(moduleName = "WAWebIdentityIcdcApi")
@WhatsAppWebModule(moduleName = "WAWebIdentityApiUtils")
public final class IcdcComputer {

    /**
     * Minimum hash length in bytes. The configured value is clamped at this
     * floor.
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final int MIN_HASH_LENGTH = 8;

    /**
     * Window during which a device list timestamp is considered recent (720 hours,
     * matching WA Web).
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final Duration RECENT_THRESHOLD = Duration.ofHours(720);

    /**
     * The store providing device lists, identity keys, and session state.
     */
    private final WhatsAppStore store;

    /**
     * The AB props service used to read feature flags.
     */
    private final ABPropsService abPropsService;

    /**
     * Constructs a new ICDC computer.
     *
     * @param store          the store
     * @param abPropsService the AB props service
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = {"getICDCMeta", "getICDCMetaFromDeviceRecord"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    public IcdcComputer(WhatsAppStore store, ABPropsService abPropsService) {
        this.store = Objects.requireNonNull(store, "store");
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService");
    }

    /**
     * Computes ICDC metadata for the given user.
     *
     * <p>Looks up the cached device list and delegates to
     * {@link #computeFromDeviceList(Jid, DeviceList)}.
     *
     * @param userJid the user JID
     * @return the ICDC result, or empty when no device list is cached or the cached
     *         list is marked as deleted
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMeta",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<IcdcResult> compute(Jid userJid) {
        return store.findDeviceList(userJid.toUserJid())
                .filter(deviceList -> !deviceList.deleted())
                .map(deviceList -> computeFromDeviceList(userJid, deviceList));
    }

    /**
     * Computes ICDC metadata from an already-resolved device list.
     *
     * <p>Detects whether the user has companion devices, gathers identity keys for
     * remote devices, optionally includes the sender's own identity key, computes a
     * truncated SHA-256 hash, and resolves the hosted account type.
     *
     * @param userJid    the user JID
     * @param deviceList the device list
     * @return the computed ICDC result
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.DIRECT)
    IcdcResult computeFromDeviceList(Jid userJid, DeviceList deviceList) {
        var devices = deviceList.devices();
        var timestamp = deviceList.timestamp();

        var hasCompanionDevices = devices.stream()
                .anyMatch(d -> d.id() != DeviceConstants.PRIMARY_DEVICE_ID);

        byte[] keyHash = null;
        List<Integer> keyIndexes = null;

        if (hasCompanionDevices) {
            var selfJid = store.jid().orElse(null);
            var isSelf = selfJid != null && userJid.toUserJid().equals(selfJid.toUserJid());

            Integer selfKeyIndex = null;
            var remoteDevices = new ArrayList<DeviceInfo>();
            for (var device : devices) {
                var deviceJid = device.toDeviceJid(userJid.user(), userJid.server());
                if (deviceJid.equals(selfJid)) {
                    selfKeyIndex = device.keyIndex();
                } else {
                    remoteDevices.add(device);
                }
            }

            var identityKeys = new ArrayList<byte[]>();
            var includedKeyIndexes = new ArrayList<Integer>();
            for (var device : remoteDevices) {
                var deviceJid = device.toDeviceJid(userJid.user(), userJid.server());
                var identityKey = store.findIdentityByAddress(deviceJid.toSignalAddress()).orElse(null);
                if (identityKey != null) {
                    // toEncodedPoint returns the raw 32-byte key, matching WA Web's toCurveKeyPubKey
                    // which strips the 0x05 prefix from 33-byte Signal keys.
                    identityKeys.add(identityKey.toEncodedPoint());
                    includedKeyIndexes.add(device.keyIndex());
                }
            }

            if (isSelf) {
                // Cobalt always has a valid identity key pair, so the WA Web null guard is unreachable here.
                identityKeys.add(store.identityKeyPair().publicKey().toEncodedPoint());
                if (selfKeyIndex != null) {
                    includedKeyIndexes.add(selfKeyIndex);
                }
            }

            keyHash = computeIdentityHash(identityKeys, getHashLength());

            if (includedKeyIndexes.size() != devices.size()) {
                keyIndexes = includedKeyIndexes;
            }
        }

        var resultTimestamp = (hasCompanionDevices || isRecent(timestamp)) ? timestamp : null;

        // Cobalt uses a single accountType field whereas WA Web reports senderAccountType
        // and receiverAccountType. The caller decides which role this result represents.
        // For self, advAccountType is used as a proxy for getIsHostedMeAccount().
        ADVEncryptionType accountType = null;
        if (isBizHostedDevicesEnabled()) {
            var selfJid = store.jid().orElse(null);
            var isSelf = selfJid != null && userJid.toUserJid().equals(selfJid.toUserJid());
            if (isSelf && deviceList.advAccountType() == ADVEncryptionType.HOSTED) {
                accountType = ADVEncryptionType.HOSTED;
            } else if (!isSelf && deviceList.advAccountType() == ADVEncryptionType.HOSTED) {
                accountType = ADVEncryptionType.HOSTED;
            }
        }

        return new IcdcResult(keyHash, resultTimestamp, keyIndexes, accountType);
    }

    /**
     * Computes the truncated SHA-256 hash of sorted, concatenated identity keys.
     *
     * <p>The keys are sorted lexicographically using unsigned byte comparison,
     * concatenated, hashed with SHA-256, and truncated to the requested length.
     *
     * @param identityKeys the raw 32-byte identity key points
     * @param hashLength   the desired output hash length in bytes
     * @return the truncated hash
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "computeIdentityHash",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebIdentityApiUtils",
            exports = "identityKeysToBinary",
            adaptation = WhatsAppAdaptation.DIRECT)
    static byte[] computeIdentityHash(List<byte[]> identityKeys, int hashLength) {
        try {
            var sorted = new ArrayList<>(identityKeys);
            sorted.sort(IcdcComputer::compareKeyBytes);

            var digest = MessageDigest.getInstance("SHA-256");
            for (var key : sorted) {
                digest.update(key);
            }
            var hash = digest.digest();

            var truncated = new byte[Math.min(hashLength, hash.length)];
            System.arraycopy(hash, 0, truncated, 0, truncated.length);
            return truncated;
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("SHA-256 not available", e);
        }
    }

    /**
     * Compares two byte arrays lexicographically using unsigned byte ordering.
     *
     * @param a the first array
     * @param b the second array
     * @return a negative value when {@code a} sorts before {@code b}, zero when
     *         equal, or a positive value otherwise
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityApiUtils",
            exports = "identityKeysToBinary",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static int compareKeyBytes(byte[] a, byte[] b) {
        var minLength = Math.min(a.length, b.length);
        for (var i = 0; i < minLength; i++) {
            var cmp = Byte.toUnsignedInt(a[i]) - Byte.toUnsignedInt(b[i]);
            if (cmp != 0) {
                return cmp;
            }
        }
        return a.length - b.length;
    }

    /**
     * Returns the configured hash length, clamped to {@value MIN_HASH_LENGTH} bytes.
     *
     * @return the effective hash length in bytes
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.DIRECT)
    private int getHashLength() {
        return Math.max(
                abPropsService.getInt(ABProp.MD_ICDC_HASH_LENGTH),
                MIN_HASH_LENGTH
        );
    }

    /**
     * Returns whether the given timestamp is within the recent threshold.
     *
     * @param timestamp the timestamp, or {@code null}
     * @return {@code true} when {@code timestamp} is non-{@code null} and within
     *         {@link #RECENT_THRESHOLD}
     */
    @WhatsAppWebExport(moduleName = "WAWebIdentityIcdcApi",
            exports = "getICDCMetaFromDeviceRecord",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean isRecent(Instant timestamp) {
        return timestamp != null
                && Duration.between(timestamp, Instant.now()).compareTo(RECENT_THRESHOLD) < 0;
    }

    /**
     * Returns whether the hosted-devices feature is enabled.
     *
     * @return {@code true} when {@code adv_accept_hosted_devices} is set
     */
    @WhatsAppWebExport(moduleName = "WAWebBizCoexGatingUtils",
            exports = "bizHostedDevicesEnabled",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private boolean isBizHostedDevicesEnabled() {
        return abPropsService.getBool(ABProp.ADV_ACCEPT_HOSTED_DEVICES);
    }
}
