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
 * Parsed response for the LID-change notification query. Carries the previous and current linked-identity values
 * projected from {@code data.xwa2_notify_lid_change}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexLidChangeNotification")
public final class LidChangeNotificationMexResponse implements MexOperation.Response.Json {
    /**
     * The previous LID value reported by the relay.
     */
    private final String oldValue;

    /**
     * The new LID value reported by the relay.
     */
    private final String newValue;

    /**
     * Constructs a new response with the given old and new LID values.
     *
     * @param oldValue the previous LID value
     * @param newValue the new LID value
     */
    private LidChangeNotificationMexResponse(String oldValue, String newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@link Optional#empty()} if the expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexLidChangeNotification", exports = "parseLidChangeNotification",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<LidChangeNotificationMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(LidChangeNotificationMexResponse::of);
    }

    /**
     * Returns the previous LID value reported by the relay.
     *
     * @return an {@link Optional} containing the previous LID, or empty if absent
     */
    public Optional<String> oldValue() {
        return Optional.ofNullable(oldValue);
    }

    /**
     * Returns the new LID value reported by the relay.
     *
     * @apiNote The accessor is named {@code newValue()} to avoid clashing with the Java {@code new} keyword.
     * @return an {@link Optional} containing the new LID, or empty if absent
     */
    public Optional<String> newValue() {
        return Optional.ofNullable(newValue);
    }

    /**
     * Parses the response from the raw JSON payload bytes.
     *
     * @param json the raw JSON bytes from the {@code <result>} child
     * @return an {@link Optional} containing the parsed response, or empty if the envelope is missing
     */
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
