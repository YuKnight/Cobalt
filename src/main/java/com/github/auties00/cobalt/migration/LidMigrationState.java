package com.github.auties00.cobalt.migration;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

/**
 * Represents the state machine for LID migration.
 */
@ProtobufEnum
public enum LidMigrationState {
    /**
     * Migration has not been initiated.
     * This is the initial state before any migration activity.
     */
    NOT_STARTED(0),

    /**
     * Waiting for the AB prop to enable LID migration.
     * The client checks server-sent feature flags to determine if migration is enabled.
     */
    WAITING_PROP(1),

    /**
     * Waiting for LID mappings from the primary device.
     * The primary device sends mappings via LIDMigrationMappingSyncMessage.
     */
    WAITING_MAPPINGS(2),

    /**
     * Mappings received and validated, ready to start migration.
     * The client has all necessary data to perform the migration.
     */
    READY(3),

    /**
     * Migration is currently in progress.
     * Threads are being migrated from PN to LID addressing.
     */
    IN_PROGRESS(4),

    /**
     * Migration completed successfully.
     * All eligible threads have been migrated to LID addressing.
     */
    COMPLETE(5),

    /**
     * Migration failed due to a critical error.
     * This may require session termination or re-pairing.
     */
    FAILED(6),

    /**
     * Migration disabled.
     */
    DISABLED(7);

    final int index;

    LidMigrationState(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    /**
     * Returns whether this state indicates migration has finished (successfully or not).
     *
     * @return true if migration is in a terminal state
     */
    public boolean isTerminal() {
        return this == DISABLED || this == COMPLETE || this == FAILED;
    }
}
