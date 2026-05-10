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
 * response to a {@link SmaxPreKeysFetchKeyBundlesRequest}.
 */
public sealed interface SmaxPreKeysFetchKeyBundlesResponse extends SmaxOperation.Response
        permits SmaxPreKeysFetchKeyBundlesResponse.Success, SmaxPreKeysFetchKeyBundlesResponse.ClientError, SmaxPreKeysFetchKeyBundlesResponse.ServerError {

    /**
     * Tries each {@link SmaxPreKeysFetchKeyBundlesResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxPreKeysFetchKeyBundlesRPC",
            exports = "sendFetchKeyBundlesRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxPreKeysFetchKeyBundlesResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay produced a
     * {@code <list>} carrying one {@code <user>} entry per requested
     * user. Each entry is either a successful key-bundle projection
     * ({@link UserKeyBundle}) or a per-user error projection
     * ({@link UserError}).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchKeyBundlesResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysIQResultResponseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysResponsePaddingMixin")
    final class Success implements SmaxPreKeysFetchKeyBundlesResponse {
        /**
         * The list of per-user bundle projections, one entry per
         * requested user (in the same order as the request).
         */
        private final List<UserEntry> users;

        /**
         * Constructs a successful reply.
         *
         * @param users the per-user entries. Never {@code null}
         *              (defaults to empty)
         */
        public Success(List<UserEntry> users) {
            this.users = List.copyOf(Objects.requireNonNullElse(users, List.of()));
        }

        /**
         * Returns the list of per-user entries.
         *
         * @return an unmodifiable list of entries. Never {@code null}
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
         *         empty when the stanza does not match the success
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInPreKeysFetchKeyBundlesResponseSuccess",
                exports = "parseFetchKeyBundlesResponseSuccess",
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
            return "SmaxPreKeysFetchKeyBundlesResponse.Success[users=" + users + ']';
        }

        /**
         * Sealed disjunction of the two per-user reply shapes. Either a
         * fully populated {@link UserKeyBundle} when the relay had the
         * bundle on hand, or a {@link UserError} when the bundle could
         * not be assembled (user blocked, registration mismatch, no
         * pre-keys uploaded, etc.).
         */
        public sealed interface UserEntry permits UserKeyBundle, UserError {

            /**
             * Returns the per-user JID echoed by the relay.
             *
             * @return the JID. Never {@code null}
             */
            Jid userJid();

            /**
             * Tries to parse a {@link UserEntry} from the given
             * {@code <user/>} grandchild.
             *
             * @param userNode the {@code <user/>} grandchild. Never
             *                 {@code null}
             * @return an {@link Optional} carrying the parsed entry, or
             *         empty when the grandchild matches neither the
             *         success nor error shape
             */
            static Optional<UserEntry> of(Node userNode) {
                Objects.requireNonNull(userNode, "userNode cannot be null");
                var success = UserKeyBundle.of(userNode).orElse(null);
                if (success != null) {
                    return Optional.of(success);
                }
                return UserError.of(userNode).map(error -> error);
            }
        }

        /**
         * The successful per-user projection. Carries the full Signal
         * pre-key bundle plus optional device-identity attestation.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchKeyBundlesUserSuccessMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysRegistrationIDMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysKeyTypeMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysIdentityKeyMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysPreKeyMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysSignedPreKeyMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysDeviceIdentityMixin")
        public static final class UserKeyBundle implements UserEntry {
            /**
             * The per-user JID echoed by the relay.
             */
            private final Jid userJid;

            /**
             * The optional relay-side timestamp ({@code t} attribute, in
             * seconds since the UNIX epoch).
             */
            private final Long timestamp;

            /**
             * The optional {@code is_cloud_api="true"} marker. Set when
             * the bundle belongs to a Cloud-API hosted account.
             */
            private final boolean cloudApi;

            /**
             * The 4-byte registration id (raw bytes, big-endian).
             */
            private final byte[] registrationId;

            /**
             * The optional 1-byte key-type marker (literal {@code [5]}
             * for Curve25519). Absent on legacy bundles.
             */
            private final byte[] keyType;

            /**
             * The 32-byte Signal identity public key.
             */
            private final byte[] identityKey;

            /**
             * The optional unsigned pre-key. Null when the relay had
             * no fresh pre-keys to surface.
             */
            private final PreKey preKey;

            /**
             * The signed pre-key (always present in a successful
             * bundle).
             */
            private final SignedPreKey signedPreKey;

            /**
             * The optional device-identity attestation bytes. Present
             * only when the request asked for {@code reason="identity"}.
             */
            private final byte[] deviceIdentity;

            /**
             * Constructs a key-bundle projection.
             *
             * @param userJid        the per-user JID. Never {@code null}
             * @param timestamp      the optional relay timestamp. May be
             *                       {@code null}
             * @param cloudApi       whether the {@code is_cloud_api}
             *                       marker was set
             * @param registrationId the 4-byte registration id. Never
             *                       {@code null}
             * @param keyType        the optional 1-byte key-type marker;
             *                       may be {@code null}
             * @param identityKey    the 32-byte identity key. Never
             *                       {@code null}
             * @param preKey         the optional unsigned pre-key. May
             *                       be {@code null}
             * @param signedPreKey   the signed pre-key. Never
             *                       {@code null}
             * @param deviceIdentity the optional device-identity bytes;
             *                       may be {@code null}
             * @throws NullPointerException if any non-optional argument
             *                              is {@code null}
             */
            public UserKeyBundle(Jid userJid,
                                 Long timestamp,
                                 boolean cloudApi,
                                 byte[] registrationId,
                                 byte[] keyType,
                                 byte[] identityKey,
                                 PreKey preKey,
                                 SignedPreKey signedPreKey,
                                 byte[] deviceIdentity) {
                this.userJid = Objects.requireNonNull(userJid, "userJid cannot be null");
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
             * Returns the per-user JID echoed by the relay.
             *
             * @return the JID. Never {@code null}
             */
            @Override
            public Jid userJid() {
                return userJid;
            }

            /**
             * Returns the optional relay timestamp.
             *
             * @return an {@link Optional} carrying the timestamp, or
             *         empty when absent
             */
            public Optional<Long> timestamp() {
                return Optional.ofNullable(timestamp);
            }

            /**
             * Returns whether the {@code is_cloud_api="true"} marker was
             * set on the entry.
             *
             * @return {@code true} when the marker was present
             */
            public boolean cloudApi() {
                return cloudApi;
            }

            /**
             * Returns the 4-byte registration id (raw bytes, big-endian).
             *
             * @return the registration id. Never {@code null}
             */
            public byte[] registrationId() {
                return registrationId;
            }

            /**
             * Returns the optional 1-byte key-type marker.
             *
             * @return an {@link Optional} carrying the key-type bytes,
             *         or empty when absent
             */
            public Optional<byte[]> keyType() {
                return Optional.ofNullable(keyType);
            }

            /**
             * Returns the 32-byte Signal identity public key.
             *
             * @return the identity key. Never {@code null}
             */
            public byte[] identityKey() {
                return identityKey;
            }

            /**
             * Returns the optional unsigned pre-key.
             *
             * @return an {@link Optional} carrying the pre-key, or empty
             *         when the relay produced none
             */
            public Optional<PreKey> preKey() {
                return Optional.ofNullable(preKey);
            }

            /**
             * Returns the signed pre-key (always present in a successful
             * bundle).
             *
             * @return the signed pre-key. Never {@code null}
             */
            public SignedPreKey signedPreKey() {
                return signedPreKey;
            }

            /**
             * Returns the optional device-identity attestation bytes.
             *
             * @return an {@link Optional} carrying the device-identity
             *         bytes, or empty when absent
             */
            public Optional<byte[]> deviceIdentity() {
                return Optional.ofNullable(deviceIdentity);
            }

            /**
             * Tries to parse a {@link UserKeyBundle} from the given
             * {@code <user/>} grandchild.
             *
             * @param userNode the {@code <user/>} grandchild
             * @return an {@link Optional} carrying the parsed bundle, or
             *         empty on no-match
             */
            @WhatsAppWebExport(moduleName = "WASmaxInPreKeysFetchKeyBundlesUserSuccessMixin",
                    exports = "parseFetchKeyBundlesUserSuccessMixin",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<UserKeyBundle> of(Node userNode) {
                Objects.requireNonNull(userNode, "userNode cannot be null");
                if (!userNode.hasDescription("user")) {
                    return Optional.empty();
                }
                var jid = userNode.getAttributeAsJid("jid").orElse(null);
                if (jid == null) {
                    return Optional.empty();
                }
                Long timestamp = null;
                if (userNode.hasAttribute("t")) {
                    var t = userNode.getAttributeAsLong("t");
                    if (t.isEmpty()) {
                        return Optional.empty();
                    }
                    timestamp = t.getAsLong();
                }
                var cloudApi = userNode.hasAttribute("is_cloud_api", "true");
                var registrationBytes = userNode.getChild("registration")
                        .flatMap(Node::toContentBytes)
                        .orElse(null);
                if (registrationBytes == null || registrationBytes.length != 4) {
                    return Optional.empty();
                }
                byte[] keyTypeBytes = null;
                var typeNode = userNode.getChild("type").orElse(null);
                if (typeNode != null) {
                    var typeContent = typeNode.toContentBytes().orElse(null);
                    if (typeContent == null || !Arrays.equals(typeContent, new byte[]{5})) {
                        // Optional whose content didn't match. Let the
                        // value remain absent (mirrors WA Web's optional
                        // semantics: failure to parse the optional sub-tree
                        // surfaces as a null-valued field).
                        keyTypeBytes = null;
                    } else {
                        keyTypeBytes = typeContent;
                    }
                }
                var identityBytes = userNode.getChild("identity")
                        .flatMap(Node::toContentBytes)
                        .orElse(null);
                if (identityBytes == null || identityBytes.length != 32) {
                    return Optional.empty();
                }
                PreKey preKeyValue = null;
                var preKeyNode = userNode.getChild("key").orElse(null);
                if (preKeyNode != null) {
                    preKeyValue = PreKey.of(preKeyNode).orElse(null);
                    if (preKeyValue == null) {
                        // The optional sub-tree exists but is malformed:
                        // surface the entry as parse-failed.
                        return Optional.empty();
                    }
                }
                var signedPreKeyNode = userNode.getChild("skey").orElse(null);
                if (signedPreKeyNode == null) {
                    return Optional.empty();
                }
                var signedPreKeyValue = SignedPreKey.of(signedPreKeyNode).orElse(null);
                if (signedPreKeyValue == null) {
                    return Optional.empty();
                }
                var deviceIdentityBytes = userNode.getChild("device-identity")
                        .flatMap(Node::toContentBytes)
                        .orElse(null);
                return Optional.of(new UserKeyBundle(
                        jid,
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
                var that = (UserKeyBundle) obj;
                return this.cloudApi == that.cloudApi
                        && Objects.equals(this.userJid, that.userJid)
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
                var result = Objects.hash(userJid, timestamp, cloudApi, preKey, signedPreKey);
                result = 31 * result + Arrays.hashCode(registrationId);
                result = 31 * result + Arrays.hashCode(keyType);
                result = 31 * result + Arrays.hashCode(identityKey);
                result = 31 * result + Arrays.hashCode(deviceIdentity);
                return result;
            }

            @Override
            public String toString() {
                return "SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle[userJid=" + userJid
                        + ", timestamp=" + timestamp
                        + ", cloudApi=" + cloudApi
                        + ", registrationId=" + (registrationId != null ? registrationId.length + " bytes" : "null")
                        + ", keyType=" + (keyType != null ? keyType.length + " bytes" : "null")
                        + ", identityKey=" + (identityKey != null ? identityKey.length + " bytes" : "null")
                        + ", preKey=" + preKey
                        + ", signedPreKey=" + signedPreKey
                        + ", deviceIdentity=" + (deviceIdentity != null ? deviceIdentity.length + " bytes" : "null") + ']';
            }

            /**
             * The unsigned pre-key projection. Pairs a 3-byte key id
             * with 32 bytes of public-key material.
             */
            @WhatsAppWebModule(moduleName = "WASmaxInPreKeysPreKeyMixin")
            @WhatsAppWebModule(moduleName = "WASmaxInPreKeysKeyIDMixin")
            @WhatsAppWebModule(moduleName = "WASmaxInPreKeysKeyDataMixin")
            public static final class PreKey {
                /**
                 * The 3-byte pre-key identifier (raw bytes, big-endian).
                 */
                private final byte[] keyId;

                /**
                 * The 32-byte public-key material.
                 */
                private final byte[] keyValue;

                /**
                 * Constructs a pre-key projection.
                 *
                 * @param keyId    the 3-byte key id. Never {@code null}
                 * @param keyValue the 32-byte key material. Never
                 *                 {@code null}
                 * @throws NullPointerException if any argument is
                 *                              {@code null}
                 */
                public PreKey(byte[] keyId, byte[] keyValue) {
                    this.keyId = Objects.requireNonNull(keyId, "keyId cannot be null");
                    this.keyValue = Objects.requireNonNull(keyValue, "keyValue cannot be null");
                }

                /**
                 * Returns the 3-byte pre-key identifier.
                 *
                 * @return the key id bytes. Never {@code null}
                 */
                public byte[] keyId() {
                    return keyId;
                }

                /**
                 * Returns the 32-byte public-key material.
                 *
                 * @return the key value bytes. Never {@code null}
                 */
                public byte[] keyValue() {
                    return keyValue;
                }

                /**
                 * Tries to parse a {@link PreKey} from the given
                 * {@code <key/>} element.
                 *
                 * @param keyNode the {@code <key/>} element
                 * @return an {@link Optional} carrying the parsed
                 *         pre-key, or empty when malformed
                 */
                public static Optional<PreKey> of(Node keyNode) {
                    Objects.requireNonNull(keyNode, "keyNode cannot be null");
                    var idBytes = keyNode.getChild("id")
                            .flatMap(Node::toContentBytes)
                            .orElse(null);
                    if (idBytes == null || idBytes.length != 3) {
                        return Optional.empty();
                    }
                    var valueBytes = keyNode.getChild("value")
                            .flatMap(Node::toContentBytes)
                            .orElse(null);
                    if (valueBytes == null || valueBytes.length != 32) {
                        return Optional.empty();
                    }
                    return Optional.of(new PreKey(idBytes, valueBytes));
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj == this) {
                        return true;
                    }
                    if (obj == null || obj.getClass() != this.getClass()) {
                        return false;
                    }
                    var that = (PreKey) obj;
                    return Arrays.equals(this.keyId, that.keyId)
                            && Arrays.equals(this.keyValue, that.keyValue);
                }

                @Override
                public int hashCode() {
                    var result = Arrays.hashCode(keyId);
                    result = 31 * result + Arrays.hashCode(keyValue);
                    return result;
                }

                @Override
                public String toString() {
                    return "SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle.PreKey[keyId="
                            + (keyId != null ? keyId.length + " bytes" : "null")
                            + ", keyValue=" + (keyValue != null ? keyValue.length + " bytes" : "null") + ']';
                }
            }

            /**
             * The signed pre-key projection. Pairs a 3-byte key id with
             * 32 bytes of public-key material plus a 64-byte
             * signature.
             */
            @WhatsAppWebModule(moduleName = "WASmaxInPreKeysSignedPreKeyMixin")
            @WhatsAppWebModule(moduleName = "WASmaxInPreKeysKeyIDMixin")
            @WhatsAppWebModule(moduleName = "WASmaxInPreKeysKeyDataMixin")
            public static final class SignedPreKey {
                /**
                 * The 3-byte signed-pre-key identifier (raw bytes,
                 * big-endian).
                 */
                private final byte[] keyId;

                /**
                 * The 32-byte public-key material.
                 */
                private final byte[] keyValue;

                /**
                 * The 64-byte signature over the key value, signed by
                 * the user's identity key.
                 */
                private final byte[] signature;

                /**
                 * Constructs a signed pre-key projection.
                 *
                 * @param keyId     the 3-byte key id. Never {@code null}
                 * @param keyValue  the 32-byte key material. Never
                 *                  {@code null}
                 * @param signature the 64-byte signature. Never
                 *                  {@code null}
                 * @throws NullPointerException if any argument is
                 *                              {@code null}
                 */
                public SignedPreKey(byte[] keyId, byte[] keyValue, byte[] signature) {
                    this.keyId = Objects.requireNonNull(keyId, "keyId cannot be null");
                    this.keyValue = Objects.requireNonNull(keyValue, "keyValue cannot be null");
                    this.signature = Objects.requireNonNull(signature, "signature cannot be null");
                }

                /**
                 * Returns the 3-byte signed-pre-key identifier.
                 *
                 * @return the key id bytes. Never {@code null}
                 */
                public byte[] keyId() {
                    return keyId;
                }

                /**
                 * Returns the 32-byte public-key material.
                 *
                 * @return the key value bytes. Never {@code null}
                 */
                public byte[] keyValue() {
                    return keyValue;
                }

                /**
                 * Returns the 64-byte signature.
                 *
                 * @return the signature bytes. Never {@code null}
                 */
                public byte[] signature() {
                    return signature;
                }

                /**
                 * Tries to parse a {@link SignedPreKey} from the given
                 * {@code <skey/>} element.
                 *
                 * @param skeyNode the {@code <skey/>} element
                 * @return an {@link Optional} carrying the parsed signed
                 *         pre-key, or empty when malformed
                 */
                public static Optional<SignedPreKey> of(Node skeyNode) {
                    Objects.requireNonNull(skeyNode, "skeyNode cannot be null");
                    var idBytes = skeyNode.getChild("id")
                            .flatMap(Node::toContentBytes)
                            .orElse(null);
                    if (idBytes == null || idBytes.length != 3) {
                        return Optional.empty();
                    }
                    var valueBytes = skeyNode.getChild("value")
                            .flatMap(Node::toContentBytes)
                            .orElse(null);
                    if (valueBytes == null || valueBytes.length != 32) {
                        return Optional.empty();
                    }
                    var signatureBytes = skeyNode.getChild("signature")
                            .flatMap(Node::toContentBytes)
                            .orElse(null);
                    if (signatureBytes == null || signatureBytes.length != 64) {
                        return Optional.empty();
                    }
                    return Optional.of(new SignedPreKey(idBytes, valueBytes, signatureBytes));
                }

                @Override
                public boolean equals(Object obj) {
                    if (obj == this) {
                        return true;
                    }
                    if (obj == null || obj.getClass() != this.getClass()) {
                        return false;
                    }
                    var that = (SignedPreKey) obj;
                    return Arrays.equals(this.keyId, that.keyId)
                            && Arrays.equals(this.keyValue, that.keyValue)
                            && Arrays.equals(this.signature, that.signature);
                }

                @Override
                public int hashCode() {
                    var result = Arrays.hashCode(keyId);
                    result = 31 * result + Arrays.hashCode(keyValue);
                    result = 31 * result + Arrays.hashCode(signature);
                    return result;
                }

                @Override
                public String toString() {
                    return "SmaxPreKeysFetchKeyBundlesResponse.Success.UserKeyBundle.SignedPreKey[keyId="
                            + (keyId != null ? keyId.length + " bytes" : "null")
                            + ", keyValue=" + (keyValue != null ? keyValue.length + " bytes" : "null")
                            + ", signature=" + (signature != null ? signature.length + " bytes" : "null") + ']';
                }
            }
        }

        /**
         * The per-user error projection. Surfaces a relay-side rejection
         * for a single addressee while the rest of the {@code <list>}
         * may still carry successful bundles.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchKeyBundlesUserErrorMixin")
        @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchKeyBundlesUserErrorFallbackMixin")
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
             * @param userJid   the per-user JID. Never {@code null}
             * @param errorCode the numeric error code
             * @param errorText the human-readable text. Never
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
             * @return the JID. Never {@code null}
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
             * @return the error text. Never {@code null}
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
            @WhatsAppWebExport(moduleName = "WASmaxInPreKeysFetchKeyBundlesUserErrorMixin",
                    exports = "parseFetchKeyBundlesUserErrorMixin",
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
                // Accept the literal 500 plus the [500, 599] fallback range
                // surfaced by WASmaxInPreKeysFetchKeyBundlesUserErrorFallbackMixin.
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
                return "SmaxPreKeysFetchKeyBundlesResponse.Success.UserError[userJid=" + userJid
                        + ", errorCode=" + errorCode
                        + ", errorText=" + errorText + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * outer request as malformed, unauthorised, or referencing no valid
     * JIDs.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchKeyBundlesResponseRequestError")
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysRequestErrorsFetch")
    final class ClientError implements SmaxPreKeysFetchKeyBundlesResponse {
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
         * @param errorText the optional human-readable text. May be
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
        @WhatsAppWebExport(moduleName = "WASmaxInPreKeysFetchKeyBundlesResponseRequestError",
                exports = "parseFetchKeyBundlesResponseRequestError",
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
            return "SmaxPreKeysFetchKeyBundlesResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure while processing the request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysFetchKeyBundlesResponseServerError")
    @WhatsAppWebModule(moduleName = "WASmaxInPreKeysServerErrors")
    final class ServerError implements SmaxPreKeysFetchKeyBundlesResponse {
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
         * @param errorText the optional human-readable text. May be
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
        @WhatsAppWebExport(moduleName = "WASmaxInPreKeysFetchKeyBundlesResponseServerError",
                exports = "parseFetchKeyBundlesResponseServerError",
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
            return "SmaxPreKeysFetchKeyBundlesResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
