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
 * Parsed response of the {@link CreateInviteCodeMexRequest} mutation, exposing
 * the freshly minted invite code returned by the server.
 *
 * @implNote WA Web's {@code mexCreateInviteCode} unwraps the response by
 * reading {@code s.xwa2_growth_create_invite_code?.code}. Cobalt mirrors the
 * same projection and exposes the scalar as an {@link Optional}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexCreateInviteCodeJob")
public final class CreateInviteCodeMexResponse implements MexOperation.Response.Json {
    /**
     * The invite code scalar projected from
     * {@code xwa2_growth_create_invite_code.code}.
     */
    private final String code;

    /**
     * Constructs a response wrapping the invite {@code code} scalar parsed
     * from the GraphQL envelope.
     *
     * @param code the invite code returned by the relay, or {@code null} if absent
     */
    private CreateInviteCodeMexResponse(String code) {
        this.code = code;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the node is missing a result payload
     */
    @WhatsAppWebExport(moduleName = "WAWebMexCreateInviteCodeJob", exports = "mexCreateInviteCode",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<CreateInviteCodeMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(CreateInviteCodeMexResponse::of);
    }

    /**
     * Returns the freshly minted invite code.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> code() {
        return Optional.ofNullable(code);
    }

    /**
     * Parses a {@link CreateInviteCodeMexResponse} from the raw JSON bytes
     * of the {@code <result>} child.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the envelope is missing expected fields
     */
    private static Optional<CreateInviteCodeMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_growth_create_invite_code");
        if (root == null) {
            return Optional.empty();
        }

        var code = root.getString("code");

        return Optional.of(new CreateInviteCodeMexResponse(code));
    }
}
