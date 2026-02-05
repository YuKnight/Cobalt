package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.message.send.token.ReportingToken;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.MessageContainer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.props.ABPropsService;

import java.util.Objects;

/**
 * Builds reporting token nodes for message stanzas.
 * <p>
 * Wraps the reporting token in the proper node structure for inclusion
 * in the message stanza.
 *
 * @apiNote WAWebReportingTokenUtils.genReportingTokenBody
 */
public final class ReportingNode {
    private static final int AB_PROP_SENDER_REPORTING_TOKEN_VERSION = 8860;
    private static final int DEFAULT_REPORTING_TOKEN_VERSION = 1;

    private final ReportingToken reportingToken;
    private final ABPropsService abPropsService;

    public ReportingNode(ReportingToken reportingToken, ABPropsService abPropsService) {
        this.reportingToken = Objects.requireNonNull(reportingToken, "reportingToken cannot be null");
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService cannot be null");
    }

    /**
     * Builds a reporting node for a message.
     *
     * @param messageId     the message ID
     * @param message       the message container
     * @param messageSecret the message secret bytes
     * @param senderJid     the sender JID
     * @param remoteJid     the remote JID
     * @return the reporting node, or null if not applicable
     *
     * @apiNote WAWebReportingTokenUtils.genReportingTokenBody
     */
    public Node build(
            String messageId,
            MessageContainer message,
            byte[] messageSecret,
            Jid senderJid,
            Jid remoteJid
    ) {
        var token = reportingToken.generate(messageId, message, messageSecret, senderJid, remoteJid);
        if (token == null) {
            return null;
        }

        var version = getReportingTokenVersion();

        var reportingTokenNode = new NodeBuilder()
                .description("reporting_token")
                .attribute("v", String.valueOf(version))
                .content(token)
                .build();

        return new NodeBuilder()
                .description("reporting")
                .content(reportingTokenNode)
                .build();
    }

    private int getReportingTokenVersion() {
        return abPropsService.getInt(AB_PROP_SENDER_REPORTING_TOKEN_VERSION)
                .orElse(DEFAULT_REPORTING_TOKEN_VERSION);
    }
}
