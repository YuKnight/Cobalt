package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.info.ContextInfo;
import com.github.auties00.cobalt.model.message.common.ContextualMessage;
import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A model class that represents a snapshot of poll results at a point
 * in time.
 */
@ProtobufMessage(name = "Message.PollResultSnapshotMessage")
public final class PollResultSnapshotMessage implements ContextualMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final String name;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    final List<PollVote> pollVotes;

    @ProtobufProperty(index = 3, type = ProtobufType.MESSAGE)
    ContextInfo contextInfo;

    @ProtobufProperty(index = 4, type = ProtobufType.ENUM)
    final PollType pollType;

    PollResultSnapshotMessage(String name, List<PollVote> pollVotes, ContextInfo contextInfo, PollType pollType) {
        this.name = name;
        this.pollVotes = pollVotes;
        this.contextInfo = contextInfo;
        this.pollType = pollType;
    }

    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    public List<PollVote> pollVotes() {
        return pollVotes;
    }

    @Override
    public Optional<ContextInfo> contextInfo() {
        return Optional.ofNullable(contextInfo);
    }

    @Override
    public void setContextInfo(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;
    }

    public Optional<PollType> pollType() {
        return Optional.ofNullable(pollType);
    }

    @Override
    public Type type() {
        return Type.POLL_RESULT_SNAPSHOT;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PollResultSnapshotMessage that
                && Objects.equals(name, that.name)
                && Objects.equals(pollVotes, that.pollVotes)
                && Objects.equals(contextInfo, that.contextInfo)
                && pollType == that.pollType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, pollVotes, contextInfo, pollType);
    }

    @Override
    public String toString() {
        return "PollResultSnapshotMessage[" +
                "name=" + name +
                ", pollVotes=" + pollVotes +
                ", pollType=" + pollType +
                ']';
    }

    /**
     * A single option's vote count within a poll result snapshot.
     */
    @ProtobufMessage(name = "Message.PollResultSnapshotMessage.PollVote")
    public record PollVote(
            @ProtobufProperty(index = 1, type = ProtobufType.STRING) String optionName,
            @ProtobufProperty(index = 2, type = ProtobufType.INT64) long optionVoteCount
    ) {
    }

    /**
     * The type of poll (regular or quiz).
     */
    @ProtobufEnum(name = "Message.PollType")
    public enum PollType {
        POLL(0),
        QUIZ(1);

        final int index;

        PollType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
