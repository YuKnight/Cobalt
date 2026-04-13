package com.github.auties00.cobalt.model.jid.migration;

import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Objects;
import java.util.Optional;

/**
 * An individual mapping entry within a {@link LIDMigrationMappingSyncPayload}
 * that associates a phone number with its corresponding LID identifiers.
 *
 * <p>Each entry maps the numeric user part of a phone number JID to the numeric
 * user part of one or two LID JIDs. The {@linkplain #pn() phone number} is the
 * raw numeric identifier (for example {@code 1234567890} for the JID
 * {@code 1234567890@s.whatsapp.net}). The {@linkplain #assignedLid() assigned LID}
 * is the identifier assigned by the primary device at migration time (for example
 * {@code 123456} for the JID {@code 123456@lid}).
 *
 * <p>The optional {@linkplain #latestLid() latest LID} represents the most recent
 * LID known to the primary device, which may differ from the assigned LID if the
 * user's LID was updated after the migration process began. When present, the
 * companion device uses it to detect mapping conflicts during thread migration.
 *
 * @implNote WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSpec
 */
@ProtobufMessage(name = "LIDMigrationMapping")
public final class LIDMigrationMapping {
    /**
     * The numeric user part of the phone number JID being migrated.
     *
     * <p>This value corresponds to the user portion of a standard WhatsApp JID,
     * for example {@code 1234567890} from {@code 1234567890@s.whatsapp.net}.
     *
     * @implNote WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSpec field 1 (pn), REQUIRED UINT64
     */
    @ProtobufProperty(index = 1, type = ProtobufType.UINT64)
    long pn;

    /**
     * The numeric user part of the LID JID assigned by the primary device during
     * migration.
     *
     * <p>This value corresponds to the user portion of a LID JID, for example
     * {@code 123456} from {@code 123456@lid}.
     *
     * @implNote WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSpec field 2 (assignedLid), REQUIRED UINT64
     */
    @ProtobufProperty(index = 2, type = ProtobufType.UINT64)
    long assignedLid;

    /**
     * The numeric user part of the most recent LID JID known to the primary device,
     * or {@code null} if unavailable.
     *
     * <p>When this value differs from {@linkplain #assignedLid() assignedLid}, it
     * indicates that the user's LID was updated after the migration process began.
     *
     * @implNote WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSpec field 3 (latestLid), UINT64
     */
    @ProtobufProperty(index = 3, type = ProtobufType.UINT64)
    Long latestLid;


    /**
     * Constructs a new {@code LIDMigrationMapping} with the specified values.
     *
     * @implNote WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSpec
     * @param pn          the numeric user part of the phone number JID
     * @param assignedLid the numeric user part of the LID assigned by the primary
     * @param latestLid   the numeric user part of the most recent LID, or
     *                    {@code null} if unavailable
     */
    LIDMigrationMapping(Long pn, Long assignedLid, Long latestLid) {
        this.pn = Objects.requireNonNull(pn);
        this.assignedLid = Objects.requireNonNull(assignedLid);
        this.latestLid = latestLid;
    }

    /**
     * Returns the numeric user part of the phone number JID being migrated.
     *
     * @implNote ADAPTED: WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSpec.pn
     * @return the phone number as a {@link Jid}
     */
    public Jid pn() {
        return Jid.of(pn);
    }

    /**
     * Returns the numeric user part of the LID assigned by the primary device.
     *
     * @implNote ADAPTED: WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSpec.assignedLid
     * @return the assigned LID as a {@link Jid}
     */
    public Jid assignedLid() {
        return Jid.of(String.valueOf(assignedLid), JidServer.lid());
    }

    /**
     * Returns the numeric user part of the most recent LID known to the primary
     * device.
     *
     * @implNote ADAPTED: WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSpec.latestLid
     * @return an {@code Optional} containing the latest LID if available, or
     *         an empty {@code Optional} if not set
     */
    public Optional<Jid> latestLid() {
        return latestLid == null
                ? Optional.empty()
                : Optional.of(Jid.of(String.valueOf(latestLid), JidServer.lid()));
    }

    /**
     * Sets the numeric user part of the phone number JID being migrated.
     *
     * @implNote ADAPTED: WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSpec.pn
     * @param pn the new phone number numeric identifier
     */
    public void setPn(long pn) {
        this.pn = pn;
    }

    /**
     * Sets the numeric user part of the LID assigned by the primary device.
     *
     * @implNote ADAPTED: WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSpec.assignedLid
     * @param assignedLid the new assigned LID numeric identifier
     */
    public void setAssignedLid(long assignedLid) {
        this.assignedLid = assignedLid;
    }

    /**
     * Sets the numeric user part of the most recent LID known to the primary
     * device.
     *
     * @implNote ADAPTED: WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSpec.latestLid
     * @param latestLid the new latest LID, or {@code null} to clear
     */
    public void setLatestLid(Long latestLid) {
        this.latestLid = latestLid;
    }
}
