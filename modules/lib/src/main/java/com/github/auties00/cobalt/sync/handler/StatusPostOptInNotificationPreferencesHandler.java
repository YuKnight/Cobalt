package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.device.StatusPostOptInNotificationPreferencesAction;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
/**
 * Handles {@link StatusPostOptInNotificationPreferencesAction} sync mutations
 * ({@code "status_post_opt_in_notification_preferences_action"}).
 *
 * <p>Each mutation carries a single {@code enabled} boolean flag that controls
 * whether the linked WhatsApp account has opted in to receive notifications for
 * status posts. The flag is persisted on the local {@code WhatsAppStore} via
 * {@code setStatusPostOptInNotificationPreferencesEnabled}. Only {@code SET}
 * operations are accepted; any other operation maps to
 * {@link MutationApplicationResult#unsupported()} and a missing or wrong-typed
 * value maps to {@link MutationApplicationResult#malformed()}.
 *
 * <p><b>NO_WA_BASIS:</b> The
 * {@code SyncActionValue.StatusPostOptInNotificationPreferencesAction} protobuf
 * is defined in {@code WAWebProtobufSyncAction.pb} (exported as
 * {@code SyncActionValue$StatusPostOptInNotificationPreferencesActionSpec}) at
 * top-level field index {@code 71} of {@code SyncActionValue}, with one optional
 * field ({@code enabled: bool} at index {@code 1}). The same module's
 * action-name table maps the numeric id {@code 71} to the string
 * {@code "status_post_opt_in_notification_preferences_action"}, and the
 * collection-name resolver explicitly maps
 * {@code STATUS_POST_OPT_IN_NOTIFICATION_PREFERENCES_ACTION -> REGULAR_HIGH}.
 * However, the current WA Web snapshot does <em>not</em> ship a corresponding
 * sync handler module (no {@code WAWebStatusPostOptInNotificationPreferencesSync}
 * or similarly-named module). The action is also absent from
 * {@code WAWebCollectionHandlerActions.ActionHandlers}, the registry of 60+
 * sync handlers consumed by
 * {@code WAWebSyncdGetActionHandler.setActionHandlers}. Consequently WA Web
 * would never dispatch any incoming mutation with this action via
 * {@code WAWebSyncdGetActionHandler.getActionHandler("status_post_opt_in_notification_preferences_action")}
 * (the lookup would return {@code undefined} and the mutation would be
 * skipped). The string literal
 * {@code "status_post_opt_in_notification_preferences_action"} is only present
 * inside {@code WAWebProtobufSyncAction.pb} as the protobuf field name; no
 * other WA Web module references it.
 *
 * <p>The Cobalt handler is a forward-looking implementation: it follows the
 * Cobalt sync handler conventions used by every other registered handler
 * (singleton, {@code applyMutation} producing a typed
 * {@link MutationApplicationResult}, eager store update on {@code SET}). The
 * shape of the handler — only-{@code SET}, single-boolean payload, single
 * store setter — is inferred directly from the protobuf shape (one optional
 * boolean field) and from sibling boolean-flag handlers such as
 * {@code PrivacySettingChannelsPersonalisedRecommendationHandler} and
 * {@code DisableLinkPreviewsHandler} which follow the same
 * {@code single-boolean -> single store setter} pattern. Every behavioural
 * step here is Cobalt-inferred until WA Web ships the matching sync module.
 */
public final class StatusPostOptInNotificationPreferencesHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of
     * {@code StatusPostOptInNotificationPreferencesHandler}.
     */
    public static final StatusPostOptInNotificationPreferencesHandler INSTANCE =
            new StatusPostOptInNotificationPreferencesHandler();

    /**
     * Private constructor that enforces the singleton pattern.
     */
    private StatusPostOptInNotificationPreferencesHandler() {

    }

    /**
     * {@inheritDoc}
     * @return the canonical
     *         {@code "status_post_opt_in_notification_preferences_action"}
     *         string
     */
    @Override
    public String actionName() {
        return StatusPostOptInNotificationPreferencesAction.ACTION_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@link SyncPatchType#REGULAR_HIGH}, matching the
     * {@code STATUS_POST_OPT_IN_NOTIFICATION_PREFERENCES_ACTION -> REGULAR_HIGH}
     * mapping declared in {@code WAWebProtobufSyncAction.pb}.
     * @return {@link SyncPatchType#REGULAR_HIGH}
     */
    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR_HIGH;
    }

    /**
     * {@inheritDoc}
     * @return the integer version constant declared on the action class
     */
    @Override
    public int version() {
        return StatusPostOptInNotificationPreferencesAction.ACTION_VERSION;
    }

    /**
     * Applies a status post opt-in notification preferences mutation and
     * returns the detailed outcome.
     *
     * <p>The processing pipeline is:
     * <ol>
     *   <li>If the operation is not {@link SyncdOperation#SET}, return
     *       {@link MutationApplicationResult#unsupported()}. Only {@code SET}
     *       mutations are accepted; the action carries a single boolean
     *       opt-in flag and there is no semantic for {@code REMOVE}.</li>
     *   <li>Resolve the mutation value to a
     *       {@link StatusPostOptInNotificationPreferencesAction}; if the value
     *       is missing or of the wrong type, return
     *       {@link MutationApplicationResult#malformed()}.</li>
     *   <li>Persist the resolved opt-in boolean on the store via
     *       {@code WhatsAppStore.setStatusPostOptInNotificationPreferencesEnabled}
     *       and return {@link MutationApplicationResult#success()}.</li>
     * </ol>
     *
     * <p>The store accessors
     * {@code statusPostOptInNotificationPreferencesEnabled()} and
     * {@code setStatusPostOptInNotificationPreferencesEnabled(...)} already
     * exist on {@code WhatsAppStore} / {@code AbstractWhatsAppStore}; this
     * handler is the sole writer.
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

        if (!(mutation.value().action().orElse(null) instanceof StatusPostOptInNotificationPreferencesAction action)) {
            return MutationApplicationResult.malformed();
        }

        client.store().setStatusPostOptInNotificationPreferencesEnabled(action.enabled());
        return MutationApplicationResult.success();
    }
}
