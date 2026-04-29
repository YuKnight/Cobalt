package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound stanza variant.
 */
public final class IqQueryProductListCatalogRequest implements IqOperation.Request {
    /**
     * The merchant catalog JID being queried.
     */
    private final Jid catalogJid;

    /**
     * The list of product ids to fetch — non-empty.
     */
    private final List<String> productIds;

    /**
     * The requested image width (in pixels).
     */
    private final int width;

    /**
     * The requested image height (in pixels).
     */
    private final int height;

    /**
     * The optional direct-connection encrypted info blob.
     */
    private final String directConnectionEncryptedInfo;

    /**
     * Constructs a request.
     *
     * @param catalogJid                    the catalog JID; never
     *                                      {@code null}
     * @param productIds                    the product ids; never
     *                                      {@code null} and must be
     *                                      non-empty
     * @param width                         the requested image width
     * @param height                        the requested image height
     * @param directConnectionEncryptedInfo the optional direct-connection
     *                                      blob; may be {@code null}
     * @throws NullPointerException     if {@code catalogJid} or
     *                                  {@code productIds} is
     *                                  {@code null}
     * @throws IllegalArgumentException when {@code productIds} is empty
     */
    public IqQueryProductListCatalogRequest(Jid catalogJid,
                   List<String> productIds,
                   int width,
                   int height,
                   String directConnectionEncryptedInfo) {
        this.catalogJid = Objects.requireNonNull(catalogJid, "catalogJid cannot be null");
        Objects.requireNonNull(productIds, "productIds cannot be null");
        if (productIds.isEmpty()) {
            throw new IllegalArgumentException("productIds cannot be empty");
        }
        this.productIds = List.copyOf(productIds);
        this.width = width;
        this.height = height;
        this.directConnectionEncryptedInfo = directConnectionEncryptedInfo;
    }

    /**
     * Returns the catalog JID.
     *
     * @return the JID; never {@code null}
     */
    public Jid catalogJid() {
        return catalogJid;
    }

    /**
     * Returns the requested product ids.
     *
     * @return an unmodifiable list; never {@code null}
     */
    public List<String> productIds() {
        return productIds;
    }

    /**
     * Returns the requested image width.
     *
     * @return the width
     */
    public int width() {
        return width;
    }

    /**
     * Returns the requested image height.
     *
     * @return the height
     */
    public int height() {
        return height;
    }

    /**
     * Returns the direct-connection encrypted info blob.
     *
     * @return an {@link Optional} carrying the blob
     */
    public Optional<String> directConnectionEncryptedInfo() {
        return Optional.ofNullable(directConnectionEncryptedInfo);
    }

    /**
     * Builds the outbound IQ stanza ready for dispatch.
     *
     * @return a {@link NodeBuilder} carrying the IQ envelope
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebQueryProductListCatalogJob",
            exports = "QueryProductListCatalog", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var children = new ArrayList<Node>();
        for (var id : productIds) {
            var idNode = new NodeBuilder()
                    .description("id")
                    .content(id)
                    .build();
            children.add(new NodeBuilder()
                    .description("product")
                    .content(idNode)
                    .build());
        }
        children.add(new NodeBuilder()
                .description("width")
                .content(String.valueOf(width))
                .build());
        children.add(new NodeBuilder()
                .description("height")
                .content(String.valueOf(height))
                .build());
        if (directConnectionEncryptedInfo != null) {
            children.add(new NodeBuilder()
                    .description("direct_connection_encrypted_info")
                    .content(directConnectionEncryptedInfo)
                    .build());
        }
        var productListNode = new NodeBuilder()
                .description("product_list")
                .attribute("jid", catalogJid)
                .content(children)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "w:biz:catalog")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(productListNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqQueryProductListCatalogRequest) obj;
        return this.width == that.width
                && this.height == that.height
                && Objects.equals(this.catalogJid, that.catalogJid)
                && Objects.equals(this.productIds, that.productIds)
                && Objects.equals(this.directConnectionEncryptedInfo, that.directConnectionEncryptedInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalogJid, productIds, width, height, directConnectionEncryptedInfo);
    }

    @Override
    public String toString() {
        return "IqQueryProductListCatalogRequest[catalogJid=" + catalogJid
                + ", productIds=" + productIds + ", width=" + width
                + ", height=" + height + ']';
    }
}
