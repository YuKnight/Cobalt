package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.setting.ChatLockSettings;
import com.github.auties00.cobalt.model.setting.UserPassword;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles chat lock settings sync actions.
 *
 * <p>This handler processes mutations related to the global chat lock settings
 * (e.g., whether locked chats are hidden, the secret code configuration).
 * It validates each mutation's {@link ChatLockSettings} value, including
 * thorough validation of the secret code's transformer, encoding, data,
 * and arguments (iterations and salt).
 *
 * <p>Index format: {@code ["setting_chatLock"]}
 *
 * @implNote WAWebChatLockSettingsSync — singleton instance exported as {@code default},
 *           extends {@code AccountSyncdActionBase} with collection {@code RegularLow},
 *           version {@code 7}, action {@code "setting_chatLock"}
 */
public final class ChatLockSettingsHandler implements WebAppStateActionHandler {
    /**
     * Logger for chat lock settings sync handler.
     *
     * @implNote ADAPTED: WAWebChatLockSettingsSync uses WALogger; Cobalt uses java.util.logging
     */
    private static final Logger LOGGER = Logger.getLogger(ChatLockSettingsHandler.class.getName()); // ADAPTED: WAWebChatLockSettingsSync — WALogger

    /**
     * The singleton instance of {@code ChatLockSettingsHandler}.
     *
     * @implNote WAWebChatLockSettingsSync.default — the module exports a singleton
     *           {@code new f()} as its default export
     */
    public static final ChatLockSettingsHandler INSTANCE = new ChatLockSettingsHandler();

    /**
     * Constructs a new {@code ChatLockSettingsHandler}.
     *
     * @implNote WAWebChatLockSettingsSync — constructor sets
     *           {@code collectionName = CollectionName.RegularLow}
     */
    private ChatLockSettingsHandler() {
        // WAWebChatLockSettingsSync constructor
    }

    /**
     * Returns the action name for chat lock settings.
     *
     * @implNote WAWebChatLockSettingsSync.getAction — returns
     *           {@code WASyncdConst.Actions.ChatLockSettings} which is {@code "setting_chatLock"}
     * @return the action name {@code "setting_chatLock"}
     */
    @Override
    public String actionName() {
        return ChatLockSettings.ACTION_NAME; // WAWebChatLockSettingsSync.getAction
    }

    /**
     * Returns the collection name for chat lock settings.
     *
     * @implNote WAWebChatLockSettingsSync — constructor sets
     *           {@code collectionName = WASyncdConst.CollectionName.RegularLow}
     * @return {@link SyncPatchType#REGULAR_LOW}
     */
    @Override
    public SyncPatchType collectionName() {
        return ChatLockSettings.COLLECTION_NAME; // WAWebChatLockSettingsSync.collectionName = RegularLow
    }

    /**
     * Returns the mutation format version for chat lock settings.
     *
     * @implNote WAWebChatLockSettingsSync.getVersion — returns {@code 7}
     * @return the version number {@code 7}
     */
    @Override
    public int version() {
        return ChatLockSettings.ACTION_VERSION; // WAWebChatLockSettingsSync.getVersion
    }

    /**
     * Applies a single chat lock settings mutation.
     *
     * @implNote WAWebChatLockSettingsSync.applyMutations — per-mutation logic
     *           extracted for single-mutation application. Validates operation
     *           is SET, checks {@code chatLockSettings} is present, validates
     *           {@code secretCode} if present, and persists the settings to
     *           the store.
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == com.github.auties00.cobalt.model.sync.SyncActionState.SUCCESS; // WAWebChatLockSettingsSync.applyMutations
    }

    /**
     * Applies a single chat lock settings mutation and returns the detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebChatLockSettingsSync.applyMutations}: validates
     * that the operation is SET, that the sync action value contains a non-null
     * {@code chatLockSettings}, and that any present {@code secretCode} has valid
     * transformer, encoding, transformed data, and transformer arguments (iterations
     * and salt).
     *
     * <p>The WA Web null check on {@code hideLockedChats} is classified as ADAPTED:
     * Cobalt's {@link ChatLockSettings#hideLockedChats()} accessor coalesces
     * {@code null} to {@code false} per project convention for nullable Boolean
     * fields, making the null case indistinguishable from an explicit {@code false}
     * value. In practice, valid mutations from WhatsApp servers always include
     * this field.
     *
     * @implNote WAWebChatLockSettingsSync.applyMutations — single-mutation path;
     *           saves immediately since there is no batch to accumulate across
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed mutation application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        if (mutation.operation() != SyncdOperation.SET) { // WAWebChatLockSettingsSync.applyMutations: if (e.operation !== "set")
            return MutationApplicationResult.unsupported(); // WAWebChatLockSettingsSync.applyMutations: {actionState: SyncActionState.Unsupported}
        }

        if (!(mutation.value().action().orElse(null) instanceof ChatLockSettings settings)) { // WAWebChatLockSettingsSync.applyMutations: var t = e.value.chatLockSettings; if (t == null)
            return malformedActionValue(); // WAWebChatLockSettingsSync.applyMutations: malformedActionValue(n.collectionName)
        }

        // ADAPTED: WAWebChatLockSettingsSync.applyMutations: var s = t.hideLockedChats; if (s == null) return malformed
        // Cobalt's hideLockedChats() coalesces null to false per nullable Boolean convention;
        // the null-vs-false distinction is not observable through the public accessor.

        if (!isSecretCodeValid(settings)) { // WAWebChatLockSettingsSync.applyMutations: secretCode validation block
            return malformedActionValue(); // WAWebChatLockSettingsSync.applyMutations: malformedActionValue(n.collectionName)
        }

        client.store().setChatLockSettings(settings); // WAWebChatLockSettingsSync.applyMutations: getChatLockSettings().updateAndSave(r)
        return MutationApplicationResult.success(); // WAWebChatLockSettingsSync.applyMutations: {actionState: SyncActionState.Success}
    }

    /**
     * Applies a batch of chat lock settings mutations.
     *
     * <p>Per WhatsApp Web {@code WAWebChatLockSettingsSync.applyMutations}: iterates
     * all mutations, accumulating the last valid {@code chatLockSettings} value from
     * SET operations. Each mutation is individually validated for well-formedness
     * (non-null chatLockSettings, valid secretCode if present). After iteration,
     * persists the accumulated value once via
     * {@code getChatLockSettings().updateAndSave(r)}.
     *
     * <p>If no valid mutation is found, logs a warning matching WA Web's
     * {@code "ChatLockSettingsSync: mutations parse failed"} message.
     *
     * @implNote WAWebChatLockSettingsSync.applyMutations — the main entry point for
     *           batch mutation application. Accumulates {@code r} across all mutations
     *           and saves once at the end.
     * @param client    the WhatsApp client instance
     * @param mutations the batch of mutations to apply
     * @return a list of results parallel to the input
     */
    @Override
    public List<MutationApplicationResult> applyMutationBatchResults(WhatsAppClient client, List<DecryptedMutation.Trusted> mutations) {
        if (mutations.isEmpty()) {
            return List.of(); // NO_WA_BASIS — defensive empty check
        }

        ChatLockSettings lastValid = null; // WAWebChatLockSettingsSync.applyMutations: var r
        var results = new ArrayList<MutationApplicationResult>(mutations.size()); // WAWebChatLockSettingsSync.applyMutations: t.map(function(e) {...})
        for (var mutation : mutations) { // WAWebChatLockSettingsSync.applyMutations: t.map(function(e) {...})
            if (mutation.operation() != SyncdOperation.SET) { // WAWebChatLockSettingsSync.applyMutations: if (e.operation !== "set")
                results.add(MutationApplicationResult.unsupported()); // WAWebChatLockSettingsSync.applyMutations: {actionState: SyncActionState.Unsupported}
                continue;
            }

            if (!(mutation.value().action().orElse(null) instanceof ChatLockSettings settings)) { // WAWebChatLockSettingsSync.applyMutations: var t = e.value.chatLockSettings; if (t == null)
                results.add(malformedActionValue()); // WAWebChatLockSettingsSync.applyMutations: malformedActionValue(n.collectionName)
                continue;
            }

            // ADAPTED: WAWebChatLockSettingsSync.applyMutations: var s = t.hideLockedChats; if (s == null) return malformed
            // Cobalt's hideLockedChats() coalesces null to false per nullable Boolean convention;
            // the null-vs-false distinction is not observable through the public accessor.

            if (!isSecretCodeValid(settings)) { // WAWebChatLockSettingsSync.applyMutations: secretCode validation
                results.add(malformedActionValue()); // WAWebChatLockSettingsSync.applyMutations: malformedActionValue(n.collectionName)
                continue;
            }

            lastValid = settings; // WAWebChatLockSettingsSync.applyMutations: r = {hideLockedChats: s, secretCode: ...}
            results.add(MutationApplicationResult.success()); // WAWebChatLockSettingsSync.applyMutations: {actionState: SyncActionState.Success}
        }

        if (lastValid != null) { // WAWebChatLockSettingsSync.applyMutations: if (r != null)
            client.store().setChatLockSettings(lastValid); // WAWebChatLockSettingsSync.applyMutations: getChatLockSettings().updateAndSave(r)
        } else {
            LOGGER.warning("ChatLockSettingsSync: mutations parse failed"); // WAWebChatLockSettingsSync.applyMutations: WALogger.WARN("ChatLockSettingsSync: mutations parse failed")
        }

        return results;
    }

    /**
     * Validates the secret code within a {@link ChatLockSettings} instance.
     *
     * <p>Per WhatsApp Web {@code WAWebChatLockSettingsSync.applyMutations}: when
     * {@code secretCode} is non-null, the following must all hold:
     * <ul>
     *   <li>{@code encoding}, {@code transformedData}, {@code transformer}, and
     *       {@code transformerArg} must all be non-null/non-empty</li>
     *   <li>{@code transformer} must equal
     *       {@link UserPassword.Transformer#PBKDF2_HMAC_SHA512}</li>
     *   <li>The {@code transformerArg} list must contain entries with keys
     *       {@code "iterations"} and {@code "salt"}, each with a non-null value
     *       of the appropriate type ({@code asUnsignedInteger} for iterations,
     *       {@code asBlob} for salt)</li>
     * </ul>
     *
     * <p>If {@code secretCode} is absent, no validation is needed and the method
     * returns {@code true}.
     *
     * @implNote WAWebChatLockSettingsSync.applyMutations — inline secretCode
     *           validation block within the per-mutation map callback
     * @param settings the chat lock settings to validate
     * @return {@code true} if the secret code is absent or well-formed,
     *         {@code false} if malformed
     */
    private boolean isSecretCodeValid(ChatLockSettings settings) {
        var secretCode = settings.secretCode(); // WAWebChatLockSettingsSync.applyMutations: var u = t.secretCode
        if (secretCode.isEmpty()) { // WAWebChatLockSettingsSync.applyMutations: if (u != null) {...} — null secretCode is valid
            return true;
        }

        var password = secretCode.get();

        // WAWebChatLockSettingsSync.applyMutations: var c = u.encoding, d = u.transformedData, m = u.transformer, p = u.transformerArg
        var encoding = password.encoding(); // WAWebChatLockSettingsSync.applyMutations: var c = u.encoding
        var transformedData = password.transformedData(); // WAWebChatLockSettingsSync.applyMutations: var d = u.transformedData
        var transformer = password.transformer(); // WAWebChatLockSettingsSync.applyMutations: var m = u.transformer
        var transformerArgs = password.transformerArg(); // WAWebChatLockSettingsSync.applyMutations: var p = u.transformerArg

        // WAWebChatLockSettingsSync.applyMutations: if (p == null || m == null || d == null || c == null) return malformedActionValue
        if (transformerArgs.isEmpty() || transformer.isEmpty() || transformedData.isEmpty() || encoding.isEmpty()) {
            return false;
        }

        // WAWebChatLockSettingsSync.applyMutations: if (m !== PBKDF2_HMAC_SHA512) return malformedActionValue
        if (transformer.get() != UserPassword.Transformer.PBKDF2_HMAC_SHA512) {
            return false;
        }

        // WAWebChatLockSettingsSync.applyMutations: var _ = p.reduce(function(e, t) {
        //   return t.value == null || (t.key === "iterations" ? e.iterations = t.value.asUnsignedInteger
        //                              : t.key === "salt" && (e.salt = t.value.asBlob)), e
        // }, {})
        var hasIterations = false; // WAWebChatLockSettingsSync.applyMutations: _.iterations
        var hasSalt = false; // WAWebChatLockSettingsSync.applyMutations: _.salt
        for (var arg : transformerArgs) { // WAWebChatLockSettingsSync.applyMutations: p.reduce
            var value = arg.value().orElse(null); // WAWebChatLockSettingsSync.applyMutations: t.value
            if (value == null) { // WAWebChatLockSettingsSync.applyMutations: if (t.value == null) continue (via return e)
                continue;
            }
            var key = arg.key().orElse(null); // WAWebChatLockSettingsSync.applyMutations: t.key
            if ("iterations".equals(key)) { // WAWebChatLockSettingsSync.applyMutations: t.key === "iterations"
                // WAWebChatLockSettingsSync.applyMutations: e.iterations = t.value.asUnsignedInteger
                // WA Web reads Value.asUnsignedInteger directly; Cobalt uses the oneof accessor
                // and checks the variant type to match the same semantics
                if (value.value().orElse(null) instanceof UserPassword.TransformerArg.ValueSpec.AsUnsignedInteger ui // ADAPTED: WAWebChatLockSettingsSync.applyMutations
                        && ui.asUnsignedInteger() != null) {
                    hasIterations = true;
                }
            } else if ("salt".equals(key)) { // WAWebChatLockSettingsSync.applyMutations: t.key === "salt"
                // WAWebChatLockSettingsSync.applyMutations: e.salt = t.value.asBlob
                if (value.value().orElse(null) instanceof UserPassword.TransformerArg.ValueSpec.AsBlob blob // ADAPTED: WAWebChatLockSettingsSync.applyMutations
                        && blob.asBlob() != null) {
                    hasSalt = true;
                }
            }
        }

        // WAWebChatLockSettingsSync.applyMutations: if (_.iterations == null || _.salt == null) return malformedActionValue
        return hasIterations && hasSalt;
    }
}
