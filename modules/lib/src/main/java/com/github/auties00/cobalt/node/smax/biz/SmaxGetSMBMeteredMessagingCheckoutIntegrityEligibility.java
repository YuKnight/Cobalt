package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Documented {@code is_eligible} enum carried by the
 * {@code <integrity/>} child of the success response. Boolean
 * surfaced as the literal {@code "true"}/{@code "false"} pair.
 */
@WhatsAppWebModule(moduleName = "WASmaxInSmbMeteredMessagingAccountEnums")
public enum SmaxGetSMBMeteredMessagingCheckoutIntegrityEligibility {
    /**
     * Literal {@code "false"}. Campaign held back by integrity
     * review.
     */
    FALSE,
    /**
     * Literal {@code "true"}. Campaign cleared.
     */
    TRUE;

    /**
     * Tries to parse a wire-form attribute string.
     *
     * @param value the attribute value; may be {@code null}
     * @return an {@link Optional} carrying the matching enum
     *         constant, or empty
     */
    public static Optional<SmaxGetSMBMeteredMessagingCheckoutIntegrityEligibility> of(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(SmaxGetSMBMeteredMessagingCheckoutIntegrityEligibility.valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
