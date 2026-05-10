package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Locale;
import java.util.Optional;

/**
 * Literal-tuple validator for documented boolean-shaped attributes on
 * CTWA (click-to-WhatsApp) banner suggestion stanzas. Accepts the
 * wire literals {@code "false"} and {@code "true"}, mapped onto the
 * named flag constants {@link #FALSE} and {@link #TRUE}.
 *
 * <p>Currently consumed by
 * {@code WASmaxInBizCtwaActionBannerSuggestionRequest} for the
 * {@code <config revoked/>} attribute.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionEnums")
@WhatsAppWebExport(
        moduleName = "WASmaxInBizCtwaActionEnums",
        exports = "ENUM_FALSE_TRUE",
        adaptation = WhatsAppAdaptation.ADAPTED
)
public enum SmaxBannerSuggestionFalseTrueFlag {
    /**
     * Wire literal {@code "false"}. For the {@code revoked}
     * attribute, indicates the banner is still active.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaActionEnums",
            exports = "ENUM_FALSE_TRUE",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    FALSE,
    /**
     * Wire literal {@code "true"}. For the {@code revoked}
     * attribute, indicates the banner has been revoked server-side.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaActionEnums",
            exports = "ENUM_FALSE_TRUE",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    TRUE;

    /**
     * Tries to parse a wire-form attribute string into the matching
     * enum constant. Mirrors the WA Web {@code attrStringEnum}
     * lookup, which is a case-sensitive dictionary match against the
     * lowercase literals.
     *
     * @param value the attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum
     *         constant, or empty when {@code value} is {@code null}
     *         or does not match any documented literal
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaActionEnums",
            exports = "ENUM_FALSE_TRUE",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    public static Optional<SmaxBannerSuggestionFalseTrueFlag> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxBannerSuggestionFalseTrueFlag.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
