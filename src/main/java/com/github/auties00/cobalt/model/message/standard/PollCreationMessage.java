package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.info.ChatMessageInfo;
import com.github.auties00.cobalt.model.info.ContextInfo;
import com.github.auties00.cobalt.model.message.common.ContextualMessage;
import com.github.auties00.cobalt.model.poll.PollOption;
import com.github.auties00.cobalt.util.SecureBytes;
import it.auties.protobuf.annotation.ProtobufBuilder;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;
import java.util.Optional;


/**
 * A model class that represents a message holding a poll inside
 */
@ProtobufMessage(name = "Message.PollCreationMessage")
public final class PollCreationMessage implements ContextualMessage {
    @ProtobufProperty(index = 1, type = ProtobufType.BYTES)
    byte[] encryptionKey;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String title;

    @ProtobufProperty(index = 3, type = ProtobufType.MESSAGE)
    final List<PollOption> selectableOptions;

    @ProtobufProperty(index = 4, type = ProtobufType.UINT32)
    final int selectableOptionsCount;

    @ProtobufProperty(index = 5, type = ProtobufType.MESSAGE)
    ContextInfo contextInfo;

    PollCreationMessage(byte[] encryptionKey, String title, List<PollOption> selectableOptions, int selectableOptionsCount, ContextInfo contextInfo) {
        this.encryptionKey = encryptionKey;
        this.title = title;
        this.selectableOptions = selectableOptions;
        this.selectableOptionsCount = selectableOptionsCount;
        this.contextInfo = contextInfo;
    }

    /**
     * Constructs a new builder to create a PollCreationMessage The newsletters can be later sent using
     * {@link WhatsAppClient#sendChatMessage(ChatMessageInfo)}
     *
     * @param title             the non-null title of the poll
     * @param selectableOptions the null-null non-empty options of the poll
     * @return a non-null new message
     */
    @ProtobufBuilder(className = "PollCreationMessageSimpleBuilder")
    static PollCreationMessage simpleBuilder(String title, List<PollOption> selectableOptions) {
        if (title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (selectableOptions.size() <= 1) {
            throw new IllegalArgumentException("Options must have at least two entries");
        }
        return new PollCreationMessageBuilder()
                .encryptionKey(SecureBytes.random(32))
                .title(title)
                .selectableOptions(selectableOptions)
                .selectableOptionsCount(selectableOptions.size())
                .build();
    }

    @Override
    public Type type() {
        return Type.POLL_CREATION;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    public String title() {
        return title;
    }

    public List<PollOption> selectableOptions() {
        return selectableOptions;
    }

    public int selectableOptionsCount() {
        return selectableOptionsCount;
    }

    public Optional<byte[]> encryptionKey() {
        return Optional.ofNullable(encryptionKey);
    }

    public void setEncryptionKey(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    @Override
    public Optional<ContextInfo> contextInfo() {
        return Optional.ofNullable(contextInfo);
    }

    @Override
    public void setContextInfo(ContextInfo contextInfo) {
        this.contextInfo = contextInfo;
    }
}