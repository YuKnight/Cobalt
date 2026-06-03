package com.github.auties00.cobalt.listener.linked;

import com.github.auties00.cobalt.client.LinkedWhatsAppClientListener;

import com.github.auties00.cobalt.listener.WhatsAppListener;

/**
 * The sealed marker for every event a {@link com.github.auties00.cobalt.client.LinkedWhatsAppClient}
 * emits.
 *
 * <p>Each single-method Linked listener in this package extends this marker, and the aggregator
 * {@link LinkedWhatsAppClientListener} extends them all. Registering a listener of this type observes
 * the Linked event stream; the concrete event is recovered by the dispatcher through a pattern-match.
 */
public sealed interface WhatsAppLinkedListener extends WhatsAppListener permits
        LinkedAboutChangedListener,
        LinkedAccountTypeChangedListener,
        LinkedBlockedContactsListener,
        LinkedBusinessPrivacySettingChangedListener,
        LinkedCallEndedListener,
        LinkedCallInteractionListener,
        LinkedCallLinkAdmittedListener,
        LinkedCallLinkDeniedListener,
        LinkedCallLinkLobbyJoinRequestListener,
        LinkedCallListener,
        LinkedCallMuteChangedListener,
        LinkedCallOfferNoticeListener,
        LinkedCallParticipantsChangedListener,
        LinkedCallPeerStateChangedListener,
        LinkedCallPreacceptListener,
        LinkedCallVideoStateChangedListener,
        LinkedCallVideoUpgradeRequestListener,
        LinkedChatsListener,
        LinkedContactBlacklistListener,
        LinkedContactBlockedListener,
        LinkedContactPresenceListener,
        LinkedContactsListener,
        LinkedContactTextStatusListener,
        LinkedDeviceIdentityChangedListener,
        LinkedDevicesListener,
        LinkedDisappearingModeChangedListener,
        LinkedDisconnectedListener,
        LinkedFacebookGraphQlSessionChangedListener,
        LinkedGroupsListener,
        LinkedLocaleChangedListener,
        LinkedLoggedInListener,
        LinkedMessageDeletedListener,
        LinkedMessageReplyListener,
        LinkedMessageStatusListener,
        LinkedNameChangedListener,
        LinkedNewContactListener,
        LinkedNewMessageListener,
        LinkedNewslettersListener,
        LinkedNewStatusListener,
        LinkedNodeReceivedListener,
        LinkedNodeSentListener,
        LinkedOptOutListListener,
        LinkedPrivacySettingChangedListener,
        LinkedProfilePictureChangedListener,
        LinkedRegistrationCodeListener,
        LinkedStatusListener,
        LinkedStatusPrivacyChangedListener,
        LinkedTosNoticesChangedListener,
        LinkedWebAppPrimaryFeaturesListener,
        LinkedWebAppStateActionListener,
        LinkedWebHistorySyncMessagesListener,
        LinkedWebHistorySyncPastParticipantsListener,
        LinkedWebHistorySyncProgressListener,
        LinkedWhatsAppWebGraphQlSessionChangedListener,
        LinkedWhatsAppClientListener {
}