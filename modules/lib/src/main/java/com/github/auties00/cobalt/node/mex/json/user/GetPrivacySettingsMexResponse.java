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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parsed response for the privacy settings fetch query. Carries the per-feature privacy preferences projected from
 * {@code data.xwa2_fetch_wa_users}.
 */
public final class GetPrivacySettingsMexResponse implements MexOperation.Response.Json {
    /**
     * The user records returned by the relay, each carrying the requesting user's privacy settings.
     */
    private final List<Item> items;

    /**
     * Constructs a new response carrying the given items.
     *
     * @param items the user records returned by the relay
     */
    private GetPrivacySettingsMexResponse(List<Item> items) {
        this.items = items;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@link Optional#empty()} if the expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetPrivacySettingsQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<GetPrivacySettingsMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(GetPrivacySettingsMexResponse::of);
    }

    /**
     * Returns the user records carried by this response.
     *
     * @return the list of items, empty if absent
     */
    public List<Item> items() {
        return items;
    }

    /**
     * Single user record carrying the privacy settings record alongside the user identifier.
     */
    public static final class Item {
        /**
         * The privacy settings record for the user.
         */
        private final PrivacySettings privacySettings;

        /**
         * The opaque user identifier returned by the relay.
         */
        private final String id;

        /**
         * Constructs a new item with the given fields.
         *
         * @param privacySettings the privacy settings record
         * @param id the opaque user identifier
         */
        private Item(PrivacySettings privacySettings, String id) {
            this.privacySettings = privacySettings;
            this.id = id;
        }

        /**
         * Returns the privacy settings record for the user.
         *
         * @return an {@link Optional} containing the record, or empty if absent
         */
        public Optional<PrivacySettings> privacySettings() {
            return Optional.ofNullable(privacySettings);
        }

        /**
         * Returns the opaque user identifier.
         *
         * @return an {@link Optional} containing the identifier, or empty if absent
         */
        public Optional<String> id() {
            return Optional.ofNullable(id);
        }

        /**
         * Privacy settings record. Carries the per-feature settings list projected from {@code privacy_settings}.
         */
        public static final class PrivacySettings {
            /**
             * The per-feature settings entries.
             */
            private final List<Settings> settings;

            /**
             * Constructs a new privacy settings record with the given entries.
             *
             * @param settings the per-feature settings entries
             */
            private PrivacySettings(List<Settings> settings) {
                this.settings = settings;
            }

            /**
             * Returns the per-feature settings entries.
             *
             * @return the list of entries, empty if absent
             */
            public List<Settings> settings() {
                return settings;
            }

            /**
             * Single settings entry pairing a privacy feature with its current setting value.
             */
            public static final class Settings {
                /**
                 * The privacy feature key, for example {@code last_seen} or {@code profile}.
                 */
                private final String feature;

                /**
                 * The setting value for the feature, for example {@code all} or {@code contacts}.
                 */
                private final String setting;

                /**
                 * Constructs a new settings entry with the given fields.
                 *
                 * @param feature the privacy feature key
                 * @param setting the setting value
                 */
                private Settings(String feature, String setting) {
                    this.feature = feature;
                    this.setting = setting;
                }

                /**
                 * Returns the privacy feature key.
                 *
                 * @return an {@link Optional} containing the feature key, or empty if absent
                 */
                public Optional<String> feature() {
                    return Optional.ofNullable(feature);
                }

                /**
                 * Returns the setting value associated with the feature.
                 *
                 * @return an {@link Optional} containing the setting value, or empty if absent
                 */
                public Optional<String> setting() {
                    return Optional.ofNullable(setting);
                }

                /**
                 * Parses a settings entry from the given JSON object.
                 *
                 * @param obj the JSON object to parse
                 * @return an {@link Optional} containing the parsed entry, or empty if {@code obj} is {@code null}
                 */
                static Optional<Settings> of(JSONObject obj) {
                    if (obj == null) {
                        return Optional.empty();
                    }

                    var feature = obj.getString("feature");
                    var setting = obj.getString("setting");
                    return Optional.of(new Settings(feature, setting));
                }

                /**
                 * Parses a list of settings entries from the given JSON array.
                 *
                 * @param arr the JSON array to parse
                 * @return the list of parsed entries, empty if {@code arr} is {@code null}
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
             * Parses a privacy settings record from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed record, or empty if {@code obj} is {@code null}
             */
            static Optional<PrivacySettings> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var settings = Settings.ofArray(obj.getJSONArray("settings"));
                return Optional.of(new PrivacySettings(settings));
            }

            /**
             * Parses a list of privacy settings records from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed records, empty if {@code arr} is {@code null}
             */
            static List<PrivacySettings> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<PrivacySettings>(arr.size());
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

            var privacySettings = PrivacySettings.of(obj.getJSONObject("privacy_settings")).orElse(null);
            var id = obj.getString("id");
            return Optional.of(new Item(privacySettings, id));
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
    private static Optional<GetPrivacySettingsMexResponse> of(byte[] json) {
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

        return Optional.of(new GetPrivacySettingsMexResponse(items));
    }
}
