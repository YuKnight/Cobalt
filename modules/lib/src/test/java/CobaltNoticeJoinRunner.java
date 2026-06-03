import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientDevice;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.model.contact.ContactStatus;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests the JOIN-from-notice hypothesis: when this companion receives a group-call {@code offer_notice}
 * (for its own account's call placed on another device), it replies with a {@code <preaccept>} addressed
 * to {@code <callId>@call} (the captured participation stanza), then logs whatever the server sends back
 * to see if it delivers the full offer + relay so the device can join.
 */
void main() throws Exception {
    var ownNumber = 19153544650L;
    var capability = new byte[]{0x01, 0x05, (byte) 0xF7, 0x09, (byte) 0xE4, (byte) 0xBB, 0x13};
    var sent = new AtomicBoolean(false);
    LinkedWhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.persistent(Path.of(".temp/cobalt-emu-store")))
            .loadLatestOrCreateConnection()
            .device(WhatsAppClientDevice.web())
            .historySetting(WhatsAppWebClientHistory.standard(false))
            .unregistered(ownNumber, code -> System.out.println("PAIRING CODE: " + code))
            .addLoggedInListener(api -> {
                System.out.println("LINKED OK as " + ownNumber);
                try { api.editPresence(ContactStatus.AVAILABLE); } catch (Throwable ignored) {}
            })
            .addNodeReceivedListener((api, n) -> {
                if (!n.description().equals("call")) {
                    return;
                }
                var s = n.toString();
                System.out.println("RX " + (s.length() > 1200 ? s.substring(0, 1200) : s));
                var payload = n.getChild().orElse(null);
                if (payload == null || !payload.description().equals("offer_notice")) {
                    return;
                }
                if (!sent.compareAndSet(false, true)) {
                    return;
                }
                var callId = payload.getAttributeAsString("call-id", null);
                var creator = payload.getAttributeAsJid("call-creator", null);
                if (callId == null || creator == null) {
                    return;
                }
                var target = Jid.of(callId + "@call");
                var preaccept = new NodeBuilder()
                        .description("call")
                        .attribute("to", target)
                        .content(new NodeBuilder()
                                .description("preaccept")
                                .attribute("call-id", callId)
                                .attribute("call-creator", creator)
                                .content(
                                        new NodeBuilder().description("audio").attribute("enc", "opus").attribute("rate", "16000").build(),
                                        new NodeBuilder().description("encopt").attribute("keygen", "2").build(),
                                        new NodeBuilder().description("capability").attribute("ver", "1").content(capability).build())
                                .build())
                        .build();
                System.out.println("JOIN-TRY sending preaccept to " + target + " : " + preaccept);
                try {
                    api.sendNodeWithNoResponse(preaccept);
                    System.out.println("JOIN-TRY preaccept sent");
                } catch (Throwable t) {
                    System.out.println("JOIN-TRY failed: " + t);
                }
            })
            .addDisconnectedListener((_, reason) -> System.out.println("DISCONNECTED: " + reason))
            .connect()
            .waitForDisconnection();
}
