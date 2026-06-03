package com.github.auties00.cobalt.client;

import com.github.auties00.cobalt.model.business.profile.BusinessProfile;
import com.github.auties00.cobalt.model.jid.JidProvider;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.MessageInfo;
import com.github.auties00.cobalt.model.message.MessageKey;

/**
 * Root contract shared by every WhatsApp client flavour Cobalt exposes.
 *
 * <p>WhatsApp can be reached through two fundamentally different transports, and this sealed
 * interface is the common supertype of both:
 * <ul>
 *   <li>{@link LinkedWhatsAppClient} drives the reverse-engineered Web/Desktop/Mobile protocol over
 *       an encrypted XMPP-like socket (Signal/Noise cryptography, protobuf wire format, QR or
 *       registration pairing, app-state sync).</li>
 *   <li>{@link CloudWhatsAppClient} drives Meta's hosted WhatsApp Cloud API: stateless HTTPS/JSON
 *       requests to {@code graph.facebook.com} for outbound traffic and a webhook receiver for
 *       inbound traffic.</li>
 * </ul>
 *
 * <p>This type collects only the operations that are meaningful on both transports: lifecycle
 * control, listener registration, sending a message, reacting, marking a chat read, editing the
 * own business profile, and blocking contacts. Everything that exists on a single transport (the
 * hundreds of socket-only operations on {@link LinkedWhatsAppClient}, the REST-only management
 * operations on {@link CloudWhatsAppClient}) lives on the respective sub-interface. Callers that
 * want flavour-agnostic code depend on this type; callers that need a transport-specific feature
 * depend on the matching sub-interface.
 *
 * <p>The persistence model is intentionally not shared: the Linked client is backed by a rich
 * {@code LinkedWhatsAppStore} holding Signal keys and synced collections, whereas the Cloud client keeps
 * only a lightweight credential session, so no {@code store()} accessor appears here.
 *
 * @apiNote
 * Obtain an instance through {@link #builder()}, which branches into the Linked flavours
 * ({@link WhatsAppClientBuilder#linked()}) or the Cloud flavour
 * ({@link WhatsAppClientBuilder#cloud()}). Because the type is sealed, a {@code switch} over a
 * {@code WhatsAppClient} is exhaustive with {@link LinkedWhatsAppClient} and
 * {@link CloudWhatsAppClient} as the only cases.
 */
public sealed interface WhatsAppClient permits LinkedWhatsAppClient, CloudWhatsAppClient {
    /**
     * Returns the entry point for assembling a configured {@link WhatsAppClient} of either flavour.
     *
     * @apiNote
     * The returned builder exposes {@link WhatsAppClientBuilder#linked()} for the socket-based
     * Web/Mobile flavours and {@link WhatsAppClientBuilder#cloud()} for the Cloud API flavour.
     *
     * @return the shared {@link WhatsAppClientBuilder} singleton
     */
    static WhatsAppClientBuilder builder() {
        return WhatsAppClientBuilder.INSTANCE;
    }

    /**
     * Brings the client up and starts processing traffic.
     *
     * <p>For {@link LinkedWhatsAppClient} this opens the encrypted socket and starts the stanza
     * pump; for {@link CloudWhatsAppClient} this validates the access token and starts the webhook
     * receiver. The method returns as soon as the client is live; subsequent events are delivered
     * asynchronously to registered listeners.
     *
     * @return {@code this}, narrowed to the concrete flavour, for fluent chaining
     * @throws IllegalStateException if the client is already connected
     */
    WhatsAppClient connect();

    /**
     * Tears the client down, releasing the socket or stopping the webhook receiver.
     *
     * <p>After this call the client is inert until {@link #connect()} or {@link #reconnect()} is
     * invoked again. Pending in-flight work is allowed to settle where the transport supports it.
     */
    void disconnect();

    /**
     * Tears the client down and immediately brings it back up.
     *
     * <p>For {@link LinkedWhatsAppClient} this re-establishes the socket reusing the persisted
     * credentials; for {@link CloudWhatsAppClient} this restarts the webhook receiver and
     * re-validates the access token.
     */
    void reconnect();

    /**
     * Returns whether the client is currently live.
     *
     * @return {@code true} if the socket is up (Linked) or the webhook receiver is running (Cloud),
     *         {@code false} otherwise
     */
    boolean isConnected();

    /**
     * Blocks the calling thread until the client disconnects.
     *
     * <p>The call returns when a terminal disconnect lands: a non-reconnecting socket close for
     * {@link LinkedWhatsAppClient}, or the webhook receiver stopping for
     * {@link CloudWhatsAppClient}. It is the idiomatic way to keep a process alive for the lifetime
     * of a session.
     *
     * @return {@code this}, narrowed to the concrete flavour, for fluent chaining
     */
    WhatsAppClient waitForDisconnection();

    /**
     * Sends a message to a recipient and returns the key that identifies it.
     *
     * <p>The {@link MessageContainer} is the universal, transport-independent message model. The
     * Linked client encrypts and dispatches it over the socket; the Cloud client translates it to
     * the Cloud {@code /messages} REST shape and posts it. The returned {@link MessageKey} carries
     * the recipient and the freshly minted message id (the {@code wamid} on the Cloud transport),
     * so callers can correlate later status updates.
     *
     * @param recipient the destination chat or user
     * @param message   the message content to send
     * @return the key identifying the sent message
     */
    MessageKey sendMessage(JidProvider recipient, MessageContainer message);

    /**
     * Sends a pre-assembled {@link MessageInfo}.
     *
     * <p>This overload is for callers that have already built a full message descriptor (recipient,
     * content, and metadata). The Cloud client reads the recipient and {@link MessageContainer} out
     * of the descriptor and posts them; the Linked client dispatches the descriptor directly.
     *
     * @param messageInfo the message descriptor to send
     */
    void sendMessage(MessageInfo messageInfo);

    /**
     * Reacts to a message with an emoji.
     *
     * <p>Replacing an existing reaction is done by sending a new emoji for the same
     * {@link MessageKey}; clearing it is done with {@link #removeReaction(MessageKey)}.
     *
     * @param messageKey the key of the message to react to
     * @param emoji      the reaction emoji
     */
    void addReaction(MessageKey messageKey, String emoji);

    /**
     * Removes the reaction previously sent to a message.
     *
     * @param messageKey the key of the message whose reaction is cleared
     */
    void removeReaction(MessageKey messageKey);

    /**
     * Marks a chat as read.
     *
     * <p>On the Linked transport this sends a read receipt covering the chat; on the Cloud
     * transport, where read receipts are addressed to a specific inbound message, the client marks
     * the most recent inbound message of the chat as read.
     *
     * @param chat the chat to mark read
     */
    void markChatAsRead(JidProvider chat);

    /**
     * Updates the business profile of the account this client operates.
     *
     * <p>Both transports expose the same {@link BusinessProfile} model. The Linked client pushes it
     * over the socket; the Cloud client posts it to the phone number's
     * {@code whatsapp_business_profile} edge.
     *
     * @param profile the new business profile
     */
    void editBusinessProfile(BusinessProfile profile);

    /**
     * Blocks a contact so they can no longer message this account.
     *
     * @param contact the contact to block
     */
    void blockContact(JidProvider contact);

    /**
     * Unblocks a previously {@linkplain #blockContact(JidProvider) blocked} contact.
     *
     * @param contact the contact to unblock
     */
    void unblockContact(JidProvider contact);
}
