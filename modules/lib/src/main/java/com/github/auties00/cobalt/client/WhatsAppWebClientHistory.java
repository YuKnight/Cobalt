package com.github.auties00.cobalt.client;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;

/**
 * Declares how much chat history a WhatsApp Web companion client should
 * pull from the primary device after linking.
 *
 * <p>When a web companion first pairs with a mobile device, WhatsApp runs
 * a one-shot history synchronisation that replays past conversations into
 * the companion's local store. This class lets the integrator pick the
 * volume of that replay ({@link #discard(boolean)},
 * {@link #standard(boolean)}, {@link #extended(boolean)}, or a
 * {@link #custom(int, boolean)} cap) and whether newsletters should be
 * included. The choice directly impacts memory, bandwidth, and pairing
 * time budget.
 *
 * <p>Instances are immutable value objects. The factory methods return
 * cached constants for the common presets to avoid allocations in hot
 * builder paths.
 *
 * @implNote WAWebProtobufsCompanionReg.pb:
 * {@code DeviceProps$HistorySyncConfigSpec} is the wire-level protobuf
 * message encoded from this class when sending the pairing request.
 * @see WhatsAppClient
 * @see WhatsAppClientBuilder.Options.Web#historySetting(WhatsAppWebClientHistory)
 */
@ProtobufMessage
@WhatsAppWebModule(moduleName = "WAWebProtobufsCompanionReg.pb")
public final class WhatsAppWebClientHistory {
    /**
     * Cached constant for the zero-history variant with newsletters
     * disabled.
     */
    private static final WhatsAppWebClientHistory ZERO = new WhatsAppWebClientHistory(0, false);
    /**
     * Cached constant for the zero-history variant with newsletters
     * enabled.
     */
    private static final WhatsAppWebClientHistory ZERO_WITH_NEWSLETTERS = new WhatsAppWebClientHistory(0, true);
    /**
     * Cached constant for WhatsApp Web's default history size (59206
     * items), newsletters disabled.
     */
    private static final WhatsAppWebClientHistory STANDARD = new WhatsAppWebClientHistory(59206, false);
    /**
     * Cached constant for WhatsApp Web's default history size (59206
     * items), newsletters enabled.
     */
    private static final WhatsAppWebClientHistory STANDARD_WITH_NEWSLETTERS = new WhatsAppWebClientHistory(59206, true);
    /**
     * Cached constant for the extended-history variant (request as much
     * as the server will deliver), newsletters disabled.
     */
    private static final WhatsAppWebClientHistory EXTENDED = new WhatsAppWebClientHistory(Integer.MAX_VALUE, false);
    /**
     * Cached constant for the extended-history variant, newsletters
     * enabled.
     */
    private static final WhatsAppWebClientHistory EXTENDED_WITH_NEWSLETTERS = new WhatsAppWebClientHistory(Integer.MAX_VALUE, true);

    /**
     * The maximum number of history items to request during the initial
     * sync; {@link Integer#MAX_VALUE} means unlimited.
     */
    @WhatsAppWebExport(moduleName = "WAWebProtobufsCompanionReg.pb",
            exports = "DeviceProps$HistorySyncConfigSpec", adaptation = WhatsAppAdaptation.DIRECT)
    @ProtobufProperty(index = 1, type = ProtobufType.INT32)
    final int size;

    /**
     * Whether newsletters should be included in the initial history sync.
     */
    @WhatsAppWebExport(moduleName = "WAWebProtobufsCompanionReg.pb",
            exports = "DeviceProps$HistorySyncConfigSpec", adaptation = WhatsAppAdaptation.DIRECT)
    @ProtobufProperty(index = 2, type = ProtobufType.BOOL)
    final boolean newsletters;

    /**
     * Constructs a new policy with the given size and newsletter flag.
     *
     * <p>Reserved for use by the protobuf deserialiser and the static
     * factory methods; callers should instead use the public factories
     * such as {@link #custom(int, boolean)}.
     *
     * @param size        the history size cap
     * @param newsletters whether newsletters are included
     */
    WhatsAppWebClientHistory(int size, boolean newsletters) {
        this.size = size;
        this.newsletters = newsletters;
    }

    /**
     * Creates a policy that discards all chat history, keeping only new messages from session creation onwards.
     * <p>
     * This is the most resource-efficient option but provides no access to historical messages.
     * Recommended for applications that only need real-time messaging capabilities.
     * </p>
     *
     * @param newsletters whether newsletters should be synchronized during the initial connection
     * @return a policy that discards all previous chat history
     */
    public static WhatsAppWebClientHistory discard(boolean newsletters) {
        return newsletters ? ZERO_WITH_NEWSLETTERS : ZERO;
    }

    /**
     * Creates a policy using WhatsApp Web's default history synchronization settings.
     * <p>
     * This policy provides a balanced approach between resource usage and message availability,
     * syncing approximately the last few weeks of chat history. This is the recommended setting
     * for most applications as it matches the official WhatsApp Web behavior.
     * </p>
     *
     * @param newsletters whether newsletters should be synchronized during the initial connection
     * @return a policy using standard WhatsApp Web history limits
     */
    public static WhatsAppWebClientHistory standard(boolean newsletters) {
        return newsletters ? STANDARD_WITH_NEWSLETTERS : STANDARD;
    }

    /**
     * Creates a policy that attempts to synchronize most available chat history.
     * <p>
     * This policy requests the maximum amount of chat history that WhatsApp allows,
     * which may include several months or years of messages depending on account age.
     * <strong>Warning:</strong> This can consume significant system resources and bandwidth.
     * </p>
     *
     * @param newsletters whether newsletters should be synchronized during the initial connection
     * @return a policy that requests extended chat history
     */
    public static WhatsAppWebClientHistory extended(boolean newsletters) {
        return newsletters ? EXTENDED_WITH_NEWSLETTERS : EXTENDED;
    }

    /**
     * Creates a policy with a custom history size limit.
     * <p>
     * Allows fine-grained control over the amount of history to synchronize.
     * The actual amount of history received may be less than requested if the account
     * doesn't have enough historical data or if WhatsApp imposes server-side limits.
     * </p>
     *
     * @param size        the maximum value of historical items to synchronize (must be non-negative)
     * @param newsletters whether newsletters should be synchronized during the initial connection
     * @return a policy with the specified custom size limit
     * @throws IllegalArgumentException if size is negative
     */
    public static WhatsAppWebClientHistory custom(int size, boolean newsletters) {
        return new WhatsAppWebClientHistory(size, newsletters);
    }

    /**
     * Checks if this policy discards all chat history.
     * <p>
     * A zero-size policy means no historical messages will be synchronized,
     * and only new messages from session creation onwards will be available.
     * </p>
     *
     * @return {@code true} if this policy discards all history, {@code false} otherwise
     */
    public boolean isZero() {
        return size == 0;
    }

    /**
     * Checks if this policy requests extended chat history beyond the standard amount.
     * <p>
     * Extended policies typically result in longer sync times and higher resource usage
     * but provide access to more historical messages.
     * </p>
     *
     * @return {@code true} if this policy requests more than the standard amount of history
     */
    public boolean isExtended() {
        return size > STANDARD.size();
    }

    /**
     * Returns the maximum value of historical items this policy will attempt to synchronize.
     * <p>
     * This represents the upper limit of history items to request from WhatsApp's servers.
     * The actual amount synchronized may be less due to server limitations or account history.
     * </p>
     *
     * @return the history size limit, or {@link Integer#MAX_VALUE} for unlimited requests
     */
    public int size() {
        return size;
    }

    /**
     * Checks if this policy includes newsletter synchronization.
     * <p>
     * When enabled, newsletters and their associated metadata will be synchronized
     * along with regular chat history during the initial connection.
     * </p>
     *
     * @return {@code true} if newsletters should be synchronized, {@code false} otherwise
     */
    public boolean hasNewsletters() {
        return newsletters;
    }

    /**
     * Compares this policy to another object for structural equality.
     *
     * @param o the object to compare with
     * @return {@code true} if {@code o} is a
     *         {@code WhatsAppWebClientHistory} with the same size and
     *         newsletter flag
     */
    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof WhatsAppWebClientHistory that
                && size == that.size
                && newsletters == that.newsletters;
    }

    /**
     * Returns a hash code derived from the size and newsletter flag.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(size, newsletters);
    }

    /**
     * Returns a human-readable description of this policy suitable for
     * logs.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "WhatsappWebHistorySetting[" +
                "size=" + size + ", " +
                "newsletters=" + newsletters + ']';
    }
}