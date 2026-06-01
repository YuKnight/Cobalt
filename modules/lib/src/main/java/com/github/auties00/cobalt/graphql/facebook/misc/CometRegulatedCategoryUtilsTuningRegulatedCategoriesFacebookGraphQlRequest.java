package com.github.auties00.cobalt.graphql.facebook.misc;

import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.graphql.facebook.FacebookGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

/**
 * Builds the Facebook GraphQL query that tunes a targeting spec for the configured regulated categories in the
 * WhatsApp Business ad-creation flow.
 *
 * <p>The query takes five GraphQL variables. {@code legacyAccount} is the Facebook ad account the
 * targeting belongs to. {@code targetSpec}, {@code regulatedCategories}, {@code specialAdCategoryCountries}
 * and {@code tuningOptions} parameterise the regulated-category tuning: the targeting spec to tune,
 * the regulated categories to tune for, the special-ad-category countries that apply, and the tuning
 * options object. The relay returns the tuned targeting spec under the linked {@code hec} root's
 * {@code tune_target_spec_for_categories.target_spec_string}; the reply is consumed through
 * {@link CometRegulatedCategoryUtilsTuningRegulatedCategoriesFacebookGraphQlResponse}.
 *
 * @implNote This implementation derives its variables from the operation spec because the
 * {@code LWICometRegulatedCategoryUtilsTuningRegulatedCategoriesQuery} module and its compiled
 * {@code .graphql} document are not present in the static bundle of snapshot {@code 1040120866}; it
 * is one of the Comet ad-creation documents loaded on demand. {@code legacyAccount} is a Facebook
 * ad-account identifier (a numeric string), not a WhatsApp address, so it is modelled as a
 * {@code String} rather than a {@link com.github.auties00.cobalt.model.jid.Jid}. The
 * {@code targetSpec}, {@code regulatedCategories}, {@code specialAdCategoryCountries} and
 * {@code tuningOptions} variables are GraphQL input objects whose field names are not recoverable
 * from the bundle, so each is accepted as a caller-supplied, already JSON-encoded object literal and
 * spliced in verbatim via {@link JSONWriter#writeRaw(String)}. Once a caller that builds those
 * objects surfaces, replace the raw-JSON fields with typed scalar fields mirroring that
 * construction.
 *
 * @see CometRegulatedCategoryUtilsTuningRegulatedCategoriesFacebookGraphQlResponse
 */
@WhatsAppWebModule(moduleName = "LWICometRegulatedCategoryUtilsTuningRegulatedCategoriesQuery")
public final class CometRegulatedCategoryUtilsTuningRegulatedCategoriesFacebookGraphQlRequest implements FacebookGraphQlOperation.Request {
    /**
     * The persisted document identifier the Meta graph endpoint maps to the server-side compiled
     * GraphQL document for this operation.
     *
     * <p>Emitted as the {@code doc_id} field of the Facebook GraphQL request body.
     */
    @WhatsAppWebExport(moduleName = "LWICometRegulatedCategoryUtilsTuningRegulatedCategoriesQuery.graphql",
            exports = "params.id", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String DOC_ID = "9654629321281796";

    /**
     * The GraphQL operation name carried by this request.
     *
     * <p>Used as the persisted-query lookup key and as the perf-telemetry tag.
     */
    @WhatsAppWebExport(moduleName = "LWICometRegulatedCategoryUtilsTuningRegulatedCategoriesQuery.graphql",
            exports = "params.name", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "LWICometRegulatedCategoryUtilsTuningRegulatedCategoriesQuery";

    /**
     * The {@code legacyAccount} GraphQL variable naming the Facebook ad account the targeting belongs
     * to, or {@code null} to omit it.
     *
     * <p>A Facebook ad-account identifier (a numeric string), not a WhatsApp address.
     */
    private final String legacyAccount;

    /**
     * The pre-encoded JSON of the {@code targetSpec} GraphQL input object carrying the targeting spec
     * to tune, or {@code null} to omit it.
     */
    private final String targetSpecJson;

    /**
     * The pre-encoded JSON of the {@code regulatedCategories} GraphQL input value carrying the
     * regulated categories to tune for, or {@code null} to omit it.
     */
    private final String regulatedCategoriesJson;

    /**
     * The pre-encoded JSON of the {@code specialAdCategoryCountries} GraphQL input value carrying the
     * special-ad-category countries that apply, or {@code null} to omit it.
     */
    private final String specialAdCategoryCountriesJson;

    /**
     * The pre-encoded JSON of the {@code tuningOptions} GraphQL input object carrying the tuning
     * options, or {@code null} to omit it.
     */
    private final String tuningOptionsJson;

    /**
     * Constructs a regulated-category tuning query request.
     *
     * <p>The {@code legacyAccount} populates the {@code legacyAccount} GraphQL variable. The
     * {@code targetSpecJson}, {@code regulatedCategoriesJson}, {@code specialAdCategoryCountriesJson}
     * and {@code tuningOptionsJson} are the already-JSON-encoded {@code targetSpec},
     * {@code regulatedCategories}, {@code specialAdCategoryCountries} and {@code tuningOptions} input
     * values; their field names are defined by the server-side input types and are not modelled here
     * (see the class {@code @implNote}). Each value that is {@code null} omits its variable from the
     * serialized object.
     *
     * @param legacyAccount                  the Facebook ad-account identifier, or {@code null} to
     *                                       omit the variable
     * @param targetSpecJson                 the already-JSON-encoded {@code targetSpec} value, or
     *                                       {@code null} to omit the variable
     * @param regulatedCategoriesJson        the already-JSON-encoded {@code regulatedCategories}
     *                                       value, or {@code null} to omit the variable
     * @param specialAdCategoryCountriesJson the already-JSON-encoded {@code specialAdCategoryCountries}
     *                                       value, or {@code null} to omit the variable
     * @param tuningOptionsJson              the already-JSON-encoded {@code tuningOptions} value, or
     *                                       {@code null} to omit the variable
     */
    public CometRegulatedCategoryUtilsTuningRegulatedCategoriesFacebookGraphQlRequest(String legacyAccount, String targetSpecJson, String regulatedCategoriesJson, String specialAdCategoryCountriesJson, String tuningOptionsJson) {
        this.legacyAccount = legacyAccount;
        this.targetSpecJson = targetSpecJson;
        this.regulatedCategoriesJson = regulatedCategoriesJson;
        this.specialAdCategoryCountriesJson = specialAdCategoryCountriesJson;
        this.tuningOptionsJson = tuningOptionsJson;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String docId() {
        return DOC_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return OPERATION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This implementation emits {@code {"legacyAccount": <legacyAccount>, "targetSpec":
     * <targetSpecJson>, "regulatedCategories": <regulatedCategoriesJson>, "specialAdCategoryCountries":
     * <specialAdCategoryCountriesJson>, "tuningOptions": <tuningOptionsJson>}}, writing each variable
     * only when its value is non-null and emitting {@code "{}"} when all are {@code null}. The
     * {@code targetSpec}, {@code regulatedCategories}, {@code specialAdCategoryCountries} and
     * {@code tuningOptions} values are spliced in as raw JSON values via
     * {@link JSONWriter#writeRaw(String)} because they are supplied already encoded.
     */
    @Override
    public String variables() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            if (legacyAccount != null) {
                writer.writeName("legacyAccount");
                writer.writeColon();
                writer.writeString(legacyAccount);
            }

            if (targetSpecJson != null) {
                writer.writeName("targetSpec");
                writer.writeColon();
                writer.writeRaw(targetSpecJson);
            }

            if (regulatedCategoriesJson != null) {
                writer.writeName("regulatedCategories");
                writer.writeColon();
                writer.writeRaw(regulatedCategoriesJson);
            }

            if (specialAdCategoryCountriesJson != null) {
                writer.writeName("specialAdCategoryCountries");
                writer.writeColon();
                writer.writeRaw(specialAdCategoryCountriesJson);
            }

            if (tuningOptionsJson != null) {
                writer.writeName("tuningOptions");
                writer.writeColon();
                writer.writeRaw(tuningOptionsJson);
            }
            writer.endObject();
            try (var output = new StringWriter()) {
                writer.flushTo(output);
                return output.toString();
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
