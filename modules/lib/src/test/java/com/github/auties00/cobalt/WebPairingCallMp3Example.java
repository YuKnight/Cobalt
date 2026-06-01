import com.github.auties00.cobalt.call.CallOptions;
import com.github.auties00.cobalt.call.source.AudioFileSource;
import com.github.auties00.cobalt.client.LinkedWhatsAppClient;
import com.github.auties00.cobalt.client.WhatsAppClientDevice;
import com.github.auties00.cobalt.client.WhatsAppClientVerificationHandler;
import com.github.auties00.cobalt.client.WhatsAppWebClientHistory;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;

import java.nio.file.Path;

void main() throws IOException {
    var ownNumber = 393495089819L;
    var mp3 = Path.of("C:/Users/Alessandro Autiero/Downloads/Brazy girls.mp3");
    LinkedWhatsAppClient.builder()
            .webClient(WhatsAppStoreFactory.persistent())
            .loadLatestOrCreateConnection()
            .device(WhatsAppClientDevice.web())
            .historySetting(WhatsAppWebClientHistory.standard(false))
            .unregistered(WhatsAppClientVerificationHandler.Web.QrCode.toTerminal())
            .addLoggedInListener(api -> {
                System.out.println("Linked. Placing call.");
                var peer = Jid.of("19153544650@s.whatsapp.net");
                var call = api.startCall(peer, CallOptions.audio());
                try (var source = new AudioFileSource(mp3)) {
                    System.out.println("Call started; pumping MP3 -> localAudioSink");
                    var pump = Thread.startVirtualThread(() -> {
                        try {
                            var sink = call.localAudioSink();
                            for (var frame = source.next();
                                 frame != null && call.state() != com.github.auties00.cobalt.call.CallState.ENDED;
                                 frame = source.next()) {
                                sink.write(frame);
                            }
                            System.out.println("MP3 stream EOF or call ended; pump exiting");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (Throwable t) {
                            System.out.println("Pump thread failed: " + t);
                            t.printStackTrace();
                        }
                    });
                    call.awaitEnded();
                    pump.join();
                    System.out.println("Call ended");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                } finally {
                    try {
                        call.close();
                    } catch (Throwable ignored) {
                    }
                }
            })
            .addNodeReceivedListener((_, incoming) -> System.out.printf("Received node %s%n", incoming))
            .addNodeSentListener((_, outgoing) -> System.out.printf("Sent node %s%n", outgoing))
            .addDisconnectedListener((_, reason) -> System.out.printf("Disconnected: %s%n", reason))
            .connect()
            .waitForDisconnection();
}
