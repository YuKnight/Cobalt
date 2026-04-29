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
 * The response variant of {@link FetchNewsletterInsightsMexResponse} that exposes the data
 * returned by the server after a successful query.
 *
 * @implNote WAWebMexFetchNewsletterInsightsJob: adapts the JSON root returned by the GraphQL
 * query into a Java value object.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterInsightsJob")
public final class FetchNewsletterInsightsMexResponse implements MexOperation.Response.Json {
    private final String newsletterId;
    private final State state;
    private final Long lastUpdateTime;
    private final String metricsStatus;
    private final List<Result> result;

    private FetchNewsletterInsightsMexResponse(String newsletterId, State state, Long lastUpdateTime, String metricsStatus, List<Result> result) {
        this.newsletterId = newsletterId;
        this.state = state;
        this.lastUpdateTime = lastUpdateTime;
        this.metricsStatus = metricsStatus;
        this.result = result;
    }

    /**
     * Parses a MEX response from the given IQ response node.
     *
     * @implNote WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights: WA Web relies on the
     * GraphQL client to unwrap the response. Cobalt performs the
     * unwrapping manually from the IQ {@code <result>} child.
     * @param node the IQ response node received from the relay
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the node is missing a result payload
     */
    public static Optional<FetchNewsletterInsightsMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchNewsletterInsightsMexResponse::of);
    }

    /**
     * Returns the {@code newsletter_id} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> newsletterId() {
        return Optional.ofNullable(newsletterId);
    }

    /**
     * Returns the {@code state} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<State> state() {
        return Optional.ofNullable(state);
    }

    /**
     * Returns the {@code last_update_time} field.
     *
     * @return an {@link Optional} containing the value as an {@link Instant}, or empty if absent
     */
    public Optional<Instant> lastUpdateTime() {
        return Optional.ofNullable(lastUpdateTime).map(Instant::ofEpochSecond);
    }

    /**
     * Returns the {@code metrics_status} field.
     *
     * @return an {@link Optional} containing the value, or empty if absent
     */
    public Optional<String> metricsStatus() {
        return Optional.ofNullable(metricsStatus);
    }

    /**
     * Returns the {@code result} field.
     *
     * @return the list of values, empty if absent
     */
    public List<Result> result() {
        return result;
    }

    /**
     * A parsed {@code State} object.
     */
    public static final class State {
        private final String type;

        private State(String type) {
            this.type = type;
        }

        /**
         * Returns the {@code type} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> type() {
            return Optional.ofNullable(type);
        }

        /**
         * Parses a {@code State} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<State> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var type = obj.getString("type");
            return Optional.of(new State(type));
        }

        /**
         * Parses a list of {@code State} from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
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
     * A parsed {@code Result} object.
     */
    public static final class Result {
        private final String id;
        private final List<Values> values;

        private Result(String id, List<Values> values) {
            this.id = id;
            this.values = values;
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
         * Returns the {@code values} field.
         *
         * @return the list of values, empty if absent
         */
        public List<Values> values() {
            return values;
        }

        /**
         * A parsed {@code Values} object.
         */
        public static final class Values {
            private final String value;
            private final String country;
            private final String role;
            private final Long timestamp;

            private Values(String value, String country, String role, Long timestamp) {
                this.value = value;
                this.country = country;
                this.role = role;
                this.timestamp = timestamp;
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
             * Returns the {@code country} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> country() {
                return Optional.ofNullable(country);
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
             * Returns the {@code timestamp} field.
             *
             * @return an {@link Optional} containing the value as an {@link Instant}, or empty if absent
             */
            public Optional<Instant> timestamp() {
                return Optional.ofNullable(timestamp).map(Instant::ofEpochSecond);
            }

            /**
             * Parses a {@code Values} from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<Values> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var value = obj.getString("value");
                var country = obj.getString("country");
                var role = obj.getString("role");
                var timestamp = obj.getLong("timestamp");
                return Optional.of(new Values(value, country, role, timestamp));
            }

            /**
             * Parses a list of {@code Values} from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<Values> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<Values>(arr.size());
                for (var i = 0; i < arr.size(); i++) {
                    of(arr.getJSONObject(i)).ifPresent(result::add);
                }
                return result;
            }
        }

        /**
         * Parses a {@code Result} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<Result> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var id = obj.getString("id");
            var values = Values.ofArray(obj.getJSONArray("values"));
            return Optional.of(new Result(id, values));
        }

        /**
         * Parses a list of {@code Result} from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
         */
        static List<Result> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<Result>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }

    /**
     * Parses a {@link FetchNewsletterInsightsMexResponse} from the raw JSON bytes of the
     * {@code <result>} child.
     *
     * @implNote WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights: mirrors the implicit
     * unwrapping that WA Web performs on the GraphQL response,
     * extracting the {@code xwa2_newsletter_admin_insights} root.
     * @param json the UTF-8 encoded JSON payload
     * @return an {@link Optional} containing the parsed response, or
     *         empty if the envelope is missing expected fields
     */
    private static Optional<FetchNewsletterInsightsMexResponse> of(byte[] json) {
        // WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights
        // Parses the raw JSON payload, bailing out if fastjson2 returns null
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        // WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights
        // Descends into the standard GraphQL "data" envelope
        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        // WAWebMexFetchNewsletterInsightsJob.mexFetchNewsletterInsights
        // Extracts the operation-specific root keyed by xwa2_newsletter_admin_insights
        var root = data.getJSONObject("xwa2_newsletter_admin_insights");
        if (root == null) {
            return Optional.empty();
        }

        var newsletterId = root.getString("newsletter_id");
        var state = State.of(root.getJSONObject("state")).orElse(null);
        var lastUpdateTime = root.getLong("last_update_time");
        var metricsStatus = root.getString("metrics_status");
        var result = Result.ofArray(root.getJSONArray("result"));

        return Optional.of(new FetchNewsletterInsightsMexResponse(newsletterId, state, lastUpdateTime, metricsStatus, result));
    }
}
