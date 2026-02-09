package com.github.auties00.cobalt.model.message.common;

import com.github.auties00.cobalt.model.message.standard.EncryptedCommentMessage;
import com.github.auties00.cobalt.model.message.standard.EncryptedEventResponseMessage;
import com.github.auties00.cobalt.model.message.standard.EncryptedReactionMessage;
import com.github.auties00.cobalt.model.message.standard.PollUpdateMessage;
import com.github.auties00.cobalt.model.message.standard.SecretEncryptedMessage;

public sealed interface EncryptedMessage extends Message permits EncryptedCommentMessage, EncryptedEventResponseMessage, EncryptedReactionMessage, PollUpdateMessage, SecretEncryptedMessage {
    @Override
    default Category category() {
        return Category.ENCRYPTED;
    }
}
