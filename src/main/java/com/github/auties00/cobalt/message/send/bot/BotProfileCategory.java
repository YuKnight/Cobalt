package com.github.auties00.cobalt.message.send.bot;

/**
 * Bot profile category types.
 *
 * @apiNote WAWebBotProfileCategory.BotProfileCategory
 */
public enum BotProfileCategory {
    SYNTHETIC("synthetic"),
    LIVING("living"),
    FICTIONAL("fictional"),
    HISTORICAL("historical");

    private final String value;

    BotProfileCategory(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
