import com.github.auties00.cobalt.call.CallOptions;
import com.github.auties00.cobalt.call.CallState;
import com.github.auties00.cobalt.call.frame.video.VideoFrame;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientDevice;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;

import java.nio.file.Path;

/**
 * Live end-to-end VIDEO call runner: places an audio-and-video call to the desktop account and pumps a
 * synthetic 720x480 I420 test pattern (a moving colour gradient) into the call's local video sink so
 * the relay-allocated, hop-by-hop-keyed VP8 video can be verified rendering on the real desktop peer.
 */
void main() throws Exception {
    var ownNumber = 19153544650L;
    var peer = Jid.of("393495089819@s.whatsapp.net");
    var w = 720;
    var h = 480;
    LinkedWhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.persistent(Path.of(".temp/cobalt-emu-store")))
            .loadLatestOrCreateConnection()
            .device(WhatsAppClientDevice.web())
            .historySetting(WhatsAppWebClientHistory.standard(false))
            .unregistered(ownNumber, code -> System.out.println("PAIRING CODE: " + code))
            .addLoggedInListener(api -> {
                System.out.println("LINKED OK as " + ownNumber);
                com.github.auties00.cobalt.call.ActiveCall call;
                try {
                    // Place an AUDIO call (the working path), connect, then upgrade to video so the
                    // audio-to-video upgrade is exercised over the established media transport.
                    call = api.startCall(peer, CallOptions.audio());
                    System.out.println("CALL STARTED id=" + call.callId());
                } catch (Throwable t) {
                    System.out.println("startCall FAILED: " + t);
                    t.printStackTrace();
                    return;
                }
                // Upgrade to video shortly after the call connects, then pump video frames.
                Thread.startVirtualThread(() -> {
                    try {
                        for (var i = 0; i < 40 && call.state() != CallState.ACTIVE && call.state() != CallState.ENDED; i++) {
                            Thread.sleep(250);
                        }
                        Thread.sleep(1000);
                        System.out.println("REQUESTING VIDEO UPGRADE (state=" + call.state() + ")");
                        call.requestVideoUpgrade();
                        Thread.sleep(5000);
                        System.out.println("STARTING SCREEN SHARE");
                        call.startScreenShare();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                var pump = Thread.startVirtualThread(() -> {
                    try {
                        var sink = call.localVideoSink();
                        var frameSize = w * h * 3 / 2;
                        long pts = 0;
                        var seq = 0;
                        while (call.state() != CallState.ENDED) {
                            var buf = new byte[frameSize];
                            // Y plane: moving diagonal gradient; U/V: rotating chroma so the frame
                            // visibly changes (a static frame can be dropped as a duplicate).
                            for (var i = 0; i < w * h; i++) {
                                buf[i] = (byte) ((i + seq * 8) & 0xFF);
                            }
                            java.util.Arrays.fill(buf, w * h, w * h + (w * h / 4), (byte) (128 + (seq % 64)));
                            java.util.Arrays.fill(buf, w * h + (w * h / 4), frameSize, (byte) (128 - (seq % 64)));
                            sink.write(new VideoFrame(buf, w, h, pts));
                            pts += 66;
                            seq++;
                            Thread.sleep(66); // ~15 fps
                        }
                        System.out.println("video pump exiting; call ended");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable t) {
                        System.out.println("VIDEO PUMP FAILED: " + t);
                        t.printStackTrace();
                    }
                });
                try {
                    call.awaitEnded();
                    pump.join();
                    System.out.println("CALL ENDED");
                } catch (Exception e) {
                    System.out.println("CALL ERROR: " + e);
                } finally {
                    try { call.close(); } catch (Throwable ignored) {}
                }
            })
            .addNodeReceivedListener((_, n) -> { var s = n.toString(); if (s.contains("video") || s.contains("relay") || s.contains("accept")) System.out.println("RX " + (s.length() > 300 ? s.substring(0, 300) : s)); })
            .addDisconnectedListener((_, reason) -> System.out.println("DISCONNECTED: " + reason))
            .connect()
            .waitForDisconnection();
}
