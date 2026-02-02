package com.github.auties00.cobalt.message.receive.decryption;

import com.github.auties00.cobalt.exception.MessageDecryptionException;
import com.github.auties00.cobalt.exception.MessageDecryptionException.Reason;
import com.github.auties00.cobalt.message.protocol.MessagePadding;
import com.github.auties00.cobalt.message.protocol.MessageSignalEncryptionType;
import com.github.auties00.cobalt.message.protocol.SenderKeyNameFactory;
import com.github.auties00.cobalt.message.send.encryption.MessageEncryptionService;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.model.MessageContainer;
import com.github.auties00.cobalt.model.message.model.MessageContainerSpec;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.libsignal.SignalSessionCipher;
import com.github.auties00.libsignal.exception.SignalDecryptException;
import com.github.auties00.libsignal.exception.SignalMissingSenderKeyException;
import com.github.auties00.libsignal.exception.SignalMissingSenderKeyStateException;
import com.github.auties00.libsignal.exception.SignalMissingSessionException;
import com.github.auties00.libsignal.exception.SignalUninitializedSessionException;
import com.github.auties00.libsignal.exception.SignalUntrustedIdentityException;
import com.github.auties00.libsignal.groups.SignalGroupCipher;
import com.github.auties00.libsignal.protocol.SignalMessage;
import com.github.auties00.libsignal.protocol.SignalPreKeyMessage;
import com.github.auties00.libsignal.protocol.SignalSenderKeyDistributionMessage;
import it.auties.protobuf.exception.ProtobufDeserializationException;
import it.auties.protobuf.stream.ProtobufInputStream;

import java.util.Objects;

/**
 * Service for decrypting messages using the Signal Protocol.
 * Handles both 1:1 messages (using Signal sessions) and group messages (using sender keys).
 * This is the counterpart to {@link MessageEncryptionService}.
 */
public final class MessageDecryptionService {
    private final WhatsAppStore store;
    private final SignalSessionCipher sessionCipher;
    private final SignalGroupCipher groupCipher;

    public MessageDecryptionService(
            WhatsAppStore store,
            SignalSessionCipher sessionCipher,
            SignalGroupCipher groupCipher
    ) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.sessionCipher = Objects.requireNonNull(sessionCipher, "sessionCipher cannot be null");
        this.groupCipher = Objects.requireNonNull(groupCipher, "groupCipher cannot be null");
    }

    /**
     * Decrypts a message from a specific device using Signal Protocol.
     * Handles both PreKeySignalMessage (pkmsg) for new sessions and SignalMessage (msg) for existing sessions.
     *
     * <p>Signal Exception Mapping:
     * <ul>
     *   <li>{@code SignalMissingSessionException} → SESSION_NOT_FOUND (send retry, sender will re-send as pkmsg)</li>
     *   <li>{@code SignalUninitializedSessionException} → SESSION_NOT_FOUND (session exists but not initialized)</li>
     *   <li>{@code SignalUntrustedIdentityException} → UNTRUSTED_IDENTITY (identity key changed)</li>
     *   <li>{@code SignalDecryptException} → DECRYPT_FAILED or DUPLICATE_MESSAGE (based on message)</li>
     *   <li>{@code ProtobufDeserializationException} → INVALID_MESSAGE (malformed Signal message)</li>
     * </ul>
     *
     * @param ciphertext     the encrypted message bytes
     * @param senderJid      the sender's device JID
     * @param encryptionType the type of encryption (PKMSG or MSG)
     * @return the decrypted plaintext (padding removed)
     * @throws MessageDecryptionException if decryption fails
     */
    public byte[] decryptFromDevice(byte[] ciphertext, Jid senderJid, MessageSignalEncryptionType encryptionType) {
        Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        Objects.requireNonNull(encryptionType, "encryptionType cannot be null");

        var address = senderJid.toSignalAddress();
        return switch (encryptionType) {
            case PKMSG -> {
                try {
                    var message = SignalPreKeyMessage.ofSerialized(ciphertext);
                    var paddedPlaintext = sessionCipher.decrypt(address, message);
                    yield MessagePadding.removePadding(paddedPlaintext);
                } catch (ProtobufDeserializationException e) {
                    throw new MessageDecryptionException(Reason.INVALID_MESSAGE,
                            "Invalid PreKeySignalMessage format from: " + senderJid, e);
                } catch (SignalMissingSessionException e) {
                    // This shouldn't happen for pkmsg since it establishes a session,
                    // but handle it anyway for robustness
                    throw new MessageDecryptionException(Reason.SESSION_NOT_FOUND,
                            "No session for PreKeyMessage from: " + senderJid, e);
                } catch (SignalUninitializedSessionException e) {
                    throw new MessageDecryptionException(Reason.SESSION_NOT_FOUND,
                            "Session not initialized for: " + senderJid, e);
                } catch (SignalUntrustedIdentityException e) {
                    throw new MessageDecryptionException(Reason.UNTRUSTED_IDENTITY,
                            "Identity key changed for: " + senderJid, e);
                } catch (SignalDecryptException e) {
                    // Check for duplicate message (old counter) in error message
                    var reason = isDuplicateCounterError(e) ? Reason.DUPLICATE_MESSAGE : Reason.DECRYPT_FAILED;
                    throw new MessageDecryptionException(reason,
                            "Decryption failed for PreKeyMessage from: " + senderJid, e);
                } catch (SecurityException e) {
                    // Bad MAC or signature verification failure
                    throw new MessageDecryptionException(Reason.INVALID_MESSAGE,
                            "Security verification failed for message from: " + senderJid, e);
                }
            }
            case MSG -> {
                try {
                    var message = SignalMessage.ofSerialized(ciphertext);
                    var paddedPlaintext = sessionCipher.decrypt(address, message);
                    yield MessagePadding.removePadding(paddedPlaintext);
                } catch (ProtobufDeserializationException e) {
                    throw new MessageDecryptionException(Reason.INVALID_MESSAGE,
                            "Invalid SignalMessage format from: " + senderJid, e);
                } catch (SignalMissingSessionException e) {
                    // MSG type requires existing session - send retry so sender re-sends as pkmsg
                    throw new MessageDecryptionException(Reason.SESSION_NOT_FOUND,
                            "No session exists for MSG from: " + senderJid, e);
                } catch (SignalUninitializedSessionException e) {
                    throw new MessageDecryptionException(Reason.SESSION_NOT_FOUND,
                            "Session not initialized for: " + senderJid, e);
                } catch (SignalUntrustedIdentityException e) {
                    throw new MessageDecryptionException(Reason.UNTRUSTED_IDENTITY,
                            "Identity key changed for: " + senderJid, e);
                } catch (SignalDecryptException e) {
                    var reason = isDuplicateCounterError(e) ? Reason.DUPLICATE_MESSAGE : Reason.DECRYPT_FAILED;
                    throw new MessageDecryptionException(reason,
                            "Decryption failed for MSG from: " + senderJid, e);
                } catch (SecurityException e) {
                    throw new MessageDecryptionException(Reason.INVALID_MESSAGE,
                            "Security verification failed for message from: " + senderJid, e);
                }
            }
            case SKMSG -> throw new IllegalArgumentException("Use decryptFromGroup for SKMSG encryption type");
        };
    }

    /**
     * Checks if a SignalDecryptException is due to duplicate message counter (old counter error).
     * This happens when we receive a message with a counter we've already seen.
     * Per the design doc (edge case 59), these should be processed normally with delivery receipt.
     */
    private boolean isDuplicateCounterError(SignalDecryptException e) {
        var message = e.getMessage();
        return message != null && (
                message.contains("old counter") ||
                message.contains("Received message with old counter")
        );
    }

    /**
     * Decrypts a group message using Sender Key encryption.
     *
     * <p>Signal Exception Mapping:
     * <ul>
     *   <li>{@code SignalMissingSenderKeyException} → NO_SENDER_KEY (no sender key record)</li>
     *   <li>{@code SignalMissingSenderKeyStateException} → INVALID_SENDER_KEY (key exists but no state for ID)</li>
     *   <li>{@code SignalDecryptException} → DECRYPT_FAILED or DUPLICATE_MESSAGE</li>
     *   <li>{@code SecurityException} → INVALID_SENDER_KEY (signature verification failed)</li>
     * </ul>
     *
     * @param ciphertext the encrypted message bytes
     * @param groupJid   the group JID
     * @param senderJid  the sender's device JID
     * @return the decrypted plaintext (padding removed)
     * @throws MessageDecryptionException if decryption fails
     */
    public byte[] decryptFromGroup(byte[] ciphertext, Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);

        try {
            // groupCipher.decrypt() takes raw bytes and deserializes internally
            var paddedPlaintext = groupCipher.decrypt(senderKeyName, ciphertext);
            return MessagePadding.removePadding(paddedPlaintext);
        } catch (SignalMissingSenderKeyException e) {
            // No sender key record exists at all - sender should re-distribute
            throw new MessageDecryptionException(Reason.NO_SENDER_KEY,
                    "No sender key exists for group: " + groupJid + " sender: " + senderJid, e);
        } catch (SignalMissingSenderKeyStateException e) {
            // Sender key record exists but no state for the message's key ID
            // This can happen if the sender rotated their key and we have an old one
            throw new MessageDecryptionException(Reason.INVALID_SENDER_KEY,
                    "Sender key state not found for ID " + e.id().orElse(-1) +
                            " in group: " + groupJid + " sender: " + senderJid, e);
        } catch (SignalDecryptException e) {
            var reason = isDuplicateCounterError(e) ? Reason.DUPLICATE_MESSAGE : Reason.DECRYPT_FAILED;
            throw new MessageDecryptionException(reason,
                    "Group decryption failed for message from: " + senderJid + " in group: " + groupJid, e);
        } catch (SecurityException e) {
            // Signature verification failed on the sender key message
            throw new MessageDecryptionException(Reason.INVALID_SENDER_KEY,
                    "Sender key signature verification failed from: " + senderJid + " in group: " + groupJid, e);
        } catch (ProtobufDeserializationException e) {
            throw new MessageDecryptionException(Reason.INVALID_MESSAGE,
                    "Invalid SenderKeyMessage format from: " + senderJid + " in group: " + groupJid, e);
        }
    }

    /**
     * Decrypts a message and decodes it into a MessageContainer.
     * This is a convenience method that combines decryption and protobuf decoding.
     *
     * @param ciphertext     the encrypted message bytes
     * @param senderJid      the sender's device JID
     * @param encryptionType the type of encryption
     * @return the decoded MessageContainer
     * @throws MessageDecryptionException if decryption fails
     */
    public MessageContainer decryptAndDecodeFromDevice(byte[] ciphertext, Jid senderJid, MessageSignalEncryptionType encryptionType) {
        var plaintext = decryptFromDevice(ciphertext, senderJid, encryptionType);
        return MessageContainerSpec.decode(ProtobufInputStream.fromBytes(plaintext));
    }

    /**
     * Decrypts a group message and decodes it into a MessageContainer.
     *
     * @param ciphertext the encrypted message bytes
     * @param groupJid   the group JID
     * @param senderJid  the sender's device JID
     * @return the decoded MessageContainer
     * @throws MessageDecryptionException if decryption fails
     */
    public MessageContainer decryptAndDecodeFromGroup(byte[] ciphertext, Jid groupJid, Jid senderJid) {
        var plaintext = decryptFromGroup(ciphertext, groupJid, senderJid);
        return MessageContainerSpec.decode(ProtobufInputStream.fromBytes(plaintext));
    }

    /**
     * Processes a received sender key distribution message,
     * storing the sender key for future group message decryption.
     *
     * @param groupJid        the group JID
     * @param senderJid       the sender's device JID
     * @param distributionMsg the sender key distribution message
     */
    public void processSenderKeyDistribution(Jid groupJid, Jid senderJid, SignalSenderKeyDistributionMessage distributionMsg) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        Objects.requireNonNull(distributionMsg, "distributionMsg cannot be null");
        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        groupCipher.process(senderKeyName, distributionMsg);
    }

    /**
     * Processes a received sender key distribution message from raw bytes.
     *
     * @param groupJid         the group JID
     * @param senderJid        the sender's device JID
     * @param distributionData the raw distribution message bytes
     */
    public void processSenderKeyDistribution(Jid groupJid, Jid senderJid, byte[] distributionData) {
        Objects.requireNonNull(distributionData, "distributionData cannot be null");
        var distributionMsg = SignalSenderKeyDistributionMessage.ofSerialized(distributionData);
        processSenderKeyDistribution(groupJid, senderJid, distributionMsg);
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
}
