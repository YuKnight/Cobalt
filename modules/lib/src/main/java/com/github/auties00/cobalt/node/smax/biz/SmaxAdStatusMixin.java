package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code <ad_status/>} child projection. Bundles the two
 * documented boolean-flag attributes a relay surfaces on a linked
 * Facebook-page or WhatsApp-ad-identity entry.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizLinkingAdStatusMixin")
public final class SmaxAdStatusMixin {
    /**
     * The mandatory {@code has_created_ad} flag.
     */
    private final SmaxGetLinkedAccountsFalseTrueFlag hasCreatedAd;

    /**
     * The mandatory {@code has_active_ctwa_ad} flag.
     */
    private final SmaxGetLinkedAccountsFalseTrueFlag hasActiveCtwaAd;

    /**
     * Constructs a new mixin projection.
     *
     * @param hasCreatedAd    the {@code has_created_ad} flag; never
     *                        {@code null}
     * @param hasActiveCtwaAd the {@code has_active_ctwa_ad} flag;
     *                        never {@code null}
     * @throws NullPointerException if either argument is {@code null}
     */
    public SmaxAdStatusMixin(SmaxGetLinkedAccountsFalseTrueFlag hasCreatedAd,
                             SmaxGetLinkedAccountsFalseTrueFlag hasActiveCtwaAd) {
        this.hasCreatedAd = Objects.requireNonNull(hasCreatedAd, "hasCreatedAd cannot be null");
        this.hasActiveCtwaAd = Objects.requireNonNull(hasActiveCtwaAd, "hasActiveCtwaAd cannot be null");
    }

    /**
     * Returns the {@code has_created_ad} flag.
     *
     * @return the flag; never {@code null}
     */
    public SmaxGetLinkedAccountsFalseTrueFlag hasCreatedAd() {
        return hasCreatedAd;
    }

    /**
     * Returns the {@code has_active_ctwa_ad} flag.
     *
     * @return the flag; never {@code null}
     */
    public SmaxGetLinkedAccountsFalseTrueFlag hasActiveCtwaAd() {
        return hasActiveCtwaAd;
    }

    /**
     * Tries to parse the projection from the given parent node by
     * locating its mandatory {@code <ad_status/>} child.
     *
     * @param node the parent node hosting the {@code <ad_status/>}
     *             child; never {@code null}
     * @return an {@link Optional} carrying the projection, or empty
     *         when the parent has no {@code <ad_status/>} child or
     *         when either of the two mandatory enum attributes is
     *         missing or unrecognised
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingAdStatusMixin",
            exports = "parseAdStatusMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxAdStatusMixin> of(Node node) {
        Objects.requireNonNull(node, "node cannot be null");
        // WASmaxInBizLinkingAdStatusMixin.parseAdStatusMixin:
        // flattenedChildWithTag(node, "ad_status") — the child is mandatory.
        var adStatusNode = node.getChild("ad_status").orElse(null);
        if (adStatusNode == null) {
            return Optional.empty();
        }
        // WASmaxInBizLinkingAdStatusMixin.parseAdStatusMixin:
        // attrStringEnum(child, "has_created_ad", ENUM_FALSE_TRUE).
        var hasCreatedAdStr = adStatusNode.getAttributeAsString("has_created_ad").orElse(null);
        var hasCreatedAd = SmaxGetLinkedAccountsFalseTrueFlag.of(hasCreatedAdStr).orElse(null);
        if (hasCreatedAd == null) {
            return Optional.empty();
        }
        // WASmaxInBizLinkingAdStatusMixin.parseAdStatusMixin:
        // attrStringEnum(child, "has_active_ctwa_ad", ENUM_FALSE_TRUE).
        var hasActiveCtwaAdStr = adStatusNode.getAttributeAsString("has_active_ctwa_ad").orElse(null);
        var hasActiveCtwaAd = SmaxGetLinkedAccountsFalseTrueFlag.of(hasActiveCtwaAdStr).orElse(null);
        if (hasActiveCtwaAd == null) {
            return Optional.empty();
        }
        return Optional.of(new SmaxAdStatusMixin(hasCreatedAd, hasActiveCtwaAd));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxAdStatusMixin) obj;
        return this.hasCreatedAd == that.hasCreatedAd
                && this.hasActiveCtwaAd == that.hasActiveCtwaAd;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hasCreatedAd, hasActiveCtwaAd);
    }

    @Override
    public String toString() {
        return "SmaxAdStatusMixin[hasCreatedAd=" + hasCreatedAd
                + ", hasActiveCtwaAd=" + hasActiveCtwaAd + ']';
    }
}
