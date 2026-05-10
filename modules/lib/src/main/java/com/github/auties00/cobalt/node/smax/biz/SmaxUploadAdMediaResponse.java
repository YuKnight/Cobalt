package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxDeprecatedIqErrorResponseOptionalFromMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqErrorResponseMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxUploadAdMediaRequest}.
 */
public sealed interface SmaxUploadAdMediaResponse extends SmaxOperation.Response
        permits SmaxUploadAdMediaResponse.Success, SmaxUploadAdMediaResponse.ClientError, SmaxUploadAdMediaResponse.ServerError {

    /**
     * Tries each {@link SmaxUploadAdMediaResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza. Used to validate
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
     * The {@code Success} reply variant. The relay echoed the media
     * registrations.
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
            // WASmaxInBizCtwaNativeAdUploadAdMediaResponseSuccess.parseUploadAdMediaResponseSuccess: assertTag(t, "iq")
            // WASmaxInBizCtwaNativeAdUploadAdMediaResponseSuccess.parseUploadAdMediaResponseSuccess: parseHackBaseIQResultResponseMixin(t, n)
            // WASmaxInBizCtwaNativeAdHackBaseIQResultResponseMixin.parseHackBaseIQResultResponseMixin:
            //   assertTag(iq) + optional(attrUserJid, "to") + parseIQResultResponseMixin(t, n)
            // The optional "to" projection is a no-op: WA never reads it (see SmaxIqResultResponseMixin javadoc).
            // ADAPTED: WA orders assertTag(iq) -> optionalChildWithTag(media) -> parseHackBase... -> mapChildrenWithTag(media_list);
            // Cobalt fuses assertTag(iq) + parseHackBase... into validate() up-front. All checks are still applied
            // and a stanza succeeds iff every WA check would have succeeded.
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            // WASmaxInBizCtwaNativeAdUploadAdMediaResponseSuccess.parseUploadAdMediaResponseSuccess:
            //   optionalChildWithTag(t, "media", s) where s = parseUploadAdMediaResponseSuccessMedia
            // ADAPTED: WA's optionalChild rejects when more than one <media/> child is present; Cobalt's
            // getChild silently picks the first. The relay never emits multiple <media/> children, so this
            // divergence is unreachable in practice and consistent with the rest of the SMAX package.
            SmaxUploadAdMediaMediaEntry media = null;
            var mediaNode = node.getChild("media").orElse(null);
            if (mediaNode != null) {
                var parsed = parseEntry(mediaNode);
                if (parsed.isEmpty()) {
                    return Optional.empty();
                }
                media = parsed.get();
            }
            // WASmaxInBizCtwaNativeAdUploadAdMediaResponseSuccess.parseUploadAdMediaResponseSuccess:
            //   mapChildrenWithTag(t, "media_list", 0, 10, e) where e = parseUploadAdMediaResponseSuccessMediaList
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
            // WASmaxParseUtils.mapChildrenWithTag enforces the [0, 10] cardinality range.
            if (entries.size() > 10) {
                return Optional.empty();
            }
            // WAResultOrError.makeResult({...mixin_result, media: a.value, mediaList: l.value})
            return Optional.of(new Success(media, entries));
        }

        /**
         * Parses a single {@code (id, type)} entry from the given
         * {@code <media/>} or {@code <media_list/>} node.
         *
         * <p>Consolidates WA Web's two byte-identical parsers
         * {@code parseUploadAdMediaResponseSuccessMedia} and
         * {@code parseUploadAdMediaResponseSuccessMediaList}, which
         * differ only in the asserted tag name. The tag check is
         * already enforced by the call sites: {@code <media>} arrives
         * from {@code Node#getChild("media")} and {@code <media_list>}
         * arrives from {@code Node#streamChildren("media_list")}, so
         * a redundant {@code assertTag} in the helper would be a
         * no-op.
         *
         * @param node the {@code <media>} or {@code <media_list>}
         *             node
         * @return an {@link Optional} carrying the parsed entry, or
         *         empty when either attribute is missing or
         *         malformed
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdUploadAdMediaResponseSuccess",
                exports = "parseUploadAdMediaResponseSuccessMedia",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdUploadAdMediaResponseSuccess",
                exports = "parseUploadAdMediaResponseSuccessMediaList",
                adaptation = WhatsAppAdaptation.ADAPTED)
        private static Optional<SmaxUploadAdMediaMediaEntry> parseEntry(Node node) {
            // WASmaxParseUtils.attrString(e, "id"): required string attribute
            var id = node.getAttributeAsString("id").orElse(null);
            if (id == null) {
                return Optional.empty();
            }
            // WASmaxParseUtils.attrStringEnum(e, "type", ENUM_IMAGE_VIDEO):
            // required attribute keyed against the lowercase {image,video} dictionary.
            var typeStr = node.getAttributeAsString("type").orElse(null);
            if (typeStr == null) {
                return Optional.empty();
            }
            var type = SmaxUploadAdMediaMediaType.of(typeStr).orElse(null);
            if (type == null) {
                return Optional.empty();
            }
            // WAResultOrError.makeResult({id: n.value, type: r.value})
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
     * The {@code ClientError} reply variant. The relay rejected the
     * request with one of the two {@code 4xx} error codes drawn from
     * the native-ad-error catalogue: either
     * {@code (text="bad-request", code=400)} from
     * {@code WASmaxInBizCtwaNativeAdIQErrorBadRequestMixin} or
     * {@code (text="forbidden", code=403)} from
     * {@code WASmaxInBizCtwaNativeAdIQErrorForbiddenMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdUploadAdMediaResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdNativeAdErrors")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdIQErrorBadRequestMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdIQErrorForbiddenMixin")
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdNativeAdErrors",
                exports = "parseNativeAdErrors",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdDeprecatedIQErrorResponseOptionalFromMixin",
                exports = "parseDeprecatedIQErrorResponseOptionalFromMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdIQErrorBadRequestMixin",
                exports = "parseIQErrorBadRequestMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdIQErrorForbiddenMixin",
                exports = "parseIQErrorForbiddenMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            // WASmaxInBizCtwaNativeAdUploadAdMediaResponseError.parseUploadAdMediaResponseError: parseDeprecatedIQErrorResponseOptionalFromMixin(e, t)
            if (!SmaxDeprecatedIqErrorResponseOptionalFromMixin.validate(node, request)) {
                return Optional.empty();
            }
            // WASmaxInBizCtwaNativeAdUploadAdMediaResponseError.parseUploadAdMediaResponseError: flattenedChildWithTag(e, "error")
            // (the parseError helper extracts the <error code text/> child; the assertTag(error) check
            // run by each native-ad-error mixin is implicit because parseError pulls the <error/> child by tag)
            var envelope = SmaxIqErrorResponseMixin.parseError(node).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            // WASmaxInBizCtwaNativeAdNativeAdErrors.parseNativeAdErrors: tries IQErrorBadRequestMixin first
            // WASmaxInBizCtwaNativeAdIQErrorBadRequestMixin.parseIQErrorBadRequestMixin: literal(attrString, e, "text", "bad-request") + literal(attrInt, e, "code", 400)
            // WASmaxInBizCtwaNativeAdNativeAdErrors.parseNativeAdErrors: then IQErrorForbiddenMixin
            // WASmaxInBizCtwaNativeAdIQErrorForbiddenMixin.parseIQErrorForbiddenMixin: literal(attrString, e, "text", "forbidden") + literal(attrInt, e, "code", 403)
            return matchClientErrorPair(envelope.code(), envelope.text())
                    ? Optional.of(new ClientError(envelope.code(), envelope.text()))
                    : Optional.empty();
        }

        /**
         * Returns whether the supplied {@code (code, text)} pair
         * matches one of the two {@code ClientError} arms enumerated by
         * {@code WASmaxInBizCtwaNativeAdNativeAdErrors.parseNativeAdErrors}.
         *
         * <p>The 4xx half of the disjunction admits exactly two pairs:
         * {@code ("bad-request", 400)} and
         * {@code ("forbidden", 403)}. Stanzas whose {@code <error/>}
         * child carries any other 4xx code or any other text are
         * rejected: WA Web propagates the failure of the last mixin
         * arm, and the upstream {@code parseUploadAdMediaResponseError}
         * caller treats that as a no-parse.
         *
         * @param code the parsed error code
         * @param text the parsed error text; may be {@code null}
         * @return {@code true} when the pair matches one of the
         *         enumerated client-error arms; {@code false} otherwise
         */
        private static boolean matchClientErrorPair(int code, String text) {
            return ("bad-request".equals(text) && code == 400)
                    || ("forbidden".equals(text) && code == 403);
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
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure while processing the request and
     * replied with one of the two {@code 5xx} pairs enumerated by
     * {@code WASmaxInBizCtwaNativeAdNativeAdErrors}: either
     * {@code (text="internal-server-error", code=500)} from
     * {@code WASmaxInBizCtwaNativeAdIQErrorInternalServerErrorMixin}
     * or {@code (text="service-unavailable", code=503)} from
     * {@code WASmaxInBizCtwaNativeAdIQErrorServiceUnavailableMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdUploadAdMediaResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdNativeAdErrors")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdIQErrorInternalServerErrorMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInBizCtwaNativeAdIQErrorServiceUnavailableMixin")
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
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdUploadAdMediaResponseError",
                exports = "parseUploadAdMediaResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdNativeAdErrors",
                exports = "parseNativeAdErrors",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdDeprecatedIQErrorResponseOptionalFromMixin",
                exports = "parseDeprecatedIQErrorResponseOptionalFromMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdIQErrorInternalServerErrorMixin",
                exports = "parseIQErrorInternalServerErrorMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        @WhatsAppWebExport(moduleName = "WASmaxInBizCtwaNativeAdIQErrorServiceUnavailableMixin",
                exports = "parseIQErrorServiceUnavailableMixin",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ServerError> of(Node node, Node request) {
            // WASmaxInBizCtwaNativeAdUploadAdMediaResponseError.parseUploadAdMediaResponseError: parseDeprecatedIQErrorResponseOptionalFromMixin(e, t)
            if (!SmaxDeprecatedIqErrorResponseOptionalFromMixin.validate(node, request)) {
                return Optional.empty();
            }
            // WASmaxInBizCtwaNativeAdUploadAdMediaResponseError.parseUploadAdMediaResponseError: flattenedChildWithTag(e, "error")
            var envelope = SmaxIqErrorResponseMixin.parseError(node).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            // WASmaxInBizCtwaNativeAdNativeAdErrors.parseNativeAdErrors: third arm IQErrorInternalServerErrorMixin
            // WASmaxInBizCtwaNativeAdIQErrorInternalServerErrorMixin.parseIQErrorInternalServerErrorMixin: literal(attrString, e, "text", "internal-server-error") + literal(attrInt, e, "code", 500)
            // WASmaxInBizCtwaNativeAdNativeAdErrors.parseNativeAdErrors: fourth arm IQErrorServiceUnavailableMixin
            // WASmaxInBizCtwaNativeAdIQErrorServiceUnavailableMixin.parseIQErrorServiceUnavailableMixin: literal(attrString, e, "text", "service-unavailable") + literal(attrInt, e, "code", 503)
            return matchServerErrorPair(envelope.code(), envelope.text())
                    ? Optional.of(new ServerError(envelope.code(), envelope.text()))
                    : Optional.empty();
        }

        /**
         * Returns whether the supplied {@code (code, text)} pair
         * matches one of the two {@code ServerError} arms enumerated
         * by
         * {@code WASmaxInBizCtwaNativeAdNativeAdErrors.parseNativeAdErrors}.
         *
         * <p>The 5xx half of the disjunction admits exactly two pairs:
         * {@code ("internal-server-error", 500)} and
         * {@code ("service-unavailable", 503)}. Stanzas whose
         * {@code <error/>} child carries any other 5xx code or any
         * other text are rejected and fall through the disjunction the
         * same way they do in WA Web.
         *
         * @param code the parsed error code
         * @param text the parsed error text; may be {@code null}
         * @return {@code true} when the pair matches one of the
         *         enumerated server-error arms; {@code false} otherwise
         */
        private static boolean matchServerErrorPair(int code, String text) {
            return ("internal-server-error".equals(text) && code == 500)
                    || ("service-unavailable".equals(text) && code == 503);
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
