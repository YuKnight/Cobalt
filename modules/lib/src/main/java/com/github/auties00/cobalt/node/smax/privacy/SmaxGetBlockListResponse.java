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
 * Sealed family of inbound reply variants produced by the relay.
 *
 * @implNote {@code WASmaxBlocklistsGetBlockListRPC.sendGetBlockListRPC}
 *           tries the variants in fall-through order; Cobalt mirrors
 *           that with {@link Optional#empty()} on no-match.
 */
public sealed interface SmaxGetBlockListResponse extends SmaxOperation.Response
        permits SmaxGetBlockListResponse.SuccessWithMatch,
        SmaxGetBlockListResponse.SuccessWithMismatch,
        SmaxGetBlockListResponse.MigratedSuccessWithMismatch,
        SmaxGetBlockListResponse.ForceMigratedSuccessWithMismatch,
        SmaxGetBlockListResponse.CAPISuccessWithMismatch,
        SmaxGetBlockListResponse.ClientError,
        SmaxGetBlockListResponse.ServerError {

    /**
     * Tries each {@link SmaxGetBlockListResponse} variant in priority order and
     * returns the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza; never {@code null}
     * @param request the original outbound stanza; never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         empty on no-match
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WASmaxBlocklistsGetBlockListRPC",
            exports = "sendGetBlockListRPC", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends SmaxGetBlockListResponse> of(Node node, Node request) {
        Objects.requireNonNull(node, "node cannot be null");
        Objects.requireNonNull(request, "request cannot be null");
        var successWithMismatch = SuccessWithMismatch.of(node, request);
        if (successWithMismatch.isPresent()) {
            return successWithMismatch;
        }
        var migratedSuccessWithMismatch = MigratedSuccessWithMismatch.of(node, request);
        if (migratedSuccessWithMismatch.isPresent()) {
            return migratedSuccessWithMismatch;
        }
        var forceMigratedSuccessWithMismatch = ForceMigratedSuccessWithMismatch.of(node, request);
        if (forceMigratedSuccessWithMismatch.isPresent()) {
            return forceMigratedSuccessWithMismatch;
        }
        var capiSuccessWithMismatch = CAPISuccessWithMismatch.of(node, request);
        if (capiSuccessWithMismatch.isPresent()) {
            return capiSuccessWithMismatch;
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
     * Descriptor for one entry in the relay-returned list of blocked
     * users.
     *
     * <p>The {@code jid} attribute is the only required field; the
     * other fields are populated only on the migrated/CAPI variants.
     *
     * @implNote {@code WASmaxInBlocklistsGetBlockListResponseSuccessWithMismatchListItem}
     *           and the migrated/CAPI/force-migrated variants project
     *           an item with at most these four fields. Cobalt
     *           consolidates the four shapes into this single record.
     */
    record Item(Jid jid, boolean active, String displayName, String blocklistIdentifier) {
        /**
         * Returns the {@code displayName} as an {@link Optional}.
         *
         * @return an {@link Optional} carrying the display name, or
         *         empty when omitted
         */
        public Optional<String> displayNameAsOptional() {
            return Optional.ofNullable(displayName);
        }

        /**
         * Returns the {@code blocklistIdentifier} as an {@link Optional}.
         *
         * @return an {@link Optional} carrying the identifier, or
         *         empty when omitted
         */
        public Optional<String> blocklistIdentifierAsOptional() {
            return Optional.ofNullable(blocklistIdentifier);
        }
    }

    /**
     * Validates the {@code <iq from id type="result">} envelope and
     * extracts the inner {@code <list/>} child common to every
     * {@code SuccessWithMismatch}-shape variant.
     *
     * @param node    the inbound stanza
     * @param request the original outbound request
     * @return an {@link Optional} carrying the {@code <list/>} child,
     *         or empty when the envelope or list is missing
     */
    private static Optional<Node> validateMismatchEnvelope(Node node, Node request) {
        if (!SmaxIqResultResponseMixin.validate(node, request)) {
            return Optional.empty();
        }
        return node.getChild("list");
    }

    /**
     * Parses the {@code <item/>} children of a {@code <list/>} node
     * into a list of {@link Item} records.
     *
     * @param list                  the {@code <list/>} node
     * @param requireJid            whether the {@code jid} attribute
     *                              is required on each {@code <item/>}
     *                              child (required for the standard
     *                              and migrated variants, optional
     *                              for the force-migrated and CAPI
     *                              variants)
     * @return the parsed list of items; never {@code null}
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
     * The {@code SuccessWithMatch} reply variant — the relay returned
     * a bare {@code <iq type="result">} envelope (no {@code <list/>}
     * child) signalling that the client's cached blocklist is up to
     * date.
     *
     * @implNote {@code WASmaxInBlocklistsGetBlockListResponseSuccessWithMatch.parseGetBlockListResponseSuccessWithMatch}
     *           — projects only {@code {type: "result"}}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetBlockListResponseSuccessWithMatch")
    final class SuccessWithMatch implements SmaxGetBlockListResponse {
        /**
         * Constructs a new successful match reply.
         */
        public SuccessWithMatch() {
        }

        /**
         * Tries to parse a {@link SuccessWithMatch} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the envelope shape does not match (e.g.
         *         because the reply has a {@code <list/>} child
         *         indicating a mismatch)
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetBlockListResponseSuccessWithMatch",
                exports = "parseGetBlockListResponseSuccessWithMatch",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessWithMatch> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            if (node.getChild("list").isPresent()) {
                return Optional.empty();
            }
            return Optional.of(new SuccessWithMatch());
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
            return SuccessWithMatch.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxGetBlockListResponse.SuccessWithMatch[]";
        }
    }

    /**
     * The {@code SuccessWithMismatch} reply variant — the relay
     * returned the full PN-addressed blocklist because the client's
     * cached digest does not match the server's current digest.
     *
     * @implNote {@code WASmaxInBlocklistsGetBlockListResponseSuccessWithMismatch.parseGetBlockListResponseSuccessWithMismatch}
     *           projects {@code (listDhash, listAddressingMode, listItem)}.
     *           {@code listAddressingMode} is the optional literal
     *           {@code "pn"} marker.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetBlockListResponseSuccessWithMismatch")
    final class SuccessWithMismatch implements SmaxGetBlockListResponse {
        /**
         * The optional new server-side digest.
         */
        private final String listDhash;

        /**
         * Whether the relay marked the list as PN-addressed (the
         * literal {@code "pn"} value of the {@code addressing_mode}
         * attribute).
         */
        private final boolean phoneNumberAddressed;

        /**
         * The parsed list of blocklist entries.
         */
        private final List<Item> listItem;

        /**
         * Constructs a new mismatch reply.
         *
         * @param listDhash            the new server-side digest;
         *                             may be {@code null}
         * @param phoneNumberAddressed whether the list is
         *                             PN-addressed
         * @param listItem             the parsed item list; never
         *                             {@code null}
         */
        public SuccessWithMismatch(String listDhash, boolean phoneNumberAddressed, List<Item> listItem) {
            this.listDhash = listDhash;
            this.phoneNumberAddressed = phoneNumberAddressed;
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
         * Returns whether the list is PN-addressed.
         *
         * @return {@code true} when the relay marked the list as
         *         PN-addressed
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
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetBlockListResponseSuccessWithMismatch",
                exports = "parseGetBlockListResponseSuccessWithMismatch",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<SuccessWithMismatch> of(Node node, Node request) {
            var list = validateMismatchEnvelope(node, request).orElse(null);
            if (list == null) {
                return Optional.empty();
            }
            var addressingMode = list.getAttributeAsString("addressing_mode").orElse(null);
            if (addressingMode != null && !addressingMode.equals("pn")) {
                return Optional.empty();
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            var items = parseItems(list, true);
            return Optional.of(new SuccessWithMismatch(dhash, addressingMode != null, items));
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
            return this.phoneNumberAddressed == that.phoneNumberAddressed
                    && Objects.equals(this.listDhash, that.listDhash)
                    && Objects.equals(this.listItem, that.listItem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listDhash, phoneNumberAddressed, listItem);
        }

        @Override
        public String toString() {
            return "SmaxGetBlockListResponse.SuccessWithMismatch[listDhash=" + listDhash
                    + ", phoneNumberAddressed=" + phoneNumberAddressed
                    + ", listItem=" + listItem + ']';
        }
    }

    /**
     * The {@code MigratedSuccessWithMismatch} reply variant — the
     * relay returned the full LID-addressed blocklist after a
     * regular PN-to-LID migration.
     *
     * @implNote {@code WASmaxInBlocklistsGetBlockListResponseMigratedSuccessWithMismatch.parseGetBlockListResponseMigratedSuccessWithMismatch}
     *           — same shape as {@link SuccessWithMismatch} but with
     *           {@code addressing_mode="lid"} required (not optional)
     *           and the inner {@code <item jid/>} children using LID
     *           JIDs rather than PN JIDs.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetBlockListResponseMigratedSuccessWithMismatch")
    final class MigratedSuccessWithMismatch implements SmaxGetBlockListResponse {
        /**
         * The optional new server-side digest.
         */
        private final String listDhash;

        /**
         * The parsed list of blocklist entries (LID-addressed).
         */
        private final List<Item> listItem;

        /**
         * Constructs a new migrated mismatch reply.
         *
         * @param listDhash the new server-side digest; may be
         *                  {@code null}
         * @param listItem  the parsed item list; never {@code null}
         */
        public MigratedSuccessWithMismatch(String listDhash, List<Item> listItem) {
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
         * Tries to parse a {@link MigratedSuccessWithMismatch}
         * variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the envelope shape does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetBlockListResponseMigratedSuccessWithMismatch",
                exports = "parseGetBlockListResponseMigratedSuccessWithMismatch",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<MigratedSuccessWithMismatch> of(Node node, Node request) {
            var list = validateMismatchEnvelope(node, request).orElse(null);
            if (list == null) {
                return Optional.empty();
            }
            if (!list.hasAttribute("addressing_mode", "lid")) {
                return Optional.empty();
            }
            if (list.hasAttribute("dirty", "true")) {
                return Optional.empty();
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            var items = parseItems(list, true);
            return Optional.of(new MigratedSuccessWithMismatch(dhash, items));
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
            return Objects.equals(this.listDhash, that.listDhash)
                    && Objects.equals(this.listItem, that.listItem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listDhash, listItem);
        }

        @Override
        public String toString() {
            return "SmaxGetBlockListResponse.MigratedSuccessWithMismatch[listDhash=" + listDhash
                    + ", listItem=" + listItem + ']';
        }
    }

    /**
     * The {@code ForceMigratedSuccessWithMismatch} reply variant —
     * issued when the relay force-migrates an out-of-date PN
     * blocklist to LID. The {@code <list/>} carries an additional
     * {@code dirty="true"} marker.
     *
     * @implNote {@code WASmaxInBlocklistsGetBlockListResponseForceMigratedSuccessWithMismatch.parseGetBlockListResponseForceMigratedSuccessWithMismatch}.
     *           In contrast to the regular {@link MigratedSuccessWithMismatch},
     *           the inner {@code <item jid/>} attribute is OPTIONAL
     *           because the force-migration may not be able to map
     *           every PN entry to a LID.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetBlockListResponseForceMigratedSuccessWithMismatch")
    final class ForceMigratedSuccessWithMismatch implements SmaxGetBlockListResponse {
        /**
         * The optional new server-side digest.
         */
        private final String listDhash;

        /**
         * The parsed list of blocklist entries (LID-addressed,
         * sparsely populated).
         */
        private final List<Item> listItem;

        /**
         * Constructs a new force-migrated mismatch reply.
         *
         * @param listDhash the new server-side digest; may be
         *                  {@code null}
         * @param listItem  the parsed item list; never {@code null}
         */
        public ForceMigratedSuccessWithMismatch(String listDhash, List<Item> listItem) {
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
         * Tries to parse a {@link ForceMigratedSuccessWithMismatch}
         * variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the envelope shape does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetBlockListResponseForceMigratedSuccessWithMismatch",
                exports = "parseGetBlockListResponseForceMigratedSuccessWithMismatch",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<ForceMigratedSuccessWithMismatch> of(Node node, Node request) {
            var list = validateMismatchEnvelope(node, request).orElse(null);
            if (list == null) {
                return Optional.empty();
            }
            if (!list.hasAttribute("addressing_mode", "lid")) {
                return Optional.empty();
            }
            if (!list.hasAttribute("dirty", "true")) {
                return Optional.empty();
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            var items = parseItems(list, false);
            return Optional.of(new ForceMigratedSuccessWithMismatch(dhash, items));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (ForceMigratedSuccessWithMismatch) obj;
            return Objects.equals(this.listDhash, that.listDhash)
                    && Objects.equals(this.listItem, that.listItem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listDhash, listItem);
        }

        @Override
        public String toString() {
            return "SmaxGetBlockListResponse.ForceMigratedSuccessWithMismatch[listDhash=" + listDhash
                    + ", listItem=" + listItem + ']';
        }
    }

    /**
     * The {@code CAPISuccessWithMismatch} reply variant — issued by
     * the Cloud-API server flavour rather than the regular relay.
     * Same wire shape as {@link MigratedSuccessWithMismatch} but with
     * a different envelope source — distinguished only by relay-side
     * metadata that Cobalt does not currently track.
     *
     * @implNote {@code WASmaxInBlocklistsGetBlockListResponseCAPISuccessWithMismatch.parseGetBlockListResponseCAPISuccessWithMismatch}.
     *           Wire-shape-equivalent to the regular migrated variant,
     *           differing only in that the inner {@code <item jid/>}
     *           attribute is optional. The fall-through ordering in
     *           {@link SmaxGetBlockListResponse#of(Node, Node)} attempts the regular
     *           migrated variant first and the CAPI variant
     *           afterwards, matching WA Web's ordering exactly.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetBlockListResponseCAPISuccessWithMismatch")
    final class CAPISuccessWithMismatch implements SmaxGetBlockListResponse {
        /**
         * The optional new server-side digest.
         */
        private final String listDhash;

        /**
         * The parsed list of blocklist entries.
         */
        private final List<Item> listItem;

        /**
         * Constructs a new CAPI mismatch reply.
         *
         * @param listDhash the new server-side digest; may be
         *                  {@code null}
         * @param listItem  the parsed item list; never {@code null}
         */
        public CAPISuccessWithMismatch(String listDhash, List<Item> listItem) {
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
         * Tries to parse a {@link CAPISuccessWithMismatch} variant.
         *
         * @param node    the inbound stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the envelope shape does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetBlockListResponseCAPISuccessWithMismatch",
                exports = "parseGetBlockListResponseCAPISuccessWithMismatch",
                adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<CAPISuccessWithMismatch> of(Node node, Node request) {
            var list = validateMismatchEnvelope(node, request).orElse(null);
            if (list == null) {
                return Optional.empty();
            }
            if (!list.hasAttribute("addressing_mode", "lid")) {
                return Optional.empty();
            }
            if (list.hasAttribute("dirty", "true")) {
                return Optional.empty();
            }
            var dhash = list.getAttributeAsString("dhash").orElse(null);
            var items = parseItems(list, false);
            return Optional.of(new CAPISuccessWithMismatch(dhash, items));
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
            return Objects.equals(this.listDhash, that.listDhash)
                    && Objects.equals(this.listItem, that.listItem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listDhash, listItem);
        }

        @Override
        public String toString() {
            return "SmaxGetBlockListResponse.CAPISuccessWithMismatch[listDhash=" + listDhash
                    + ", listItem=" + listItem + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant — the relay rejected the
     * request as malformed.
     *
     * @implNote {@code WASmaxInBlocklistsGetBlockListResponseInvalidRequest.parseGetBlockListResponseInvalidRequest}
     *           routes through {@code parseGetBlocklistErrors} for a
     *           per-RPC enum lookup. Cobalt collapses to the universal
     *           {@code (errorCode, errorText)} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetBlockListResponseInvalidRequest")
    final class ClientError implements SmaxGetBlockListResponse {
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
         *         empty when the envelope shape does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetBlockListResponseInvalidRequest",
                exports = "parseGetBlockListResponseInvalidRequest",
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
            return "SmaxGetBlockListResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant — the relay encountered
     * a transient internal failure.
     *
     * @implNote {@code WASmaxInBlocklistsGetBlockListResponseInternalServerError.parseGetBlockListResponseInternalServerError}
     *           — collapsed to the universal {@code (code, text)} pair.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInBlocklistsGetBlockListResponseInternalServerError")
    final class ServerError implements SmaxGetBlockListResponse {
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
         *         empty when the envelope shape does not match
         */
        @WhatsAppWebExport(moduleName = "WASmaxInBlocklistsGetBlockListResponseInternalServerError",
                exports = "parseGetBlockListResponseInternalServerError",
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
            return "SmaxGetBlockListResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
