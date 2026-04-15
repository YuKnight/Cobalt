package com.github.auties00.cobalt.socket.application;

import com.github.auties00.cobalt.socket.threading.SocketClientLayerContext;
import com.github.auties00.cobalt.socket.application.websocket.WebSocketLayerContext;
import com.github.auties00.cobalt.socket.application.whatsapp.WhatsAppLayerContext;

public sealed interface SocketClientApplicationLayerContext extends SocketClientLayerContext permits WebSocketLayerContext, WhatsAppLayerContext {

}
