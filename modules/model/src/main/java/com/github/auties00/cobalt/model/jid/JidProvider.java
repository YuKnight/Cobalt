package com.github.auties00.cobalt.model.jid;

import com.github.auties00.cobalt.model.chat.Chat;
import com.github.auties00.cobalt.model.contact.Contact;
import com.github.auties00.cobalt.model.newsletter.Newsletter;

/**
 * Provides a uniform way to obtain a {@link Jid} from any WhatsApp model type that is
 * addressable through the WhatsApp protocol.
 *
 * <p>Several types in the WhatsApp domain carry an associated JID, such as contacts,
 * chats, newsletters, and server descriptors. This sealed interface unifies access to
 * the underlying {@code Jid} by requiring each implementor to expose a {@link #toJid()}
 * method. API consumers can therefore accept a {@code JidProvider} wherever a JID is
 * needed, freeing callers from having to adapt between the concrete model types.
 *
 * <p>The permitted implementors are {@link Contact}, {@link Chat}, {@link Newsletter},
 * {@link Jid} (which returns itself) and {@link JidServer} (which returns a server-only
 * JID for its domain).
 */
public sealed interface JidProvider permits Contact, Jid, JidServer, Chat, Newsletter {
    /**
     * Returns the {@link Jid} that addresses this object in the WhatsApp protocol.
     *
     * @return the non-null {@code Jid} for this object
     */
    Jid toJid();
}
