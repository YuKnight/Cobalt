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
 * Response variant for {@link FetchNewsletterAdminInfoMexRequest} carrying the parsed server reply.
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
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<FetchNewsletterAdminInfoMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_newsletter_admin");
        if (root == null) {
            return Optional.empty();
        }

        var adminCount = root.getLong("admin_count");
        var id = root.getString("id");

        return Optional.of(new FetchNewsletterAdminInfoMexResponse(adminCount, id));
    }
}
