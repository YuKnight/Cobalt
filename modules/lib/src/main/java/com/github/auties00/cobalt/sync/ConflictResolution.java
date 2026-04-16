package com.github.auties00.cobalt.sync;

import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.sync.ConflictResolutionState;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Represents the result of a conflict resolution between a local pending
 * mutation and an incoming remote mutation with the same index.
 *
 * <p>Per WhatsApp Web, some handlers (e.g., archive, clear chat) may
 * produce a merged mutation when neither range fully encloses the other.
 * In that case, the merged mutation replaces the original local pending
 * mutation and is applied to local state instead of the remote.
 *
 * @param state           the resolution state indicating which mutation to keep
 * @param mergedMutation  an optional merged mutation to apply and add to pending,
 *                        only present when the handler merges two non-enclosing
 *                        ranges and returns {@code SKIP_REMOTE_DROP_LOCAL}
 * @implNote Wraps the return value of the conflict resolution callbacks
 *     defined in {@code WAWebSyncActionCore.doConflictResolution} and
 *     consumed by {@code WAWebApplyActionUtils.applyAction}.
 */
@WhatsAppWebModule(moduleName = "WAWebSyncActionCore")
public record ConflictResolution(
        ConflictResolutionState state,
        DecryptedMutation.Trusted mergedMutation
) {
    /**
     * Creates a resolution with no merged mutation.
     *
     * @param state the resolution state
     * @return a new conflict resolution
     * @implNote Convenience factory for the common case where a handler
     *     decides between keeping local or remote without producing a
     *     merged mutation.
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncActionCore", exports = "doConflictResolution", adaptation = WhatsAppAdaptation.ADAPTED)
    public static ConflictResolution of(ConflictResolutionState state) {
        return new ConflictResolution(state, null);
    }

    /**
     * Creates a resolution that merges the local and remote mutations.
     *
     * <p>Per WhatsApp Web, the merged mutation replaces the old local
     * pending mutation and is applied to local state. Both the original
     * local and remote mutations are dropped.
     *
     * @param merged the merged mutation to apply and add to pending
     * @return a new conflict resolution with {@code SKIP_REMOTE_DROP_LOCAL} state
     * @implNote Factory used by handlers that produce a merged mutation
     *     (e.g. archive/clear chat range merges).
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncActionCore", exports = "doConflictResolution", adaptation = WhatsAppAdaptation.ADAPTED)
    public static ConflictResolution merged(DecryptedMutation.Trusted merged) {
        return new ConflictResolution(ConflictResolutionState.SKIP_REMOTE_DROP_LOCAL, merged);
    }
}
