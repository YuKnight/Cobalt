package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsUpdateBlockListRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsUpdateBlockListBlockOrUpdateBlockListUnblockItemMixinGroup")
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsUpdateBlockListBlockItemMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsUpdateBlockListUnblockItemMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsUpdateBlockListOrUpdateBlockListNonMigratedBlockItemMixinGroup")
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsUpdateBlockListNonMigratedBlockItemMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBlocklistsUpdateBlockListReportBlockEntryPointMixin")
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
     * The optional {@code <biz_opt_out>} child. Populated when the
     * block carries a marketing-message opt-out reason payload.
     */
    private final BizOptOut bizOptOut;

    /**
     * Constructs a request.
     *
     * @param action           the action; never {@code null}
     * @param itemJid          the target JID; never {@code null}
     * @param itemDhash        the cached digest; may be {@code null}
     * @param entryPointSource the report entry-point source; may be
     *                         {@code null}
     * @param bizOptOut        the optional {@code <biz_opt_out>}
     *                         payload; may be {@code null}
     * @throws NullPointerException if {@code action} or
     *                              {@code itemJid} is {@code null}
     */
    public SmaxUpdateBlockListRequest(SmaxUpdateBlockListAction action, Jid itemJid, String itemDhash, String entryPointSource, BizOptOut bizOptOut) {
        this.action = Objects.requireNonNull(action, "action cannot be null");
        this.itemJid = Objects.requireNonNull(itemJid, "itemJid cannot be null");
        this.itemDhash = itemDhash;
        this.entryPointSource = entryPointSource;
        this.bizOptOut = bizOptOut;
    }

    /**
     * Constructs a request without a {@code <biz_opt_out>} payload.
     *
     * <p>Convenience overload for the dominant call-pattern where the
     * caller never attaches a marketing-message opt-out reason.
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
        this(action, itemJid, itemDhash, entryPointSource, null);
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
     * Returns the optional {@code <biz_opt_out>} payload.
     *
     * @return an {@link Optional} carrying the payload, or empty when
     *         omitted
     */
    public Optional<BizOptOut> bizOptOut() {
        return Optional.ofNullable(bizOptOut);
    }

    /**
     * Builds the outbound IQ stanza.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsUpdateBlockListRequest",
            exports = "makeUpdateBlockListRequest", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsUpdateBlockListBlockOrUpdateBlockListUnblockItemMixinGroup",
            exports = "mergeUpdateBlockListBlockOrUpdateBlockListUnblockItemMixinGroup",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsUpdateBlockListBlockItemMixin",
            exports = "mergeUpdateBlockListBlockItemMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsUpdateBlockListUnblockItemMixin",
            exports = "mergeUpdateBlockListUnblockItemMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsUpdateBlockListOrUpdateBlockListNonMigratedBlockItemMixinGroup",
            exports = "mergeUpdateBlockListOrUpdateBlockListNonMigratedBlockItemMixinGroup",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsUpdateBlockListNonMigratedBlockItemMixin",
            exports = "mergeUpdateBlockListNonMigratedBlockItemMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsUpdateBlockListReportBlockEntryPointMixin",
            exports = "mergeUpdateBlockListReportBlockEntryPointMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var itemBuilder = new NodeBuilder()
                .description("item")
                // WASmaxOutBlocklistsUpdateBlockListBlockOrUpdateBlockListUnblockItemMixinGroup.mergeUpdateBlockListBlockOrUpdateBlockListUnblockItemMixinGroup:
                // dispatcher between t.updateBlockListBlockItem (-> BlockItemMixin) and
                // t.updateBlockListUnblockItem (-> UnblockItemMixin); throws SmaxMixinGroupExhaustiveError otherwise.
                // Cobalt closes the variant set with the SmaxUpdateBlockListAction enum, so the dispatch is
                // a single action.wire() lookup and the exhaustiveness throw is unreachable.
                // WASmaxOutBlocklistsUpdateBlockListBlockItemMixin.mergeUpdateBlockListBlockItemMixin:
                // smax("item", { action: "block" }) merged with the non-migrated/migrated dispatch group.
                // WASmaxOutBlocklistsUpdateBlockListUnblockItemMixin.mergeUpdateBlockListUnblockItemMixin:
                // same shape with action: "unblock". Cobalt collapses both into action.wire().
                .attribute("action", action.wire())
                // WASmaxOutBlocklistsUpdateBlockListOrUpdateBlockListNonMigratedBlockItemMixinGroup.mergeUpdateBlockListOrUpdateBlockListNonMigratedBlockItemMixinGroup:
                // dispatcher between t.updateBlockListMigratedBlockItem (LID-addressed, drops to MigratedBlockItemMixin)
                // and t.updateBlockListNonMigratedBlockItem (PN-addressed, drops to NonMigratedBlockItemMixin).
                // Cobalt only ever ships the non-migrated branch, so the dispatch degenerates to the line below.
                // WASmaxOutBlocklistsUpdateBlockListNonMigratedBlockItemMixin.mergeUpdateBlockListNonMigratedBlockItemMixin:
                // smax("item", { jid: WAWap.USER_JID(itemJid) }) — USER_JID is a thin wrapper over WAWap.JID,
                // and Cobalt's NodeBuilder.attribute(String, JidProvider) emits the wap-encoded JID directly.
                .attribute("jid", itemJid);
        if (itemDhash != null) {
            itemBuilder.attribute("dhash", itemDhash);
        }
        // WASmaxChildren.OPTIONAL_CHILD(makeUpdateBlockListRequestItemBizOptOut, bizOptOutArgs):
        // when bizOptOutArgs is non-null, the makeUpdateBlockListRequestItemBizOptOut
        // factory builds a <biz_opt_out> child of <item> with the seven optional
        // attributes (reason, reason_description, entry_point, first_message,
        // business_discovery_entry_point, business_discovery_timestamp,
        // business_discovery_id). Cobalt mirrors the factory inline below.
        if (bizOptOut != null) {
            itemBuilder.content(bizOptOut.toNode());
        }
        var iqBuilder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "blocklist")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(itemBuilder.build());
        // WASmaxOutBlocklistsUpdateBlockListReportBlockEntryPointMixin.mergeUpdateBlockListReportBlockEntryPointMixin:
        // smax("iq", null, smax("entry_point", { source: WAWap.CUSTOM_STRING(entryPointSource) })) merged into the
        // outbound IQ via WASmaxMixins.optionalMerge — applied only when entryPointSource is non-null, in which
        // case the merged child reduces to appending an <entry_point source="..."/> node to the IQ.
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
                && Objects.equals(this.entryPointSource, that.entryPointSource)
                && Objects.equals(this.bizOptOut, that.bizOptOut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, itemJid, itemDhash, entryPointSource, bizOptOut);
    }

    @Override
    public String toString() {
        return "SmaxUpdateBlockListRequest[action=" + action
                + ", itemJid=" + itemJid
                + ", itemDhash=" + itemDhash
                + ", entryPointSource=" + entryPointSource
                + ", bizOptOut=" + bizOptOut + ']';
    }

    /**
     * The {@code <biz_opt_out>} child carried by an {@code <item>}
     * node when the block records a marketing-message opt-out reason.
     *
     * <p>WA Web's
     * {@code WASmaxOutBlocklistsUpdateBlockListRequest.makeUpdateBlockListRequestItemBizOptOut}
     * factory builds a single {@code <biz_opt_out>} stanza whose seven
     * attributes are all optional and therefore guarded by
     * {@code WASmaxAttrs.OPTIONAL}. Cobalt models the payload as an
     * immutable record with seven nullable fields and renders the
     * stanza via {@link #toNode()}; missing fields are simply not
     * emitted as attributes.
     *
     * @param reason                       the optional reason marker;
     *                                     may be {@code null}
     * @param reasonDescription            the optional free-form
     *                                     reason description; may be
     *                                     {@code null}
     * @param entryPoint                   the optional entry-point
     *                                     marker; may be {@code null}
     * @param firstMessage                 the optional first-message
     *                                     hint; may be {@code null}
     * @param businessDiscoveryEntryPoint  the optional discovery
     *                                     entry-point marker; may be
     *                                     {@code null}
     * @param businessDiscoveryTimestamp   the optional discovery
     *                                     timestamp in seconds; may
     *                                     be {@code null}
     * @param businessDiscoveryId          the optional discovery id;
     *                                     may be {@code null}
     */
    public record BizOptOut(String reason, String reasonDescription, String entryPoint,
                            String firstMessage, String businessDiscoveryEntryPoint,
                            Long businessDiscoveryTimestamp, String businessDiscoveryId) {
        /**
         * Builds the {@code <biz_opt_out>} stanza for this payload.
         *
         * @return the built {@link Node}; never {@code null}
         */
        @WhatsAppWebExport(moduleName = "WASmaxOutBlocklistsUpdateBlockListRequest",
                exports = "makeUpdateBlockListRequestItemBizOptOut", adaptation = WhatsAppAdaptation.DIRECT)
        public Node toNode() {
            // WASmaxOutBlocklistsUpdateBlockListRequest.makeUpdateBlockListRequestItemBizOptOut:
            // smax("biz_opt_out", { reason, reason_description, entry_point, first_message,
            //                       business_discovery_entry_point, business_discovery_timestamp,
            //                       business_discovery_id })
            // every attribute is wrapped in WASmaxAttrs.OPTIONAL — null arguments collapse to
            // WAWap.DROP_ATTR and the attribute is omitted from the wire entirely. Cobalt
            // mirrors that by guarding each NodeBuilder.attribute(...) call with a null check.
            var builder = new NodeBuilder()
                    .description("biz_opt_out");
            if (reason != null) {
                builder.attribute("reason", reason);
            }
            if (reasonDescription != null) {
                builder.attribute("reason_description", reasonDescription);
            }
            if (entryPoint != null) {
                builder.attribute("entry_point", entryPoint);
            }
            if (firstMessage != null) {
                builder.attribute("first_message", firstMessage);
            }
            if (businessDiscoveryEntryPoint != null) {
                builder.attribute("business_discovery_entry_point", businessDiscoveryEntryPoint);
            }
            if (businessDiscoveryTimestamp != null) {
                // WAWap.INT serialises the integer as a numeric attribute; NodeBuilder.attribute(String, Number)
                // forwards through the same Number coercion path.
                builder.attribute("business_discovery_timestamp", businessDiscoveryTimestamp);
            }
            if (businessDiscoveryId != null) {
                builder.attribute("business_discovery_id", businessDiscoveryId);
            }
            return builder.build();
        }

        /**
         * Returns the optional reason marker.
         *
         * @return an {@link Optional} carrying the reason, or empty
         *         when omitted
         */
        public Optional<String> reasonAsOptional() {
            return Optional.ofNullable(reason);
        }

        /**
         * Returns the optional reason description.
         *
         * @return an {@link Optional} carrying the description, or
         *         empty when omitted
         */
        public Optional<String> reasonDescriptionAsOptional() {
            return Optional.ofNullable(reasonDescription);
        }

        /**
         * Returns the optional entry-point marker.
         *
         * @return an {@link Optional} carrying the entry-point, or
         *         empty when omitted
         */
        public Optional<String> entryPointAsOptional() {
            return Optional.ofNullable(entryPoint);
        }

        /**
         * Returns the optional first-message hint.
         *
         * @return an {@link Optional} carrying the first-message
         *         hint, or empty when omitted
         */
        public Optional<String> firstMessageAsOptional() {
            return Optional.ofNullable(firstMessage);
        }

        /**
         * Returns the optional business-discovery entry-point.
         *
         * @return an {@link Optional} carrying the entry-point, or
         *         empty when omitted
         */
        public Optional<String> businessDiscoveryEntryPointAsOptional() {
            return Optional.ofNullable(businessDiscoveryEntryPoint);
        }

        /**
         * Returns the optional business-discovery timestamp in
         * seconds.
         *
         * @return an {@link Optional} carrying the timestamp, or
         *         empty when omitted
         */
        public Optional<Long> businessDiscoveryTimestampAsOptional() {
            return Optional.ofNullable(businessDiscoveryTimestamp);
        }

        /**
         * Returns the optional business-discovery id.
         *
         * @return an {@link Optional} carrying the id, or empty when
         *         omitted
         */
        public Optional<String> businessDiscoveryIdAsOptional() {
            return Optional.ofNullable(businessDiscoveryId);
        }
    }
}
