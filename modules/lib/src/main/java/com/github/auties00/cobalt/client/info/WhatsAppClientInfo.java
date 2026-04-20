package com.github.auties00.cobalt.client.info;

import com.github.auties00.cobalt.model.device.pairing.ClientAppVersion;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;

/**
 * Provides the advertised client application version that Cobalt reports to
 * WhatsApp servers when opening a connection.
 *
 * <p>Every WhatsApp client, whether web, desktop or mobile, must announce its
 * build version during the Noise handshake and throughout various registration
 * calls. Servers compare the declared version against their supported range
 * and can refuse sessions that look outdated, so advertising an up-to-date
 * version is essential for Cobalt to establish and maintain sessions. This
 * sealed interface exposes the minimum information required for that
 * advertisement: a {@link ClientAppVersion}. The mobile sub-interface
 * additionally contributes business-vs-personal branding and a per-platform
 * registration token computation.
 *
 * <p>Instances are produced via static factories that scrape the latest public
 * build metadata from the appropriate distribution channel:
 * <ul>
 *   <li>{@link WhatsAppWebClientInfo} fetches {@code web.whatsapp.com}'s
 *       landing page and extracts the {@code client_revision} field.</li>
 *   <li>{@link WhatsAppWindowsClientInfo} combines the web version with the
 *       Microsoft Store package build of the native hybrid shell so that the
 *       handshake {@code appVersion} carries a realistic
 *       {@code quaternary} component, the way the real
 *       {@code WAWebBuildConstants.WINDOWS_BUILD} URL parameter does.</li>
 *   <li>{@link WhatsAppMobileClientInfo} resolves to either an Android APK
 *       ({@code WhatsApp.apk}) or iOS bundle ({@code net.whatsapp.WhatsApp}
 *       lookup on the App Store) and reads the version, signing material
 *       and executable hashes needed for the native registration protocol.</li>
 * </ul>
 *
 * <p>Results are cached per platform-and-flavour behind a double-checked lock
 * so that a Cobalt process only performs one network round trip per unique
 * client flavour.
 *
 * @apiNote This interface has no direct WA Web counterpart. WA Web hardcodes
 *          its build constants at compile time in {@code WAWebBuildConstants}
 *          ({@code VERSION_PRIMARY}, {@code VERSION_SECONDARY},
 *          {@code VERSION_TERTIARY} read from {@code SiteData.client_revision}).
 *          Because Cobalt is not shipped as part of a WhatsApp release it has
 *          to discover the current version dynamically, which is why this
 *          interface and its implementations exist outside the WA Web module
 *          hierarchy.
 * @see ClientAppVersion
 * @see ClientPlatformType
 */
public sealed interface WhatsAppClientInfo
        permits WhatsAppWebClientInfo, WhatsAppWindowsClientInfo, WhatsAppMobileClientInfo {
    /**
     * Returns the appropriate {@code WhatsAppClientInfo} implementation for
     * the given platform, using the personal-app flavour for mobile platforms
     * and the web flavour for desktop targets.
     *
     * <p>Mobile business variants ({@code ANDROID_BUSINESS},
     * {@code IOS_BUSINESS}) return the dedicated business APK/IPA flavour so
     * that the advertised version and registration token match a real
     * WhatsApp Business build. The macOS desktop target shares the web
     * flavour because the Mac Catalyst build embeds the same JavaScript
     * bundle as {@code web.whatsapp.com} and advertises no additional
     * build number on the wire. The Windows desktop target has its own
     * {@link WhatsAppWindowsClientInfo} so that the handshake
     * {@code appVersion} carries the Windows store build number in
     * {@code quaternary}, matching the {@code WINDOWS_BUILD} URL
     * parameter injected by the real hybrid shell.
     *
     * @param platform the target client platform
     * @return a cached {@code WhatsAppClientInfo} whose version matches the
     *         latest published build for that platform
     * @throws IllegalStateException if {@code platform} does not correspond to
     *                               any known Cobalt client flavour
     */
    static WhatsAppClientInfo of(ClientPlatformType platform) {
        return switch (platform) {
            case ANDROID -> WhatsAppAndroidClientInfo.ofPersonal();
            case IOS -> WhatsAppIosClientInfo.ofPersonal();
            case ANDROID_BUSINESS -> WhatsAppAndroidClientInfo.ofBusiness();
            case IOS_BUSINESS -> WhatsAppIosClientInfo.ofBusiness();
            case WINDOWS -> WhatsAppWindowsClientInfo.of();
            case MACOS, WEB -> WhatsAppWebClientInfo.of();
            default -> throw new IllegalStateException("Unexpected value: " + platform);
        };
    }

    /**
     * Returns the advertised application version for this client flavour.
     *
     * <p>This is the value Cobalt places into the handshake client payload's
     * {@code userAgent.appVersion} field and, for mobile clients, also into
     * the MD5-based build hash used by the registration token algorithm.
     *
     * @return the client application version
     */
    ClientAppVersion version();
}
