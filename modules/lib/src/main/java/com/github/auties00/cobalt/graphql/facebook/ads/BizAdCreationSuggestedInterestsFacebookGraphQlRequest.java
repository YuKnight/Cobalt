package com.github.auties00.cobalt.graphql.facebook.ads;

import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.graphql.facebook.FacebookGraphQlOperation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;

/**
 * Builds the Facebook GraphQL query that fetches suggested detailed-targeting interests for the WhatsApp
 * Business ad-creation audience picker.
 *
 * <p>The query takes three GraphQL variables. The {@code adAccountID} variable is the Facebook
 * ad-account legacy id scoping the suggestion request; it is a numeric Facebook account id rather
 * than a WhatsApp address, so it is kept as a {@link String}. The {@code detailedTargetingItems}
 * variable is the list of detailed-targeting selections the advertiser has already picked, used as
 * the seed the server expands suggestions from. The {@code count} variable is the maximum number of
 * suggested interests to return. The query returns the suggested interests under the advertiser's
 * {@code ad_account}; the reply is consumed through
 * {@link BizAdCreationSuggestedInterestsFacebookGraphQlResponse}.
 *
 * @implNote This implementation accepts {@code detailedTargetingItems} as a caller-supplied,
 * already JSON-encoded array literal because the {@code AdDetailedTargetingItem} input field names
 * are not present in the JS bundle of snapshot {@code 1040120866} (the
 * {@code useWAWebBizAdCreationSuggestedInterestsQuery.graphql} document and its hook are not
 * compiled there); the value is emitted verbatim as the {@code detailedTargetingItems} variable.
 * Once a caller that builds the list surfaces, replace this with a typed element model mirroring
 * that construction.
 *
 * @see BizAdCreationSuggestedInterestsFacebookGraphQlResponse
 */
@WhatsAppWebModule(moduleName = "useWAWebBizAdCreationSuggestedInterestsQuery")
public final class BizAdCreationSuggestedInterestsFacebookGraphQlRequest implements FacebookGraphQlOperation.Request {
    /**
     * The persisted document identifier the Meta graph endpoint maps to the server-side compiled
     * GraphQL document for this operation.
     *
     * <p>Emitted as the {@code doc_id} field of the Facebook GraphQL request body.
     */
    @WhatsAppWebExport(moduleName = "useWAWebBizAdCreationSuggestedInterestsQuery.graphql",
            exports = "params.id", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String DOC_ID = "26091585947105251";

    /**
     * The GraphQL operation name carried by this request.
     *
     * <p>Used as the persisted-query lookup key and as the perf-telemetry tag.
     */
    @WhatsAppWebExport(moduleName = "useWAWebBizAdCreationSuggestedInterestsQuery.graphql",
            exports = "params.name", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "useWAWebBizAdCreationSuggestedInterestsQuery";

    /**
     * The {@code adAccountID} GraphQL variable carrying the Facebook ad-account legacy id scoping the
     * suggestion request, or {@code null} to omit it.
     */
    private final String adAccountId;

    /**
     * The pre-encoded JSON array of the {@code detailedTargetingItems} GraphQL variable carrying the
     * already-selected detailed-targeting seed items, or {@code null} to omit it.
     */
    private final String detailedTargetingItemsJson;

    /**
     * The {@code count} GraphQL variable carrying the maximum number of suggested interests to
     * return, or {@code null} to omit it.
     */
    private final Integer count;

    /**
     * Constructs a suggested-interests query request.
     *
     * <p>The {@code adAccountId} is the Facebook ad-account legacy id scoping the request,
     * {@code detailedTargetingItemsJson} is the already-JSON-encoded array of detailed-targeting seed
     * items whose element field names are defined by the server-side {@code AdDetailedTargetingItem}
     * input type and are not modelled here (see the class {@code @implNote}), and {@code count} is the
     * result limit. Each value that is {@code null} omits its variable from the serialized object.
     *
     * @param adAccountId                the Facebook ad-account legacy id, or {@code null} to omit the
     *                                   variable
     * @param detailedTargetingItemsJson the already-JSON-encoded {@code detailedTargetingItems} array,
     *                                   or {@code null} to omit the variable
     * @param count                      the maximum number of results to return, or {@code null} to
     *                                   omit the variable
     */
    public BizAdCreationSuggestedInterestsFacebookGraphQlRequest(String adAccountId, String detailedTargetingItemsJson, Integer count) {
        this.adAccountId = adAccountId;
        this.detailedTargetingItemsJson = detailedTargetingItemsJson;
        this.count = count;
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
     * @implNote This implementation emits {@code {"adAccountID": <adAccountId>,
     * "detailedTargetingItems": <detailedTargetingItemsJson>, "count": <count>}}, writing each
     * variable only when its value is non-null and emitting {@code "{}"} when all are {@code null}.
     * The {@code detailedTargetingItems} value is spliced in as a raw JSON value via
     * {@link JSONWriter#writeRaw(String)} because it is supplied already encoded.
     */
    @Override
    public String variables() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            if (adAccountId != null) {
                writer.writeName("adAccountID");
                writer.writeColon();
                writer.writeString(adAccountId);
            }

            if (detailedTargetingItemsJson != null) {
                writer.writeName("detailedTargetingItems");
                writer.writeColon();
                writer.writeRaw(detailedTargetingItemsJson);
            }

            if (count != null) {
                writer.writeName("count");
                writer.writeColon();
                writer.writeInt32(count);
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
