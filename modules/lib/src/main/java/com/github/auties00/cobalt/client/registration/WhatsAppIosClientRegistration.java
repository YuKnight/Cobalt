package com.github.auties00.cobalt.client.registration;

import com.github.auties00.cobalt.client.WhatsAppClientVerificationHandler;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Locale;

/**
 * Mobile registration driver that impersonates the native iOS WhatsApp
 * application.
 *
 * <p>Adds iOS-specific behaviour on top of
 * {@link WhatsAppMobileClientRegistration}:
 * <ul>
 *   <li>{@link #createRequest(String, String)} uses the iOS User-Agent
 *       derived from the store's device description and sends only the
 *       minimal HTTP headers the iOS app sends (no {@code WaMsysRequest},
 *       no explicit {@code Accept}, no per-request token header).</li>
 *   <li>{@link #getRequestVerificationCodeParameters(String)} populates
 *       only the short set of form fields the iOS app sends: method, SIM
 *       MCC/MNC, reason, and cellular signal strength.</li>
 *   <li>{@link #generateFdid()} formats the device family UUID in upper
 *       case, matching the iOS behaviour.</li>
 * </ul>
 *
 * @apiNote iOS-specific driver for the native mobile registration
 *          protocol. Not present in WA Web.
 * @see WhatsAppMobileClientRegistration
 */
public final class WhatsAppIosClientRegistration extends WhatsAppMobileClientRegistration {
    /**
     * Constructs a new iOS registration bound to the given store and
     * verification handler.
     *
     * @param store the store carrying identity keys and phone number
     * @param verification the verification handler supplying the method
     *                     and the user-entered code
     */
    public WhatsAppIosClientRegistration(WhatsAppStore store, WhatsAppClientVerificationHandler.Mobile verification) {
        super(store, verification);
    }

    /**
     * Builds an HTTP POST request to the registration endpoint with the
     * encrypted body wrapped in the {@code ENC=} form field and the
     * minimal iOS headers.
     *
     * @param path the API sub-path ({@code /exist}, {@code /code},
     *             {@code /register})
     * @param body the Base64 URL-encoded encrypted form body
     * @return a ready-to-send HTTP request
     */
    @Override
    protected HttpRequest createRequest(String path, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create("%s%s".formatted(MOBILE_REGISTRATION_ENDPOINT, path)))
                .POST(HttpRequest.BodyPublishers.ofString("ENC=" + body))
                // Derives the iOS User-Agent from the store's device spec
                .header("User-Agent", store.device().toUserAgent(store.clientVersion()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
    }

    /**
     * Returns the short set of iOS-specific form fields that the
     * {@code /code} endpoint expects in addition to the shared
     * registration parameters.
     *
     * <p>Unlike the Android variant the iOS client sends only the
     * verification method, empty SIM MCC/MNC, an empty reason, and a
     * cellular signal strength of {@code 1}.
     *
     * @param method the verification method chosen by the user
     * @return the alternating name/value form parameters
     */
    @Override
    protected String[] getRequestVerificationCodeParameters(String method) {
        return new String[]{
                "method", method,
                "sim_mcc", "000",
                "sim_mnc", "000",
                "reason", "",
                "cellular_strength", "1"
        };
    }

    /**
     * Returns the device family identifier formatted as an upper-case UUID,
     * matching the iOS client's {@code fdid} scheme.
     *
     * @return the upper-case UUID string
     */
    @Override
    protected String generateFdid() {
        return store.fdid().toString().toUpperCase(Locale.ROOT);
    }
}
