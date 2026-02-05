package com.github.auties00.cobalt.message.send.bot;

/**
 * Bot message types used in the {@code <bot>} stanza node.
 * <p>
 * This enum covers three semantic categories that are all carried
 * as the {@code type} attribute on the {@code <bot>} stanza node:
 * <ul>
 *   <li>Body types: {@link #PROMPT}, {@link #COMMAND}, {@link #VOICE}
 *       — the type of user input being sent to the bot (WAWebBotTypes.BotMsgBodyType)</li>
 *   <li>Feedback: {@link #FEEDBACK} — set when the message is a bot feedback message</li>
 *   <li>Welcome: {@link #REQUEST_WELCOME} — set when the subtype is {@code bot_request_welcome}</li>
 * </ul>
 *
 * @apiNote WAWebBotTypes.BotMsgBodyType (PROMPT, COMMAND, VOICE),
 *          stanza type="feedback", stanza subtype="request_welcome"
 */
public enum BotMessageType {
    REQUEST_WELCOME("request_welcome"),
    FEEDBACK("feedback"),
    COMMAND("command"),
    PROMPT("prompt"),
    VOICE("voice");

    private final String value;

    BotMessageType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
