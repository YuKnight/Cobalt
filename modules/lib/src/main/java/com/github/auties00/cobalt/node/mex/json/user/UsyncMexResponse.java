package com.github.auties00.cobalt.node.mex.json.user;

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
 * The parsed response for this MEX query.
 */
public final class UsyncMexResponse implements MexOperation.Response.Json {
    private final List<Item> items;

    private UsyncMexResponse(List<Item> items) {
        this.items = items;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexUsyncQuery.graphql: reads the {@code items[]}
     * array of per-user directory records.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsyncQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<UsyncMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(UsyncMexResponse::of);
    }

    /**
     * Returns the list of items in this response.
     *
     * @return the list of items, empty if absent
     */
    public List<Item> items() {
        return items;
    }

    /**
     * A parsed {@code Item} object.
     */
    public static final class Item {
        private final String jid;
        private final String countryCode;
        private final UsernameInfo usernameInfo;
        private final AboutStatusInfo aboutStatusInfo;
        private final String id;

        private Item(String jid, String countryCode, UsernameInfo usernameInfo, AboutStatusInfo aboutStatusInfo, String id) {
            this.jid = jid;
            this.countryCode = countryCode;
            this.usernameInfo = usernameInfo;
            this.aboutStatusInfo = aboutStatusInfo;
            this.id = id;
        }

        /**
         * Returns the {@code jid} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> jid() {
            return Optional.ofNullable(jid);
        }

        /**
         * Returns the {@code country_code} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> countryCode() {
            return Optional.ofNullable(countryCode);
        }

        /**
         * Returns the {@code username_info} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<UsernameInfo> usernameInfo() {
            return Optional.ofNullable(usernameInfo);
        }

        /**
         * Returns the {@code about_status_info} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<AboutStatusInfo> aboutStatusInfo() {
            return Optional.ofNullable(aboutStatusInfo);
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
         * A parsed {@code UsernameInfo} object.
         */
        public static final class UsernameInfo {
            private final String username;
            private final String state;
            private final Long timestamp;
            private final String pin;
            private final String status;

            private UsernameInfo(String username, String state, Long timestamp, String pin, String status) {
                this.username = username;
                this.state = state;
                this.timestamp = timestamp;
                this.pin = pin;
                this.status = status;
            }

            /**
             * Returns the {@code username} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> username() {
                return Optional.ofNullable(username);
            }

            /**
             * Returns the {@code state} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> state() {
                return Optional.ofNullable(state);
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
             * Returns the {@code pin} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> pin() {
                return Optional.ofNullable(pin);
            }

            /**
             * Returns the {@code status} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> status() {
                return Optional.ofNullable(status);
            }

            /**
             * Parses a {@code UsernameInfo} from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<UsernameInfo> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var username = obj.getString("username");
                var state = obj.getString("state");
                var timestamp = obj.getLong("timestamp");
                var pin = obj.getString("pin");
                var status = obj.getString("status");
                return Optional.of(new UsernameInfo(username, state, timestamp, pin, status));
            }

            /**
             * Parses a list of {@code UsernameInfo} from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<UsernameInfo> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<UsernameInfo>(arr.size());
                for (var i = 0; i < arr.size(); i++) {
                    of(arr.getJSONObject(i)).ifPresent(result::add);
                }
                return result;
            }
        }

        /**
         * A parsed {@code AboutStatusInfo} object.
         */
        public static final class AboutStatusInfo {
            private final String text;
            private final Long timestamp;
            private final String status;

            private AboutStatusInfo(String text, Long timestamp, String status) {
                this.text = text;
                this.timestamp = timestamp;
                this.status = status;
            }

            /**
             * Returns the {@code text} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> text() {
                return Optional.ofNullable(text);
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
             * Returns the {@code status} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> status() {
                return Optional.ofNullable(status);
            }

            /**
             * Parses a {@code AboutStatusInfo} from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<AboutStatusInfo> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var text = obj.getString("text");
                var timestamp = obj.getLong("timestamp");
                var status = obj.getString("status");
                return Optional.of(new AboutStatusInfo(text, timestamp, status));
            }

            /**
             * Parses a list of {@code AboutStatusInfo} from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<AboutStatusInfo> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<AboutStatusInfo>(arr.size());
                for (var i = 0; i < arr.size(); i++) {
                    of(arr.getJSONObject(i)).ifPresent(result::add);
                }
                return result;
            }
        }

        /**
         * Parses a {@code Item} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<Item> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var jid = obj.getString("jid");
            var countryCode = obj.getString("country_code");
            var usernameInfo = UsernameInfo.of(obj.getJSONObject("username_info")).orElse(null);
            var aboutStatusInfo = AboutStatusInfo.of(obj.getJSONObject("about_status_info")).orElse(null);
            var id = obj.getString("id");
            return Optional.of(new Item(jid, countryCode, usernameInfo, aboutStatusInfo, id));
        }

        /**
         * Parses a list of {@code Item} from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed results, empty if {@code arr} is {@code null}
         */
        static List<Item> ofArray(JSONArray arr) {
            if (arr == null) {
                return List.of();
            }

            var result = new ArrayList<Item>(arr.size());
            for (var i = 0; i < arr.size(); i++) {
                of(arr.getJSONObject(i)).ifPresent(result::add);
            }
            return result;
        }
    }

    private static Optional<UsyncMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var rootArr = data.getJSONArray("xwa2_fetch_wa_users");
        var items = Item.ofArray(rootArr);

        return Optional.of(new UsyncMexResponse(items));
    }
}
