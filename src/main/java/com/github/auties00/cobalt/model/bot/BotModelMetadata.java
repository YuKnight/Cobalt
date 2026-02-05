package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Metadata about the AI model used for bot responses.
 *
 * @apiNote WAWebProtobufsE2E.pb.BotModelMetadata
 */
@ProtobufMessage(name = "BotModelMetadata")
public final class BotModelMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    final ModelType modelType;

    @ProtobufProperty(index = 2, type = ProtobufType.ENUM)
    final PremiumModelStatus premiumModelStatus;

    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String modelNameOverride;

    BotModelMetadata(ModelType modelType, PremiumModelStatus premiumModelStatus, String modelNameOverride) {
        this.modelType = modelType;
        this.premiumModelStatus = premiumModelStatus;
        this.modelNameOverride = modelNameOverride;
    }

    public Optional<ModelType> modelType() {
        return Optional.ofNullable(modelType);
    }

    public Optional<PremiumModelStatus> premiumModelStatus() {
        return Optional.ofNullable(premiumModelStatus);
    }

    public Optional<String> modelNameOverride() {
        return Optional.ofNullable(modelNameOverride);
    }

    @ProtobufEnum(name = "BotModelMetadata.ModelType")
    public enum ModelType {
        UNKNOWN_TYPE(0),
        LLAMA_PROD(1),
        LLAMA_PROD_PREMIUM(2);

        final int index;

        ModelType(@ProtobufEnumIndex int index) {
            this.index = index;
        }
    }

    @ProtobufEnum(name = "BotModelMetadata.PremiumModelStatus")
    public enum PremiumModelStatus {
        UNKNOWN_STATUS(0),
        AVAILABLE(1),
        QUOTA_EXCEED_LIMIT(2);

        final int index;

        PremiumModelStatus(@ProtobufEnumIndex int index) {
            this.index = index;
        }
    }
}
