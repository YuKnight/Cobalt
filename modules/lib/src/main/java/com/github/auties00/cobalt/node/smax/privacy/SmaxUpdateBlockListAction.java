package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxBaseServerErrorMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The outbound action to perform on the target JID.
 */
public enum SmaxUpdateBlockListAction {
    /**
     * Add the target JID to the blocklist.
     */
    BLOCK("block"),

    /**
     * Remove the target JID from the blocklist.
     */
    UNBLOCK("unblock");

    /**
     * The wire string for this action.
     */
    private final String wire;

    /**
     * Constructs an action enum constant.
     *
     * @param wire the wire string
     */
    SmaxUpdateBlockListAction(String wire) {
        this.wire = wire;
    }

    /**
     * Returns the wire string.
     *
     * @return the wire string; never {@code null}
     */
    public String wire() {
        return wire;
    }
}
