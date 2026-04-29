package com.github.auties00.cobalt.registration.push;

import com.github.auties00.cobalt.client.WhatsAppDevice;
import com.github.auties00.cobalt.client.WhatsAppDevicePushClient;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * No-op {@link WhatsAppDevicePushClient} returning empty token and code values.
 * Used by the registration code as the low-trust default when no push
 * client is configured: the {@code push_token} and {@code push_code}
 * form fields are still emitted but with empty values, which the server
 * tolerates as a low-trust signal.
 *
 * <p>Stateless and inherently thread-safe. Constructed via
 * {@link WhatsAppDevicePushClient#noop()}.
 */
public final class NoopMobilePushClient implements WhatsAppDevicePushClient {
    /**
     * Cached, unmodifiable view of every {@link ClientPlatformType}
     * entry. Returned by {@link #supportedPlatforms()} so the no-op
     * client can be paired with any device without further checks.
     */
    private static final Set<ClientPlatformType> ALL_PLATFORMS =
            Collections.unmodifiableSet(EnumSet.allOf(ClientPlatformType.class));

    /**
     * Singleton instance
     */
    public static final NoopMobilePushClient INSTANCE = new NoopMobilePushClient();

    /**
     * Package-private constructor invoked by
     * {@link WhatsAppDevicePushClient#noop()}.
     */
    private NoopMobilePushClient() {

    }

    /**
     * Returns every entry of {@link ClientPlatformType}, since the
     * no-op client accepts any device unconditionally.
     *
     * @return an unmodifiable set containing every
     *         {@link ClientPlatformType} value
     */
    @Override
    public Set<ClientPlatformType> supportedPlatforms() {
        return ALL_PLATFORMS;
    }

    /**
     * No-op: there is nothing to authenticate against.
     *
     * @param device ignored
     */
    @Override
    public void authenticate(WhatsAppDevice device) {
    }

    /**
     * Reports that this client is always "authenticated" so callers do
     * not need to special-case it: the empty token / empty code values
     * it produces are valid even without any real authentication.
     *
     * @return {@code true} unconditionally
     */
    @Override
    public boolean isAuthenticated() {
        return true;
    }

    /**
     * Returns the empty string so the {@code push_token} form field is
     * still emitted but with an empty value.
     *
     * @return the empty string
     */
    @Override
    public String getPushToken() {
        return "";
    }

    /**
     * Returns the empty string so the {@code push_code} form field is
     * still emitted but with an empty value.
     *
     * @return the empty string
     */
    @Override
    public String getPushCode() {
        return "";
    }
}
