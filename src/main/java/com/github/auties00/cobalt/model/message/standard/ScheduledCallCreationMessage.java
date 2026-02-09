package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;

/**
 * A model class that represents a scheduled call creation message.
 */
@ProtobufMessage(name = "Message.ScheduledCallCreationMessage")
public final class ScheduledCallCreationMessage implements Message {
    @ProtobufProperty(index = 1, type = ProtobufType.INT64)
    final long scheduledTimestampMs;

    @ProtobufProperty(index = 2, type = ProtobufType.ENUM)
    final CallType callType;

    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String title;

    ScheduledCallCreationMessage(long scheduledTimestampMs, CallType callType, String title) {
        this.scheduledTimestampMs = scheduledTimestampMs;
        this.callType = callType;
        this.title = title;
    }

    public long scheduledTimestampMs() {
        return scheduledTimestampMs;
    }

    public CallType callType() {
        return callType != null ? callType : CallType.UNKNOWN;
    }

    public String title() {
        return title;
    }

    @Override
    public Type type() {
        return Type.SCHEDULED_CALL_CREATION;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ScheduledCallCreationMessage that
                && scheduledTimestampMs == that.scheduledTimestampMs
                && callType == that.callType
                && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduledTimestampMs, callType, title);
    }

    @Override
    public String toString() {
        return "ScheduledCallCreationMessage[" +
                "scheduledTimestampMs=" + scheduledTimestampMs +
                ", callType=" + callType +
                ", title=" + title +
                ']';
    }

    @ProtobufEnum(name = "Message.ScheduledCallCreationMessage.CallType")
    public enum CallType {
        UNKNOWN(0),
        VOICE(1),
        VIDEO(2);

        final int index;

        CallType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
