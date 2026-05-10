package com.github.auties00.cobalt.client.info;

import com.github.auties00.cobalt.model.device.pairing.ClientAppVersion;
import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;

/**
 * Represents the public identity of the WhatsApp client that Cobalt impersonates
 * when it opens a connection to WhatsApp servers.
 *
 * <p>Every WhatsApp client (web, desktop or mobile) must announce a build
 * version and a platform during the Noise handshake and throughout registration.
 * Servers compare the declared build against the supported range and may
 * refuse sessions whose advertised version looks outdated, so providing a
 * truthful and up to date {@code WhatsAppClientInfo} is essential for Cobalt
 * to establish and keep a session alive.
 *
 * <p>This sealed root exposes the minimum data every flavour needs to publish,
 * namely a {@link ClientAppVersion}. The mobile sub interface
 * {@link WhatsAppMobileClientInfo} additionally contributes a
 * business-versus-personal flag and a per platform registration token
 * computation.
 *
 * <p>Concrete implementations are produced by static factories that scrape the
 * latest published build metadata from the appropriate distribution channel.
 * {@link WhatsAppWebClientInfo} reads the {@code client_revision} field
 * embedded in the {@code web.whatsapp.com} landing page,
 * {@link WhatsAppWindowsClientInfo} combines that web revision with the
 * Microsoft Store package build of the native Windows shell, and
 * {@link WhatsAppMobileClientInfo} resolves to either an Android APK
 * downloaded from the Play Store or to the iOS bundle reported by the App
 * Store lookup API. Results are cached per platform-and-flavour behind a
 * double checked lock so a Cobalt process performs at most one network round
 * trip per unique client flavour.
 *
 * @apiNote This interface has no direct WhatsApp Web counterpart. WhatsApp
 *          Web hardcodes its build constants at compile time inside
 *          {@code WAWebBuildConstants} ({@code VERSION_PRIMARY},
 *          {@code VERSION_SECONDARY}, {@code VERSION_TERTIARY} read from
 *          {@code SiteData.client_revision}). Because Cobalt is not shipped
 *          as part of a WhatsApp release it must discover the running version
 *          dynamically, which is why this interface and its implementations
 *          live outside the WhatsApp Web module hierarchy.
 * @see ClientAppVersion
 * @see ClientPlatformType
 */
public sealed interface WhatsAppClientInfo
        permits WhatsAppWebClientInfo, WhatsAppWindowsClientInfo, WhatsAppMobileClientInfo {
    /**
     * Returns the {@code WhatsAppClientInfo} implementation that matches the
     * given client platform.
     *
     * <p>Mobile platforms resolve to the consumer or business APK or IPA
     * flavour, the Windows desktop platform resolves to
     * {@link WhatsAppWindowsClientInfo} so its handshake carries the Microsoft
     * Store build number in the {@code quaternary} version slot, and the
     * macOS and web platforms share {@link WhatsAppWebClientInfo} because
     * the macOS desktop client is a Mac Catalyst port that loads the same
     * JavaScript bundle as {@code web.whatsapp.com}.
     *
     * @param platform the target client platform
     * @return a cached {@code WhatsAppClientInfo} whose advertised version
     *         matches the latest published build for that platform
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
     * Returns the application version that this client flavour advertises to
     * WhatsApp servers.
     *
     * <p>The returned value is placed into the handshake client payload's
     * {@code userAgent.appVersion} field and, on mobile flavours, also feeds
     * the MD5 build hash consumed by the registration token algorithm.
     *
     * @return the advertised client application version
     */
    ClientAppVersion version();
}
