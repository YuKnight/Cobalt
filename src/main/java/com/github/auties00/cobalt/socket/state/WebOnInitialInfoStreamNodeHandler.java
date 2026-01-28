package com.github.auties00.cobalt.socket.state;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.migration.LidMigrationService;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.socket.SocketStream;

public final class WebOnInitialInfoStreamNodeHandler extends SocketStream.Handler {
    private final LidMigrationService lidMigrationService;

    public WebOnInitialInfoStreamNodeHandler(WhatsAppClient whatsapp, LidMigrationService lidMigrationService) {
        super(whatsapp, "success");
        this.lidMigrationService = lidMigrationService;
    }

    @Override
    public void handle(Node node) {
        if(!whatsapp.store().registered()) {
            whatsapp.store()
                    .setRegistered(true);
            whatsapp.store()
                    .serialize();
        }

        // Initialize LID migration service
        lidMigrationService.initialize();
        lidMigrationService.onMigrationEnabled(); // We don't really support A/B props, so we always enable migration

        for(var listener : whatsapp.store().listeners()) {
            Thread.startVirtualThread(() -> listener.onLoggedIn(whatsapp));
        }
    }
}
