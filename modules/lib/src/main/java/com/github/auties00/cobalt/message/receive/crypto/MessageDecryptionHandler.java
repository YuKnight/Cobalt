package com.github.auties00.cobalt.message.receive.crypto;

import com.github.auties00.cobalt.exception.WhatsAppMessageException;
import com.github.auties00.cobalt.message.receive.stanza.MessageReceiveEncryptedPayload;
import com.github.auties00.cobalt.message.MessageEncryptionType;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Tracks decryption outcomes across every encrypted payload in an incoming message
 * stanza and produces a composite {@link MessageDecryptionResult}.
 *
 * <p>A single stanza may carry up to two {@code <enc>} nodes: a sender-key group
 * message (SKMSG) and a per-device Signal message (PKMSG or MSG). The handler tracks
 * failures for each slot independently and applies WA Web's resolution rules to
 * determine the overall result:
 * <ul>
 *   <li>If a non-SKMSG payload fails with a retryable Signal error, no further
 *       payloads are attempted; the failure is final.</li>
 *   <li>If the SKMSG fails but the non-SKMSG succeeds (or was not present), the
 *       overall result is {@link MessageDecryptionResult#SUCCESS}.</li>
 *   <li>Error types are classified from the exception hierarchy and mapped to the
 *       appropriate result code for receipt generation.</li>
 * </ul>
 */
@WhatsAppWebModule(moduleName = "WAWebMsgProcessingDecryptionHandler")
public final class MessageDecryptionHandler {
    /**
     * Logger for decryption handler diagnostics.
     */
    private static final System.Logger LOGGER = System.getLogger(MessageDecryptionHandler.class.getName());

    /**
     * Error types that block further decryption attempts when observed on a
     * non-SKMSG payload.
     *
     * @implNote Mirrors the WA Web {@code C = new Set([y.SignalRetryable])} constant.
     * Only {@code SIGNAL_RETRYABLE} blocks further attempts via {@code canDecryptNext}.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static final Set<DecryptionErrorType> RETRYABLE_BLOCKERS =
            EnumSet.of(DecryptionErrorType.SIGNAL_RETRYABLE);

    /**
     * Set of enc types that have been accessed (attempted) so far.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.DIRECT)
    private final Set<MessageEncryptionType> accessedEncs =
            EnumSet.noneOf(MessageEncryptionType.class);

    /**
     * Failure from the non-SKMSG (PKMSG or MSG) payload, if any.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.DIRECT)
    private EncFailure pkOrMsgFailure;

    /**
     * Failure from the SKMSG payload, if any.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.DIRECT)
    private EncFailure skMsgFailure;

    /**
     * Returns whether the next encrypted payload should be attempted.
     *
     * <p>If a non-SKMSG payload already failed with a retryable Signal error, no
     * further payloads are attempted. Otherwise the payload is allowed and recorded
     * in {@link #accessedEncs}.
     *
     * @param enc the next encrypted payload to consider
     * @return {@code true} if decryption should be attempted
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.DIRECT)
    public boolean canDecryptNext(MessageReceiveEncryptedPayload enc) {
        if (pkOrMsgFailure != null && RETRYABLE_BLOCKERS.contains(pkOrMsgFailure.errorType)) {
            return false;
        }

        accessedEncs.add(enc.e2eType());
        return true;
    }

    /**
     * Records a decryption failure for the given encrypted payload.
     *
     * <p>The exception is classified into a {@link DecryptionErrorType} and stored
     * in the appropriate slot (SKMSG or PKMSG/MSG).
     *
     * @param enc   the encrypted payload that failed
     * @param error the decryption exception
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.DIRECT)
    public void handleError(
            MessageReceiveEncryptedPayload enc,
            WhatsAppMessageException.Receive error
    ) {
        var errorType = classifyError(error);
        var failure = new EncFailure(enc, error, errorType);

        if (enc.e2eType().isSenderKeyMessage()) {
            skMsgFailure = failure;
        } else {
            pkOrMsgFailure = failure;
        }

        LOGGER.log(System.Logger.Level.DEBUG,
                "Decryption error for {0}: {1} ({2})",
                enc.e2eType(), errorType, error.getMessage());
    }

    /**
     * Computes the composite decryption result from every tracked failure.
     *
     * <p>Resolution rules:
     * <ol>
     *   <li>If no failures occurred, returns SUCCESS.</li>
     *   <li>Picks the dominant failure: prefers skMsgFailure, else pkOrMsgFailure.</li>
     *   <li>If SKMSG was accessed and did not fail but the other slot did, the
     *       overall result is still SUCCESS.</li>
     *   <li>Otherwise maps the dominant failure's error type to a result code.</li>
     * </ol>
     *
     * @return the composite decryption result
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.DIRECT)
    public MessageDecryptionResult getResult() {
        var dominant = skMsgFailure != null ? skMsgFailure : pkOrMsgFailure;

        if (dominant == null) {
            return MessageDecryptionResult.SUCCESS;
        }

        var skMsgAccessed = accessedEncs.contains(MessageEncryptionType.SKMSG);
        if (skMsgAccessed && skMsgFailure == null) {
            return MessageDecryptionResult.SUCCESS;
        }

        return mapErrorToResult(dominant);
    }

    /**
     * Returns the failed encrypted payload, when one was recorded, so callers can
     * use it for duplicate message dedup handling.
     *
     * @return an {@link Optional} wrapping the failed enc payload
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<MessageReceiveEncryptedPayload> failedEnc() {
        var dominant = skMsgFailure != null ? skMsgFailure : pkOrMsgFailure;
        return dominant != null ? Optional.of(dominant.enc) : Optional.empty();
    }

    /**
     * Returns the exception from the dominant failure, when one was recorded.
     *
     * @return an {@link Optional} wrapping the decryption exception
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.DIRECT)
    public Optional<WhatsAppMessageException.Receive> failedError() {
        var dominant = skMsgFailure != null ? skMsgFailure : pkOrMsgFailure;
        return dominant != null ? Optional.of(dominant.error) : Optional.empty();
    }

    /**
     * Classifies a decryption exception into a {@link DecryptionErrorType}.
     *
     * @param error the exception to classify
     * @return the classified error type
     *
     * @implNote WA Web sub-classifies {@code SignalDecryptionError} by checking the
     * error message text for {@code "errDuplicateMsg"}. In Cobalt the exception
     * hierarchy already separates {@code DuplicateMessage} from other Signal errors
     * at the source.
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private static DecryptionErrorType classifyError(WhatsAppMessageException.Receive error) {
        return switch (error) {
            case WhatsAppMessageException.Receive.UnknownDevice _ ->
                    DecryptionErrorType.UNKNOWN_DEVICE;
            case WhatsAppMessageException.Receive.DuplicateMessage _ ->
                    DecryptionErrorType.SIGNAL_DUPLICATE_MESSAGE;
            case WhatsAppMessageException.Receive.InvalidDeviceSentMessage _ ->
                    DecryptionErrorType.DEVICE_SENT_MESSAGE;
            case WhatsAppMessageException.Receive.InvalidProtobuf _ ->
                    DecryptionErrorType.INVALID_PROTOBUF;
            case WhatsAppMessageException.Receive.HsmMismatch _ ->
                    DecryptionErrorType.HSM_MISMATCH;
            case WhatsAppMessageException.Receive.BroadcastEphemeralSettings _ ->
                    DecryptionErrorType.BROADCAST_EPH_SETTINGS;
            case WhatsAppMessageException.Receive.NoSession _,
                 WhatsAppMessageException.Receive.InvalidKey _,
                 WhatsAppMessageException.Receive.InvalidKeyId _,
                 WhatsAppMessageException.Receive.InvalidOneTimeKey _,
                 WhatsAppMessageException.Receive.InvalidSignedPreKey _,
                 WhatsAppMessageException.Receive.InvalidMessage _,
                 WhatsAppMessageException.Receive.InvalidSignature _,
                 WhatsAppMessageException.Receive.FutureMessage _,
                 WhatsAppMessageException.Receive.BadMac _,
                 WhatsAppMessageException.Receive.NoSenderKey _,
                 WhatsAppMessageException.Receive.InvalidSenderKey _,
                 WhatsAppMessageException.Receive.AdvFailure _ ->
                    DecryptionErrorType.SIGNAL_RETRYABLE;
            default -> DecryptionErrorType.UNKNOWN;
        };
    }

    /**
     * Maps a tracked failure's error type to the corresponding
     * {@link MessageDecryptionResult} code.
     *
     * @param failure the failure to map
     * @return the corresponding result code
     */
    @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
            adaptation = WhatsAppAdaptation.DIRECT)
    private static MessageDecryptionResult mapErrorToResult(EncFailure failure) {
        return switch (failure.errorType) {
            case SIGNAL_RETRYABLE, UNKNOWN_DEVICE, BROADCAST_EPH_SETTINGS ->
                    MessageDecryptionResult.RETRY;
            case SIGNAL_DUPLICATE_MESSAGE ->
                    MessageDecryptionResult.SIGNAL_OLD_COUNTER_ERROR;
            case DEVICE_SENT_MESSAGE, INVALID_PROTOBUF ->
                    MessageDecryptionResult.PARSE_VALIDATION_ERROR;
            case HSM_MISMATCH ->
                    MessageDecryptionResult.HSM_MISMATCH;
            case UNKNOWN ->
                    MessageDecryptionResult.PARSE_ERROR;
        };
    }

    /**
     * Classifies decryption errors into categories that drive the overall message
     * processing result and receipt type.
     */
    @WhatsAppWebModule(moduleName = "WAWebMsgProcessingDecryptionHandler")
    private enum DecryptionErrorType {
        /**
         * Retryable Signal protocol error such as no session, invalid key, invalid
         * message, bad MAC, or missing sender key.
         *
         * <p>Triggers a retry receipt.
         */
        SIGNAL_RETRYABLE,

        /**
         * Message counter was already seen (duplicate or old counter).
         *
         * <p>Handled specially for dedup; may still produce a delivery receipt.
         */
        SIGNAL_DUPLICATE_MESSAGE,

        /**
         * Message came from a companion device not in the local device list.
         *
         * <p>Triggers device list sync and retry.
         */
        UNKNOWN_DEVICE,

        /**
         * DeviceSentMessage wrapper was missing, invalid, or present when it should
         * not be.
         */
        DEVICE_SENT_MESSAGE,

        /**
         * Decrypted protobuf failed structural validation (multiple message keys,
         * type mismatch, etc.).
         */
        INVALID_PROTOBUF,

        /**
         * Stanza indicated HSM but the protobuf content did not match (or vice
         * versa).
         */
        HSM_MISMATCH,

        /**
         * Failed to decode broadcast ephemeral settings from the shared secret.
         */
        BROADCAST_EPH_SETTINGS,

        /**
         * Unclassified error that does not match any known category.
         */
        UNKNOWN
    }

    /**
     * Encapsulates a single encrypted payload's decryption failure: the payload, the
     * exception, and the classified error type.
     */
    @WhatsAppWebModule(moduleName = "WAWebMsgProcessingDecryptionHandler")
    private static final class EncFailure {
        /**
         * Encrypted payload that failed decryption.
         */
        @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final MessageReceiveEncryptedPayload enc;

        /**
         * Exception that caused the failure.
         */
        @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final WhatsAppMessageException.Receive error;

        /**
         * Classified error type produced by {@link #classifyError}.
         */
        @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
                adaptation = WhatsAppAdaptation.DIRECT)
        private final DecryptionErrorType errorType;

        /**
         * Constructs a new failure record.
         *
         * @param enc       the encrypted payload that failed
         * @param error     the exception that caused the failure
         * @param errorType the classified error type
         */
        @WhatsAppWebExport(moduleName = "WAWebMsgProcessingDecryptionHandler", exports = "createDecryptionHandler",
                adaptation = WhatsAppAdaptation.DIRECT)
        private EncFailure(
                MessageReceiveEncryptedPayload enc,
                WhatsAppMessageException.Receive error,
                DecryptionErrorType errorType
        ) {
            this.enc = enc;
            this.error = error;
            this.errorType = errorType;
        }
    }
}
