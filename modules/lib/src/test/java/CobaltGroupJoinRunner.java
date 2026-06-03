import com.github.auties00.cobalt.call.CallOptions;
import com.github.auties00.cobalt.call.CallState;
import com.github.auties00.cobalt.call.source.AudioFileSource;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientDevice;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.model.contact.ContactStatus;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;

import java.nio.file.Path;

/**
 * Live group-call JOIN runner: connects a Cobalt store paired as a linked device of account
 * 19153544650, then auto-accepts the first incoming call (placed from a different Tested5 member,
 * e.g. the desktop account 393495089819) and pumps a tone MP3 into the local audio sink so the
 * relay-allocated, hop-by-hop-keyed group SRTP media can be verified end-to-end.
 */
void main() throws Exception {
    var ownNumber = 19153544650L;
    var mp3 = Path.of(".temp/tone.mp3").toAbsolutePath();
    // Accept one call at a time: a flood of server-buffered stale offers (from earlier test runs)
    // otherwise spawns many concurrent accepts and closes the session.
    var busy = new java.util.concurrent.atomic.AtomicBoolean(false);
    LinkedWhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.persistent(Path.of(".temp/cobalt-emu-store")))
            .loadLatestOrCreateConnection()
            .device(WhatsAppClientDevice.web())
            .releaseChannel(com.github.auties00.cobalt.model.device.pairing.ClientPayload.ClientReleaseChannel.BETA)
            .historySetting(WhatsAppWebClientHistory.standard(false))
            .unregistered(ownNumber, code -> System.out.println("PAIRING CODE: " + code))
            .addLoggedInListener(api -> {
                System.out.println("LINKED OK as " + ownNumber + " — marking available, waiting for an incoming group call");
                try { api.editPresence(ContactStatus.AVAILABLE); System.out.println("presence=AVAILABLE sent"); }
                catch (Throwable t) { System.out.println("presence send failed: " + t); }
            })
            .addCallListener((api, incoming) -> {
                System.out.println("INCOMING CALL id=" + incoming.callId()
                        + " group=" + incoming.group()
                        + " groupJid=" + incoming.groupJid().orElse(null)
                        + " creator=" + incoming.peer()
                        + " hasTransport=" + incoming.transportSpec().isPresent());
                if (!busy.compareAndSet(false, true)) {
                    System.out.println("BUSY: ignoring concurrent incoming call " + incoming.callId());
                    return;
                }
                try {
                    var call = api.acceptCall(incoming, CallOptions.audio());
                    System.out.println("ACCEPTED id=" + call.callId() + " state=" + call.state());
                    var watcher = Thread.startVirtualThread(() -> {
                        var last = (CallState) null;
                        while (call.state() != CallState.ENDED) {
                            var s = call.state();
                            if (s != last) { System.out.println("STATE -> " + s); last = s; }
                            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
                        }
                    });
                    var inbound = Thread.startVirtualThread(() -> {
                        try {
                            var src = call.remoteAudioSource();
                            long count = 0;
                            for (var f = src.next(); f != null && call.state() != CallState.ENDED; f = src.next()) {
                                count++;
                                if (count % 50 == 1) {
                                    System.out.println("INBOUND-AUDIO frames=" + count);
                                }
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (Throwable t) {
                            System.out.println("INBOUND reader failed: " + t);
                        }
                    });
                    var pump = Thread.startVirtualThread(() -> {
                        try (var source = new AudioFileSource(mp3)) {
                            var sink = call.localAudioSink();
                            for (var frame = source.next();
                                 frame != null && call.state() != CallState.ENDED;
                                 frame = source.next()) {
                                sink.write(frame);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (Throwable t) {
                            System.out.println("PUMP FAILED (call stays up): " + t);
                        }
                    });
                    var actions = Thread.startVirtualThread(() -> {
                        try {
                            call.awaitState(CallState.ACTIVE);
                            Thread.sleep(1200);
                            System.out.println("ACTION raiseHand"); call.raiseHand(); Thread.sleep(1500);
                            System.out.println("ACTION lowerHand"); call.lowerHand(); Thread.sleep(1500);
                            System.out.println("ACTION requestPeerMute(" + incoming.peer() + ")");
                            call.requestPeerMute(incoming.peer().toString()); Thread.sleep(1500);
                            System.out.println("ACTION mute(true)"); call.mute(true, false); Thread.sleep(1500);
                            System.out.println("ACTION mute(false)"); call.mute(false, false); Thread.sleep(1500);
                            System.out.println("ACTION requestVideoUpgrade"); call.requestVideoUpgrade(); Thread.sleep(1500);
                            System.out.println("ACTION sendReaction"); call.sendReaction("👍"); Thread.sleep(1500);
                            System.out.println("ACTIONS DONE");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (Throwable t) {
                            System.out.println("ACTION FAILED: " + t);
                        }
                    });
                    call.awaitEnded();
                    pump.join();
                    watcher.join();
                    actions.interrupt();
                    inbound.interrupt();
                    System.out.println("CALL ENDED");
                } catch (Throwable t) {
                    System.out.println("JOIN FAILED: " + t);
                    t.printStackTrace();
                } finally {
                    busy.set(false);
                }
            })
            .addNodeReceivedListener((_, n) -> { if (n.description().equals("call")) { var s = n.toString(); System.out.println("RX " + (s.length() > 8000 ? s.substring(0, 8000) : s)); } })
            .addNodeSentListener((_, n) -> { var s = n.toString(); if (s.contains("user_action") || s.contains("raise_hand") || s.contains("mute_v2") || s.contains("video_state") || s.contains("request-state")) System.out.println("TX-ACTION " + (s.length() > 500 ? s.substring(0, 500) : s)); })
            .addCallInteractionListener((_, callId, fromJid, interaction) -> System.out.println("RX-INTERACTION call=" + callId + " from=" + fromJid + " " + interaction))
            .addDisconnectedListener((_, reason) -> System.out.println("DISCONNECTED: " + reason))
            .connect()
            .waitForDisconnection();
}
