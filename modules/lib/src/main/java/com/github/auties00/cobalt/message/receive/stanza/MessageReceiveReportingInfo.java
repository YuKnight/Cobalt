package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds the reporting token metadata parsed from the {@code <reporting>}
 * child of an incoming message stanza.
 *
 * <p>Reporting tokens let WhatsApp's content moderation system
 * cryptographically verify that a reported message was actually
 * received by the reporter. When a user reports a message, the token
 * and tag are attached to the report so the server can confirm the
 * content was genuine. Cobalt stores these tokens alongside the
 * message so they remain available for future reporting.
 *
 * @implNote WAWebHandleMsgParser function k(): parses the reporting
 * node to extract reporting_token (bytes plus version),
 * reporting_tag (bytes), and stanzaTs (timestamp from the parent
 * stanza's {@code t} attribute).
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceiveReportingInfo {
    /**
     * The stanza timestamp, captured alongside the reporting token for
     * correlation with the original message.
     *
     * @implNote WAWebHandleMsgParser function k(): {@code stanzaTs} from
     * {@code e.attrTime("t")}.
     */
    private final Instant stanzaTs;

    /**
     * The reporting token bytes from the {@code <reporting_token>} child,
     * cryptographically binding the message to the server-issued token.
     *
     * @implNote WAWebHandleMsgParser function k(): {@code reportingToken}
     * from {@code r.contentBytes()}.
     */
    private final byte[] reportingToken;

    /**
     * The reporting token version from the {@code v} attribute of the
     * token child.
     *
     * @implNote WAWebHandleMsgParser function k(): {@code version} from
     * {@code r.attrInt("v")}.
     */
    private final int version;

    /**
     * The reporting tag bytes from the {@code <reporting_tag>} child,
     * acting as an authenticator for the reporting token.
     *
     * @implNote WAWebHandleMsgParser function k(): {@code reportingTag}
     * from {@code a.contentBytes()}.
     */
    private final byte[] reportingTag;

    /**
     * Constructs a new reporting info record with all parsed fields.
     *
     * @param stanzaTs       the stanza timestamp, never {@code null}
     * @param reportingToken the reporting token bytes, or {@code null}
     * @param version        the reporting token version
     * @param reportingTag   the reporting tag bytes, or {@code null}
     *
     * @throws NullPointerException if {@code stanzaTs} is {@code null}
     *
     * @implNote WAWebHandleMsgParser function k(): builds the reporting
     * info object from the reporting node and its children.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public MessageReceiveReportingInfo(
            Instant stanzaTs,
            byte[] reportingToken,
            int version,
            byte[] reportingTag
    ) {
        this.stanzaTs = Objects.requireNonNull(stanzaTs, "stanzaTs cannot be null");
        this.reportingToken = reportingToken;
        this.version = version;
        this.reportingTag = reportingTag;
    }

    /**
     * Returns the stanza timestamp preserved for reporting correlation.
     *
     * @return the timestamp, never {@code null}
     * @implNote WAWebHandleMsgParser function k(): {@code stanzaTs}.
     */
    public Instant stanzaTs() {
        return stanzaTs;
    }

    /**
     * Returns the reporting token bytes, when present.
     *
     * @return an {@link Optional} wrapping the reporting token bytes
     * @implNote WAWebHandleMsgParser function k(): {@code reportingToken}.
     */
    public Optional<byte[]> reportingToken() {
        return Optional.ofNullable(reportingToken);
    }

    /**
     * Returns the reporting token version.
     *
     * @return the version number
     * @implNote WAWebHandleMsgParser function k(): {@code version}.
     */
    public int version() {
        return version;
    }

    /**
     * Returns the reporting tag bytes, when present.
     *
     * @return an {@link Optional} wrapping the reporting tag bytes
     * @implNote WAWebHandleMsgParser function k(): {@code reportingTag}.
     */
    public Optional<byte[]> reportingTag() {
        return Optional.ofNullable(reportingTag);
    }
}
