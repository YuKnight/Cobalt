package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Builds Click-to-WhatsApp (CTWA) attribution nodes for message stanzas.
 * <p>
 * CTWA attribution tracks messages that originated from external entry points
 * like ads or partner links.
 *
 * @apiNote WAWebSendMsgCtwaAttributionNode.getCtwaAttributionNode
 */
public final class CtwaNode {
    private final WhatsAppStore store;

    public CtwaNode(WhatsAppStore store) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
    }

    /**
     * Builds a CTWA attribution node for a chat if applicable.
     *
     * @param chatJid the chat JID
     * @return the CTWA node, or null if not applicable
     *
     * @apiNote WAWebSendMsgCtwaAttributionNode.getCtwaAttributionNode
     */
    public Node build(Jid chatJid) {
        Objects.requireNonNull(chatJid, "chatJid cannot be null");

        if (!store.isCtxLoggingEnabled()) {
            return null;
        }

        var entryPoint = store.getExternalEntryPoint(chatJid).orElse(null);
        if (entryPoint == null) {
            return null;
        }

        if (!shouldSendAttribution(chatJid, entryPoint.partnerName())) {
            return null;
        }

        var attributionData = buildAttributionData(entryPoint);
        var attributionBytes = attributionData.getBytes(StandardCharsets.UTF_8);

        return new NodeBuilder()
                .description("ctwa")
                .content(attributionBytes)
                .build();
    }

    private boolean shouldSendAttribution(Jid chatJid, String partnerName) {
        var loggingOption = store.getFirstMessageLoggingOption();

        return switch (loggingOption) {
            case NEW_CHATS_OR_EXISTING_CHATS_WITH_PARTNER_LINKS ->
                    partnerName != null || !hasMultipleMessages(chatJid);
            case ALL_CHATS -> true;
            default -> !hasMultipleMessages(chatJid);
        };
    }

    private boolean hasMultipleMessages(Jid chatJid) {
        var messageCount = store.countNonSystemMessages(chatJid);
        return messageCount > 1;
    }

    private String buildAttributionData(ExternalEntryPoint entryPoint) {
        var data = new HashMap<String, Object>();
        data.put("lt", "WEB_" + entryPoint.deepLinkType());

        if (!entryPoint.authSuccess()) {
            data.put("s", 0);
        }

        if (entryPoint.partnerName() != null) {
            data.put("p", entryPoint.partnerName());
        }

        return toJson(data);
    }

    private String toJson(Map<String, Object> data) {
        var sb = new StringBuilder("{");
        var first = true;

        for (var entry : data.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;

            sb.append("\"").append(entry.getKey()).append("\":");

            var value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else {
                sb.append(value);
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * External entry point data.
     */
    public record ExternalEntryPoint(String deepLinkType, boolean authSuccess, String partnerName) {
        public ExternalEntryPoint {
            Objects.requireNonNull(deepLinkType, "deepLinkType cannot be null");
        }
    }

    /**
     * First message logging options.
     */
    public enum FirstMessageLoggingOption {
        NEW_CHATS_OR_EXISTING_CHATS_WITH_PARTNER_LINKS,
        ALL_CHATS,
        NEW_CHATS_ONLY
    }
}
