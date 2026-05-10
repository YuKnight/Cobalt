package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 */
public sealed interface SmaxGetOptOutListResponse extends SmaxOperation.Response
        permits SmaxGetOptOutListResponse.SuccessWithMatch,
        SmaxGetOptOutListResponse.SuccessWithMismatch,
        SmaxGetOptOutListResponse.ClientError,
        SmaxGetOptOutListResponse.ServerError {

    /**
     * Tries each {@link SmaxGetOptOutListResponse} variant in priority order.
     *
     * @param node    the inbound stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxBlocklistsGetOptOutListRPC",
            exports = "sendGetOptOutListRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGetOptOutListResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var successWithMismatch = SuccessWithMismatch.of(node, request);
        if (successWithMismatch.isPresent()) {
            return successWithMismatch;
        }
        var successWithMatch = SuccessWithMatch.of(node, request);
        if (successWithMatch.isPresent()) {
            return successWithMatch;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * Descriptor for one entry in the relay-returned opt-out list.
     *
     * <p>Mirrors the {@code WAResultOrError} payload of
     * {@code WASmaxInBlocklistsBizOptOutResponseMixin.parseBizOptOutResponseMixin},
     * which exposes optional {@code action} / {@code category} /
     * {@code expiry_at} attributes plus the
     * {@link BizOptOutId} disjunction.
     *
     * @param action       the optional {@code action} attribute (e.g.
     *                     {@code "block"}); may be {@code null}
     * @param category     the optional {@code category} attribute; may
     *                     be {@code null}
     * @param expiryAt     the optional {@code expiry_at} attribute,
     *                     interpreted as a non-negative integer; may
     *                     be {@code null}
     * @param bizOptOutIds the {@link BizOptOutId} disjunction; never
     *                     {@code null}
     */
    record Item(String action, String category, Long expiryAt, BizOptOutId bizOptOutIds) {
        /**
         * Compact constructor that null-checks the disjunction.
         *
         * @param action       the optional action; may be {@code null}
         * @param category     the optional category; may be {@code null}
         * @param expiryAt     the optional expiry; may be {@code null}
         * @param bizOptOutIds the disjunction; never {@code null}
         * @throws NullPointerException if {@code bizOptOutIds} is
         *                              {@code null}
         */
        public Item {
            Objects.requireNonNull(bizOptOutIds, "bizOptOutIds cannot be null");
        }

        /**
         * Returns the action as an {@link Optional}.
         *
         * @return the action, or empty when omitted
         */
        public Optional<String> actionAsOptional() {
            return Optional.ofNullable(action);
        }

        /**
         * Returns the category as an {@link Optional}.
         *
         * @return the category, or empty when omitted
         */
        public Optional<String> categoryAsOptional() {
            return Optional.ofNullable(category);
        }

        /**
         * Returns the expiry-at timestamp as an {@link Optional}.
         *
         * @return the timestamp, or empty when omitted
         */
        public Optional<Long> expiryAtAsOptional() {
            return Optional.ofNullable(expiryAt);
        }
    }

    /**
     * Parses a single {@code <item>} entry, mirroring
     * {@code WASmaxInBlocklistsBizOptOutResponseMixin.parseBizOptOutResponseMixin}.
     *
     * @param itemNode the {@code <item>} node; never {@code null}
     * @return an {@link Optional} carrying the populated
     *         {@link Item}, or empty when the
     *         {@code biz_opt_out_ids} disjunction does not match or
     *         {@code expiry_at} is present but negative
     */
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsBizOptOutResponseMixin",
            exports = "parseBizOptOutResponseMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetOptOutListResponseSuccessWithMismatch",
            exports = "parseGetOptOutListResponseSuccessWithMismatchListItem",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static Optional<Item> parseItem(Node itemNode) {
        // WASmaxInBlocklistsBizOptOutResponseMixin.parseBizOptOutResponseMixin:
        // optional(attrString, item, "action")
        var action = itemNode.getAttributeAsString("action").orElse(null);
        // WASmaxInBlocklistsBizOptOutResponseMixin.parseBizOptOutResponseMixin:
        // optional(attrString, item, "category")
        var category = itemNode.getAttributeAsString("category").orElse(null);
        // WASmaxInBlocklistsBizOptOutResponseMixin.parseBizOptOutResponseMixin:
        // optional(attrIntRange, item, "expiry_at", 0, void 0)
        Long expiryAt = null;
        if (itemNode.hasAttribute("expiry_at")) {
            var parsed = itemNode.getAttributeAsLong("expiry_at");
            if (parsed.isEmpty() || parsed.getAsLong() < 0L) {
                return Optional.empty();
            }
            expiryAt = parsed.getAsLong();
        }
        // WASmaxInBlocklistsBizOptOutResponseMixin.parseBizOptOutResponseMixin:
        // WASmaxInBlocklistsBizOptOutIds.parseBizOptOutIds(item)
        var ids = BizOptOutId.parse(itemNode).orElse(null);
        if (ids == null) {
            return Optional.empty();
        }
        return Optional.of(new Item(action, category, expiryAt, ids));
    }

    /**
     * The {@code SuccessWithMatch} reply variant. The client's
     * cache is up to date.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetOptOutListResponseSuccessWithMatch")
    final class SuccessWithMatch implements SmaxGetOptOutListResponse {
        /**
         * Whether the inbound reply carried a {@code category}
         * attribute. Mirrors {@code hasCategory: s.value != null} from
         * {@code WASmaxInBlocklistsGetOptOutListResponseSuccessWithMatch.parseGetOptOutListResponseSuccessWithMatch}.
         */
        private final boolean hasCategory;

        /**
         * Constructs a successful match reply.
         *
         * @param hasCategory whether the reply echoed a category
         *                    attribute
         */
        public SuccessWithMatch(boolean hasCategory) {
            this.hasCategory = hasCategory;
        }

        /**
         * Returns whether the reply echoed a category attribute.
         *
         * @return {@code true} when the reply carried a category
         */
        public boolean hasCategory() {
            return hasCategory;
        }

        /**
         * Tries to parse a {@link SuccessWithMatch} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the envelope shape does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetOptOutListResponseSuccessWithMatch",
                exports = "parseGetOptOutListResponseSuccessWithMatch",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessWithMatch> of(Node node, Node request) {
            // WASmaxInBlocklistsGetOptOutListResponseSuccessWithMatch.parseGetOptOutListResponseSuccessWithMatch:
            // assertTag(e,"iq") + attrStringFromReference(t,["to"]) + literal(attrString,e,"from",...)
            // + literal(attrString,e,"type","result") + literal(attrString,e,"id",...).
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            // WASmaxInBlocklistsGetOptOutListResponseSuccessWithMatch.parseGetOptOutListResponseSuccessWithMatch:
            // l = optionalAttrStringFromReference(t,["category"]); s = optionalLiteral(attrString,e,"category",l.value).
            // optionalLiteral semantics: when expected (request) value is null -> trivially succeed (no
            // reply check). When expected value is non-null -> reply must echo it OR be absent. When reply
            // is present but mismatches -> variant rejected. The reported hasCategory is s.value != null,
            // i.e. whether the REPLY actually carried the category attribute.
            var requestCategory = request.getAttributeAsString("category").orElse(null);
            var replyCategory = node.getAttributeAsString("category").orElse(null);
            if (requestCategory != null && replyCategory != null && !replyCategory.equals(requestCategory)) {
                return Optional.empty();
            }
            return Optional.of(new SuccessWithMatch(replyCategory != null));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessWithMatch) obj;
            return this.hasCategory == that.hasCategory;
        }

        @Override
        public int hashCode() {
            return Objects.hash(hasCategory);
        }

        @Override
        public String toString() {
            return "SmaxGetOptOutListResponse.SuccessWithMatch[hasCategory=" + hasCategory + ']';
        }
    }

    /**
     * The {@code SuccessWithMismatch} reply variant. The relay
     * returned the full opt-out list because the client's cached
     * digest does not match.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetOptOutListResponseSuccessWithMismatch")
    final class SuccessWithMismatch implements SmaxGetOptOutListResponse {
        /**
         * The optional new server-side digest.
         */
        private final String listDhash;

        /**
         * The parsed list of opt-out entries.
         */
        private final List<Item> listItem;

        /**
         * Constructs a mismatch reply.
         *
         * @param listDhash the new digest; may be {@code null}
         * @param listItem  the parsed list; never {@code null}
         */
        public SuccessWithMismatch(String listDhash, List<Item> listItem) {
            this.listDhash = listDhash;
            this.listItem = List.copyOf(Objects.requireNonNull(listItem, "listItem cannot be null"));
        }

        /**
         * Returns the optional new digest.
         *
         * @return an {@link Optional} carrying the digest, or empty
         *         when omitted
         */
        public Optional<String> listDhash() {
            return Optional.ofNullable(listDhash);
        }

        /**
         * Returns the parsed item list.
         *
         * @return an unmodifiable list of items; never {@code null}
         */
        public List<Item> listItem() {
            return listItem;
        }

        /**
         * Tries to parse a {@link SuccessWithMismatch} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the envelope shape does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetOptOutListResponseSuccessWithMismatch",
                exports = "parseGetOptOutListResponseSuccessWithMismatch",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessWithMismatch> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var list = node.getChild("list").orElse(null);
            if (list == null) {
                return Optional.empty();
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            var items = new ArrayList<Item>();
            for (var child : list.getChildren("item")) {
                var parsed = parseItem(child).orElse(null);
                if (parsed == null) {
                    return Optional.empty();
                }
                items.add(parsed);
            }
            return Optional.of(new SuccessWithMismatch(dhash, Collections.unmodifiableList(items)));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (SuccessWithMismatch) obj;
            return Objects.equals(this.listDhash, that.listDhash)
                    && Objects.equals(this.listItem, that.listItem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listDhash, listItem);
        }

        @Override
        public String toString() {
            return "SmaxGetOptOutListResponse.SuccessWithMismatch[listDhash=" + listDhash
                    + ", listItem=" + listItem + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * request as malformed.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetOptOutListResponseInvalidRequest")
    final class ClientError implements SmaxGetOptOutListResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional error text; may be
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on schema mismatch
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetOptOutListResponseInvalidRequest",
                exports = "parseGetOptOutListResponseInvalidRequest",
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
            return "SmaxGetOptOutListResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. Transient internal
     * failure.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetOptOutListResponseInternalServerError")
    final class ServerError implements SmaxGetOptOutListResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional error text; may be
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
         * Returns the optional error text.
         *
         * @return an {@link Optional} carrying the error text, or
         *         empty when omitted
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on schema mismatch
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetOptOutListResponseInternalServerError",
                exports = "parseGetOptOutListResponseInternalServerError",
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
            return "SmaxGetOptOutListResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
