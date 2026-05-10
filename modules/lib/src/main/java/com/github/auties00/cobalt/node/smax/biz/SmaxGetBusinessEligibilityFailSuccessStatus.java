package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import java.util.Locale;
import java.util.Optional;

/**
 * Documented {@code status} enum carried by the
 * {@code <meta_verified/>} and {@code <genai/>} children. The binary
 * pass / fail eligibility marker.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageEnums")
public enum SmaxGetBusinessEligibilityFailSuccessStatus {
    /**
     * The feature is not currently available to the calling
     * business.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageEnums",
            exports = "ENUM_FAIL_SUCCESS",
            adaptation = WhatsAppAdaptation.DIRECT)
    FAIL,
    /**
     * The feature is available.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageEnums",
            exports = "ENUM_FAIL_SUCCESS",
            adaptation = WhatsAppAdaptation.DIRECT)
    SUCCESS;

    /**
     * Tries to parse a wire-form attribute string into a
     * {@link SmaxGetBusinessEligibilityFailSuccessStatus} enum value.
     *
     * @param value the attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum
     *         constant, or empty when the value is {@code null} or
     *         does not match a documented literal
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageEnums",
            exports = "ENUM_FAIL_SUCCESS",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxGetBusinessEligibilityFailSuccessStatus> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxGetBusinessEligibilityFailSuccessStatus.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
