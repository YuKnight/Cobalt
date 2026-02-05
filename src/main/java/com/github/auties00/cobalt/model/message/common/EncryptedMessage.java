package com.github.auties00.cobalt.model.message.common;

import com.github.auties00.cobalt.model.message.standard.EncEventResponseMessage;
import com.github.auties00.cobalt.model.message.standard.EncryptedReactionMessage;
import com.github.auties00.cobalt.model.message.standard.PollUpdateMessage;
import com.github.auties00.cobalt.model.message.standard.SecretEncryptedMessage;

public sealed interface EncryptedMessage permits EncEventResponseMessage, EncryptedReactionMessage, PollUpdateMessage, SecretEncryptedMessage {
    String secretName();
}
