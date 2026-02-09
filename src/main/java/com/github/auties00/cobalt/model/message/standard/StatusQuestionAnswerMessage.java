package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A model class that represents a status question answer message.
 */
@ProtobufMessage(name = "Message.StatusQuestionAnswerMessage")
public final class StatusQuestionAnswerMessage implements Message {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey key;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String text;

    StatusQuestionAnswerMessage(ChatMessageKey key, String text) {
        this.key = key;
        this.text = text;
    }

    public Optional<ChatMessageKey> key() {
        return Optional.ofNullable(key);
    }

    public Optional<String> text() {
        return Optional.ofNullable(text);
    }

    @Override
    public Type type() {
        return Type.STATUS_QUESTION_ANSWER;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StatusQuestionAnswerMessage that
                && Objects.equals(key, that.key)
                && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, text);
    }

    @Override
    public String toString() {
        return "StatusQuestionAnswerMessage[text=" + text + ']';
    }
}
