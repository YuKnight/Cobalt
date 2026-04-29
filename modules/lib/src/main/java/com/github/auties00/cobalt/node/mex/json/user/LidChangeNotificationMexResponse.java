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
 * The parsed response for this MEX query.
 */
public final class LidChangeNotificationMexResponse implements MexOperation.Response.Json {
    private final String oldValue;
    private final String newValue;

    private LidChangeNotificationMexResponse(String oldValue, String newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexLidChangeNotificationQuery.graphql: the compiled
     * Relay {@code selections} array selects the {@code XWA2LidChange}
     * linked field {@code xwa2_notify_lid_change} carrying the scalar
     * children {@code old} and {@code new}; this method extracts both
     * straight from the JSON payload of the {@code <result>} child.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexLidChangeNotificationQuery.graphql", exports = "selections",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<LidChangeNotificationMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(LidChangeNotificationMexResponse::of);
    }

    /**
     * Returns the previous LID value carried by the GraphQL response.
     *
     * @implNote WAWebMexLidChangeNotificationQuery.graphql: corresponds to
     * the scalar selection {@code selections[0].selections[0].name = "old"}
     * inside the {@code xwa2_notify_lid_change} linked field.
     * @return an {@link Optional} containing the value, or empty if absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexLidChangeNotificationQuery.graphql", exports = "selections",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Optional<String> oldValue() {
        return Optional.ofNullable(oldValue);
    }

    /**
     * Returns the new LID value carried by the GraphQL response.
     *
     * @implNote WAWebMexLidChangeNotificationQuery.graphql: corresponds to
     * the scalar selection {@code selections[0].selections[1].name = "new"}
     * inside the {@code xwa2_notify_lid_change} linked field. Renamed to
     * {@code newValue()} to avoid clashing with the Java {@code new}
     * keyword.
     * @return an {@link Optional} containing the value, or empty if absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexLidChangeNotificationQuery.graphql", exports = "selections",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public Optional<String> newValue() {
        return Optional.ofNullable(newValue);
    }

    private static Optional<LidChangeNotificationMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_notify_lid_change");
        if (root == null) {
            return Optional.empty();
        }

        var oldValue = root.getString("old");
        var newValue = root.getString("new");

        return Optional.of(new LidChangeNotificationMexResponse(oldValue, newValue));
    }
}
