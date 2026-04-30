package com.github.auties00.cobalt.node.mex.json.community;

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
import java.util.OptionalLong;

/**
 * Parsed response for the subgroup participant-count query. Carries the per-subgroup participant counts projected from
 * {@code data.xwa2_group_query_by_id.sub_groups.edges}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexQuerySubgroupParticipantCountJob")
public final class QuerySubgroupParticipantCountMexResponse implements MexOperation.Response.Json {
    /**
     * The subgroup edges container returned by the relay.
     */
    private final SubGroups subGroups;

    /**
     * The community group identifier returned by the relay.
     */
    private final String id;

    /**
     * Constructs a new response with the given fields.
     *
     * @param subGroups the subgroup edges container
     * @param id the community group identifier
     */
    private QuerySubgroupParticipantCountMexResponse(SubGroups subGroups, String id) {
        this.subGroups = subGroups;
        this.id = id;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@link Optional#empty()} if the expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexQuerySubgroupParticipantCountJob", exports = "mexQuerySubgroupParticipantCountJob",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<QuerySubgroupParticipantCountMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(QuerySubgroupParticipantCountMexResponse::of);
    }

    /**
     * Returns the subgroup edges container.
     *
     * @return an {@link Optional} containing the container, or empty if absent
     */
    public Optional<SubGroups> subGroups() {
        return Optional.ofNullable(subGroups);
    }

    /**
     * Returns the community group identifier.
     *
     * @return an {@link Optional} containing the identifier, or empty if absent
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Subgroup edges container. Wraps the array of subgroup nodes carrying participant counts.
     */
    public static final class SubGroups {
        /**
         * The subgroup edges returned by the relay.
         */
        private final List<Edges> edges;

        /**
         * Constructs a new container with the given edges.
         *
         * @param edges the subgroup edges
         */
        private SubGroups(List<Edges> edges) {
            this.edges = edges;
        }

        /**
         * Returns the subgroup edges.
         *
         * @return the list of edges, empty if absent
         */
        public List<Edges> edges() {
            return edges;
        }

        /**
         * Single edge wrapper around a subgroup participant-count node.
         */
        public static final class Edges {
            /**
             * The subgroup node carried by the edge.
             */
            private final Node node;

            /**
             * Constructs a new edge wrapping the given node.
             *
             * @param node the subgroup node
             */
            private Edges(Node node) {
                this.node = node;
            }

            /**
             * Returns the subgroup node carried by this edge.
             *
             * @return an {@link Optional} containing the node, or empty if absent
             */
            public Optional<Node> node() {
                return Optional.ofNullable(node);
            }

            /**
             * Subgroup participant-count node. Captures the subgroup identifier and the total participant count.
             */
            public static final class Node {
                /**
                 * The subgroup identifier.
                 */
                private final String id;

                /**
                 * The total participant count for the subgroup.
                 */
                private final Long totalParticipantsCount;

                /**
                 * Constructs a new node.
                 *
                 * @param id the subgroup identifier
                 * @param totalParticipantsCount the total participant count
                 */
                private Node(String id, Long totalParticipantsCount) {
                    this.id = id;
                    this.totalParticipantsCount = totalParticipantsCount;
                }

                /**
                 * Returns the subgroup identifier.
                 *
                 * @return an {@link Optional} containing the identifier, or empty if absent
                 */
                public Optional<String> id() {
                    return Optional.ofNullable(id);
                }

                /**
                 * Returns the total participant count for this subgroup.
                 *
                 * @return an {@link OptionalLong} containing the count, or empty if absent
                 */
                public OptionalLong totalParticipantsCount() {
                    return totalParticipantsCount != null ? OptionalLong.of(totalParticipantsCount) : OptionalLong.empty();
                }

                /**
                 * Parses a participant-count node from the given JSON object.
                 *
                 * @param obj the JSON object to parse
                 * @return an {@link Optional} containing the parsed node, or empty if {@code obj} is {@code null}
                 */
                static Optional<Node> of(JSONObject obj) {
                    if (obj == null) {
                        return Optional.empty();
                    }

                    var id = obj.getString("id");
                    var totalParticipantsCount = obj.getLong("total_participants_count");
                    return Optional.of(new Node(id, totalParticipantsCount));
                }

                /**
                 * Parses a list of participant-count nodes from the given JSON array.
                 *
                 * @param arr the JSON array to parse
                 * @return the list of parsed nodes, empty if {@code arr} is {@code null}
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
             * Parses an edge wrapper from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed edge, or empty if {@code obj} is {@code null}
             */
            static Optional<Edges> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var node = Node.of(obj.getJSONObject("node")).orElse(null);
                return Optional.of(new Edges(node));
            }

            /**
             * Parses a list of edge wrappers from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed edges, empty if {@code arr} is {@code null}
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
         * Parses a subgroups container from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed container, or empty if {@code obj} is {@code null}
         */
        static Optional<SubGroups> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var edges = Edges.ofArray(obj.getJSONArray("edges"));
            return Optional.of(new SubGroups(edges));
        }

        /**
         * Parses a list of subgroups containers from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed containers, empty if {@code arr} is {@code null}
         */
        static List<SubGroups> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<SubGroups>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }

    /**
     * Parses the response from the raw JSON payload bytes.
     *
     * @param json the raw JSON bytes from the {@code <result>} child
     * @return an {@link Optional} containing the parsed response, or empty if the envelope is missing
     */
    private static Optional<QuerySubgroupParticipantCountMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_group_query_by_id");
        if (root == null) {
            return Optional.empty();
        }

        var subGroups = SubGroups.of(root.getJSONObject("sub_groups")).orElse(null);
        var id = root.getString("id");

        return Optional.of(new QuerySubgroupParticipantCountMexResponse(subGroups, id));
    }
}
