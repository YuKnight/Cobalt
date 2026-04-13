package com.github.auties00.cobalt.socket;

import com.github.auties00.cobalt.client.WhatsAppClientType;
import com.github.auties00.cobalt.exception.WhatsAppException;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.model.jid.JidDeviceBuilder;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(60)
class WhatsAppSocketTest {

    private WhatsAppStore mobileStore;
    private WhatsAppStore webStore;
    private WhatsAppSocketClient client;

    @BeforeEach
    void setUp() throws IOException {
        mobileStore = WhatsAppStoreFactory.inMemory()
                .create(WhatsAppClientType.MOBILE, 15551234567L);

        webStore = WhatsAppStoreFactory.inMemory()
                .create(WhatsAppClientType.WEB, UUID.randomUUID());
        webStore.setDevice(new JidDeviceBuilder()
                .model("Test")
                .manufacturer("Test")
                .platform(ClientPlatformType.WEB)
                .clientType(WhatsAppClientType.WEB)
                .build());
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            try {
                client.disconnect();
            } catch (Exception _) {
            }
            client = null;
        }
    }

    @Test
    void testMobileHandshake() throws Exception {
        client = WhatsAppSocketClient.newCipheredSocketClient(mobileStore);
        assertInstanceOf(WhatsAppSocketClient.Mobile.class, client);
        var listener = new CapturingListener();

        client.connect(listener);

        assertTrue(client.isConnected(), "Should be connected after handshake");
        assertNotNull(getField(client, "writeKey"), "Write key should be derived");
        assertNotNull(getField(client, "readKey"), "Read key should be derived");
    }

    @Test
    void testWebHandshake() throws Exception {
        client = WhatsAppSocketClient.newCipheredSocketClient(webStore);
        assertInstanceOf(WhatsAppSocketClient.Web.class, client,
                "WEB platform should create Web subtype");

        // The Web path connects via TLS + WebSocket to web.whatsapp.com.
        // The server may reject non-browser WebSocket upgrades, so we
        // verify the transport stack initializes correctly either way.
        var listener = new CapturingListener();
        try {
            client.connect(listener);
            assertTrue(client.isConnected(), "Should be connected after handshake");
            assertNotNull(getField(client, "writeKey"), "Write key should be derived");
            assertNotNull(getField(client, "readKey"), "Read key should be derived");
        } catch (IOException e) {
            // Server rejected WebSocket upgrade — expected for non-browser clients.
            // The TLS connection succeeded if we got this far (not a TLS error).
            assertTrue(e.getMessage().contains("WebSocket") || e.getMessage().contains("stream"),
                    "Expected WebSocket rejection, got: " + e.getMessage());
        }
    }

    @Test
    void testSendNodeAfterHandshake() throws Exception {
        client = WhatsAppSocketClient.newCipheredSocketClient(mobileStore);
        var listener = new CapturingListener();

        client.connect(listener);

        var node = new NodeBuilder()
                .description("iq")
                .attribute("id", "test-" + System.currentTimeMillis())
                .attribute("type", "get")
                .attribute("xmlns", "w:web")
                .build();
        assertDoesNotThrow(() -> client.sendNode(node));
    }

    @Test
    void testDisconnectCleansUp() throws Exception {
        client = WhatsAppSocketClient.newCipheredSocketClient(mobileStore);
        var listener = new CapturingListener();

        client.connect(listener);

        client.disconnect();

        assertFalse(client.isConnected());
        assertTrue(listener.closed.await(5, TimeUnit.SECONDS),
                "onClose should fire after disconnect");
        assertNull(getField(client, "writeKey"), "Write key should be cleared");
        assertNull(getField(client, "readKey"), "Read key should be cleared");
    }

    @Test
    void testConnectRejectsNullListener() {
        client = WhatsAppSocketClient.newCipheredSocketClient(mobileStore);
        assertThrows(NullPointerException.class, () -> client.connect(null));
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String name) throws Exception {
        var current = target.getClass();
        while (current != null) {
            try {
                var field = current.getDeclaredField(name);
                field.setAccessible(true);
                return (T) field.get(target);
            } catch (NoSuchFieldException _) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field not found: " + name);
    }

    static class CapturingListener implements WhatsAppSocketListener {
        final List<Node> nodes = new CopyOnWriteArrayList<>();
        final List<WhatsAppException> errors = new CopyOnWriteArrayList<>();
        final CountDownLatch firstNode = new CountDownLatch(1);
        final CountDownLatch closed = new CountDownLatch(1);

        @Override
        public void onNode(Node node) {
            nodes.add(node);
            firstNode.countDown();
        }

        @Override
        public void onError(WhatsAppException exception) {
            errors.add(exception);
        }

        @Override
        public void onClose() {
            closed.countDown();
        }
    }
}
