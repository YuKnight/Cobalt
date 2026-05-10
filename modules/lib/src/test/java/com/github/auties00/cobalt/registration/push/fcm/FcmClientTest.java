package com.github.auties00.cobalt.registration.push.fcm;

import com.github.auties00.cobalt.client.WhatsAppDevice;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.registration.push.AbstractWhatsAppDevicePushClientTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 15, unit = TimeUnit.MINUTES)
class FcmClientTest extends AbstractWhatsAppDevicePushClientTest {
    // c2dm/register3 throttles back-to-back anonymous registrations from the
    // same IP. Sleeping between tests keeps every authenticate() call cold.
    private static final AtomicBoolean FIRST_TEST = new AtomicBoolean(true);

    FcmClientTest() {
        super(WhatsAppDevice.android(false));
    }

    @BeforeEach
    void throttleAgainstC2dmAntiAbuse() throws InterruptedException {
        if (FIRST_TEST.compareAndSet(true, false)) {
            return;
        }
        TimeUnit.SECONDS.sleep(45);
    }

    @Override
    protected FcmClient newPushClient() {
        return FcmClient.newSession();
    }

    @Test
    void supportedPlatformsListsBothAndroidVariants() {
        try (var client = FcmClient.newSession()) {
            assertEquals(
                    Set.of(ClientPlatformType.ANDROID, ClientPlatformType.ANDROID_BUSINESS),
                    client.supportedPlatforms());
        }
    }

    @Test
    void closeIsIdempotentBeforeAuthenticate() {
        var client = FcmClient.newSession();
        client.close();
        assertDoesNotThrow(client::close);
    }

    @Test
    void accessorsRejectUnauthenticatedClient() {
        try (var client = FcmClient.newSession()) {
            assertFalse(client.isAuthenticated());
            assertThrows(IllegalStateException.class, client::getSession);
            assertThrows(IllegalStateException.class, client::getPushToken);
            assertThrows(IllegalStateException.class, client::getPushCode);
        }
    }

    @Test
    void rejectsNonAndroidDevice() {
        try (var client = FcmClient.newSession()) {
            assertThrows(IllegalArgumentException.class,
                    () -> client.authenticate(WhatsAppDevice.ios(false)));
            assertThrows(IllegalArgumentException.class,
                    () -> client.authenticate(WhatsAppDevice.web()));
            assertFalse(client.isAuthenticated());
        }
    }

    @Test
    void authenticatesPersonalAndProducesPushToken() {
        try (var client = FcmClient.newSession()) {
            client.authenticate(WhatsAppDevice.android(false));
            assertTrue(client.isAuthenticated());
            var token = client.getPushToken();
            assertNotNull(token);
            assertFalse(token.isBlank(),
                    () -> "expected non-empty FCM registration token, got: " + token);
        }
    }

    @Test
    void authenticatesBusinessAndProducesPushToken() {
        try (var client = FcmClient.newSession()) {
            client.authenticate(WhatsAppDevice.android(true));
            assertTrue(client.isAuthenticated());
            assertFalse(client.getPushToken().isBlank());
        }
    }

    @Test
    void rejectsDoubleAuthenticate() {
        try (var client = FcmClient.newSession()) {
            client.authenticate(WhatsAppDevice.android(false));
            assertThrows(IllegalStateException.class,
                    () -> client.authenticate(WhatsAppDevice.android(false)));
        }
    }

    @Test
    void sessionRoundTripsThroughLoadSession() throws Exception {
        FcmSession saved;
        String firstToken;
        try (var client = FcmClient.newSession()) {
            client.authenticate(WhatsAppDevice.android(false));
            firstToken = client.getPushToken();
            saved = client.getSession();
            assertNotEquals(0L, saved.androidId());
            assertNotEquals(0L, saved.securityToken());
            assertFalse(saved.fcmToken().isBlank());
        }

        try (var loaded = FcmClient.loadSession(saved)) {
            assertTrue(loaded.isAuthenticated());
            // The FCM registration token is server-issued and persisted in
            // the session, so a reload must surface the exact same value.
            assertEquals(firstToken, loaded.getPushToken());
        }
    }
}
