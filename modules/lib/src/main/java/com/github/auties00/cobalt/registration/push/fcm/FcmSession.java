package com.github.auties00.cobalt.registration.push.fcm;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.ArrayList;
import java.util.List;

/**
 * Full serializable state of an {@link FcmClient}: the immutable
 * {@link FcmConfig} plus every credential and stream cursor accumulated
 * across the registration pipeline and the live MCS stream.
 *
 * <p>This is the only class the caller needs to round-trip via
 * {@link FcmClient#getSession()} /
 * {@link FcmClient#loadSession(FcmSession)} to keep the same FCM token
 * across process restarts.
 */
@ProtobufMessage(name = "FcmSession")
public final class FcmSession {
    /**
     * The configuration the session was created with. Bundled in the
     * serialized output so a saved session loads back without the caller
     * having to remember which {@link FcmConfig} it was originally
     * created against.
     */
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    FcmConfig config;

    /**
     * Server-assigned Android device id from the {@code /checkin} step.
     * Becomes the username on the MCS login. {@code 0} means no checkin
     * has been performed yet.
     */
    @ProtobufProperty(index = 2, type = ProtobufType.UINT64)
    long androidId;

    /**
     * Server-assigned security token paired with {@link #androidId}.
     * Becomes the password on the MCS login. {@code 0} means no checkin
     * has been performed yet.
     */
    @ProtobufProperty(index = 3, type = ProtobufType.UINT64)
    long securityToken;

    /**
     * Firebase Installation ID returned by the FIS endpoint, sent as
     * the {@code X-appid} header on GCM register3. Empty when the FIS
     * step has not been performed (or when {@link FcmConfig#useFis()}
     * is {@code false}).
     */
    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    String fid;

    /**
     * FIS auth token returned alongside {@link #fid}, sent as the
     * {@code X-Goog-Firebase-Installations-Auth} header on GCM
     * register3. Refreshed when {@link #fisExpiresAt} is reached.
     */
    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    String fisAuthToken;

    /**
     * FIS refresh token returned alongside {@link #fid}. Currently
     * stored but not consumed. The client just re-runs the install
     * flow when {@link #fisAuthToken} expires.
     */
    @ProtobufProperty(index = 6, type = ProtobufType.STRING)
    String fisRefreshToken;

    /**
     * Unix-epoch second at which {@link #fisAuthToken} expires.
     */
    @ProtobufProperty(index = 7, type = ProtobufType.UINT64)
    long fisExpiresAt;

    /**
     * The FCM registration token, the public output of the whole
     * three-step registration. Empty until GCM register3 succeeds.
     * This is the value the caller hands to whoever wants to push to
     * the device.
     */
    @ProtobufProperty(index = 8, type = ProtobufType.STRING)
    String fcmToken;

    /**
     * Per-message persistent ids the server has delivered. The MCS
     * login replays them so the server can stop redelivering. Bounded
     * to the most-recent 50 entries by the client.
     */
    @ProtobufProperty(index = 9, type = ProtobufType.STRING)
    List<String> persistentIds;

    /**
     * Constructs a new session with the given values. Used by the
     * protobuf codec on decode.
     */
    FcmSession(FcmConfig config, long androidId, long securityToken, String fid,
               String fisAuthToken, String fisRefreshToken, long fisExpiresAt,
               String fcmToken, List<String> persistentIds) {
        this.config = config;
        this.androidId = androidId;
        this.securityToken = securityToken;
        this.fid = fid;
        this.fisAuthToken = fisAuthToken;
        this.fisRefreshToken = fisRefreshToken;
        this.fisExpiresAt = fisExpiresAt;
        this.fcmToken = fcmToken;
        this.persistentIds = persistentIds == null ? new ArrayList<>() : persistentIds;
    }

    /**
     * Creates an empty session bound to {@code config}. Every
     * credential field is zero/empty. Used by
     * {@link FcmClient#newSession(FcmConfig)}.
     */
    static FcmSession newSession(FcmConfig config) {
        return new FcmSession(config, 0L, 0L, "", "", "", 0L, "", new ArrayList<>());
    }

    public FcmConfig config() {
        return config;
    }

    public long androidId() {
        return androidId;
    }

    public long securityToken() {
        return securityToken;
    }

    public String fid() {
        return fid;
    }

    public String fisAuthToken() {
        return fisAuthToken;
    }

    public String fisRefreshToken() {
        return fisRefreshToken;
    }

    public long fisExpiresAt() {
        return fisExpiresAt;
    }

    public String fcmToken() {
        return fcmToken;
    }

    public List<String> persistentIds() {
        return persistentIds;
    }

    void setAndroidId(long androidId) {
        this.androidId = androidId;
    }

    void setSecurityToken(long securityToken) {
        this.securityToken = securityToken;
    }

    void setFid(String fid) {
        this.fid = fid;
    }

    void setFisAuthToken(String fisAuthToken) {
        this.fisAuthToken = fisAuthToken;
    }

    void setFisRefreshToken(String fisRefreshToken) {
        this.fisRefreshToken = fisRefreshToken;
    }

    void setFisExpiresAt(long fisExpiresAt) {
        this.fisExpiresAt = fisExpiresAt;
    }

    void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
