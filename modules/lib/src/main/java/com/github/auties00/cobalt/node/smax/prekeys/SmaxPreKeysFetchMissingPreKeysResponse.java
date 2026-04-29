package com.github.auties00.cobalt.node.smax.prekeys;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxPreKeysFetchMissingPreKeysRequest}.
 *
 * @implNote {@code WASmaxPreKeysFetchMissingPreKeysRPC.sendFetchMissingPreKeysRPC}
 *           tries {@code Success} → {@code RequestError} →
 *           {@code ServerError} in order; Cobalt mirrors the priority
 *           and renames {@code RequestError} to the consistent
 *           {@link SmaxPreKeysFetchMissingPreKeysResponse.ClientError} naming.
 */
public sealed interface SmaxPreKeysFetchMissingPreKeysResponse extends SmaxOperation.Response
        permits SmaxPreKeysFetchMissingPreKeysResponse.Success, SmaxPreKeysFetchMissingPreKeysResponse.ClientError, SmaxPreKeysFetchMissingPreKeysResponse.ServerError {

    /**
     * Tries each {@link SmaxPreKeysFetchMissingPreKeysResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or empty
     *         on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxPreKeysFetchMissingPreKeysRPC",
            exports = "sendFetchMissingPreKeysRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxPreKeysFetchMissingPreKeysResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant — the relay produced a
     * {@code <list>} carrying one {@code <user>} entry per requested
     * user, each entry carrying one {@code <device>} per requested
     * device.
     *
     * @implNote {@code WASmaxInPreKeysFetchMissingPreKeysResponseSuccess.parseFetchMissingPreKeysResponseSuccess}
     *           validates the {@code <iq type="result">} envelope,
     *           extracts the {@code <list/>} child, and projects every
     *           {@code <user/>} via the {@code UserSuccess} →
     *           {@code UserError} → {@code UserErrorFallback}
     *           disjunction. Cobalt collapses the two error sub-shapes
     *           into the single {@link UserError} variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchMissingPreKeysResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysIQResultResponseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysResponsePaddingMixin")
    final class Success implements SmaxPreKeysFetchMissingPreKeysResponse {
        /**
         * The list of per-user projections.
         */
        private final List<UserEntry> users;

        /**
         * Constructs a successful reply.
         *
         * @param users the per-user entries; never {@code null}
         *              (defaults to empty)
         */
        public Success(List<UserEntry> users) {
            this.users = List.copyOf(Objects.requireNonNullElse(users, List.of()));
        }

        /**
         * Returns the list of per-user projections.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<UserEntry> users() {
            return users;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPreKeysFetchMissingPreKeysResponseSuccess",
                exports = "parseFetchMissingPreKeysResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var listNode = node.getChild("list").orElse(null);
            if (listNode == null) {
                return Optional.empty();
            }
            var entries = new ArrayList<UserEntry>();
            for (var userNode : listNode.getChildren("user")) {
                var entry = UserEntry.of(userNode).orElse(null);
                if (entry == null) {
                    return Optional.empty();
                }
                entries.add(entry);
            }
            return Optional.of(new Success(entries));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Success) obj;
            return Objects.equals(this.users, that.users);
        }

        @Override
        public int hashCode() {
            return Objects.hash(users);
        }

        @Override
        public String toString() {
            return "SmaxPreKeysFetchMissingPreKeysResponse.Success[users=" + users + ']';
        }

        /**
         * Sealed disjunction of the two per-user reply shapes — either a
         * {@link UserDeviceBundle} carrying device-level pre-key
         * projections, or a {@link UserError} when the relay rejected
         * the per-user fetch.
         */
        public sealed interface UserEntry permits UserDeviceBundle, UserError {

            /**
             * Returns the per-user JID echoed by the relay.
             *
             * @return the JID; never {@code null}
             */
            Jid userJid();

            /**
             * Tries to parse a {@link UserEntry} from the given
             * {@code <user/>} grandchild.
             *
             * @param userNode the {@code <user/>} grandchild; never
             *                 {@code null}
             * @return an {@link Optional} carrying the parsed entry, or
             *         empty on no-match
             *
             * @implNote mirrors
             *           {@code WASmaxInPreKeysUserFetchMissingPreKeysSuccessOrFetchMissingPreKeysErrorOrFetchMissingPreKeysErrorFallbackMixinGroup}.
             */
            static Optional<UserEntry> of(Node userNode) {
                Objects.requireNonNull(userNode, "userNode cannot be null");
                var success = UserDeviceBundle.of(userNode).orElse(null);
                if (success != null) {
                    return Optional.of(success);
                }
                return UserError.of(userNode).map(error -> error);
            }
        }

        /**
         * The successful per-user projection — carries one
         * {@link DeviceKeyBundle} per device the relay was able to
         * resolve.
         *
         * @implNote {@code WASmaxInPreKeysFetchMissingPreKeysUserSuccessMixin.parseFetchMissingPreKeysUserSuccessMixin}
         *           projects {@code <user jid={UserJid}>(<device id>...)*}
         *           where each {@code <device/>} carries the same
         *           full bundle subtree as the {@code FetchKeyBundles}
         *           per-user success projection plus a
         *           {@code <registration/>} mixin pinning the bundle to
         *           a specific registration id.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchMissingPreKeysUserSuccessMixin")
        public static final class UserDeviceBundle implements UserEntry {
            /**
             * The per-user JID echoed by the relay.
             */
            private final Jid userJid;

            /**
             * The per-device bundle projections (1..100 per WA Web).
             */
            private final List<DeviceKeyBundle> devices;

            /**
             * Constructs a per-user projection.
             *
             * @param userJid the user JID; never {@code null}
             * @param devices the per-device bundles; never {@code null}
             *                (defaults to empty)
             * @throws NullPointerException if {@code userJid} is
             *                              {@code null}
             */
            public UserDeviceBundle(Jid userJid, List<DeviceKeyBundle> devices) {
                this.userJid = Objects.requireNonNull(userJid, "userJid cannot be null");
                this.devices = List.copyOf(Objects.requireNonNullElse(devices, List.of()));
            }

            /**
             * Returns the per-user JID echoed by the relay.
             *
             * @return the JID; never {@code null}
             */
            @Override
            public Jid userJid() {
                return userJid;
            }

            /**
             * Returns the per-device bundle projections.
             *
             * @return an unmodifiable list; never {@code null}
             */
            public List<DeviceKeyBundle> devices() {
                return devices;
            }

            /**
             * Tries to parse a {@link UserDeviceBundle} from the given
             * {@code <user/>} grandchild.
             *
             * @param userNode the {@code <user/>} grandchild
             * @return an {@link Optional} carrying the parsed
             *         projection, or empty on no-match
             */
            @WhatsAppWebExport(moduleName = "WASmaxInPreKeysFetchMissingPreKeysUserSuccessMixin",
                    exports = "parseFetchMissingPreKeysUserSuccessMixin",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<UserDeviceBundle> of(Node userNode) {
                Objects.requireNonNull(userNode, "userNode cannot be null");
                if (!userNode.hasDescription("user")) {
                    return Optional.empty();
                }
                var jid = userNode.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var devicesList = new ArrayList<DeviceKeyBundle>();
                for (var deviceNode : userNode.getChildren("device")) {
                    var device = DeviceKeyBundle.of(deviceNode).orElse(null);
                    if (device == null) {
                        return Optional.empty();
                    }
                    devicesList.add(device);
                }
                if (devicesList.isEmpty()) {
                    // The mixin requires at least one device per user
                    // (REPEATED_CHILD min=1) — surface as parse-fail so
                    // the UserError branch can be tried.
                    return Optional.empty();
                }
                return Optional.of(new UserDeviceBundle(jid, devicesList));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (UserDeviceBundle) obj;
                return Objects.equals(this.userJid, that.userJid)
                        && Objects.equals(this.devices, that.devices);
            }

            @Override
            public int hashCode() {
                return Objects.hash(userJid, devices);
            }

            @Override
            public String toString() {
                return "SmaxPreKeysFetchMissingPreKeysResponse.Success.UserDeviceBundle[userJid=" + userJid
                        + ", devices=" + devices + ']';
            }
        }

        /**
         * The per-device pre-key bundle projection — carries every
         * piece of cryptographic material needed to seed a Signal
         * session for one device of a target user.
         *
         * @implNote {@code WASmaxInPreKeysFetchMissingPreKeysUserSuccessDevice}
         *           projects {@code <device id=INT(t)>(<registration/>
         *           <type/>? <identity/> <key/>? <skey/>
         *           <device-identity/>?)}.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchMissingPreKeysUserSuccessMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysRegistrationIDMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysKeyTypeMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysIdentityKeyMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysPreKeyMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysSignedPreKeyMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysDeviceIdentityMixin")
        public static final class DeviceKeyBundle {
            /**
             * The numeric device id ({@code 0..99}).
             */
            private final int deviceId;

            /**
             * The optional relay-side timestamp ({@code t} attribute, in
             * seconds since the UNIX epoch).
             */
            private final Long timestamp;

            /**
             * The optional {@code is_cloud_api="true"} marker.
             */
            private final boolean cloudApi;

            /**
             * The 4-byte registration id (raw bytes, big-endian).
             */
            private final byte[] registrationId;

            /**
             * The optional 1-byte key-type marker (literal {@code [5]}).
             */
            private final byte[] keyType;

            /**
             * The 32-byte Signal identity public key.
             */
            private final byte[] identityKey;

            /**
             * The optional unsigned pre-key.
             */
            private final SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle.PreKey preKey;

            /**
             * The signed pre-key.
             */
            private final SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle.SignedPreKey signedPreKey;

            /**
             * The optional device-identity attestation bytes.
             */
            private final byte[] deviceIdentity;

            /**
             * Constructs a device-key-bundle projection.
             *
             * @param deviceId       the device id
             * @param timestamp      the optional relay timestamp; may be
             *                       {@code null}
             * @param cloudApi       whether the cloud-api marker was set
             * @param registrationId the 4-byte registration id; never
             *                       {@code null}
             * @param keyType        the optional 1-byte key-type marker;
             *                       may be {@code null}
             * @param identityKey    the 32-byte identity key; never
             *                       {@code null}
             * @param preKey         the optional pre-key; may be
             *                       {@code null}
             * @param signedPreKey   the signed pre-key; never
             *                       {@code null}
             * @param deviceIdentity the optional device-identity bytes;
             *                       may be {@code null}
             * @throws NullPointerException if any non-optional argument
             *                              is {@code null}
             */
            public DeviceKeyBundle(int deviceId,
                                   Long timestamp,
                                   boolean cloudApi,
                                   byte[] registrationId,
                                   byte[] keyType,
                                   byte[] identityKey,
                                   SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle.PreKey preKey,
                                   SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle.SignedPreKey signedPreKey,
                                   byte[] deviceIdentity) {
                this.deviceId = deviceId;
                this.timestamp = timestamp;
                this.cloudApi = cloudApi;
                this.registrationId = Objects.requireNonNull(registrationId, "registrationId cannot be null");
                this.keyType = keyType;
                this.identityKey = Objects.requireNonNull(identityKey, "identityKey cannot be null");
                this.preKey = preKey;
                this.signedPreKey = Objects.requireNonNull(signedPreKey, "signedPreKey cannot be null");
                this.deviceIdentity = deviceIdentity;
            }

            /**
             * Returns the numeric device id.
             *
             * @return the device id
             */
            public int deviceId() {
                return deviceId;
            }

            /**
             * Returns the optional relay timestamp.
             *
             * @return an {@link Optional} carrying the timestamp
             */
            public Optional<Long> timestamp() {
                return Optional.ofNullable(timestamp);
            }

            /**
             * Returns whether the cloud-api marker was set.
             *
             * @return {@code true} when the marker was present
             */
            public boolean cloudApi() {
                return cloudApi;
            }

            /**
             * Returns the 4-byte registration id.
             *
             * @return the registration bytes; never {@code null}
             */
            public byte[] registrationId() {
                return registrationId;
            }

            /**
             * Returns the optional 1-byte key-type marker.
             *
             * @return an {@link Optional} carrying the marker
             */
            public Optional<byte[]> keyType() {
                return Optional.ofNullable(keyType);
            }

            /**
             * Returns the 32-byte identity key.
             *
             * @return the identity key bytes; never {@code null}
             */
            public byte[] identityKey() {
                return identityKey;
            }

            /**
             * Returns the optional pre-key.
             *
             * @return an {@link Optional} carrying the pre-key
             */
            public Optional<SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle.PreKey> preKey() {
                return Optional.ofNullable(preKey);
            }

            /**
             * Returns the signed pre-key.
             *
             * @return the signed pre-key; never {@code null}
             */
            public SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle.SignedPreKey signedPreKey() {
                return signedPreKey;
            }

            /**
             * Returns the optional device-identity attestation bytes.
             *
             * @return an {@link Optional} carrying the bytes
             */
            public Optional<byte[]> deviceIdentity() {
                return Optional.ofNullable(deviceIdentity);
            }

            /**
             * Tries to parse a {@link DeviceKeyBundle} from the given
             * {@code <device/>} element.
             *
             * @param deviceNode the {@code <device/>} element
             * @return an {@link Optional} carrying the parsed bundle, or
             *         empty on no-match
             */
            @WhatsAppWebExport(moduleName = "WASmaxInPreKeysFetchMissingPreKeysUserSuccessMixin",
                    exports = "parseFetchMissingPreKeysUserSuccessDevice",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<DeviceKeyBundle> of(Node deviceNode) {
                Objects.requireNonNull(deviceNode, "deviceNode cannot be null");
                if (!deviceNode.hasDescription("device")) {
                    return Optional.empty();
                }
                var idAttribute = deviceNode.getAttributeAsInt("id");
                if (idAttribute.isEmpty()) {
                    return Optional.empty();
                }
                var id = idAttribute.getAsInt();
                if (id < 0 || id > 99) {
                    return Optional.empty();
                }
                Long timestamp = null;
                if (deviceNode.hasAttribute("t")) {
                    var t = deviceNode.getAttributeAsLong("t");
                    if (t.isEmpty()) {
                        return Optional.empty();
                    }
                    timestamp = t.getAsLong();
                }
                var cloudApi = deviceNode.hasAttribute("is_cloud_api", "true");
                var registrationBytes = deviceNode.getChild("registration")
                        .flatMap(Node::toContentBytes)
                        .orElse(null);
                if (registrationBytes == null || registrationBytes.length != 4) {
                    return Optional.empty();
                }
                byte[] keyTypeBytes = null;
                var typeNode = deviceNode.getChild("type").orElse(null);
                if (typeNode != null) {
                    var typeContent = typeNode.toContentBytes().orElse(null);
                    if (typeContent != null && Arrays.equals(typeContent, new byte[]{5})) {
                        keyTypeBytes = typeContent;
                    }
                }
                var identityBytes = deviceNode.getChild("identity")
                        .flatMap(Node::toContentBytes)
                        .orElse(null);
                if (identityBytes == null || identityBytes.length != 32) {
                    return Optional.empty();
                }
                SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle.PreKey preKeyValue = null;
                var preKeyNode = deviceNode.getChild("key").orElse(null);
                if (preKeyNode != null) {
                    preKeyValue = SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle.PreKey.of(preKeyNode).orElse(null);
                    if (preKeyValue == null) {
                        return Optional.empty();
                    }
                }
                var signedPreKeyNode = deviceNode.getChild("skey").orElse(null);
                if (signedPreKeyNode == null) {
                    return Optional.empty();
                }
                var signedPreKeyValue = SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle.SignedPreKey.of(signedPreKeyNode).orElse(null);
                if (signedPreKeyValue == null) {
                    return Optional.empty();
                }
                var deviceIdentityBytes = deviceNode.getChild("device-identity")
                        .flatMap(Node::toContentBytes)
                        .orElse(null);
                return Optional.of(new DeviceKeyBundle(
                        id,
                        timestamp,
                        cloudApi,
                        registrationBytes,
                        keyTypeBytes,
                        identityBytes,
                        preKeyValue,
                        signedPreKeyValue,
                        deviceIdentityBytes));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (DeviceKeyBundle) obj;
                return this.deviceId == that.deviceId
                        && this.cloudApi == that.cloudApi
                        && Objects.equals(this.timestamp, that.timestamp)
                        && Arrays.equals(this.registrationId, that.registrationId)
                        && Arrays.equals(this.keyType, that.keyType)
                        && Arrays.equals(this.identityKey, that.identityKey)
                        && Objects.equals(this.preKey, that.preKey)
                        && Objects.equals(this.signedPreKey, that.signedPreKey)
                        && Arrays.equals(this.deviceIdentity, that.deviceIdentity);
            }

            @Override
            public int hashCode() {
                var result = Objects.hash(deviceId, timestamp, cloudApi, preKey, signedPreKey);
                result = 31 * result + Arrays.hashCode(registrationId);
                result = 31 * result + Arrays.hashCode(keyType);
                result = 31 * result + Arrays.hashCode(identityKey);
                result = 31 * result + Arrays.hashCode(deviceIdentity);
                return result;
            }

            @Override
            public String toString() {
                return "SmaxPreKeysFetchMissingPreKeysResponse.Success.DeviceKeyBundle[deviceId=" + deviceId
                        + ", timestamp=" + timestamp
                        + ", cloudApi=" + cloudApi
                        + ", registrationId=" + (registrationId != null ? registrationId.length + " bytes" : "null")
                        + ", keyType=" + (keyType != null ? keyType.length + " bytes" : "null")
                        + ", identityKey=" + (identityKey != null ? identityKey.length + " bytes" : "null")
                        + ", preKey=" + preKey
                        + ", signedPreKey=" + signedPreKey
                        + ", deviceIdentity=" + (deviceIdentity != null ? deviceIdentity.length + " bytes" : "null") + ']';
            }
        }

        /**
         * The per-user error projection — surfaces a relay-side
         * rejection for a single addressee.
         *
         * @implNote {@code WASmaxInPreKeysFetchMissingPreKeysUserErrorMixin.parseFetchMissingPreKeysUserErrorMixin}
         *           parses the literal {@code code="500"} variant; the
         *           companion
         *           {@code WASmaxInPreKeysFetchMissingPreKeysUserErrorFallbackMixin}
         *           accepts {@code code} in {@code [500, 599]}.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchMissingPreKeysUserErrorMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchMissingPreKeysUserErrorFallbackMixin")
        public static final class UserError implements UserEntry {
            /**
             * The per-user JID echoed by the relay.
             */
            private final Jid userJid;

            /**
             * The numeric error code.
             */
            private final int errorCode;

            /**
             * The human-readable error text.
             */
            private final String errorText;

            /**
             * Constructs a per-user error projection.
             *
             * @param userJid   the per-user JID; never {@code null}
             * @param errorCode the numeric error code
             * @param errorText the human-readable text; never
             *                  {@code null}
             * @throws NullPointerException if {@code userJid} or
             *                              {@code errorText} is
             *                              {@code null}
             */
            public UserError(Jid userJid, int errorCode, String errorText) {
                this.userJid = Objects.requireNonNull(userJid, "userJid cannot be null");
                this.errorCode = errorCode;
                this.errorText = Objects.requireNonNull(errorText, "errorText cannot be null");
            }

            /**
             * Returns the per-user JID echoed by the relay.
             *
             * @return the JID; never {@code null}
             */
            @Override
            public Jid userJid() {
                return userJid;
            }

            /**
             * Returns the numeric error code.
             *
             * @return the error code
             */
            public int errorCode() {
                return errorCode;
            }

            /**
             * Returns the human-readable error text.
             *
             * @return the error text; never {@code null}
             */
            public String errorText() {
                return errorText;
            }

            /**
             * Tries to parse a {@link UserError} from the given
             * {@code <user/>} grandchild.
             *
             * @param userNode the {@code <user/>} grandchild
             * @return an {@link Optional} carrying the parsed error, or
             *         empty on no-match
             */
            @WhatsAppWebExport(moduleName = "WASmaxInPreKeysFetchMissingPreKeysUserErrorMixin",
                    exports = "parseFetchMissingPreKeysUserErrorMixin",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<UserError> of(Node userNode) {
                Objects.requireNonNull(userNode, "userNode cannot be null");
                if (!userNode.hasDescription("user")) {
                    return Optional.empty();
                }
                var jid = userNode.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                var errorNode = userNode.getChild("error").orElse(null);
                if (errorNode == null) {
                    return Optional.empty();
                }
                var text = errorNode.getAttributeAsString("text").orElse(null);
                if (text == null) {
                    return Optional.empty();
                }
                var code = errorNode.getAttributeAsInt("code").orElse(-1);
                if (code < 500 || code > 599) {
                    return Optional.empty();
                }
                return Optional.of(new UserError(jid, code, text));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (UserError) obj;
                return this.errorCode == that.errorCode
                        && Objects.equals(this.userJid, that.userJid)
                        && Objects.equals(this.errorText, that.errorText);
            }

            @Override
            public int hashCode() {
                return Objects.hash(userJid, errorCode, errorText);
            }

            @Override
            public String toString() {
                return "SmaxPreKeysFetchMissingPreKeysResponse.Success.UserError[userJid=" + userJid
                        + ", errorCode=" + errorCode
                        + ", errorText=" + errorText + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * outer request as malformed, unauthorised, or referencing no valid
     * JIDs.
     *
     * @implNote {@code WASmaxInPreKeysFetchMissingPreKeysResponseRequestError.parseFetchMissingPreKeysResponseRequestError}
     *           routes through {@code WASmaxInPreKeysRequestErrorsFetch};
     *           Cobalt collapses to the raw {@code (code, text)} pair
     *           filtered by the {@code [400, 500)} client-error range.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchMissingPreKeysResponseRequestError")
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysRequestErrorsFetch")
    final class ClientError implements SmaxPreKeysFetchMissingPreKeysResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ClientError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or empty
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPreKeysFetchMissingPreKeysResponseRequestError",
                exports = "parseFetchMissingPreKeysResponseRequestError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ClientError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ClientError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxPreKeysFetchMissingPreKeysResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure while processing the request.
     *
     * @implNote {@code WASmaxInPreKeysFetchMissingPreKeysResponseServerError.parseFetchMissingPreKeysResponseServerError}
     *           routes through {@code WASmaxInPreKeysServerErrors};
     *           Cobalt collapses to the raw {@code (code, text)} pair
     *           filtered by the {@code [500, ∞)} server-error range.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchMissingPreKeysResponseServerError")
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysServerErrors")
    final class ServerError implements SmaxPreKeysFetchMissingPreKeysResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ServerError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text, or empty
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPreKeysFetchMissingPreKeysResponseServerError",
                exports = "parseFetchMissingPreKeysResponseServerError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ServerError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ServerError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ServerError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxPreKeysFetchMissingPreKeysResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
