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
 * Base class for the inbound message receivers, providing the shared protobuf decoding
 * and self-account identification utilities used by every receive path.
 *
 * <p>Both concrete subclasses ({@link ChatMessageReceiver} and
 * {@link NewsletterMessageReceiver}) need to decode a protobuf payload into a
 * {@link MessageContainer} and to recognise messages originating from the logged-in
 * user's own account (for example a message echoed from a companion device).
 *
 * @param <T> the {@link MessageInfo} subtype produced by the concrete receiver
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsg")
abstract sealed class MessageReceiver<T extends MessageInfo>
        permits ChatMessageReceiver, NewsletterMessageReceiver {

    /**
     * Logger for receive processing diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(MessageReceiver.class.getName());

    /**
     * Central session data repository shared with every receive subclass.
     */
    final WhatsAppStore store;

    /**
     * Constructs a new receiver with the required store dependency.
     *
     * @param store the central session data store
     * @throws NullPointerException if {@code store} is {@code null}
     */
    MessageReceiver(WhatsAppStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    /**
     * Processes an incoming message node, producing the appropriate {@link MessageInfo}
     * subtype.
     *
     * @param node    the raw {@code <message>} node
     * @param fromJid the JID from the stanza's {@code from} attribute
     * @return the processed message info, or {@code null} for messages that should be
     *         silently acknowledged (for example unavailable fanout placeholders)
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsg", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    abstract T receive(Node node, Jid fromJid);

    /**
     * Returns the JID of the currently logged-in device, or fails fast when no session
     * is active.
     *
     * @return the self JID
     * @throws IllegalStateException if no session is active
     */
    Jid requireSelfJid() {
        return store.jid().orElseThrow(() ->
                new IllegalStateException("Not logged in"));
    }

    /**
     * Decodes a raw protobuf byte array into a {@link MessageContainer}.
     *
     * <p>On failure the method logs a warning and returns {@code null} rather than
     * throwing, so callers can decide whether a missing protobuf should produce a NACK,
     * a retry, or a silent drop based on the message context.
     *
     * @param messageId the message id used for log context
     * @param plaintext the raw protobuf bytes
     * @return the decoded container, or {@code null} on failure
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgProcess", exports = "processDecryptedMessageProto",
            adaptation = WhatsAppAdaptation.ADAPTED)
    MessageContainer decodeProtobuf(String messageId, byte[] plaintext) {
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
     * Returns whether the stanza was authored by the currently logged-in user.
     *
     * <p>Convenience overload that extracts the sender JID from the parsed stanza and
     * delegates to the JID comparison.
     *
     * @param stanza the parsed stanza
     * @return {@code true} if the sender matches the logged-in user
     */
    @WhatsAppWebExport(moduleName = "WAWebUserPrefsMeUser", exports = "isMeAccount",
            adaptation = WhatsAppAdaptation.ADAPTED)
    boolean isFromMe(MessageReceiveStanza stanza) {
        return isFromMe(stanza.senderJid());
    }

    /**
     * Returns whether the given sender JID represents the currently logged-in user's
     * account, matching either the PN or the LID identity.
     *
     * <p>Comparison is performed on user-level JIDs so companion-device addressing is
     * treated as the same account as the primary device. Both the PN and the LID are
     * checked so that a message addressed via either mode is recognised as self.
     *
     * @param senderJid the sender JID to check
     * @return {@code true} if the sender matches the logged-in user's PN or LID
     */
    @WhatsAppWebExport(moduleName = "WAWebUserPrefsMeUser", exports = "isMeAccount",
            adaptation = WhatsAppAdaptation.ADAPTED)
    boolean isFromMe(Jid senderJid) {
        if (senderJid == null) {
            return false;
        }
        var senderUser = senderJid.toUserJid();
        var selfPn = store.jid().orElse(null);
        if (selfPn != null && senderUser.equals(selfPn.toUserJid())) {
            return true;
        }
        var selfLid = store.lid().orElse(null);
        return selfLid != null && senderUser.equals(selfLid.toUserJid());
    }
}
