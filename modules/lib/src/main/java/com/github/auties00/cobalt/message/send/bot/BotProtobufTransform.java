package com.github.auties00.cobalt.message.send.bot;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.bot.feedback.BotFeedbackMessage;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.context.ContextInfo;
import com.github.auties00.cobalt.model.message.context.ContextualMessage;
import com.github.auties00.cobalt.model.message.system.FutureProofMessage;
import com.github.auties00.cobalt.model.message.system.ProtocolMessage;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.util.Objects;

/**
 * Applies the bot-specific protobuf transforms required before encrypting a
 * message destined for a bot device. The transforms swap the user's message
 * secret with the bot-derived secret, strip quoted-message content for non-bot
 * participants, and convert PN JIDs to LID for FBID bots. Each method mutates
 * the supplied container in place via setters.
 */
@WhatsAppWebModule(moduleName = "WAWebE2EProtoGenerator")
public final class BotProtobufTransform {
    /**
     * Holds the store consulted for LID-to-phone lookups during the FBID bot
     * transforms.
     */
    private final WhatsAppStore store;

    /**
     * Constructs a transform bound to the given store.
     *
     * @param store the store providing JID lookups
     */
    public BotProtobufTransform(WhatsAppStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    /**
     * Applies the CAPI bot-invoke transform: replaces the user message secret
     * with the bot-derived secret, strips quoted-message content for non-bot
     * participants, and removes the {@code remoteJid} from protocol-message keys.
     *
     * @param container        the message container, mutated in place
     * @param botMessageSecret the derived bot message secret, or {@code null}
     *                         to merely clear the existing secret
     */
    @WhatsAppWebExport(moduleName = "WAWebE2EProtoGenerator", exports = "updateBotInvokeMsgProtoCopyForCapi",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public void transformForCapi(MessageContainer container, byte[] botMessageSecret) {
        container.messageContextInfo().ifPresent(info -> {
            info.setMessageSecret(null);
            info.setBotMessageSecret(botMessageSecret);
        });
        stripQuotedMessageForNonBot(container);
        stripProtocolMessageRemoteJid(container);
    }

    /**
     * Applies the FBID bot transform by converting the PN participant JID in
     * the quoted-message context info to LID.
     *
     * @param container the message container, mutated in place
     */
    @WhatsAppWebExport(moduleName = "WAWebE2EProtoGenerator", exports = "updateFbidBotProtobuf",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public void transformForFbidBot(MessageContainer container) {
        var contextInfo = resolveInnerContextInfo(container);
        if (contextInfo == null) {
            return;
        }

        var participant = contextInfo.quotedMessageSenderJid();
        if (participant.isEmpty() || participant.get().hasBotServer()) {
            return;
        }

        store.findLidByPhone(participant.get())
                .ifPresent(contextInfo::setQuotedMessageSenderJid);
    }

    /**
     * Applies the FBID bot-invoke transform by converting the protocol message
     * key's participant from PN to LID.
     *
     * @param container the message container, mutated in place
     */
    @WhatsAppWebExport(moduleName = "WAWebE2EProtoGenerator", exports = "updateFbidBotInvokeProtobuf",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public void transformForFbidBotInvoke(MessageContainer container) {
        if (!(container.content() instanceof ProtocolMessage pm)) {
            return;
        }

        var key = pm.key().orElse(null);
        if (key == null) {
            return;
        }

        var participant = key.senderJid().orElse(null);
        if (participant == null || participant.hasBotServer() || participant.hasLidServer()) {
            return;
        }

        store.findLidByPhone(participant).ifPresent(key::setSenderJid);
    }

    /**
     * Applies the generic bot transform by stripping {@code remoteJid} and
     * {@code participant} from protocol-message keys.
     *
     * @param container the message container, mutated in place
     */
    @WhatsAppWebExport(moduleName = "WAWebE2EProtoGenerator", exports = "updateBotProtobuf",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public void transformForBot(MessageContainer container) {
        if (!(container.content() instanceof ProtocolMessage pm)) {
            return;
        }

        pm.key().ifPresent(key -> {
            key.setParentJid(null);
            key.setSenderJid(null);
        });
    }

    /**
     * Returns the inner {@link ContextInfo}, transparently unwrapping the
     * {@link FutureProofMessage} botInvokeMessage wrapper.
     *
     * @param container the message container to inspect
     * @return the inner context info, or {@code null} when not present
     */
    private static ContextInfo resolveInnerContextInfo(MessageContainer container) {
        return container.content() instanceof ContextualMessage contextualMessage
                ? contextualMessage.contextInfo().orElse(null)
                : null;
    }

    /**
     * Strips the quoted message from the inner context info when the quoted
     * participant is not itself a bot.
     *
     * @param container the message container to mutate
     */
    private static void stripQuotedMessageForNonBot(MessageContainer container) {
        var contextInfo = resolveInnerContextInfo(container);
        if (contextInfo == null) {
            return;
        }

        var participant = contextInfo.quotedMessageSenderJid().orElse(null);
        if (participant == null || participant.hasBotServer()) {
            return;
        }

        contextInfo.clearQuotedMessage();
    }

    /**
     * Strips the {@code remoteJid} from protocol-message keys carried by the
     * container, including the optional bot-feedback message key.
     *
     * @param container the message container to mutate
     */
    private static void stripProtocolMessageRemoteJid(MessageContainer container) {
        if (!(container.content() instanceof ProtocolMessage pm)) {
            return;
        }

        pm.key().ifPresent(key -> key.setParentJid(null));
        pm.botFeedbackMessage()
                .flatMap(BotFeedbackMessage::messageKey)
                .ifPresent(key -> key.setParentJid(null));
    }
}
