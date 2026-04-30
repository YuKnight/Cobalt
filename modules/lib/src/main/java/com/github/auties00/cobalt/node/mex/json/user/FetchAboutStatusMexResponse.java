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
 * Parsed response for the about-status fetch query. Carries the per-user about-status update history projected from
 * {@code data.xwa2_users_updates_since}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchAboutStatusJob")
public final class FetchAboutStatusMexResponse implements MexOperation.Response.Json {
    /**
     * The per-user about-status records returned by the relay.
     */
    private final List<Item> items;

    /**
     * Constructs a new response carrying the given items.
     *
     * @param items the per-user about-status records
     */
    private FetchAboutStatusMexResponse(List<Item> items) {
        this.items = items;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@link Optional#empty()} if the expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchAboutStatusJob", exports = "mexGetAbout",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchAboutStatusMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchAboutStatusMexResponse::of);
    }

    /**
     * Returns the per-user records carried by this response.
     *
     * @return the list of items, empty if absent
     */
    public List<Item> items() {
        return items;
    }

    /**
     * Single {@code xwa2_users_updates_since} entry. Wraps the chronological list of about-status updates known for a
     * single user.
     */
    public static final class Item {
        /**
         * The chronological list of about-status updates for this user.
         */
        private final List<Updates> updates;

        /**
         * Constructs a new entry with the given updates list.
         *
         * @param updates the chronological list of updates
         */
        private Item(List<Updates> updates) {
            this.updates = updates;
        }

        /**
         * Returns the chronological list of about-status updates for this user.
         *
         * @return the list of updates, empty if absent
         */
        public List<Updates> updates() {
            return updates;
        }

        /**
         * A single about-status update entry.
         */
        public static final class Updates {
            /**
             * The about-status text recorded by this update.
             */
            private final String text;

            /**
             * Constructs a new update entry.
             *
             * @param text the about-status text
             */
            private Updates(String text) {
                this.text = text;
            }

            /**
             * Returns the about-status text recorded by this update.
             *
             * @return an {@link Optional} containing the text, or empty if absent
             */
            public Optional<String> text() {
                return Optional.ofNullable(text);
            }

            /**
             * Parses a single update entry from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed entry, or empty if {@code obj} is {@code null}
             */
            static Optional<Updates> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var text = obj.getString("text");
                return Optional.of(new Updates(text));
            }

            /**
             * Parses a list of update entries from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed entries, empty if {@code arr} is {@code null}
             */
            static List<Updates> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<Updates>(arr.size());
                for (var i = 0; i < arr.size(); i++) {
                    of(arr.getJSONObject(i)).ifPresent(result::add);
                }
                return result;
            }
        }

        /**
         * Parses a single {@code Item} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed item, or empty if {@code obj} is {@code null}
         */
        static Optional<Item> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var updates = Updates.ofArray(obj.getJSONArray("updates"));
            return Optional.of(new Item(updates));
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
    private static Optional<FetchAboutStatusMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var rootArr = data.getJSONArray("xwa2_users_updates_since");
        var items = Item.ofArray(rootArr);

        return Optional.of(new FetchAboutStatusMexResponse(items));
    }
}
