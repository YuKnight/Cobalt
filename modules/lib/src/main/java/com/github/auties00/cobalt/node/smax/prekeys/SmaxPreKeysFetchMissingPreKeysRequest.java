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
 * The outbound stanza variant — wraps the per-user
 * {@code <user>(<device id> <registration/>)} list in the canonical
 * {@code <iq xmlns="encrypt" type="get" to="s.whatsapp.net">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPreKeysFetchMissingPreKeysRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutPreKeysClientRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutPreKeysRegistrationIDMixin")
public final class SmaxPreKeysFetchMissingPreKeysRequest implements SmaxOperation.Request {
    /**
     * The list of users whose missing pre-keys should be re-fetched.
     */
    private final List<UserKeyFetchRequest> users;

    /**
     * Constructs a request for the given list of users.
     *
     * @param users the per-user requests; never {@code null} and never
     *              empty
     * @throws NullPointerException     if {@code users} is {@code null}
     * @throws IllegalArgumentException if {@code users} is empty
     */
    public SmaxPreKeysFetchMissingPreKeysRequest(List<UserKeyFetchRequest> users) {
        Objects.requireNonNull(users, "users cannot be null");
        if (users.isEmpty()) {
            throw new IllegalArgumentException("users cannot be empty");
        }
        this.users = List.copyOf(users);
    }

    /**
     * Returns the list of users carried by this request.
     *
     * @return an unmodifiable list of per-user requests; never
     *         {@code null}
     */
    public List<UserKeyFetchRequest> users() {
        return users;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <key_fetch/>} payload
     *
     * @implNote {@code WASmaxOutPreKeysFetchMissingPreKeysRequest.makeFetchMissingPreKeysRequest}
     *           composes
     *           {@code WASmaxOutPreKeysClientRequestMixin}
     *           ({@code id=generateId()}, {@code xmlns="encrypt"},
     *           {@code to=S_WHATSAPP_NET}) over a {@code <key_fetch/>}
     *           carrying {@code REPEATED_CHILD(<user jid reason?>(<device
     *           id> <registration/>)...)}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPreKeysFetchMissingPreKeysRequest",
            exports = "makeFetchMissingPreKeysRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var userNodes = new ArrayList<Node>(users.size());
        for (var user : users) {
            var deviceNodes = new ArrayList<Node>(user.devices().size());
            for (var device : user.devices()) {
                var registrationNode = new NodeBuilder()
                        .description("registration")
                        .content(device.registrationId())
                        .build();
                var deviceNode = new NodeBuilder()
                        .description("device")
                        .attribute("id", device.deviceId())
                        .content(registrationNode)
                        .build();
                deviceNodes.add(deviceNode);
            }
            var userBuilder = new NodeBuilder()
                    .description("user")
                    .attribute("jid", user.userJid());
            if (user.hasUserReasonIdentity()) {
                userBuilder.attribute("reason", "identity");
            }
            userBuilder.content(deviceNodes);
            userNodes.add(userBuilder.build());
        }
        var keyFetchNode = new NodeBuilder()
                .description("key_fetch")
                .content(userNodes)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "encrypt")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(keyFetchNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxPreKeysFetchMissingPreKeysRequest) obj;
        return Objects.equals(this.users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }

    @Override
    public String toString() {
        return "SmaxPreKeysFetchMissingPreKeysRequest[users=" + users + ']';
    }

    /**
     * Per-user entry in the outbound {@code <key_fetch>} payload — pairs
     * a target user JID with a list of per-device fetch entries plus
     * the optional {@code reason="identity"} hint.
     *
     * @implNote {@code WASmaxOutPreKeysFetchMissingPreKeysRequest.makeFetchMissingPreKeysRequestKeyFetchUser}
     *           emits {@code <user jid=JID(t) reason?=OPTIONAL_LITERAL("identity",
     *           hasUserReasonIdentity)>(<device id=INT(t) ><registration/></device>)*}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutPreKeysFetchMissingPreKeysRequest")
    public static final class UserKeyFetchRequest {
        /**
         * The target user JID whose missing pre-keys are being
         * re-fetched.
         */
        private final Jid userJid;

        /**
         * Whether to include the {@code reason="identity"} hint asking
         * the relay to attach the device-identity attestation.
         */
        private final boolean hasUserReasonIdentity;

        /**
         * The list of per-device fetch entries (1..100 per WA Web).
         */
        private final List<DeviceKeyFetchRequest> devices;

        /**
         * Constructs a per-user request entry.
         *
         * @param userJid               the target user JID; never
         *                              {@code null}
         * @param hasUserReasonIdentity whether to include the
         *                              identity-reason hint
         * @param devices               the per-device entries; never
         *                              {@code null}, may be empty (the
         *                              relay still echoes the user node
         *                              when devices is empty, mirroring
         *                              the {@code REPEATED_CHILD(0, 100)}
         *                              schema)
         * @throws NullPointerException if {@code userJid} or
         *                              {@code devices} is {@code null}
         */
        public UserKeyFetchRequest(Jid userJid, boolean hasUserReasonIdentity, List<DeviceKeyFetchRequest> devices) {
            this.userJid = Objects.requireNonNull(userJid, "userJid cannot be null");
            this.hasUserReasonIdentity = hasUserReasonIdentity;
            this.devices = List.copyOf(Objects.requireNonNullElse(devices, List.of()));
        }

        /**
         * Returns the target user JID.
         *
         * @return the user JID; never {@code null}
         */
        public Jid userJid() {
            return userJid;
        }

        /**
         * Returns whether the identity-reason hint is set.
         *
         * @return {@code true} when the hint is set
         */
        public boolean hasUserReasonIdentity() {
            return hasUserReasonIdentity;
        }

        /**
         * Returns the per-device fetch entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<DeviceKeyFetchRequest> devices() {
            return devices;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (UserKeyFetchRequest) obj;
            return this.hasUserReasonIdentity == that.hasUserReasonIdentity
                    && Objects.equals(this.userJid, that.userJid)
                    && Objects.equals(this.devices, that.devices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userJid, hasUserReasonIdentity, devices);
        }

        @Override
        public String toString() {
            return "SmaxPreKeysFetchMissingPreKeysRequest.UserKeyFetchRequest[userJid=" + userJid
                    + ", hasUserReasonIdentity=" + hasUserReasonIdentity
                    + ", devices=" + devices + ']';
        }
    }

    /**
     * Per-device entry — pairs a numeric device id with the 4-byte
     * registration id whose stale-state needs refreshing.
     *
     * @implNote {@code WASmaxOutPreKeysFetchMissingPreKeysRequest.makeFetchMissingPreKeysRequestKeyFetchUserDevice}
     *           emits {@code <device id=INT(t)><registration>BYTES</registration></device>}
     *           via the {@code WASmaxOutPreKeysRegistrationIDMixin} that
     *           merges the 4-byte registration content into the device
     *           element.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutPreKeysFetchMissingPreKeysRequest")
    @WhatsAppWebModule(moduleName = "WASmaxOutPreKeysRegistrationIDMixin")
    public static final class DeviceKeyFetchRequest {
        /**
         * The numeric device id ({@code 0..99}).
         */
        private final int deviceId;

        /**
         * The 4-byte registration id whose freshness needs validating.
         */
        private final byte[] registrationId;

        /**
         * Constructs a per-device fetch entry.
         *
         * @param deviceId       the device id; {@code 0..99}
         * @param registrationId the 4-byte registration id; never
         *                       {@code null}
         * @throws NullPointerException if {@code registrationId} is
         *                              {@code null}
         */
        public DeviceKeyFetchRequest(int deviceId, byte[] registrationId) {
            this.deviceId = deviceId;
            this.registrationId = Objects.requireNonNull(registrationId, "registrationId cannot be null");
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
         * Returns the 4-byte registration id.
         *
         * @return the registration bytes; never {@code null}
         */
        public byte[] registrationId() {
            return registrationId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (DeviceKeyFetchRequest) obj;
            return this.deviceId == that.deviceId
                    && Arrays.equals(this.registrationId, that.registrationId);
        }

        @Override
        public int hashCode() {
            var result = Integer.hashCode(deviceId);
            result = 31 * result + Arrays.hashCode(registrationId);
            return result;
        }

        @Override
        public String toString() {
            return "SmaxPreKeysFetchMissingPreKeysRequest.DeviceKeyFetchRequest[deviceId=" + deviceId
                    + ", registrationId=" + (registrationId != null ? registrationId.length + " bytes" : "null") + ']';
        }
    }
}
