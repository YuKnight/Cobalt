package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Optional;

/**
 * Literal-tuple validator for the SMB-profile discoverability tri-state
 * carried by documented business-settings attributes. Accepts the
 * uppercase wire literals {@code "DISCOVERABLE"} / {@code "HIDDEN"} /
 * {@code "UNDEFINED"} mapped onto the named flag constants
 * {@link #DISCOVERABLE} / {@link #HIDDEN} / {@link #UNDEFINED}.
 *
 * <p>No Cobalt parser observes this enum on the wire today; it is
 * provided for completeness to mirror the WA literal universe and to
 * record provenance for the
 * {@code WASmaxInBizSettingsEnums.ENUM_DISCOVERABLE_HIDDEN_UNDEFINED}
 * export.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizSettingsEnums")
@WhatsAppWebExport(
        moduleName = "WASmaxInBizSettingsEnums",
        exports = "ENUM_DISCOVERABLE_HIDDEN_UNDEFINED",
        adaptation = WhatsAppAdaptation.ADAPTED
)
public enum SmaxBizSettingsDiscoverableHiddenUndefinedFlag {
    /**
     * Wire literal {@code "DISCOVERABLE"}. The SMB profile is
     * publicly discoverable.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizSettingsEnums",
            exports = "ENUM_DISCOVERABLE_HIDDEN_UNDEFINED",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    DISCOVERABLE,

    /**
     * Wire literal {@code "HIDDEN"}. The SMB profile is hidden from
     * directory and search surfaces.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizSettingsEnums",
            exports = "ENUM_DISCOVERABLE_HIDDEN_UNDEFINED",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    HIDDEN,

    /**
     * Wire literal {@code "UNDEFINED"}. The discoverability has not
     * been set by the user; the client surfaces the relevant
     * onboarding dialog whenever this value is observed.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizSettingsEnums",
            exports = "ENUM_DISCOVERABLE_HIDDEN_UNDEFINED",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    UNDEFINED;

    /**
     * Tries to parse a wire-form attribute string into the matching
     * enum constant. Mirrors the WA Web {@code attrStringEnum} lookup,
     * which is a case-sensitive dictionary match against the documented
     * literals.
     *
     * @param value the attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum constant,
     *         or empty when {@code value} is {@code null} or does not
     *         match any documented literal
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizSettingsEnums",
            exports = "ENUM_DISCOVERABLE_HIDDEN_UNDEFINED",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    public static Optional<SmaxBizSettingsDiscoverableHiddenUndefinedFlag> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxBizSettingsDiscoverableHiddenUndefinedFlag.valueOf(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
