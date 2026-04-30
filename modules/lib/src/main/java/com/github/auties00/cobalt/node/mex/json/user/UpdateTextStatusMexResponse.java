package com.github.auties00.cobalt.node.mex.json.user;

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
 * Parsed response for the update-text-status mutation. Carries the relay's status token from
 * {@code data.xwa2_update_text_status}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexUpdateTextStatusJob")
public final class UpdateTextStatusMexResponse implements MexOperation.Response.Json {
    /**
     * The status token reported by the relay after the mutation runs.
     */
    private final String result;

    /**
     * Constructs a new response with the given status token.
     *
     * @param result the status token reported by the relay
     */
    private UpdateTextStatusMexResponse(String result) {
        this.result = result;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@link Optional#empty()} if the expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUpdateTextStatusJob", exports = "mexUpdateTextStatus",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<UpdateTextStatusMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(UpdateTextStatusMexResponse::of);
    }

    /**
     * Returns the status token reported by the relay.
     *
     * @return an {@link Optional} containing the status token, or empty if absent
     */
    public Optional<String> result() {
        return Optional.ofNullable(result);
    }

    /**
     * Parses the response from the raw JSON payload bytes.
     *
     * @param json the raw JSON bytes from the {@code <result>} child
     * @return an {@link Optional} containing the parsed response, or empty if the envelope is missing
     */
    private static Optional<UpdateTextStatusMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_update_text_status");
        if (root == null) {
            return Optional.empty();
        }

        var result = root.getString("result");

        return Optional.of(new UpdateTextStatusMexResponse(result));
    }
}
