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
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsSetDescriptionRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
public final class SmaxGroupsSetDescriptionRequest implements SmaxOperation.Request {
    /**
     * The group JID whose description is being changed.
     */
    private final Jid groupJid;

    /**
     * The new description revision id; {@code null} when only
     * {@code prev} is supplied (rare).
     */
    private final String descriptionId;

    /**
     * The previous description revision id being replaced; may be
     * {@code null} when this is the first description.
     */
    private final String descriptionPrev;

    /**
     * When {@code true}, the relay clears the description and ignores
     * {@code body}; when {@code false} the {@code body} is required.
     */
    private final boolean delete;

    /**
     * The new description body text; {@code null} when {@code delete}
     * is {@code true}.
     */
    private final String body;

    /**
     * Constructs a request.
     *
     * @param groupJid        the group JID; never {@code null}
     * @param descriptionId   the new revision id; may be {@code null}
     * @param descriptionPrev the previous revision id; may be
     *                        {@code null}
     * @param delete          {@code true} to clear the description
     * @param body            the new description text; required when
     *                        {@code delete} is {@code false}, may be
     *                        {@code null} otherwise
     * @throws NullPointerException if {@code groupJid} is {@code null}
     */
    public SmaxGroupsSetDescriptionRequest(Jid groupJid, String descriptionId, String descriptionPrev, boolean delete, String body) {
        this.groupJid = Objects.requireNonNull(groupJid, "groupJid cannot be null");
        this.descriptionId = descriptionId;
        this.descriptionPrev = descriptionPrev;
        this.delete = delete;
        this.body = body;
    }

    /**
     * Returns the group JID.
     *
     * @return the group JID
     */
    public Jid groupJid() {
        return groupJid;
    }

    /**
     * Returns the new description revision id.
     *
     * @return an {@link Optional} carrying the id, or empty when not
     *         set
     */
    public Optional<String> descriptionId() {
        return Optional.ofNullable(descriptionId);
    }

    /**
     * Returns the previous description revision id being replaced.
     *
     * @return an {@link Optional} carrying the previous id, or empty
     */
    public Optional<String> descriptionPrev() {
        return Optional.ofNullable(descriptionPrev);
    }

    /**
     * Returns whether this request clears the description rather than
     * replacing it.
     *
     * @return {@code true} when this is a delete request
     */
    public boolean delete() {
        return delete;
    }

    /**
     * Returns the new description body.
     *
     * @return an {@link Optional} carrying the new body, or empty
     */
    public Optional<String> body() {
        return Optional.ofNullable(body);
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         {@code <description/>} payload
     *
     * @implNote {@code WASmaxOutGroupsSetDescriptionRequest.makeSetDescriptionRequest}
     *           composes the {@code <description id prev delete>}
     *           child carrying the {@code <body/>} sub-element when
     *           {@code body} is supplied.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsSetDescriptionRequest",
            exports = "makeSetDescriptionRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WASmaxOutGroupsSetDescriptionRequest: smax("description", {id, prev, delete}, OPTIONAL_CHILD(body))
        var descriptionBuilder = new NodeBuilder().description("description");
        if (descriptionId != null) {
            descriptionBuilder.attribute("id", descriptionId);
        }
        if (descriptionPrev != null) {
            descriptionBuilder.attribute("prev", descriptionPrev);
        }
        if (delete) {
            descriptionBuilder.attribute("delete", "true");
        }
        if (body != null) {
            var bodyNode = new NodeBuilder()
                    .description("body")
                    .content(body.getBytes(StandardCharsets.UTF_8))
                    .build();
            descriptionBuilder.content(bodyNode);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", groupJid)
                .attribute("type", "set")
                .content(descriptionBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsSetDescriptionRequest) obj;
        return this.delete == that.delete
                && Objects.equals(this.groupJid, that.groupJid)
                && Objects.equals(this.descriptionId, that.descriptionId)
                && Objects.equals(this.descriptionPrev, that.descriptionPrev)
                && Objects.equals(this.body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupJid, descriptionId, descriptionPrev, delete, body);
    }

    @Override
    public String toString() {
        return "SmaxGroupsSetDescriptionRequest[groupJid=" + groupJid
                + ", descriptionId=" + descriptionId
                + ", descriptionPrev=" + descriptionPrev
                + ", delete=" + delete
                + ", body=" + body + ']';
    }
}
