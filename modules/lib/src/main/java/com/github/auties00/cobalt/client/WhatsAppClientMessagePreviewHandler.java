
package com.github.auties00.cobalt.client;

import com.github.auties00.cobalt.model.message.Message;

// TODO
@FunctionalInterface
public interface WhatsAppClientMessagePreviewHandler {
    static WhatsAppClientMessagePreviewHandler enabled(boolean allowInference) {
        return message -> {};
    }

    static WhatsAppClientMessagePreviewHandler disabled() {
        return _ -> {};
    }

    void attribute(Message message);
}