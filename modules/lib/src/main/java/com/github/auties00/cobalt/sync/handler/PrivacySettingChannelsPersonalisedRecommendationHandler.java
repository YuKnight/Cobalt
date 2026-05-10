package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.privacy.PrivacySettingChannelsPersonalisedRecommendationAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
/**
 * Handles {@link PrivacySettingChannelsPersonalisedRecommendationAction} sync
 * mutations ({@code "setting_channels_personalised_recommendation_optout"}).
 *
 * <p>Each mutation carries a single {@code isUserOptedOut} boolean flag that
 * controls whether the linked WhatsApp account has opted out of personalised
 * channel recommendations. The flag is persisted on the local
 * {@code WhatsAppStore} via {@code setChannelsPersonalisedRecommendationOptOut}.
 * Only {@code SET} operations are accepted; any other operation maps to
 * {@link MutationApplicationResult#unsupported()} and a missing or wrong-typed
 * value maps to {@link MutationApplicationResult#malformed()}.
 *
 * <p><b>NO_WA_BASIS:</b> The
 * {@code SyncActionValue.PrivacySettingChannelsPersonalisedRecommendationAction}
 * protobuf is defined in {@code WAWebProtobufSyncAction.pb} (exported as
 * {@code SyncActionValue$PrivacySettingChannelsPersonalisedRecommendationActionSpec})
 * with one optional field ({@code isUserOptedOut: bool} at index {@code 1}),
 * and the {@code WAWebProtobufSyncAction.pb} collection-name resolver maps
 * {@code PRIVACY_SETTING_CHANNELS_PERSONALISED_RECOMMENDATION_ACTION} (numeric
 * id {@code 64}) to the {@code REGULAR} collection. However, the current WA
 * Web snapshot does <em>not</em> ship a corresponding sync handler module (no
 * {@code WAWebPrivacySettingChannelsPersonalisedRecommendationSync}). The
 * action is also absent from {@code WASyncdConst.Actions}, the action-name
 * enum consumed by {@code WAWebSyncdGetActionHandler}, and from
 * {@code WAWebCollectionHandlerActions.ActionHandlers}, the registry consumed
 * by {@code WAWebSyncdGetActionHandler.setActionHandlers}. Consequently WA Web
 * would never dispatch any incoming mutation with this action via
 * {@code WAWebSyncdGetActionHandler.getActionHandler("setting_channels_personalised_recommendation_optout")}
 * (the lookup would return {@code undefined} and the mutation would be
 * skipped). The string literal
 * {@code "setting_channels_personalised_recommendation_optout"} is only
 * present inside {@code WAWebProtobufSyncAction.pb} as the protobuf field
 * name; no other WA Web module references it.
 *
 * <p>The Cobalt handler is a forward-looking implementation: it follows the
 * Cobalt sync handler conventions used by every other registered handler
 * (singleton, {@code applyMutation} producing a typed
 * {@link MutationApplicationResult}, eager store update on {@code SET}). The
 * shape of the handler — only-{@code SET}, single-boolean payload, single
 * store setter — is inferred directly from the protobuf shape (one optional
 * boolean field) and from sibling boolean-flag privacy handlers such as
 * {@code DisableLinkPreviewsHandler} which follow the same
 * {@code single-boolean -> single store setter} pattern. Every behavioural
 * step here is Cobalt-inferred until WA Web ships the matching
 * {@code WAWebPrivacySettingChannelsPersonalisedRecommendationSync} module.
 */
public final class PrivacySettingChannelsPersonalisedRecommendationHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of
     * {@code PrivacySettingChannelsPersonalisedRecommendationHandler}.
     */
    public static final PrivacySettingChannelsPersonalisedRecommendationHandler INSTANCE =
            new PrivacySettingChannelsPersonalisedRecommendationHandler();

    /**
     * Private constructor that enforces the singleton pattern.
     */
    private PrivacySettingChannelsPersonalisedRecommendationHandler() {

    }

    /**
     * {@inheritDoc}
     * @return the canonical
     *         {@code "setting_channels_personalised_recommendation_optout"}
     *         string
     */
    @Override
    public String actionName() {
        return PrivacySettingChannelsPersonalisedRecommendationAction.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link SyncPatchType#REGULAR}, matching the
     * {@code PRIVACY_SETTING_CHANNELS_PERSONALISED_RECOMMENDATION_ACTION ->
     * REGULAR} mapping declared in {@code WAWebProtobufSyncAction.pb}.
     * @return {@link SyncPatchType#REGULAR}
     */
    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR;
    }

    /**
     * {@inheritDoc}
     * @return the integer version constant declared on the action class
     */
    @Override
    public int version() {
        return PrivacySettingChannelsPersonalisedRecommendationAction.ACTION_VERSION;
    }

    /**
     * Applies a personalised channel recommendation opt-out mutation and
     * returns the detailed outcome.
     *
     * <p>The processing pipeline is:
     * <ol>
     *   <li>If the operation is not {@link SyncdOperation#SET}, return
     *       {@link MutationApplicationResult#unsupported()}. Only {@code SET}
     *       mutations are accepted; the action carries a single boolean
     *       opt-out flag and there is no semantic for {@code REMOVE}.</li>
     *   <li>Resolve the mutation value to a
     *       {@link PrivacySettingChannelsPersonalisedRecommendationAction};
     *       if the value is missing or of the wrong type, return
     *       {@link MutationApplicationResult#malformed()}.</li>
     *   <li>Persist the resolved opt-out boolean on the store via
     *       {@code WhatsAppStore.setChannelsPersonalisedRecommendationOptOut}
     *       and return {@link MutationApplicationResult#success()}.</li>
     * </ol>
     *
     * <p>The store accessors
     * {@code channelsPersonalisedRecommendationOptOut()} and
     * {@code setChannelsPersonalisedRecommendationOptOut(...)} already exist
     * on {@code WhatsAppStore} / {@code AbstractWhatsAppStore}; this handler
     * is the sole writer.
     * @param client   the {@link WhatsAppClient} instance linked to the
     *                 mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof PrivacySettingChannelsPersonalisedRecommendationAction action)) {
            return MutationApplicationResult.malformed();
        }

        client.store().setChannelsPersonalisedRecommendationOptOut(action.isUserOptedOut());
        return MutationApplicationResult.success();
    }
}
