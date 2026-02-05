package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Metadata about a planning step in bot progress.
 *
 * @apiNote WAWebProtobufsE2E.pb.BotProgressIndicatorMetadata.BotPlanningStepMetadata
 */
@ProtobufMessage(name = "BotProgressIndicatorMetadata.BotPlanningStepMetadata")
public final class BotPlanningStepMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String statusTitle;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String statusBody;

    @ProtobufProperty(index = 4, type = ProtobufType.ENUM)
    final PlanningStepStatus status;

    @ProtobufProperty(index = 5, type = ProtobufType.BOOL)
    final boolean isReasoning;

    @ProtobufProperty(index = 6, type = ProtobufType.BOOL)
    final boolean isEnhancedSearch;

    BotPlanningStepMetadata(
            String statusTitle,
            String statusBody,
            PlanningStepStatus status,
            boolean isReasoning,
            boolean isEnhancedSearch
    ) {
        this.statusTitle = statusTitle;
        this.statusBody = statusBody;
        this.status = status;
        this.isReasoning = isReasoning;
        this.isEnhancedSearch = isEnhancedSearch;
    }

    public Optional<String> statusTitle() {
        return Optional.ofNullable(statusTitle);
    }

    public Optional<String> statusBody() {
        return Optional.ofNullable(statusBody);
    }

    public Optional<PlanningStepStatus> status() {
        return Optional.ofNullable(status);
    }

    public boolean isReasoning() {
        return isReasoning;
    }

    public boolean isEnhancedSearch() {
        return isEnhancedSearch;
    }

    @ProtobufEnum(name = "BotProgressIndicatorMetadata.BotPlanningStepMetadata.PlanningStepStatus")
    public enum PlanningStepStatus {
        UNKNOWN(0),
        PENDING(1),
        IN_PROGRESS(2),
        COMPLETE(3);

        final int index;

        PlanningStepStatus(@ProtobufEnumIndex int index) {
            this.index = index;
        }
    }
}
