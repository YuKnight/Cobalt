package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;
import java.util.Objects;

/**
 * A model class that represents a call log message.
 */
@ProtobufMessage(name = "Message.CallLogMessage")
public final class CallLogMessage implements Message {
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    final boolean isVideo;

    @ProtobufProperty(index = 2, type = ProtobufType.ENUM)
    final CallOutcome callOutcome;

    @ProtobufProperty(index = 3, type = ProtobufType.INT64)
    final long durationSecs;

    @ProtobufProperty(index = 4, type = ProtobufType.ENUM)
    final CallType callType;

    @ProtobufProperty(index = 5, type = ProtobufType.MESSAGE)
    final List<CallParticipant> participants;

    CallLogMessage(boolean isVideo, CallOutcome callOutcome, long durationSecs, CallType callType, List<CallParticipant> participants) {
        this.isVideo = isVideo;
        this.callOutcome = callOutcome;
        this.durationSecs = durationSecs;
        this.callType = callType;
        this.participants = participants;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public CallOutcome callOutcome() {
        return callOutcome != null ? callOutcome : CallOutcome.CONNECTED;
    }

    public long durationSecs() {
        return durationSecs;
    }

    public CallType callType() {
        return callType != null ? callType : CallType.REGULAR;
    }

    public List<CallParticipant> participants() {
        return participants;
    }

    @Override
    public Type type() {
        return Type.CALL_LOG;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CallLogMessage that
                && isVideo == that.isVideo
                && durationSecs == that.durationSecs
                && callOutcome == that.callOutcome
                && callType == that.callType
                && Objects.equals(participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isVideo, callOutcome, durationSecs, callType, participants);
    }

    @Override
    public String toString() {
        return "CallLogMessage[isVideo=" + isVideo +
                ", callOutcome=" + callOutcome +
                ", durationSecs=" + durationSecs +
                ']';
    }

    @ProtobufMessage(name = "Message.CallLogMessage.CallParticipant")
    public record CallParticipant(
            @ProtobufProperty(index = 1, type = ProtobufType.STRING) String jid,
            @ProtobufProperty(index = 2, type = ProtobufType.ENUM) CallOutcome callOutcome
    ) {
    }

    @ProtobufEnum(name = "Message.CallLogMessage.CallOutcome")
    public enum CallOutcome {
        CONNECTED(0),
        MISSED(1),
        FAILED(2),
        REJECTED(3),
        ACCEPTED_ELSEWHERE(4),
        ONGOING(5),
        SILENCED_BY_DND(6),
        SILENCED_UNKNOWN_CALLER(7);

        final int index;

        CallOutcome(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }

    @ProtobufEnum(name = "Message.CallLogMessage.CallType")
    public enum CallType {
        REGULAR(0),
        SCHEDULED_CALL(1),
        VOICE_CHAT(2);

        final int index;

        CallType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
