import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientDevice;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;

import java.nio.file.Path;

/**
 * Connects as a companion of the emulator account and logs every inbound/outbound {@code <call>}
 * stanza in full, so that a group call placed from the emulator's native WhatsApp can be observed
 * from the caller's own linked-device side (revealing the caller-sent offer structure).
 */
void main() throws Exception {
    var ownNumber = 19153544650L;
    LinkedWhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.persistent(Path.of(".temp/cobalt-emu-store")))
            .loadLatestOrCreateConnection()
            .device(WhatsAppClientDevice.web())
            .releaseChannel(com.github.auties00.cobalt.model.device.pairing.ClientPayload.ClientReleaseChannel.BETA)
            .historySetting(WhatsAppWebClientHistory.standard(false))
            .unregistered(ownNumber, code -> System.out.println("PAIRING CODE: " + code))
            .addLoggedInListener(api -> {
                System.out.println("LINKED OK as " + ownNumber + " — observing call stanzas");
                try {
                    api.enableWebBetaEnrollment();
                    System.out.println("WEB BETA ENROLLMENT pushed");
                } catch (Throwable t) {
                    System.out.println("WEB BETA ENROLLMENT failed: " + t);
                }
            })
            .addNodeReceivedListener((_, n) -> {
                if (n.description().equals("call")) {
                    System.out.println("RX-CALL " + n);
                }
            })
            .addNodeSentListener((_, n) -> {
                if (n.description().equals("call")) {
                    System.out.println("TX-CALL " + n);
                }
            })
            .connect()
            .waitForDisconnection();
}
