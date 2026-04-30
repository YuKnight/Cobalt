package com.github.auties00.cobalt.node.iq.syncd;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.iq.IqOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of inbound reply variants produced by the relay in
 * response to a {@link IqSyncdServerSyncRequest}.
 *
 * @implNote {@code WAWebSyncdServerSync.serverSync} encodes the
 *           failure modes as either fatal-vs-retryable based on the
 *           top-level error code, or as per-collection state on
 *           success. Cobalt collapses to the standard
 *           {@code Success} (carrying the raw node for downstream
 *           parsing) / {@code ClientError} / {@code ServerError}
 *           split.
 */
public sealed interface IqSyncdServerSyncResponse extends IqOperation.Response
        permits IqSyncdServerSyncResponse.Success, IqSyncdServerSyncResponse.ClientError, IqSyncdServerSyncResponse.ServerError {

    /**
     * Tries each {@link IqSyncdServerSyncResponse} variant in priority order and returns
     * the first that parses cleanly.
     *
     * @param node    the inbound IQ stanza received from the relay.
     *                Never {@code null}
     * @param request the original outbound stanza. Used to validate
     *                echoed identifiers. Never {@code null}
     * @return an {@link Optional} carrying the parsed variant, or
     *         {@link Optional#empty()} when no documented variant
     *         matched the stanza shape
     * @throws NullPointerException if either argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdServerSync",
            exports = "serverSync", adaptation = WhatsAppAdaptation.ADAPTED)
    static Optional<? extends IqSyncdServerSyncResponse> of(Node node, Node request) {
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
     * The {@code Success} reply variant. The relay returned the
     * per-collection sync state, projected into a typed list of
     * {@link IqSyncdServerSyncResponseCollection} entries.
     */
    @WhatsAppWebModule(moduleName = "WAWebSyncdServerSync")
    @WhatsAppWebModule(moduleName = "WAWebSyncdResponseParser")
    final class Success implements IqSyncdServerSyncResponse {
        /**
         * The list of typed collection projections. One per
         * {@code <collection/>} child of the inbound
         * {@code <sync/>} envelope.
         */
        private final List<IqSyncdServerSyncResponseCollection> collections;

        /**
         * Constructs a new successful reply.
         *
         * @param collections the typed collection projections. Never
         *                    {@code null}, possibly empty
         * @throws NullPointerException if {@code collections} is
         *                              {@code null}
         */
        public Success(List<IqSyncdServerSyncResponseCollection> collections) {
            Objects.requireNonNull(collections, "collections cannot be null");
            this.collections = collections;
        }

        /**
         * Returns the typed list of per-collection projections.
         *
         * @return an unmodifiable view of the collections. Never
         *         {@code null}, possibly empty
         */
        public java.util.SequencedCollection<IqSyncdServerSyncResponseCollection> collections() {
            return java.util.Collections.unmodifiableSequencedCollection(collections);
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
         *
         * @implNote {@code WAWebSyncdResponseParser.syncResponseParser}:
         *           {@code child("sync").mapChildrenWithTag("collection",
         *           ...)} projecting each child via the inline
         *           {@code stateOfCollection} helper. Cobalt mirrors
         *           the projection without decoding the encoded patch
         *           or snapshot payloads (those remain the syncd
         *           subsystem's responsibility).
         */
        @WhatsAppWebExport(moduleName = "WAWebSyncdResponseParser",
                exports = "syncResponseParser", adaptation = WhatsAppAdaptation.ADAPTED)
        public static Optional<Success> of(Node node, Node request) {
            if (!SmaxIqResultResponseMixin.validate(node, request)) {
                return Optional.empty();
            }
            var syncChild = node.getChild("sync").orElse(null);
            if (syncChild == null) {
                return Optional.empty();
            }
            var collections = new ArrayList<IqSyncdServerSyncResponseCollection>();
            for (var collectionChild : syncChild.getChildren("collection")) {
                parseCollection(collectionChild).ifPresent(collections::add);
            }
            return Optional.of(new Success(collections));
        }

        /**
         * Projects a single {@code <collection/>} child into a typed
         * {@link IqSyncdServerSyncResponseCollection}.
         *
         * @param child the {@code <collection/>} node. Never
         *              {@code null}
         * @return an {@link Optional} carrying the projection, or
         *         empty when the child lacks a {@code name} attribute
         *
         * @implNote {@code WAWebSyncdResponseParser.syncResponseParser}:
         *           the inline state classifier maps
         *           {@code type="error"} + {@code <error code/>} +
         *           {@code has_more_patches} onto the
         *           {@link IqSyncdServerSyncCollectionState} enum.
         *           Cobalt mirrors the same lookup verbatim.
         */
        private static Optional<IqSyncdServerSyncResponseCollection> parseCollection(Node child) {
            var name = child.getAttributeAsString("name").orElse(null);
            if (name == null) {
                return Optional.empty();
            }
            var hasMorePatches = child.hasAttribute("has_more_patches");
            // WAWebSyncdResponseParser.syncResponseParser: state-of-collection helper
            IqSyncdServerSyncCollectionState state;
            if (child.hasAttribute("type", "error")) {
                var errorChild = child.getChild("error").orElse(null);
                var errorCode = errorChild == null
                        ? null
                        : errorChild.getAttributeAsString("code").orElse(null);
                if ("409".equals(errorCode)) {
                    state = hasMorePatches
                            ? IqSyncdServerSyncCollectionState.CONFLICT_HAS_MORE
                            : IqSyncdServerSyncCollectionState.CONFLICT;
                } else if ("400".equals(errorCode)
                        || "404".equals(errorCode)
                        || "405".equals(errorCode)) {
                    state = IqSyncdServerSyncCollectionState.ERROR_FATAL;
                } else {
                    state = IqSyncdServerSyncCollectionState.ERROR_RETRY;
                }
            } else {
                state = hasMorePatches
                        ? IqSyncdServerSyncCollectionState.SUCCESS_HAS_MORE
                        : IqSyncdServerSyncCollectionState.SUCCESS;
            }
            // WAWebSyncdResponseParser.syncResponseParser: hasAttr("version") branch
            var version = child.getAttributeAsLong("version").stream().boxed().findFirst().orElse(null);
            // WAWebSyncdResponseParser.syncResponseParser: hasChild("patches") branch
            var patches = new ArrayList<byte[]>();
            child.getChild("patches").ifPresent(patchesNode ->
                    patchesNode.streamChildren("patch").forEach(patchNode ->
                            patchNode.toContentBytes().ifPresent(patches::add)));
            // WAWebSyncdResponseParser.syncResponseParser: hasChild("snapshot") branch
            var snapshot = child.getChild("snapshot")
                    .flatMap(Node::toContentBytes)
                    .orElse(null);
            return Optional.of(new IqSyncdServerSyncResponseCollection(
                    name, state, version, patches, snapshot));
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
            return Objects.equals(this.collections, that.collections);
        }

        @Override
        public int hashCode() {
            return Objects.hash(collections);
        }

        @Override
        public String toString() {
            return "IqSyncdServerSyncResponse.Success[collections=" + collections + ']';
        }
    }

    /**
     * The {@code ClientError} reply variant. The relay rejected the
     * sync as fatal. Codes {@code 400, 404, 405, 406} map to
     * {@code SyncdFatalError} in WA Web (no retry).
     */
    @WhatsAppWebModule(moduleName = "WAWebSyncdServerSync")
    final class ClientError implements IqSyncdServerSyncResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new client-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text. May be
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
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ClientError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         client-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebSyncdServerSync",
                exports = "serverSync", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqSyncdServerSyncResponse.ClientError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }

    /**
     * The {@code ServerError} reply variant. The relay encountered a
     * transient internal failure (codes {@code >= 500}) while
     * processing the sync. WA Web maps this to
     * {@code SyncdRetryableError} which the sync loop retries with
     * exponential backoff.
     */
    @WhatsAppWebModule(moduleName = "WAWebSyncdServerSync")
    final class ServerError implements IqSyncdServerSyncResponse {
        /**
         * The numeric server-side error code.
         */
        private final int errorCode;

        /**
         * The human-readable error text, when the relay supplied one.
         */
        private final String errorText;

        /**
         * Constructs a new server-error reply.
         *
         * @param errorCode the numeric error code
         * @param errorText the optional human-readable text. May be
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
         * @return an {@link Optional} carrying the error text, or empty
         *         when the relay omitted it
         */
        public Optional<String> errorText() {
            return Optional.ofNullable(errorText);
        }

        /**
         * Tries to parse a {@link ServerError} variant from the given
         * inbound stanza.
         *
         * @param node    the inbound IQ stanza
         * @param request the original outbound request
         * @return an {@link Optional} carrying the parsed variant, or
         *         empty when the stanza does not match the
         *         server-error schema
         */
        @WhatsAppWebExport(moduleName = "WAWebSyncdServerSync",
                exports = "serverSync", adaptation = WhatsAppAdaptation.ADAPTED)
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
            return "IqSyncdServerSyncResponse.ServerError[errorCode=" + errorCode
                    + ", errorText=" + errorText + ']';
        }
    }
}
