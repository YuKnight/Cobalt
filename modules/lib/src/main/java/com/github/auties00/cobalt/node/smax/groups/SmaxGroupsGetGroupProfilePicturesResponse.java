package com.github.auties00.cobalt.node.smax.groups;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link SmaxGroupsGetGroupProfilePicturesRequest}.
 */
public sealed interface SmaxGroupsGetGroupProfilePicturesResponse extends SmaxOperation.Response
        permits SmaxGroupsGetGroupProfilePicturesResponse.Success, SmaxGroupsGetGroupProfilePicturesResponse.ClientError, SmaxGroupsGetGroupProfilePicturesResponse.ServerError {

    /**
     * Tries each {@link SmaxGroupsGetGroupProfilePicturesResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay;
     *                never {@code null}
     * @param request the original outbound stanza — used to
     *                validate echoed identifiers; never
     *                {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is
     *                              {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxGroupsGetGroupProfilePicturesRPC",
            exports = "sendGetGroupProfilePicturesRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGroupsGetGroupProfilePicturesResponse> of(Node node, Node request) {
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
     * The {@code SuccessGroupPictures} reply variant — carries a
     * {@code <pictures>} wrapper holding one {@link Picture} per
     * requested group.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetGroupProfilePicturesResponseSuccessGroupPictures")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetGroupProfilePicturesProfilePicturesResponseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetGroupProfilePicturesSuccessOrGetGroupProfilePicturesPartialProfilePictureResponseMixinGroup")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsParentOrSubGroupMixinGroup")
    final class Success implements SmaxGroupsGetGroupProfilePicturesResponse {
        /**
         * The list of picture replies, one per requested group.
         */
        private final List<Picture> pictures;

        /**
         * Constructs a new successful reply.
         *
         * @param pictures the per-picture replies; never
         *                 {@code null}
         * @throws NullPointerException if {@code pictures} is
         *                              {@code null}
         */
        public Success(List<Picture> pictures) {
            Objects.requireNonNull(pictures, "pictures cannot be null");
            this.pictures = List.copyOf(pictures);
        }

        /**
         * Returns the per-picture replies.
         *
         * @return an unmodifiable list of picture replies; never
         *         {@code null}
         */
        public List<Picture> pictures() {
            return pictures;
        }

        /**
         * Tries to parse a {@link Success} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant,
         *         or empty when the stanza does not match the
         *         success schema
         */
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetGroupProfilePicturesResponseSuccessGroupPictures",
                exports = "parseGetGroupProfilePicturesResponseSuccessGroupPictures",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var picturesWrapper = node.getChild("pictures").orElse(null);
            if (picturesWrapper == null) {
                return Optional.empty();
            }
            var pictureChildren = picturesWrapper.getChildren("picture");
            if (pictureChildren.isEmpty()) {
                return Optional.empty();
            }
            var pictures = new ArrayList<Picture>(pictureChildren.size());
            for (var pictureNode : pictureChildren) {
                var picture = Picture.of(pictureNode).orElse(null);
                if (picture == null) {
                    return Optional.empty();
                }
                pictures.add(picture);
            }
            return Optional.of(new Success(pictures));
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
            return Objects.equals(this.pictures, that.pictures);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pictures);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetGroupProfilePicturesResponse.Success[pictures=" + pictures + ']';
        }
    }

    /**
     * Per-picture projection — carries the addressing JID alongside
     * the optional {@code id}, {@code type}, {@code url},
     * {@code direct_path} and inline {@code blob} bytes.
     *
     * <p>The relay's per-picture response is a disjunction of two
     * sub-shapes: the "success" projection (carrying the picture
     * URL or blob) and the "partial" projection (carrying a
     * {@code did_not_change}/{@code not_found}/error marker). This
     * record unifies both branches; {@link #url}/{@link #directPath}/{@link #blob}
     * are populated only on the success branch, and the
     * {@code <picture/>} child is exposed verbatim via
     * {@link #raw()} so callers can inspect any sub-marker the
     * partial branch may carry.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetGroupProfilePicturesSuccessProfilePictureResponseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetGroupProfilePicturesPartialProfilePictureResponseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsProfilePictureUrlResponseMixin")
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsProfilePictureBlobResponseMixin")
    final class Picture {
        /**
         * The parent-group JID when this picture targets a parent
         * group; mutually exclusive with {@link #subGroupJid}.
         */
        private final Jid parentGroupJid;

        /**
         * The sub-group JID when this picture targets a sub-group;
         * mutually exclusive with {@link #parentGroupJid}.
         */
        private final Jid subGroupJid;

        /**
         * The picture id, when the relay supplied one.
         */
        private final String pictureId;

        /**
         * The picture type ({@code "image"} or {@code "preview"})
         * when the relay supplied one.
         */
        private final String pictureType;

        /**
         * The picture URL — populated only on the URL-projection
         * success branch.
         */
        private final String url;

        /**
         * The {@code direct_path} attribute — populated only on the
         * URL-projection success branch.
         */
        private final String directPath;

        /**
         * The inline blob bytes — populated only on the
         * blob-projection success branch.
         */
        private final byte[] blob;

        /**
         * The raw {@code <picture/>} node carrying any
         * partial-branch marker
         * ({@code did_not_change}/{@code not_found}/{@code bad_*}).
         */
        private final Node raw;

        /**
         * Constructs a picture entry.
         *
         * @param parentGroupJid optional parent-group JID
         * @param subGroupJid    optional sub-group JID
         * @param pictureId      optional picture id
         * @param pictureType    optional picture type
         * @param url            optional picture URL
         * @param directPath     optional direct-path attribute
         * @param blob           optional inline blob bytes
         * @param raw            the raw {@code <picture/>} node;
         *                       never {@code null}
         * @throws NullPointerException if {@code raw} is
         *                              {@code null}
         */
        public Picture(Jid parentGroupJid, Jid subGroupJid,
                       String pictureId, String pictureType,
                       String url, String directPath, byte[] blob, Node raw) {
            this.parentGroupJid = parentGroupJid;
            this.subGroupJid = subGroupJid;
            this.pictureId = pictureId;
            this.pictureType = pictureType;
            this.url = url;
            this.directPath = directPath;
            this.blob = blob;
            this.raw = Objects.requireNonNull(raw, "raw cannot be null");
        }

        /**
         * Returns the parent-group JID when set.
         *
         * @return an {@link Optional} carrying the JID
         */
        public Optional<Jid> parentGroupJid() {
            return Optional.ofNullable(parentGroupJid);
        }

        /**
         * Returns the sub-group JID when set.
         *
         * @return an {@link Optional} carrying the JID
         */
        public Optional<Jid> subGroupJid() {
            return Optional.ofNullable(subGroupJid);
        }

        /**
         * Returns the picture id when supplied by the relay.
         *
         * @return an {@link Optional} carrying the id
         */
        public Optional<String> pictureId() {
            return Optional.ofNullable(pictureId);
        }

        /**
         * Returns the picture type when supplied by the relay.
         *
         * @return an {@link Optional} carrying the type
         */
        public Optional<String> pictureType() {
            return Optional.ofNullable(pictureType);
        }

        /**
         * Returns the picture URL on the URL-projection branch.
         *
         * @return an {@link Optional} carrying the URL
         */
        public Optional<String> url() {
            return Optional.ofNullable(url);
        }

        /**
         * Returns the {@code direct_path} attribute on the
         * URL-projection branch.
         *
         * @return an {@link Optional} carrying the direct path
         */
        public Optional<String> directPath() {
            return Optional.ofNullable(directPath);
        }

        /**
         * Returns the inline blob bytes on the blob-projection
         * branch.
         *
         * @return an {@link Optional} carrying the blob bytes
         */
        public Optional<byte[]> blob() {
            return Optional.ofNullable(blob);
        }

        /**
         * Returns the raw {@code <picture/>} node carrying the
         * remaining attributes and any partial-branch sub-marker.
         *
         * @return the raw node; never {@code null}
         */
        public Node raw() {
            return raw;
        }

        /**
         * Tries to parse a {@link Picture} from the given
         * {@code <picture/>} child.
         *
         * @param node the {@code <picture/>} child node
         * @return an {@link Optional} carrying the parsed picture,
         *         or empty when the child does not satisfy the
         *         addressing-disjunction schema
         */
        public static Optional<Picture> of(Node node) {
            Objects.requireNonNull(node, "node cannot be null");
            if (!node.hasDescription("picture")) {
                return Optional.empty();
            }
            var parentGroupJid = node.getAttributeAsJid("parent_group_jid").orElse(null);
            var subGroupJid = node.getAttributeAsJid("sub_group_jid").orElse(null);
            if (parentGroupJid == null && subGroupJid == null) {
                return Optional.empty();
            }
            var pictureId = node.getAttributeAsString("id").orElse(null);
            var pictureType = node.getAttributeAsString("type").orElse(null);
            var url = node.getAttributeAsString("url").orElse(null);
            var directPath = node.getAttributeAsString("direct_path").orElse(null);
            var blob = node.toContentBytes().orElse(null);
            var picture = new Picture(parentGroupJid, subGroupJid, pictureId,
                    pictureType, url, directPath, blob, node);
            return Optional.of(picture);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Picture) obj;
            return Objects.equals(this.parentGroupJid, that.parentGroupJid)
                    && Objects.equals(this.subGroupJid, that.subGroupJid)
                    && Objects.equals(this.pictureId, that.pictureId)
                    && Objects.equals(this.pictureType, that.pictureType)
                    && Objects.equals(this.url, that.url)
                    && Objects.equals(this.directPath, that.directPath)
                    && Arrays.equals(this.blob, that.blob)
                    && Objects.equals(this.raw, that.raw);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parentGroupJid, subGroupJid, pictureId, pictureType,
                    url, directPath, Arrays.hashCode(blob), raw);
        }

        @Override
        public String toString() {
            return "SmaxGroupsGetGroupProfilePicturesResponse.Picture[parentGroupJid="
                    + parentGroupJid + ", subGroupJid=" + subGroupJid
                    + ", pictureId=" + pictureId + ", pictureType=" + pictureType
                    + ", url=" + url + ", directPath=" + directPath
                    + ", blob=" + Arrays.toString(blob)
                    + ", raw=" + raw + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected
     * the request as malformed or unauthorised.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetGroupProfilePicturesResponseClientError")
    final class ClientError implements SmaxGroupsGetGroupProfilePicturesResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetGroupProfilePicturesResponseClientError",
                exports = "parseGetGroupProfilePicturesResponseClientError",
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
            return "SmaxGroupsGetGroupProfilePicturesResponse.ClientError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered
     * a transient internal failure while processing the request.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInGroupsGetGroupProfilePicturesResponseServerError")
    final class ServerError implements SmaxGroupsGetGroupProfilePicturesResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInGroupsGetGroupProfilePicturesResponseServerError",
                exports = "parseGetGroupProfilePicturesResponseServerError",
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
            return "SmaxGroupsGetGroupProfilePicturesResponse.ServerError[errorCode="
                    + errorCode + ", errorText=" + errorText + ']';
        }
    }
}
