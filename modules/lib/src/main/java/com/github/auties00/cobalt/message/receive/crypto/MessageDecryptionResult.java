package com.github.auties00.cobalt.message.receive.crypto;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

/**
 * Enumerates the outcomes of the end-to-end decryption pipeline for an incoming message.
 *
 * <p>After {@link MessageDecryptionHandler} iterates over every encrypted payload in a
 * stanza, it condenses the aggregated outcome into one of these values. The result drives
 * the subsequent decision tree (delivery receipt, retry receipt, NACK, or silent drop)
 * and determines whether the decoded content is stored locally.
 *
 * @implNote {@link #BACKFILL} additionally absorbs every variant of
 * WAWebHandleMsgTypes.flow.PlaceholderType ({@code E2E}, {@code FANOUT},
 * {@code BOT_UNAVAILABLE_FANOUT}, {@code HOSTED_UNAVAILABLE_FANOUT},
 * {@code VIEW_ONCE_UNAVAILABLE_FANOUT}) since the downstream receipt logic is identical
 * for all four placeholder kinds.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleMsgTypes.flow")
public enum MessageDecryptionResult {
    /**
     * At least one encrypted payload was successfully decrypted and the protobuf content
     * was successfully processed.
     *
     * <p>Callers send a delivery receipt and persist the decoded content locally.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "E2EProcessResult",
            adaptation = WhatsAppAdaptation.DIRECT)
    SUCCESS,

    /**
     * Decryption failed with a retryable Signal protocol error.
     *
     * <p>A retry receipt requests the sender to re-encrypt and re-send. From the second
     * retry onward the receipt also carries a fresh prekey bundle so the sender can
     * rebuild the Signal session.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "E2EProcessResult",
            adaptation = WhatsAppAdaptation.DIRECT)
    RETRY,

    /**
     * The stanza indicated a highly structured message but the decoded protobuf did not
     * contain one (or vice versa).
     *
     * <p>The content is silently dropped because the mismatch indicates a protocol-level
     * inconsistency that retrying would not resolve.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "E2EProcessResult",
            adaptation = WhatsAppAdaptation.DIRECT)
    HSM_MISMATCH,

    /**
     * The message is an unavailable fanout placeholder for a companion device.
     *
     * <p>An ack is sent and no content is stored. Unavailable messages appear when a
     * companion device requires history backfill but does not yet have the content.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "E2EProcessResult",
            adaptation = WhatsAppAdaptation.DIRECT)
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "PlaceholderType",
            adaptation = WhatsAppAdaptation.ADAPTED)
    BACKFILL,

    /**
     * The decrypted bytes could not be parsed as a {@code MessageContainer} protobuf or
     * contained unrecognised content.
     *
     * <p>A NACK with {@code ParsingError} is returned.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "E2EProcessResult",
            adaptation = WhatsAppAdaptation.DIRECT)
    PARSE_ERROR,

    /**
     * The decoded protobuf parsed correctly but failed structural validation (for example
     * a stanza/protobuf type mismatch or an invalid {@code DeviceSentMessage} envelope).
     *
     * <p>A NACK with {@code InvalidProtobuf} is returned, optionally with an
     * {@code e2eFailureReason} meta attribute for telemetry.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "E2EProcessResult",
            adaptation = WhatsAppAdaptation.DIRECT)
    PARSE_VALIDATION_ERROR,

    /**
     * The Signal protocol reported a duplicate message counter.
     *
     * <p>If the message qualifies for dedup the cached delivery outcome is reused;
     * otherwise it is treated like a normal delivery.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleMsgTypes.flow", exports = "E2EProcessResult",
            adaptation = WhatsAppAdaptation.DIRECT)
    SIGNAL_OLD_COUNTER_ERROR
}
