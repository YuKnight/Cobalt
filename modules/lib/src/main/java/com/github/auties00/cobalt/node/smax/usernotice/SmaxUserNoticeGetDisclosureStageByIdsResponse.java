package com.github.auties00.cobalt.node.smax.usernotice;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
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
 *
 * @implNote {@code WASmaxUserNoticeGetDisclosureStageByIdsRPC.sendGetDisclosureStageByIdsRPC}
 *           tries {@code ClientSuccess} → {@code ClientError} →
 *           {@code ServerError} in order.
 */
public sealed interface SmaxUserNoticeGetDisclosureStageByIdsResponse extends SmaxOperation.Response
        permits SmaxUserNoticeGetDisclosureStageByIdsResponse.ClientSuccess, SmaxUserNoticeGetDisclosureStageByIdsResponse.ClientError, SmaxUserNoticeGetDisclosureStageByIdsResponse.ServerError {

    /**
     * Tries each {@link SmaxUserNoticeGetDisclosureStageByIdsResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxUserNoticeGetDisclosureStageByIdsRPC",
            exports = "sendGetDisclosureStageByIdsRPC",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxUserNoticeGetDisclosureStageByIdsResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var clientSuccess = ClientSuccess.of(node, request);
        if (clientSuccess.isPresent()) {
            return clientSuccess;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * The {@code ClientSuccess} reply variant. The relay returned
     * zero or more disclosure-stage entries.
     *
     * @implNote {@code WASmaxInUserNoticeGetDisclosureStageByIdsResponseClientSuccess.parseGetDisclosureStageByIdsResponseClientSuccess}
     *           validates the IQ-result envelope and projects every
     *           {@code <notice>} child via
     *           {@code mapChildrenWithTag(notice, 0, ∞)}. Per-notice
     *           {@code version} and {@code type} are optional in
     *           this variant (unlike {@link SmaxUserNoticeGetDisclosures}
     *           where they are required).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeGetDisclosureStageByIdsResponseClientSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeIQResultResponseMixin")
    final class ClientSuccess implements SmaxUserNoticeGetDisclosureStageByIdsResponse {
        /**
         * The list of disclosure-stage entries returned by the relay.
         */
        private final List<DisclosureStageNotice> notices;

        /**
         * Constructs a new {@code ClientSuccess} reply.
         *
         * @param notices the list of stage entries. Never
         *                {@code null}
         */
        public ClientSuccess(List<DisclosureStageNotice> notices) {
            this.notices = List.copyOf(Objects.requireNonNullElse(notices, List.of()));
        }

        /**
         * Returns the list of stage entries.
         *
         * @return an unmodifiable list. Never {@code null}
         */
        public List<DisclosureStageNotice> notices() {
            return notices;
        }

        /**
         * Tries to parse a {@link ClientSuccess} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInUserNoticeGetDisclosureStageByIdsResponseClientSuccess",
                exports = "parseGetDisclosureStageByIdsResponseClientSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientSuccess> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var notices = new ArrayList<DisclosureStageNotice>();
            for (var noticeNode : node.getChildren("notice")) {
                var notice = DisclosureStageNotice.of(noticeNode).orElse(null);
                if (notice == null) {
                    return Optional.empty();
                }
                notices.add(notice);
            }
            return Optional.of(new ClientSuccess(notices));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ClientSuccess) obj;
            return Objects.equals(this.notices, that.notices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(notices);
        }

        @Override
        public String toString() {
            return "SmaxUserNoticeGetDisclosureStageByIdsResponse.ClientSuccess[notices=" + notices + ']';
        }

        /**
         * A single {@code <notice>} entry. Projects the
         * per-disclosure stage marker carried in the reply.
         *
         * <p>Note: unlike
         * {@link SmaxUserNoticeGetDisclosuresResponse.ClientSuccess.DisclosureNotice},
         * the {@code version} and {@code type} attributes are
         * optional. The StageByIds RPC only commits to surfacing
         * the {@code (id, t, stage)} triple.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeStageMixin")
        public static final class DisclosureStageNotice {
            /**
             * The relay-side timestamp ({@code t}). Seconds since
             * epoch.
             */
            private final long timestampSeconds;

            /**
             * The optional notice version ({@code version}, ≥ 1).
             */
            private final Integer version;

            /**
             * The optional notice type ({@code type}, ≥ 0).
             */
            private final Integer type;

            /**
             * The disclosure id from
             * {@code WASmaxInUserNoticeStageMixin}.
             */
            private final long noticeId;

            /**
             * The current stage (0..1000) of the disclosure.
             */
            private final int stage;

            /**
             * Constructs a new disclosure-stage notice.
             *
             * @param timestampSeconds the relay-side timestamp
             * @param version          the optional notice version
             *                         (≥ 1). May be {@code null}
             * @param type             the optional notice type (≥ 0);
             *                         may be {@code null}
             * @param noticeId         the disclosure id
             * @param stage            the current stage (0..1000)
             */
            public DisclosureStageNotice(long timestampSeconds, Integer version, Integer type,
                                         long noticeId, int stage) {
                this.timestampSeconds = timestampSeconds;
                this.version = version;
                this.type = type;
                this.noticeId = noticeId;
                this.stage = stage;
            }

            /**
             * Returns the relay-side timestamp.
             *
             * @return the timestamp in seconds
             */
            public long timestampSeconds() {
                return timestampSeconds;
            }

            /**
             * Returns the optional notice version.
             *
             * @return an {@link Optional} carrying the version
             */
            public Optional<Integer> version() {
                return Optional.ofNullable(version);
            }

            /**
             * Returns the optional notice type.
             *
             * @return an {@link Optional} carrying the type
             */
            public Optional<Integer> type() {
                return Optional.ofNullable(type);
            }

            /**
             * Returns the disclosure id.
             *
             * @return the id
             */
            public long noticeId() {
                return noticeId;
            }

            /**
             * Returns the current stage.
             *
             * @return the stage (0..1000)
             */
            public int stage() {
                return stage;
            }

            /**
             * Tries to parse a stage notice from the given
             * {@code <notice>} child.
             *
             * @param node the {@code <notice>} child. Never
             *             {@code null}
             * @return an {@link Optional} carrying the parsed entry
             */
            @WhatsAppWebExport(moduleName = "WASmaxInUserNoticeGetDisclosureStageByIdsResponseClientSuccess",
                    exports = "parseGetDisclosureStageByIdsResponseClientSuccessNotice",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<DisclosureStageNotice> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("notice")) {
                    return Optional.empty();
                }
                var timestampOpt = node.getAttributeAsLong("t");
                if (timestampOpt.isEmpty()) {
                    return Optional.empty();
                }
                var versionAttr = node.getAttributeAsInt("version").orElse(-1);
                Integer version = null;
                if (versionAttr >= 1) {
                    version = versionAttr;
                } else if (node.hasAttribute("version")) {
                    // present but out of range
                    return Optional.empty();
                }
                var typeAttr = node.getAttributeAsInt("type").orElse(-1);
                Integer type = null;
                if (typeAttr >= 0) {
                    type = typeAttr;
                } else if (node.hasAttribute("type")) {
                    return Optional.empty();
                }
                var idOpt = node.getAttributeAsLong("id");
                if (idOpt.isEmpty()) {
                    return Optional.empty();
                }
                var stageOpt = node.getAttributeAsInt("stage");
                if (stageOpt.isEmpty() || stageOpt.getAsInt() < 0 || stageOpt.getAsInt() > 1000) {
                    return Optional.empty();
                }
                return Optional.of(new DisclosureStageNotice(timestampOpt.getAsLong(),
                        version, type, idOpt.getAsLong(), stageOpt.getAsInt()));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (DisclosureStageNotice) obj;
                return this.timestampSeconds == that.timestampSeconds
                        && this.noticeId == that.noticeId
                        && this.stage == that.stage
                        && Objects.equals(this.version, that.version)
                        && Objects.equals(this.type, that.type);
            }

            @Override
            public int hashCode() {
                return Objects.hash(timestampSeconds, version, type, noticeId, stage);
            }

            @Override
            public String toString() {
                return "SmaxUserNoticeGetDisclosureStageByIdsResponse.ClientSuccess.DisclosureStageNotice[timestampSeconds="
                        + timestampSeconds
                        + ", version=" + version
                        + ", type=" + type
                        + ", noticeId=" + noticeId
                        + ", stage=" + stage + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request as malformed (always {@code 400 bad-request}).
     *
     * @implNote {@code WASmaxInUserNoticeGetDisclosureStageByIdsResponseClientError.parseGetDisclosureStageByIdsResponseClientError}
     *           composes the IQ-error envelope check with
     *           {@code WASmaxInUserNoticeIQErrorBadRequestMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeGetDisclosureStageByIdsResponseClientError")
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeIQErrorBadRequestMixin")
    final class ClientError implements SmaxUserNoticeGetDisclosureStageByIdsResponse {
        /**
         * The numeric error code. Always {@code 400}.
         */
        private final int errorCode;

        /**
         * The human-readable error text. Always {@code "bad-request"}
         * when present.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the error code
         * @param errorText the optional text. May be {@code null}
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the error text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInUserNoticeGetDisclosureStageByIdsResponseClientError",
                exports = "parseGetDisclosureStageByIdsResponseClientError",
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
            return "SmaxUserNoticeGetDisclosureStageByIdsResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered
     * an internal failure while processing the request.
     *
     * <p>The user-notice domain projects two documented variants
     * ({@code internal-server-error/500}, {@code rate-overlimit/429});
     * Cobalt collapses them into the single
     * {@code (errorCode, errorText)} pair.
     *
     * @implNote {@code WASmaxInUserNoticeGetDisclosureStageByIdsResponseServerError.parseGetDisclosureStageByIdsResponseServerError}
     *           composes the error-envelope check with
     *           {@code WASmaxInUserNoticeUserNoticeServerError}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeGetDisclosureStageByIdsResponseServerError")
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeUserNoticeServerError")
    final class ServerError implements SmaxUserNoticeGetDisclosureStageByIdsResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text. May be
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
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInUserNoticeGetDisclosureStageByIdsResponseServerError",
                exports = "parseGetDisclosureStageByIdsResponseServerError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ServerError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                // The user-notice ServerError disjunction includes 429 (rate-overlimit) which the
                // shared parseServerError helper treats as a client error; fall back to parseClientError
                // for the 429 case only.
                var clientEnvelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
                if (clientEnvelope == null || clientEnvelope.code() != 429) {
                    return Optional.empty();
                }
                return Optional.of(new ServerError(clientEnvelope.code(), clientEnvelope.text()));
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
            return "SmaxUserNoticeGetDisclosureStageByIdsResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
