package com.github.auties00.cobalt.message.send.ack;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * The result of parsing a server acknowledgement node returned after
 * sending a message stanza.
 *
 * <p>The server always returns the same set of attributes; some are
 * optional and represented here via {@link Optional} accessors.  The
 * presence of an {@linkplain #error() error code} indicates a
 * server-side rejection, while a non-empty {@linkplain #phash() phash}
 * indicates a device-list mismatch requiring a resend.
 *
 * @apiNote WAWebSendMsgCommonApi.sendMsgAckSyncParser: the parsed ack
 * structure.  A non-null {@code error} indicates a server-side rejection
 * (e.g., 421 for stale group addressing mode).
 * @see AckParser
 * @see NackReason
 */
public final class AckResult {
    private final Instant timestamp;
    private final String sync;
    private final String phash;
    private final boolean refreshLid;
    private final String addressingMode;
    private final Integer count;
    private final Integer error;

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
     * Returns the server timestamp.
     *
     * @return the timestamp, or empty if absent
     *
     * @apiNote WAWebSendMsgCommonApi: {@code e.attrTime("t")}
     */
    public Optional<Instant> timestamp() {
        return Optional.ofNullable(timestamp);
    }

    /**
     * Returns the sync attribute.
     *
     * @return the sync value, or empty if absent
     *
     * @apiNote WAWebSendMsgCommonApi: {@code e.maybeAttrString("sync")}
     */
    public Optional<String> sync() {
        return Optional.ofNullable(sync);
    }

    /**
     * Returns the participant hash returned by the server for group messages.
     *
     * <p>A non-empty value indicates the server's participant list differs
     * from the client's, requiring a device-list resync and message resend.
     *
     * @return the server phash, or empty if the hashes matched
     *
     * @apiNote WAWebSendMsgCommonApi: {@code e.maybeAttrString("phash")}.
     * WAWebSendGroupSkmsgJob: triggers resendPersistedGroupMsgWrapper when
     * {@code phash != null && phash !== localPhash}.
     * WAWebSendUserMsgJob: triggers resendUserMsg when {@code phash != null}.
     */
    public Optional<String> phash() {
        return Optional.ofNullable(phash);
    }

    /**
     * Returns whether the server requests a LID refresh for the recipient.
     *
     * @return {@code true} if a LID refresh is requested
     *
     * @apiNote WAWebSendMsgCommonApi:
     * {@code e.hasAttr("refresh_lid") ? e.attrString("refresh_lid") === "true" : false}.
     * WAWebSendUserMsgJob.maybeRefreshLid: triggers a contact list sync
     * when {@code true}.
     */
    public boolean refreshLid() {
        return refreshLid;
    }

    /**
     * Returns the addressing mode the server expects for this chat.
     *
     * <p>When the returned mode differs from the mode the client used to
     * send the message, the client must migrate the group's participant
     * data and resend.
     *
     * @return the addressing mode ({@code "pn"} or {@code "lid"}),
     *         or empty if absent
     *
     * @apiNote WAWebSendMsgCommonApi: {@code e.maybeAttrString("addressing_mode")}.
     * WAWebSendGroupSkmsgJob: compares against local addressing mode and
     * calls handleAddressingModeMismatch on difference.
     */
    public Optional<String> addressingMode() {
        return Optional.ofNullable(addressingMode);
    }

    /**
     * Returns the recipient count reported by the server.
     *
     * @return the count, or empty if absent
     *
     * @apiNote WAWebSendMsgCommonApi: {@code e.maybeAttrInt("count")}.
     * WAWebSendGroupSkmsgJob: merges into the message table when present.
     */
    public OptionalInt count() {
        return count != null
                ? OptionalInt.of(count)
                : OptionalInt.empty();
    }

    /**
     * Returns the error code included by the server.
     *
     * <p>An empty return value indicates success.
     *
     * @return the error code, or empty if successful
     *
     * @apiNote WAWebSendMsgCommonApi: {@code e.maybeAttrInt("error")}.
     * WAWebSendGroupSkmsgJob: checks for
     * {@link NackReason#STALE_GROUP_ADDRESSING_MODE} (421).
     */
    public OptionalInt error() {
        return error != null
                ? OptionalInt.of(error)
                : OptionalInt.empty();
    }

    /**
     * Returns whether the ack indicates success (no error code).
     *
     * @return {@code true} if {@link #error()} is empty
     */
    public boolean isSuccess() {
        return error == null;
    }

    /**
     * Returns whether the server's participant hash differs from the
     * client's, indicating a device-list mismatch that requires a resend.
     *
     * @return {@code true} if a phash is present
     */
    public boolean hasPhashMismatch() {
        return phash != null;
    }

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
