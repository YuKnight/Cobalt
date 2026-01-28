package com.github.auties00.cobalt.model.sync;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a single LID migration mapping entry.
 * <p>
 * This record maps a phone number to its assigned and latest LID values.
 * The phone number and LIDs are stored as numeric values that can be
 * converted to JID format.
 */
@ProtobufMessage(name = "LIDMigrationMapping")
public final class LIDMigrationMapping {
    @ProtobufProperty(index = 1, type = ProtobufType.UINT64)
    final Long phoneNumber;

    @ProtobufProperty(index = 2, type = ProtobufType.UINT64)
    final Long assignedLid;

    @ProtobufProperty(index = 3, type = ProtobufType.UINT64)
    final Long latestLid;

    LIDMigrationMapping(Long phoneNumber, Long assignedLid, Long latestLid) {
        this.phoneNumber = phoneNumber;
        this.assignedLid = assignedLid;
        this.latestLid = latestLid;
    }

    /**
     * Returns the phone number as a JID.
     *
     * @return Optional containing the phone number JID if pn is not null
     */
    public Optional<Jid> phoneNumber() {
        if (phoneNumber == null) {
            return Optional.empty();
        }
        return Optional.of(Jid.of(phoneNumber.toString(), JidServer.user()));
    }

    /**
     * Returns the assigned LID as a JID.
     *
     * @return Optional containing the assigned LID JID if assignedLid is not null
     */
    public Optional<Jid> assignedLid() {
        if (assignedLid == null) {
            return Optional.empty();
        }
        return Optional.of(Jid.of(assignedLid.toString(), JidServer.lid()));
    }

    /**
     * Returns the latest LID as a JID.
     *
     * @return Optional containing the latest LID JID if latestLid is not null
     */
    public Optional<Jid> latestLid() {
        if (latestLid == null) {
            return Optional.empty();
        }
        return Optional.of(Jid.of(latestLid.toString(), JidServer.lid()));
    }

    /**
     * Returns the effective LID to use (latest if available, otherwise assigned).
     *
     * @return Optional containing the effective LID JID
     */
    public Optional<Jid> effectiveLid() {
        return latestLid().or(this::assignedLid);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LIDMigrationMapping that
               && Objects.equals(phoneNumber, that.phoneNumber)
               && Objects.equals(assignedLid, that.assignedLid)
               && Objects.equals(latestLid, that.latestLid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, assignedLid, latestLid);
    }

    @Override
    public String toString() {
        return "LIDMigrationMapping[" +
               "pn=" + phoneNumber + ", " +
               "assignedLid=" + assignedLid + ", " +
               "latestLid=" + latestLid + ']';
    }
}
