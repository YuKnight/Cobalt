package com.github.auties00.cobalt.client.info;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.auties00.cobalt.model.device.pairing.ClientAppVersion;
import com.github.auties00.cobalt.util.PlayStoreUtils;
import net.dongliu.apk.parser.ByteArrayApkFile;
import net.dongliu.apk.parser.bean.ApkSigner;
import net.dongliu.apk.parser.bean.CertificateMeta;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.zip.ZipInputStream;

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
 * This class downloads the current APK through
 * {@link PlayStoreUtils#downloadApk(String)} (base + every split in the
 * Play Store App Bundle), extracts all of that material, and caches it so
 * the heavy download runs at most once per flavour per JVM.
 *
 * <p>Two flavours are supported: the consumer build
 * ({@code com.whatsapp}) and the business build ({@code com.whatsapp.w4b}).
 * Each flavour has its own lazily-initialised singleton protected by a
 * double-checked lock.
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
     * Known paths that the {@code about_logo.png} drawable has shipped
     * under across WhatsApp releases. The PBKDF2 password derivation
     * consumes the first matching entry; remaining paths are fallbacks
     * for older or differently-bucketed builds.
     */
    private static final List<String> ABOUT_LOGO_PATHS = List.of(
            "res/drawable-hdpi/about_logo.png",
            "res/drawable-hdpi-v4/about_logo.png",
            "res/drawable-xxhdpi-v4/about_logo.png"
    );

    /**
     * Play Store package identifier for the consumer WhatsApp APK.
     */
    private static final String PERSONAL_PACKAGE = "com.whatsapp";

    /**
     * Play Store package identifier for the WhatsApp Business APK.
     */
    private static final String BUSINESS_PACKAGE = "com.whatsapp.w4b";

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
     * Play Store {@code versionCode} the APK was fetched at, used purely
     * as the invalidation key for the on-disk JSON cache.
     */
    private final int versionCode;

    /**
     * Constructs a new instance from the values extracted out of the APK.
     *
     * @param version the parsed application version
     * @param versionCode the Play Store {@code versionCode} used as the
     *     cache invalidation key
     * @param md5Hash the MD5 digest of {@code classes.dex}
     * @param secretKey the derived HMAC-SHA1 key
     * @param certificates the APK signing certificates in DER form
     * @param business whether this represents the business flavour
     */
    private WhatsAppAndroidClientInfo(ClientAppVersion version, int versionCode, byte[] md5Hash, SecretKeySpec secretKey, byte[][] certificates, boolean business) {
        this.version = version;
        this.versionCode = versionCode;
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
     * Downloads the consumer or business APK through the anonymous Play
     * Store pipeline and extracts the version, {@code classes.dex} hash,
     * signing certificates, and derived HMAC key.
     *
     * <p>Play Store distributes modern WhatsApp builds as an App Bundle,
     * so the returned collection contains a base APK plus one or more
     * configuration splits. The base APK carries the manifest,
     * {@code classes.dex} and signing certificates and is fully
     * materialised into a {@link ByteArrayApkFile} because cert
     * extraction needs random access to the APK Signing Block at the
     * tail of the archive. Splits are only consulted for
     * {@code about_logo.png} when the base APK omits it, and are
     * stream-scanned with {@link ZipInputStream} so they never get
     * parsed by {@code apk-parser} nor fully held in memory. Any split
     * stream left unread is closed in the {@code finally} block so its
     * HTTP transfer is aborted.
     *
     * @param business {@code true} for the business flavour, {@code false}
     *                 for the consumer flavour
     * @return a populated {@code WhatsAppAndroidClientInfo} instance
     * @throws RuntimeException if the HTTP download, the APK parsing, or
     *                          the cryptographic derivation fails
     */
    private static WhatsAppAndroidClientInfo queryApkInfo(boolean business) {
        var packageName = business ? BUSINESS_PACKAGE : PERSONAL_PACKAGE;
        try {
            // Queries the Play Store catalogue for the latest versionCode
            // first so we can short-circuit straight to the on-disk cache
            // when nothing has changed since the previous run.
            var latest = PlayStoreUtils.latestVersion(packageName);
            var cached = loadCached(business);
            if (cached != null && cached.versionCode == latest.code()) {
                return cached;
            }

            // The cache is either missing or stale — download the current
            // APK through the already-resolved versionCode, extract the
            // required material, and persist a fresh cache for the next
            // JVM.
            try (var downloaded = PlayStoreUtils.downloadApk(packageName, latest.code());
                 var baseApk = new ByteArrayApkFile(downloaded.baseApk().readAllBytes())) {
                // Tries the base APK first — most builds ship about_logo.png there.
                var aboutLogo = findAboutLogoInBase(baseApk);
                if (aboutLogo == null) {
                    // Density-qualified drawables only ship in config.<density>dpi
                    // splits, never in native-ABI or locale splits. Close every
                    // non-density split eagerly so its HTTP transfer is aborted
                    // instead of spending tens of MB downloading native libs we
                    // will not scan, and scan the rest via ZipInputStream until
                    // one of the known paths is found.
                    for (var split : downloaded.splits().entrySet()) {
                        if (!isDensityConfigSplit(split.getKey())) {
                            try {
                                split.getValue().close();
                            } catch (IOException ignored) {
                            }
                            continue;
                        }
                        aboutLogo = findAboutLogoInSplit(split.getValue());
                        if (aboutLogo != null) {
                            break;
                        }
                    }
                }
                if (aboutLogo == null) {
                    throw new NoSuchElementException("Missing about_logo.png from apk");
                }

                // Parses AndroidManifest.xml to extract the advertised build version
                var version = ClientAppVersion.of(baseApk.getApkMeta().getVersionName());

                // Hashes the compiled Dalvik bytecode so the server can verify
                // the caller possesses the exact same classes.dex
                var digest = MessageDigest.getInstance("MD5");
                digest.update(baseApk.getFileData("classes.dex"));
                var md5Hash = digest.digest();

                // Derives the PBKDF2-HMAC-SHA1 key from the package name and the
                // about_logo.png asset, which together act as an application secret
                var secretKey = getSecretKey(baseApk.getApkMeta().getPackageName(), aboutLogo);

                // Extracts the APK signing certificates so each one can be folded
                // into the registration token HMAC
                var certificates = getCertificates(baseApk);

                var info = new WhatsAppAndroidClientInfo(version, latest.code(), md5Hash, secretKey, certificates, business);
                saveCached(info);
                return info;
            }
        } catch (IOException | GeneralSecurityException exception) {
            throw new RuntimeException("Cannot extract data from APK", exception);
        }
    }

    /**
     * Reads and decodes the on-disk JSON cache for the given flavour.
     * Any read or parse failure is swallowed and causes {@code null} to
     * be returned, triggering a fresh download.
     *
     * @param business whether this is the business flavour
     * @return the decoded client info, or {@code null} if the file is
     *     missing, unreadable, or malformed
     */
    private static WhatsAppAndroidClientInfo loadCached(boolean business) {
        var path = cacheFile(business);
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try (var in = Files.newInputStream(path)) {
            var json = JSON.parseObject(in);
            var decoder = Base64.getDecoder();
            var version = ClientAppVersion.of(json.getString("version"));
            var versionCode = json.getIntValue("versionCode");
            var md5Hash = decoder.decode(json.getString("md5Hash"));
            var secretKey = new SecretKeySpec(decoder.decode(json.getString("secretKey")), "PBKDF2");
            var certs = json.getJSONArray("certificates");
            var certificates = new byte[certs.size()][];
            for (var i = 0; i < certs.size(); i++) {
                certificates[i] = decoder.decode(certs.getString(i));
            }
            return new WhatsAppAndroidClientInfo(version, versionCode, md5Hash, secretKey, certificates, business);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    /**
     * Serialises {@code info} to JSON and writes it to the per-flavour
     * cache file, creating parent directories as needed. Byte fields are
     * emitted as Base64 strings so the payload stays plain JSON. Any
     * write failure is silently swallowed: the cache is a best-effort
     * optimisation and a failed write just means the next invocation
     * will re-fetch.
     *
     * @param info the client info to persist
     */
    private static void saveCached(WhatsAppAndroidClientInfo info) {
        try {
            var path = cacheFile(info.business);
            var parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            var encoder = Base64.getEncoder();
            var certificates = new JSONArray();
            for (var cert : info.certificates) {
                certificates.add(encoder.encodeToString(cert));
            }
            var json = new JSONObject();
            json.put("versionCode", info.versionCode);
            json.put("version", info.version.toString());
            json.put("md5Hash", encoder.encodeToString(info.md5Hash));
            json.put("secretKey", encoder.encodeToString(info.secretKey.getEncoded()));
            json.put("certificates", certificates);
            Files.writeString(path, json.toJSONString());
        } catch (IOException | RuntimeException ignored) {
            // Cache write failures are non-fatal; next call will re-fetch.
        }
    }

    /**
     * Resolves the on-disk path of the JSON cache file for the given
     * flavour, rooted at {@code $user.home/.cobalt/cache/}.
     *
     * @param business whether this is the business flavour
     * @return the cache file path
     */
    private static Path cacheFile(boolean business) {
        return Path.of(
                System.getProperty("user.home"),
                ".cobalt",
                "cache",
                "wa-android-" + (business ? "business" : "personal") + ".json"
        );
    }

    /**
     * Returns {@code true} when the given Play Store split identifier
     * designates a density-configuration split (e.g. {@code config.hdpi},
     * {@code config.xxhdpi}, {@code config.xxxhdpi}). Only these splits
     * can carry density-qualified drawables such as
     * {@code about_logo.png}; ABI splits ({@code config.arm64_v8a}, ...)
     * and locale splits ({@code config.en}, ...) never do.
     *
     * @param splitName the split identifier reported by the Play Store
     * @return {@code true} if the split can carry density-qualified
     *     drawables
     */
    private static boolean isDensityConfigSplit(String splitName) {
        return splitName != null && splitName.endsWith("dpi");
    }

    /**
     * Looks up each {@link #ABOUT_LOGO_PATHS} entry in the base APK and
     * returns the first match, or {@code null} if none are present.
     *
     * @param baseApk the materialised base APK
     * @return the raw PNG bytes, or {@code null} when the base APK does
     *     not carry the drawable
     * @throws IOException if reading an APK entry fails
     */
    private static byte[] findAboutLogoInBase(ByteArrayApkFile baseApk) throws IOException {
        for (var path : ABOUT_LOGO_PATHS) {
            var data = baseApk.getFileData(path);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    /**
     * Stream-scans a split APK for an {@code about_logo.png} entry under
     * any of the known {@link #ABOUT_LOGO_PATHS}, returning the first
     * match. Uses {@link ZipInputStream} rather than {@link ByteArrayApkFile}
     * so the caller never has to hold the full split in memory and can
     * abort the underlying HTTP transfer as soon as the entry is located.
     *
     * @param stream the split's response body stream
     * @return the raw PNG bytes, or {@code null} when the split does not
     *     carry any of the candidate paths
     * @throws IOException if reading the stream or an entry's decompressed
     *     bytes fails
     */
    private static byte[] findAboutLogoInSplit(InputStream stream) throws IOException {
        try (var zip = new ZipInputStream(stream)) {
            var entry = zip.getNextEntry();
            while (entry != null) {
                if (ABOUT_LOGO_PATHS.contains(entry.getName())) {
                    return zip.readAllBytes();
                }
                entry = zip.getNextEntry();
            }
            return null;
        }
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
