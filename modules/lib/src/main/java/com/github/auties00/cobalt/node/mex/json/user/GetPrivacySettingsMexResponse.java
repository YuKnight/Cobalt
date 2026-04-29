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
 * The parsed response for this MEX query.
 */
public final class GetPrivacySettingsMexResponse implements MexOperation.Response.Json {
    private final List<Item> items;

    private GetPrivacySettingsMexResponse(List<Item> items) {
        this.items = items;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexGetPrivacySettingsQuery.graphql: reads the
     * {@code items[]} array of privacy settings records.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexGetPrivacySettingsQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<GetPrivacySettingsMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(GetPrivacySettingsMexResponse::of);
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
        private final PrivacySettings privacySettings;
        private final String id;

        private Item(PrivacySettings privacySettings, String id) {
            this.privacySettings = privacySettings;
            this.id = id;
        }

        /**
         * Returns the {@code privacy_settings} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<PrivacySettings> privacySettings() {
            return Optional.ofNullable(privacySettings);
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
         * A parsed {@code PrivacySettings} object.
         */
        public static final class PrivacySettings {
            private final List<Settings> settings;

            private PrivacySettings(List<Settings> settings) {
                this.settings = settings;
            }

            /**
             * Returns the {@code settings} field.
             *
             * @return the list of values, empty if absent
             */
            public List<Settings> settings() {
                return settings;
            }

            /**
             * A parsed {@code Settings} object.
             */
            public static final class Settings {
                private final String feature;
                private final String setting;

                private Settings(String feature, String setting) {
                    this.feature = feature;
                    this.setting = setting;
                }

                /**
                 * Returns the {@code feature} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<String> feature() {
                    return Optional.ofNullable(feature);
                }

                /**
                 * Returns the {@code setting} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<String> setting() {
                    return Optional.ofNullable(setting);
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

                    var feature = obj.getString("feature");
                    var setting = obj.getString("setting");
                    return Optional.of(new Settings(feature, setting));
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
             * Parses a {@code PrivacySettings} from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<PrivacySettings> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var settings = Settings.ofArray(obj.getJSONArray("settings"));
                return Optional.of(new PrivacySettings(settings));
            }

            /**
             * Parses a list of {@code PrivacySettings} from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
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
         * Parses a {@code Item} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
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
