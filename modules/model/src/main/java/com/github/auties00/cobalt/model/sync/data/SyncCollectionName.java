package com.github.auties00.cobalt.model.sync.data;

import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;

@ProtobufEnum(name = "CollectionName")
public enum SyncCollectionName {
    COLLECTION_NAME_UNKNOWN(0),
    REGULAR(1),
    REGULAR_LOW(2),
    REGULAR_HIGH(3),
    CRITICAL_BLOCK(4),
    CRITICAL_UNBLOCK_LOW(5);

    SyncCollectionName(@ProtobufEnumIndex int index) {
        this.index = index;
    }

    final int index;

    public int index() {
        return this.index;
    }
}
