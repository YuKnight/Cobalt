package com.github.auties00.cobalt.model.bot;

import com.github.auties00.cobalt.model.message.common.ChatMessageKey;
import it.auties.protobuf.annotation.ProtobufEnum;
import it.auties.protobuf.annotation.ProtobufEnumIndex;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

import java.util.Optional;

/**
 * Metadata about a bot plugin.
 *
 * @apiNote WAWebProtobufsE2E.pb.BotPluginMetadata
 */
@ProtobufMessage(name = "BotPluginMetadata")
public final class BotPluginMetadata {
    @ProtobufProperty(index = 1, type = ProtobufType.ENUM)
    final SearchProvider provider;

    @ProtobufProperty(index = 2, type = ProtobufType.ENUM)
    final PluginType pluginType;

    @ProtobufProperty(index = 3, type = ProtobufType.STRING)
    final String thumbnailCdnUrl;

    @ProtobufProperty(index = 4, type = ProtobufType.STRING)
    final String profilePhotoCdnUrl;

    @ProtobufProperty(index = 5, type = ProtobufType.STRING)
    final String searchProviderUrl;

    @ProtobufProperty(index = 6, type = ProtobufType.UINT32)
    final Integer referenceIndex;

    @ProtobufProperty(index = 7, type = ProtobufType.UINT32)
    final Integer expectedLinksCount;

    @ProtobufProperty(index = 9, type = ProtobufType.STRING)
    final String searchQuery;

    @ProtobufProperty(index = 10, type = ProtobufType.MESSAGE)
    final ChatMessageKey parentPluginMessageKey;

    BotPluginMetadata(
            SearchProvider provider,
            PluginType pluginType,
            String thumbnailCdnUrl,
            String profilePhotoCdnUrl,
            String searchProviderUrl,
            Integer referenceIndex,
            Integer expectedLinksCount,
            String searchQuery,
            ChatMessageKey parentPluginMessageKey
    ) {
        this.provider = provider;
        this.pluginType = pluginType;
        this.thumbnailCdnUrl = thumbnailCdnUrl;
        this.profilePhotoCdnUrl = profilePhotoCdnUrl;
        this.searchProviderUrl = searchProviderUrl;
        this.referenceIndex = referenceIndex;
        this.expectedLinksCount = expectedLinksCount;
        this.searchQuery = searchQuery;
        this.parentPluginMessageKey = parentPluginMessageKey;
    }

    public Optional<SearchProvider> provider() {
        return Optional.ofNullable(provider);
    }

    public Optional<PluginType> pluginType() {
        return Optional.ofNullable(pluginType);
    }

    public Optional<String> thumbnailCdnUrl() {
        return Optional.ofNullable(thumbnailCdnUrl);
    }

    public Optional<String> profilePhotoCdnUrl() {
        return Optional.ofNullable(profilePhotoCdnUrl);
    }

    public Optional<String> searchProviderUrl() {
        return Optional.ofNullable(searchProviderUrl);
    }

    public Optional<Integer> referenceIndex() {
        return Optional.ofNullable(referenceIndex);
    }

    public Optional<Integer> expectedLinksCount() {
        return Optional.ofNullable(expectedLinksCount);
    }

    public Optional<String> searchQuery() {
        return Optional.ofNullable(searchQuery);
    }

    public Optional<ChatMessageKey> parentPluginMessageKey() {
        return Optional.ofNullable(parentPluginMessageKey);
    }

    @ProtobufEnum(name = "BotPluginMetadata.SearchProvider")
    public enum SearchProvider {
        BING(0),
        GOOGLE(1),
        WOLFRAM_ALPHA(2);

        final int index;

        SearchProvider(@ProtobufEnumIndex int index) {
            this.index = index;
        }
    }

    @ProtobufEnum(name = "BotPluginMetadata.PluginType")
    public enum PluginType {
        WEB_SEARCH(1),
        REELS(2),
        IMAGE_GENERATION(3);

        final int index;

        PluginType(@ProtobufEnumIndex int index) {
            this.index = index;
        }
    }
}
