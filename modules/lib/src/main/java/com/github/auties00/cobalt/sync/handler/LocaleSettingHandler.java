package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncActionValueBuilder;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.setting.LocaleSetting;
import com.github.auties00.cobalt.model.sync.action.setting.LocaleSettingBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.SyncPendingMutation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Applies {@code setting_locale} mutations decoded from app state sync.
 *
 * <p>Handles the {@code LocaleSetting} sync action in the
 * {@link SyncPatchType#CRITICAL_BLOCK} collection. A mutation of this type
 * carries the user's preferred locale (e.g. {@code "en_US"}) as chosen on
 * the paired companion device, and instructs every other linked client to
 * update its own UI locale to match.
 *
 * <p>On WhatsApp Web the handler forwards the new locale to the frontend
 * l10n layer via {@code WAWebBackendApi.frontendSendAndReceive("setLocale", ...)}.
 * Cobalt does not ship a UI layer, so instead it:
 * <ol>
 *   <li>writes the new locale into {@link com.github.auties00.cobalt.store.WhatsAppStore},</li>
 *   <li>dispatches a {@code onLocaleChanged} event to every registered
 *       {@link com.github.auties00.cobalt.client.WhatsAppClientListener} on a
 *       virtual thread.</li>
 * </ol>
 *
 * <p>As on WA Web, the mutation is skipped entirely on the Windows hybrid
 * client because locale management is delegated to the host operating
 * system there.
 *
 * @implNote WAWebLocaleSettingSync.default — concrete subclass of
 *           {@code WAWebSyncdAction.AccountSyncdActionBase} with
 *           {@code collectionName = CriticalBlock}, {@code getVersion() = 3},
 *           {@code getAction() = Actions.LocaleSetting} and
 *           {@code applyMutations()} implementing the per-mutation locale apply.
 */
@WhatsAppWebModule(moduleName = "WAWebLocaleSettingSync")
public final class LocaleSettingHandler implements WebAppStateActionHandler {
    /**
     * Singleton instance of this handler.
     *
     * <p>WA Web instantiates the handler exactly once at module evaluation
     * time via {@code var _ = new p; l.default = _;}. Cobalt mirrors that by
     * exposing a module-level constant.
     *
     * @implNote WAWebLocaleSettingSync — {@code var _ = new p; l.default = _}
     */
    @WhatsAppWebExport(moduleName = "WAWebLocaleSettingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    public static final LocaleSettingHandler INSTANCE = new LocaleSettingHandler();

    /**
     * Creates a new {@code LocaleSettingHandler}.
     *
     * <p>The constructor is private because callers should always go through
     * {@link #INSTANCE}, matching the WA Web module-level singleton.
     *
     * @implNote WAWebLocaleSettingSync — hidden {@code function a()} constructor
     *           that only initializes {@code this.collectionName = CriticalBlock}
     */
    @WhatsAppWebExport(moduleName = "WAWebLocaleSettingSync", exports = "default", adaptation = WhatsAppAdaptation.ADAPTED)
    private LocaleSettingHandler() {

    }

    /**
     * Returns the action name this handler processes.
     *
     * @implNote WAWebLocaleSettingSync.getAction — returns
     *           {@code WASyncdConst.Actions.LocaleSetting}, which resolves to
     *           the string {@code "setting_locale"}
     * @return the constant {@link LocaleSetting#ACTION_NAME}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLocaleSettingSync", exports = "getAction", adaptation = WhatsAppAdaptation.DIRECT)
    public String actionName() {
        return LocaleSetting.ACTION_NAME; // WAWebLocaleSettingSync.getAction -> Actions.LocaleSetting
    }

    /**
     * Returns the sync collection this handler's action belongs to.
     *
     * <p>On WA Web this is set on the prototype inside the constructor as
     * {@code this.collectionName = CollectionName.CriticalBlock}.
     *
     * @implNote WAWebLocaleSettingSync — {@code this.collectionName = WASyncdConst.CollectionName.CriticalBlock}
     * @return the constant {@link LocaleSetting#COLLECTION_NAME}, always
     *         {@link SyncPatchType#CRITICAL_BLOCK}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLocaleSettingSync", exports = "collectionName", adaptation = WhatsAppAdaptation.DIRECT)
    public SyncPatchType collectionName() {
        return LocaleSetting.COLLECTION_NAME; // WAWebLocaleSettingSync -> CollectionName.CriticalBlock
    }

    /**
     * Returns the mutation format version this handler supports.
     *
     * @implNote WAWebLocaleSettingSync.getVersion — {@code return 3}
     * @return the constant {@link LocaleSetting#ACTION_VERSION}, always {@code 3}
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLocaleSettingSync", exports = "getVersion", adaptation = WhatsAppAdaptation.DIRECT)
    public int version() {
        return LocaleSetting.ACTION_VERSION; // WAWebLocaleSettingSync.getVersion -> 3
    }

    /**
     * Applies a single decoded locale mutation.
     *
     * <p>Thin bridge over {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * that reduces the richer {@link MutationApplicationResult} state to a
     * legacy boolean: {@code true} only for {@link SyncActionState#SUCCESS},
     * {@code false} for {@code MALFORMED}, {@code UNSUPPORTED}, {@code SKIPPED}
     * and {@code FAILED}.
     *
     * @implNote ADAPTED: WAWebLocaleSettingSync.applyMutations — the WA Web
     *           inner async callback returns a {@code SyncActionState}; Cobalt
     *           exposes both a boolean and a richer result through two methods
     * @param client   the WhatsApp client the mutation is being applied to
     * @param mutation the trusted, decoded mutation to apply
     * @return {@code true} if the apply succeeded, {@code false} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLocaleSettingSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // ADAPTED: WAWebLocaleSettingSync.applyMutations
    }

    /**
     * Applies a single decoded locale mutation and returns the detailed result.
     *
     * <p>This method implements the body of the WA Web per-mutation callback
     * passed to {@code Promise.all(a.map(async e => { ... }))} inside
     * {@code applyMutations(a)}. The order of checks mirrors WA Web exactly:
     * <ol>
     *   <li><b>Windows hybrid short-circuit</b> — WA Web uses
     *       {@code WAWebEnvironment.isWindows} (the {@code win_hybrid} runtime
     *       gate) to skip the mutation entirely. Cobalt uses the paired device
     *       platform, matching {@code SettingsSyncHandler}'s convention of
     *       treating {@link ClientPlatformType#WINDOWS} as the Windows hybrid
     *       client.</li>
     *   <li><b>Operation filter</b> — WA Web falls through to the final
     *       {@code p++; return {actionState: Unsupported};} branch for any
     *       operation other than {@code "set"}. Cobalt returns
     *       {@link MutationApplicationResult#unsupported()}.</li>
     *   <li><b>Missing localeSetting payload</b> — WA Web reads
     *       {@code var n = e.value, a = n.localeSetting; if (!a) { i++;
     *       return malformedActionValue(this.collectionName); }}. Cobalt
     *       checks that the decoded action is a {@link LocaleSetting} and
     *       returns {@link MutationApplicationResult#malformed()} otherwise.</li>
     *   <li><b>Null locale field</b> — WA Web: {@code var s = a.locale;
     *       if (s == null) { l++; return {actionState: Skipped}; }}. Cobalt
     *       returns {@link MutationApplicationResult#skipped()}.</li>
     *   <li><b>Apply the new locale</b> — WA Web awaits
     *       {@code WAWebBackendApi.frontendSendAndReceive("setLocale",
     *       {locale: s, priority: L10N_PRIORITY.PHONE, reload: false})}
     *       to push the new locale into the l10n subsystem of the web UI.
     *       Cobalt has no frontend, so it persists the locale into
     *       {@link com.github.auties00.cobalt.store.WhatsAppStore#setLocale(String)}
     *       and notifies listeners via {@code onLocaleChanged(client, oldLocale, newLocale)}.</li>
     *   <li><b>Success</b> — returns {@link MutationApplicationResult#success()}.</li>
     * </ol>
     *
     * <p>WA Web also increments local telemetry counters ({@code i}, {@code l},
     * {@code p}), appends each applied locale to a bounded {@code _} array
     * ({@code if (_.length < 3) _.push(s)}), and emits {@code WALogger.LOG}/
     * {@code WALogger.WARN} messages at the end of the batch. These are
     * intentionally omitted in Cobalt as telemetry/logging noise with no
     * behavioral impact.
     *
     * <p>WA Web wraps the whole per-mutation body in a {@code try/catch} that
     * swallows any exception and returns {@code {actionState: Failed}}. In
     * Cobalt, exceptions are allowed to propagate and the configured
     * {@code WhatsAppClientErrorHandler} decides recovery, per Cobalt's
     * pluggable error model.
     *
     * @implNote WAWebLocaleSettingSync.applyMutations — per-mutation async body
     * @param client   the WhatsApp client the mutation is being applied to
     * @param mutation the trusted, decoded mutation to apply
     * @return {@link MutationApplicationResult#skipped()} on Windows hybrid or
     *         null locale; {@link MutationApplicationResult#unsupported()} for
     *         non-{@code SET} operations; {@link MutationApplicationResult#malformed()}
     *         if the decoded action is not a {@link LocaleSetting};
     *         {@link MutationApplicationResult#success()} otherwise
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebLocaleSettingSync", exports = "applyMutations", adaptation = WhatsAppAdaptation.ADAPTED)
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // WAWebLocaleSettingSync.applyMutations: if (WAWebEnvironment.isWindows) return {actionState: Skipped}
        if (client.store().device() != null && client.store().device().platform() == ClientPlatformType.WINDOWS) {
            return MutationApplicationResult.skipped();
        }

        // WAWebLocaleSettingSync.applyMutations: if (e.operation === "set") { ... } p++; return {actionState: Unsupported}
        if (mutation.operation() != SyncdOperation.SET) {
            return MutationApplicationResult.unsupported();
        }

        // WAWebLocaleSettingSync.applyMutations: var n = e.value, a = n.localeSetting; if (!a) { i++; return malformedActionValue(this.collectionName) }
        if (!(mutation.value().action().orElse(null) instanceof LocaleSetting setting)) {
            return MutationApplicationResult.malformed();
        }

        // WAWebLocaleSettingSync.applyMutations: var s = a.locale; if (s == null) { l++; return {actionState: Skipped} }
        var newLocale = setting.locale().orElse(null);
        if (newLocale == null) {
            return MutationApplicationResult.skipped();
        }

        // ADAPTED: WAWebLocaleSettingSync.applyMutations:
        //   yield WAWebBackendApi.frontendSendAndReceive("setLocale",
        //       {locale: s, priority: L10N_PRIORITY.PHONE, reload: false});
        // Cobalt has no frontend l10n layer, so the locale is persisted into
        // the store and broadcast to application listeners instead.
        var oldLocale = client.store().locale().orElse(null);
        client.store().setLocale(newLocale); // ADAPTED: WAWebLocaleSettingSync.applyMutations -> frontendSendAndReceive("setLocale", ...)
        for (var listener : client.store().listeners()) {
            Thread.startVirtualThread(() -> listener.onLocaleChanged(client, oldLocale, newLocale)); // ADAPTED: WAWebLocaleSettingSync -> notify Cobalt listeners instead of frontend IPC
        }

        // NO_WA_BASIS: the following WA Web telemetry is intentionally dropped:
        //   - i/l/p counters and the trailing WALogger.LOG/WARN calls
        //   - the bounded "_.push(s)" tracker (unused even on WA Web)
        // WAWebLocaleSettingSync.applyMutations: return {actionState: Success}
        return MutationApplicationResult.success();
    }

    /**
     * Builds a pending {@code setting_locale} mutation that broadcasts the
     * given locale to every linked device.
     *
     * <p>WA Web does not ship a dedicated {@code getLocaleMutation} helper on
     * {@code WAWebLocaleSettingSync}; outgoing locale changes go through the
     * generic {@code WAWebSyncdActionUtils.buildPendingMutation} pathway used
     * by every other {@code AccountSyncdActionBase} subclass. Cobalt exposes
     * a typed helper here — mirroring {@code WAWebPushNameSync.getPushnameMutation}
     * and {@code WAWebDisableLinkPreviewsSync.getMutation} — so the public
     * {@code WhatsAppClient.changeLocale} setter can build a single mutation
     * without hand-rolling the protobuf wrapping.
     *
     * @implNote ADAPTED: WAWebSyncdActionUtils.buildPendingMutation — shaped
     *           after {@code WAWebDisableLinkPreviewsSync.getMutation} (same
     *           {@code collection / indexArgs=[] / value / version / operation=SET
     *           / timestamp / action} payload).
     * @param timestamp the mutation timestamp
     * @param locale    the new BCP-47 locale tag (e.g. {@code "en_US"})
     * @return a pending mutation carrying the {@code setting_locale} action
     * @throws NullPointerException if {@code timestamp} or {@code locale} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSyncdActionUtils", exports = "buildPendingMutation", adaptation = WhatsAppAdaptation.ADAPTED)
    public SyncPendingMutation getLocaleMutation(Instant timestamp, String locale) {
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        Objects.requireNonNull(locale, "locale cannot be null");
        var setting = new LocaleSettingBuilder() // ADAPTED: WAWebSyncdActionUtils.buildPendingMutation value shape: {localeSetting: {locale: s}}
                .locale(locale)
                .build();
        var value = new SyncActionValueBuilder() // ADAPTED: WAWebSyncdActionUtils.buildPendingMutation: encodeProtobuf(SyncActionValueSpec, {...l, timestamp: i})
                .timestamp(timestamp)
                .localeSetting(setting)
                .build();
        var index = JSON.toJSONString(List.of(actionName())); // ADAPTED: WAWebSyncdActionUtils.buildPendingMutation: index = JSON.stringify([action].concat(indexArgs)) with indexArgs = []
        var pending = new DecryptedMutation.Trusted(
                index,
                value,
                SyncdOperation.SET, // ADAPTED: WAWebSyncdActionUtils.buildPendingMutation: operation: SyncdOperation.SET
                timestamp,
                version() // ADAPTED: WAWebSyncdActionUtils.buildPendingMutation: version: this.getVersion()
        );
        return new SyncPendingMutation(pending, 0); // ADAPTED: WAWebSyncdActionUtils.buildPendingMutation
    }
}
