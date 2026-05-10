package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import java.util.Locale;
import java.util.Optional;

/**
 * Documented {@code state} enum carried by the
 * {@code <profile_sync>} child of {@code <fb_page/>} and the
 * {@code <catalog>} child of {@code <fb_biz/>}. Three-way toggle.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizLinkingEnums")
public enum SmaxGetLinkedAccountsDisableImportState {
    /**
     * Sync is disabled. Wire form: {@code "disable"}.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingEnums",
            exports = "ENUM_DISABLE_IMPORT", adaptation = WhatsAppAdaptation.DIRECT)
    DISABLE,
    /**
     * Import is in progress / requested. Wire form: {@code "import"}.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingEnums",
            exports = "ENUM_DISABLE_IMPORT", adaptation = WhatsAppAdaptation.DIRECT)
    IMPORT;

    /**
     * Tries to parse a wire-form attribute string.
     *
     * @param value the attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum
     *         constant, or empty
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingEnums",
            exports = "ENUM_DISABLE_IMPORT", adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxGetLinkedAccountsDisableImportState> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxGetLinkedAccountsDisableImportState.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
