package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;

import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of the two {@code biz_opt_out_ids} disjunction
 * variants produced by
 * {@code WASmaxInBlocklistsBizOptOutIds.parseBizOptOutIds}.
 *
 * <p>WA Web encodes the result as a tagged union with a {@code name}
 * discriminator of either {@code "BizOptOutBrandID"} or
 * {@code "BizOptOutJid"}, and a {@code value} payload projected by the
 * matching arm mixin. Cobalt encodes that union as a sealed
 * interface; the discriminator is recovered by structural pattern
 * matching on the variant type, mirroring the
 * {@code bizOptOutIds.name === "BizOptOutBrandID"} /
 * {@code bizOptOutIds.name === "BizOptOutJid"} branches in
 * {@code WAWebGetOptOutList.getOptOutList} and
 * {@code WAWebOptOutBizAction.optOutContact}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsBizOptOutIds")
public sealed interface BizOptOutId permits BizOptOutId.BrandId, BizOptOutId.UserJid {
    /**
     * The {@code BizOptOutBrandID} arm of the disjunction. Carries
     * the required brand id and an optional business JID.
     *
     * @param bizOptOutBrandId the required brand id; never
     *                         {@code null}
     * @param bizJid           the optional business user JID; may be
     *                         {@code null}
     */
    record BrandId(String bizOptOutBrandId, Jid bizJid) implements BizOptOutId {
        /**
         * Constructs a {@code BizOptOutBrandID} arm.
         *
         * @param bizOptOutBrandId the required brand id; never
         *                         {@code null}
         * @param bizJid           the optional business user JID; may
         *                         be {@code null}
         * @throws NullPointerException if {@code bizOptOutBrandId} is
         *                              {@code null}
         */
        public BrandId {
            Objects.requireNonNull(bizOptOutBrandId, "bizOptOutBrandId cannot be null");
        }

        /**
         * Returns the optional business JID.
         *
         * @return an {@link Optional} carrying the JID, or empty when
         *         omitted
         */
        public Optional<Jid> bizJidAsOptional() {
            return Optional.ofNullable(bizJid);
        }
    }

    /**
     * The {@code BizOptOutJid} arm of the disjunction. Carries the
     * required business user JID.
     *
     * @param bizOptOutJid the required business user JID; never
     *                     {@code null}
     */
    record UserJid(Jid bizOptOutJid) implements BizOptOutId {
        /**
         * Constructs a {@code BizOptOutJid} arm.
         *
         * @param bizOptOutJid the required business user JID; never
         *                     {@code null}
         * @throws NullPointerException if {@code bizOptOutJid} is
         *                              {@code null}
         */
        public UserJid {
            Objects.requireNonNull(bizOptOutJid, "bizOptOutJid cannot be null");
        }
    }

    /**
     * Tries to project a {@link BizOptOutId} variant off an
     * {@code <item>} node, mirroring
     * {@code WASmaxInBlocklistsBizOptOutIds.parseBizOptOutIds}.
     *
     * <p>The {@code BrandId} arm is tried first
     * ({@link SmaxBizOptOutBrandIdMixin#parse(Node)}); on miss the
     * {@code UserJid} arm is tried
     * ({@link SmaxBizOptOutJidMixin#parse(Node)}). Returns
     * {@link Optional#empty()} when neither arm matches.
     *
     * @param item the source {@code <item>} node; never {@code null}
     * @return an {@link Optional} carrying the projected variant, or
     *         empty when the schema does not match
     * @throws NullPointerException if {@code item} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsBizOptOutIds",
            exports = "parseBizOptOutIds", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<BizOptOutId> parse(Node item) {
        Objects.requireNonNull(item, "item cannot be null");
        // WASmaxInBlocklistsBizOptOutIds.parseBizOptOutIds:
        // var t = WASmaxInBlocklistsBizOptOutBrandIDMixin.parseBizOptOutBrandIDMixin(item);
        // if (t.success) return makeResult({name:"BizOptOutBrandID", value:t.value});
        var brand = SmaxBizOptOutBrandIdMixin.parse(item);
        if (brand.isPresent()) {
            var p = brand.get();
            return Optional.of(new BrandId(p.bizOptOutBrandId(), p.bizJid()));
        }
        // WASmaxInBlocklistsBizOptOutIds.parseBizOptOutIds:
        // var n = WASmaxInBlocklistsBizOptOutJidMixin.parseBizOptOutJidMixin(item);
        // if (n.success) return makeResult({name:"BizOptOutJid", value:n.value});
        var jid = SmaxBizOptOutJidMixin.parse(item);
        if (jid.isPresent()) {
            return Optional.of(new UserJid(jid.get().bizOptOutJid()));
        }
        // WASmaxInBlocklistsBizOptOutIds.parseBizOptOutIds:
        // return WASmaxParseUtils.errorMixinDisjunction(item,
        //     ["BizOptOutBrandID", "BizOptOutJid"], [t, n]);
        return Optional.empty();
    }
}
