package com.github.auties00.cobalt.model.bot;

import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Metadata about a bot message.
 * <p>
 * Per WhatsApp Web BotMetadata: contains information about bot interactions
 * including avatar state, persona, plugins, and session data.
 *
 * @apiNote WAWebProtobufsE2E.pb.BotMetadata
 */
@ProtobufMessage(name = "BotMetadata")
public final class BotMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.MESSAGE)
    final BotAvatarMetadata avatarMetadata;

    @ProtobufProperty(index = 2, type = ProtobufType.STRING)
    final String personaId;

    @ProtobufProperty(index = 3, type = ProtobufType.MESSAGE)
    final BotPluginMetadata pluginMetadata;

    @ProtobufProperty(index = 4, type = ProtobufType.MESSAGE)
    final BotSuggestedPromptMetadata suggestedPromptMetadata;

    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    final String invokerJid;

    @ProtobufProperty(index = 6, type = ProtobufType.MESSAGE)
    final BotSessionMetadata sessionMetadata;

    @ProtobufProperty(index = 7, type = ProtobufType.MESSAGE)
    final BotMemuMetadata memuMetadata;

    @ProtobufProperty(index = 8, type = ProtobufType.STRING)
    final String timezone;

    @ProtobufProperty(index = 9, type = ProtobufType.MESSAGE)
    final BotReminderMetadata reminderMetadata;

    @ProtobufProperty(index = 10, type = ProtobufType.MESSAGE)
    final BotModelMetadata modelMetadata;

    @ProtobufProperty(index = 11, type = ProtobufType.STRING)
    final String messageDisclaimerText;

    @ProtobufProperty(index = 12, type = ProtobufType.MESSAGE)
    final BotProgressIndicatorMetadata progressIndicatorMetadata;

    @ProtobufProperty(index = 13, type = ProtobufType.MESSAGE)
    final BotCapabilityMetadata capabilityMetadata;

    @ProtobufProperty(index = 31, type = ProtobufType.MESSAGE)
    final AIThreadInfo botThreadInfo;

    @ProtobufProperty(index = 35, type = ProtobufType.MESSAGE)
    final BotGroupMetadata botGroupMetadata;

    BotMetadata(
            BotAvatarMetadata avatarMetadata,
            String personaId,
            BotPluginMetadata pluginMetadata,
            BotSuggestedPromptMetadata suggestedPromptMetadata,
            String invokerJid,
            BotSessionMetadata sessionMetadata,
            BotMemuMetadata memuMetadata,
            String timezone,
            BotReminderMetadata reminderMetadata,
            BotModelMetadata modelMetadata,
            String messageDisclaimerText,
            BotProgressIndicatorMetadata progressIndicatorMetadata,
            BotCapabilityMetadata capabilityMetadata,
            AIThreadInfo botThreadInfo,
            BotGroupMetadata botGroupMetadata
    ) {
        this.avatarMetadata = avatarMetadata;
        this.personaId = personaId;
        this.pluginMetadata = pluginMetadata;
        this.suggestedPromptMetadata = suggestedPromptMetadata;
        this.invokerJid = invokerJid;
        this.sessionMetadata = sessionMetadata;
        this.memuMetadata = memuMetadata;
        this.timezone = timezone;
        this.reminderMetadata = reminderMetadata;
        this.modelMetadata = modelMetadata;
        this.messageDisclaimerText = messageDisclaimerText;
        this.progressIndicatorMetadata = progressIndicatorMetadata;
        this.capabilityMetadata = capabilityMetadata;
        this.botThreadInfo = botThreadInfo;
        this.botGroupMetadata = botGroupMetadata;
    }

    public Optional<BotAvatarMetadata> avatarMetadata() {
        return Optional.ofNullable(avatarMetadata);
    }

    /**
     * Gets the persona ID for this bot message.
     * <p>
     * Per WhatsApp Web WAWebSimpleSignalPNToFBIDMigration.getFbidBotPersonaType:
     * Used to determine the persona_type attribute for FBID bots.
     *
     * @return the persona ID, or empty
     */
    public Optional<String> personaId() {
        return Optional.ofNullable(personaId);
    }

    public Optional<BotPluginMetadata> pluginMetadata() {
        return Optional.ofNullable(pluginMetadata);
    }

    public Optional<BotSuggestedPromptMetadata> suggestedPromptMetadata() {
        return Optional.ofNullable(suggestedPromptMetadata);
    }

    /**
     * Gets the invoker JID - the JID of the user who invoked/prompted the bot.
     *
     * @return the invoker JID, or empty
     */
    public Optional<String> invokerJid() {
        return Optional.ofNullable(invokerJid);
    }

    public Optional<BotSessionMetadata> sessionMetadata() {
        return Optional.ofNullable(sessionMetadata);
    }

    public Optional<BotMemuMetadata> memuMetadata() {
        return Optional.ofNullable(memuMetadata);
    }

    public Optional<String> timezone() {
        return Optional.ofNullable(timezone);
    }

    public Optional<BotReminderMetadata> reminderMetadata() {
        return Optional.ofNullable(reminderMetadata);
    }

    public Optional<BotModelMetadata> modelMetadata() {
        return Optional.ofNullable(modelMetadata);
    }

    public Optional<String> messageDisclaimerText() {
        return Optional.ofNullable(messageDisclaimerText);
    }

    public Optional<BotProgressIndicatorMetadata> progressIndicatorMetadata() {
        return Optional.ofNullable(progressIndicatorMetadata);
    }

    /**
     * Gets the capability metadata advertising supported rich response features.
     *
     * @return the capability metadata, or empty
     * @apiNote WAWebProtobufsAICommon.pb.BotMetadata field 13
     */
    public Optional<BotCapabilityMetadata> capabilityMetadata() {
        return Optional.ofNullable(capabilityMetadata);
    }

    /**
     * Gets the AI thread information for this bot conversation.
     *
     * @return the thread info, or empty
     * @apiNote WAWebProtobufsAICommon.pb.BotMetadata field 31
     */
    public Optional<AIThreadInfo> botThreadInfo() {
        return Optional.ofNullable(botThreadInfo);
    }

    /**
     * Gets the group metadata for bot group interactions.
     *
     * @return the group metadata, or empty
     * @apiNote WAWebProtobufsAICommon.pb.BotMetadata field 35
     */
    public Optional<BotGroupMetadata> botGroupMetadata() {
        return Optional.ofNullable(botGroupMetadata);
    }
}
