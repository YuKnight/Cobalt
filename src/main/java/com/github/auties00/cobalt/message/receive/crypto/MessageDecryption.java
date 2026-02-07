package com.github.auties00.cobalt.message.receive.crypto;

import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.message.receive.addressing.PhoneNumberMessageAddressingMode;
import com.github.auties00.cobalt.message.send.crypto.MessageSignalEncryptionType;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.MessageContainer;
import com.github.auties00.cobalt.model.message.common.MessageContainerSpec;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.libsignal.SignalSessionCipher;
import com.github.auties00.libsignal.exception.*;
import com.github.auties00.libsignal.groups.SignalGroupCipher;
import com.github.auties00.libsignal.protocol.SignalMessage;
import com.github.auties00.libsignal.protocol.SignalPreKeyMessage;
import com.github.auties00.libsignal.protocol.SignalSenderKeyDistributionMessage;
import it.auties.protobuf.exception.ProtobufDeserializationException;
import it.auties.protobuf.stream.ProtobufInputStream;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Service for decrypting messages using the Signal Protocol.
 * Handles both 1:1 messages (using Signal sessions) and group messages (using sender keys).
 */
public final class MessageDecryption {
    private static final int MIN_PADDING = 1;
    private static final int MAX_PADDING = 16;


    private final WhatsAppStore store;
    private final SignalSessionCipher sessionCipher;
    private final SignalGroupCipher groupCipher;

    public MessageDecryption(
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
     *   <li>{@code SignalMissingSessionException} → NoSession (send retry, sender will re-send as pkmsg)</li>
     *   <li>{@code SignalUninitializedSessionException} → NoSession (session exists but not initialized)</li>
     *   <li>{@code SignalUntrustedIdentityException} → InvalidSignature (identity key changed)</li>
     *   <li>{@code SignalDecryptException} → Unknown or DuplicateMessage (based on message)</li>
     *   <li>{@code ProtobufDeserializationException} → InvalidMessage (malformed Signal message)</li>
     * </ul>
     *
     * @param ciphertext     the encrypted message bytes
     * @param senderJid      the sender's device JID
     * @param encryptionType the type of encryption (PKMSG or MSG)
     * @return the decrypted plaintext (padding removed)
     * @throws WhatsAppMessageException.Receive if decryption fails
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
                    yield removePadding(paddedPlaintext);
                } catch (ProtobufDeserializationException e) {
                    throw new WhatsAppMessageException.Receive.InvalidMessage(
                            "Invalid PreKeySignalMessage format from: " + senderJid, e);
                } catch (SignalMissingSessionException e) {
                    // This shouldn't happen for pkmsg since it establishes a session,
                    // but handle it anyway for robustness
                    throw new WhatsAppMessageException.Receive.NoSession(
                            "No session for PreKeyMessage from: " + senderJid, false, e);
                } catch (SignalUninitializedSessionException e) {
                    throw new WhatsAppMessageException.Receive.NoSession(
                            "Session not initialized for: " + senderJid, false, e);
                } catch (SignalUntrustedIdentityException e) {
                    throw new WhatsAppMessageException.Receive.InvalidSignature(
                            "Identity key changed for: " + senderJid, e);
                } catch (SignalDecryptException e) {
                    // Check for duplicate message (old counter) in error message
                    if (isDuplicateCounterError(e)) {
                        throw new WhatsAppMessageException.Receive.DuplicateMessage(
                                "Decryption failed for PreKeyMessage from: " + senderJid, e);
                    }
                    throw new WhatsAppMessageException.Receive.Unknown(
                            "Decryption failed for PreKeyMessage from: " + senderJid, e);
                } catch (SecurityException e) {
                    // Bad MAC or signature verification failure
                    throw new WhatsAppMessageException.Receive.InvalidMessage(
                            "Security verification failed for message from: " + senderJid, e);
                }
            }
            case MSG -> {
                try {
                    var message = SignalMessage.ofSerialized(ciphertext);
                    var paddedPlaintext = sessionCipher.decrypt(address, message);
                    yield removePadding(paddedPlaintext);
                } catch (ProtobufDeserializationException e) {
                    throw new WhatsAppMessageException.Receive.InvalidMessage(
                            "Invalid SignalMessage format from: " + senderJid, e);
                } catch (SignalMissingSessionException e) {
                    // MSG type requires existing session - send retry so sender re-sends as pkmsg
                    throw new WhatsAppMessageException.Receive.NoSession(
                            "No session exists for MSG from: " + senderJid, false, e);
                } catch (SignalUninitializedSessionException e) {
                    throw new WhatsAppMessageException.Receive.NoSession(
                            "Session not initialized for: " + senderJid, false, e);
                } catch (SignalUntrustedIdentityException e) {
                    throw new WhatsAppMessageException.Receive.InvalidSignature(
                            "Identity key changed for: " + senderJid, e);
                } catch (SignalDecryptException e) {
                    if (isDuplicateCounterError(e)) {
                        throw new WhatsAppMessageException.Receive.DuplicateMessage(
                                "Decryption failed for MSG from: " + senderJid, e);
                    }
                    throw new WhatsAppMessageException.Receive.Unknown(
                            "Decryption failed for MSG from: " + senderJid, e);
                } catch (SecurityException e) {
                    throw new WhatsAppMessageException.Receive.InvalidMessage(
                            "Security verification failed for message from: " + senderJid, e);
                }
            }
            case SKMSG -> throw new IllegalArgumentException("Use decryptFromGroup for SKMSG encryption type");
        };
    }

    /**
     * Removes padding from a decrypted message.
     * Reads the last byte to determine padding length and removes it.
     *
     * @param paddedPlaintext the padded plaintext bytes
     * @return the original plaintext without padding
     * @throws IllegalArgumentException if the padding is invalid
     */
    private static byte[] removePadding(byte[] paddedPlaintext) {
        Objects.requireNonNull(paddedPlaintext, "paddedPlaintext cannot be null");

        if (paddedPlaintext.length == 0) {
            throw new IllegalArgumentException("Padded plaintext cannot be empty");
        }

        // Last byte indicates padding length
        var paddingLength = paddedPlaintext[paddedPlaintext.length - 1] & 0xFF;

        if (paddingLength < MIN_PADDING || paddingLength > MAX_PADDING) {
            throw new IllegalArgumentException(
                    "Invalid padding length: " + paddingLength + " (expected " + MIN_PADDING + "-" + MAX_PADDING + ")"
            );
        }

        if (paddingLength > paddedPlaintext.length) {
            throw new IllegalArgumentException(
                    "Padding length " + paddingLength + " exceeds message length " + paddedPlaintext.length
            );
        }

        var originalLength = paddedPlaintext.length - paddingLength;
        return Arrays.copyOf(paddedPlaintext, originalLength);
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
     *   <li>{@code SignalMissingSenderKeyException} → NoSenderKey (no sender key record)</li>
     *   <li>{@code SignalMissingSenderKeyStateException} → InvalidSenderKey (key exists but no state for ID)</li>
     *   <li>{@code SignalDecryptException} → Unknown or DuplicateMessage</li>
     *   <li>{@code SecurityException} → InvalidSenderKey (signature verification failed)</li>
     * </ul>
     *
     * @param ciphertext the encrypted message bytes
     * @param groupJid   the group JID
     * @param senderJid  the sender's device JID
     * @return the decrypted plaintext (padding removed)
     * @throws WhatsAppMessageException.Receive if decryption fails
     */
    public byte[] decryptFromGroup(byte[] ciphertext, Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        var senderKeyName = PhoneNumberMessageAddressingMode.SenderKeyNameFactory.create(groupJid, senderJid);

        try {
            // groupCipher.decrypt() takes raw bytes and deserializes internally
            var paddedPlaintext = groupCipher.decrypt(senderKeyName, ciphertext);
            return removePadding(paddedPlaintext);
        } catch (SignalMissingSenderKeyException e) {
            // No sender key record exists at all - sender should re-distribute
            throw new WhatsAppMessageException.Receive.NoSenderKey(
                    "No sender key exists for group: " + groupJid + " sender: " + senderJid, e);
        } catch (SignalMissingSenderKeyStateException e) {
            // Sender key record exists but no state for the message's key ID
            // This can happen if the sender rotated their key and we have an old one
            throw new WhatsAppMessageException.Receive.InvalidSenderKey(
                    "Sender key state not found for ID " + e.id().orElse(-1) +
                            " in group: " + groupJid + " sender: " + senderJid, e);
        } catch (SignalDecryptException e) {
            if (isDuplicateCounterError(e)) {
                throw new WhatsAppMessageException.Receive.DuplicateMessage(
                        "Group decryption failed for message from: " + senderJid + " in group: " + groupJid, e);
            }
            throw new WhatsAppMessageException.Receive.Unknown(
                    "Group decryption failed for message from: " + senderJid + " in group: " + groupJid, e);
        } catch (SecurityException e) {
            // Signature verification failed on the sender key message
            throw new WhatsAppMessageException.Receive.InvalidSenderKey(
                    "Sender key signature verification failed from: " + senderJid + " in group: " + groupJid, e);
        } catch (ProtobufDeserializationException e) {
            throw new WhatsAppMessageException.Receive.InvalidMessage(
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
     * @throws WhatsAppMessageException.Receive if decryption fails
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
     * @throws WhatsAppMessageException.Receive if decryption fails
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
        var senderKeyName = PhoneNumberMessageAddressingMode.SenderKeyNameFactory.create(groupJid, senderJid);
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
        var senderKeyName = PhoneNumberMessageAddressingMode.SenderKeyNameFactory.create(groupJid, senderJid);
        return store.findSenderKeyByName(senderKeyName).isPresent();
    }

    /**
     * Extracts the sender's identity key from a PreKeySignalMessage ciphertext.
     * This is used for ADV (Account Device Verification) validation before decryption.
     *
     * <p>The PreKeySignalMessage structure contains the sender's identity key
     * which can be extracted without decrypting the message. This is used to
     * verify ADV signatures for companion devices before proceeding with decryption.
     *
     * @param ciphertext the PKMSG ciphertext bytes
     * @return the identity key bytes (32 bytes), or empty if extraction fails
     *
     * @apiNote WAWebSignalUtilsApi.extractIdentityKey: extracts identity key from
     * PreKeySignalMessage for ADV validation.
     */
    public Optional<byte[]> extractIdentityKeyFromPkmsg(byte[] ciphertext) {
        if (ciphertext == null || ciphertext.length == 0) {
            return Optional.empty();
        }

        try {
            var message = SignalPreKeyMessage.ofSerialized(ciphertext);
            var identityKey = message.identityKey();
            if (identityKey == null) {
                return Optional.empty();
            }
            return Optional.of(identityKey.toEncodedPoint());
        } catch (ProtobufDeserializationException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
