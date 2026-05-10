package com.github.auties00.cobalt.registration.push.apns;

import com.github.auties00.cobalt.client.WhatsAppDevice;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.registration.push.AbstractWhatsAppDevicePushClientTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 10, unit = TimeUnit.MINUTES)
class ApnsClientTest extends AbstractWhatsAppDevicePushClientTest {

    ApnsClientTest() {
        super(WhatsAppDevice.ios(false));
    }

    @Override
    protected ApnsClient newPushClient() {
        return ApnsClient.newSession();
    }

    @Test
    void supportedPlatformsListsBothIosVariants() {
        try (var client = ApnsClient.newSession()) {
            assertEquals(
                    Set.of(ClientPlatformType.IOS, ClientPlatformType.IOS_BUSINESS),
                    client.supportedPlatforms());
        }
    }

    @Test
    void closeIsIdempotentBeforeAuthenticate() {
        var client = ApnsClient.newSession();
        client.close();
        assertDoesNotThrow(client::close);
    }

    @Test
    void accessorsRejectUnauthenticatedClient() {
        try (var client = ApnsClient.newSession()) {
            assertFalse(client.isAuthenticated());
            assertThrows(IllegalStateException.class, client::getSession);
            assertThrows(IllegalStateException.class, client::getPushToken);
            assertThrows(IllegalStateException.class, client::getPushCode);
        }
    }

    @Test
    void rejectsNonIosDevice() {
        try (var client = ApnsClient.newSession()) {
            assertThrows(IllegalArgumentException.class,
                    () -> client.authenticate(WhatsAppDevice.android(false)));
            assertThrows(IllegalArgumentException.class,
                    () -> client.authenticate(WhatsAppDevice.web()));
            assertFalse(client.isAuthenticated());
        }
    }

    @Test
    void authenticatesPersonalAndProducesPushToken() {
        try (var client = ApnsClient.newSession()) {
            client.authenticate(WhatsAppDevice.ios(false));
            assertTrue(client.isAuthenticated());
            var token = client.getPushToken();
            assertNotNull(token);
            // APNS device tokens are 32 bytes, hex-encoded
            assertEquals(64, token.length(), () -> "expected 64-char hex token, got: " + token);
        }
    }

    @Test
    void authenticatesBusinessAndProducesPushToken() {
        try (var client = ApnsClient.newSession()) {
            client.authenticate(WhatsAppDevice.ios(true));
            assertTrue(client.isAuthenticated());
            assertEquals(64, client.getPushToken().length());
        }
    }

    @Test
    void rejectsDoubleAuthenticate() {
        try (var client = ApnsClient.newSession()) {
            client.authenticate(WhatsAppDevice.ios(false));
            assertThrows(IllegalStateException.class,
                    () -> client.authenticate(WhatsAppDevice.ios(false)));
        }
    }

    @Test
    void sessionRoundTripsThroughLoadSession() throws Exception {
        ApnsSession saved;
        try (var client = ApnsClient.newSession()) {
            client.authenticate(WhatsAppDevice.ios(false));
            assertEquals(64, client.getPushToken().length());
            saved = client.getSession();
            assertTrue(saved.privateKeyDer().length > 0);
            assertTrue(saved.publicKeyDer().length > 0);
            assertTrue(saved.deviceCertificate().length > 0);
        }

        // Apple rotates the APNS device token across courier sessions, so the
        // reload must produce a 64-char hex token but not necessarily the same
        // one. Only the activation cert is durable; the token is per-courier.
        try (var loaded = ApnsClient.loadSession(saved)) {
            assertTrue(loaded.isAuthenticated());
            assertEquals(64, loaded.getPushToken().length());
        }
    }
}
