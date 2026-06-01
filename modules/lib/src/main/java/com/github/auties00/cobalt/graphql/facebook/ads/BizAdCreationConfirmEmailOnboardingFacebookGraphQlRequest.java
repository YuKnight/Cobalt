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
 * Builds the comet mutation that confirms the email step of WhatsApp Business ad-account onboarding.
 *
 * <p>The single {@code input} GraphQL variable is the onboarding-data upsert object WhatsApp Web
 * passes to {@code wa_ad_account_upsert_onboarding_data(input: $input)}. The compiled
 * {@code useWAWebBizAdCreationConfirmEmailOnboardingMutation.graphql} document and the hook module
 * that builds the object are both absent from the analysed bundle, so its field names are not
 * recoverable; the caller supplies it as an already JSON-encoded object. The mutation returns the
 * upsert outcome under {@code wa_ad_account_upsert_onboarding_data}; the reply is consumed through
 * {@link BizAdCreationConfirmEmailOnboardingFacebookGraphQlResponse}.
 *
 * @implNote This implementation accepts the {@code input} object as a caller-supplied, already
 * JSON-encoded object literal because neither the
 * {@code useWAWebBizAdCreationConfirmEmailOnboardingMutation.graphql} document nor a caller building
 * the input object is present in the JS bundle of snapshot {@code 1040120866}; the value is emitted
 * verbatim as the {@code input} variable. Once a caller that builds the object surfaces, replace this
 * with typed scalar fields mirroring that construction.
 *
 * @see BizAdCreationConfirmEmailOnboardingFacebookGraphQlResponse
 */
@WhatsAppWebModule(moduleName = "useWAWebBizAdCreationConfirmEmailOnboardingMutation")
public final class BizAdCreationConfirmEmailOnboardingFacebookGraphQlRequest implements FacebookGraphQlOperation.Request {
    /**
     * The persisted document identifier the Meta graph endpoint maps to the server-side compiled
     * GraphQL document for this operation.
     *
     * <p>Emitted as the {@code doc_id} field of the Facebook GraphQL request body.
     */
    @WhatsAppWebExport(moduleName = "useWAWebBizAdCreationConfirmEmailOnboardingMutation.graphql",
            exports = "params.id", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String DOC_ID = "25542802338674299";

    /**
     * The GraphQL operation name carried by this request.
     *
     * <p>Used as the persisted-query lookup key and as the perf-telemetry tag.
     */
    @WhatsAppWebExport(moduleName = "useWAWebBizAdCreationConfirmEmailOnboardingMutation.graphql",
            exports = "params.name", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "useWAWebBizAdCreationConfirmEmailOnboardingMutation";

    /**
     * The pre-encoded JSON of the {@code input} GraphQL object carrying the onboarding data to
     * upsert, or {@code null} to omit it.
     */
    private final String inputJson;

    /**
     * Constructs a confirm-email-onboarding mutation request.
     *
     * <p>The {@code inputJson} is the already-JSON-encoded {@code input} object holding the
     * onboarding data to upsert; its field names are defined by the server-side input type and are
     * not modelled here (see the class {@code @implNote}). A {@code null} value omits the variable
     * from the serialized object.
     *
     * @param inputJson the already-JSON-encoded {@code input} object, or {@code null} to omit the
     *                  variable
     */
    public BizAdCreationConfirmEmailOnboardingFacebookGraphQlRequest(String inputJson) {
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
