package com.github.auties00.cobalt.model.sync.setting.privacy;

import com.github.auties00.cobalt.model.sync.SyncAction;
import it.auties.protobuf.annotation.ProtobufMessage;
import it.auties.protobuf.annotation.ProtobufProperty;
import it.auties.protobuf.model.ProtobufType;

@ProtobufMessage(name = "SyncActionValue.PrivacySettingDisableLinkPreviewsAction")
public final class PrivacySettingDisableLinkPreviewsAction implements SyncAction {
    @ProtobufProperty(index = 1, type = ProtobufType.BOOL)
    Boolean isPreviewsDisabled;


    PrivacySettingDisableLinkPreviewsAction(Boolean isPreviewsDisabled) {
        this.isPreviewsDisabled = isPreviewsDisabled;
    }

    public boolean isPreviewsDisabled() {
        return isPreviewsDisabled != null && isPreviewsDisabled;
    }

    public PrivacySettingDisableLinkPreviewsAction setPreviewsDisabled(Boolean isPreviewsDisabled) {
        this.isPreviewsDisabled = isPreviewsDisabled;
        return this;
    }
}
