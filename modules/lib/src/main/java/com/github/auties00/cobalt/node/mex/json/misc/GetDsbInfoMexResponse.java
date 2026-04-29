package com.github.auties00.cobalt.node.mex.json.misc;

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
public final class GetDsbInfoMexResponse implements MexOperation.Response.Json {
    private final String referenceNumber;

    private GetDsbInfoMexResponse(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexGetDsbInfoJob.mexGetDsbInfo: reads
     * {@code xwa2_get_dsb_info.reference_number} from the GraphQL
     * payload returned by {@code WAWebMexClient.fetchQuery}.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetDsbInfoJob", exports = "mexGetDsbInfo",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<GetDsbInfoMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(GetDsbInfoMexResponse::of);
    }

    /**
     * Returns the {@code reference_number} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> referenceNumber() {
        return Optional.ofNullable(referenceNumber);
    }

    private static Optional<GetDsbInfoMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_get_dsb_info");
        if (root == null) {
            return Optional.empty();
        }

        var referenceNumber = root.getString("reference_number");

        return Optional.of(new GetDsbInfoMexResponse(referenceNumber));
    }
}
