package com.github.auties00.cobalt.migration;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

/**
 * Represents the state machine for LID 1:1 thread account migration.
 * Maps to the WA Web {@code LidThreadMigrationStatus} internal enum with additional
 * Cobalt-specific states for the error model and initial state tracking.
 *
 * @implNote WAWebLid1X1ThreadAccountMigrations.flow.LidThreadMigrationStatus
 */
@ProtobufEnum
public enum LidMigrationState {
    /**
     * Migration has not been initiated.
     * This is the initial state before any migration activity begins.
     *
     * @implNote NO_WA_BASIS — Cobalt-specific initial state for state machine tracking
     */
    NOT_STARTED(0),

    /**
     * Waiting for the AB prop to enable LID migration.
     * The client checks server-sent feature flags to determine if migration is enabled.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.LidThreadMigrationStatus.WAITING_PROP
     */
    WAITING_PROP(1),

    /**
     * Waiting for LID mappings from the primary device.
     * The primary device sends mappings via LIDMigrationMappingSyncMessage.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.LidThreadMigrationStatus.WAITING_MAPPINGS
     */
    WAITING_MAPPINGS(2),

    /**
     * Mappings received and validated, ready to start migration.
     * The client has all necessary data to perform the migration.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.LidThreadMigrationStatus.READY
     */
    READY(3),

    /**
     * Migration is currently in progress.
     * Threads are being migrated from PN to LID addressing.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.LidThreadMigrationStatus.IN_PROGRESS
     */
    IN_PROGRESS(4),

    /**
     * Migration completed successfully.
     * All eligible threads have been migrated to LID addressing.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.LidThreadMigrationStatus.COMPLETE
     */
    COMPLETE(5),

    /**
     * Migration failed due to a critical error.
     * This may require session termination or re-pairing.
     *
     * @implNote NO_WA_BASIS — Cobalt-specific error state for configurable error handling
     */
    FAILED(6),

    /**
     * Migration is disabled and will not proceed.
     *
     * @implNote NO_WA_BASIS — Cobalt-specific disabled state for configurable error handling
     */
    DISABLED(7);

    /**
     * The protobuf index value for this migration state.
     *
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.LidThreadMigrationStatus
     */
    final int index;

    /**
     * Creates a new LID migration state with the specified protobuf index.
     *
     * @param index the protobuf index value
     * @implNote WAWebLid1X1ThreadAccountMigrations.flow.LidThreadMigrationStatus
     */
    LidMigrationState(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    /**
     * Returns whether this state indicates migration has finished, whether
     * successfully, with failure, or by being disabled.
     *
     * @return {@code true} if migration is in a terminal state
     * @implNote ADAPTED: WAWebLid1X1ThreadAccountMigrations.flow — convenience for Cobalt state machine
     */
    public boolean isTerminal() {
        return this == DISABLED || this == COMPLETE || this == FAILED;
    }
}
