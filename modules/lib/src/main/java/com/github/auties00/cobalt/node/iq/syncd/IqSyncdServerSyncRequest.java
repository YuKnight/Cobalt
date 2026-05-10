package com.github.auties00.cobalt.node.iq.syncd;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SequencedCollection;

/**
 * The outbound syncd sync IQ. Wraps a typed list of
 * {@link IqSyncdServerSyncRequestCollection} entries inside the canonical
 * {@code <iq xmlns="w:sync:app:state" type="set"><sync>…</sync></iq>}
 * envelope.
 *
 * <p>Each collection entry contributes one {@code <collection>} child
 * with the standard {@code name}, {@code return_snapshot} and
 * {@code version} attributes. Entries that ship local mutations
 * additionally attach a {@code <patch>} grandchild carrying the encoded
 * {@code SyncdPatch} protobuf bytes.
 */
@WhatsAppWebModule(moduleName = "WAWebSyncdServerSync")
@WhatsAppWebModule(moduleName = "WAWebSyncdRequestBuilderBuild")
public final class IqSyncdServerSyncRequest implements IqOperation.Request {
    /**
     * The default collection version used in the {@code version}
     * attribute when the caller has never synced a collection.
     */
    @WhatsAppWebExport(moduleName = "WASyncdConst",
            exports = "DEFAULT_COLLECTION_VERSION", adaptation = WhatsAppAdaptation.DIRECT)
    public static final long DEFAULT_COLLECTION_VERSION = 0L;

    /**
     * The list of collection entries to sync.
     */
    private final List<IqSyncdServerSyncRequestCollection> collections;

    /**
     * Constructs a new server-sync request.
     *
     * @param collections the list of collection entries. Never
     *                    {@code null}, may be empty (the
     *                    no-collection case produces a degenerate
     *                    {@code <sync/>} child)
     * @throws NullPointerException if {@code collections} is
     *                              {@code null}
     */
    public IqSyncdServerSyncRequest(List<IqSyncdServerSyncRequestCollection> collections) {
        Objects.requireNonNull(collections, "collections cannot be null");
        this.collections = collections;
    }

    /**
     * Returns the list of collection entries.
     *
     * @return an unmodifiable view of the entries. Never {@code null},
     *         possibly empty
     */
    public SequencedCollection<IqSyncdServerSyncRequestCollection> collections() {
        return Collections.unmodifiableSequencedCollection(collections);
    }

    /**
     * Builds the outbound IQ stanza from the typed collection list.
     *
     * @return a {@link NodeBuilder} carrying the
     *         {@code <iq xmlns="w:sync:app:state" type="set">} envelope
     *         and the typed {@code <sync>}/{@code <collection>}
     *         payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebSyncdRequestBuilderBuild",
            exports = "buildSyncIqNode", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        // WAWebSyncdRequestBuilderBuild._buildCollectionNodes: project each typed
        // entry into wap("collection", {name, return_snapshot, version}, optionalPatch)
        var collectionNodes = new ArrayList<Node>(collections.size());
        for (var collection : collections) {
            var collectionBuilder = new NodeBuilder()
                    .description("collection")
                    .attribute("name", collection.name())
                    .attribute("return_snapshot", collection.version().isEmpty() ? "true" : "false")
                    .attribute("version", collection.version().orElse(DEFAULT_COLLECTION_VERSION));
            // WAWebSyncdRequestBuilderBuild._buildCollectionNodes: optional patch grandchild
            collection.patch().ifPresent(patch -> {
                var patchNode = new NodeBuilder()
                        .description("patch")
                        .content(patch)
                        .build();
                collectionBuilder.content(patchNode);
            });
            collectionNodes.add(collectionBuilder.build());
        }
        // WAWebSyncdRequestBuilderBuild.buildSyncIqNode: wap("sync", null, collectionNodes)
        var syncNode = new NodeBuilder()
                .description("sync")
                .content(collectionNodes)
                .build();
        // WAWebSyncdRequestBuilderBuild.buildSyncIqNode: wap("iq", {id, to: S_WHATSAPP_NET,
        // type: "set", xmlns: "w:sync:app:state"}, syncNode) - id is added by sendNode when missing
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:sync:app:state")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(syncNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqSyncdServerSyncRequest) obj;
        return Objects.equals(this.collections, that.collections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collections);
    }

    @Override
    public String toString() {
        return "IqSyncdServerSyncRequest[collections=" + collections + ']';
    }
}
