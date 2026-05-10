package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Locale;
import java.util.Optional;

/**
 * Literal-tuple validator for documented boolean-shaped attributes on
 * SMB business-settings stanzas. Accepts the lowercase wire literals
 * {@code "false"} and {@code "true"}, mapped onto the named flag
 * constants {@link #FALSE} and {@link #TRUE}.
 *
 * <p>Currently consumed by UI flows in {@code WAWebSmbDataSharingOptInModalDialog}
 * (which signals SMB data-sharing opt-in / opt-out via
 * {@code ENUM_FALSE_TRUE.true} / {@code ENUM_FALSE_TRUE.false}); no
 * Cobalt parser observes it on the wire today, so this enum is
 * provided for completeness and to mirror the WA literal universe.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizSettingsEnums")
@WhatsAppWebExport(
        moduleName = "WASmaxInBizSettingsEnums",
        exports = "ENUM_FALSE_TRUE",
        adaptation = WhatsAppAdaptation.ADAPTED
)
public enum SmaxBizSettingsFalseTrueFlag {
    /**
     * Wire literal {@code "false"}.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizSettingsEnums",
            exports = "ENUM_FALSE_TRUE",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    FALSE,

    /**
     * Wire literal {@code "true"}.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizSettingsEnums",
            exports = "ENUM_FALSE_TRUE",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    TRUE;

    /**
     * Tries to parse a wire-form attribute string into the matching
     * enum constant. Mirrors the WA Web {@code attrStringEnum} lookup,
     * which is a case-sensitive dictionary match against the lowercase
     * literals.
     *
     * @param value the attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum constant,
     *         or empty when {@code value} is {@code null} or does not
     *         match any documented literal
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizSettingsEnums",
            exports = "ENUM_FALSE_TRUE",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    public static Optional<SmaxBizSettingsFalseTrueFlag> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxBizSettingsFalseTrueFlag.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
