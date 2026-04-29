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
 * The parsed response for this MEX query.
 */
public final class FetchSubgroupSuggestionsMexResponse implements MexOperation.Response.Json {
    private final String id;
    private final SubGroupSuggestions subGroupSuggestions;

    private FetchSubgroupSuggestionsMexResponse(String id, SubGroupSuggestions subGroupSuggestions) {
        this.id = id;
        this.subGroupSuggestions = subGroupSuggestions;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexFetchSubgroupSuggestionsJob.mexFetchSubgroupSuggestions:
     * extracts the {@code <result>} child and decodes its JSON bytes.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchSubgroupSuggestionsJob", exports = "mexFetchSubgroupSuggestions",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchSubgroupSuggestionsMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchSubgroupSuggestionsMexResponse::of);
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
     * Returns the {@code sub_group_suggestions} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<SubGroupSuggestions> subGroupSuggestions() {
        return Optional.ofNullable(subGroupSuggestions);
    }

    /**
     * A parsed {@code SubGroupSuggestions} object.
     */
    public static final class SubGroupSuggestions {
        private final List<Edges> edges;

        private SubGroupSuggestions(List<Edges> edges) {
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
                private final Subject subject;
                private final Description description;
                private final Creator creator;
                private final Long creationTime;
                private final Long totalParticipantsCount;
                private final Boolean isExistingGroup;
                private final String hiddenGroup;

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
                 * Returns the {@code id} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<String> id() {
                    return Optional.ofNullable(id);
                }

                /**
                 * Returns the {@code subject} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<Subject> subject() {
                    return Optional.ofNullable(subject);
                }

                /**
                 * Returns the {@code description} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<Description> description() {
                    return Optional.ofNullable(description);
                }

                /**
                 * Returns the {@code creator} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<Creator> creator() {
                    return Optional.ofNullable(creator);
                }

                /**
                 * Returns the {@code creation_time} field.
                 *
                 * @return an {@link Optional} containing the value as an {@link Instant}, or empty if absent
                 */
                public Optional<Instant> creationTime() {
                    return Optional.ofNullable(creationTime).map(Instant::ofEpochSecond);
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
                 * Returns the {@code is_existing_group} field.
                 *
                 * @return {@code true} if the value is present and true, {@code false} otherwise
                 */
                public boolean isExistingGroup() {
                    return isExistingGroup != null && isExistingGroup;
                }

                /**
                 * Returns the {@code hidden_group} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<String> hiddenGroup() {
                    return Optional.ofNullable(hiddenGroup);
                }

                /**
                 * A parsed {@code Subject} object.
                 */
                public static final class Subject {
                    private final String value;

                    private Subject(String value) {
                        this.value = value;
                    }

                    /**
                     * Returns the {@code value} field.
                     *
                     * @return an {@link Optional} containing the value, or empty if absent
                     */
                    public Optional<String> value() {
                        return Optional.ofNullable(value);
                    }

                    /**
                     * Parses a {@code Subject} from the given JSON object.
                     *
                     * @param obj the JSON object to parse
                     * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
                     */
                    static Optional<Subject> of(JSONObject obj) {
                        if (obj == null) {
                            return Optional.empty();
                        }

                        var value = obj.getString("value");
                        return Optional.of(new Subject(value));
                    }

                    /**
                     * Parses a list of {@code Subject} from the given JSON array.
                     *
                     * @param arr the JSON array to parse
                     * @return the list of parsed results, empty if {@code arr} is {@code null}
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
                 * A parsed {@code Description} object.
                 */
                public static final class Description {
                    private final String value;
                    private final String id;

                    private Description(String value, String id) {
                        this.value = value;
                        this.id = id;
                    }

                    /**
                     * Returns the {@code value} field.
                     *
                     * @return an {@link Optional} containing the value, or empty if absent
                     */
                    public Optional<String> value() {
                        return Optional.ofNullable(value);
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
                     * Parses a {@code Description} from the given JSON object.
                     *
                     * @param obj the JSON object to parse
                     * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
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
                     * Parses a list of {@code Description} from the given JSON array.
                     *
                     * @param arr the JSON array to parse
                     * @return the list of parsed results, empty if {@code arr} is {@code null}
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
                 * A parsed {@code Creator} object.
                 */
                public static final class Creator {
                    private final String id;

                    private Creator(String id) {
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
                     * Parses a {@code Creator} from the given JSON object.
                     *
                     * @param obj the JSON object to parse
                     * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
                     */
                    static Optional<Creator> of(JSONObject obj) {
                        if (obj == null) {
                            return Optional.empty();
                        }

                        var id = obj.getString("id");
                        return Optional.of(new Creator(id));
                    }

                    /**
                     * Parses a list of {@code Creator} from the given JSON array.
                     *
                     * @param arr the JSON array to parse
                     * @return the list of parsed results, empty if {@code arr} is {@code null}
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
         * Parses a {@code SubGroupSuggestions} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<SubGroupSuggestions> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var edges = Edges.ofArray(obj.getJSONArray("edges"));
            return Optional.of(new SubGroupSuggestions(edges));
        }

        /**
         * Parses a list of {@code SubGroupSuggestions} from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
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
