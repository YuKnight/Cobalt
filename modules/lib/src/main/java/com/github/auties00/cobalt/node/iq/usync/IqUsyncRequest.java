package com.github.auties00.cobalt.node.iq.usync;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.util.RandomIdUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the {@code <usync/>}
 * envelope with the per-protocol query elements and the per-user
 * list.
 */
@WhatsAppWebModule(moduleName = "WAWebUsync")
public final class IqUsyncRequest implements IqOperation.Request {
    /**
     * The query mode driving the relay's caching strategy.
     */
    private final IqUsyncMode mode;

    /**
     * The {@code context} attribute. Free-form caller-supplied
     * tag describing why the query is being issued (the WA Web
     * {@code USyncQuery} default is {@code "interactive"}).
     */
    private final String context;

    /**
     * The list of bare protocol-tag descriptions to query for
     * each user. Routed into one bare grandchild of the
     * {@code <query/>} child per entry (e.g. an entry of
     * {@code "devices"} yields {@code <devices/>}). The WA Web
     * usync child modules are the canonical source of valid
     * tags.
     */
    private final List<String> protocols;

    /**
     * The list of users being queried.
     */
    private final List<User> users;

    /**
     * Constructs a new usync request.
     *
     * @param mode      the query mode. Never {@code null}
     * @param context   the context tag. Never {@code null}
     * @param protocols the protocol tags. Never {@code null} and
     *                  never empty
     * @param users     the users to query. Never {@code null}
     * @throws NullPointerException     if any reference argument
     *                                  is {@code null}
     * @throws IllegalArgumentException if {@code protocols} is
     *                                  empty
     */
    public IqUsyncRequest(IqUsyncMode mode, String context, List<String> protocols, List<User> users) {
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
        this.context = Objects.requireNonNull(context, "context cannot be null");
        Objects.requireNonNull(protocols, "protocols cannot be null");
        if (protocols.isEmpty()) {
            throw new IllegalArgumentException("protocols cannot be empty");
        }
        this.protocols = List.copyOf(protocols);
        Objects.requireNonNull(users, "users cannot be null");
        this.users = List.copyOf(users);
    }

    /**
     * Returns the query mode.
     *
     * @return the mode. Never {@code null}
     */
    public IqUsyncMode mode() {
        return mode;
    }

    /**
     * Returns the context tag.
     *
     * @return the tag. Never {@code null}
     */
    public String context() {
        return context;
    }

    /**
     * Returns the unmodifiable list of protocol tags.
     *
     * @return the tags. Never {@code null} or empty
     */
    public List<String> protocols() {
        return protocols;
    }

    /**
     * Returns the unmodifiable list of users.
     *
     * @return the users. Never {@code null}
     */
    public List<User> users() {
        return users;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the {@code <usync/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUsync",
            exports = "USyncQuery", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var queryChildren = new ArrayList<Node>(protocols.size());
        for (var protocol : protocols) {
            var protocolNode = new NodeBuilder()
                    .description(protocol)
                    .build();
            queryChildren.add(protocolNode);
        }
        var queryNode = new NodeBuilder()
                .description("query")
                .content(queryChildren)
                .build();
        var userChildren = new ArrayList<Node>(users.size());
        for (var user : users) {
            userChildren.add(user.toUserNode());
        }
        var listNode = new NodeBuilder()
                .description("list")
                .content(userChildren)
                .build();
        var usyncNode = new NodeBuilder()
                .description("usync")
                .attribute("sid", RandomIdUtils.newId())
                .attribute("index", "0")
                .attribute("last", "true")
                .attribute("mode", mode.wireValue())
                .attribute("context", context)
                .content(queryNode, listNode)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("id", RandomIdUtils.newId())
                .attribute("to", JidServer.user())
                .attribute("xmlns", "usync")
                .attribute("type", "get")
                .content(usyncNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqUsyncRequest) obj;
        return this.mode == that.mode
                && Objects.equals(this.context, that.context)
                && Objects.equals(this.protocols, that.protocols)
                && Objects.equals(this.users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, context, protocols, users);
    }

    @Override
    public String toString() {
        return "IqUsyncRequest[mode=" + mode
                + ", context=" + context
                + ", protocols=" + protocols
                + ", users=" + users + ']';
    }

    /**
     * Typed user entry inside the outbound {@code <list/>}
     * envelope. One {@code <user jid pn_jid>} subtree carrying
     * any protocol-specific user-element payloads.
     *
     * <p>Both {@link #userJid()} and {@link #pnJid()} are
     * optional because some user shapes (e.g. username-lookup,
     * phone-only-lookup) carry exactly one of the two
     * identifiers. At least one must be present otherwise WA
     * Web's {@code USyncQuery.validate} drops the entry.
     */
    @WhatsAppWebModule(moduleName = "WAWebUsync")
    @WhatsAppWebModule(moduleName = "WAWebUsyncUser")
    public static final class User {
        /**
         * The primary user JID. Emitted in the {@code jid}
         * attribute when present.
         */
        private final Jid userJid;

        /**
         * The secondary phone-number JID. Emitted in the
         * {@code pn_jid} attribute when present. Used for the
         * dual-jid (LID + PN) flow.
         */
        private final Jid pnJid;

        /**
         * The list of per-protocol user-element payload nodes
         * (e.g. {@code <contact>+15551234567</contact>},
         * {@code <devices device_hash ts/>}). Routed verbatim as
         * children of the {@code <user/>} subtree.
         */
        private final List<Node> userPayloads;

        /**
         * Constructs a new user entry.
         *
         * @param userJid      the primary JID. May be
         *                     {@code null} when only
         *                     {@code pnJid} is supplied
         * @param pnJid        the phone-number JID. May be
         *                     {@code null} when only
         *                     {@code userJid} is supplied
         * @param userPayloads the per-protocol payload nodes.
         *                     Never {@code null}
         * @throws NullPointerException if {@code userPayloads}
         *                              is {@code null}
         */
        public User(Jid userJid, Jid pnJid, List<Node> userPayloads) {
            this.userJid = userJid;
            this.pnJid = pnJid;
            Objects.requireNonNull(userPayloads, "userPayloads cannot be null");
            this.userPayloads = List.copyOf(userPayloads);
        }

        /**
         * Returns the optional primary user JID.
         *
         * @return an {@link Optional} carrying the JID, or empty
         *         when only the phone JID was supplied
         */
        public Optional<Jid> userJid() {
            return Optional.ofNullable(userJid);
        }

        /**
         * Returns the optional phone-number JID.
         *
         * @return an {@link Optional} carrying the JID, or empty
         *         when only the primary JID was supplied
         */
        public Optional<Jid> pnJid() {
            return Optional.ofNullable(pnJid);
        }

        /**
         * Returns the unmodifiable list of per-protocol payload
         * nodes.
         *
         * @return the payloads. Never {@code null}
         */
        public List<Node> userPayloads() {
            return userPayloads;
        }

        /**
         * Renders this user entry as the {@code <user/>} subtree
         * routed inside the outbound {@code <list/>} envelope.
         *
         * @return the rendered node
         */
        @WhatsAppWebExport(moduleName = "WAWebUsync",
                exports = "USyncQuery", adaptation = WhatsAppAdaptation.ADAPTED)
        public Node toUserNode() {
            var builder = new NodeBuilder()
                    .description("user");
            if (userJid != null) {
                builder = builder.attribute("jid", userJid);
            }
            if (pnJid != null) {
                builder = builder.attribute("pn_jid", pnJid);
            }
            return builder
                    .content(new ArrayList<>(userPayloads))
                    .build();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (User) obj;
            return Objects.equals(this.userJid, that.userJid)
                    && Objects.equals(this.pnJid, that.pnJid)
                    && Objects.equals(this.userPayloads, that.userPayloads);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userJid, pnJid, userPayloads);
        }

        @Override
        public String toString() {
            return "Request.User[userJid=" + userJid
                    + ", pnJid=" + pnJid
                    + ", userPayloads=" + userPayloads + ']';
        }
    }
}
