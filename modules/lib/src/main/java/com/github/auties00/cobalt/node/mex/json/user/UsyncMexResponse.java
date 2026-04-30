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
 * Parsed response for the usync MEX query. Carries the per-user directory metadata projected from
 * {@code data.xwa2_fetch_wa_users}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexUsync")
public final class UsyncMexResponse implements MexOperation.Response.Json {
    /**
     * The per-user directory records returned by the relay.
     */
    private final List<Item> items;

    /**
     * Constructs a new response carrying the given items.
     *
     * @param items the per-user directory records
     */
    private UsyncMexResponse(List<Item> items) {
        this.items = items;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@link Optional#empty()} if the expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexUsync", exports = "mexUsyncQuery",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<UsyncMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(UsyncMexResponse::of);
    }

    /**
     * Returns the per-user directory records carried by this response.
     *
     * @return the list of items, empty if absent
     */
    public List<Item> items() {
        return items;
    }

    /**
     * Single per-user directory record. Carries the JID, country code, optional username info and optional
     * about-status info for a queried user.
     */
    public static final class Item {
        /**
         * The user's JID.
         */
        private final String jid;

        /**
         * The user's phone country code, when requested.
         */
        private final String countryCode;

        /**
         * The user's username record, when requested.
         */
        private final UsernameInfo usernameInfo;

        /**
         * The user's about-status record, when requested.
         */
        private final AboutStatusInfo aboutStatusInfo;

        /**
         * The opaque record identifier reported by the relay.
         */
        private final String id;

        /**
         * Constructs a new item with the given fields.
         *
         * @param jid the user's JID
         * @param countryCode the user's phone country code
         * @param usernameInfo the user's username record
         * @param aboutStatusInfo the user's about-status record
         * @param id the opaque record identifier
         */
        private Item(String jid, String countryCode, UsernameInfo usernameInfo, AboutStatusInfo aboutStatusInfo, String id) {
            this.jid = jid;
            this.countryCode = countryCode;
            this.usernameInfo = usernameInfo;
            this.aboutStatusInfo = aboutStatusInfo;
            this.id = id;
        }

        /**
         * Returns the user's JID.
         *
         * @return an {@link Optional} containing the JID, or empty if absent
         */
        public Optional<String> jid() {
            return Optional.ofNullable(jid);
        }

        /**
         * Returns the user's phone country code.
         *
         * @return an {@link Optional} containing the country code, or empty if absent
         */
        public Optional<String> countryCode() {
            return Optional.ofNullable(countryCode);
        }

        /**
         * Returns the user's username record.
         *
         * @return an {@link Optional} containing the record, or empty if absent
         */
        public Optional<UsernameInfo> usernameInfo() {
            return Optional.ofNullable(usernameInfo);
        }

        /**
         * Returns the user's about-status record.
         *
         * @return an {@link Optional} containing the record, or empty if absent
         */
        public Optional<AboutStatusInfo> aboutStatusInfo() {
            return Optional.ofNullable(aboutStatusInfo);
        }

        /**
         * Returns the opaque record identifier reported by the relay.
         *
         * @return an {@link Optional} containing the identifier, or empty if absent
         */
        public Optional<String> id() {
            return Optional.ofNullable(id);
        }

        /**
         * Username record carried by a usync entry. Captures the assigned username, its registration state, the
         * registration timestamp, the recovery PIN hash and the per-user query status.
         */
        public static final class UsernameInfo {
            /**
             * The username currently assigned to the user.
             */
            private final String username;

            /**
             * The registration state of the username.
             */
            private final String state;

            /**
             * The epoch-second timestamp at which the username was registered.
             */
            private final Long timestamp;

            /**
             * The recovery PIN hash associated with the username.
             */
            private final String pin;

            /**
             * The per-user status reported by the usync request.
             */
            private final String status;

            /**
             * Constructs a new username record.
             *
             * @param username the assigned username
             * @param state the registration state
             * @param timestamp the registration epoch-second timestamp
             * @param pin the recovery PIN hash
             * @param status the per-user query status
             */
            private UsernameInfo(String username, String state, Long timestamp, String pin, String status) {
                this.username = username;
                this.state = state;
                this.timestamp = timestamp;
                this.pin = pin;
                this.status = status;
            }

            /**
             * Returns the username currently assigned to the user.
             *
             * @return an {@link Optional} containing the username, or empty if absent
             */
            public Optional<String> username() {
                return Optional.ofNullable(username);
            }

            /**
             * Returns the registration state of the username.
             *
             * @return an {@link Optional} containing the state, or empty if absent
             */
            public Optional<String> state() {
                return Optional.ofNullable(state);
            }

            /**
             * Returns the registration timestamp.
             *
             * @return an {@link Optional} containing the {@link Instant}, or empty if absent
             */
            public Optional<Instant> timestamp() {
                return Optional.ofNullable(timestamp).map(Instant::ofEpochSecond);
            }

            /**
             * Returns the recovery PIN hash.
             *
             * @return an {@link Optional} containing the PIN hash, or empty if absent
             */
            public Optional<String> pin() {
                return Optional.ofNullable(pin);
            }

            /**
             * Returns the per-user status reported by the request.
             *
             * @return an {@link Optional} containing the status, or empty if absent
             */
            public Optional<String> status() {
                return Optional.ofNullable(status);
            }

            /**
             * Parses a username record from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed record, or empty if {@code obj} is {@code null}
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
             * Parses a list of username records from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed records, empty if {@code arr} is {@code null}
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
         * About-status record carried by a usync entry. Captures the about-status text body, the last-update epoch-
         * second timestamp and the per-user query status.
         */
        public static final class AboutStatusInfo {
            /**
             * The about-status text body.
             */
            private final String text;

            /**
             * The epoch-second timestamp at which the about-status was last updated.
             */
            private final Long timestamp;

            /**
             * The per-user query status reported for the about-status field.
             */
            private final String status;

            /**
             * Constructs a new about-status record.
             *
             * @param text the about-status text body
             * @param timestamp the last-update epoch-second timestamp
             * @param status the per-user query status
             */
            private AboutStatusInfo(String text, Long timestamp, String status) {
                this.text = text;
                this.timestamp = timestamp;
                this.status = status;
            }

            /**
             * Returns the about-status text body.
             *
             * @return an {@link Optional} containing the text, or empty if absent
             */
            public Optional<String> text() {
                return Optional.ofNullable(text);
            }

            /**
             * Returns the last-update timestamp of the about-status.
             *
             * @return an {@link Optional} containing the {@link Instant}, or empty if absent
             */
            public Optional<Instant> timestamp() {
                return Optional.ofNullable(timestamp).map(Instant::ofEpochSecond);
            }

            /**
             * Returns the per-user query status reported for this field.
             *
             * @return an {@link Optional} containing the status, or empty if absent
             */
            public Optional<String> status() {
                return Optional.ofNullable(status);
            }

            /**
             * Parses an about-status record from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed record, or empty if {@code obj} is {@code null}
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
             * Parses a list of about-status records from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed records, empty if {@code arr} is {@code null}
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
         * Parses a single item from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed item, or empty if {@code obj} is {@code null}
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
         * Parses a list of items from the given JSON array.
         *
         * @param arr the JSON array to parse
         * @return the list of parsed items, empty if {@code arr} is {@code null}
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

    /**
     * Parses the response from the raw JSON payload bytes.
     *
     * @param json the raw JSON bytes from the {@code <result>} child
     * @return an {@link Optional} containing the parsed response, or empty if the envelope is missing
     */
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
