package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.MusicUserIdAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

/**
 * Handles {@link MusicUserIdAction} sync mutations ({@code "music_user_id"}).
 *
 * <p>Each mutation carries either a single {@code musicUserId} string or a
 * {@code musicUserIdMap} of provider {@code -> userId} entries. The whole
 * action object is persisted on the local {@code WhatsAppStore} via
 * {@code setMusicUserIdState}. Only {@code SET} operations are accepted; any
 * other operation maps to {@link MutationApplicationResult#unsupported()} and
 * a missing, wrong-typed, or completely empty value maps to
 * {@link MutationApplicationResult#malformed()}.
 *
 * <p><b>NO_WA_BASIS:</b> The {@code SyncActionValue.MusicUserIdAction}
 * protobuf is defined in {@code WAWebProtobufSyncAction.pb} (exported as
 * {@code SyncActionValue$MusicUserIdActionSpec}) with two optional fields
 * ({@code musicUserId: string} at index {@code 1} and
 * {@code musicUserIdMap: map<string, string>} at index {@code 2}), but the
 * current WA Web snapshot does <em>not</em> ship a corresponding sync handler
 * module (no {@code WAWebMusicUserIdSync}). The action is also absent from
 * {@code WAWebCollectionHandlerActions.ActionHandlers}, the registry consumed
 * by {@code WAWebSyncdGetActionHandler.setActionHandlers}, so WA Web would
 * never dispatch any incoming mutation with this action via
 * {@code WAWebSyncdGetActionHandler.getActionHandler("music_user_id")} (the
 * lookup would return {@code undefined} and the mutation would be skipped).
 * The closest WA Web modules that touch the music surface
 * ({@code WAWebMusicParsingUtils}, {@code WAWebMusicGatingUtils},
 * {@code WAWebMusicPlaybackUtils}, {@code WAWebMusicUserPrefs},
 * {@code WAWebMusicConsumptionEligibilityUpdater},
 * {@code WAWebMusicEligibleCountriesProvider},
 * {@code WAWebFetchMusicEligibleCountries},
 * {@code WAWebUpdateMusicBlocklistAction},
 * {@code WAWebSNAPLUploadMusicConsumptionLogs},
 * {@code WAWebMediaDownloadMmsMusicArtwork}) all deal with music consumption
 * eligibility, country gating, parsing, playback or media download — none of
 * them consume {@code SyncActionValue.MusicUserIdAction}.
 *
 * <p>The Cobalt handler is a forward-looking implementation: it follows the
 * Cobalt sync handler conventions used by every other registered handler
 * (singleton, {@code applyMutationResult} producing a typed
 * {@link MutationApplicationResult}, eager store update on {@code SET}). Every
 * behavioural step here is Cobalt-inferred until WA Web ships the matching
 * {@code WAWebMusicUserIdSync} module.
 *
 * @implNote NO_WA_BASIS: no WA Web sync handler exists for
 *           {@code "music_user_id"}. Only the protobuf shape
 *           {@code SyncActionValue.MusicUserIdAction} (two optional fields,
 *           {@code musicUserId: string} at index {@code 1} and
 *           {@code musicUserIdMap: map<string, string>} at index {@code 2},
 *           from {@code WAWebProtobufSyncAction.pb}) is present in the WA Web
 *           snapshot. {@code WAWebCollectionHandlerActions.ActionHandlers}
 *           does not include a music user id handler.
 */
public final class MusicUserIdHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code MusicUserIdHandler}.
     *
     * @implNote NO_WA_BASIS: WA Web has no sync handler module for
     *           {@code "music_user_id"}; the singleton mirrors the
     *           {@code l.default = new u()} pattern used by every other Cobalt
     *           sync handler.
     */
    public static final MusicUserIdHandler INSTANCE = new MusicUserIdHandler();

    /**
     * Private constructor that enforces the singleton pattern.
     *
     * @implNote NO_WA_BASIS: no WA Web counterpart constructor; mirrors the
     *           Cobalt handler convention of a private no-arg constructor with
     *           a public {@code INSTANCE} field.
     */
    private MusicUserIdHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: returns the canonical {@code "music_user_id"}
     *           action name declared on {@link MusicUserIdAction#ACTION_NAME}.
     *           This name matches the protobuf field name
     *           {@code musicUserIdAction} in {@code WAWebProtobufSyncAction.pb}
     *           but no WA Web {@code WASyncdConst.Actions} entry references
     *           it.
     * @return the canonical {@code "music_user_id"} string
     */
    @Override
    public String actionName() {
        return MusicUserIdAction.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link SyncPatchType#REGULAR} as an inferred default.
     *
     * @implNote NO_WA_BASIS: WA Web does not declare a collection for this
     *           action since no handler module exists. Cobalt assigns
     *           {@link SyncPatchType#REGULAR} to keep the value consistent
     *           with other identifier-shaped account-level handlers (e.g.
     *           {@code WamoUserIdentifierHandler},
     *           {@code PrivacySettingChannelsPersonalisedRecommendationHandler})
     *           that operate on a single global user-account value stored on
     *           the flattened Cobalt store. The model class
     *           {@link MusicUserIdAction} (a context file) does not yet expose
     *           a {@code COLLECTION_NAME} constant, so the value is inlined
     *           here.
     * @return {@link SyncPatchType#REGULAR}
     */
    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR; // NO_WA_BASIS: no WA Web sync handler declares a collection for "music_user_id"; REGULAR matches sibling identifier-style handlers
    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: WA Web has no version constant for this action;
     *           Cobalt defaults to {@link MusicUserIdAction#ACTION_VERSION}
     *           ({@code 1}) matching every other unmigrated sync action
     *           handler.
     * @return the integer version constant declared on the action class
     */
    @Override
    public int version() {
        return MusicUserIdAction.ACTION_VERSION;
    }

    /**
     * Applies a music user id mutation.
     *
     * <p>Boolean adapter on top of
     * {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}:
     * returns {@code true} only when the underlying result is
     * {@link SyncActionState#SUCCESS}. {@code MALFORMED} and
     * {@code UNSUPPORTED} both map to {@code false}, mirroring the convention
     * used by every other Cobalt sync handler.
     *
     * @implNote ADAPTED: NO_WA_BASIS — there is no WA Web
     *           {@code WAWebMusicUserIdSync.applyMutations} to map to. The
     *           boolean collapse mirrors the
     *           {@code SUCCESS == true, everything-else == false} pattern used
     *           by all other Cobalt sync handlers.
     * @param client   the {@link WhatsAppClient} instance linked to the
     *                 mutation
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was successfully applied,
     *         {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, wamService, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: NO_WA_BASIS — boolean collapse over the typed result
    }

    /**
     * Applies a music user id mutation and returns the detailed outcome.
     *
     * <p>The processing pipeline is:
     * <ol>
     *   <li>If the operation is not {@link SyncdOperation#SET}, return
     *       {@link MutationApplicationResult#unsupported()}. Only {@code SET}
     *       mutations are accepted; the action carries an identifier-style
     *       payload (a single string and/or a string-keyed map) and there is
     *       no semantic for {@code REMOVE}.</li>
     *   <li>Resolve the mutation value to a {@link MusicUserIdAction}; if the
     *       value is missing or of the wrong type, return
     *       {@link MutationApplicationResult#malformed()}.</li>
     *   <li>Reject mutations where both {@code musicUserId} and
     *       {@code musicUserIdMap} are empty by returning
     *       {@link MutationApplicationResult#malformed()}: at least one of
     *       the two protobuf fields must be present for the mutation to carry
     *       any meaningful update.</li>
     *   <li>Persist the resolved {@link MusicUserIdAction} on the store via
     *       {@code WhatsAppStore.setMusicUserIdState} and return
     *       {@link MutationApplicationResult#success()}.</li>
     * </ol>
     *
     * <p>The store accessors {@code musicUserIdState()} and
     * {@code setMusicUserIdState(...)} already exist on
     * {@code WhatsAppStore} / {@code AbstractWhatsAppStore}; this handler is
     * the sole writer.
     *
     * @implNote NO_WA_BASIS: no WA Web sync handler implements
     *           {@code "music_user_id"}. The shape of this method —
     *           only-{@code SET}, identifier-style payload, single store
     *           setter — is inferred from the protobuf
     *           {@code SyncActionValue.MusicUserIdAction} (two optional
     *           fields, {@code musicUserId} and {@code musicUserIdMap}) and
     *           from sibling identifier-style handlers (e.g.
     *           {@code WamoUserIdentifierHandler}) which follow the same
     *           {@code identifier -> single store setter} pattern.
     * @param client   the {@link WhatsAppClient} instance linked to the
     *                 mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof MusicUserIdAction action)) {
            return MutationApplicationResult.malformed();
        }

        if (action.musicUserId().isEmpty() && action.musicUserIdMap().isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        client.store().setMusicUserIdState(action);
        return MutationApplicationResult.success();
    }
}
