package com.github.auties00.cobalt.migration;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

/**
 * Represents the state machine for LID migration.
 * <p>
 * The migration follows this state flow:
 * <pre>
 * NOT_STARTED → WAITING_PROP → WAITING_MAPPINGS → READY → IN_PROGRESS → COMPLETE
 *                    ↓              ↓                ↓
 *                 FAILED         FAILED           FAILED
 * </pre>
 * <p>
 * State transitions:
 * <ul>
 *     <li>{@link #NOT_STARTED} → {@link #WAITING_PROP}: When connection established</li>
 *     <li>{@link #WAITING_PROP} → {@link #WAITING_MAPPINGS}: When AB prop enables migration</li>
 *     <li>{@link #WAITING_MAPPINGS} → {@link #READY}: When mappings received from primary</li>
 *     <li>{@link #READY} → {@link #IN_PROGRESS}: When migration execution starts</li>
 *     <li>{@link #IN_PROGRESS} → {@link #COMPLETE}: When all threads migrated successfully</li>
 *     <li>Any state → {@link #FAILED}: On critical error</li>
 * </ul>
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
    FAILED(6);

    final int index;

    LidMigrationState(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    /**
     * Returns whether this state allows transitioning to the next state.
     *
     * @return true if migration can proceed
     */
    public boolean canProceed() {
        return this != COMPLETE && this != FAILED;
    }

    /**
     * Returns whether this state indicates migration is active.
     *
     * @return true if migration is in an active state
     */
    public boolean isActive() {
        return this == WAITING_PROP || this == WAITING_MAPPINGS || this == READY || this == IN_PROGRESS;
    }

    /**
     * Returns whether this state indicates migration has finished (successfully or not).
     *
     * @return true if migration is in a terminal state
     */
    public boolean isTerminal() {
        return this == COMPLETE || this == FAILED;
    }
}
