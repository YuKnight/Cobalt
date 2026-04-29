package com.github.auties00.cobalt.node.smax.privacy;

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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxBlocklistsGetOptOutListRPC.sendGetOptOutListRPC}.
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
     * @implNote {@code WASmaxInBlocklistsBizOptOutResponseMixin.parseBizOptOutResponseMixin}
     *           projects all six business-opt-out attributes plus an
     *           optional reason/description pair. Cobalt collapses
     *           the attributes into this single record.
     */
    record Item(String reason, String reasonDescription, String entryPoint, String firstMessage,
                String businessDiscoveryEntryPoint, Long businessDiscoveryTimestamp,
                String businessDiscoveryId) {
        /**
         * Returns the reason as an {@link Optional}.
         *
         * @return the reason, or empty when omitted
         */
        public Optional<String> reasonAsOptional() {
            return Optional.ofNullable(reason);
        }

        /**
         * Returns the reason description as an {@link Optional}.
         *
         * @return the description, or empty when omitted
         */
        public Optional<String> reasonDescriptionAsOptional() {
            return Optional.ofNullable(reasonDescription);
        }

        /**
         * Returns the entry point as an {@link Optional}.
         *
         * @return the entry point, or empty when omitted
         */
        public Optional<String> entryPointAsOptional() {
            return Optional.ofNullable(entryPoint);
        }

        /**
         * Returns the first message as an {@link Optional}.
         *
         * @return the first message, or empty when omitted
         */
        public Optional<String> firstMessageAsOptional() {
            return Optional.ofNullable(firstMessage);
        }

        /**
         * Returns the business-discovery entry point as an
         * {@link Optional}.
         *
         * @return the entry point, or empty when omitted
         */
        public Optional<String> businessDiscoveryEntryPointAsOptional() {
            return Optional.ofNullable(businessDiscoveryEntryPoint);
        }

        /**
         * Returns the business-discovery timestamp as an
         * {@link Optional}.
         *
         * @return the timestamp, or empty when omitted
         */
        public Optional<Long> businessDiscoveryTimestampAsOptional() {
            return Optional.ofNullable(businessDiscoveryTimestamp);
        }

        /**
         * Returns the business-discovery id as an {@link Optional}.
         *
         * @return the id, or empty when omitted
         */
        public Optional<String> businessDiscoveryIdAsOptional() {
            return Optional.ofNullable(businessDiscoveryId);
        }
    }

    /**
     * Parses a {@code <biz_opt_out>} child of an item node.
     *
     * @param itemNode the {@code <item>} container
     * @return the populated {@link Item}; never {@code null}
     */
    private static Item parseItem(Node itemNode) {
        var bizOptOut = itemNode.getChild("biz_opt_out").orElse(itemNode);
        var reason = bizOptOut.getAttributeAsString("reason").orElse(null);
        var reasonDescription = bizOptOut.getAttributeAsString("reason_description").orElse(null);
        var entryPoint = bizOptOut.getAttributeAsString("entry_point").orElse(null);
        var firstMessage = bizOptOut.getAttributeAsString("first_message").orElse(null);
        var bdEntryPoint = bizOptOut.getAttributeAsString("business_discovery_entry_point").orElse(null);
        var bdTimestampOpt = bizOptOut.getAttributeAsString("business_discovery_timestamp").orElse(null);
        Long bdTimestamp = null;
        if (bdTimestampOpt != null) {
            try {
                bdTimestamp = Long.parseLong(bdTimestampOpt);
            } catch (NumberFormatException ignored) {
                bdTimestamp = null;
            }
        }
        var bdId = bizOptOut.getAttributeAsString("business_discovery_id").orElse(null);
        return new Item(reason, reasonDescription, entryPoint, firstMessage,
                bdEntryPoint, bdTimestamp, bdId);
    }

    /**
     * The {@code SuccessWithMatch} reply variant — the client's
     * cache is up to date.
     *
     * @implNote {@code WASmaxInBlocklistsGetOptOutListResponseSuccessWithMatch.parseGetOptOutListResponseSuccessWithMatch}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetOptOutListResponseSuccessWithMatch")
    final class SuccessWithMatch implements SmaxGetOptOutListResponse {
        /**
         * Whether the request carried a {@code category} attribute.
         * The relay echoes it; Cobalt projects only its presence.
         */
        private final boolean hasCategory;

        /**
         * Constructs a successful match reply.
         *
         * @param hasCategory whether the request was category-scoped
         */
        public SuccessWithMatch(boolean hasCategory) {
            this.hasCategory = hasCategory;
        }

        /**
         * Returns whether the request was category-scoped.
         *
         * @return {@code true} when the request carried a category
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
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            if (node.getChild("list").isPresent()) {
                return Optional.empty();
            }
            var requestCategory = request.getAttributeAsString("category").orElse(null);
            if (requestCategory != null && !node.hasAttribute("category", requestCategory)) {
                return Optional.empty();
            }
            return Optional.of(new SuccessWithMatch(requestCategory != null));
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
     * The {@code SuccessWithMismatch} reply variant — the relay
     * returned the full opt-out list because the client's cached
     * digest does not match.
     *
     * @implNote {@code WASmaxInBlocklistsGetOptOutListResponseSuccessWithMismatch.parseGetOptOutListResponseSuccessWithMismatch}.
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
                items.add(parseItem(child));
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
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed.
     *
     * @implNote {@code WASmaxInBlocklistsGetOptOutListResponseInvalidRequest.parseGetOptOutListResponseInvalidRequest}.
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
     * The {@code ServerError} reply variant — transient internal
     * failure.
     *
     * @implNote {@code WASmaxInBlocklistsGetOptOutListResponseInternalServerError.parseGetOptOutListResponseInternalServerError}.
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
