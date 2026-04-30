package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
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
import java.util.OptionalLong;

/**
 * Response variant for {@link FetchNewsletterDehydratedMexRequest} carrying the parsed server reply.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterDehydratedJob")
public final class FetchNewsletterDehydratedMexResponse implements MexOperation.Response.Json {
    private final String id;
    private final ThreadMetadata threadMetadata;
    private final ViewerMetadata viewerMetadata;

    private FetchNewsletterDehydratedMexResponse(String id, ThreadMetadata threadMetadata, ViewerMetadata viewerMetadata) {
        this.id = id;
        this.threadMetadata = threadMetadata;
        this.viewerMetadata = viewerMetadata;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<FetchNewsletterDehydratedMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchNewsletterDehydratedMexResponse::of);
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
     * Returns the {@code thread_metadata} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<ThreadMetadata> threadMetadata() {
        return Optional.ofNullable(threadMetadata);
    }

    /**
     * Returns the {@code viewer_metadata} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<ViewerMetadata> viewerMetadata() {
        return Optional.ofNullable(viewerMetadata);
    }

    /**
     * A parsed {@code ThreadMetadata} object.
     */
    public static final class ThreadMetadata {
        private final Long subscribersCount;
        private final String verification;
        private final Settings settings;
        private final WamoSub wamoSub;

        private ThreadMetadata(Long subscribersCount, String verification, Settings settings, WamoSub wamoSub) {
            this.subscribersCount = subscribersCount;
            this.verification = verification;
            this.settings = settings;
            this.wamoSub = wamoSub;
        }

        /**
         * Returns the {@code subscribers_count} field.
         *
     * @return an {@link OptionalLong} containing the value, or empty if absent
         */
        public OptionalLong subscribersCount() {
            return subscribersCount != null ? OptionalLong.of(subscribersCount) : OptionalLong.empty();
        }

        /**
         * Returns the {@code verification} field.
         *
     * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> verification() {
            return Optional.ofNullable(verification);
        }

        /**
         * Returns the {@code settings} field.
         *
     * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<Settings> settings() {
            return Optional.ofNullable(settings);
        }

        /**
         * Returns the {@code wamo_sub} field.
         *
     * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<WamoSub> wamoSub() {
            return Optional.ofNullable(wamoSub);
        }

        /**
         * A parsed {@code Settings} object.
         */
        public static final class Settings {
            private final ReactionCodes reactionCodes;

            private Settings(ReactionCodes reactionCodes) {
                this.reactionCodes = reactionCodes;
            }

            /**
             * Returns the {@code reaction_codes} field.
             *
     * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<ReactionCodes> reactionCodes() {
                return Optional.ofNullable(reactionCodes);
            }

            /**
             * A parsed {@code ReactionCodes} object.
             */
            public static final class ReactionCodes {
                private final String value;

                private ReactionCodes(String value) {
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
                 * Parses a {@code ReactionCodes} from the given JSON object.
                 *
     * @param obj the JSON object to parse
                 * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
                 */
                static Optional<ReactionCodes> of(JSONObject obj) {
                    if (obj == null) {
                        return Optional.empty();
                    }

                    var value = obj.getString("value");
                    return Optional.of(new ReactionCodes(value));
                }

                /**
                 * Parses a list of {@code ReactionCodes} from the given JSON array.
                 *
     * @param arr the JSON array to parse
                 * @return the list of parsed results, empty if {@code arr} is {@code null}
                 */
                static List<ReactionCodes> ofArray(JSONArray arr) {
                    if (arr == null) {
                        return List.of();
                    }

                    var result = new ArrayList<ReactionCodes>(arr.size());
                    for (var i = 0; i < arr.size(); i++) {
                        of(arr.getJSONObject(i)).ifPresent(result::add);
                    }
                    return result;
                }
            }

            /**
             * Parses a {@code Settings} from the given JSON object.
             *
     * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<Settings> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var reactionCodes = ReactionCodes.of(obj.getJSONObject("reaction_codes")).orElse(null);
                return Optional.of(new Settings(reactionCodes));
            }

            /**
             * Parses a list of {@code Settings} from the given JSON array.
             *
     * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<Settings> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<Settings>(arr.size());
                for (var i = 0; i < arr.size(); i++) {
                    of(arr.getJSONObject(i)).ifPresent(result::add);
                }
                return result;
            }
        }

        /**
         * A parsed {@code WamoSub} object.
         */
        public static final class WamoSub {
            private final String planId;

            private WamoSub(String planId) {
                this.planId = planId;
            }

            /**
             * Returns the {@code plan_id} field.
             *
     * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> planId() {
                return Optional.ofNullable(planId);
            }

            /**
             * Parses a {@code WamoSub} from the given JSON object.
             *
     * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<WamoSub> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var planId = obj.getString("plan_id");
                return Optional.of(new WamoSub(planId));
            }

            /**
             * Parses a list of {@code WamoSub} from the given JSON array.
             *
     * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<WamoSub> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<WamoSub>(arr.size());
                for (var i = 0; i < arr.size(); i++) {
                    of(arr.getJSONObject(i)).ifPresent(result::add);
                }
                return result;
            }
        }

        /**
         * Parses a {@code ThreadMetadata} from the given JSON object.
         *
     * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<ThreadMetadata> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var subscribersCount = obj.getLong("subscribers_count");
            var verification = obj.getString("verification");
            var settings = Settings.of(obj.getJSONObject("settings")).orElse(null);
            var wamoSub = WamoSub.of(obj.getJSONObject("wamo_sub")).orElse(null);
            return Optional.of(new ThreadMetadata(subscribersCount, verification, settings, wamoSub));
        }

        /**
         * Parses a list of {@code ThreadMetadata} from the given JSON array.
         *
     * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
         */
        static List<ThreadMetadata> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<ThreadMetadata>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }

    /**
     * A parsed {@code ViewerMetadata} object.
     */
    public static final class ViewerMetadata {
        private final String wamoSubStatus;

        private ViewerMetadata(String wamoSubStatus) {
            this.wamoSubStatus = wamoSubStatus;
        }

        /**
         * Returns the {@code wamo_sub_status} field.
         *
     * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> wamoSubStatus() {
            return Optional.ofNullable(wamoSubStatus);
        }

        /**
         * Parses a {@code ViewerMetadata} from the given JSON object.
         *
     * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<ViewerMetadata> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var wamoSubStatus = obj.getString("wamo_sub_status");
            return Optional.of(new ViewerMetadata(wamoSubStatus));
        }

        /**
         * Parses a list of {@code ViewerMetadata} from the given JSON array.
         *
     * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
         */
        static List<ViewerMetadata> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<ViewerMetadata>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }

    /**
     * Parses a {@link FetchNewsletterDehydratedMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<FetchNewsletterDehydratedMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var root = data.getJSONObject("xwa2_newsletter");
        if (root == null) {
            return Optional.empty();
        }

        var id = root.getString("id");
        var threadMetadata = ThreadMetadata.of(root.getJSONObject("thread_metadata")).orElse(null);
        var viewerMetadata = ViewerMetadata.of(root.getJSONObject("viewer_metadata")).orElse(null);

        return Optional.of(new FetchNewsletterDehydratedMexResponse(id, threadMetadata, viewerMetadata));
    }
}
