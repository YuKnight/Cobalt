package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.RecentEmojiWeight;
import com.github.auties00.cobalt.model.sync.action.media.RecentEmojiWeightsAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

import java.util.List;

/**
 * Handles {@link RecentEmojiWeightsAction} sync mutations
 * ({@code "recent_emoji_weights_action"}).
 *
 * <p>Each mutation carries a list of {@link RecentEmojiWeight} entries — opaque
 * {@code (emoji, weight)} pairs that describe the relative usage frequency of
 * recently picked emojis. The list is persisted on the local
 * {@code WhatsAppStore} via {@code setRecentEmojiWeights} so that emoji
 * suggestion ranking can be restored across devices. Only {@code SET}
 * operations are accepted; any other operation maps to
 * {@link MutationApplicationResult#unsupported()} and a missing or
 * wrong-typed value maps to {@link MutationApplicationResult#malformed()}.
 *
 * <p><b>NO_WA_BASIS:</b> The
 * {@code SyncActionValue.RecentEmojiWeightsAction} protobuf is defined in
 * {@code WAWebProtobufSyncAction.pb} as field
 * {@code recentEmojiWeightsAction} at index {@code 11} (exported as
 * {@code SyncActionValue$RecentEmojiWeightsActionSpec}) with a single
 * repeated field {@code weights: RecentEmojiWeight} at index {@code 1}, where
 * each {@code RecentEmojiWeight} carries an {@code emoji: string} (index
 * {@code 1}) and a {@code weight: float} (index {@code 2}). The mutation
 * name is wired in the same module's
 * {@code MutationProps$MutationName} enum
 * ({@code RECENT_EMOJI_WEIGHTS_ACTION:11}) with the canonical action string
 * {@code "recent_emoji_weights_action"}, and the inline collection router in
 * {@code WAWebProtobufSyncAction.pb}
 * ({@code e===c.RECENT_EMOJI_WEIGHTS_ACTION?u.REGULAR_LOW}) explicitly maps
 * the action to the {@code REGULAR_LOW} collection.
 *
 * <p>However, the current WA Web snapshot does <em>not</em> ship a
 * corresponding sync handler module (no {@code WAWebRecentEmojiWeightsSync}).
 * The action is also absent from
 * {@code WAWebCollectionHandlerActions.ActionHandlers}, the registry consumed
 * by {@code WAWebSyncdGetActionHandler.setActionHandlers}, so WA Web would
 * never dispatch any incoming mutation with this action via
 * {@code WAWebSyncdGetActionHandler.getActionHandler("recent_emoji_weights_action")}
 * (the lookup would return {@code undefined} and the mutation would be
 * skipped). Recent emojis on WA Web are tracked entirely in the in-memory
 * {@code WAWebRecentEmojiCollection} (declared in {@code WAWebCollections}
 * and seeded by {@code WAWebRecentEmojiModel}), incremented by
 * {@code WAWebEmojiSuggestions.react} when the user selects a suggestion,
 * and consumed by ranking helpers in {@code WAWebEmojiSearch}; none of these
 * modules read or write the {@code SyncActionValue.RecentEmojiWeightsAction}
 * protobuf. The action appears to be a mobile-only sync surface that the WA
 * Web client tolerates only at the protobuf shape level.
 *
 * <p>The Cobalt handler is a forward-looking implementation: it follows the
 * Cobalt sync handler conventions used by every other registered handler
 * (singleton, {@code applyMutationResult} producing a typed
 * {@link MutationApplicationResult}, eager store update on {@code SET}). Every
 * behavioural step here is Cobalt-inferred until WA Web ships the matching
 * {@code WAWebRecentEmojiWeightsSync} module.
 *
 * @implNote NO_WA_BASIS: no WA Web sync handler exists for
 *           {@code "recent_emoji_weights_action"}. Only the protobuf shape
 *           {@code SyncActionValue.RecentEmojiWeightsAction} (field index
 *           {@code 11}, single repeated {@code weights: RecentEmojiWeight} at
 *           index {@code 1}, from {@code WAWebProtobufSyncAction.pb}), the
 *           {@code MutationProps$MutationName.RECENT_EMOJI_WEIGHTS_ACTION:11}
 *           enum entry, the canonical action name
 *           {@code "recent_emoji_weights_action"}, and the inline collection
 *           mapping ({@code RECENT_EMOJI_WEIGHTS_ACTION -> REGULAR_LOW}) are
 *           present in the WA Web snapshot.
 *           {@code WAWebCollectionHandlerActions.ActionHandlers} does not
 *           include a recent emoji weights handler.
 */
public final class RecentEmojiWeightsHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code RecentEmojiWeightsHandler}.
     *
     * @implNote NO_WA_BASIS: WA Web has no sync handler module for
     *           {@code "recent_emoji_weights_action"}; the singleton mirrors
     *           the {@code l.default = new u()} pattern used by every other
     *           Cobalt sync handler.
     */
    public static final RecentEmojiWeightsHandler INSTANCE = new RecentEmojiWeightsHandler();

    /**
     * Private constructor that enforces the singleton pattern.
     *
     * @implNote NO_WA_BASIS: no WA Web counterpart constructor; mirrors the
     *           Cobalt handler convention of a private no-arg constructor with
     *           a public {@code INSTANCE} field.
     */
    private RecentEmojiWeightsHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: returns the canonical
     *           {@code "recent_emoji_weights_action"} action name declared on
     *           {@link RecentEmojiWeightsAction#ACTION_NAME}. This name matches
     *           the protobuf field name {@code recentEmojiWeightsAction} (index
     *           {@code 11}) in {@code WAWebProtobufSyncAction.pb} and the
     *           {@code MutationProps$MutationName.RECENT_EMOJI_WEIGHTS_ACTION}
     *           enum entry, but no WA Web runtime handler references it.
     * @return the canonical {@code "recent_emoji_weights_action"} string
     */
    @Override
    public String actionName() {
        return RecentEmojiWeightsAction.ACTION_NAME; // NO_WA_BASIS: WAWebProtobufSyncAction.pb only declares the protobuf field recentEmojiWeightsAction at index 11
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link SyncPatchType#REGULAR_LOW} as inferred from the WA Web
     * protobuf-side collection router.
     *
     * @implNote NO_WA_BASIS: WA Web does not declare a sync handler for this
     *           action, but the inline collection router in
     *           {@code WAWebProtobufSyncAction.pb}
     *           ({@code e===c.RECENT_EMOJI_WEIGHTS_ACTION?u.REGULAR_LOW})
     *           explicitly maps the action id {@code 11} to the
     *           {@code REGULAR_LOW} collection. Cobalt mirrors that mapping by
     *           returning {@link SyncPatchType#REGULAR_LOW}.
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return RecentEmojiWeightsAction.COLLECTION_NAME; // NO_WA_BASIS: matches the WAWebProtobufSyncAction.pb inline collection mapping RECENT_EMOJI_WEIGHTS_ACTION -> REGULAR_LOW
    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: WA Web has no version constant for this action;
     *           Cobalt defaults to
     *           {@link RecentEmojiWeightsAction#ACTION_VERSION} ({@code 11},
     *           mirroring the protobuf field index since no runtime handler
     *           ever publishes a version constant).
     * @return the integer version constant declared on the action class
     */
    @Override
    public int version() {
        return RecentEmojiWeightsAction.ACTION_VERSION; // NO_WA_BASIS: WA Web has no recent emoji weights version constant; defaults to the protobuf field index 11
    }

    /**
     * Applies a recent emoji weights mutation.
     *
     * <p>Boolean adapter on top of
     * {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}:
     * returns {@code true} only when the underlying result is
     * {@link SyncActionState#SUCCESS}. {@code MALFORMED} and
     * {@code UNSUPPORTED} both map to {@code false}, mirroring the convention
     * used by every other Cobalt sync handler.
     *
     * @implNote ADAPTED: NO_WA_BASIS — there is no WA Web
     *           {@code WAWebRecentEmojiWeightsSync.applyMutations} to map to.
     *           The boolean collapse mirrors the
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
     * Applies a recent emoji weights mutation and returns the detailed
     * outcome.
     *
     * <p>The processing pipeline is:
     * <ol>
     *   <li>If the operation is not {@link SyncdOperation#SET}, return
     *       {@link MutationApplicationResult#unsupported()}. Only {@code SET}
     *       mutations are accepted; the action carries a full snapshot of the
     *       recent emoji weight list and there is no semantic for
     *       {@code REMOVE}.</li>
     *   <li>Resolve the mutation value to a
     *       {@link RecentEmojiWeightsAction}; if the value is missing or of
     *       the wrong type, return
     *       {@link MutationApplicationResult#malformed()}.</li>
     *   <li>Persist the resolved {@link RecentEmojiWeight} list on the store
     *       via {@code WhatsAppStore.setRecentEmojiWeights} and return
     *       {@link MutationApplicationResult#success()}.</li>
     * </ol>
     *
     * <p>The store accessors {@code recentEmojiWeights()} and
     * {@code setRecentEmojiWeights(...)} already exist on {@code WhatsAppStore}
     * / {@code AbstractWhatsAppStore}; this handler is the sole writer.
     *
     * @implNote NO_WA_BASIS: no WA Web sync handler implements
     *           {@code "recent_emoji_weights_action"}. The shape of this
     *           method — only-{@code SET}, full snapshot replacement of the
     *           weights list, single store setter — is inferred from the
     *           protobuf {@code SyncActionValue.RecentEmojiWeightsAction}
     *           ({@code repeated weights: RecentEmojiWeight} at index
     *           {@code 1}) and from sibling list-snapshot handlers (e.g.
     *           {@code FavoritesHandler}) which follow the same {@code SET}-only
     *           pattern. The closest WA Web touchpoint for the underlying data
     *           model is {@code WAWebRecentEmojiCollection} /
     *           {@code WAWebRecentEmojiModel}, but those operate on a purely
     *           local in-memory collection and never observe this protobuf.
     * @param client   the {@link WhatsAppClient} instance linked to the
     *                 mutation
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, WamService wamService, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // NO_WA_BASIS: only SET makes sense for a snapshot-style weight list
            return MutationApplicationResult.unsupported();
        }

        if (!(mutation.value().action().orElse(null) instanceof RecentEmojiWeightsAction action)) { // NO_WA_BASIS: payload type guard
            return MutationApplicationResult.malformed();
        }

        var weights = action.weights(); // NO_WA_BASIS: full snapshot of the recent emoji weight list (already null-safe via the action accessor)
        client.store().setRecentEmojiWeights(weights); // NO_WA_BASIS: persist the snapshot on the flattened Cobalt store
        return MutationApplicationResult.success();
    }
}
