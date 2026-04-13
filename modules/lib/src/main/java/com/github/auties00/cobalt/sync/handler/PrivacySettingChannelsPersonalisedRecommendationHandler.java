package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
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
 * (singleton, {@code applyMutationResult} producing a typed
 * {@link MutationApplicationResult}, eager store update on {@code SET}). The
 * shape of the handler — only-{@code SET}, single-boolean payload, single
 * store setter — is inferred directly from the protobuf shape (one optional
 * boolean field) and from sibling boolean-flag privacy handlers such as
 * {@code DisableLinkPreviewsHandler} which follow the same
 * {@code single-boolean -> single store setter} pattern. Every behavioural
 * step here is Cobalt-inferred until WA Web ships the matching
 * {@code WAWebPrivacySettingChannelsPersonalisedRecommendationSync} module.
 *
 * @implNote NO_WA_BASIS: no WA Web sync handler exists for
 *           {@code "setting_channels_personalised_recommendation_optout"}.
 *           Only the protobuf shape
 *           {@code SyncActionValue.PrivacySettingChannelsPersonalisedRecommendationAction}
 *           (one optional field, {@code isUserOptedOut: bool} at index
 *           {@code 1}, from {@code WAWebProtobufSyncAction.pb}) is present in
 *           the WA Web snapshot, together with the
 *           {@code PRIVACY_SETTING_CHANNELS_PERSONALISED_RECOMMENDATION_ACTION
 *           -> REGULAR} mapping in the same module.
 *           {@code WASyncdConst.Actions} does not declare a constant for this
 *           action and {@code WAWebCollectionHandlerActions.ActionHandlers}
 *           does not include a personalised-recommendation handler.
 */
public final class PrivacySettingChannelsPersonalisedRecommendationHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of
     * {@code PrivacySettingChannelsPersonalisedRecommendationHandler}.
     *
     * @implNote NO_WA_BASIS: WA Web has no sync handler module for
     *           {@code "setting_channels_personalised_recommendation_optout"};
     *           the singleton mirrors the {@code l.default = new u()} pattern
     *           used by every other Cobalt sync handler.
     */
    public static final PrivacySettingChannelsPersonalisedRecommendationHandler INSTANCE =
            new PrivacySettingChannelsPersonalisedRecommendationHandler(); // NO_WA_BASIS: no WA Web counterpart singleton

    /**
     * Private constructor that enforces the singleton pattern.
     *
     * @implNote NO_WA_BASIS: no WA Web counterpart constructor; mirrors the
     *           Cobalt handler convention of a private no-arg constructor with
     *           a public {@code INSTANCE} field.
     */
    private PrivacySettingChannelsPersonalisedRecommendationHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: returns the canonical
     *           {@code "setting_channels_personalised_recommendation_optout"}
     *           action name declared on
     *           {@link PrivacySettingChannelsPersonalisedRecommendationAction#ACTION_NAME}.
     *           This name matches the protobuf field name string in
     *           {@code WAWebProtobufSyncAction.pb} but no WA Web
     *           {@code WASyncdConst.Actions} entry references it.
     * @return the canonical
     *         {@code "setting_channels_personalised_recommendation_optout"}
     *         string
     */
    @Override
    public String actionName() {
        return PrivacySettingChannelsPersonalisedRecommendationAction.ACTION_NAME; // NO_WA_BASIS: WAWebProtobufSyncAction.pb only declares the protobuf field name string
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link SyncPatchType#REGULAR}, matching the
     * {@code PRIVACY_SETTING_CHANNELS_PERSONALISED_RECOMMENDATION_ACTION ->
     * REGULAR} mapping declared in {@code WAWebProtobufSyncAction.pb}.
     *
     * @implNote NO_WA_BASIS for the handler itself, but the value is anchored
     *           in {@code WAWebProtobufSyncAction.pb}: the collection-name
     *           resolver explicitly maps
     *           {@code e === c.PRIVACY_SETTING_CHANNELS_PERSONALISED_RECOMMENDATION_ACTION
     *           ? u.REGULAR} (alongside
     *           {@code BUSINESS_BROADCAST_ASSOCIATION_ACTION} and
     *           {@code DETECTED_OUTCOMES_STATUS_ACTION}). Cobalt mirrors the
     *           same {@link SyncPatchType#REGULAR} value.
     * @return {@link SyncPatchType#REGULAR}
     */
    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR; // WAWebProtobufSyncAction.pb collection resolver: PRIVACY_SETTING_CHANNELS_PERSONALISED_RECOMMENDATION_ACTION -> u.REGULAR
    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: WA Web has no version constant for this action;
     *           Cobalt defaults to
     *           {@link PrivacySettingChannelsPersonalisedRecommendationAction#ACTION_VERSION}
     *           ({@code 1}) matching every other unmigrated sync action
     *           handler.
     * @return the integer version constant declared on the action class
     */
    @Override
    public int version() {
        return PrivacySettingChannelsPersonalisedRecommendationAction.ACTION_VERSION; // NO_WA_BASIS: WA Web has no personalised-recommendation version constant; defaults to 1
    }

    /**
     * Applies a personalised channel recommendation opt-out mutation.
     *
     * <p>Boolean adapter on top of
     * {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}:
     * returns {@code true} only when the underlying result is
     * {@link SyncActionState#SUCCESS}. {@code MALFORMED} and
     * {@code UNSUPPORTED} both map to {@code false}, mirroring the convention
     * used by every other Cobalt sync handler.
     *
     * @implNote ADAPTED: NO_WA_BASIS — there is no WA Web
     *           {@code WAWebPrivacySettingChannelsPersonalisedRecommendationSync.applyMutations}
     *           to map to. The boolean collapse mirrors the
     *           {@code SUCCESS == true, everything-else == false} pattern used
     *           by all other Cobalt sync handlers.
     * @param client   the {@link WhatsAppClient} instance linked to the
     *                 mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was successfully applied,
     *         {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: NO_WA_BASIS — boolean collapse over the typed result
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
     *
     * @implNote NO_WA_BASIS: no WA Web sync handler implements
     *           {@code "setting_channels_personalised_recommendation_optout"}.
     *           The shape of this method — only-{@code SET}, single-boolean
     *           payload, single store setter — is inferred from the protobuf
     *           {@code SyncActionValue.PrivacySettingChannelsPersonalisedRecommendationAction}
     *           (one optional {@code isUserOptedOut: bool} field) and from
     *           sibling boolean-flag privacy handlers such as
     *           {@code DisableLinkPreviewsHandler} which follow the same
     *           {@code single-boolean -> single store setter} pattern.
     * @param client   the {@link WhatsAppClient} instance linked to the
     *                 mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // NO_WA_BASIS: only SET makes sense for a single-boolean opt-out flag
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof PrivacySettingChannelsPersonalisedRecommendationAction action)) { // NO_WA_BASIS: payload type guard
            return MutationApplicationResult.malformed();
        }

        client.store().setChannelsPersonalisedRecommendationOptOut(action.isUserOptedOut()); // NO_WA_BASIS: persist the opt-out flag on the flattened Cobalt store
        return MutationApplicationResult.success();
    }
}
