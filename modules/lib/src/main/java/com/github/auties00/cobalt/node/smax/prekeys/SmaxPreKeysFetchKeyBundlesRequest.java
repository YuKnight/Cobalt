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
 * {@code <user jid reason?/>} list in the canonical
 * {@code <iq xmlns="encrypt" type="get" to="s.whatsapp.net">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutPreKeysFetchKeyBundlesRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutPreKeysClientRequestMixin")
public final class SmaxPreKeysFetchKeyBundlesRequest implements SmaxOperation.Request {
    /**
     * The list of users to fetch pre-key bundles for.
     */
    private final List<UserKeyRequest> users;

    /**
     * Constructs a request for the given list of users.
     *
     * @param users the per-user requests; never {@code null} and never
     *              empty
     * @throws NullPointerException     if {@code users} is {@code null}
     * @throws IllegalArgumentException if {@code users} is empty
     */
    public SmaxPreKeysFetchKeyBundlesRequest(List<UserKeyRequest> users) {
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
    public List<UserKeyRequest> users() {
        return users;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <key/>} payload
     *
     * @implNote {@code WASmaxOutPreKeysFetchKeyBundlesRequest.makeFetchKeyBundlesRequest}
     *           composes
     *           {@code WASmaxOutPreKeysClientRequestMixin}
     *           ({@code id=generateId()}, {@code xmlns="encrypt"},
     *           {@code to=S_WHATSAPP_NET}) over a {@code <key/>} child
     *           carrying {@code REPEATED_CHILD(<user jid reason?/>, 1,
     *           100000)}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutPreKeysFetchKeyBundlesRequest",
            exports = "makeFetchKeyBundlesRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var userNodes = new ArrayList<Node>(users.size());
        for (var user : users) {
            var userBuilder = new NodeBuilder()
                    .description("user")
                    .attribute("jid", user.userJid());
            if (user.hasUserReasonIdentity()) {
                userBuilder.attribute("reason", "identity");
            }
            userNodes.add(userBuilder.build());
        }
        var keyNode = new NodeBuilder()
                .description("key")
                .content(userNodes)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "encrypt")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(keyNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxPreKeysFetchKeyBundlesRequest) obj;
        return Objects.equals(this.users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }

    @Override
    public String toString() {
        return "SmaxPreKeysFetchKeyBundlesRequest[users=" + users + ']';
    }

    /**
     * Per-user entry in the outbound {@code <key>} payload — pairs a
     * target user JID with the optional
     * {@code reason="identity"} hint that asks the relay to include the
     * device-identity attestation in the response.
     *
     * @implNote {@code WASmaxOutPreKeysFetchKeyBundlesRequest.makeFetchKeyBundlesRequestKeyUser}
     *           emits {@code <user jid=JID(t) reason?=OPTIONAL_LITERAL("identity",
     *           hasUserReasonIdentity)/>}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxOutPreKeysFetchKeyBundlesRequest")
    public static final class UserKeyRequest {
        /**
         * The target user JID whose pre-key bundle is being requested.
         */
        private final Jid userJid;

        /**
         * Whether to include the {@code reason="identity"} hint asking
         * the relay to attach the device-identity attestation.
         */
        private final boolean hasUserReasonIdentity;

        /**
         * Constructs a per-user request entry.
         *
         * @param userJid               the target user JID; never
         *                              {@code null}
         * @param hasUserReasonIdentity whether to include the
         *                              identity-reason hint
         * @throws NullPointerException if {@code userJid} is
         *                              {@code null}
         */
        public UserKeyRequest(Jid userJid, boolean hasUserReasonIdentity) {
            this.userJid = Objects.requireNonNull(userJid, "userJid cannot be null");
            this.hasUserReasonIdentity = hasUserReasonIdentity;
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
         * @return {@code true} when the hint is set; {@code false}
         *         otherwise
         */
        public boolean hasUserReasonIdentity() {
            return hasUserReasonIdentity;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (UserKeyRequest) obj;
            return this.hasUserReasonIdentity == that.hasUserReasonIdentity
                    && Objects.equals(this.userJid, that.userJid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userJid, hasUserReasonIdentity);
        }

        @Override
        public String toString() {
            return "SmaxPreKeysFetchKeyBundlesRequest.UserKeyRequest[userJid=" + userJid
                    + ", hasUserReasonIdentity=" + hasUserReasonIdentity + ']';
        }
    }
}
