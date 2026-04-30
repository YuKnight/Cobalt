package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

/**
 * Builds the {@code <tctoken>} stanza child node carrying the trust contact token for
 * the recipient.
 *
 * <p>The token is included only when the
 * {@code privacy_token_sending_on_all_1_on_1_messages} AB prop is enabled and the chat
 * has a non-expired TC token.
 *
 * <p>This class also hosts Cobalt's adapted port of the
 * {@code WAWebTrustedContactsUtils} module, which exposes a small set of pure
 * token-lifetime predicates and a MEX base64 encoder. Because the JS module has no
 * state of its own (it reads only AB props and the system clock), its helpers live
 * here alongside the single current caller.
 *
 * @see CsTokenStanza
 * @see ChatFanoutStanza
 */
@WhatsAppWebModule(moduleName = "WAWebSendMsgCreateFanoutStanza")
@WhatsAppWebModule(moduleName = "WAWebTrustedContactsUtils")
public final class TcTokenStanza {
    /**
     * Upper bound on {@code tctoken_duration} in seconds, 180 days.
     */
    @WhatsAppWebExport(moduleName = "WAWebTrustedContactsUtils", exports = "getTcTokenDuration",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final int MAX_TC_TOKEN_DURATION_SECONDS = 15_552_000;

    /**
     * Store used for chat lookup.
     */
    private final WhatsAppStore store;

    /**
     * AB props service used to gate token emission and to read the token expiry
     * parameters.
     */
    private final ABPropsService abPropsService;

    /**
     * Creates a new TC token stanza builder.
     *
     * @param store          the WhatsApp store for chat lookup
     * @param abPropsService the AB props service for feature gating and token expiry
     *                       parameters
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public TcTokenStanza(WhatsAppStore store, ABPropsService abPropsService) {
        this.store = Objects.requireNonNull(store, "store");
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService");
    }

    /**
     * Builds the {@code <tctoken>} node for the given chat recipient.
     *
     * <p>Returns {@code null} when the AB prop is disabled, the chat is not found, the
     * chat has no TC token, or the token timestamp is expired.
     *
     * @param chatJid the recipient chat JID
     * @return the tctoken node, or {@code null} if not applicable
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCreateFanoutStanza", exports = "createFanoutMsgStanza",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Node build(Jid chatJid) {
        var tcTokenEnabled = abPropsService.getBool(ABProp.PRIVACY_TOKEN_SENDING_ON_ALL_1_ON_1_MESSAGES);
        if (!tcTokenEnabled) {
            return null;
        }

        var chat = store.findChatByJid(chatJid).orElse(null);
        if (chat == null) {
            return null;
        }

        var tcToken = chat.tcToken().orElse(null);
        var tcTokenTimestamp = chat.tcTokenTimestamp().orElse(null);

        if (tcToken == null || tcTokenTimestamp == null) {
            return null;
        }

        if (isTokenExpired(tcTokenTimestamp, TcTokenMode.RECEIVER)) {
            return null;
        }

        return new NodeBuilder()
                .description("tctoken")
                .content(tcToken)
                .build();
    }

    /**
     * Returns the token duration in seconds for the given mode, clamped to
     * {@link #MAX_TC_TOKEN_DURATION_SECONDS}.
     *
     * @param mode the trusted-contact token role to query
     * @return the duration in seconds
     * @throws NullPointerException if {@code mode} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebTrustedContactsUtils", exports = "getTcTokenDuration",
            adaptation = WhatsAppAdaptation.DIRECT)
    private int getTcTokenDuration(TcTokenMode mode) {
        var durationProp = mode == TcTokenMode.RECEIVER
                ? ABProp.TCTOKEN_DURATION
                : ABProp.TCTOKEN_DURATION_SENDER;
        return Math.min(abPropsService.getInt(durationProp), MAX_TC_TOKEN_DURATION_SECONDS);
    }

    /**
     * Returns the unix-seconds cutoff below which a token timestamp is considered
     * expired, for the given mode.
     *
     * <p>The cutoff is computed as
     * {@code (currentBucket - (numBuckets - 1)) * duration} where
     * {@code currentBucket = floor(unixTime / duration)}.
     *
     * @param mode the trusted-contact token role to query
     * @return the cutoff expressed in seconds since the Unix epoch
     * @throws NullPointerException if {@code mode} is {@code null}
     * @implNote WA Web's {@code castToUnixTime} clamp to int32 is irrelevant for
     * realistic bucket math and matches the value space of
     * {@link Instant#getEpochSecond()}.
     */
    @WhatsAppWebExport(moduleName = "WAWebTrustedContactsUtils", exports = "tokenExpirationCutoff",
            adaptation = WhatsAppAdaptation.DIRECT)
    private long tokenExpirationCutoff(TcTokenMode mode) {
        var bucketsProp = mode == TcTokenMode.RECEIVER
                ? ABProp.TCTOKEN_NUM_BUCKETS
                : ABProp.TCTOKEN_NUM_BUCKETS_SENDER;

        var numBuckets = abPropsService.getInt(bucketsProp);

        var duration = getTcTokenDuration(mode);
        if (duration <= 0) {
            // Cobalt guards against a zero or negative duration so the divide below is
            // always safe. WA Web's AB prop default is 604800.
            return Long.MAX_VALUE;
        }

        var currentBucket = Math.floorDiv(Instant.now().getEpochSecond(), duration);
        var cutoffBucket = currentBucket - (numBuckets - 1);
        return cutoffBucket * duration;
    }

    /**
     * Returns whether the given token timestamp is expired for the given mode.
     *
     * @param tokenTimestamp the token timestamp to check
     * @param mode           the trusted-contact token role to query
     * @return {@code true} if the token is expired
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebTrustedContactsUtils", exports = "isTokenExpired",
            adaptation = WhatsAppAdaptation.DIRECT)
    private boolean isTokenExpired(Instant tokenTimestamp, TcTokenMode mode) {
        return tokenTimestamp.getEpochSecond() < tokenExpirationCutoff(mode);
    }

    /**
     * Returns whether a new sender-side TC token should be issued.
     *
     * <p>A new token is required when no prior token exists, or when the prior token
     * falls into an earlier {@code tctoken_duration_sender} bucket than the current
     * time.
     *
     * @param tokenTimestamp the prior sender-token timestamp, or {@code null} if no
     *                       token has been issued
     * @return {@code true} if a new token should be sent
     * @implNote The JS function reads the raw {@code tctoken_duration_sender} AB prop
     * directly without going through {@code getTcTokenDuration}, so the value is not
     * clamped to {@link #MAX_TC_TOKEN_DURATION_SECONDS} here either.
     */
    @WhatsAppWebExport(moduleName = "WAWebTrustedContactsUtils", exports = "shouldSendNewToken",
            adaptation = WhatsAppAdaptation.DIRECT)
    private boolean shouldSendNewToken(Instant tokenTimestamp) {
        if (tokenTimestamp == null) {
            return true;
        }
        var duration = abPropsService.getInt(ABProp.TCTOKEN_DURATION_SENDER);
        if (duration <= 0) {
            // Cobalt guards against a zero or negative duration; WA Web's default is 604800.
            return true;
        }
        var nowBucket = Math.floorDiv(Instant.now().getEpochSecond(), duration);
        var tokenBucket = Math.floorDiv(tokenTimestamp.getEpochSecond(), duration);
        return nowBucket > tokenBucket;
    }

    /**
     * Encodes a raw trusted-contact token as a standard base64 string suitable for
     * embedding in a MEX (GraphQL) privacy-token argument.
     *
     * @param tcToken the raw token bytes
     * @return the base64-encoded token, with padding
     * @throws NullPointerException if {@code tcToken} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebTrustedContactsUtils", exports = "encodeTcTokenForMex",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static String encodeTcTokenForMex(byte[] tcToken) {
        return Base64.getEncoder().encodeToString(Objects.requireNonNull(tcToken, "tcToken"));
    }

    /**
     * Identifies the role a trusted-contact token plays when its lifetime parameters
     * are queried. Each role is backed by a distinct pair of {@code tctoken_duration*}
     * and {@code tctoken_num_buckets*} AB props.
     */
    @WhatsAppWebExport(moduleName = "WAWebTrustedContactsUtils", exports = "TcTokenMode",
            adaptation = WhatsAppAdaptation.DIRECT)
    private enum TcTokenMode {
        /**
         * The current device is the token's sender. Lifetime parameters are read from
         * {@code tctoken_duration_sender} and {@code tctoken_num_buckets_sender}.
         */
        SENDER,

        /**
         * The current device is the token's receiver. Lifetime parameters are read from
         * {@code tctoken_duration} and {@code tctoken_num_buckets}.
         */
        RECEIVER
    }
}
