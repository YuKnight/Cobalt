package com.github.auties00.cobalt.sync.handler;

import com.github.auties00.cobalt.client.WhatsAppClient;
import com.github.auties00.cobalt.model.sync.SyncPatchType;
import com.github.auties00.cobalt.model.sync.action.media.NoteEditAction;
import com.github.auties00.cobalt.sync.crypto.DecryptedMutation;

/**
 * Handles note edit actions.
 *
 * <p>This handler processes mutations for chat notes. Per WhatsApp Web
 * (WAWebNoteSync), only SET is supported; REMOVE is unsupported. On SET:
 * <ul>
 * <li>If {@code deleted} is {@code true}, the note is removed from the
 *     NoteTable and NoteCollection by its id (index[1]).
 * <li>Otherwise, the web client validates that type and chatJid are present,
 *     resolves the chat, determines the note type (unstructured vs structured),
 *     and upserts the note with id, type, chatJid, content (unstructuredContent),
 *     modifiedAt (mutation timestamp / 1000), and createdAt (action createdAt / 1000).
 * </ul>
 *
 * <p>Since the store has no note storage, this handler is a no-op.
 *
 * <p>Index format: ["note_edit", noteId]
 */
public final class NoteEditHandler implements WebAppStateActionHandler {
    /**
     * The singleton instance of {@code NoteEditHandler}.
     */
    public static final NoteEditHandler INSTANCE = new NoteEditHandler();

    private NoteEditHandler() {

    }

    @Override
    public String actionName() {
        return NoteEditAction.ACTION_NAME;
    }

    @Override
    public SyncPatchType collectionName() {
        return NoteEditAction.COLLECTION_NAME;
    }

    @Override
    public int version() {
        return NoteEditAction.ACTION_VERSION;
    }

    /**
     * Applies a note edit mutation.
     *
     * <p>Per WhatsApp Web, on SET with deleted=true the note (by id from index[1])
     * is removed. On SET without deleted, the web client validates type and chatJid,
     * resolves the chat, and upserts the note with its content and timestamps into
     * IndexedDB and the NoteCollection. Only SET is supported.
     *
     * <p>No-op: the store has no note storage.
     *
     * @param client   the WhatsAppClient instance linked to the mutation
     * @param mutation the mutation to apply
     * @return {@code true} always
     */
    @Override
    public boolean applyMutation(WhatsAppClient client, DecryptedMutation.Trusted mutation) {
        // Web manages notes in WAWebSchemaNote/WAWebNoteCollection (IndexedDB).
        // No equivalent storage exists in the store.
        return true;
    }
}
