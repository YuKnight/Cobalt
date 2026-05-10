package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import java.util.Locale;
import java.util.Optional;

/**
 * Documented {@code should_show_privacy_interstitial_to_new_users}
 * enum carried by the {@code <meta_verified/>} child. Boolean
 * surfaced as the literal {@code "true"}/{@code "false"} pair.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageEnums")
public enum SmaxGetBusinessEligibilityFalseTrueFlag {
    /**
     * Literal {@code "false"} attribute value.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageEnums",
            exports = "ENUM_FALSE_TRUE",
            adaptation = WhatsAppAdaptation.ADAPTED)
    FALSE,
    /**
     * Literal {@code "true"} attribute value.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageEnums",
            exports = "ENUM_FALSE_TRUE",
            adaptation = WhatsAppAdaptation.ADAPTED)
    TRUE;

    /**
     * Tries to parse a wire-form attribute string into a
     * {@link SmaxGetBusinessEligibilityFalseTrueFlag} enum value.
     *
     * @param value the attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum
     *         constant, or empty when the value is {@code null} or
     *         does not match a documented literal
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageEnums",
            exports = "ENUM_FALSE_TRUE",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxGetBusinessEligibilityFalseTrueFlag> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxGetBusinessEligibilityFalseTrueFlag.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
