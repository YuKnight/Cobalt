package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.NotificationActivitySettingAction;
import com.github.auties00.cobalt.model.sync.action.setting.NotificationActivitySettingActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Applies {@code notificationActivitySetting} mutations decoded from app state sync.
 *
 * <p>Handles the {@code SyncActionValue.NotificationActivitySettingAction} sync
 * action in the {@link SyncPatchType#REGULAR} collection. A mutation of this
 * type carries the user's per-account notification activity preference, encoded
 * as a {@link NotificationActivitySettingAction.NotificationActivitySetting}
 * enum value: {@code DEFAULT_ALL_MESSAGES}, {@code ALL_MESSAGES},
 * {@code HIGHLIGHTS} or {@code DEFAULT_HIGHLIGHTS}. The setting controls which
 * incoming messages should produce a system notification on the paired devices.
 *
 * <h2>Forward-looking handler — no current WA Web counterpart</h2>
 *
 * <p>This handler has <b>no concrete WA Web sync module</b>. WA Web defines
 * the {@code NotificationActivitySettingAction} protobuf schema (action index
 * {@code 60}, action name {@code "notificationActivitySetting"}, collection
 * {@code REGULAR}, version {@code 1}) inside {@code WAWebProtobufSyncAction.pb},
 * but it never registers a {@code WAWebNotificationActivitySettingSync} module
 * in {@code WAWebCollectionHandlerActions.ActionHandlers}. As a result, when
 * such a mutation reaches WA Web, {@code WAWebSyncdGetActionHandler.getActionHandler}
 * returns {@code undefined} and the mutation is dropped by the upstream
 * dispatcher as unsupported.
 *
 * <p>Cobalt anticipates a future WA Web release that adds the missing handler
 * by implementing the obvious shape derived from the protobuf metadata:
 * <ul>
 *   <li>{@code SET}-only operation,</li>
 *   <li>routed via {@link NotificationActivitySettingAction#ACTION_NAME},</li>
 *   <li>persisted to {@link com.github.auties00.cobalt.store.WhatsAppStore#setNotificationActivitySetting},</li>
 *   <li>stored in {@link SyncPatchType#REGULAR} at version
 *       {@link NotificationActivitySettingAction#ACTION_VERSION}.</li>
 * </ul>
 *
 * <p>The metadata mirrors {@code WAWebProtobufSyncAction.pb}'s
 * {@code getMutationProps$CollectionName} branch
 * {@code e === c.NOTIFICATION_ACTIVITY_SETTING_ACTION ? u.REGULAR}, so when WA
 * Web ships the real {@code WAWebNotificationActivitySettingSync} this file
 * should already be wire-compatible.
 */
public final class NotificationActivitySettingHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of this handler.
     *
     * <p>Mirrors the WA Web pattern of exposing each sync handler as a
     * module-level singleton ({@code var _ = new p; l.default = _}). Even
     * though WA Web has no concrete module for this action, sibling handlers
     * such as {@code WAWebLocaleSettingSync} and
     * {@code WAWebPushNameSync} all follow this convention, and Cobalt's
     * dispatcher expects a single shared instance per handler class.
     */
    public static final NotificationActivitySettingHandler INSTANCE = new NotificationActivitySettingHandler();

    /**
     * Creates a new {@code NotificationActivitySettingHandler}.
     *
     * <p>The constructor is private because callers should always go through
     * {@link #INSTANCE}, matching the WA Web module-level singleton pattern
     * used by other sync handlers.
     */
    private NotificationActivitySettingHandler() {

    }

    /**
     * Returns the action name this handler processes.
     * @return the constant {@link NotificationActivitySettingAction#ACTION_NAME},
     *         always {@code "notificationActivitySetting"}
     */
    @Override
    public String actionName() {
        return NotificationActivitySettingAction.ACTION_NAME;
    }

    /**
     * Returns the sync collection this handler's action belongs to.
     *
     * <p>Per {@code WAWebProtobufSyncAction.pb} the
     * {@code getMutationProps$CollectionName} resolver branches on
     * {@code e === c.NOTIFICATION_ACTIVITY_SETTING_ACTION} and returns
     * {@code u.REGULAR}, so the mutation is stored in the regular-priority
     * sync collection.
     * @return {@link SyncPatchType#REGULAR}
     */
    @Override
    public SyncPatchType collectionName() {
        return SyncPatchType.REGULAR;
    }

    /**
     * Returns the mutation format version this handler supports.
     *
     * <p>WA Web has no concrete sync module for this action, so there is no
     * {@code getVersion()} method to inspect. The protobuf shape only defines
     * a single field ({@code notificationActivitySetting} at index 1), so
     * Cobalt declares the initial version {@code 1} as a forward-looking
     * default. The actual value should be re-checked when WA Web ships the
     * matching {@code WAWebNotificationActivitySettingSync} module.
     * @return the constant {@link NotificationActivitySettingAction#ACTION_VERSION},
     *         always {@code 1}
     */
    @Override
    public int version() {
        return NotificationActivitySettingAction.ACTION_VERSION;
    }

    /**
     * Applies a single decoded notification activity setting mutation and
     * returns the detailed result.
     *
     * <p>WA Web has no concrete handler to mirror, so this implementation
     * follows the canonical shape used by every other sibling sync handler
     * (e.g. {@code WAWebLocaleSettingSync.applyMutations}):
     * <ol>
     *   <li><b>Operation filter</b> — only {@code SET} mutations are processed;
     *       any other operation falls through to
     *       {@link MutationApplicationResult#unsupported()}, mirroring the
     *       trailing {@code p++; return {actionState: Unsupported}} branch in
     *       sibling handlers.</li>
     *   <li><b>Action type and payload check</b> — the mutation value must
     *       decode to a {@link NotificationActivitySettingAction} with a
     *       non-empty {@code notificationActivitySetting} enum field. Sibling
     *       handlers express the same guard as
     *       {@code var n = e.value, a = n.someAction; if (!a) { i++; return malformedActionValue(this.collectionName) }}.
     *       Cobalt collapses the equivalent
     *       {@link SyncdIndexUtils#malformedActionValue}
     *       contract into {@link MutationApplicationResult#malformed()}.</li>
     *   <li><b>Apply the new setting</b> — persists the decoded enum into
     *       {@link com.github.auties00.cobalt.store.WhatsAppStore#setNotificationActivitySetting}.
     *       Since WA Web does not yet have an apply path, there is no UI/IPC
     *       call to mirror; only the store side-effect is performed.</li>
     *   <li><b>Success</b> — returns {@link MutationApplicationResult#success()}.</li>
     * </ol>
     *
     * <p>Sibling WA Web handlers also wrap the per-mutation body in a
     * {@code try/catch} that swallows any exception and returns
     * {@code {actionState: Failed}}. In Cobalt, exceptions are allowed to
     * propagate and the configured
     * {@code WhatsAppClientErrorHandler} decides recovery, per Cobalt's
     * pluggable error model.
     * @param client   the WhatsApp client the mutation is being applied to
     * @param mutation the trusted, decoded mutation to apply
     * @return {@link MutationApplicationResult#unsupported()} for non-{@code SET}
     *         operations; {@link MutationApplicationResult#malformed()} if the
     *         decoded action is not a {@link NotificationActivitySettingAction}
     *         or has no {@code notificationActivitySetting} field;
     *         {@link MutationApplicationResult#success()} otherwise
     */
    @Override
    public MutationApplicationResult applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // ADAPTED: sibling WAWeb*Sync.applyMutations: if (e.operation === "set") { ... } p++; return {actionState: Unsupported}
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        // ADAPTED: sibling WAWeb*Sync.applyMutations: var n = e.value, a = n.notificationActivitySetting; if (!a) { i++; return malformedActionValue(this.collectionName) }
        if (!(mutation.value().action().orElse(null) instanceof NotificationActivitySettingAction action)
                || action.notificationActivitySetting().isEmpty()) {
            return MutationApplicationResult.malformed();
        }

        // NO_WA_BASIS: WA Web has no apply path; Cobalt persists the decoded enum into the store as the obvious side-effect
        client.store().setNotificationActivitySetting(action.notificationActivitySetting().get());

        // ADAPTED: sibling WAWeb*Sync.applyMutations: return {actionState: Success}
        return MutationApplicationResult.success();
    }

    /**
     * Builds a pending {@code notificationActivitySetting} mutation carrying
     * the given notification activity preference.
     *
     * <p>NO_WA_BASIS: WA Web has no outgoing helper for this action; the shape
     * follows {@code WAWebSyncdActionUtils.buildPendingMutation} as used by
     * every other sibling {@code AccountSyncdActionBase} subclass. Cobalt
     * surfaces the helper so the public
     * {@code WhatsAppClient.changeNotificationActivity} setter can build a single
     * mutation without hand-rolling the protobuf wrapping.
     * @param timestamp the mutation timestamp
     * @param setting   the new {@link NotificationActivitySettingAction.NotificationActivitySetting}
     * @return a pending mutation carrying the {@code notificationActivitySetting}
     *         action
     * @throws NullPointerException if {@code timestamp} or {@code setting} is {@code null}
     */
    public SyncPendingMutation getNotificationActivityMutation(Instant timestamp, NotificationActivitySettingAction.NotificationActivitySetting setting) {
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        Objects.requireNonNull(setting, "setting cannot be null");
        var action = new NotificationActivitySettingActionBuilder()
                .notificationActivitySetting(setting)
                .build();
        var value = new SyncActionValueBuilder()
                .timestamp(timestamp)
                .notificationActivitySettingAction(action)
                .build();
        var index = JSON.toJSONString(List.of(actionName()));
        var pending = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET,
                timestamp,
                version()
        );
        return new SyncPendingMutation(pending, 0);
    }
}
