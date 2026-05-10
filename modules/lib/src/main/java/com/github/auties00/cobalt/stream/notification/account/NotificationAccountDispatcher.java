package com.github.auties00.cobalt.stream.notification.account;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.device.DeviceService;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.stream.SocketStream;

/**
 * Routes inbound {@code account} category notification stanzas to specialised
 * handlers based on the {@code type} attribute of the stanza.
 *
 * <p>This dispatcher owns one instance of each concrete account-category
 * handler and forwards each incoming node to the matching handler. Stanzas
 * with an unrecognised {@code type} are silently ignored.
 */
@WhatsAppWebModule(moduleName = "WAWebCommsHandleLoggedInStanza")
public final class NotificationAccountDispatcher implements SocketStream.Handler {
    /**
     * Handler for {@code type="account_sync"} notifications.
     */
    private final NotificationAccountStreamHandler accountHandler;

    /**
     * Handler for {@code type="contacts"} notifications.
     */
    private final NotificationContactStreamHandler contactHandler;

    /**
     * Handler for {@code type="disappearing_mode"} notifications.
     */
    private final NotificationDisappearingModeStreamHandler disappearingModeHandler;

    /**
     * Handler for {@code type="privacy_token"} notifications.
     */
    private final NotificationPrivacyStreamHandler privacyHandler;

    /**
     * Handler for {@code type="picture"} and {@code type="status"} notifications.
     */
    private final NotificationProfileStreamHandler profileHandler;

    /**
     * Constructs a new dispatcher and instantiates every sub-handler with the
     * shared {@link WhatsAppClient} and {@link DeviceService}.
     *
     * @param whatsapp      the non-{@code null} client providing store and network access
     * @param deviceService the non-{@code null} device service used by the account-sync handler
     */
    public NotificationAccountDispatcher(WhatsAppClient whatsapp, DeviceService deviceService) {
        this.accountHandler = new NotificationAccountStreamHandler(whatsapp, deviceService);
        this.contactHandler = new NotificationContactStreamHandler(whatsapp);
        this.disappearingModeHandler = new NotificationDisappearingModeStreamHandler(whatsapp);
        this.privacyHandler = new NotificationPrivacyStreamHandler(whatsapp);
        this.profileHandler = new NotificationProfileStreamHandler(whatsapp);
    }

    /**
     * Dispatches the incoming node to the appropriate account-category
     * handler based on the stanza's {@code type} attribute.
     *
     * @param node the incoming notification stanza
     */
    @WhatsAppWebExport(moduleName = "WAWebCommsHandleLoggedInStanza", exports = "handleLoggedInStanza", adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public void handle(Node node) {
        var type = node.getAttributeAsString("type", null);
        if (type == null) {
            return;
        }

        switch (type) {
            case "account_sync" -> accountHandler.handle(node);
            case "contacts" -> contactHandler.handle(node);
            case "disappearing_mode" -> disappearingModeHandler.handle(node);
            case "privacy_token" -> privacyHandler.handle(node);
            case "picture", "status" -> profileHandler.handle(node);
            default -> {
            }
        }
    }

    /**
     * Fans out a reset call to every sub-handler so that any cached state
     * (pending acks, in-flight refresh jobs, etc.) is discarded on a socket
     * reconnect.
     */
    @Override
    public void reset() {
        accountHandler.reset();
        contactHandler.reset();
        disappearingModeHandler.reset();
        privacyHandler.reset();
        profileHandler.reset();
    }
}
