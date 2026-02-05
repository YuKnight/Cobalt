package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Metadata about a bot's avatar state.
 *
 * @apiNote WAWebProtobufsE2E.pb.BotAvatarMetadata
 */
@ProtobufMessage(name = "BotAvatarMetadata")
public final class BotAvatarMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.UINT32)
    final Integer sentiment;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String behaviorGraph;

    @ProtobufProperty(index = 3, type = ProtobufType.UINT32)
    final Integer action;

    @ProtobufProperty(index = 4, type = ProtobufType.UINT32)
    final Integer intensity;

    @ProtobufProperty(index = 5, type = ProtobufType.UINT32)
    final Integer wordCount;

    BotAvatarMetadata(Integer sentiment, String behaviorGraph, Integer action, Integer intensity, Integer wordCount) {
        this.sentiment = sentiment;
        this.behaviorGraph = behaviorGraph;
        this.action = action;
        this.intensity = intensity;
        this.wordCount = wordCount;
    }

    public Optional<Integer> sentiment() {
        return Optional.ofNullable(sentiment);
    }

    public Optional<String> behaviorGraph() {
        return Optional.ofNullable(behaviorGraph);
    }

    public Optional<Integer> action() {
        return Optional.ofNullable(action);
    }

    public Optional<Integer> intensity() {
        return Optional.ofNullable(intensity);
    }

    public Optional<Integer> wordCount() {
        return Optional.ofNullable(wordCount);
    }
}
