package com.github.auties00.cobalt.device.icdc;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Signals the outcome of inspecting a message's ICDC metadata for hosted
 * (business coexistence) transitions.
 *
 * <p>WhatsApp's business coexistence feature allows users to migrate a chat between
 * the end-to-end encrypted account and a hosted business account. Every incoming
 * message carries a small metadata block that declares the sender's and recipient's
 * account types; Cobalt inspects that block as messages arrive so the UI can flag
 * mismatches (for example, a contact that was E2EE yesterday is HOSTED today) and
 * so the local device record can be refreshed against the server.
 *
 * <p>Produced by
 * {@link com.github.auties00.cobalt.device.DeviceService#handleHostedIcdcMetadataInline}
 * and consumed by the message receive pipeline.
 *
 * @implNote WAWebIcdcHandlerApi.handleHostedIcdcMetadataInline: returns an object
 * with {@code hostedBizEncMismatch} and {@code senderOrRecipientAccountTypeHosted}
 * fields.
 * @param hostedBizEncMismatch               {@code true} if there is a mismatch between the local
 *                                           ADV account type and the incoming HOSTED type, indicating
 *                                           a device list needs to be refreshed
 * @param senderOrRecipientAccountTypeHosted {@code true} if the sender or recipient account
 *                                           type in the message metadata is HOSTED
 */
@WhatsAppWebModule(moduleName = "WAWebIcdcHandlerApi")
public record HostedIcdcResult(
        boolean hostedBizEncMismatch,
        boolean senderOrRecipientAccountTypeHosted
) {
    /**
     * Default result indicating no hosted involvement.
     *
     * <p>Returned whenever the hosted devices feature is disabled, the chat is with
     * self, the chat is not a user chat, or the message simply does not carry
     * hosted metadata. Callers treat this as a "no-op" outcome.
     *
     * @implNote WAWebIcdcHandlerApi.handleHostedIcdcMetadataInline: the default return value
     * {@code {hostedBizEncMismatch: false, senderOrRecipientAccountTypeHosted: false}}
     * is returned when hosted devices are not enabled, the JID is self, or the JID is not a user.
     */
    @WhatsAppWebExport(moduleName = "WAWebIcdcHandlerApi",
            exports = "handleHostedIcdcMetadataInline",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public static final HostedIcdcResult DEFAULT = new HostedIcdcResult(false, false);
}
