package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Documented {@code display} enum carried by the
 * {@code <config display/>} attribute.
 *
 * @implNote Mirrors {@code WASmaxInBizCtwaActionEnums.ENUM_INFO_WARNING}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaActionEnums")
public enum SmaxBannerSuggestionBannerDisplay {
    /**
     * Render as a neutral informational banner.
     */
    INFO,
    /**
     * Render as a warning banner.
     */
    WARNING;

    /**
     * Tries to parse a wire-form attribute string.
     *
     * @param value the attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum
     *         constant, or empty
     */
    public static Optional<SmaxBannerSuggestionBannerDisplay> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxBannerSuggestionBannerDisplay.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
