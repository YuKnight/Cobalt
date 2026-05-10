import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;

void main() throws IOException {
    long phoneNumber = 393495089819L;
    WhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.temporary())
            .createConnection()
            .unregistered(phoneNumber, code -> System.out.println("COBALT_PAIRING_CODE=" + code))
            .addNodeReceivedListener((_, incoming) -> System.out.printf("Received node %s%n", incoming))
            .addNodeSentListener((_, outgoing) -> System.out.printf("Sent node %s%n", outgoing))
            .addLoggedInListener(api -> System.out.println("COBALT_LOGGED_IN"))
            .addDisconnectedListener((_, reason) -> System.out.println("COBALT_DISCONNECTED=" + reason))
            .connect()
            .waitForDisconnection();
    System.out.println("COBALT_DONE");
}
