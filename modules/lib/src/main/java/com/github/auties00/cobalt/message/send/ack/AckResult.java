package com.github.auties00.cobalt.message.send.ack;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Holds the result of parsing the {@code <ack>} stanza that the server returns after
 * an outgoing message has been processed.
 *
 * <p>A non-empty {@linkplain #error() error} indicates a server-side rejection. A
 * non-empty {@linkplain #phash() phash} signals a device-list mismatch that requires
 * a resend to the new devices.
 *
 * @see AckParser
 * @see NackReason
 */
@WhatsAppWebModule(moduleName = "WAWebSendMsgCommonApi")
public final class AckResult {
    /**
     * Holds the server timestamp from the {@code t} attribute, or {@code null} when
     * the attribute was absent.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final Instant timestamp;

    /**
     * Holds the {@code sync} attribute, or {@code null} when absent.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String sync;

    /**
     * Holds the participant hash returned for group sends, or {@code null} when the
     * server's view matches the client's.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String phash;

    /**
     * Indicates whether the server is requesting a LID refresh for the recipient.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final boolean refreshLid;

    /**
     * Holds the addressing mode the server expects, or {@code null} when not reported.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final String addressingMode;

    /**
     * Holds the recipient count reported by the server, or {@code null} when absent.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final Integer count;

    /**
     * Holds the server error code, or {@code null} when the send succeeded.
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final Integer error;

    /**
     * Constructs an ack result with the parsed attribute values.
     *
     * @param timestamp      the server timestamp, or {@code null}
     * @param sync           the sync attribute, or {@code null}
     * @param phash          the participant hash, or {@code null}
     * @param refreshLid     whether a LID refresh is requested
     * @param addressingMode the addressing mode, or {@code null}
     * @param count          the recipient count, or {@code null}
     * @param error          the error code, or {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    AckResult(
            Instant timestamp,
            String sync,
            String phash,
            boolean refreshLid,
            String addressingMode,
            Integer count,
            Integer error
    ) {
        this.timestamp = timestamp;
        this.sync = sync;
        this.phash = phash;
        this.refreshLid = refreshLid;
        this.addressingMode = addressingMode;
        this.count = count;
        this.error = error;
    }

    /**
     * Returns the server timestamp recorded against the message.
     *
     * @return the timestamp, or empty when the attribute was absent
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<Instant> timestamp() {
        return Optional.ofNullable(timestamp);
    }

    /**
     * Returns the {@code sync} attribute carried on the ack.
     *
     * @return the sync value, or empty when absent
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<String> sync() {
        return Optional.ofNullable(sync);
    }

    /**
     * Returns the participant hash sent back by the server for group messages.
     *
     * <p>A non-empty value indicates the server's participant list differs from the
     * client's, requiring a device-list resync and a resend to the delta devices.
     *
     * @return the server phash, or empty when the hashes matched
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<String> phash() {
        return Optional.ofNullable(phash);
    }

    /**
     * Returns whether the server requested a LID refresh for the recipient.
     *
     * @return {@code true} when a LID refresh is requested
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean refreshLid() {
        return refreshLid;
    }

    /**
     * Returns the addressing mode the server expects for this chat.
     *
     * <p>When the returned mode differs from the mode the client used, the client
     * must migrate the group's participant data and resend.
     *
     * @return the addressing mode ({@code "pn"} or {@code "lid"}), or empty when absent
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<String> addressingMode() {
        return Optional.ofNullable(addressingMode);
    }

    /**
     * Returns the recipient count reported by the server.
     *
     * @return the count, or empty when absent
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public OptionalInt count() {
        return count != null
                ? OptionalInt.of(count)
                : OptionalInt.empty();
    }

    /**
     * Returns the error code carried on the ack. An empty value means the send was
     * accepted by the server.
     *
     * @return the error code, or empty on success
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "sendMsgAckSyncParser",
            adaptation = WhatsAppAdaptation.DIRECT)
    public OptionalInt error() {
        return error != null
                ? OptionalInt.of(error)
                : OptionalInt.empty();
    }

    /**
     * Returns whether the ack indicates a successful send.
     *
     * @return {@code true} when no error code was returned
     */
    public boolean isSuccess() {
        return error == null;
    }

    /**
     * Returns whether the server reported a participant-hash mismatch that requires
     * a resend to the delta devices.
     *
     * @return {@code true} when a phash is present
     */
    public boolean hasPhashMismatch() {
        return phash != null;
    }

    /**
     * Returns a debug representation of all parsed attributes.
     *
     * @return a string with every field value
     */
    @Override
    public String toString() {
        return "AckResult[" +
                "timestamp=" + timestamp +
                ", sync=" + sync +
                ", phash=" + phash +
                ", refreshLid=" + refreshLid +
                ", addressingMode=" + addressingMode +
                ", count=" + count +
                ", error=" + error +
                ']';
    }
}
