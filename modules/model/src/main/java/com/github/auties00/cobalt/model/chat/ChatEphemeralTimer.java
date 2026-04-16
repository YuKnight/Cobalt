package com.github.auties00.cobalt.model.chat;

import it.auties.protobuf.annotation.ProtobufDeserializer;
import it.auties.protobuf.annotation.ProtobufSerializer;

import java.time.Duration;
import java.util.Arrays;

/**
 * Represents the available durations for disappearing messages in a WhatsApp chat.
 *
 * <p>When disappearing messages are enabled in a one-to-one or group conversation,
 * every new message sent to the chat is automatically deleted after the configured
 * duration. WhatsApp supports a fixed set of timer values: 24 hours, 7 days, and
 * 90 days, plus a special {@link #OFF} value that disables the feature entirely.
 *
 * <p>Instances are serialized to and from protobuf as an integer representing the
 * duration in seconds. The {@link #of(Integer)} factory method also accepts values
 * expressed in days for backward compatibility.
 */
public enum ChatEphemeralTimer {
    /**
     * Disappearing messages are disabled. Messages in the chat are retained
     * indefinitely.
     */
    OFF(Duration.ofDays(0)),

    /**
     * Messages disappear after 24 hours.
     */
    ONE_DAY(Duration.ofDays(1)),

    /**
     * Messages disappear after 7 days.
     */
    ONE_WEEK(Duration.ofDays(7)),

    /**
     * Messages disappear after 90 days.
     */
    THREE_MONTHS(Duration.ofDays(90));

    /**
     * The duration after which messages are automatically deleted.
     */
    private final Duration period;

    /**
     * Constructs a {@code ChatEphemeralTimer} with the given duration.
     *
     * @param period the duration after which messages are deleted
     */
    ChatEphemeralTimer(Duration period) {
        this.period = period;
    }

    /**
     * Returns the duration after which messages are automatically deleted
     * in this timer mode.
     *
     * @return the ephemeral timer duration, never {@code null}
     */
    public Duration period() {
        return period;
    }

    /**
     * Returns the {@code ChatEphemeralTimer} matching the given integer value.
     *
     * <p>The value may be expressed in seconds or in days. If the value is
     * {@code null} or does not match any known timer, {@link #OFF} is returned.
     *
     * @param value the timer value in seconds or days, or {@code null}
     * @return the matching timer, or {@link #OFF} if no match is found
     */
    @ProtobufDeserializer
    public static ChatEphemeralTimer of(Integer value) {
        return value == null ? OFF : Arrays.stream(values())
                .filter(entry -> entry.period().toSeconds() == value || entry.period().toDays() == value)
                .findFirst()
                .orElse(OFF);
    }

    /**
     * Returns this timer's duration expressed in seconds, suitable for
     * protobuf serialization.
     *
     * @return the duration in seconds as an {@link Integer}
     */
    @ProtobufSerializer
    public Integer periodSeconds() {
        return (int) period.toSeconds();
    }
}
