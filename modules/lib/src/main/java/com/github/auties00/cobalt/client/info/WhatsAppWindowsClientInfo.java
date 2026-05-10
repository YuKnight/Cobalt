package com.github.auties00.cobalt.client.info;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.meta.model.WhatsAppWebPlatform;
import com.github.auties00.cobalt.model.device.pairing.ClientAppVersion;
import com.github.auties00.cobalt.model.device.pairing.ClientAppVersionBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Represents the build identity of the WhatsApp Desktop for Windows client
 * and resolves it by combining the web JavaScript bundle version with the
 * Microsoft Store package version of the native hybrid shell.
 *
 * <p>The real Windows shell loads the same JavaScript bundle that
 * {@code web.whatsapp.com} serves and injects its own store build number
 * into the bundle as the {@code ?windowsBuild=XXXXXX} URL parameter. The
 * bundle reads that parameter in {@code WAWebBuildConstants} and exposes it
 * as {@code WINDOWS_BUILD}; {@code WAWebClientPayload} then folds it into
 * the handshake {@code appVersion} as the {@code quaternary} component (and
 * overrides {@code mcc}/{@code mnc} from its halves when it is six digits
 * long).
 *
 * <p>Because Cobalt is not running inside the hybrid shell it does not
 * receive that URL parameter, so this class discovers a plausible value by
 * querying the public Microsoft Store display catalog for the
 * {@code 5319275A.WhatsAppDesktop} package and extracting the package
 * {@code Version} string (for example {@code "2.2525.6.0"}). The middle
 * two components are collapsed into a six digit integer
 * ({@code secondary * 100 + tertiary}) that mirrors the format the hybrid
 * shell emits. When the network query fails for any reason the class falls
 * back to a hardcoded plausible build so that pairing can still proceed
 * offline or on hosts where outbound access to Microsoft Store is blocked.
 *
 * <p>The {@link ClientAppVersion} returned by {@link #version()} therefore
 * has its {@code primary}, {@code secondary} and {@code tertiary} taken from
 * {@link WhatsAppWebClientInfo} (the web bundle the shell hosts) and its
 * {@code quaternary} set to the Windows store build integer.
 *
 * @apiNote The real shell reads its own build number at runtime through
 *          native APIs and passes it through the URL parameter contract
 *          described above. Cobalt approximates that path from the outside
 *          using the public Microsoft Store metadata.
 * @see WhatsAppClientInfo
 * @see WhatsAppWebClientInfo
 */
@WhatsAppWebModule(moduleName = "WAWebBuildConstants", platform = WhatsAppWebPlatform.WINDOWS)
final class WhatsAppWindowsClientInfo implements WhatsAppClientInfo {
    /**
     * Holds the cached singleton instance of the resolved Windows client
     * info.
     *
     * <p>Populated lazily on the first call to {@link #of()} and protected
     * by {@link #windowsInfoLock} using the double checked locking idiom.
     */
    private static volatile WhatsAppWindowsClientInfo windowsInfo;

    /**
     * Holds the monitor used to serialise initialisation of
     * {@link #windowsInfo}.
     */
    private static final Object windowsInfoLock = new Object();

    /**
     * Holds the Microsoft Store display catalog endpoint for the
     * {@code 9NKSQGP7F2NH} product, which corresponds to the
     * {@code 5319275A.WhatsAppDesktop} UWP package.
     *
     * <p>The response is a JSON document whose {@code Packages[].Version}
     * fields carry the shipping build string.
     */
    private static final URI WINDOWS_STORE_URL = URI.create(
            "https://displaycatalog.mp.microsoft.com/v7.0/products/9NKSQGP7F2NH?languages=en-us&market=US&fieldsTemplate=Details"
    );

    /**
     * Holds the JSON marker that precedes the full package name in the
     * Microsoft Store display catalog response, for example
     * {@code "PackageFullName":"5319275A.WhatsAppDesktop_2.2613.101.0_neutral_~_cv1g1gvanyjgm"}.
     *
     * <p>The marker is scoped to the {@code PackageFullName} key so that
     * the preceding {@code PackageFamilyName} entry
     * ({@code "5319275A.WhatsAppDesktop_cv1g1gvanyjgm"}, the family hash
     * with no version) is skipped. The next underscore after the prefix
     * then reliably terminates the dotted version substring.
     */
    private static final String PACKAGE_MARKER = "\"PackageFullName\":\"5319275A.WhatsAppDesktop_";

    /**
     * Holds the fallback build number used when the Microsoft Store query
     * fails.
     */
    private static final int DEFAULT_WINDOWS_BUILD = 261301;

    /**
     * Holds the resolved application version advertised by this info
     * instance.
     */
    private final ClientAppVersion version;

    /**
     * Constructs a new immutable instance with the given resolved version.
     *
     * @param version the version combining the web base with the Windows
     *                store build number in {@code quaternary}
     */
    private WhatsAppWindowsClientInfo(ClientAppVersion version) {
        this.version = version;
    }

    /**
     * Returns the cached Windows client info, performing the store query on
     * the first call.
     *
     * <p>Subsequent invocations within the same JVM return the same
     * instance. If the initial query fails with a runtime exception the
     * failure is not cached and the next call will retry.
     *
     * @return the resolved Windows client info
     */
    public static WhatsAppWindowsClientInfo of() {
        if (windowsInfo == null) {
            synchronized (windowsInfoLock) {
                if (windowsInfo == null) {
                    windowsInfo = queryWindowsInfo();
                }
            }
        }
        return windowsInfo;
    }

    /**
     * Builds the resolved Windows client info from the cached web version
     * and the current Microsoft Store build number.
     *
     * <p>Any failure in the Microsoft Store query is swallowed and the
     * {@link #DEFAULT_WINDOWS_BUILD} fallback is used so that a Cobalt host
     * without outbound access to Microsoft Store can still pair.
     *
     * @return a fresh info instance with a version of the form
     *         {@code 2.3000.X.WINDOWS_BUILD}
     */
    private static WhatsAppWindowsClientInfo queryWindowsInfo() {
        var webVersion = WhatsAppWebClientInfo.of().version();
        var primary = webVersion.primary();
        var secondary = webVersion.secondary();
        var tertiary = webVersion.tertiary();
        var version = new ClientAppVersionBuilder()
                .primary(primary.isPresent() ? primary.getAsInt() : null)
                .secondary(secondary.isPresent() ? secondary.getAsInt() : null)
                .tertiary(tertiary.isPresent() ? tertiary.getAsInt() : null)
                .quaternary(queryWindowsBuild())
                .build();
        return new WhatsAppWindowsClientInfo(version);
    }

    /**
     * Queries the Microsoft Store display catalog for the current
     * {@code 5319275A.WhatsAppDesktop} package version and converts it to
     * the six digit build integer the hybrid shell injects as
     * {@code windowsBuild}.
     *
     * <p>The Microsoft Store returns the full package name in the form
     * {@code 5319275A.WhatsAppDesktop_2.2613.101.0_x64__cv1g1gvanyjgm}. The
     * shipping hybrid shell folds this into
     * {@code secondary * 100 + (tertiary % 100)}: the tertiary component
     * encodes a {@code channel}{@code build} pair where the leading digit
     * is the release channel ({@code 1} for {@code RELEASE}) and the
     * trailing two digits are the build counter. For
     * {@code 2.2613.101.0} this yields {@code 2613 * 100 + 1 = 261301},
     * matching the value observed on the wire from the real
     * {@code WhatsApp.Root.dll} shell.
     *
     * <p>Any failure (network error, missing marker, malformed response)
     * returns {@link #DEFAULT_WINDOWS_BUILD}.
     *
     * @return the resolved six digit build number, or the fallback constant
     *         when discovery fails
     */
    @WhatsAppWebExport(moduleName = "WAWebBuildConstants", exports = "WINDOWS_BUILD", platform = WhatsAppWebPlatform.WINDOWS, adaptation = WhatsAppAdaptation.ADAPTED)
    private static int queryWindowsBuild() {
        try (var httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()) {
            var request = HttpRequest.newBuilder()
                    .uri(WINDOWS_STORE_URL)
                    .GET()
                    .header("Accept", "application/json")
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return DEFAULT_WINDOWS_BUILD;
            }
            var body = response.body();
            var markerStart = body.indexOf(PACKAGE_MARKER);
            if (markerStart < 0) {
                return DEFAULT_WINDOWS_BUILD;
            }
            var versionStart = markerStart + PACKAGE_MARKER.length();
            var versionEnd = body.indexOf('_', versionStart);
            if (versionEnd < 0) {
                return DEFAULT_WINDOWS_BUILD;
            }
            var versionStr = body.substring(versionStart, versionEnd);
            var parts = versionStr.split("\\.");
            if (parts.length < 3) {
                return DEFAULT_WINDOWS_BUILD;
            }
            var secondary = Integer.parseInt(parts[1]);
            var tertiary = Integer.parseInt(parts[2]);
            var build = secondary * 100 + (tertiary % 100);
            if (build < 100000 || build > 999999) {
                return DEFAULT_WINDOWS_BUILD;
            }
            return build;
        } catch (IOException | InterruptedException | NumberFormatException exception) {
            return DEFAULT_WINDOWS_BUILD;
        }
    }

    /**
     * Returns the WhatsApp Desktop for Windows client application version,
     * combining the web JavaScript bundle version with the Microsoft Store
     * package build number in the {@code quaternary} slot.
     *
     * @return the resolved version
     */
    @Override
    @WhatsAppWebExport(moduleName = "WAWebBuildConstants", exports = "VERSION_BASE_WITH_WINDOWS_BUILD", platform = WhatsAppWebPlatform.WINDOWS, adaptation = WhatsAppAdaptation.ADAPTED)
    public ClientAppVersion version() {
        return version;
    }
}
