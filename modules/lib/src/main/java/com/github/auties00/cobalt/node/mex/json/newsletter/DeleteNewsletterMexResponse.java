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
 * The response variant of {@link DeleteNewsletterMexResponse} exposing the
 * newsletter identifier and its post-deletion state.
 *
 * @implNote WAWebMexDeleteNewsletterJob: adapts the
 * {@code xwa2_newsletter_delete_v2} root of the JSON response.
 */
@WhatsAppWebModule(moduleName = "WAWebMexDeleteNewsletterJob")
public final class DeleteNewsletterMexResponse implements MexOperation.Response.Json {
    /**
     * The identifier of the deleted newsletter, as echoed by the server.
     */
    private final String id;

    /**
     * The state object returned by the server after the deletion, which
     * carries the terminal status type.
     */
    private final State state;

    /**
     * Creates a response carrying the deleted newsletter identifier and
     * its post-deletion state.
     *
     * @param id the newsletter identifier echoed by the server
     * @param state the state object describing the deletion outcome
     */
    private DeleteNewsletterMexResponse(String id, State state) {
        this.id = id;
        this.state = state;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexDeleteNewsletterJob.mexDeleteNewsletter: WA Web
     * relies on the GraphQL client to unwrap the response. Cobalt
     * performs the unwrapping manually from the IQ {@code <result>} child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<DeleteNewsletterMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(DeleteNewsletterMexResponse::of);
    }

    /**
     * Returns the identifier of the deleted newsletter.
     *
     * @return an {@link Optional} containing the identifier, or empty if
     *         the server did not echo it back
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Returns the state object carrying the newsletter's post-deletion
     * status.
     *
     * @return an {@link Optional} containing the state, or empty if the
     *         server omitted it
     */
    public Optional<State> state() {
        return Optional.ofNullable(state);
    }

    /**
     * The state object nested under {@code xwa2_newsletter_delete_v2}
     * describing the terminal newsletter state after deletion.
     */
    public static final class State {
        /**
         * The textual state identifier. After deletion this is typically
         * the terminal {@code GONE} state used by the WhatsApp backend to
         * mark removed channels.
         */
        private final String type;

        /**
         * Creates a state object wrapping the textual type.
         *
         * @param type the raw state identifier returned by the server
         */
        private State(String type) {
            this.type = type;
        }

        /**
         * Returns the textual state identifier.
         *
         * @return an {@link Optional} containing the type, or empty if
         *         absent from the server reply
         */
        public Optional<String> type() {
            return Optional.ofNullable(type);
        }

        /**
         * Parses a single {@link State} object from the given JSON map.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed state, or
         *         empty if {@code obj} is {@code null}
         */
        static Optional<State> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var type = obj.getString("type");
            return Optional.of(new State(type));
        }

        /**
         * Parses a list of {@link State} objects from the given JSON
         * array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed states, empty if {@code arr} is
         *         {@code null}
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
     * Parses a {@link DeleteNewsletterMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexDeleteNewsletterJob.mexDeleteNewsletter: mirrors
     * the implicit unwrapping that WA Web performs on the GraphQL
     * response, extracting the {@code xwa2_newsletter_delete_v2} root.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<DeleteNewsletterMexResponse> of(byte[] json) {
        // WAWebMexDeleteNewsletterJob.mexDeleteNewsletter
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexDeleteNewsletterJob.mexDeleteNewsletter
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexDeleteNewsletterJob.mexDeleteNewsletter
        // Extracts the mutation-specific root keyed by xwa2_newsletter_delete_v2
        var root = data.getJSONObject("xwa2_newsletter_delete_v2");
        if (root == null) {
            return Optional.empty();
        }

        var id = root.getString("id");
        var state = State.of(root.getJSONObject("state")).orElse(null);

        return Optional.of(new DeleteNewsletterMexResponse(id, state));
    }
}
