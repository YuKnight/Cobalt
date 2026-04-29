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
 * The parsed response for this MEX query.
 */
public final class QuerySubgroupParticipantCountMexResponse implements MexOperation.Response.Json {
    private final SubGroups subGroups;
    private final String id;

    private QuerySubgroupParticipantCountMexResponse(SubGroups subGroups, String id) {
        this.subGroups = subGroups;
        this.id = id;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexQuerySubgroupParticipantCountJobQuery.graphql:
     * extracts the {@code <result>} child and decodes its JSON bytes.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexQuerySubgroupParticipantCountJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<QuerySubgroupParticipantCountMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(QuerySubgroupParticipantCountMexResponse::of);
    }

    /**
     * Returns the {@code sub_groups} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<SubGroups> subGroups() {
        return Optional.ofNullable(subGroups);
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
     * A parsed {@code SubGroups} object.
     */
    public static final class SubGroups {
        private final List<Edges> edges;

        private SubGroups(List<Edges> edges) {
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

            private Edges(Node node) {
                this.node = node;
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
                private final Long totalParticipantsCount;

                private Node(String id, Long totalParticipantsCount) {
                    this.id = id;
                    this.totalParticipantsCount = totalParticipantsCount;
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
                 * Returns the {@code total_participants_count} field.
                 *
                 * @return an {@link OptionalLong} containing the value, or empty if absent
                 */
                public OptionalLong totalParticipantsCount() {
                    return totalParticipantsCount != null ? OptionalLong.of(totalParticipantsCount) : OptionalLong.empty();
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
                    var totalParticipantsCount = obj.getLong("total_participants_count");
                    return Optional.of(new Node(id, totalParticipantsCount));
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
                return Optional.of(new Edges(node));
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
         * Parses a {@code SubGroups} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<SubGroups> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var edges = Edges.ofArray(obj.getJSONArray("edges"));
            return Optional.of(new SubGroups(edges));
        }

        /**
         * Parses a list of {@code SubGroups} from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
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
