package com.github.auties00.cobalt.message.send.bot;

/**
 * Business bot types.
 */
public enum BusinessBotType {
    BIZ_1P(1),
    BIZ_3P(3);

    private final int value;

    BusinessBotType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
