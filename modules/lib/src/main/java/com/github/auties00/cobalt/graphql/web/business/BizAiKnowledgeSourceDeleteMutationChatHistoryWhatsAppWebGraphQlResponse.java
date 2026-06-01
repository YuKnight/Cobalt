package com.github.auties00.cobalt.graphql.web.business;

import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.business.ai.BusinessAiMutationResult;
import com.github.auties00.cobalt.model.business.ai.BusinessAiMutationResultBuilder;

import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the delete-chat-history-source mutation built by
 * {@link BizAiKnowledgeSourceDeleteMutationChatHistoryWhatsAppWebGraphQlRequest} into a
 * {@link BusinessAiMutationResult}.
 *
 * <p>Projects the linked {@code xfb_maiba_delete_chat_history} field, whose single {@code success}
 * scalar reports whether the chat-history source was deleted, onto the shared mutation-result shape.
 * WhatsApp Web treats only an explicit {@code true} as a successful deletion.
 *
 * @see BizAiKnowledgeSourceDeleteMutationChatHistoryWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebBizAiKnowledgeSourceDeleteMutationChatHistoryMutation")
public final class BizAiKnowledgeSourceDeleteMutationChatHistoryWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
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
    private BizAiKnowledgeSourceDeleteMutationChatHistoryWhatsAppWebGraphQlResponse(BusinessAiMutationResult result) {
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
    public static Optional<BizAiKnowledgeSourceDeleteMutationChatHistoryWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }

        var node = data.getJSONObject("xfb_maiba_delete_chat_history");
        if (node == null) {
            return Optional.of(new BizAiKnowledgeSourceDeleteMutationChatHistoryWhatsAppWebGraphQlResponse(null));
        }

        var result = new BusinessAiMutationResultBuilder()
                .success(Boolean.TRUE.equals(node.getBoolean("success")))
                .build();
        return Optional.of(new BizAiKnowledgeSourceDeleteMutationChatHistoryWhatsAppWebGraphQlResponse(result));
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
