package com.github.auties00.cobalt.model.business.ads;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Input model for the interest-suggestion query of the Click-to-WhatsApp
 * ad creation flow.
 *
 * <p>A Click-to-WhatsApp ad is a paid promotion that opens a chat with the
 * business when tapped. After picking a few interests the merchant asks
 * the server for further related interests to widen the audience. This
 * input carries the parameters the server uses to compute the
 * suggestions.
 *
 * <p>The {@link #detailedTargetingItemsJson() chosen interests} is a
 * JSON-encoded list of the interests the merchant has already selected;
 * its structure is defined by the server and is carried verbatim as a
 * string. The {@link #adAccountId() ad account} scopes the suggestion and
 * the {@link #count() count} caps how many to return; both may be left
 * unset.
 */
@ProtobufMessage(name = "BusinessAdInterestSuggestionQuery")
public final class BusinessAdInterestSuggestionQuery {
    /**
     * Advertising-account identifier the suggestion is scoped to. Unset
     * omits the variable.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String adAccountId;

    /**
     * JSON-encoded chosen interests the suggestion is computed from. The
     * field set is defined by the server and is carried verbatim. Unset
     * omits the variable.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String detailedTargetingItemsJson;

    /**
     * Maximum number of suggestions to return. Unset omits the variable
     * so the server applies its default cap.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.INT32)
    final Integer count;

    /**
     * Constructs a new {@code BusinessAdInterestSuggestionQuery}. Every
     * argument may be {@code null} to omit the corresponding variable from
     * the request.
     *
     * @param adAccountId                the advertising-account identifier,
     *                                   or {@code null}
     * @param detailedTargetingItemsJson the JSON-encoded chosen interests,
     *                                   or {@code null}
     * @param count                      the maximum number of suggestions,
     *                                   or {@code null}
     */
    public BusinessAdInterestSuggestionQuery(String adAccountId, String detailedTargetingItemsJson,
                                             Integer count) {
        this.adAccountId = adAccountId;
        this.detailedTargetingItemsJson = detailedTargetingItemsJson;
        this.count = count;
    }

    /**
     * Returns the advertising-account identifier.
     *
     * @return an {@link Optional} carrying the identifier, or empty when
     *         unset
     */
    public Optional<String> adAccountId() {
        return Optional.ofNullable(adAccountId);
    }

    /**
     * Returns the JSON-encoded chosen interests.
     *
     * @return an {@link Optional} carrying the JSON, or empty when unset
     */
    public Optional<String> detailedTargetingItemsJson() {
        return Optional.ofNullable(detailedTargetingItemsJson);
    }

    /**
     * Returns the maximum number of suggestions to return.
     *
     * @return an {@link OptionalInt} carrying the count, or empty when
     *         unset
     */
    public OptionalInt count() {
        return count == null ? OptionalInt.empty() : OptionalInt.of(count);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BusinessAdInterestSuggestionQuery) obj;
        return Objects.equals(adAccountId, that.adAccountId)
                && Objects.equals(detailedTargetingItemsJson, that.detailedTargetingItemsJson)
                && Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adAccountId, detailedTargetingItemsJson, count);
    }

    @Override
    public String toString() {
        return "BusinessAdInterestSuggestionQuery[" +
                "adAccountId=" + adAccountId + ", " +
                "detailedTargetingItemsJson=" + detailedTargetingItemsJson + ", " +
                "count=" + count + ']';
    }
}
