package com.github.auties00.cobalt.message.receive.crypto;

import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.message.MessageEncryptionType;
import com.github.auties00.cobalt.message.send.bot.BotMessageSecret;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.SecretMessageContainerSpec;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.libsignal.SignalSessionCipher;
import com.github.auties00.libsignal.exception.*;
import com.github.auties00.libsignal.groups.SignalGroupCipher;
import com.github.auties00.libsignal.protocol.SignalMessage;
import com.github.auties00.libsignal.protocol.SignalPreKeyMessage;
import com.github.auties00.libsignal.protocol.SignalSenderKeyDistributionMessage;
import it.auties.protobuf.exception.ProtobufDeserializationException;

import javax.crypto.Cipher;
import javax.crypto.KDF;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Decrypts incoming WhatsApp message payloads using the Signal Protocol and the MSMSG
 * bot message scheme.
 *
 * <p>Three decryption paths are exposed, one per encryption type:
 * <ul>
 *   <li><b>Per-device</b> (PKMSG/MSG): Signal session cipher for 1:1 messages, via
 *       {@link #decryptFromDevice}.</li>
 *   <li><b>Group</b> (SKMSG): Signal sender-key cipher for group messages, via
 *       {@link #decryptFromGroup}.</li>
 *   <li><b>Bot</b> (MSMSG): AES-GCM with an HKDF-derived key from the target message's
 *       {@code messageSecret}, via {@link #decryptBotMessage}.</li>
 * </ul>
 *
 * <p>Helpers for processing incoming sender-key distribution messages and for
 * inspecting existing session state are also provided.
 *
 * @implNote In WA Web a single {@code decryptEnc} dispatches to all three ciphers.
 * In Cobalt the dispatch is performed by the caller rather than by a single dispatch
 * method.
 */
@WhatsAppWebModule(moduleName = "WAWebMsgProcessingDecryptEnc")
@WhatsAppWebModule(moduleName = "WAWebSignalCipherApi")
@WhatsAppWebModule(moduleName = "WAWebBotMessageSecret")
@WhatsAppWebModule(moduleName = "WAWebCryptoLibrary")
public final class MessageDecryption {
    /**
     * Minimum valid PKCS#7 padding length.
     */
    private static final int MIN_PADDING = 1;

    /**
     * Maximum valid PKCS#7 padding length.
     */
    private static final int MAX_PADDING = 16;

    /**
     * HKDF-derived AES-GCM key size in bytes for MSMSG decryption.
     */
    @WhatsAppWebExport(moduleName = "WAWebBotMessageSecret", exports = "decryptMsmsgBotMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final int HKDF_KEY_SIZE = 32;

    /**
     * AES-GCM authentication tag size in bits for MSMSG decryption.
     */
    @WhatsAppWebExport(moduleName = "WAWebBotMessageSecret", exports = "decryptMsmsgBotMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final int AES_GCM_TAG_BITS = 128;

    /**
     * HKDF algorithm identifier for MSMSG key derivation (HKDF-SHA256).
     */
    @WhatsAppWebExport(moduleName = "WAWebBotMessageSecret", exports = "decryptMsmsgBotMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final String HKDF_ALGORITHM = "HKDF-SHA256";

    /**
     * AES-GCM transformation identifier for MSMSG decryption.
     */
    @WhatsAppWebExport(moduleName = "WAWebBotMessageSecret", exports = "decryptMsmsgBotMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";

    /**
     * WhatsApp store used for session and sender-key lookups.
     */
    private final WhatsAppStore store;

    /**
     * Signal session cipher used for per-device decryption.
     */
    private final SignalSessionCipher sessionCipher;

    /**
     * Signal group cipher used for sender-key decryption.
     */
    private final SignalGroupCipher groupCipher;

    /**
     * Constructs a new decryption service.
     *
     * @param store         the WhatsApp store for session and key lookups
     * @param sessionCipher the Signal session cipher for 1:1 decryption
     * @param groupCipher   the Signal group cipher for group decryption
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptEnc", exports = "decryptEnc",
            adaptation = WhatsAppAdaptation.ADAPTED)
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
     * Decrypts a per-device message using the Signal Protocol.
     *
     * <p>Handles both {@code PreKeySignalMessage} (PKMSG, for new sessions) and
     * {@code SignalMessage} (MSG, for existing sessions). Signal-specific exceptions
     * are mapped onto the Cobalt {@link WhatsAppMessageException.Receive} hierarchy
     * so upstream code can decide the receipt type uniformly.
     *
     * @param ciphertext     the encrypted message bytes
     * @param senderJid      the sender's device JID
     * @param encryptionType the type of encryption (PKMSG or MSG)
     * @return the decrypted plaintext with padding already removed
     * @throws WhatsAppMessageException.Receive if decryption fails
     * @throws NullPointerException             if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptEnc", exports = "decryptEnc",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebSignalCipherApi", exports = "decryptSignalProto",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebCryptoLibrary", exports = "decryptSignalProto",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public byte[] decryptFromDevice(byte[] ciphertext, Jid senderJid, MessageEncryptionType encryptionType) {
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
                    // PKMSG establishes a session, so missing-session is unusual but mapped for robustness.
                    throw new WhatsAppMessageException.Receive.NoSession(
                            "No session for PreKeyMessage from: " + senderJid, false, e);
                } catch (SignalUninitializedSessionException e) {
                    throw new WhatsAppMessageException.Receive.NoSession(
                            "Session not initialized for: " + senderJid, false, e);
                } catch (SignalUntrustedIdentityException e) {
                    throw new WhatsAppMessageException.Receive.InvalidSignature(
                            "Identity key changed for: " + senderJid, e);
                } catch (SignalDecryptException e) {
                    if (isDuplicateCounterError(e)) {
                        throw new WhatsAppMessageException.Receive.DuplicateMessage(
                                "Decryption failed for PreKeyMessage from: " + senderJid, e);
                    }
                    throw new WhatsAppMessageException.Receive.Unknown(
                            "Decryption failed for PreKeyMessage from: " + senderJid, e);
                } catch (SecurityException e) {
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
                    // MSG requires an existing session; raise NoSession so the sender can re-send as PKMSG.
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
            case MSMSG -> throw new IllegalArgumentException("Use decryptBotMessage for MSMSG encryption type");
        };
    }

    /**
     * Returns whether a {@link SignalDecryptException} indicates a duplicate-message
     * or old-counter condition.
     *
     * <p>Triggered when a message with an already-seen counter is received, typically
     * due to resends or delivery races. Cobalt inspects the exception message text
     * because libsignal does not currently expose a dedicated subtype for this
     * condition.
     *
     * @param e the decrypt exception to inspect
     * @return {@code true} if the error message matches a duplicate counter condition
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoLibrary", exports = {"decryptSignalProto", "decryptGroupSignalProto"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    private boolean isDuplicateCounterError(SignalDecryptException e) {
        var message = e.getMessage();
        return message != null && (
                message.contains("old counter") ||
                message.contains("Received message with old counter")
        );
    }

    /**
     * Decrypts a group message using Signal's sender-key scheme.
     *
     * <p>Signal-specific exceptions are mapped as follows:
     * <ul>
     *   <li>{@code SignalMissingSenderKeyException}: NoSenderKey (no record)</li>
     *   <li>{@code SignalMissingSenderKeyStateException}: InvalidSenderKey (record
     *       exists but no state for ID)</li>
     *   <li>{@code SignalDecryptException}: Unknown or DuplicateMessage</li>
     *   <li>{@code SecurityException}: InvalidSenderKey (signature verification failure)</li>
     * </ul>
     *
     * @param ciphertext the encrypted message bytes
     * @param groupJid   the group JID
     * @param senderJid  the sender's device JID
     * @return the decrypted plaintext with padding already removed
     * @throws WhatsAppMessageException.Receive if decryption fails
     * @throws NullPointerException             if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptEnc", exports = "decryptEnc",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebSignalCipherApi", exports = "decryptGroupSignalProto",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebCryptoLibrary", exports = "decryptGroupSignalProto",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public byte[] decryptFromGroup(byte[] ciphertext, Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(ciphertext, "ciphertext cannot be null");
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);

        try {
            var paddedPlaintext = groupCipher.decrypt(senderKeyName, ciphertext);
            return removePadding(paddedPlaintext);
        } catch (SignalMissingSenderKeyException e) {
            throw new WhatsAppMessageException.Receive.NoSenderKey(
                    "No sender key exists for group: " + groupJid + " sender: " + senderJid, e);
        } catch (SignalMissingSenderKeyStateException e) {
            // Record exists but no state for the message key id (sender rotated their key).
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
            throw new WhatsAppMessageException.Receive.InvalidSenderKey(
                    "Sender key signature verification failed from: " + senderJid + " in group: " + groupJid, e);
        } catch (ProtobufDeserializationException e) {
            throw new WhatsAppMessageException.Receive.InvalidMessage(
                    "Invalid SenderKeyMessage format from: " + senderJid + " in group: " + groupJid, e);
        }
    }

    /**
     * Decrypts a bot message (MSMSG) payload.
     *
     * <p>The ciphertext is a serialised {@code MessageSecretMessage} protobuf
     * containing {@code encIv} and {@code encPayload} fields. The decryption key is
     * derived from the provided {@code messageSecret} via HKDF-SHA256 with the message
     * id and sender JIDs as info; AAD equals {@code messageId + 0x00 + botSenderJid}.
     *
     * <p>The caller is responsible for resolving the {@code messageId} (from bot edit
     * target or stanza id), looking up the {@code messageSecret} from the target
     * message, and resolving the sender JIDs from stanza addressing.
     *
     * @param ciphertext      the MSMSG ciphertext (MessageSecretMessage protobuf)
     * @param messageSecret   the 32-byte secret from the target message
     * @param messageId       the message id used for key derivation and AAD
     * @param targetSenderJid the target message sender's user JID
     * @param botSenderJid    the bot's user JID
     * @return the decrypted inner plaintext
     * @throws WhatsAppMessageException.Receive if decryption fails
     * @throws NullPointerException             if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebBotMessageSecret", exports = "decryptMsmsgBotMessage",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public byte[] decryptBotMessage(
            byte[] ciphertext,
            byte[] messageSecret,
            String messageId,
            Jid targetSenderJid,
            Jid botSenderJid
    ) {
        Objects.requireNonNull(ciphertext, "ciphertext");
        Objects.requireNonNull(messageSecret, "messageSecret");
        Objects.requireNonNull(messageId, "messageId");
        Objects.requireNonNull(targetSenderJid, "targetSenderJid");
        Objects.requireNonNull(botSenderJid, "botSenderJid");

        try {
            var secretMessage = SecretMessageContainerSpec.decode(ciphertext);
            var encIv = secretMessage.encIv().orElseThrow(() ->
                    new WhatsAppMessageException.Receive.InvalidMessage(
                            "MSMSG missing encIv", null));
            var encPayload = secretMessage.encPayload().orElseThrow(() ->
                    new WhatsAppMessageException.Receive.InvalidMessage(
                            "MSMSG missing encPayload", null));

            var botSecret = BotMessageSecret.derive(messageSecret);
            var aesKey = deriveBotPerMessageKey(
                    messageId, targetSenderJid, botSenderJid, botSecret);
            var aad = buildBotAad(messageId, botSenderJid);

            var cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            var keySpec = new SecretKeySpec(aesKey, "AES");
            var gcmSpec = new GCMParameterSpec(AES_GCM_TAG_BITS, encIv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            cipher.updateAAD(aad);
            return cipher.doFinal(encPayload);
        } catch (WhatsAppMessageException.Receive e) {
            throw e;
        } catch (Exception e) {
            throw new WhatsAppMessageException.Receive.Unknown(
                    "MSMSG decryption failed", e);
        }
    }

    /**
     * Removes PKCS#7 padding from a decrypted plaintext.
     *
     * <p>Reads the last byte to determine padding length and returns the original
     * plaintext with the padding stripped.
     *
     * @param paddedPlaintext the padded plaintext bytes
     * @return the plaintext with padding removed
     * @throws IllegalArgumentException if the padding is invalid
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgProcess", exports = "processDecryptedMessageProto",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static byte[] removePadding(byte[] paddedPlaintext) {
        Objects.requireNonNull(paddedPlaintext, "paddedPlaintext cannot be null");

        if (paddedPlaintext.length == 0) {
            throw new IllegalArgumentException("Padded plaintext cannot be empty");
        }

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
     * Processes a received sender key distribution message, storing the sender key
     * for future group message decryption.
     *
     * @param groupJid        the group JID
     * @param senderJid       the sender's device JID
     * @param distributionMsg the sender key distribution message
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoLibrary", exports = "processSenderKeyDistributionMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void processSenderKeyDistribution(Jid groupJid, Jid senderJid, SignalSenderKeyDistributionMessage distributionMsg) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        Objects.requireNonNull(distributionMsg, "distributionMsg cannot be null");

        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        groupCipher.process(senderKeyName, distributionMsg);
    }

    /**
     * Processes a received sender key distribution message from raw bytes,
     * deserialising the distribution message first.
     *
     * @param groupJid         the group JID
     * @param senderJid        the sender's device JID
     * @param distributionData the raw distribution message bytes
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoLibrary", exports = "processSenderKeyDistributionMsg",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void processSenderKeyDistribution(Jid groupJid, Jid senderJid, byte[] distributionData) {
        Objects.requireNonNull(distributionData, "distributionData cannot be null");

        var distributionMsg = SignalSenderKeyDistributionMessage.ofSerialized(distributionData);
        processSenderKeyDistribution(groupJid, senderJid, distributionMsg);
    }

    /**
     * Returns whether a Signal session exists for the given device.
     *
     * @param deviceJid the device JID to check
     * @return {@code true} if a session exists, {@code false} otherwise
     * @throws NullPointerException if {@code deviceJid} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoLibrary", exports = "getRemoteRegId",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean hasSessionWith(Jid deviceJid) {
        Objects.requireNonNull(deviceJid, "deviceJid cannot be null");

        var address = deviceJid.toSignalAddress();
        return store.findSessionByAddress(address).isPresent();
    }

    /**
     * Returns whether a sender key exists for the given group and sender.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender's device JID
     * @return {@code true} if a sender key exists, {@code false} otherwise
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebCryptoLibrary", exports = "getGroupSenderKeyInfo",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public boolean hasSenderKey(Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        return store.findSenderKeyByName(senderKeyName).isPresent();
    }

    /**
     * Extracts the sender's identity key from a PreKeySignalMessage ciphertext for
     * use in ADV validation before decryption.
     *
     * <p>The PreKeySignalMessage structure carries the sender's identity key, which
     * can be extracted without decrypting the inner message. This allows verifying
     * ADV signatures for companion devices before attempting decryption.
     *
     * @param ciphertext the PKMSG ciphertext bytes
     * @return an {@link Optional} wrapping the 32-byte identity key
     */
    @WhatsAppWebExport(moduleName = "WAWebSignalUtilsApi", exports = "extractIdentityKey",
            adaptation = WhatsAppAdaptation.DIRECT)
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

    /**
     * Derives the per-message AES-GCM key for MSMSG decryption using HKDF-SHA256.
     *
     * <p>Performs HKDF-Extract with a null (all-zero) salt over the bot secret, then
     * HKDF-Expand with info built as
     * {@code messageId || targetSenderJid || botSenderJid}.
     *
     * @param messageId       the message id (or bot edit target id)
     * @param targetSenderJid the target message sender's user JID
     * @param botSenderJid    the bot's user JID
     * @param botSecret       the base bot secret derived from messageSecret
     * @return the 32-byte AES-GCM key
     * @throws GeneralSecurityException if the HKDF provider is unavailable
     */
    @WhatsAppWebExport(moduleName = "WAWebBotMessageSecret", exports = "decryptMsmsgBotMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    private byte[] deriveBotPerMessageKey(
            String messageId,
            Jid targetSenderJid,
            Jid botSenderJid,
            byte[] botSecret
    ) throws GeneralSecurityException {
        var idBytes = messageId.getBytes(StandardCharsets.UTF_8);
        var targetBytes = targetSenderJid.toString().getBytes(StandardCharsets.UTF_8);
        var botBytes = botSenderJid.toString().getBytes(StandardCharsets.UTF_8);

        var info = new byte[idBytes.length + targetBytes.length + botBytes.length];
        System.arraycopy(idBytes, 0, info, 0, idBytes.length);
        System.arraycopy(targetBytes, 0, info, idBytes.length, targetBytes.length);
        System.arraycopy(botBytes, 0, info, idBytes.length + targetBytes.length, botBytes.length);

        var kdf = KDF.getInstance(HKDF_ALGORITHM);
        var params = HKDFParameterSpec.ofExtract()
                .addIKM(botSecret)
                .thenExpand(info, HKDF_KEY_SIZE);
        return kdf.deriveData(params);
    }

    /**
     * Builds the AAD for MSMSG AES-GCM decryption as
     * {@code messageId + 0x00 + botSenderJid}.
     *
     * @param messageId    the message id
     * @param botSenderJid the bot's user JID
     * @return the AAD bytes
     */
    @WhatsAppWebExport(moduleName = "WAWebBotMessageSecret", exports = "decryptMsmsgBotMessage",
            adaptation = WhatsAppAdaptation.DIRECT)
    private byte[] buildBotAad(String messageId, Jid botSenderJid) {
        var idBytes = messageId.getBytes(StandardCharsets.UTF_8);
        var botBytes = botSenderJid.toString().getBytes(StandardCharsets.UTF_8);
        var aad = new byte[idBytes.length + 1 + botBytes.length];
        System.arraycopy(idBytes, 0, aad, 0, idBytes.length);
        aad[idBytes.length] = 0x00;
        System.arraycopy(botBytes, 0, aad, idBytes.length + 1, botBytes.length);
        return aad;
    }
}
