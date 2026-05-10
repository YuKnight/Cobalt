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
 */
public sealed interface SmaxUserNoticeGetDisclosuresResponse extends SmaxOperation.Response
        permits SmaxUserNoticeGetDisclosuresResponse.ClientSuccess, SmaxUserNoticeGetDisclosuresResponse.ClientError, SmaxUserNoticeGetDisclosuresResponse.ServerError {

    /**
     * Tries each {@link SmaxUserNoticeGetDisclosuresResponse} variant in priority order and
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
    @WhatsAppWebExport(moduleName = "WASmaxUserNoticeGetDisclosuresRPC",
            exports = "sendGetDisclosuresRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxUserNoticeGetDisclosuresResponse> of(Node node, Node request) {
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
     * zero or more disclosure entries.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeGetDisclosuresResponseClientSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeIQResultResponseMixin")
    final class ClientSuccess implements SmaxUserNoticeGetDisclosuresResponse {
        /**
         * The list of disclosure notices returned by the relay.
         */
        private final List<DisclosureNotice> notices;

        /**
         * Constructs a new {@code ClientSuccess} reply.
         *
         * @param notices the list of disclosure notices. Never
         *                {@code null}
         */
        public ClientSuccess(List<DisclosureNotice> notices) {
            this.notices = List.copyOf(Objects.requireNonNullElse(notices, List.of()));
        }

        /**
         * Returns the list of disclosure notices.
         *
         * @return an unmodifiable list. Never {@code null}
         */
        public List<DisclosureNotice> notices() {
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
        @WhatsAppWebExport(moduleName = "WASmaxInUserNoticeGetDisclosuresResponseClientSuccess",
                exports = "parseGetDisclosuresResponseClientSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientSuccess> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var notices = new ArrayList<DisclosureNotice>();
            for (var noticeNode : node.getChildren("notice")) {
                var notice = DisclosureNotice.of(noticeNode).orElse(null);
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
            return "SmaxUserNoticeGetDisclosuresResponse.ClientSuccess[notices=" + notices + ']';
        }

        /**
         * A single {@code <notice>} entry. The relay's projection of
         * one disclosure carrying the per-notice timestamp,
         * version/type pair, and the {@code WASmaxInUserNoticeStageMixin}
         * (id + stage) progression marker.
         */
        @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeStageMixin")
        public static final class DisclosureNotice {
            /**
             * The relay-side timestamp ({@code t}). Seconds since
             * epoch.
             */
            private final long timestampSeconds;

            /**
             * The notice version ({@code version}). At least 1.
             */
            private final int version;

            /**
             * The notice type ({@code type}). Non-negative.
             */
            private final int type;

            /**
             * The notice id from {@code WASmaxInUserNoticeStageMixin}.
             * Uniquely identifies the disclosure across versions.
             */
            private final long noticeId;

            /**
             * The current stage (0..1000) of the disclosure.
             */
            private final int stage;

            /**
             * Constructs a new disclosure notice.
             *
             * @param timestampSeconds the relay-side timestamp
             * @param version          the notice version (≥ 1)
             * @param type             the notice type (≥ 0)
             * @param noticeId         the disclosure id
             * @param stage            the current stage (0..1000)
             */
            public DisclosureNotice(long timestampSeconds, int version, int type,
                                    long noticeId, int stage) {
                this.timestampSeconds = timestampSeconds;
                this.version = version;
                this.type = type;
                this.noticeId = noticeId;
                this.stage = stage;
            }

            /**
             * Returns the relay-side timestamp in seconds.
             *
             * @return the timestamp
             */
            public long timestampSeconds() {
                return timestampSeconds;
            }

            /**
             * Returns the notice version.
             *
             * @return the version
             */
            public int version() {
                return version;
            }

            /**
             * Returns the notice type.
             *
             * @return the type
             */
            public int type() {
                return type;
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
             * Tries to parse a disclosure notice from the given
             * {@code <notice>} child.
             *
             * @param node the {@code <notice>} child. Never
             *             {@code null}
             * @return an {@link Optional} carrying the parsed entry
             */
            @WhatsAppWebExport(moduleName = "WASmaxInUserNoticeGetDisclosuresResponseClientSuccess",
                    exports = "parseGetDisclosuresResponseClientSuccessNotice",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<DisclosureNotice> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("notice")) {
                    return Optional.empty();
                }
                var timestampOpt = node.getAttributeAsLong("t");
                if (timestampOpt.isEmpty()) {
                    return Optional.empty();
                }
                var versionOpt = node.getAttributeAsInt("version");
                if (versionOpt.isEmpty() || versionOpt.getAsInt() < 1) {
                    return Optional.empty();
                }
                var typeOpt = node.getAttributeAsInt("type");
                if (typeOpt.isEmpty() || typeOpt.getAsInt() < 0) {
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
                return Optional.of(new DisclosureNotice(timestampOpt.getAsLong(),
                        versionOpt.getAsInt(), typeOpt.getAsInt(),
                        idOpt.getAsLong(), stageOpt.getAsInt()));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (DisclosureNotice) obj;
                return this.timestampSeconds == that.timestampSeconds
                        && this.version == that.version
                        && this.type == that.type
                        && this.noticeId == that.noticeId
                        && this.stage == that.stage;
            }

            @Override
            public int hashCode() {
                return Objects.hash(timestampSeconds, version, type, noticeId, stage);
            }

            @Override
            public String toString() {
                return "SmaxUserNoticeGetDisclosuresResponse.ClientSuccess.DisclosureNotice[timestampSeconds="
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
     */
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeGetDisclosuresResponseClientError")
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeIQErrorBadRequestMixin")
    final class ClientError implements SmaxUserNoticeGetDisclosuresResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInUserNoticeGetDisclosuresResponseClientError",
                exports = "parseGetDisclosuresResponseClientError",
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
            return "SmaxUserNoticeGetDisclosuresResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered
     * an internal failure while processing the request.
     *
     * <p>The user-notice domain projects two documented variants
     * ({@code internal-server-error/500}, {@code rate-overlimit/429}).
     * Note that {@code 429} maps to "too many requests" but the WA
     * Web disjunction lumps it under {@code ServerError}. Cobalt
     * collapses them into the single {@code (errorCode, errorText)}
     * pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeGetDisclosuresResponseServerError")
    @WhatsAppWebModule(moduleName = "WASmaxInUserNoticeUserNoticeServerError")
    final class ServerError implements SmaxUserNoticeGetDisclosuresResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInUserNoticeGetDisclosuresResponseServerError",
                exports = "parseGetDisclosuresResponseServerError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ServerError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null);
            if (envelope == null) {
                // The user-notice ServerError disjunction includes 429 (rate-overlimit) which
                // the shared parseServerError treats as a client error; fall back to parseClientError
                // and only accept the 429 case.
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
            return "SmaxUserNoticeGetDisclosuresResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
