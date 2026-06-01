package com.github.auties00.cobalt.model.business.ads;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * Input model for the multi-category regulated-targeting adjustment of the
 * Click-to-WhatsApp ad creation flow.
 *
 * <p>A Click-to-WhatsApp ad is a paid promotion that opens a chat with the
 * business when tapped. When the audience targeting falls under several
 * regulated categories at once, plus the special-ad-category restrictions
 * applicable to certain countries, the server rewrites the targeting into
 * a compliant form in a single round trip. This input carries the
 * parameters the server uses to compute that batched rewrite.
 *
 * <p>The {@link #targetingSpecJson() targeting},
 * {@link #regulatedCategoriesJson() regulated categories},
 * {@link #specialAdCategoryCountriesJson() special-ad-category
 * countries}, and {@link #tuningOptionsJson() tuning options} are each
 * JSON-encoded objects whose field sets are defined by the server-side
 * input types and are therefore carried verbatim as strings. The
 * {@link #adAccountId() ad account} scopes the adjustment.
 */
@ProtobufMessage(name = "BusinessAdRegulatedCategoryBatchTuning")
public final class BusinessAdRegulatedCategoryBatchTuning {
    /**
     * Advertising-account identifier the targeting belongs to. Unset omits
     * the variable.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String adAccountId;

    /**
     * JSON-encoded audience targeting specification to adjust. The field
     * set is defined by the server and is carried verbatim. Unset omits
     * the variable.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String targetingSpecJson;

    /**
     * JSON-encoded list of regulated categories that apply. The field set
     * is defined by the server and is carried verbatim. Unset omits the
     * variable.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String regulatedCategoriesJson;

    /**
     * JSON-encoded list of countries whose special-ad-category rules
     * apply. The field set is defined by the server and is carried
     * verbatim. Unset omits the variable.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final String specialAdCategoryCountriesJson;

    /**
     * JSON-encoded tuning options. The field set is defined by the server
     * and is carried verbatim. Unset omits the variable.
     */
    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    final String tuningOptionsJson;

    /**
     * Constructs a new {@code BusinessAdRegulatedCategoryBatchTuning}.
     * Every argument may be {@code null} to omit the corresponding
     * variable from the request.
     *
     * @param adAccountId                    the advertising-account
     *                                       identifier, or {@code null}
     * @param targetingSpecJson              the JSON-encoded targeting, or
     *                                       {@code null}
     * @param regulatedCategoriesJson        the JSON-encoded categories, or
     *                                       {@code null}
     * @param specialAdCategoryCountriesJson the JSON-encoded countries, or
     *                                       {@code null}
     * @param tuningOptionsJson              the JSON-encoded tuning
     *                                       options, or {@code null}
     */
    public BusinessAdRegulatedCategoryBatchTuning(String adAccountId, String targetingSpecJson,
                                                  String regulatedCategoriesJson,
                                                  String specialAdCategoryCountriesJson,
                                                  String tuningOptionsJson) {
        this.adAccountId = adAccountId;
        this.targetingSpecJson = targetingSpecJson;
        this.regulatedCategoriesJson = regulatedCategoriesJson;
        this.specialAdCategoryCountriesJson = specialAdCategoryCountriesJson;
        this.tuningOptionsJson = tuningOptionsJson;
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
     * Returns the JSON-encoded audience targeting specification.
     *
     * @return an {@link Optional} carrying the JSON, or empty when unset
     */
    public Optional<String> targetingSpecJson() {
        return Optional.ofNullable(targetingSpecJson);
    }

    /**
     * Returns the JSON-encoded regulated categories.
     *
     * @return an {@link Optional} carrying the JSON, or empty when unset
     */
    public Optional<String> regulatedCategoriesJson() {
        return Optional.ofNullable(regulatedCategoriesJson);
    }

    /**
     * Returns the JSON-encoded special-ad-category countries.
     *
     * @return an {@link Optional} carrying the JSON, or empty when unset
     */
    public Optional<String> specialAdCategoryCountriesJson() {
        return Optional.ofNullable(specialAdCategoryCountriesJson);
    }

    /**
     * Returns the JSON-encoded tuning options.
     *
     * @return an {@link Optional} carrying the JSON, or empty when unset
     */
    public Optional<String> tuningOptionsJson() {
        return Optional.ofNullable(tuningOptionsJson);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BusinessAdRegulatedCategoryBatchTuning) obj;
        return Objects.equals(adAccountId, that.adAccountId)
                && Objects.equals(targetingSpecJson, that.targetingSpecJson)
                && Objects.equals(regulatedCategoriesJson, that.regulatedCategoriesJson)
                && Objects.equals(specialAdCategoryCountriesJson, that.specialAdCategoryCountriesJson)
                && Objects.equals(tuningOptionsJson, that.tuningOptionsJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adAccountId, targetingSpecJson, regulatedCategoriesJson,
                specialAdCategoryCountriesJson, tuningOptionsJson);
    }

    @Override
    public String toString() {
        return "BusinessAdRegulatedCategoryBatchTuning[" +
                "adAccountId=" + adAccountId + ", " +
                "targetingSpecJson=" + targetingSpecJson + ", " +
                "regulatedCategoriesJson=" + regulatedCategoriesJson + ", " +
                "specialAdCategoryCountriesJson=" + specialAdCategoryCountriesJson + ", " +
                "tuningOptionsJson=" + tuningOptionsJson + ']';
    }
}
