package com.github.auties00.cobalt.node.smax.groups;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 */
public sealed interface SmaxGroupsGetReportedMessagesResponse extends SmaxOperation.Response
        permits SmaxGroupsGetReportedMessagesResponse.Success, SmaxGroupsGetReportedMessagesResponse.ClientError, SmaxGroupsGetReportedMessagesResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsGetReportedMessagesResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsGetReportedMessagesRPC",
            exports = "sendGetReportedMessagesRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsGetReportedMessagesResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var success = Success.of(node, request);
        if (success.isPresent()) {
            return success;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code Success} reply variant — carries the list of
     * pending {@link Report} entries.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetReportedMessagesResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGroupAddressingModeMixin")
    final class Success implements SmaxGroupsGetReportedMessagesResponse {
        /**
         * The list of reported-message entries.
         */
        private final List<Report> reports;

        /**
         * Constructs a new successful reply.
         *
         * @param reports the per-report entries; never {@code null}
         * @throws NullPointerException if {@code reports} is
         *                              {@code null}
         */
        public Success(List<Report> reports) {
            Objects.requireNonNull(reports, "reports cannot be null");
            this.reports = List.copyOf(reports);
        }

        /**
         * Returns the per-report entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<Report> reports() {
            return reports;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetReportedMessagesResponseSuccess",
                exports = "parseGetReportedMessagesResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var wrapper = node.getChild("reports").orElse(null);
            if (wrapper == null) {
                return Optional.empty();
            }
            var reportNodes = wrapper.getChildren("report");
            var reports = new ArrayList<Report>(reportNodes.size());
            for (var reportNode : reportNodes) {
                var report = Report.of(reportNode).orElse(null);
                if (report == null) {
                    return Optional.empty();
                }
                reports.add(report);
            }
            return Optional.of(new Success(reports));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Success) obj;
            return Objects.equals(this.reports, that.reports);
        }

        @Override
        public int hashCode() {
            return Objects.hash(reports);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetReportedMessagesResponse.Success[reports=" + reports + ']';
        }
    }

    /**
     * Per-report projection — carries the reported message stanza
     * id alongside the list of reporters.
     */
    final class Report {
        /**
         * The stanza-id of the reported group message.
         */
        private final String messageId;

        /**
         * The reporters who filed reports against the message.
         */
        private final List<Reporter> reporters;

        /**
         * Constructs a report entry.
         *
         * @param messageId the reported message stanza id; never
         *                  {@code null}
         * @param reporters the reporter list; never {@code null}
         *                  and never empty
         * @throws NullPointerException     if {@code messageId} or
         *                                  {@code reporters} is
         *                                  {@code null}
         * @throws IllegalArgumentException if {@code reporters} is
         *                                  empty
         */
        public Report(String messageId, List<Reporter> reporters) {
            this.messageId = Objects.requireNonNull(messageId, "messageId cannot be null");
            Objects.requireNonNull(reporters, "reporters cannot be null");
            if (reporters.isEmpty()) {
                throw new IllegalArgumentException("reporters cannot be empty");
            }
            this.reporters = List.copyOf(reporters);
        }

        /**
         * Returns the reported message stanza id.
         *
         * @return the id; never {@code null}
         */
        public String messageId() {
            return messageId;
        }

        /**
         * Returns the reporter list.
         *
         * @return an unmodifiable list of reporters; never empty
         */
        public List<Reporter> reporters() {
            return reporters;
        }

        /**
         * Tries to parse a {@link Report} from a {@code <report>}
         * child.
         *
         * @param node the {@code <report>} child node
         * @return an {@link Optional} carrying the parsed report,
         *         or empty when the child does not match the
         *         schema
         */
        public static Optional<Report> of(Node node) {
            Objects.requireNonNull(node, "node cannot be null");
            if (!node.hasDescription("report")) {
                return Optional.empty();
            }
            var messageId = node.getAttributeAsString("message_id").orElse(null);
            if (messageId == null) {
                return Optional.empty();
            }
            var reporterNodes = node.getChildren("reporter");
            if (reporterNodes.isEmpty()) {
                return Optional.empty();
            }
            var reporters = new ArrayList<Reporter>(reporterNodes.size());
            for (var reporterNode : reporterNodes) {
                var reporter = Reporter.of(reporterNode).orElse(null);
                if (reporter == null) {
                    return Optional.empty();
                }
                reporters.add(reporter);
            }
            return Optional.of(new Report(messageId, reporters));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Report) obj;
            return Objects.equals(this.messageId, that.messageId)
                    && Objects.equals(this.reporters, that.reporters);
        }

        @Override
        public int hashCode() {
            return Objects.hash(messageId, reporters);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetReportedMessagesResponse.Report[messageId=" + messageId
                    + ", reporters=" + reporters + ']';
        }
    }

    /**
     * Per-reporter projection — carries the reporting user's JID
     * alongside the unix-time timestamp the report was filed at.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsIdentityMixin")
    final class Reporter {
        /**
         * The reporter's user JID.
         */
        private final Jid jid;

        /**
         * The unix-time timestamp at which the report was filed.
         */
        private final long timestamp;

        /**
         * Constructs a reporter entry.
         *
         * @param jid       the reporter's JID; never {@code null}
         * @param timestamp the report timestamp
         * @throws NullPointerException     if {@code jid} is
         *                                  {@code null}
         * @throws IllegalArgumentException if {@code timestamp} is
         *                                  negative
         */
        public Reporter(Jid jid, long timestamp) {
            this.jid = Objects.requireNonNull(jid, "jid cannot be null");
            if (timestamp < 0) {
                throw new IllegalArgumentException("timestamp must be non-negative");
            }
            this.timestamp = timestamp;
        }

        /**
         * Returns the reporter's JID.
         *
         * @return the JID; never {@code null}
         */
        public Jid jid() {
            return jid;
        }

        /**
         * Returns the report timestamp.
         *
         * @return the timestamp in unix-seconds
         */
        public long timestamp() {
            return timestamp;
        }

        /**
         * Tries to parse a {@link Reporter} from a
         * {@code <reporter>} child.
         *
         * @param node the {@code <reporter>} child node
         * @return an {@link Optional} carrying the parsed reporter,
         *         or empty when the child does not match the
         *         schema
         */
        public static Optional<Reporter> of(Node node) {
            Objects.requireNonNull(node, "node cannot be null");
            if (!node.hasDescription("reporter")) {
                return Optional.empty();
            }
            var jid = node.getAttributeAsJid("jid").orElse(null);
            if (jid == null) {
                return Optional.empty();
            }
            var timestampOptional = node.getAttributeAsLong("timestamp");
            if (timestampOptional.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Reporter(jid, timestampOptional.getAsLong()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Reporter) obj;
            return this.timestamp == that.timestamp
                    && Objects.equals(this.jid, that.jid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jid, timestamp);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetReportedMessagesResponse.Reporter[jid=" + jid
                    + ", timestamp=" + timestamp + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetReportedMessagesResponseClientError")
    final class ClientError implements SmaxGroupsGetReportedMessagesResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied
         * one.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ClientError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetReportedMessagesResponseClientError",
                exports = "parseGetReportedMessagesResponseClientError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ClientError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ClientError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetReportedMessagesResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetReportedMessagesResponseServerError")
    final class ServerError implements SmaxGroupsGetReportedMessagesResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied
         * one.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text; may be
         *                  {@code null}
         */
        public ServerError(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the error code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional human-readable error text.
         *
         * @return an {@link Optional} carrying the error text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetReportedMessagesResponseServerError",
                exports = "parseGetReportedMessagesResponseServerError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ServerError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            return Optional.of(new ServerError(envelope.code(), envelope.text()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ServerError) obj;
            return this.errorCode == that.errorCode && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetReportedMessagesResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
