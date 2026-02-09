package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.info.ContextInfo;
import com.github.auties00.cobalt.model.message.common.ContextualMessage;
import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A model class that represents a calendar event message.
 */
@ProtobufMessage(name = "Message.EventMessage")
public final class EventMessage implements ContextualMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    ContextInfo contextInfo;

    @ProtobufProperty(index = 2, type = ProtobufType.BOOL)
    final boolean isCanceled;

    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String name;

    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final String description;

    @ProtobufProperty(index = 5, type = ProtobufType.MESSAGE)
    final LocationMessage location;

    @ProtobufProperty(index = 6, type = ProtobufType.STRING)
    final String joinLink;

    @ProtobufProperty(index = 7, type = ProtobufType.INT64)
    final long startTime;

    @ProtobufProperty(index = 8, type = ProtobufType.INT64)
    final long endTime;

    @ProtobufProperty(index = 9, type = ProtobufType.BOOL)
    final boolean extraGuestsAllowed;

    @ProtobufProperty(index = 10, type = ProtobufType.BOOL)
    final boolean isScheduleCall;

    @ProtobufProperty(index = 11, type = ProtobufType.BOOL)
    final boolean hasReminder;

    @ProtobufProperty(index = 12, type = ProtobufType.INT64)
    final long reminderOffsetSec;

    EventMessage(
            ContextInfo contextInfo,
            boolean isCanceled,
            String name,
            String description,
            LocationMessage location,
            String joinLink,
            long startTime,
            long endTime,
            boolean extraGuestsAllowed,
            boolean isScheduleCall,
            boolean hasReminder,
            long reminderOffsetSec
    ) {
        this.contextInfo = contextInfo;
        this.isCanceled = isCanceled;
        this.name = name;
        this.description = description;
        this.location = location;
        this.joinLink = joinLink;
        this.startTime = startTime;
        this.endTime = endTime;
        this.extraGuestsAllowed = extraGuestsAllowed;
        this.isScheduleCall = isScheduleCall;
        this.hasReminder = hasReminder;
        this.reminderOffsetSec = reminderOffsetSec;
    }

    @Override
    public Optional<ContextInfo> contextInfo() {
        return Optional.ofNullable(contextInfo);
    }

    @Override
    public void setContextInfo(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Optional<LocationMessage> location() {
        return Optional.ofNullable(location);
    }

    public Optional<String> joinLink() {
        return Optional.ofNullable(joinLink);
    }

    public long startTime() {
        return startTime;
    }

    public long endTime() {
        return endTime;
    }

    public boolean extraGuestsAllowed() {
        return extraGuestsAllowed;
    }

    public boolean isScheduleCall() {
        return isScheduleCall;
    }

    public boolean hasReminder() {
        return hasReminder;
    }

    public long reminderOffsetSec() {
        return reminderOffsetSec;
    }

    @Override
    public Type type() {
        return Type.EVENT;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof EventMessage that
                && isCanceled == that.isCanceled
                && startTime == that.startTime
                && endTime == that.endTime
                && extraGuestsAllowed == that.extraGuestsAllowed
                && isScheduleCall == that.isScheduleCall
                && hasReminder == that.hasReminder
                && reminderOffsetSec == that.reminderOffsetSec
                && Objects.equals(contextInfo, that.contextInfo)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(location, that.location)
                && Objects.equals(joinLink, that.joinLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                contextInfo, isCanceled, name, description, location,
                joinLink, startTime, endTime, extraGuestsAllowed,
                isScheduleCall, hasReminder, reminderOffsetSec
        );
    }

    @Override
    public String toString() {
        return "EventMessage[" +
                "name=" + name +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", isCanceled=" + isCanceled +
                ']';
    }
}
