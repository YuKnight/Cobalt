package com.github.auties00.cobalt.graphql.web.business;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.business.ai.BusinessAiMutationResult;
import com.github.auties00.cobalt.model.business.ai.BusinessAiMutationResultBuilder;

import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the mark-all-lead-data-as-seen mutation built by
 * {@link BizAiLeadGenMarkAllSeenWhatsAppWebGraphQlRequest} into a {@link BusinessAiMutationResult}.
 *
 * <p>Projects the linked {@code meta_ai_biz_agent_wa_mark_all_lead_gen_data_as_seen_for_flow} field,
 * whose single {@code success} scalar reports whether the leads were marked as seen, onto the shared
 * mutation-result shape. WhatsApp Web treats the mutation as successful only when {@code success} is
 * exactly {@code true}.
 *
 * @see BizAiLeadGenMarkAllSeenWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebBizAiLeadGenMarkAllSeenMutation")
public final class BizAiLeadGenMarkAllSeenWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
    /**
     * Holds the projected mutation result, or {@code null} when the relay omitted the field.
     */
    private final BusinessAiMutationResult result;

    /**
     * Constructs a response wrapping the projected mutation result.
     *
     * <p>Reserved for the static parser.
     *
     * @param result the projected mutation result, or {@code null} when the relay omitted the field
     */
    private BizAiLeadGenMarkAllSeenWhatsAppWebGraphQlResponse(BusinessAiMutationResult result) {
        this.result = result;
    }

    /**
     * Parses the WhatsApp Web GraphQL response from the unwrapped GraphQL {@code data} object and projects the
     * {@code success} scalar onto a {@link BusinessAiMutationResult}.
     *
     * @param data the unwrapped GraphQL {@code data} object returned by
     *             {@link com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlClient#send(WhatsAppWebGraphQlOperation.Request)}
     * @return the parsed response, or empty when {@code data} is {@code null}
     */
    public static Optional<BizAiLeadGenMarkAllSeenWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }

        var node = data.getJSONObject("meta_ai_biz_agent_wa_mark_all_lead_gen_data_as_seen_for_flow");
        if (node == null) {
            return Optional.of(new BizAiLeadGenMarkAllSeenWhatsAppWebGraphQlResponse(null));
        }

        var result = new BusinessAiMutationResultBuilder()
                .success(Boolean.TRUE.equals(node.getBoolean("success")))
                .build();
        return Optional.of(new BizAiLeadGenMarkAllSeenWhatsAppWebGraphQlResponse(result));
    }

    /**
     * Returns the projected mutation result.
     *
     * @return the projected {@link BusinessAiMutationResult}, or empty when the relay omitted the field
     */
    public Optional<BusinessAiMutationResult> result() {
        return Optional.ofNullable(result);
    }
}
