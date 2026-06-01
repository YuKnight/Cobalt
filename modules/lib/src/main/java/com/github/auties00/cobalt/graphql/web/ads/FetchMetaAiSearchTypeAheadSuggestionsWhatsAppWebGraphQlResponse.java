package com.github.auties00.cobalt.graphql.web.ads;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.business.ai.MetaAiSearchSuggestion;
import com.github.auties00.cobalt.model.business.ai.MetaAiSearchSuggestionBuilder;
import com.github.auties00.cobalt.model.business.ai.MetaAiSearchSuggestions;
import com.github.auties00.cobalt.model.business.ai.MetaAiSearchSuggestionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parses the WhatsApp Web GraphQL response of the Meta AI search type-ahead query built by
 * {@link FetchMetaAiSearchTypeAheadSuggestionsWhatsAppWebGraphQlRequest} into a {@link MetaAiSearchSuggestions}.
 *
 * <p>Reads the linked root {@code xwa_genai_meta_ai_search_typeahead} and projects its
 * {@code suggestions} array onto the Cobalt domain model: one {@link MetaAiSearchSuggestion} per
 * tile, pairing the displayed suggestion copy (read from the wire alias {@code query}), the
 * originating session id, and the opaque ranking context.
 *
 * @see FetchMetaAiSearchTypeAheadSuggestionsWhatsAppWebGraphQlRequest
 */
@WhatsAppWebModule(moduleName = "WAWebFetchMetaAISearchTypeAheadSuggestionsQuery")
public final class FetchMetaAiSearchTypeAheadSuggestionsWhatsAppWebGraphQlResponse implements WhatsAppWebGraphQlOperation.Response {
    /**
     * Holds the parsed type-ahead suggestions bundle.
     */
    private final MetaAiSearchSuggestions suggestions;

    /**
     * Constructs a response wrapping the parsed suggestions bundle.
     *
     * <p>Reserved for the static parser.
     *
     * @param suggestions the parsed suggestions bundle, or {@code null} when the relay omitted the
     *                    field
     */
    private FetchMetaAiSearchTypeAheadSuggestionsWhatsAppWebGraphQlResponse(MetaAiSearchSuggestions suggestions) {
        this.suggestions = suggestions;
    }

    /**
     * Parses the WhatsApp Web GraphQL response from the unwrapped GraphQL {@code data} object.
     *
     * <p>Reads the linked root {@code xwa_genai_meta_ai_search_typeahead} and projects it onto a
     * {@link MetaAiSearchSuggestions}; the returned {@link Optional} is empty when {@code data} or
     * the typeahead object is missing.
     *
     * @param data the unwrapped GraphQL {@code data} object returned by
     *             {@link com.github.auties00.cobalt.graphql.web.WhatsAppWebGraphQlClient#send(WhatsAppWebGraphQlOperation.Request)}
     * @return the parsed response, or empty when {@code data} or the typeahead object is missing
     */
    public static Optional<FetchMetaAiSearchTypeAheadSuggestionsWhatsAppWebGraphQlResponse> of(JSONObject data) {
        if (data == null) {
            return Optional.empty();
        }

        var node = data.getJSONObject("xwa_genai_meta_ai_search_typeahead");
        if (node == null) {
            return Optional.empty();
        }

        var suggestions = new MetaAiSearchSuggestionsBuilder()
                .suggestions(parseSuggestions(node.getJSONArray("suggestions")))
                .build();
        return Optional.of(new FetchMetaAiSearchTypeAheadSuggestionsWhatsAppWebGraphQlResponse(suggestions));
    }

    /**
     * Projects the {@code suggestions} array onto a list of {@link MetaAiSearchSuggestion}.
     *
     * <p>The displayed suggestion copy is read from the wire key {@code query} (the alias of the
     * GraphQL {@code suggestion} field).
     *
     * @param arr the JSON array to project
     * @return the projected list, empty when {@code arr} is {@code null}
     */
    private static List<MetaAiSearchSuggestion> parseSuggestions(JSONArray arr) {
        if (arr == null) {
            return List.of();
        }

        var result = new ArrayList<MetaAiSearchSuggestion>(arr.size());
        for (var i = 0; i < arr.size(); i++) {
            var obj = arr.getJSONObject(i);
            if (obj == null) {
                continue;
            }

            var copy = obj.getString("query");
            result.add(new MetaAiSearchSuggestionBuilder()
                    .suggestion(copy)
                    .query(copy)
                    .sessionId(obj.getString("session_id"))
                    .build());
        }
        return result;
    }

    /**
     * Returns the parsed type-ahead suggestions bundle.
     *
     * @return the parsed {@link MetaAiSearchSuggestions}, never {@code null}
     */
    public MetaAiSearchSuggestions suggestions() {
        return suggestions;
    }
}
