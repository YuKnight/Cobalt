package com.github.auties00.cobalt.message.send.senderkey;

import com.github.auties00.cobalt.device.util.DeviceConstants;
import com.github.auties00.cobalt.message.encryption.MessageEncryption;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.message.common.MessageContainer;
import com.github.auties00.cobalt.model.message.common.MessageContainerBuilder;
import com.github.auties00.cobalt.model.message.server.SenderKeyDistributionMessageBuilder;
import com.github.auties00.cobalt.store.WhatsAppStore;

import java.util.*;

/**
 * Distributes sender keys to group participants.
 *
 * @apiNote WAWebGetGroupKeyDistributionMsg, WAWebApiParticipantStore
 */
public final class SenderKeyDistribution {
    private static final System.Logger LOGGER = System.getLogger("SenderKeyDistribution");

    private final WhatsAppStore store;
    private final MessageEncryption encryption;

    public SenderKeyDistribution(WhatsAppStore store, MessageEncryption encryption) {
        this.store = Objects.requireNonNull(store, "store cannot be null");
        this.encryption = Objects.requireNonNull(encryption, "encryption cannot be null");
    }

    /**
     * Creates a sender key distribution message container for a group.
     */
    public MessageContainer createDistributionMessage(Jid groupJid, Jid senderJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");

        var senderKeyBytes = encryption.getSenderKeyBytes(groupJid, senderJid);

        var distributionMessage = new SenderKeyDistributionMessageBuilder()
                .groupJid(groupJid)
                .data(senderKeyBytes)
                .build();

        return new MessageContainerBuilder()
                .senderKeyDistributionMessage(distributionMessage)
                .build();
    }

    /**
     * Categorizes participants into those who need the sender key and those who already have it.
     */
    public Lists categorizeParticipants(Jid groupJid, Jid senderJid, Collection<Jid> participants) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(senderJid, "senderJid cannot be null");
        Objects.requireNonNull(participants, "participants cannot be null");

        var needsKey = new ArrayList<Jid>();
        var hasKey = new ArrayList<Jid>();

        boolean rotateKey = store.anyUserNeedsSenderKeyRotation(participants);

        for (var participant : participants) {
            if (hasSenderKeyForParticipant(groupJid, senderJid, participant)) {
                hasKey.add(participant);
            } else {
                needsKey.add(participant);
            }
        }

        if (rotateKey) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Sender key rotation needed for {0}, all {1} participants need key",
                    groupJid, participants.size());

            for (var participant : participants) {
                store.checkAndClearSenderKeyRotationNeeded(participant);
            }

            var allParticipants = new ArrayList<Jid>(needsKey.size() + hasKey.size());
            allParticipants.addAll(needsKey);
            allParticipants.addAll(hasKey);

            return new Lists(
                    Collections.unmodifiableList(allParticipants),
                    Collections.emptyList(),
                    true
            );
        }

        LOGGER.log(System.Logger.Level.DEBUG,
                "Categorized participants for {0}: {1} need key, {2} have key",
                groupJid, needsKey.size(), hasKey.size());

        return new Lists(
                Collections.unmodifiableList(needsKey),
                Collections.unmodifiableList(hasKey),
                false
        );
    }

    private boolean hasSenderKeyForParticipant(Jid groupJid, Jid senderJid, Jid participantJid) {
        return store.hasSenderKeyDistributed(groupJid, participantJid);
    }

    /**
     * Marks participants as having received the sender key.
     */
    public void markParticipantsHaveSenderKey(Jid groupJid, Collection<Jid> participants) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(participants, "participants cannot be null");

        var count = 0;
        for (var participant : participants) {
            if (participant.device() == DeviceConstants.HOSTED_DEVICE_ID) {
                continue;
            }
            store.markSenderKeyDistributed(groupJid, participant);
            count++;
        }

        LOGGER.log(System.Logger.Level.DEBUG,
                "Marked {0} participants as having sender key for {1}", count, groupJid);
    }

    /**
     * Clears the sender key distribution status for all participants in a group.
     */
    public void clearDistributionStatus(Jid groupJid) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        store.clearSenderKeyDistribution(groupJid);
        LOGGER.log(System.Logger.Level.DEBUG, "Cleared sender key distribution status for {0}", groupJid);
    }

    /**
     * Marks the sender key as forgotten for specific participants.
     */
    public void forgetSenderKey(Jid groupJid, Collection<Jid> participants) {
        Objects.requireNonNull(groupJid, "groupJid cannot be null");
        Objects.requireNonNull(participants, "participants cannot be null");

        for (var participant : participants) {
            store.forgetSenderKeyDistributed(groupJid, participant);
        }

        LOGGER.log(System.Logger.Level.DEBUG,
                "Marked sender key as forgotten for {0} participants in {1}",
                participants.size(), groupJid);
    }

    /**
     * Categorized sender key lists.
     */
    public record Lists(List<Jid> needsDistribution, List<Jid> alreadyHasKey, boolean rotateKey) {
        public List<Jid> allParticipants() {
            var all = new ArrayList<Jid>(needsDistribution.size() + alreadyHasKey.size());
            all.addAll(needsDistribution);
            all.addAll(alreadyHasKey);
            return Collections.unmodifiableList(all);
        }

        public boolean hasParticipantsNeedingDistribution() {
            return !needsDistribution.isEmpty();
        }

        public int totalCount() {
            return needsDistribution.size() + alreadyHasKey.size();
        }
    }
}
