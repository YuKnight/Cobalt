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
 * Documented {@code should_show_privacy_interstitial_to_new_users}
 * enum carried by the {@code <meta_verified/>} child — boolean
 * surfaced as the literal {@code "true"}/{@code "false"} pair.
 *
 * @implNote Mirrors {@code WASmaxInBizMarketingMessageEnums.ENUM_FALSE_TRUE}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageEnums")
public enum SmaxGetBusinessEligibilityFalseTrueFlag {
    /**
     * Literal {@code "false"} attribute value.
     */
    FALSE,
    /**
     * Literal {@code "true"} attribute value.
     */
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
