package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The outbound {@code <iq xmlns="w:biz" type="get">} stanza that fetches
 * one or more typed business profiles. Each entry materialises one
 * {@code <profile jid tag/>} child; the optional version tag lets the
 * relay short-circuit when the cached profile matches.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryBusinessProfileJob")
public final class IqQueryBusinessProfileRequest implements IqOperation.Request {
    /**
     * The list of {@code (businessJid, tag)} entries to query — at least
     * one entry is required.
     */
    private final List<IqQueryBusinessProfileRequestEntry> entries;

    /**
     * The protocol version emitted as the {@code <business_profile v=V/>}
     * attribute (currently {@code "1"} or {@code "116"} depending on
     * gating).
     */
    private final int version;

    /**
     * Constructs a request.
     *
     * @param entries the list of entries; never {@code null} and must be
     *                non-empty
     * @param version the protocol version; routed verbatim into the
     *                {@code v} attribute
     * @throws NullPointerException     if {@code entries} is {@code null}
     * @throws IllegalArgumentException when {@code entries} is empty
     */
    public IqQueryBusinessProfileRequest(List<IqQueryBusinessProfileRequestEntry> entries, int version) {
        Objects.requireNonNull(entries, "entries cannot be null");
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("entries cannot be empty");
        }
        this.entries = List.copyOf(entries);
        this.version = version;
    }

    /**
     * Returns the requested entries.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<IqQueryBusinessProfileRequestEntry> entries() {
        return entries;
    }

    /**
     * Returns the protocol version.
     *
     * @return the protocol version
     */
    public int version() {
        return version;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebQueryBusinessProfileJob",
            exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var profileNodes = new ArrayList<Node>();
        for (var entry : entries) {
            var profileBuilder = new NodeBuilder()
                    .description("profile")
                    .attribute("jid", entry.businessJid());
            if (entry.tag().isPresent()) {
                profileBuilder.attribute("tag", entry.tag().get());
            }
            profileNodes.add(profileBuilder.build());
        }
        var businessProfileNode = new NodeBuilder()
                .description("business_profile")
                .attribute("v", String.valueOf(version))
                .content(profileNodes)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(businessProfileNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqQueryBusinessProfileRequest) obj;
        return this.version == that.version
                && Objects.equals(this.entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries, version);
    }

    @Override
    public String toString() {
        return "IqQueryBusinessProfileRequest[entries=" + entries
                + ", version=" + version + ']';
    }
}
