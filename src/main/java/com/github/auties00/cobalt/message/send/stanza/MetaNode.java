package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.Message;
import com.github.auties00.cobalt.model.message.common.MessageContainer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.util.Objects;

/**
 * Builds meta nodes for message stanzas.
 * <p>
 * Meta nodes contain message metadata like poll type, event type, thread info,
 * LID origin, view once status, and AI thread ID.
 *
 * @apiNote WAWebSendMsgMetaNode.genMetaNode
 */
public final class MetaNode {
    private final WhatsAppStore store;

    public MetaNode(WhatsAppStore store) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
    }

    /**
     * Builds a meta node for a message.
     *
     * @param chatJid the chat JID
     * @param message the message container
     * @param context the meta node context
     * @return the meta node, or null if not needed
     *
     * @apiNote WAWebSendMsgMetaNode.genMetaNode
     */
    public Node build(Jid chatJid, MessageContainer message, Context context) {
        Objects.requireNonNull(chatJid, "chatJid cannot be null");
        Objects.requireNonNull(message, "message cannot be null");

        var unwrapped = message.unbox();

        var pollType = getPollType(unwrapped);
        var eventType = getEventType(unwrapped);
        var origin = getOriginAttribute(chatJid, context);
        var threadInfo = getCommentThreadInfo(message, context);
        var hashedAiThreadId = context != null ? context.hashedAiThreadId() : null;
        var isViewOnce = unwrapped.isViewOnce();
        var appData = getAppDataAttribute(message, context);

        boolean needsMetaNode = pollType != null
                || eventType != null
                || threadInfo != null
                || origin != null
                || (context != null && context.appendHostedSenderIntent())
                || appData != null
                || isViewOnce
                || hashedAiThreadId != null;

        if (!needsMetaNode) {
            return null;
        }

        var builder = new NodeBuilder().description("meta");

        if (origin != null) {
            builder.attribute("origin", String.valueOf(origin));
        }

        if (context != null && context.appendHostedSenderIntent()) {
            builder.attribute("sender_intent", "hosted");
        }

        if (pollType != null) {
            builder.attribute("polltype", pollType);
        }

        if (eventType != null) {
            builder.attribute("event_type", eventType);
        }

        if (threadInfo != null) {
            builder.attribute("thread_msg_id", threadInfo.threadMsgId());
            if (threadInfo.threadMsgSenderLid() != null) {
                builder.attribute("thread_msg_sender_jid", threadInfo.threadMsgSenderLid());
            }
        }

        if (appData != null) {
            builder.attribute("appdata", appData);
        }

        if (isViewOnce) {
            builder.attribute("view_once", "true");
        }

        if (hashedAiThreadId != null && !hashedAiThreadId.isEmpty()) {
            builder.attribute("conversation_thread_id", hashedAiThreadId);
        }

        return builder.build();
    }

    private Integer getOriginAttribute(Jid chatJid, Context context) {
        if (!chatJid.hasLidServer()) {
            return null;
        }

        if (context == null || context.lidOriginType() == null) {
            return null;
        }

        if (context.lidOriginType() == LidOriginType.PNH_CTWA) {
            return context.lidOriginType().value();
        }

        return null;
    }

    private String getPollType(Message message) {
        var type = message.type();

        if (type == Message.Type.POLL_CREATION) {
            return "creation";
        }

        if (type == Message.Type.POLL_UPDATE) {
            var pollUpdate = message.pollUpdateMessage().orElse(null);
            if (pollUpdate != null && pollUpdate.vote().isPresent()) {
                return "vote";
            }
        }

        if (type == Message.Type.POLL_RESULT_SNAPSHOT) {
            if (store.isPollResultSnapshotPollTypeEnvelopeEnabled()) {
                return "result_snapshot";
            }
        }

        return null;
    }

    private String getEventType(Message message) {
        var type = message.type();

        if (type == Message.Type.EVENT) {
            return "creation";
        }

        if (type == Message.Type.EVENT_RESPONSE) {
            return "response";
        }

        if (type == Message.Type.SECRET_ENCRYPTED) {
            var secretMsg = message.secretEncryptedMessage().orElse(null);
            if (secretMsg != null && secretMsg.isEventEdit()) {
                return "edit";
            }
        }

        return null;
    }

    private CommentThreadInfo getCommentThreadInfo(MessageContainer message, Context context) {
        var unwrapped = message.unbox();
        if (unwrapped.type() != Message.Type.COMMENT) {
            return null;
        }

        var contextInfo = unwrapped.contextInfo().orElse(null);
        if (contextInfo == null) {
            return null;
        }

        var quotedId = contextInfo.quotedMessageId().orElse(null);
        var quotedParticipant = contextInfo.quotedMessageSenderJid().orElse(null);

        if (quotedId == null) {
            return null;
        }

        String senderLid = null;
        if (quotedParticipant != null) {
            var lid = store.toLid(quotedParticipant);
            if (lid != null) {
                senderLid = lid.toString();
            }
        }

        return new CommentThreadInfo(quotedId, senderLid);
    }

    private String getAppDataAttribute(MessageContainer message, Context context) {
        var unwrapped = message.unbox();

        if (unwrapped.type() == Message.Type.PROTOCOL) {
            var proto = unwrapped.protocolMessage().orElse(null);
            if (proto != null && proto.protocolType() == Message.ProtocolType.EPHEMERAL_SYNC_RESPONSE) {
                return "default";
            }
        }

        if (context != null && context.isCategoryPeerMessage()) {
            return "default";
        }

        if (unwrapped.type() == Message.Type.MESSAGE_HISTORY_NOTICE) {
            return "group_history";
        }

        return null;
    }

    /**
     * LID origin types.
     */
    public enum LidOriginType {
        NONE(0),
        PNH_CTWA(1),
        UPGRADE(2),
        OTHER(3);

        private final int value;

        LidOriginType(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    /**
     * Context for meta node generation.
     */
    public record Context(
            LidOriginType lidOriginType,
            String hashedAiThreadId,
            boolean appendHostedSenderIntent,
            boolean isCategoryPeerMessage
    ) {
        public static Context empty() {
            return new Context(null, null, false, false);
        }
    }

    /**
     * Comment thread information.
     */
    public record CommentThreadInfo(String threadMsgId, String threadMsgSenderLid) {
        public CommentThreadInfo {
            Objects.requireNonNull(threadMsgId, "threadMsgId cannot be null");
        }
    }
}
