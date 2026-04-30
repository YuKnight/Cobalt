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
 * Section display-type discriminator (V3 only). Projects the
 * {@code <section display_type>} attribute through
 * {@code WASmaxInBotEnums.ENUM_HIDDEN_HSCROLL_HSCROLLICEBREAKERS_HSCROLLLARGE_HSCROLLSMALL_LISTVIEW}.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBotEnums")
public enum SmaxBotBotListSectionDisplayType {
    /**
     * The section is hidden from the directory sheet.
     */
    HIDDEN("hidden"),
    /**
     * The section renders as a horizontal scroller (default).
     */
    HSCROLL("hscroll"),
    /**
     * The section renders as a horizontal scroller carrying
     * "icebreaker" prompt cards.
     */
    HSCROLL_ICEBREAKERS("hscroll_icebreakers"),
    /**
     * The section renders as a horizontal scroller with large bot
     * cards.
     */
    HSCROLL_LARGE("hscroll_large"),
    /**
     * The section renders as a horizontal scroller with small bot
     * cards.
     */
    HSCROLL_SMALL("hscroll_small"),
    /**
     * The section renders as a vertical list.
     */
    LISTVIEW("listview");

    /**
     * The wire-level literal carried by the {@code display_type}
     * attribute.
     */
    private final String wireValue;

    /**
     * Constructs a new enum constant.
     *
     * @param wireValue the wire-level literal; never {@code null}
     */
    SmaxBotBotListSectionDisplayType(String wireValue) {
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
     * Resolves a {@link SmaxBotBotListSectionDisplayType} from the wire-level
     * literal.
     *
     * @param wireValue the wire-level literal
     * @return an {@link Optional} carrying the resolved enum
     *         constant, or empty when the literal is unknown
     */
    public static Optional<SmaxBotBotListSectionDisplayType> ofWire(String wireValue) {
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
