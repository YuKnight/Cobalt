package com.github.auties00.cobalt.sync.exchange;

import com.github.auties00.cobalt.exception.WhatsAppWebAppStateSyncException;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.media.ExternalBlobReference;
import com.github.auties00.cobalt.model.media.ExternalBlobReferenceSpec;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.data.SyncdPatch;
import com.github.auties00.cobalt.model.sync.data.SyncdPatchSpec;
import com.github.auties00.cobalt.node.Node;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.SequencedCollection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses sync response nodes into {@link MutationSyncResponse} objects.
 *
 * <p>Handles both snapshot and patch responses, including error detection
 * for server-side error codes (409, 400, 404).
 */
@WhatsAppWebModule(moduleName = "WAWebSyncdResponseParser")
@WhatsAppWebModule(moduleName = "WAWebSyncdDecode")
@WhatsAppWebModule(moduleName = "WAWebSyncdValidateServerSyncProtobuf")
public final class MutationResponseParser {
    /**
     * Logger used for diagnostic output, including decoded {@code clientDebugData}
     * from incoming {@link SyncdPatch} messages.
     */
    private static final Logger LOGGER = Logger.getLogger(MutationResponseParser.class.getName());

    /**
     * Parses a single-collection sync response node into a {@link MutationSyncResponse}.
     *
     * <p>Used for push sync responses where only one collection is expected.
     * @param responseNode the raw response node from the server
     * @return the parsed sync response
     * @throws WhatsAppWebAppStateSyncException.Conflict if the server returns a 409 error
     * @throws WhatsAppWebAppStateSyncException.UnexpectedError if the server returns a fatal error
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdResponseParser", exports = "syncResponseParser", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationSyncResponse parseSyncResponse(Node responseNode) {
        // ADAPTED: WAParseIqResponse.parseIqResponse + WAWebSyncdServerSync.k
        var iqType = responseNode.getAttributeAsString("type");
        if (iqType.isPresent() && iqType.get().equals("error")) {
            var iqErrorNode = responseNode.getChild("error").orElse(null);
            var errorCode = iqErrorNode != null
                    ? iqErrorNode.getAttributeAsString("code").map(Integer::parseInt).orElse(0)
                    : 0;
            var errorText = iqErrorNode != null
                    ? iqErrorNode.getAttributeAsString("text").orElse("unknown")
                    : "unknown";
            handleIqLevelError(errorCode, errorText, iqErrorNode);
        }

        // WAWebSyncdResponseParser.syncResponseParser — t.child("sync")
        var syncNode = responseNode.getChild("sync")
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                        "Response missing 'sync' node",
                        null
                ));

        // WAWebSyncdResponseParser.syncResponseParser — single collection extraction
        var collectionNode = syncNode.getChild("collection")
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                        "Response missing 'collection' node",
                        null
                ));

        // WAWebSyncdResponseParser.h — check type="error"
        var type = collectionNode.getAttributeAsString("type");
        if (type.isPresent() && type.get().equals("error")) {
            handleErrorResponse(collectionNode);
        }

        // WAWebSyncdResponseParser.syncResponseParser — CollectionName.cast(e.attrString("name"))
        var collectionName = collectionNode.getAttributeAsString("name")
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                        "Collection missing 'name' attribute",
                        null
                ));
        var patchType = SyncPatchType.of(collectionName) // WAWebSyncdResponseParser — CollectionName.cast
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                        "Invalid collection name: " + collectionName,
                        null
                ));

        // WAWebSyncdResponseParser.syncResponseParser — parseInt(e.attrString("version"), 10)
        var version = collectionNode.getAttributeAsLong("version")
                .orElse(0L);

        // WAWebSyncdResponseParser.h — e.hasAttr("has_more_patches")
        var hasMore = collectionNode.hasAttribute("has_more_patches");

        // WAWebSyncdResponseParser.syncResponseParser — e.hasChild("snapshot"), e.hasChild("patches")
        var snapshotNode = collectionNode.getChild("snapshot");
        var patchesNode = collectionNode.getChild("patches");

        // WAWebSyncdResponseParser.syncResponseParser — decodeExternalBlobReference, decodeSyncdPatch
        var snapshotRef = snapshotNode.map(this::parseSnapshotReference).orElse(null);
        var patches = patchesNode.map(this::parsePatches).orElse(List.of());
        return new MutationSyncResponse(patchType, version, hasMore, patches, snapshotRef);
    }

    /**
     * Parses a batched sync response node into multiple {@link MutationSyncResponse} objects.
     *
     * <p>Per WhatsApp Web behavior, a single IQ response can contain multiple
     * {@code <collection>} children under the {@code <sync>} node.
     * @param responseNode the raw response node from the server
     * @return the list of parsed sync responses, one per collection
     * @throws WhatsAppWebAppStateSyncException.Conflict if any collection returns a 409 error
     * @throws WhatsAppWebAppStateSyncException.UnexpectedError if the server returns a fatal error
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdResponseParser", exports = "syncResponseParser", adaptation = WhatsAppAdaptation.DIRECT)
    public List<MutationSyncResponse> parseBatchedSyncResponse(Node responseNode) {
        // ADAPTED: WAParseIqResponse.parseIqResponse + WAWebSyncdServerSync.k
        var iqType = responseNode.getAttributeAsString("type");
        if (iqType.isPresent() && iqType.get().equals("error")) {
            var iqErrorNode = responseNode.getChild("error").orElse(null);
            var errorCode = iqErrorNode != null
                    ? iqErrorNode.getAttributeAsString("code").map(Integer::parseInt).orElse(0)
                    : 0;
            var errorText = iqErrorNode != null
                    ? iqErrorNode.getAttributeAsString("text").orElse("unknown")
                    : "unknown";
            handleIqLevelError(errorCode, errorText, iqErrorNode);
        }

        // WAWebSyncdResponseParser.syncResponseParser — t.child("sync")
        var syncNode = responseNode.getChild("sync")
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                        "Response missing 'sync' node",
                        null
                ));

        // WAWebSyncdResponseParser.syncResponseParser — r.mapChildrenWithTag("collection", ...)
        var collectionNodes = syncNode.getChildren("collection");
        var results = new ArrayList<MutationSyncResponse>(collectionNodes.size());
        for (var collectionNode : collectionNodes) {
            results.add(parseCollectionNode(collectionNode));
        }
        return results; // WAWebSyncdResponseParser.syncResponseParser — return n
    }

    /**
     * Parses a single collection node into a {@link MutationSyncResponse}.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdResponseParser.h}: when a collection
     * has {@code type="error"}, the error state is captured on the response object
     * rather than thrown. This allows batched responses to process other collections
     * independently even when some fail.
     * @param collectionNode the collection node to parse
     * @return the parsed sync response, with error captured in {@link MutationSyncResponse#collectionError()} if applicable
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdResponseParser", exports = "syncResponseParser", adaptation = WhatsAppAdaptation.DIRECT)
    private MutationSyncResponse parseCollectionNode(Node collectionNode) {
        // WAWebSyncdResponseParser.syncResponseParser — CollectionName.cast(e.attrString("name"))
        var collectionName = collectionNode.getAttributeAsString("name")
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                        "Collection missing 'name' attribute",
                        null
                ));
        var patchType = SyncPatchType.of(collectionName)
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                        "Invalid collection name: " + collectionName,
                        null
                ));

        // WAWebSyncdResponseParser.h — e.hasAttr("type") && e.attrString("type") === "error"
        // Per WA Web: collection-level errors return a state, not throw — capture on response
        var type = collectionNode.getAttributeAsString("type");
        if (type.isPresent() && type.get().equals("error")) {
            var collectionError = buildCollectionError(collectionNode); // WAWebSyncdResponseParser.h
            var hasMore = collectionNode.hasAttribute("has_more_patches"); // WAWebSyncdResponseParser.h
            return new MutationSyncResponse(patchType, 0L, hasMore, List.of(), null, collectionError);
        }

        // WAWebSyncdResponseParser.syncResponseParser — parseInt(e.attrString("version"), 10)
        var version = collectionNode.getAttributeAsLong("version")
                .orElse(0L);
        // WAWebSyncdResponseParser.h — e.hasAttr("has_more_patches")
        var hasMore = collectionNode.hasAttribute("has_more_patches");

        // WAWebSyncdResponseParser.syncResponseParser — e.hasChild("snapshot"), e.hasChild("patches")
        var snapshotNode = collectionNode.getChild("snapshot");
        var patchesNode = collectionNode.getChild("patches");

        // WAWebSyncdResponseParser.syncResponseParser — decodeExternalBlobReference, decodeSyncdPatch
        var snapshotRef = snapshotNode.map(this::parseSnapshotReference).orElse(null);
        var patches = patchesNode.map(this::parsePatches).orElse(List.of());
        return new MutationSyncResponse(patchType, version, hasMore, patches, snapshotRef);
    }

    /**
     * Builds a collection-level error exception from a collection error node.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdResponseParser.h}: collection-level
     * errors are returned as states ({@code CollectionState.Conflict},
     * {@code CollectionState.ErrorFatal}, {@code CollectionState.ErrorRetry})
     * rather than thrown. This method creates the corresponding exception to
     * be stored on the {@link MutationSyncResponse} for later processing.
     * @param collectionNode the collection node containing the error
     * @return the exception representing the collection-level error
     */
    private WhatsAppWebAppStateSyncException buildCollectionError(Node collectionNode) {
        // WAWebSyncdResponseParser.h — n.attrString("code")
        var errorNode = collectionNode.getChild("error");
        var errorCode = errorNode
                .flatMap(e -> e.getAttributeAsString("code"))
                .orElse("unknown");

        return switch (errorCode) {
            // WAWebSyncdResponseParser.h — r === "409" => Conflict/ConflictHasMore
            case "409" -> new WhatsAppWebAppStateSyncException.Conflict(
                    collectionNode.hasAttribute("has_more_patches")
            );
            // WAWebSyncdResponseParser.h — r === "400" || r === "404" => ErrorFatal
            case "400", "404" -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                    "Server returned fatal error code: " + errorCode, null
            );
            // WAWebSyncdResponseParser.h — default => ErrorRetry
            default -> new WhatsAppWebAppStateSyncException.RetryableServerError(errorCode);
        };
    }

    /**
     * Handles collection-level error responses from the server by throwing.
     *
     * <p>Used in single-collection responses where there is no need to continue
     * processing other collections. For batched responses, use
     * {@link #buildCollectionError(Node)} instead.
     * @param collectionNode the collection node containing the error
     * @throws WhatsAppWebAppStateSyncException.Conflict for 409 errors (Conflict/ConflictHasMore)
     * @throws WhatsAppWebAppStateSyncException.UnexpectedError for 400/404 errors (ErrorFatal)
     * @throws WhatsAppWebAppStateSyncException.RetryableServerError for other errors (ErrorRetry)
     */
    private void handleErrorResponse(Node collectionNode) {
        throw buildCollectionError(collectionNode);
    }

    /**
     * Handles IQ-level errors that affect all collections in the request.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdServerSync.k}: IQ-level errors are checked
     * before parsing individual collection responses. Error codes 400, 404, 405,
     * and 406 are fatal; all others are retryable.
     * @param errorCode the IQ error code
     * @param errorText the IQ error text
     * @param errorNode the error node, or {@code null}
     * @throws WhatsAppWebAppStateSyncException.UnexpectedError for fatal error codes
     * @throws WhatsAppWebAppStateSyncException.RetryableServerError for retryable error codes
     */
    private void handleIqLevelError(int errorCode, String errorText, Node errorNode) {
        switch (errorCode) {
            // WAWebSyncdServerSync.k — e === 400, 404, 405, 406 => SyncdFatalError
            case 400, 404, 405, 406 -> throw new WhatsAppWebAppStateSyncException.UnexpectedError(
                    "IQ-level fatal error " + errorCode + ": " + errorText, null);
            // WAWebSyncdServerSync.k — default => SyncdRetryableError(t, n)
            default -> {
                var serverBackoffMs = errorNode != null
                        ? errorNode.getAttributeAsLong("backoff", null)
                        : null;
                throw new WhatsAppWebAppStateSyncException.RetryableServerError(
                        String.valueOf(errorCode), serverBackoffMs);
            }
        }
    }

    /**
     * Parses snapshot node content as an {@link ExternalBlobReference}.
     *
     * <p>Per WhatsApp Web behavior, the snapshot content bytes encode an
     * {@code ExternalBlobReference} that must be downloaded from MMS to
     * obtain the actual {@code SyncdSnapshot} data.
     * @param snapshotNode the snapshot node
     * @return the parsed external blob reference
     * @throws WhatsAppWebAppStateSyncException.UnexpectedError if the node has no content
     *         or the protobuf fails to deserialize (WA Web {@code SyncdFatalError}
     *         {@code "syncd: external blob reference protobuf deserialization failed: ..."})
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdDecode", exports = "decodeExternalBlobReference", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebSyncdValidateServerSyncProtobuf", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private ExternalBlobReference parseSnapshotReference(Node snapshotNode) {
        var snapshotBytes = snapshotNode.toContentBytes()
                .orElseThrow(() -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                        "Snapshot node has no content",
                        null
                ));

        try {
            // WAWebSyncdDecode.decodeExternalBlobReference — decodeProtobuf(ExternalBlobReferenceSpec, n)
            return ExternalBlobReferenceSpec.decode(snapshotBytes);
        } catch (Exception e) {
            // WAWebSyncdDecode.decodeExternalBlobReference — catch → throw new SyncdFatalError(
            //   "syncd: external blob reference protobuf deserialization failed: " + err.message)
            // ADAPTED: WA Web also calls reportSyncdFatalError(EXTERNAL_BLOB_REFERENCE_PROTOBUF_DESERIALIZATION_FAILED, {collection})
            // and WALogger.ERROR — WAM/telemetry skipped per Cobalt's error model.
            throw new WhatsAppWebAppStateSyncException.UnexpectedError(
                    "syncd: external blob reference protobuf deserialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parses patch nodes into a collection of {@link SyncdPatch} objects.
     * @param patchesNode the patches parent node
     * @return the parsed patches
     * @throws WhatsAppWebAppStateSyncException.UnexpectedError if any patch node has no
     *         content or the protobuf fails to deserialize (WA Web {@code SyncdFatalError}
     *         {@code "syncd: patch protobuf deserialization failed: ..."})
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdDecode", exports = "decodeSyncdPatch", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebSyncdValidateServerSyncProtobuf", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private SequencedCollection<SyncdPatch> parsePatches(Node patchesNode) {
        var patches = new ArrayList<SyncdPatch>();

        // WAWebSyncdResponseParser.syncResponseParser — e.child("patches").mapChildrenWithTag("patch", ...)
        var patchNodes = patchesNode.getChildren("patch");

        for (var patchNode : patchNodes) {
            var patchBytes = patchNode.toContentBytes()
                    .orElseThrow(() -> new WhatsAppWebAppStateSyncException.UnexpectedError(
                            "Patch node has no content",
                            null
                    ));

            try {
                // WAWebSyncdDecode.decodeSyncdPatch — decodeProtobuf(SyncdPatchSpec, t)
                var patch = SyncdPatchSpec.decode(patchBytes);
                logClientDebugData(patch);
                patches.add(patch);
            } catch (Exception e) {
                // WAWebSyncdDecode.decodeSyncdPatch — catch → throw new SyncdFatalError(
                //   "syncd: patch protobuf deserialization failed: " + err.message)
                // ADAPTED: WA Web also calls reportSyncdFatalError(PATCH_PROTOBUF_DESERIALIZATION_FAILED, {collection})
                // and WALogger.ERROR — WAM/telemetry skipped per Cobalt's error model.
                throw new WhatsAppWebAppStateSyncException.UnexpectedError(
                        "syncd: patch protobuf deserialization failed: " + e.getMessage(), e);
            }
        }

        return patches;
    }

    /**
     * Decodes and logs the {@code clientDebugData} field of a {@link SyncdPatch} for
     * diagnostic purposes when the {@link Logger} is enabled at {@link Level#FINE FINE}.
     *
     * <p>Mirrors WhatsApp Web's {@code _applyPatch} which logs the decoded
     * {@code currentLthash} and {@code newLthash} from the patch debug data so that
     * server-side LT hash transitions can be cross-checked against client computations
     * during diagnosis. Decoding is best-effort and never throws.
     * @param patch the patch whose debug data should be logged, never {@code null}
     */
    private void logClientDebugData(SyncdPatch patch) {
        if (!LOGGER.isLoggable(Level.FINE)) {
            return;
        }
        patch.decodedClientDebugData().ifPresent(debug -> {
            var hex = HexFormat.of();
            var current = debug.currentLthash().map(hex::formatHex).orElse("<none>");
            var next = debug.newLthash().map(hex::formatHex).orElse("<none>");
            LOGGER.fine(() -> "patch debug: currentLthash=" + current + " newLthash=" + next);
        });
    }
}
