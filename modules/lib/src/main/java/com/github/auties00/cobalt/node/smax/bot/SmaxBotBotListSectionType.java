package com.github.auties00.cobalt.node.smax.bot;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Optional;

/**
 * Section type discriminator. Projects the {@code <section type>}
 * attribute through {@code WASmaxInBotEnums.ENUM_ALL_CATEGORY_FEATURED}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBotEnums")
@WhatsAppWebExport(
        moduleName = "WASmaxInBotEnums",
        exports = "ENUM_ALL_CATEGORY_FEATURED",
        adaptation = WhatsAppAdaptation.ADAPTED
)
public enum SmaxBotBotListSectionType {
    /**
     * The "all bots" section. A flat list aggregating every bot.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBotEnums",
            exports = "ENUM_ALL_CATEGORY_FEATURED",
            adaptation = WhatsAppAdaptation.DIRECT
    )
    ALL("all"),
    /**
     * A category section. Bots grouped by topical category.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBotEnums",
            exports = "ENUM_ALL_CATEGORY_FEATURED",
            adaptation = WhatsAppAdaptation.DIRECT
    )
    CATEGORY("category"),
    /**
     * A featured / curated section.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBotEnums",
            exports = "ENUM_ALL_CATEGORY_FEATURED",
            adaptation = WhatsAppAdaptation.DIRECT
    )
    FEATURED("featured");

    /**
     * The wire-level literal carried by the {@code type} attribute.
     */
    private final String wireValue;

    /**
     * Constructs a new enum constant.
     *
     * @param wireValue the wire-level literal; never {@code null}
     */
    SmaxBotBotListSectionType(String wireValue) {
        this.wireValue = wireValue;
    }

    /**
     * Returns the wire-level literal.
     *
     * @return the literal; never {@code null}
     */
    public String wireValue() {
        return wireValue;
    }

    /**
     * Resolves a {@link SmaxBotBotListSectionType} from the wire-level literal.
     *
     * @param wireValue the wire-level literal
     * @return an {@link Optional} carrying the resolved enum
     *         constant, or empty when the literal is unknown
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBotEnums",
            exports = "ENUM_ALL_CATEGORY_FEATURED",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    public static Optional<SmaxBotBotListSectionType> ofWire(String wireValue) {
        if (wireValue == null) {
            return Optional.empty();
        }
        for (var value : values()) {
            if (value.wireValue.equals(wireValue)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
