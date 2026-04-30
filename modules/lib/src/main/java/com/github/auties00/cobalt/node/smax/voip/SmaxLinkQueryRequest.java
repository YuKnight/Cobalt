package com.github.auties00.cobalt.node.smax.voip;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps a {@code <link_query token
 * media action/>} payload in the {@code <call to="call">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutVoipLinkQueryRequest")
public final class SmaxLinkQueryRequest implements SmaxOperation.Request {
    /**
     * The shareable call-link token to resolve.
     */
    private final String linkQueryToken;

    /**
     * The media type the caller intends to use; either {@code "audio"}
     * or {@code "video"} on the wire. Required by the relay so it can
     * gate-check the join attempt against the link's configured media.
     */
    private final String linkQueryMedia;

    /**
     * The optional action the caller is performing. Typically
     * {@code "preview"} for a passive resolve and {@code "edit"} for
     * a creator-side metadata edit.
     */
    private final String linkQueryAction;

    /**
     * Constructs a request.
     *
     * @param linkQueryToken  the call-link token; never {@code null}
     * @param linkQueryMedia  the media type; never {@code null}
     * @param linkQueryAction the optional action; may be {@code null}
     * @throws NullPointerException if {@code linkQueryToken} or
     *                              {@code linkQueryMedia} is
     *                              {@code null}
     */
    public SmaxLinkQueryRequest(String linkQueryToken, String linkQueryMedia, String linkQueryAction) {
        this.linkQueryToken = Objects.requireNonNull(linkQueryToken, "linkQueryToken cannot be null");
        this.linkQueryMedia = Objects.requireNonNull(linkQueryMedia, "linkQueryMedia cannot be null");
        this.linkQueryAction = linkQueryAction;
    }

    /**
     * Returns the call-link token.
     *
     * @return the token; never {@code null}
     */
    public String linkQueryToken() {
        return linkQueryToken;
    }

    /**
     * Returns the media type.
     *
     * @return the media type; never {@code null}
     */
    public String linkQueryMedia() {
        return linkQueryMedia;
    }

    /**
     * Returns the optional action.
     *
     * @return an {@link Optional} carrying the action, or empty when
     *         omitted
     */
    public Optional<String> linkQueryAction() {
        return Optional.ofNullable(linkQueryAction);
    }

    /**
     * Builds the outbound stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the {@code <call>}
     *         envelope around a {@code <link_query/>} payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutVoipLinkQueryRequest",
            exports = "makeLinkQueryRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var linkQueryBuilder = new NodeBuilder()
                .description("link_query")
                .attribute("token", linkQueryToken)
                .attribute("media", linkQueryMedia);
        if (linkQueryAction != null) {
            linkQueryBuilder.attribute("action", linkQueryAction);
        }
        return new NodeBuilder()
                .description("call")
                .attribute("to", JidServer.call())
                .content(linkQueryBuilder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxLinkQueryRequest) obj;
        return Objects.equals(this.linkQueryToken, that.linkQueryToken)
                && Objects.equals(this.linkQueryMedia, that.linkQueryMedia)
                && Objects.equals(this.linkQueryAction, that.linkQueryAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(linkQueryToken, linkQueryMedia, linkQueryAction);
    }

    @Override
    public String toString() {
        return "SmaxLinkQueryRequest[linkQueryToken=" + linkQueryToken
                + ", linkQueryMedia=" + linkQueryMedia
                + ", linkQueryAction=" + linkQueryAction + ']';
    }
}
