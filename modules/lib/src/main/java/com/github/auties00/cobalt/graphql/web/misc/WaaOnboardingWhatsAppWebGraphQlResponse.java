package com.github.auties00.cobalt.graphql.web.misc;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.business.waa.WhatsAppAdsAdAccount;
import com.github.auties00.cobalt.model.business.waa.WhatsAppAdsAdAccountBuilder;

import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the WhatsApp Ads onboarding mutation built by
 * {@link WaaOnboardingWhatsAppWebGraphQlRequest} into a {@link WhatsAppAdsAdAccount}.
 *
 * <p>Reads the linked root {@code create_or_onboard_wa_ad_account} and projects its
 * {@code ad_account_id} (which doubles as the success signal: the WhatsApp client treats a missing
 * id as an onboarding failure) and {@code status} onto the Cobalt domain model.
 *
 * @see WaaOnboardingWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebWAAOnboardingMutation")
public final class WaaOnboardingWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
    /**
     * Holds the parsed ad account.
     */
    private final WhatsAppAdsAdAccount adAccount;

    /**
     * Constructs a response wrapping the parsed ad account.
     *
     * <p>Reserved for the static parser.
     *
     * @param adAccount the parsed ad account, or {@code null} when the relay omitted the field
     */
    private WaaOnboardingWhatsAppWebGraphQlResponse(WhatsAppAdsAdAccount adAccount) {
        this.adAccount = adAccount;
    }

    /**
     * Parses the WhatsApp Web GraphQL response from the unwrapped GraphQL {@code data} object.
     *
     * <p>Reads the linked root {@code create_or_onboard_wa_ad_account} and projects it onto a
     * {@link WhatsAppAdsAdAccount}; the returned {@link Optional} is empty when {@code data} or the
     * ad-account object is missing.
     *
     * @param data the unwrapped GraphQL {@code data} object returned by
     *             {@link com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlClient#send(WhatsAppWebGraphQlOperation.Request)}
     * @return the parsed response, or empty when {@code data} or the ad-account object is missing
     */
    public static Optional<WaaOnboardingWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }

        var node = data.getJSONObject("create_or_onboard_wa_ad_account");
        if (node == null) {
            return Optional.empty();
        }

        var adAccount = new WhatsAppAdsAdAccountBuilder()
                .accountId(node.getString("ad_account_id"))
                .status(node.getString("status"))
                .build();
        return Optional.of(new WaaOnboardingWhatsAppWebGraphQlResponse(adAccount));
    }

    /**
     * Returns the parsed ad account.
     *
     * @return the parsed {@link WhatsAppAdsAdAccount}, never {@code null}
     */
    public WhatsAppAdsAdAccount adAccount() {
        return adAccount;
    }
}
