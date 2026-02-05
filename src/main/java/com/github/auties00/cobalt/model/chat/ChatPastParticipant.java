package com.github.auties00.cobalt.model.chat;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.util.Clock;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Class representing a past participant in a chat
 */
@ProtobufMessage(name = "PastParticipant")
public final class ChatPastParticipant {
    /**
     * Duration after which past participant records expire.
     *
     * @apiNote WAWebDbPastParticipant: PAST_PARTICIPANT_EXPIRATION_DAYS
     */
    private static final Duration EXPIRATION = Duration.ofDays(180);

    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    final Jid jid;

    @ProtobufProperty(index = 2, type = ProtobufType.ENUM)
    final Reason reason;

    @ProtobufProperty(index = 3, type = ProtobufType.UINT64)
    final long timestampSeconds;

    ChatPastParticipant(Jid jid, Reason reason, long timestampSeconds) {
        this.jid = Objects.requireNonNull(jid, "value cannot be null");
        this.reason = Objects.requireNonNull(reason, "reason cannot be null");
        this.timestampSeconds = timestampSeconds;
    }

    public Jid jid() {
        return jid;
    }

    /**
     * Returns when the past participant left the chat
     *
     * @return an optional
     */
    public Optional<ZonedDateTime> timestamp() {
        return Clock.parseSeconds(timestampSeconds);
    }

    public Reason reason() {
        return reason;
    }

    public long timestampSeconds() {
        return timestampSeconds;
    }

    /**
     * Returns whether this past participant record has expired.
     * <p>
     * Per WhatsApp Web: past participant records are pruned after 180 days.
     *
     * @return true if the record is expired and should be pruned
     *
     * @apiNote WAWebDbPastParticipant: checks if record exceeds PAST_PARTICIPANT_EXPIRATION_DAYS
     */
    public boolean isExpired() {
        var age = Duration.between(Instant.ofEpochSecond(timestampSeconds), Instant.now());
        return age.compareTo(EXPIRATION) > 0;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ChatPastParticipant that
                && Objects.equals(jid, that.jid)
                && Objects.equals(reason, that.reason)
                && timestampSeconds == that.timestampSeconds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jid, reason, timestampSeconds);
    }

    @Override
    public String toString() {
        return "ChatPastParticipant[" +
                "value=" + jid + ", " +
                "reason=" + reason + ", " +
                "timestampSeconds=" + timestampSeconds + ']';
    }

    /**
     * Enum representing the errorReason for a past participant leaving the chat.
     */
    @ProtobufEnum(name = "PastParticipant.LeaveReason")
    public enum Reason {
        /**
         * The past participant left the chat voluntarily.
         */
        LEFT(0),
        /**
         * The past participant was removed from the chat.
         */
        REMOVED(1);

        final int index;

        Reason(@ProtobufEnumIndex int index) {
            this.index = index;
        }
    }
}