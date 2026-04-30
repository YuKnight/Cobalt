package com.github.auties00.cobalt.message.send.senderkey;

import com.github.auties00.cobalt.device.DeviceService;
import com.github.auties00.cobalt.device.icdc.IcdcResult;
import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryption;
import com.github.auties00.cobalt.message.send.crypto.MessageEncryptedPayload;
import com.github.auties00.cobalt.message.send.icdc.IcdcEnricher;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.MessageContainer;
import com.github.auties00.cobalt.model.message.MessageContainerSpec;
import com.github.auties00.cobalt.model.message.system.DeviceSentMessageBuilder;
import com.github.auties00.cobalt.model.message.group.SenderKeyDistributionMessageBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.util.*;

/**
 * Distributes the sender's group encryption key to participants that do not yet possess it.
 *
 * <p>Before a group message encrypted with a sender key (SKMSG) can be decrypted by a
 * participant, that participant must have received the sender's
 * {@code SenderKeyDistributionMessage}. This service builds the per-device encrypted
 * distribution messages, populates ICDC metadata per recipient and wraps companion device
 * payloads in {@code DeviceSentMessage}.
 */
@WhatsAppWebModule(moduleName = "WAWebGetGroupKeyDistributionMsg")
public final class SenderKeyDistribution {
    /**
     * Logger for sender-key distribution diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(SenderKeyDistribution.class.getName());

    /**
     * Encryption service used to encrypt the distribution payload per device.
     */
    private final MessageEncryption encryption;

    /**
     * Device service used to compute ICDC metadata for sender and recipient JIDs.
     */
    private final DeviceService deviceService;

    /**
     * Store used to resolve the self JID and update the identity range after distribution.
     */
    private final WhatsAppStore store;

    /**
     * Creates a new sender-key distribution service.
     *
     * @param encryption    the encryption service for per-device encryption
     * @param deviceService the device service for ICDC and sessions
     * @param store         the store for self JID and identity range
     * @throws NullPointerException if any argument is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebGetGroupKeyDistributionMsg", exports = "getKeyDistributionMsg",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public SenderKeyDistribution(
            MessageEncryption encryption,
            DeviceService deviceService,
            WhatsAppStore store
    ) {
        this.encryption = Objects.requireNonNull(encryption, "encryption");
        this.deviceService = Objects.requireNonNull(deviceService, "deviceService");
        this.store = Objects.requireNonNull(store, "store");
    }

    /**
     * Encrypts the sender-key distribution message for each device without DSM wrapping
     * and without a participant hash.
     *
     * <p>Convenience overload equivalent to calling
     * {@link #encrypt(Jid, byte[], Collection, boolean, String)} with
     * {@code shouldWrapDsm = false} and {@code phash = null}. Matches the group SKMSG and
     * status sending flows where DSM wrapping is not performed at the key distribution
     * level.
     *
     * @param groupJid       the group JID
     * @param senderKeyBytes the serialised {@code SenderKeyDistributionMessage} from the
     *                       Signal group cipher
     * @param devices        the device JIDs that need the sender key
     * @return the per-device encrypted payloads
     * @throws NullPointerException          if any argument is {@code null}
     * @throws WhatsAppMessageException.Send if encryption fails for a primary device
     */
    @WhatsAppWebExport(moduleName = "WAWebGetGroupKeyDistributionMsg", exports = "getKeyDistributionMsg",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public List<MessageEncryptedPayload> encrypt(
            Jid groupJid,
            byte[] senderKeyBytes,
            Collection<Jid> devices
    ) {
        return encrypt(groupJid, senderKeyBytes, devices, false, null);
    }

    /**
     * Encrypts the sender-key distribution message for each device, populating ICDC
     * metadata per recipient.
     *
     * <p>Each device receives the distribution protobuf encrypted individually via the
     * Signal session cipher (pkmsg or msg). When {@code shouldWrapDsm} is {@code true},
     * companion devices receive a {@code DeviceSentMessage} wrapper with the group JID as
     * destination and the optional {@code phash}. All devices receive ICDC metadata
     * appropriate to their role: sender-only for self, sender plus recipient for others.
     * Devices that fail encryption are logged and dropped, except primary devices whose
     * failure is propagated.
     *
     * @param groupJid       the group JID
     * @param senderKeyBytes the serialised {@code SenderKeyDistributionMessage} from the
     *                       Signal group cipher
     * @param devices        the device JIDs that need the sender key
     * @param shouldWrapDsm  whether to wrap self-device protos in a
     *                       {@code DeviceSentMessage}
     * @param phash          the participant hash to include in the DSM, or {@code null}
     * @return the per-device encrypted payloads
     * @throws NullPointerException          if {@code groupJid}, {@code senderKeyBytes},
     *                                       or {@code devices} is {@code null}
     * @throws WhatsAppMessageException.Send if encryption fails for a primary device
     * @implNote Session establishment for the distribution devices is the caller's
     * responsibility. {@code WAWebSendGroupSkmsgJob} invokes {@code ensureE2ESessions}
     * before this function and emits the {@code PrekeysDepletionEvent}; Cobalt's
     * {@code GroupMessageSender} mirrors that by calling
     * {@code deviceService.ensureSessions} on the distribution devices before invoking
     * this method.
     */
    @WhatsAppWebExport(moduleName = "WAWebGetGroupKeyDistributionMsg", exports = "getKeyDistributionMsg",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public List<MessageEncryptedPayload> encrypt(
            Jid groupJid,
            byte[] senderKeyBytes,
            Collection<Jid> devices,
            boolean shouldWrapDsm,
            String phash
    ) {
        Objects.requireNonNull(groupJid, "groupJid");
        Objects.requireNonNull(senderKeyBytes, "senderKeyBytes");
        Objects.requireNonNull(devices, "devices");

        var skDistMessage = new SenderKeyDistributionMessageBuilder()
                .groupJid(groupJid)
                .axolotlSenderKeyDistributionMessage(senderKeyBytes)
                .build();
        var baseContainer = MessageContainer.of(skDistMessage);

        var protosByUser = generateMsgProtobufs(
                baseContainer, devices, shouldWrapDsm, groupJid, phash);

        var results = new ArrayList<MessageEncryptedPayload>(devices.size());
        for (var device : devices) {
            try {
                var userKey = device.toUserJid();
                var proto = protosByUser.getOrDefault(userKey, baseContainer);
                var plaintext = MessageContainerSpec.encode(proto);
                var payload = encryption.encryptForDevice(device, plaintext);
                results.add(payload);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "getKeyDistributionMsg: encryption fail for {0}: {1}",
                        device, e.getMessage());
                if (isPrimaryDevice(device)) {
                    throw new WhatsAppMessageException.Send.Unknown(
                            "getKeyDistributionMsg: encryption fail for primary device " + device, e);
                }
            }
        }

        store.updateIdentityRange(devices);

        return Collections.unmodifiableList(results);
    }

    /**
     * Encrypts a companion device-sent message that carries the participant hash for
     * group sync.
     *
     * <p>Used by the broadcast flow to deliver a DSM-wrapped message with phash to
     * companion (self-account) devices. The message is wrapped as a
     * {@code DeviceSentMessage} with the group JID as destination and the phash set, then
     * enriched with the sender's ICDC metadata. No recipient ICDC is emitted because the
     * targets are self devices.
     *
     * @param message          the message proto to wrap as DSM
     * @param companionDevices the companion device JIDs
     * @param phash            the participant hash to include in the DSM
     * @param groupJid         the group/broadcast JID for the DSM destination
     * @return the per-device encrypted payloads, or {@code null} if {@code companionDevices}
     *         is empty or all encryption attempts fail
     * @throws NullPointerException if {@code message}, {@code phash}, or {@code groupJid}
     *                              is {@code null}
     */
    @WhatsAppWebExport(moduleName = "WAWebGetGroupKeyDistributionMsg", exports = "getCompanionDsmPhashMsg",
            adaptation = WhatsAppAdaptation.ADAPTED)
    @WhatsAppWebExport(moduleName = "WAWebDeviceSentMessageProtoUtils", exports = "wrapDeviceSentMessage",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public List<MessageEncryptedPayload> encryptCompanionDsmPhash(
            MessageContainer message,
            Collection<Jid> companionDevices,
            String phash,
            Jid groupJid
    ) {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(phash, "phash");
        Objects.requireNonNull(groupJid, "groupJid");

        if (companionDevices == null || companionDevices.isEmpty()) {
            return null;
        }

        var selfJid = store.jid().orElse(null);
        IcdcResult senderIcdc = null;
        if (selfJid != null) {
            senderIcdc = deviceService.computeIcdc(selfJid).orElse(null);
        }

        var dsm = new DeviceSentMessageBuilder()
                .destinationJid(groupJid)
                .messageContainer(message)
                .phash(phash)
                .build();
        var dsmContainer = MessageContainer.of(dsm);

        var enriched = IcdcEnricher.enrich(dsmContainer, senderIcdc, null);
        var plaintext = MessageContainerSpec.encode(enriched);

        var results = new ArrayList<MessageEncryptedPayload>(companionDevices.size());
        for (var device : companionDevices) {
            try {
                var payload = encryption.encryptForDevice(device, plaintext);
                results.add(payload);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.WARNING,
                        "getCompanionDsmPhashMsg: encryption fail for {0}: {1}",
                        device, e.getMessage());
            }
        }

        return results.isEmpty() ? null : results;
    }

    /**
     * Precalculates per-user message protos with ICDC metadata and optional DSM wrapping.
     *
     * <p>Deduplicates devices by user JID, computes the sender's ICDC metadata once, then
     * for each unique user wraps the proto as a {@code DeviceSentMessage} when the user
     * is self and {@code shouldWrapDsm} is {@code true}, computes the recipient's ICDC
     * metadata when the user is a different account, and finally populates ICDC on the
     * proto.
     *
     * @param baseContainer the base message container with the SK distribution
     * @param devices       the device JIDs to process
     * @param shouldWrapDsm whether to wrap self-device protos as DSM
     * @param groupJid      the group JID for DSM destination
     * @param phash         the participant hash for DSM, or {@code null}
     * @return a map from user JID to enriched message container
     */
    @WhatsAppWebExport(moduleName = "WAWebGetGroupKeyDistributionMsg", exports = "generateMsgProtobufs",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebDeviceSentMessageProtoUtils", exports = "wrapDeviceSentMessage",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private Map<Jid, MessageContainer> generateMsgProtobufs(
            MessageContainer baseContainer,
            Collection<Jid> devices,
            boolean shouldWrapDsm,
            Jid groupJid,
            String phash
    ) {
        var selfJid = store.jid().orElse(null);

        IcdcResult senderIcdc = null;
        if (selfJid != null) {
            senderIcdc = deviceService.computeIcdc(selfJid).orElse(null);
        }

        var uniqueUsers = devices.stream()
                .map(Jid::toUserJid)
                .distinct()
                .toList();

        var result = new HashMap<Jid, MessageContainer>(uniqueUsers.size());

        for (var userJid : uniqueUsers) {
            var isSelf = selfJid != null && userJid.equals(selfJid.toUserJid());

            MessageContainer proto;

            IcdcResult recipientIcdc = null;
            if (isSelf) {
                if (shouldWrapDsm) {
                    var dsmBuilder = new DeviceSentMessageBuilder()
                            .destinationJid(groupJid)
                            .messageContainer(baseContainer);
                    if (phash != null) {
                        dsmBuilder.phash(phash);
                    }
                    proto = MessageContainer.of(dsmBuilder.build());
                } else {
                    proto = baseContainer;
                }
            } else {
                proto = baseContainer;
                recipientIcdc = deviceService.computeIcdc(userJid).orElse(null);
            }

            proto = IcdcEnricher.enrich(proto, senderIcdc, recipientIcdc);
            result.put(userJid, proto);
        }

        return result;
    }

    /**
     * Returns whether the given device JID identifies a primary device.
     *
     * <p>A primary device has a device identifier of {@code 0} (the default device ID).
     *
     * @param device the device JID to check
     * @return {@code true} if this is a primary device
     */
    @WhatsAppWebExport(moduleName = "WAWebSendMsgCommonApi", exports = "isPrimaryDevice",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static boolean isPrimaryDevice(Jid device) {
        return device.device() == 0;
    }
}
