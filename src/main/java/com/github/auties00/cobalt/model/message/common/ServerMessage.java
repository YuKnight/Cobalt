package com.github.auties00.cobalt.model.message.common;

import com.github.auties00.cobalt.model.message.server.*;
import com.github.auties00.cobalt.model.message.standard.EncEventResponseMessage;
import com.github.auties00.cobalt.model.message.standard.EncryptedReactionMessage;
import com.github.auties00.cobalt.model.message.standard.PinInChatMessage;
import com.github.auties00.cobalt.model.message.standard.SecretEncryptedMessage;

/**
 * A model interface that represents a message sent by a WhatsappWeb's server
 */
public sealed interface ServerMessage extends Message permits DeviceSentMessage, DeviceSyncMessage, EncEventResponseMessage, EncryptedReactionMessage, PinInChatMessage, ProtocolMessage, SecretEncryptedMessage, SenderKeyDistributionMessage, StickerSyncRMRMessage {
    @Override
    default Category category() {
        return Category.SERVER;
    }
}
