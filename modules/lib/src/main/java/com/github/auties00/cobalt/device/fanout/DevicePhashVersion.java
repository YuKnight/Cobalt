package com.github.auties00.cobalt.device.fanout;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Selects the participant hash (phash) algorithm when verifying group message recipient sets.
 *
 * <p>WhatsApp embeds a {@code phash} attribute on group message stanzas so that the server
 * and the sender can agree on the list of participant devices being addressed. V1 (SHA-1)
 * is the legacy format, V2 (SHA-256) is the current format and is the only one that
 * supports Meta AI bot injection for groups. Callers pick the appropriate version based on
 * the stanza they are producing.
 *
 * <p>Used by {@link DevicePhashCalculator} to drive both the hash algorithm and the JID
 * serialization shape.
 *
 * @implNote WAWebPhashUtils: defines phashV1 (SHA-1) and phashV2 (SHA-256) algorithms
 * for calculating participant hashes used in group message stanzas.
 */
@WhatsAppWebModule(moduleName = "WAWebPhashUtils")
public enum DevicePhashVersion {

    /**
     * Version 1: SHA-1 based phash with "1:" prefix.
     *
     * @implNote WAWebPhashUtils.phashV1: legacy format, does not support Meta AI bot injection.
     * Uses {@code asUserWidOrThrow(e).toString({legacy: true})} for JID formatting.
     */
    V1("SHA-1", "1:", false),

    /**
     * Version 2: SHA-256 based phash with "2:" prefix.
     *
     * @implNote WAWebPhashUtils.phashV2: current format, supports open and TEE Meta AI bot
     * injection for groups. Uses {@code e.toString({legacy: true, formatFull: true})} for
     * JID formatting, which includes agent and device components.
     */
    V2("SHA-256", "2:", true);

    /**
     * The hash algorithm name (e.g., "SHA-1", "SHA-256").
     */
    private final String algorithm;

    /**
     * The version prefix (e.g., "1:", "2:").
     */
    private final String prefix;

    /**
     * Whether this version supports Meta AI bot injection.
     */
    private final boolean supportsMetaBot;

    /**
     * Constructs a new phash version with the given algorithm, prefix, and bot support flag.
     *
     * @param algorithm    the hash algorithm name
     * @param prefix       the version prefix string
     * @param supportsMetaBot whether this version supports Meta AI bot injection
     * @implNote WAWebPhashUtils: phashV1 uses SHA-1 with "1:" prefix, phashV2 uses SHA-256
     * with "2:" prefix. Bot injection is only supported in phashV2.
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = {"phashV1", "phashV2"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    DevicePhashVersion(String algorithm, String prefix, boolean supportsMetaBot) {
        this.algorithm = algorithm;
        this.prefix = prefix;
        this.supportsMetaBot = supportsMetaBot;
    }

    /**
     * Returns the hash algorithm name for this version.
     *
     * @return the algorithm name (e.g., "SHA-1", "SHA-256")
     * @implNote WAWebPhashUtils: phashV1 uses {@code crypto.subtle.digest({name: "SHA-1"})},
     * phashV2 uses {@code WACryptoSha256.sha256()}.
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = {"phashV1", "phashV2"},
            adaptation = WhatsAppAdaptation.DIRECT)
    public String algorithm() {
        return algorithm;
    }

    /**
     * Returns the prefix string for this version.
     *
     * @return the prefix (e.g., "1:", "2:")
     * @implNote WAWebPhashUtils: phashV1 returns "1:" + base64, phashV2 returns "2:" + base64.
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = {"phashV1", "phashV2"},
            adaptation = WhatsAppAdaptation.DIRECT)
    public String prefix() {
        return prefix;
    }

    /**
     * Returns whether this version supports Meta AI bot injection.
     *
     * @return {@code true} if Meta AI bot can be injected
     * @implNote WAWebPhashUtils: only phashV2 supports bot injection via
     * WAWebBotGroupGatingUtils checks.
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = "phashV2",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean supportsMetaBot() {
        return supportsMetaBot;
    }
}
