import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientDevice;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Live group-call runner: connects, lists the synced group chats and their members, then places a
 * group call to the first group that has at least two other members (preferring one containing the
 * desktop test account) so the group-offer signaling and SFU media path can be verified.
 */
void main() throws Exception {
    var ownNumber = 19153544650L;
    var desktopUser = "393495089819";
    LinkedWhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.persistent(Path.of(".temp/cobalt-emu-store")))
            .loadLatestOrCreateConnection()
            .device(WhatsAppClientDevice.web())
            .historySetting(WhatsAppWebClientHistory.standard(false))
            .unregistered(ownNumber, code -> System.out.println("PAIRING CODE: " + code))
            .addLoggedInListener(api -> {
                System.out.println("LINKED OK as " + ownNumber);
                System.out.println("SELF JID = " + api.store().accountStore().jid().orElse(null)
                        + " LID = " + api.store().accountStore().lid().orElse(null));
                try {
                    Thread.sleep(12000); // let chats sync
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                var self = api.store().accountStore().jid().map(Jid::toUserJid).orElse(null);
                var groups = api.store().chatStore().chats().stream()
                        .filter(c -> c.jid().hasGroupOrCommunityServer())
                        .toList();
                System.out.println("GROUPS: " + groups.size());
                Jid chosenGroup = null;
                for (var g : groups) {
                    System.out.println("  GROUP " + g.jid() + " name=" + g.name().orElse("?"));
                    if (g.name().map(n -> n.contains("Tested")).orElse(false)) {
                        chosenGroup = g.jid();
                    }
                }
                if (chosenGroup == null && !groups.isEmpty()) {
                    chosenGroup = groups.get(0).jid();
                }
                if (chosenGroup == null) {
                    System.out.println("NO group found");
                    return;
                }
                // Query full group metadata via the legacy w:g2 socket IQ (no GraphQL needed).
                var participants = new ArrayList<Jid>();
                try {
                    var meta = api.queryChatMetadata(chosenGroup);
                    if (meta instanceof com.github.auties00.cobalt.model.chat.group.GroupMetadata gm) {
                        for (var p : gm.participants()) {
                            if (self == null || !p.userJid().user().equals(self.user())) {
                                participants.add(p.userJid());
                            }
                        }
                    }
                } catch (Throwable t) {
                    System.out.println("queryChatMetadata failed: " + t);
                }
                System.out.println("GROUP " + chosenGroup + " full members=" + participants);
                if (participants.isEmpty()) {
                    participants.add(Jid.of(desktopUser + "@s.whatsapp.net"));
                }
                System.out.println("PLACING GROUP CALL to " + chosenGroup + " participants=" + participants);
                try {
                    var call = api.startGroupCall(chosenGroup, participants, false);
                    System.out.println("GROUP CALL STARTED id=" + call.callId());
                    call.awaitEnded();
                    System.out.println("GROUP CALL ENDED");
                } catch (Throwable t) {
                    System.out.println("GROUP CALL FAILED: " + t);
                    t.printStackTrace();
                }
            })
            .addNodeReceivedListener((_, n) -> { var s = n.toString(); if (s.contains("call") || s.contains("relay") || s.contains("group")) System.out.println("RX " + (s.length() > 4000 ? s.substring(0, 4000) : s)); })
            .addNodeSentListener((_, n) -> { var s = n.toString(); if (s.contains("offer") || s.contains("group")) System.out.println("TX " + (s.length() > 4000 ? s.substring(0, 4000) : s)); })
            .addDisconnectedListener((_, reason) -> System.out.println("DISCONNECTED: " + reason))
            .connect()
            .waitForDisconnection();
}
