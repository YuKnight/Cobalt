package com.github.auties00.cobalt.model.message.system.history;

import com.github.auties00.cobalt.model.message.context.ContextInfo;
import com.github.auties00.cobalt.model.message.context.ContextualMessage;

import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.*;
import java.util.Optional;

/**
 * Informs a chat participant that a batch of historical messages is about to
 * be shared with them.
 *
 * <p>This notice is sent right before the corresponding
 * {@link MessageHistoryBundle} so that the chat UI can render a preview
 * banner (for example "You will receive N messages since T") and prepare the
 * user for the back-fill that follows. Unlike the bundle itself, the notice
 * carries no media reference; it only carries the descriptive metadata and
 * the surrounding context info.
 */
@ProtobufMessage(name = "Message.MessageHistoryNotice")
public final class MessageHistoryNotice implements ContextualMessage {
    /**
     * The quoted-message and mention context in which the notice was sent.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    ContextInfo contextInfo;

    /**
     * The descriptive metadata (recipients, oldest timestamp, message count)
     * of the history bundle being announced.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    MessageHistoryMetadata messageHistoryMetadata;


    /**
     * Constructs a new history-sharing notice.
     *
     * @param contextInfo            the surrounding context info
     * @param messageHistoryMetadata the metadata of the announced history
     *                               bundle
     */
    MessageHistoryNotice(ContextInfo contextInfo, MessageHistoryMetadata messageHistoryMetadata) {
        this.contextInfo = contextInfo;
        this.messageHistoryMetadata = messageHistoryMetadata;
    }

    /**
     * Returns the contextual information (quoted message, mentions, ephemeral
     * settings) that surrounds this notice.
     *
     * @return an {@link Optional} containing the context info, or
     *         {@link Optional#empty()} if it was not provided
     */
    public Optional<ContextInfo> contextInfo() {
        return Optional.ofNullable(contextInfo);
    }

    /**
     * Returns the descriptive metadata of the history bundle being announced.
     *
     * @return an {@link Optional} containing the history metadata, or
     *         {@link Optional#empty()} if it was not provided
     */
    public Optional<MessageHistoryMetadata> messageHistoryMetadata() {
        return Optional.ofNullable(messageHistoryMetadata);
    }

    /**
     * Sets the contextual information associated with this notice.
     *
     * @param contextInfo the new context info, may be {@code null}
     */
    public void setContextInfo(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;
    }

    /**
     * Sets the descriptive metadata of the announced history bundle.
     *
     * @param messageHistoryMetadata the new history metadata, may be
     *                               {@code null}
     */
    public void setMessageHistoryMetadata(MessageHistoryMetadata messageHistoryMetadata) {
        this.messageHistoryMetadata = messageHistoryMetadata;
    }
}
