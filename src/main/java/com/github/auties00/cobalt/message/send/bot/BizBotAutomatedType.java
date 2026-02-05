package com.github.auties00.cobalt.message.send.bot;

/**
 * Business bot automated type for disclosure system messages.
 * <p>
 * Used to track transitions between bot automation states and
 * generate appropriate disclosure system messages in chats.
 *
 * @apiNote WAWebBotTypes.BizBotAutomatedType
 */
public enum BizBotAutomatedType {
    UNKNOWN("unknown"),
    PARTIAL_1P("1p_partial"),
    FULL_3P("3p_full");

    private final String value;

    BizBotAutomatedType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
