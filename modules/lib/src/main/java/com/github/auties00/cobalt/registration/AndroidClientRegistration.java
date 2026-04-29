package com.github.auties00.cobalt.registration;

import com.github.auties00.cobalt.client.WhatsAppClientVerificationHandler;
import com.github.auties00.cobalt.client.WhatsAppDeviceAttestor;
import com.github.auties00.cobalt.client.WhatsAppDevicePushClient;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Mobile registration driver that impersonates the native Android
 * WhatsApp application.
 *
 * <p>Adds Android-specific behaviour on top of
 * {@link MobileClientRegistration}:
 * <ul>
 *   <li>{@link #createRequest(String, String, String)} attaches the Android
 *       User-Agent derived from the store's device description plus the
 *       {@code WaMsysRequest} and {@code request_token} headers the
 *       Android app sends.</li>
 *   <li>{@link #getRequestVerificationCodeParameters(String)} populates
 *       the long list of Android-only form fields that the
 *       {@code /code} endpoint expects (SIM MCC/MNC, advertising ID,
 *       backup token, cellular signal strength, client metrics, and so
 *       on) and now appends the Play Integrity attestation triple
 *       ({@code gpia}, {@code _gg}, {@code _gi}, {@code _gp}) produced
 *       by the configured
 *       {@link WhatsAppDeviceAttestor.Android}.</li>
 *   <li>{@link #generateFdid()} formats the device family UUID in lower
 *       case, matching the Android behaviour.</li>
 * </ul>
 *
 * @apiNote Android-specific driver for the native mobile registration
 *          protocol. Not present in WA Web. Package-private because
 *          callers should always go through
 *          {@link MobileClientRegistration#newRegistration} rather
 *          than constructing this class directly.
 * @see MobileClientRegistration
 */
final class AndroidClientRegistration extends MobileClientRegistration {
    /**
     * The Android device attestor the registration consults before each
     * outgoing request. Never {@code null}: the constructor substitutes
     * {@link WhatsAppDeviceAttestor.Android#NONE} when the caller
     * supplies {@code null}.
     */
    private final WhatsAppDeviceAttestor.Android attestor;

    /**
     * The push client the registration consults for the {@code push_token}
     * and {@code push_code} form fields. Never {@code null}: the
     * constructor substitutes {@link WhatsAppDevicePushClient#noop()} when the
     * caller supplies {@code null}.
     */
    private final WhatsAppDevicePushClient pushClient;

    /**
     * Constructs a new Android registration bound to the given store,
     * verification handler, attestor, and push client.
     *
     * @param store the store carrying identity keys and phone number
     * @param verification the verification handler supplying the method
     *                     and the user-entered code
     * @param attestor the Android device attestor, or {@code null} to
     *                 use the low-trust
     *                 {@link WhatsAppDeviceAttestor.Android#NONE}
     *                 fallback
     * @param pushClient the push client, or {@code null} to use the
     *                   low-trust {@link WhatsAppDevicePushClient#noop()}
     *                   fallback
     */
    AndroidClientRegistration(
            WhatsAppStore store,
            WhatsAppClientVerificationHandler.Mobile verification,
            WhatsAppDeviceAttestor.Android attestor,
            WhatsAppDevicePushClient pushClient) {
        super(store, verification);
        this.attestor = Objects.requireNonNullElse(attestor, WhatsAppDeviceAttestor.Android.NONE);
        this.pushClient = Objects.requireNonNullElse(pushClient, WhatsAppDevicePushClient.noop());
    }

    /**
     * Builds an HTTP POST request to the registration endpoint with the
     * pre-assembled body and the Android-specific headers.
     *
     * <p>The {@code body} argument is already in its final wire form:
     * the base class has prepended the {@code ENC=} envelope marker and,
     * when an Android keystore signature was produced, appended the
     * {@code &H=<hex>} fragment. When {@code authorizationHeader} is
     * non-{@code null} the {@code Authorization} header carrying the
     * keystore attestation certificate chain is attached as well.
     *
     * @param path the API sub-path ({@code /exist}, {@code /code},
     *             {@code /register}, {@code /challenge}, {@code /security})
     * @param body the fully-assembled request body
     * @param authorizationHeader the {@code Authorization} header value,
     *                            or {@code null} to omit the header
     * @return a ready-to-send HTTP request
     */
    @Override
    protected HttpRequest createRequest(String path, String body, String authorizationHeader) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create("%s%s".formatted(MOBILE_REGISTRATION_ENDPOINT, path)))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("User-Agent", store.device().toUserAgent(store.clientVersion()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "text/json")
                .header("WaMsysRequest", "1")
                .header("request_token", UUID.randomUUID().toString());
        if (authorizationHeader != null) {
            builder.header("Authorization", authorizationHeader);
        }
        return builder.build();
    }

    /**
     * Signs the base64 ENC body with the configured
     * {@link WhatsAppDeviceAttestor.Android} and packages the result
     * into the {@code H=} suffix and {@code Authorization} header.
     *
     * <p>The attestor's {@link WhatsAppDeviceAttestor.Android#sign sign}
     * call returns raw signature bytes and a raw certificate chain; this
     * method hex-encodes the signature (lowercase, no separator, the
     * format the WhatsApp server expects) and URL-safe-base64-encodes the
     * certificate chain without padding. When either component comes back
     * empty the {@link WhatsAppDeviceAttestor.Android#NONE} attestor is
     * in effect and {@link BodyAttestation#EMPTY} is returned, which
     * tells the base class to skip both the {@code &H=} fragment and the
     * header.
     *
     * @param encBodyBytes the UTF-8 bytes of the base64 ENC body to sign
     * @return the packaged signature and header value, or
     *         {@link BodyAttestation#EMPTY} when no real signature is
     *         available
     */
    @Override
    protected BodyAttestation attestBody(byte[] encBodyBytes) {
        var signed = attestor.sign(store, encBodyBytes);
        var signature = signed.signature();
        var chain = signed.certificateChain();
        if (signature.length == 0) {
            return BodyAttestation.EMPTY;
        }
        var hex = HexFormat.of().formatHex(signature);
        var auth = chain.length == 0
                ? null
                : Base64.getUrlEncoder().withoutPadding().encodeToString(chain);
        return new BodyAttestation(hex, auth);
    }

    /**
     * Returns the large set of Android-specific form fields that the
     * {@code /code} endpoint expects in addition to the shared
     * registration parameters.
     *
     * <p>The exact field set was verified by hooking
     * {@code mbedtls_gcm_crypt_and_tag} (mbedTLS AES-GCM, used inside
     * {@code libwhatsapp.so}) on a live Android client and dumping the
     * pre-encryption form body. Every name and default value here is
     * what the native client emits for a {@code method=voice} run on a
     * fresh device. The Play Integrity sextuple
     * ({@code gpia, _gg, _gi, _gp, _ge, _ga}) is not emitted here
     * because it is added on every attested endpoint by
     * {@link #attestationFields()}.
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
                "sim_type", "1",
                "recaptcha", "%7B%22stage%22%3A%22ABPROP_DISABLED%22%7D",
                "network_radio_type", "1",
                "prefer_sms_over_flash", "true",
                "simnum", "0",
                "airplane_mode_type", "0",
                "client_metrics", buildClientMetrics(),
                "mistyped", "7",
                "advertising_id", store.advertisingId().toString(),
                "hasinrc", "1",
                "roaming_type", "0",
                "device_ram", "3.57",
                "education_screen_displayed", "true",
                "pid", String.valueOf(ProcessHandle.current().pid()),
                "cellular_strength", "5",
                "backup_token", toUrlHex(store.backupToken()),
                "tos_version", "5",
                "call_log_permission", "false",
                "manage_call_permission", "false",
                "clicked_education_link", "false",
                "aid", "",
                "push_code", pushClient.getPushCode()
        };
    }

    /**
     * Returns the Play Integrity sextuple ({@code gpia}, {@code _gg},
     * {@code _gi}, {@code _gp}, {@code _ge}, {@code _ga}) produced by
     * the configured {@link WhatsAppDeviceAttestor.Android} plus the FCM
     * device {@code push_token} produced by the configured
     * {@link WhatsAppDevicePushClient}, ready for injection into every attested
     * request body.
     *
     * <p>Both groups are routed through here because they share a
     * lifecycle: {@code push_token} is sent on every attested endpoint
     * (matching the live native-client capture for {@code /v2/exist})
     * and the Play Integrity values are required on every attested
     * endpoint too. The {@link WhatsAppDeviceAttestor.Android#NONE}
     * attestor returns six empty strings for Play Integrity and the
     * {@link WhatsAppDevicePushClient#noop()} push client returns an empty
     * {@code push_token}, which the registration server tolerates as a
     * low-trust signal.
     *
     * @return the alternating name/value form parameters
     */
    @Override
    protected String[] attestationFields() {
        var attestation = attestor.attest(store);
        return new String[]{
                "gpia", attestation.gpia(),
                "_gg", attestation.gg(),
                "_gi", attestation.gi(),
                "_gp", attestation.gp(),
                "_ge", attestation.ge(),
                "_ga", attestation.ga(),
                "push_token", pushClient.getPushToken()
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

    /**
     * Builds the percent-encoded {@code client_metrics} JSON payload
     * tracking the real attempt count driven by the base class's retry
     * loop.
     *
     * <p>Reproduces the shape captured from a live native Android client
     * via Frida-instrumented {@code mbedtls_gcm_crypt_and_tag} on a
     * {@code method=voice} run: {@code attempts} integer,
     * {@code app_campaign_download_source} string, and
     * {@code is_sim_absent} boolean. Field order matches what the
     * native client emits.
     *
     * @return the percent-encoded JSON string
     */
    private String buildClientMetrics() {
        var json = "{\"attempts\":" + attempt
                + ",\"app_campaign_download_source\":\"" + attestor.downloadSource().wireValue() + "\""
                + ",\"is_sim_absent\":false}";
        return URLEncoder.encode(json, StandardCharsets.UTF_8);
    }
}
