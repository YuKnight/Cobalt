package com.github.auties00.cobalt.registration.push.fcm;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

/**
 * Immutable settings that identify the Firebase project and Android
 * application for which {@link FcmClient} is registering.
 *
 * <p>The default values come from the Telegram Android client and are
 * suitable for any caller that just wants to receive arbitrary FCM
 * pushes (the project itself does not have to be the caller's). Pass a
 * custom instance via {@link FcmClient#newSession(FcmConfig)} to
 * impersonate a different Android app.
 *
 * <p>Stored inside {@link FcmSession} so a serialized session round-trips
 * its configuration along with the credentials.
 */
@ProtobufMessage(name = "FcmConfig")
public final class FcmConfig {
    /**
     * Configuration for WhatsApp's consumer Android app
     * ({@code com.whatsapp}). Values mirror the
     * {@code google-services.json} bundled with the Play Store APK.
     */
    public static final FcmConfig WHATSAPP_PERSONAL = new FcmConfig(
            "whatsapp-messenger",
            "1:293955441834:android:7373a2d0bdfa3228",
            "AIzaSyCGOJbGQ95SWrXxl8wk-_cRQZcJl42bvDU",
            "293955441834",
            "com.whatsapp",
            "38a0f7d505fe18fec64fbf343ecaaaf310dbd799",
            true);

    /**
     * Configuration for WhatsApp Business' Android app
     * ({@code com.whatsapp.w4b}). Verified against the consumer
     * {@code base.apk}: every Firebase resource ({@code project_id},
     * {@code google_app_id}, {@code google_api_key},
     * {@code gcm_defaultSenderId}) is identical to
     * {@link #WHATSAPP_PERSONAL}, and both APKs are signed with the
     * same WhatsApp Inc. certificate. Only the package name differs.
     */
    public static final FcmConfig WHATSAPP_BUSINESS = new FcmConfig(
            "whatsapp-messenger",
            "1:293955441834:android:7373a2d0bdfa3228",
            "AIzaSyCGOJbGQ95SWrXxl8wk-_cRQZcJl42bvDU",
            "293955441834",
            "com.whatsapp.w4b",
            "38a0f7d505fe18fec64fbf343ecaaaf310dbd799",
            true);

    /**
     * Firebase project id, e.g. {@code "tmessages2"}. Used as the path
     * segment of the FIS endpoint.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.STRING)
    String projectId;

    /**
     * Firebase application id, e.g.
     * {@code "1:760348033671:android:f6afd7b67eae3860"}. Sent as the
     * {@code appId} field on the FIS request and as the
     * {@code X-gmp_app_id} header on the GCM register3 call.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    String appId;

    /**
     * Firebase API key, used as the {@code x-goog-api-key} header on
     * the FIS request.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    String apiKey;

    /**
     * GCM sender id (numeric, also called the Firebase project number),
     * used as both the {@code sender} form field and the
     * {@code X-subtype}/{@code X-subscription} headers on the GCM
     * register3 call.
     */
    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    String senderId;

    /**
     * Android package name to impersonate, e.g.
     * {@code "org.telegram.messenger"}. Sent as the {@code app} form
     * field and the {@code X-Android-Package} header.
     */
    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    String packageName;

    /**
     * Hex-encoded SHA-1 of the Android application's signing
     * certificate, sent as the {@code cert} form field and the
     * {@code X-Android-Cert} header. Spaces and colons are stripped at
     * encode time; case is normalised per call site.
     */
    @ProtobufProperty(index = 6, type = ProtobufType.STRING)
    String certSha1;

    /**
     * Whether to perform the Firebase Installations (FIS) step before
     * the GCM register3 call. Modern Firebase-backed apps need it; some
     * legacy pre-Firebase projects (registered against the bare GCM
     * endpoint) must skip it. {@code true} is the right default for
     * any project set up after 2019.
     */
    @ProtobufProperty(index = 7, type = ProtobufType.BOOL)
    boolean useFis;

    /**
     * Constructs a new immutable config with the given values.
     *
     * @param projectId   the Firebase project id
     * @param appId       the Firebase app id
     * @param apiKey      the Firebase API key
     * @param senderId    the GCM sender id (numeric)
     * @param packageName the Android package name
     * @param certSha1    hex-encoded SHA-1 of the signing certificate
     * @param useFis      whether to call the FIS endpoint
     */
    FcmConfig(String projectId, String appId, String apiKey, String senderId,
              String packageName, String certSha1, boolean useFis) {
        this.projectId = projectId;
        this.appId = appId;
        this.apiKey = apiKey;
        this.senderId = senderId;
        this.packageName = packageName;
        this.certSha1 = certSha1;
        this.useFis = useFis;
    }

    /**
     * @return the Firebase project id
     */
    public String projectId() {
        return projectId;
    }

    /**
     * @return the Firebase app id
     */
    public String appId() {
        return appId;
    }

    /**
     * @return the Firebase API key
     */
    public String apiKey() {
        return apiKey;
    }

    /**
     * @return the GCM sender id
     */
    public String senderId() {
        return senderId;
    }

    /**
     * @return the Android package name
     */
    public String packageName() {
        return packageName;
    }

    /**
     * @return hex-encoded SHA-1 of the signing certificate
     */
    public String certSha1() {
        return certSha1;
    }

    /**
     * @return whether to call the FIS endpoint
     */
    public boolean useFis() {
        return useFis;
    }
}
