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
import java.util.Optional;

/**
 * Parsed response of the {@link FetchGroupIsInternalMexRequest} query,
 * exposing whether the queried group is flagged as internal by the relay.
 *
 * @implNote WA Web's {@code mexFetchGroupIsInternal} unwraps the response by
 * reading {@code n.xwa2_group_query_by_id?.properties?.internal === true}.
 * Cobalt mirrors the same triple-strict-equality projection so {@code null},
 * absent objects and explicit {@code false} all collapse to {@code false}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchGroupIsInternalJob")
public final class FetchGroupIsInternalMexResponse implements MexOperation.Response.Json {
    /**
     * The internal-flag scalar projected from
     * {@code xwa2_group_query_by_id.properties.internal}.
     */
    private final boolean internal;

    /**
     * Constructs a response wrapping the boolean internal-flag scalar.
     *
     * @param internal whether the relay reports the group as internal
     */
    private FetchGroupIsInternalMexResponse(boolean internal) {
        this.internal = internal;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the node is missing a result payload
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchGroupIsInternalJob", exports = "mexFetchGroupIsInternal",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchGroupIsInternalMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchGroupIsInternalMexResponse::of);
    }

    /**
     * Returns whether the queried group is flagged as internal.
     *
     * @return {@code true} if the relay reports the group as internal,
     *         {@code false} otherwise
     */
    public boolean isInternal() {
        return internal;
    }

    /**
     * Parses a {@link FetchGroupIsInternalMexResponse} from the raw JSON
     * bytes of the {@code <result>} child.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the envelope is missing expected fields
     */
    private static Optional<FetchGroupIsInternalMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_group_query_by_id");
        if (root == null) {
            return Optional.empty();
        }

        // The "properties" sub-object is shared by all four group inline fragments
        var properties = root.getJSONObject("properties");
        if (properties == null) {
            return Optional.of(new FetchGroupIsInternalMexResponse(false));
        }

        // ((t = n.xwa2_group_query_by_id) == null || (t = t.properties) == null ? void 0 : t.internal) === true
        var internal = Boolean.TRUE.equals(properties.getBoolean("internal"));

        return Optional.of(new FetchGroupIsInternalMexResponse(internal));
    }
}
