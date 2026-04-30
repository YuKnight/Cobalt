package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.system.ProtocolMessage;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds the {@code <ctwa_attribution>} stanza child node for Click-to-WhatsApp
 * attribution tracking.
 *
 * <p>When a user opens a chat via a CTWA ad link, WhatsApp records the entry point
 * (link type, partner name, auth status). The first message sent in that chat carries
 * the recorded attribution data so the server can attribute the conversation to the ad.
 *
 * <p>External entry points are stored in an in-memory map keyed by chat JID string,
 * mirroring how WA Web stores them in {@code WAWebUserPrefsStore} under the
 * {@code EXTERNAL_ENTRY_POINT} key. Entries expire after one week.
 */
@WhatsAppWebModule(moduleName = "WAWebSendMsgCtwaAttributionNode")
@WhatsAppWebModule(moduleName = "WAWebExternalEntryPointPrefs")
@WhatsAppWebModule(moduleName = "WAWebExternalCtxConfig")
public final class CtwaAttributionStanza {
    /**
     * In-memory store of external entry points keyed by chat JID string.
     *
     * @implNote WA Web persists this map via
     * {@code WAWebUserPrefsStore.setUser(KEYS.EXTERNAL_ENTRY_POINT, ...)}; Cobalt's
     * unified store-system collapses UserPrefs into in-memory state.
     */
    private final ConcurrentHashMap<String, ExternalEntryPoint> entryPoints;

    /**
     * Store used for chat lookup and message counting during the first-message logging
     * policy check.
     */
    private final WhatsAppStore store;

    /**
     * AB props service used to gate CTWA logging.
     */
    private final ABPropsService abPropsService;

    /**
     * Creates a new CTWA attribution stanza builder.
     *
     * @param store          the WhatsApp store
     * @param abPropsService the AB props service
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCtwaAttributionNode", exports = "getCtwaAttributionNode",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public CtwaAttributionStanza(WhatsAppStore store, ABPropsService abPropsService) {
        this.store = Objects.requireNonNull(store, "store");
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService");
        this.entryPoints = new ConcurrentHashMap<>();
    }

    /**
     * Saves an external entry point for the given chat JID, stamping {@code addedTime}
     * with the current instant.
     *
     * <p>Mirrors the JS {@code saveExternalEntryPoint} signature: the caller passes the
     * four raw fields and the record is constructed internally with {@link Instant#now()}
     * as the added-time.
     *
     * @param chatJid      the chat JID
     * @param deepLinkType the deep link type
     * @param authSuccess  whether authentication succeeded during the ad flow
     * @param partnerName  the partner name, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalEntryPointPrefs", exports = "saveExternalEntryPoint",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void saveEntryPoint(Jid chatJid, String deepLinkType, boolean authSuccess, String partnerName) {
        var addedTime = Instant.now();
        var entryPoint = new ExternalEntryPoint(deepLinkType, authSuccess, partnerName, addedTime);
        saveEntryPoint(chatJid, entryPoint);
    }

    /**
     * Saves an already-built external entry point for the given chat JID.
     *
     * <p>Any previously stored entry point for the same chat is replaced. Expired
     * entries are pruned during save, matching the JS {@code c(t)} persist helper.
     *
     * @param chatJid    the chat JID
     * @param entryPoint the entry point to save
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalEntryPointPrefs", exports = "saveExternalEntryPoint",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public void saveEntryPoint(Jid chatJid, ExternalEntryPoint entryPoint) {
        entryPoints.put(chatJid.toString(), entryPoint);
        pruneExpired();
    }

    /**
     * Deletes the external entry point for the given chat JID.
     *
     * <p>The JS guard {@code n != null && (delete t[e.toString()], c(t))} skips
     * persistence when no entry existed for the JID. {@link ConcurrentHashMap#remove}
     * is a no-op for absent keys and Cobalt has no separate persistence write to skip,
     * so observable behavior is identical.
     *
     * @param chatJid the chat JID
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalEntryPointPrefs", exports = "deleteExternalEntryPoint",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void deleteEntryPoint(Jid chatJid) {
        entryPoints.remove(chatJid.toString());
    }

    /**
     * Retrieves the external entry point for the given chat JID, returning empty if
     * none exists or the entry has expired.
     *
     * <p>Like the JS source, this method does not prune expired entries; it merely
     * declines to return them. Pruning happens during
     * {@link #saveEntryPoint(Jid, ExternalEntryPoint)}.
     *
     * @param chatJid the chat JID
     * @return the entry point, or empty
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalEntryPointPrefs", exports = "getExternalEntryPoint",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<ExternalEntryPoint> getEntryPoint(Jid chatJid) {
        var entry = entryPoints.get(chatJid.toString());
        if (entry == null || entry.isExpired()) {
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    /**
     * Builds the {@code <ctwa_attribution>} node for the given chat, if CTWA attribution
     * is applicable.
     *
     * <p>Returns {@code null} when the chat is {@code null}, CTWA context logging is
     * disabled via AB prop, no external entry point exists for the chat, or the
     * first-message logging policy excludes this chat.
     *
     * @param chatJid the chat JID being sent to
     * @return the ctwa attribution node, or {@code null} if not applicable
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCtwaAttributionNode", exports = "getCtwaAttributionNode",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node build(Jid chatJid) {
        if (chatJid == null) {
            return null;
        }

        if (!isCtxLoggingEnabled()) {
            return null;
        }

        var entryPoint = getEntryPoint(chatJid).orElse(null);
        if (entryPoint == null) {
            return null;
        }

        var chat = store.findChatByJid(chatJid).orElse(null);
        if (!shouldLogFirstMessage(chat, entryPoint.partnerName())) {
            return null;
        }

        // LinkedHashMap preserves the JS object's key insertion order so the serialised
        // bytes match WA Web's JSON.stringify output exactly.
        var json = new LinkedHashMap<String, Object>();
        json.put("lt", "WEB_" + entryPoint.deepLinkType());
        if (!entryPoint.authSuccess()) {
            json.put("s", 0);
        }
        if (entryPoint.partnerName() != null) {
            json.put("p", entryPoint.partnerName());
        }

        var jsonBytes = serializeJson(json).getBytes(StandardCharsets.UTF_8);

        return new NodeBuilder()
                .description("ctwa_attribution")
                .content(jsonBytes)
                .build();
    }

    /**
     * Returns whether CTWA context logging is currently enabled for this client.
     *
     * <p>Driven by the {@code external_ctx_authorise_wa_chat} AB prop. When disabled,
     * no {@code <ctwa_attribution>} child is produced and pending external entry points
     * are not deleted on send.
     *
     * @return {@code true} when the AB prop is enabled
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalCtxConfig", exports = "isCtxLoggingEnabled",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean isCtxLoggingEnabled() {
        return abPropsService.getBool(ABProp.EXTERNAL_CTX_AUTHORISE_WA_CHAT);
    }

    /**
     * Returns the active first-message logging policy for CTWA attribution.
     *
     * <p>The policy is encoded in the {@code external_ctx_authorise_existing_chats} AB
     * prop as an integer that the JS source translates into one of three string labels.
     * Cobalt models the same labels as a Java enum so the downstream switch is
     * exhaustive at compile time. {@code 1} maps to
     * {@link FirstMessageLoggingOption#NEW_CHATS_OR_EXISTING_CHATS_WITH_PARTNER_LINKS},
     * {@code 2} maps to {@link FirstMessageLoggingOption#ALL_CHATS}, and any other value
     * maps to {@link FirstMessageLoggingOption#NEW_CHATS_ONLY}.
     *
     * @return the resolved logging option, never {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalCtxConfig", exports = "getFirstMessageLoggingOption",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public FirstMessageLoggingOption getFirstMessageLoggingOption() {
        var loggingOption = abPropsService.getInt(ABProp.EXTERNAL_CTX_AUTHORISE_EXISTING_CHATS);
        return switch (loggingOption) {
            case 1 -> FirstMessageLoggingOption.NEW_CHATS_OR_EXISTING_CHATS_WITH_PARTNER_LINKS;
            case 2 -> FirstMessageLoggingOption.ALL_CHATS;
            default -> FirstMessageLoggingOption.NEW_CHATS_ONLY;
        };
    }

    /**
     * Returns the list of URL query parameter names that carry the CTWA deep-link token.
     *
     * <p>The base list is parsed from the {@code external_ctx_url_param_names} AB prop
     * (a comma-separated string defaulting to {@code "partnertoken"}). When
     * {@code external_ctx_foa_logging} is enabled, the trailing {@code "token"}
     * parameter name is appended. Empty parameter names are filtered out and surrounding
     * whitespace is trimmed.
     *
     * @return an unmodifiable list of URL parameter names to inspect for CTWA deep-link
     *         tokens
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalCtxConfig", exports = "getExternalCtxUrlParamNames",
            adaptation = WhatsAppAdaptation.DIRECT)
    public List<String> getExternalCtxUrlParamNames() {
        var rawValue = abPropsService.getString(ABProp.EXTERNAL_CTX_URL_PARAM_NAMES);
        if (rawValue == null) {
            rawValue = "";
        }

        var names = new ArrayList<String>();
        for (var token : rawValue.split(",")) {
            var trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                names.add(trimmed);
            }
        }

        if (names.isEmpty()) {
            names.add(PARTNER_TOKEN_PARAM);
        }

        if (isFoaLoggingEnabled()) {
            names.add(TOKEN_PARAM);
        }

        return List.copyOf(names);
    }

    /**
     * Default URL query parameter name carrying the CTWA partner deep-link token.
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalCtxConfig", exports = "e",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final String PARTNER_TOKEN_PARAM = "partnertoken";

    /**
     * Additional URL query parameter name appended to the deep-link token list when
     * {@code external_ctx_foa_logging} is enabled.
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalCtxConfig", exports = "s",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final String TOKEN_PARAM = "token";

    /**
     * Returns whether the {@code external_ctx_foa_logging} AB prop is enabled.
     *
     * @return {@code true} when First-Open-Attribution logging is enabled
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalCtxConfig", exports = "c",
            adaptation = WhatsAppAdaptation.DIRECT)
    private boolean isFoaLoggingEnabled() {
        return abPropsService.getInt(ABProp.EXTERNAL_CTX_FOA_LOGGING) == 1;
    }

    /**
     * Determines whether the first-message logging policy allows attribution for this
     * chat.
     *
     * <p>For {@link FirstMessageLoggingOption#NEW_CHATS_OR_EXISTING_CHATS_WITH_PARTNER_LINKS},
     * logs when a partner name exists or the chat is new. For
     * {@link FirstMessageLoggingOption#ALL_CHATS}, always logs. For
     * {@link FirstMessageLoggingOption#NEW_CHATS_ONLY}, logs only when the chat is new.
     *
     * @param chat        the chat, or {@code null} if not found
     * @param partnerName the partner name from the entry point, or {@code null}
     * @return {@code true} if attribution should be included
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCtwaAttributionNode", exports = "getCtwaAttributionNode",
            adaptation = WhatsAppAdaptation.DIRECT)
    private boolean shouldLogFirstMessage(Chat chat, String partnerName) {
        var loggingOption = getFirstMessageLoggingOption();
        return switch (loggingOption) {
            case NEW_CHATS_OR_EXISTING_CHATS_WITH_PARTNER_LINKS ->
                    partnerName != null || !hasMultipleNonSystemMessages(chat);
            case ALL_CHATS -> true;
            case NEW_CHATS_ONLY -> !hasMultipleNonSystemMessages(chat);
        };
    }

    /**
     * Mirrors the three string labels returned by
     * {@code WAWebExternalCtxConfig.getFirstMessageLoggingOption}.
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalCtxConfig", exports = "getFirstMessageLoggingOption",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public enum FirstMessageLoggingOption {
        /**
         * Attributes every chat with at most one non-system message, plus existing chats
         * that already carry a partner link.
         */
        NEW_CHATS_OR_EXISTING_CHATS_WITH_PARTNER_LINKS,

        /**
         * Attributes every chat regardless of message count.
         */
        ALL_CHATS,

        /**
         * Attributes only chats with at most one non-system message (the default policy).
         */
        NEW_CHATS_ONLY
    }

    /**
     * Returns whether the chat contains more than one non-system, non-send-failure
     * message.
     *
     * <p>A chat with zero or one user-sent or received message is considered "new" for
     * attribution purposes.
     *
     * @param chat the chat, or {@code null}
     * @return {@code true} if the chat has more than one qualifying message
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCtwaAttributionNode", exports = "getCtwaAttributionNode",
            adaptation = WhatsAppAdaptation.DIRECT)
    private boolean hasMultipleNonSystemMessages(Chat chat) {
        if (chat == null) {
            return false;
        }

        var count = 0;
        for (var msg : chat.messages()) {
            if (isSystemMessage(msg)) {
                continue;
            }

            count++;
            if (count > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a message is a system message type, corresponding to WA Web's
     * {@code SYSTEM_MESSAGE_TYPES} classification.
     *
     * <p>System messages include broadcast notifications, call logs, debug messages,
     * E2E notifications, group notifications, newsletter notifications, generic
     * notifications, notification templates, protocol messages, pinned messages and
     * decrypted poll-add-option entries.
     *
     * @param msg the message to check
     * @return {@code true} if the message is a system type
     * @implNote ADAPTED: Cobalt approximates the JS classification by treating any
     * message with a non-null {@code stubType} as a system message and additionally
     * flags protocol messages.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgType", exports = "SYSTEM_MESSAGE_TYPES",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private boolean isSystemMessage(ChatMessageInfo msg) {
        if (msg.messageStubType().isPresent()) {
            return true;
        }

        var content = msg.message();
        if (content == null) {
            return true;
        }

        return content.content() instanceof ProtocolMessage;
    }

    /**
     * Removes expired entry points from the in-memory map.
     *
     * <p>This is the pruning step of the JS {@code c(t)} persist helper, invoked by
     * both {@code saveExternalEntryPoint} and {@code deleteExternalEntryPoint} before
     * persistence. The Cobalt port retains prune-on-save semantics but skips the
     * persistence write because the in-memory map is the source of truth.
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalEntryPointPrefs", exports = "saveExternalEntryPoint",
            adaptation = WhatsAppAdaptation.DIRECT)
    private void pruneExpired() {
        entryPoints.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Serialises a map to a minimal JSON string with proper string escaping.
     *
     * <p>Handles {@link String}, {@link Number} and {@link Boolean} values. String
     * values are escaped to match {@code JSON.stringify} output: backslash, double-quote
     * and the four standard control-character shortcuts ({@code \b}, {@code \f},
     * {@code \n}, {@code \r}, {@code \t}) emit their two-character escape sequences,
     * and any other control character below {@code U+0020} emits a {@code \\u00XX}
     * escape. The output is byte-equivalent to
     * {@code TextEncoder().encode(JSON.stringify(n))} for partner-supplied strings that
     * may contain quotes, backslashes or control characters.
     *
     * @param map the key-value pairs to serialise
     * @return the JSON string
     * @implNote ECMA-262 {@code QuoteJSONString} defines the escape set; Cobalt mirrors
     * the subset that {@code JSON.stringify} actually emits.
     */
    private String serializeJson(LinkedHashMap<String, Object> map) {
        var sb = new StringBuilder("{");
        var first = true;
        for (var entry : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            appendJsonString(sb, entry.getKey());
            sb.append(':');
            var value = entry.getValue();
            if (value instanceof String s) {
                appendJsonString(sb, s);
            } else {
                sb.append(value);
            }
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Appends a JSON-quoted, properly escaped string to the buffer.
     *
     * @param sb    the buffer to append to
     * @param value the string to encode
     */
    private static void appendJsonString(StringBuilder sb, String value) {
        sb.append('"');
        for (var i = 0; i < value.length(); i++) {
            var ch = value.charAt(i);
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                }
            }
        }
        sb.append('"');
    }
}
