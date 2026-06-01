import com.github.auties00.cobalt.call.CallOptions;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientDevice;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;

/**
 * Pairs a fresh Cobalt store to phone number 393495089819 via the WhatsApp pairing-code flow
 * (no QR), then dials peer 19153544650 once linked. Run with a fresh persistent store directory
 * so the existing 393495089819 session is not loaded.
 */
void main() throws IOException {
    var ownNumber = 393495089819L;
    LinkedWhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.persistent())
            .loadLatestOrCreateConnection()
            .device(WhatsAppClientDevice.web())
            .historySetting(WhatsAppWebClientHistory.standard(false))
            .unregistered(ownNumber, code -> System.out.println("PAIRING CODE: " + code))
            .addLoggedInListener(api -> {
                System.out.println("Linked. Connected: " + api.store().settingsStore().privacySettings());
                var peer = Jid.of("19153544650@s.whatsapp.net");
                try (var call = api.startCall(peer, CallOptions.audio())) {
                    System.out.println("Call started");
                    call.awaitEnded();
                    System.out.println("Call ended");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            })
            .addNodeReceivedListener((_, incoming) -> System.out.printf("Received node %s%n", incoming))
            .addNodeSentListener((_, outgoing) -> System.out.printf("Sent node %s%n", outgoing))
            .addDisconnectedListener((_, reason) -> System.out.printf("Disconnected: %s%n", reason))
            .connect()
            .waitForDisconnection();
}
