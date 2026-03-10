package com.github.auties00.cobalt.sync;

import com.github.auties00.cobalt.sync.handler.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class WebAppStateHandlerRegistry {
    private final Map<String, WebAppStateActionHandler> handlers;

    /**
     * Constructs a new handler registry and registers the default handlers.
     */
    public WebAppStateHandlerRegistry() {
        this.handlers = new HashMap<>();
        registerDefaultHandlers();
    }

    private void registerDefaultHandlers() {
        // Chat actions
        registerHandler(ArchiveChatHandler.INSTANCE);
        registerHandler(PinChatHandler.INSTANCE);
        registerHandler(MuteChatHandler.INSTANCE);
        registerHandler(MarkChatAsReadHandler.INSTANCE);
        registerHandler(ClearChatHandler.INSTANCE);
        registerHandler(DeleteChatHandler.INSTANCE);
        registerHandler(LockChatHandler.INSTANCE);

        // Message actions
        registerHandler(StarMessageHandler.INSTANCE);
        registerHandler(DeleteMessageForMeHandler.INSTANCE);
        registerHandler(InteractiveMessageHandler.INSTANCE);

        // Contact actions
        registerHandler(ContactActionHandler.INSTANCE);
        registerHandler(LidContactHandler.INSTANCE);
        registerHandler(PnForLidChatHandler.INSTANCE);
        registerHandler(ShareOwnPnHandler.INSTANCE);

        // Label actions
        registerHandler(LabelEditHandler.INSTANCE);
        registerHandler(LabelAssociationHandler.INSTANCE);
        registerHandler(LabelReorderingHandler.INSTANCE);

        // Business actions
        registerHandler(QuickReplyHandler.INSTANCE);
        registerHandler(AgentActionHandler.INSTANCE);
        registerHandler(ChatAssignmentHandler.INSTANCE);
        registerHandler(ChatAssignmentOpenedStatusHandler.INSTANCE);
        registerHandler(MarketingMessageHandler.INSTANCE);
        registerHandler(MarketingMessageBroadcastHandler.INSTANCE);
        registerHandler(BusinessBroadcastListHandler.INSTANCE);
        registerHandler(BusinessBroadcastAssociationHandler.INSTANCE);
        registerHandler(BusinessBroadcastCampaignHandler.INSTANCE);
        registerHandler(BusinessBroadcastInsightsHandler.INSTANCE);
        registerHandler(MerchantPaymentPartnerHandler.INSTANCE);
        registerHandler(NoteEditHandler.INSTANCE);

        // Sticker and avatar actions
        registerHandler(FavoriteStickerHandler.INSTANCE);
        registerHandler(RemoveRecentStickerHandler.INSTANCE);
        registerHandler(RecentEmojiWeightsHandler.INSTANCE);
        registerHandler(AvatarUpdatedHandler.INSTANCE);
        registerHandler(MusicUserIdHandler.INSTANCE);
        registerHandler(NewsletterSavedInterestsHandler.INSTANCE);

        // AI actions
        registerHandler(AiThreadDeleteHandler.INSTANCE);
        registerHandler(AiThreadRenameHandler.INSTANCE);
        registerHandler(MaibaAIFeaturesControlHandler.INSTANCE);
        registerHandler(UGCBotHandler.INSTANCE);

        // Payment actions
        registerHandler(PaymentInfoHandler.INSTANCE);
        registerHandler(PaymentTosHandler.INSTANCE);
        registerHandler(CustomPaymentMethodsHandler.INSTANCE);

        // User preference actions
        registerHandler(UserStatusMuteHandler.INSTANCE);
        registerHandler(TimeFormatHandler.INSTANCE);
        registerHandler(FavoritesHandler.INSTANCE);
        registerHandler(SubscriptionHandler.INSTANCE);
        registerHandler(StatusPostOptInNotificationPreferencesHandler.INSTANCE);
        registerHandler(NotificationActivitySettingHandler.INSTANCE);
        registerHandler(WamoUserIdentifierHandler.INSTANCE);

        // System actions
        registerHandler(NuxActionHandler.INSTANCE);
        registerHandler(PrimaryVersionHandler.INSTANCE);
        registerHandler(SentinelHandler.INSTANCE);
        registerHandler(PrimaryFeatureHandler.INSTANCE);
        registerHandler(AndroidUnsupportedActionsHandler.INSTANCE);
        registerHandler(DeviceCapabilitiesHandler.INSTANCE);
        registerHandler(BotWelcomeRequestHandler.INSTANCE);
        registerHandler(DetectedOutcomesStatusHandler.INSTANCE);
        registerHandler(WaffleAccountLinkStateHandler.INSTANCE);
        registerHandler(CtwaPerCustomerDataSharingHandler.INSTANCE);

        // Settings
        registerHandler(PushNameSettingHandler.INSTANCE);
        registerHandler(LocaleSettingHandler.INSTANCE);
        registerHandler(UnarchiveChatsSettingHandler.INSTANCE);
        registerHandler(StatusPrivacyHandler.INSTANCE);
        registerHandler(PrivacySettingChannelsPersonalisedRecommendationHandler.INSTANCE);
        registerHandler(PrivateProcessingSettingHandler.INSTANCE);
        registerHandler(DisableLinkPreviewsHandler.INSTANCE);
        registerHandler(VoipRelayAllCallsHandler.INSTANCE);
        registerHandler(ChatLockSettingsHandler.INSTANCE);
        registerHandler(ExternalWebBetaHandler.INSTANCE);
        registerHandler(SettingsSyncHandler.INSTANCE);
        registerHandler(NctSaltSyncHandler.INSTANCE);
        registerHandler(CallLogHandler.INSTANCE);
        registerHandler(DeleteIndividualCallLogHandler.INSTANCE);
        registerHandler(UsernameChatStartModeHandler.INSTANCE);
        registerHandler(OutContactHandler.INSTANCE);
    }

    public void registerHandler(WebAppStateActionHandler handler) {
        handlers.put(handler.actionName(), handler);
    }

    public Optional<WebAppStateActionHandler> findHandler(String actionName) {
        return Optional.ofNullable(handlers.get(actionName));
    }
}
