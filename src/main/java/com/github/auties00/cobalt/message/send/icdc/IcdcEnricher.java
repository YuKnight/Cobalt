package com.github.auties00.cobalt.message.send.icdc;

import com.github.auties00.cobalt.device.icdc.IcdcResult;
import com.github.auties00.cobalt.model.chat.ChatMessageContextInfoBuilder;
import com.github.auties00.cobalt.model.device.DeviceListMetadataBuilder;
import com.github.auties00.cobalt.model.message.MessageContainer;

/**
 * Populates ICDC (Identity Change Detection Consistency) metadata on
 * outgoing message containers.
 *
 * <p>ICDC metadata is written into the container's
 * {@code messageContextInfo.deviceListMetadata} so that recipients can
 * detect changes in the sender's or recipient's device list since the
 * last key exchange.
 *
 * @apiNote WAWebE2EProtoGenerator.populateMessageContextInfo: merges
 * ICDC metadata into the existing messageContextInfo, preserving other
 * fields such as messageSecret and botMetadata.
 */
public final class IcdcEnricher {
    private IcdcEnricher() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Returns a copy of the container with ICDC metadata populated in its
     * {@code messageContextInfo}.
     *
     * <p>Preserves any existing fields on the container's
     * {@code DeviceContextInfo} (e.g. messageSecret, botMetadata) while
     * adding or replacing the {@code deviceListMetadata} and setting
     * {@code deviceListMetadataVersion} to 2.
     *
     * @param container     the original message container
     * @param senderIcdc    the sender's ICDC result, or {@code null}
     * @param recipientIcdc the recipient's ICDC result, or {@code null}
     * @return the enriched container (unchanged if both args are {@code null})
     *
     * @apiNote WAWebE2EProtoGenerator.populateMessageContextInfo: merges
     * ICDC metadata into the existing messageContextInfo.
     */
    public static MessageContainer enrich(
            MessageContainer container,
            IcdcResult senderIcdc,
            IcdcResult recipientIcdc
    ) {
        if (senderIcdc == null && recipientIcdc == null) {
            return container;
        }

        var metadataBuilder = new DeviceListMetadataBuilder();
        if (senderIcdc != null) {
            senderIcdc.keyHash().ifPresent(metadataBuilder::senderKeyHash);
            senderIcdc.timestamp().ifPresent(metadataBuilder::senderTimestamp);
            metadataBuilder.senderKeyIndexes(senderIcdc.keyIndexes());
            senderIcdc.accountType().ifPresent(metadataBuilder::senderAccountType);
        }
        if (recipientIcdc != null) {
            recipientIcdc.keyHash().ifPresent(metadataBuilder::recipientKeyHash);
            recipientIcdc.timestamp().ifPresent(metadataBuilder::recipientTimestamp);
            metadataBuilder.recipientKeyIndexes(recipientIcdc.keyIndexes());
            recipientIcdc.accountType().ifPresent(metadataBuilder::receiverAccountType);
        }

        // WAWebE2EProtoGenerator.populateMessageContextInfo:
        // merges with existing messageContextInfo, preserving other fields
        var existing = container.messageContextInfo().orElse(null);
        var infoBuilder = new ChatMessageContextInfoBuilder()
                .deviceListMetadata(metadataBuilder.build())
                .deviceListMetadataVersion(2);
        if (existing != null) {
            existing.messageSecret().ifPresent(infoBuilder::messageSecret);
            existing.paddingBytes().ifPresent(infoBuilder::paddingBytes);
            existing.botMessageSecret().ifPresent(infoBuilder::botMessageSecret);
            existing.botMetadata().ifPresent(infoBuilder::botMetadata);
            infoBuilder.capiCreatedGroup(existing.capiCreatedGroup());
            existing.supportPayload().ifPresent(infoBuilder::supportPayload);
            infoBuilder.threadId(existing.threadId());
        }

        return container.withMessageContextInfo(infoBuilder.build());
    }
}
