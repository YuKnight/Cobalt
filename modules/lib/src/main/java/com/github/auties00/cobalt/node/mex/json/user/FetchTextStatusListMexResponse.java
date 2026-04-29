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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The parsed response for this MEX query.
 */
public final class FetchTextStatusListMexResponse implements MexOperation.Response.Json {
    private final List<Item> items;

    private FetchTextStatusListMexResponse(List<Item> items) {
        this.items = items;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @implNote WAWebMexFetchTextStatusListJobQuery.graphql: reads the
     * {@code items[]} array of text status entries.
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@code Optional.empty()} if the
     *         expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchTextStatusListJobQuery.graphql", exports = "params.id",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchTextStatusListMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchTextStatusListMexResponse::of);
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
        private final String text;
        private final Long lastUpdateTime;
        private final Long ephemeralDurationSec;
        private final Emoji emoji;

        private Item(String jid, String text, Long lastUpdateTime, Long ephemeralDurationSec, Emoji emoji) {
            this.jid = jid;
            this.text = text;
            this.lastUpdateTime = lastUpdateTime;
            this.ephemeralDurationSec = ephemeralDurationSec;
            this.emoji = emoji;
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
         * Returns the {@code text} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<String> text() {
            return Optional.ofNullable(text);
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
         * Returns the {@code ephemeral_duration_sec} field.
         *
         * @return an {@link Optional} containing the value as a {@link Duration}, or empty if absent
         */
        public Optional<Duration> ephemeralDurationSec() {
            return Optional.ofNullable(ephemeralDurationSec).map(Duration::ofSeconds);
        }

        /**
         * Returns the {@code emoji} field.
         *
         * @return an {@link Optional} containing the value, or empty if absent
         */
        public Optional<Emoji> emoji() {
            return Optional.ofNullable(emoji);
        }

        /**
         * A parsed {@code Emoji} object.
         */
        public static final class Emoji {
            private final String content;

            private Emoji(String content) {
                this.content = content;
            }

            /**
             * Returns the {@code content} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> content() {
                return Optional.ofNullable(content);
            }

            /**
             * Parses a {@code Emoji} from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<Emoji> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var content = obj.getString("content");
                return Optional.of(new Emoji(content));
            }

            /**
             * Parses a list of {@code Emoji} from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<Emoji> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<Emoji>(arr.size());
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
            var text = obj.getString("text");
            var lastUpdateTime = obj.getLong("last_update_time");
            var ephemeralDurationSec = obj.getLong("ephemeral_duration_sec");
            var emoji = Emoji.of(obj.getJSONObject("emoji")).orElse(null);
            return Optional.of(new Item(jid, text, lastUpdateTime, ephemeralDurationSec, emoji));
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

    private static Optional<FetchTextStatusListMexResponse> of(byte[] json) {
        var jsonObject = JSON.parseObject(json);
        if (jsonObject == null) {
            return Optional.empty();
        }

        var data = jsonObject.getJSONObject("data");
        if (data == null) {
            return Optional.empty();
        }

        var rootArr = data.getJSONArray("xwa2_text_status_list");
        var items = Item.ofArray(rootArr);

        return Optional.of(new FetchTextStatusListMexResponse(items));
    }
}
