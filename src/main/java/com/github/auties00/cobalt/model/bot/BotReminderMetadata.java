package com.github.auties00.cobalt.model.bot;

import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Metadata about bot reminders.
 *
 * @apiNote WAWebProtobufsE2E.pb.BotReminderMetadata
 */
@ProtobufMessage(name = "BotReminderMetadata")
public final class BotReminderMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey requestMessageKey;

    @ProtobufProperty(index = 2, type = ProtobufType.ENUM)
    final ReminderAction action;

    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String name;

    @ProtobufProperty(index = 4, type = ProtobufType.UINT64)
    final Long nextTriggerTimestamp;

    @ProtobufProperty(index = 5, type = ProtobufType.ENUM)
    final ReminderFrequency frequency;

    BotReminderMetadata(
            ChatMessageKey requestMessageKey,
            ReminderAction action,
            String name,
            Long nextTriggerTimestamp,
            ReminderFrequency frequency
    ) {
        this.requestMessageKey = requestMessageKey;
        this.action = action;
        this.name = name;
        this.nextTriggerTimestamp = nextTriggerTimestamp;
        this.frequency = frequency;
    }

    public Optional<ChatMessageKey> requestMessageKey() {
        return Optional.ofNullable(requestMessageKey);
    }

    public Optional<ReminderAction> action() {
        return Optional.ofNullable(action);
    }

    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    public Optional<Long> nextTriggerTimestamp() {
        return Optional.ofNullable(nextTriggerTimestamp);
    }

    public Optional<ReminderFrequency> frequency() {
        return Optional.ofNullable(frequency);
    }

    @ProtobufEnum(name = "BotReminderMetadata.ReminderAction")
    public enum ReminderAction {
        NOTIFY(1),
        CREATE(2),
        DELETE(3),
        UPDATE(4);

        final int index;

        ReminderAction(@ProtobufEnumIndex int index) {
            this.index = index;
        }
    }

    @ProtobufEnum(name = "BotReminderMetadata.ReminderFrequency")
    public enum ReminderFrequency {
        ONCE(1),
        DAILY(2),
        WEEKLY(3),
        MONTHLY(4);

        final int index;

        ReminderFrequency(@ProtobufEnumIndex int index) {
            this.index = index;
        }
    }
}
