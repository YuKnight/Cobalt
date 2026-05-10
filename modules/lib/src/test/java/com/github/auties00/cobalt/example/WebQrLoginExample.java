import com.github.auties00.cobalt.call.CallOptions;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientVerificationHandler;
import com.github.auties00.cobalt.client.WhatsAppDevice;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.model.chat.ChatMessageInfo;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;
import com.github.auties00.cobalt.util.SchedulerUtils;

void main() throws IOException {
    WhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.temporary())
            .createConnection()
            .device(WhatsAppDevice.desktop())
            .historySetting(WhatsAppWebClientHistory.extended(true))
            .unregistered(WhatsAppClientVerificationHandler.Web.QrCode.toTerminal())
            .addLoggedInListener(api -> {
                System.out.printf("Connected: %s%n", api.store().privacySettings());
                SchedulerUtils.scheduleDelayed(Duration.ofSeconds(10), () -> {
                    System.out.println("Starting call");
                    try(var call = api.startCall(Jid.of(393457877997L), CallOptions.audio())) {
                        System.out.println("Call started: " + call.callId());
                        TimeUnit.SECONDS.sleep(10);
                        System.out.println("Closing call");
                    }catch(Throwable throwable) {
                        System.err.println("Call failed");
                        throwable.printStackTrace();
                    }
                });
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
            .addWebHistorySyncMessagesListener((_, chat, last) -> System.out.printf("%s now has %s messages: %s(oldest message: %s)%n", chat.name(), chat.messageCount(), !last ? "waiting for more" : "done", chat.oldestMessage().flatMap(ChatMessageInfo::timestamp).orElse(null)))
            .addDisconnectedListener((_, reason) -> System.out.printf("Disconnected: %s%n", reason))
            .connect()
            .waitForDisconnection();
}