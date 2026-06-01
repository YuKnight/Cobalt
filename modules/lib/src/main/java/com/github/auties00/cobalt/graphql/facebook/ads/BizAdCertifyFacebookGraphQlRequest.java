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
 * Builds the comet mutation that submits the caller's ads-integrity self-certification.
 *
 * <p>The mutation takes a single {@code input} GraphQL object carrying the self-certification
 * payload. The relay returns the certification outcome under
 * {@code ads_integrity_self_certification_certify}, whose {@code certified_user_name} and
 * {@code create_time} scalars record who certified and when; the reply is consumed through
 * {@link BizAdCertifyFacebookGraphQlResponse}.
 *
 * @implNote This implementation accepts the {@code input} object as a caller-supplied, already
 * JSON-encoded object literal because the {@code useWAWebBizAdCertifyMutation} hook module and its
 * input type are not present in the static bundle of snapshot {@code 1040120866}; it is one of the
 * Comet ad-creation documents loaded on demand. The value is emitted verbatim as the {@code input}
 * variable. Once a caller that builds the object surfaces, replace this with typed scalar fields
 * mirroring that construction.
 *
 * @see BizAdCertifyFacebookGraphQlResponse
 */
@WhatsAppWebModule(moduleName = "useWAWebBizAdCertifyMutation")
public final class BizAdCertifyFacebookGraphQlRequest implements FacebookGraphQlOperation.Request {
    /**
     * The persisted document identifier the Meta graph endpoint maps to the server-side compiled
     * GraphQL document for this operation.
     *
     * <p>Emitted as the {@code doc_id} field of the Facebook GraphQL request body.
     */
    @WhatsAppWebExport(moduleName = "useWAWebBizAdCertifyMutation.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String DOC_ID = "25340988522238060";

    /**
     * The GraphQL operation name carried by this request.
     *
     * <p>Used as the persisted-query lookup key and as the perf-telemetry tag.
     */
    @WhatsAppWebExport(moduleName = "useWAWebBizAdCertifyMutation.graphql", exports = "params.name",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final String OPERATION_NAME = "useWAWebBizAdCertifyMutation";

    /**
     * The pre-encoded JSON of the {@code input} GraphQL object carrying the self-certification
     * payload, or {@code null} to omit it.
     */
    private final String inputJson;

    /**
     * Constructs an ads-integrity self-certification mutation request.
     *
     * <p>The {@code inputJson} is the already-JSON-encoded {@code input} object holding the
     * self-certification payload; its field names are defined by the server-side input type and are
     * not modelled here (see the class {@code @implNote}). A {@code null} value omits the variable
     * from the serialized object.
     *
     * @param inputJson the already-JSON-encoded {@code input} object, or {@code null} to omit the
     *                  variable
     */
    public BizAdCertifyFacebookGraphQlRequest(String inputJson) {
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
