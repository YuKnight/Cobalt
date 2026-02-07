package com.github.auties00.cobalt.model.chat;

import java.util.Optional;

/**
 * Common interface for chat settings
 */
public sealed interface ChatSetting permits GroupSetting, CommunitySetting {
    int index();

    /**
     * Resolves a {@link ChatSetting} from its protobuf index.
     *
     * @param index the protobuf enum index
     * @return the setting, or empty if the index is unknown
     */
    static Optional<ChatSetting> of(int index) {
        var group = GroupSettingSpec.decode(index);
        if (group != null) {
            return Optional.of(group);
        }
        var community = CommunitySettingSpec.decode(index);
        if (community != null) {
            return Optional.of(community);
        }
        return Optional.empty();
    }
}
