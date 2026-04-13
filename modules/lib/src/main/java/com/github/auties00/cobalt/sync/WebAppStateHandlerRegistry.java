package com.github.auties00.cobalt.sync;

import com.github.auties00.cobalt.sync.handler.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry mapping action names to their handlers.
 *
 * <p>Per WhatsApp Web {@code WAWebSyncdGetActionHandler}: the registry stores
 * all action handlers and provides lookup by action name and a global
 * max-supported-version query for version gating.
 *
 * @implNote WAWebSyncdGetActionHandler.setActionHandlers, WAWebSyncdGetActionHandler.getActionHandler, WAWebSyncdGetActionHandler.maxSupportedVersion
 */
public final class WebAppStateHandlerRegistry {
    /**
     * Map of action names to their registered handlers.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdGetActionHandler}: this corresponds to the
     * lazily-constructed {@code Map} variable {@code e}, keyed by each handler's
     * {@code getAction()} value. In Cobalt, the map is eagerly constructed at
     * registry creation time.
     *
     * @implNote WAWebSyncdGetActionHandler (var e)
     */
    private final Map<String, WebAppStateActionHandler> handlers; // WAWebSyncdGetActionHandler: var e (lazy Map keyed by getAction())

    /**
     * Constructs a new handler registry and registers the default handlers.
     *
     * @implNote WAWebSyncdGetActionHandler.setActionHandlers, WAWebCollectionHandlerActions.ActionHandlers
     */
    public WebAppStateHandlerRegistry() {
        this.handlers = new HashMap<>(); // ADAPTED: WAWebSyncdGetActionHandler (eagerly created vs WA Web lazy Map)
        registerDefaultHandlers(); // WAWebHandleSuccess -> WAWebSyncdGetActionHandler.setActionHandlers(WAWebCollectionHandlerActions.ActionHandlers)
    }

    /**
     * Registers all default action handlers that correspond to the WhatsApp Web
     * {@code WAWebCollectionHandlerActions.ActionHandlers} array.
     *
     * <p>Per WhatsApp Web, this array is passed to
     * {@code WAWebSyncdGetActionHandler.setActionHandlers} during the success
     * handler flow ({@code WAWebHandleSuccess}).
     *
     * @implNote WAWebCollectionHandlerActions.ActionHandlers
     */
    private void registerDefaultHandlers() { // WAWebCollectionHandlerActions.ActionHandlers
        // Chat actions
        registerHandler(ArchiveChatHandler.INSTANCE); // WAWebArchiveChatSync
        registerHandler(PinChatHandler.INSTANCE); // WAWebPinChatSync
        registerHandler(MuteChatHandler.INSTANCE); // WAWebMuteChatSync
        registerHandler(MarkChatAsReadHandler.INSTANCE); // WAWebMarkChatAsReadSync
        registerHandler(ClearChatHandler.INSTANCE); // WAWebClearChatSync
        registerHandler(DeleteChatHandler.INSTANCE); // WAWebDeleteChatSync
        registerHandler(LockChatHandler.INSTANCE); // WAWebLockChatSync

        // Message actions
        registerHandler(StarMessageHandler.INSTANCE); // WAWebStarMessageSync
        registerHandler(DeleteMessageForMeHandler.INSTANCE); // WAWebDeleteMessageForMeSync
        registerHandler(InteractiveMessageHandler.INSTANCE); // WAWebInteractiveMessageSync

        // Contact actions
        registerHandler(ContactActionHandler.INSTANCE); // WAWebContactSync
        registerHandler(LidContactHandler.INSTANCE); // WAWebLidContactSync
        registerHandler(PnForLidChatHandler.INSTANCE); // WAWebPnForLidChatSync
        registerHandler(ShareOwnPnHandler.INSTANCE); // WAWebShareOwnPnSync

        // Label actions
        registerHandler(LabelEditHandler.INSTANCE); // WAWebLabelSync
        registerHandler(LabelAssociationHandler.INSTANCE); // WAWebLabelJidSync
        registerHandler(LabelReorderingHandler.INSTANCE); // WAWebLabelReorderingSync

        // Business actions
        registerHandler(QuickReplyHandler.INSTANCE); // WAWebQuickRepliesSync
        registerHandler(AgentActionHandler.INSTANCE); // WAWebAgentSync
        registerHandler(ChatAssignmentHandler.INSTANCE); // WAWebChatAssignmentSync
        registerHandler(ChatAssignmentOpenedStatusHandler.INSTANCE); // WAWebChatAssignmentOpenedStatusSync
        registerHandler(MarketingMessageHandler.INSTANCE); // WAWebPremiumMessageSync
        registerHandler(MarketingMessageBroadcastHandler.INSTANCE); // WAWebPremiumMessageBroadcastSync
        registerHandler(BusinessBroadcastListHandler.INSTANCE); // WAWebBroadcastListSync
        registerHandler(BusinessBroadcastCampaignHandler.INSTANCE); // WAWebBroadcastCampaignSync
        registerHandler(BusinessBroadcastInsightsHandler.INSTANCE); // WAWebBusinessBroadcastInsightsSync
        registerHandler(CustomerDataHandler.INSTANCE); // WAWebCustomerDataSync
        registerHandler(MerchantPaymentPartnerHandler.INSTANCE); // WAWebMerchantPaymentPartnerSync
        registerHandler(NoteEditHandler.INSTANCE); // WAWebNoteSync

        // Sticker and avatar actions
        registerHandler(FavoriteStickerHandler.INSTANCE); // WAWebStickersFavoriteSyncAction
        registerHandler(RemoveRecentStickerHandler.INSTANCE); // WAWebStickersRemoveRecentSyncAction
        registerHandler(AvatarUpdatedHandler.INSTANCE); // WAWebStickersAvatarUpdatedSyncAction

        // AI actions
        registerHandler(AiThreadDeleteHandler.INSTANCE); // WAWebAiThreadDeleteSync
        registerHandler(AiThreadRenameHandler.INSTANCE); // WAWebAiThreadRenameSync

        // Payment actions
        registerHandler(PaymentInfoHandler.INSTANCE); // WAWebPaymentInfoSync
        registerHandler(PaymentTosHandler.INSTANCE); // WAWebPaymentTosSync
        registerHandler(CustomPaymentMethodsHandler.INSTANCE); // WAWebCustomPaymentMethodsSync

        // User preference actions
        registerHandler(UserStatusMuteHandler.INSTANCE); // WAWebUserStatusMuteSync
        registerHandler(TimeFormatHandler.INSTANCE); // WAWebTimeFormatSync
        registerHandler(FavoritesHandler.INSTANCE); // WAWebFavoritesSync

        // System actions
        registerHandler(NuxActionHandler.INSTANCE); // WAWebNuxSync
        registerHandler(PrimaryVersionHandler.INSTANCE); // WAWebPrimaryVersionSync
        registerHandler(SentinelHandler.INSTANCE); // WAWebSentinelMutationSync
        registerHandler(PrimaryFeatureHandler.INSTANCE); // WAWebPrimaryFeatureSync
        registerHandler(AndroidUnsupportedActionsHandler.INSTANCE); // WAWebAndroidUnsupportedActionsSync
        registerHandler(DeviceCapabilitiesHandler.INSTANCE); // WAWebDeviceCapabilitiesSync
        registerHandler(BotWelcomeRequestHandler.INSTANCE); // WAWebBotWelcomeRequestSync
        registerHandler(DetectedOutcomesStatusHandler.INSTANCE); // WAWebDetectedOutcomesStatusSync
        registerHandler(WaffleAccountLinkStateHandler.INSTANCE); // WAWebWaffleAccountLinkStateSync
        registerHandler(CtwaPerCustomerDataSharingHandler.INSTANCE); // WAWebCtwaPerCustomerDataSharingSync

        // Settings
        registerHandler(PushNameSettingHandler.INSTANCE); // WAWebPushNameSync
        registerHandler(LocaleSettingHandler.INSTANCE); // WAWebLocaleSettingSync
        registerHandler(UnarchiveChatsSettingHandler.INSTANCE); // WAWebArchiveSettingSync
        registerHandler(StatusPrivacyHandler.INSTANCE); // WAWebStatusPrivacySettingSync
        registerHandler(DisableLinkPreviewsHandler.INSTANCE); // WAWebDisableLinkPreviewsSync
        registerHandler(VoipRelayAllCallsHandler.INSTANCE); // WAWebVoipRelayAllCallsSettingSync
        registerHandler(ChatLockSettingsHandler.INSTANCE); // WAWebChatLockSettingsSync
        registerHandler(ExternalWebBetaHandler.INSTANCE); // WAWebExternalWebBetaSync
        registerHandler(SettingsSyncHandler.INSTANCE); // WAWebSettingsSync
        registerHandler(NctSaltSyncHandler.INSTANCE); // WAWebNctSaltSync
        registerHandler(CallLogHandler.INSTANCE); // WAWebCallLogSync
        registerHandler(SubscriptionHandler.INSTANCE); // WAWebSubscriptionsSyncV2Sync
        registerHandler(OutContactHandler.INSTANCE); // WAWebOutContactSync
    }

    /**
     * Registers a handler for its declared action name.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdGetActionHandler.setActionHandlers}:
     * the array of handlers is stored and lazily indexed by action name via
     * {@code e.getAction()}.
     *
     * @param handler the handler to register
     * @implNote WAWebSyncdGetActionHandler.setActionHandlers
     */
    public void registerHandler(WebAppStateActionHandler handler) {
        handlers.put(handler.actionName(), handler); // WAWebSyncdGetActionHandler.setActionHandlers
    }

    /**
     * Finds a handler by action name.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdGetActionHandler.getActionHandler}:
     * looks up the handler map by action string. Returns {@code Optional.empty()}
     * when no handler is registered for the given action.
     *
     * @param actionName the action name to look up
     * @return the handler, or empty if not registered
     * @implNote WAWebSyncdGetActionHandler.getActionHandler
     */
    public Optional<WebAppStateActionHandler> findHandler(String actionName) {
        return Optional.ofNullable(handlers.get(actionName)); // ADAPTED: WAWebSyncdGetActionHandler.getActionHandler (returns Optional instead of undefined, no lazy Map init)
    }

    /**
     * Returns the maximum version supported by any registered handler.
     *
     * <p>Per WhatsApp Web {@code WAWebSyncdGetActionHandler.maxSupportedVersion}:
     * computes the maximum of all handler versions. Used for fast pre-filtering
     * of mutations whose version exceeds any handler's capability.
     *
     * @return the maximum supported version across all handlers
     * @implNote WAWebSyncdGetActionHandler.maxSupportedVersion
     */
    public int maxSupportedVersion() {
        return handlers.values().stream() // ADAPTED: WAWebSyncdGetActionHandler.maxSupportedVersion (recomputes each call vs WA Web lazy cache in var s)
                .mapToInt(WebAppStateActionHandler::version) // WAWebSyncdGetActionHandler.maxSupportedVersion: e.getVersion()
                .max() // WAWebSyncdGetActionHandler.maxSupportedVersion: Math.max.apply(Math, ...)
                .orElse(0); // ADAPTED: returns 0 when empty vs WA Web returns -Infinity (impossible in practice)
    }
}
