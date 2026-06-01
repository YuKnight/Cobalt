import com.github.auties00.cobalt.call.CallOptions;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientVerificationHandler;
import com.github.auties00.cobalt.client.WhatsAppClientDevice;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;

/**
 * Runnable example that logs a Web client in by printing a QR code to the terminal, then registers
 * listeners that report sync progress (contacts, chats, newsletters, history, app-state actions)
 * and send a message to a named chat once chats arrive; run it as a single-file program through the
 * launcher protocol.
 */
void main() throws IOException {
    LinkedWhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.persistent())
            .loadLatestOrCreateConnection()
            .device(WhatsAppClientDevice.web())
            .historySetting(WhatsAppWebClientHistory.standard(false))
            .unregistered(WhatsAppClientVerificationHandler.Web.QrCode.toTerminal())
            .addLoggedInListener(api -> {
                System.out.printf("Connected: %s%n", api.store().settingsStore().privacySettings());
                var peer = Jid.of("393668765864@s.whatsapp.net");
                try(var call = api.startCall(peer, CallOptions.audio())) {
                    System.out.println("Call started");
                    call.awaitEnded();
                    System.out.println("Call ended");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            })
            .addWebAppPrimaryFeaturesListener((_, features) -> System.out.printf("Received features: %s%n", features))
            .addNewMessageListener((_, message) -> System.out.println(message))
            .addContactsListener((_, contacts) -> System.out.printf("Contacts: %s%n", contacts.size()))
            .addChatsListener((api, chats) -> System.out.printf("Chats: %s%n", chats.size()))
            .addNewslettersListener((_, newsletters) -> System.out.printf("Newsletters: %s%n", newsletters.size()))
            .addNodeReceivedListener((_, incoming) -> System.out.printf("Received node %s%n", incoming))
            .addNodeSentListener((_, outgoing) -> System.out.printf("Sent node %s%n", outgoing))
            .addWebAppStateActionListener((_, action, info) -> System.out.printf("New action: %s, info: %s%n", action, info))
            .addMessageStatusListener((_, info) -> System.out.printf("Message status update for %s%n", info.key().id()))
            .addWebHistorySyncMessagesListener((_, chats, last) -> {
                for (var chat : chats) {
                    System.out.printf("%s now has %s messages (oldest message: %s)%n", chat.name(), chat.messageCount(), chat.oldestMessage().flatMap(ChatMessageInfo::timestamp).orElse(null));
                }
                System.out.printf("History sync chunk: %s chats, %s%n", chats.size(), last ? "done" : "waiting for more");
            })
            .addDisconnectedListener((_, reason) -> System.out.printf("Disconnected: %s%n", reason))
            .connect()
            .waitForDisconnection();
}