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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants.
 *
 * @implNote {@code WASmaxBlocklistsUpdateBlockListRPC.sendUpdateBlockListRPC}.
 */
public sealed interface SmaxUpdateBlockListResponse extends SmaxOperation.Response
        permits SmaxUpdateBlockListResponse.SuccessWithMatch,
        SmaxUpdateBlockListResponse.SuccessWithMismatch,
        SmaxUpdateBlockListResponse.MigratedSuccessWithMismatch,
        SmaxUpdateBlockListResponse.CAPISuccessWithMismatch,
        SmaxUpdateBlockListResponse.ClientError,
        SmaxUpdateBlockListResponse.ServerError {

    /**
     * Tries each {@link SmaxUpdateBlockListResponse} variant in priority order.
     *
     * @param node    the inbound stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxBlocklistsUpdateBlockListRPC",
            exports = "sendUpdateBlockListRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxUpdateBlockListResponse> of(Node node, Node request) {
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
        var migratedSuccessWithMismatch = MigratedSuccessWithMismatch.of(node, request);
        if (migratedSuccessWithMismatch.isPresent()) {
            return migratedSuccessWithMismatch;
        }
        var capiSuccessWithMismatch = CAPISuccessWithMismatch.of(node, request);
        if (capiSuccessWithMismatch.isPresent()) {
            return capiSuccessWithMismatch;
        }
        var clientError = ClientError.of(node, request);
        if (clientError.isPresent()) {
            return clientError;
        }
        return ServerError.of(node, request);
    }

    /**
     * Descriptor for one entry in a mismatch reply's item list.
     *
     * @implNote {@code WASmaxInBlocklistsGetBlockListResponseSuccessWithMismatchListItem}
     *           and the migrated/CAPI variants project this same shape.
     */
    record Item(Jid jid, boolean active, String displayName, String blocklistIdentifier) {
        /**
         * Returns the display name as an {@link Optional}.
         *
         * @return the display name, or empty when omitted
         */
        public Optional<String> displayNameAsOptional() {
            return Optional.ofNullable(displayName);
        }

        /**
         * Returns the blocklist identifier as an {@link Optional}.
         *
         * @return the identifier, or empty when omitted
         */
        public Optional<String> blocklistIdentifierAsOptional() {
            return Optional.ofNullable(blocklistIdentifier);
        }
    }

    /**
     * Parses {@code <item/>} children into a list of {@link Item}.
     *
     * @param list       the {@code <list/>} node
     * @param requireJid whether the {@code jid} attribute is
     *                   required
     * @return the parsed list; never {@code null}
     */
    private static List<Item> parseItems(Node list, boolean requireJid) {
        var items = new ArrayList<Item>();
        for (var child : list.getChildren("item")) {
            var jid = child.getAttributeAsString("jid")
                    .map(Jid::of)
                    .orElse(null);
            if (jid == null && requireJid) {
                continue;
            }
            var active = child.hasAttribute("active", "true");
            var displayName = child.getAttributeAsString("display_name").orElse(null);
            var blocklistIdentifier = child.getAttributeAsString("blocklist_identifier").orElse(null);
            items.add(new Item(jid, active, displayName, blocklistIdentifier));
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * The {@code SuccessWithMatch} reply variant — bare echo with
     * {@code matched="true"}; the relay confirmed the action without
     * needing to return a re-synced list.
     *
     * @implNote {@code WASmaxInBlocklistsUpdateBlockListResponseSuccessWithMatch.parseUpdateBlockListResponseSuccessWithMatch}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseSuccessWithMatch")
    final class SuccessWithMatch implements SmaxUpdateBlockListResponse {
        /**
         * The new server-side digest of the blocklist.
         */
        private final String listDhash;

        /**
         * Constructs a successful match reply.
         *
         * @param listDhash the new digest; never {@code null}
         * @throws NullPointerException if {@code listDhash} is
         *                              {@code null}
         */
        public SuccessWithMatch(String listDhash) {
            this.listDhash = Objects.requireNonNull(listDhash, "listDhash cannot be null");
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
         * Tries to parse a {@link SuccessWithMatch} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the envelope shape does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseSuccessWithMatch",
                exports = "parseUpdateBlockListResponseSuccessWithMatch",
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
            return Optional.of(new SuccessWithMatch(dhash));
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
            return Objects.equals(this.listDhash, that.listDhash);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listDhash);
        }

        @Override
        public String toString() {
            return "SmaxUpdateBlockListResponse.SuccessWithMatch[listDhash=" + listDhash + ']';
        }
    }

    /**
     * The {@code SuccessWithMismatch} reply variant — the relay
     * applied the action but also returned the resulting blocklist
     * because the client's cache was stale (matched="false").
     *
     * @implNote {@code WASmaxInBlocklistsUpdateBlockListResponseSuccessWithMismatch.parseUpdateBlockListResponseSuccessWithMismatch}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseSuccessWithMismatch")
    final class SuccessWithMismatch implements SmaxUpdateBlockListResponse {
        /**
         * Whether the relay echoed the request's {@code dhash} as
         * the {@code c_dhash} attribute (signalling stale cache).
         */
        private final boolean hasListCDhash;

        /**
         * The new server-side digest.
         */
        private final String listDhash;

        /**
         * Whether the list is PN-addressed.
         */
        private final boolean phoneNumberAddressed;

        /**
         * The parsed item list.
         */
        private final List<Item> listItem;

        /**
         * Constructs a mismatch reply.
         *
         * @param hasListCDhash        whether {@code c_dhash} was
         *                             present
         * @param listDhash            the new digest; never
         *                             {@code null}
         * @param phoneNumberAddressed whether the list is
         *                             PN-addressed
         * @param listItem             the parsed list; never
         *                             {@code null}
         */
        public SuccessWithMismatch(boolean hasListCDhash, String listDhash,
                                   boolean phoneNumberAddressed, List<Item> listItem) {
            this.hasListCDhash = hasListCDhash;
            this.listDhash = Objects.requireNonNull(listDhash, "listDhash cannot be null");
            this.phoneNumberAddressed = phoneNumberAddressed;
            this.listItem = List.copyOf(Objects.requireNonNull(listItem, "listItem cannot be null"));
        }

        /**
         * Returns whether the {@code c_dhash} attribute was present.
         *
         * @return {@code true} when {@code c_dhash} was present
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
         * Returns whether the list is PN-addressed.
         *
         * @return {@code true} when the list is PN-addressed
         */
        public boolean phoneNumberAddressed() {
            return phoneNumberAddressed;
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
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseSuccessWithMismatch",
                exports = "parseUpdateBlockListResponseSuccessWithMismatch",
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
            var addressingMode = list.getAttributeAsString("addressing_mode").orElse(null);
            if (addressingMode != null && !addressingMode.equals("pn")) {
                return Optional.empty();
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            if (dhash == null) {
                return Optional.empty();
            }
            var hasListCDhash = list.getAttributeAsString("c_dhash").isPresent();
            var items = parseItems(list, true);
            return Optional.of(new SuccessWithMismatch(hasListCDhash, dhash, addressingMode != null, items));
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
                    && this.phoneNumberAddressed == that.phoneNumberAddressed
                    && Objects.equals(this.listDhash, that.listDhash)
                    && Objects.equals(this.listItem, that.listItem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hasListCDhash, listDhash, phoneNumberAddressed, listItem);
        }

        @Override
        public String toString() {
            return "SmaxUpdateBlockListResponse.SuccessWithMismatch[hasListCDhash=" + hasListCDhash
                    + ", listDhash=" + listDhash
                    + ", phoneNumberAddressed=" + phoneNumberAddressed
                    + ", listItem=" + listItem + ']';
        }
    }

    /**
     * The {@code MigratedSuccessWithMismatch} reply variant — same
     * shape as {@link SuccessWithMismatch} but for an
     * LID-addressed blocklist.
     *
     * @implNote {@code WASmaxInBlocklistsUpdateBlockListResponseMigratedSuccessWithMismatch.parseUpdateBlockListResponseMigratedSuccessWithMismatch}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseMigratedSuccessWithMismatch")
    final class MigratedSuccessWithMismatch implements SmaxUpdateBlockListResponse {
        /**
         * Whether the {@code c_dhash} attribute was present.
         */
        private final boolean hasListCDhash;

        /**
         * The new digest.
         */
        private final String listDhash;

        /**
         * The parsed item list (LID-addressed, jid required).
         */
        private final List<Item> listItem;

        /**
         * Constructs a migrated mismatch reply.
         *
         * @param hasListCDhash whether {@code c_dhash} was present
         * @param listDhash     the new digest; never {@code null}
         * @param listItem      the parsed list; never {@code null}
         */
        public MigratedSuccessWithMismatch(boolean hasListCDhash, String listDhash, List<Item> listItem) {
            this.hasListCDhash = hasListCDhash;
            this.listDhash = Objects.requireNonNull(listDhash, "listDhash cannot be null");
            this.listItem = List.copyOf(Objects.requireNonNull(listItem, "listItem cannot be null"));
        }

        /**
         * Returns whether {@code c_dhash} was present.
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
         * Tries to parse a {@link MigratedSuccessWithMismatch}
         * variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on schema mismatch
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseMigratedSuccessWithMismatch",
                exports = "parseUpdateBlockListResponseMigratedSuccessWithMismatch",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<MigratedSuccessWithMismatch> of(Node node, Node request) {
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
            if (!list.hasAttribute("addressing_mode", "lid")) {
                return Optional.empty();
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            if (dhash == null) {
                return Optional.empty();
            }
            var hasListCDhash = list.getAttributeAsString("c_dhash").isPresent();
            var items = parseItems(list, true);
            return Optional.of(new MigratedSuccessWithMismatch(hasListCDhash, dhash, items));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (MigratedSuccessWithMismatch) obj;
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
            return "SmaxUpdateBlockListResponse.MigratedSuccessWithMismatch[hasListCDhash=" + hasListCDhash
                    + ", listDhash=" + listDhash
                    + ", listItem=" + listItem + ']';
        }
    }

    /**
     * The {@code CAPISuccessWithMismatch} reply variant — wire-shape
     * equivalent to {@link MigratedSuccessWithMismatch} but with
     * the inner {@code <item jid/>} attribute optional.
     *
     * @implNote {@code WASmaxInBlocklistsUpdateBlockListResponseCAPISuccessWithMismatch.parseUpdateBlockListResponseCAPISuccessWithMismatch}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseCAPISuccessWithMismatch")
    final class CAPISuccessWithMismatch implements SmaxUpdateBlockListResponse {
        /**
         * Whether the {@code c_dhash} attribute was present.
         */
        private final boolean hasListCDhash;

        /**
         * The new digest.
         */
        private final String listDhash;

        /**
         * The parsed item list (sparsely populated).
         */
        private final List<Item> listItem;

        /**
         * Constructs a CAPI mismatch reply.
         *
         * @param hasListCDhash whether {@code c_dhash} was present
         * @param listDhash     the new digest; never {@code null}
         * @param listItem      the parsed list; never {@code null}
         */
        public CAPISuccessWithMismatch(boolean hasListCDhash, String listDhash, List<Item> listItem) {
            this.hasListCDhash = hasListCDhash;
            this.listDhash = Objects.requireNonNull(listDhash, "listDhash cannot be null");
            this.listItem = List.copyOf(Objects.requireNonNull(listItem, "listItem cannot be null"));
        }

        /**
         * Returns whether {@code c_dhash} was present.
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
         * Tries to parse a {@link CAPISuccessWithMismatch} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on schema mismatch
         *
         * @implNote The wire shape is identical to
         *           {@link MigratedSuccessWithMismatch} except that
         *           the per-item {@code jid} attribute is optional.
         *           The fall-through ordering in
         *           {@link SmaxUpdateBlockListResponse#of(Node, Node)} attempts the
         *           strict migrated variant first, so the CAPI
         *           variant only matches when the migrated parser
         *           rejects the stanza — typically because of a
         *           missing per-item jid.
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseCAPISuccessWithMismatch",
                exports = "parseUpdateBlockListResponseCAPISuccessWithMismatch",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<CAPISuccessWithMismatch> of(Node node, Node request) {
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
            if (!list.hasAttribute("addressing_mode", "lid")) {
                return Optional.empty();
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            if (dhash == null) {
                return Optional.empty();
            }
            var hasListCDhash = list.getAttributeAsString("c_dhash").isPresent();
            var items = parseItems(list, false);
            return Optional.of(new CAPISuccessWithMismatch(hasListCDhash, dhash, items));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (CAPISuccessWithMismatch) obj;
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
            return "SmaxUpdateBlockListResponse.CAPISuccessWithMismatch[hasListCDhash=" + hasListCDhash
                    + ", listDhash=" + listDhash
                    + ", listItem=" + listItem + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — request rejected as
     * malformed, optionally carrying an addressing-mode hint.
     *
     * @implNote {@code WASmaxInBlocklistsUpdateBlockListResponseInvalidRequest.parseUpdateBlockListResponseInvalidRequest}
     *           projects an additional {@code error.addressing_mode}
     *           enum (lid or pn) that Cobalt surfaces through the
     *           dedicated {@link #errorAddressingMode()} accessor.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseInvalidRequest")
    final class ClientError implements SmaxUpdateBlockListResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The optional human-readable error text.
         */
        private final String errorText;

        /**
         * The optional addressing-mode hint carried on the
         * {@code <error/>} child.
         */
        private final String errorAddressingMode;

        /**
         * Constructs a client-error reply.
         *
         * @param errorCode           the numeric error code
         * @param errorText           the optional error text; may
         *                            be {@code null}
         * @param errorAddressingMode the addressing-mode hint; may
         *                            be {@code null}
         */
        public ClientError(int errorCode, String errorText, String errorAddressingMode) {
            this.errorCode = errorCode;
            this.errorText = errorText;
            this.errorAddressingMode = errorAddressingMode;
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
         * Returns the optional addressing-mode hint.
         *
         * @return an {@link Optional} carrying the addressing-mode
         *         (typically {@code "lid"} or {@code "pn"}), or empty
         *         when the relay omitted it
         */
        public Optional<String> errorAddressingMode() {
            return Optional.ofNullable(errorAddressingMode);
        }

        /**
         * Tries to parse a {@link ClientError} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty on schema mismatch
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseInvalidRequest",
                exports = "parseUpdateBlockListResponseInvalidRequest",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ClientError> of(Node node, Node request) {
            var envelope = SmaxBaseServerErrorMixin.parseClientError(node, request).orElse(null);
            if (envelope == null) {
                return Optional.empty();
            }
            var addressingMode = node.getChild("error")
                    .flatMap(child -> child.getAttributeAsString("addressing_mode"))
                    .orElse(null);
            return Optional.of(new ClientError(envelope.code(), envelope.text(), addressingMode));
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
                    && Objects.equals(this.errorText, that.errorText)
                    && Objects.equals(this.errorAddressingMode, that.errorAddressingMode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errorCode, errorText, errorAddressingMode);
        }

        @Override
        public String toString() {
            return "SmaxUpdateBlockListResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText
                    + ", errorAddressingMode=" + errorAddressingMode + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — transient internal
     * failure.
     *
     * @implNote {@code WASmaxInBlocklistsUpdateBlockListResponseServerError.parseUpdateBlockListResponseServerError}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseServerError")
    final class ServerError implements SmaxUpdateBlockListResponse {
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
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsUpdateBlockListResponseServerError",
                exports = "parseUpdateBlockListResponseServerError",
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
            return "SmaxUpdateBlockListResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
