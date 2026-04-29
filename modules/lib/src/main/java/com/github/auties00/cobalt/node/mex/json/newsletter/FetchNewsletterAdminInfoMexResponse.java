package com.github.auties00.cobalt.node.mex.json.newsletter;

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
import java.util.OptionalLong;

/**
 * The response variant of {@link FetchNewsletterAdminInfoMexResponse} that exposes the data
 * returned by the server after a successful query.
 *
 * @implNote WAWebMexFetchNewsletterAdminInfoJob: adapts the JSON root returned by the GraphQL
 * query into a Java value object.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterAdminInfoJob")
public final class FetchNewsletterAdminInfoMexResponse implements MexOperation.Response.Json {
    private final Long adminCount;
    private final String id;

    private FetchNewsletterAdminInfoMexResponse(Long adminCount, String id) {
        this.adminCount = adminCount;
        this.id = id;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexFetchNewsletterAdminInfoJob.mexFetchNewsletterAdminInfo: WA Web relies on the
     * GraphQL client to unwrap the response. Cobalt performs the
     * unwrapping manually from the IQ {@code <result>} child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<FetchNewsletterAdminInfoMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchNewsletterAdminInfoMexResponse::of);
    }

    /**
     * Returns the {@code admin_count} field.
     *
     * @return an {@link OptionalLong} containing the value, or empty if absent
     */
    public OptionalLong adminCount() {
        return adminCount != null ? OptionalLong.of(adminCount) : OptionalLong.empty();
    }

    /**
     * Returns the {@code id} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Parses a {@link FetchNewsletterAdminInfoMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexFetchNewsletterAdminInfoJob.mexFetchNewsletterAdminInfo: mirrors the implicit
     * unwrapping that WA Web performs on the GraphQL response,
     * extracting the {@code xwa2_newsletter_admin} root.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<FetchNewsletterAdminInfoMexResponse> of(byte[] json) {
        // WAWebMexFetchNewsletterAdminInfoJob.mexFetchNewsletterAdminInfo
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexFetchNewsletterAdminInfoJob.mexFetchNewsletterAdminInfo
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexFetchNewsletterAdminInfoJob.mexFetchNewsletterAdminInfo
        // Extracts the operation-specific root keyed by xwa2_newsletter_admin
        var root = data.getJSONObject("xwa2_newsletter_admin");
        if (root == null) {
            return Optional.empty();
        }

        var adminCount = root.getLong("admin_count");
        var id = root.getString("id");

        return Optional.of(new FetchNewsletterAdminInfoMexResponse(adminCount, id));
    }
}
