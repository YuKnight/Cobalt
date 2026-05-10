package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import java.util.Locale;
import java.util.Optional;

/**
 * Documented {@code state} enum carried by the
 * {@code <whatsapp_as_page_button>} grandchild of
 * {@code <fb_page/>}. Opt-in toggle for the
 * "WhatsApp as page button" overlay.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizLinkingEnums")
public enum SmaxGetLinkedAccountsOffOnState {
    /**
     * The toggle is off. Wire form: {@code "off"}.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingEnums",
            exports = "ENUM_OFF_ON", adaptation = WhatsAppAdaptation.DIRECT)
    OFF,
    /**
     * The toggle is on. Wire form: {@code "on"}.
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingEnums",
            exports = "ENUM_OFF_ON", adaptation = WhatsAppAdaptation.DIRECT)
    ON;

    /**
     * Tries to parse a wire-form attribute string.
     *
     * @param value the attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum
     *         constant, or empty
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBizLinkingEnums",
            exports = "ENUM_OFF_ON", adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<SmaxGetLinkedAccountsOffOnState> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxGetLinkedAccountsOffOnState.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
