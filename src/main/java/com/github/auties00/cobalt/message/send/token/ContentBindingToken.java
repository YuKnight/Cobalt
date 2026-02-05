package com.github.auties00.cobalt.message.send.token;

import com.github.auties00.cobalt.message.send.util.CryptoUtils;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.MessageContainer;
import com.github.auties00.cobalt.model.message.standard.TextMessage;
import com.github.auties00.cobalt.props.ABProp;
import com.github.auties00.cobalt.props.ABPropsService;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Generates content bindings (RCAT - Reporting Content Authentication Token).
 * <p>
 * Content bindings are used for messages with URLs to enable content verification
 * and reporting. Each participant receives a unique binding derived from their JID
 * and the message secret.
 *
 * @apiNote WAWebMsgRcatUtils.genContentBindingForMsg
 */
public final class ContentBindingToken {
    private static final System.Logger LOGGER = System.getLogger("ContentBinding");

    private static final String RCAT_INFO_SUFFIX = "Rcat";
    private static final int NONCE_LENGTH = 32;
    private static final int BINDING_LENGTH = 8;
    private static final int DEFAULT_MAX_GROUP_SIZE_FOR_RCAT = 100;

    private static final Pattern YOUTUBE_URL_PATTERN = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?v=|embed/|v/)|youtu\\.be/)([a-zA-Z0-9_-]{11})"
    );

    private final WhatsAppStore store;
    private final ABPropsService abPropsService;

    public ContentBindingToken(WhatsAppStore store, ABPropsService abPropsService) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.abPropsService = Objects.requireNonNull(abPropsService, "abPropsService cannot be null");
    }

    /**
     * Generates content bindings for participants.
     */
    public Map<Jid, byte[]> generate(
            String messageId,
            MessageContainer message,
            byte[] messageSecret,
            Jid senderJid,
            Collection<Jid> participants,
            boolean isSentByMe
    ) {
        if (!isSentByMe) {
            return null;
        }

        if (participants == null || participants.isEmpty()) {
            return null;
        }

        if (participants.size() > getMaxGroupSizeForRcat()) {
            return null;
        }

        if (messageSecret == null || messageSecret.length == 0) {
            return null;
        }

        var contentId = getContentIdBytes(message);
        if (contentId == null) {
            return null;
        }

        var senderUserJid = senderJid.toUserJid();
        var result = new HashMap<Jid, byte[]>();

        for (var participant : participants) {
            var participantUserJid = participant.toUserJid();

            try {
                var nonce = deriveNonce(messageId, messageSecret, senderUserJid, participantUserJid);
                var binding = CryptoUtils.hmacSha256Truncated(nonce, contentId, BINDING_LENGTH);
                result.put(participantUserJid, binding);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "Failed to generate content binding for {0}: {1}",
                        participantUserJid, e.getMessage());
            }
        }

        return result.isEmpty() ? null : result;
    }

    /**
     * Generates sender content binding for the sender's own reference.
     */
    public byte[] generateForSender(
            String messageId,
            MessageContainer message,
            byte[] messageSecret,
            Jid senderJid
    ) {
        if (messageSecret == null || messageSecret.length == 0) {
            return null;
        }

        var contentId = getContentIdBytes(message);
        if (contentId == null) {
            return null;
        }

        var senderUserJid = senderJid.toUserJid();

        try {
            var nonce = deriveNonce(messageId, messageSecret, senderUserJid, senderUserJid);
            return CryptoUtils.hmacSha256Truncated(nonce, contentId, BINDING_LENGTH);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to generate sender content binding: {0}", e.getMessage());
            return null;
        }
    }

    private int getMaxGroupSizeForRcat() {
        return abPropsService.getInt(ABProp.MAXIMUM_GROUP_SIZE_FOR_RCAT_AB_PROP_CODE)
                .orElse(DEFAULT_MAX_GROUP_SIZE_FOR_RCAT);
    }

    private byte[] getContentIdBytes(MessageContainer message) {
        var unwrapped = message.unbox();
        var content = unwrapped.content();

        if (!(content instanceof TextMessage textMsg)) {
            return null;
        }

        var matchedText = textMsg.matchedText().orElse(null);
        if (matchedText == null || matchedText.trim().isEmpty()) {
            return null;
        }

        var contentId = extractYoutubeVideoId(matchedText);
        if (contentId == null) {
            contentId = matchedText;
        }

        return contentId.getBytes(StandardCharsets.UTF_8);
    }

    private String extractYoutubeVideoId(String url) {
        var matcher = YOUTUBE_URL_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private byte[] deriveNonce(String messageId, byte[] secret, Jid senderJid, Jid recipientJid)
            throws NoSuchAlgorithmException, InvalidKeyException {
        var info = (messageId +
                senderJid.toString() +
                recipientJid.toString() +
                RCAT_INFO_SUFFIX).getBytes(StandardCharsets.UTF_8);

        return CryptoUtils.hkdfExtractAndExpand(secret, info, NONCE_LENGTH);
    }
}
