package com.github.auties00.cobalt.message.send.stanza;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Represents a CTWA (Click-to-WhatsApp) external entry point recorded when a user opens
 * a chat via a CTWA ad link.
 *
 * <p>Each entry point captures the ad deep-link type, authentication status, optional
 * partner name and the time it was added. Entry points expire after one week.
 *
 * @param deepLinkType the type of deep link that led the user to this chat
 * @param authSuccess  {@code true} if the user was successfully authenticated during the
 *                     ad flow
 * @param partnerName  the partner or advertiser name, or {@code null} if not available
 * @param addedTime    the instant when this entry point was recorded
 */
@WhatsAppWebModule(moduleName = "WAWebExternalEntryPointPrefs")
public record ExternalEntryPoint(
        String deepLinkType,
        boolean authSuccess,
        String partnerName,
        Instant addedTime
) {
    /**
     * Maximum age of an external entry point before it is considered expired and
     * discarded.
     */
    @WhatsAppWebExport(moduleName = "WATimeUtils", exports = "WEEK_MILLISECONDS",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final Duration MAX_AGE = Duration.ofDays(7);

    /**
     * Returns whether this entry point has expired.
     *
     * <p>Expiry uses a strict greater-than comparison against {@link #MAX_AGE} to match
     * the JS source.
     *
     * @return {@code true} if expired
     */
    @WhatsAppWebExport(moduleName = "WAWebExternalEntryPointPrefs", exports = "getExternalEntryPoint",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean isExpired() {
        return Duration.between(addedTime, Instant.now()).compareTo(MAX_AGE) > 0;
    }

    /**
     * Returns the partner name as an {@link Optional}.
     *
     * @return the partner name, or empty if {@code null}
     */
    public Optional<String> optionalPartnerName() {
        return Optional.ofNullable(partnerName);
    }
}
