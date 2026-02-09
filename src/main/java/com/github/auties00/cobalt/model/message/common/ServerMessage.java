package com.github.auties00.cobalt.model.message.common;

import com.github.auties00.cobalt.model.message.server.*;
import com.github.auties00.cobalt.model.message.standard.PinInChatMessage;

/**
 * A model interface that represents a message sent by a WhatsappWeb's server
 */
public sealed interface ServerMessage extends Message permits DeviceSentMessage, DeviceSyncMessage, PinInChatMessage, ProtocolMessage, SenderKeyDistributionMessage, StickerSyncRMRMessage, MessageHistoryBundle, MessageHistoryNotice {
    @Override
    default Category category() {
        return Category.SERVER;
    }
}
