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
 * Builds the Facebook GraphQL query that reads the WhatsApp Business ad-creation budget state for the linked
 * boosted component.
 *
 * <p>The single {@code input} GraphQL variable is the {@code CTWABoostedComponentInput} object
 * WhatsApp Web passes to {@code lwi.boosted_component(caller: ..., input: $input)}. The compiled
 * {@code useWAWebBizAdCreationAdAccountUpdate_BudgetQuery.graphql} document declares the variable
 * opaquely and no caller building the object is present in the analysed bundle, so its field names
 * are not recoverable; the caller supplies it as an already JSON-encoded object. The query returns
 * the boosted component's current budget amount, the high-granularity budget option list,
 * and the minimum daily-budget constraint under {@code lwi.boosted_component}; the reply is consumed
 * through {@link BizAdCreationAdAccountUpdate_BudgetFacebookGraphQlResponse}.
 *
 * @implNote This implementation accepts the {@code input} object as a caller-supplied, already
 * JSON-encoded object literal because the {@code CTWABoostedComponentInput} field names are not
 * present in the JS bundle of snapshot {@code 1040120866}; the value is emitted verbatim as the
 * {@code input} variable. Once a caller that builds the object surfaces, replace this with typed
 * scalar fields mirroring that construction.
 *
 * @see BizAdCreationAdAccountUpdate_BudgetFacebookGraphQlResponse
 */
@WhatsAppWebModule(moduleName = "useWAWebBizAdCreationAdAccountUpdate_BudgetQuery")
public final class BizAdCreationAdAccountUpdate_BudgetFacebookGraphQlRequest implements FacebookGraphQlOperation.Request {
    /**
     * The persisted document identifier the Meta graph endpoint maps to the server-side compiled
     * GraphQL document for this operation.
     *
     * <p>Emitted as the {@code doc_id} field of the Facebook GraphQL request body.
     */
    @WhatsAppWebExport(moduleName = "useWAWebBizAdCreationAdAccountUpdate_BudgetQuery.graphql",
            exports = "params.id", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String DOC_ID = "27196835776576294";

    /**
     * The GraphQL operation name carried by this request.
     *
     * <p>Used as the persisted-query lookup key and as the perf-telemetry tag.
     */
    @WhatsAppWebExport(moduleName = "useWAWebBizAdCreationAdAccountUpdate_BudgetQuery.graphql",
            exports = "params.name", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "useWAWebBizAdCreationAdAccountUpdate_BudgetQuery";

    /**
     * The pre-encoded JSON of the {@code input} GraphQL object identifying the boosted component to
     * read, or {@code null} to omit it.
     */
    private final String inputJson;

    /**
     * Constructs a budget-state query request.
     *
     * <p>The {@code inputJson} is the already-JSON-encoded {@code input} object identifying the
     * boosted component; its field names are defined by the server-side
     * {@code CTWABoostedComponentInput} type and are not modelled here (see the class
     * {@code @implNote}). A {@code null} value omits the variable from the serialized object.
     *
     * @param inputJson the already-JSON-encoded {@code input} object, or {@code null} to omit the
     *                  variable
     */
    public BizAdCreationAdAccountUpdate_BudgetFacebookGraphQlRequest(String inputJson) {
        this.inputJson = inputJson;
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
     * @implNote This implementation emits {@code {"input": <inputJson>}}, writing the variable only
     * when its value is non-null and emitting {@code "{}"} when it is {@code null}. The {@code input}
     * value is spliced in as a raw JSON value via {@link JSONWriter#writeRaw(String)} because it is
     * supplied already encoded.
     */
    @Override
    public String variables() {
        try (var writer = JSONWriter.ofUTF8()) {
            writer.startObject();
            if (inputJson != null) {
                writer.writeName("input");
                writer.writeColon();
                writer.writeRaw(inputJson);
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
