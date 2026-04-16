package com.github.auties00.cobalt.client.info;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.model.device.pairing.ClientAppVersion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Resolves the client metadata required to impersonate the native iOS
 * WhatsApp application during the mobile registration protocol.
 *
 * <p>The iOS registration protocol uses a much simpler token scheme than its
 * Android counterpart: an MD5 digest of a static 40-character secret
 * (different for consumer and business builds) concatenated with the MD5
 * build hash of the current version and the national phone number. Unlike
 * Android there is no need to download the signed binary, because the
 * secrets are embedded in this class; the only information that changes
 * between releases is the published version string, which this class
 * retrieves from the Apple App Store lookup API.
 *
 * <p>Two flavours are supported: the consumer
 * {@code net.whatsapp.WhatsApp} bundle and the business
 * {@code net.whatsapp.WhatsAppSMB} bundle. Each flavour has its own lazily
 * initialised singleton protected by a double-checked lock.
 *
 * @apiNote This class has no WA Web counterpart: it implements the native
 *          iOS registration token scheme that lives inside the iOS WhatsApp
 *          IPA. WA Web never touches the mobile registration protocol.
 * @see WhatsAppMobileClientInfo
 */
final class WhatsAppIosClientInfo implements WhatsAppMobileClientInfo {
    /**
     * Apple App Store lookup URL that returns JSON metadata for the
     * consumer WhatsApp bundle.
     */
    private static final URI MOBILE_PERSONAL_IOS_URL = URI.create("https://itunes.apple.com/lookup?bundleId=net.whatsapp.WhatsApp");

    /**
     * Apple App Store lookup URL that returns JSON metadata for the
     * business WhatsApp bundle.
     */
    private static final URI MOBILE_BUSINESS_IOS_URL = URI.create("https://itunes.apple.com/lookup?bundleId=net.whatsapp.WhatsAppSMB");

    /**
     * User-Agent used when calling the App Store lookup API, mimicking a
     * recent mobile Safari on an iPhone.
     */
    private static final String MOBILE_IOS_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3.1 Mobile/15E148 Safari/604.1";

    /**
     * Cached instance for the consumer iOS flavour, populated on first
     * access by {@link #ofPersonal()}.
     */
    private static volatile WhatsAppIosClientInfo personalIpaInfo;

    /**
     * Monitor used to serialise initialisation of {@link #personalIpaInfo}.
     */
    private static final Object personalIpaInfoLock = new Object();

    /**
     * Cached instance for the business iOS flavour, populated on first
     * access by {@link #ofBusiness()}.
     */
    private static volatile WhatsAppIosClientInfo businessIpaInfo;

    /**
     * Monitor used to serialise initialisation of {@link #businessIpaInfo}.
     */
    private static final Object businessIpaInfoLock = new Object();

    /**
     * Static secret prefix used by the consumer iOS registration token
     * algorithm.
     *
     * <p>Reverse-engineered from the iOS consumer WhatsApp binary.
     */
    private static final String MOBILE_IOS_STATIC = "0a1mLfGUIBVrMKF1RdvLI5lkRBvof6vn0fD2QRSM";

    /**
     * Static secret prefix used by the business iOS registration token
     * algorithm.
     *
     * <p>Reverse-engineered from the iOS business WhatsApp binary.
     */
    private static final String MOBILE_BUSINESS_IOS_STATIC = "USUDuDYDeQhY4RF2fCSp5m3F6kJ1M2J8wS7bbNA2";

    /**
     * Application version returned by the App Store lookup, normalised to
     * the {@code 2.X.Y} form expected by WhatsApp servers.
     */
    private final ClientAppVersion version;

    /**
     * {@code true} when this instance represents the WhatsApp Business IPA.
     */
    private final boolean business;

    /**
     * Constructs a new instance from the App Store lookup result.
     *
     * @param version the parsed application version
     * @param business whether this represents the business flavour
     */
    private WhatsAppIosClientInfo(ClientAppVersion version, boolean business) {
        this.version = version;
        this.business = business;
    }

    /**
     * Returns the cached consumer iOS info, performing the App Store
     * lookup on the first call.
     *
     * @return the consumer iOS client info
     * @throws RuntimeException if the App Store lookup fails
     */
    public static WhatsAppIosClientInfo ofPersonal() {
        if (personalIpaInfo == null) {
            synchronized (personalIpaInfoLock) {
                if(personalIpaInfo == null) {
                    personalIpaInfo = queryIpaInfo(false);
                }
            }
        }
        return personalIpaInfo;
    }

    /**
     * Returns the cached business iOS info, performing the App Store
     * lookup on the first call.
     *
     * @return the business iOS client info
     * @throws RuntimeException if the App Store lookup fails
     */
    public static WhatsAppIosClientInfo ofBusiness() {
        if (businessIpaInfo == null) {
            synchronized (businessIpaInfoLock) {
                if(businessIpaInfo == null) {
                    businessIpaInfo = queryIpaInfo(true);
                }
            }
        }
        return businessIpaInfo;
    }

    /**
     * Calls the Apple App Store lookup API for the requested bundle and
     * parses the returned version string into a {@link ClientAppVersion}.
     *
     * <p>If the App Store returns an empty result array or a missing
     * {@code version} field, this method returns {@code null} and the
     * calling accessor will leave the singleton unpopulated so that a later
     * call can retry. Version strings returned without a leading
     * {@code "2."} are prefixed to match the canonical WhatsApp versioning
     * scheme.
     *
     * @param business {@code true} for the business flavour, {@code false}
     *                 for the consumer flavour
     * @return a populated {@code WhatsAppIosClientInfo} or {@code null} if
     *         the lookup returned no usable data
     * @throws RuntimeException if the HTTP exchange fails
     */
    private static WhatsAppIosClientInfo queryIpaInfo(boolean business) {
        try(var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()) {
            var request = HttpRequest.newBuilder()
                    .uri(business ? MOBILE_BUSINESS_IOS_URL : MOBILE_PERSONAL_IOS_URL)
                    .header("User-Agent", MOBILE_IOS_USER_AGENT)
                    .GET()
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                throw new IOException("HTTP request failed with status code: " + response.statusCode());
            }

            // Parses the JSON envelope and extracts the results array
            var jsonObject = JSON.parseObject(response.body());
            var results = jsonObject.getJSONArray("results");
            if (results == null || results.isEmpty()) {
                return null;
            }

            // Reads the version string from the first result entry
            var result = results.getJSONObject(0);
            var version = result.getString("version");
            if (version == null) {
                return null;
            }

            // Normalises versions returned without the leading 2. prefix
            if (!version.startsWith("2.")) {
                version = "2." + version;
            }

            var parsedVersion = ClientAppVersion.of(version);
            return new WhatsAppIosClientInfo(parsedVersion, business);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Cannot query iOS version", e);
        }
    }

    /**
     * Returns the application version reported by the App Store for this
     * flavour.
     *
     * @return the parsed version
     */
    @Override
    public ClientAppVersion version() {
        return version;
    }

    /**
     * Returns whether this instance represents the WhatsApp Business iOS
     * bundle.
     *
     * @return {@code true} for business, {@code false} for consumer
     */
    @Override
    public boolean business() {
        return business;
    }

    /**
     * Computes the iOS registration token for the given national phone
     * number.
     *
     * <p>The token is the lower-case hex MD5 of the concatenation of the
     * flavour-specific static secret, the hex-encoded build hash of the
     * current version (see {@link ClientAppVersion#toHash()}) and the
     * decimal national phone number. No key material from the signed
     * binary is involved on iOS.
     *
     * @param nationalPhoneNumber the phone number in its national form,
     *                            without the country code
     * @return the hex-encoded MD5 digest suitable for direct use as the
     *         {@code token} form parameter
     * @throws UnsupportedOperationException if MD5 is not available (should
     *                                       not happen on any JDK)
     */
    @Override
    public String computeRegistrationToken(long nationalPhoneNumber) {
        try {
            // Picks the business or consumer static secret prefix
            var staticToken = business ? MOBILE_BUSINESS_IOS_STATIC : MOBILE_IOS_STATIC;

            // Concatenates prefix || hex(buildHash) || nationalPhoneNumber
            var token = staticToken + HexFormat.of().formatHex(version.toHash()) + nationalPhoneNumber;

            // MD5-hashes the composed token and returns its hex representation
            var digest = MessageDigest.getInstance("MD5");
            digest.update(token.getBytes());
            var result = digest.digest();
            return HexFormat.of().formatHex(result);
        } catch (NoSuchAlgorithmException exception) {
            throw new UnsupportedOperationException("Missing md5 implementation", exception);
        }
    }
}
