package com.github.auties00.cobalt.socket.state;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.socket.SocketStream;

public final class WebPropsStreamNodeHandler extends SocketStream.Handler {
    private final ABPropsService abPropsService;

    public WebPropsStreamNodeHandler(WhatsAppClient whatsapp, ABPropsService abPropsService) {
        super(whatsapp, "iq");
        this.abPropsService = abPropsService;
    }

    @Override
    public void handle(Node node) {
        if (!node.hasAttribute("xmlns", "abt")) {
            return;
        }

        abPropsService.process(node);
    }

    @Override
    public void reset() {
        abPropsService.clear();
    }
}
