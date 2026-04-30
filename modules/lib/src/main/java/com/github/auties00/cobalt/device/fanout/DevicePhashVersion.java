package com.github.auties00.cobalt.device.fanout;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Selects the participant hash (phash) algorithm used on WhatsApp group message stanzas.
 *
 * <p>Group message stanzas carry a {@code phash} attribute so that the server and the
 * sender can confirm they share the same view of the group's device membership. V1
 * (SHA-1) is the legacy format and V2 (SHA-256) is the current format. Only V2
 * supports injecting the Meta AI bot account into the hashed set when the bot is
 * participating in the group.
 *
 * @see DevicePhashCalculator
 */
@WhatsAppWebModule(moduleName = "WAWebPhashUtils")
public enum DevicePhashVersion {

    /**
     * Legacy SHA-1 phash, encoded with the {@code "1:"} prefix. Does not support
     * Meta AI bot injection.
     */
    V1("SHA-1", "1:", false),

    /**
     * Current SHA-256 phash, encoded with the {@code "2:"} prefix. Supports
     * Meta AI bot injection in groups.
     */
    V2("SHA-256", "2:", true);

    /**
     * The Java {@link java.security.MessageDigest} algorithm name.
     */
    private final String algorithm;

    /**
     * The literal prefix prepended to the base64-encoded truncated hash.
     */
    private final String prefix;

    /**
     * Whether this version permits Meta AI bot injection.
     */
    private final boolean supportsMetaBot;

    /**
     * Constructs a new phash version.
     *
     * @param algorithm       the {@code MessageDigest} algorithm name
     * @param prefix          the version prefix
     * @param supportsMetaBot whether Meta AI bot injection is supported
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
     * Returns the {@link java.security.MessageDigest} algorithm name for this version.
     *
     * @return the algorithm name, either {@code "SHA-1"} or {@code "SHA-256"}
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = {"phashV1", "phashV2"},
            adaptation = WhatsAppAdaptation.DIRECT)
    public String algorithm() {
        return algorithm;
    }

    /**
     * Returns the literal prefix prepended to the encoded hash.
     *
     * @return the prefix, either {@code "1:"} or {@code "2:"}
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = {"phashV1", "phashV2"},
            adaptation = WhatsAppAdaptation.DIRECT)
    public String prefix() {
        return prefix;
    }

    /**
     * Returns whether this version permits Meta AI bot injection.
     *
     * @return {@code true} if Meta AI bot injection is allowed
     */
    @WhatsAppWebExport(moduleName = "WAWebPhashUtils",
            exports = "phashV2",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean supportsMetaBot() {
        return supportsMetaBot;
    }
}
