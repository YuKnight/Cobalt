package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Optional;

/**
 * Literal-tuple validator for the documented {@code <token_type>}
 * content carried by the
 * {@link SmaxGetAccessTokenAndSessionCookiesResponse.Success} reply.
 * Accepts the wire literals {@code "Strong"} and {@code "Weak"},
 * mapped onto the named constants {@link #STRONG} and {@link #WEAK}.
 *
 * <p>Currently consumed by
 * {@code WASmaxInBizCtwaAdAccountGetAccessTokenAndSessionCookiesResponseSuccess.parseGetAccessTokenAndSessionCookiesResponseSuccessTokenType}
 * via {@code WASmaxParseUtils.contentStringEnum}, which rejects any
 * value outside this set.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountEnums")
@WhatsAppWebExport(
        moduleName = "WASmaxInBizCtwaAdAccountEnums",
        exports = "ENUM_STRONG_WEAK",
        adaptation = WhatsAppAdaptation.ADAPTED
)
public enum SmaxGetAccessTokenAndSessionCookiesTokenType {
    /**
     * Wire literal {@code "Strong"}. Indicates a long-lived strong
     * Facebook Graph access token.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaAdAccountEnums",
            exports = "ENUM_STRONG_WEAK",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    STRONG("Strong"),
    /**
     * Wire literal {@code "Weak"}. Indicates a short-lived weak
     * Facebook Graph access token.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaAdAccountEnums",
            exports = "ENUM_STRONG_WEAK",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    WEAK("Weak");

    /**
     * The exact wire-form literal carried by the {@code <token_type>}
     * element content.
     */
    private final String wireValue;

    /**
     * Constructs a constant for the supplied wire-form literal.
     *
     * @param wireValue the exact wire-form literal; never
     *                  {@code null}
     */
    SmaxGetAccessTokenAndSessionCookiesTokenType(String wireValue) {
        this.wireValue = wireValue;
    }

    /**
     * Returns the exact wire-form literal for this constant.
     *
     * @return the wire-form literal (e.g. {@code "Strong"}); never
     *         {@code null}
     */
    public String wireValue() {
        return wireValue;
    }

    /**
     * Tries to parse the wire-form content string into the matching
     * enum constant. The lookup is case-sensitive, mirroring the WA
     * Web {@code contentStringEnum} dictionary match against the
     * documented literals.
     *
     * @param value the content string; may be {@code null}
     * @return an {@link Optional} carrying the matching enum
     *         constant, or empty when {@code value} is {@code null}
     *         or does not match any documented literal
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaAdAccountEnums",
            exports = "ENUM_STRONG_WEAK",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    public static Optional<SmaxGetAccessTokenAndSessionCookiesTokenType> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return switch (value) {
            case "Strong" -> Optional.of(STRONG);
            case "Weak" -> Optional.of(WEAK);
            default -> Optional.empty();
        };
    }
}
