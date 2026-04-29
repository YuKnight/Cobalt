package com.github.auties00.cobalt.node.mex.json.group;

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
import java.util.Objects;
import java.util.Optional;

/**
 * The response variant of {@link CreateInviteCodeMexResponse} that exposes the
 * data returned by the server after a successful mutation.
 *
 * @implNote WAWebMexCreateInviteCodeJob: adapts the JSON root returned by
 * the GraphQL mutation into a Java value object. WA Web's
 * {@code mexCreateInviteCode} unwraps the response by reading
 * {@code s.xwa2_growth_create_invite_code?.code}; Cobalt mirrors the
 * same projection and exposes the scalar as an {@link Optional}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexCreateInviteCodeJob")
public final class CreateInviteCodeMexResponse implements MexOperation.Response.Json {
    private final String code;

    private CreateInviteCodeMexResponse(String code) {
        this.code = code;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexCreateInviteCodeJob.mexCreateInviteCode: WA Web
     * relies on the GraphQL client to unwrap the response. Cobalt
     * performs the unwrapping manually from the IQ {@code <result>}
     * child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    @WhatsAppWebExport(moduleName = "WAWebMexCreateInviteCodeJob", exports = "mexCreateInviteCode",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<CreateInviteCodeMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(CreateInviteCodeMexResponse::of);
    }

    /**
     * Returns the freshly minted invite {@code code} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> code() {
        return Optional.ofNullable(code);
    }

    /**
     * Parses a {@link CreateInviteCodeMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexCreateInviteCodeJob.mexCreateInviteCode: mirrors
     * the implicit unwrapping that WA Web performs on the GraphQL
     * response, extracting the
     * {@code xwa2_growth_create_invite_code.code} scalar.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<CreateInviteCodeMexResponse> of(byte[] json) {
        // WAWebMexCreateInviteCodeJob.mexCreateInviteCode
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexCreateInviteCodeJob.mexCreateInviteCode
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexCreateInviteCodeJob.mexCreateInviteCode
        // Extracts the operation-specific root keyed by xwa2_growth_create_invite_code
        var root = data.getJSONObject("xwa2_growth_create_invite_code");
        if (root == null) {
            return Optional.empty();
        }

        var code = root.getString("code");

        return Optional.of(new CreateInviteCodeMexResponse(code));
    }
}
