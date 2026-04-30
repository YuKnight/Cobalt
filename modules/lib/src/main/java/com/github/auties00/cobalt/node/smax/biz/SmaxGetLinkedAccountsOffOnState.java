package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Documented {@code state} enum carried by the
 * {@code <whatsapp_as_page_button>} grandchild of
 * {@code <fb_page/>}. Opt-in toggle for the
 * "WhatsApp as page button" overlay.
 *
 * @implNote Mirrors {@code WASmaxInBizLinkingEnums.ENUM_OFF_ON}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizLinkingEnums")
public enum SmaxGetLinkedAccountsOffOnState {
    /**
     * The toggle is off.
     */
    OFF,
    /**
     * The toggle is on.
     */
    ON;

    /**
     * Tries to parse a wire-form attribute string.
     *
     * @param value the attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum
     *         constant, or empty
     */
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
