package com.github.auties00.cobalt.message.send.encryption;

import com.github.auties00.cobalt.message.protocol.MessageSignalEncryptionType;
import com.github.auties00.cobalt.message.protocol.MessagePadding;
import com.github.auties00.cobalt.message.protocol.SenderKeyNameFactory;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.model.MessageContainer;
import com.github.auties00.cobalt.model.message.model.MessageContainerSpec;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.libsignal.SignalSessionCipher;
import com.github.auties00.libsignal.groups.SignalGroupCipher;
import com.github.auties00.libsignal.protocol.SignalSenderKeyDistributionMessage;

import java.util.Objects;

/**
 * Service for encrypting messages using the Signal Protocol.
 * Handles both 1:1 messages (using Signal sessions) and group messages (using sender keys).
 */
public final class MessageEncryptionService {
    private final WhatsAppStore store;
    private final SignalSessionCipher sessionCipher;
    private final SignalGroupCipher groupCipher;

    public MessageEncryptionService(
            WhatsAppStore store,
            SignalSessionCipher sessionCipher,
            SignalGroupCipher groupCipher
    ) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.sessionCipher = Objects.requireNonNull(sessionCipher, "sessionCipher cannot be null");
        this.groupCipher = Objects.requireNonNull(groupCipher, "groupCipher cannot be null");
    }

    /**
     * Encrypts a message for a specific device using Signal Protocol.
     * Uses PreKeySignalMessage (pkmsg) for new sessions or SignalMessage (msg) for existing sessions.
     *
     * @param plaintext the serialized message protobuf bytes
     * @param deviceJid the target device JID
     * @return the encrypted payload with type (PKMSG or MSG)
     */
    public MessageEncryptedPayload encryptForDevice(byte[] plaintext, Jid deviceJid) {
        Objects.requireNonNull(plaintext, "plaintext cannot be null");
        Objects.requireNonNull(deviceJid, "deviceJid cannot be null");

        var paddedPlaintext = MessagePadding.addPadding(plaintext);
        var address = deviceJid.toSignalAddress();
        var ciphertext = sessionCipher.encrypt(address, paddedPlaintext);
        var encryptionType = MessageSignalEncryptionType.fromSignalCiphertext(ciphertext);

        return new MessageEncryptedPayload(ciphertext, encryptionType);
    }

    /**
     * Encrypts a message container for a specific device.
     * Convenience method that serializes the container first.
     *
     * @param message   the message container to encrypt
     * @param deviceJid the target device JID
     * @return the encrypted payload with type
     */
    public MessageEncryptedPayload encryptForDevice(MessageContainer message, Jid deviceJid) {
        Objects.requireNonNull(message, "message cannot be null");
        var plaintext = MessageContainerSpec.encode(message);
        return encryptForDevice(plaintext, deviceJid);
    }

    /**
     * Encrypts a message for a group using Sender Key encryption.
     * All group members with the sender key can decrypt this message.
     *
     * @param plaintext the serialized message protobuf bytes
     * @param groupJid  the group JID
     * @param senderJid the sender's device JID (usually current user)
     * @return the encrypted payload with type SKMSG
     */
    public MessageEncryptedPayload encryptForGroup(byte[] plaintext, Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(plaintext, "plaintext cannot be null");
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        var paddedPlaintext = MessagePadding.addPadding(plaintext);
        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        var ciphertext = groupCipher.encrypt(senderKeyName, paddedPlaintext);

        return new MessageEncryptedPayload(ciphertext, MessageSignalEncryptionType.SKMSG);
    }

    /**
     * Encrypts a message container for a group.
     * Convenience method that serializes the container first.
     *
     * @param message   the message container to encrypt
     * @param groupJid  the group JID
     * @param senderJid the sender's device JID
     * @return the encrypted payload with type SKMSG
     */
    public MessageEncryptedPayload encryptForGroup(MessageContainer message, Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(message, "message cannot be null");
        var plaintext = MessageContainerSpec.encode(message);
        return encryptForGroup(plaintext, groupJid, senderJid);
    }

    /**
     * Checks if a Signal session exists with the specified device.
     *
     * @param deviceJid the device JID to check
     * @return true if a session exists, false otherwise
     */
    public boolean hasSessionWith(Jid deviceJid) {
        Objects.requireNonNull(deviceJid, "deviceJid cannot be null");
        var address = deviceJid.toSignalAddress();
        return store.findSessionByAddress(address).isPresent();
    }

    /**
     * Checks if a sender key exists for the specified group and sender.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender's device JID
     * @return true if a sender key exists, false otherwise
     */
    public boolean hasSenderKey(Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        return store.findSenderKeyByName(senderKeyName).isPresent();
    }

    /**
     * Generates a sender key distribution message for distributing our sender key
     * to devices that don't have it.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender's device JID
     * @return the sender key distribution message bytes
     */
    public SignalSenderKeyDistributionMessage generateSenderKeyDistribution(Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        return groupCipher.create(senderKeyName);
    }

    /**
     * Processes a received sender key distribution message,
     * storing the sender key for future decryption.
     *
     * @param groupJid        the group JID
     * @param senderJid       the sender's device JID
     * @param distributionMsg the distribution message bytes
     */
    public void processSenderKeyDistribution(Jid groupJid, Jid senderJid, SignalSenderKeyDistributionMessage distributionMsg) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        Objects.requireNonNull(distributionMsg, "distributionMsg cannot be null");
        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        groupCipher.process(senderKeyName, distributionMsg);
    }
}
