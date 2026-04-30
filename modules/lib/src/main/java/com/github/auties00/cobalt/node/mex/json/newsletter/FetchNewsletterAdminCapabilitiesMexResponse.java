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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Response variant for {@link FetchNewsletterAdminCapabilitiesMexRequest} carrying the parsed server reply.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterAdminCapabilitiesJob")
public final class FetchNewsletterAdminCapabilitiesMexResponse implements MexOperation.Response.Json {
    private final List<String> capabilities;

    private FetchNewsletterAdminCapabilitiesMexResponse(List<String> capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<FetchNewsletterAdminCapabilitiesMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchNewsletterAdminCapabilitiesMexResponse::of);
    }

    /**
     * Returns the raw newsletter capability values granted to the
     * authenticated admin.
     *
     * @return an unmodifiable {@link List} of capability identifiers; never
     *         {@code null} but possibly empty
     */
    public List<String> capabilities() {
        return capabilities;
    }

    /**
     * Parses a {@link FetchNewsletterAdminCapabilitiesMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<FetchNewsletterAdminCapabilitiesMexResponse> of(byte[] json) {
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

        var capabilitiesArray = root.getJSONArray("capabilities");
        var capabilities = new ArrayList<String>();
        if (capabilitiesArray != null) {
            for (var i = 0; i < capabilitiesArray.size(); i++) {
                var value = capabilitiesArray.getString(i);
                if (value != null) {
                    capabilities.add(value);
                }
            }
        }

        return Optional.of(new FetchNewsletterAdminCapabilitiesMexResponse(List.copyOf(capabilities)));
    }
}
