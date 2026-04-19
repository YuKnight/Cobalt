package com.github.auties00.cobalt.message;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.device.DeviceService;
import com.github.auties00.cobalt.message.receive.MessageReceivingService;
import com.github.auties00.cobalt.message.receive.crypto.MessageDecryption;
import com.github.auties00.cobalt.message.send.MessageSendingService;
import com.github.auties00.cobalt.message.send.ack.AckResult;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryption;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.MessageInfo;
import com.github.auties00.cobalt.model.newsletter.NewsletterMessageInfo;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.libsignal.SignalSessionCipher;
import com.github.auties00.libsignal.groups.SignalGroupCipher;

import java.util.Objects;

/**
 * Facade that exposes a single entry point for both outgoing and incoming
 * message traffic on top of Cobalt's split send/receive subsystems.
 *
 * <p>Internally the service wires a {@link MessageSendingService} and a
 * {@link MessageReceivingService} that share the same Signal ciphers and
 * {@link WhatsAppClient} instance; each call delegates to the appropriate
 * subsystem. Callers who need finer-grained control can still instantiate the
 * sending and receiving services directly, but the facade is the recommended
 * integration surface and mirrors the coarse send / handle split that WA Web
 * exposes at the top of its messaging stack.
 *
 * <p>The facade does not own any state of its own: all caches, queues, and
 * counters live on the underlying services. Stopping the facade therefore
 * reduces to clearing any transient caches on the receive side via
 * {@link #clearPendingMessages()}.
 *
 * @apiNote WA Web ships two entry points with equivalent responsibilities:
 * {@code WAWebSendMsgJob.encryptAndSendMsg} drives outbound fanout and
 * {@code WAWebCommsHandleMessagingStanza.handleMessagingStanza} drives inbound
 * dispatch. Cobalt collapses them into a single service so the high-level API
 * is easier to consume.
 */
public final class MessageService {
    /**
     * The sending service that owns outbound fanout, device fetch, and stanza
     * emission.
     *
     * @implNote ADAPTED: the responsibilities held by
     * {@code WAWebSendMsgJob.encryptAndSendMsg} and friends in WA Web.
     */
    private final MessageSendingService sendingService;

    /**
     * The receiving service that owns inbound stanza parsing, Signal
     * decryption, and message info construction.
     *
     * @implNote ADAPTED: the responsibilities held by
     * {@code WAWebCommsHandleMessagingStanza.handleMessagingStanza} and the
     * downstream {@code WAWebMsgProcessing} modules in WA Web.
     */
    private final MessageReceivingService receivingService;

    /**
     * Assembles the send and receive pipelines from the supplied
     * collaborators.
     *
     * <p>The caller is responsible for providing a Signal session cipher and
     * a Signal group cipher that are both backed by the same
     * {@link WhatsAppClient#store() store}; otherwise the send and receive
     * sides will disagree about session state.
     *
     * @param client         the WhatsApp client used to send and register
     *                       stanza handlers
     * @param sessionCipher  the Signal session cipher used for one-to-one
     *                       encryption
     * @param groupCipher    the Signal group cipher used for sender-key fanout
     * @param deviceService  the device service used to resolve per-user
     *                       device lists before fanout
     * @param abPropsService the AB props service used to gate optional
     *                       protocol behaviour
     * @throws NullPointerException if any argument is {@code null}
     */
    public MessageService(
            WhatsAppClient client,
            SignalSessionCipher sessionCipher,
            SignalGroupCipher groupCipher,
            DeviceService deviceService,
            ABPropsService abPropsService
    ) {
        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(sessionCipher, "sessionCipher");
        Objects.requireNonNull(groupCipher, "groupCipher");
        Objects.requireNonNull(deviceService, "deviceService");
        Objects.requireNonNull(abPropsService, "abPropsService");

        var store = client.store();
        var encryption = new MessageEncryption(store, sessionCipher, groupCipher);
        var decryption = new MessageDecryption(store, sessionCipher, groupCipher);
        this.sendingService = new MessageSendingService(client, encryption, deviceService, abPropsService);
        this.receivingService = new MessageReceivingService(store, decryption);
    }

    /**
     * Sends a fresh message to the given chat, handling all preparation,
     * device fanout, and ack tracking.
     *
     * <p>This overload accepts a raw {@link MessageContainer}; the sending
     * service is responsible for allocating a message id, resolving the
     * sender/recipient device lists, encrypting, and waiting for the server
     * acknowledgment.
     *
     * @param chatJid   the recipient chat JID
     * @param container the raw message payload
     * @return the server ack result
     * @throws NullPointerException if any argument is {@code null}
     * @see MessageSendingService#send(Jid, MessageContainer)
     */
    public AckResult send(Jid chatJid, MessageContainer container) {
        // Delegates to the sending service that owns the outbound pipeline
        return sendingService.send(chatJid, container);
    }

    /**
     * Sends a pre-populated message that the caller has already decorated with
     * a key, timestamp, and any extension metadata.
     *
     * <p>Use this overload when the message has been prepared by the caller
     * (for example when rehydrating a draft or re-transmitting after a
     * nack): the sending service will not mutate the incoming
     * {@link MessageInfo}.
     *
     * @param messageInfo the fully-prepared outgoing message, either a
     *                    {@link ChatMessageInfo} or a
     *                    {@link NewsletterMessageInfo}
     * @return the server ack result
     * @throws NullPointerException if {@code messageInfo} is {@code null}
     * @see MessageSendingService#send(MessageInfo)
     */
    public AckResult send(MessageInfo messageInfo) {
        // Delegates to the sending service for already-prepared outgoing messages
        return sendingService.send(messageInfo);
    }

    /**
     * Sends a peer protocol message to one of the user's own devices.
     *
     * <p>Peer messages never reach other users: they carry app-state sync
     * payloads, key shares, and fatal-exception notifications between the
     * linked devices of the current account.
     *
     * @param targetDevice the target device JID (typically the primary device)
     * @param messageInfo  the peer protocol message
     * @return the server ack result
     * @throws NullPointerException if any argument is {@code null}
     * @see MessageSendingService#sendPeer(Jid, ChatMessageInfo)
     */
    public AckResult sendPeer(Jid targetDevice, ChatMessageInfo messageInfo) {
        // Delegates to the sending service for peer (self-device) messages
        return sendingService.sendPeer(targetDevice, messageInfo);
    }

    /**
     * Processes an inbound {@code <message>} stanza and returns a typed
     * {@link MessageInfo} suitable for application-level consumption.
     *
     * <p>Newsletter messages are returned as
     * {@link NewsletterMessageInfo}. All other messages go through the Signal
     * decryption pipeline and are returned as {@link ChatMessageInfo}.
     * Fanout placeholders for messages that the server could not deliver
     * produce a {@code null} return value so the caller can distinguish them
     * from real messages.
     *
     * @param node the raw inbound {@code <message>} node
     * @return the processed message info, or {@code null} for unavailable
     *         fanout placeholders
     * @throws com.github.auties00.cobalt.exception.WhatsAppMessageException.Receive
     *         if decryption or validation fails for an encrypted payload
     * @see MessageReceivingService#process(Node)
     */
    public MessageInfo process(Node node) {
        // Delegates to the receiving service for inbound stanza decoding
        return receivingService.process(node);
    }

    /**
     * Clears the pending-message deduplication cache held by the receiving
     * service.
     *
     * <p>Should be invoked when the offline delivery phase completes so that
     * stanzas replayed in a new session are not mistakenly treated as
     * duplicates of the pre-reconnect traffic.
     *
     * @see MessageReceivingService#clearPendingMessages()
     */
    public void clearPendingMessages() {
        // Resets the receiving-side dedup cache when offline delivery ends
        receivingService.clearPendingMessages();
    }
}
