package com.github.auties00.cobalt.node.smax.bot;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Theme-mode discriminator (V2 only) — projects the {@code <theme mode>}
 * attribute through {@code WASmaxInBotEnums.ENUM_DARK_LIGHT}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBotEnums")
public enum SmaxBotBotListThemeMode {
    /**
     * The dark-mode colour bundle.
     */
    DARK("dark"),
    /**
     * The light-mode colour bundle.
     */
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
