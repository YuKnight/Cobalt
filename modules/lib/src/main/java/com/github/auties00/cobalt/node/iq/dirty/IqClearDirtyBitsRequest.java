package com.github.auties00.cobalt.node.iq.dirty;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The outbound {@code <iq xmlns="urn:xmpp:whatsapp:dirty" type="set">}
 * stanza variant — wraps one {@code <clean type timestamp/>} child per
 * dirty entry being cleared.
 */
@WhatsAppWebModule(moduleName = "WAWebClearDirtyBitsJob")
public final class IqClearDirtyBitsRequest implements IqOperation.Request {
    /**
     * The list of dirty-bit entries to clear. Must be non-empty —
     * WA Web's {@code clearDirtyBits} early-returns on empty input
     * without dispatching anything.
     */
    private final List<DirtyEntry> entries;

    /**
     * Constructs a new clear-dirty-bits request.
     *
     * @param entries the list of dirty entries to clear; never
     *                {@code null} and never empty
     * @throws NullPointerException     if {@code entries} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code entries} is empty
     */
    public IqClearDirtyBitsRequest(List<DirtyEntry> entries) {
        Objects.requireNonNull(entries, "entries cannot be null");
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("entries cannot be empty");
        }
        this.entries = List.copyOf(entries);
    }

    /**
     * Returns the unmodifiable list of dirty entries being cleared.
     *
     * @return the entries; never {@code null} or empty
     */
    public List<DirtyEntry> entries() {
        return entries;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <clean/>} payload children
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebClearDirtyBitsJob",
            exports = "clearDirtyBits", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        // WAWebClearDirtyBitsJob: t.map(e => wap("clean", {type, timestamp}))
        var cleanNodes = new ArrayList<Node>(entries.size());
        for (var entry : entries) {
            var cleanNode = new NodeBuilder()
                    .description("clean")
                    .attribute("type", entry.type())
                    .attribute("timestamp", entry.timestamp())
                    .build();
            cleanNodes.add(cleanNode);
        }
        // WAWebClearDirtyBitsJob: wap("iq", {to:S_WHATSAPP_NET, type:"set", xmlns, id}, cleans)
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "urn:xmpp:whatsapp:dirty")
                .attribute("to", Jid.userServer())
                .attribute("type", "set")
                .content(cleanNodes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqClearDirtyBitsRequest) obj;
        return Objects.equals(this.entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }

    @Override
    public String toString() {
        return "IqClearDirtyBitsRequest[entries=" + entries + ']';
    }

    /**
     * Per-resource dirty-bit entry — a {@code (type, timestamp)} pair
     * routed verbatim into one {@code <clean/>} child of the outbound
     * IQ.
     */
    @WhatsAppWebModule(moduleName = "WAWebClearDirtyBitsJob")
    public static final class DirtyEntry {
        /**
         * The resource type (e.g. {@code "account_sync"},
         * {@code "groups"}, {@code "blocklist"}). Routed verbatim into
         * the {@code type} attribute.
         */
        private final String type;

        /**
         * The high-water-mark timestamp the relay should treat as
         * "clean as of". Routed verbatim into the {@code timestamp}
         * attribute as an integer.
         */
        private final long timestamp;

        /**
         * Constructs a new dirty-bit entry.
         *
         * @param type      the resource type; never {@code null}
         * @param timestamp the high-water-mark timestamp
         * @throws NullPointerException if {@code type} is {@code null}
         */
        public DirtyEntry(String type, long timestamp) {
            this.type = Objects.requireNonNull(type, "type cannot be null");
            this.timestamp = timestamp;
        }

        /**
         * Returns the resource type.
         *
         * @return the type; never {@code null}
         */
        public String type() {
            return type;
        }

        /**
         * Returns the high-water-mark timestamp.
         *
         * @return the timestamp
         */
        public long timestamp() {
            return timestamp;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (DirtyEntry) obj;
            return this.timestamp == that.timestamp
                    && Objects.equals(this.type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, timestamp);
        }

        @Override
        public String toString() {
            return "Request.DirtyEntry[type=" + type
                    + ", timestamp=" + timestamp + ']';
        }
    }
}
