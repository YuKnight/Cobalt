package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Documented {@code status} enum carried by the
 * {@code <marketing_messages/>} child — the four-way status surface.
 *
 * @implNote Mirrors {@code WASmaxInBizMarketingMessageEnums.ENUM_FAIL_PAUSED_SUCCESS_WARNING}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageEnums")
public enum SmaxGetBusinessEligibilityMarketingMessagesStatus {
    /**
     * The feature is not available.
     */
    FAIL,
    /**
     * The feature is temporarily paused.
     */
    PAUSED,
    /**
     * The feature is available with no caveats.
     */
    SUCCESS,
    /**
     * The feature is available but the relay is surfacing a
     * non-fatal warning (e.g. quota close to exhaustion).
     */
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
