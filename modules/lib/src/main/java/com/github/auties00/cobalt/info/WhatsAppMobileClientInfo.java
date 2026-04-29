package com.github.auties00.cobalt.info;

import com.github.auties00.cobalt.model.device.pairing.ClientPlatformType;

/**
 * Provides mobile-specific client metadata that the native WhatsApp
 * registration protocol requires.
 *
 * <p>The mobile registration protocol exposed at
 * {@code https://v.whatsapp.net/v2} is the channel by which a real Android or
 * iOS app claims ownership of a phone number, requests an SMS or voice
 * verification code, and finally submits the received code to obtain a
 * registered Signal identity. Every call in that protocol is authenticated by
 * an HMAC-based registration token computed from the app's signing material,
 * the phone number, and the advertised client version. This interface
 * captures the two pieces of metadata that Cobalt needs to faithfully
 * impersonate such a mobile client:
 * <ol>
 *   <li>whether the client is the consumer ({@code WhatsApp}) or business
 *       ({@code WhatsApp Business}) edition, which changes server-side
 *       feature gating and some protocol constants,</li>
 *   <li>how to compute the per-phone-number registration token, which is
 *       platform-dependent: Android uses a PBKDF2-derived HMAC over the APK
 *       signing certificates, while iOS uses an MD5 of a static secret
 *       concatenated with the build hash.</li>
 * </ol>
 *
 * <p>Concrete implementations are downloaded and cached lazily via
 * {@link WhatsAppAndroidClientInfo#ofPersonal()} / {@code ofBusiness()} and
 * {@link WhatsAppIosClientInfo#ofPersonal()} / {@code ofBusiness()}.
 *
 * @apiNote The mobile registration protocol is implemented by the native
 *          Android / iOS WhatsApp applications, not by WhatsApp Web. There is
 *          therefore no WA Web module that this interface adapts; it exists
 *          solely so that Cobalt can drive the mobile registration flow from
 *          Java without embedding the proprietary binaries.
 * @see WhatsAppAndroidClientInfo
 * @see WhatsAppIosClientInfo
 */
public sealed interface WhatsAppMobileClientInfo
        extends WhatsAppClientInfo
        permits WhatsAppAndroidClientInfo, WhatsAppIosClientInfo {
    /**
     * Returns the mobile client info corresponding to the given platform.
     *
     * <p>Unlike {@link WhatsAppClientInfo#of(ClientPlatformType)}, this
     * accessor is restricted to mobile platforms: passing a web or desktop
     * platform throws, because those platforms do not have a native
     * registration protocol.
     *
     * @param platform the mobile platform to resolve
     * @return a cached {@code WhatsAppMobileClientInfo} for the platform and
     *         flavour
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
     * signing certificates, and in the iOS case a different static secret in
     * the registration token derivation. The mobile registration and pairing
     * layers inspect this flag to branch on business-specific fields such as
     * the verified-name certificate.
     *
     * @return {@code true} for a business flavour, {@code false} for
     *         consumer
     */
    boolean business();

    /**
     * Computes the per-phone-number registration token expected by the
     * mobile {@code /exist}, {@code /code}, and {@code /register} endpoints.
     *
     * <p>The algorithm differs by platform:
     * <ul>
     *   <li>On Android an HMAC-SHA1 is computed over the APK signing
     *       certificates, the MD5 hash of {@code classes.dex}, and the
     *       national phone number, keyed by a PBKDF2-derived key from the
     *       package name plus the {@code about_logo.png} asset.</li>
     *   <li>On iOS an MD5 hash is computed over a static secret (which
     *       differs between consumer and business builds) concatenated with
     *       the hex-encoded build hash and the national phone number.</li>
     * </ul>
     * The resulting bytes are URL-encoded so that the caller can drop them
     * directly into the form-encoded registration request body.
     *
     * @param nationalPhoneNumber the phone number in its national form,
     *                            without country code
     * @return the URL-encoded registration token
     */
    String computeRegistrationToken(long nationalPhoneNumber);
}
