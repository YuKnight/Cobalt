package com.github.auties00.cobalt.node.iq.group;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to an {@link IqQueryGroupInviteProfilePicRequest}.
 */
@WhatsAppWebModule(moduleName = "WAWebQueryGroupInviteProfilePicApi")
public sealed interface IqQueryGroupInviteProfilePicResponse extends IqOperation.Response
        permits IqQueryGroupInviteProfilePicResponse.Success, IqQueryGroupInviteProfilePicResponse.ClientError, IqQueryGroupInviteProfilePicResponse.ServerError {

    /**
     * Tries each {@link IqQueryGroupInviteProfilePicResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when no documented variant matched
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryGroupInviteProfilePicApi",
            exports = "queryGroupInviteLinkProfilePic",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebQueryGroupInviteProfilePicApi",
            exports = "queryGroupInviteMessageProfilePic",
            adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqQueryGroupInviteProfilePicResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — carries the
     * {@code <picture>} grandchild's identifier, MIME type, CDN URL
     * and direct path.
     *
     * @implNote {@code WAWebQueryGroupInviteProfilePicApi.queryGroupProfilePicParser}:
     *           {@code child("picture")} -> {@code {id, type, url,
     *           direct_path}}.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryGroupInviteProfilePicApi")
    final class Success implements IqQueryGroupInviteProfilePicResponse {
        /**
         * The picture identifier.
         */
        private final String pictureId;

        /**
         * The picture MIME type.
         */
        private final String pictureType;

        /**
         * The CDN URL.
         */
        private final String url;

        /**
         * The direct path on the CDN.
         */
        private final String directPath;

        /**
         * Constructs a successful reply.
         *
         * @param pictureId   the picture identifier; never
         *                    {@code null}
         * @param pictureType the picture MIME type; never
         *                    {@code null}
         * @param url         the CDN URL; never {@code null}
         * @param directPath  the direct path; never {@code null}
         * @throws NullPointerException if any argument is
         *                              {@code null}
         */
        public Success(String pictureId, String pictureType, String url, String directPath) {
            this.pictureId = Objects.requireNonNull(pictureId, "pictureId cannot be null");
            this.pictureType = Objects.requireNonNull(pictureType, "pictureType cannot be null");
            this.url = Objects.requireNonNull(url, "url cannot be null");
            this.directPath = Objects.requireNonNull(directPath, "directPath cannot be null");
        }

        /**
         * Returns the picture identifier.
         *
         * @return the id; never {@code null}
         */
        public String pictureId() {
            return pictureId;
        }

        /**
         * Returns the picture MIME type.
         *
         * @return the type; never {@code null}
         */
        public String pictureType() {
            return pictureType;
        }

        /**
         * Returns the CDN URL.
         *
         * @return the URL; never {@code null}
         */
        public String url() {
            return url;
        }

        /**
         * Returns the direct path on the CDN.
         *
         * @return the path; never {@code null}
         */
        public String directPath() {
            return directPath;
        }

        /**
         * Tries to parse a {@link Success} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         */
        @WhatsAppWebExport(moduleName = "WAWebQueryGroupInviteProfilePicApi",
                exports = "queryGroupProfilePicParser",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var picture = node.getChild("picture").orElse(null);
            if (picture == null) {
                return Optional.empty();
            }
            var id = picture.getAttributeAsString("id").orElse(null);
            var type = picture.getAttributeAsString("type").orElse(null);
            var url = picture.getAttributeAsString("url").orElse(null);
            var directPath = picture.getAttributeAsString("direct_path").orElse(null);
            if (id == null || type == null || url == null || directPath == null) {
                return Optional.empty();
            }
            return Optional.of(new Success(id, type, url, directPath));
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
            return Objects.equals(this.pictureId, that.pictureId)
                    && Objects.equals(this.pictureType, that.pictureType)
                    && Objects.equals(this.url, that.url)
                    && Objects.equals(this.directPath, that.directPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pictureId, pictureType, url, directPath);
        }

        @Override
        public String toString() {
            return "IqQueryGroupInviteProfilePicResponse.Success[pictureId=" + pictureId
                    + ", pictureType=" + pictureType
                    + ", url=" + url
                    + ", directPath=" + directPath + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — {@code 4xx} rejection.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryGroupInviteProfilePicApi")
    final class ClientError implements IqQueryGroupInviteProfilePicResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional text; may be {@code null}
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
         * @return an {@link Optional} carrying the text, or empty
         *         when omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebQueryGroupInviteProfilePicApi",
                exports = "queryGroupInviteLinkProfilePic",
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqQueryGroupInviteProfilePicResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — {@code 5xx} transient
     * failure.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryGroupInviteProfilePicApi")
    final class ServerError implements IqQueryGroupInviteProfilePicResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional text; may be {@code null}
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the text, or empty
         *         when omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebQueryGroupInviteProfilePicApi",
                exports = "queryGroupInviteLinkProfilePic",
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
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "IqQueryGroupInviteProfilePicResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
