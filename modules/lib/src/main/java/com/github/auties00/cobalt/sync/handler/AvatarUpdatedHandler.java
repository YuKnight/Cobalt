package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.AvatarUpdatedAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

/**
 * Handles {@code avatar_updated_action} app-state sync mutations.
 *
 * <p>Per WhatsApp Web {@code WAWebStickersAvatarUpdatedSyncAction}, only
 * {@code SET} operations are supported. The handler:
 * <ol>
 *   <li>Checks the {@code enable_avatars_on_web_companion} A/B prop and
 *       returns {@code Unsupported} for every mutation when the feature
 *       is disabled.</li>
 *   <li>Returns {@code Unsupported} for non-{@code SET} operations.</li>
 *   <li>Returns {@code Malformed} when the action or its {@code eventType}
 *       is missing.</li>
 *   <li>Skips mutations whose timestamp is at or before the local pairing
 *       timestamp (events that happened before this companion was paired).</li>
 *   <li>For {@code CREATED}/{@code UPDATED} marks the local user as having
 *       an avatar, for {@code DELETED} marks the user as not having one,
 *       and clears the recent avatar stickers cache.</li>
 * </ol>
 *
 * <p>Index format: {@code ["avatar_updated_action"]}.
 *
 * @implNote WAWebStickersAvatarUpdatedSyncAction
 */
@WhatsAppWebModule(moduleName = "WAWebStickersAvatarUpdatedSyncAction")
public final class AvatarUpdatedHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance shared by the {@code WebAppStateHandlerRegistry}.
     *
     * @implNote WAWebStickersAvatarUpdatedSyncAction: {@code var m=new d}—the module exports
     *           a single instance of the handler class.
     */
    @WhatsAppWebExport(moduleName = "WAWebStickersAvatarUpdatedSyncAction", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final AvatarUpdatedHandler INSTANCE = new AvatarUpdatedHandler();

    /**
     * Constructs a new handler.
     *
     * @implNote WAWebStickersAvatarUpdatedSyncAction: the JS class is constructed once at
     *           module load time; Cobalt enforces this by exposing only {@link #INSTANCE}.
     */
    @WhatsAppWebExport(moduleName = "WAWebStickersAvatarUpdatedSyncAction", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private AvatarUpdatedHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebStickersAvatarUpdatedSyncAction.getAction: {@code return WASyncdConst.Actions.AvatarUpdated}.
     * @return the canonical {@code "avatar_updated_action"} identifier
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersAvatarUpdatedSyncAction", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return AvatarUpdatedAction.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebStickersAvatarUpdatedSyncAction: {@code this.collectionName=WASyncdConst.CollectionName.Regular}
     *           assigned in the constructor of the handler.
     * @return {@link SyncPatchType#REGULAR}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersAvatarUpdatedSyncAction", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return AvatarUpdatedAction.COLLECTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote WAWebStickersAvatarUpdatedSyncAction.getVersion: {@code return 7}.
     * @return {@code 7}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersAvatarUpdatedSyncAction", exports = "default", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return AvatarUpdatedAction.ACTION_VERSION;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and reports {@code true} only when the result is
     * {@link SyncActionState#SUCCESS}. {@code Unsupported}, {@code Malformed} and
     * {@code Skipped} all map to {@code false} so the mutation does not
     * progress further in callers that only consume the legacy boolean API.
     *
     * @implNote ADAPTED: WAWebStickersAvatarUpdatedSyncAction.applyMutations returns a
     *           per-mutation {@code WASyncdConst.SyncActionState}; Cobalt collapses every
     *           non-{@code Success} value to {@code false} for the legacy boolean entry point.
     * @param client   the WhatsApp client linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the action state is {@code SUCCESS}, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersAvatarUpdatedSyncAction", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: collapses Unsupported/Malformed/Skipped to false
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implements the body of {@code WAWebStickersAvatarUpdatedSyncAction.applyMutations}
     * for a single mutation. The WA Web counter logging that aggregates
     * {@code notSupported}, {@code malformed} and {@code skipped} mutations
     * is intentionally omitted (WAM/telemetry).
     *
     * @implNote WAWebStickersAvatarUpdatedSyncAction.applyMutations
     * @param client   the WhatsApp client linked to the mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebStickersAvatarUpdatedSyncAction", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        //   if (!WAWebAvatarGatingUtils.avatarsOnWebEnabled())
        //     return mutations.map(() => ({actionState: Unsupported}))
        //   return WAWebABProps.getABPropConfigValue("enable_avatars_on_web_companion")
        if (!client.abPropsService().getBool(ABProp.ENABLE_AVATARS_ON_WEB_COMPANION)) {
            return MutationApplicationResult.unsupported();
        }

        //   if (e.operation !== "set") return notSupported++, {actionState: Unsupported}
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        //   var l = e.value.avatarUpdatedAction?.eventType
        //   if (l == null) return malformed++, malformedActionValue(this.collectionName)
        if (!(mutation.value().action().orElse(null) instanceof AvatarUpdatedAction action)) {
            return malformedActionValue();
        }
        var eventType = action.eventType().orElse(null);
        if (eventType == null) {
            return malformedActionValue();
        }

        //   var s = WAWebUserPrefsMultiDevice.getPairingTimestamp()
        //   if (s != null) {
        //     var u = WATimeUtils.castMilliSecondsToUnixTime(e.timestamp)
        //     if (u <= WATimeUtils.castToUnixTime(s)) return skipped++, {actionState: Skipped}
        //   }
        var pairingTimestamp = client.store().pairingTimestamp().orElse(null);
        if (pairingTimestamp != null && !mutation.timestamp().isAfter(pairingTimestamp)) {
            return MutationApplicationResult.skipped();
        }

        //   case CREATED: case UPDATED: WAWebHasAvatar.saveHasAvatarOnTempStorage(true); break
        //   case DELETED: WAWebHasAvatar.saveHasAvatarOnTempStorage(false); break
        //
        //
        // Cobalt collapses the UserPrefsStore "UserHasAvatar" key into
        // WhatsAppStore.setHasAvatar(Boolean) (Optional<Boolean> hasAvatar() getter).
        switch (eventType) {
            case CREATED, UPDATED -> client.store().setHasAvatar(true);
            case DELETED -> client.store().setHasAvatar(false);
        }

        //   WAWebRecentStickerCollectionMd.RecentStickerCollectionMd.removeAllRecentAvatarStickers()
        client.store().removeAllRecentAvatarStickers();
        return MutationApplicationResult.success();
    }
}
