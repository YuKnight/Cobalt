package com.github.auties00.cobalt.client.registration;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClientVerificationHandler;
import com.github.auties00.cobalt.client.info.WhatsAppMobileClientInfo;
import com.github.auties00.cobalt.exception.WhatsAppRegistrationException;
import com.github.auties00.cobalt.model.business.*;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.cobalt.util.DataUtils;
import com.github.auties00.curve25519.Curve25519;
import com.github.auties00.libsignal.key.SignalIdentityKeyPair;
import com.github.auties00.libsignal.key.SignalIdentityPublicKey;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Drives the mobile phone-number registration flow against WhatsApp's
 * {@code v.whatsapp.net/v2} endpoint.
 *
 * <p>Registering a phone number with WhatsApp as a native Android or iOS
 * client involves three sequential calls against the legacy mobile
 * registration API:
 * <ol>
 *   <li>{@code /exist} asks the server whether an account already exists
 *       for the exact Signal identity/noise keys that Cobalt is about to
 *       claim. The Cobalt logic treats a {@code "reason": "incorrect"}
 *       response as confirmation that the keys are free and proceeds,
 *       otherwise it aborts.</li>
 *   <li>{@code /code} asks the server to send a verification code via the
 *       method the user chose (SMS, voice call, or WhatsApp OTP).</li>
 *   <li>{@code /register} submits the verification code the user provided
 *       and completes the claim.</li>
 * </ol>
 * The concrete subclasses {@link WhatsAppAndroidClientRegistration} and
 * {@link WhatsAppIosClientRegistration} plug in the platform-specific
 * request parameters, User-Agent, device identifier format, and HTTP
 * headers.
 *
 * <p>All request bodies are end-to-end encrypted with AES-GCM under a
 * Curve25519 shared key derived between a freshly generated ephemeral key
 * pair and a hardcoded server registration key, and then Base64-encoded and
 * wrapped in a {@code ENC=...} form field, mirroring the scheme the native
 * mobile apps use.
 *
 * @apiNote This class has no WA Web counterpart. WA Web clients pair with
 *          an already-registered phone via the QR or link-code flow rather
 *          than driving the mobile registration API themselves, so this
 *          class exists purely to let Cobalt take the role of a native
 *          Android or iOS client.
 * @see WhatsAppAndroidClientRegistration
 * @see WhatsAppIosClientRegistration
 */
public abstract sealed class WhatsAppMobileClientRegistration implements AutoCloseable
        permits WhatsAppAndroidClientRegistration, WhatsAppIosClientRegistration {
    /**
     * Base URL of the mobile registration API shared by all three
     * operations ({@code /exist}, {@code /code}, {@code /register}).
     */
    public static final String MOBILE_REGISTRATION_ENDPOINT = "https://v.whatsapp.net/v2";

    /**
     * WhatsApp's hardcoded Curve25519 public key used as the peer in the
     * request-encryption ECDH, extracted from the native mobile apps.
     */
    private static final byte[] REGISTRATION_PUBLIC_KEY = HexFormat.of().parseHex("8e8c0f74c3ebc5d7a6865c6c3c843856b06121cce8ea774d22fb6f122512302d");

    /**
     * Single-byte Signal identity key type marker, URL-safe Base64 encoded,
     * sent as the {@code e_keytype} form field.
     */
    private static final String SIGNAL_PUBLIC_KEY_TYPE = Base64.getUrlEncoder().encodeToString(new byte[]{SignalIdentityPublicKey.type()});

    /**
     * Shared HTTP client used for every registration request.
     *
     * <p>Created once per registration instance and closed together with
     * the instance via {@link #close()}.
     */
    protected final HttpClient httpClient;

    /**
     * Cobalt store providing the Signal identity keys, Noise keys,
     * registration ID, FDID, device ID, phone number, and other persistent
     * credentials that the registration requests rely on.
     */
    protected final WhatsAppStore store;

    /**
     * Callback used to ask the calling code which verification method to
     * request (SMS / voice / WhatsApp OTP) and what verification code the
     * user entered.
     */
    protected final WhatsAppClientVerificationHandler.Mobile verification;

    /**
     * Constructs a new registration for the given store and verification
     * callbacks.
     *
     * @param store the store carrying identity keys and the phone number
     * @param verification the verification handler supplying the method
     *                     and the user-entered code
     * @throws NullPointerException if either argument is {@code null}
     */
    protected WhatsAppMobileClientRegistration(WhatsAppStore store, WhatsAppClientVerificationHandler.Mobile verification) {
        Objects.requireNonNull(store, "store cannot be null");
        Objects.requireNonNull(verification, "verification cannot be null");
        this.store = store;
        this.verification = verification;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    /**
     * Returns the concrete registration implementation that matches the
     * platform configured on the given store.
     *
     * @param store the store whose device platform drives the selection
     * @param verification the verification handler passed to the new
     *                     registration
     * @return a concrete {@code WhatsAppMobileClientRegistration}
     * @throws IllegalArgumentException if the store's platform is not a
     *                                  supported mobile platform
     */
    public static WhatsAppMobileClientRegistration of(WhatsAppStore store, WhatsAppClientVerificationHandler.Mobile verification) {
        return switch (store.device().platform()) {
            case ANDROID -> new WhatsAppAndroidClientRegistration(store, verification);
            case IOS -> new WhatsAppIosClientRegistration(store, verification);
            default -> throw new IllegalArgumentException("Unsupported platform: " + store.device().platform());
        };
    }

    /**
     * Returns the platform-specific form parameters that accompany a
     * {@code /code} request in addition to the shared registration
     * parameters.
     *
     * @param method the verification method chosen by the user
     *               (for example {@code "sms"}, {@code "voice"},
     *               {@code "wa_old"})
     * @return an alternating name/value array of additional parameters
     */
    protected abstract String[] getRequestVerificationCodeParameters(String method);

    /**
     * Returns the device family identifier used as the {@code fdid} form
     * field.
     *
     * <p>Android uses the lower-case UUID form, iOS uses the upper-case
     * UUID form, matching the behaviour of the respective native apps.
     *
     * @return the formatted device family identifier
     */
    protected abstract String generateFdid();

    /**
     * Builds an {@link HttpRequest} for the given sub-path of the mobile
     * registration endpoint with the encrypted body and the correct set of
     * platform-specific headers.
     *
     * @param path the sub-path, starting with a slash
     *             (for example {@code "/exist"})
     * @param body the Base64 URL-encoded encrypted form body
     * @return a ready-to-send HTTP request
     */
    protected abstract HttpRequest createRequest(String path, String body);

    /**
     * Executes the entire three-step registration flow and saves the
     * resulting state on success.
     *
     * <p>The flow short-circuits when the account already exists with the
     * exact keys being claimed, in which case no verification code is
     * requested. On success the store is marked as registered and its JID
     * is set to the phone-number JID.
     *
     * @throws WhatsAppRegistrationException if any server response
     *                                       indicates a non-recoverable
     *                                       failure or if the underlying
     *                                       HTTP exchange fails
     */
    public void register() {
        try {
            // Checks that the account slot for the local keys is still free
            assertRegistrationKeys();

            // Asks the server to send a verification code unless one was already requested
            requestVerificationCodeIfNecessary();

            // Submits the code the user entered and finalises the registration
            sendVerificationCode();
        } catch (IOException | InterruptedException exception) {
            throw new WhatsAppRegistrationException(exception);
        }
    }

    /**
     * Probes {@code /exist} twice to confirm that no account already holds
     * the exact Signal / Noise keys the store is about to register.
     *
     * <p>The server answers {@code "reason": "incorrect"} when the keys
     * differ from whatever the phone number currently has on file, which
     * paradoxically means the number is free from the perspective of these
     * specific keys. The probe is retried once to tolerate transient
     * oddities.
     *
     * @throws IOException if the HTTP call fails
     * @throws InterruptedException if the sending thread is interrupted
     * @throws WhatsAppRegistrationException if neither attempt returns
     *                                       {@code "incorrect"}
     */
    private void assertRegistrationKeys() throws IOException, InterruptedException {
        // Builds the registration parameters without the registration token,
        // since /exist does not require it
        var attrs = getRegistrationOptions(false);

        // Sends the first probe and accepts the "incorrect" reply
        var result = sendRequest("/exist", attrs);
        var response = JSON.parseObject(result);
        if (Objects.equals(response.getString("reason"), "incorrect")) {
            return;
        }

        // Retries once in case the first response was a transient anomaly
        result = sendRequest("/exist", attrs);
        response = JSON.parseObject(result);
        if (Objects.equals(response.getString("reason"), "incorrect")) {
            return;
        }

        // Raises a registration failure carrying the raw server payload for diagnostics
        throw new WhatsAppRegistrationException("Cannot get account data", new String(result));
    }


    /**
     * Invokes the verification handler to find out which code method to
     * request and, if one is chosen, calls {@code /code}.
     *
     * <p>If the handler returns an empty optional it is understood to mean
     * that a code has already been requested outside Cobalt, so this step
     * is skipped and the flow proceeds directly to
     * {@link #sendVerificationCode()}.
     *
     * @throws IOException if the HTTP call fails
     * @throws InterruptedException if the sending thread is interrupted
     */
    private void requestVerificationCodeIfNecessary() throws IOException, InterruptedException {
        // Asks the user-supplied handler for the desired verification method
        var codeResult = verification.requestMethod();
        if (codeResult.isEmpty()) {
            return;
        }

        // Requests the code from the server and persists an in-progress marker
        requestVerificationCode(codeResult.get());
        saveRegistrationStatus(false);
    }

    /**
     * Calls {@code /code} in a retry loop until the server confirms the
     * request with a successful status or a definite error.
     *
     * <p>The loop tracks the last error so that two consecutive identical
     * errors abort the flow; transient errors are retried once. Specific
     * conditions map to user-facing exceptions: {@code too_recent}/{@code
     * too_many} variants mean the caller is spamming, and {@code no_routes}
     * means WhatsApp refused to deliver the code to the given number via
     * the chosen method.
     *
     * @param method the verification method the user asked for
     * @throws IOException if the HTTP call fails
     * @throws InterruptedException if the sending thread is interrupted
     * @throws WhatsAppRegistrationException if the server reports a
     *                                       blocking error
     */
    private void requestVerificationCode(String method) throws IOException, InterruptedException {
        String lastError = null;
        while (true) {
            // Rebuilds the parameters each iteration because the registration token depends on state
            var params = getRequestVerificationCodeParameters(method);
            var attrs = getRegistrationOptions(true, params);

            // Sends the /code request and parses its JSON response
            var result = sendRequest("/code", attrs);
            var response = JSON.parseObject(result);
            var status = response.getString("status");
            if (isSuccessful(status)) {
                return;
            }

            // Rejects the flow if the server signalled rate limiting
            var reason = response.getString("reason");
            if(isTooRecent(reason)) {
                throw new WhatsAppRegistrationException("Please wait before trying to register this phone value again. Don't spam!", new String(result));
            }

            // Raises a targeted message when the server blocks the method entirely
            if(isRegistrationBlocked(reason)) {
                var resultJson = new String(result);
                if(method.equals("wa_old")) {
                    throw new WhatsAppRegistrationException("The registration attempt was blocked by Whatsapp: you might want to change platform(iOS/Android) or try using a residential proxy (don't spam)", resultJson);
                }else {
                    throw new WhatsAppRegistrationException("The registration attempt was blocked by Whatsapp: please try using a Whatsapp OTP as a verification method", resultJson);
                }
            }

            // Aborts if the same error reason appears twice in a row
            if (Objects.equals(reason, lastError)) {
                throw new WhatsAppRegistrationException("An error occurred while registering: " + reason, new String(result));
            }

            lastError = reason;
        }
    }

    /**
     * Returns {@code true} when the given {@code reason} string indicates
     * that the client is being rate limited.
     *
     * @param reason the reason string from a {@code /code} JSON response
     * @return {@code true} if the reason is a known rate-limit keyword
     */
    private boolean isTooRecent(String reason) {
        return reason.equalsIgnoreCase("too_recent")
               || reason.equalsIgnoreCase("too_many")
               || reason.equalsIgnoreCase("too_many_guesses")
               || reason.equalsIgnoreCase("too_many_all_methods");
    }

    /**
     * Returns {@code true} when the given {@code reason} string indicates
     * that WhatsApp has refused to deliver the verification code to the
     * number.
     *
     * @param reason the reason string from a {@code /code} JSON response
     * @return {@code true} if the reason means routing is unavailable
     */
    private boolean isRegistrationBlocked(String reason) {
        return reason.equalsIgnoreCase("no_routes");
    }


    /**
     * Submits the verification code that the user entered to the
     * {@code /register} endpoint and marks the store as registered on
     * success.
     *
     * <p>The entered code is normalised to strip whitespace and dashes
     * before being embedded in the request so that common user formats
     * (for example {@code "123-456"}) are accepted.
     *
     * @throws IOException if the HTTP call fails
     * @throws InterruptedException if the sending thread is interrupted
     * @throws WhatsAppRegistrationException if the server refuses the
     *                                       submitted code
     */
    public void sendVerificationCode() throws IOException, InterruptedException {
        // Reads the code the user entered from the verification handler
        var code = verification.verificationCode();

        // Appends the normalised code to the registration parameters and sends the request
        var attrs = getRegistrationOptions(true, "code", normalizeCodeResult(code));
        var result = sendRequest("/register", attrs);
        var response = JSON.parseObject(result);
        var status = response.getString("status");
        if (isSuccessful(status)) {
            // Persists the successful registration and the derived JID
            saveRegistrationStatus(true);
            return;
        }
        throw new WhatsAppRegistrationException("Cannot confirm registration", new String(result));
    }

    /**
     * Persists the registration state to the store and, on success, the
     * JID derived from the phone number.
     *
     * @param registered whether the flow has completed successfully
     * @throws IOException if the store save fails
     * @throws WhatsAppRegistrationException if the phone number is not set
     *                                       on the store when registration
     *                                       succeeds
     */
    private void saveRegistrationStatus(boolean registered) throws IOException {
        store.setRegistered(registered);
        if (registered) {
            // Derives the phone-number JID and stores it as the local JID
            var phoneNumber = store.phoneNumber()
                    .orElseThrow(() -> new WhatsAppRegistrationException("Phone number wasn't set"));
            var jid = Jid.of(phoneNumber);
            store.setJid(jid);
        }
        store.save();
    }

    /**
     * Strips dashes and whitespace from the user-entered verification code
     * so common formats such as {@code "123-456"} are accepted.
     *
     * @param code the raw code from the verification handler
     * @return the digits-only code
     */
    private String normalizeCodeResult(String code) {
        return code.replaceAll("-", "")
                .trim();
    }

    /**
     * Returns {@code true} when the registration API status string
     * indicates success.
     *
     * @param status the {@code status} field from a JSON response
     * @return {@code true} for {@code ok}, {@code sent}, or
     *         {@code verified}
     */
    private boolean isSuccessful(String status) {
        return status.equalsIgnoreCase("ok")
               || status.equalsIgnoreCase("sent")
               || status.equalsIgnoreCase("verified");
    }

    /**
     * Encrypts the given form body with a fresh ephemeral Curve25519 key,
     * prepends the ephemeral public key, Base64-URL encodes the result, and
     * sends it as an HTTP request against the given sub-path.
     *
     * <p>The server decrypts the payload with its own half of the ECDH,
     * using the hardcoded {@link #REGISTRATION_PUBLIC_KEY} as the other
     * peer.
     *
     * @param path the API sub-path ({@code /exist}, {@code /code},
     *             {@code /register})
     * @param params the unencrypted form body
     * @return the raw response bytes
     * @throws IOException if the HTTP call fails
     * @throws InterruptedException if the sending thread is interrupted
     * @throws RuntimeException if encryption fails or the HTTP status is
     *                          not 200
     */
    private byte[] sendRequest(String path, String params) throws IOException, InterruptedException {
        try {
            // Generates a per-request ephemeral key pair for the outgoing ECDH
            var keypair = SignalIdentityKeyPair.random();
            var key = Curve25519.sharedKey(keypair.privateKey().toEncodedPoint(), REGISTRATION_PUBLIC_KEY);

            // Encrypts the body with AES-256-GCM using a zero IV per the mobile protocol
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(128, new byte[12])
            );
            var result = cipher.doFinal(params.getBytes(StandardCharsets.UTF_8));

            // Concatenates the ephemeral public key with the ciphertext and URL-base64 encodes it
            var cipheredParameters = Base64.getUrlEncoder().encodeToString(DataUtils.concatByteArrays(keypair.publicKey().toEncodedPoint(), result));
            var requestBuilder = createRequest(path, cipheredParameters);

            // Sends the request via the shared HTTP client and validates the status code
            var response = httpClient.send(requestBuilder, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() != 200) {
                throw new RuntimeException("Cannot send request to " + path + ": status code" + response.statusCode());
            }
            return response.body();
        } catch (GeneralSecurityException exception) {
            throw new RuntimeException("Cannot encrypt request", exception);
        }
    }

    /**
     * Assembles the common registration form body, optionally including a
     * registration token and any extra attributes the caller wants to
     * append.
     *
     * <p>The resulting string contains every shared field the server
     * expects: country code, national number, release channel, Signal
     * identity public key, pre-key, signed pre-key and its signature, noise
     * key, FDID, business verified-name certificate when applicable, and
     * so on. Fields whose value is {@code null} are omitted from the output
     * without error.
     *
     * @param useToken whether to compute and include the registration
     *                 token in the {@code token} field
     * @param attributes optional additional alternating name/value pairs
     *                   appended after the shared fields
     * @return the URL-encoded form body
     */
    private String getRegistrationOptions(boolean useToken, String... attributes) {
        // Parses the store's phone number into a structured object for CC/NN extraction
        var phoneNumber = getPhoneNumber(store);

        // Computes the registration token on demand; null disables the token
        var token = getToken(phoneNumber, useToken);

        // Builds the verified-name certificate only for business platforms
        var certificate = generateBusinessCertificate();
        var fdid = generateFdid();

        // Assembles the set of shared form fields expected by the registration API
        var registrationParams = toFormParams(
                "cc", String.valueOf(phoneNumber.getCountryCode()),
                "in", String.valueOf(phoneNumber.getNationalNumber()),
                "rc", String.valueOf(store.releaseChannel().index()),
                "lg", "en",
                "lc", "US",
                "authkey", Base64.getUrlEncoder().encodeToString(store.noiseKeyPair().publicKey().toEncodedPoint()),
                "vname", certificate,
                "e_regid", Base64.getUrlEncoder().encodeToString(DataUtils.intToBytes(store.registrationId(), 4)),
                "e_keytype", SIGNAL_PUBLIC_KEY_TYPE,
                "e_ident", Base64.getUrlEncoder().encodeToString(store.identityKeyPair().publicKey().toEncodedPoint()),
                "e_skey_id", Base64.getUrlEncoder().encodeToString(DataUtils.intToBytes(store.signedKeyPair().id(), 3)),
                "e_skey_val", Base64.getUrlEncoder().encodeToString(store.signedKeyPair().publicKey().toEncodedPoint()),
                "e_skey_sig", Base64.getUrlEncoder().encodeToString(store.signedKeyPair().signature()),
                "fdid", fdid,
                "expid", Base64.getUrlEncoder().encodeToString(store.deviceId()),
                "id", toUrlHex(store.identityId()),
                "token", useToken ? token : null
        );

        // Merges any caller-supplied extra parameters into the final body
        var additionalParams = toFormParams(attributes);
        if (additionalParams.isEmpty()) {
            return registrationParams;
        } else if(registrationParams.isEmpty()) {
            return additionalParams;
        } else {
            return registrationParams + "&" + additionalParams;
        }
    }

    /**
     * Computes the registration token for the given phone number via the
     * platform-specific algorithm, unless the caller requests that the
     * token be omitted.
     *
     * @param phoneNumber the parsed phone number
     * @param useToken whether a token should be computed
     * @return the token, or {@code null} if {@code useToken} is
     *         {@code false}
     */
    private String getToken(PhoneNumber phoneNumber, boolean useToken) {
        if (!useToken) {
            return null;
        }

        // Delegates to the platform-specific WhatsAppMobileClientInfo
        var info = WhatsAppMobileClientInfo.of(store.device().platform());
        return info.computeRegistrationToken(phoneNumber.getNationalNumber());
    }

    /**
     * Generates a dummy WhatsApp Business verified-name certificate when
     * the current platform is a business flavour.
     *
     * <p>The certificate carries an empty verified name and a random serial
     * number, and is signed with the local Signal identity private key.
     * Consumer platforms return {@code null}.
     *
     * @return the Base64-URL encoded certificate or {@code null} for
     *         consumer platforms
     */
    protected String generateBusinessCertificate() {
        // Skips certificate generation for non-business platforms
        var platform = store.device().platform();
        if(platform != ClientPlatformType.ANDROID_BUSINESS && platform != ClientPlatformType.IOS_BUSINESS) {
            return null;
        }

        // Builds the certificate details with an empty name and a random serial
        var details = new BusinessVerifiedNameCertificateDetailsBuilder()
                .verifiedName("")
                .issuer(BusinessVerifiedNameCertificate.CertificateIssuer.SMALL_BUSINESS)
                .serial(Math.abs(new SecureRandom().nextLong()))
                .build();
        var encodedDetails = BusinessVerifiedNameCertificateDetailsSpec.encode(details);

        // Signs the details with the local identity private key and packages them
        var certificate = new BusinessVerifiedNameCertificateBuilder()
                .details(encodedDetails)
                .signature(Curve25519.sign(store.identityKeyPair().privateKey().toEncodedPoint(), encodedDetails))
                .build();
        return Base64.getUrlEncoder().encodeToString(BusinessVerifiedNameCertificateSpec.encode(certificate));
    }

    /**
     * Reads the phone number from the store and parses it into a
     * {@link PhoneNumber} usable for CC/NN extraction.
     *
     * @param store the store carrying the registered phone number
     * @return the parsed phone number
     * @throws WhatsAppRegistrationException if the store has no phone
     *                                       number or it cannot be parsed
     */
    protected static PhoneNumber getPhoneNumber(WhatsAppStore store) {
        var phoneNumber = store.phoneNumber()
                .orElseThrow(() -> new WhatsAppRegistrationException("Phone number wasn't set"));
        try {
            // Parses the number using libphonenumber with E.164 leading plus
            return PhoneNumberUtil.getInstance()
                    .parse("+" + phoneNumber, null);
        }catch (NumberParseException exception) {
            throw new WhatsAppRegistrationException("Malformed phone number: " + phoneNumber);
        }
    }

    /**
     * Percent-encodes every byte of the given buffer as {@code %XX} in
     * upper case, producing the mobile API's {@code id} field value.
     *
     * @param buffer the byte buffer to format
     * @return the percent-encoded upper-case hex string
     */
    protected String toUrlHex(byte[] buffer) {
        var id = new StringBuilder();
        for (var x : buffer) {
            id.append(String.format("%%%02x", x));
        }
        return id.toString().toUpperCase(Locale.ROOT);
    }

    /**
     * Joins the given alternating name/value pairs into a
     * {@code name1=value1&name2=value2} form body, skipping pairs whose
     * value is {@code null}.
     *
     * @param entries alternating name/value pairs, totalling an even count
     * @return the joined form body
     * @throws IllegalArgumentException if {@code entries.length} is odd
     */
    private String toFormParams(String... entries) {
        if (entries == null) {
            return "";
        }

        var length = entries.length;
        if ((length & 1) != 0) {
            throw new IllegalArgumentException("Odd form patches");
        }

        // Walks every name/value pair, skipping entries whose value is null
        var result = new StringJoiner("&");
        for (var i = 0; i < length; i += 2) {
            if (entries[i + 1] == null) {
                continue;
            }
            result.add(entries[i] + "=" + entries[i + 1]);
        }

        return result.toString();
    }

    /**
     * Closes the shared {@link HttpClient} backing this registration.
     *
     * <p>Further calls to {@link #register()} on the same instance will
     * fail once this has been called.
     */
    @Override
    public void close() {
        httpClient.close();
    }
}
