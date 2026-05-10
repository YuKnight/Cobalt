package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Locale;
import java.util.Optional;

/**
 * Literal-tuple validator for the documented {@code display} attribute
 * of the CTWA (click-to-WhatsApp) banner suggestion {@code <config/>}
 * stanza. Accepts the wire literals {@code "info"} and {@code "warning"}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionEnums")
@WhatsAppWebExport(
        moduleName = "WASmaxInBizCtwaActionEnums",
        exports = "ENUM_INFO_WARNING",
        adaptation = WhatsAppAdaptation.ADAPTED
)
public enum SmaxBannerSuggestionBannerDisplay {
    /**
     * Renders as a neutral informational banner.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaActionEnums",
            exports = "ENUM_INFO_WARNING",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    INFO,
    /**
     * Renders as a warning banner.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaActionEnums",
            exports = "ENUM_INFO_WARNING",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    WARNING;

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
            exports = "ENUM_INFO_WARNING",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    public static Optional<SmaxBannerSuggestionBannerDisplay> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxBannerSuggestionBannerDisplay.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
