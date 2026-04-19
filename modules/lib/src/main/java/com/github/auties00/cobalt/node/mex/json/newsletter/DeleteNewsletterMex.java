package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.mex.json.MexJsonOperation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Permanently deletes a newsletter owned by the authenticated user.
 *
 * <p>Deleting a newsletter is an irreversible action that removes the
 * channel, its messages and its subscriber list from the WhatsApp servers.
 * Only the current owner may issue the deletion; all followers stop
 * receiving updates as soon as the mutation succeeds.
 *
 * <p>The request carries the newsletter identifier and the response echoes
 * the identifier together with the transitional {@code state} object that
 * indicates the newsletter has moved into a terminal deleted state.
 *
 * @implNote WAWebMexDeleteNewsletterJob: adapts the
 * {@code mexDeleteNewsletter} GraphQL mutation, which in WA Web is invoked
 * via {@code WAWebMexClient.fetchQuery} with a single {@code newsletter_id}
 * variable and unwraps the {@code xwa2_newsletter_delete_v2} root of the
 * response.
 */
@WhatsAppWebModule(moduleName = "WAWebMexDeleteNewsletterJob")
public sealed interface DeleteNewsletterMex extends MexJsonOperation permits DeleteNewsletterMex.Request, DeleteNewsletterMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay to
     * the {@code DeleteNewsletter} compiled mutation.
     *
     * @implNote WAWebMexDeleteNewsletterJobMutation.graphql: corresponds to
     * the compiled document id registered for the
     * {@code mexDeleteNewsletter} mutation.
     */
    String QUERY_ID = "30062808666639665";

    /**
     * The request variant of {@link DeleteNewsletterMex} that serialises the
     * mutation variables and emits the outbound IQ stanza.
     *
     * @implNote WAWebMexDeleteNewsletterJob.mexDeleteNewsletter: adapts the
     * {@code variables} object constructed inline inside the JS
     * {@code fetchQuery} call.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexDeleteNewsletterJob")
    final class Request implements DeleteNewsletterMex {
        /**
         * The identifier of the newsletter being deleted.
         *
         * @implNote WAWebMexDeleteNewsletterJob.mexDeleteNewsletter: mirrors
         * the {@code newsletter_id} variable passed to {@code fetchQuery}.
         */
        @WhatsAppWebExport(moduleName = "WAWebMexDeleteNewsletterJob", exports = "mexDeleteNewsletter",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final String newsletterId;

        /**
         * Creates a request targeting the given newsletter for deletion.
         *
         * @implNote WAWebMexDeleteNewsletterJob.mexDeleteNewsletter: mirrors
         * the single positional parameter of the JS function.
         * @param newsletterId the identifier of the newsletter to delete
         */
        @WhatsAppWebExport(moduleName = "WAWebMexDeleteNewsletterJob", exports = "mexDeleteNewsletter",
                adaptation = WhatsAppAdaptation.DIRECT)
        public Request(String newsletterId) {
            this.newsletterId = newsletterId;
        }

        /**
         * Builds the IQ stanza that dispatches this deletion mutation to the
         * WhatsApp relay.
         *
         * @implNote WAWebMexDeleteNewsletterJob.mexDeleteNewsletter: WA Web
         * constructs the {@code variables} object inline and delegates to
         * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
         * via {@code fastjson2.JSONWriter} and wraps it through
         * {@link MexJsonOperation#createMexNode(String, String)}.
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables
         */
        @WhatsAppWebExport(moduleName = "WAWebMexDeleteNewsletterJob", exports = "mexDeleteNewsletter",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public NodeBuilder toNode() {
            // WAWebMexDeleteNewsletterJob.mexDeleteNewsletter
            // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
            try (var writer = JSONWriter.ofUTF8()) {
                // WAWebMexDeleteNewsletterJob.mexDeleteNewsletter
                // Begins the outer envelope and the nested "variables" object
                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();

                // WAWebMexDeleteNewsletterJob.mexDeleteNewsletter
                // Emits the newsletter_id variable when present, skipping it otherwise
                if (newsletterId != null) {
                    writer.writeName("newsletter_id");
                    writer.writeColon();
                    writer.writeString(newsletterId);
                }
                writer.endObject();
                writer.endObject();

                // ADAPTED: WAWebMexDeleteNewsletterJob.mexDeleteNewsletter
                // Flushes the JSON into a StringWriter and wraps it in the shared MEX IQ envelope
                try (var output = new StringWriter()) {
                    writer.flushTo(output);
                    return MexJsonOperation.createMexNode(QUERY_ID, output.toString());
                }
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
    }

    /**
     * The response variant of {@link DeleteNewsletterMex} exposing the
     * newsletter identifier and its post-deletion state.
     *
     * @implNote WAWebMexDeleteNewsletterJob: adapts the
     * {@code xwa2_newsletter_delete_v2} root of the JSON response.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexDeleteNewsletterJob")
    final class Response implements DeleteNewsletterMex {
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
        private Response(String id, State state) {
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
        public static Optional<Response> of(Node node) {
            return node.getChild("result")
                    .flatMap(Node::toContentBytes)
                    .flatMap(Response::of);
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
         * Parses a {@link Response} from the raw JSON bytes of the
         * {@code <result>} child.
         *
         * @implNote WAWebMexDeleteNewsletterJob.mexDeleteNewsletter: mirrors
         * the implicit unwrapping that WA Web performs on the GraphQL
         * response, extracting the {@code xwa2_newsletter_delete_v2} root.
         * @param json the UTF-8 encoded JSON payload
         * @return an {@link Optional} containing the parsed response, or
         *         empty if the envelope is missing expected fields
         */
        private static Optional<Response> of(byte[] json) {
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

            return Optional.of(new Response(id, state));
        }
    }
}
