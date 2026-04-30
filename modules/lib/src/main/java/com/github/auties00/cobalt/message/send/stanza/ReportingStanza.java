package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.message.send.token.ReportingToken;
import com.github.auties00.cobalt.message.send.token.ReportingTokenContent;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.Message;
import com.github.auties00.cobalt.model.message.MessageContainerSpec;
import com.github.auties00.cobalt.model.message.event.EncEventResponseMessage;
import com.github.auties00.cobalt.model.message.security.EncReactionMessage;
import com.github.auties00.cobalt.model.message.poll.PollUpdateMessage;
import com.github.auties00.cobalt.model.message.text.ReactionMessage;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;

import java.security.GeneralSecurityException;
import java.util.Objects;

/**
 * Builds the {@code <reporting>} stanza child node carrying the reporting token
 * (franking tag) used for message integrity verification.
 *
 * <p>Reporting tokens are emitted only when the
 * {@code rt_sender_reporting_token_version} AB prop is greater than zero, the message
 * type is compatible (not a reaction, poll vote, or event response), and the message
 * carries a {@code messageSecret}.
 */
@WhatsAppWebModule(moduleName = "WAWebReportingTokenUtils")
@WhatsAppWebModule(moduleName = "WAWebMessagingGatingUtils")
@WhatsAppWebModule(moduleName = "WAWebMessagePluginGenerateReportingTokenContent")
public final class ReportingStanza {
    /**
     * Logger for reporting token generation failures.
     */
    private static final System.Logger LOGGER = System.getLogger(ReportingStanza.class.getName());

    /**
     * AB props service used to query the sender reporting token version.
     */
    private final ABPropsService abPropsService;

    /**
     * Creates a new reporting stanza builder.
     *
     * @param abPropsService the AB props service for version lookup
     * @throws NullPointerException if {@code abPropsService} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebReportingTokenUtils", exports = "genReportingTokenBody",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public ReportingStanza(ABPropsService abPropsService) {
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService");
    }

    /**
     * Builds the {@code <reporting>} node for the given message.
     *
     * <p>Returns {@code null} when reporting tokens are disabled, the message type is
     * incompatible, or the message has no {@code messageSecret}.
     *
     * @param messageInfo the outgoing message
     * @param selfJid     the sender's user JID
     * @param remoteJid   the remote JID (recipient for 1:1, group JID for groups,
     *                    status JID for broadcasts)
     * @return the reporting node, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebReportingTokenUtils", exports = "genReportingTokenBody",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node build(ChatMessageInfo messageInfo, Jid selfJid, Jid remoteJid) {
        var senderVersion = getSenderReportingTokenVersion();
        if (!isReportingTokenSendingEnabled(senderVersion)) {
            return null;
        }

        var message = messageInfo.message().content();
        if (!isMsgTypeCompatible(message)) {
            return null;
        }

        var messageSecret = messageInfo.messageSecret().orElse(null);
        if (messageSecret == null) {
            return null;
        }

        // Compute the sparse copy of the encoded MessageContainer that contains only
        // the field numbers whitelisted by REPORTING_TOKEN_CONFIG_BASE64 for the
        // current sender version.
        var fullProto = MessageContainerSpec.encode(messageInfo.message());
        var serializedProto = ReportingTokenContent.compute(fullProto, senderVersion);

        var id = messageInfo.key().id();
        if (id.isEmpty()) {
            return null;
        }

        try {
            var reportingToken = ReportingToken.generate(
                    messageSecret,
                    id.get(),
                    selfJid.toUserJid(),
                    remoteJid.toUserJid(),
                    serializedProto,
                    senderVersion
            );
            if (reportingToken.isEmpty()) {
                return null;
            }

            var reportingBody = new NodeBuilder()
                    .description("reporting_token")
                    .attribute("v", String.valueOf(reportingToken.get().version()))
                    .content(reportingToken.get().token())
                    .build();
            return new NodeBuilder()
                    .description("reporting")
                    .content(reportingBody)
                    .build();
        } catch (GeneralSecurityException e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to generate reporting token: {0}", e.getMessage());
            return null;
        }
    }

    /**
     * Returns the sender reporting token version that the client should advertise when
     * generating outgoing reporting tokens.
     *
     * <p>A value of zero or less disables reporting-token generation entirely. The
     * integer is consumed by {@link ReportingToken#generate} to select the HMAC
     * key-derivation scheme used for the token.
     *
     * @return the sender reporting token version
     */
    @WhatsAppWebExport(moduleName = "WAWebMessagingGatingUtils",
            exports = "getSenderReportingTokenVersion", adaptation = WhatsAppAdaptation.DIRECT)
    int getSenderReportingTokenVersion() {
        return abPropsService.getInt(ABProp.RT_SENDER_REPORTING_TOKEN_VERSION);
    }

    /**
     * Returns whether reporting-token generation is enabled for outgoing messages.
     *
     * <p>WA Web defines this gate as
     * {@code getSenderReportingTokenVersion() > 0}; a non-zero version selects the HMAC
     * scheme, zero disables generation.
     *
     * @param senderVersion the sender reporting token version
     * @return {@code true} when the sender version is strictly positive
     */
    @WhatsAppWebExport(moduleName = "WAWebMessagingGatingUtils",
            exports = "isReportingTokenSendingEnabled", adaptation = WhatsAppAdaptation.DIRECT)
    static boolean isReportingTokenSendingEnabled(int senderVersion) {
        return senderVersion > 0;
    }

    /**
     * Returns whether the message type is compatible with reporting tokens.
     *
     * <p>Reactions, encrypted reactions, encrypted event responses and poll vote
     * updates are excluded.
     *
     * @param message the message content to check
     * @return {@code true} if the message type supports reporting tokens
     */
    @WhatsAppWebExport(moduleName = "WAWebMessagePluginGenerateReportingTokenContent",
            exports = "isMsgTypeReportingTokenCompatible", adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean isMsgTypeCompatible(Message message) {
        return switch (message) {
            case ReactionMessage _, PollUpdateMessage _, EncReactionMessage _, EncEventResponseMessage _ -> false;
            case null, default -> true;
        };
    }
}
