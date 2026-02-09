package com.github.auties00.cobalt.message.receive.stanza;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Parsed reporting token information from the {@code <reporting>} child
 * node of an incoming message stanza.
 *
 * <p>Reporting tokens are used by WhatsApp's content moderation system.
 * When present, they must be validated and stored alongside the message
 * for potential future reporting.
 *
 * @apiNote WAWebHandleMsgParser function I(): parses the reporting node
 * to extract reporting_token (bytes + version) and reporting_tag (bytes).
 */
public final class MessageReceiveReportingInfo {
    private final byte[] reportingToken;
    private final int version;
    private final byte[] reportingTag;

    public MessageReceiveReportingInfo(
            byte[] reportingToken,
            int version,
            byte[] reportingTag
    ) {
        this.reportingToken = reportingToken;
        this.version = version;
        this.reportingTag = reportingTag;
    }

    public Optional<byte[]> reportingToken() {
        return Optional.ofNullable(reportingToken);
    }

    public int version() {
        return version;
    }

    public Optional<byte[]> reportingTag() {
        return Optional.ofNullable(reportingTag);
    }
}
