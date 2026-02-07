package com.github.auties00.cobalt.message.send.token;

import com.github.auties00.cobalt.model.jid.Jid;

import javax.crypto.KDF;
import javax.crypto.Mac;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Generates per-recipient content-binding tokens (RCAT) for URL messages.
 *
 * <p>Content binding associates an outgoing URL message with a
 * cryptographic tag that lets the server verify the content without
 * learning the plaintext URL.  Each recipient gets a unique 8-byte tag
 * derived from:
 * <ol>
 *   <li>A 32-byte <em>nonce</em> produced by HKDF-SHA-256 over
 *       the message secret, keyed by
 *       {@code msgId ‖ senderJid ‖ recipientJid ‖ "Rcat"}.</li>
 *   <li>An HMAC-SHA-256 of the URL content ID using that nonce,
 *       truncated to the first 8 bytes.</li>
 * </ol>
 *
 * @apiNote WAWebMsgRcatUtils: provides {@code genContentBindingForMsg},
 * {@code deriveNonce}, and the HMAC truncation logic.
 */
public final class ContentBindingToken {
    /**
     * The info-suffix appended when deriving the per-recipient nonce.
     *
     * @apiNote WAWebMsgRcatUtils.deriveNonce: joins
     * {@code [msgId, senderJid, recipientJid, "Rcat"]} to form the
     * HKDF info parameter.
     */
    private static final String NONCE_INFO_SUFFIX = "Rcat";

    /**
     * Output length for the HKDF-derived nonce.
     *
     * @apiNote WAWebMsgRcatUtils: {@code u = 32}
     */
    private static final int NONCE_LENGTH = 32;

    /**
     * Number of leading HMAC bytes kept as the content-binding tag.
     *
     * @apiNote WAWebMsgRcatUtils: {@code hmacSha256(nonce, contentId).slice(0, 8)}
     */
    private static final int TAG_LENGTH = 8;

    /**
     * The HKDF algorithm used for nonce derivation.
     */
    private static final String HKDF_ALGORITHM = "HKDF-SHA256";

    /**
     * The HMAC algorithm used for tag computation.
     */
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Length of a YouTube video ID.
     */
    private static final int YT_VIDEO_ID_LENGTH = 11;

    private ContentBindingToken() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Resolves the content ID from a matched URL text.
     *
     * <p>If the URL is a YouTube link, extracts the 11-character video
     * ID.  Otherwise returns the full matched text.  The result is
     * encoded as UTF-8 bytes.
     *
     * <p>Uses direct string inspection instead of regex for performance:
     * YouTube video IDs are always exactly 11 characters and appear at
     * a fixed position relative to the host/path prefix.
     *
     * @param matchedText the matched URL text from the message
     * @return the content ID bytes
     *
     * @apiNote WAWebMsgRcatUtils.getContentId: calls
     * {@code parseYoutubeVideoId(matchedText)}; if non-null, uses the
     * video ID, otherwise uses the full matched text.
     * WAWebUtilsYoutubeUrlParser.parseYoutubeVideoId: matches
     * youtu.be/ID, youtube.com/watch?v=ID, youtube.com/shorts/ID.
     */
    public static byte[] resolveContentId(String matchedText) {
        Objects.requireNonNull(matchedText, "matchedText");
        var videoId = parseYoutubeVideoId(matchedText);
        var contentId = videoId != null ? videoId : matchedText;
        return contentId.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Extracts the YouTube video ID from a URL, or returns {@code null}
     * if the URL is not a recognised YouTube format.
     *
     * <p>Recognised formats (with optional {@code www.} and {@code m.}):
     * <ul>
     *   <li>{@code https://youtu.be/XXXXXXXXXXX}</li>
     *   <li>{@code https://youtube.com/watch?v=XXXXXXXXXXX}</li>
     *   <li>{@code https://youtube.com/shorts/XXXXXXXXXXX}</li>
     * </ul>
     *
     * @apiNote WAWebUtilsYoutubeUrlParser.parseYoutubeVideoId
     */
    private static String parseYoutubeVideoId(String url) {
        // Skip scheme: find the position after "://"
        var schemeEnd = url.indexOf("://");
        if (schemeEnd < 0) {
            return null;
        }
        var hostStart = schemeEnd + 3;

        // WAWebURLUtils.withoutWww: skip "www." prefix
        if (url.startsWith("www.", hostStart)) {
            hostStart += 4;
        }

        // youtu.be/XXXXXXXXXXX
        if (url.startsWith("youtu.be/", hostStart)) {
            return extractVideoId(url, hostStart + 9);
        }

        // Skip optional "m." prefix
        if (url.startsWith("m.", hostStart)) {
            hostStart += 2;
        }

        if (!url.startsWith("youtube.com/", hostStart)) {
            return null;
        }
        var pathStart = hostStart + 12;

        // youtube.com/watch?v=XXXXXXXXXXX
        if (url.startsWith("watch?v=", pathStart)) {
            return extractVideoId(url, pathStart + 8);
        }

        // youtube.com/shorts/XXXXXXXXXXX
        if (url.startsWith("shorts/", pathStart)) {
            return extractVideoId(url, pathStart + 7);
        }

        return null;
    }

    /**
     * Extracts exactly {@value #YT_VIDEO_ID_LENGTH} characters starting
     * at {@code offset}, or returns {@code null} if insufficient characters
     * remain.
     */
    private static String extractVideoId(String url, int offset) {
        if (offset + YT_VIDEO_ID_LENGTH > url.length()) {
            return null;
        }
        return url.substring(offset, offset + YT_VIDEO_ID_LENGTH);
    }

    /**
     * Generates content-binding tags for each recipient of a URL message.
     *
     * @param messageId     the outgoing message's stanza ID
     * @param messageSecret the 32-byte message secret
     * @param senderJid     the sender's user JID
     * @param recipientJids the recipients' user JIDs
     * @param contentId     the URL content identifier (matched URL text or
     *                      YouTube video ID), encoded as UTF-8
     * @return an unmodifiable map from recipient JID to its 8-byte tag
     * @throws NullPointerException     if any argument is {@code null}
     * @throws GeneralSecurityException if a cryptographic operation fails
     *
     * @apiNote WAWebMsgRcatUtils.genContentBindingForMsg: iterates over
     * recipients, derives a nonce per recipient via {@code deriveNonce},
     * then computes {@code hmacSha256(nonce, contentId)[0:8]}.
     */
    public static Map<Jid, byte[]> generate(
            String messageId,
            byte[] messageSecret,
            Jid senderJid,
            Collection<Jid> recipientJids,
            byte[] contentId
    ) throws GeneralSecurityException {
        Objects.requireNonNull(messageId, "messageId");
        Objects.requireNonNull(messageSecret, "messageSecret");
        Objects.requireNonNull(senderJid, "senderJid");
        Objects.requireNonNull(recipientJids, "recipientJids");
        Objects.requireNonNull(contentId, "contentId");

        var result = new LinkedHashMap<Jid, byte[]>(recipientJids.size());
        for (var recipientJid : recipientJids) {
            // WAWebMsgRcatUtils.deriveNonce: HKDF(messageSecret, info, 32)
            var nonce = deriveNonce(messageId, messageSecret, senderJid, recipientJid);

            // WAWebMsgRcatUtils: hmacSha256(nonce, contentId).slice(0, 8)
            var tag = hmacTruncated(nonce, contentId);
            result.put(recipientJid, tag);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Derives the per-recipient 32-byte nonce via HKDF-SHA-256 expand.
     *
     * <p>The info parameter is the UTF-8 encoding of
     * {@code msgId + senderJid + recipientJid + "Rcat"} (concatenated
     * without separators).
     *
     * @param messageId     the message stanza ID
     * @param messageSecret the 32-byte message secret (used as the PRK)
     * @param senderJid     the sender's user JID
     * @param recipientJid  the recipient's user JID
     * @return a 32-byte nonce
     * @throws GeneralSecurityException if HKDF is unavailable
     *
     * @apiNote WAWebMsgRcatUtils.deriveNonce:
     * {@code info = encode([msgId, senderJid, recipientJid, "Rcat"].join(""))},
     * then {@code HKDF.extractAndExpand(messageSecret, info, 32)}.
     */
    static byte[] deriveNonce(
            String messageId,
            byte[] messageSecret,
            Jid senderJid,
            Jid recipientJid
    ) throws GeneralSecurityException {
        // WAWebMsgRcatUtils: [msgId, senderJid, recipientJid, "Rcat"].join("")
        var info = (messageId + senderJid + recipientJid + NONCE_INFO_SUFFIX)
                .getBytes(StandardCharsets.UTF_8);

        // WAWebMsgRcatUtils: HKDF.extractAndExpand(messageSecret, info, 32)
        var kdf = KDF.getInstance(HKDF_ALGORITHM);
        var prk = new SecretKeySpec(messageSecret, HKDF_ALGORITHM);
        var params = HKDFParameterSpec.expandOnly(prk, info, NONCE_LENGTH);
        return kdf.deriveData(params);
    }

    /**
     * Computes HMAC-SHA-256 of {@code data} keyed by {@code key},
     * truncated to the first {@value #TAG_LENGTH} bytes.
     *
     * @apiNote WAWebMsgRcatUtils: {@code hmacSha256(nonce, contentId).slice(0, 8)}
     */
    private static byte[] hmacTruncated(byte[] key, byte[] data) throws GeneralSecurityException {
        var mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
        var full = mac.doFinal(data);
        return Arrays.copyOf(full, TAG_LENGTH);
    }
}
