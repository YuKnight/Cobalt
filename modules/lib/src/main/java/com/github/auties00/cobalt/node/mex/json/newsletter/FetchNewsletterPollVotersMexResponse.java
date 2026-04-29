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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The response variant of {@link FetchNewsletterPollVotersMexResponse} that exposes the data
 * returned by the server after a successful query.
 *
 * @implNote WAWebMexFetchNewsletterPollVotersJob: adapts the JSON root returned by the GraphQL
 * query into a Java value object.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterPollVotersJob")
public final class FetchNewsletterPollVotersMexResponse implements MexOperation.Response.Json {
    private final List<Votes> votes;

    private FetchNewsletterPollVotersMexResponse(List<Votes> votes) {
        this.votes = votes;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexFetchNewsletterPollVotersJob.default: WA Web relies on the
     * GraphQL client to unwrap the response. Cobalt performs the
     * unwrapping manually from the IQ {@code <result>} child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<FetchNewsletterPollVotersMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchNewsletterPollVotersMexResponse::of);
    }

    /**
     * Returns the {@code votes} field.
     *
     * @return the list of values, empty if absent
     */
    public List<Votes> votes() {
        return votes;
    }

    /**
     * A parsed {@code Votes} object.
     */
    public static final class Votes {
        private final String voteHash;
        private final VoterList voterList;

        private Votes(String voteHash, VoterList voterList) {
            this.voteHash = voteHash;
            this.voterList = voterList;
        }

        /**
         * Returns the {@code vote_hash} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> voteHash() {
            return Optional.ofNullable(voteHash);
        }

        /**
         * Returns the {@code voter_list} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<VoterList> voterList() {
            return Optional.ofNullable(voterList);
        }

        /**
         * A parsed {@code VoterList} object.
         */
        public static final class VoterList {
            private final List<Edges> edges;

            private VoterList(List<Edges> edges) {
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
                private final Long actionTime;
                private final Node node;

                private Edges(Long actionTime, Node node) {
                    this.actionTime = actionTime;
                    this.node = node;
                }

                /**
                 * Returns the {@code action_time} field.
                 *
                 * @implNote WAWebMexFetchNewsletterPollVotersJob.default: WA Web's
                 * post-processor divides the raw value by {@code 1e6} and feeds the
                 * result to {@code WATimeUtils.castToUnixTime}, indicating that the
                 * GraphQL field is reported in microseconds.
                 * @return an {@link Optional} containing the value as an {@link Instant}, or empty if absent
                 */
                public Optional<Instant> actionTime() {
                    return Optional.ofNullable(actionTime)
                            .map(micros -> Instant.ofEpochSecond(micros / 1_000_000L));
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
                 * A parsed {@code Node} object.
                 */
                public static final class Node {
                    private final String id;

                    private Node(String id) {
                        this.id = id;
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
                        return Optional.of(new Node(id));
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

                    var actionTime = obj.getLong("action_time");
                    var node = Node.of(obj.getJSONObject("node")).orElse(null);
                    return Optional.of(new Edges(actionTime, node));
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
             * Parses a {@code VoterList} from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<VoterList> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var edges = Edges.ofArray(obj.getJSONArray("edges"));
                return Optional.of(new VoterList(edges));
            }

            /**
             * Parses a list of {@code VoterList} from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<VoterList> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<VoterList>(arr.size());
                for (var i = 0; i < arr.size(); i++) {
                    of(arr.getJSONObject(i)).ifPresent(result::add);
                }
                return result;
            }
        }

        /**
         * Parses a {@code Votes} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<Votes> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var voteHash = obj.getString("vote_hash");
            var voterList = VoterList.of(obj.getJSONObject("voter_list")).orElse(null);
            return Optional.of(new Votes(voteHash, voterList));
        }

        /**
         * Parses a list of {@code Votes} from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
         */
        static List<Votes> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<Votes>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }

    /**
     * Parses a {@link FetchNewsletterPollVotersMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexFetchNewsletterPollVotersJob.default: mirrors the implicit
     * unwrapping that WA Web performs on the GraphQL response,
     * extracting the {@code voter_list} root.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<FetchNewsletterPollVotersMexResponse> of(byte[] json) {
        // WAWebMexFetchNewsletterPollVotersJob.default
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexFetchNewsletterPollVotersJob.default
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexFetchNewsletterPollVotersJob.default
        // Extracts the operation-specific root keyed by voter_list
        var root = data.getJSONObject("voter_list");
        if (root == null) {
            return Optional.empty();
        }

        var votes = Votes.ofArray(root.getJSONArray("votes"));

        return Optional.of(new FetchNewsletterPollVotersMexResponse(votes));
    }
}
