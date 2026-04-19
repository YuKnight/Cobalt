package com.github.auties00.cobalt.message.receive;

import com.github.auties00.cobalt.message.receive.stanza.MessageReceiveStanza;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.MessageContainerSpec;
import com.github.auties00.cobalt.model.message.MessageInfo;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.util.Objects;

/**
 * Base class for the inbound message receivers, providing the shared
 * protobuf decoding and self-account identification utilities used by
 * every receive path.
 *
 * <p>Subclasses implement path-specific processing logic. The two
 * concrete receivers are:
 * <ul>
 *   <li>{@link ChatMessageReceiver} for E2E-encrypted 1:1, group,
 *       broadcast, status, and peer messages.</li>
 *   <li>{@link NewsletterMessageReceiver} for plaintext newsletter
 *       messages.</li>
 * </ul>
 *
 * <p>Both paths share the need to decode a protobuf payload into a
 * {@link MessageContainer} and to recognise messages originating from
 * the logged-in user's own account (for example a message from a
 * companion device).
 *
 * @param <T> the {@link MessageInfo} subtype produced by the concrete receiver
 *
 * @apiNote WAWebHandleMsg: E2E message processing entry point.
 * WAWebHandleNewsletterMsg: newsletter message entry point.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsg")
abstract sealed class MessageReceiver<T extends MessageInfo>
        permits ChatMessageReceiver, NewsletterMessageReceiver {

    /**
     * Logger for diagnostic messages during receive processing.
     *
     * @implNote WAWebHandleMsg uses WALogger with tagged template
     * literals; Cobalt uses {@code System.Logger} instead.
     */
    private static final System.Logger LOGGER = System.getLogger(MessageReceiver.class.getName());

    /**
     * The central session data repository shared with every receive
     * subclass.
     *
     * @implNote WAWebHandleMsg and WAWebHandleNewsletterMsg access the
     * store via module-level imports; Cobalt passes it via constructor
     * injection.
     */
    final WhatsAppStore store;

    /**
     * Constructs a new receiver with the required store dependency.
     *
     * @param store the central session data store
     *
     * @throws NullPointerException if {@code store} is {@code null}
     *
     * @implNote WAWebHandleMsg uses module-level imports for store
     * access; Cobalt uses constructor-based DI.
     */
    MessageReceiver(WhatsAppStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    /**
     * Processes an incoming message node, producing the appropriate
     * {@link MessageInfo} subtype.
     *
     * @param node    the raw {@code <message>} node
     * @param fromJid the JID from the stanza's {@code from} attribute
     * @return the processed message info, or {@code null} for messages
     *         that should be silently acknowledged (for example
     *         unavailable fanout placeholders)
     *
     * @implNote WAWebHandleMsg.default: the main entry point for
     * incoming E2E message handling; the newsletter counterpart is
     * WAWebHandleNewsletterMsg.default.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsg", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    abstract T receive(Node node, Jid fromJid);

    /**
     * Returns the JID of the currently logged-in device, or fails fast
     * when no session is active.
     *
     * @return the self JID
     *
     * @throws IllegalStateException if no session is active
     *
     * @implNote Java-specific convenience accessor; there is no WA
     * Web counterpart because WA Web code always operates within an
     * authenticated UI shell.
     */
    Jid requireSelfJid() {
        return store.jid().orElseThrow(() ->
                new IllegalStateException("Not logged in"));
    }

    /**
     * Decodes a raw protobuf byte array into a {@link MessageContainer}.
     *
     * <p>On failure the method logs a warning and returns {@code null}
     * rather than throwing, so that callers can decide whether a
     * missing protobuf should produce a NACK, a retry, or silent
     * drop based on the message context.
     *
     * @param messageId the message id used for log context
     * @param plaintext the raw protobuf bytes
     * @return the decoded container, or {@code null} on failure
     *
     * @implNote WAWebHandleMsgProcess.processDecryptedMessageProto:
     * strips PKCS#7 padding and then decodes the protobuf; here the
     * padding has already been removed by the Signal cipher, so only
     * the decode step remains.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgProcess", exports = "processDecryptedMessageProto",
            adaptation = WhatsAppAdaptation.ADAPTED)
    MessageContainer decodeProtobuf(String messageId, byte[] plaintext) {
        // WAWebHandleMsgProcess.processDecryptedMessageProto
        // Decodes the plaintext as a MessageContainer and logs a warning on failure
        try {
            return MessageContainerSpec.decode(plaintext);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to decode protobuf for message {0}: {1}",
                    messageId, e.getMessage());
            return null;
        }
    }

    /**
     * Returns whether the stanza was authored by the currently
     * logged-in user.
     *
     * <p>Convenience overload that extracts the sender JID from the
     * parsed stanza and delegates to the JID comparison.
     *
     * @param stanza the parsed stanza
     * @return {@code true} if the sender matches the logged-in user
     *
     * @implNote WAWebMsgProcessingApiUtils:
     * {@code fromMe = isMeAccount(author)}.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingApiUtils", exports = "isMeAccount",
            adaptation = WhatsAppAdaptation.ADAPTED)
    boolean isFromMe(MessageReceiveStanza stanza) {
        return isFromMe(stanza.senderJid());
    }

    /**
     * Returns whether the given sender JID represents the currently
     * logged-in user's account.
     *
     * @param senderJid the sender JID to check
     * @return {@code true} if the sender matches the logged-in user
     *
     * @implNote WAWebMsgProcessingApiUtils:
     * {@code fromMe = isMeAccount(author)}. Comparison is performed
     * on user-level JIDs so that companion-device addressing is
     * treated as the same account as the primary device.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingApiUtils", exports = "isMeAccount",
            adaptation = WhatsAppAdaptation.ADAPTED)
    boolean isFromMe(Jid senderJid) {
        // WAWebMsgProcessingApiUtils.isMeAccount
        // Compares the sender's user-level JID to the logged-in self JID
        var selfJid = store.jid().orElse(null);
        if (selfJid == null) {
            return false;
        }
        return senderJid.toUserJid().equals(selfJid.toUserJid());
    }
}
