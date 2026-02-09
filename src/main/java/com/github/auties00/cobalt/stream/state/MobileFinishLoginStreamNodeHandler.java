package com.github.auties00.cobalt.stream.state;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.migration.LidMigrationService;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.stream.SocketStream;

public final class MobileFinishLoginStreamNodeHandler extends SocketStream.Handler {
    private final LidMigrationService lidMigrationService;

    public MobileFinishLoginStreamNodeHandler(WhatsAppClient whatsapp, LidMigrationService lidMigrationService) {
        super(whatsapp, "success");
        this.lidMigrationService = lidMigrationService;
    }

    @Override
    public void handle(Node node) {
        // TODO: Implement mobile login
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
