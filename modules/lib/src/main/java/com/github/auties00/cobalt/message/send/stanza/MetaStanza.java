package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.bot.metrics.BotMetricsEntryPoint;
import com.github.auties00.cobalt.model.bot.metrics.BotMetricsMetadata;
import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.chat.ChatMessageContextInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.*;
import com.github.auties00.cobalt.model.message.event.EncEventResponseMessage;
import com.github.auties00.cobalt.model.message.event.EventMessage;
import com.github.auties00.cobalt.model.message.poll.PollCreationMessage;
import com.github.auties00.cobalt.model.message.poll.PollResultSnapshotMessage;
import com.github.auties00.cobalt.model.message.poll.PollUpdateMessage;
import com.github.auties00.cobalt.model.message.security.SecretEncMessage;
import com.github.auties00.cobalt.model.message.system.ProtocolMessage;
import com.github.auties00.cobalt.model.message.system.history.MessageHistoryNotice;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.util.Objects;

/**
 * Builds the {@code <meta>} stanza child node carrying auxiliary metadata about the
 * message being sent.
 *
 * <p>The node may include any of the following attributes depending on the message type
 * and recipient: {@code origin} (LID origin type or bot entry-point origin),
 * {@code destination_id} (bot metrics destination identifier), {@code sender_intent}
 * ({@code "hosted"} for hosted business accounts), {@code polltype}
 * ({@code "creation"}, {@code "vote"}, {@code "result_snapshot"}), {@code event_type}
 * ({@code "creation"}, {@code "response"}, {@code "edit"}), {@code view_once}
 * ({@code "true"} for view-once media), {@code appdata} ({@code "member_tag"},
 * {@code "default"}, {@code "group_history"}), {@code tag_reason}
 * ({@code "user_delete"}, {@code "user_update"}), {@code thread_msg_id} and
 * {@code thread_msg_sender_jid} (comment thread identifiers),
 * {@code conversation_thread_id} (hashed AI conversation thread identifier), and
 * {@code status_setting} (status privacy setting for status messages).
 */
@WhatsAppWebModule(moduleName = "WAWebSendMsgMetaNode")
public final class MetaStanza {
    /**
     * Store used for resolving chat metadata such as LID origin type and verified
     * business names.
     */
    private final WhatsAppStore store;

    /**
     * Creates a new meta stanza builder.
     *
     * @param store the WhatsApp store for resolving chat metadata
     * @throws NullPointerException if {@code store} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgMetaNode", exports = "genMetaNode",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MetaStanza(WhatsAppStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    /**
     * Builds the {@code <meta>} node for an E2E-encrypted message.
     *
     * @param chatJid          the recipient chat JID, used to resolve {@code origin} and
     *                         {@code sender_intent}
     * @param container        the message container
     * @param statusSetting    the status privacy setting ({@code "contacts"},
     *                         {@code "allowlist"}, {@code "denylist"}), or {@code null}
     *                         for non-status messages
     * @param hashedAiThreadId the HMAC-hashed AI thread identifier for the
     *                         {@code conversation_thread_id} attribute, or {@code null}
     *                         when this is not an AI thread
     * @return the meta node, or {@code null} if no metadata attributes are applicable
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgMetaNode", exports = "genMetaNode",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node buildChat(Jid chatJid, MessageContainer container, String statusSetting, String hashedAiThreadId) {
        var message = container.content();

        var polltype = resolvePollType(message);
        var eventType = resolveEventType(message);
        var appdata = resolveAppdata(message);
        var tagReason = resolveTagReason(message);

        // ADAPTED: WA Web checks n.data.mediaData?.isViewOnce; Cobalt checks the
        // protobuf wrapper type, which is semantically equivalent.
        var viewOnce = container.futureProofContentType() == FutureProofMessageType.VIEW_ONCE ? "true" : null;

        var botMetrics = container.messageContextInfo()
                .flatMap(ChatMessageContextInfo::botMetadata)
                .map(bot -> bot.botMetricsMetadata().orElse(null))
                .orElse(null);

        var destinationId = botMetrics != null
                ? botMetrics.destinationId().orElse(null)
                : null;

        // Bot origin from destinationEntryPoint takes precedence, but only when the
        // recipient is a Meta AI bot; otherwise the LID origin attribute applies.
        var botOrigin = botMetrics != null
                ? botMetrics.destinationEntryPoint()
                        .map(MetaStanza::resolveBotOrigin)
                        .orElse(null)
                : null;
        var origin = botOrigin != null && isMetaAiBot(chatJid)
                ? botOrigin
                : resolveOrigin(chatJid);

        var senderIntent = isHostedRecipient(chatJid) ? "hosted" : null;

        // Cobalt extracts thread_msg_id and thread_msg_sender_jid from the first
        // non-AI thread entry, mirroring the JS extractCommentTargetIdAndSenderLid.
        String threadMsgId = null;
        Jid threadMsgSenderJid = null;
        var deviceInfo = container.messageContextInfo().orElse(null);
        if (deviceInfo != null) {
            var threads = deviceInfo.threadId();
            for (var thread : threads) {
                if (thread.threadType().orElse(null) == MessageThreadId.ThreadType.AI_THREAD) {
                    continue;
                }
                var threadKey = thread.threadKey();
                var keyId = threadKey.flatMap(MessageKey::id)
                        .filter(entry -> !entry.isEmpty());
                if (keyId.isPresent()) {
                    threadMsgId = keyId.get();
                    threadMsgSenderJid = threadKey.flatMap(MessageKey::senderJid)
                            .orElse(null);
                    break;
                }
            }
        }

        if (polltype == null && eventType == null && viewOnce == null
                && origin == null && destinationId == null
                && senderIntent == null && appdata == null
                && threadMsgId == null && hashedAiThreadId == null
                && tagReason == null && statusSetting == null) {
            return null;
        }

        return new NodeBuilder()
                .description("meta")
                .attribute("origin", origin)
                .attribute("destination_id", destinationId)
                .attribute("sender_intent", senderIntent)
                .attribute("polltype", polltype)
                .attribute("event_type", eventType)
                .attribute("thread_msg_id", threadMsgId)
                .attribute("thread_msg_sender_jid", threadMsgSenderJid)
                .attribute("appdata", appdata)
                .attribute("view_once", viewOnce)
                .attribute("conversation_thread_id", hashedAiThreadId)
                .attribute("tag_reason", tagReason)
                .attribute("status_setting", statusSetting)
                .build();
    }

    /**
     * Convenience overload for callers that do not have a pre-computed hashed AI thread
     * identifier.
     *
     * @param chatJid       the recipient chat JID
     * @param container     the message container
     * @param statusSetting the status privacy setting, or {@code null}
     * @return the meta node, or {@code null} if no metadata attributes are applicable
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgMetaNode", exports = "genMetaNode",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node buildChat(Jid chatJid, MessageContainer container, String statusSetting) {
        return buildChat(chatJid, container, statusSetting, null);
    }

    /**
     * Resolves the {@code polltype} attribute from the message content.
     *
     * <p>Poll creations map to {@code "creation"}, poll updates with a vote map to
     * {@code "vote"}, and poll result snapshots map to {@code "result_snapshot"}.
     *
     * @param message the unwrapped message
     * @return the poll type string, or {@code null} for non-poll messages
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgMetaNode", exports = "genMetaNode",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static String resolvePollType(Message message) {
        return switch (message) {
            case PollCreationMessage _ -> "creation";
            case PollUpdateMessage p when p.vote().isPresent() -> "vote";
            case PollResultSnapshotMessage _ -> "result_snapshot";
            default -> null;
        };
    }

    /**
     * Resolves the {@code event_type} attribute from the message content.
     *
     * <p>Event messages map to {@code "creation"}, encrypted event responses map to
     * {@code "response"}, and event-edit secret-encrypted messages map to
     * {@code "edit"}.
     *
     * @param message the unwrapped message
     * @return the event type string, or {@code null} for non-event messages
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgMetaNode", exports = "genMetaNode",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static String resolveEventType(Message message) {
        return switch (message) {
            case EventMessage _ -> "creation";
            case EncEventResponseMessage _ -> "response";
            case SecretEncMessage s
                    when s.secretEncType().orElse(null) == SecretEncMessage.SecretEncType.EVENT_EDIT -> "edit";
            default -> null;
        };
    }

    /**
     * Resolves the {@code appdata} attribute from the message content.
     *
     * <p>Returns {@code "member_tag"} for group member label change protocol messages,
     * {@code "default"} for ephemeral sync response protocol messages,
     * {@code "group_history"} for message history notices, or {@code null} otherwise.
     * The {@code "default"} value for peer messages is handled separately by
     * {@code PeerMessageSender}, which builds its own meta node.
     *
     * @param message the unwrapped message
     * @return the appdata string, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgMetaNode", exports = "genMetaNode",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static String resolveAppdata(Message message) {
        if (message instanceof ProtocolMessage pm
                && pm.type().orElse(null) == ProtocolMessage.Type.GROUP_MEMBER_LABEL_CHANGE) {
            return "member_tag";
        }
        if (message instanceof ProtocolMessage pm
                && pm.type().orElse(null) == ProtocolMessage.Type.EPHEMERAL_SYNC_RESPONSE) {
            return "default";
        }
        if (message instanceof MessageHistoryNotice) {
            return "group_history";
        }
        return null;
    }

    /**
     * Resolves the {@code tag_reason} attribute from the message content.
     *
     * <p>For group member label change protocol messages, returns {@code "user_delete"}
     * when the label is empty or absent, or {@code "user_update"} when the label has a
     * value.
     *
     * @param message the unwrapped message
     * @return the tag reason string, or {@code null} for non-label messages
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgMetaNode", exports = "genMetaNode",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static String resolveTagReason(Message message) {
        if (!(message instanceof ProtocolMessage pm)
                || pm.type().orElse(null) != ProtocolMessage.Type.GROUP_MEMBER_LABEL_CHANGE) {
            return null;
        }
        var label = pm.memberLabel()
                .flatMap(ml -> ml.label())
                .orElse(null);
        return (label == null || label.isEmpty()) ? "user_delete" : "user_update";
    }

    /**
     * Resolves the {@code origin} meta attribute for LID chats.
     *
     * <p>Returns the LID origin type string (e.g. {@code "ctwa"}) when the chat JID is
     * a LID and the chat's origin type is {@code PNH_CTWA}; returns {@code null}
     * otherwise.
     *
     * @param chatJid the recipient chat JID
     * @return the origin string, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgMetaNode", exports = "getOriginAttribute",
            adaptation = WhatsAppAdaptation.DIRECT)
    private String resolveOrigin(Jid chatJid) {
        if (!chatJid.hasLidServer()) {
            return null;
        }

        return store.findChatByJid(chatJid)
                .flatMap(Chat::lidOriginType)
                .filter("ctwa"::equals)
                .orElse(null);
    }

    /**
     * Returns whether the given JID identifies a Meta AI bot account.
     *
     * <p>Compares against both known Meta AI JIDs: the FBID bot WID and the PN bot WID
     * ({@code 13135550002@c.us}).
     *
     * @param jid the JID to check
     * @return {@code true} if the JID is a Meta AI bot
     */
    @WhatsAppWebExport(moduleName = "WAWebBotUtils", exports = "isMetaAiBot",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean isMetaAiBot(Jid jid) {
        return jid.equals(Jid.metaAiBotAccount())
                || "13135550002".equals(jid.user());
    }

    /**
     * Maps a {@link BotMetricsEntryPoint} to the corresponding origin string for the
     * {@code <meta>} node.
     *
     * @param entryPoint the bot metrics entry point
     * @return the origin string, or {@code null} if the entry point has no
     *         corresponding origin value
     * @implNote The {@code WEB_INTRO_PANEL} and {@code WEB_NAVIGATION_BAR} entry points
     * are web-specific values not yet present in Cobalt's enum and fall through to the
     * {@code default} branch.
     */
    @WhatsAppWebExport(moduleName = "WAWebBotLoggingUtils", exports = "getBotOriginFromBotMetricsEntryPoint",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static String resolveBotOrigin(BotMetricsEntryPoint entryPoint) {
        return switch (entryPoint) {
            case FAVICON -> "favicon";
            case CHATLIST -> "chat_list";
            case AISEARCH_NULL_STATE_SUGGESTION -> "nullstate_suggestion";
            case AISEARCH_TYPE_AHEAD_SUGGESTION -> "typeahead_suggestion";
            case DEEPLINK -> "deeplink";
            case NOTIFICATION -> "notification";
            case AI_TAB -> "ai_tab";
            case ASK_META_AI_CONTEXT_MENU -> "ask_meta_ai_context_menu";
            case ASK_META_AI_CONTEXT_MENU_1ON1 -> "ask_meta_ai_context_menu_1on1";
            case ASK_META_AI_CONTEXT_MENU_GROUP -> "ask_meta_ai_context_menu_group";
            case META_AI_FORWARD -> "meta_ai_forward";
            default -> null;
        };
    }

    /**
     * Returns whether the recipient is a hosted business account.
     *
     * @param chatJid the recipient chat JID
     * @return {@code true} if the recipient has hosted business storage
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgMetaNode", exports = "genMetaNode",
            adaptation = WhatsAppAdaptation.DIRECT)
    private boolean isHostedRecipient(Jid chatJid) {
        return store.findVerifiedBusinessName(chatJid)
                .map(vbn -> vbn.hostStorage().isPresent())
                .orElse(false);
    }

    /**
     * Builds a {@code <meta questiontype="question">} node for newsletter question
     * messages.
     *
     * @return the meta node
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutMessagePublishQuestionTypeQuestionMixin", exports = "applyMixin",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Node buildNewsletterQuestion() {
        return new NodeBuilder()
                .description("meta")
                .attribute("questiontype", "question")
                .build();
    }

    /**
     * Builds a {@code <meta questiontype="reply">} node for newsletter question reply
     * messages.
     *
     * @return the meta node
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutMessagePublishQuestionTypeReplyMixin", exports = "applyMixin",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Node buildNewsletterQuestionReply() {
        return new NodeBuilder()
                .description("meta")
                .attribute("questiontype", "reply")
                .build();
    }

    /**
     * Builds a {@code <meta questiontype="response">} node for newsletter question
     * response messages.
     *
     * @return the meta node
     */
    @WhatsAppWebExport(moduleName = "WASmaxOutMessagePublishQuestionTypeResponseMixin", exports = "applyMixin",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static Node buildNewsletterQuestionResponse() {
        return new NodeBuilder()
                .description("meta")
                .attribute("questiontype", "response")
                .build();
    }
}
