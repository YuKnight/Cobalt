package com.github.auties00.cobalt.node.smax.biz;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
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
import java.util.OptionalInt;

/**
 * Single {@code <campaign/>} grandchild of the outbound
 * {@code <pending_campaigns>} block. Describes a previously-reserved
 * send whose impact must be accounted for in the new quote.
 */
@WhatsAppWebModule(moduleName = "WASmaxOutSmbMeteredMessagingAccountGetSMBMeteredMessagingCheckoutRequest")
public final class SmaxGetSMBMeteredMessagingCheckoutPendingCampaign {
    /**
     * The number of free reserved messages on this previously-issued
     * campaign.
     */
    private final int freeReservedMsgs;

    /**
     * The optional send-timestamp of the previously-issued campaign
     * (epoch seconds).
     */
    private final Integer sendTimestamp;

    /**
     * Constructs a new entry.
     *
     * @param freeReservedMsgs the number of reserved messages
     * @param sendTimestamp    the optional send timestamp; may be
     *                         {@code null}
     */
    public SmaxGetSMBMeteredMessagingCheckoutPendingCampaign(int freeReservedMsgs, Integer sendTimestamp) {
        this.freeReservedMsgs = freeReservedMsgs;
        this.sendTimestamp = sendTimestamp;
    }

    /**
     * Returns the reserved-message count.
     *
     * @return the count
     */
    public int freeReservedMsgs() {
        return freeReservedMsgs;
    }

    /**
     * Returns the optional send timestamp.
     *
     * @return an {@link OptionalInt} carrying the timestamp, or
     *         empty when the entry omitted it
     */
    public OptionalInt sendTimestamp() {
        if (sendTimestamp == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(sendTimestamp);
    }

    /**
     * Builds the {@code <campaign/>} child node.
     *
     * @return the materialised {@link Node}
     */
    public Node toNode() {
        var builder = new NodeBuilder()
                .description("campaign")
                .attribute("free_reserved_msgs", freeReservedMsgs);
        if (sendTimestamp != null) {
            builder.attribute("send_timestamp", sendTimestamp.intValue());
        }
        return builder.build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (SmaxGetSMBMeteredMessagingCheckoutPendingCampaign) obj;
        return this.freeReservedMsgs == that.freeReservedMsgs
                && Objects.equals(this.sendTimestamp, that.sendTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(freeReservedMsgs, sendTimestamp);
    }

    @Override
    public String toString() {
        return "SmaxGetSMBMeteredMessagingCheckoutPendingCampaign[freeReservedMsgs=" + freeReservedMsgs
                + ", sendTimestamp=" + sendTimestamp + ']';
    }
}
