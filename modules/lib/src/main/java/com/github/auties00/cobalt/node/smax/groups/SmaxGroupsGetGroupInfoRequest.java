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
 * The outbound stanza variant — wraps the {@code <query/>} payload
 * (with optional {@code phash} dehydration hint and optional
 * {@code <add_request/>} probe) in the canonical
 * {@code <iq xmlns="w:g2" type="get" to="<groupJid>">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetGroupInfoRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseGetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsAddRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsCodeMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetGroupInfoRequestTypeMixin")
public final class SmaxGroupsGetGroupInfoRequest implements SmaxOperation.Request {
    /**
     * The group JID whose metadata is being queried. Routed verbatim
     * into the IQ's {@code to} attribute.
     */
    private final Jid groupJid;

    /**
     * The optional dehydration hash hint. When supplied, the relay
     * may skip parts of the projection that haven't changed since
     * the caller's last fetch and return a delta-only response.
     */
    private final String queryPhash;

    /**
     * The optional V4-invite-link {@code <add_request expiration="…"
     * admin="…"/>} expiration timestamp.
     */
    private final Long addRequestExpiration;

    /**
     * The optional V4-invite-link admin-targeted {@code add_request}
     * recipient. Mutually exclusive with {@link #addRequestCode}.
     */
    private final Jid addRequestAdmin;

    /**
     * The optional V4-invite-link code-targeted {@code add_request}
     * code. Mutually exclusive with {@link #addRequestAdmin}.
     */
    private final String addRequestCode;

    /**
     * Constructs a metadata-only request for the given group.
     *
     * @param groupJid the group JID; never {@code null}
     * @throws NullPointerException if {@code groupJid} is {@code null}
     */
    public SmaxGroupsGetGroupInfoRequest(Jid groupJid) {
        this(groupJid, null, null, null, null);
    }

    /**
     * Constructs a fully-parametrised request.
     *
     * @param groupJid             the group JID; never {@code null}
     * @param queryPhash           the optional dehydration hash hint;
     *                             may be {@code null}
     * @param addRequestExpiration the optional add-request expiration
     *                             timestamp; may be {@code null}
     * @param addRequestAdmin      the optional add-request admin
     *                             target; may be {@code null}
     * @param addRequestCode       the optional add-request code
     *                             target; may be {@code null}
     * @throws NullPointerException if {@code groupJid} is {@code null}
     */
    public SmaxGroupsGetGroupInfoRequest(Jid groupJid, String queryPhash, Long addRequestExpiration,
                   Jid addRequestAdmin, String addRequestCode) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        this.queryPhash = queryPhash;
        this.addRequestExpiration = addRequestExpiration;
        this.addRequestAdmin = addRequestAdmin;
        this.addRequestCode = addRequestCode;
    }

    /**
     * Returns the group JID being queried.
     *
     * @return the group JID; never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns the optional dehydration hash.
     *
     * @return an {@link Optional} carrying the hash, or empty when
     *         the caller did not supply one
     */
    public Optional<String> queryPhash() {
        return Optional.ofNullable(queryPhash);
    }

    /**
     * Returns the optional V4-invite-link add-request expiration.
     *
     * @return an {@link Optional} carrying the expiration, or empty
     *         when the caller did not supply one
     */
    public Optional<Long> addRequestExpiration() {
        return Optional.ofNullable(addRequestExpiration);
    }

    /**
     * Returns the optional V4-invite-link add-request admin target.
     *
     * @return an {@link Optional} carrying the admin JID, or empty
     *         when the caller did not supply one
     */
    public Optional<Jid> addRequestAdmin() {
        return Optional.ofNullable(addRequestAdmin);
    }

    /**
     * Returns the optional V4-invite-link add-request code target.
     *
     * @return an {@link Optional} carrying the code, or empty when
     *         the caller did not supply one
     */
    public Optional<String> addRequestCode() {
        return Optional.ofNullable(addRequestCode);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <query/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsGetGroupInfoRequest",
            exports = "makeGetGroupInfoRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var queryBuilder = new NodeBuilder()
                .description("query");
        if (queryPhash != null) {
            queryBuilder.attribute("phash", queryPhash);
        }
        if (addRequestExpiration != null) {
            var addRequestBuilder = new NodeBuilder()
                    .description("add_request")
                    .attribute("expiration", addRequestExpiration);
            if (addRequestAdmin != null) {
                addRequestBuilder.attribute("admin", addRequestAdmin);
            }
            if (addRequestCode != null) {
                addRequestBuilder.attribute("code", addRequestCode);
            }
            queryBuilder.content(addRequestBuilder.build());
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "get")
                .content(queryBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsGetGroupInfoRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.queryPhash, that.queryPhash)
                && Objects.equals(this.addRequestExpiration, that.addRequestExpiration)
                && Objects.equals(this.addRequestAdmin, that.addRequestAdmin)
                && Objects.equals(this.addRequestCode, that.addRequestCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, queryPhash, addRequestExpiration, addRequestAdmin, addRequestCode);
    }

    @Override
    public String toString() {
        return "SmaxGroupsGetGroupInfoRequest[groupJid=" + groupJid
                + ", queryPhash=" + queryPhash
                + ", addRequestExpiration=" + addRequestExpiration
                + ", addRequestAdmin=" + addRequestAdmin
                + ", addRequestCode=" + addRequestCode + ']';
    }
}
