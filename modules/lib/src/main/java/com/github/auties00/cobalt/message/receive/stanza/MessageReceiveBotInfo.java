package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Optional;

/**
 * Holds the bot-related metadata parsed from an incoming message stanza.
 *
 * <p>Bot info is populated when the message involves a WhatsApp bot
 * (either a Meta AI bot or a business 1P/3P bot). It carries timing
 * information (for out-of-order bot response ordering), edit metadata
 * (for multi-turn bot edits such as refined or finalised answers), and
 * the bot's business classification. Cobalt surfaces these fields to
 * callers so they can render bot responses correctly and route bot
 * interactions back through the bot message secret.
 *
 * @implNote WAWebHandleMsgParser function b(): parses the {@code <bot>}
 * node to extract botSenderTimestampMs, botEditTargetId, botEditType,
 * botMsgBodyType, and bizBotType.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceiveBotInfo {
    /**
     * The bot sender timestamp in milliseconds from the
     * {@code sender_timestamp_ms} attribute.
     *
     * @implNote WAWebHandleMsgParser function b(): {@code botSenderTimestampMs}.
     */
    private final String senderTimestampMs;

    /**
     * The target message identifier of the bot edit, referencing the
     * original bot message that is being edited.
     *
     * @implNote WAWebHandleMsgParser function b(): {@code botEditTargetId}.
     */
    private final String editTargetId;

    /**
     * The bot edit type (for example {@code "inner"} or {@code "last"}),
     * identifying whether the edit replaces an intermediate token stream
     * or the final bot answer.
     *
     * @implNote WAWebHandleMsgParser function b(): {@code botEditType}.
     */
    private final String editType;

    /**
     * The bot message body type, identifying the format of the response
     * payload.
     *
     * @implNote WAWebHandleMsgParser function b(): {@code botMsgBodyType}.
     */
    private final String bodyType;

    /**
     * The business bot classification ({@code "1"} for 1P,
     * {@code "3"} for 3P).
     *
     * @implNote WAWebHandleMsgParser function b(): {@code bizBotType}.
     */
    private final String bizBotType;

    /**
     * Constructs a new bot info record with all parsed fields.
     *
     * @param senderTimestampMs the bot sender timestamp in milliseconds, or {@code null}
     * @param editTargetId      the target message id for edits, or {@code null}
     * @param editType          the bot edit type, or {@code null}
     * @param bodyType          the bot message body type, or {@code null}
     * @param bizBotType        the business bot classification, or {@code null}
     *
     * @implNote WAWebHandleMsgParser function b(): constructs the bot
     * info object with the parsed fields.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgParser", exports = "incomingMsgParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public MessageReceiveBotInfo(
            String senderTimestampMs,
            String editTargetId,
            String editType,
            String bodyType,
            String bizBotType
    ) {
        this.senderTimestampMs = senderTimestampMs;
        this.editTargetId = editTargetId;
        this.editType = editType;
        this.bodyType = bodyType;
        this.bizBotType = bizBotType;
    }

    /**
     * Returns the bot sender timestamp in milliseconds, when present.
     *
     * @return an {@link Optional} wrapping the timestamp string
     * @implNote WAWebHandleMsgParser function b(): {@code botSenderTimestampMs}.
     */
    public Optional<String> senderTimestampMs() {
        return Optional.ofNullable(senderTimestampMs);
    }

    /**
     * Returns the target message identifier for bot edits, when present.
     *
     * @return an {@link Optional} wrapping the target id
     * @implNote WAWebHandleMsgParser function b(): {@code botEditTargetId}.
     */
    public Optional<String> editTargetId() {
        return Optional.ofNullable(editTargetId);
    }

    /**
     * Returns the bot edit type (for example {@code "inner"} or
     * {@code "last"}), when present.
     *
     * @return an {@link Optional} wrapping the edit type
     * @apiNote WAWebBotTypes.BotMsgEditType
     * @implNote WAWebHandleMsgParser function b(): {@code botEditType}.
     */
    public Optional<String> editType() {
        return Optional.ofNullable(editType);
    }

    /**
     * Returns the bot message body type, when present.
     *
     * @return an {@link Optional} wrapping the body type
     * @apiNote WAWebBotTypes.BotMsgBodyType
     * @implNote WAWebHandleMsgParser function b(): {@code botMsgBodyType}.
     */
    public Optional<String> bodyType() {
        return Optional.ofNullable(bodyType);
    }

    /**
     * Returns the business bot classification ({@code "1"} for 1P,
     * {@code "3"} for 3P), when present.
     *
     * @return an {@link Optional} wrapping the biz bot classification
     * @apiNote WAWebBotTypes.BizBotType
     * @implNote WAWebHandleMsgParser function b(): {@code bizBotType}.
     */
    public Optional<String> bizBotType() {
        return Optional.ofNullable(bizBotType);
    }
}
