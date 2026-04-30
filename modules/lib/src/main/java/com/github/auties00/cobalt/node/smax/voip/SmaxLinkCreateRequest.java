package com.github.auties00.cobalt.node.smax.voip;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the {@code <link_create/>}
 * payload in the canonical {@code <call to="call">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutVoipLinkCreateRequest")
public final class SmaxLinkCreateRequest implements SmaxOperation.Request {
    /**
     * The optional media type of the call to be created. Either
     * {@code "audio"} or {@code "video"} on the wire; modelled as a
     * raw string here for forward-compat.
     */
    private final String linkCreateMedia;

    /**
     * The optional call-creator device JID. Supplied when the caller
     * already knows which of its own devices will host the call.
     */
    private final Jid linkCreateCallCreator;

    /**
     * The optional pre-allocated call identifier. Used by clients
     * that issue {@code link_create} as a follow-up to an in-flight
     * call rather than a fresh call.
     */
    private final String linkCreateCallId;

    /**
     * The optional username the call link should display as the
     * creator. Surfaced in the join-prompt UI.
     */
    private final String linkCreateLinkCreatorUsername;

    /**
     * Whether the new link should have its waiting-room gate enabled
     * from the start. {@code true} maps to {@code waiting_room_enabled="1"}
     * on the wire; {@code false} omits the attribute entirely.
     */
    private final boolean linkCreateWaitingRoomEnabled;

    /**
     * The optional event-start timestamp. Supplied when the link is
     * created for a scheduled call rather than an immediate call.
     */
    private final Instant eventStartTime;

    /**
     * Constructs a request with every wire-level attribute spelled
     * out.
     *
     * @param linkCreateMedia              the optional media type;
     *                                     may be {@code null}
     * @param linkCreateCallCreator        the optional call-creator
     *                                     device JID; may be
     *                                     {@code null}
     * @param linkCreateCallId             the optional pre-allocated
     *                                     call id; may be {@code null}
     * @param linkCreateLinkCreatorUsername the optional creator
     *                                     username; may be
     *                                     {@code null}
     * @param linkCreateWaitingRoomEnabled whether the link should
     *                                     enable the waiting-room gate
     * @param eventStartTime               the optional event start
     *                                     instant; may be {@code null}
     */
    public SmaxLinkCreateRequest(String linkCreateMedia,
                   Jid linkCreateCallCreator,
                   String linkCreateCallId,
                   String linkCreateLinkCreatorUsername,
                   boolean linkCreateWaitingRoomEnabled,
                   Instant eventStartTime) {
        this.linkCreateMedia = linkCreateMedia;
        this.linkCreateCallCreator = linkCreateCallCreator;
        this.linkCreateCallId = linkCreateCallId;
        this.linkCreateLinkCreatorUsername = linkCreateLinkCreatorUsername;
        this.linkCreateWaitingRoomEnabled = linkCreateWaitingRoomEnabled;
        this.eventStartTime = eventStartTime;
    }

    /**
     * Returns the optional media type.
     *
     * @return an {@link Optional} carrying the media type, or empty
     *         when omitted
     */
    public Optional<String> linkCreateMedia() {
        return Optional.ofNullable(linkCreateMedia);
    }

    /**
     * Returns the optional call-creator device JID.
     *
     * @return an {@link Optional} carrying the device JID, or empty
     *         when omitted
     */
    public Optional<Jid> linkCreateCallCreator() {
        return Optional.ofNullable(linkCreateCallCreator);
    }

    /**
     * Returns the optional pre-allocated call identifier.
     *
     * @return an {@link Optional} carrying the call id, or empty when
     *         omitted
     */
    public Optional<String> linkCreateCallId() {
        return Optional.ofNullable(linkCreateCallId);
    }

    /**
     * Returns the optional creator username.
     *
     * @return an {@link Optional} carrying the username, or empty when
     *         omitted
     */
    public Optional<String> linkCreateLinkCreatorUsername() {
        return Optional.ofNullable(linkCreateLinkCreatorUsername);
    }

    /**
     * Returns whether the waiting-room gate should be enabled.
     *
     * @return {@code true} when the gate should be enabled,
     *         {@code false} otherwise
     */
    public boolean linkCreateWaitingRoomEnabled() {
        return linkCreateWaitingRoomEnabled;
    }

    /**
     * Returns the optional event-start instant.
     *
     * @return an {@link Optional} carrying the event start, or empty
     *         when omitted
     */
    public Optional<Instant> eventStartTime() {
        return Optional.ofNullable(eventStartTime);
    }

    /**
     * Builds the outbound stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the {@code <call>}
     *         envelope around a {@code <link_create/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutVoipLinkCreateRequest",
            exports = "makeLinkCreateRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var linkCreateBuilder = new NodeBuilder()
                .description("link_create");
        if (linkCreateMedia != null) {
            linkCreateBuilder.attribute("media", linkCreateMedia);
        }
        if (linkCreateCallCreator != null) {
            linkCreateBuilder.attribute("call-creator", linkCreateCallCreator);
        }
        if (linkCreateCallId != null) {
            linkCreateBuilder.attribute("call-id", linkCreateCallId);
        }
        if (linkCreateLinkCreatorUsername != null) {
            linkCreateBuilder.attribute("link_creator_username", linkCreateLinkCreatorUsername);
        }
        if (linkCreateWaitingRoomEnabled) {
            linkCreateBuilder.attribute("waiting_room_enabled", "1");
        }
        if (eventStartTime != null) {
            var eventNode = new NodeBuilder()
                    .description("event")
                    .attribute("start_time", eventStartTime.getEpochSecond())
                    .build();
            linkCreateBuilder.content(eventNode);
        }
        return new NodeBuilder()
                .description("call")
                .attribute("to", JidServer.call())
                .content(linkCreateBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxLinkCreateRequest) obj;
        return this.linkCreateWaitingRoomEnabled == that.linkCreateWaitingRoomEnabled
                && Objects.equals(this.linkCreateMedia, that.linkCreateMedia)
                && Objects.equals(this.linkCreateCallCreator, that.linkCreateCallCreator)
                && Objects.equals(this.linkCreateCallId, that.linkCreateCallId)
                && Objects.equals(this.linkCreateLinkCreatorUsername, that.linkCreateLinkCreatorUsername)
                && Objects.equals(this.eventStartTime, that.eventStartTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkCreateMedia, linkCreateCallCreator, linkCreateCallId,
                linkCreateLinkCreatorUsername, linkCreateWaitingRoomEnabled, eventStartTime);
    }

    @Override
    public String toString() {
        return "SmaxLinkCreateRequest[linkCreateMedia=" + linkCreateMedia
                + ", linkCreateCallCreator=" + linkCreateCallCreator
                + ", linkCreateCallId=" + linkCreateCallId
                + ", linkCreateLinkCreatorUsername=" + linkCreateLinkCreatorUsername
                + ", linkCreateWaitingRoomEnabled=" + linkCreateWaitingRoomEnabled
                + ", eventStartTime=" + eventStartTime + ']';
    }
}
