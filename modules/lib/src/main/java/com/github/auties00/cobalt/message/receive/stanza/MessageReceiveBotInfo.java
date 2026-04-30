package com.github.auties00.cobalt.message.receive.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.Optional;

/**
 * Holds the bot-related metadata parsed from an incoming message stanza's
 * {@code <bot>} child.
 *
 * <p>Bot info is populated when the message involves a Meta AI bot or a business
 * 1P/3P bot. It carries the bot sender timestamp (for out-of-order ordering), edit
 * metadata for multi-turn streaming responses, and the bot's business classification.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgParser")
public final class MessageReceiveBotInfo {
    /**
     * Bot sender timestamp in milliseconds from the {@code sender_timestamp_ms}
     * attribute.
     */
    private final String senderTimestampMs;

    /**
     * Target message identifier of the bot edit, referencing the original bot message
     * being edited.
     */
    private final String editTargetId;

    /**
     * Bot edit type (for example {@code "inner"} for a streaming token replacement,
     * {@code "last"} for the final answer).
     */
    private final String editType;

    /**
     * Body type of the bot response payload.
     */
    private final String bodyType;

    /**
     * Business bot classification ({@code "1"} for 1P, {@code "3"} for 3P).
     */
    private final String bizBotType;

    /**
     * Constructs a new bot info record.
     *
     * @param senderTimestampMs the bot sender timestamp, or {@code null}
     * @param editTargetId      the target id for edits, or {@code null}
     * @param editType          the bot edit type, or {@code null}
     * @param bodyType          the bot message body type, or {@code null}
     * @param bizBotType        the business bot classification, or {@code null}
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
     */
    public Optional<String> senderTimestampMs() {
        return Optional.ofNullable(senderTimestampMs);
    }

    /**
     * Returns the target message identifier for bot edits, when present.
     *
     * @return an {@link Optional} wrapping the target id
     */
    public Optional<String> editTargetId() {
        return Optional.ofNullable(editTargetId);
    }

    /**
     * Returns the bot edit type, when present.
     *
     * @return an {@link Optional} wrapping the edit type
     */
    public Optional<String> editType() {
        return Optional.ofNullable(editType);
    }

    /**
     * Returns the bot message body type, when present.
     *
     * @return an {@link Optional} wrapping the body type
     */
    public Optional<String> bodyType() {
        return Optional.ofNullable(bodyType);
    }

    /**
     * Returns the business bot classification, when present.
     *
     * @return an {@link Optional} wrapping the biz bot classification
     */
    public Optional<String> bizBotType() {
        return Optional.ofNullable(bizBotType);
    }
}
