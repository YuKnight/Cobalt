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
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsGetLinkedGroupRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseGetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQGetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsQueryLinkedGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsOptionalSubGroupMixin")
public final class SmaxGroupsGetLinkedGroupRequest implements SmaxOperation.Request {
    /**
     * The group JID the IQ is addressed to (the "anchor" group whose
     * linked counterpart is being looked up).
     */
    private final Jid groupJid;

    /**
     * The linkage direction selector — {@code "parent_group"} or
     * {@code "sub_group"}.
     */
    private final String queryLinkedType;

    /**
     * The linked group's JID — interpretation depends on
     * {@link #queryLinkedType}: it identifies the parent community
     * when {@code type="parent_group"}, or the target sub-group
     * when {@code type="sub_group"}.
     */
    private final Jid queryLinkedJid;

    /**
     * The optional disambiguation hint pinning the sub-group target.
     */
    private final Jid subGroupJid;

    /**
     * Constructs a request without a sub-group disambiguation hint.
     *
     * @param groupJid        the IQ-{@code to} group; never
     *                        {@code null}
     * @param queryLinkedType the linkage direction; never
     *                        {@code null}
     * @param queryLinkedJid  the linked group JID; never
     *                        {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public SmaxGroupsGetLinkedGroupRequest(Jid groupJid, String queryLinkedType, Jid queryLinkedJid) {
        this(groupJid, queryLinkedType, queryLinkedJid, null);
    }

    /**
     * Constructs a fully-parametrised request.
     *
     * @param groupJid        the IQ-{@code to} group; never
     *                        {@code null}
     * @param queryLinkedType the linkage direction; never
     *                        {@code null}
     * @param queryLinkedJid  the linked group JID; never
     *                        {@code null}
     * @param subGroupJid     the optional sub-group disambiguation
     *                        hint; may be {@code null}
     * @throws NullPointerException if {@code groupJid},
     *                              {@code queryLinkedType} or
     *                              {@code queryLinkedJid} is
     *                              {@code null}
     */
    public SmaxGroupsGetLinkedGroupRequest(Jid groupJid, String queryLinkedType, Jid queryLinkedJid, Jid subGroupJid) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        this.queryLinkedType = Objects.requireNonNull(queryLinkedType, "queryLinkedType cannot be null");
        this.queryLinkedJid = Objects.requireNonNull(queryLinkedJid, "queryLinkedJid cannot be null");
        this.subGroupJid = subGroupJid;
    }

    /**
     * Returns the IQ-{@code to} group JID.
     *
     * @return the group JID; never {@code null}
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns the linkage direction.
     *
     * @return the type token; never {@code null}
     */
    public String queryLinkedType() {
        return queryLinkedType;
    }

    /**
     * Returns the linked group's JID.
     *
     * @return the linked group JID; never {@code null}
     */
    public Jid queryLinkedJid() {
        return queryLinkedJid;
    }

    /**
     * Returns the optional sub-group disambiguation hint.
     *
     * @return an {@link Optional} carrying the sub-group JID
     */
    public Optional<Jid> subGroupJid() {
        return Optional.ofNullable(subGroupJid);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <query_linked/>} payload
     *
     * @implNote {@code WASmaxOutGroupsGetLinkedGroupRequest.makeGetLinkedGroupRequest}
     *           composes
     *           {@code <query_linked type=CUSTOM_STRING(t)
     *           jid=GROUP_JID(t) sub_group_jid?>}, wrapped in
     *           {@code WASmaxOutGroupsBaseGetGroupMixin}
     *           ({@code xmlns="w:g2"}, {@code to=GROUP_JID(t)},
     *           {@code id=generateId()}, {@code type="get"}).
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsGetLinkedGroupRequest",
            exports = "makeGetLinkedGroupRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var queryLinkedBuilder = new NodeBuilder()
                .description("query_linked")
                .attribute("type", queryLinkedType)
                .attribute("jid", queryLinkedJid);
        if (subGroupJid != null) {
            queryLinkedBuilder.attribute("sub_group_jid", subGroupJid);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "get")
                .content(queryLinkedBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsGetLinkedGroupRequest) obj;
        return Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.queryLinkedType, that.queryLinkedType)
                && Objects.equals(this.queryLinkedJid, that.queryLinkedJid)
                && Objects.equals(this.subGroupJid, that.subGroupJid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, queryLinkedType, queryLinkedJid, subGroupJid);
    }

    @Override
    public String toString() {
        return "SmaxGroupsGetLinkedGroupRequest[groupJid=" + groupJid
                + ", queryLinkedType=" + queryLinkedType
                + ", queryLinkedJid=" + queryLinkedJid
                + ", subGroupJid=" + subGroupJid + ']';
    }
}
