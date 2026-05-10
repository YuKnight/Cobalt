package com.github.auties00.cobalt.registration;

import com.github.auties00.cobalt.client.WhatsAppClientVerificationHandler;
import com.github.auties00.cobalt.client.WhatsAppDeviceAttestor;
import com.github.auties00.cobalt.client.WhatsAppDevicePushClient;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Locale;
import java.util.Objects;

/**
 * Mobile registration driver that impersonates the native iOS WhatsApp
 * application.
 *
 * <p>Adds iOS-specific behaviour on top of
 * {@link MobileClientRegistration}:
 * <ul>
 *   <li>{@link #createRequest(String, String, String)} uses the iOS User-Agent
 *       derived from the store's device description and sends only the
 *       minimal HTTP headers the iOS app sends (no {@code WaMsysRequest},
 *       no explicit {@code Accept}, no per-request token header,
 *       no {@code Authorization} header).</li>
 *   <li>{@link #getRequestVerificationCodeParameters(String)} populates
 *       the short set of form fields the iOS app sends: method, SIM
 *       MCC/MNC, jailbroken flag, APNS push token + silent-push code,
 *       and cellular signal strength.</li>
 *   <li>{@link #generateFdid()} formats the device family UUID in upper
 *       case, matching the iOS behaviour.</li>
 * </ul>
 *
 * <p>The configured {@link WhatsAppDeviceAttestor.Ios} produces an
 * Apple App Attest attestation+assertion pair plus the matching
 * {@code keyId} (per {@link WhatsAppDeviceAttestor.Ios.AppAttestData}),
 * and this driver emits both on every attested endpoint, in the wire
 * shape confirmed by Frida runtime tracing of {@code
 * -[RegistrationAttestationManagerImpl assertionFor:path:]} and
 * {@code attestationPayloadForRegistrationFor:} together with
 * {@code -[NSMutableURLRequest setHTTPBody:]} /
 * {@code setValue:forHTTPHeaderField:} on a live iOS WhatsApp
 * registration:
 * <ul>
 *   <li>The per-request CBOR assertion goes into the {@code H=}
 *       suffix of the body, wrapped in a JSON envelope
 *       {@code {"assertion":"<base64 CBOR>"}}.</li>
 *   <li>The session-stable CBOR attestation goes into the
 *       {@code Authorization} request header alongside the
 *       {@code keyId}, joined by a literal {@code "|"}:
 *       {@code <base64 attestation>|<base64 keyId>}.</li>
 * </ul>
 * Both wire slots are skipped on the funnel endpoints
 * ({@code /v2/client_log}, {@code /v2/pre_pn_client_log}), matching
 * the native client's behaviour where
 * {@code attestationFor:path:} returns {@code nil} for those paths.
 *
 * @apiNote iOS-specific driver for the native mobile registration
 *          protocol. Not present in WA Web. Package-private because
 *          callers should always go through
 *          {@link MobileClientRegistration#newRegistration} rather
 *          than constructing this class directly.
 * @see MobileClientRegistration
 */
final class IosClientRegistration extends MobileClientRegistration {
    /**
     * The iOS attestor the registration consults before each outgoing
     * request. Never {@code null}: the constructor substitutes
     * {@link WhatsAppDeviceAttestor.Ios#NONE} when the caller supplies
     * {@code null}.
     */
    private final WhatsAppDeviceAttestor.Ios attestor;

    /**
     * The push client the registration consults for the {@code push_token}
     * and {@code push_code} form fields. Never {@code null}: the
     * constructor substitutes {@link WhatsAppDevicePushClient#noop()} when the
     * caller supplies {@code null}.
     */
    private final WhatsAppDevicePushClient pushClient;

    /**
     * Constructs a new iOS registration bound to the given store,
     * verification handler, attestor, and push client.
     *
     * @param store the store carrying identity keys and phone number
     * @param verification the verification handler supplying the method
     *                     and the user-entered code
     * @param attestor the iOS device attestor, or {@code null} to use
     *                 the low-trust {@link WhatsAppDeviceAttestor.Ios#NONE}
     *                 fallback
     * @param pushClient the push client, or {@code null} to use the
     *                   low-trust {@link WhatsAppDevicePushClient#noop()} fallback
     */
    IosClientRegistration(
            WhatsAppStore store,
            WhatsAppClientVerificationHandler.Mobile verification,
            WhatsAppDeviceAttestor.Ios attestor,
            WhatsAppDevicePushClient pushClient) {
        super(store, verification);
        this.attestor = Objects.requireNonNullElse(attestor, WhatsAppDeviceAttestor.Ios.NONE);
        this.pushClient = Objects.requireNonNullElse(pushClient, WhatsAppDevicePushClient.noop());
    }

    /**
     * Builds an HTTP POST request to the registration endpoint with the
     * pre-assembled body and the minimal iOS headers, attaching the
     * App Attest {@code Authorization} header when one is supplied.
     *
     * @param path the API sub-path ({@code /exist}, {@code /code},
     *             {@code /register}, {@code /challenge},
     *             {@code /security})
     * @param body the fully-assembled request body
     * @param authorizationHeader the
     *                            {@code <base64 attestation>|<base64 keyId>}
     *                            value produced by {@link #attestBody},
     *                            or {@code null} to omit the header
     * @return a ready-to-send HTTP request
     */
    @Override
    protected HttpRequest createRequest(String path, String body, String authorizationHeader) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create("%s%s".formatted(MOBILE_REGISTRATION_ENDPOINT, path)))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("User-Agent", store.device().toUserAgent(store.clientVersion()))
                .header("Content-Type", "application/x-www-form-urlencoded");
        if (authorizationHeader != null) {
            builder.header("Authorization", authorizationHeader);
        }
        return builder.build();
    }

    /**
     * Asks the configured {@link WhatsAppDeviceAttestor.Ios} to mint
     * an App Attest payload and packages it into the
     * {@link BodyAttestation} pair the base class appends to the
     * request:
     * <ul>
     *   <li>The {@code H=} body suffix carries
     *       {@code {"assertion":"<base64 CBOR>"}} — a JSON envelope
     *       around the per-request CBOR assertion the attestor
     *       returned. The native iOS client emits the JSON verbatim
     *       (with {@code "/"} JSON-escaped to {@code "\/"} but
     *       otherwise un-URL-encoded), and the WhatsApp registration
     *       server tolerates the resulting raw {@code =}, {@code +}
     *       and {@code /} characters in the form value.</li>
     *   <li>The {@code Authorization} header carries
     *       {@code <base64 attestation>|<base64 keyId>} — the cached
     *       CBOR attestation object joined to the App Attest
     *       {@code keyId} by a literal {@code "|"}.</li>
     * </ul>
     * When the attestor returns
     * {@link WhatsAppDeviceAttestor.Ios.AppAttestData#EMPTY}
     * (the case for the {@link WhatsAppDeviceAttestor.Ios#NONE}
     * fallback or for any other attestor that cannot mint App
     * Attest), {@link BodyAttestation#EMPTY} is returned and the base
     * class skips both wire slots — matching the low-trust shape a
     * simulator or jailbroken device would emit, which the server
     * tolerates as a downgrade signal.
     *
     * <p>Note that, unlike Android's {@code H=} which is an HMAC
     * over the encrypted body, the iOS assertion does not sign
     * {@code encBodyBytes}: the {@code clientDataHash} the attestor
     * binds the assertion to is the SHA-256 of the noise public key,
     * derived from the store rather than from the request body.
     * {@code encBodyBytes} is accepted only because the base-class
     * contract is platform-neutral.
     *
     * @param encBodyBytes the UTF-8 bytes of the base64 ENC body;
     *                     unused by this implementation
     * @return the App Attest pair, or {@link BodyAttestation#EMPTY}
     *         when no real attestation is available
     */
    @Override
    protected BodyAttestation attestBody(byte[] encBodyBytes) {
        var data = attestor.attest(store);
        if (data.attestation().isEmpty() || data.assertion().isEmpty() || data.keyId().isEmpty()) {
            return BodyAttestation.EMPTY;
        }
        var bodyAttestation = "{\"assertion\":\"" + data.assertion() + "\"}";
        var authorizationHeader = data.attestation() + "|" + data.keyId();
        return new BodyAttestation(bodyAttestation, authorizationHeader);
    }

    /**
     * Returns the iOS-specific form fields that the {@code /code}
     * endpoint expects in addition to the shared registration parameters.
     *
     * <p>Mirrors the parameters that the native iOS client's
     * {@code -[WARegistrationURLBuilder
     * verificationCodeRequestURLWithBaseURL:method:mcc:mnc:jailbroken:
     * context:oldPhoneNumber:silentPushNotifRegCode:
     * iosDeviceRegistrationUUID:cellularStrength:]} builder emits:
     * verification method, empty SIM MCC/MNC, jailbroken flag {@code 0},
     * the APNS silent-push verification code received via
     * {@code application:didReceiveRemoteNotification:} (sourced from
     * the push client's {@link WhatsAppDevicePushClient#getPushCode}), and a
     * cellular signal strength of {@code 1}. The APNS device token
     * itself is not emitted here because it ships on every attested
     * endpoint via {@link #attestationFields()}.
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
                "jailbroken", "0",
                "push_code", pushClient.getPushCode(),
                "cellular_strength", "1"
        };
    }

    /**
     * Returns the iOS-specific form fields that ship on every
     * attested endpoint inside the encrypted body — currently just
     * the APNS device token.
     *
     * <p>App Attest payloads do not appear here because they ride
     * outside the encrypted body: the assertion goes into the
     * {@code H=} suffix appended after the {@code ENC=} envelope
     * (built by {@link #attestBody(byte[])}) and the attestation
     * goes into the {@code Authorization} request header (built by
     * {@link #attestBody(byte[])} and attached by
     * {@link #createRequest(String, String, String) createRequest}).
     * That mirrors the native iOS client which calls its own
     * {@code attestationPayloadForRegistrationFor:} and
     * {@code assertionFor:path:} hooks at request-build time, not
     * during form-field assembly.
     *
     * <p>The {@code push_token} field carries the APNS device token
     * the iOS app receives via {@code -[UIApplication
     * application:didRegisterForRemoteNotificationsWithDeviceToken:]}
     * and gets advertised on every attested endpoint so the
     * registration server knows where to silent-push the verification
     * code.
     *
     * @return the alternating name/value form parameters
     */
    @Override
    protected String[] attestationFields() {
        return new String[]{
                "push_token", pushClient.getPushToken()
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
