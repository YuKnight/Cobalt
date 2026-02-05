package com.github.auties00.cobalt.message.send.bot;

/**
 * Bot posing-as-professional status types.
 *
 * @apiNote WAWebBotTypes.BotPosingAsProfessionalType
 */
public enum BotPosingAsProfessionalType {
    UNKNOWN("unknown"),
    YES("yes"),
    NO("no");

    private final String value;

    BotPosingAsProfessionalType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
