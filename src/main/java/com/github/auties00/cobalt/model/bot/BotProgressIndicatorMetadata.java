package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;
import java.util.Optional;

/**
 * Metadata about bot progress indicators (loading states).
 *
 * @apiNote WAWebProtobufsE2E.pb.BotProgressIndicatorMetadata
 */
@ProtobufMessage(name = "BotProgressIndicatorMetadata")
public final class BotProgressIndicatorMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String progressDescription;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    final List<BotPlanningStepMetadata> stepsMetadata;

    @ProtobufProperty(index = 3, type = ProtobufType.INT64)
    final Long estimatedCompletionTime;

    BotProgressIndicatorMetadata(
            String progressDescription,
            List<BotPlanningStepMetadata> stepsMetadata,
            Long estimatedCompletionTime
    ) {
        this.progressDescription = progressDescription;
        this.stepsMetadata = stepsMetadata;
        this.estimatedCompletionTime = estimatedCompletionTime;
    }

    public Optional<String> progressDescription() {
        return Optional.ofNullable(progressDescription);
    }

    public List<BotPlanningStepMetadata> stepsMetadata() {
        return stepsMetadata;
    }

    public Optional<Long> estimatedCompletionTime() {
        return Optional.ofNullable(estimatedCompletionTime);
    }
}
