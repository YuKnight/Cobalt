package com.github.auties00.cobalt.stream.notification.account;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.model.chat.ChatEphemeralTimer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.stream.SocketStream;

import java.time.Instant;

/**
 * Handles incoming {@code disappearing_mode} notification stanzas.
 *
 * <p>When a contact changes their default disappearing-message duration, the server
 * pushes a notification of type {@code disappearing_mode}. This handler parses the
 * duration and setting timestamp from the child {@code <disappearing_mode>} element,
 * updates the corresponding chat's ephemeral timer in the local store (with a
 * timestamp guard to prevent stale updates from overwriting newer ones), and sends
 * an acknowledgement stanza back to the server.
 *
 */
@WhatsAppWebModule(moduleName = "WAWebHandleDisappearingModeNotification")
final class NotificationDisappearingModeStreamHandler implements SocketStream.Handler {
    /**
     * Logger for diagnostic messages from this handler.
     */
    private static final System.Logger LOGGER = System.getLogger(NotificationDisappearingModeStreamHandler.class.getName());

    /**
     * The WhatsApp client providing access to the store and socket operations.
     */
    private final WhatsAppClient whatsapp;

    /**
     * Constructs a new handler with the specified WhatsApp client.
     *
     * @param whatsapp the non-{@code null} WhatsApp client instance
     */
    NotificationDisappearingModeStreamHandler(WhatsAppClient whatsapp) {
        this.whatsapp = whatsapp;
    }

    /**
     * Handles a {@code disappearing_mode} notification stanza by parsing the disappearing
     * mode duration and setting timestamp, updating the corresponding chat's ephemeral
     * timer in the local store, and sending an acknowledgement stanza.
     *
     * <p>The handler extracts the {@code from} attribute as a user JID and looks up the
     * corresponding chat. If the chat does not exist, the notification is silently ignored
     * (matching WA Web's behavior of skipping absent contact records). Updates are guarded
     * by a timestamp comparison: the chat's ephemeral settings are only modified if the
     * existing setting timestamp is {@code null} (and the new timestamp is non-zero) or
     * if the existing timestamp is strictly less than the new one, preventing stale
     * notifications from overwriting newer settings.
     *
     * @param node the non-{@code null} notification stanza node
     */
    @Override
    public void handle(Node node) {
        handleDisappearingModeNotification(node);
        sendNotificationAck(node);
    }

    /**
     * Performs the core disappearing-mode update logic by parsing the notification, looking up the chat, applying the timestamp guard, and updating the ephemeral settings.
     *
     * @param node the non-{@code null} notification stanza node
     */
    private void handleDisappearingModeNotification(Node node) {
        var from = node.getAttributeAsJid("from")
                .map(jid -> jid.toUserJid())
                .orElse(null);
        var disappearingMode = node.getChild("disappearing_mode").orElse(null);
        if (from == null || disappearingMode == null) {
            return;
        }

        var duration = disappearingMode.getAttributeAsInt("duration", 0);
        var rawTimestamp = disappearingMode.getAttributeAsLong("t", (Long) null);
        var settingTimestamp = rawTimestamp != null ? Instant.ofEpochSecond(rawTimestamp) : null;

        // WA Web looks up a contact record; Cobalt uses chat-level ephemeral fields.
        var chat = whatsapp.store()
                .findChatByJid(from)
                .orElse(null);
        if (chat == null) {
            return;
        }

        // Timestamp guard: only update when the existing timestamp is null and the new one is non-zero, or when the existing timestamp is strictly older than the new one.
        var existingTimestamp = chat.ephemeralSettingTimestamp().orElse(null);
        var newTimestampSeconds = rawTimestamp != null ? rawTimestamp : 0L;
        if (existingTimestamp == null && newTimestampSeconds != 0
                || existingTimestamp != null && settingTimestamp != null && existingTimestamp.isBefore(settingTimestamp)) {
            chat.setEphemeralExpiration(ChatEphemeralTimer.of(duration));
            chat.setEphemeralSettingTimestamp(settingTimestamp);
        }
    }

    /**
     * Sends an acknowledgement stanza back to the server for the received notification.
     *
     * <p>The ack stanza includes the original stanza's {@code id} as its own {@code id},
     * the {@code from} JID as the {@code to} target, a {@code class} of
     * {@code "notification"}, and a {@code type} of {@code "disappearing_mode"}.
     *
     * @param node the non-{@code null} notification stanza node to acknowledge
     */
    private void sendNotificationAck(Node node) {
        var stanzaId = node.getAttributeAsString("id", null);
        var stanzaFrom = node.getAttributeAsJid("from", null);
        if (stanzaId == null || stanzaFrom == null) {
            return;
        }

        whatsapp.sendNodeWithNoResponse(new NodeBuilder()
                .description("ack")
                .attribute("id", stanzaId)
                .attribute("to", stanzaFrom)
                .attribute("class", "notification")
                .attribute("type", "disappearing_mode")
                .build());
    }
}
