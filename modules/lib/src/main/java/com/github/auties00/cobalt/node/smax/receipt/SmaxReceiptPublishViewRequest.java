package com.github.auties00.cobalt.node.smax.receipt;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound {@code <receipt type="view">} stanza variant.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutReceiptPublishViewRequest")
@WhatsAppWebModule(moduleName = "WASmaxOutReceiptSenderAggregatedViewPublishMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutReceiptViewTypeMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutReceiptNewsletterMixin")
@WhatsAppWebModule(moduleName = "WASmaxOutReceiptStatusClassMixin")
public final class SmaxReceiptPublishViewRequest implements SmaxOperation.Request {
    /**
     * The opaque stanza id used as the receipt's {@code id} attribute.
     * Usually the id of the original incoming {@code <message>}
     * being acknowledged.
     */
    private final String receiptId;

    /**
     * The recipient JID of the acknowledgement. Typically a
     * newsletter / status JID.
     */
    private final Jid receiptTo;

    /**
     * When {@code true} the receipt carries
     * {@code class="status"}, marking the ack as a status-broadcast
     * view receipt rather than a regular newsletter view receipt.
     */
    private final boolean hasStatusClass;

    /**
     * The list of {@code <item server_id=INT/>} entries. Between
     * {@code 0} and {@code 255} entries.
     */
    private final List<Integer> itemServerIds;

    /**
     * Constructs a new view-receipt request.
     *
     * @param receiptId      the stanza id. Never {@code null}
     * @param receiptTo      the recipient JID. Never {@code null}
     * @param hasStatusClass whether to emit {@code class="status"}
     * @param itemServerIds  the list of server ids. Never
     *                       {@code null}, between {@code 0} and
     *                       {@code 255} entries
     * @throws NullPointerException     if any required argument is
     *                                  {@code null}
     * @throws IllegalArgumentException if {@code itemServerIds}
     *                                  carries more than {@code 255}
     *                                  entries
     */
    public SmaxReceiptPublishViewRequest(String receiptId, Jid receiptTo, boolean hasStatusClass,
                   List<Integer> itemServerIds) {
        this.receiptId = Objects.requireNonNull(receiptId, "receiptId cannot be null");
        this.receiptTo = Objects.requireNonNull(receiptTo, "receiptTo cannot be null");
        Objects.requireNonNull(itemServerIds, "itemServerIds cannot be null");
        if (itemServerIds.size() > 255) {
            throw new IllegalArgumentException(
                    "itemServerIds must carry at most 255 entries");
        }
        this.hasStatusClass = hasStatusClass;
        this.itemServerIds = List.copyOf(itemServerIds);
    }

    /**
     * Returns the stanza id used as the receipt's {@code id}.
     *
     * @return the id. Never {@code null}
     */
    public String receiptId() {
        return receiptId;
    }

    /**
     * Returns the recipient JID.
     *
     * @return the JID. Never {@code null}
     */
    public Jid receiptTo() {
        return receiptTo;
    }

    /**
     * Returns whether the receipt carries {@code class="status"}.
     *
     * @return {@code true} when the {@code class="status"} attribute
     *         is set
     */
    public boolean hasStatusClass() {
        return hasStatusClass;
    }

    /**
     * Returns the list of {@code <item server_id/>} ids.
     *
     * @return an unmodifiable list. Never {@code null}
     */
    public List<Integer> itemServerIds() {
        return itemServerIds;
    }

    /**
     * Builds the outbound {@code <receipt>} stanza ready for
     * dispatch.
     *
     * @return a {@link NodeBuilder} carrying the receipt envelope
     *         and the {@code <list>} payload
     *
     * @implNote {@code WASmaxOutReceiptPublishViewRequest.makePublishViewRequest}
     *           composes {@code mergeNewsletterMixin} →
     *           {@code mergeViewTypeMixin} →
     *           {@code mergeSenderAggregatedViewPublishMixin} over a
     *           bare {@code <receipt/>} root.
     */
    @Override
    @WhatsAppWebExport(moduleName = "WASmaxOutReceiptPublishViewRequest",
            exports = "makePublishViewRequest", adaptation = WhatsAppAdaptation.DIRECT)
    public NodeBuilder toNode() {
        var itemNodes = new ArrayList<Node>(itemServerIds.size());
        for (var serverId : itemServerIds) {
            var itemNode = new NodeBuilder()
                    .description("item")
                    .attribute("server_id", serverId)
                    .build();
            itemNodes.add(itemNode);
        }
        var listNode = new NodeBuilder()
                .description("list")
                .content(itemNodes)
                .build();
        var receiptBuilder = new NodeBuilder()
                .description("receipt")
                .attribute("id", receiptId)
                .attribute("to", receiptTo)
                .attribute("type", "view")
                .attribute("class", "status", hasStatusClass)
                .content(listNode);
        return receiptBuilder;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxReceiptPublishViewRequest) obj;
        return this.hasStatusClass == that.hasStatusClass
                && Objects.equals(this.receiptId, that.receiptId)
                && Objects.equals(this.receiptTo, that.receiptTo)
                && Objects.equals(this.itemServerIds, that.itemServerIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiptId, receiptTo, hasStatusClass, itemServerIds);
    }

    @Override
    public String toString() {
        return "SmaxReceiptPublishViewRequest[receiptId=" + receiptId
                + ", receiptTo=" + receiptTo
                + ", hasStatusClass=" + hasStatusClass
                + ", itemServerIds=" + itemServerIds + ']';
    }
}
