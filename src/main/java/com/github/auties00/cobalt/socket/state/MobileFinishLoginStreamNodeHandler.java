package com.github.auties00.cobalt.socket.state;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.migration.LidMigrationService;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.socket.SocketStream;

public final class MobileFinishLoginStreamNodeHandler extends SocketStream.Handler {
    private final LidMigrationService lidMigrationService;

    public MobileFinishLoginStreamNodeHandler(WhatsAppClient whatsapp, LidMigrationService lidMigrationService) {
        super(whatsapp, "success");
        this.lidMigrationService = lidMigrationService;
    }

    @Override
    public void handle(Node node) {
        // Initialize LID migration service
        lidMigrationService.initialize();
        lidMigrationService.onMigrationEnabled(); // We don't really support A/B props, so we always enable migration

        // TODO: Implement mobile login
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
