package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Optional;

/**
 * Literal-tuple validator for the documented
 * {@code <Result><status>...</status></Result>} content carried by
 * the {@link SmaxSendAccountRecoveryNonceResponse.Success} reply.
 * Accepts the wire literals {@code "Fail"} and {@code "Success"},
 * mapped onto the named constants {@link #FAIL} and {@link #SUCCESS}.
 *
 * <p>Currently consumed by
 * {@code WASmaxInBizCtwaAdAccountSendAccountRecoveryNonceResponseMixin.parseSendAccountRecoveryNonceResponseMixin}
 * via {@code WASmaxParseUtils.contentStringEnum}, which rejects any
 * value outside this set.
 */
@WhatsAppWebModule(moduleName = "WASmaxInBizCtwaAdAccountEnums")
@WhatsAppWebExport(
        moduleName = "WASmaxInBizCtwaAdAccountEnums",
        exports = "ENUM_FAIL_SUCCESS",
        adaptation = WhatsAppAdaptation.ADAPTED
)
public enum SmaxSendAccountRecoveryNonceStatus {
    /**
     * Wire literal {@code "Fail"}. The relay tried but failed to
     * dispatch the recovery email.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaAdAccountEnums",
            exports = "ENUM_FAIL_SUCCESS",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    FAIL("Fail"),
    /**
     * Wire literal {@code "Success"}. The relay dispatched the
     * recovery email.
     */
    @WhatsAppWebExport(
            moduleName = "WASmaxInBizCtwaAdAccountEnums",
            exports = "ENUM_FAIL_SUCCESS",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    SUCCESS("Success");

    /**
     * The exact wire-form literal carried by the {@code <status>}
     * element content.
     */
    private final String wireValue;

    /**
     * Constructs a constant for the supplied wire-form literal.
     *
     * @param wireValue the exact wire-form literal; never
     *                  {@code null}
     */
    SmaxSendAccountRecoveryNonceStatus(String wireValue) {
        this.wireValue = wireValue;
    }

    /**
     * Returns the exact wire-form literal for this constant.
     *
     * @return the wire-form literal (e.g. {@code "Fail"}); never
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
            exports = "ENUM_FAIL_SUCCESS",
            adaptation = WhatsAppAdaptation.ADAPTED
    )
    public static Optional<SmaxSendAccountRecoveryNonceStatus> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return switch (value) {
            case "Fail" -> Optional.of(FAIL);
            case "Success" -> Optional.of(SUCCESS);
            default -> Optional.empty();
        };
    }
}
