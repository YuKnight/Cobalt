package com.github.auties00.cobalt.message.send.token;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.cobalt.util.WhatsAppIdGenerator;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Manages Trust Contact (TC) tokens in message sending.
 * <p>
 * TC tokens are used to establish trusted contact relationships and are
 * included in 1:1 messages when the contact has a valid, non-expired token.
 *
 * @apiNote WAWebTrustedContactsUtils
 */
public final class TrustContactToken {
    private static final System.Logger LOGGER = System.getLogger("TrustContactToken");
    private static final int DEFAULT_TC_TOKEN_DURATION = 15552000;
    private static final int DEFAULT_NUM_BUCKETS = 3;

    private final WhatsAppStore store;
    private final WhatsAppClient client;

    public TrustContactToken(WhatsAppStore store, WhatsAppClient client) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.client = client;
    }

    /**
     * Sends a TC token to the specified chat recipient if needed.
     */
    public void sendToken(Jid chatJid) {
        Objects.requireNonNull(chatJid, "chatJid cannot be null");

        if (!chatJid.hasUserServer() && !chatJid.hasLidServer()) {
            return;
        }

        if (chatJid.hasBotServer() || chatJid.isPsa()) {
            return;
        }

        CompletableFuture.runAsync(() -> sendTokenInternal(chatJid));
    }

    private void sendTokenInternal(Jid chatJid) {
        try {
            var chat = store.findChatByJid(chatJid).orElse(null);
            Long lastSentTimestamp = chat != null ? chat.tcTokenSenderTimestamp().orElse(null) : null;

            if (!shouldSendNewToken(lastSentTimestamp)) {
                LOGGER.log(System.Logger.Level.TRACE, "TC token not needed for chat {0}", chatJid);
                return;
            }

            var currentTime = System.currentTimeMillis() / 1000;

            Jid targetJid;
            if (store.getABPropBool("lid_trusted_token_issue_to_lid", false)) {
                targetJid = store.toLid(chatJid);
            } else {
                targetJid = chatJid.toUserJid();
            }

            if (targetJid == null) {
                LOGGER.log(System.Logger.Level.DEBUG, "Cannot determine target JID for TC token: {0}", chatJid);
                return;
            }

            issuePrivacyToken(targetJid, List.of(TokenType.TRUSTED_CONTACT), currentTime);
            store.updateChatTcTokenSenderTimestamp(chatJid, currentTime);

            LOGGER.log(System.Logger.Level.DEBUG, "Sent TC token to chat {0}", chatJid);

        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING, "sendTcToken failed for {0}: {1}", chatJid, e.getMessage());
        }
    }

    /**
     * Issues privacy tokens to a recipient.
     */
    public void issuePrivacyToken(Jid recipientJid, List<TokenType> tokenTypes, long timestamp) {
        Objects.requireNonNull(recipientJid, "recipientJid cannot be null");
        Objects.requireNonNull(tokenTypes, "tokenTypes cannot be null");

        if (client == null) {
            LOGGER.log(System.Logger.Level.WARNING, "Cannot issue privacy token: client not available");
            return;
        }

        var tokenNodes = tokenTypes.stream()
                .map(type -> new NodeBuilder()
                        .description("token")
                        .attribute("jid", recipientJid)
                        .attribute("t", String.valueOf(timestamp))
                        .attribute("type", type.value())
                        .build())
                .toList();

        var tokensNode = new NodeBuilder()
                .description("tokens")
                .content(tokenNodes)
                .build();

        var iqNode = new NodeBuilder()
                .description("iq")
                .attribute("to", Jid.SERVER)
                .attribute("type", "set")
                .attribute("xmlns", "privacy")
                .attribute("id", WhatsAppIdGenerator.generate())
                .content(tokensNode)
                .build();

        client.sendNode(iqNode);
    }

    /**
     * Gets the TC token to include in a message stanza for a chat.
     */
    public byte[] getForChat(Jid chatJid) {
        Objects.requireNonNull(chatJid, "chatJid cannot be null");

        if (!chatJid.hasUserServer() && !chatJid.hasLidServer()) {
            return null;
        }

        if (!store.isPrivacyTokenSendingEnabled()) {
            return null;
        }

        var chat = store.findChatByJid(chatJid).orElse(null);
        if (chat == null) {
            return null;
        }

        var tcToken = chat.tcToken().orElse(null);
        var tcTokenTimestamp = chat.tcTokenTimestamp().orElse(null);

        if (tcToken == null || tcToken.length == 0) {
            return null;
        }

        if (tcTokenTimestamp == null) {
            return null;
        }

        if (isTokenExpired(tcTokenTimestamp, Mode.RECEIVER)) {
            LOGGER.log(System.Logger.Level.DEBUG, "TC token expired for chat {0}", chatJid);
            return null;
        }

        return tcToken;
    }

    /**
     * Checks if a TC token is expired.
     */
    public boolean isTokenExpired(long tokenTimestamp, Mode mode) {
        var cutoff = getTokenExpirationCutoff(mode);
        return tokenTimestamp < cutoff;
    }

    /**
     * Gets the token expiration cutoff timestamp.
     */
    public long getTokenExpirationCutoff(Mode mode) {
        var duration = getTokenDuration(mode);
        var numBuckets = getNumBuckets(mode);

        var currentTime = System.currentTimeMillis() / 1000;
        var currentBucket = currentTime / duration;
        var oldestValidBucket = currentBucket - (numBuckets - 1);

        return oldestValidBucket * duration;
    }

    /**
     * Gets the TC token duration in seconds.
     */
    public int getTokenDuration(Mode mode) {
        var propValue = mode == Mode.RECEIVER
                ? store.getABPropInt("tctoken_duration", DEFAULT_TC_TOKEN_DURATION)
                : store.getABPropInt("tctoken_duration_sender", DEFAULT_TC_TOKEN_DURATION);
        return Math.min(propValue, DEFAULT_TC_TOKEN_DURATION);
    }

    private int getNumBuckets(Mode mode) {
        return mode == Mode.RECEIVER
                ? store.getABPropInt("tctoken_num_buckets", DEFAULT_NUM_BUCKETS)
                : store.getABPropInt("tctoken_num_buckets_sender", DEFAULT_NUM_BUCKETS);
    }

    /**
     * Checks if a new TC token should be sent.
     */
    public boolean shouldSendNewToken(Long lastSentTimestamp) {
        if (lastSentTimestamp == null) {
            return true;
        }

        var duration = getTokenDuration(Mode.SENDER);
        var currentTime = System.currentTimeMillis() / 1000;
        var currentBucket = currentTime / duration;
        var lastSentBucket = lastSentTimestamp / duration;

        return currentBucket > lastSentBucket;
    }

    /**
     * Privacy token types.
     */
    public enum TokenType {
        TRUSTED_CONTACT("trusted_contact");

        private final String value;

        TokenType(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    /**
     * TC token mode.
     */
    public enum Mode {
        SENDER,
        RECEIVER
    }
}
