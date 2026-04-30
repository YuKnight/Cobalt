package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxIqErrorResponseMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Wire-level addressing mode for the outbound contact-blacklist request.
 */
public enum SmaxGetContactBlacklistAddressingMode {
    /**
     * The legacy PN-addressed variant. Emits a bare
     * {@code <privacy>} envelope with no {@code addressing_mode}
     * attribute.
     */
    PN,

    /**
     * The migrated LID-addressed variant. Emits a
     * {@code <privacy addressing_mode="lid">} envelope.
     */
    LID
}
