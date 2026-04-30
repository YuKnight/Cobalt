package com.github.auties00.cobalt.node.smax.inappcomms;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the {@code <event/>} payload
 * in the canonical {@code <iq xmlns="w:comms" type="set"
 * to="s.whatsapp.net">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutInAppCommsEventRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutInAppCommsBaseIQSetRequestMixin")
public final class SmaxInAppCommsEventRequest implements SmaxOperation.Request {
    /**
     * The promotion id the event is being reported against.
     */
    private final String eventPromotionId;

    /**
     * The event type. The discriminator that tells the analytics
     * pipeline what kind of interaction happened (impression, click,
     * dismiss, etc.).
     */
    private final String eventType;

    /**
     * The event timestamp in seconds since epoch.
     */
    private final long eventTimestampSec;

    /**
     * The event-specific opaque log payload.
     */
    private final String eventLogdata;

    /**
     * Constructs a request.
     *
     * @param eventPromotionId  the promotion id. Never {@code null}
     * @param eventType         the event type. Never {@code null}
     * @param eventTimestampSec the event timestamp in seconds
     * @param eventLogdata      the event log payload. Never
     *                          {@code null}
     * @throws NullPointerException if any string argument is
     *                              {@code null}
     */
    public SmaxInAppCommsEventRequest(String eventPromotionId, String eventType,
                   long eventTimestampSec, String eventLogdata) {
        this.eventPromotionId = Objects.requireNonNull(eventPromotionId, "eventPromotionId cannot be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType cannot be null");
        this.eventTimestampSec = eventTimestampSec;
        this.eventLogdata = Objects.requireNonNull(eventLogdata, "eventLogdata cannot be null");
    }

    /**
     * Returns the promotion id.
     *
     * @return the promotion id. Never {@code null}
     */
    public String eventPromotionId() {
        return eventPromotionId;
    }

    /**
     * Returns the event type.
     *
     * @return the event type. Never {@code null}
     */
    public String eventType() {
        return eventType;
    }

    /**
     * Returns the event timestamp in seconds.
     *
     * @return the timestamp
     */
    public long eventTimestampSec() {
        return eventTimestampSec;
    }

    /**
     * Returns the event log payload.
     *
     * @return the log payload. Never {@code null}
     */
    public String eventLogdata() {
        return eventLogdata;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and the
     *         {@code <event/>} child
     *
     * @implNote {@code WASmaxOutInAppCommsEventRequest.makeEventRequest}
     *           composes
     *           {@code WASmaxOutInAppCommsBaseIQSetRequestMixin}
     *           ({@code id=generateId()}, {@code type="set"}) over
     *           {@code <iq xmlns="w:comms" to="s.whatsapp.net">}
     *           with the {@code <event/>} child carrying every
     *           field as a {@code CUSTOM_STRING}/{@code INT}
     *           attribute.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutInAppCommsEventRequest",
            exports = "makeEventRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var eventNode = new NodeBuilder()
                .description("event")
                .attribute("promotion_id", eventPromotionId)
                .attribute("type", eventType)
                .attribute("timestamp_sec", eventTimestampSec)
                .attribute("logdata", eventLogdata)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:comms")
                .attribute("to", JidServer.user())
                .attribute("type", "set")
                .content(eventNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxInAppCommsEventRequest) obj;
        return this.eventTimestampSec == that.eventTimestampSec
                && Objects.equals(this.eventPromotionId, that.eventPromotionId)
                && Objects.equals(this.eventType, that.eventType)
                && Objects.equals(this.eventLogdata, that.eventLogdata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventPromotionId, eventType, eventTimestampSec, eventLogdata);
    }

    @Override
    public String toString() {
        return "SmaxInAppCommsEventRequest[eventPromotionId=" + eventPromotionId
                + ", eventType=" + eventType
                + ", eventTimestampSec=" + eventTimestampSec
                + ", eventLogdata=" + eventLogdata + ']';
    }
}
