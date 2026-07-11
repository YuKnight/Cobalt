import com.github.auties00.cobalt.calls.stream.AudioInput;
import com.github.auties00.cobalt.calls.stream.AudioOutput;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.client.linked.LinkedWhatsAppClientDevice;
import com.github.auties00.cobalt.client.linked.LinkedWhatsAppClientVerificationHandler;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.device.pairing.ClientPayload;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.linked.LinkedWhatsAppStoreFactory;

/**
 * Runnable example that logs a Web client in by printing a QR code to the terminal, then registers
 * listeners that report sync progress (contacts, chats, newsletters, history, app-state actions)
 * and send a message to a named chat once chats arrive; run it as a single-file program through the
 * launcher protocol.
 */
void main() throws IOException {
    System.out.println("Hello World");
    WhatsAppClient.builder()
            .linkedApi()
            .webClient(LinkedWhatsAppStoreFactory.persistent())
            .loadLatestOrCreateConnection()
            .device(LinkedWhatsAppClientDevice.web())
            .defaultHistory()
            .releaseChannel(ClientPayload.ClientReleaseChannel.BETA)
            .unregistered(LinkedWhatsAppClientVerificationHandler.Web.Passkey.toTerminal())
            .addLoggedInListener(client -> {
                try {
                    System.out.printf("Connected: %s%n", client.store().settingsStore().privacySettings());
                    var peer = Jid.of("19153544650@s.whatsapp.net");
                    var audio = Path.of("C:\\Users\\Alessandro Autiero\\Downloads\\Brazy girls.mp3");
                    var video = Path.of("C:\\Users\\Alessandro Autiero\\Downloads\\file_example_MP4_1920_18MG.mp4");
                    var path = Files.createTempFile("cobalt-call-inbound-", ".wav");
                    var path1 = Files.createTempFile("cobalt-call-inbound1-", ".y4m");

                    var call = client.startCall(peer,
                            AudioOutput.fromFile(audio), AudioInput.toWav(path));
                    System.out.println("Path: " + path);
                    System.out.printf("Called %s: %s%n", peer, call.callId());
                }catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            })
            .addWebAppPrimaryFeaturesListener((_, features) -> System.out.printf("Received features: %s%n", features))
            .addNewMessageListener((_, message) -> System.out.println(message))
            .addContactsListener((_, contacts) -> System.out.printf("Contacts: %s%n", contacts.size()))
            .addChatsListener((api, chats) -> System.out.printf("Chats: %s%n", chats.size()))
            .addNewslettersListener((_, newsletters) -> System.out.printf("Newsletters: %s%n", newsletters.size()))
            .addNodeReceivedListener((_, incoming) -> System.out.printf("Received stanza %s%n", incoming))
            .addNodeSentListener((_, outgoing) -> System.out.printf("Sent stanza %s%n", outgoing))
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