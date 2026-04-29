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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The response variant of {@link LogNewsletterExposuresMexResponse} that exposes the data
 * returned by the server after a successful mutation.
 *
 * @implNote WAWebMexLogNewsletterExposuresJob: adapts the JSON root returned by the GraphQL
 * mutation into a Java value object. The compiled GraphQL artifact only
 * selects {@code __typename} on the {@code xwa2_newsletter_log_exposures}
 * field, so success is signalled by the mere presence of that root.
 */
@WhatsAppWebModule(moduleName = "WAWebMexLogNewsletterExposuresJob")
public final class LogNewsletterExposuresMexResponse implements MexOperation.Response.Json {

    /**
     * Constructs an empty {@link LogNewsletterExposuresMexResponse}.
     *
     * @implNote WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures: WA Web ignores the
     * response payload entirely (the function does not even {@code return}
     * the awaited value). Cobalt still exposes a {@link LogNewsletterExposuresMexResponse}
     * placeholder so callers can pattern-match on the sealed hierarchy.
     */
    private LogNewsletterExposuresMexResponse() {
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures: WA Web relies on the
     * GraphQL client to unwrap the response. Cobalt performs the
     * unwrapping manually from the IQ {@code <result>} child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<LogNewsletterExposuresMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(LogNewsletterExposuresMexResponse::of);
    }

    /**
     * Parses a {@link LogNewsletterExposuresMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures: mirrors the implicit
     * unwrapping that WA Web performs on the GraphQL response,
     * extracting the {@code xwa2_newsletter_log_exposures} root which the
     * compiled GraphQL artifact populates on success with a single
     * {@code __typename} selection.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<LogNewsletterExposuresMexResponse> of(byte[] json) {
        // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexLogNewsletterExposuresJob.mexLogNewsletterExposures
        // Probes for the xwa2_newsletter_log_exposures marker so callers can confirm the batch was accepted
        var root = data.get("xwa2_newsletter_log_exposures");
        if (root == null) {
            return Optional.empty();
        }

        return Optional.of(new LogNewsletterExposuresMexResponse());
    }
}
