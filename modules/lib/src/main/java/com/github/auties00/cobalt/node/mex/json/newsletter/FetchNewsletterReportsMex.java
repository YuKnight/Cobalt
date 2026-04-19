package com.github.auties00.cobalt.node.mex.json.newsletter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.mex.json.MexJsonOperation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Fetches the list of user-submitted reports against a newsletter.
 *
 * <p>Newsletter admins and moderators use this query to review reports filed by followers, including the report reason, reporter metadata and timestamps.
 *
 * @implNote WAWebMexFetchNewsletterReportsJob: adapts the {@code mexFetchNewsletterReports} GraphQL query,
 * which in WA Web is invoked via {@code WAWebMexClient.fetchQuery} and
 * whose response is unwrapped by the same module. Cobalt models the request
 * and response as sibling variants of a sealed interface rather than a
 * free-standing async function.
 */
@WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterReportsJob")
public sealed interface FetchNewsletterReportsMex extends MexJsonOperation permits FetchNewsletterReportsMex.Request, FetchNewsletterReportsMex.Response {
    /**
     * The numeric GraphQL query identifier assigned by the WhatsApp relay
     * to the {@code FetchNewsletterReports} compiled query.
     *
     * @implNote WAWebMexFetchNewsletterReportsJobQuery.graphql: corresponds to the compiled
     * document id registered for the {@code mexFetchNewsletterReports} query.
     */
    String QUERY_ID = "25246702401624388";

    /**
     * The request variant of {@link FetchNewsletterReportsMex} that serialises the
     * query variables and emits the outbound IQ stanza.
     *
     * @implNote WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports: adapts the {@code variables}
     * object constructed inline in the JS implementation into a dedicated
     * Java class.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterReportsJob")
    final class Request implements FetchNewsletterReportsMex {

        public Request() {
        }

        /**
         * Builds the IQ stanza that dispatches this operation to the
         * WhatsApp relay.
         *
         * @implNote WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports: WA Web constructs the
         * {@code variables} object inline and delegates to
         * {@code WAWebMexClient.fetchQuery}. Cobalt writes the JSON directly
         * via {@code fastjson2.JSONWriter} and wraps it through
         * {@link MexJsonOperation#createMexNode(String, String)}.
         * @return a {@link NodeBuilder} carrying the IQ envelope and the
         *         serialised GraphQL variables
         */
        @WhatsAppWebExport(moduleName = "WAWebMexFetchNewsletterReportsJob", exports = "mexFetchNewsletterReports",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public NodeBuilder toNode() {
            // WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports
            // Opens a UTF-8 JSON writer that will serialise the GraphQL variables envelope
            try (var writer = JSONWriter.ofUTF8()) {
                // WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports
                // Begins the outer envelope and the nested "variables" object consumed by WAWebMexClient.fetchQuery
                writer.startObject();
                writer.writeName("variables");
                writer.writeColon();
                writer.startObject();
                writer.endObject();
                writer.endObject();
                try (var output = new StringWriter()) {
                    writer.flushTo(output);
                    return MexJsonOperation.createMexNode(QUERY_ID, output.toString());
                }
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
    }

    /**
     * The response variant of {@link FetchNewsletterReportsMex} that exposes the data
     * returned by the server after a successful query.
     *
     * @implNote WAWebMexFetchNewsletterReportsJob: adapts the JSON root returned by the GraphQL
     * query into a Java value object.
     */
    @WhatsAppWebModule(moduleName = "WAWebMexFetchNewsletterReportsJob")
    final class Response implements FetchNewsletterReportsMex {
        private final List<ChannelsReports> channelsReports;

        private Response(List<ChannelsReports> channelsReports) {
            this.channelsReports = channelsReports;
        }

        /**
         * Parses a MEX response from the given IQ response node.
         *
         * @implNote WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports: WA Web relies on the
         * GraphQL client to unwrap the response. Cobalt performs the
         * unwrapping manually from the IQ {@code <result>} child.
         * @param node the IQ response node received from the relay
         * @return an {@link Optional} containing the parsed response, or
         *         empty if the node is missing a result payload
         */
        public static Optional<Response> of(Node node) {
            return node.getChild("result")
                    .flatMap(Node::toContentBytes)
                    .flatMap(Response::of);
        }

        /**
         * Returns the {@code channels_reports} field.
         *
         * @return the list of values, empty if absent
         */
        public List<ChannelsReports> channelsReports() {
            return channelsReports;
        }

        /**
         * A parsed {@code ChannelsReports} object.
         */
        public static final class ChannelsReports {
            private final String reportId;
            private final String status;
            private final Long creationTime;
            private final Long lastUpdateTime;
            private final String channelName;
            private final String channelJid;
            private final String serverMsgId;
            private final String responseServerMsgId;
            private final String notifyName;
            private final Appeal appeal;

            private ChannelsReports(String reportId, String status, Long creationTime, Long lastUpdateTime, String channelName, String channelJid, String serverMsgId, String responseServerMsgId, String notifyName, Appeal appeal) {
                this.reportId = reportId;
                this.status = status;
                this.creationTime = creationTime;
                this.lastUpdateTime = lastUpdateTime;
                this.channelName = channelName;
                this.channelJid = channelJid;
                this.serverMsgId = serverMsgId;
                this.responseServerMsgId = responseServerMsgId;
                this.notifyName = notifyName;
                this.appeal = appeal;
            }

            /**
             * Returns the {@code report_id} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> reportId() {
                return Optional.ofNullable(reportId);
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
             * Returns the {@code creation_time} field.
             *
             * @return an {@link Optional} containing the value as an {@link Instant}, or empty if absent
             */
            public Optional<Instant> creationTime() {
                return Optional.ofNullable(creationTime).map(Instant::ofEpochSecond);
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
             * Returns the {@code channel_name} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> channelName() {
                return Optional.ofNullable(channelName);
            }

            /**
             * Returns the {@code channel_jid} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> channelJid() {
                return Optional.ofNullable(channelJid);
            }

            /**
             * Returns the {@code server_msg_id} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> serverMsgId() {
                return Optional.ofNullable(serverMsgId);
            }

            /**
             * Returns the {@code response_server_msg_id} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> responseServerMsgId() {
                return Optional.ofNullable(responseServerMsgId);
            }

            /**
             * Returns the {@code notify_name} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<String> notifyName() {
                return Optional.ofNullable(notifyName);
            }

            /**
             * Returns the {@code appeal} field.
             *
             * @return an {@link Optional} containing the value, or empty if absent
             */
            public Optional<Appeal> appeal() {
                return Optional.ofNullable(appeal);
            }

            /**
             * A parsed {@code Appeal} object.
             */
            public static final class Appeal {
                private final String state;
                private final String appealReason;
                private final Long creationTime;
                private final String reportId;
                private final String appealId;

                private Appeal(String state, String appealReason, Long creationTime, String reportId, String appealId) {
                    this.state = state;
                    this.appealReason = appealReason;
                    this.creationTime = creationTime;
                    this.reportId = reportId;
                    this.appealId = appealId;
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
                 * Returns the {@code appeal_reason} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<String> appealReason() {
                    return Optional.ofNullable(appealReason);
                }

                /**
                 * Returns the {@code creation_time} field.
                 *
                 * @return an {@link Optional} containing the value as an {@link Instant}, or empty if absent
                 */
                public Optional<Instant> creationTime() {
                    return Optional.ofNullable(creationTime).map(Instant::ofEpochSecond);
                }

                /**
                 * Returns the {@code report_id} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<String> reportId() {
                    return Optional.ofNullable(reportId);
                }

                /**
                 * Returns the {@code appeal_id} field.
                 *
                 * @return an {@link Optional} containing the value, or empty if absent
                 */
                public Optional<String> appealId() {
                    return Optional.ofNullable(appealId);
                }

                /**
                 * Parses a {@code Appeal} from the given JSON object.
                 *
                 * @param obj the JSON object to parse
                 * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
                 */
                static Optional<Appeal> of(JSONObject obj) {
                    if (obj == null) {
                        return Optional.empty();
                    }

                    var state = obj.getString("state");
                    var appealReason = obj.getString("appeal_reason");
                    var creationTime = obj.getLong("creation_time");
                    var reportId = obj.getString("report_id");
                    var appealId = obj.getString("appeal_id");
                    return Optional.of(new Appeal(state, appealReason, creationTime, reportId, appealId));
                }

                /**
                 * Parses a list of {@code Appeal} from the given JSON array.
                 *
                 * @param arr the JSON array to parse
                 * @return the list of parsed results, empty if {@code arr} is {@code null}
                 */
                static List<Appeal> ofArray(JSONArray arr) {
                    if (arr == null) {
                        return List.of();
                    }

                    var result = new ArrayList<Appeal>(arr.size());
                    for (var i = 0; i < arr.size(); i++) {
                        of(arr.getJSONObject(i)).ifPresent(result::add);
                    }
                    return result;
                }
            }

            /**
             * Parses a {@code ChannelsReports} from the given JSON object.
             *
             * @param obj the JSON object to parse
             * @return an {@link Optional} containing the parsed result, or empty if {@code obj} is {@code null}
             */
            static Optional<ChannelsReports> of(JSONObject obj) {
                if (obj == null) {
                    return Optional.empty();
                }

                var reportId = obj.getString("report_id");
                var status = obj.getString("status");
                var creationTime = obj.getLong("creation_time");
                var lastUpdateTime = obj.getLong("last_update_time");
                var channelName = obj.getString("channel_name");
                var channelJid = obj.getString("channel_jid");
                var serverMsgId = obj.getString("server_msg_id");
                var responseServerMsgId = obj.getString("response_server_msg_id");
                var notifyName = obj.getString("notify_name");
                var appeal = Appeal.of(obj.getJSONObject("appeal")).orElse(null);
                return Optional.of(new ChannelsReports(reportId, status, creationTime, lastUpdateTime, channelName, channelJid, serverMsgId, responseServerMsgId, notifyName, appeal));
            }

            /**
             * Parses a list of {@code ChannelsReports} from the given JSON array.
             *
             * @param arr the JSON array to parse
             * @return the list of parsed results, empty if {@code arr} is {@code null}
             */
            static List<ChannelsReports> ofArray(JSONArray arr) {
                if (arr == null) {
                    return List.of();
                }

                var result = new ArrayList<ChannelsReports>(arr.size());
                for (var i = 0; i < arr.size(); i++) {
                    of(arr.getJSONObject(i)).ifPresent(result::add);
                }
                return result;
            }
        }

        /**
         * Parses a {@link Response} from the raw JSON bytes of the
         * {@code <result>} child.
         *
         * @implNote WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports: mirrors the implicit
         * unwrapping that WA Web performs on the GraphQL response,
         * extracting the {@code xwa2_channels_reports} root.
         * @param json the UTF-8 encoded JSON payload
         * @return an {@link Optional} containing the parsed response, or
         *         empty if the envelope is missing expected fields
         */
        private static Optional<Response> of(byte[] json) {
            // WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports
            // Parses the raw JSON payload, bailing out if fastjson2 returns null
            var jsonObject = JSON.parseObject(json);
            if (jsonObject == null) {
                return Optional.empty();
            }

            // WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports
            // Descends into the standard GraphQL "data" envelope
            var data = jsonObject.getJSONObject("data");
            if (data == null) {
                return Optional.empty();
            }

            // WAWebMexFetchNewsletterReportsJob.mexFetchNewsletterReports
            // Extracts the operation-specific root keyed by xwa2_channels_reports
            var root = data.getJSONObject("xwa2_channels_reports");
            if (root == null) {
                return Optional.empty();
            }

            var channelsReports = ChannelsReports.ofArray(root.getJSONArray("channels_reports"));

            return Optional.of(new Response(channelsReports));
        }
    }
}
