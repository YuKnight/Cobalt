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
 * <p>Although the configured {@link WhatsAppDeviceAttestor.Ios}
 * produces an Apple App Attest attestation+assertion pair (signed over
 * {@code SHA-256(authkey)} per
 * {@link WhatsAppDeviceAttestor.Ios.AppAttestData}), this driver does
 * not currently emit it on the wire. The wire-field names that carry
 * the App Attest payload have not been confirmed by static analysis of
 * the native iOS binary: the relevant code paths sit behind Swift
 * protocol-witness dispatch and the feature itself is gated by an
 * {@code isAttestationAtRegistrationEnabled} server-driven flag on the
 * native client. Until the wire shape is settled by Frida runtime
 * tracing of {@code -[RegistrationAttestationManagerImpl
 * assertionFor:path:]} together with {@code -[NSMutableURLRequest
 * setHTTPBody:]} on a real device, {@link #attestationFields()}
 * returns just the APNS {@code push_token} and the attestor passed at
 * construction is stored but never consulted. It stays in the API
 * surface so that embedders can wire a real attestor today and have it
 * activate the moment the wire shape is filled in here without any
 * builder change.
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
     * pre-assembled body and the minimal iOS headers.
     *
     * <p>{@code authorizationHeader} is always {@code null} in the iOS
     * flow because the current iOS registration does not send an
     * {@code Authorization} header, but the parameter is still accepted
     * so the shared base-class contract stays platform-neutral.
     *
     * @param path the API sub-path ({@code /exist}, {@code /code},
     *             {@code /register})
     * @param body the fully-assembled request body
     * @param authorizationHeader ignored by the iOS flow. Retained for
     *                            interface compatibility
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
     * Returns {@link BodyAttestation#EMPTY}. The iOS flow does not
     * attach an {@code H=} body suffix or an {@code Authorization}
     * request header.
     *
     * <p>This is not a stub. Static analysis of the native iOS binary
     * confirms it: the only {@code H=%s} format string in the binary
     * is unrelated debug formatting. The {@code "H"} CFString in the
     * iOS form-field master table has no code xrefs. The
     * {@code "Authorization"} string has no xrefs from any function in
     * the registration / URL-builder / attestation address ranges. And
     * {@code -[NSMutableURLRequest setValue:forHTTPHeaderField:]} is
     * never called from those ranges. The Android keystore-signature
     * mechanism that produces both legs simply does not exist on iOS,
     * which is why {@link #attestBody(byte[])} is permanently empty
     * and {@link #createRequest createRequest}'s
     * {@code authorizationHeader} parameter is permanently {@code null}.
     *
     * @param encBodyBytes the UTF-8 bytes of the base64 ENC body. Unused
     * @return {@link BodyAttestation#EMPTY}
     */
    @Override
    protected BodyAttestation attestBody(byte[] encBodyBytes) {
        return BodyAttestation.EMPTY;
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
     * Returns the iOS-specific attestation-time fields that ship on
     * every attested endpoint, currently just the APNS device token.
     *
     * <p>App Attest payloads (the attestation+assertion CBOR pair the
     * configured {@link WhatsAppDeviceAttestor.Ios} can mint) are not
     * emitted because the wire-field names under which those payloads
     * ship in {@code /v2/exist}, {@code /v2/code}, {@code /v2/register},
     * {@code /v2/challenge}, {@code /v2/security} cannot be determined
     * from static analysis of the native iOS binary. Every emission
     * path goes through Swift protocol-witness dispatch
     * ({@code attestationPayloadForRegistrationFor:} and
     * {@code assertionFor:path:} on
     * {@code RegistrationAttestationManagerImpl}) and the feature is
     * gated by an {@code isAttestationAtRegistrationEnabled} Swift
     * lazy property that may not even be active in a given IPA build.
     *
     * <p>The {@code push_token} field, by contrast, is well-known: it
     * carries the APNS device token the iOS app receives via
     * {@code -[UIApplication
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
