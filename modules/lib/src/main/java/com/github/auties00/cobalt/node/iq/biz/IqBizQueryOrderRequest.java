package com.github.auties00.cobalt.node.iq.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.iq.IqOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound {@code <iq xmlns="fb:thrift_iq" type="get">} stanza that
 * fetches the typed detail of a single business order, identified by id
 * and authenticated via a merchant-supplied token.
 */
@WhatsAppWebModule(moduleName = "WAWebBizQueryOrderJob")
public final class IqBizQueryOrderRequest implements IqOperation.Request {
    /**
     * The order id to fetch.
     */
    private final String orderId;

    /**
     * The requested image width (in pixels).
     */
    private final int width;

    /**
     * The requested image height (in pixels).
     */
    private final int height;

    /**
     * The merchant-issued order authentication token.
     */
    private final String token;

    /**
     * The optional direct-connection encrypted info blob.
     */
    private final String directConnectionEncryptedInfo;

    /**
     * Constructs a request.
     *
     * @param orderId                       the order id; never
     *                                      {@code null}
     * @param width                         the requested image width
     * @param height                        the requested image height
     * @param token                         the order auth token; never
     *                                      {@code null}
     * @param directConnectionEncryptedInfo the optional direct-connection
     *                                      blob; may be {@code null}
     * @throws NullPointerException if {@code orderId} or {@code token}
     *                              is {@code null}
     */
    public IqBizQueryOrderRequest(String orderId, int width, int height, String token,
                   String directConnectionEncryptedInfo) {
        this.orderId = Objects.requireNonNull(orderId, "orderId cannot be null");
        this.width = width;
        this.height = height;
        this.token = Objects.requireNonNull(token, "token cannot be null");
        this.directConnectionEncryptedInfo = directConnectionEncryptedInfo;
    }

    /**
     * Returns the order id.
     *
     * @return the id; never {@code null}
     */
    public String orderId() {
        return orderId;
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
     * Returns the order auth token.
     *
     * @return the token; never {@code null}
     */
    public String token() {
        return token;
    }

    /**
     * Returns the direct-connection blob, when supplied.
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
    @WhatsAppWebExport(moduleName = "WAWebBizQueryOrderJob",
            exports = "queryOrder", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var widthNode = new NodeBuilder()
                .description("width")
                .content(String.valueOf(width))
                .build();
        var heightNode = new NodeBuilder()
                .description("height")
                .content(String.valueOf(height))
                .build();
        var imageDimensionsNode = new NodeBuilder()
                .description("image_dimensions")
                .content(List.of(widthNode, heightNode))
                .build();
        var tokenNode = new NodeBuilder()
                .description("token")
                .content(token)
                .build();
        var orderChildren = new ArrayList<Node>();
        orderChildren.add(imageDimensionsNode);
        orderChildren.add(tokenNode);
        if (directConnectionEncryptedInfo != null) {
            orderChildren.add(new NodeBuilder()
                    .description("direct_connection_encrypted_info")
                    .content(directConnectionEncryptedInfo)
                    .build());
        }
        var orderNode = new NodeBuilder()
                .description("order")
                .attribute("op", "get")
                .attribute("id", orderId)
                .content(orderChildren)
                .build();
        return new NodeBuilder()
                .description("iq")
                .attribute("xmlns", "fb:thrift_iq")
                .attribute("to", JidServer.user())
                .attribute("type", "get")
                .content(orderNode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (IqBizQueryOrderRequest) obj;
        return this.width == that.width
                && this.height == that.height
                && Objects.equals(this.orderId, that.orderId)
                && Objects.equals(this.token, that.token)
                && Objects.equals(this.directConnectionEncryptedInfo, that.directConnectionEncryptedInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, width, height, token, directConnectionEncryptedInfo);
    }

    @Override
    public String toString() {
        return "IqBizQueryOrderRequest[orderId=" + orderId + ", width=" + width
                + ", height=" + height + ']';
    }
}
