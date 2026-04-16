package com.github.auties00.cobalt.model.message.system.history;

import com.github.auties00.cobalt.model.message.Message;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.github.auties00.cobalt.model.mixin.InstantSecondsMixin;
import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.*;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Describes the contents of a shared message history bundle in terms of
 * recipients, time range and size.
 *
 * <p>When a user shares a chat's history with another participant (for example
 * when adding someone to an existing group or forwarding a conversation), the
 * client packages the matching messages into a {@link MessageHistoryBundle}
 * and attaches this metadata so that both sides can display a preview such as
 * "N messages since T have been shared with X, Y, Z" before the recipient
 * opens the bundle.
 */
@ProtobufMessage(name = "Message.MessageHistoryMetadata")
public final class MessageHistoryMetadata implements Message {
    /**
     * The display strings of the participants who are receiving the shared
     * history, used to render the preview banner.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    List<String> historyReceivers;

    /**
     * The timestamp of the earliest message contained in the shared history
     * bundle.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.INT64, mixins = InstantSecondsMixin.class)
    Instant oldestMessageTimestamp;

    /**
     * The total number of messages contained in the shared history bundle.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.INT64)
    Long messageCount;


    /**
     * Constructs a new history metadata payload.
     *
     * @param historyReceivers       the list of history receivers
     * @param oldestMessageTimestamp the timestamp of the earliest shared
     *                               message
     * @param messageCount           the total number of shared messages
     */
    MessageHistoryMetadata(List<String> historyReceivers, Instant oldestMessageTimestamp, Long messageCount) {
        this.historyReceivers = historyReceivers;
        this.oldestMessageTimestamp = oldestMessageTimestamp;
        this.messageCount = messageCount;
    }

    /**
     * Returns an unmodifiable view of the display strings that identify the
     * recipients of the shared history.
     *
     * @return the list of recipients, or an empty list when none were
     *         provided
     */
    public List<String> historyReceivers() {
        return historyReceivers == null ? List.of() : Collections.unmodifiableList(historyReceivers);
    }

    /**
     * Returns the timestamp of the earliest message included in the shared
     * bundle.
     *
     * @return an {@link Optional} containing the oldest-message timestamp, or
     *         {@link Optional#empty()} when it was not provided
     */
    public Optional<Instant> oldestMessageTimestamp() {
        return Optional.ofNullable(oldestMessageTimestamp);
    }

    /**
     * Returns the total number of messages contained in the shared bundle.
     *
     * @return an {@link OptionalLong} containing the message count, or
     *         {@link OptionalLong#empty()} when it was not provided
     */
    public OptionalLong messageCount() {
        return messageCount == null ? OptionalLong.empty() : OptionalLong.of(messageCount);
    }

    /**
     * Sets the display strings that identify the recipients of the shared
     * history.
     *
     * @param historyReceivers the new list of recipients, may be {@code null}
     */
    public void setHistoryReceivers(List<String> historyReceivers) {
        this.historyReceivers = historyReceivers;
    }

    /**
     * Sets the timestamp of the earliest message included in the shared
     * bundle.
     *
     * @param oldestMessageTimestamp the new timestamp, may be {@code null}
     */
    public void setOldestMessageTimestamp(Instant oldestMessageTimestamp) {
        this.oldestMessageTimestamp = oldestMessageTimestamp;
    }

    /**
     * Sets the total number of messages contained in the shared bundle.
     *
     * @param messageCount the new message count, may be {@code null}
     */
    public void setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
    }
}
