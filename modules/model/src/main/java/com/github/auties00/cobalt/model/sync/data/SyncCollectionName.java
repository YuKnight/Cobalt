package com.github.auties00.cobalt.model.sync.data;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

/**
 * Logical identifier of an app-state collection used in sync operations.
 *
 * <p>App-state is partitioned into named collections, each holding a specific
 * family of actions (for example chat-level actions, critical account-level
 * settings, or bulk low-priority settings). The values in this enum name the
 * partitions used by the sync protocol so that patches, snapshots and
 * mutations can be routed to the correct state store.
 */
@ProtobufEnum(name = "CollectionName")
public enum SyncCollectionName {
    /**
     * Sentinel value used when the collection is missing or unrecognised.
     */
    COLLECTION_NAME_UNKNOWN(0),
    /**
     * Standard-priority collection for most user actions.
     */
    REGULAR(1),
    /**
     * Low-priority, bulky collection used for large volumes of non-essential
     * actions.
     */
    REGULAR_LOW(2),
    /**
     * High-priority collection for actions that must sync quickly.
     */
    REGULAR_HIGH(3),
    /**
     * Critical collection containing block-related state.
     */
    CRITICAL_BLOCK(4),
    /**
     * Critical, low-priority collection containing unblock-related state.
     */
    CRITICAL_UNBLOCK_LOW(5);

    /**
     * Constructs a collection-name constant with the given protobuf index.
     *
     * @param index the protobuf wire index
     */
    SyncCollectionName(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    /**
     * The protobuf wire index assigned to this collection.
     */
    final int index;

    /**
     * Returns the protobuf wire index of this collection name.
     *
     * @return the protobuf enum index
     */
    public int index() {
        return this.index;
    }
}
