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
     * Returns a policy that discards all chat history, keeping only
     * messages received after the session is established.
     *
     * <p>This is the most resource-efficient option and is suited to
     * applications that only need real-time messaging capabilities.
     *
     * @param newsletters whether newsletters should be synchronised
     *                    during the initial connection
     * @return the configured policy
     */
    public static WhatsAppWebClientHistory discard(boolean newsletters) {
        return newsletters ? ZERO_WITH_NEWSLETTERS : ZERO;
    }

    /**
     * Returns a policy that mirrors WhatsApp Web's default history-sync
     * volume.
     *
     * <p>The setting balances resource usage against message availability
     * by syncing approximately the last few weeks of chat history. This
     * is the recommended option for most applications as it matches the
     * official WhatsApp Web behaviour.
     *
     * @param newsletters whether newsletters should be synchronised
     *                    during the initial connection
     * @return the configured policy
     */
    public static WhatsAppWebClientHistory standard(boolean newsletters) {
        return newsletters ? STANDARD_WITH_NEWSLETTERS : STANDARD;
    }

    /**
     * Returns a policy that requests as much chat history as the server
     * is willing to deliver.
     *
     * <p>The replay may include several months or years of messages
     * depending on account age. The option can consume significant
     * memory and bandwidth.
     *
     * @param newsletters whether newsletters should be synchronised
     *                    during the initial connection
     * @return the configured policy
     */
    public static WhatsAppWebClientHistory extended(boolean newsletters) {
        return newsletters ? EXTENDED_WITH_NEWSLETTERS : EXTENDED;
    }

    /**
     * Returns a policy with a caller-supplied history-size cap.
     *
     * <p>The actual amount of history delivered may be smaller than
     * requested if the account does not have enough historical data or
     * if WhatsApp's servers impose a lower cap.
     *
     * @param size        the maximum number of historical items to
     *                    synchronise, which must be non-negative
     * @param newsletters whether newsletters should be synchronised
     *                    during the initial connection
     * @return the configured policy
     * @throws IllegalArgumentException if {@code size} is negative
     */
    public static WhatsAppWebClientHistory custom(int size, boolean newsletters) {
        return new WhatsAppWebClientHistory(size, newsletters);
    }

    /**
     * Returns whether this policy discards all chat history.
     *
     * @return {@code true} if the size cap is zero, {@code false}
     *         otherwise
     */
    public boolean isZero() {
        return size == 0;
    }

    /**
     * Returns whether this policy requests more chat history than the
     * standard preset.
     *
     * @return {@code true} if the size cap exceeds the standard preset,
     *         {@code false} otherwise
     */
    public boolean isExtended() {
        return size > STANDARD.size();
    }

    /**
     * Returns the upper bound on the number of historical items this
     * policy requests.
     *
     * <p>The actual amount synchronised may be smaller due to server
     * limitations or account history.
     *
     * @return the history-size cap, or {@link Integer#MAX_VALUE} for an
     *         unbounded request
     */
    public int size() {
        return size;
    }

    /**
     * Returns whether this policy includes newsletters in the initial
     * synchronisation.
     *
     * @return {@code true} if newsletters are included, {@code false}
     *         otherwise
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