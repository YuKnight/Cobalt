package com.github.auties00.cobalt.node.smax.profilepicture;

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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants — five success shapes
 * plus one error shape.
 *
 * @implNote {@code WASmaxProfilePictureGetRPC.sendGetRPC} tries
 *           {@code SuccessPictureURL} → {@code SuccessAvatarURLs}
 *           → {@code SuccessPictureBlob} → {@code SuccessNoData}
 *           → {@code Error}.
 */
public sealed interface SmaxProfilePictureGetResponse extends SmaxOperation.Response
        permits SmaxProfilePictureGetResponse.SuccessPictureURL, SmaxProfilePictureGetResponse.SuccessAvatarURLs,
        SmaxProfilePictureGetResponse.SuccessPictureBlob, SmaxProfilePictureGetResponse.SuccessNoData, SmaxProfilePictureGetResponse.Error {

    /**
     * Tries each {@link SmaxProfilePictureGetResponse} variant in priority order.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxProfilePictureGetRPC",
            exports = "sendGetRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxProfilePictureGetResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var pictureUrl = SuccessPictureURL.of(node, request);
        if (pictureUrl.isPresent()) {
            return pictureUrl;
        }
        var avatarUrls = SuccessAvatarURLs.of(node, request);
        if (avatarUrls.isPresent()) {
            return avatarUrls;
        }
        var pictureBlob = SuccessPictureBlob.of(node, request);
        if (pictureBlob.isPresent()) {
            return pictureBlob;
        }
        var noData = SuccessNoData.of(node, request);
        if (noData.isPresent()) {
            return noData;
        }
        return Error.of(node, request);
    }

    /**
     * The {@code SuccessPictureURL} reply variant — the relay
     * returns a CDN-hosted picture URL plus content hash.
     *
     * @implNote {@code WASmaxInProfilePictureGetResponseSuccessPictureURL.parseGetResponseSuccessPictureURL}
     *           validates the IQ-result envelope, extracts the
     *           {@code <picture id type url direct_path hash?
     *           has_staging?/>} child and projects each attribute.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureGetResponseSuccessPictureURL")
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureIQResultResponseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureEnums")
    final class SuccessPictureURL implements SmaxProfilePictureGetResponse {
        /**
         * The opaque picture id (used as a cache key on subsequent
         * requests).
         */
        private final String pictureId;

        /**
         * The picture type — one of {@code "image"} /
         * {@code "preview"}.
         */
        private final String pictureType;

        /**
         * The CDN URL.
         */
        private final String pictureUrl;

        /**
         * The CDN direct-path segment (used by the local download
         * pipeline to compose a full media URL).
         */
        private final String pictureDirectPath;

        /**
         * The optional content hash.
         */
        private final String pictureHash;

        /**
         * The optional {@code has_staging} marker — one of
         * {@code "false"} / {@code "true"}.
         */
        private final String pictureHasStaging;

        /**
         * Constructs a new picture-URL reply.
         *
         * @param pictureId         the id; never {@code null}
         * @param pictureType       the type; never {@code null}
         * @param pictureUrl        the URL; never {@code null}
         * @param pictureDirectPath the direct path; never
         *                          {@code null}
         * @param pictureHash       the optional hash; may be
         *                          {@code null}
         * @param pictureHasStaging the optional staging marker; may
         *                          be {@code null}
         * @throws NullPointerException if any required argument is
         *                              {@code null}
         */
        public SuccessPictureURL(String pictureId, String pictureType, String pictureUrl,
                                 String pictureDirectPath, String pictureHash,
                                 String pictureHasStaging) {
            this.pictureId = Objects.requireNonNull(pictureId, "pictureId cannot be null");
            this.pictureType = Objects.requireNonNull(pictureType, "pictureType cannot be null");
            this.pictureUrl = Objects.requireNonNull(pictureUrl, "pictureUrl cannot be null");
            this.pictureDirectPath = Objects.requireNonNull(pictureDirectPath, "pictureDirectPath cannot be null");
            this.pictureHash = pictureHash;
            this.pictureHasStaging = pictureHasStaging;
        }

        /**
         * Returns the picture id.
         *
         * @return the id; never {@code null}
         */
        public String pictureId() {
            return pictureId;
        }

        /**
         * Returns the picture type.
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
        public String pictureUrl() {
            return pictureUrl;
        }

        /**
         * Returns the CDN direct path.
         *
         * @return the path; never {@code null}
         */
        public String pictureDirectPath() {
            return pictureDirectPath;
        }

        /**
         * Returns the optional content hash.
         *
         * @return an {@link Optional} carrying the hash
         */
        public Optional<String> pictureHash() {
            return Optional.ofNullable(pictureHash);
        }

        /**
         * Returns the optional staging marker.
         *
         * @return an {@link Optional} carrying the marker
         */
        public Optional<String> pictureHasStaging() {
            return Optional.ofNullable(pictureHasStaging);
        }

        /**
         * Tries to parse a {@link SuccessPictureURL} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInProfilePictureGetResponseSuccessPictureURL",
                exports = "parseGetResponseSuccessPictureURL",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessPictureURL> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var picture = node.getChild("picture").orElse(null);
            if (picture == null) {
                return Optional.empty();
            }
            var id = picture.getAttributeAsString("id").orElse(null);
            if (id == null) {
                return Optional.empty();
            }
            var type = picture.getAttributeAsString("type").orElse(null);
            if (type == null || (!"image".equals(type) && !"preview".equals(type))) {
                return Optional.empty();
            }
            var url = picture.getAttributeAsString("url").orElse(null);
            if (url == null) {
                return Optional.empty();
            }
            var directPath = picture.getAttributeAsString("direct_path").orElse(null);
            if (directPath == null) {
                return Optional.empty();
            }
            var hash = picture.getAttributeAsString("hash").orElse(null);
            var hasStaging = picture.getAttributeAsString("has_staging").orElse(null);
            if (hasStaging != null && !"false".equals(hasStaging) && !"true".equals(hasStaging)) {
                return Optional.empty();
            }
            return Optional.of(new SuccessPictureURL(id, type, url, directPath, hash, hasStaging));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessPictureURL) obj;
            return Objects.equals(this.pictureId, that.pictureId)
                    && Objects.equals(this.pictureType, that.pictureType)
                    && Objects.equals(this.pictureUrl, that.pictureUrl)
                    && Objects.equals(this.pictureDirectPath, that.pictureDirectPath)
                    && Objects.equals(this.pictureHash, that.pictureHash)
                    && Objects.equals(this.pictureHasStaging, that.pictureHasStaging);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pictureId, pictureType, pictureUrl, pictureDirectPath,
                    pictureHash, pictureHasStaging);
        }

        @Override
        public String toString() {
            return "SmaxProfilePictureGetResponse.SuccessPictureURL[pictureId=" + pictureId
                    + ", pictureType=" + pictureType
                    + ", pictureUrl=" + pictureUrl
                    + ", pictureDirectPath=" + pictureDirectPath
                    + ", pictureHash=" + pictureHash
                    + ", pictureHasStaging=" + pictureHasStaging + ']';
        }
    }

    /**
     * The {@code SuccessAvatarURLs} reply variant — the entity
     * uses an avatar; the relay returns between {@code 1} and
     * {@code 4} avatar URLs (one per pose-id).
     *
     * @implNote {@code WASmaxInProfilePictureGetResponseSuccessAvatarURLs.parseGetResponseSuccessAvatarURLs}
     *           validates the IQ-result envelope and projects the
     *           {@code <avatar url pose_id hash?/>} children
     *           ({@code 1..4} entries).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureGetResponseSuccessAvatarURLs")
    final class SuccessAvatarURLs implements SmaxProfilePictureGetResponse {
        /**
         * The list of avatar entries — between {@code 1} and
         * {@code 4} entries.
         */
        private final List<AvatarUrl> avatars;

        /**
         * Constructs a new avatar-URLs reply.
         *
         * @param avatars the avatar entries; never {@code null}
         * @throws NullPointerException if {@code avatars} is
         *                              {@code null}
         */
        public SuccessAvatarURLs(List<AvatarUrl> avatars) {
            Objects.requireNonNull(avatars, "avatars cannot be null");
            this.avatars = List.copyOf(avatars);
        }

        /**
         * Returns the avatar entries.
         *
         * @return an unmodifiable list; never {@code null}
         */
        public List<AvatarUrl> avatars() {
            return avatars;
        }

        /**
         * Tries to parse a {@link SuccessAvatarURLs} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInProfilePictureGetResponseSuccessAvatarURLs",
                exports = "parseGetResponseSuccessAvatarURLs",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessAvatarURLs> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var avatars = new ArrayList<AvatarUrl>();
            for (var avatarNode : node.getChildren("avatar")) {
                var avatar = AvatarUrl.of(avatarNode).orElse(null);
                if (avatar == null) {
                    return Optional.empty();
                }
                avatars.add(avatar);
            }
            if (avatars.isEmpty() || avatars.size() > 4) {
                return Optional.empty();
            }
            return Optional.of(new SuccessAvatarURLs(avatars));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessAvatarURLs) obj;
            return Objects.equals(this.avatars, that.avatars);
        }

        @Override
        public int hashCode() {
            return Objects.hash(avatars);
        }

        @Override
        public String toString() {
            return "SmaxProfilePictureGetResponse.SuccessAvatarURLs[avatars=" + avatars + ']';
        }

        /**
         * A single {@code <avatar url pose_id hash?/>} entry.
         */
        public static final class AvatarUrl {
            /**
             * The CDN URL.
             */
            private final String url;

            /**
             * The pose id.
             */
            private final String poseId;

            /**
             * The optional content hash.
             */
            private final String hash;

            /**
             * Constructs a new avatar-URL entry.
             *
             * @param url    the URL; never {@code null}
             * @param poseId the pose id; never {@code null}
             * @param hash   the optional hash; may be {@code null}
             * @throws NullPointerException if any required argument
             *                              is {@code null}
             */
            public AvatarUrl(String url, String poseId, String hash) {
                this.url = Objects.requireNonNull(url, "url cannot be null");
                this.poseId = Objects.requireNonNull(poseId, "poseId cannot be null");
                this.hash = hash;
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
             * Returns the pose id.
             *
             * @return the id; never {@code null}
             */
            public String poseId() {
                return poseId;
            }

            /**
             * Returns the optional content hash.
             *
             * @return an {@link Optional} carrying the hash
             */
            public Optional<String> hash() {
                return Optional.ofNullable(hash);
            }

            /**
             * Tries to parse an avatar-URL entry.
             *
             * @param node the {@code <avatar/>} child
             * @return an {@link Optional} carrying the parsed entry
             */
            @WhatsAppWebExport(moduleName = "WASmaxInProfilePictureGetResponseSuccessAvatarURLs",
                    exports = "parseGetResponseSuccessAvatarURLsAvatar",
                    adaptation = WhatsAppAdaptation.ADAPTED)
            public static Optional<AvatarUrl> of(Node node) {
                Objects.requireNonNull(node, "node cannot be null");
                if (!node.hasDescription("avatar")) {
                    return Optional.empty();
                }
                var url = node.getAttributeAsString("url").orElse(null);
                if (url == null) {
                    return Optional.empty();
                }
                var poseId = node.getAttributeAsString("pose_id").orElse(null);
                if (poseId == null) {
                    return Optional.empty();
                }
                var hash = node.getAttributeAsString("hash").orElse(null);
                return Optional.of(new AvatarUrl(url, poseId, hash));
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (AvatarUrl) obj;
                return Objects.equals(this.url, that.url)
                        && Objects.equals(this.poseId, that.poseId)
                        && Objects.equals(this.hash, that.hash);
            }

            @Override
            public int hashCode() {
                return Objects.hash(url, poseId, hash);
            }

            @Override
            public String toString() {
                return "SmaxProfilePictureGetResponse.SuccessAvatarURLs.AvatarUrl[url=" + url
                        + ", poseId=" + poseId
                        + ", hash=" + hash + ']';
            }
        }
    }

    /**
     * The {@code SuccessPictureBlob} reply variant — the picture
     * is small enough to be inlined as raw bytes content of the
     * {@code <picture>} child.
     *
     * @implNote {@code WASmaxInProfilePictureGetResponseSuccessPictureBlob.parseGetResponseSuccessPictureBlob}
     *           validates the IQ-result envelope and extracts the
     *           {@code <picture id type has_staging?>{bytes}</picture>}
     *           projection (the content bytes carry the full
     *           binary picture payload).
     */
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureGetResponseSuccessPictureBlob")
    final class SuccessPictureBlob implements SmaxProfilePictureGetResponse {
        /**
         * The opaque picture id.
         */
        private final String pictureId;

        /**
         * The picture type — one of {@code "image"} /
         * {@code "preview"}.
         */
        private final String pictureType;

        /**
         * The optional staging marker.
         */
        private final String pictureHasStaging;

        /**
         * The raw picture bytes.
         */
        private final byte[] pictureElementValue;

        /**
         * Constructs a new picture-blob reply.
         *
         * @param pictureId           the id; never {@code null}
         * @param pictureType         the type; never {@code null}
         * @param pictureHasStaging   the optional staging marker;
         *                            may be {@code null}
         * @param pictureElementValue the raw picture bytes; never
         *                            {@code null}
         * @throws NullPointerException if any required argument is
         *                              {@code null}
         */
        public SuccessPictureBlob(String pictureId, String pictureType,
                                  String pictureHasStaging, byte[] pictureElementValue) {
            this.pictureId = Objects.requireNonNull(pictureId, "pictureId cannot be null");
            this.pictureType = Objects.requireNonNull(pictureType, "pictureType cannot be null");
            this.pictureHasStaging = pictureHasStaging;
            this.pictureElementValue = Objects.requireNonNull(pictureElementValue,
                    "pictureElementValue cannot be null");
        }

        /**
         * Returns the picture id.
         *
         * @return the id; never {@code null}
         */
        public String pictureId() {
            return pictureId;
        }

        /**
         * Returns the picture type.
         *
         * @return the type; never {@code null}
         */
        public String pictureType() {
            return pictureType;
        }

        /**
         * Returns the optional staging marker.
         *
         * @return an {@link Optional} carrying the marker
         */
        public Optional<String> pictureHasStaging() {
            return Optional.ofNullable(pictureHasStaging);
        }

        /**
         * Returns the raw picture bytes.
         *
         * @return the bytes; never {@code null}
         */
        public byte[] pictureElementValue() {
            return pictureElementValue;
        }

        /**
         * Tries to parse a {@link SuccessPictureBlob} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInProfilePictureGetResponseSuccessPictureBlob",
                exports = "parseGetResponseSuccessPictureBlob",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessPictureBlob> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var picture = node.getChild("picture").orElse(null);
            if (picture == null) {
                return Optional.empty();
            }
            var id = picture.getAttributeAsString("id").orElse(null);
            if (id == null) {
                return Optional.empty();
            }
            var type = picture.getAttributeAsString("type").orElse(null);
            if (type == null || (!"image".equals(type) && !"preview".equals(type))) {
                return Optional.empty();
            }
            var hasStaging = picture.getAttributeAsString("has_staging").orElse(null);
            if (hasStaging != null && !"false".equals(hasStaging) && !"true".equals(hasStaging)) {
                return Optional.empty();
            }
            var bytes = picture.toContentBytes().orElse(null);
            if (bytes == null || bytes.length == 0) {
                return Optional.empty();
            }
            return Optional.of(new SuccessPictureBlob(id, type, hasStaging, bytes));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessPictureBlob) obj;
            return Objects.equals(this.pictureId, that.pictureId)
                    && Objects.equals(this.pictureType, that.pictureType)
                    && Objects.equals(this.pictureHasStaging, that.pictureHasStaging)
                    && Arrays.equals(this.pictureElementValue, that.pictureElementValue);
        }

        @Override
        public int hashCode() {
            var result = Objects.hash(pictureId, pictureType, pictureHasStaging);
            result = 31 * result + Arrays.hashCode(pictureElementValue);
            return result;
        }

        @Override
        public String toString() {
            return "SmaxProfilePictureGetResponse.SuccessPictureBlob[pictureId=" + pictureId
                    + ", pictureType=" + pictureType
                    + ", pictureHasStaging=" + pictureHasStaging
                    + ", pictureElementValue="
                    + Arrays.toString(pictureElementValue) + ']';
        }
    }

    /**
     * The {@code SuccessNoData} reply variant — the entity has no
     * picture / avatar set; the relay returns a bare result IQ
     * envelope with no payload children.
     *
     * @implNote {@code WASmaxInProfilePictureGetResponseSuccessNoData.parseGetResponseSuccessNoData}
     *           is just the IQ-result envelope check — Cobalt
     *           additionally verifies the absence of the
     *           {@code <picture>} and {@code <avatar>} children to
     *           avoid false positives against the four
     *           picture-bearing variants which are tried first.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureGetResponseSuccessNoData")
    final class SuccessNoData implements SmaxProfilePictureGetResponse {
        /**
         * Constructs a new no-data reply.
         */
        public SuccessNoData() {
        }

        /**
         * Tries to parse a {@link SuccessNoData} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInProfilePictureGetResponseSuccessNoData",
                exports = "parseGetResponseSuccessNoData",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessNoData> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            // Disambiguate from the picture-bearing success variants — those
            // are tried first by the dispatcher but, when this static factory
            // is invoked directly, we want to ensure no false positive.
            if (node.getChild("picture").isPresent()) {
                return Optional.empty();
            }
            if (node.getChild("avatar").isPresent()) {
                return Optional.empty();
            }
            return Optional.of(new SuccessNoData());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return SuccessNoData.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxProfilePictureGetResponse.SuccessNoData[]";
        }
    }

    /**
     * The {@code Error} reply variant — one of seven documented
     * IQ-error sub-mixins.
     *
     * <p>Carries one of the following {@code (code, text)} pairs:
     * <ul>
     *   <li>{@code (400, "bad-request")}
     *   <li>{@code (401, "not-authorized")}
     *   <li>{@code (404, "item-not-found")}
     *   <li>{@code (429, "rate-overlimit")}
     *   <li>{@code (500, "internal-server-error")}
     *   <li>{@code (501, "feature-not-implemented")}
     *   <li>{@code (503, "service-unavailable")}
     * </ul>
     *
     * @implNote {@code WASmaxInProfilePictureGetResponseError.parseGetResponseError}
     *           validates the IQ-error envelope and projects the
     *           {@code <error/>} child through
     *           {@code WASmaxInProfilePictureProfilePictureGetErrors}
     *           — a disjunction over the seven sub-mixins above.
     *           Cobalt collapses all seven into the single
     *           {@code (errorCode, errorText)} pair below.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureGetResponseError")
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureProfilePictureGetErrors")
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureIQErrorBadRequestMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureIQErrorNotAuthorizedMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureIQErrorItemNotFoundMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureIQErrorRateOverlimitMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureIQErrorInternalServerErrorMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureIQErrorFeatureNotImplementedMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInProfilePictureIQErrorServiceUnavailableMixin")
    final class Error implements SmaxProfilePictureGetResponse {
        /**
         * The numeric error code.
         */
        private final int errorCode;

        /**
         * The error text.
         */
        private final String errorText;

        /**
         * Constructs a new error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional error text; may be
         *                  {@code null}
         */
        public Error(int errorCode, String errorText) {
            this.errorCode = errorCode;
            this.errorText = errorText;
        }

        /**
         * Returns the numeric error code.
         *
         * @return the code
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the text
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse an {@link Error} variant.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant
         */
        @WhatsAppWebExport(moduleName = "WASmaxInProfilePictureGetResponseError",
                exports = "parseGetResponseError",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Error> of(Node node, Node request) {
            // 4xx → ClientError envelope, 5xx → ServerError envelope.
            var clientEnvelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            var serverEnvelope = clientEnvelope == null
                    ? SmaxBaseServerErrorMixin.parseServerError(node, request).orElse(null)
                    : null;
            var envelope = clientEnvelope != null ? clientEnvelope : serverEnvelope;
            if (envelope == null) {
                return Optional.empty();
            }
            var code = envelope.code();
            var text = envelope.text();
            // Validate the code/text pair against the documented disjunction.
            if ((code == 400 && "bad-request".equals(text))
                    || (code == 401 && "not-authorized".equals(text))
                    || (code == 404 && "item-not-found".equals(text))
                    || (code == 429 && "rate-overlimit".equals(text))
                    || (code == 500 && "internal-server-error".equals(text))
                    || (code == 501 && "feature-not-implemented".equals(text))
                    || (code == 503 && "service-unavailable".equals(text))) {
                return Optional.of(new Error(code, text));
            }
            return Optional.empty();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Error) obj;
            return this.errorCode == that.errorCode
                    && Objects.equals(this.errorText, that.errorText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText);
        }

        @Override
        public String toString() {
            return "SmaxProfilePictureGetResponse.Error[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
