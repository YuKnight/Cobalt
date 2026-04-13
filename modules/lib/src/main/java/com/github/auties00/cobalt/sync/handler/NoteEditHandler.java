package com.github.auties00.cobalt.sync.handler;

import com.alibaba.fastjson2.JSON;
import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.MutationApplicationResult;
import com.github.auties00.cobalt.model.sync.SyncActionState;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.NoteEditAction;
import com.github.auties00.cobalt.model.sync.action.media.NoteEditActionBuilder;
import com.github.auties00.cobalt.model.sync.data.SyncdOperation;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Handles note edit sync actions.
 *
 * <p>Per WhatsApp Web {@code WAWebNoteSync}, this handler extends
 * {@code AccountSyncdActionBase} and processes the {@code "note_edit"} action.
 * It supports only SET operations. The per-mutation flow:
 * <ol>
 *   <li>Extract {@code indexParts[1]} as the note id ({@code s})</li>
 *   <li>Read the {@code noteEditAction} from the sync action value</li>
 *   <li>If {@code deleted === true}, remove the note by id and return {@code Success}</li>
 *   <li>Validate {@code type}, {@code chatJid} are non-null</li>
 *   <li>Validate {@code chatJid} via {@code validateChatJid}</li>
 *   <li>Resolve the chat via {@code resolveChatForMutationIndex}; if missing, return {@code Orphan}</li>
 *   <li>Resolve the canonical chat jid {@code L = widToChatJid(createWid(chat.id))}</li>
 *   <li>Resolve the note id {@code E = resolveNoteId(validatedChatJid, L, s)}:
 *       the original {@code s} when {@code L === validatedChatJid}, otherwise
 *       {@code sha256Str(L)} (SHA-256 hex encoded via Base64)</li>
 *   <li>Build the note record and call {@code addOrEditNote}</li>
 *   <li>Return {@code Success}</li>
 * </ol>
 *
 * <p>After processing the batch, WA Web calls
 * {@code frontendFireAndForget("removeNotes", ...)} and
 * {@code frontendFireAndForget("upsertNotesFromSyncd", ...)}. Cobalt's store
 * mutations are applied directly, so the frontend dispatch is omitted.
 *
 * @implNote WAWebNoteSync.default — singleton instance of the handler class that
 *           extends {@code WAWebSyncdAction.AccountSyncdActionBase}
 */
public final class NoteEditHandler implements WebAppStateActionHandler {
    /**
     * Logger for this handler.
     *
     * @implNote ADAPTED: WAWebNoteSync uses WALogger.WARN; Cobalt uses java.util.logging
     */
    private static final Logger LOGGER = Logger.getLogger(NoteEditHandler.class.getName()); // ADAPTED: WAWebNoteSync — WALogger

    /**
     * Singleton instance of this handler.
     *
     * <p>Per WhatsApp Web {@code WAWebNoteSync}, the module creates a single
     * instance ({@code var h = new g()}) and exports it as the default export.
     *
     * @implNote WAWebNoteSync — {@code var h = new g(); l.default = h}
     */
    public static final NoteEditHandler INSTANCE = new NoteEditHandler();

    /**
     * Constructs the singleton note edit handler.
     *
     * @implNote WAWebNoteSync — private constructor for singleton pattern
     */
    private NoteEditHandler() {

    }

    /**
     * Returns the action type name this handler processes.
     *
     * @implNote WAWebNoteSync.getAction — returns
     *           {@code WASyncdConst.Actions.NoteEdit} ({@code "note_edit"})
     * @return the action type name
     */
    @Override
    public String actionName() {
        return NoteEditAction.ACTION_NAME; // WAWebNoteSync.getAction
    }

    /**
     * Returns the sync collection this handler's action belongs to.
     *
     * @implNote WAWebNoteSync constructor — sets {@code collectionName}
     *           to {@code WASyncdConst.CollectionName.RegularLow}
     * @return the sync patch type / collection name
     */
    @Override
    public SyncPatchType collectionName() {
        return NoteEditAction.COLLECTION_NAME; // WAWebNoteSync constructor: collectionName = RegularLow
    }

    /**
     * Returns the mutation format version for this handler.
     *
     * @implNote WAWebNoteSync.getVersion — returns {@code 7}
     * @return the handler's supported mutation version
     */
    @Override
    public int version() {
        return NoteEditAction.ACTION_VERSION; // WAWebNoteSync.getVersion
    }

    /**
     * Applies a single note edit mutation to local state.
     *
     * <p>Delegates to {@link #applyMutationResult(WhatsAppClient, DecryptedMutation.Trusted)}
     * and returns {@code true} if the result is {@link SyncActionState#SUCCESS}.
     *
     * @implNote WAWebNoteSync.applyMutations — per-mutation application logic
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return {@code true} if the mutation was applied successfully, {@code false} otherwise
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        return applyMutationResult(client, mutation).actionState() == SyncActionState.SUCCESS; // WAWebNoteSync.applyMutations
    }

    /**
     * Applies a note edit mutation and returns a detailed result.
     *
     * <p>Per WhatsApp Web {@code WAWebNoteSync.applyMutations}, the per-mutation
     * logic performs the following steps, wrapped in a try/catch that yields
     * {@link SyncActionState#FAILED} on any exception:
     * <ol>
     *   <li>If operation is not SET, return {@code Unsupported}</li>
     *   <li>Extract {@code s = indexParts[1]} and validate it is non-empty;
     *       otherwise return {@code malformedActionIndex()}</li>
     *   <li>Read {@code u = value.noteEditAction}; if missing, return
     *       {@code malformedActionValue(collectionName)}</li>
     *   <li>If {@code u.deleted === true}, remove the note by id and return {@code Success}</li>
     *   <li>Validate {@code u.type}, {@code u.chatJid} are non-null;
     *       {@code validateChatJid(u.chatJid)} is non-null</li>
     *   <li>Resolve the chat via {@code resolveChatForMutationIndex}; if missing,
     *       return {@code Orphan} with the orphan model</li>
     *   <li>Compute the resolved canonical chat jid {@code L = widToChatJid(createWid(chat.id))}</li>
     *   <li>Compute the resolved note id {@code E = resolveNoteId(validatedChatJid, L, s)}</li>
     *   <li>Build the note record and call {@code addOrEditNote}</li>
     *   <li>Return {@code Success}</li>
     * </ol>
     *
     * @implNote WAWebNoteSync.applyMutations — the per-mutation handler within
     *           the Promise.all mapping function
     * @param client   the WhatsApp client instance
     * @param mutation the mutation to apply
     * @return the detailed application result
     */
    @Override
    public MutationApplicationResult applyMutationResult(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        try { // WAWebNoteSync.applyMutations: try { ... } catch(e) { return {actionState: Failed} }
            // WAWebNoteSync.applyMutations: if (e.operation !== "set") return a++, {actionState: Unsupported}
            if (mutation.operation() != SyncdOperation.SET) {
                return MutationApplicationResult.unsupported();
            }

            // WAWebNoteSync.applyMutations: var t = e.indexParts, n = e.value, s = t[1]
            var indexArray = JSON.parseArray(mutation.index());
            if (indexArray.size() < 2) {
                return malformedActionIndex(); // WAWebNoteSync.applyMutations: if (!s) return r.malformedActionIndex()
            }
            var noteId = indexArray.getString(1); // WAWebNoteSync.applyMutations: var s = t[1]
            if (noteId == null || noteId.isEmpty()) {
                return malformedActionIndex(); // WAWebNoteSync.applyMutations: if (!s) return r.malformedActionIndex()
            }

            // WAWebNoteSync.applyMutations: var u = n.noteEditAction; if (!u) return i++, malformedActionValue(collectionName)
            if (!(mutation.value().action().orElse(null) instanceof NoteEditAction action)) {
                return malformedActionValue();
            }

            var states = new HashMap<>(client.store().noteStates());

            // WAWebNoteSync.applyMutations: if (u.deleted === true) {
            //     yield getNoteTable().remove(s); v.push(s); return {actionState: Success}
            // }
            if (action.deleted()) {
                states.remove(noteId); // WAWebNoteSync.applyMutations: getNoteTable().remove(s)
                client.store().setNoteStates(states);
                return MutationApplicationResult.success(); // WAWebNoteSync.applyMutations: {actionState: Success}
            }

            // WAWebNoteSync.applyMutations: var c = u.chatJid, d = u.createdAt, m = u.type, p = u.unstructuredContent
            var type = action.type().orElse(null); // WAWebNoteSync.applyMutations: m = u.type
            var rawChatJid = action.chatJid().orElse(null); // WAWebNoteSync.applyMutations: c = u.chatJid
            var content = action.unstructuredContent().orElse(null); // WAWebNoteSync.applyMutations: p = u.unstructuredContent
            var createdAtOpt = action.createdAt(); // WAWebNoteSync.applyMutations: d = u.createdAt

            // WAWebNoteSync.applyMutations: if (m == null) return l++, malformedActionValue(collectionName)
            if (type == null) {
                return malformedActionValue();
            }
            // WAWebNoteSync.applyMutations: if (c == null) return g++, malformedActionValue(collectionName)
            if (rawChatJid == null) {
                return malformedActionValue();
            }
            // WAWebNoteSync.applyMutations: var _ = validateChatJid(c); if (_ == null) return h++, malformedActionValue(collectionName)
            // ADAPTED: WAWebNoteSync.applyMutations — validateChatJid(c); Cobalt uses Jid.of which is more lenient than validateChatJid
            var validatedChatJid = rawChatJid;

            // WAWebNoteSync.applyMutations: d == null && y++ (counter only, not a failure)
            if (createdAtOpt.isEmpty()) {
                LOGGER.warning("noteEditAction.createdAt is empty"); // WAWebNoteSync.applyMutations: WALogger.WARN("noteEditAction.createdAt is empty for %d mutations", y)
            }
            // WAWebNoteSync.applyMutations:
            //   var f = maybeNumber(d); d != null && f == null && C++
            // ADAPTED: WAWebNoteSync.applyMutations — WALongInt.maybeNumber converts BigInt-safe numbers;
            // Cobalt's Long is already 64-bit so no conversion / safe-int check is needed.

            // WAWebNoteSync.applyMutations: p == null && b++ (counter only, not a failure)
            if (content == null) {
                LOGGER.warning("noteEditAction.unstructuredContent is empty"); // WAWebNoteSync.applyMutations: WALogger.WARN("noteEditAction.unstructuredContent is empty for %d mutations", b)
            }

            // WAWebNoteSync.applyMutations: var R = yield resolveChatForMutationIndex(createWid(c))
            // if (!R.success) return {actionState: Orphan, orphanModel: R.orphanModel}
            // ADAPTED: WAWebNoteSync.applyMutations — Cobalt uses findChatByJid
            var chat = client.store().findChatByJid(validatedChatJid);
            if (chat.isEmpty()) {
                return MutationApplicationResult.orphan(validatedChatJid.toString(), "Chat"); // WAWebNoteSync.applyMutations: {actionState: Orphan, orphanModel: R.orphanModel}
            }

            // WAWebNoteSync.applyMutations: var L = widToChatJid(createWid(R.chat.id))
            // ADAPTED: Cobalt's Chat.jid() already returns the canonical chat JID
            var resolvedChatJid = chat.get().jid();

            // WAWebNoteSync.applyMutations: var E = yield r.resolveNoteId(_, L, s)
            var resolvedNoteId = resolveNoteId(validatedChatJid, resolvedChatJid, noteId);

            // WAWebNoteSync.applyMutations: var k = {
            //     id: E,
            //     type: m === UNSTRUCTURED ? "unstructured" : "structured",
            //     chatJid: L,
            //     content: p != null ? p : "",
            //     modifiedAt: Math.floor(e.timestamp / 1000),
            //     createdAt: Math.floor((f != null ? f : 0) / 1000)
            // }
            // ADAPTED: WAWebNoteSync.applyMutations — WA Web stores a flattened note record
            // ({id, type, chatJid, content, modifiedAt, createdAt}) in the NoteTable IDB. Cobalt
            // stores the NoteEditAction itself in the flat noteStates map keyed by the resolved
            // note id, preserving the action's type enum and createdAt in native units (ms).
            var record = new NoteEditActionBuilder()
                    .type(type) // WAWebNoteSync.applyMutations: type: m === UNSTRUCTURED ? "unstructured" : "structured"
                    .chatJid(resolvedChatJid) // WAWebNoteSync.applyMutations: chatJid: L
                    .createdAt(createdAtOpt.orElse(0L)) // WAWebNoteSync.applyMutations: createdAt: Math.floor((f != null ? f : 0) / 1000)
                    .deleted(false) // ADAPTED: explicit false for the non-deleted branch
                    .unstructuredContent(content != null ? content : "") // WAWebNoteSync.applyMutations: content: p != null ? p : ""
                    .build();

            // WAWebNoteSync.applyMutations: yield addOrEditNote(k); S.push(k)
            states.put(resolvedNoteId, record);
            client.store().setNoteStates(states);

            return MutationApplicationResult.success(); // WAWebNoteSync.applyMutations: {actionState: Success}
        } catch (Exception e) {
            // WAWebNoteSync.applyMutations: catch(e) { return {actionState: Failed} }
            LOGGER.warning("Note edit mutation failed: " + e.getMessage()); // ADAPTED: WAWebNoteSync — log instead of silent catch
            return MutationApplicationResult.failed();
        }
    }

    /**
     * Resolves the note id used as the store key for a note edit mutation.
     *
     * <p>Per WhatsApp Web {@code WAWebNoteSync.resolveNoteId}:
     * <pre>{@code
     * resolveNoteId(e, t, n) {
     *     return t === e ? n : sha256Str(t);
     * }
     * }</pre>
     * where {@code e} is the action's validated chat jid, {@code t} is the
     * resolved canonical chat jid from the store, and {@code n} is the note id
     * from the mutation index. If both jids match, the original note id is used;
     * otherwise the note id is derived from the SHA-256 of the resolved chat jid
     * (base64-encoded).
     *
     * @implNote WAWebNoteSync.resolveNoteId — {@code t === e ? n : generateNoteId(t)}
     *           where {@code generateNoteId} is {@code WAWebNotesIdUtils.generateNoteId}
     *           which delegates to {@code WACryptoSha256.sha256Str} (SHA-256, base64)
     * @param actionChatJid the validated chat jid from the action payload
     * @param resolvedChatJid the canonical chat jid resolved from the store
     * @param indexNoteId the note id from the mutation index
     * @return the resolved note id used as the store key
     */
    private String resolveNoteId(Jid actionChatJid, Jid resolvedChatJid, String indexNoteId) {
        // WAWebNoteSync.resolveNoteId: return t === e ? n : generateNoteId(t)
        if (resolvedChatJid.equals(actionChatJid)) {
            return indexNoteId;
        }
        return generateNoteId(resolvedChatJid.toString()); // WAWebNotesIdUtils.generateNoteId -> WACryptoSha256.sha256Str
    }

    /**
     * Generates a note id by computing the SHA-256 hash of the given string.
     *
     * <p>Per WhatsApp Web {@code WAWebNotesIdUtils.generateNoteId}:
     * <pre>{@code
     * generateNoteId(e) { return sha256Str(e); }
     * }</pre>
     * where {@code sha256Str} produces SHA-256 bytes of the UTF-8 code points and
     * then base64-encodes them via {@code WABase64.encodeB64}.
     *
     * @implNote WAWebNotesIdUtils.generateNoteId — {@code sha256Str(e)};
     *           {@code WACryptoSha256.sha256Str} hashes the UTF-8 bytes and
     *           {@code WABase64.encodeB64} produces the standard base64 encoding
     * @param input the string to hash
     * @return the base64-encoded SHA-256 hash of the input
     * @throws IllegalStateException if the SHA-256 algorithm is unavailable on the JVM
     */
    private String generateNoteId(String input) {
        try {
            var digest = MessageDigest.getInstance("SHA-256"); // WACryptoSha256.sha256
            var hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash); // WABase64.encodeB64
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
