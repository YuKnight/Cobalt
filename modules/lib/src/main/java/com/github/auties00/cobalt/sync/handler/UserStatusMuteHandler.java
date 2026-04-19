package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.group.GroupMetadata;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.model.sync.action.contact.UserStatusMuteAction;
import com.github.auties00.cobalt.model.sync.action.contact.UserStatusMuteActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.List;

/**
 * Handles {@code userStatusMute} sync actions from the {@code regular_high} collection.
 *
 * <p>This handler processes mutations that mute or unmute another user's (or group's)
 * status updates across linked devices. The mute state is applied to the matching
 * contact in the local store; for groups, it is written to the typed
 * {@link GroupMetadata#statusMuted()} field on the corresponding metadata row.
 * When the referenced contact or group is unknown to the local store, the
 * mutation is recorded as orphan so that a future {@code contact} or
 * {@code group_metadata} sync may retry it.
 *
 * <p>Index format: {@code ["userStatusMute", widString]} where {@code widString}
 * is a user wid (legacy form) or a group wid.
 *
 * @implNote WAWebUserStatusMuteSync.default — singleton instance of the user status
 *           mute sync action handler extending {@code AccountSyncdActionBase} with
 *           {@code collectionName = WASyncdConst.CollectionName.RegularHigh}
 */
@WhatsAppWebModule(moduleName = "WAWebUserStatusMuteSync")
public final class UserStatusMuteHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of this handler.
     *
     * @implNote WAWebUserStatusMuteSync — module-level singleton {@code p = new m()}
     *           assigned to {@code l.default = p}
     */
    @WhatsAppWebExport(moduleName = "WAWebUserStatusMuteSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final UserStatusMuteHandler INSTANCE = new UserStatusMuteHandler();

    /**
     * Private constructor preventing external instantiation.
     *
     * @implNote WAWebUserStatusMuteSync — class {@code m} constructor sets
     *           {@code collectionName = WASyncdConst.CollectionName.RegularHigh}
     */
    @WhatsAppWebExport(moduleName = "WAWebUserStatusMuteSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private UserStatusMuteHandler() {

    }

    /**
     * Returns the action name for this handler.
     *
     * @implNote WAWebUserStatusMuteSync.getAction — returns
     *           {@code WASyncdConst.Actions.UserStatusMute} (value: {@code "userStatusMute"})
     * @return the action name string
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUserStatusMuteSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return UserStatusMuteAction.ACTION_NAME; // WAWebUserStatusMuteSync.getAction
    }

    /**
     * Returns the sync collection this handler belongs to.
     *
     * @implNote WAWebUserStatusMuteSync — constructor sets
     *           {@code collectionName = WASyncdConst.CollectionName.RegularHigh}
     * @return the sync patch type
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUserStatusMuteSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return UserStatusMuteAction.COLLECTION_NAME; // WAWebUserStatusMuteSync.collectionName
    }

    /**
     * Returns the mutation format version for this handler.
     *
     * @implNote WAWebUserStatusMuteSync.getVersion — returns {@code 7}
     * @return the version number
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUserStatusMuteSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return UserStatusMuteAction.ACTION_VERSION; // WAWebUserStatusMuteSync.getVersion
    }

    /**
     * Applies a single user status mute mutation and returns whether it succeeded.
     *
     * @implNote WAWebUserStatusMuteSync.applyMutations — per-mutation logic within
     *           the batch handler, delegating to
     *           {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUserStatusMuteSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // WAWebUserStatusMuteSync.applyMutations
    }

    /**
     * Applies a single user status mute mutation and returns the detailed result.
     *
     * <p>For {@code SET} operations, this method:
     * <ul>
     *   <li>Validates the mutation index second element is a usable wid string;
     *       returns {@code MALFORMED} otherwise</li>
     *   <li>Validates the mutation value carries a {@link UserStatusMuteAction}
     *       with a non-{@code null} {@code muted} field; returns {@code MALFORMED} otherwise</li>
     *   <li>For group wids, returns {@code ORPHAN} when the group is unknown to
     *       the local store; otherwise updates the {@link GroupMetadata#statusMuted()}
     *       field on the corresponding metadata row and returns {@code SUCCESS}</li>
     *   <li>For user wids, returns {@code ORPHAN} when the contact is unknown;
     *       otherwise updates the contact's {@code statusMuted} field and returns
     *       {@code SUCCESS}</li>
     * </ul>
     *
     * <p>For non-{@code SET} operations, returns {@code UNSUPPORTED}.
     *
     * @implNote WAWebUserStatusMuteSync.applyMutations — per-mutation processing
     *           within the batch {@code applyMutations(t)} method. WA Web pre-fetches
     *           the set of existing user/group ids via {@code c(t)} (a single
     *           {@code WAWebLidAwareContactsDB.bulkGet} + {@code GroupMetadataTable.bulkGet})
     *           then for each mutation checks {@code i.has(u)}: if true the mutation is
     *           buffered into {@code l} (users) or {@code d} (groups) and returns
     *           {@code Success}; otherwise returns {@code Orphan} with
     *           {@code modelType: SyncModelType.UserStatusMute}. Cobalt does the
     *           per-mutation lookup directly via {@code findContactByJid} /
     *           {@code findChatByJid} since the store is in-memory and there is no
     *           bulk-fetch advantage.
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebUserStatusMuteSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebUserStatusMuteSync.applyMutations: if (e.operation === "set") ... else { _++; return {actionState: Unsupported} }
            return MutationApplicationResult.unsupported(); // WAWebUserStatusMuteSync.applyMutations: {actionState: Unsupported}
        }

        // MISMATCH (ORDER): WA Web checks the index (indexParts[1] is a wid) BEFORE the value. Cobalt currently
        // checks the value first, so a mutation with both a bad index and a missing muted field ends up tagged
        // {@code MALFORMED_VALUE} in Cobalt vs {@code MALFORMED_INDEX} in WA Web. Reordered below to match WA Web.
        var indexArray = JSON.parseArray(mutation.index()); // WAWebUserStatusMuteSync.applyMutations: var n = e.indexParts
        var widString = indexArray.getString(1); // WAWebUserStatusMuteSync.applyMutations: var u = n[1]
        if (widString == null || widString.isEmpty()) { // WAWebUserStatusMuteSync.applyMutations: if (!u || !WAWebWid.isWid(u)) return a.malformedActionIndex()
            return malformedActionIndex(); // WAWebUserStatusMuteSync.applyMutations: return a.malformedActionIndex()
        }

        Jid wid;
        try {
            wid = Jid.of(widString); // WAWebUserStatusMuteSync.applyMutations: WAWebWid.isWid(u) — validates wid string
        } catch (RuntimeException e) {
            return malformedActionIndex(); // WAWebUserStatusMuteSync.applyMutations: !WAWebWid.isWid(u) -> malformedActionIndex
        }

        if (!(mutation.value().action().orElse(null) instanceof UserStatusMuteAction action)) { // WAWebUserStatusMuteSync.applyMutations: var c = (t = s.userStatusMuteAction) == null ? void 0 : t.muted; if (c === void 0) return malformedActionValue(a.collectionName)
            return malformedActionValue(); // WAWebUserStatusMuteSync.applyMutations: return WAWebSyncdIndexUtils.malformedActionValue(a.collectionName)
        }

        // MISMATCH (COALESCE): WA Web specifically tests {@code c === void 0} where {@code c = userStatusMuteAction.muted}.
        // Cobalt's {@link UserStatusMuteAction#muted()} coalesces a {@code null} Boolean to {@code false}, so a
        // mutation with {@code userStatusMuteAction: {muted: null}} is silently applied as "unmute" here instead of
        // returning {@code malformedActionValue}. Fixing this would require promoting the nullable Boolean accessor
        // out of the package-private field; the project-wide "nullable boolean coalesces to false" convention
        // (see {@code feedback_nullable_bool_accessors.md}) intentionally accepts this divergence.
        // ADAPTED: WAWebUserStatusMuteSync.applyMutations — malformed-counter + first-three WARN log telemetry
        // is intentionally omitted (matches the project-wide policy of dropping WAM/WALogger paths).
        if (wid.hasServer(JidServer.groupOrCommunity())) { // WAWebUserStatusMuteSync.applyMutations: WAWebWid.isGroup(u)
            var groupMetadata = client.store().findChatMetadata(wid).orElse(null); // WAWebUserStatusMuteSync.applyMutations: i.has(u) — c() builds set from getGroupMetadataTable().bulkGet(a)
            if (!(groupMetadata instanceof GroupMetadata group)) { // WAWebUserStatusMuteSync.applyMutations: !i.has(u) -> Orphan
                return MutationApplicationResult.orphan(widString, "UserStatusMute"); // WAWebUserStatusMuteSync.applyMutations: {actionState: Orphan, orphanModel: {modelId: u, modelType: SyncModelType.UserStatusMute}}
            }
            group.setStatusMuted(action.muted()); // WAWebUserStatusMuteSync.applyMutations: d.push({id: u, statusMute: c}) -> later getGroupMetadataTable().bulkMergeOnly(d)
            // WAWebUserStatusMuteSync.applyMutations: frontendFireAndForget("updateContactsStatusMute", {groupStatusMuteUpdates: d, ...})
            // ADAPTED: Cobalt does not have frontend event dispatching; the store update is sufficient
            return MutationApplicationResult.success(); // WAWebUserStatusMuteSync.applyMutations: {actionState: SyncActionState.Success}
        }

        var contact = client.store().findContactByJid(wid); // WAWebUserStatusMuteSync.applyMutations: i.has(u) — c() builds set from WAWebLidAwareContactsDB.bulkGet(n)
        if (contact.isEmpty()) { // WAWebUserStatusMuteSync.applyMutations: !i.has(u) -> Orphan
            return MutationApplicationResult.orphan(widString, "UserStatusMute"); // WAWebUserStatusMuteSync.applyMutations: {actionState: Orphan, orphanModel: {modelId: u, modelType: SyncModelType.UserStatusMute}}
        }

        contact.get().setStatusMuted(action.muted()); // WAWebUserStatusMuteSync.applyMutations: l.push({id: u, statusMute: c}) -> later getContactTable().bulkCreateOrMerge(l)
        // WAWebUserStatusMuteSync.applyMutations: frontendFireAndForget("updateContactsStatusMute", {userStatusMuteUpdates: l, ...})
        // ADAPTED: Cobalt does not have frontend event dispatching; the store update is sufficient
        return MutationApplicationResult.success(); // WAWebUserStatusMuteSync.applyMutations: {actionState: SyncActionState.Success}
    }

    /**
     * Builds a pending mutation for muting or unmuting a user's status updates.
     *
     * <p>Per WhatsApp Web {@code WAWebUserStatusMuteSync.getMutationForStatusMute(e, t, n)}:
     * <ol>
     *   <li>Constructs the value with {@code {userStatusMuteAction: {muted: t}}}</li>
     *   <li>Builds the pending mutation via {@code WAWebSyncdActionUtils.buildPendingMutation}
     *       with {@code action = this.getAction()}, {@code collection = this.collectionName},
     *       {@code indexArgs = [e.toString({legacy: true})]}, {@code operation = SET},
     *       {@code timestamp = n}, {@code value}, and {@code version = this.getVersion()}</li>
     * </ol>
     *
     * @implNote WAWebUserStatusMuteSync.getMutationForStatusMute
     * @param wid       the JID of the user (or group) whose status mute state is being updated
     * @param muted     the new status mute state
     * @param timestamp the mutation timestamp
     * @return the pending mutation for the user status mute action
     */
    @WhatsAppWebExport(moduleName = "WAWebUserStatusMuteSync", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPendingMutation getMutationForStatusMute(Jid wid, boolean muted, Instant timestamp) {
        var action = new UserStatusMuteActionBuilder() // WAWebUserStatusMuteSync.getMutationForStatusMute: var r = {userStatusMuteAction: {muted: t}}
                .muted(muted) // WAWebUserStatusMuteSync.getMutationForStatusMute: muted: t
                .build();
        var value = new SyncActionValueBuilder() // WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp) // WAWebSyncdActionUtils.buildPendingMutation: timestamp: n
                .userStatusMuteAction(action) // WAWebUserStatusMuteSync.getMutationForStatusMute: value: {userStatusMuteAction: ...}
                .build();
        var index = JSON.toJSONString(List.of(actionName(), wid.toString())); // WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs)) where indexArgs = [e.toString({legacy: true})]
        var mutation = new DecryptedMutation.Trusted( // WAWebSyncdActionUtils.buildPendingMutation: return { collection, index, ... }
                index,
                value,
                SyncdOperation.SET, // WAWebUserStatusMuteSync.getMutationForStatusMute: operation: SyncdMutation$SyncdOperation.SET
                timestamp,
                version()
        );
        return new SyncPendingMutation(mutation, 0); // WAWebSyncdActionUtils.buildPendingMutation
    }
}
