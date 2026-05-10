package com.github.auties00.cobalt.node.smax.abprops;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay.
 */
public sealed interface SmaxAbPropsGetGroupExperimentConfigResponse extends SmaxOperation.Response
        permits SmaxAbPropsGetGroupExperimentConfigResponse.Success, SmaxAbPropsGetGroupExperimentConfigResponse.ClientError, SmaxAbPropsGetGroupExperimentConfigResponse.ServerError {

    /**
     * Tries each {@link SmaxAbPropsGetGroupExperimentConfigResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza. Never {@code null}
     * @param request the original outbound stanza. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxAbPropsGetGroupExperimentConfigRPC",
            exports = "sendGetGroupExperimentConfigRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxAbPropsGetGroupExperimentConfigResponse> of(Node node, Node request) {
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
     * materialised group-scoped props bundle.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInAbPropsGetGroupExperimentConfigResponseSuccess")
    @WhatsAppWebModule(moduleName = "WASmaxInAbPropsIQResultResponseMixin")
    final class Success implements SmaxAbPropsGetGroupExperimentConfigResponse {
        /**
         * The relay-returned content hash.
         */
        private final String propsHash;

        /**
         * The relay-returned refresh-cooldown hint, in seconds.
         */
        private final Integer propsRefresh;

        /**
         * The relay-returned refresh id.
         */
        private final Integer propsRefreshId;

        /**
         * The relay-returned A/B framework key, when supplied.
         */
        private final String propsAbKey;

        /**
         * The raw {@code <props/>} subtree.
         */
        private final Node propsNode;

        /**
         * Constructs a new success projection.
         *
         * @param propsHash      the relay-returned hash. May be
         *                       {@code null}
         * @param propsRefresh   the relay-returned refresh-cooldown;
         *                       may be {@code null}
         * @param propsRefreshId the relay-returned refresh id. May be
         *                       {@code null}
         * @param propsAbKey     the relay-returned ab-key. May be
         *                       {@code null}
         * @param propsNode      the raw {@code <props/>} subtree. Never
         *                       {@code null}
         * @throws NullPointerException if {@code propsNode} is
         *                              {@code null}
         */
        public Success(String propsHash, Integer propsRefresh, Integer propsRefreshId,
                       String propsAbKey, Node propsNode) {
            this.propsHash = propsHash;
            this.propsRefresh = propsRefresh;
            this.propsRefreshId = propsRefreshId;
            this.propsAbKey = propsAbKey;
            this.propsNode = Objects.requireNonNull(propsNode, "propsNode cannot be null");
        }

        /**
         * Returns the relay-returned content hash.
         *
         * @return an {@link Optional} carrying the hash, or empty
         */
        public Optional<String> propsHash() {
            return Optional.ofNullable(propsHash);
        }

        /**
         * Returns the relay-returned refresh-cooldown.
         *
         * @return an {@link Optional} carrying the cooldown, or empty
         */
        public Optional<Integer> propsRefresh() {
            return Optional.ofNullable(propsRefresh);
        }

        /**
         * Returns the relay-returned refresh id.
         *
         * @return an {@link Optional} carrying the refresh id, or
         *         empty
         */
        public Optional<Integer> propsRefreshId() {
            return Optional.ofNullable(propsRefreshId);
        }

        /**
         * Returns the relay-returned A/B framework key.
         *
         * @return an {@link Optional} carrying the ab-key, or empty
         */
        public Optional<String> propsAbKey() {
            return Optional.ofNullable(propsAbKey);
        }

        /**
         * Returns the raw {@code <props/>} subtree.
         *
         * @return the {@code <props/>} node. Never {@code null}
         */
        public Node propsNode() {
            return propsNode;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInAbPropsGetGroupExperimentConfigResponseSuccess",
                exports = "parseGetGroupExperimentConfigResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var props = node.getChild("props").orElse(null);
            if (props == null) {
                return Optional.empty();
            }
            var hash = props.getAttributeAsString("hash").orElse(null);
            var refresh = props.getAttributeAsInt("refresh").isPresent()
                    ? props.getAttributeAsInt("refresh").getAsInt()
                    : null;
            var refreshId = props.getAttributeAsInt("refresh_id").isPresent()
                    ? props.getAttributeAsInt("refresh_id").getAsInt()
                    : null;
            var abKey = props.getAttributeAsString("ab_key").orElse(null);
            return Optional.of(new Success(hash, refresh, refreshId, abKey, props));
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
            return Objects.equals(this.propsHash, that.propsHash)
                    && Objects.equals(this.propsRefresh, that.propsRefresh)
                    && Objects.equals(this.propsRefreshId, that.propsRefreshId)
                    && Objects.equals(this.propsAbKey, that.propsAbKey)
                    && Objects.equals(this.propsNode, that.propsNode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(propsHash, propsRefresh, propsRefreshId, propsAbKey, propsNode);
        }

        @Override
        public String toString() {
            return "SmaxAbPropsGetGroupExperimentConfigResponse.Success[propsHash=" + propsHash
                    + ", propsRefresh=" + propsRefresh
                    + ", propsRefreshId=" + propsRefreshId
                    + ", propsAbKey=" + propsAbKey + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request as malformed or unauthorised.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInAbPropsGetGroupExperimentConfigResponseErrorNoRetry")
    final class ClientError implements SmaxAbPropsGetGroupExperimentConfigResponse {
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
         * @return an {@link Optional} carrying the text, or empty
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
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInAbPropsGetGroupExperimentConfigResponseErrorNoRetry",
                exports = "parseGetGroupExperimentConfigResponseErrorNoRetry",
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
            return "SmaxAbPropsGetGroupExperimentConfigResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure and asks the client to retry.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInAbPropsGetGroupExperimentConfigResponseErrorRetry")
    final class ServerError implements SmaxAbPropsGetGroupExperimentConfigResponse {
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
         * @return an {@link Optional} carrying the text, or empty
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
         *         empty on no-match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInAbPropsGetGroupExperimentConfigResponseErrorRetry",
                exports = "parseGetGroupExperimentConfigResponseErrorRetry",
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
            return "SmaxAbPropsGetGroupExperimentConfigResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
