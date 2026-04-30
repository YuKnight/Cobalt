package com.github.auties00.cobalt.message.send.id;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

/**
 * Identifies the algorithm used to generate a WhatsApp message identifier.
 *
 * @see MessageIdGenerator
 */
@WhatsAppWebModule(moduleName = "WAWebMsgKey")
public enum MessageIdVersion {
    /**
     * Identifies the deprecated random-only algorithm. The generated id is the
     * prefix {@code "3EB0"} followed by 16 uppercase hex characters drawn from
     * 8 random bytes.
     */
    V1,

    /**
     * Identifies the current SHA-256 based algorithm. The generated id is the
     * prefix {@code "3EB0"} followed by 18 uppercase hex characters taken from
     * the first 9 bytes of {@code SHA256(int64(unixTime) || utf8(senderJid) ||
     * random(16))}.
     */
    V2
}
