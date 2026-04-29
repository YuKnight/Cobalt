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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant — wraps the
 * {@code <sub_group_suggestion/>} payload in the canonical
 * {@code <iq xmlns="w:g2" type="set" to="<parent>">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsCreateSubGroupSuggestionRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseSetGroupMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutGroupsBaseIQSetRequestMixin")
public final class SmaxGroupsCreateSubGroupSuggestionRequest implements SmaxOperation.Request {
    /**
     * The parent (community) group JID. Routed verbatim into the IQ's
     * {@code to} attribute.
     */
    private final Jid parentGroupJid;

    /**
     * The suggestion body — either {@link SmaxGroupsCreateSubGroupSuggestionSuggestion.NewGroup} or
     * {@link SmaxGroupsCreateSubGroupSuggestionSuggestion.ExistingGroups}.
     */
    private final SmaxGroupsCreateSubGroupSuggestionSuggestion suggestion;

    /**
     * Constructs a request.
     *
     * @param parentGroupJid the parent community JID; never
     *                       {@code null}
     * @param suggestion     the suggestion body; never {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public SmaxGroupsCreateSubGroupSuggestionRequest(Jid parentGroupJid, SmaxGroupsCreateSubGroupSuggestionSuggestion suggestion) {
        this.parentGroupJid = Objects.requireNonNull(parentGroupJid, "parentGroupJid cannot be null");
        this.suggestion = Objects.requireNonNull(suggestion, "suggestion cannot be null");
    }

    /**
     * Returns the parent group JID.
     *
     * @return the parent group JID; never {@code null}
     */
    public Jid parentGroupJid() {
        return parentGroupJid;
    }

    /**
     * Returns the suggestion body.
     *
     * @return the suggestion; never {@code null}
     */
    public SmaxGroupsCreateSubGroupSuggestionSuggestion suggestion() {
        return suggestion;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <sub_group_suggestion/>} payload
     *
     * @implNote {@code WASmaxOutGroupsCreateSubGroupSuggestionRequest.makeCreateSubGroupSuggestionRequest}
     *           composes
     *           {@code WASmaxOutGroupsBaseSetGroupMixin} over a
     *           {@code <sub_group_suggestion/>} root that gets merged
     *           with either the new-group or existing-groups mixin.
     *           Cobalt builds the same shape inline; the
     *           {@code id} attribute is injected by
     *           {@code WhatsAppClient.sendNode}.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutGroupsCreateSubGroupSuggestionRequest",
            exports = "makeCreateSubGroupSuggestionRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var suggestionBuilder = new NodeBuilder()
                .description("sub_group_suggestion");
        suggestion.contributeTo(suggestionBuilder);
        var suggestionNode = suggestionBuilder.build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:g2")
                .attribute("to", parentGroupJid)
                .attribute("type", "set")
                .content(suggestionNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsCreateSubGroupSuggestionRequest) obj;
        return Objects.equals(this.parentGroupJid, that.parentGroupJid)
                && Objects.equals(this.suggestion, that.suggestion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentGroupJid, suggestion);
    }

    @Override
    public String toString() {
        return "SmaxGroupsCreateSubGroupSuggestionRequest[parentGroupJid=" + parentGroupJid
                + ", suggestion=" + suggestion + ']';
    }
}
