package com.github.auties00.cobalt.message.send.bot;

/**
 * Bot persona types for FBID bots.
 */
public enum BotPersonaType {
    DEFAULT("default"),
    FIRST_PARTY_CHARACTER("1p"),
    UGC("ugc");

    private final String value;

    BotPersonaType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
