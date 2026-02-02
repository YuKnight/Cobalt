package com.github.auties00.cobalt.socket.state;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.migration.LidMigrationService;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.socket.SocketStream;

public final class WebOnInitialInfoStreamNodeHandler extends SocketStream.Handler {
    private final LidMigrationService lidMigrationService;
    private final ABPropsService abPropsService;

    public WebOnInitialInfoStreamNodeHandler(WhatsAppClient whatsapp, LidMigrationService lidMigrationService, ABPropsService abPropsService) {
        super(whatsapp, "success");
        this.lidMigrationService = lidMigrationService;
        this.abPropsService = abPropsService;
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

        // Trigger A/B props query
        abPropsService.sync();

        // Handle LID migration
        var lidMigrationEnabled = abPropsService.getBool(ABProp.LID_STATUS_SEND_ENABLED_AB_PROP_CODE)
                .orElse(true);
        if (lidMigrationEnabled) {
            lidMigrationService.enableMigration();
        } else {
            lidMigrationService.disableMigration();
        }

        for(var listener : whatsapp.store().listeners()) {
            Thread.startVirtualThread(() -> listener.onLoggedIn(whatsapp));
        }
    }
}
