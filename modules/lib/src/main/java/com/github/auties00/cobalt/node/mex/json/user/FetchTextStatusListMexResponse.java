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
 * Parsed response for the text-status list query. Carries the per-user text-status entries projected from
 * {@code data.xwa2_text_status_list}.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchTextStatusListJob")
public final class FetchTextStatusListMexResponse implements MexOperation.Response.Json {
    /**
     * The text-status entries returned by the relay, one per requested user.
     */
    private final List<Item> items;

    /**
     * Constructs a new response carrying the given items.
     *
     * @param items the per-user text-status entries
     */
    private FetchTextStatusListMexResponse(List<Item> items) {
        this.items = items;
    }

    /**
     * Parses the MEX response carried by an inbound IQ stanza.
     *
     * @param node the inbound IQ stanza carrying the {@code <result>} child
     * @return the parsed response, or {@link Optional#empty()} if the expected JSON shape is absent
     */
    @WhatsAppWebExport(moduleName = "WAWebMexFetchTextStatusListJob", exports = "mexGetTextStatusList",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static Optional<FetchTextStatusListMexResponse> of(Node node) {
        return node.getChild("result")
                .flatMap(Node::toContentBytes)
                .flatMap(FetchTextStatusListMexResponse::of);
    }

    /**
     * Returns the per-user text-status entries carried by this response.
     *
     * @return the list of items, empty if absent
     */
    public List<Item> items() {
        return items;
    }

    /**
     * Single text-status entry. Mirrors the {@code parseTextStatusServerResponse} record produced by WA Web from each
     * {@code xwa2_text_status_list[i]} element.
     */
    public static final class Item {
        /**
         * The author JID of the text status.
         */
        private final String jid;

        /**
         * The body of the text status.
         */
        private final String text;

        /**
         * The epoch-second timestamp at which the status was last updated.
         */
        private final Long lastUpdateTime;

        /**
         * The ephemeral duration in seconds after which the status expires.
         */
        private final Long ephemeralDurationSec;

        /**
         * The optional emoji decoration shown next to the status text.
         */
        private final Emoji emoji;

        /**
         * Constructs a new entry with the given fields.
         *
         * @param jid the author JID
         * @param text the body of the text status
         * @param lastUpdateTime the last-update epoch-second timestamp
         * @param ephemeralDurationSec the ephemeral duration in seconds
         * @param emoji the optional emoji decoration
         */
        private Item(String jid, String text, Long lastUpdateTime, Long ephemeralDurationSec, Emoji emoji) {
            this.jid = jid;
            this.text = text;
            this.lastUpdateTime = lastUpdateTime;
            this.ephemeralDurationSec = ephemeralDurationSec;
            this.emoji = emoji;
        }

        /**
         * Returns the author JID of the text status.
         *
         * @return an {@link Optional} containing the JID, or empty if absent
         */
        public Optional<String> jid() {
            return Optional.ofNullable(jid);
        }

        /**
         * Returns the body of the text status.
         *
         * @return an {@link Optional} containing the text, or empty if absent
         */
        public Optional<String> text() {
            return Optional.ofNullable(text);
        }

        /**
         * Returns the last-update timestamp of the text status.
         *
         * @return an {@link Optional} containing the {@link Instant}, or empty if absent
         */
        public Optional<Instant> lastUpdateTime() {
            return Optional.ofNullable(lastUpdateTime).map(Instant::ofEpochSecond);
        }

        /**
         * Returns the ephemeral duration after which the text status expires.
         *
         * @return an {@link Optional} containing the {@link Duration}, or empty if absent
         */
        public Optional<Duration> ephemeralDurationSec() {
            return Optional.ofNullable(ephemeralDurationSec).map(Duration::ofSeconds);
        }

        /**
         * Returns the optional emoji decoration shown next to the status text.
         *
         * @return an {@link Optional} containing the emoji, or empty if absent
         */
        public Optional<Emoji> emoji() {
            return Optional.ofNullable(emoji);
        }

        /**
         * Single emoji decoration attached to a text status.
         */
        public static final class Emoji {
            /**
             * The unicode content of the emoji.
             */
            private final String content;

            /**
             * Constructs a new emoji decoration with the given content.
             *
             * @param content the unicode content of the emoji
             */
            private Emoji(String content) {
                this.content = content;
            }

            /**
             * Returns the unicode content of the emoji.
             *
             * @return an {@link Optional} containing the content, or empty if absent
             */
            public Optional<String> content() {
                return Optional.ofNullable(content);
            }

            /**
             * Parses an emoji entry from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed entry, or empty if {@code obj} is {@code null}
             */
            static Optional<Emoji> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var content = obj.getString("content");
                return Optional.of(new Emoji(content));
            }

            /**
             * Parses a list of emoji entries from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed entries, empty if {@code arr} is {@code null}
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
            var text = obj.getString("text");
            var lastUpdateTime = obj.getLong("last_update_time");
            var ephemeralDurationSec = obj.getLong("ephemeral_duration_sec");
            var emoji = Emoji.of(obj.getJSONObject("emoji")).orElse(null);
            return Optional.of(new Item(jid, text, lastUpdateTime, ephemeralDurationSec, emoji));
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
