package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsUpdateBlockListRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsUpdateBlockListBlockItemMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsUpdateBlockListUnblockItemMixin")
public final class SmaxUpdateBlockListRequest implements SmaxOperation.Request {
    /**
     * The action to perform.
     */
    private final SmaxUpdateBlockListAction action;

    /**
     * The target user JID.
     */
    private final Jid itemJid;

    /**
     * The optional client-side digest of the cached blocklist.
     * supplied so the relay can return a {@code SuccessWithMatch}
     * envelope when the cache is up to date.
     */
    private final String itemDhash;

    /**
     * The optional report-block entry-point source. Surfaced when
     * the block originates from a "report and block" action so the
     * relay can attribute the report.
     */
    private final String entryPointSource;

    /**
     * Constructs a request.
     *
     * @param action           the action; never {@code null}
     * @param itemJid          the target JID; never {@code null}
     * @param itemDhash        the cached digest; may be {@code null}
     * @param entryPointSource the report entry-point source; may be
     *                         {@code null}
     * @throws NullPointerException if {@code action} or
     *                              {@code itemJid} is {@code null}
     */
    public SmaxUpdateBlockListRequest(SmaxUpdateBlockListAction action, Jid itemJid, String itemDhash, String entryPointSource) {
        this.action = Objects.requireNonNull(action, "action cannot be null");
        this.itemJid = Objects.requireNonNull(itemJid, "itemJid cannot be null");
        this.itemDhash = itemDhash;
        this.entryPointSource = entryPointSource;
    }

    /**
     * Returns the action.
     *
     * @return the action; never {@code null}
     */
    public SmaxUpdateBlockListAction action() {
        return action;
    }

    /**
     * Returns the target user JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid itemJid() {
        return itemJid;
    }

    /**
     * Returns the optional cached digest.
     *
     * @return an {@link Optional} carrying the digest, or empty when
     *         omitted
     */
    public Optional<String> itemDhash() {
        return Optional.ofNullable(itemDhash);
    }

    /**
     * Returns the optional report entry-point source.
     *
     * @return an {@link Optional} carrying the entry-point source,
     *         or empty when omitted
     */
    public Optional<String> entryPointSource() {
        return Optional.ofNullable(entryPointSource);
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsUpdateBlockListRequest",
            exports = "makeUpdateBlockListRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var itemBuilder = new NodeBuilder()
                .description("item")
                .attribute("action", action.wire())
                .attribute("jid", itemJid);
        if (itemDhash != null) {
            itemBuilder.attribute("dhash", itemDhash);
        }
        var iqBuilder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "blocklist")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(itemBuilder.build());
        if (entryPointSource != null) {
            var entryPointNode = new NodeBuilder()
                    .description("entry_point")
                    .attribute("source", entryPointSource)
                    .build();
            iqBuilder.content(entryPointNode);
        }
        return iqBuilder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxUpdateBlockListRequest) obj;
        return this.action == that.action
                && Objects.equals(this.itemJid, that.itemJid)
                && Objects.equals(this.itemDhash, that.itemDhash)
                && Objects.equals(this.entryPointSource, that.entryPointSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, itemJid, itemDhash, entryPointSource);
    }

    @Override
    public String toString() {
        return "SmaxUpdateBlockListRequest[action=" + action
                + ", itemJid=" + itemJid
                + ", itemDhash=" + itemDhash
                + ", entryPointSource=" + entryPointSource + ']';
    }
}
