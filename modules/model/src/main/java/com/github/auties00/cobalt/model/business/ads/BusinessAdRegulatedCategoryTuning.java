package com.github.auties00.cobalt.model.business.ads;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * Input model for the single-category regulated-targeting adjustment of the
 * Click-to-WhatsApp ad creation flow.
 *
 * <p>A Click-to-WhatsApp ad is a paid promotion that opens a chat with the
 * business when tapped. When the audience targeting falls under one
 * regulated category (for example, housing, credit, or employment in the
 * United States, or the European Union Digital Services Act elsewhere) the
 * server rewrites the targeting into a compliant form. This input carries
 * the parameters the server uses to compute that rewrite.
 *
 * <p>The {@link #targetingSpec() targeting} is the audience specification
 * to adjust; its structure is defined by the server and is carried
 * verbatim as a string. The {@link #regulatedCategory() regulated
 * category} names which category applies, taken from Meta's standard
 * regulated-category catalog. The {@link #tuningOptionsJson() tuning
 * options} are a JSON-encoded object whose field set is defined by the
 * server and are carried verbatim. The {@link #adAccountId() ad account}
 * scopes the adjustment.
 */
@ProtobufMessage(name = "BusinessAdRegulatedCategoryTuning")
public final class BusinessAdRegulatedCategoryTuning {
    /**
     * Advertising-account identifier the targeting belongs to. Unset omits
     * the variable.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String adAccountId;

    /**
     * Audience targeting specification to adjust. The structure is defined
     * by the server and is carried verbatim. Unset omits the variable.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String targetingSpec;

    /**
     * Regulated category that applies. The recognised values come from
     * Meta's standard regulated-category catalog (housing, credit, or
     * employment in the United States, plus the European Union Digital
     * Services Act elsewhere); the set is not exposed by the WhatsApp Web
     * bundle and is therefore not modelled as an enum. Unset omits the
     * variable.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String regulatedCategory;

    /**
     * JSON-encoded tuning options. The field set is defined by the server
     * and is carried verbatim. Unset omits the variable.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final String tuningOptionsJson;

    /**
     * Constructs a new {@code BusinessAdRegulatedCategoryTuning}. Every
     * argument may be {@code null} to omit the corresponding variable from
     * the request.
     *
     * @param adAccountId       the advertising-account identifier, or
     *                          {@code null}
     * @param targetingSpec     the audience targeting, or {@code null}
     * @param regulatedCategory the applicable category, or {@code null}
     * @param tuningOptionsJson the JSON-encoded tuning options, or
     *                          {@code null}
     */
    public BusinessAdRegulatedCategoryTuning(String adAccountId, String targetingSpec,
                                             String regulatedCategory, String tuningOptionsJson) {
        this.adAccountId = adAccountId;
        this.targetingSpec = targetingSpec;
        this.regulatedCategory = regulatedCategory;
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
     * Returns the audience targeting specification.
     *
     * @return an {@link Optional} carrying the specification, or empty
     *         when unset
     */
    public Optional<String> targetingSpec() {
        return Optional.ofNullable(targetingSpec);
    }

    /**
     * Returns the regulated category that applies.
     *
     * @return an {@link Optional} carrying the category, or empty when
     *         unset
     */
    public Optional<String> regulatedCategory() {
        return Optional.ofNullable(regulatedCategory);
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
        var that = (BusinessAdRegulatedCategoryTuning) obj;
        return Objects.equals(adAccountId, that.adAccountId)
                && Objects.equals(targetingSpec, that.targetingSpec)
                && Objects.equals(regulatedCategory, that.regulatedCategory)
                && Objects.equals(tuningOptionsJson, that.tuningOptionsJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adAccountId, targetingSpec, regulatedCategory, tuningOptionsJson);
    }

    @Override
    public String toString() {
        return "BusinessAdRegulatedCategoryTuning[" +
                "adAccountId=" + adAccountId + ", " +
                "targetingSpec=" + targetingSpec + ", " +
                "regulatedCategory=" + regulatedCategory + ", " +
                "tuningOptionsJson=" + tuningOptionsJson + ']';
    }
}
