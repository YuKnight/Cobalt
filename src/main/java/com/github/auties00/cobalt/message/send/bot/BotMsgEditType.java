package com.github.auties00.cobalt.message.send.bot;

/**
 * Bot message edit types for streamed bot responses.
 * <p>
 * When a bot streams its response, it progressively edits the message.
 * FIRST is the initial message, INNER are intermediate updates,
 * LAST is the final update, and FULL is a non-streamed complete response.
 *
 * @apiNote WAWebBotTypes.BotMsgEditType
 */
public enum BotMsgEditType {
    FIRST("first"),
    INNER("inner"),
    LAST("last"),
    FULL("full");

    private final String value;

    BotMsgEditType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
