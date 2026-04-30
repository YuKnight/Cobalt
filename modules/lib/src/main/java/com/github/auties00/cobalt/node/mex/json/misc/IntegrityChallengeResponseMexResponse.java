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
 * Parsed response of the {@link IntegrityChallengeResponseMexRequest}
 * mutation, exposing the {@code success} and {@code error_message} scalars
 * from the {@code xwa2_submit_integrity_challenge_response} envelope.
 */
@WhatsAppWebModule(moduleName = "WAWebMexIntegrityChallengeResponse")
public final class IntegrityChallengeResponseMexResponse implements MexOperation.Response.Json {
    /**
     * The {@code success} scalar reflecting the relay's verdict on the
     * submitted challenge.
     */
    private final Boolean success;
    /**
     * The {@code error_message} scalar populated when the challenge was
     * rejected.
     */
    private final String errorMessage;

    /**
     * Constructs a response wrapping the parsed scalar fields.
     *
     * @param success      the {@code success} scalar, or {@code null} if absent
     * @param errorMessage the {@code error_message} scalar, or {@code null} if absent
     */
    private IntegrityChallengeResponseMexResponse(Boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the node is missing a result payload
     */
    @WhatsAppWebExport(moduleName = "WAWebMexIntegrityChallengeResponse", exports = "mexSubmitPasskeyChallengeResponse",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Optional<IntegrityChallengeResponseMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(IntegrityChallengeResponseMexResponse::of);
    }

    /**
     * Returns the relay's verdict on the submitted challenge.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<Boolean> success() {
        return Optional.ofNullable(success);
    }

    /**
     * Returns the error message reported by the relay when the challenge
     * was rejected.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> errorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    /**
     * Parses a {@link IntegrityChallengeResponseMexResponse} from the raw
     * JSON bytes of the {@code <result>} child.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the envelope is missing expected fields
     */
    private static Optional<IntegrityChallengeResponseMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_submit_integrity_challenge_response");
        if (root == null) {
            return Optional.empty();
        }

        var success = root.getBoolean("success");
        var errorMessage = root.getString("error_message");

        return Optional.of(new IntegrityChallengeResponseMexResponse(success, errorMessage));
    }
}
