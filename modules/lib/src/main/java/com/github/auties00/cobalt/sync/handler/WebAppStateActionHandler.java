package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.sync.ConflictResolution;
import com.github.auties00.cobalt.model.sync.ConflictResolutionState;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Interface for handling specific action types in app state sync.
 *
 * <p>Each implementation handles a specific type of mutation (e.g., archive, pin, mute)
 * and is responsible for:
 * <ul>
 *   <li>Applying mutations to local state</li>
 *   <li>Resolving conflicts between local and remote mutations</li>
 *   <li>Handling orphan cases when referenced entities don't exist</li>
 * </ul>
 *
 * <p>Per WhatsApp Web, each handler declares:
 * <ul>
 *   <li>{@link #actionName()} — the action identifier used for routing</li>
 *   <li>{@link #collectionName()} — the sync collection this action belongs to</li>
 *   <li>{@link #version()} — the mutation format version for version gating</li>
 * </ul>
 *
 * <p>This interface flattens WA Web's five-class category hierarchy
 * ({@code AccountSyncdActionBase}, {@code ChatSyncdActionBase},
 * {@code ChatOrContactSyncdActionBase}, {@code MessageSyncdActionBase},
 * {@code ChatMessageRangeSyncdActionBase}) plus their shared prototype root
 * into a single interface, mirroring the Cobalt store flattening design.
 * The {@code is*SyncdAction()} / {@code as*SyncdActionHandler()} discrimination
 * pattern from WA Web is intentionally dropped because Cobalt handlers are
 * dispatched directly by {@link #actionName()} without category casts.
 */
@WhatsAppWebModule(moduleName = "WAWebSyncdAction")
public interface WebAppStateActionHandler {
    /**
     * Gets the action type name this handler processes.
     * @return the action type name
     */
    String actionName();

    /**
     * Returns the sync collection this handler's action belongs to.
     *
     * <p>Per WhatsApp Web, each handler declares which collection its mutations
     * are stored in (e.g., {@code REGULAR}, {@code CRITICAL_BLOCK}).
     * @return the sync patch type / collection name
     */
    SyncPatchType collectionName();

    /**
     * Returns the mutation format version for this handler.
     *
     * <p>Per WhatsApp Web, each handler declares a version number used for
     * version gating. Mutations with a version higher than this value are
     * skipped to avoid processing with incompatible logic.
     * @return the handler's supported mutation version
     */
    int version();

    /**
     * Applies a single mutation and returns a richer WA-style outcome.
     * @param client   the WhatsApp client
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    MutationApplicationResult applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation);

    /**
     * Applies a batch of mutations to local state.
     *
     * <p>Per WhatsApp Web, each handler receives the full batch of mutations
     * for its action type via {@code applyMutations(mutations, options)}.
     * Some handlers (e.g., favorites, primary feature, archive setting) apply
     * only the mutation with the latest timestamp rather than processing each
     * mutation sequentially.
     *
     * <p>The default implementation processes mutations one by one via
     * {@link #applyMutation(WhatsAppClient, DecryptedMutation.Trusted)}.
     * Handlers that need batch-level deduplication should override this method.
     * @param client    the WhatsAppClient instance linked to the mutations
     * @param mutations the batch of mutations to apply (already version-gated)
     * @return the detailed application results parallel to the input
     */
    default List<MutationApplicationResult> applyMutationBatch(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        var results = new ArrayList<MutationApplicationResult>(mutations.size());
        for (var mutation : mutations) {
            results.add(applyMutation(client, mutation));
        }
        return results;
    }

    /**
     * Resolves a conflict between a local pending mutation and an incoming
     * remote mutation with the same index.
     *
     * <p>The default implementation uses timestamp comparison: the mutation
     * with the later (or equal) timestamp wins. Subclasses can override this
     * to implement specialized logic (e.g., message-range merging).
     * @param localMutation  the local pending mutation
     * @param remoteMutation the incoming remote mutation
     * @return the conflict resolution indicating which mutation to keep and
     *         optionally a merged mutation
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdAction", exports = "AccountSyncdActionBase", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSyncdAction", exports = "ChatSyncdActionBase", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSyncdAction", exports = "ChatOrContactSyncdActionBase", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSyncdAction", exports = "MessageSyncdActionBase", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSyncdAction", exports = "ChatMessageRangeSyncdActionBase", adaptation = WhatsAppAdaptation.DIRECT)
    default ConflictResolution resolveConflicts(DecryptedMutation.Trusted localMutation, DecryptedMutation.Trusted remoteMutation) {
        if (remoteMutation.timestamp().compareTo(localMutation.timestamp()) >= 0) {
            return ConflictResolution.of(ConflictResolutionState.APPLY_REMOTE_DROP_LOCAL);
        } else {
            return ConflictResolution.of(ConflictResolutionState.SKIP_REMOTE);
        }
    }

    /**
     * Allows a handler to drop a remote mutation when a different pending local
     * mutation makes it obsolete, mirroring WA's cross-index conflict hook.
     * @param remoteMutation the candidate remote mutation
     * @param pendingByIndex all pending mutations indexed by mutation index
     * @return whether the remote mutation should be dropped
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdAction", exports = "AccountSyncdActionBase", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSyncdAction", exports = "ChatSyncdActionBase", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSyncdAction", exports = "ChatOrContactSyncdActionBase", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSyncdAction", exports = "MessageSyncdActionBase", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSyncdAction", exports = "ChatMessageRangeSyncdActionBase", adaptation = WhatsAppAdaptation.DIRECT)
    default boolean dropMutationDueToCrossIndexConflict(
            DecryptedMutation.Trusted remoteMutation,
            Map<String, DecryptedMutation.Trusted> pendingByIndex
    ) {
        return false;
    }
}

