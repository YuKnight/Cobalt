package com.github.auties00.cobalt.message.send.bot;

/**
 * Bot feedback kinds for user feedback on bot responses.
 * <p>
 * This is the string-based feedback kind used in the bot interaction layer.
 * The protobuf-level BotFeedbackKind (integer-based) is defined in the
 * BotFeedbackMessage protobuf model.
 *
 * @apiNote WAWebBotTypes.BotFeedbackKind (Mirrored enum — values match names)
 */
public enum BotFeedbackKind {
    POSITIVE,
    NEGATIVE_GENERIC,
    NEGATIVE_HELPFUL,
    NEGATIVE_INTERESTING,
    NEGATIVE_ACCURATE,
    NEGATIVE_SAFE,
    NEGATIVE_OTHER,
    NEGATIVE_REFUSED,
    NEGATIVE_NOT_VISUALLY_APPEALING,
    NEGATIVE_NOT_RELEVANT_TO_TEXT,
    NEGATIVE_NOT_ENTERTAINING,
    NEGATIVE_NOT_CUSTOMIZABLE,
    NEGATIVE_NOT_INTERESTING;

    public String value() {
        return name();
    }
}
