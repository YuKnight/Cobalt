package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxUploadAdMediaRequest}.
 *
 * @implNote {@code WASmaxBizCtwaNativeAdUploadAdMediaRPC.sendUploadAdMediaRPC}
 *           tries {@code Success} → {@code Error} in order. Cobalt
 *           collapses the {@code Error} arm into the
 *           {@code ClientError}/{@code ServerError} pair via the
 *           shared {@link SmaxBaseServerErrorMixin} helpers.
 */
public sealed interface SmaxUploadAdMediaResponse extends SmaxOperation.Response
        permits SmaxUploadAdMediaResponse.Success, SmaxUploadAdMediaResponse.ClientError, SmaxUploadAdMediaResponse.ServerError {

    /**
     * Tries each {@link SmaxUploadAdMediaResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to validate
     *                echoed identifiers; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxBizCtwaNativeAdUploadAdMediaRPC",
            exports = "sendUploadAdMediaRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxUploadAdMediaResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant — the relay echoed the media
     * registrations.
     *
     * @implNote {@code WASmaxInBizCtwaNativeAdUploadAdMediaResponseSuccess.parseUploadAdMediaResponseSuccess}
     *           validates the {@code <iq from id type="result">}
     *           envelope, optionally projects the primary
     *           {@code <media id type/>} child, then enumerates 0..10
     *           {@code <media_list id type/>} children.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdUploadAdMediaResponseSuccess")
    final class Success implements SmaxUploadAdMediaResponse {
        /**
         * The optional primary {@code <media/>} echo.
         */
        private final SmaxUploadAdMediaMediaEntry media;

        /**
         * The list of {@code <media_list/>} echoes (0..10 entries).
         */
        private final List<SmaxUploadAdMediaMediaEntry> mediaList;

        /**
         * Constructs a new successful reply.
         *
         * @param media     the optional primary media echo; may be
         *                  {@code null}
         * @param mediaList the media-list echoes; never {@code null}
         * @throws NullPointerException if {@code mediaList} is
         *                              {@code null}
         */
        public Success(SmaxUploadAdMediaMediaEntry media, List<SmaxUploadAdMediaMediaEntry> mediaList) {
            Objects.requireNonNull(mediaList, "mediaList cannot be null");
            this.media = media;
            this.mediaList = List.copyOf(mediaList);
        }

        /**
         * Returns the optional primary media echo.
         *
         * @return an {@link Optional} carrying the entry, or empty
         *         when the relay omitted the {@code <media/>} child
         */
        public Optional<SmaxUploadAdMediaMediaEntry> media() {
            return Optional.ofNullable(media);
        }

        /**
         * Returns the media-list echoes.
         *
         * @return an unmodifiable list of 0..10 entries; never
         *         {@code null}
         */
        public List<SmaxUploadAdMediaMediaEntry> mediaList() {
            return mediaList;
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdUploadAdMediaResponseSuccess",
                exports = "parseUploadAdMediaResponseSuccess",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            Objects.requireNonNull(node, "node cannot be null");
            Objects.requireNonNull(request, "request cannot be null");
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            SmaxUploadAdMediaMediaEntry media = null;
            var mediaNode = node.getChild("media").orElse(null);
            if (mediaNode != null) {
                var parsed = parseEntry(mediaNode);
                if (parsed.isEmpty()) {
                    return Optional.empty();
                }
                media = parsed.get();
            }
            var entries = new ArrayList<SmaxUploadAdMediaMediaEntry>();
            var iter = node.streamChildren("media_list").iterator();
            while (iter.hasNext()) {
                var listNode = iter.next();
                var parsed = parseEntry(listNode);
                if (parsed.isEmpty()) {
                    return Optional.empty();
                }
                entries.add(parsed.get());
            }
            if (entries.size() > 10) {
                return Optional.empty();
            }
            return Optional.of(new Success(media, entries));
        }

        /**
         * Parses a single {@code (id, type)} entry from the given
         * node.
         *
         * @param node the {@code <media>} or {@code <media_list>}
         *             node
         * @return an {@link Optional} carrying the parsed entry, or
         *         empty when either attribute is missing or
         *         malformed
         */
        private static Optional<SmaxUploadAdMediaMediaEntry> parseEntry(Node node) {
            var id = node.getAttributeAsString("id").orElse(null);
            if (id == null) {
                return Optional.empty();
            }
            var typeStr = node.getAttributeAsString("type").orElse(null);
            if (typeStr == null) {
                return Optional.empty();
            }
            var type = SmaxUploadAdMediaMediaType.of(typeStr).orElse(null);
            if (type == null) {
                return Optional.empty();
            }
            return Optional.of(new SmaxUploadAdMediaMediaEntry(id, type));
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
            return Objects.equals(this.media, that.media)
                    && Objects.equals(this.mediaList, that.mediaList);
        }

        @Override
        public int hashCode() {
            return Objects.hash(media, mediaList);
        }

        @Override
        public String toString() {
            return "SmaxUploadAdMediaResponse.Success[media=" + media
                    + ", mediaList=" + mediaList + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request with a {@code 4xx} error code drawn from the
     * native-ad-error catalogue.
     *
     * @implNote {@code WASmaxInBizCtwaNativeAdUploadAdMediaResponseError.parseUploadAdMediaResponseError}
     *           routes the {@code <error/>} child through
     *           {@code WASmaxInBizCtwaNativeAdNativeAdErrors}; Cobalt
     *           collapses to the raw {@code (code, text)} pair via
     *           the shared {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdUploadAdMediaResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdNativeAdErrors")
    final class ClientError implements SmaxUploadAdMediaResponse {
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
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdUploadAdMediaResponseError",
                exports = "parseUploadAdMediaResponseError",
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
            return "SmaxUploadAdMediaResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered a
     * transient internal failure ({@code 5xx}) while processing the
     * request.
     *
     * @implNote Sourced from the {@code 5xx} arms of
     *           {@code WASmaxInBizCtwaNativeAdNativeAdErrors}; Cobalt
     *           routes through the shared
     *           {@link SmaxBaseServerErrorMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdUploadAdMediaResponseError")
    final class ServerError implements SmaxUploadAdMediaResponse {
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
         * @return an {@link Optional} carrying the error text, or
         *         empty when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the
         * given inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         server-error schema
         */
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
            return "SmaxUploadAdMediaResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
