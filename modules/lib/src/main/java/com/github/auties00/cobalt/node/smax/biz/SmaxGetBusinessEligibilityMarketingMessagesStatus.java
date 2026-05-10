package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import java.util.Locale;
import java.util.Optional;

/**
 * Documented {@code status} enum carried by the
 * {@code <marketing_messages/>} child. The four-way status surface.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageEnums")
public enum SmaxGetBusinessEligibilityMarketingMessagesStatus {
    /**
     * The feature is not available.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageEnums",
            exports = "ENUM_FAIL_PAUSED_SUCCESS_WARNING",
            adaptation = WhatsAppAdaptation.DIRECT)
    FAIL,
    /**
     * The feature is temporarily paused.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageEnums",
            exports = "ENUM_FAIL_PAUSED_SUCCESS_WARNING",
            adaptation = WhatsAppAdaptation.DIRECT)
    PAUSED,
    /**
     * The feature is available with no caveats.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageEnums",
            exports = "ENUM_FAIL_PAUSED_SUCCESS_WARNING",
            adaptation = WhatsAppAdaptation.DIRECT)
    SUCCESS,
    /**
     * The feature is available but the relay is surfacing a
     * non-fatal warning (e.g. Quota close to exhaustion).
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageEnums",
            exports = "ENUM_FAIL_PAUSED_SUCCESS_WARNING",
            adaptation = WhatsAppAdaptation.DIRECT)
    WARNING;

    /**
     * Tries to parse a wire-form attribute string into a
     * {@link SmaxGetBusinessEligibilityMarketingMessagesStatus} enum value.
     *
     * @param value the attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum
     *         constant, or empty when the value does not match a
     *         documented literal
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizMarketingMessageEnums",
            exports = "ENUM_FAIL_PAUSED_SUCCESS_WARNING",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxGetBusinessEligibilityMarketingMessagesStatus> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxGetBusinessEligibilityMarketingMessagesStatus.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
