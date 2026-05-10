package com.github.auties00.cobalt.message.send.crypto;

import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.message.MessageEncryptionType;
import com.github.auties00.cobalt.message.receive.crypto.MessageDecryption;
import com.github.auties00.cobalt.message.receive.crypto.SenderKeyNameFactory;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.cobalt.util.DataUtils;
import com.github.auties00.libsignal.SignalSessionCipher;
import com.github.auties00.libsignal.groups.SignalGroupCipher;
import com.github.auties00.libsignal.protocol.SignalSenderKeyDistributionMessage;

import java.util.Objects;

/**
 * Encrypts outgoing messages using the Signal Protocol. Handles both 1:1
 * messages over Signal sessions and group messages over sender keys, and is
 * the sending counterpart to {@link MessageDecryption}.
 */
@WhatsAppWebModule(moduleName = "WAWebEncryptMsgProtobuf")
@WhatsAppWebModule(moduleName = "WAWebBackendJobsCommon")
@WhatsAppWebModule(moduleName = "WAWebSignalCipherApi")
@WhatsAppWebModule(moduleName = "WAWebSignalSessionApi")
@WhatsAppWebModule(moduleName = "WAWebCryptoLibrary")
@WhatsAppWebModule(moduleName = "WASignalGroupCipher")
public final class MessageEncryption {
    /**
     * Holds the logger used for encryption diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(MessageEncryption.class.getName());

    /**
     * Holds the current ciphertext version written into outgoing {@code <enc>}
     * stanzas.
     */
    @WhatsAppWebExport(moduleName = "WAWebBackendJobsCommon", exports = "CIPHERTEXT_VERSION",
            adaptation = WhatsAppAdaptation.DIRECT)
    public static final int CIPHERTEXT_VERSION = 2;

    /**
     * Holds the store consulted for Signal session and sender-key lookups.
     */
    private final WhatsAppStore store;

    /**
     * Holds the per-device Signal session cipher.
     */
    private final SignalSessionCipher sessionCipher;

    /**
     * Holds the sender-key group cipher.
     */
    private final SignalGroupCipher groupCipher;

    /**
     * Holds the minimum number of random padding bytes appended to a plaintext
     * before encryption.
     */
    private static final int MIN_PADDING = 1;

    /**
     * Holds the maximum number of random padding bytes appended to a plaintext
     * before encryption.
     */
    private static final int MAX_PADDING = 16;

    /**
     * Constructs an encryption service bound to the given dependencies.
     *
     * @param store         the store providing Signal protocol state
     * @param sessionCipher the cipher used for 1:1 encryption
     * @param groupCipher   the cipher used for sender-key encryption
     */
    @WhatsAppWebExport(moduleName = "WAWebEncryptMsgProtobuf", exports = {"encryptMsgProtobuf", "encryptMsgSenderKey"},
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MessageEncryption(
            WhatsAppStore store,
            SignalSessionCipher sessionCipher,
            SignalGroupCipher groupCipher
    ) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.sessionCipher = Objects.requireNonNull(sessionCipher, "sessionCipher cannot be null");
        this.groupCipher = Objects.requireNonNull(groupCipher, "groupCipher cannot be null");
    }

    /**
     * Encrypts the given plaintext for a specific recipient device. The
     * plaintext is padded with one to sixteen random bytes before encryption,
     * and the result is either a {@code PreKeySignalMessage} for new sessions
     * or a {@code SignalMessage} for established ones.
     *
     * @param recipientJid the recipient device JID
     * @param plaintext    the protobuf-encoded plaintext bytes
     * @return the encrypted payload along with its type
     * @throws NullPointerException                    if any argument is {@code null}
     * @throws WhatsAppMessageException.Send.Unknown if encryption fails
     */
    @WhatsAppWebExport(moduleName = "WAWebEncryptMsgProtobuf", exports = "encryptMsgProtobuf",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebSignalCipherApi", exports = "encryptSignalProto",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebCryptoLibrary", exports = "encryptSignalProto",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MessageEncryptedPayload encryptForDevice(Jid recipientJid, byte[] plaintext) {
        Objects.requireNonNull(recipientJid, "recipientJid cannot be null");
        Objects.requireNonNull(plaintext, "plaintext cannot be null");

        var paddedPlaintext = addPadding(plaintext);
        var address = recipientJid.toSignalAddress();

        try {
            var ciphertextMessage = sessionCipher.encrypt(address, paddedPlaintext);
            var encryptionType = MessageEncryptionType.fromSignalCiphertext(ciphertextMessage);

            LOGGER.log(System.Logger.Level.DEBUG,
                    "Encrypted message for {0}, type={1}",
                    recipientJid, encryptionType);

            return new MessageEncryptedPayload(
                    encryptionType,
                    ciphertextMessage.toSerialized(),
                    recipientJid
            );
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "encryptMsgProtobuf: encryption fail for {0}: {1}",
                    recipientJid, e.getMessage());

            try {
                store.removeSession(address);
                LOGGER.log(System.Logger.Level.DEBUG,
                        "Removed stale session for {0} after encryption failure",
                        recipientJid);
            } catch (Exception cleanupError) {
                LOGGER.log(System.Logger.Level.DEBUG,
                        "Failed to cleanup session for {0}: {1}",
                        recipientJid, cleanupError.getMessage());
            }

            throw new WhatsAppMessageException.Send.Unknown(
                    "Failed to encrypt message for device: " + recipientJid, e
            );
        }
    }

    /**
     * Encrypts the given plaintext for a group using sender-key encryption.
     * The same ciphertext is delivered to every group member who already holds
     * the corresponding {@code SenderKeyDistributionMessage}.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender device JID, PN or LID depending on addressing mode
     * @param plaintext the protobuf-encoded plaintext bytes
     * @return the SKMSG-typed encrypted payload
     * @throws NullPointerException                    if any argument is {@code null}
     * @throws WhatsAppMessageException.Send.Unknown if encryption fails
     */
    @WhatsAppWebExport(moduleName = "WAWebEncryptMsgProtobuf", exports = "encryptMsgSenderKey",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebSignalCipherApi", exports = "encryptSenderKeyMsgSignalProto",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebCryptoLibrary", exports = "encryptSenderKeyMsgSignalProto",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WASignalGroupCipher", exports = "encryptSenderKeyMsgWithSession",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public MessageEncryptedPayload encryptForGroup(Jid groupJid, Jid senderJid, byte[] plaintext) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        Objects.requireNonNull(plaintext, "plaintext cannot be null");

        var paddedPlaintext = addPadding(plaintext);
        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);

        try {
            var ciphertextMessage = groupCipher.encrypt(senderKeyName, paddedPlaintext);

            LOGGER.log(System.Logger.Level.DEBUG,
                    "Encrypted group message for {0}, sender={1}",
                    groupJid, senderJid);

            return new MessageEncryptedPayload(
                    MessageEncryptionType.SKMSG,
                    ciphertextMessage.toSerialized(),
                    null
            );
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "encryptMsgSenderKey: encryption fail for {0}: {1}",
                    groupJid, e.getMessage());
            throw new WhatsAppMessageException.Send.Unknown(
                    "Failed to encrypt group message for group: " + groupJid, e
            );
        }
    }

    /**
     * Appends one to sixteen random padding bytes to the given plaintext.
     * Every padding byte carries the padding length value, matching WA Web's
     * {@code writeRandomPadMax16} layout.
     *
     * @param plaintext the unpadded plaintext bytes
     * @return the padded plaintext
     * @throws NullPointerException if {@code plaintext} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "encodeAndPad",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebSignalCommonUtils", exports = "writeRandomPadMax16",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WACryptoPkcs7", exports = "writeRandomPadMax16",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static byte[] addPadding(byte[] plaintext) {
        Objects.requireNonNull(plaintext, "plaintext cannot be null");

        var paddingLength = MIN_PADDING + (DataUtils.randomByteArray(1)[0] & 0x0F);

        var padded = new byte[plaintext.length + paddingLength];
        System.arraycopy(plaintext, 0, padded, 0, plaintext.length);

        for (var i = plaintext.length; i < padded.length; i++) {
            padded[i] = (byte) paddingLength;
        }

        return padded;
    }

    /**
     * Creates a sender-key distribution message for the given group/sender
     * pair. The distribution message must reach members that do not yet hold
     * the sender's key before they can decrypt SKMSG payloads.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender device JID
     * @return the sender-key distribution message
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSignalSessionApi", exports = "getGroupSenderKeyInfo",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WASignalGroupCipher", exports = "createSenderKeyDistributionProto",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public SignalSenderKeyDistributionMessage createSenderKeyDistributionMessage(Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        return groupCipher.create(senderKeyName);
    }

    /**
     * Returns the serialised sender-key distribution bytes for the given
     * group/sender pair.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender device JID
     * @return the serialised distribution bytes
     */
    @WhatsAppWebExport(moduleName = "WAWebGetGroupKeyDistributionMsg", exports = "getKeyDistributionMsg",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public byte[] getSenderKeyBytes(Jid groupJid, Jid senderJid) {
        var distributionMessage = createSenderKeyDistributionMessage(groupJid, senderJid);
        return distributionMessage.toSerialized();
    }

    /**
     * Deletes the sender key for the given group, forcing it to be regenerated
     * on the next send. This is required after participants are removed.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender device JID
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSignalSessionApi", exports = "deleteGroupSenderKeyInfo",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void rotateSenderKey(Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        store.removeSenderKeys(senderKeyName);

        LOGGER.log(System.Logger.Level.DEBUG,
                "Rotated sender key for group {0}, sender {1}",
                groupJid, senderJid);
    }

    /**
     * Returns whether a Signal session already exists with the given device.
     *
     * @param deviceJid the device JID to query
     * @return {@code true} when a session exists
     * @throws NullPointerException if {@code deviceJid} is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebSignalSessionApi", exports = "hasSignalSessions",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean hasSessionWith(Jid deviceJid) {
        Objects.requireNonNull(deviceJid, "deviceJid cannot be null");
        var address = deviceJid.toSignalAddress();
        return store.findSessionByAddress(address).isPresent();
    }

    /**
     * Returns whether a sender key already exists for the given group/sender pair.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender device JID
     * @return {@code true} when a sender key is present
     * @throws NullPointerException if any argument is {@code null}
     */
    public boolean hasSenderKey(Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        return store.findSenderKeyByName(senderKeyName).isPresent();
    }

}
