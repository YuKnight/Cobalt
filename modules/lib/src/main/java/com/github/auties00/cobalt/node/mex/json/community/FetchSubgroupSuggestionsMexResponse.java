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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Parsed response for the fetch-subgroup-suggestions query. Carries the per-community list of suggested subgroups
 * projected from {@code data.xwa2_group_query_by_id.sub_group_suggestions}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchSubgroupSuggestionsJob")
public final class FetchSubgroupSuggestionsMexResponse implements MexOperation.Response.Json {
    /**
     * The community group identifier returned by the relay.
     */
    private final String id;

    /**
     * The subgroup suggestions container returned by the relay.
     */
    private final SubGroupSuggestions subGroupSuggestions;

    /**
     * Constructs a new response with the given fields.
     *
     * @param id the community group identifier
     * @param subGroupSuggestions the subgroup suggestions container
     */
    private FetchSubgroupSuggestionsMexResponse(String id, SubGroupSuggestions subGroupSuggestions) {
        this.id = id;
        this.subGroupSuggestions = subGroupSuggestions;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@link Optional#empty()} if the expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchSubgroupSuggestionsJob", exports = "mexFetchSubgroupSuggestions",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchSubgroupSuggestionsMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchSubgroupSuggestionsMexResponse::of);
    }

    /**
     * Returns the community group identifier returned by the relay.
     *
     * @return an {@link Optional} containing the identifier, or empty if absent
     */
    public Optional<String> id() {
        return Optional.ofNullable(id);
    }

    /**
     * Returns the subgroup suggestions container.
     *
     * @return an {@link Optional} containing the container, or empty if absent
     */
    public Optional<SubGroupSuggestions> subGroupSuggestions() {
        return Optional.ofNullable(subGroupSuggestions);
    }

    /**
     * Subgroup suggestions container. Wraps the {@code edges} array carrying the suggested subgroup nodes.
     */
    public static final class SubGroupSuggestions {
        /**
         * The suggestion edges returned by the relay.
         */
        private final List<Edges> edges;

        /**
         * Constructs a new suggestions container with the given edges.
         *
         * @param edges the suggestion edges
         */
        private SubGroupSuggestions(List<Edges> edges) {
            this.edges = edges;
        }

        /**
         * Returns the suggestion edges.
         *
         * @return the list of edges, empty if absent
         */
        public List<Edges> edges() {
            return edges;
        }

        /**
         * Single edge wrapper around a suggested subgroup node.
         */
        public static final class Edges {
            /**
             * The suggested subgroup node carried by the edge.
             */
            private final Node node;

            /**
             * Constructs a new edge wrapping the given node.
             *
             * @param node the suggested subgroup node
             */
            private Edges(Node node) {
                this.node = node;
            }

            /**
             * Returns the suggested subgroup node.
             *
             * @return an {@link Optional} containing the node, or empty if absent
             */
            public Optional<Node> node() {
                return Optional.ofNullable(node);
            }

            /**
             * Suggested subgroup node. Captures the subgroup identifier together with its subject, description,
             * creator, creation timestamp, participant count, existing-group flag and hidden-group state.
             */
            public static final class Node {
                /**
                 * The subgroup identifier.
                 */
                private final String id;

                /**
                 * The subgroup subject metadata.
                 */
                private final Subject subject;

                /**
                 * The subgroup description metadata.
                 */
                private final Description description;

                /**
                 * The subgroup creator metadata.
                 */
                private final Creator creator;

                /**
                 * The subgroup creation epoch-second timestamp.
                 */
                private final Long creationTime;

                /**
                 * The total participant count for the subgroup.
                 */
                private final Long totalParticipantsCount;

                /**
                 * Whether the suggested subgroup is already an existing group the user is part of.
                 */
                private final Boolean isExistingGroup;

                /**
                 * The hidden-group state tag.
                 */
                private final String hiddenGroup;

                /**
                 * Constructs a new suggested subgroup node.
                 *
                 * @param id the subgroup identifier
                 * @param subject the subject metadata
                 * @param description the description metadata
                 * @param creator the creator metadata
                 * @param creationTime the creation epoch-second timestamp
                 * @param totalParticipantsCount the total participant count
                 * @param isExistingGroup whether the user is already a participant
                 * @param hiddenGroup the hidden-group state tag
                 */
                private Node(String id, Subject subject, Description description, Creator creator, Long creationTime, Long totalParticipantsCount, Boolean isExistingGroup, String hiddenGroup) {
                    this.id = id;
                    this.subject = subject;
                    this.description = description;
                    this.creator = creator;
                    this.creationTime = creationTime;
                    this.totalParticipantsCount = totalParticipantsCount;
                    this.isExistingGroup = isExistingGroup;
                    this.hiddenGroup = hiddenGroup;
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
                 * Returns the subgroup subject metadata.
                 *
                 * @return an {@link Optional} containing the subject, or empty if absent
                 */
                public Optional<Subject> subject() {
                    return Optional.ofNullable(subject);
                }

                /**
                 * Returns the subgroup description metadata.
                 *
                 * @return an {@link Optional} containing the description, or empty if absent
                 */
                public Optional<Description> description() {
                    return Optional.ofNullable(description);
                }

                /**
                 * Returns the subgroup creator metadata.
                 *
                 * @return an {@link Optional} containing the creator, or empty if absent
                 */
                public Optional<Creator> creator() {
                    return Optional.ofNullable(creator);
                }

                /**
                 * Returns the subgroup creation timestamp.
                 *
                 * @return an {@link Optional} containing the {@link Instant}, or empty if absent
                 */
                public Optional<Instant> creationTime() {
                    return Optional.ofNullable(creationTime).map(Instant::ofEpochSecond);
                }

                /**
                 * Returns the total participant count for the subgroup.
                 *
                 * @return an {@link OptionalLong} containing the count, or empty if absent
                 */
                public OptionalLong totalParticipantsCount() {
                    return totalParticipantsCount != null ? OptionalLong.of(totalParticipantsCount) : OptionalLong.empty();
                }

                /**
                 * Returns whether the suggested subgroup is already an existing group the user is part of.
                 *
                 * @return {@code true} when the flag is present and set, {@code false} otherwise
                 */
                public boolean isExistingGroup() {
                    return isExistingGroup != null && isExistingGroup;
                }

                /**
                 * Returns the hidden-group state tag.
                 *
                 * @return an {@link Optional} containing the tag, or empty if absent
                 */
                public Optional<String> hiddenGroup() {
                    return Optional.ofNullable(hiddenGroup);
                }

                /**
                 * Subject metadata for a suggested subgroup. Carries the subject text value.
                 */
                public static final class Subject {
                    /**
                     * The subject text value.
                     */
                    private final String value;

                    /**
                     * Constructs a new subject record.
                     *
                     * @param value the subject text value
                     */
                    private Subject(String value) {
                        this.value = value;
                    }

                    /**
                     * Returns the subject text value.
                     *
                     * @return an {@link Optional} containing the value, or empty if absent
                     */
                    public Optional<String> value() {
                        return Optional.ofNullable(value);
                    }

                    /**
                     * Parses a subject record from the given JSON object.
                     *
                     * @param obj the JSON object to parse
                     * @return an {@link Optional} containing the parsed record, or empty if {@code obj} is {@code null}
                     */
                    static Optional<Subject> of(JSONObject obj) {
                        if (obj == null) {
                            return Optional.empty();
                        }

                        var value = obj.getString("value");
                        return Optional.of(new Subject(value));
                    }

                    /**
                     * Parses a list of subject records from the given JSON array.
                     *
                     * @param arr the JSON array to parse
                     * @return the list of parsed records, empty if {@code arr} is {@code null}
                     */
                    static List<Subject> ofArray(JSONArray arr) {
                        if (arr == null) {
                            return List.of();
                        }

                        var result = new ArrayList<Subject>(arr.size());
                        for (var i = 0; i < arr.size(); i++) {
                            of(arr.getJSONObject(i)).ifPresent(result::add);
                        }
                        return result;
                    }
                }

                /**
                 * Description metadata for a suggested subgroup.
                 */
                public static final class Description {
                    /**
                     * The description text value.
                     */
                    private final String value;

                    /**
                     * The description metadata identifier.
                     */
                    private final String id;

                    /**
                     * Constructs a new description record.
                     *
                     * @param value the description text value
                     * @param id the description metadata identifier
                     */
                    private Description(String value, String id) {
                        this.value = value;
                        this.id = id;
                    }

                    /**
                     * Returns the description text value.
                     *
                     * @return an {@link Optional} containing the value, or empty if absent
                     */
                    public Optional<String> value() {
                        return Optional.ofNullable(value);
                    }

                    /**
                     * Returns the description metadata identifier.
                     *
                     * @return an {@link Optional} containing the identifier, or empty if absent
                     */
                    public Optional<String> id() {
                        return Optional.ofNullable(id);
                    }

                    /**
                     * Parses a description record from the given JSON object.
                     *
                     * @param obj the JSON object to parse
                     * @return an {@link Optional} containing the parsed record, or empty if {@code obj} is {@code null}
                     */
                    static Optional<Description> of(JSONObject obj) {
                        if (obj == null) {
                            return Optional.empty();
                        }

                        var value = obj.getString("value");
                        var id = obj.getString("id");
                        return Optional.of(new Description(value, id));
                    }

                    /**
                     * Parses a list of description records from the given JSON array.
                     *
                     * @param arr the JSON array to parse
                     * @return the list of parsed records, empty if {@code arr} is {@code null}
                     */
                    static List<Description> ofArray(JSONArray arr) {
                        if (arr == null) {
                            return List.of();
                        }

                        var result = new ArrayList<Description>(arr.size());
                        for (var i = 0; i < arr.size(); i++) {
                            of(arr.getJSONObject(i)).ifPresent(result::add);
                        }
                        return result;
                    }
                }

                /**
                 * Creator metadata for a suggested subgroup.
                 */
                public static final class Creator {
                    /**
                     * The creator identifier.
                     */
                    private final String id;

                    /**
                     * Constructs a new creator record.
                     *
                     * @param id the creator identifier
                     */
                    private Creator(String id) {
                        this.id = id;
                    }

                    /**
                     * Returns the creator identifier.
                     *
                     * @return an {@link Optional} containing the identifier, or empty if absent
                     */
                    public Optional<String> id() {
                        return Optional.ofNullable(id);
                    }

                    /**
                     * Parses a creator record from the given JSON object.
                     *
                     * @param obj the JSON object to parse
                     * @return an {@link Optional} containing the parsed record, or empty if {@code obj} is {@code null}
                     */
                    static Optional<Creator> of(JSONObject obj) {
                        if (obj == null) {
                            return Optional.empty();
                        }

                        var id = obj.getString("id");
                        return Optional.of(new Creator(id));
                    }

                    /**
                     * Parses a list of creator records from the given JSON array.
                     *
                     * @param arr the JSON array to parse
                     * @return the list of parsed records, empty if {@code arr} is {@code null}
                     */
                    static List<Creator> ofArray(JSONArray arr) {
                        if (arr == null) {
                            return List.of();
                        }

                        var result = new ArrayList<Creator>(arr.size());
                        for (var i = 0; i < arr.size(); i++) {
                            of(arr.getJSONObject(i)).ifPresent(result::add);
                        }
                        return result;
                    }
                }

                /**
                 * Parses a suggested subgroup node from the given JSON object.
                 *
                 * @param obj the JSON object to parse
                 * @return an {@link Optional} containing the parsed node, or empty if {@code obj} is {@code null}
                 */
                static Optional<Node> of(JSONObject obj) {
                    if (obj == null) {
                        return Optional.empty();
                    }

                    var id = obj.getString("id");
                    var subject = Subject.of(obj.getJSONObject("subject")).orElse(null);
                    var description = Description.of(obj.getJSONObject("description")).orElse(null);
                    var creator = Creator.of(obj.getJSONObject("creator")).orElse(null);
                    var creationTime = obj.getLong("creation_time");
                    var totalParticipantsCount = obj.getLong("total_participants_count");
                    var isExistingGroup = obj.getBoolean("is_existing_group");
                    var hiddenGroup = obj.getString("hidden_group");
                    return Optional.of(new Node(id, subject, description, creator, creationTime, totalParticipantsCount, isExistingGroup, hiddenGroup));
                }

                /**
                 * Parses a list of suggested subgroup nodes from the given JSON array.
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
         * Parses a suggestions container from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed container, or empty if {@code obj} is {@code null}
         */
        static Optional<SubGroupSuggestions> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var edges = Edges.ofArray(obj.getJSONArray("edges"));
            return Optional.of(new SubGroupSuggestions(edges));
        }

        /**
         * Parses a list of suggestions containers from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed containers, empty if {@code arr} is {@code null}
         */
        static List<SubGroupSuggestions> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<SubGroupSuggestions>(arr.size());
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
    private static Optional<FetchSubgroupSuggestionsMexResponse> of(byte[] json) {
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

        var id = root.getString("id");
        var subGroupSuggestions = SubGroupSuggestions.of(root.getJSONObject("sub_group_suggestions")).orElse(null);

        return Optional.of(new FetchSubgroupSuggestionsMexResponse(id, subGroupSuggestions));
    }
}
