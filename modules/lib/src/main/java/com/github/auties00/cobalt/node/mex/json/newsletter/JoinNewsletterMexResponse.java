package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
 * Response variant for {@link JoinNewsletterMexRequest} carrying the parsed server reply.
 */
@WhatsAppWebModule(moduleName = "WAWebMexJoinNewsletterJob")
public final class JoinNewsletterMexResponse implements MexOperation.Response.Json {
    private final String id;
    private final State state;

    private JoinNewsletterMexResponse(String id, State state) {
        this.id = id;
        this.state = state;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<JoinNewsletterMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(JoinNewsletterMexResponse::of);
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
     * Returns the {@code state} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<State> state() {
        return Optional.ofNullable(state);
    }

    /**
     * A parsed {@code State} object.
     */
    public static final class State {
        private final String type;

        private State(String type) {
            this.type = type;
        }

        /**
         * Returns the {@code type} field.
         *
     * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> type() {
            return Optional.ofNullable(type);
        }

        /**
         * Parses a {@code State} from the given JSON object.
         *
     * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<State> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var type = obj.getString("type");
            return Optional.of(new State(type));
        }

        /**
         * Parses a list of {@code State} from the given JSON array.
         *
     * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
         */
        static List<State> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<State>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }

    /**
     * Parses a {@link JoinNewsletterMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<JoinNewsletterMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_newsletter_join_v2");
        if (root == null) {
            return Optional.empty();
        }

        var id = root.getString("id");
        var state = State.of(root.getJSONObject("state")).orElse(null);

        return Optional.of(new JoinNewsletterMexResponse(id, state));
    }
}
