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
 * The parsed response for this MEX mutation.
 */
public final class UpdateTextStatusMexResponse implements MexOperation.Response.Json {
    private final String result;

    private UpdateTextStatusMexResponse(String result) {
        this.result = result;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexUpdateTextStatusJob.mexUpdateTextStatus: reads
     * the {@code result} status from
     * {@code data.xwa2_update_text_status}.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUpdateTextStatusJob", exports = "mexUpdateTextStatus",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<UpdateTextStatusMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(UpdateTextStatusMexResponse::of);
    }

    /**
     * Returns the {@code result} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> result() {
        return Optional.ofNullable(result);
    }

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
