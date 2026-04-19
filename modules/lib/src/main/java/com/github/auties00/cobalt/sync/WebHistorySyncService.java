package com.github.auties00.cobalt.sync;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.exception.WhatsAppHistorySyncException;
import com.github.auties00.cobalt.media.MediaConnection;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.migration.LidMigrationService;
import com.github.auties00.cobalt.model.message.system.history.HistorySyncNotification;
import com.github.auties00.cobalt.model.sync.history.HistorySync;
import it.auties.protobuf.stream.ProtobufInputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.InflaterInputStream;

/**
 * Downloads, decrypts and decodes {@code HistorySyncNotification} payloads
 * sent by the primary device after a companion has been linked.
 *
 * <p>On every incoming {@link HistorySyncNotification} the primary device
 * advertises an encrypted history chunk (or inlines a bootstrap payload when
 * the data is small enough to skip the CDN round-trip). This service resolves
 * the notification to a decoded {@link HistorySync} protobuf, fans the chunk
 * out to the registered history-sync listeners, and forwards the payload to
 * the {@link LidMigrationService} so that any phone-number to LID mappings
 * carried with the chunk are ingested.
 *
 * <p>Processing runs on a dedicated virtual thread because history blobs can
 * reach several megabytes and the caller is the stanza dispatch thread; keeping
 * it off the main path avoids back-pressuring other incoming messages.
 *
 * @implNote {@code WAWebHandleHistorySyncNotification.default} drives the
 *           equivalent JS flow, using
 *           {@code WAWebDownloadManager.downloadAndMaybeDecrypt} for the CDN
 *           fetch,
 *           {@code WAWebHandleHistorySyncChunk.handleHistorySyncChunk} for
 *           per-chunk application, and
 *           {@code WAWebApiHistorySyncNotification} for the background queue.
 *           Cobalt collapses the download, decrypt, inflate and decode stages
 *           into {@link MediaConnection#download(com.github.auties00.cobalt.model.media.MediaProvider)},
 *           which already returns a plaintext, already-inflated stream for the
 *           {@link com.github.auties00.cobalt.model.media.MediaPath#HISTORY_SYNC}
 *           media type.
 */
@WhatsAppWebModule(moduleName = "WAWebHandleHistorySyncNotification")
@WhatsAppWebModule(moduleName = "WAWebHistorySyncNotificationUtils")
@WhatsAppWebModule(moduleName = "WAWebHandleHistorySyncChunk")
public final class WebHistorySyncService {
    /**
     * Logger used to trace non-fatal download and decode failures without
     * propagating them into the stanza dispatch loop.
     */
    private static final System.Logger LOGGER = System.getLogger(WebHistorySyncService.class.getName());

    /**
     * The WhatsApp client, used to reach the store (for the shared media
     * connection) and to notify history-sync listeners.
     */
    private final WhatsAppClient whatsapp;

    /**
     * The LID migration service that consumes the
     * {@code phoneNumberToLidMappings} attached to every history sync chunk.
     */
    private final LidMigrationService lidMigrationService;

    /**
     * Constructs a new service bound to the given client.
     *
     * @param whatsapp            the WhatsApp client
     * @param lidMigrationService the LID migration service that receives the
     *                            decoded chunks
     * @throws NullPointerException if any argument is {@code null}
     */
    public WebHistorySyncService(WhatsAppClient whatsapp, LidMigrationService lidMigrationService) {
        this.whatsapp = Objects.requireNonNull(whatsapp, "whatsapp cannot be null");
        this.lidMigrationService = Objects.requireNonNull(lidMigrationService, "lidMigrationService cannot be null");
    }

    /**
     * Schedules the asynchronous processing of a history-sync notification.
     *
     * <p>The actual download, decryption and decoding happen on a dedicated
     * virtual thread so that the caller (the message stanza dispatcher) is not
     * blocked waiting for a potentially large CDN fetch.
     *
     * @param notification the history-sync notification to process, may be
     *                     {@code null} in which case the call is a no-op
     * @implNote {@code WAWebApiHistorySyncNotification.enqueueNotification}
     *           similarly defers chunk handling to a background queue.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleHistorySyncNotification", exports = "default",
            adaptation = WhatsAppAdaptation.ADAPTED)
    public void process(HistorySyncNotification notification) {
        if (notification == null) {
            return;
        }
        Thread.startVirtualThread(() -> processSync(notification));
    }

    /**
     * Runs the full download-decrypt-decode-fanout pipeline for a single
     * notification on the current thread. Any failure is logged at warning
     * level and swallowed because history sync is non-fatal: the companion
     * can continue operating on partial history and the primary will retry.
     *
     * @param notification the non-null notification to process
     * @implNote Mirrors the body of
     *           {@code WAWebHandleHistorySyncNotification.default}, minus the
     *           WAM progress events which are emitted separately.
     */
    private void processSync(HistorySyncNotification notification) {
        try {
            var historySync = decode(notification);
            if (historySync == null) {
                return;
            }
            dispatch(historySync);
        } catch (WhatsAppHistorySyncException exception) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "History sync chunk processing failed: {0}", exception.getMessage());
        } catch (RuntimeException exception) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "History sync chunk processing failed", exception);
        }
    }

    /**
     * Resolves a notification to a decoded {@link HistorySync} payload,
     * reading either the inline bootstrap payload or the decrypted CDN blob.
     *
     * @param notification the non-null notification
     * @return the decoded payload, or {@code null} when the notification is
     *         empty (no direct path and no inline payload)
     * @throws WhatsAppHistorySyncException if the CDN download fails or the
     *                                      decoded bytes cannot be parsed as
     *                                      a {@link HistorySync} protobuf
     */
    @WhatsAppWebExport(moduleName = "WAWebDownloadManager", exports = "downloadAndMaybeDecrypt",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private HistorySync decode(HistorySyncNotification notification) {
        // WAWebHandleHistorySyncNotification.default: initialHistBootstrapInlinePayload
        // short-circuits the CDN download when the primary inlined the bootstrap
        // bytes directly in the E2EE message (see the inlineInitialPayloadInE2EeMsg
        // device prop set at pairing time).
        var inlinePayload = notification.initialHistBootstrapInlinePayload().orElse(null);
        if (inlinePayload != null && inlinePayload.length > 0) {
            try (var stream = new InflaterInputStream(new ByteArrayInputStream(inlinePayload))) {
                return decodeBytes(stream);
            } catch (Exception exception) {
                throw new WhatsAppHistorySyncException("Failed to decode inline history bootstrap payload", exception);
            }
        }

        if (notification.directPath().isEmpty() || notification.mediaKey().isEmpty()) {
            // No download URL and no inline payload: nothing to decode. This can
            // legitimately happen for MESSAGE_ACCESS_STATUS or NO_HISTORY markers.
            return null;
        }

        // WAWebDownloadManager.downloadAndMaybeDecrypt: MediaConnection.download
        // streams the ciphertext from the CDN, HMAC-verifies it, AES-CBC decrypts
        // it with keys derived via HKDF("WhatsApp History Keys"), and inflates the
        // resulting zlib-compressed plaintext, all transparently to the caller.
        MediaConnection mediaConnection;
        try {
            mediaConnection = whatsapp.store().awaitMediaConnection();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new WhatsAppHistorySyncException("Interrupted while waiting for media connection", exception);
        }
        try (var stream = mediaConnection.download(notification)) {
            return decodeBytes(stream);
        } catch (Exception exception) {
            throw new WhatsAppHistorySyncException("Failed to download or decode history sync chunk", exception);
        }
    }

    /**
     * Decodes a plaintext history sync byte stream into a {@link HistorySync}
     * payload. Full and light variants share the same wire layout for their
     * common fields, so the decoder always uses the full spec and simply
     * observes empty chat/status lists for light chunks.
     *
     * @param stream the plaintext protobuf stream
     * @return the decoded payload
     * @implNote {@code WAWebBinHistorySync.HistorySync.decode}: the WA Web
     *           protobuf runtime uses a single generated decoder that handles
     *           both shapes.
     */
    @WhatsAppWebExport(moduleName = "WAWebBinHistorySync", exports = "HistorySync",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private HistorySync decodeBytes(InputStream stream) {
        return HistorySync.ofFull(ProtobufInputStream.fromStream(stream));
    }

    /**
     * Fans the decoded chunk out to the registered history-sync listeners and
     * the LID migration service.
     *
     * <p>Listener callbacks run on dedicated virtual threads so that a slow
     * listener cannot block the next chunk or the LID mapping ingest.
     *
     * @param historySync the decoded payload
     * @implNote {@code WAWebHandleHistorySyncChunk.handleHistorySyncChunk}
     *           performs the equivalent fan-out to the WA Web reactive
     *           collections; Cobalt routes through the listener interface.
     */
    @WhatsAppWebExport(moduleName = "WAWebHandleHistorySyncChunk", exports = "handleHistorySyncChunk",
            adaptation = WhatsAppAdaptation.ADAPTED)
    private void dispatch(HistorySync historySync) {
        var store = whatsapp.store();
        var listeners = store.listeners();
        var syncType = historySync.syncType();
        var progressValue = historySync.progress().orElse(0);
        // "recent" in the listener contract means "this percentage tracks the
        // recent-messages stream"; WA Web surfaces the same boolean via the
        // syncType it passes to its UI progress reducer.
        var recent = syncType == HistorySync.HistorySyncType.RECENT;
        var isLast = progressValue >= 100;

        for (var listener : listeners) {
            Thread.startVirtualThread(() -> listener.onWebHistorySyncProgress(whatsapp, progressValue, recent));
        }

        for (var chat : historySync.chats()) {
            for (var listener : listeners) {
                Thread.startVirtualThread(() -> listener.onWebHistorySyncMessages(whatsapp, chat, isLast));
            }
        }

        for (var pastParticipants : historySync.pastParticipants()) {
            var groupJid = pastParticipants.groupJid().orElse(null);
            if (groupJid == null) {
                continue;
            }
            var participants = pastParticipants.pastParticipants();
            for (var listener : listeners) {
                Thread.startVirtualThread(() -> listener.onWebHistorySyncPastParticipants(whatsapp, groupJid, participants));
            }
        }

        // WAWebLid1X1ThreadAccountMigrations.setLidMigrationMappings: the
        // history-sync variant of the mapping ingest consumes the top-level
        // phoneNumberToLidMappings list and the per-chat LID fields.
        lidMigrationService.processHistorySync(historySync);
    }
}
