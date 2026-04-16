package com.github.auties00.cobalt.util;

import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates WhatsApp stanza identifiers and session identifiers used in
 * IQ correlation, message requests and analytics.
 *
 * <p>The instance-level generator produces sequenced IDs that share a
 * stable random prefix for the session, mirroring the way WhatsApp Web
 * emits {@code <random1>.<random2>-<n>} stanza IDs that a server can
 * correlate back to one client session.
 *
 * @implNote Adapts the stanza-ID generator implemented in WA Web's
 *     {@code WAWebStanzaIdGen} and the session ID generator used by
 *     {@code WAWebMdWamTypes}.
 */
@WhatsAppWebModule(moduleName = "WAWebStanzaIdGen")
public final class RandomIdUtils {
    /**
     * The random session prefix shared by every ID emitted from this
     * instance.
     */
    private final String prefix;

    /**
     * Monotonically increasing counter appended after the prefix.
     */
    private final AtomicLong counter;

    /**
     * Creates a new generator with a freshly randomised prefix and a
     * counter starting at {@code 1}.
     *
     * @implNote WA Web initialises the session prefix once per page load
     *     via {@code WAWebStanzaIdGen.init}.
     */
    public RandomIdUtils() {
        var num1 = DataUtils.randomInt(65536);
        var num2 = DataUtils.randomInt(65536);
        this.prefix = num1 + "." + num2 + "-";
        this.counter = new AtomicLong(1);
    }

    /**
     * Returns the next sequenced identifier in the form
     * {@code <rnd1>.<rnd2>-<n>}.
     *
     * @return the generated identifier
     * @implNote Adapts {@code WAWebStanzaIdGen.generateId}.
     */
    @WhatsAppWebExport(moduleName = "WAWebStanzaIdGen", exports = "generateId", adaptation = WhatsAppAdaptation.ADAPTED)
    public String generateId() {
        return prefix + counter.getAndIncrement();
    }

    /**
     * Returns a one-shot identifier that reuses the same textual layout as
     * {@link #generateId()} but always ends in {@code -1}.
     *
     * @return the generated identifier
     * @implNote Adapts {@code WAWebStanzaIdGen.newId}; used where a fresh
     *     generator instance would be overkill.
     */
    @WhatsAppWebExport(moduleName = "WAWebStanzaIdGen", exports = "newId", adaptation = WhatsAppAdaptation.ADAPTED)
    public static String newId() {
        var num1 = DataUtils.randomInt(65536);
        var num2 = DataUtils.randomInt(65536);
        return num1 + "." + num2 + "-1";
    }

    /**
     * Returns a WhatsApp session identifier suitable for WAM analytics,
     * composed of an epoch-seconds timestamp, a 10-digit random number and
     * a 0 to 999 random suffix.
     *
     * @return the generated session identifier
     * @implNote Adapts the WA Web WAM session ID generator used by
     *     {@code WAWebMdWamTypes} when bootstrapping analytics sessions.
     */
    @WhatsAppWebExport(moduleName = "WAWebMdWamTypes", exports = "generateSid", adaptation = WhatsAppAdaptation.ADAPTED)
    public static String generateSid() {
        return Instant.now().getEpochSecond()
               + "-" + DataUtils.randomLong(1_000_000_000, 9_999_999_999L)
               + "-" + DataUtils.randomInt(0, 1000);
    }
}
