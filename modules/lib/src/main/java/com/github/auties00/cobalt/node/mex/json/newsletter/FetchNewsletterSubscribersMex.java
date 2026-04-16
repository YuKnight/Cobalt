package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.node.mex.json.MexJsonOperation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Fetches a paginated list of subscribers for a given newsletter.
 *
 * <p>This query returns the subscriber list for a newsletter including subscribe time and role information. Unlike the WA Web follower query, this endpoint keys its response under xwa2_newsletter_subscribers and has no direct counterpart in the public WA Web source.
 */
public sealed interface FetchNewsletterSubscribersMex extends MexJsonOperation permits FetchNewsletterSubscribersMex.Request, FetchNewsletterSubscribersMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterSubscribers} compiled query.
     */
    String QUERY_ID = "9537574256318798";

    /**
     * The request variant of {@link FetchNewsletterSubscribersMex} that serialises the
     * query variables and emits the outbound IQ stanza.
     */
    final class Request implements FetchNewsletterSubscribersMex {
        private final String input;

        public Request(String input) {
            this.input = input;
        }

        /**
         * Builds the IQ stanza that dispatches this operation to the
         * WhatsApp relay.
         *
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables
         */
        public NodeBuilder toNode() {
            // Builds the JSON variables envelope
            // Opens a UTF-8 JSON writer to serialise the GraphQL variables map

            try (var writer = JSONWriter.ofUTF8()) {
                // Begins the outer envelope and the nested "variables" object expected by the relay

                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();
                if (input != null) {
                    writer.writeName("input");
                    writer.writeColon();
                    writer.writeString(input);
                }
                writer.endObject();
                writer.endObject();
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
     * The response variant of {@link FetchNewsletterSubscribersMex} that exposes the data
     * returned by the server after a successful query.
     */
    final class Response implements FetchNewsletterSubscribersMex {
        private final Subscribers subscribers;

        private Response(Subscribers subscribers) {
            this.subscribers = subscribers;
        }

        /**
         * Parses a MEX response from the given IQ response node.
         *
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
         * Returns the {@code subscribers} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<Subscribers> subscribers() {
            return Optional.ofNullable(subscribers);
        }

        /**
         * A parsed {@code Subscribers} object.
         */
        public static final class Subscribers {
            private final List<Edges> edges;

            private Subscribers(List<Edges> edges) {
                this.edges = edges;
            }

            /**
             * Returns the {@code edges} field.
             *
             * @return the list of values, empty if absent
             */
            public List<Edges> edges() {
                return edges;
            }

            /**
             * A parsed {@code Edges} object.
             */
            public static final class Edges {
                private final Node node;
                private final Long subscribeTime;
                private final String role;

                private Edges(Node node, Long subscribeTime, String role) {
                    this.node = node;
                    this.subscribeTime = subscribeTime;
                    this.role = role;
                }

                /**
                 * Returns the {@code node} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<Node> node() {
                    return Optional.ofNullable(node);
                }

                /**
                 * Returns the {@code subscribe_time} field.
                 *
                 * @return an {@link Optional} containing the value as an {@link Instant}, or empty if absent
                 */
                public Optional<Instant> subscribeTime() {
                    return Optional.ofNullable(subscribeTime).map(Instant::ofEpochSecond);
                }

                /**
                 * Returns the {@code role} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<String> role() {
                    return Optional.ofNullable(role);
                }

                /**
                 * A parsed {@code Node} object.
                 */
                public static final class Node {
                    private final String id;
                    private final String displayName;
                    private final String pn;

                    private Node(String id, String displayName, String pn) {
                        this.id = id;
                        this.displayName = displayName;
                        this.pn = pn;
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
                     * Returns the {@code display_name} field.
                     *
                     * @return an {@link Optional} containing the value, or empty if absent
                     */
                    public Optional<String> displayName() {
                        return Optional.ofNullable(displayName);
                    }

                    /**
                     * Returns the {@code pn} field.
                     *
                     * @return an {@link Optional} containing the value, or empty if absent
                     */
                    public Optional<String> pn() {
                        return Optional.ofNullable(pn);
                    }

                    /**
                     * Parses a {@code Node} from the given JSON object.
                     *
                     * @param obj the JSON object to parse
                     * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
                     */
                    static Optional<Node> of(JSONObject obj) {
                        if (obj == null) {
                            return Optional.empty();
                        }

                        var id = obj.getString("id");
                        var displayName = obj.getString("display_name");
                        var pn = obj.getString("pn");
                        return Optional.of(new Node(id, displayName, pn));
                    }

                    /**
                     * Parses a list of {@code Node} from the given JSON array.
                     *
                     * @param arr the JSON array to parse
                     * @return the list of parsed results, empty if {@code arr} is {@code null}
                     */
                    static List<Node> ofArray(JSONArray arr) {
                        if (arr == null) {
                            return List.of();
                        }

                        var result = new ArrayList<Node>(arr.size());
                        for (var i = 0; i < arr.size(); i++) {
                            of(arr.getJSONObject(i)).ifPresent(result::add);
                        }
                        return result;
                    }
                }

                /**
                 * Parses a {@code Edges} from the given JSON object.
                 *
                 * @param obj the JSON object to parse
                 * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
                 */
                static Optional<Edges> of(JSONObject obj) {
                    if (obj == null) {
                        return Optional.empty();
                    }

                    var node = Node.of(obj.getJSONObject("node")).orElse(null);
                    var subscribeTime = obj.getLong("subscribe_time");
                    var role = obj.getString("role");
                    return Optional.of(new Edges(node, subscribeTime, role));
                }

                /**
                 * Parses a list of {@code Edges} from the given JSON array.
                 *
                 * @param arr the JSON array to parse
                 * @return the list of parsed results, empty if {@code arr} is {@code null}
                 */
                static List<Edges> ofArray(JSONArray arr) {
                    if (arr == null) {
                        return List.of();
                    }

                    var result = new ArrayList<Edges>(arr.size());
                    for (var i = 0; i < arr.size(); i++) {
                        of(arr.getJSONObject(i)).ifPresent(result::add);
                    }
                    return result;
                }
            }

            /**
             * Parses a {@code Subscribers} from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<Subscribers> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var edges = Edges.ofArray(obj.getJSONArray("edges"));
                return Optional.of(new Subscribers(edges));
            }

            /**
             * Parses a list of {@code Subscribers} from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<Subscribers> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<Subscribers>(arr.size());
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
         * @param json the UTF-8 encoded JSON payload
         * @return an {@link Optional} containing the parsed response, or
         *         empty if the envelope is missing expected fields
         */
        private static Optional<Response> of(byte[] json) {
            // Parses the raw JSON payload and bails out if fastjson2 returns null

            var jsonObject = JSON.parseObject(json);
            if (jsonObject == null) {
                return Optional.empty();
            }

            var data = jsonObject.getJSONObject("data");
            if (data == null) {
                return Optional.empty();
            }

            var root = data.getJSONObject("xwa2_newsletter_subscribers");
            if (root == null) {
                return Optional.empty();
            }

            var subscribers = Subscribers.of(root.getJSONObject("subscribers")).orElse(null);

            return Optional.of(new Response(subscribers));
        }
    }
}
