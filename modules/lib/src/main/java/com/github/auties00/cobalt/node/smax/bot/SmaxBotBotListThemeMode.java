package com.github.auties00.cobalt.node.smax.bot;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Optional;

/**
 * Theme-mode discriminator (V2 only). Projects the {@code <theme mode>}
 * attribute through {@code WASmaxInBotEnums.ENUM_DARK_LIGHT}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBotEnums")
@WhatsAppWebExport(
        moduleName = "WASmaxInBotEnums",
        exports = "ENUM_DARK_LIGHT",
        adaptation = WhatsAppAdaptation.ADAPTED
)
public enum SmaxBotBotListThemeMode {
    /**
     * The dark-mode colour bundle.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBotEnums",
            exports = "ENUM_DARK_LIGHT",
            adaptation = WhatsAppAdaptation.DIRECT
    )
    DARK("dark"),
    /**
     * The light-mode colour bundle.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBotEnums",
            exports = "ENUM_DARK_LIGHT",
            adaptation = WhatsAppAdaptation.DIRECT
    )
    LIGHT("light");

    /**
     * The wire-level literal carried by the {@code mode} attribute.
     */
    private final String wireValue;

    /**
     * Constructs a new enum constant.
     *
     * @param wireValue the wire-level literal; never {@code null}
     */
    SmaxBotBotListThemeMode(String wireValue) {
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
     * Resolves a {@link SmaxBotBotListThemeMode} from the wire-level literal.
     *
     * @param wireValue the wire-level literal
     * @return an {@link Optional} carrying the resolved enum
     *         constant, or empty when the literal is unknown
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBotEnums",
            exports = "ENUM_DARK_LIGHT",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    public static Optional<SmaxBotBotListThemeMode> ofWire(String wireValue) {
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
