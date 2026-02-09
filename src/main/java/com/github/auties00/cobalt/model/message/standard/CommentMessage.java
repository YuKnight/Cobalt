package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.Message;
import com.github.auties00.cobalt.model.message.common.MessageContainer;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A comment message on a post or status.
 *
 * <p>In group contexts that require encryption (CAG), this is
 * encrypted into an {@link EncryptedCommentMessage} before sending.
 *
 * @apiNote WAWebProtobufsE2E.pb.Message.CommentMessage: contains
 * the comment content as a nested Message and the target message key.
 */
@ProtobufMessage(name = "Message.CommentMessage")
public final class CommentMessage implements Message {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final MessageContainer message;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    final ChatMessageKey targetMessageKey;

    CommentMessage(MessageContainer message, ChatMessageKey targetMessageKey) {
        this.message = message;
        this.targetMessageKey = Objects.requireNonNull(targetMessageKey, "targetMessageKey cannot be null");
    }

    public Optional<MessageContainer> message() {
        return Optional.ofNullable(message);
    }

    public ChatMessageKey targetMessageKey() {
        return targetMessageKey;
    }

    @Override
    public Type type() {
        return Type.COMMENT;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CommentMessage that
                && Objects.equals(message, that.message)
                && Objects.equals(targetMessageKey, that.targetMessageKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, targetMessageKey);
    }

    @Override
    public String toString() {
        return "CommentMessage[" +
                "message=" + message +
                ", targetMessageKey=" + targetMessageKey +
                ']';
    }
}
