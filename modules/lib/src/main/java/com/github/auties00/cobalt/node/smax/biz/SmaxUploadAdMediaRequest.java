package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant. Wraps the primary {@code <media/>}
 * child plus 0..10 {@code <media_list/>} children inside the canonical
 * {@code <iq xmlns="fb:thrift_iq" type="set">} envelope.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaNativeAdUploadAdMediaRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutBizCtwaNativeAdHackBaseIQSetRequestMixin")
public final class SmaxUploadAdMediaRequest implements SmaxOperation.Request {
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
     * Constructs a request without a primary {@code <media/>} child
     * and an empty media list.
     */
    public SmaxUploadAdMediaRequest() {
        this(null, List.of());
    }

    /**
     * Constructs a request.
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
        Objects.requireNonNull(mediaList, "mediaList cannot be null");
        if (mediaList.size() > 10) {
            throw new IllegalArgumentException("mediaList must contain at most 10 entries");
        }
        this.media = media;
        this.mediaList = List.copyOf(mediaList);
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
     *
     * @implNote {@code WASmaxOutBizCtwaNativeAdUploadAdMediaRequest.makeUploadAdMediaRequest}
     *           composes
     *           {@code WASmaxOutBizCtwaNativeAdHackBaseIQSetRequestMixin}
     *           ({@code id=generateId()}, {@code type="set"}) over a
     *           bare {@code <iq xmlns="fb:thrift_iq" smax_id=74>} root
     *           that optionally carries a single
     *           {@code <media id type/>} child followed by 0..10
     *           {@code <media_list id type/>} children.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutBizCtwaNativeAdUploadAdMediaRequest",
            exports = "makeUploadAdMediaRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        if (media != null) {
            var mediaNode = new NodeBuilder()
                    .description("media")
                    .attribute("id", media.id())
                    .attribute("type", media.type().wire())
                    .build();
            children.add(mediaNode);
        }
        for (var entry : mediaList) {
            var entryNode = new NodeBuilder()
                    .description("media_list")
                    .attribute("id", entry.id())
                    .attribute("type", entry.type().wire())
                    .build();
            children.add(entryNode);
        }
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("type", "set")
                .content(children);
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
        return Objects.equals(this.media, that.media)
                && Objects.equals(this.mediaList, that.mediaList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(media, mediaList);
    }

    @Override
    public String toString() {
        return "SmaxUploadAdMediaRequest[media=" + media
                + ", mediaList=" + mediaList + ']';
    }
}
