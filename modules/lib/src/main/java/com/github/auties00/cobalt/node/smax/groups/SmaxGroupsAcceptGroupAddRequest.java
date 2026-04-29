package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the
 * {@code <accept code expiration admin/>} payload.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsAcceptGroupAddRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsAcceptGroupAddRequest implements SmaxOperation.Request {
    /**
     * The group JID hosting the pending {@code add_request}.
     */
    private final Jid groupJid;

    /**
     * The accept code copied verbatim from the originating
     * {@code <add_request/>} payload.
     */
    private final String acceptCode;

    /**
     * The expiration timestamp (seconds) carried by the originating
     * {@code <add_request/>}.
     */
    private final long acceptExpiration;

    /**
     * The admin who authored the pending {@code add_request}.
     */
    private final Jid acceptAdmin;

    /**
     * Constructs a request.
     *
     * @param groupJid         the group JID; never {@code null}
     * @param acceptCode       the accept code; never {@code null}
     * @param acceptExpiration the expiration timestamp (seconds)
     * @param acceptAdmin      the inviting admin; never {@code null}
     * @throws NullPointerException if {@code groupJid},
     *                              {@code acceptCode} or
     *                              {@code acceptAdmin} is {@code null}
     */
    public SmaxGroupsAcceptGroupAddRequest(Jid groupJid, String acceptCode, long acceptExpiration, Jid acceptAdmin) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        this.acceptCode = Objects.requireNonNull(acceptCode, "acceptCode cannot be null");
        this.acceptExpiration = acceptExpiration;
        this.acceptAdmin = Objects.requireNonNull(acceptAdmin, "acceptAdmin cannot be null");
    }

    /**
     * Returns the group JID.
     *
     * @return the group JID; never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns the accept code.
     *
     * @return the accept code; never {@code null}
     */
    public String acceptCode() {
        return acceptCode;
    }

    /**
     * Returns the expiration timestamp (seconds since epoch).
     *
     * @return the expiration
     */
    public long acceptExpiration() {
        return acceptExpiration;
    }

    /**
     * Returns the inviting admin's JID.
     *
     * @return the admin JID; never {@code null}
     */
    public Jid acceptAdmin() {
        return acceptAdmin;
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <accept/>} payload
     *
     * @implNote {@code WASmaxOutGroupsAcceptGroupAddRequest.makeAcceptGroupAddRequest}
     *           composes {@code WASmaxOutGroupsBaseSetGroupMixin}
     *           over a {@code <accept code=CUSTOM_STRING(t)
     *           expiration=INT(n) admin=USER_JID(r)/>} child.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsAcceptGroupAddRequest",
            exports = "makeAcceptGroupAddRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutGroupsAcceptGroupAddRequest: smax("accept", {code, expiration, admin})
        var acceptNode = new NodeBuilder()
                .description("accept")
                .attribute("code", acceptCode)
                .attribute("expiration", acceptExpiration)
                .attribute("admin", acceptAdmin)
                .build();
        // WASmaxOutGroupsBaseSetGroupMixin: smax("iq", {to: GROUP_JID(t), xmlns: "w:g2"})
        // WASmaxOutGroupsBaseIQSetRequestMixin: smax("iq", {id: generateId(), type: "set"})
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(acceptNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsAcceptGroupAddRequest) obj;
        return this.acceptExpiration == that.acceptExpiration
                && Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.acceptCode, that.acceptCode)
                && Objects.equals(this.acceptAdmin, that.acceptAdmin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, acceptCode, acceptExpiration, acceptAdmin);
    }

    @Override
    public String toString() {
        return "SmaxGroupsAcceptGroupAddRequest[groupJid=" + groupJid
                + ", acceptCode=" + acceptCode
                + ", acceptExpiration=" + acceptExpiration
                + ", acceptAdmin=" + acceptAdmin + ']';
    }
}
