package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;

import java.util.Objects;
import java.util.Optional;

/**
 * Projection helper for the {@code BizOptOutBrandID} arm of the
 * {@code biz_opt_out_ids} disjunction.
 *
 * <p>WA Web ships
 * {@code WASmaxInBlocklistsBizOptOutBrandIDMixin.parseBizOptOutBrandIDMixin}
 * as a tiny mixin that reads two attributes off the same {@code <item>}
 * node:
 * <ul>
 *   <li>a required {@code biz_opt_out_brand_id} string;</li>
 *   <li>an optional {@code biz_jid} validated against
 *       {@code WAJids.validateUserJid} (must satisfy
 *       {@link Jid#hasUserServer()}).</li>
 * </ul>
 *
 * <p>Cobalt collapses the {@code WAResultOrError} envelope into a
 * plain {@link Optional}: {@link Optional#empty()} signals either the
 * required attribute is missing or the optional JID is present but
 * fails the user-JID predicate; a populated result mirrors WA's
 * {@code makeResult({bizOptOutBrandId, bizJid})} pair.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsBizOptOutBrandIDMixin")
public final class SmaxBizOptOutBrandIdMixin {
    /**
     * Private constructor. The class is a static-only utility.
     */
    private SmaxBizOptOutBrandIdMixin() {
        throw new AssertionError("SmaxBizOptOutBrandIdMixin cannot be instantiated");
    }

    /**
     * The projected {@code BizOptOutBrandID} pair.
     *
     * @param bizOptOutBrandId the required brand id; never
     *                         {@code null}
     * @param bizJid           the optional user JID; may be
     *                         {@code null}
     */
    public record Projection(String bizOptOutBrandId, Jid bizJid) {
        /**
         * Constructs a projection.
         *
         * @param bizOptOutBrandId the required brand id; never
         *                         {@code null}
         * @param bizJid           the optional user JID; may be
         *                         {@code null}
         * @throws NullPointerException if {@code bizOptOutBrandId} is
         *                              {@code null}
         */
        public Projection {
            Objects.requireNonNull(bizOptOutBrandId, "bizOptOutBrandId cannot be null");
        }

        /**
         * Returns the optional user JID.
         *
         * @return an {@link Optional} carrying the JID, or empty when
         *         omitted
         */
        public Optional<Jid> bizJidAsOptional() {
            return Optional.ofNullable(bizJid);
        }
    }

    /**
     * Tries to project the {@code BizOptOutBrandID} arm off an
     * {@code <item>} node.
     *
     * <p>Returns {@link Optional#empty()} when the required
     * {@code biz_opt_out_brand_id} attribute is missing, or when an
     * {@code biz_jid} attribute is present but does not parse as a
     * user JID (i.e. {@link Jid#hasUserServer()} returns
     * {@code false}).
     *
     * @param item the source {@code <item>} node; never {@code null}
     * @return an {@link Optional} carrying the projected pair, or
     *         empty when the schema does not match
     * @throws NullPointerException if {@code item} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsBizOptOutBrandIDMixin",
            exports = "parseBizOptOutBrandIDMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<Projection> parse(Node item) {
        Objects.requireNonNull(item, "item cannot be null");
        // WASmaxInBlocklistsBizOptOutBrandIDMixin.parseBizOptOutBrandIDMixin:
        // WASmaxParseUtils.attrString(item, "biz_opt_out_brand_id")
        var brandId = item.getAttributeAsString("biz_opt_out_brand_id").orElse(null);
        if (brandId == null) {
            return Optional.empty();
        }
        // WASmaxInBlocklistsBizOptOutBrandIDMixin.parseBizOptOutBrandIDMixin:
        // WASmaxParseUtils.optional(WASmaxParseJid.attrUserJid, item, "biz_jid")
        Jid bizJid = null;
        if (item.hasAttribute("biz_jid")) {
            var parsed = item.getAttributeAsJid("biz_jid").orElse(null);
            // WASmaxParseJid.attrUserJid -> WAJids.validateUserJid
            if (parsed == null || !parsed.hasUserServer()) {
                return Optional.empty();
            }
            bizJid = parsed;
        }
        return Optional.of(new Projection(brandId, bizJid));
    }
}
