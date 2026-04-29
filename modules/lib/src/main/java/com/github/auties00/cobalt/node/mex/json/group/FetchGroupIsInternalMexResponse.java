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
 * The response variant of {@link FetchGroupIsInternalMexResponse} that exposes
 * the data returned by the server after a successful query.
 *
 * @implNote WAWebMexFetchGroupIsInternalJob: adapts the JSON root
 * returned by the GraphQL query into a Java value object. WA Web's
 * {@code mexFetchGroupIsInternal} unwraps the response by reading
 * {@code n.xwa2_group_query_by_id?.properties?.internal === true};
 * Cobalt mirrors the same triple-strict-equality projection.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchGroupIsInternalJob")
public final class FetchGroupIsInternalMexResponse implements MexOperation.Response.Json {
    private final boolean internal;

    private FetchGroupIsInternalMexResponse(boolean internal) {
        this.internal = internal;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal:
     * WA Web relies on the GraphQL client to unwrap the response.
     * Cobalt performs the unwrapping manually from the IQ
     * {@code <result>} child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
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
     * @implNote WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal:
     * mirrors WA Web's strict-equality projection
     * {@code properties?.internal === true}, which collapses
     * {@code null}, {@code undefined} and explicit {@code false} to
     * {@code false}.
     * @return {@code true} if the relay reports the group as internal,
     *         {@code false} otherwise
     */
    public boolean isInternal() {
        return internal;
    }

    /**
     * Parses a {@link FetchGroupIsInternalMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal:
     * mirrors the implicit unwrapping that WA Web performs on the
     * GraphQL response, extracting the
     * {@code xwa2_group_query_by_id.properties.internal} scalar.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<FetchGroupIsInternalMexResponse> of(byte[] json) {
        // WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal
        // Extracts the operation-specific root keyed by xwa2_group_query_by_id
        var root = data.getJSONObject("xwa2_group_query_by_id");
        if (root == null) {
            return Optional.empty();
        }

        // WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal
        // Walks into the "properties" sub-object shared by all four group inline fragments
        var properties = root.getJSONObject("properties");
        if (properties == null) {
            return Optional.of(new FetchGroupIsInternalMexResponse(false));
        }

        // WAWebMexFetchGroupIsInternalJob.mexFetchGroupIsInternal
        // ((t = n.xwa2_group_query_by_id) == null || (t = t.properties) == null ? void 0 : t.internal) === true
        var internal = Boolean.TRUE.equals(properties.getBoolean("internal"));

        return Optional.of(new FetchGroupIsInternalMexResponse(internal));
    }
}
