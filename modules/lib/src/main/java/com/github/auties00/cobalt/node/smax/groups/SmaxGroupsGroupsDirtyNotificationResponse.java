package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The inbound notification — the relay's "your group list is stale"
 * hint carrying the affected group JIDs.
 *
 * @implNote {@code WASmaxInGroupsGroupsDirtyNotificationRequest.parseGroupsDirtyNotificationRequest}
 *           validates the {@code <notification type="w:gp2"
 *           from="g.us">} envelope, extracts the
 *           {@code <groups_dirty>} child, and projects every
 *           {@code <group jid="…"/>} grandchild into the
 *           {@link #dirtyGroups()} list.
 */
@WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupsDirtyNotificationRequest")
@WhatsAppWebModule(moduleName = "WASmaxInGroupsServerNotificationMixin")
public final class SmaxGroupsGroupsDirtyNotificationResponse implements SmaxOperation.Response {
    /**
     * The affected group JIDs (1..10000 entries per WA Web).
     */
    private final List<Jid> dirtyGroups;

    /**
     * Constructs a new inbound projection.
     *
     * @param dirtyGroups the list of stale-group JIDs; never
     *                    {@code null} (defaults to empty)
     */
    public SmaxGroupsGroupsDirtyNotificationResponse(List<Jid> dirtyGroups) {
        this.dirtyGroups = List.copyOf(Objects.requireNonNullElse(dirtyGroups, List.of()));
    }

    /**
     * Returns the list of affected group JIDs.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<Jid> dirtyGroups() {
        return dirtyGroups;
    }

    /**
     * Tries to parse an {@link SmaxGroupsGroupsDirtyNotificationResponse} projection from the given
     * {@code <notification/>} stanza.
     *
     * @param node the inbound notification stanza
     * @return an {@link Optional} carrying the projection, or empty
     *         when the stanza doesn't match the expected shape
     */
    @WhatsAppWebExport(moduleName = "WASmaxInGroupsGroupsDirtyNotificationRequest",
            exports = "parseGroupsDirtyNotificationRequest",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxGroupsGroupsDirtyNotificationResponse> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        if (!node.hasDescription("notification")) {
            return Optional.empty();
        }
        if (!node.hasAttribute("type", "w:gp2")) {
            return Optional.empty();
        }
        // The from attribute domain must equal the group server
        var from = node.getAttributeAsJid("from").orElse(null);
        if (from == null || !"g.us".equals(from.server().toString())) {
            return Optional.empty();
        }
        var groupsDirty = node.getChild("groups_dirty").orElse(null);
        if (groupsDirty == null) {
            return Optional.empty();
        }
        var groups = groupsDirty.streamChildren("group")
                .map(child -> child.getAttributeAsJid("jid").orElse(null))
                .filter(Objects::nonNull)
                .toList();
        return Optional.of(new SmaxGroupsGroupsDirtyNotificationResponse(groups));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGroupsGroupsDirtyNotificationResponse) obj;
        return Objects.equals(this.dirtyGroups, that.dirtyGroups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dirtyGroups);
    }

    @Override
    public String toString() {
        return "SmaxGroupsGroupsDirtyNotificationResponse[dirtyGroups=" + dirtyGroups + ']';
    }
}
