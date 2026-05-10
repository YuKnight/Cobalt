package com.github.auties00.cobalt.registration.push;

import com.github.auties00.cobalt.client.WhatsAppClientType;
import com.github.auties00.cobalt.client.WhatsAppClientVerificationHandler;
import com.github.auties00.cobalt.client.WhatsAppDevice;
import com.github.auties00.cobalt.client.WhatsAppDevicePushClient;
import com.github.auties00.cobalt.exception.WhatsAppRegistrationException;
import com.github.auties00.cobalt.infra.Faker;
import com.github.auties00.cobalt.registration.MobileClientRegistration;
import com.github.auties00.cobalt.store.WhatsAppStoreFactory;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractWhatsAppDevicePushClientTest {
    private final WhatsAppDevice device;

    protected AbstractWhatsAppDevicePushClientTest(WhatsAppDevice device) {
        this.device = device;
    }

    protected abstract WhatsAppDevicePushClient newPushClient();

    @Test
    void deliversPushCodeViaFullRegistration() throws Throwable {
        var maxAttempts = 5;
        var perAttemptTimeout = Duration.ofMinutes(2);
        Throwable lastFailure = null;
        for (var attempt = 1; attempt <= maxAttempts; attempt++) {
            var phoneNumber = Faker.randomItalianMobile();
            try (var pushClient = newPushClient()) {
                pushClient.authenticate(device);

                var store = WhatsAppStoreFactory.temporary()
                        .create(WhatsAppClientType.MOBILE, phoneNumber);
                store.setDevice(device);

                var verification = WhatsAppClientVerificationHandler.Mobile
                        .whatsapp(pushClient::getPushCode);

                var registrationFailure = new AtomicReference<Throwable>();
                var registrationDone = new CountDownLatch(1);
                var thread = Thread.ofVirtual().start(() -> {
                    try (var registration = MobileClientRegistration.newRegistration(
                            store, verification, null, pushClient)) {
                        registration.register();
                    } catch (Throwable t) {
                        registrationFailure.set(t);
                    } finally {
                        registrationDone.countDown();
                    }
                });

                if (!registrationDone.await(perAttemptTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                    // WA never silent-pushed: getPushCode is parked on the holder's
                    // monitor inside the registration thread. Closing the push
                    // client wakes it and lets the registration thread unwind.
                    pushClient.close();
                    if (!registrationDone.await(15, TimeUnit.SECONDS)) {
                        thread.interrupt();
                        registrationDone.await();
                    }
                    lastFailure = new AssertionError(
                            "attempt " + attempt + " timed out waiting for push to " + phoneNumber);
                    System.out.printf("attempt %d/%d for %d timed out waiting for push%n",
                            attempt, maxAttempts, phoneNumber);
                    continue;
                }

                var failure = registrationFailure.get();
                if (failure == null) {
                    assertTrue(store.registered(),
                            "store must report registered after a successful flow");
                    return;
                }
                if (failure instanceof WhatsAppRegistrationException reg) {
                    lastFailure = reg;
                    System.out.printf("attempt %d/%d for %d rejected by Whatsapp: %s%n",
                            attempt, maxAttempts, phoneNumber, reg.getMessage());
                    continue;
                }
                throw failure;
            }
        }
        fail("Registration via push did not succeed within " + maxAttempts + " attempts: "
                        + lastFailure.getMessage(),
                lastFailure);
    }
}
