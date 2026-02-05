package com.github.auties00.cobalt.model.info;

import com.github.auties00.cobalt.model.bot.BotMetadata;
import com.github.auties00.cobalt.model.message.common.MessageThreadId;
import com.github.auties00.cobalt.model.sync.DeviceListMetadata;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.List;
import java.util.Optional;

/**
 * Context information embedded in messages for device synchronization and bot metadata.
 * <p>
 * Per WhatsApp Web MessageContextInfo: contains device list metadata for ICDC,
 * message secrets for encryption, and bot/thread metadata for AI features.
 *
 * @apiNote WAWebProtobufsE2E.pb.MessageContextInfo
 */
@ProtobufMessage(name = "MessageContextInfo")
public final class DeviceContextInfo implements Info {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final DeviceListMetadata deviceListMetadata;

    @ProtobufProperty(index = 2, type = ProtobufType.INT32)
    final int deviceListMetadataVersion;

    @ProtobufProperty(index = 3, type = ProtobufType.BYTES)
    byte[] messageSecret;

    @ProtobufProperty(index = 4, type = ProtobufType.BYTES)
    byte[] paddingBytes;

    /**
     * Bot message secret - used instead of messageSecret when sending to bots.
     * Per WhatsApp Web WAWebE2EProtoGenerator.updateBotInvokeMsgProtoCopyForCapi:
     * the messageSecret is removed and botMessageSecret is added for bot messages.
     */
    @ProtobufProperty(index = 6, type = ProtobufType.BYTES)
    byte[] botMessageSecret;

    /**
     * Bot metadata - contains information about bot interactions.
     * Per WhatsApp Web BotMetadata: includes personaId for FBID bots,
     * plugin metadata, session info, and more.
     *
     * @apiNote WAWebProtobufsE2E.pb.MessageContextInfo.botMetadata
     */
    @ProtobufProperty(index = 7, type = ProtobufType.MESSAGE)
    BotMetadata botMetadata;

    /**
     * Flag indicating the group was created via Cloud API (CAPI).
     * <p>
     * Per WhatsApp Web WAWebE2EProtoGenerator.updateGroupMsgProtoWithCapiFlag:
     * Set to true when sending messages to CAPI-created groups.
     * This flag is used to identify groups created through the Business API
     * rather than through the regular WhatsApp client.
     *
     * @apiNote WAWebProtobufsE2E.pb.MessageContextInfo.capiCreatedGroup
     */
    @ProtobufProperty(index = 11, type = ProtobufType.BOOL)
    boolean capiCreatedGroup;

    /**
     * Support payload for CAPI support accounts (SAGA debug info).
     * <p>
     * Per WhatsApp Web WAWebE2EProtoGenerator.addDebugInfoSupportPayload:
     * Contains JSON payload with version, debug_information, and citations_carousel
     * for messages sent to CAPI support accounts with SAGA V1 enabled.
     *
     * @apiNote WAWebProtobufsE2E.pb.MessageContextInfo.supportPayload
     */
    @ProtobufProperty(index = 12, type = ProtobufType.STRING)
    String supportPayload;

    /**
     * Thread IDs - identifies conversation threads, particularly AI threads.
     * Per WhatsApp Web ThreadID: the threadKey.id is used as client_thread_id
     * in bot nodes and hashed for hashed_ai_thread_id in meta nodes.
     *
     * @apiNote WAWebProtobufsE2E.pb.MessageContextInfo.threadId
     */
    @ProtobufProperty(index = 15, type = ProtobufType.MESSAGE)
    List<MessageThreadId> threadId;

    DeviceContextInfo(
            DeviceListMetadata deviceListMetadata,
            int deviceListMetadataVersion,
            byte[] paddingBytes,
            byte[] messageSecret,
            byte[] botMessageSecret,
            BotMetadata botMetadata,
            boolean capiCreatedGroup,
            String supportPayload,
            List<MessageThreadId> threadId
    ) {
        this.deviceListMetadata = deviceListMetadata;
        this.deviceListMetadataVersion = deviceListMetadataVersion;
        this.paddingBytes = paddingBytes;
        this.messageSecret = messageSecret;
        this.botMessageSecret = botMessageSecret;
        this.botMetadata = botMetadata;
        this.capiCreatedGroup = capiCreatedGroup;
        this.supportPayload = supportPayload;
        this.threadId = threadId;
    }

    public Optional<DeviceListMetadata> deviceListMetadata() {
        return Optional.ofNullable(deviceListMetadata);
    }

    public int deviceListMetadataVersion() {
        return deviceListMetadataVersion;
    }

    public Optional<byte[]> messageSecret() {
        return Optional.ofNullable(messageSecret);
    }

    public void setMessageSecret(byte[] messageSecret) {
        this.messageSecret = messageSecret;
    }

    public Optional<byte[]> paddingBytes() {
        return Optional.ofNullable(paddingBytes);
    }

    public void setPaddingBytes(byte[] paddingBytes) {
        this.paddingBytes = paddingBytes;
    }

    public Optional<byte[]> botMessageSecret() {
        return Optional.ofNullable(botMessageSecret);
    }

    public void setBotMessageSecret(byte[] botMessageSecret) {
        this.botMessageSecret = botMessageSecret;
    }

    public Optional<BotMetadata> botMetadata() {
        return Optional.ofNullable(botMetadata);
    }

    public void setBotMetadata(BotMetadata botMetadata) {
        this.botMetadata = botMetadata;
    }

    /**
     * Returns whether this message is for a CAPI (Cloud API) created group.
     *
     * @return true if the group was created via Cloud API
     */
    public boolean capiCreatedGroup() {
        return capiCreatedGroup;
    }

    /**
     * Sets the CAPI created group flag.
     *
     * @param capiCreatedGroup true if the group was created via Cloud API
     */
    public void setCapiCreatedGroup(boolean capiCreatedGroup) {
        this.capiCreatedGroup = capiCreatedGroup;
    }

    /**
     * Returns the support payload for CAPI support accounts.
     *
     * @return the support payload JSON, or empty if not set
     */
    public Optional<String> supportPayload() {
        return Optional.ofNullable(supportPayload);
    }

    /**
     * Sets the support payload for CAPI support accounts.
     *
     * @param supportPayload the support payload JSON
     */
    public void setSupportPayload(String supportPayload) {
        this.supportPayload = supportPayload;
    }

    public List<MessageThreadId> threadId() {
        return threadId;
    }

    public void setThreadId(List<MessageThreadId> threadId) {
        this.threadId = threadId;
    }
}