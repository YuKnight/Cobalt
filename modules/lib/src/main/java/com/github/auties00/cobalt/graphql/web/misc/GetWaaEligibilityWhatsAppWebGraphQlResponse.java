package com.github.auties00.cobalt.graphql.web.misc;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.business.waa.WhatsAppAdsEligibility;
import com.github.auties00.cobalt.model.business.waa.WhatsAppAdsEligibilityBuilder;

import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the WhatsApp Ads eligibility query built by
 * {@link GetWaaEligibilityWhatsAppWebGraphQlRequest} into a {@link WhatsAppAdsEligibility}.
 *
 * <p>Reads the linked root {@code eval_wa_ad_account_eligibility_rules} and projects its single
 * {@code eligibility_result} scalar onto the Cobalt domain model: the per-flow eligibility verdict
 * the WhatsApp client uses to gate entry into the click-to-WhatsApp advertising flow.
 *
 * @see GetWaaEligibilityWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebGetWAAEligibilityQuery")
public final class GetWaaEligibilityWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
    /**
     * Holds the parsed eligibility verdict.
     */
    private final WhatsAppAdsEligibility eligibility;

    /**
     * Constructs a response wrapping the parsed eligibility verdict.
     *
     * <p>Reserved for the static parser.
     *
     * @param eligibility the parsed eligibility verdict, or {@code null} when the relay omitted the
     *                    field
     */
    private GetWaaEligibilityWhatsAppWebGraphQlResponse(WhatsAppAdsEligibility eligibility) {
        this.eligibility = eligibility;
    }

    /**
     * Parses the WhatsApp Web GraphQL response from the unwrapped GraphQL {@code data} object.
     *
     * <p>Reads the linked root {@code eval_wa_ad_account_eligibility_rules} and projects it onto a
     * {@link WhatsAppAdsEligibility}; the returned {@link Optional} is empty when {@code data} or
     * the eligibility object is missing.
     *
     * @param data the unwrapped GraphQL {@code data} object returned by
     *             {@link com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlClient#send(WhatsAppWebGraphQlOperation.Request)}
     * @return the parsed response, or empty when {@code data} or the eligibility object is missing
     */
    public static Optional<GetWaaEligibilityWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }

        var node = data.getJSONObject("eval_wa_ad_account_eligibility_rules");
        if (node == null) {
            return Optional.empty();
        }

        var eligibility = new WhatsAppAdsEligibilityBuilder()
                .verdict(node.getString("eligibility_result"))
                .build();
        return Optional.of(new GetWaaEligibilityWhatsAppWebGraphQlResponse(eligibility));
    }

    /**
     * Returns the parsed eligibility verdict.
     *
     * @return the parsed {@link WhatsAppAdsEligibility}, never {@code null}
     */
    public WhatsAppAdsEligibility eligibility() {
        return eligibility;
    }
}
