package com.github.auties00.cobalt.model.sync.data;

import com.github.auties00.cobalt.model.error.DisconnectReason;
import com.github.auties00.cobalt.model.media.ExternalBlobReference;
import com.github.auties00.cobalt.model.signal.KeyId;

import java.util.Collections;
import java.util.List;
import it.auties.protobuf.annotation.*;
import it.auties.protobuf.model.*;
import java.util.Optional;
import java.util.OptionalInt;

@ProtobufMessage(name = "SyncdPatch")
public final class SyncdPatch {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    SyncdVersion version;

    @ProtobufProperty(index = 2, type = ProtobufType.MESSAGE)
    List<SyncdMutation> mutations;

    @ProtobufProperty(index = 3, type = ProtobufType.MESSAGE)
    ExternalBlobReference externalMutations;

    @ProtobufProperty(index = 4, type = ProtobufType.BYTES)
    byte[] snapshotMac;

    @ProtobufProperty(index = 5, type = ProtobufType.BYTES)
    byte[] patchMac;

    @ProtobufProperty(index = 6, type = ProtobufType.MESSAGE)
    KeyId keyId;

    @ProtobufProperty(index = 7, type = ProtobufType.MESSAGE)
    DisconnectReason exitCode;

    @ProtobufProperty(index = 8, type = ProtobufType.UINT32)
    Integer deviceIndex;

    @ProtobufProperty(index = 9, type = ProtobufType.BYTES)
    byte[] clientDebugData;


    SyncdPatch(SyncdVersion version, List<SyncdMutation> mutations, ExternalBlobReference externalMutations, byte[] snapshotMac, byte[] patchMac, KeyId keyId, DisconnectReason exitCode, Integer deviceIndex, byte[] clientDebugData) {
        this.version = version;
        this.mutations = mutations;
        this.externalMutations = externalMutations;
        this.snapshotMac = snapshotMac;
        this.patchMac = patchMac;
        this.keyId = keyId;
        this.exitCode = exitCode;
        this.deviceIndex = deviceIndex;
        this.clientDebugData = clientDebugData;
    }

    public Optional<SyncdVersion> version() {
        return Optional.ofNullable(version);
    }

    public List<SyncdMutation> mutations() {
        return mutations == null ? List.of() : Collections.unmodifiableList(mutations);
    }

    public Optional<ExternalBlobReference> externalMutations() {
        return Optional.ofNullable(externalMutations);
    }

    public Optional<byte[]> snapshotMac() {
        return Optional.ofNullable(snapshotMac);
    }

    public Optional<byte[]> patchMac() {
        return Optional.ofNullable(patchMac);
    }

    public Optional<KeyId> keyId() {
        return Optional.ofNullable(keyId);
    }

    public Optional<DisconnectReason> exitCode() {
        return Optional.ofNullable(exitCode);
    }

    public OptionalInt deviceIndex() {
        return deviceIndex == null ? OptionalInt.empty() : OptionalInt.of(deviceIndex);
    }

    public Optional<byte[]> clientDebugData() {
        return Optional.ofNullable(clientDebugData);
    }

    /**
     * Decodes the {@link #clientDebugData()} raw bytes into a {@link PatchDebugData} message.
     *
     * <p>The {@code clientDebugData} field is encoded as a {@link PatchDebugData} protobuf
     * by the sender (see {@code MutationRequestBuilder} for the outgoing path) and is intended
     * for diagnostic logging on the receiving side. Decoding is best-effort: a malformed or
     * absent payload yields {@link Optional#empty()} rather than propagating an exception so
     * that debug data corruption can never break sync processing.
     *
     * @implNote WAWebSyncdValidateServerSyncProtobuf.validatePatchProtobuf — decodes
     *           {@code clientDebugData} via {@code decodeProtobuf(PatchDebugDataSpec, ...)}
     *           and includes the decoded object in the validated output for debug logging
     *           (the {@code _applyPatch} function uses {@code currentLthash} and
     *           {@code newLthash} for employee-visible logging).
     * @return an {@link Optional} containing the decoded {@link PatchDebugData}, or
     *         {@link Optional#empty()} if no debug data is present or decoding failed
     */
    public Optional<PatchDebugData> decodedClientDebugData() {
        if (clientDebugData == null || clientDebugData.length == 0) {
            return Optional.empty();
        }
        try {
            return Optional.of(PatchDebugDataSpec.decode(clientDebugData));
        } catch (Exception e) {
            // Debug data failure must not break sync — swallow and return empty.
            return Optional.empty();
        }
    }

    public void setVersion(SyncdVersion version) {
        this.version = version;
    }

    public void setMutations(List<SyncdMutation> mutations) {
        this.mutations = mutations;
    }

    public void setExternalMutations(ExternalBlobReference externalMutations) {
        this.externalMutations = externalMutations;
    }

    public void setSnapshotMac(byte[] snapshotMac) {
        this.snapshotMac = snapshotMac;
    }

    public void setPatchMac(byte[] patchMac) {
        this.patchMac = patchMac;
    }

    public void setKeyId(KeyId keyId) {
        this.keyId = keyId;
    }

    public void setExitCode(DisconnectReason exitCode) {
        this.exitCode = exitCode;
    }

    public void setDeviceIndex(Integer deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    public void setClientDebugData(byte[] clientDebugData) {
        this.clientDebugData = clientDebugData;
    }
}
