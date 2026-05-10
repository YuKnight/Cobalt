package com.github.auties00.cobalt.node.usync.result;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.node.usync.UsyncProtocolResult;

/**
 * Sealed family of every protocol-specific success variant a USync per-user,
 * per-protocol parser can return.
 *
 * <p>Sibling permit of {@link UsyncProtocolError} under
 * {@link UsyncProtocolResult}. A parser yields either an error or one of the
 * eleven concrete shapes permitted here.
 */
@WhatsAppWebModule(moduleName = "WAWebUsync")
public sealed interface UsyncProtocolResponse extends UsyncProtocolResult permits
        BotProfileResult,
        BusinessResult,
        ContactResult,
        DeviceResult,
        DisappearingModeResult,
        FeatureResult,
        LidResult,
        PictureResult,
        StatusResult,
        TextStatusResult,
        UsernameResult {
}
