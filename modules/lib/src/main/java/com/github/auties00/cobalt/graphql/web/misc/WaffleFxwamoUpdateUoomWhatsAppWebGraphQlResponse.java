package com.github.auties00.cobalt.graphql.web.misc;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the Waffle FX WAMO update-UOOM mutation built by
 * {@link WaffleFxwamoUpdateUoomWhatsAppWebGraphQlRequest}.
 *
 * <p>Exposes the single boolean scalar field {@code xfb_waffle_fx_wamo_update_uoom}, the outcome of
 * propagating the caller's universal opt-out into the linked Meta advertising surfaces. WhatsApp Web
 * marks the GPC-completed user preference only when this outcome is {@code true}.
 *
 * @see WaffleFxwamoUpdateUoomWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebWaffleFXWAMOUpdateUOOMMutation")
public final class WaffleFxwamoUpdateUoomWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
    /**
     * Holds the update outcome returned under {@code xfb_waffle_fx_wamo_update_uoom}, or {@code null}
     * when the relay omitted the field.
     */
    private final Boolean updated;

    /**
     * Constructs a response wrapping the parsed update outcome.
     *
     * <p>Reserved for the static parser.
     *
     * @param updated the update outcome, or {@code null} when the relay omitted the field
     */
    private WaffleFxwamoUpdateUoomWhatsAppWebGraphQlResponse(Boolean updated) {
        this.updated = updated;
    }

    /**
     * Parses the WhatsApp Web GraphQL response from the unwrapped GraphQL {@code data} object.
     *
     * <p>Reads the scalar root {@code xfb_waffle_fx_wamo_update_uoom}; the returned {@link Optional}
     * is empty when {@code data} is {@code null}.
     *
     * @param data the unwrapped GraphQL {@code data} object returned by
     *             {@link com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlClient#send(WhatsAppWebGraphQlOperation.Request)}
     * @return the parsed response, or empty when {@code data} is {@code null}
     */
    public static Optional<WaffleFxwamoUpdateUoomWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }

        var updated = data.getBoolean("xfb_waffle_fx_wamo_update_uoom");
        return Optional.of(new WaffleFxwamoUpdateUoomWhatsAppWebGraphQlResponse(updated));
    }

    /**
     * Returns whether the universal opt-out update succeeded.
     *
     * @return {@code true} when the relay reported a successful update, {@code false} when it
     *         reported failure or omitted the field
     */
    public boolean updated() {
        return updated != null && updated;
    }
}
