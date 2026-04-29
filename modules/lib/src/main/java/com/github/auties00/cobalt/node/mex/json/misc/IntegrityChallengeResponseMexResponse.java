package com.github.auties00.cobalt.node.mex.json.misc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.mex.MexOperation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * The response variant of {@link IntegrityChallengeResponseMexResponse} that
 * exposes the data returned by the server after a successful mutation.
 *
 * @implNote WAWebMexIntegrityChallengeResponse: adapts the JSON root
 * returned by the GraphQL mutation into a Java value object. WA Web's
 * {@code mexSubmitPasskeyChallengeResponse} returns the
 * {@code xwa2_submit_integrity_challenge_response} sub-object
 * verbatim; the compiled GraphQL artifact projects
 * {@code success} and {@code error_message} on it. Cobalt exposes
 * both scalars through typed accessors.
 */
@WhatsAppWebModule(moduleName = "WAWebMexIntegrityChallengeResponse")
public final class IntegrityChallengeResponseMexResponse implements MexOperation.Response.Json {
    private final Boolean success;
    private final String errorMessage;

    private IntegrityChallengeResponseMexResponse(Boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse:
     * WA Web relies on the GraphQL client to unwrap the response.
     * Cobalt performs the unwrapping manually from the IQ
     * {@code <result>} child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    @WhatsAppWebExport(moduleName = "WAWebMexIntegrityChallengeResponse", exports = "mexSubmitPasskeyChallengeResponse",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Optional<IntegrityChallengeResponseMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(IntegrityChallengeResponseMexResponse::of);
    }

    /**
     * Returns the {@code success} field reported by the relay.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<Boolean> success() {
        return Optional.ofNullable(success);
    }

    /**
     * Returns the {@code error_message} field reported by the relay
     * when the challenge was rejected.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> errorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    /**
     * Parses a {@link IntegrityChallengeResponseMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse:
     * mirrors the implicit unwrapping that WA Web performs on the
     * GraphQL response, extracting the
     * {@code xwa2_submit_integrity_challenge_response} root.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<IntegrityChallengeResponseMexResponse> of(byte[] json) {
        // WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexIntegrityChallengeResponse.mexSubmitPasskeyChallengeResponse
        // Extracts the operation-specific root keyed by xwa2_submit_integrity_challenge_response
        var root = data.getJSONObject("xwa2_submit_integrity_challenge_response");
        if (root == null) {
            return Optional.empty();
        }

        var success = root.getBoolean("success");
        var errorMessage = root.getString("error_message");

        return Optional.of(new IntegrityChallengeResponseMexResponse(success, errorMessage));
    }
}
