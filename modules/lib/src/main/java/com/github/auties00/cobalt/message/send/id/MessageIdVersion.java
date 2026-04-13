package com.github.auties00.cobalt.message.send.id;

/**
 * The version of the message-ID generation algorithm.
 *
 * @apiNote WAWebMsgKey.newId: tries V2 first, falls back to V1 on error.
 * @see MessageIdGenerator
 */
public enum MessageIdVersion {
    /**
     * The deprecated random-only algorithm.
     * Produces 20-character IDs consisting of the prefix {@code "3EB0"}
     * followed by 16 uppercase hex characters (8 random bytes).
     *
     * @apiNote WAWebMsgKey.newId_DEPRECATED: {@code "3EB0" + randomHex(8)},
     * where {@code randomHex(n)} generates {@code n} random bytes and
     * hex-encodes each byte as two uppercase characters.
     */
    V1,

    /**
     * The current SHA-256 based algorithm.
     * Produces 22-character IDs consisting of the prefix {@code "3EB0"}
     * followed by 18 uppercase hex characters derived from the first 9 bytes
     * of a SHA-256 digest over {@code int64(unixTime) || utf8(senderJid) || random(16)}.
     *
     * @apiNote WAWebMsgKeyNewId.getMsgKeyNewSHA256Id: builds the pre-image with
     * {@code WABinary.writeInt64(unixTime)}, {@code writeString(meUser)},
     * {@code writeBuffer(parseHex(randomHex(16)))}, then SHA-256 hashes it
     * and takes the first 9 bytes.
     */
    V2
}
