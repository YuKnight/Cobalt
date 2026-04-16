package com.github.auties00.cobalt.client.registration;

import com.github.auties00.cobalt.client.WhatsAppClientVerificationHandler;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Locale;
import java.util.UUID;

/**
 * Mobile registration driver that impersonates the native Android WhatsApp
 * application.
 *
 * <p>Adds Android-specific behaviour on top of
 * {@link WhatsAppMobileClientRegistration}:
 * <ul>
 *   <li>{@link #createRequest(String, String)} attaches the Android
 *       User-Agent derived from the store's device description plus the
 *       {@code WaMsysRequest} and {@code request_token} headers the
 *       Android app sends.</li>
 *   <li>{@link #getRequestVerificationCodeParameters(String)} populates
 *       the long list of Android-only form fields that the
 *       {@code /code} endpoint expects (SIM MCC/MNC, advertising ID,
 *       backup token, cellular signal strength, client metrics, and so
 *       on).</li>
 *   <li>{@link #generateFdid()} formats the device family UUID in lower
 *       case, matching the Android behaviour.</li>
 * </ul>
 *
 * @apiNote Android-specific driver for the native mobile registration
 *          protocol. Not present in WA Web.
 * @see WhatsAppMobileClientRegistration
 */
public final class WhatsAppAndroidClientRegistration extends WhatsAppMobileClientRegistration {
    /**
     * Constructs a new Android registration bound to the given store and
     * verification handler.
     *
     * @param store the store carrying identity keys and phone number
     * @param verification the verification handler supplying the method
     *                     and the user-entered code
     */
    public WhatsAppAndroidClientRegistration(WhatsAppStore store, WhatsAppClientVerificationHandler.Mobile verification) {
        super(store, verification);
    }

    /**
     * Builds an HTTP POST request to the registration endpoint with the
     * encrypted body wrapped in the {@code ENC=} form field and the
     * Android-specific headers.
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
                // Derives the Android User-Agent from the store's device spec
                .header("User-Agent", store.device().toUserAgent(store.clientVersion()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "text/json")
                // Signals to the server that this is a Msys-based client build
                .header("WaMsysRequest", "1")
                // Assigns a fresh UUID per request to help the server correlate retries
                .header("request_token", UUID.randomUUID().toString())
                .build();
    }

    /**
     * Returns the large set of Android-specific form fields that the
     * {@code /code} endpoint expects in addition to the shared
     * registration parameters.
     *
     * <p>The values simulate a plausible Android handset: empty SIM MCC/MNC,
     * network radio type {@code 1} (GSM), no roaming, 3.57 GB RAM, strong
     * cellular signal, and client metrics pretending the app has attempted
     * registration 20 times. The advertising ID and backup token are read
     * from the store so that they remain stable across the registration
     * flow.
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
                "mcc", "000",
                "mnc", "000",
                "feo2_query_status", "error_security_exception",
                "db", "1",
                "sim_type", "0",
                "recaptcha", "%7B%22stage%22%3A%22ABPROP_DISABLED%22%7D",
                "network_radio_type", "1",
                "prefer_sms_over_flash", "false",
                "simnum", "0",
                "airplane_mode_type", "0",
                "client_metrics", "%7B%22attempts%22%3A20%2C%22app_campaign_download_source%22%3A%22google-play%7Cunknown%22%7D",
                "mistyped", "7",
                "advertising_id", store.advertisingId().toString(),
                "hasinrc", "1",
                "roaming_type", "0",
                "device_ram", "3.57",
                "education_screen_displayed", "false",
                "pid", String.valueOf(ProcessHandle.current().pid()),
                "gpia", "",
                "cellular_strength", "5",
                "_gg", "",
                "_gi", "",
                "_gp", "",
                "backup_token", toUrlHex(store.backupToken()),
                "hasav", "2"
        };
    }

    /**
     * Returns the device family identifier formatted as a lower-case UUID,
     * matching the Android client's {@code fdid} scheme.
     *
     * @return the lower-case UUID string
     */
    @Override
    protected String generateFdid() {
        return store.fdid().toString().toLowerCase(Locale.ROOT);
    }
}
