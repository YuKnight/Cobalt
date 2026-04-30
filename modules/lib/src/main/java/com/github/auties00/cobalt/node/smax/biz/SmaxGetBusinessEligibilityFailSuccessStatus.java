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
 * {@code <meta_verified/>} and {@code <genai/>} children. The binary
 * pass / fail eligibility marker.
 *
 * @implNote Mirrors {@code WASmaxInBizMarketingMessageEnums.ENUM_FAIL_SUCCESS}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizMarketingMessageEnums")
public enum SmaxGetBusinessEligibilityFailSuccessStatus {
    /**
     * The feature is not currently available to the calling
     * business.
     */
    FAIL,
    /**
     * The feature is available.
     */
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
