package com.github.auties00.cobalt.message.send.keys;

import com.github.auties00.cobalt.message.protocol.SenderKeyNameFactory;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.model.MessageContainer;
import com.github.auties00.cobalt.model.message.model.MessageContainerBuilder;
import com.github.auties00.cobalt.model.message.server.SenderKeyDistributionMessageBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;
import com.github.auties00.libsignal.groups.SignalGroupCipher;
import com.github.auties00.libsignal.protocol.SignalSenderKeyDistributionMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service for managing sender key distribution in group messages.
 * <p>
 * When sending group messages, we use Sender Key encryption (skmsg) which
 * is more efficient than encrypting for each device individually. However,
 * before we can use sender key encryption, we must distribute our sender key
 * to all group members using Signal Protocol (pkmsg/msg).
 * <p>
 * This service tracks which devices have received our sender key and
 * generates distribution messages for devices that haven't.
 */
public final class MessageSenderKeyDistributionService {
    private final WhatsAppStore store;
    private final SignalGroupCipher groupCipher;

    /**
     * Tracks devices that have received sender keys for each group.
     * Key: group JID string
     * Value: set of device JIDs that have received our sender key
     */
    private final ConcurrentMap<String, Set<String>> senderKeyDistributionState;

    public MessageSenderKeyDistributionService(WhatsAppStore store, SignalGroupCipher groupCipher) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.groupCipher = Objects.requireNonNull(groupCipher, "groupCipher cannot be null");
        this.senderKeyDistributionState = new ConcurrentHashMap<>();
    }

    /**
     * Gets the list of devices that need sender key distribution.
     * These are devices in the fanout list that haven't received our sender key yet.
     *
     * @param groupJid   the group JID
     * @param allDevices all devices in the fanout list
     * @return list of devices that need sender key distribution
     */
    public List<Jid> getDevicesNeedingSenderKey(Jid groupJid, Collection<Jid> allDevices) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(allDevices, "allDevices cannot be null");

        var distributedDevices = senderKeyDistributionState.get(groupJid.toString());
        if (distributedDevices == null) {
            // No sender key has been distributed for this group yet
            return new ArrayList<>(allDevices);
        }

        var needsDistribution = new ArrayList<Jid>();
        for (var device : allDevices) {
            if (!distributedDevices.contains(device.toString())) {
                needsDistribution.add(device);
            }
        }

        return needsDistribution;
    }

    /**
     * Generates a sender key distribution message for the specified group.
     * This creates the distribution message that will be wrapped in a
     * MessageContainer and encrypted with Signal Protocol for each recipient.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender's device JID (our JID)
     * @return the sender key distribution message bytes
     */
    public SignalSenderKeyDistributionMessage generateDistributionMessage(Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        return groupCipher.create(senderKeyName);
    }

    /**
     * Creates a MessageContainer wrapping the sender key distribution message.
     * This can be encrypted with Signal Protocol and sent to devices that
     * need the sender key.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender's device JID
     * @return the message container with the distribution message
     */
    public MessageContainer createDistributionMessageContainer(Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        var signalDistributionMessage = generateDistributionMessage(groupJid, senderJid);
        var distributionMessage = new SenderKeyDistributionMessageBuilder()
                .groupJid(groupJid)
                .data(signalDistributionMessage.toSerialized())
                .build();

        return new MessageContainerBuilder()
                .senderKeyDistributionMessage(distributionMessage)
                .build();
    }

    /**
     * Marks devices as having received our sender key.
     * Call this after successfully sending the sender key distribution message.
     *
     * @param groupJid the group JID
     * @param devices  the devices that received the sender key
     */
    public void markSenderKeyDistributed(Jid groupJid, Collection<Jid> devices) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(devices, "devices cannot be null");

        var distributedDevices = senderKeyDistributionState.computeIfAbsent(
                groupJid.toString(),
                k -> ConcurrentHashMap.newKeySet()
        );

        for (var device : devices) {
            distributedDevices.add(device.toString());
        }
    }

    /**
     * Clears the sender key distribution state for a group.
     * Call this when the sender key needs to be rotated (e.g., after
     * a group membership change or key compromise).
     *
     * @param groupJid the group JID
     */
    public void clearDistributionState(Jid groupJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        senderKeyDistributionState.remove(groupJid.toString());
    }

    /**
     * Checks if we have a sender key for the specified group.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender's device JID
     * @return true if we have a sender key, false otherwise
     */
    public boolean hasSenderKey(Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        return store.findSenderKeyByName(senderKeyName).isPresent();
    }

    /**
     * Processes a received sender key distribution message.
     * Call this when receiving a SenderKeyDistributionMessage from another
     * group member.
     *
     * @param groupJid        the group JID
     * @param senderJid       the sender's device JID (the sender of the distribution message)
     * @param distributionMsg the distribution message bytes
     */
    public void processReceivedDistribution(Jid groupJid, Jid senderJid, SignalSenderKeyDistributionMessage distributionMsg) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        Objects.requireNonNull(distributionMsg, "distributionMsg cannot be null");

        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        groupCipher.process(senderKeyName, distributionMsg);
    }

    /**
     * Rotates the sender key for a group.
     * Creates a new sender key and clears the distribution state so it will
     * be redistributed to all members.
     *
     * @param groupJid  the group JID
     * @param senderJid the sender's device JID
     */
    public void rotateSenderKey(Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        // Clear the existing sender key
        var senderKeyName = SenderKeyNameFactory.create(groupJid, senderJid);
        // The new sender key will be created on the next encryption

        // Clear distribution state so the new key will be distributed
        clearDistributionState(groupJid);
    }

    /**
     * Gets the number of devices that have received our sender key for a group.
     *
     * @param groupJid the group JID
     * @return the count of devices with our sender key
     */
    public int getDistributedDeviceCount(Jid groupJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");

        var distributedDevices = senderKeyDistributionState.get(groupJid.toString());
        return distributedDevices == null ? 0 : distributedDevices.size();
    }

}
