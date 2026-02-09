package com.github.auties00.cobalt.model.message.standard;

import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import com.github.auties00.cobalt.model.message.common.Message;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * A model class that represents a status notification message.
 */
@ProtobufMessage(name = "Message.StatusNotificationMessage")
public final class StatusNotificationMessage implements Message {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final ChatMessageKey responseMessageKey;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    final ChatMessageKey originalMessageKey;

    @ProtobufProperty(index = 3, type = ProtobufType.ENUM)
    final StatusNotificationType notificationType;

    StatusNotificationMessage(ChatMessageKey responseMessageKey, ChatMessageKey originalMessageKey, StatusNotificationType notificationType) {
        this.responseMessageKey = responseMessageKey;
        this.originalMessageKey = originalMessageKey;
        this.notificationType = notificationType;
    }

    public Optional<ChatMessageKey> responseMessageKey() {
        return Optional.ofNullable(responseMessageKey);
    }

    public Optional<ChatMessageKey> originalMessageKey() {
        return Optional.ofNullable(originalMessageKey);
    }

    public StatusNotificationType notificationType() {
        return notificationType != null ? notificationType : StatusNotificationType.UNKNOWN;
    }

    @Override
    public Type type() {
        return Type.STATUS_NOTIFICATION;
    }

    @Override
    public Category category() {
        return Category.STANDARD;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof StatusNotificationMessage that
                && Objects.equals(responseMessageKey, that.responseMessageKey)
                && Objects.equals(originalMessageKey, that.originalMessageKey)
                && notificationType == that.notificationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(responseMessageKey, originalMessageKey, notificationType);
    }

    @Override
    public String toString() {
        return "StatusNotificationMessage[type=" + notificationType + ']';
    }

    @ProtobufEnum(name = "Message.StatusNotificationMessage.StatusNotificationType")
    public enum StatusNotificationType {
        UNKNOWN(0),
        STATUS_ADD_YOURS(1),
        STATUS_RESHARE(2),
        STATUS_QUESTION_ANSWER_RESHARE(3);

        final int index;

        StatusNotificationType(@ProtobufEnumIndex int index) {
            this.index = index;
        }

        public int index() {
            return index;
        }
    }
}
