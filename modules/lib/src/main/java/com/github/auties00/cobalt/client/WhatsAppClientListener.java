package com.github.auties00.cobalt.client;

import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.chat.group.GroupPastParticipant;
import com.github.auties00.cobalt.model.message.MessageInfo;
import com.github.auties00.cobalt.model.sync.SyncAction;
import com.github.auties00.cobalt.model.device.identity.ADVEncryptionType;
import com.github.auties00.cobalt.model.call.CallOffer;
import com.github.auties00.cobalt.model.contact.Contact;
import com.github.auties00.cobalt.model.contact.ContactTextStatus;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.newsletter.Newsletter;
import com.github.auties00.cobalt.model.privacy.PrivacySettingEntry;
import com.github.auties00.cobalt.node.Node;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A callback interface that observes events emitted by a
 * {@link WhatsAppClient} during its lifecycle.
 *
 * <p>Cobalt funnels every notable event (socket traffic, login, logout,
 * message reception, contact updates, sync progress, call offers, identity
 * changes, and so on) through listener callbacks so that application code
 * can react without having to poll the store. All callback methods carry
 * empty default implementations, so integrators only need to override the
 * events they are interested in.
 *
 * <p>Listeners are registered via
 * {@link WhatsAppClient#addListener(WhatsAppClientListener)} and can be
 * removed with
 * {@link WhatsAppClient#removeListener(WhatsAppClientListener)}. Each
 * invocation runs on a virtual thread, so a long-running listener does not
 * block the client socket or stanza pipeline.
 *
 * @see WhatsAppClient#addListener(WhatsAppClientListener)
 * @see WhatsAppClient#removeListener(WhatsAppClientListener)
 */
public interface WhatsAppClientListener {
    /**
     * Notifies the listener that a node has been sent to the WhatsApp
     * server.
     *
     * @param whatsapp the client emitting the event
     * @param outgoing the node that was sent
     */
    default void onNodeSent(WhatsAppClient whatsapp, Node outgoing) {
    }

    /**
     * Notifies the listener that a node has been received from the
     * WhatsApp server.
     *
     * @param whatsapp the client emitting the event
     * @param incoming the node that was received
     */
    default void onNodeReceived(WhatsAppClient whatsapp, Node incoming) {
    }

    /**
     * Notifies the listener that a successful connection and login to a
     * WhatsApp account has been established.
     *
     * <p>When this event fires, data such as chats and contacts may not
     * yet be loaded into memory. Use the corresponding event handlers for
     * specific data types, such as
     * {@link #onChats(WhatsAppClient, Collection)} and
     * {@link #onContacts(WhatsAppClient, Collection)}.
     *
     * @param whatsapp the client emitting the event
     */
    default void onLoggedIn(WhatsAppClient whatsapp) {
    }

    /**
     * Notifies the listener that the connection to WhatsApp has been
     * terminated.
     *
     * @param whatsapp the client emitting the event
     * @param reason   the reason for disconnection
     * @see WhatsAppClientDisconnectReason
     */
    default void onDisconnected(WhatsAppClient whatsapp, WhatsAppClientDisconnectReason reason) {
    }

    /**
     * Notifies the listener that an app-state action has been received
     * from WhatsApp Web.
     *
     * <p>This event is only triggered for web client connections.
     *
     * @param whatsapp the client emitting the event
     * @param action   the action that was executed
     * @param index    the data associated with this action
     */
    default void onWebAppStateAction(WhatsAppClient whatsapp, SyncAction action, String index) {
    }

    /**
     * Notifies the listener that the primary feature flags have been
     * received from WhatsApp Web.
     *
     * <p>This event is only triggered for web client connections.
     *
     * @param whatsapp the client emitting the event
     * @param features the collection of feature flags that were sent
     */
    default void onWebAppPrimaryFeatures(WhatsAppClient whatsapp, List<String> features) {
    }

    /**
     * Notifies the listener that the full contact list has been received
     * from WhatsApp.
     *
     * @param whatsapp the client emitting the event
     * @param contacts the collection of contacts
     */
    default void onContacts(WhatsAppClient whatsapp, Collection<Contact> contacts) {
    }

    /**
     * Notifies the listener that a contact's presence status has been
     * updated.
     *
     * @param whatsapp     the client emitting the event
     * @param conversation the chat related to this presence update
     * @param participant  the contact whose presence status changed
     */
    default void onContactPresence(WhatsAppClient whatsapp, Jid conversation, Jid participant) {
    }

    /**
     * Notifies the listener that the full chat list has been received
     * from WhatsApp.
     *
     * <p>When this event fires, all chat metadata is available, excluding
     * message content. For message content refer to
     * {@link #onWebHistorySyncMessages(WhatsAppClient, Chat, boolean)}.
     * Particularly old chats may be loaded later through the history-sync
     * process.
     *
     * @param whatsapp the client emitting the event
     * @param chats    the collection of chats
     */
    default void onChats(WhatsAppClient whatsapp, Collection<Chat> chats) {
    }

    /**
     * Notifies the listener that the full newsletter list has been
     * received from WhatsApp.
     *
     * @param whatsapp    the client emitting the event
     * @param newsletters the collection of newsletters
     */
    default void onNewsletters(WhatsAppClient whatsapp, Collection<Newsletter> newsletters) {
    }

    /**
     * Notifies the listener that messages for a chat have been received
     * during history synchronization.
     *
     * <p>This event is only triggered during initial QR code scanning and
     * history syncing. On subsequent connections messages are already
     * loaded in the chats.
     *
     * @param whatsapp the client emitting the event
     * @param chat     the chat being synchronized
     * @param last     {@code true} if these are the final messages for
     *                 this chat, {@code false} if more are coming
     */
    default void onWebHistorySyncMessages(WhatsAppClient whatsapp, Chat chat, boolean last) {
    }

    /**
     * Notifies the listener that past participants for a group have been
     * received during history synchronization.
     *
     * @param whatsapp              the client emitting the event
     * @param chatJid               the group chat JID
     * @param groupPastParticipants the collection of past participants
     */
    default void onWebHistorySyncPastParticipants(WhatsAppClient whatsapp, Jid chatJid, Collection<GroupPastParticipant> groupPastParticipants) {
    }

    /**
     * Notifies the listener of progress made by the history-synchronization
     * process.
     *
     * <p>This event is only triggered during initial QR code scanning and
     * history syncing.
     *
     * @param whatsapp   the client emitting the event
     * @param percentage the percentage of synchronization completed
     * @param recent     {@code true} if syncing recent messages,
     *                   {@code false} if syncing older messages
     */
    default void onWebHistorySyncProgress(WhatsAppClient whatsapp, int percentage, boolean recent) {
    }

    /**
     * Notifies the listener that a new message has been received.
     *
     * @param whatsapp the client emitting the event
     * @param info     the message that was received
     */
    default void onNewMessage(WhatsAppClient whatsapp, MessageInfo info) {
    }

    /**
     * Notifies the listener that a message has been deleted.
     *
     * @param whatsapp the client emitting the event
     * @param info     the message that was deleted
     * @param everyone {@code true} if the message was deleted for
     *                 everyone, {@code false} if deleted only for the user
     */
    default void onMessageDeleted(WhatsAppClient whatsapp, MessageInfo info, boolean everyone) {
    }

    /**
     * Notifies the listener that a message's status has changed (sent,
     * delivered, read).
     *
     * @param whatsapp the client emitting the event
     * @param info     the message whose status changed
     */
    default void onMessageStatus(WhatsAppClient whatsapp, MessageInfo info) {
    }

    /**
     * Notifies the listener that the full status feed has been received
     * from WhatsApp.
     *
     * @param whatsapp the client emitting the event
     * @param status   the collection of status updates
     */
    default void onStatus(WhatsAppClient whatsapp, Collection<ChatMessageInfo> status) {
    }

    /**
     * Notifies the listener that a new status update has been received.
     *
     * @param whatsapp the client emitting the event
     * @param status   the new status message
     */
    default void onNewStatus(WhatsAppClient whatsapp, ChatMessageInfo status) {
    }

    /**
     * Notifies the listener that a message has been sent in reply to a
     * previous message.
     *
     * @param whatsapp the client emitting the event
     * @param response the reply message
     * @param quoted   the message being replied to
     */
    default void onMessageReply(WhatsAppClient whatsapp, MessageInfo response, MessageInfo quoted) {
    }

    /**
     * Notifies the listener that a contact's profile picture has changed.
     *
     * @param whatsapp the client emitting the event
     * @param jid      the contact whose profile picture changed
     */
    default void onProfilePictureChanged(WhatsAppClient whatsapp, Jid jid) {
    }

    /**
     * Notifies the listener that the user's display name has changed.
     *
     * @param whatsapp the client emitting the event
     * @param oldName  the previous name
     * @param newName  the new name
     */
    default void onNameChanged(WhatsAppClient whatsapp, String oldName, String newName) {
    }

    /**
     * Notifies the listener that the user's about/status text has
     * changed.
     *
     * @param whatsapp the client emitting the event
     * @param oldAbout the previous about text
     * @param newAbout the new about text
     */
    default void onAboutChanged(WhatsAppClient whatsapp, String oldAbout, String newAbout) {
    }

    /**
     * Notifies the listener that a contact's text status metadata has
     * changed.
     *
     * @param whatsapp the client emitting the event
     * @param contact  the contact whose text status changed
     * @param status   the new text status
     */
    default void onContactTextStatus(WhatsAppClient whatsapp, Jid contact, ContactTextStatus status) {
    }

    /**
     * Notifies the listener that the user's locale settings have changed.
     *
     * @param whatsapp  the client emitting the event
     * @param oldLocale the previous locale
     * @param newLocale the new locale
     */
    default void onLocaleChanged(WhatsAppClient whatsapp, String oldLocale, String newLocale) {
    }

    /**
     * Notifies the listener that a contact has been blocked or unblocked.
     *
     * @param whatsapp the client emitting the event
     * @param contact  the contact that was blocked or unblocked
     */
    default void onContactBlocked(WhatsAppClient whatsapp, Jid contact) {
    }

    /**
     * Notifies the listener that a new contact has been added to the
     * contact list.
     *
     * @param whatsapp the client emitting the event
     * @param contact  the new contact
     */
    default void onNewContact(WhatsAppClient whatsapp, Contact contact) {
    }

    /**
     * Notifies the listener that a privacy setting has been changed.
     *
     * @param whatsapp        the client emitting the event
     * @param newPrivacyEntry the new privacy setting
     */
    default void onPrivacySettingChanged(WhatsAppClient whatsapp, PrivacySettingEntry newPrivacyEntry) {
    }

    /**
     * Notifies the listener that a registration code (OTP) has been
     * requested from a new device.
     *
     * <p>This event is only triggered for the mobile API.
     *
     * @param whatsapp the client emitting the event
     * @param code     the registration code
     */
    default void onRegistrationCode(WhatsAppClient whatsapp, long code) {
    }

    /**
     * Notifies the listener that a phone call has been received.
     *
     * @param whatsapp the client emitting the event
     * @param call     the phone call information
     */
    default void onCall(WhatsAppClient whatsapp, CallOffer call) {
    }

    /**
     * Notifies the listener that a device's identity key has changed.
     *
     * <p>This indicates that the device was reset, reinstalled, or
     * potentially compromised. Applications should display a security
     * warning to users.
     *
     * @param whatsapp       the client emitting the event
     * @param userJid        the user whose device changed
     * @param changedDevices the devices with new identity keys
     */
    default void onDeviceIdentityChanged(WhatsAppClient whatsapp, Jid userJid, Set<Jid> changedDevices) {
    }

    /**
     * Notifies the listener that a contact's account type has changed
     * between {@code E2EE} and {@code HOSTED}.
     *
     * <p>This indicates a transition in the contact's encryption
     * configuration.
     *
     * @param whatsapp the client emitting the event
     * @param userJid  the user whose account type changed
     * @param oldType  the previous account type
     * @param newType  the new account type
     */
    default void onAccountTypeChanged(WhatsAppClient whatsapp, Jid userJid, ADVEncryptionType oldType, ADVEncryptionType newType) {
    }
}
