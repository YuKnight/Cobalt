package com.github.auties00.cobalt.graphql.web.misc;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.business.marketing.BusinessMarketingCampaign;
import com.github.auties00.cobalt.model.business.marketing.BusinessMarketingCampaignBuilder;

import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the create-marketing-campaign mutation built by
 * {@link CreateMarketingCampaignActionWhatsAppWebGraphQlRequest} into a {@link BusinessMarketingCampaign}.
 *
 * <p>Reads the linked {@code whatsapp_marketing_messages_create} field and projects the created
 * Click-to-WhatsApp marketing-messages campaign onto the Cobalt domain model: the ad-campaign group,
 * ad-campaign, ad-group, ad, and ad-creative ids, plus the campaign name, status, lifetime budget, and
 * start time.
 *
 * @see CreateMarketingCampaignActionWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebCreateMarketingCampaignActionMutation")
public final class CreateMarketingCampaignActionWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
    /**
     * Holds the parsed campaign.
     */
    private final BusinessMarketingCampaign campaign;

    /**
     * Constructs a response wrapping the parsed campaign.
     *
     * <p>Reserved for the static parser.
     *
     * @param campaign the parsed campaign, or {@code null} when the relay omitted the field
     */
    private CreateMarketingCampaignActionWhatsAppWebGraphQlResponse(BusinessMarketingCampaign campaign) {
        this.campaign = campaign;
    }

    /**
     * Parses the WhatsApp Web GraphQL response from the unwrapped GraphQL {@code data} object.
     *
     * <p>Reads the linked root {@code whatsapp_marketing_messages_create} and projects it onto a
     * {@link BusinessMarketingCampaign}; the returned {@link Optional} is empty when {@code data} or the
     * campaign object is missing.
     *
     * @param data the unwrapped GraphQL {@code data} object returned by
     *             {@link com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlClient#send(WhatsAppWebGraphQlOperation.Request)}
     * @return the parsed response, or empty when {@code data} or the campaign object is missing
     */
    public static Optional<CreateMarketingCampaignActionWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }

        var node = data.getJSONObject("whatsapp_marketing_messages_create");
        if (node == null) {
            return Optional.empty();
        }

        var campaign = new BusinessMarketingCampaignBuilder()
                .adCampaignGroupId(node.getString("ad_campaign_group_id"))
                .adCampaignId(node.getString("ad_campaign_id"))
                .adGroupId(node.getString("ad_group_id"))
                .adId(node.getString("ad_id"))
                .adCreativeId(node.getString("ad_creative_id"))
                .name(node.getString("campaign_name"))
                .status(node.getString("status"))
                .lifetimeBudget(node.getString("lifetime_budget"))
                .startTime(node.getString("start_time"))
                .build();
        return Optional.of(new CreateMarketingCampaignActionWhatsAppWebGraphQlResponse(campaign));
    }

    /**
     * Returns the parsed campaign.
     *
     * @return the parsed {@link BusinessMarketingCampaign}, never {@code null}
     */
    public BusinessMarketingCampaign campaign() {
        return campaign;
    }
}
