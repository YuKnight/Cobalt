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
 * Response variant for {@link LogNewsletterExposuresMexRequest} carrying the parsed server reply.
 */
@WhatsAppWebModule(moduleName = "WAWebMexLogNewsletterExposuresJob")
public final class LogNewsletterExposuresMexResponse implements MexOperation.Response.Json {

    /**
     * Constructs an empty {@link LogNewsletterExposuresMexResponse}.
     */
    private LogNewsletterExposuresMexResponse() {
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
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
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<LogNewsletterExposuresMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.get("xwa2_newsletter_log_exposures");
        if (root == null) {
            return Optional.empty();
        }

        return Optional.of(new LogNewsletterExposuresMexResponse());
    }
}
