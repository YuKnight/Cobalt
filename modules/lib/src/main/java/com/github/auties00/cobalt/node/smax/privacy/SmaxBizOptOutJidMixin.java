package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;

import java.util.Objects;
import java.util.Optional;

/**
 * Projection helper for the {@code BizOptOutJid} arm of the
 * {@code biz_opt_out_ids} disjunction.
 *
 * <p>WA Web ships
 * {@code WASmaxInBlocklistsBizOptOutJidMixin.parseBizOptOutJidMixin}
 * as a tiny mixin that reads a single attribute off the
 * {@code <item>} node:
 * <ul>
 *   <li>a required {@code biz_opt_out_jid} validated against
 *       {@code WAJids.validateUserJid} (must satisfy
 *       {@link Jid#hasUserServer()}).</li>
 * </ul>
 *
 * <p>Cobalt collapses the {@code WAResultOrError} envelope into a
 * plain {@link Optional}: {@link Optional#empty()} signals either the
 * required attribute is missing or it fails to parse as a user JID;
 * a populated result mirrors WA's {@code makeResult({bizOptOutJid})}
 * singleton.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBlocklistsBizOptOutJidMixin")
public final class SmaxBizOptOutJidMixin {
    /**
     * Private constructor. The class is a static-only utility.
     */
    private SmaxBizOptOutJidMixin() {
        throw new AssertionError("SmaxBizOptOutJidMixin cannot be instantiated");
    }

    /**
     * The projected {@code BizOptOutJid} singleton.
     *
     * @param bizOptOutJid the required user JID; never {@code null}
     */
    public record Projection(Jid bizOptOutJid) {
        /**
         * Constructs a projection.
         *
         * @param bizOptOutJid the required user JID; never
         *                     {@code null}
         * @throws NullPointerException if {@code bizOptOutJid} is
         *                              {@code null}
         */
        public Projection {
            Objects.requireNonNull(bizOptOutJid, "bizOptOutJid cannot be null");
        }
    }

    /**
     * Tries to project the {@code BizOptOutJid} arm off an
     * {@code <item>} node.
     *
     * <p>Returns {@link Optional#empty()} when the required
     * {@code biz_opt_out_jid} attribute is missing, or when it does
     * not parse as a user JID (i.e. {@link Jid#hasUserServer()}
     * returns {@code false}).
     *
     * @param item the source {@code <item>} node; never {@code null}
     * @return an {@link Optional} carrying the projected JID, or
     *         empty when the schema does not match
     * @throws NullPointerException if {@code item} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsBizOptOutJidMixin",
            exports = "parseBizOptOutJidMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<Projection> parse(Node item) {
        Objects.requireNonNull(item, "item cannot be null");
        // WASmaxInBlocklistsBizOptOutJidMixin.parseBizOptOutJidMixin:
        // WASmaxParseJid.attrUserJid(item, "biz_opt_out_jid")
        //   -> WASmaxParseUtils.attrValidate(item, "biz_opt_out_jid",
        //                                    WAJids.validateUserJid, "UserJid")
        var parsed = item.getAttributeAsJid("biz_opt_out_jid").orElse(null);
        if (parsed == null || !parsed.hasUserServer()) {
            return Optional.empty();
        }
        return Optional.of(new Projection(parsed));
    }
}
