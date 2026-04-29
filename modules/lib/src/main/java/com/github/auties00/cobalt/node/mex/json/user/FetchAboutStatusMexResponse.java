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
public final class FetchAboutStatusMexResponse implements MexOperation.Response.Json {
    private final List<Item> items;

    private FetchAboutStatusMexResponse(List<Item> items) {
        this.items = items;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexFetchAboutStatusJobQuery.graphql: reads the
     * {@code items[].updates[]} array containing about-status entries.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchAboutStatusJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchAboutStatusMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchAboutStatusMexResponse::of);
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
        private final List<Updates> updates;

        private Item(List<Updates> updates) {
            this.updates = updates;
        }

        /**
         * Returns the {@code updates} field.
         *
         * @return the list of values, empty if absent
         */
        public List<Updates> updates() {
            return updates;
        }

        /**
         * A parsed {@code Updates} object.
         */
        public static final class Updates {
            private final String text;

            private Updates(String text) {
                this.text = text;
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
             * Parses a {@code Updates} from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<Updates> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var text = obj.getString("text");
                return Optional.of(new Updates(text));
            }

            /**
             * Parses a list of {@code Updates} from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
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
         * Parses a {@code Item} from the given JSON object.
         *
         * @param obj the JSON object to parse
         * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
         */
        static Optional<Item> of(JSONObject obj) {
            if (obj == null) {
                return Optional.empty();
            }

            var updates = Updates.ofArray(obj.getJSONArray("updates"));
            return Optional.of(new Item(updates));
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
