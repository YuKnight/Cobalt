package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.NewsletterSavedInterestsAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import com.github.auties00.cobalt.wam.WamService;

/**
 * Handles {@link NewsletterSavedInterestsAction} sync mutations
 * ({@code "newsletter_saved_interests"}).
 *
 * <p>Each mutation carries a single {@code newsletterSavedInterests} string
 * (an opaque, server-defined token blob describing the user's saved newsletter
 * interest selections) which is persisted on the local {@code WhatsAppStore}
 * via {@code setNewsletterSavedInterests}. Only {@code SET} operations are
 * accepted; any other operation maps to
 * {@link MutationApplicationResult#unsupported()} and a missing or empty value
 * maps to {@link MutationApplicationResult#malformed()}.
 *
 * <p><b>NO_WA_BASIS:</b> The
 * {@code SyncActionValue.NewsletterSavedInterestsAction} protobuf is defined
 * in {@code WAWebProtobufSyncAction.pb} as field index {@code 75} with a single
 * {@code newsletterSavedInterests: string} (index {@code 1}, exported as
 * {@code SyncActionValue$NewsletterSavedInterestsActionSpec}). The collection
 * is hardcoded to {@code REGULAR} by the protobuf-side router
 * ({@code e===c.NEWSLETTER_SAVED_INTERESTS_ACTION?u.REGULAR} in
 * {@code WAWebProtobufSyncAction.pb}). However, the current WA Web snapshot
 * does <em>not</em> ship a corresponding sync handler module (no
 * {@code WAWebNewsletterSavedInterestsSync}). The action is also absent from
 * {@code WAWebCollectionHandlerActions.ActionHandlers}, the registry consumed
 * by {@code WAWebSyncdGetActionHandler.setActionHandlers}, so WA Web would
 * never dispatch any incoming mutation with this action via
 * {@code WAWebSyncdGetActionHandler.getActionHandler("newsletter_saved_interests")}
 * (the lookup would return {@code undefined} and the mutation would be
 * skipped). The closest WA Web modules that touch the newsletter surface
 * deal with newsletter metadata, channel discovery, channel directory and
 * newsletter cleanup tasks (for example {@code WAWebNewsletterCleanupTasks}
 * registered in {@code WAWebTasksDefinitions}), none of which consume
 * {@code SyncActionValue.NewsletterSavedInterestsAction}.
 *
 * <p>The Cobalt handler is a forward-looking implementation: it follows the
 * Cobalt sync handler conventions used by every other registered handler
 * (singleton, {@code applyMutationResult} producing a typed
 * {@link MutationApplicationResult}, eager store update on {@code SET}). Every
 * behavioural step here is Cobalt-inferred until WA Web ships the matching
 * {@code WAWebNewsletterSavedInterestsSync} module.
 *
 * @implNote NO_WA_BASIS: no WA Web sync handler exists for
 *           {@code "newsletter_saved_interests"}. Only the protobuf shape
 *           {@code SyncActionValue.NewsletterSavedInterestsAction} (field
 *           index {@code 75}, single {@code newsletterSavedInterests: string}
 *           at index {@code 1}, from {@code WAWebProtobufSyncAction.pb}) and
 *           the inline collection mapping
 *           ({@code NEWSLETTER_SAVED_INTERESTS_ACTION -> REGULAR}) are
 *           present in the WA Web snapshot.
 *           {@code WAWebCollectionHandlerActions.ActionHandlers} does not
 *           include a newsletter saved interests handler.
 */
public final class NewsletterSavedInterestsHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code NewsletterSavedInterestsHandler}.
     *
     * @implNote NO_WA_BASIS: WA Web has no sync handler module for
     *           {@code "newsletter_saved_interests"}; the singleton mirrors the
     *           {@code l.default = new u()} pattern used by every other Cobalt
     *           sync handler.
     */
    public static final NewsletterSavedInterestsHandler INSTANCE = new NewsletterSavedInterestsHandler();

    /**
     * Private constructor that enforces the singleton pattern.
     *
     * @implNote NO_WA_BASIS: no WA Web counterpart constructor; mirrors the
     *           Cobalt handler convention of a private no-arg constructor with
     *           a public {@code INSTANCE} field.
     */
    private NewsletterSavedInterestsHandler() {

    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: returns the canonical
     *           {@code "newsletter_saved_interests"} action name declared on
     *           {@link NewsletterSavedInterestsAction#ACTION_NAME}. This name
     *           matches the protobuf field name
     *           {@code newsletterSavedInterestsAction} in
     *           {@code WAWebProtobufSyncAction.pb} but no WA Web
     *           {@code WASyncdConst.Actions} entry references it from a
     *           runtime handler.
     * @return the canonical {@code "newsletter_saved_interests"} string
     */
    @Override
    public String actionName() {
        return NewsletterSavedInterestsAction.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link SyncPatchType#REGULAR} as inferred from the WA Web
     * protobuf-side collection router.
     *
     * @implNote NO_WA_BASIS: WA Web does not declare a sync handler for this
     *           action, but the inline collection router in
     *           {@code WAWebProtobufSyncAction.pb}
     *           ({@code e===c.NEWSLETTER_SAVED_INTERESTS_ACTION?u.REGULAR})
     *           explicitly maps the action id {@code 75} to the
     *           {@code REGULAR} collection. Cobalt mirrors that mapping by
     *           returning {@link SyncPatchType#REGULAR}.
     * @return {@link SyncPatchType#REGULAR}
     */
    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR; // NO_WA_BASIS: matches the WAWebProtobufSyncAction.pb inline collection mapping NEWSLETTER_SAVED_INTERESTS_ACTION -> REGULAR
    }

    /**
     * {@inheritDoc}
     *
     * @implNote NO_WA_BASIS: WA Web has no version constant for this action;
     *           Cobalt defaults to
     *           {@link NewsletterSavedInterestsAction#ACTION_VERSION}
     *           ({@code 1}) matching every other unmigrated sync action
     *           handler.
     * @return the integer version constant declared on the action class
     */
    @Override
    public int version() {
        return NewsletterSavedInterestsAction.ACTION_VERSION;
    }

    /**
     * Applies a newsletter saved interests mutation.
     *
     * <p>Boolean adapter on top of
     * {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}:
     * returns {@code true} only when the underlying result is
     * {@link SyncActionState#SUCCESS}. {@code MALFORMED} and
     * {@code UNSUPPORTED} both map to {@code false}, mirroring the convention
     * used by every other Cobalt sync handler.
     *
     * @implNote ADAPTED: NO_WA_BASIS — there is no WA Web
     *           {@code WAWebNewsletterSavedInterestsSync.applyMutations} to
     *           map to. The boolean collapse mirrors the
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
     * Applies a newsletter saved interests mutation and returns the detailed
     * outcome.
     *
     * <p>The processing pipeline is:
     * <ol>
     *   <li>If the operation is not {@link SyncdOperation#SET}, return
     *       {@link MutationApplicationResult#unsupported()}. Only {@code SET}
     *       mutations are accepted; the action carries a single string token
     *       and there is no semantic for {@code REMOVE}.</li>
     *   <li>Resolve the mutation value to a
     *       {@link NewsletterSavedInterestsAction}; if the value is missing
     *       or of the wrong type, or if {@code newsletterSavedInterests} is
     *       empty, return {@link MutationApplicationResult#malformed()}.</li>
     *   <li>Persist the resolved interests string on the store via
     *       {@code WhatsAppStore.setNewsletterSavedInterests} and return
     *       {@link MutationApplicationResult#success()}.</li>
     * </ol>
     *
     * <p>The store accessors {@code newsletterSavedInterests()} and
     * {@code setNewsletterSavedInterests(...)} already exist on
     * {@code WhatsAppStore} / {@code AbstractWhatsAppStore}; this handler is
     * the sole writer.
     *
     * @implNote NO_WA_BASIS: no WA Web sync handler implements
     *           {@code "newsletter_saved_interests"}. The shape of this
     *           method — only-{@code SET}, single string payload, single
     *           store setter — is inferred from the protobuf
     *           {@code SyncActionValue.NewsletterSavedInterestsAction}
     *           ({@code newsletterSavedInterests: string} at index {@code 1})
     *           and from sibling identifier-style handlers (e.g.
     *           {@code WamoUserIdentifierHandler}, {@code MusicUserIdHandler})
     *           which follow the same {@code single-string -> single store
     *           setter} pattern.
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

        if (!(mutation.value().action().orElse(null) instanceof NewsletterSavedInterestsAction action)
                || action.newsletterSavedInterests().isEmpty()) { // NO_WA_BASIS: newsletterSavedInterests is the only field on the protobuf and is required for any meaningful update
            return MutationApplicationResult.malformed();
        }

        client.store().setNewsletterSavedInterests(action.newsletterSavedInterests().get());
        return MutationApplicationResult.success();
    }
}
