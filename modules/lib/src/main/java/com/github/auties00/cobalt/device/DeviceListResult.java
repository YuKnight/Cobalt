package com.github.auties00.cobalt.device;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.DeviceConstants;
import com.github.auties00.cobalt.model.device.info.DeviceInfo;
import com.github.auties00.cobalt.model.device.info.DeviceList;
import com.github.auties00.cobalt.model.device.info.DeviceListBuilder;
import com.github.auties00.cobalt.model.jid.Jid;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Outcome of parsing a single user's device list from a USync response.
 *
 * <p>WhatsApp's USync protocol returns three kinds of per-user results: a full
 * device list with signed key index data, an omitted marker confirming the server
 * has no newer information because the local device-hash matches, or an error when
 * the server refuses to answer for that user. This sealed interface captures all
 * three outcomes so downstream handling can switch on the variant.
 *
 * <p>Produced by {@link com.github.auties00.cobalt.device.stanza.DeviceUSyncResponseParser}
 * and consumed by {@link DeviceService}.
 */
@WhatsAppWebModule(moduleName = "WAWebAdvHandlerApi")
public sealed interface DeviceListResult {

    /**
     * Returns the user JID this result refers to.
     *
     * @return the user JID, or empty for global errors that are not tied to a user
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvHandlerApi",
            exports = "handleADVDeviceSyncResult",
            adaptation = WhatsAppAdaptation.ADAPTED)
    Optional<Jid> userJid();

    /**
     * Returns a copy of this result with hosted devices removed.
     *
     * <p>For {@link Full} results the hosted devices are filtered out. For
     * {@link Omitted} and {@link Error} results the same instance is returned
     * unchanged.
     *
     * @return a result without hosted devices
     */
    @WhatsAppWebExport(moduleName = "WAWebAdvHandlerApi",
            exports = "handleADVDeviceSyncResult",
            adaptation = WhatsAppAdaptation.ADAPTED)
    DeviceListResult withoutHostedDevices();

    /**
     * Full device list response variant emitted when the server returned a complete
     * device list for a user.
     *
     * <p>Carries the decoded device list, the account signature key needed to verify
     * later key rotations, and the username fetched in the same round-trip when the
     * username protocol was included in the query.
     */
    @WhatsAppWebModule(moduleName = "WAWebHandleAdvForUsyncApi")
    @WhatsAppWebModule(moduleName = "WAWebHandleAdvKeyIndexResultApi")
    final class Full implements DeviceListResult {
        /**
         * The decoded device list.
         */
        private final DeviceList deviceList;

        /**
         * The account signature key from the signed key index list, or {@code null}
         * when the server did not include one.
         */
        private final byte[] accountSignatureKey;

        /**
         * The username co-fetched via the username USync protocol, or {@code null}
         * when the protocol was not included or no username was returned.
         */
        private final String username;

        /**
         * Constructs a new full device list result.
         *
         * @param deviceList          the device list
         * @param accountSignatureKey the account signature key, or {@code null}
         * @param username            the username, or {@code null}
         * @throws NullPointerException if {@code deviceList} is {@code null}
         */
        public Full(DeviceList deviceList, byte[] accountSignatureKey, String username) {
            this.deviceList = Objects.requireNonNull(deviceList, "deviceList cannot be null");
            this.accountSignatureKey = accountSignatureKey;
            this.username = username;
        }

        /**
         * {@inheritDoc}
         */
        @WhatsAppWebExport(moduleName = "WAWebAdvHandlerApi",
                exports = "handleADVDeviceSyncResult",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public Optional<Jid> userJid() {
            return Optional.of(deviceList.userJid());
        }

        /**
         * Returns the device list payload.
         *
         * @return the device list
         */
        public DeviceList deviceList() {
            return deviceList;
        }

        /**
         * Returns the account signature key from the signed key index list.
         *
         * @return the account signature key, or empty when none was provided
         */
        @WhatsAppWebExport(moduleName = "WAWebHandleAdvDeviceNotificationUtils",
                exports = "verifySKeyIndexWithAccSigKey",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Optional<byte[]> accountSignatureKey() {
            return Optional.ofNullable(accountSignatureKey);
        }

        /**
         * Returns the username co-fetched via the username USync protocol.
         *
         * @return the username, or empty when none was returned
         */
        @WhatsAppWebExport(moduleName = "WAWebUsyncUsername",
                exports = "usernameParser",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Optional<String> username() {
            return Optional.ofNullable(username);
        }

        /**
         * Returns whether this result contains at least one hosted device.
         *
         * @return {@code true} if a hosted device is present
         */
        @WhatsAppWebExport(moduleName = "WAWebBizCoexUtils",
                exports = "hasHostedDevice",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public boolean hasHostedDevice() {
            return deviceList.devices().stream().anyMatch(DeviceInfo::isHosted);
        }

        /**
         * {@inheritDoc}
         */
        @WhatsAppWebExport(moduleName = "WAWebAdvHandlerApi",
                exports = "handleADVDeviceSyncResult",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public DeviceListResult withoutHostedDevices() {
            var filteredDevices = deviceList.devices()
                    .stream()
                    .filter(device -> device.id() != DeviceConstants.HOSTED_DEVICE_ID)
                    .toList();
            if (filteredDevices.size() == deviceList.devices().size()) {
                return this;
            }

            var filteredList = new DeviceListBuilder()
                    .userJid(deviceList.userJid())
                    .devices(filteredDevices)
                    .timestamp(deviceList.timestamp())
                    .rawId(deviceList.rawId())
                    .deleted(deviceList.deleted())
                    .deletedChangedToHost(deviceList.deletedChangedToHost())
                    .advAccountType(deviceList.advAccountType())
                    .expectedTimestamp(deviceList.expectedTimestamp())
                    .expectedTimestampLastDeviceJobTimestamp(deviceList.expectedTimestampLastDeviceJobTimestamp())
                    .expectedTimestampUpdateTimestamp(deviceList.expectedTimestampUpdateTimestamp())
                    .currentIndex(deviceList.currentIndex())
                    .validIndexes(deviceList.validIndexes())
                    .build();
            return new DeviceListResult.Full(filteredList, accountSignatureKey, username);
        }
    }

    /**
     * Omitted device list variant emitted when the server confirms the local device
     * hash is up-to-date and declines to retransmit the full list.
     *
     * <p>Carries only the server-reported timestamps and a marker that distinguishes
     * real omitted results (where the cache is kept and reset to primary-only) from
     * the synthetic ones produced when handling account-type transitions from
     * HOSTED to E2EE.
     */
    @WhatsAppWebModule(moduleName = "WAWebHandleAdvOmittedResultApi")
    final class Omitted implements DeviceListResult {
        /**
         * The user JID this result refers to.
         */
        private final Jid userJid;

        /**
         * The server-reported timestamp, or {@code null} when not present.
         */
        private final Instant timestamp;

        /**
         * The server-reported expected timestamp, or {@code null} when not present.
         */
        private final Instant expectedTimestamp;

        /**
         * Whether this result was produced by the omitted-result code path. Used to
         * distinguish a real omitted response from a synthetic one used for HOSTED
         * to E2EE transitions.
         */
        private final boolean fromHandleOmittedResult;

        /**
         * Constructs a new omitted device list result.
         *
         * @param userJid                 the user JID
         * @param timestamp               the server's timestamp, or {@code null}
         * @param expectedTimestamp       the server's expected timestamp, or {@code null}
         * @param fromHandleOmittedResult whether this result came from the omitted code path
         * @throws NullPointerException if {@code userJid} is {@code null}
         */
        @WhatsAppWebExport(moduleName = "WAWebHandleAdvOmittedResultApi",
                exports = "handleOmittedResult",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public Omitted(Jid userJid, Instant timestamp, Instant expectedTimestamp, boolean fromHandleOmittedResult) {
            this.userJid = Objects.requireNonNull(userJid, "userJid cannot be null");
            this.timestamp = timestamp;
            this.expectedTimestamp = expectedTimestamp;
            this.fromHandleOmittedResult = fromHandleOmittedResult;
        }

        /**
         * {@inheritDoc}
         */
        @WhatsAppWebExport(moduleName = "WAWebHandleAdvOmittedResultApi",
                exports = "handleOmittedResult",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public Optional<Jid> userJid() {
            return Optional.of(userJid);
        }

        /**
         * Returns the server-reported timestamp.
         *
         * @return the timestamp, or empty when not present
         */
        @WhatsAppWebExport(moduleName = "WAWebUsyncDevice",
                exports = "deviceParser",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Optional<Instant> timestamp() {
            return Optional.ofNullable(timestamp);
        }

        /**
         * Returns the server-reported expected timestamp.
         *
         * @return the expected timestamp, or empty when not present
         */
        @WhatsAppWebExport(moduleName = "WAWebUsyncDevice",
                exports = "deviceParser",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Optional<Instant> expectedTimestamp() {
            return Optional.ofNullable(expectedTimestamp);
        }

        /**
         * Returns whether this result came from the omitted-result code path.
         *
         * @return {@code true} when the result is a genuine omitted response
         */
        @WhatsAppWebExport(moduleName = "WAWebHandleAdvOmittedResultApi",
                exports = "handleOmittedResult",
                adaptation = WhatsAppAdaptation.DIRECT)
        public boolean fromHandleOmittedResult() {
            return fromHandleOmittedResult;
        }

        /**
         * {@inheritDoc}
         */
        @WhatsAppWebExport(moduleName = "WAWebAdvHandlerApi",
                exports = "handleADVDeviceSyncResult",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @Override
        public DeviceListResult withoutHostedDevices() {
            return this;
        }
    }

    /**
     * Error variant emitted when the server refused to answer, either with a
     * per-user device error or a global USync error.
     *
     * <p>Fatal errors (global {@code error.all}) abort the entire batch. Non-fatal
     * errors only suppress the affected user so the caller can continue processing
     * other entries.
     */
    @WhatsAppWebModule(moduleName = "WAWebUsyncDevice")
    final class Error implements DeviceListResult {
        /**
         * The user JID this error is associated with, or {@code null} for global
         * errors.
         */
        private final Jid userJid;

        /**
         * The error code reported by the server.
         */
        private final int errorCode;

        /**
         * The error text reported by the server.
         */
        private final String errorText;

        /**
         * Whether this is a fatal error that aborts the entire batch.
         */
        private final boolean fatal;

        /**
         * Constructs a new error result.
         *
         * @param userJid   the user JID, or {@code null} for global errors
         * @param errorCode the error code
         * @param errorText the error text
         * @param fatal     whether the error is fatal
         * @throws NullPointerException if {@code errorText} is {@code null}
         */
        @WhatsAppWebExport(moduleName = "WAWebUsyncDevice",
                exports = "deviceParser",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public Error(Jid userJid, int errorCode, String errorText, boolean fatal) {
            this.userJid = userJid;
            this.errorCode = errorCode;
            this.errorText = Objects.requireNonNull(errorText, "errorText cannot be null");
            this.fatal = fatal;
        }

        /**
         * {@inheritDoc}
         */
        @WhatsAppWebExport(moduleName = "WAWebAdvHandlerApi",
                exports = "handleADVDeviceSyncResult",
                adaptation = WhatsAppAdaptation.DIRECT)
        @Override
        public Optional<Jid> userJid() {
            return Optional.ofNullable(userJid);
        }

        /**
         * Returns the error code reported by the server.
         *
         * @return the error code
         */
        @WhatsAppWebExport(moduleName = "WAWebUsyncDevice",
                exports = "deviceParser",
                adaptation = WhatsAppAdaptation.DIRECT)
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the error text reported by the server.
         *
         * @return the error text
         */
        @WhatsAppWebExport(moduleName = "WAWebUsyncDevice",
                exports = "deviceParser",
                adaptation = WhatsAppAdaptation.DIRECT)
        public String errorText() {
            return errorText;
        }

        /**
         * Returns whether this error is fatal.
         *
         * @return {@code true} for fatal errors that abort the batch
         */
        @WhatsAppWebExport(moduleName = "WAWebAdvHandlerApi",
                exports = "handleADVDeviceSyncResult",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public boolean fatal() {
            return fatal;
        }

        /**
         * {@inheritDoc}
         */
        @WhatsAppWebExport(moduleName = "WAWebAdvHandlerApi",
                exports = "handleADVDeviceSyncResult",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @Override
        public DeviceListResult withoutHostedDevices() {
            return this;
        }
    }
}
