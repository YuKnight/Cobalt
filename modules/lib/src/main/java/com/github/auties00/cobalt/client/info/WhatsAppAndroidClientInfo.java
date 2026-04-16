package com.github.auties00.cobalt.client.info;

import com.github.auties00.cobalt.model.device.pairing.ClientAppVersion;
import net.dongliu.apk.parser.ByteArrayApkFile;
import net.dongliu.apk.parser.bean.ApkSigner;
import net.dongliu.apk.parser.bean.CertificateMeta;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Resolves the client metadata required to impersonate the native Android
 * WhatsApp application during the mobile registration protocol.
 *
 * <p>The Android registration protocol binds every request to the identity
 * of the signed APK that is claiming the phone number. Specifically, the
 * server expects an HMAC-SHA1 token computed over the APK signing
 * certificates, the MD5 digest of the embedded {@code classes.dex}, and the
 * national phone number, keyed by a PBKDF2-derived key seeded from the
 * package name, the {@code about_logo.png} resource and a hardcoded salt.
 * This class downloads the current APK from WhatsApp's distribution URLs,
 * extracts all of that material, and caches it so the heavy download runs at
 * most once per flavour per JVM.
 *
 * <p>Two flavours are supported: the consumer build from
 * {@code whatsapp.com/android/current/WhatsApp.apk} and the business build
 * from {@code d.cdnpure.com}. Each flavour has its own lazily-initialised
 * singleton protected by a double-checked lock.
 *
 * @apiNote This class has no WA Web counterpart: it implements the native
 *          Android registration token scheme that lives inside the Android
 *          WhatsApp APK. WA Web clients pair via QR/link instead and never
 *          touch this protocol.
 * @see WhatsAppMobileClientInfo
 */
final class WhatsAppAndroidClientInfo implements WhatsAppMobileClientInfo {
    /**
     * Static salt used by the PBKDF2-HMAC-SHA1 routine that derives the
     * HMAC key for the registration token.
     *
     * <p>Base64-encoded at rest so that the value does not appear as a
     * plaintext hex blob in the source. The byte content itself is
     * reverse-engineered from the Android WhatsApp binary.
     */
    private static final byte[] MOBILE_ANDROID_SALT = Base64.getDecoder().decode("PkTwKSZqUfAUyR0rPQ8hYJ0wNsQQ3dW1+3SCnyTXIfEAxxS75FwkDf47wNv/c8pP3p0GXKR6OOQmhyERwx74fw1RYSU10I4r1gyBVDbRJ40pidjM41G1I1oN");

    /**
     * Public distribution URL for the current consumer WhatsApp APK.
     */
    private static final URI MOBILE_PERSONAL_ANDROID_URL = URI.create("https://www.whatsapp.com/android/current/WhatsApp.apk");

    /**
     * Mirror URL that serves the current WhatsApp Business APK.
     *
     * <p>WhatsApp does not publish the business APK on
     * {@code whatsapp.com} so the implementation relies on a third-party
     * mirror that tracks the latest release.
     */
    private static final URI MOBILE_BUSINESS_ANDROID_URL = URI.create("https://d.cdnpure.com/b/APK/com.whatsapp.w4b?version=latest");

    /**
     * User-Agent sent with the APK download request to avoid being served
     * an HTML error page instead of the binary.
     */
    private static final String MOBILE_ANDROID_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36";

    /**
     * Cached instance for the consumer APK flavour, populated on first
     * access by {@link #ofPersonal()}.
     */
    private static volatile WhatsAppAndroidClientInfo personalApkInfo;

    /**
     * Monitor used to serialise initialisation of {@link #personalApkInfo}.
     */
    private static final Object personalApkInfoLock = new Object();

    /**
     * Cached instance for the business APK flavour, populated on first
     * access by {@link #ofBusiness()}.
     */
    private static volatile WhatsAppAndroidClientInfo businessApkInfo;

    /**
     * Monitor used to serialise initialisation of {@link #businessApkInfo}.
     */
    private static final Object businessApkInfoLock = new Object();

    /**
     * Application version read from the APK's {@code AndroidManifest.xml}
     * via {@link ClientAppVersion#of(String)}.
     */
    private final ClientAppVersion version;

    /**
     * MD5 hash of the APK's {@code classes.dex} entry.
     *
     * <p>This hash is fed into the registration token HMAC so the server can
     * verify that the caller knows the contents of a real signed DEX file.
     */
    private final byte[] md5Hash;

    /**
     * HMAC-SHA1 secret key derived via PBKDF2 from the package name and the
     * {@code about_logo.png} asset.
     */
    private final SecretKeySpec secretKey;

    /**
     * APK signing certificates, each in its raw X.509 DER form.
     *
     * <p>All certificates are folded into the registration token HMAC in
     * order so that the server can verify signature chain identity.
     */
    private final byte[][] certificates;

    /**
     * {@code true} when this instance represents the WhatsApp Business APK.
     */
    private final boolean business;

    /**
     * Constructs a new instance from the values extracted out of the APK.
     *
     * @param version the parsed application version
     * @param md5Hash the MD5 digest of {@code classes.dex}
     * @param secretKey the derived HMAC-SHA1 key
     * @param certificates the APK signing certificates in DER form
     * @param business whether this represents the business flavour
     */
    private WhatsAppAndroidClientInfo(ClientAppVersion version, byte[] md5Hash, SecretKeySpec secretKey, byte[][] certificates, boolean business) {
        this.version = version;
        this.md5Hash = md5Hash;
        this.secretKey = secretKey;
        this.certificates = certificates;
        this.business = business;
    }

    /**
     * Returns the cached consumer APK info, downloading and parsing the APK
     * on the first call.
     *
     * @return the consumer Android client info
     * @throws RuntimeException if the APK download or parsing fails
     */
    public static WhatsAppAndroidClientInfo ofPersonal() {
        if (personalApkInfo == null) {
            synchronized (personalApkInfoLock) {
                if(personalApkInfo == null) {
                    personalApkInfo = queryApkInfo(false);
                }
            }
        }
        return personalApkInfo;
    }

    /**
     * Returns the cached business APK info, downloading and parsing the APK
     * on the first call.
     *
     * @return the business Android client info
     * @throws RuntimeException if the APK download or parsing fails
     */
    public static WhatsAppAndroidClientInfo ofBusiness() {
        if (businessApkInfo == null) {
            synchronized (businessApkInfoLock) {
                if(businessApkInfo == null) {
                    businessApkInfo = queryApkInfo(true);
                }
            }
        }
        return businessApkInfo;
    }

    /**
     * Downloads the consumer or business APK and extracts the version,
     * {@code classes.dex} hash, signing certificates, and derived HMAC key.
     *
     * @param business {@code true} for the business flavour, {@code false}
     *                 for the consumer flavour
     * @return a populated {@code WhatsAppAndroidClientInfo} instance
     * @throws RuntimeException if the HTTP download, the APK parsing, or the
     *                          cryptographic derivation fails
     */
    private static WhatsAppAndroidClientInfo queryApkInfo(boolean business) {
        try(var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()) {
            var request = HttpRequest.newBuilder()
                    .uri(business ? MOBILE_BUSINESS_ANDROID_URL : MOBILE_PERSONAL_ANDROID_URL)
                    .GET()
                    .header("User-Agent", MOBILE_ANDROID_USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IOException("HTTP request failed with status code: " + response.statusCode());
            }

            try (var apkFile = new ByteArrayApkFile(response.body())) {
                // Parses AndroidManifest.xml to extract the advertised build version
                var version = ClientAppVersion.of(apkFile.getApkMeta().getVersionName());

                // Hashes the compiled Dalvik bytecode so the server can verify
                // the caller possesses the exact same classes.dex
                var digest = MessageDigest.getInstance("MD5");
                digest.update(apkFile.getFileData("classes.dex"));
                var md5Hash = digest.digest();

                // Derives the PBKDF2-HMAC-SHA1 key from the package name and the
                // about_logo.png asset, which together act as an application secret
                var secretKey = getSecretKey(apkFile.getApkMeta().getPackageName(), getAboutLogo(apkFile));

                // Extracts the APK signing certificates so each one can be folded
                // into the registration token HMAC
                var certificates = getCertificates(apkFile);
                return new WhatsAppAndroidClientInfo(version, md5Hash, secretKey, certificates, business);
            }
        } catch (IOException | GeneralSecurityException | InterruptedException exception) {
            throw new RuntimeException("Cannot extract data from APK", exception);
        }
    }

    /**
     * Returns the raw bytes of the {@code about_logo.png} asset from the
     * APK, tolerating the several density-bucket paths the image can live
     * under across WhatsApp releases.
     *
     * @param apkFile the parsed APK
     * @return the PNG bytes of the {@code about_logo} drawable
     * @throws IOException if reading the APK entry fails
     * @throws NoSuchElementException if none of the known paths contain the
     *                                asset
     */
    private static byte[] getAboutLogo(ByteArrayApkFile apkFile) throws IOException {
        // Looks up the drawable in the default hdpi bucket first
        var resource = apkFile.getFileData("res/drawable-hdpi/about_logo.png");
        if (resource != null) {
            return resource;
        }

        // Falls back to the hdpi-v4 API-gated bucket used by older releases
        var resourceV4 = apkFile.getFileData("res/drawable-hdpi-v4/about_logo.png");
        if (resourceV4 != null) {
            return resourceV4;
        }

        // Falls back to the xxhdpi-v4 bucket used by recent releases
        var xxResourceV4 = apkFile.getFileData("res/drawable-xxhdpi-v4/about_logo.png");
        if (xxResourceV4 != null) {
            return xxResourceV4;
        }

        throw new NoSuchElementException("Missing about_logo.png from apk");
    }

    /**
     * Extracts the raw DER bytes of every certificate that signs the APK.
     *
     * @param apkFile the parsed APK
     * @return a two-dimensional array with one certificate per row in DER
     *         form
     * @throws IOException if reading the signers metadata fails
     * @throws CertificateException if any certificate cannot be parsed
     */
    private static byte[][] getCertificates(ByteArrayApkFile apkFile) throws IOException, CertificateException {
        // Flattens the signer-to-certificates relationship into a single array
        return apkFile.getApkSingers()
                .stream()
                .map(ApkSigner::getCertificateMetas)
                .flatMap(Collection::stream)
                .map(CertificateMeta::getData)
                .toArray(byte[][]::new);
    }

    /**
     * Derives the HMAC-SHA1 secret key used to sign registration tokens by
     * running a manual PBKDF2-HMAC-SHA1 over the concatenation of the
     * package name and the {@code about_logo.png} bytes.
     *
     * <p>The implementation performs the PBKDF2 expansion in-line rather than
     * delegating to {@code javax.crypto.SecretKeyFactory} because the Android
     * original does the same and because it avoids loading the large default
     * PBKDF2 provider.
     *
     * @param packageName the APK package identifier used as the salt prefix
     * @param resource the raw {@code about_logo.png} bytes appended to the
     *                 package name to form the password
     * @return a {@code SecretKeySpec} wrapping the derived 64-byte key as
     *         {@code PBKDF2}
     * @throws IOException if IO fails (declared by contract, not thrown in
     *                     practice)
     * @throws GeneralSecurityException if HMAC-SHA1 cannot be instantiated
     */
    private static SecretKeySpec getSecretKey(String packageName, byte[] resource) throws IOException, GeneralSecurityException {
        // Composes the PBKDF2 password as packageName || about_logo.png bytes
        var packageBytes = packageName.getBytes(StandardCharsets.UTF_8);
        var password = new byte[packageBytes.length + resource.length];
        System.arraycopy(packageBytes, 0, password, 0, packageBytes.length);
        System.arraycopy(resource, 0, password, packageBytes.length, resource.length);

        // Initialises HMAC-SHA1 with the password as the HMAC key
        var mac = Mac.getInstance("HmacSHA1");
        var keySpec = new SecretKeySpec(password, mac.getAlgorithm());
        mac.init(keySpec);

        // Sets up the manual PBKDF2 expansion parameters used by the Android app
        var keySize = 64;
        var macLen = mac.getMacLength();
        var iterations = 128;
        var blocks = (keySize + macLen - 1) / macLen;

        var out = new byte[keySize];
        var state = new byte[macLen];
        var iBuf = new byte[4];

        var offset = 0;
        for (var block = 1; block <= blocks; ++block) {
            // Processes one PBKDF2 block: salt || INT(block) then iterate
            mac.update(MOBILE_ANDROID_SALT);

            iBuf[0] = (byte) (block >>> 24);
            iBuf[1] = (byte) (block >>> 16);
            iBuf[2] = (byte) (block >>> 8);
            iBuf[3] = (byte) (block);
            mac.update(iBuf, 0, iBuf.length);

            mac.doFinal(state, 0);

            var toCopy = Math.min(macLen, keySize - offset);
            System.arraycopy(state, 0, out, offset, toCopy);

            // XOR-folds the remaining iteration outputs into the block output
            for (var cnt = 1; cnt < iterations; ++cnt) {
                mac.update(state, 0, macLen);

                mac.doFinal(state, 0);

                for (var j = 0; j < toCopy; ++j) {
                    out[offset + j] ^= state[j];
                }
            }

            offset += toCopy;
        }

        return new SecretKeySpec(out, 0, out.length, "PBKDF2");
    }

    /**
     * Returns the application version extracted from the APK manifest.
     *
     * @return the parsed version
     */
    @Override
    public ClientAppVersion version() {
        return version;
    }

    /**
     * Returns whether this instance represents the WhatsApp Business APK.
     *
     * @return {@code true} for business, {@code false} for consumer
     */
    @Override
    public boolean business() {
        return business;
    }

    /**
     * Computes the Android registration token for the given national phone
     * number.
     *
     * <p>Feeds each APK signing certificate, followed by the {@code
     * classes.dex} MD5 hash and finally the decimal ASCII of the phone
     * number, into an HMAC-SHA1 keyed by the derived {@link #secretKey},
     * then Base64-encodes the result and URL-encodes the Base64. The
     * resulting string is suitable for direct inclusion as the {@code token}
     * form parameter of the mobile registration requests.
     *
     * @param nationalPhoneNumber the phone number in its national form,
     *                            without the country code
     * @return the URL-encoded Base64 HMAC
     * @throws InternalError if HMAC-SHA1 is not available (should not happen
     *                       on any JDK)
     */
    @Override
    public String computeRegistrationToken(long nationalPhoneNumber) {
        try {
            // Initialises HMAC-SHA1 with the derived secret as the key
            var mac = Mac.getInstance("HMACSHA1");
            mac.init(secretKey);

            // Feeds each signing certificate into the HMAC in order
            for (var certificate : certificates) {
                mac.update(certificate);
            }

            // Appends the classes.dex digest and the decimal phone number
            mac.update(md5Hash);
            mac.update(String.valueOf(nationalPhoneNumber).getBytes(StandardCharsets.UTF_8));
            return URLEncoder.encode(Base64.getEncoder().encodeToString(mac.doFinal()), StandardCharsets.UTF_8);
        }catch (GeneralSecurityException exception) {
            throw new InternalError("Cannot compute registration token", exception);
        }
    }
}
