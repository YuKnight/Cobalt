package com.github.auties00.cobalt.message.send.bot;

import javax.crypto.KDF;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Objects;

/**
 * Derives the bot message secret from a message's base secret.
 *
 * <p>Bot messages use a derived secret rather than the original
 * message secret, so that the user's message secret is not leaked
 * to the bot.  The derivation uses HKDF-SHA-256 expand with the
 * info string {@code "Bot Message"}.
 *
 * @apiNote WAWebBotMessageSecret.genBotMsgSecretFromMsgSecret:
 * {@code HKDF.extractAndExpand(messageSecret, "Bot Message", 32)}.
 */
public final class BotMessageSecret {
    private static final String HKDF_ALGORITHM = "HKDF-SHA256";
    private static final String INFO = "Bot Message";
    private static final int SECRET_LENGTH = 32;

    private BotMessageSecret() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Derives the bot message secret from the base message secret.
     *
     * @param messageSecret the 32-byte base message secret
     * @return the 32-byte derived bot message secret
     * @throws GeneralSecurityException if HKDF fails
     *
     * @apiNote WAWebBotMessageSecret.genBotMsgSecretFromMsgSecret
     */
    public static byte[] derive(byte[] messageSecret) throws GeneralSecurityException {
        Objects.requireNonNull(messageSecret, "messageSecret");
        var kdf = KDF.getInstance(HKDF_ALGORITHM);
        var prk = new SecretKeySpec(messageSecret, HKDF_ALGORITHM);
        var params = HKDFParameterSpec.expandOnly(prk, INFO.getBytes(), SECRET_LENGTH);
        return kdf.deriveData(params);
    }
}
