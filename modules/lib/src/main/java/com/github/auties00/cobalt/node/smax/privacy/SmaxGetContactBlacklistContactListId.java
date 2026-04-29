package com.github.auties00.cobalt.node.smax.privacy;

import com.github.auties00.cobalt.meta.annotation.WhatsAppWebExport;
import com.github.auties00.cobalt.meta.annotation.WhatsAppWebModule;
import com.github.auties00.cobalt.meta.model.WhatsAppAdaptation;
import com.github.auties00.cobalt.model.jid.Jid;
import com.github.auties00.cobalt.model.jid.JidServer;
import com.github.auties00.cobalt.node.Node;
import com.github.auties00.cobalt.node.NodeBuilder;
import com.github.auties00.cobalt.node.smax.SmaxOperation;
import com.github.auties00.cobalt.node.smax.util.SmaxIqErrorResponseMixin;
import com.github.auties00.cobalt.node.smax.util.SmaxIqResultResponseMixin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Sealed family of {@code contactListIds} discriminators projected by
 * each {@code <user/>} child of the LID success variant.
 *
 * @implNote {@code WASmaxInPrivacyContactListIds.parseContactListIds}
 *           is a disjunction over {@code UsernameMixin},
 *           {@code PnJidMixin}, and {@code EmptyContactListIdentifierMixin};
 *           Cobalt projects the disjunction as this sealed family.
 */
public sealed interface SmaxGetContactBlacklistContactListId
        permits SmaxGetContactBlacklistContactListId.Username, SmaxGetContactBlacklistContactListId.PnJid, SmaxGetContactBlacklistContactListId.Empty {

    /**
     * The {@code Username} discriminator — the {@code <user/>} child
     * carries a {@code username="…"} attribute identifying the entry by
     * its WhatsApp username rather than by JID.
     *
     * @implNote {@code WASmaxInPrivacyUsernameMixin.parseUsernameMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivacyUsernameMixin")
    final class Username implements SmaxGetContactBlacklistContactListId {
        /**
         * The WhatsApp username string.
         */
        private final String username;

        /**
         * Constructs a {@code Username} discriminator.
         *
         * @param username the username string; never {@code null}
         * @throws NullPointerException if {@code username} is
         *                              {@code null}
         */
        public Username(String username) {
            this.username = Objects.requireNonNull(username, "username cannot be null");
        }

        /**
         * Returns the WhatsApp username.
         *
         * @return the username string; never {@code null}
         */
        public String username() {
            return username;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (Username) obj;
            return Objects.equals(this.username, that.username);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username);
        }

        @Override
        public String toString() {
            return "SmaxGetContactBlacklistContactListId.Username[username=" + username + ']';
        }
    }

    /**
     * The {@code PnJid} discriminator — the {@code <user/>} child carries
     * a {@code pn_jid="…"} attribute echoing the legacy PN JID associated
     * with the LID-addressed entry.
     *
     * @implNote {@code WASmaxInPrivacyPnJidMixin.parsePnJidMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivacyPnJidMixin")
    final class PnJid implements SmaxGetContactBlacklistContactListId {
        /**
         * The echoed legacy PN JID.
         */
        private final Jid pnJid;

        /**
         * Constructs a {@code PnJid} discriminator.
         *
         * @param pnJid the echoed PN JID; never {@code null}
         * @throws NullPointerException if {@code pnJid} is {@code null}
         */
        public PnJid(Jid pnJid) {
            this.pnJid = Objects.requireNonNull(pnJid, "pnJid cannot be null");
        }

        /**
         * Returns the echoed legacy PN JID.
         *
         * @return the PN JID; never {@code null}
         */
        public Jid pnJid() {
            return pnJid;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (PnJid) obj;
            return Objects.equals(this.pnJid, that.pnJid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pnJid);
        }

        @Override
        public String toString() {
            return "SmaxGetContactBlacklistContactListId.PnJid[pnJid=" + pnJid + ']';
        }
    }

    /**
     * The {@code Empty} discriminator — the {@code <user/>} child carries
     * neither {@code username} nor {@code pn_jid}, indicating the relay
     * has no PN echo for this LID-addressed entry.
     *
     * @implNote {@code WASmaxInPrivacyEmptyContactListIdentifierMixin.parseEmptyContactListIdentifierMixin}.
     */
    @WhatsAppWebModule(moduleName = "WASmaxInPrivacyEmptyContactListIdentifierMixin")
    final class Empty implements SmaxGetContactBlacklistContactListId {
        /**
         * Constructs an {@code Empty} discriminator.
         */
        public Empty() {
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            return obj != null && obj.getClass() == this.getClass();
        }

        @Override
        public int hashCode() {
            return Empty.class.hashCode();
        }

        @Override
        public String toString() {
            return "SmaxGetContactBlacklistContactListId.Empty[]";
        }
    }
}
