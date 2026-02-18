package com.github.auties00.cobalt.model.sync.data;

import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.*;
import java.util.Optional;

@ProtobufMessage(name = "SyncdMutation")
public final class SyncdMutation {
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    SyncdOperation operation;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    SyncdRecord record;


    SyncdMutation(SyncdOperation operation, SyncdRecord record) {
        this.operation = operation;
        this.record = record;
    }

    public Optional<SyncdOperation> operation() {
        return Optional.ofNullable(operation);
    }

    public Optional<SyncdRecord> record() {
        return Optional.ofNullable(record);
    }

    public SyncdMutation setOperation(SyncdOperation operation) {
        this.operation = operation;
        return this;
    }

    public SyncdMutation setRecord(SyncdRecord record) {
        this.record = record;
        return this;
    }

    @ProtobufEnum(name = "SyncdMutation.SyncdOperation")
    public static enum SyncdOperation {
        SET(0, ((byte) (0x1))),
        REMOVE(1, ((byte) (0x2)));

        final int index;
        private final byte content;

        SyncdOperation(@ProtobufEnumIndex int index, byte content) {
            this.index = index;
            this.content = content;
        }

        public int index() {
            return index;
        }

        public byte content() {
            return content;
        }
    }
}
