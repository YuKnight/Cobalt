package com.github.auties00.cobalt.device.icdc;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Outcome of inspecting an incoming message's ICDC metadata for hosted (business
 * coexistence) transitions.
 *
 * <p>WhatsApp's business coexistence feature lets a chat migrate between the
 * end-to-end encrypted account and a hosted business account. Every incoming message
 * carries a metadata block declaring the sender's and recipient's account types;
 * Cobalt inspects that block as messages arrive so the UI can flag mismatches and
 * the local device record can be refreshed against the server.
 *
 * @param hostedBizEncMismatch               {@code true} when the local ADV account type
 *                                           disagrees with the incoming {@code HOSTED} type
 *                                           and a device list refresh is required
 * @param senderOrRecipientAccountTypeHosted {@code true} when either the sender or recipient
 *                                           in the message metadata is a hosted account
 */
@WhatsAppWebModule(moduleName = "WAWebIcdcHandlerApi")
public record HostedIcdcResult(
        boolean hostedBizEncMismatch,
        boolean senderOrRecipientAccountTypeHosted
) {
    /**
     * No-op outcome returned when the hosted devices feature is disabled, the chat is
     * with self, the chat is not a user chat, or the message carries no hosted
     * metadata.
     */
    @WhatsAppWebExport(moduleName = "WAWebIcdcHandlerApi",
            exports = "handleHostedIcdcMetadataInline",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static final HostedIcdcResult DEFAULT = new HostedIcdcResult(false, false);
}
