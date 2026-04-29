package com.github.auties00.cobalt.node.smax.privacy;

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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxBlocklistsUpdateOptOutListRPC.sendUpdateOptOutListRPC}.
 */
public sealed interface SmaxUpdateOptOutListResponse extends SmaxOperation.Response
        permits SmaxUpdateOptOutListResponse.SuccessWithMatch,
        SmaxUpdateOptOutListResponse.SuccessWithMismatch,
        SmaxUpdateOptOutListResponse.ClientError,
        SmaxUpdateOptOutListResponse.ServerError {

    /**
     * Tries each {@link SmaxUpdateOptOutListResponse} variant in priority order.
     *
     * @param node    the inbound stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxBlocklistsUpdateOptOutListRPC",
            exports = "sendUpdateOptOutListRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxUpdateOptOutListResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var successWithMatch = SuccessWithMatch.of(node, request);
        if (successWithMatch.isPresent()) {
            return successWithMatch;
        }
        var successWithMismatch = SuccessWithMismatch.of(node, request);
        if (successWithMismatch.isPresent()) {
            return successWithMismatch;
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
     *           projects optional {@code action}, {@code category},
     *           {@code expiry_at}, and a sub-bundle of business
     *           opt-out ids; Cobalt collapses the action/category
     *           pair plus expiry into this record.
     */
    record Item(String action, String category, Long expiryAt) {
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
     * Parses a single {@code <item>} entry, including the optional
     * {@code <biz_opt_out>} child.
     *
     * @param itemNode the source node; never {@code null}
     * @return the populated {@link Item}; never {@code null}
     */
    private static Item parseItem(Node itemNode) {
        var bizOptOut = itemNode.getChild("biz_opt_out").orElse(itemNode);
        var action = bizOptOut.getAttributeAsString("action").orElse(null);
        var category = bizOptOut.getAttributeAsString("category").orElse(null);
        var expiryAtOpt = bizOptOut.getAttributeAsLong("expiry_at");
        Long expiryAt = expiryAtOpt.isPresent() ? expiryAtOpt.getAsLong() : null;
        return new Item(action, category, expiryAt);
    }

    /**
     * The {@code SuccessWithMatch} reply variant — the relay applied
     * the action and the client's cache is up to date
     * ({@code matched="true"}).
     *
     * @implNote {@code WASmaxInBlocklistsUpdateOptOutListResponseSuccessWithMatch.parseUpdateOptOutListResponseSuccessWithMatch}
     *           validates the {@code <iq from id type="result">}
     *           envelope, asserts {@code <list matched="true" dhash>}
     *           and the inner {@code <item>} child, then projects the
     *           single item via {@code parseBizOptOutResponseMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateOptOutListResponseSuccessWithMatch")
    final class SuccessWithMatch implements SmaxUpdateOptOutListResponse {
        /**
         * The new server-side digest of the opt-out list.
         */
        private final String listDhash;

        /**
         * The single item descriptor returned by the relay.
         */
        private final Item item;

        /**
         * Constructs a new successful match reply.
         *
         * @param listDhash the new digest; never {@code null}
         * @param item      the item descriptor; never {@code null}
         * @throws NullPointerException if either argument is
         *                              {@code null}
         */
        public SuccessWithMatch(String listDhash, Item item) {
            this.listDhash = Objects.requireNonNull(listDhash, "listDhash cannot be null");
            this.item = Objects.requireNonNull(item, "item cannot be null");
        }

        /**
         * Returns the new digest.
         *
         * @return the digest; never {@code null}
         */
        public String listDhash() {
            return listDhash;
        }

        /**
         * Returns the single item descriptor.
         *
         * @return the item; never {@code null}
         */
        public Item item() {
            return item;
        }

        /**
         * Tries to parse a {@link SuccessWithMatch} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on schema mismatch
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateOptOutListResponseSuccessWithMatch",
                exports = "parseUpdateOptOutListResponseSuccessWithMatch",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessWithMatch> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var list = node.getChild("list").orElse(null);
            if (list == null) {
                return Optional.empty();
            }
            if (!list.hasAttribute("matched", "true")) {
                return Optional.empty();
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            if (dhash == null) {
                return Optional.empty();
            }
            var itemNode = list.getChild("item").orElse(null);
            if (itemNode == null) {
                return Optional.empty();
            }
            return Optional.of(new SuccessWithMatch(dhash, parseItem(itemNode)));
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
            return Objects.equals(this.listDhash, that.listDhash)
                    && Objects.equals(this.item, that.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listDhash, item);
        }

        @Override
        public String toString() {
            return "SmaxUpdateOptOutListResponse.SuccessWithMatch[listDhash=" + listDhash
                    + ", item=" + item + ']';
        }
    }

    /**
     * The {@code SuccessWithMismatch} reply variant — the relay
     * applied the action but the client's cache was stale
     * ({@code matched="false"}); the relay returned the resulting
     * opt-out list.
     *
     * @implNote {@code WASmaxInBlocklistsUpdateOptOutListResponseSuccessWithMismatch.parseUpdateOptOutListResponseSuccessWithMismatch}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateOptOutListResponseSuccessWithMismatch")
    final class SuccessWithMismatch implements SmaxUpdateOptOutListResponse {
        /**
         * Whether the {@code c_dhash} attribute was present
         * (signalling an explicit stale-cache echo).
         */
        private final boolean hasListCDhash;

        /**
         * The new server-side digest.
         */
        private final String listDhash;

        /**
         * The parsed item list.
         */
        private final List<Item> listItem;

        /**
         * Constructs a new mismatch reply.
         *
         * @param hasListCDhash whether {@code c_dhash} was present
         * @param listDhash     the new digest; never {@code null}
         * @param listItem      the parsed list; never {@code null}
         * @throws NullPointerException if {@code listDhash} or
         *                              {@code listItem} is
         *                              {@code null}
         */
        public SuccessWithMismatch(boolean hasListCDhash, String listDhash, List<Item> listItem) {
            this.hasListCDhash = hasListCDhash;
            this.listDhash = Objects.requireNonNull(listDhash, "listDhash cannot be null");
            this.listItem = List.copyOf(Objects.requireNonNull(listItem, "listItem cannot be null"));
        }

        /**
         * Returns whether the {@code c_dhash} attribute was present.
         *
         * @return {@code true} when present
         */
        public boolean hasListCDhash() {
            return hasListCDhash;
        }

        /**
         * Returns the new digest.
         *
         * @return the digest; never {@code null}
         */
        public String listDhash() {
            return listDhash;
        }

        /**
         * Returns the parsed item list.
         *
         * @return an unmodifiable list; never {@code null}
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
         *         empty on schema mismatch
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateOptOutListResponseSuccessWithMismatch",
                exports = "parseUpdateOptOutListResponseSuccessWithMismatch",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessWithMismatch> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var list = node.getChild("list").orElse(null);
            if (list == null) {
                return Optional.empty();
            }
            if (!list.hasAttribute("matched", "false")) {
                return Optional.empty();
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            if (dhash == null) {
                return Optional.empty();
            }
            var hasListCDhash = list.getAttributeAsString("c_dhash").isPresent();
            var entries = new ArrayList<Item>();
            for (var itemNode : list.getChildren("item")) {
                entries.add(parseItem(itemNode));
            }
            return Optional.of(new SuccessWithMismatch(hasListCDhash, dhash, entries));
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
            return this.hasListCDhash == that.hasListCDhash
                    && Objects.equals(this.listDhash, that.listDhash)
                    && Objects.equals(this.listItem, that.listItem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hasListCDhash, listDhash, listItem);
        }

        @Override
        public String toString() {
            return "SmaxUpdateOptOutListResponse.SuccessWithMismatch[hasListCDhash=" + hasListCDhash
                    + ", listDhash=" + listDhash
                    + ", listItem=" + listItem + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed.
     *
     * @implNote {@code WASmaxInBlocklistsUpdateOptOutListResponseInvalidRequest.parseUpdateOptOutListResponseInvalidRequest}
     *           routes through {@code parseUpdateOptoutErrors};
     *           Cobalt collapses to the {@code (code, text)} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateOptOutListResponseInvalidRequest")
    final class ClientError implements SmaxUpdateOptOutListResponse {
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
         * @return an {@link Optional} carrying the text, or empty when
         *         omitted
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
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateOptOutListResponseInvalidRequest",
                exports = "parseUpdateOptOutListResponseInvalidRequest",
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
            return "SmaxUpdateOptOutListResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — transient internal
     * failure.
     *
     * @implNote {@code WASmaxInBlocklistsUpdateOptOutListResponseServerError.parseUpdateOptOutListResponseServerError}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateOptOutListResponseServerError")
    final class ServerError implements SmaxUpdateOptOutListResponse {
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
         * @return an {@link Optional} carrying the text, or empty when
         *         omitted
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
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateOptOutListResponseServerError",
                exports = "parseUpdateOptOutListResponseServerError",
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
            return "SmaxUpdateOptOutListResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
