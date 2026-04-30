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
 * Parsed response for the fetch-all-subgroups query. Carries the community's default subgroup record together with
 * the list of regular subgroups projected from {@code data.xwa2_group_query_by_id}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchAllSubgroupsJob")
public final class FetchAllSubgroupsMexResponse implements MexOperation.Response.Json {
    /**
     * The community group identifier returned by the relay.
     */
    private final String id;

    /**
     * The community's default subgroup record.
     */
    private final DefaultSubGroup defaultSubGroup;

    /**
     * The list of regular subgroups under the community.
     */
    private final SubGroups subGroups;

    /**
     * Constructs a new response with the given fields.
     *
     * @param id the community group identifier
     * @param defaultSubGroup the default subgroup record
     * @param subGroups the regular subgroups list
     */
    private FetchAllSubgroupsMexResponse(String id, DefaultSubGroup defaultSubGroup, SubGroups subGroups) {
        this.id = id;
        this.defaultSubGroup = defaultSubGroup;
        this.subGroups = subGroups;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@link Optional#empty()} if the expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchAllSubgroupsJob", exports = "mexFetchAllSubgroups",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchAllSubgroupsMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchAllSubgroupsMexResponse::of);
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
     * Returns the community's default subgroup record.
     *
     * @return an {@link Optional} containing the record, or empty if absent
     */
    public Optional<DefaultSubGroup> defaultSubGroup() {
        return Optional.ofNullable(defaultSubGroup);
    }

    /**
     * Returns the list of regular subgroups under the community.
     *
     * @return an {@link Optional} containing the subgroup list, or empty if absent
     */
    public Optional<SubGroups> subGroups() {
        return Optional.ofNullable(subGroups);
    }

    /**
     * Default subgroup record for a community. Carries the subgroup identifier and its subject metadata.
     */
    public static final class DefaultSubGroup {
        /**
         * The default subgroup identifier.
         */
        private final String id;

        /**
         * The default subgroup subject metadata.
         */
        private final Subject subject;

        /**
         * Constructs a new default-subgroup record.
         *
         * @param id the subgroup identifier
         * @param subject the subject metadata
         */
        private DefaultSubGroup(String id, Subject subject) {
            this.id = id;
            this.subject = subject;
        }

        /**
         * Returns the default subgroup identifier.
         *
         * @return an {@link Optional} containing the identifier, or empty if absent
         */
        public Optional<String> id() {
            return Optional.ofNullable(id);
        }

        /**
         * Returns the default subgroup subject metadata.
         *
         * @return an {@link Optional} containing the subject, or empty if absent
         */
        public Optional<Subject> subject() {
            return Optional.ofNullable(subject);
        }

        /**
         * Subject metadata for a subgroup. Captures the subject value and the creation timestamp.
         */
        public static final class Subject {
            /**
             * The subject text value.
             */
            private final String value;

            /**
             * The subject creation epoch-second timestamp.
             */
            private final Long creationTime;

            /**
             * Constructs a new subject record.
             *
             * @param value the subject text value
             * @param creationTime the creation epoch-second timestamp
             */
            private Subject(String value, Long creationTime) {
                this.value = value;
                this.creationTime = creationTime;
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
             * Returns the subject creation timestamp.
             *
             * @return an {@link Optional} containing the {@link Instant}, or empty if absent
             */
            public Optional<Instant> creationTime() {
                return Optional.ofNullable(creationTime).map(Instant::ofEpochSecond);
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
                var creationTime = obj.getLong("creation_time");
                return Optional.of(new Subject(value, creationTime));
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
         * Parses a default-subgroup record from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed record, or empty if {@code obj} is {@code null}
         */
        static Optional<DefaultSubGroup> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var id = obj.getString("id");
            var subject = Subject.of(obj.getJSONObject("subject")).orElse(null);
            return Optional.of(new DefaultSubGroup(id, subject));
        }

        /**
         * Parses a list of default-subgroup records from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed records, empty if {@code arr} is {@code null}
         */
        static List<DefaultSubGroup> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<DefaultSubGroup>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }

    /**
     * Subgroups list. Wraps the {@code edges} array of regular subgroups under a community.
     */
    public static final class SubGroups {
        /**
         * The subgroup edges returned by the relay.
         */
        private final List<Edges> edges;

        /**
         * Constructs a new subgroups list.
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
         * Single edge wrapper around a subgroup node, mirroring the GraphQL connection pattern.
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
             * Subgroup node. Captures the subgroup identifier together with subject metadata, group properties and the
             * pending membership approval requests counter.
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
                 * The subgroup properties (general chat, approval mode, hidden state).
                 */
                private final Properties properties;

                /**
                 * The pending membership approval request counter.
                 */
                private final MembershipApprovalRequests membershipApprovalRequests;

                /**
                 * Constructs a new subgroup node.
                 *
                 * @param id the subgroup identifier
                 * @param subject the subject metadata
                 * @param properties the subgroup properties
                 * @param membershipApprovalRequests the pending membership approval requests counter
                 */
                private Node(String id, Subject subject, Properties properties, MembershipApprovalRequests membershipApprovalRequests) {
                    this.id = id;
                    this.subject = subject;
                    this.properties = properties;
                    this.membershipApprovalRequests = membershipApprovalRequests;
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
                 * Returns the subgroup properties.
                 *
                 * @return an {@link Optional} containing the properties, or empty if absent
                 */
                public Optional<Properties> properties() {
                    return Optional.ofNullable(properties);
                }

                /**
                 * Returns the pending membership approval requests counter.
                 *
                 * @return an {@link Optional} containing the counter record, or empty if absent
                 */
                public Optional<MembershipApprovalRequests> membershipApprovalRequests() {
                    return Optional.ofNullable(membershipApprovalRequests);
                }

                /**
                 * Subject metadata for a subgroup.
                 */
                public static final class Subject {
                    /**
                     * The subject text value.
                     */
                    private final String value;

                    /**
                     * The subject creation epoch-second timestamp.
                     */
                    private final Long creationTime;

                    /**
                     * Constructs a new subject record.
                     *
                     * @param value the subject text value
                     * @param creationTime the creation epoch-second timestamp
                     */
                    private Subject(String value, Long creationTime) {
                        this.value = value;
                        this.creationTime = creationTime;
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
                     * Returns the subject creation timestamp.
                     *
                     * @return an {@link Optional} containing the {@link Instant}, or empty if absent
                     */
                    public Optional<Instant> creationTime() {
                        return Optional.ofNullable(creationTime).map(Instant::ofEpochSecond);
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
                        var creationTime = obj.getLong("creation_time");
                        return Optional.of(new Subject(value, creationTime));
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
                 * Subgroup property flags. Captures the general-chat tag, the membership approval mode flag and the
                 * hidden-group state.
                 */
                public static final class Properties {
                    /**
                     * The general-chat tag for the subgroup.
                     */
                    private final String generalChat;

                    /**
                     * Whether membership approval mode is enabled.
                     */
                    private final Boolean membershipApprovalModeEnabled;

                    /**
                     * The hidden-group state tag.
                     */
                    private final String hiddenGroup;

                    /**
                     * Constructs a new properties record.
                     *
                     * @param generalChat the general-chat tag
                     * @param membershipApprovalModeEnabled whether approval mode is enabled
                     * @param hiddenGroup the hidden-group state tag
                     */
                    private Properties(String generalChat, Boolean membershipApprovalModeEnabled, String hiddenGroup) {
                        this.generalChat = generalChat;
                        this.membershipApprovalModeEnabled = membershipApprovalModeEnabled;
                        this.hiddenGroup = hiddenGroup;
                    }

                    /**
                     * Returns the general-chat tag for the subgroup.
                     *
                     * @return an {@link Optional} containing the tag, or empty if absent
                     */
                    public Optional<String> generalChat() {
                        return Optional.ofNullable(generalChat);
                    }

                    /**
                     * Returns whether membership approval mode is enabled for this subgroup.
                     *
                     * @return {@code true} when the flag is present and set, {@code false} otherwise
                     */
                    public boolean membershipApprovalModeEnabled() {
                        return membershipApprovalModeEnabled != null && membershipApprovalModeEnabled;
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
                     * Parses a properties record from the given JSON object.
                     *
                     * @param obj the JSON object to parse
                     * @return an {@link Optional} containing the parsed record, or empty if {@code obj} is {@code null}
                     */
                    static Optional<Properties> of(JSONObject obj) {
                        if (obj == null) {
                            return Optional.empty();
                        }

                        var generalChat = obj.getString("general_chat");
                        var membershipApprovalModeEnabled = obj.getBoolean("membership_approval_mode_enabled");
                        var hiddenGroup = obj.getString("hidden_group");
                        return Optional.of(new Properties(generalChat, membershipApprovalModeEnabled, hiddenGroup));
                    }

                    /**
                     * Parses a list of properties records from the given JSON array.
                     *
                     * @param arr the JSON array to parse
                     * @return the list of parsed records, empty if {@code arr} is {@code null}
                     */
                    static List<Properties> ofArray(JSONArray arr) {
                        if (arr == null) {
                            return List.of();
                        }

                        var result = new ArrayList<Properties>(arr.size());
                        for (var i = 0; i < arr.size(); i++) {
                            of(arr.getJSONObject(i)).ifPresent(result::add);
                        }
                        return result;
                    }
                }

                /**
                 * Counter record for pending membership approval requests.
                 */
                public static final class MembershipApprovalRequests {
                    /**
                     * The number of pending approval requests.
                     */
                    private final Long totalCount;

                    /**
                     * Constructs a new counter record.
                     *
                     * @param totalCount the number of pending approval requests
                     */
                    private MembershipApprovalRequests(Long totalCount) {
                        this.totalCount = totalCount;
                    }

                    /**
                     * Returns the number of pending approval requests.
                     *
                     * @return an {@link OptionalLong} containing the count, or empty if absent
                     */
                    public OptionalLong totalCount() {
                        return totalCount != null ? OptionalLong.of(totalCount) : OptionalLong.empty();
                    }

                    /**
                     * Parses a counter record from the given JSON object.
                     *
                     * @param obj the JSON object to parse
                     * @return an {@link Optional} containing the parsed record, or empty if {@code obj} is {@code null}
                     */
                    static Optional<MembershipApprovalRequests> of(JSONObject obj) {
                        if (obj == null) {
                            return Optional.empty();
                        }

                        var totalCount = obj.getLong("total_count");
                        return Optional.of(new MembershipApprovalRequests(totalCount));
                    }

                    /**
                     * Parses a list of counter records from the given JSON array.
                     *
                     * @param arr the JSON array to parse
                     * @return the list of parsed records, empty if {@code arr} is {@code null}
                     */
                    static List<MembershipApprovalRequests> ofArray(JSONArray arr) {
                        if (arr == null) {
                            return List.of();
                        }

                        var result = new ArrayList<MembershipApprovalRequests>(arr.size());
                        for (var i = 0; i < arr.size(); i++) {
                            of(arr.getJSONObject(i)).ifPresent(result::add);
                        }
                        return result;
                    }
                }

                /**
                 * Parses a subgroup node from the given JSON object.
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
                    var properties = Properties.of(obj.getJSONObject("properties")).orElse(null);
                    var membershipApprovalRequests = MembershipApprovalRequests.of(obj.getJSONObject("membership_approval_requests")).orElse(null);
                    return Optional.of(new Node(id, subject, properties, membershipApprovalRequests));
                }

                /**
                 * Parses a list of subgroup nodes from the given JSON array.
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
         * Parses a subgroups list from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed list, or empty if {@code obj} is {@code null}
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
    private static Optional<FetchAllSubgroupsMexResponse> of(byte[] json) {
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
        var defaultSubGroup = DefaultSubGroup.of(root.getJSONObject("default_sub_group")).orElse(null);
        var subGroups = SubGroups.of(root.getJSONObject("sub_groups")).orElse(null);

        return Optional.of(new FetchAllSubgroupsMexResponse(id, defaultSubGroup, subGroups));
    }
}
