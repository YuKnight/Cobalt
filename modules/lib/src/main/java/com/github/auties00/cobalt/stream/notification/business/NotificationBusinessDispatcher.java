package com.github.auties00.cobalt.stream.notification.business;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.migration.LidMigrationService;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.stream.SocketStream;

/**
 * Routes inbound business-category notification stanzas to specialised
 * handlers based on the {@code type} attribute of the stanza.
 *
 * <p>This dispatcher owns one instance of each concrete business-category
 * handler and forwards each incoming node to the matching handler. Stanzas
 * with an unrecognised {@code type} are silently ignored.
 *
 * @implNote Adapts the WhatsApp Web dispatch that routes business
 *     notifications to {@code WAWebHandleBusinessNotification},
 *     {@code WAWebHandleMexNotification} and
 *     {@code WAWebHandlePaymentNotification}.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleNotification")
public final class NotificationBusinessDispatcher implements SocketStream.Handler {
    /**
     * Handler for {@code business}, {@code digital_commerce_subscription}
     * and {@code fb:update} notifications.
     */
    private final NotificationBusinessStreamHandler businessHandler;

    /**
     * Handler for {@code mex} GraphQL subscription update notifications.
     */
    private final NotificationMexStreamHandler mexHandler;

    /**
     * Handler for {@code pay} payment notifications.
     */
    private final NotificationPaymentStreamHandler paymentHandler;

    /**
     * Constructs a new dispatcher and instantiates every sub-handler with
     * the shared {@link WhatsAppClient} and {@link LidMigrationService}.
     *
     * @param whatsapp            the non-{@code null} client providing store and network access
     * @param lidMigrationService the non-{@code null} migration service consumed by the MEX handler
     */
    public NotificationBusinessDispatcher(WhatsAppClient whatsapp, LidMigrationService lidMigrationService) {
        this.businessHandler = new NotificationBusinessStreamHandler(whatsapp);
        this.mexHandler = new NotificationMexStreamHandler(whatsapp, lidMigrationService);
        this.paymentHandler = new NotificationPaymentStreamHandler(whatsapp);
    }

    /**
     * Dispatches the incoming node to the appropriate business-category
     * handler based on the stanza's {@code type} attribute.
     *
     * @param node the incoming notification stanza
     * @implNote Mirrors the {@code type}-based switch in
     *     {@code WAWebHandleNotification.handleNotification} for the
     *     business category.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleNotification", exports = "handleNotification", adaptation = WhatsAppAdaptation.ADAPTED)
    @Override
    public void handle(Node node) {
        var type = node.getAttributeAsString("type", null);
        if (type == null) {
            return;
        }

        switch (type) {
            case "business", "digital_commerce_subscription", "fb:update" -> businessHandler.handle(node);
            case "mex" -> mexHandler.handle(node);
            case "pay" -> paymentHandler.handle(node);
            default -> {
            }
        }
    }

    /**
     * Fans out a reset call to every sub-handler so that any cached state
     * is discarded on a socket reconnect.
     *
     * @implNote Cobalt-specific lifecycle hook; WhatsApp Web handles this
     *     via module-level reset calls.
     */
    @Override
    public void reset() {
        businessHandler.reset();
        mexHandler.reset();
        paymentHandler.reset();
    }
}
