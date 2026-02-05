package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;
import java.util.Optional;

/**
 * Metadata about suggested prompts from a bot.
 *
 * @apiNote WAWebProtobufsE2E.pb.BotSuggestedPromptMetadata
 */
@ProtobufMessage(name = "BotSuggestedPromptMetadata")
public final class BotSuggestedPromptMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final List<String> suggestedPrompts;

    @ProtobufProperty(index = 2, type = ProtobufType.UINT32)
    final Integer selectedPromptIndex;

    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final String selectedPromptId;

    BotSuggestedPromptMetadata(List<String> suggestedPrompts, Integer selectedPromptIndex, String selectedPromptId) {
        this.suggestedPrompts = suggestedPrompts;
        this.selectedPromptIndex = selectedPromptIndex;
        this.selectedPromptId = selectedPromptId;
    }

    public List<String> suggestedPrompts() {
        return suggestedPrompts;
    }

    public Optional<Integer> selectedPromptIndex() {
        return Optional.ofNullable(selectedPromptIndex);
    }

    public Optional<String> selectedPromptId() {
        return Optional.ofNullable(selectedPromptId);
    }
}
