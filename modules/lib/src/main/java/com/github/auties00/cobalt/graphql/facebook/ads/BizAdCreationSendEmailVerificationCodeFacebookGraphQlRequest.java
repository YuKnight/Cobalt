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
 * Builds the comet mutation that sends an email verification code for a WhatsApp Business
 * ad-account email-onboarding flow.
 *
 * <p>The mutation takes a single {@code input} GraphQL variable holding the email-address and
 * ad-account context the relay needs to dispatch the verification email. The {@code input} object is
 * declared opaquely in the compiled document and no caller building it is present in the analysed
 * bundle, so the caller supplies it already JSON-encoded. The mutation returns whether the email was
 * sent and, if not, the failure reason; the reply is consumed through
 * {@link BizAdCreationSendEmailVerificationCodeFacebookGraphQlResponse}.
 *
 * @implNote This implementation accepts the {@code input} object as a caller-supplied, already
 * JSON-encoded object literal because the {@code WaAdAccountSendEmailVerificationCodeData} field
 * names are not present in the JS bundle of snapshot {@code 1040120866}; the value is emitted
 * verbatim as the {@code input} variable. Once a caller that builds the object surfaces, replace
 * this with typed scalar fields mirroring that construction.
 *
 * @see BizAdCreationSendEmailVerificationCodeFacebookGraphQlResponse
 */
@WhatsAppWebModule(moduleName = "useWAWebBizAdCreationSendEmailVerificationCodeMutation")
public final class BizAdCreationSendEmailVerificationCodeFacebookGraphQlRequest implements FacebookGraphQlOperation.Request {
    /**
     * The persisted document identifier the Meta graph endpoint maps to the server-side compiled
     * GraphQL document for this operation.
     *
     * <p>Emitted as the {@code doc_id} field of the Facebook GraphQL request body.
     */
    @WhatsAppWebExport(moduleName = "useWAWebBizAdCreationSendEmailVerificationCodeMutation.graphql",
            exports = "params.id", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String DOC_ID = "25314829244811285";

    /**
     * The GraphQL operation name carried by this request.
     *
     * <p>Used as the persisted-query lookup key and as the perf-telemetry tag.
     */
    @WhatsAppWebExport(moduleName = "useWAWebBizAdCreationSendEmailVerificationCodeMutation.graphql",
            exports = "params.name", adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "useWAWebBizAdCreationSendEmailVerificationCodeMutation";

    /**
     * The pre-encoded JSON of the {@code input} GraphQL object carrying the email and ad-account
     * context, or {@code null} to omit it.
     */
    private final String inputJson;

    /**
     * Constructs a send-email-verification-code mutation request.
     *
     * <p>The {@code inputJson} is the already-JSON-encoded {@code input} object holding the email and
     * ad-account context; its field names are defined by the server-side input type and are not
     * modelled here (see the class {@code @implNote}). A {@code null} value omits the variable from
     * the serialized object.
     *
     * @param inputJson the already-JSON-encoded {@code input} object, or {@code null} to omit the
     *                  variable
     */
    public BizAdCreationSendEmailVerificationCodeFacebookGraphQlRequest(String inputJson) {
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
