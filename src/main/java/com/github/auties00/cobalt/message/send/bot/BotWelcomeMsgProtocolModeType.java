package com.github.auties00.cobalt.message.send.bot;

/**
 * Bot welcome message protocol mode types.
 *
 * @apiNote WAWebBotTypes.BotWelcomeMsgProtocolModeType
 */
public enum BotWelcomeMsgProtocolModeType {
    NONE("none"),
    BASIC("basic");

    private final String value;

    BotWelcomeMsgProtocolModeType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
