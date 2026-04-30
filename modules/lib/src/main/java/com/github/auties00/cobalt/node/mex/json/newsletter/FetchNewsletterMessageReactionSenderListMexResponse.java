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
import java.util.Objects;
import java.util.Optional;

/**
 * Response variant for {@link FetchNewsletterMessageReactionSenderListMexRequest} carrying the parsed server reply.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterMessageReactionSenderListJob")
public final class FetchNewsletterMessageReactionSenderListMexResponse implements MexOperation.Response.Json {
    private final List<Reactions> reactions;

    private FetchNewsletterMessageReactionSenderListMexResponse(List<Reactions> reactions) {
        this.reactions = reactions;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<FetchNewsletterMessageReactionSenderListMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchNewsletterMessageReactionSenderListMexResponse::of);
    }

    /**
     * Returns the {@code reactions} field.
     *
     * @return the list of values, empty if absent
     */
    public List<Reactions> reactions() {
        return reactions;
    }

    /**
     * A parsed {@code Reactions} object.
     */
    public static final class Reactions {
        private final String reactionCode;
        private final SenderList senderList;

        private Reactions(String reactionCode, SenderList senderList) {
            this.reactionCode = reactionCode;
            this.senderList = senderList;
        }

        /**
         * Returns the {@code reaction_code} field.
         *
     * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> reactionCode() {
            return Optional.ofNullable(reactionCode);
        }

        /**
         * Returns the {@code sender_list} field.
         *
     * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<SenderList> senderList() {
            return Optional.ofNullable(senderList);
        }

        /**
         * A parsed {@code SenderList} object.
         */
        public static final class SenderList {
            private final List<Edges> edges;

            private SenderList(List<Edges> edges) {
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
                    private final String profilePicDirectPath;

                    private Node(String id, String profilePicDirectPath) {
                        this.id = id;
                        this.profilePicDirectPath = profilePicDirectPath;
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
                     * Returns the {@code profile_pic_direct_path} field.
                     *
     * @return an {@link Optional} containing the value, or empty if absent
                     */
                    public Optional<String> profilePicDirectPath() {
                        return Optional.ofNullable(profilePicDirectPath);
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
                        var profilePicDirectPath = obj.getString("profile_pic_direct_path");
                        return Optional.of(new Node(id, profilePicDirectPath));
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
             * Parses a {@code SenderList} from the given JSON object.
             *
     * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<SenderList> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var edges = Edges.ofArray(obj.getJSONArray("edges"));
                return Optional.of(new SenderList(edges));
            }

            /**
             * Parses a list of {@code SenderList} from the given JSON array.
             *
     * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<SenderList> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<SenderList>(arr.size());
                for (var i = 0; i < arr.size(); i++) {
                    of(arr.getJSONObject(i)).ifPresent(result::add);
                }
                return result;
            }
        }

        /**
         * Parses a {@code Reactions} from the given JSON object.
         *
     * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<Reactions> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var reactionCode = obj.getString("reaction_code");
            var senderList = SenderList.of(obj.getJSONObject("sender_list")).orElse(null);
            return Optional.of(new Reactions(reactionCode, senderList));
        }

        /**
         * Parses a list of {@code Reactions} from the given JSON array.
         *
     * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
         */
        static List<Reactions> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<Reactions>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }

    /**
     * Parses a {@link FetchNewsletterMessageReactionSenderListMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<FetchNewsletterMessageReactionSenderListMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_newsletters_reaction_sender_list");
        if (root == null) {
            return Optional.empty();
        }

        var reactions = Reactions.ofArray(root.getJSONArray("reactions"));

        return Optional.of(new FetchNewsletterMessageReactionSenderListMexResponse(reactions));
    }
}
