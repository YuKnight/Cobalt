package com.github.auties00.cobalt.node.iq.ctwa;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.business.ctwa.BusinessCtwaMediaType;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 */
public sealed interface IqQueryCtwaContextResponse extends IqOperation.Response
        permits IqQueryCtwaContextResponse.Success, IqQueryCtwaContextResponse.ClientError, IqQueryCtwaContextResponse.ServerError {

    /**
     * Tries each {@link IqQueryCtwaContextResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty when no documented variant matched
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebQueryCtwaContextJob",
            exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqQueryCtwaContextResponse> of(Node node, Node request) {
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
     * {@code <context>} grandchild's projection.
     *
     * @implNote {@code WAWebQueryCtwaContextJob.ctwaContext}:
     *           {@code child("context")} -> {sourceUrl, sourceId,
     *           sourceType, [title], [description], [thumbnailUrl],
     *           [thumbnail bytes], [mediaUrl], mediaType,
     *           [sourceApp], (WAMO-AGM extras: greetingMessageBody,
     *           automatedGreetingMessageShown, ctaPayload,
     *           originalImageUrl)}.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryCtwaContextJob")
    final class Success implements IqQueryCtwaContextResponse {
        /**
         * The mandatory {@code <source>}/{@code <url>} content.
         */
        private final String sourceUrl;

        /**
         * The mandatory {@code <source>}/{@code <id>} content.
         */
        private final String sourceId;

        /**
         * The mandatory {@code <source>}/{@code <type>} content.
         */
        private final String sourceType;

        /**
         * The optional {@code <headline>} content.
         */
        private final String title;

        /**
         * The optional {@code <body>} content.
         */
        private final String description;

        /**
         * The optional {@code <thumbnail>}/{@code <url>} content.
         */
        private final String thumbnailUrl;

        /**
         * The optional inline {@code <thumbnail>}/{@code <bytes>}
         * content.
         */
        private final byte[] thumbnailBytes;

        /**
         * The optional {@code <video>}/{@code <url>} content.
         */
        private final String mediaUrl;

        /**
         * The media type — {@link BusinessCtwaMediaType#VIDEO} when the
         * response carried a {@code <video>} grandchild,
         * {@link BusinessCtwaMediaType#IMAGE} otherwise. {@code null} when
         * the response had no thumbnail at all.
         */
        private final BusinessCtwaMediaType mediaType;

        /**
         * The optional {@code <sourceApp>} content (e.g.
         * {@code "instagram"}).
         */
        private final String sourceApp;

        /**
         * The optional WAMO-AGM-integrated
         * {@code <greetingMessageBody>} content.
         */
        private final String greetingMessageBody;

        /**
         * The optional WAMO-AGM-integrated
         * {@code <automatedGreetingMessageShown>} flag (the relay
         * encodes it as the literal string {@code "true"} or
         * {@code "false"}). {@code null} when omitted.
         */
        private final Boolean automatedGreetingMessageShown;

        /**
         * The optional WAMO-AGM-integrated {@code <ctaPayload>}
         * content.
         */
        private final String ctaPayload;

        /**
         * The optional WAMO-AGM-integrated
         * {@code <originalImageUrl>} content.
         */
        private final String originalImageUrl;

        /**
         * Constructs a successful reply.
         *
         * @param sourceUrl                     the source URL;
         *                                      never {@code null}
         * @param sourceId                      the source id; never
         *                                      {@code null}
         * @param sourceType                    the source type;
         *                                      never {@code null}
         * @param title                         the optional title;
         *                                      may be {@code null}
         * @param description                   the optional
         *                                      description; may be
         *                                      {@code null}
         * @param thumbnailUrl                  the optional
         *                                      thumbnail URL; may
         *                                      be {@code null}
         * @param thumbnailBytes                the optional inline
         *                                      thumbnail bytes;
         *                                      may be {@code null}
         * @param mediaUrl                      the optional video
         *                                      URL; may be
         *                                      {@code null}
         * @param mediaType                     the media type, or
         *                                      {@code null} when no
         *                                      thumbnail was shipped
         * @param sourceApp                     the optional source
         *                                      app; may be
         *                                      {@code null}
         * @param greetingMessageBody           the optional WAMO-AGM
         *                                      greeting; may be
         *                                      {@code null}
         * @param automatedGreetingMessageShown the optional WAMO-AGM
         *                                      shown flag; may be
         *                                      {@code null}
         * @param ctaPayload                    the optional WAMO-AGM
         *                                      CTA payload; may be
         *                                      {@code null}
         * @param originalImageUrl              the optional WAMO-AGM
         *                                      original image URL;
         *                                      may be {@code null}
         * @throws NullPointerException if any of the mandatory
         *                              arguments ({@code sourceUrl},
         *                              {@code sourceId},
         *                              {@code sourceType}) is
         *                              {@code null}
         */
        public Success(String sourceUrl, String sourceId, String sourceType, String title,
                       String description, String thumbnailUrl, byte[] thumbnailBytes,
                       String mediaUrl, BusinessCtwaMediaType mediaType, String sourceApp,
                       String greetingMessageBody, Boolean automatedGreetingMessageShown,
                       String ctaPayload, String originalImageUrl) {
            this.sourceUrl = Objects.requireNonNull(sourceUrl, "sourceUrl cannot be null");
            this.sourceId = Objects.requireNonNull(sourceId, "sourceId cannot be null");
            this.sourceType = Objects.requireNonNull(sourceType, "sourceType cannot be null");
            this.title = title;
            this.description = description;
            this.thumbnailUrl = thumbnailUrl;
            this.thumbnailBytes = thumbnailBytes == null ? null : thumbnailBytes.clone();
            this.mediaUrl = mediaUrl;
            this.mediaType = mediaType;
            this.sourceApp = sourceApp;
            this.greetingMessageBody = greetingMessageBody;
            this.automatedGreetingMessageShown = automatedGreetingMessageShown;
            this.ctaPayload = ctaPayload;
            this.originalImageUrl = originalImageUrl;
        }

        /**
         * Returns the source URL.
         *
         * @return the URL; never {@code null}
         */
        public String sourceUrl() {
            return sourceUrl;
        }

        /**
         * Returns the source identifier.
         *
         * @return the id; never {@code null}
         */
        public String sourceId() {
            return sourceId;
        }

        /**
         * Returns the source type.
         *
         * @return the type; never {@code null}
         */
        public String sourceType() {
            return sourceType;
        }

        /**
         * Returns the optional title.
         *
         * @return an {@link Optional} carrying the title, or empty
         */
        public Optional<String> title() {
            return Optional.ofNullable(title);
        }

        /**
         * Returns the optional description.
         *
         * @return an {@link Optional} carrying the description, or
         *         empty
         */
        public Optional<String> description() {
            return Optional.ofNullable(description);
        }

        /**
         * Returns the optional thumbnail URL.
         *
         * @return an {@link Optional} carrying the URL, or empty
         */
        public Optional<String> thumbnailUrl() {
            return Optional.ofNullable(thumbnailUrl);
        }

        /**
         * Returns the optional inline thumbnail bytes (defensive
         * copy).
         *
         * @return an {@link Optional} carrying a clone of the
         *         bytes, or empty
         */
        public Optional<byte[]> thumbnailBytes() {
            return Optional.ofNullable(thumbnailBytes).map(byte[]::clone);
        }

        /**
         * Returns the optional video URL.
         *
         * @return an {@link Optional} carrying the URL, or empty
         */
        public Optional<String> mediaUrl() {
            return Optional.ofNullable(mediaUrl);
        }

        /**
         * Returns the optional media type.
         *
         * @return an {@link Optional} carrying the type, or empty
         *         when no thumbnail was shipped
         */
        public Optional<BusinessCtwaMediaType> mediaType() {
            return Optional.ofNullable(mediaType);
        }

        /**
         * Returns the optional source-app identifier.
         *
         * @return an {@link Optional} carrying the identifier, or
         *         empty
         */
        public Optional<String> sourceApp() {
            return Optional.ofNullable(sourceApp);
        }

        /**
         * Returns the optional WAMO-AGM greeting body.
         *
         * @return an {@link Optional} carrying the greeting, or
         *         empty
         */
        public Optional<String> greetingMessageBody() {
            return Optional.ofNullable(greetingMessageBody);
        }

        /**
         * Returns the optional WAMO-AGM "shown" flag.
         *
         * @return an {@link Optional} carrying the flag, or empty
         */
        public Optional<Boolean> automatedGreetingMessageShown() {
            return Optional.ofNullable(automatedGreetingMessageShown);
        }

        /**
         * Returns the optional WAMO-AGM CTA payload.
         *
         * @return an {@link Optional} carrying the payload, or
         *         empty
         */
        public Optional<String> ctaPayload() {
            return Optional.ofNullable(ctaPayload);
        }

        /**
         * Returns the optional WAMO-AGM original image URL.
         *
         * @return an {@link Optional} carrying the URL, or empty
         */
        public Optional<String> originalImageUrl() {
            return Optional.ofNullable(originalImageUrl);
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
        @WhatsAppWebExport(moduleName = "WAWebQueryCtwaContextJob",
                exports = "ctwaContext",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var context = node.getChild("context").orElse(null);
            if (context == null) {
                return Optional.empty();
            }
            var source = context.getChild("source").orElse(null);
            if (source == null) {
                return Optional.empty();
            }
            var sourceUrl = source.getChild("url")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            var sourceId = source.getChild("id")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            var sourceType = source.getChild("type")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            if (sourceUrl == null || sourceId == null || sourceType == null) {
                return Optional.empty();
            }
            var title = context.getChild("headline")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            var description = context.getChild("body")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            String thumbnailUrl = null;
            byte[] thumbnailBytes = null;
            BusinessCtwaMediaType mediaType = null;
            String mediaUrl = null;
            var thumbnail = context.getChild("thumbnail").orElse(null);
            if (thumbnail != null) {
                thumbnailUrl = thumbnail.getChild("url")
                        .flatMap(Node::toContentString)
                        .orElse(null);
                thumbnailBytes = thumbnail.getChild("bytes")
                        .flatMap(Node::toContentBytes)
                        .orElse(null);
                var video = context.getChild("video").orElse(null);
                if (video != null) {
                    mediaUrl = video.getChild("url")
                            .flatMap(Node::toContentString)
                            .orElse(null);
                    mediaType = BusinessCtwaMediaType.VIDEO;
                } else {
                    mediaType = BusinessCtwaMediaType.IMAGE;
                }
            }
            var sourceApp = context.getChild("sourceApp")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            var greetingMessageBody = context.getChild("greetingMessageBody")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            Boolean automatedGreetingMessageShown = null;
            var automatedShown = context.getChild("automatedGreetingMessageShown")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            if (automatedShown != null) {
                automatedGreetingMessageShown = "true".equals(automatedShown);
            }
            var ctaPayload = context.getChild("ctaPayload")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            var originalImageUrl = context.getChild("originalImageUrl")
                    .flatMap(Node::toContentString)
                    .orElse(null);
            return Optional.of(new Success(sourceUrl, sourceId, sourceType, title, description,
                    thumbnailUrl, thumbnailBytes, mediaUrl, mediaType, sourceApp,
                    greetingMessageBody, automatedGreetingMessageShown, ctaPayload,
                    originalImageUrl));
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
            return Objects.equals(this.sourceUrl, that.sourceUrl)
                    && Objects.equals(this.sourceId, that.sourceId)
                    && Objects.equals(this.sourceType, that.sourceType)
                    && Objects.equals(this.title, that.title)
                    && Objects.equals(this.description, that.description)
                    && Objects.equals(this.thumbnailUrl, that.thumbnailUrl)
                    && Arrays.equals(this.thumbnailBytes, that.thumbnailBytes)
                    && Objects.equals(this.mediaUrl, that.mediaUrl)
                    && this.mediaType == that.mediaType
                    && Objects.equals(this.sourceApp, that.sourceApp)
                    && Objects.equals(this.greetingMessageBody, that.greetingMessageBody)
                    && Objects.equals(this.automatedGreetingMessageShown, that.automatedGreetingMessageShown)
                    && Objects.equals(this.ctaPayload, that.ctaPayload)
                    && Objects.equals(this.originalImageUrl, that.originalImageUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceUrl, sourceId, sourceType, title, description,
                    thumbnailUrl, Arrays.hashCode(thumbnailBytes), mediaUrl, mediaType,
                    sourceApp, greetingMessageBody, automatedGreetingMessageShown,
                    ctaPayload, originalImageUrl);
        }

        @Override
        public String toString() {
            var thumbnailBytesLength = thumbnailBytes == null ? -1 : thumbnailBytes.length;
            return "IqQueryCtwaContextResponse.Success[sourceUrl=" + sourceUrl
                    + ", sourceId=" + sourceId
                    + ", sourceType=" + sourceType
                    + ", title=" + title
                    + ", description=" + description
                    + ", thumbnailUrl=" + thumbnailUrl
                    + ", thumbnailBytesLength=" + thumbnailBytesLength
                    + ", mediaUrl=" + mediaUrl
                    + ", mediaType=" + mediaType
                    + ", sourceApp=" + sourceApp
                    + ", greetingMessageBody=" + greetingMessageBody
                    + ", automatedGreetingMessageShown=" + automatedGreetingMessageShown
                    + ", ctaPayload=" + ctaPayload
                    + ", originalImageUrl=" + originalImageUrl + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — {@code 4xx} rejection
     * (also covers the relay-side {@code <error>} child carried
     * inside an otherwise-successful envelope, mirroring
     * {@code ctwaContext.hasChild("error")}).
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryCtwaContextJob")
    final class ClientError implements IqQueryCtwaContextResponse {
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
        @WhatsAppWebExport(moduleName = "WAWebQueryCtwaContextJob",
                exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqQueryCtwaContextResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — {@code 5xx} transient
     * failure.
     */
    @WhatsAppWebModule(moduleName = "WAWebQueryCtwaContextJob")
    final class ServerError implements IqQueryCtwaContextResponse {
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
        @WhatsAppWebExport(moduleName = "WAWebQueryCtwaContextJob",
                exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqQueryCtwaContextResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
