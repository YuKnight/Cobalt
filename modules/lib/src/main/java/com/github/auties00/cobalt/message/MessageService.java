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
import com.github.auties00.cobalt.wam.WamService;
import com.github.auties00.libsignal.SignalSessionCipher;
import com.github.auties00.libsignal.groups.SignalGroupCipher;

import java.util.Objects;

/**
 * Facade exposing a single entry point for both outgoing and incoming message
 * traffic on top of Cobalt's split send and receive subsystems.
 *
 * <p>The facade owns no state. All caches, queues, and counters live on the
 * underlying {@link MessageSendingService} and {@link MessageReceivingService},
 * which share the supplied Signal ciphers and {@link WhatsAppClient}.
 *
 * @apiNote WA Web exposes two equivalent entry points:
 * {@code WAWebSendMsgJob.encryptAndSendMsg} drives outbound fanout and
 * {@code WAWebCommsHandleMessagingStanza.handleMessagingStanza} drives inbound
 * dispatch. Cobalt collapses them into a single service.
 */
public final class MessageService {
    /**
     * Sending pipeline that owns outbound fanout, device fetch, and stanza
     * emission.
     */
    private final MessageSendingService sendingService;

    /**
     * Receiving pipeline that owns inbound stanza parsing, Signal decryption,
     * and message info construction.
     */
    private final MessageReceivingService receivingService;

    /**
     * Assembles the send and receive pipelines from the supplied collaborators.
     *
     * @implSpec The session and group ciphers must be backed by the same
     * {@link WhatsAppClient#store() store} as the client, otherwise the send
     * and receive sides will disagree about session state.
     * @param client         the WhatsApp client used to send and register stanza handlers
     * @param sessionCipher  the Signal session cipher used for one-to-one encryption
     * @param groupCipher    the Signal group cipher used for sender-key fanout
     * @param deviceService  the device service used to resolve per-user device lists before fanout
     * @param abPropsService the AB props service used to gate optional protocol behaviour
     * @param wamService     the WAM telemetry service forwarded to the sending pipeline
     * @throws NullPointerException if any argument is {@code null}
     */
    public MessageService(
            WhatsAppClient client,
            SignalSessionCipher sessionCipher,
            SignalGroupCipher groupCipher,
            DeviceService deviceService,
            ABPropsService abPropsService,
            WamService wamService
    ) {
        Objects.requireNonNull(client, "client");
        Objects.requireNonNull(sessionCipher, "sessionCipher");
        Objects.requireNonNull(groupCipher, "groupCipher");
        Objects.requireNonNull(deviceService, "deviceService");
        Objects.requireNonNull(abPropsService, "abPropsService");
        Objects.requireNonNull(wamService, "wamService");

        var store = client.store();
        var encryption = new MessageEncryption(store, sessionCipher, groupCipher);
        var decryption = new MessageDecryption(store, sessionCipher, groupCipher);
        this.sendingService = new MessageSendingService(client, encryption, deviceService, abPropsService, wamService);
        this.receivingService = new MessageReceivingService(store, decryption);
    }

    /**
     * Sends a fresh message to the given chat.
     *
     * <p>Allocates a message id, resolves the sender and recipient device
     * lists, encrypts, and waits for the server acknowledgment.
     *
     * @param chatJid   the recipient chat JID
     * @param container the raw message payload
     * @return the server ack result
     * @throws NullPointerException if any argument is {@code null}
     * @see MessageSendingService#send(Jid, MessageContainer)
     */
    public AckResult send(Jid chatJid, MessageContainer container) {
        return sendingService.send(chatJid, container);
    }

    /**
     * Sends a pre-populated message that the caller has already decorated with
     * a key, timestamp, and any extension metadata.
     *
     * @apiNote Use this overload when the message has been prepared by the
     * caller (for example when rehydrating a draft or re-transmitting after a
     * nack). The sending service does not mutate the supplied
     * {@link MessageInfo}.
     * @param messageInfo the fully-prepared outgoing message, either a
     *                    {@link ChatMessageInfo} or a {@link NewsletterMessageInfo}
     * @return the server ack result
     * @throws NullPointerException if {@code messageInfo} is {@code null}
     * @see MessageSendingService#send(MessageInfo)
     */
    public AckResult send(MessageInfo messageInfo) {
        return sendingService.send(messageInfo);
    }

    /**
     * Sends a peer protocol message to one of the user's own devices.
     *
     * <p>Peer messages never reach other users. They carry app-state sync
     * payloads, key shares, and fatal-exception notifications between the
     * linked devices of the current account.
     *
     * @param targetDevice the target device JID, typically the primary device
     * @param messageInfo  the peer protocol message
     * @return the server ack result
     * @throws NullPointerException if any argument is {@code null}
     * @see MessageSendingService#sendPeer(Jid, ChatMessageInfo)
     */
    public AckResult sendPeer(Jid targetDevice, ChatMessageInfo messageInfo) {
        return sendingService.sendPeer(targetDevice, messageInfo);
    }

    /**
     * Processes an inbound {@code <message>} stanza and returns a typed
     * {@link MessageInfo} suitable for application-level consumption.
     *
     * <p>Newsletter messages are returned as {@link NewsletterMessageInfo}.
     * Every other message goes through the Signal decryption pipeline and is
     * returned as {@link ChatMessageInfo}. Fanout placeholders for messages
     * that the server could not deliver produce a {@code null} return so the
     * caller can distinguish them from real messages.
     *
     * @param node the raw inbound {@code <message>} node
     * @return the processed message info, or {@code null} for unavailable
     *         fanout placeholders
     * @throws com.github.auties00.cobalt.exception.WhatsAppMessageException.Receive
     *         if decryption or validation fails for an encrypted payload
     * @see MessageReceivingService#process(Node)
     */
    public MessageInfo process(Node node) {
        return receivingService.process(node);
    }

    /**
     * Clears the pending-message deduplication cache held by the receiving
     * service.
     *
     * @apiNote Should be invoked when the offline delivery phase completes so
     * that stanzas replayed in a new session are not mistakenly treated as
     * duplicates of the pre-reconnect traffic.
     * @see MessageReceivingService#clearPendingMessages()
     */
    public void clearPendingMessages() {
        receivingService.clearPendingMessages();
    }
}
