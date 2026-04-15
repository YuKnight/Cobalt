package com.github.auties00.cobalt.model.jid.migration;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The decoded payload of a {@link LIDMigrationMappingSyncMessage}, containing
 * the phone-number-to-LID mappings and the migration timestamp from the primary
 * device.
 *
 * <p>This message is obtained by gzip-decompressing and protobuf-decoding the
 * raw bytes carried by {@link LIDMigrationMappingSyncMessage#encodedMappingPayload()}.
 * The {@linkplain #pnToLidMappings() mappings} list associates each phone number
 * with its corresponding LID identifiers, and the
 * {@linkplain #chatDbMigrationTimestamp() migration timestamp} records the Unix
 * epoch second at which the primary device migrated its chat database.
 *
 * <p>The companion device uses this information to migrate its own 1:1 chat
 * threads from phone-number-based addressing to LID-based addressing. If the
 * mappings list is empty, the companion treats the payload as malformed and logs
 * out with a {@code LidMigrationPeerMappingsMalformed} reason.
 *
 * @implNote WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSyncPayloadSpec
 */
@ProtobufMessage(name = "LIDMigrationMappingSyncPayload")
public final class LIDMigrationMappingSyncPayload {
    /**
     * The list of phone-number-to-LID mapping entries provided by the primary
     * device.
     *
     * <p>Each entry maps a phone number's numeric identifier to its assigned
     * and optionally latest LID numeric identifiers.
     *
     * @implNote WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSyncPayloadSpec field 1 (pnToLidMappings), REPEATED MESSAGE
     */
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    List<LIDMigrationMapping> pnToLidMappings;

    /**
     * The Unix epoch second at which the primary device migrated its chat
     * database, or {@code null} if not provided.
     *
     * <p>The companion device uses this timestamp to resolve conflicts when a
     * chat's most recent message timestamp is compared against the primary's
     * migration time, determining whether local LID mappings or primary-provided
     * mappings should take precedence.
     *
     * @implNote WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSyncPayloadSpec field 2 (chatDbMigrationTimestamp), UINT64
     */
    @ProtobufProperty(index = 2, type = ProtobufType.UINT64)
    Long chatDbMigrationTimestamp;


    /**
     * Constructs a new {@code LIDMigrationMappingSyncPayload} with the specified
     * values.
     *
     * @implNote WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSyncPayloadSpec
     * @param pnToLidMappings         the list of phone-number-to-LID mapping
     *                                entries, or {@code null}
     * @param chatDbMigrationTimestamp the Unix epoch second of the primary
     *                                device's chat database migration, or
     *                                {@code null}
     */
    LIDMigrationMappingSyncPayload(List<LIDMigrationMapping> pnToLidMappings, Long chatDbMigrationTimestamp) {
        this.pnToLidMappings = pnToLidMappings;
        this.chatDbMigrationTimestamp = chatDbMigrationTimestamp;
    }

    /**
     * Returns the list of phone-number-to-LID mapping entries provided by the
     * primary device.
     *
     * @implNote WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSyncPayloadSpec.pnToLidMappings
     * @return an unmodifiable list of mapping entries, or an empty list if not set
     */
    public List<LIDMigrationMapping> pnToLidMappings() {
        return pnToLidMappings == null ? List.of() : Collections.unmodifiableList(pnToLidMappings);
    }

    /**
     * Returns the timestamp at which the primary device migrated its chat
     * database.
     *
     * @implNote ADAPTED: WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSyncPayloadSpec.chatDbMigrationTimestamp
     * @return an {@code Optional} describing the migration timestamp as an
     *         {@code Instant}, or an empty {@code Optional} if not set
     */
    public Optional<Instant> chatDbMigrationTimestamp() {
        return chatDbMigrationTimestamp == null
                ? Optional.empty()
                : Optional.of(Instant.ofEpochSecond(chatDbMigrationTimestamp));
    }

    /**
     * Sets the list of phone-number-to-LID mapping entries.
     *
     * @implNote ADAPTED: WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSyncPayloadSpec.pnToLidMappings
     * @param pnToLidMappings the new list of mapping entries, or {@code null}
     */
    public void setPnToLidMappings(List<LIDMigrationMapping> pnToLidMappings) {
        this.pnToLidMappings = pnToLidMappings;
    }

    /**
     * Sets the Unix epoch second at which the primary device migrated its chat
     * database.
     *
     * @implNote ADAPTED: WAWebProtobufLidMigrationSyncPayload.pb.LIDMigrationMappingSyncPayloadSpec.chatDbMigrationTimestamp
     * @param chatDbMigrationTimestamp the new migration timestamp in seconds
     *                                since the Unix epoch, or {@code null}
     */
    public void setChatDbMigrationTimestamp(Long chatDbMigrationTimestamp) {
        this.chatDbMigrationTimestamp = chatDbMigrationTimestamp;
    }
}
