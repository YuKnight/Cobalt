package com.github.auties00.cobalt.model.sync.action.chat;


import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.sync.SyncActionArgs;

/**
 * Index arguments for {@link InteractiveMessageAction}.
 *
 * <p>The sync index produced is
 * {@code ["interactive_message_action", remote, id, fromMe, participant, interactionId]}
 * where the first four elements form a standard message-key and the fifth
 * identifies the specific interactive element (e.g. a CTA button) within
 * that message.
 *
 * @param remote        the remote chat JID
 * @param id            the message identifier
 * @param fromMe        whether the message was sent by the current user
 * @param participant   the participant JID, or {@code null} if not applicable
 * @param interactionId the identifier of the interactive element (e.g. CTA button) within the message
 */
public record InteractiveMessageActionArgs(Jid remote, String id, boolean fromMe, Jid participant, String interactionId) implements SyncActionArgs {
    /**
     * {@inheritDoc}
     *
     * @return a five-element array encoding the message key followed by the interaction identifier
     */
    @Override
    public String[] toIndexArgs() {
        return new String[]{
                remote.toString(),
                id,
                fromMe ? "1" : "0",
                participant != null && !fromMe ? participant.toString() : "0",
                interactionId
        };
    }
}
