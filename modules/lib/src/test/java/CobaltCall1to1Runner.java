import com.github.auties00.cobalt.call.CallOptions;
import com.github.auties00.cobalt.call.CallState;
import com.github.auties00.cobalt.call.source.AudioFileSource;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientDevice;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.model.contact.ContactStatus;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;

import java.nio.file.Path;

/**
 * Live 1:1 call runner: Cobalt (paired as a 19153544650 web companion) dials 393495089819 and pumps
 * the Brazy Girls MP3 into the call so the WA Web peer can verify audible hop-by-hop SRTP media.
 */
void main() throws Exception {
    var ownNumber = 19153544650L;
    var peer = Jid.of("393495089819@s.whatsapp.net");
    var mp3 = Path.of("C:\\Users\\Alessandro Autiero\\Downloads\\Brazy girls.mp3");
    LinkedWhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.persistent(Path.of(".temp/cobalt-emu-store")))
            .loadLatestOrCreateConnection()
            .device(WhatsAppClientDevice.web())
            .historySetting(WhatsAppWebClientHistory.standard(false))
            .unregistered(ownNumber, code -> System.out.println("PAIRING CODE: " + code))
            .addLoggedInListener(api -> {
                System.out.println("LINKED OK as " + ownNumber
                        + " jid=" + api.store().accountStore().jid().orElse(null)
                        + " lid=" + api.store().accountStore().lid().orElse(null));
                try { api.editPresence(ContactStatus.AVAILABLE); } catch (Throwable ignored) {}
                try {
                    Thread.sleep(4000);
                  for (int attempt = 1; attempt <= 20; attempt++) {
                    System.out.println("DIALING 1:1 -> " + peer + " attempt=" + attempt);
                    var call = api.startCall(peer, CallOptions.audio());
                    System.out.println("CALL PLACED id=" + call.callId() + " state=" + call.state());
                    var watcher = Thread.startVirtualThread(() -> {
                        var last = (CallState) null;
                        while (call.state() != CallState.ENDED) {
                            var s = call.state();
                            if (s != last) { System.out.println("STATE -> " + s + " @" + System.currentTimeMillis()); last = s; }
                            try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                        }
                        System.out.println("STATE -> ENDED reason=" + call.endReason().orElse(null));
                    });
                    var pump = Thread.startVirtualThread(() -> {
                        try (var source = new AudioFileSource(mp3)) {
                            var sink = call.localAudioSink();
                            long n = 0;
                            var startWall = System.currentTimeMillis();
                            for (var frame = source.next();
                                 frame != null && call.state() != CallState.ENDED;
                                 frame = source.next()) {
                                sink.write(frame);
                                // Pace to real time: write each frame at start+ptsMs so the peer
                                // receives a continuous ~10ms-cadence stream like a live mic, not a burst.
                                var sleep = (startWall + frame.ptsMs()) - System.currentTimeMillis();
                                if (sleep > 1) Thread.sleep(sleep);
                                if (++n % 200 == 1) System.out.println("PUMP frames=" + n + " state=" + call.state());
                            }
                            System.out.println("MP3 EOF (frames=" + n + ") or call ended");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (Throwable t) {
                            System.out.println("PUMP FAILED: " + t);
                        }
                    });
                    call.awaitEnded();
                    pump.join();
                    watcher.join();
                    System.out.println("CALL ENDED reason=" + call.endReason().orElse(null));
                    if (call.endReason().map(Enum::name).map(r -> r.equals("HANGUP")).orElse(false)) {
                        System.out.println("peer hung up after a connected call; stopping redial");
                        break;
                    }
                    Thread.sleep(5000);
                  }
                } catch (Throwable t) {
                    System.out.println("CALL FAILED: " + t);
                    t.printStackTrace();
                }
            })
            .addNodeReceivedListener((_, n) -> { var s = n.toString(); if (s.contains("call") || s.contains("offer") || s.contains("relay")) System.out.println("RX " + (s.length() > 600 ? s.substring(0, 600) : s)); })
            .addNodeSentListener((_, n) -> { var s = n.toString(); if (s.contains("call") || s.contains("offer") || s.contains("relay") || s.contains("accept")) System.out.println("TX " + (s.length() > 400 ? s.substring(0, 400) : s)); })
            .addDisconnectedListener((_, reason) -> System.out.println("DISCONNECTED: " + reason))
            .connect()
            .waitForDisconnection();
}
