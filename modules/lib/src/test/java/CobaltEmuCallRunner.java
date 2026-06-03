import com.github.auties00.cobalt.call.CallState;
import com.github.auties00.cobalt.call.source.AudioFileSource;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientDevice;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;

/**
 * Live end-to-end call runner: connects a Cobalt store paired as a linked device of the Android
 * emulator account 19153544650, dials the desktop account 393495089819, and pumps a 440 Hz tone
 * MP3 into the call's local audio sink so the relay-allocated, hop-by-hop-keyed SRTP media can be
 * verified arriving at the real WhatsApp desktop peer.
 */
void main() throws Exception {
    var ownNumber = 19153544650L;
    var peer = Jid.of("393495089819@s.whatsapp.net");
    var mp3 = Path.of("C:\\Users\\Alessandro Autiero\\Downloads\\Brazy girls.mp3");
    LinkedWhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.persistent(Path.of(".temp/cobalt-emu-store")))
            .loadLatestOrCreateConnection()
            .device(WhatsAppClientDevice.desktop())
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
                System.out.println("PLACING GROUP CALL to " + chosenGroup + " participants=" + participants);
                try {
                    var call = api.startGroupCall(chosenGroup, participants, false);
                    System.out.println("GROUP CALL STARTED id=" + call.callId());
                    try (var source = new AudioFileSource(mp3)) {
                        var pump = Thread.startVirtualThread(() -> {
                            try {
                                var sink = call.localAudioSink();
                                for (var frame = source.next();
                                     frame != null && call.state() != CallState.ENDED;
                                     frame = source.next()) {
                                    sink.write(frame);
                                }
                                System.out.println("MP3 EOF or call ended; pump exiting");
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } catch (Throwable t) {
                                System.out.println("PUMP FAILED: " + t);
                                t.printStackTrace();
                            }
                        });
                        call.awaitEnded();
                        pump.join();
                        System.out.println("CALL ENDED");
                    } catch (Exception e) {
                        System.out.println("CALL ERROR: " + e);
                        e.printStackTrace();
                    } finally {
                        try { call.close(); } catch (Throwable ignored) {}
                    }
                    call.awaitEnded();
                    System.out.println("GROUP CALL ENDED");
                } catch (Throwable t) {
                    System.out.println("GROUP CALL FAILED: " + t);
                    t.printStackTrace();
                }
            })
            .addNodeReceivedListener((_, n) -> { var s = n.toString(); if (s.contains("mute") || s.contains("video") || s.contains("flow") || s.contains("interaction") || s.contains("relayelection") || s.contains("transport")) System.out.println("RXACT " + s); else if (s.contains("call") || s.contains("offer")) System.out.println("RX " + (s.length() > 400 ? s.substring(0, 400) : s)); })
            .addNodeSentListener((_, n) -> { var s = n.toString(); if (s.contains("call") || s.contains("relay") || s.contains("offer") || s.contains("accept")) System.out.println("TX " + (s.length() > 300 ? s.substring(0, 300) : s)); })
            .addDisconnectedListener((_, reason) -> System.out.println("DISCONNECTED: " + reason))
            .connect()
            .waitForDisconnection();
}
