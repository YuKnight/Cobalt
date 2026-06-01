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
 * Builds the Facebook GraphQL query that fetches the billing summary content for the WhatsApp Business
 * ad-creation review step.
 *
 * <p>The query takes two GraphQL variables. The {@code asset_id} variable is the Facebook billable
 * asset id whose billable account the summary is computed for; it is a numeric Facebook id rather
 * than a WhatsApp address, so it is kept as a {@link String}. The {@code budget} variable carries
 * the advertiser's chosen budget against which the server estimates taxes and totals. The query
 * returns the billable account's estimated taxes and total under {@code billable_account_by_asset_id};
 * the reply is consumed through {@link BizAdCreationSummaryContentFacebookGraphQlResponse}.
 *
 * @implNote This implementation accepts {@code budget} as a caller-supplied, already JSON-encoded
 * value because the {@code budget} GraphQL input type and field names are not recoverable from the
 * compiled {@code WAWebBizAdCreationSummaryContentQuery.graphql} document of snapshot
 * {@code 1040120866} (the document declares it only as an opaque {@code LocalArgument} forwarded as
 * a {@code Variable} to {@code billing_info}, and no in-bundle caller builds it); the value is
 * emitted verbatim as the {@code budget} variable. Once a caller that builds the budget surfaces,
 * replace this with typed scalar fields mirroring that construction.
 *
 * @see BizAdCreationSummaryContentFacebookGraphQlResponse
 */
@WhatsAppWebModule(moduleName = "WAWebBizAdCreationSummaryContentQuery")
public final class BizAdCreationSummaryContentFacebookGraphQlRequest implements FacebookGraphQlOperation.Request {
    /**
     * The persisted document identifier the Meta graph endpoint maps to the server-side compiled
     * GraphQL document for this operation.
     *
     * <p>Emitted as the {@code doc_id} field of the Facebook GraphQL request body.
     */
    @WhatsAppWebExport(moduleName = "WAWebBizAdCreationSummaryContentQuery.graphql",
            exports = "params.id", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String DOC_ID = "33567643556217656";

    /**
     * The GraphQL operation name carried by this request.
     *
     * <p>Used as the persisted-query lookup key and as the perf-telemetry tag.
     */
    @WhatsAppWebExport(moduleName = "WAWebBizAdCreationSummaryContentQuery.graphql",
            exports = "params.name", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "WAWebBizAdCreationSummaryContentQuery";

    /**
     * The {@code asset_id} GraphQL variable carrying the Facebook billable asset id whose billable
     * account is summarized, or {@code null} to omit it.
     */
    private final String assetId;

    /**
     * The pre-encoded JSON of the {@code budget} GraphQL variable carrying the advertiser's chosen
     * budget, or {@code null} to omit it.
     */
    private final String budgetJson;

    /**
     * Constructs a summary-content query request.
     *
     * <p>The {@code assetId} is the Facebook billable asset id, and {@code budgetJson} is the
     * already-JSON-encoded {@code budget} value whose shape is defined by the server-side budget input
     * type and is not modelled here (see the class {@code @implNote}). Each value that is {@code null}
     * omits its variable from the serialized object.
     *
     * @param assetId    the Facebook billable asset id, or {@code null} to omit the variable
     * @param budgetJson the already-JSON-encoded {@code budget} value, or {@code null} to omit the
     *                   variable
     */
    public BizAdCreationSummaryContentFacebookGraphQlRequest(String assetId, String budgetJson) {
        this.assetId = assetId;
        this.budgetJson = budgetJson;
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
     * @implNote This implementation emits {@code {"asset_id": <assetId>, "budget": <budgetJson>}},
     * writing each variable only when its value is non-null and emitting {@code "{}"} when both are
     * {@code null}. The {@code budget} value is spliced in as a raw JSON value via
     * {@link JSONWriter#writeRaw(String)} because it is supplied already encoded.
     */
    @Override
    public String variables() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            if (assetId != null) {
                writer.writeName("asset_id");
                writer.writeColon();
                writer.writeString(assetId);
            }

            if (budgetJson != null) {
                writer.writeName("budget");
                writer.writeColon();
                writer.writeRaw(budgetJson);
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
