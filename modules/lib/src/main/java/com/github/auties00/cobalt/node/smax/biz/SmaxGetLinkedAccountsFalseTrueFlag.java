package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import java.util.Locale;
import java.util.Optional;

/**
 * Documented {@code show_on_profile} content enum surfaced by the
 * {@code WASmaxInBizLinkingHasShowOnProfileMixin}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizLinkingEnums")
public enum SmaxGetLinkedAccountsFalseTrueFlag {
    /**
     * Literal {@code "false"} content value.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingEnums",
            exports = "ENUM_FALSE_TRUE", adaptation = WhatsAppAdaptation.DIRECT)
    FALSE,
    /**
     * Literal {@code "true"} content value.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingEnums",
            exports = "ENUM_FALSE_TRUE", adaptation = WhatsAppAdaptation.DIRECT)
    TRUE;

    /**
     * Tries to parse a wire-form value into a {@link SmaxGetLinkedAccountsFalseTrueFlag}.
     *
     * @param value the attribute or element-content value; may be
     *              {@code null}
     * @return an {@link Optional} carrying the matching enum
     *         constant, or empty
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingEnums",
            exports = "ENUM_FALSE_TRUE", adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxGetLinkedAccountsFalseTrueFlag> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxGetLinkedAccountsFalseTrueFlag.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
