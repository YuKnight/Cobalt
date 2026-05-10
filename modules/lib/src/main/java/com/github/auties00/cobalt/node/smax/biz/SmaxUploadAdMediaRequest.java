package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the primary {@code <media/>}
 * child plus 0..10 {@code <media_list/>} children inside the canonical
 * {@code <iq xmlns="fb:thrift_iq" type="set" to="s.whatsapp.net">}
 * envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaNativeAdUploadAdMediaRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaNativeAdHackBaseIQSetRequestMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaNativeAdBaseIQSetRequestMixin")
public final class SmaxUploadAdMediaRequest implements SmaxOperation.Request {
    /**
     * The optional {@code from} attribute echoed onto the outbound IQ
     * via the {@code HackBaseIQSetRequestMixin}. The active user JID is
     * the only legal value; {@code null} omits the attribute.
     */
    private final Jid iqFrom;

    /**
     * The optional primary {@code <media/>} child; {@code null} omits
     * it.
     */
    private final SmaxUploadAdMediaMediaEntry media;

    /**
     * The list of {@code <media_list/>} children (0..10 entries).
     */
    private final List<SmaxUploadAdMediaMediaEntry> mediaList;

    /**
     * Constructs a request without a {@code from} echo, without a
     * primary {@code <media/>} child, and with an empty media list.
     */
    public SmaxUploadAdMediaRequest() {
        this(null, null, List.of());
    }

    /**
     * Constructs a request without a {@code from} echo.
     *
     * @param media     the optional primary media entry; may be
     *                  {@code null}
     * @param mediaList the list of media-list entries; never
     *                  {@code null}; must contain at most 10 entries
     * @throws NullPointerException     if {@code mediaList} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code mediaList} contains
     *                                  more than 10 entries
     */
    public SmaxUploadAdMediaRequest(SmaxUploadAdMediaMediaEntry media, List<SmaxUploadAdMediaMediaEntry> mediaList) {
        this(null, media, mediaList);
    }

    /**
     * Constructs a request, optionally echoing the supplied user JID
     * onto the {@code from} attribute via the
     * {@code HackBaseIQSetRequestMixin}.
     *
     * @param iqFrom    the optional user JID echoed onto the
     *                  {@code from} attribute; may be {@code null}
     * @param media     the optional primary media entry; may be
     *                  {@code null}
     * @param mediaList the list of media-list entries; never
     *                  {@code null}; must contain at most 10 entries
     * @throws NullPointerException     if {@code mediaList} is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code mediaList} contains
     *                                  more than 10 entries
     */
    public SmaxUploadAdMediaRequest(Jid iqFrom, SmaxUploadAdMediaMediaEntry media, List<SmaxUploadAdMediaMediaEntry> mediaList) {
        Objects.requireNonNull(mediaList, "mediaList cannot be null");
        if (mediaList.size() > 10) {
            throw new IllegalArgumentException("mediaList must contain at most 10 entries");
        }
        this.iqFrom = iqFrom;
        this.media = media;
        this.mediaList = List.copyOf(mediaList);
    }

    /**
     * Returns the optional {@code from} echo.
     *
     * @return an {@link Optional} carrying the user JID, or empty when
     *         no echo was supplied
     */
    public Optional<Jid> iqFrom() {
        return Optional.ofNullable(iqFrom);
    }

    /**
     * Returns the optional primary media entry.
     *
     * @return an {@link Optional} carrying the entry, or empty when
     *         the {@code <media/>} child was omitted
     */
    public Optional<SmaxUploadAdMediaMediaEntry> media() {
        return Optional.ofNullable(media);
    }

    /**
     * Returns the additional media-list entries.
     *
     * @return an unmodifiable list of 0..10 entries; never
     *         {@code null}
     */
    public List<SmaxUploadAdMediaMediaEntry> mediaList() {
        return mediaList;
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope and
     *         the media payload
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaNativeAdUploadAdMediaRequest",
            exports = "makeUploadAdMediaRequest", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaNativeAdUploadAdMediaRequest",
            exports = "makeUploadAdMediaRequestMedia", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaNativeAdUploadAdMediaRequest",
            exports = "makeUploadAdMediaRequestMediaList", adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaNativeAdHackBaseIQSetRequestMixin",
            exports = "mergeHackBaseIQSetRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaNativeAdBaseIQSetRequestMixin",
            exports = "mergeBaseIQSetRequestMixin", adaptation = WhatsAppAdaptation.ADAPTED)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        if (media != null) {
            // WASmaxOutBizCtwaNativeAdUploadAdMediaRequest.makeUploadAdMediaRequestMedia: smax("media", {id: CUSTOM_STRING(t), type: CUSTOM_STRING(n)})
            var mediaNode = new NodeBuilder()
                    .description("media")
                    .attribute("id", media.id())
                    .attribute("type", media.type().wire())
                    .build();
            children.add(mediaNode);
        }
        for (var entry : mediaList) {
            // WASmaxOutBizCtwaNativeAdUploadAdMediaRequest.makeUploadAdMediaRequestMediaList: smax("media_list", {id: CUSTOM_STRING(t), type: CUSTOM_STRING(n)})
            var entryNode = new NodeBuilder()
                    .description("media_list")
                    .attribute("id", entry.id())
                    .attribute("type", entry.type().wire())
                    .build();
            children.add(entryNode);
        }
        var builder = new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq") // WASmaxOutBizCtwaNativeAdUploadAdMediaRequest.makeUploadAdMediaRequest: smax("iq", {xmlns: "fb:thrift_iq", smax_id: INT(74)})
                .attribute("to", JidServer.user()) // WASmaxOutBizCtwaNativeAdHackBaseIQSetRequestMixin.mergeHackBaseIQSetRequestMixin: to: WAWap.S_WHATSAPP_NET ("s.whatsapp.net")
                .attribute("type", "set") // WASmaxOutBizCtwaNativeAdBaseIQSetRequestMixin.mergeBaseIQSetRequestMixin: stamps type="set" via WASmaxMixins.mergeStanzas; id is added by the central IQ dispatch pipeline (WAWap.generateId())
                .content(children);
        if (iqFrom != null) {
            builder.attribute("from", iqFrom); // WASmaxOutBizCtwaNativeAdHackBaseIQSetRequestMixin.mergeHackBaseIQSetRequestMixin: from: OPTIONAL(USER_JID, t.iqFrom)
        }
        return builder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxUploadAdMediaRequest) obj;
        return Objects.equals(this.iqFrom, that.iqFrom)
                && Objects.equals(this.media, that.media)
                && Objects.equals(this.mediaList, that.mediaList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iqFrom, media, mediaList);
    }

    @Override
    public String toString() {
        return "SmaxUploadAdMediaRequest[iqFrom=" + iqFrom
                + ", media=" + media
                + ", mediaList=" + mediaList + ']';
    }
}
