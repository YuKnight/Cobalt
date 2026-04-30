package com.github.auties00.cobalt.node.iq.tos;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link IqQueryTosRequest}.
 *
 * @implNote {@code WAWebTosJob.queryTosState}'s
 *           {@code WAWebBackendErrors.ServerStatusCodeError} throw
 *           collapses both client and server error envelopes. Cobalt
 *           splits them into typed {@code ClientError} /
 *           {@code ServerError} variants.
 */
public sealed interface IqQueryTosResponse extends IqOperation.Response
        permits IqQueryTosResponse.Success, IqQueryTosResponse.ClientError, IqQueryTosResponse.ServerError {

    /**
     * The minimum server-recommended refresh interval in seconds
     * ({@code 7200} = 2h). Replies below this floor are clamped to
     * {@link #DEFAULT_TOS_REFRESH_INTERVAL_SECONDS}.
     *
     * @implNote {@code WAWebTosJob} local constant {@code e=7200}.
     */
    @WhatsAppWebExport(moduleName = "WAWebTosJob",
            exports = "queryTosState", adaptation = WhatsAppAdaptation.DIRECT)
    int MIN_TOS_REFRESH_INTERVAL_SECONDS = 7200;

    /**
     * The maximum server-recommended refresh interval in seconds
     * ({@code 259200} = 3d). Replies above this ceiling are clamped to
     * {@link #DEFAULT_TOS_REFRESH_INTERVAL_SECONDS}.
     *
     * @implNote {@code WAWebTosJob} local constant {@code s=259200}.
     */
    @WhatsAppWebExport(moduleName = "WAWebTosJob",
            exports = "queryTosState", adaptation = WhatsAppAdaptation.DIRECT)
    int MAX_TOS_REFRESH_INTERVAL_SECONDS = 259200;

    /**
     * The default server-recommended refresh interval in seconds
     * ({@code 86400} = 24h). Used as a fallback when the reply's
     * {@code refresh} value is out of the {@code [MIN, MAX]} range.
     *
     * @implNote {@code WAWebTosJob.DEFAULT_TOS_REFRESH_INTERVAL = u = 86400}.
     */
    @WhatsAppWebExport(moduleName = "WAWebTosJob",
            exports = "DEFAULT_TOS_REFRESH_INTERVAL", adaptation = WhatsAppAdaptation.DIRECT)
    int DEFAULT_TOS_REFRESH_INTERVAL_SECONDS = 86400;

    /**
     * Tries each {@link IqQueryTosResponse} variant in priority order and returns
     * the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay.
     *                Never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebTosJob",
            exports = "queryTosState", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqQueryTosResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay returned the
     * current notice state for every requested id.
     *
     * <p>Carries the clamped {@code refresh} interval and the list of
     * per-notice {@link NoticeState} entries.
     *
     * @implNote {@code WAWebTosJob.tosNotices} parser:
     *           {@code child("tos").attrInt("refresh")} clamped to
     *           {@code [7200, 259200]} (else {@code 86400}). Each
     *           {@code <notice id state/>} is parsed via
     *           {@code maybeAttrString("state") !== "false"}.
     */
    @WhatsAppWebModule(moduleName = "WAWebTosJob")
    final class Success implements IqQueryTosResponse {
        /**
         * The server-recommended refresh interval in seconds, clamped
         * to {@code [MIN_TOS_REFRESH_INTERVAL_SECONDS,
         * MAX_TOS_REFRESH_INTERVAL_SECONDS]} (falling back to
         * {@link #DEFAULT_TOS_REFRESH_INTERVAL_SECONDS} on out-of-range).
         */
        private final int refreshIntervalSeconds;

        /**
         * The list of per-notice state entries returned by the relay.
         */
        private final List<NoticeState> notices;

        /**
         * Constructs a new successful reply.
         *
         * @param refreshIntervalSeconds the clamped refresh interval
         * @param notices                the list of notice states.
         *                               Never {@code null}
         * @throws NullPointerException if {@code notices} is
         *                              {@code null}
         */
        public Success(int refreshIntervalSeconds, List<NoticeState> notices) {
            this.refreshIntervalSeconds = refreshIntervalSeconds;
            Objects.requireNonNull(notices, "notices cannot be null");
            this.notices = List.copyOf(notices);
        }

        /**
         * Returns the server-recommended refresh interval in seconds.
         *
         * @return the refresh interval
         */
        public int refreshIntervalSeconds() {
            return refreshIntervalSeconds;
        }

        /**
         * Returns the unmodifiable list of per-notice state entries.
         *
         * @return the notice states. Never {@code null}
         */
        public List<NoticeState> notices() {
            return notices;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the success
         *         schema
         */
        @WhatsAppWebExport(moduleName = "WAWebTosJob",
                exports = "queryTosState", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var tosChild = node.getChild("tos").orElse(null);
            if (tosChild == null) {
                return Optional.empty();
            }
            var rawRefresh = tosChild.getAttributeAsInt("refresh").orElse(-1);
            if (rawRefresh < 0) {
                return Optional.empty();
            }
            var clampedRefresh = rawRefresh;
            if (clampedRefresh > MAX_TOS_REFRESH_INTERVAL_SECONDS
                    || clampedRefresh < MIN_TOS_REFRESH_INTERVAL_SECONDS) {
                clampedRefresh = DEFAULT_TOS_REFRESH_INTERVAL_SECONDS;
            }
            var noticeChildren = tosChild.getChildren("notice");
            var notices = new ArrayList<NoticeState>(noticeChildren.size());
            for (var noticeNode : noticeChildren) {
                var id = noticeNode.getAttributeAsString("id").orElse(null);
                if (id == null) {
                    return Optional.empty();
                }
                var stateAttr = noticeNode.getAttributeAsString("state").orElse(null);
                var accepted = stateAttr == null || !stateAttr.equals("false");
                notices.add(new NoticeState(id, accepted));
            }
            return Optional.of(new Success(clampedRefresh, notices));
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
            return this.refreshIntervalSeconds == that.refreshIntervalSeconds
                    && Objects.equals(this.notices, that.notices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(refreshIntervalSeconds, notices);
        }

        @Override
        public String toString() {
            return "IqQueryTosResponse.Success[refreshIntervalSeconds="
                    + refreshIntervalSeconds + ", notices=" + notices + ']';
        }

        /**
         * Per-notice state entry projected from one {@code <notice/>}
         * child of the {@code <tos/>} reply envelope.
         *
         * @implNote {@code WAWebTosJob.tosNotices}: each notice carries
         *           an {@code id} (verbatim) and an interpreted
         *           {@code state} flag (any value other than
         *           {@code "false"} maps to {@code true}. Missing
         *           attribute defaults to {@code true}).
         */
        @WhatsAppWebModule(moduleName = "WAWebTosJob")
        public static final class NoticeState {
            /**
             * The notice id (echoed from the corresponding request
             * entry).
             */
            private final String id;

            /**
             * The accepted-state flag. {@code true} when the user has
             * accepted the notice (or when the relay omits the
             * {@code state} attribute), {@code false} only when the
             * relay explicitly returns {@code state="false"}.
             */
            private final boolean accepted;

            /**
             * Constructs a new notice state entry.
             *
             * @param id       the notice id. Never {@code null}
             * @param accepted the accepted-state flag
             * @throws NullPointerException if {@code id} is
             *                              {@code null}
             */
            public NoticeState(String id, boolean accepted) {
                this.id = Objects.requireNonNull(id, "id cannot be null");
                this.accepted = accepted;
            }

            /**
             * Returns the notice id.
             *
             * @return the id. Never {@code null}
             */
            public String id() {
                return id;
            }

            /**
             * Returns the accepted-state flag.
             *
             * @return {@code true} when the notice has been accepted,
             *         {@code false} otherwise
             */
            public boolean accepted() {
                return accepted;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (NoticeState) obj;
                return this.accepted == that.accepted
                        && Objects.equals(this.id, that.id);
            }

            @Override
            public int hashCode() {
                return Objects.hash(id, accepted);
            }

            @Override
            public String toString() {
                return "IqQueryTosResponse.Success.NoticeState[id="
                        + id + ", accepted=" + accepted + ']';
            }
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * query as malformed or unauthorised.
     *
     * @implNote {@code WAWebTosJob.queryTosState} throws
     *           {@code ServerStatusCodeError(code, text)} on any
     *           non-result. Cobalt narrows to codes {@code [400, 500)}.
     */
    @WhatsAppWebModule(moduleName = "WAWebTosJob")
    final class ClientError implements IqQueryTosResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text. May be
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
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
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
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebTosJob",
                exports = "queryTosState", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqQueryTosResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure while processing the query.
     *
     * @implNote {@code WAWebTosJob.queryTosState} folds server failures
     *           into the same {@code ServerStatusCodeError} throw.
     *           Cobalt narrows to codes {@code >= 500}.
     */
    @WhatsAppWebModule(moduleName = "WAWebTosJob")
    final class ServerError implements IqQueryTosResponse {
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
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
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
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebTosJob",
                exports = "queryTosState", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqQueryTosResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
