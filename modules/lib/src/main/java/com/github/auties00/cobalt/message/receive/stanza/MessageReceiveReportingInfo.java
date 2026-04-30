package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds the reporting token metadata parsed from the {@code <reporting>} child of an
 * incoming message stanza.
 *
 * <p>Reporting tokens let the content moderation system cryptographically verify that
 * a reported message was actually received by the reporter. The token and tag are
 * stored alongside the message so they remain available when the user reports it later.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceiveReportingInfo {
    /**
     * Stanza timestamp captured alongside the reporting token for correlation with
     * the original message.
     */
    private final Instant stanzaTs;

    /**
     * Reporting token bytes from the {@code <reporting_token>} child.
     */
    private final byte[] reportingToken;

    /**
     * Reporting token version from the {@code v} attribute of the token child.
     */
    private final int version;

    /**
     * Reporting tag bytes from the {@code <reporting_tag>} child, acting as an
     * authenticator for the reporting token.
     */
    private final byte[] reportingTag;

    /**
     * Constructs a new reporting info record.
     *
     * @param stanzaTs       the stanza timestamp, never {@code null}
     * @param reportingToken the reporting token bytes, or {@code null}
     * @param version        the reporting token version
     * @param reportingTag   the reporting tag bytes, or {@code null}
     * @throws NullPointerException if {@code stanzaTs} is {@code null}
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
     */
    public Instant stanzaTs() {
        return stanzaTs;
    }

    /**
     * Returns the reporting token bytes, when present.
     *
     * @return an {@link Optional} wrapping the reporting token bytes
     */
    public Optional<byte[]> reportingToken() {
        return Optional.ofNullable(reportingToken);
    }

    /**
     * Returns the reporting token version.
     *
     * @return the version number
     */
    public int version() {
        return version;
    }

    /**
     * Returns the reporting tag bytes, when present.
     *
     * @return an {@link Optional} wrapping the reporting tag bytes
     */
    public Optional<byte[]> reportingTag() {
        return Optional.ofNullable(reportingTag);
    }
}
