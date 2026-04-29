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

/**
 * The response variant of {@link ChangeNewsletterOwnerMexResponse} exposing the
 * identifier echoed back by the server once the ownership change is
 * applied.
 *
 * @implNote WAWebMexChangeNewsletterOwnerJob: adapts the
 * {@code xwa2_newsletter_change_owner} root of the JSON response.
 */
@WhatsAppWebModule(moduleName = "WAWebMexChangeNewsletterOwnerJob")
public final class ChangeNewsletterOwnerMexResponse implements MexOperation.Response.Json {
    /**
     * The identifier of the newsletter whose owner was changed, as echoed
     * by the server.
     */
    private final String id;

    /**
     * Creates a response carrying the newsletter identifier returned by
     * the server.
     *
     * @param id the newsletter identifier echoed by the server
     */
    private ChangeNewsletterOwnerMexResponse(String id) {
        this.id = id;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
     * WA Web relies on the GraphQL client to unwrap the response. Cobalt
     * performs the unwrapping manually from the IQ {@code <result>} child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the node does not contain a well-formed result payload
     */
    public static Optional<ChangeNewsletterOwnerMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(ChangeNewsletterOwnerMexResponse::of);
    }

    /**
     * Returns the identifier of the newsletter whose owner was changed.
     *
     * @return an {@link Optional} containing the identifier, or empty if
     *         the server did not echo it back
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Parses a {@link ChangeNewsletterOwnerMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner:
     * mirrors the implicit unwrapping that WA Web performs on the GraphQL
     * response, extracting the {@code xwa2_newsletter_change_owner} root.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or empty
     *         if the envelope is missing expected fields
     */
    private static Optional<ChangeNewsletterOwnerMexResponse> of(byte[] json) {
        // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexChangeNewsletterOwnerJob.mexChangeNewsletterOwner
        // Extracts the mutation-specific root keyed by xwa2_newsletter_change_owner
        var root = data.getJSONObject("xwa2_newsletter_change_owner");
        if (root == null) {
            return Optional.empty();
        }

        var id = root.getString("id");

        return Optional.of(new ChangeNewsletterOwnerMexResponse(id));
    }
}
