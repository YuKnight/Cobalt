package com.github.auties00.cobalt.message.send.bot;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import javax.crypto.KDF;
import javax.crypto.spec.HKDFParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Objects;

/**
 * Derives the bot message secret from a message's base secret. Bot messages
 * use a derived secret rather than the original one so the user's secret is
 * never exposed to the bot.
 */
@WhatsAppWebModule(moduleName = "WAWebBotMessageSecret")
public final class BotMessageSecret {
    /**
     * Holds the HKDF algorithm identifier used for the derivation.
     */
    private static final String HKDF_ALGORITHM = "HKDF-SHA256";

    /**
     * Holds the HKDF info string used during the expand step.
     */
    private static final String INFO = "Bot Message";

    /**
     * Holds the length, in bytes, of the derived secret.
     */
    private static final int SECRET_LENGTH = 32;

    /**
     * Prevents instantiation of this utility class.
     *
     * @throws UnsupportedOperationException always
     */
    private BotMessageSecret() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Derives the 32-byte bot message secret from the given base message secret.
     *
     * <p>Performs HKDF-Extract with a null (all-zero) salt over the given
     * {@code messageSecret}, then HKDF-Expand with the info string
     * {@code "Bot Message"} to produce 32 bytes.
     *
     * @param messageSecret the 32-byte base message secret
     * @return the derived bot message secret
     * @throws NullPointerException     if {@code messageSecret} is {@code null}
     * @throws GeneralSecurityException if HKDF derivation fails
     */
    @WhatsAppWebExport(moduleName = "WAWebBotMessageSecret", exports = "genBotMsgSecretFromMsgSecret",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static byte[] derive(byte[] messageSecret) throws GeneralSecurityException {
        Objects.requireNonNull(messageSecret, "messageSecret");
        var kdf = KDF.getInstance(HKDF_ALGORITHM);
        var params = HKDFParameterSpec.ofExtract()
                .addIKM(messageSecret)
                .thenExpand(INFO.getBytes(StandardCharsets.UTF_8), SECRET_LENGTH);
        return kdf.deriveData(params);
    }
}
