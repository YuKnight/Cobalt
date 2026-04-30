package com.github.auties00.cobalt.info;

import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;

/**
 * Represents the public identity of a native mobile WhatsApp client (Android
 * or iOS) that Cobalt impersonates while running the mobile registration
 * protocol.
 *
 * <p>The mobile registration protocol exposed at
 * {@code https://v.whatsapp.net/v2} is the channel by which a real Android
 * or iOS app claims ownership of a phone number, requests an SMS or voice
 * verification code, and finally submits the received code to obtain a
 * registered Signal identity. Every call in that protocol is authenticated
 * by a registration token computed from the app's signing material, the
 * phone number, and the advertised client version.
 *
 * <p>This interface captures the two pieces of metadata Cobalt needs in
 * order to faithfully impersonate such a mobile client. It first records
 * whether the client is the consumer ({@code WhatsApp}) or business
 * ({@code WhatsApp Business}) edition, which changes server side feature
 * gating and a few protocol constants. It also defines how the per phone
 * number registration token is computed: Android derives an HMAC over the
 * APK signing certificates with a PBKDF2 derived key, while iOS computes
 * an MD5 of a static secret concatenated with the build hash.
 *
 * <p>Concrete implementations are downloaded and cached lazily through
 * {@link WhatsAppAndroidClientInfo#ofPersonal()} and
 * {@link WhatsAppAndroidClientInfo#ofBusiness()}, and through the matching
 * iOS factories on {@link WhatsAppIosClientInfo}.
 *
 * @apiNote The mobile registration protocol is implemented by the native
 *          Android and iOS WhatsApp applications, not by WhatsApp Web. There
 *          is therefore no WhatsApp Web module that this interface adapts;
 *          it exists solely so that Cobalt can drive the mobile registration
 *          flow from Java without embedding the proprietary binaries.
 * @see WhatsAppAndroidClientInfo
 * @see WhatsAppIosClientInfo
 */
public sealed interface WhatsAppMobileClientInfo
        extends WhatsAppClientInfo
        permits WhatsAppAndroidClientInfo, WhatsAppIosClientInfo {
    /**
     * Returns the {@code WhatsAppMobileClientInfo} implementation that matches
     * the given mobile platform.
     *
     * <p>Unlike {@link WhatsAppClientInfo#of(ClientPlatformType)}, this
     * accessor is restricted to the four mobile platforms because web and
     * desktop clients do not participate in the native registration protocol.
     *
     * @param platform the target mobile platform
     * @return a cached {@code WhatsAppMobileClientInfo} for the requested
     *         platform and flavour
     * @throws IllegalStateException if {@code platform} is not one of the
     *                               four supported mobile platforms
     */
    static WhatsAppMobileClientInfo of(ClientPlatformType platform) {
        return switch (platform) {
            case ANDROID -> WhatsAppAndroidClientInfo.ofPersonal();
            case IOS -> WhatsAppIosClientInfo.ofPersonal();
            case ANDROID_BUSINESS -> WhatsAppAndroidClientInfo.ofBusiness();
            case IOS_BUSINESS -> WhatsAppIosClientInfo.ofBusiness();
            default -> throw new IllegalStateException("Unexpected value: " + platform);
        };
    }

    /**
     * Returns whether this client info represents a WhatsApp Business build
     * rather than the consumer WhatsApp build.
     *
     * <p>Business flavours use a different package identifier, different
     * signing certificates, and on iOS a different static secret in the
     * registration token derivation. The mobile registration and pairing
     * layers inspect this flag to branch on business specific fields such
     * as the verified name certificate.
     *
     * @return {@code true} for a business flavour, {@code false} for the
     *         consumer flavour
     */
    boolean business();

    /**
     * Computes the per phone number registration token expected by the
     * mobile {@code /exist}, {@code /code} and {@code /register} endpoints.
     *
     * <p>The algorithm differs by platform. On Android an HMAC-SHA1 is
     * computed over the APK signing certificates, the MD5 hash of
     * {@code classes.dex} and the national phone number, keyed by a PBKDF2
     * derived key from the package name plus the {@code about_logo.png}
     * asset. On iOS an MD5 hash is computed over a static secret (which
     * differs between consumer and business builds) concatenated with the
     * hex encoded build hash and the national phone number. The resulting
     * bytes are URL encoded so callers can drop them directly into the
     * form encoded registration request body.
     *
     * @param nationalPhoneNumber the phone number in its national form,
     *                            without the country code
     * @return the URL encoded registration token
     */
    String computeRegistrationToken(long nationalPhoneNumber);
}
