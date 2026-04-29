package com.github.auties00.cobalt.node.smax.psa;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * The two documented values of the {@code <blocking status>} attribute
 * — the WA Web parser asserts {@code attrStringEnum(ENUM_BLOCKED_UNBLOCKED)}
 * so any other literal results in a parse failure.
 */
@WhatsAppWebModule(moduleName = "WASmaxInPsaEnums")
public enum SmaxPsaChatBlockGetBlockingStatus {
    /**
     * The non-contact PSA broadcast channel is currently muted at the
     * server side.
     */
    BLOCKED("blocked"),
    /**
     * The non-contact PSA broadcast channel is currently active.
     */
    UNBLOCKED("unblocked");

    /**
     * The wire-level literal carried by the {@code status} attribute.
     */
    private final String wireValue;

    /**
     * Constructs a new enum constant.
     *
     * @param wireValue the wire-level literal; never {@code null}
     */
    SmaxPsaChatBlockGetBlockingStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    /**
     * Returns the wire-level literal.
     *
     * @return the wire-level literal; never {@code null}
     */
    public String wireValue() {
        return wireValue;
    }

    /**
     * Resolves a {@link SmaxPsaChatBlockGetBlockingStatus} from the wire-level literal.
     *
     * @param wireValue the wire-level literal; never {@code null}
     * @return an {@link Optional} carrying the resolved enum constant,
     *         or empty when the literal is not one of the documented
     *         values
     */
    public static Optional<SmaxPsaChatBlockGetBlockingStatus> ofWire(String wireValue) {
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
